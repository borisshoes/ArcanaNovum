package net.borisshoes.arcananovum.items;

import net.borisshoes.arcananovum.ArcanaConfig;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerItem;
import net.borisshoes.arcananovum.gui.arcanetome.TomeGui;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaIngredient;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaRecipe;
import net.borisshoes.arcananovum.recipes.arcana.ForgeRequirement;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.borislib.utils.MinecraftUtils;
import net.borisshoes.borislib.utils.SoundUtils;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class SpawnerHarness extends ArcanaItem {
	public static final String ID = "spawner_harness";
   
   public static final String SPAWNER_TAG = "spawner";
   
   public SpawnerHarness(){
      id = ID;
      name = "Spawner Harness";
      rarity = ArcanaRarity.EMPOWERED;
      categories = new TomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), TomeGui.TomeFilter.ITEMS, TomeGui.TomeFilter.BLOCKS};
      itemVersion = 1;
      vanillaItem = Items.SPAWNER;
      item = new SpawnerHarnessItem();
      displayName = Component.translatableWithFallback("item."+MOD_ID+"."+ID,name).withStyle(ChatFormatting.BOLD, ChatFormatting.DARK_AQUA);
      researchTasks = new ResourceKey[]{ResearchTasks.OBTAIN_SILK_TOUCH,ResearchTasks.BREAK_SPAWNER,ResearchTasks.OBTAIN_NETHERITE_INGOT,ResearchTasks.UNLOCK_STELLAR_CORE,ResearchTasks.UNLOCK_MIDNIGHT_ENCHANTER};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getDefaultMaxStackSize());
      putProperty(stack,SPAWNER_TAG,new CompoundTag());
      setPrefStack(stack);
   }
   
   @Override
   public List<Component> getItemLore(@Nullable ItemStack itemStack){
      List<MutableComponent> lore = new ArrayList<>();
      lore.add(Component.literal("")
            .append(Component.literal("While ").withStyle(ChatFormatting.DARK_GREEN))
            .append(Component.literal("Silk Touch").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(" fails to provide adequate finesse to obtain ").withStyle(ChatFormatting.DARK_GREEN))
            .append(Component.literal("spawners").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal(",").withStyle(ChatFormatting.DARK_GREEN)));
      lore.add(Component.literal("")
            .append(Component.literal("through ").withStyle(ChatFormatting.DARK_GREEN))
            .append(Component.literal("magical enhancement").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(" this harness should suffice.").withStyle(ChatFormatting.DARK_GREEN)));
      lore.add(Component.literal("")
            .append(Component.literal("Right click").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(" on a ").withStyle(ChatFormatting.DARK_GREEN))
            .append(Component.literal("mob spawner").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal(" to obtain it as an ").withStyle(ChatFormatting.DARK_GREEN))
            .append(Component.literal("item").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_GREEN)));
      lore.add(Component.literal(""));
      
      String type = "Uncaptured";
      if(itemStack != null){
         CompoundTag spawnerTag = getCompoundProperty(itemStack,SPAWNER_TAG);
         boolean hasSpawner = !spawnerTag.isEmpty();
         
         if(hasSpawner){
            type = "Empty Spawner";
            if(spawnerTag.contains("SpawnData")){
               CompoundTag spawnData = spawnerTag.getCompoundOrEmpty("SpawnData");
               CompoundTag entity = spawnData.getCompoundOrEmpty("entity");
               if(!entity.isEmpty()){
                  String entityTypeId = entity.getStringOr("id", "");
                  Optional<EntityType<?>> entityType = EntityType.byString(entityTypeId);
                  type = entityType.isPresent() ? entityType.get().getDescription().getString() : "Unknown";
               }
            }
         }
      }
      
      lore.add(Component.literal("")
            .append(Component.literal("Type - ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal(type).withStyle(ChatFormatting.DARK_GREEN))
      );
      return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   public ItemStack updateItem(ItemStack stack, MinecraftServer server){
      CompoundTag spawnerNbt = getCompoundProperty(stack,SPAWNER_TAG).copy();
      ItemStack newStack = super.updateItem(stack,server);
      putProperty(newStack,SPAWNER_TAG,spawnerNbt);
      return buildItemLore(newStack,server);
   }
   
   private void giveScrap(Player player){
      ItemStack stack = new ItemStack(Items.NETHERITE_SCRAP);
      int reduction = ArcanaNovum.CONFIG.getInt(ArcanaRegistry.INGREDIENT_REDUCTION);
      int scrapCost = (int) Math.ceil(4.0 / reduction);
      stack.setCount(scrapCost/2);
      MinecraftUtils.giveStacks(player,stack);
   }
   
   @Override
   public List<List<Component>> getBookLore(){
      List<List<Component>> list = new ArrayList<>();
      list.add(List.of(Component.literal(" Spawner Harness").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD), Component.literal("\nRarity: ").withStyle(ChatFormatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(),false)), Component.literal("\nSpawners have always been one of the few blocks that are beyond the reach of the Silk Touch enchantment. Perhaps I can enhance the enchant a bit further by giving the magic a Harness to channel").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal(" Spawner Harness").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD), Component.literal("\nadditional Arcana to. \n\nThe Harness itself has to be incredibly durable to withstand the Arcana driving the enchant into overdrive, however, even with my best efforts, the Harness can break after use.\n").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal(" Spawner Harness").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD), Component.literal("\nUse the Harness on a spawner to capture the spawner.\n\nThe Harness can then place the spawner elsewhere, with a 15% chance to break after use.\n").withStyle(ChatFormatting.BLACK)));
      return list;
   }
   
   @Override
	protected ArcanaRecipe makeRecipe(){
      ArcanaIngredient a = new ArcanaIngredient(Items.CRYING_OBSIDIAN,16);
      ArcanaIngredient b = new ArcanaIngredient(Items.OBSIDIAN,16);
      ArcanaIngredient c = new ArcanaIngredient(Items.ENCHANTED_BOOK,1).withEnchantments(new ArcanaIngredient.EnchantmentEntry(Enchantments.SILK_TOUCH,1));
      ArcanaIngredient g = new ArcanaIngredient(Items.ENDER_EYE,4);
      ArcanaIngredient h = new ArcanaIngredient(Items.IRON_BARS,16);
      ArcanaIngredient m = new ArcanaIngredient(Items.NETHERITE_INGOT,1);
      
      ArcanaIngredient[][] ingredients = {
            {a,b,c,b,a},
            {b,g,h,g,b},
            {c,h,m,h,c},
            {b,g,h,g,b},
            {a,b,c,b,a}};
      return new ArcanaRecipe(this, ingredients,new ForgeRequirement().withAnvil().withEnchanter().withCore());
   }
   
   public class SpawnerHarnessItem extends ArcanaPolymerItem {
      public SpawnerHarnessItem(){
         super(getThis());
      }
      
      @Override
      public ItemStack getPolymerItemStack(ItemStack itemStack, TooltipFlag tooltipType, PacketContext context){
         ItemStack baseStack = super.getPolymerItemStack(itemStack, tooltipType, context);
         if(!ArcanaItemUtils.isArcane(itemStack)) return baseStack;
         
         List<String> stringList = new ArrayList<>();
         CompoundTag spawnerData = getCompoundProperty(itemStack,SPAWNER_TAG);
         boolean hasSpawner = spawnerData.contains("SpawnData");
         
         if(!hasSpawner){
            stringList.add("empty");
         }
         baseStack.set(DataComponents.CUSTOM_MODEL_DATA,new CustomModelData(new ArrayList<>(),new ArrayList<>(),stringList,new ArrayList<>()));
         return baseStack;
      }
      
      @Override
      public ItemStack getDefaultInstance(){
         return prefItem;
      }
      
      @Override
      public InteractionResult useOn(UseOnContext context){
         Level world = context.getLevel();
         Player player = context.getPlayer();
         if(player == null) return InteractionResult.PASS;
         try{
            ItemStack stack = context.getItemInHand();
            CompoundTag spawnerTag = getCompoundProperty(stack,SPAWNER_TAG);
            
            if(!spawnerTag.isEmpty()){ // Has spawner, try to place
               Direction side = context.getClickedFace();
               BlockPos placePos = context.getClickedPos().offset(side.getUnitVec3i());
               if(world.getBlockState(placePos).isAir()){
                  world.setBlock(placePos, Blocks.SPAWNER.defaultBlockState(), Block.UPDATE_ALL);
                  BlockEntity blockEntity = BlockEntity.loadStatic(placePos, Blocks.SPAWNER.defaultBlockState(),spawnerTag,world.registryAccess());
                  if(blockEntity != null){
                     world.setBlockEntity(blockEntity);
                  }
                  
                  boolean reinforced = Math.max(0,ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.REINFORCED_CHASSIS.id)) > 0;
                  if(Math.random() > .15 || reinforced){ // Chance of the harness breaking after use
                     player.displayClientMessage(Component.literal("The harness successfully places the spawner.").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.ITALIC),true);
                     SoundUtils.playSongToPlayer((ServerPlayer) player, SoundEvents.CHAIN_PLACE, 1,.1f);
                     putProperty(stack,SPAWNER_TAG,new CompoundTag());
                     buildItemLore(stack,player.level().getServer());
                  }else{
                     boolean scrap = Math.max(0,ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.SALVAGEABLE_FRAME.id)) > 0;
                     player.displayClientMessage(Component.literal("The harness shatters upon placing the spawner.").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.ITALIC),true);
                     SoundUtils.playSongToPlayer((ServerPlayer) player, SoundEvents.SHIELD_BREAK, 1,.5f);
                     putProperty(stack,SPAWNER_TAG,new CompoundTag());
                     buildItemLore(stack,player.level().getServer());
                     stack.consume(stack.getCount(),player);
                     if(scrap) giveScrap(player);
                  }
                  ArcanaNovum.data(player).addXP((int) Math.max(0, ArcanaNovum.CONFIG.getInt(ArcanaRegistry.SPAWNER_HARNESS_USE)*0.15)); // Add xp
                  return InteractionResult.SUCCESS_SERVER;
               }else{
                  player.displayClientMessage(Component.literal("The harness cannot be placed here.").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC),true);
                  SoundUtils.playSongToPlayer((ServerPlayer) player, SoundEvents.FIRE_EXTINGUISH, 1,1);
               }
            }else if(world.getBlockState(context.getClickedPos()).getBlock() == Blocks.SPAWNER && world.getBlockEntity(context.getClickedPos()) instanceof SpawnerBlockEntity){
               SpawnerBlockEntity spawner = (SpawnerBlockEntity) world.getBlockEntity(context.getClickedPos());
               CompoundTag spawnerNbt = spawner.saveWithFullMetadata(world.registryAccess());
               Entity renderedEntity = spawner.getSpawner().getOrCreateDisplayEntity(world,context.getClickedPos());
               if(renderedEntity != null){
                  String entityTypeId = EntityType.getKey(renderedEntity.getType()).toString();
                  String entityTypeName = EntityType.byString(entityTypeId).get().getDescription().getString();
                  player.displayClientMessage(Component.literal("The harness captures the "+entityTypeName+" spawner.").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.ITALIC),true);
                  if(entityTypeId.equals(EntityType.getKey(EntityType.SILVERFISH).toString())) ArcanaAchievements.grant((ServerPlayer) player,ArcanaAchievements.FINALLY_USEFUL.id);
               }
               
               putProperty(stack,SPAWNER_TAG,spawnerNbt);
               world.destroyBlock(context.getClickedPos(),false);
               
               SoundUtils.playSongToPlayer((ServerPlayer) player, SoundEvents.CHAIN_BREAK, 1,.1f);
               buildItemLore(stack,player.level().getServer());
               
               return InteractionResult.SUCCESS_SERVER;
            }
         }catch (Exception e){
            e.printStackTrace();
         }
         return InteractionResult.PASS;
      }
   }
}

