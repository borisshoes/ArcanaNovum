package net.borisshoes.arcananovum.items;

import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.EnergyItem;
import net.borisshoes.arcananovum.core.MagicItemContainer;
import net.borisshoes.arcananovum.core.polymer.MagicPolymerItem;
import net.borisshoes.arcananovum.recipes.arcana.MagicItemIngredient;
import net.borisshoes.arcananovum.recipes.arcana.MagicItemRecipe;
import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.borisshoes.arcananovum.utils.MagicRarity;
import net.borisshoes.arcananovum.utils.SoundUtils;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.*;
import net.minecraft.nbt.*;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Clearable;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;

public class ChestTranslocator extends EnergyItem implements MagicItemContainer.MagicItemContainerHaver{
   
   private static final String CHEST_TXT = "item/chest_translocator_chest";
   private static final String BARREL_TXT = "item/chest_translocator_barrel";
   private static final String EMPTY_TXT = "item/chest_translocator_empty";
   
   public ChestTranslocator(){
      id = "chest_translocator";
      name = "Chest Translocator";
      rarity = MagicRarity.EMPOWERED;
      categories = new ArcaneTome.TomeFilter[]{ArcaneTome.TomeFilter.EMPOWERED, ArcaneTome.TomeFilter.ITEMS};
      itemVersion = 0;
      vanillaItem = Items.SPRUCE_BOAT;
      item = new ChestTranslocatorItem(new FabricItemSettings().maxCount(1).fireproof());
      models = new ArrayList<>();
      models.add(new Pair<>(Items.SPRUCE_CHEST_BOAT,BARREL_TXT));
      models.add(new Pair<>(Items.SPRUCE_CHEST_BOAT,CHEST_TXT));
      models.add(new Pair<>(vanillaItem,EMPTY_TXT));
      
      ItemStack stack = new ItemStack(item);
      NbtCompound tag = stack.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList enchants = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name","[{\"text\":\"Chest Translocator\",\"italic\":false,\"bold\":true,\"color\":\"gold\"}]");
      tag.put("display",display);
      tag.put("Enchantments",enchants);
      
      setBookLore(makeLore());
      setRecipe(makeRecipe());
      addMagicNbt(tag);
      tag.getCompound("arcananovum").put("contents",new NbtCompound());
      tag.getCompound("arcananovum").put("state",new NbtCompound());
      stack.setNbt(tag);
      setPrefStack(stack);
   }
   
   @Override
   public NbtList getItemLore(@Nullable ItemStack itemStack){
      NbtList loreList = new NbtList();
      loreList.add(NbtString.of("[{\"text\":\"Moving items\",\"italic\":false,\"color\":\"dark_red\"},{\"text\":\" from one \",\"color\":\"gray\"},{\"text\":\"chest \",\"color\":\"gold\"},{\"text\":\"to \",\"color\":\"gray\"},{\"text\":\"another \",\"color\":\"gold\"},{\"text\":\"is a \",\"color\":\"gray\"},{\"text\":\"hassle\",\"color\":\"red\"},{\"text\":\".\",\"color\":\"gray\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Chests\",\"italic\":false,\"color\":\"gold\"},{\"text\":\" are \",\"color\":\"gray\"},{\"text\":\"heavy\",\"color\":\"red\"},{\"text\":\" and carrying them \",\"color\":\"gray\"},{\"text\":\"slows \",\"color\":\"red\"},{\"text\":\"you down \",\"color\":\"gray\"},{\"text\":\"significantly\",\"color\":\"dark_red\"},{\"text\":\".\",\"color\":\"gray\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Sneak Right Click\",\"italic\":false,\"color\":\"dark_red\"},{\"text\":\" a \",\"color\":\"gray\"},{\"text\":\"chest \",\"color\":\"gold\"},{\"text\":\"to \",\"color\":\"gray\"},{\"text\":\"store \",\"color\":\"red\"},{\"text\":\"it in the \",\"color\":\"gray\"},{\"text\":\"Translocator\",\"color\":\"gold\"},{\"text\":\".\",\"color\":\"gray\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Right Click\",\"italic\":false,\"color\":\"dark_red\"},{\"text\":\" to \",\"color\":\"gray\"},{\"text\":\"place \",\"color\":\"red\"},{\"text\":\"a \",\"color\":\"gray\"},{\"text\":\"stored \",\"color\":\"red\"},{\"text\":\"chest\",\"color\":\"gold\"},{\"text\":\" down.\",\"color\":\"gray\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      
      return loreList;
   }
   
   @Override
   public int getMaxEnergy(ItemStack item){
      int cdLvl = Math.max(0, ArcanaAugments.getAugmentOnItem(item,ArcanaAugments.RAPID_TRANSLOCATION.id));
      return 30 - 8*cdLvl;
   }
   
   @Override
   public boolean blocksHandInteractions(ItemStack item){
      return true;
   }
   
   @Override
   public MagicItemContainer getMagicItemContainer(ItemStack item){
      NbtCompound itemNbt = item.getNbt();
      NbtCompound magicNbt = itemNbt.getCompound("arcananovum");
      NbtCompound contents = magicNbt.getCompound("contents");
      SimpleInventory inv = new SimpleInventory(27);
      for(int i = 0; i < inv.size(); i++){
         inv.setStack(i,ItemStack.EMPTY.copy());
      }
      
      if(!contents.isEmpty()){
         NbtList items = contents.getList("Items", NbtElement.COMPOUND_TYPE);
         
         for(int i = 0; i < items.size(); i++){
            NbtCompound stack = items.getCompound(i);
            ItemStack itemStack = ItemStack.fromNbt(stack);
            inv.setStack(stack.getByte("Slot"),itemStack);
         }
      }
      
      return new MagicItemContainer(inv, 27,4, "CT", "Chest Translocator", 0.5);
   }
   
   @Override
   public ItemStack updateItem(ItemStack stack, MinecraftServer server){
      NbtCompound itemNbt = stack.getNbt();
      NbtCompound magicTag = itemNbt.getCompound("arcananovum");
      NbtCompound contents = magicTag.getCompound("contents");
      NbtCompound state = magicTag.getCompound("state");
      NbtCompound newTag = super.updateItem(stack,server).getNbt();
      newTag.getCompound("arcananovum").put("contents",contents);
      newTag.getCompound("arcananovum").put("state",state);
      stack.setNbt(newTag);
      return buildItemLore(stack,server);
   }
   
   private MagicItemRecipe makeRecipe(){
      MagicItemIngredient a = new MagicItemIngredient(Items.BARREL,32,null);
      MagicItemIngredient b = new MagicItemIngredient(Items.ENDER_EYE,16,null);
      MagicItemIngredient c = new MagicItemIngredient(Items.TRAPPED_CHEST,32,null);
      MagicItemIngredient g = new MagicItemIngredient(Items.CHEST,32,null);
      ItemStack potion7 = new ItemStack(Items.POTION);
      MagicItemIngredient h = new MagicItemIngredient(Items.POTION,1, PotionUtil.setPotion(potion7, Potions.STRONG_STRENGTH).getNbt());
      MagicItemIngredient m = new MagicItemIngredient(Items.ENDER_CHEST,16,null);
      
      MagicItemIngredient[][] ingredients = {
            {a,b,c,b,a},
            {b,g,h,g,b},
            {c,h,m,h,c},
            {b,g,h,g,b},
            {a,b,c,b,a}};
      return new MagicItemRecipe(ingredients);
   }
   
   private List<String> makeLore(){
      ArrayList<String> list = new ArrayList<>();
      list.add("{\"text\":\"  Chest Translocator\\n\\nRarity: Empowered\\n\\nShulker Boxes are amazing, I love them so much. Why can't all chests be like Shulker Boxes?\\nMaybe I can do something about that, using an augmented Ender Chest and some additional strength.\"}");
      list.add("{\"text\":\"  Chest Translocator\\n\\nUsing the Translocator on a Chest, Trapped Chest, or Barrel will pick it up at the cost of a significant movement speed debuff.\\n\\nUsing the Translocator again places the container down, contents intact.\"}");
      return list;
   }
   
   public class ChestTranslocatorItem extends MagicPolymerItem {
      public ChestTranslocatorItem(Settings settings){
         super(getThis(),settings);
      }
      
      @Override
      public int getPolymerCustomModelData(ItemStack itemStack, @Nullable ServerPlayerEntity player){
         if(!MagicItemUtils.isMagic(itemStack)) return ArcanaRegistry.MODELS.get(EMPTY_TXT).value();
         NbtCompound itemNbt = itemStack.getNbt();
         NbtCompound magicNbt = itemNbt.getCompound("arcananovum");
         NbtCompound contents = magicNbt.getCompound("contents");
         NbtCompound stateTag = magicNbt.getCompound("state");
         if(!contents.isEmpty()){
            BlockState blockState = NbtHelper.toBlockState(Registries.BLOCK.getReadOnlyWrapper(),stateTag);
            if(blockState.isOf(Blocks.CHEST) || blockState.isOf(Blocks.TRAPPED_CHEST)){
               return ArcanaRegistry.MODELS.get(CHEST_TXT).value();
            }else if(blockState.isOf(Blocks.BARREL)){
               return ArcanaRegistry.MODELS.get(BARREL_TXT).value();
            }
         }
         return ArcanaRegistry.MODELS.get(EMPTY_TXT).value();
      }
      
      @Override
      public ActionResult useOnBlock(ItemUsageContext context){
         ItemStack stack = context.getStack();
         PlayerEntity playerEntity = context.getPlayer();
         if(!MagicItemUtils.isMagic(stack) || !(playerEntity instanceof ServerPlayerEntity player)) return ActionResult.PASS;
         
         World world = context.getWorld();
         BlockPos blockPos = context.getBlockPos();
         NbtCompound itemNbt = stack.getNbt();
         NbtCompound magicNbt = itemNbt.getCompound("arcananovum");
         NbtCompound contents = magicNbt.getCompound("contents");
         NbtCompound stateTag = magicNbt.getCompound("state");
         int cooldown = getEnergy(stack);
         BlockState state = world.getBlockState(blockPos);
         
         if(contents.isEmpty()){
            if(state.isOf(Blocks.CHEST) || state.isOf(Blocks.TRAPPED_CHEST) || state.isOf(Blocks.BARREL)){
                 if(cooldown == 0){
                    BlockEntity be = world.getBlockEntity(blockPos);
                    if(be == null) return ActionResult.PASS;
                    NbtCompound contentData = be.createNbtWithId();
                    magicNbt.put("contents",contentData);
                    magicNbt.put("state",NbtHelper.fromBlockState(state));
                    Clearable.clear(be);
                    world.setBlockState(blockPos,Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
                    SoundUtils.playSound(world,blockPos,SoundEvents.BLOCK_WOOD_BREAK, SoundCategory.BLOCKS, 1,1);
                    setEnergy(stack,getMaxEnergy(stack));
                 }else{
                    player.sendMessage(Text.literal("Translocator Cooldown: "+cooldown+(cooldown != 1 ? " seconds" : " second")).formatted(Formatting.GOLD), true);
                    SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_FIRE_EXTINGUISH, 1, .5f);
                 }
            }else{
               return ActionResult.PASS;
            }
         }else{
            Direction side = context.getSide();
            BlockPos placePos = blockPos.add(side.getVector());
            if(world.getBlockState(placePos).isAir()){
               BlockState blockState = NbtHelper.toBlockState(Registries.BLOCK.getReadOnlyWrapper(),stateTag);
               ItemPlacementContext ipc = new ItemPlacementContext(player,context.getHand(),context.getStack(),new BlockHitResult(context.getHitPos(),context.getSide(),context.getBlockPos(),context.hitsInsideBlock()));
               if(blockState.isOf(Blocks.CHEST)){
                  blockState = Blocks.CHEST.getPlacementState(ipc);
               }else if(blockState.isOf(Blocks.TRAPPED_CHEST)){
                  blockState = Blocks.TRAPPED_CHEST.getPlacementState(ipc);
               }
               world.setBlockState(placePos,blockState,Block.NOTIFY_ALL);
               BlockEntity blockEntity = world.getBlockEntity(placePos);
               if(blockEntity != null){
                  blockEntity.readNbt(contents);
                  if(blockEntity instanceof LootableContainerBlockEntity container){
                     int size = container.size();
                     int filled = 0;
                     for(int i = 0; i < size; i++){
                        filled += container.getStack(i).isEmpty() ? 0 : 1;
                     }
                     if(filled == size){
                        ArcanaAchievements.progress(player,ArcanaAchievements.STORAGE_RELOCATION.id, 1);
                     }else if(filled == 0){
                        ArcanaAchievements.grant(player,ArcanaAchievements.PEAK_LAZINESS.id);
                     }
                  }
               }
               
               magicNbt.put("state",new NbtCompound());
               magicNbt.put("contents",new NbtCompound());
               PLAYER_DATA.get(player).addXP(50); // Add xp
               SoundUtils.playSound(world,placePos,SoundEvents.BLOCK_WOOD_PLACE, SoundCategory.BLOCKS, 1,1);
            }else{
               player.sendMessage(Text.literal("The chest cannot be placed here.").formatted(Formatting.RED,Formatting.ITALIC),true);
               SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_FIRE_EXTINGUISH, 1,1);
            }
         }
         return ActionResult.SUCCESS;
      }
      
      @Override
      public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected){
         if(!MagicItemUtils.isMagic(stack)) return;
         if(!(world instanceof ServerWorld serverWorld && entity instanceof ServerPlayerEntity player)) return;
         NbtCompound itemNbt = stack.getNbt();
         NbtCompound magicNbt = itemNbt.getCompound("arcananovum");
         NbtCompound contents = magicNbt.getCompound("contents");
         
         if(!contents.isEmpty()){
            StatusEffectInstance slow = new StatusEffectInstance(StatusEffects.SLOWNESS, 20, 2, false, false, true);
            StatusEffectInstance fatigue = new StatusEffectInstance(StatusEffects.MINING_FATIGUE, 20, 0, false, false, true);
            player.addStatusEffect(slow);
            player.addStatusEffect(fatigue);
         }
         
         if(world.getServer().getTicks() % 20 == 0){
            addEnergy(stack, -1); // Recharge
         }
      }
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
      
      @Override
      public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player){
         if(!MagicItemUtils.isMagic(itemStack)) return vanillaItem;
         NbtCompound itemNbt = itemStack.getNbt();
         NbtCompound magicNbt = itemNbt.getCompound("arcananovum");
         NbtCompound contents = magicNbt.getCompound("contents");
         
         return !contents.isEmpty() ? Items.SPRUCE_CHEST_BOAT : vanillaItem;
      }
   }
}
