package net.borisshoes.arcananovum.blocks.forge;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.core.MagicBlock;
import net.borisshoes.arcananovum.core.Multiblock;
import net.borisshoes.arcananovum.core.MultiblockCore;
import net.borisshoes.arcananovum.core.polymer.MagicPolymerBlockEntity;
import net.borisshoes.arcananovum.core.polymer.MagicPolymerBlockItem;
import net.borisshoes.arcananovum.items.ArcaneTome;
import net.borisshoes.arcananovum.recipes.arcana.GenericMagicIngredient;
import net.borisshoes.arcananovum.recipes.arcana.MagicItemIngredient;
import net.borisshoes.arcananovum.recipes.arcana.MagicItemRecipe;
import net.borisshoes.arcananovum.utils.MagicRarity;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class RadiantFletchery extends MagicBlock implements MultiblockCore {
   
   private Multiblock multiblock;
   
   public RadiantFletchery(){
      id = "radiant_fletchery";
      name = "Radiant Fletchery";
      rarity = MagicRarity.EMPOWERED;
      categories = new ArcaneTome.TomeFilter[]{ArcaneTome.TomeFilter.EMPOWERED, ArcaneTome.TomeFilter.BLOCKS, ArcaneTome.TomeFilter.FORGE};
      itemVersion = 0;
      vanillaItem = Items.FLETCHING_TABLE;
      block = new RadiantFletcheryBlock(FabricBlockSettings.create().strength(2.5f,1200.0f).sounds(BlockSoundGroup.WOOD));
      item = new RadiantFletcheryItem(this.block,new FabricItemSettings().maxCount(1).fireproof());
      
      ItemStack stack = new ItemStack(item);
      NbtCompound tag = stack.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList enchants = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name","[{\"text\":\"Radiant Fletchery\",\"italic\":false,\"color\":\"yellow\",\"bold\":true}]");
      tag.put("display",display);
      tag.put("Enchantments",enchants);
      buildItemLore(stack, ArcanaNovum.SERVER);
      
      setBookLore(makeLore());
      setRecipe(makeRecipe());
      prefNBT = addMagicNbt(tag);
      
      stack.setNbt(prefNBT);
      prefItem = stack;
   }
   
   @Override
   public NbtList getItemLore(@Nullable ItemStack itemStack){
      NbtList loreList = new NbtList();
      loreList.add(NbtString.of("[{\"text\":\"A \",\"italic\":false,\"color\":\"light_purple\"},{\"text\":\"Forge Structure\",\"color\":\"yellow\"},{\"text\":\" addon to the \"},{\"text\":\"Starlight Forge\",\"color\":\"yellow\"},{\"text\":\".\",\"color\":\"light_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"The \",\"italic\":false,\"color\":\"light_purple\"},{\"text\":\"Fletchery \",\"color\":\"yellow\"},{\"text\":\"enables \"},{\"text\":\"efficient \",\"color\":\"dark_purple\"},{\"text\":\"creation of \"},{\"text\":\"tipped arrows\",\"color\":\"yellow\"},{\"text\":\".\",\"color\":\"light_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"The \",\"italic\":false,\"color\":\"light_purple\"},{\"text\":\"Fletchery \",\"color\":\"yellow\"},{\"text\":\"also \"},{\"text\":\"unlocks \",\"color\":\"dark_purple\"},{\"text\":\"the ability to make \"},{\"text\":\"Runic Arrows\",\"color\":\"dark_purple\"},{\"text\":\".\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Forge Structures:\",\"italic\":false,\"color\":\"light_purple\",\"bold\":true},{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\",\"bold\":false}]"));
      loreList.add(NbtString.of("[{\"text\":\"Are \",\"italic\":false,\"color\":\"dark_purple\"},{\"text\":\"multiblock structures\",\"color\":\"light_purple\"},{\"text\":\" that must be \"},{\"text\":\"built\",\"color\":\"aqua\"},{\"text\":\" in the \"},{\"text\":\"world\",\"color\":\"dark_aqua\"},{\"text\":\".\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Must \",\"italic\":false,\"color\":\"dark_aqua\"},{\"text\":\"be \",\"color\":\"dark_purple\"},{\"text\":\"placed \",\"color\":\"aqua\"},{\"text\":\"within a \",\"color\":\"dark_purple\"},{\"text\":\"17x11x17\"},{\"text\":\" \",\"color\":\"dark_purple\"},{\"text\":\"cube around a \",\"color\":\"dark_purple\"},{\"text\":\"Starlight Forge\",\"color\":\"light_purple\"},{\"text\":\".\",\"color\":\"dark_purple\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Right Click\",\"italic\":false,\"color\":\"dark_aqua\"},{\"text\":\" a \",\"color\":\"dark_purple\"},{\"text\":\"completed \",\"color\":\"aqua\"},{\"text\":\"Forge Structure\",\"color\":\"light_purple\"},{\"text\":\" to \",\"color\":\"dark_purple\"},{\"text\":\"use\",\"color\":\"aqua\"},{\"text\":\" it.\",\"color\":\"dark_purple\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Right Click\",\"italic\":false,\"color\":\"dark_aqua\"},{\"text\":\" a \",\"color\":\"dark_purple\"},{\"text\":\"Forge Structure\",\"color\":\"light_purple\"},{\"text\":\" to see a \",\"color\":\"dark_purple\"},{\"text\":\"hologram \",\"color\":\"aqua\"},{\"text\":\"of the \",\"color\":\"dark_purple\"},{\"text\":\"structure\",\"color\":\"light_purple\"},{\"text\":\".\",\"color\":\"dark_purple\"}]"));
      
      return loreList;
   }
   
   @Override
   public void loadMultiblock(){
      multiblock = Multiblock.loadFromFile(getId());
   }
   
   @Override
   public Multiblock getMultiblock(){
      return multiblock;
   }
   
   private MagicItemRecipe makeRecipe(){
      MagicItemIngredient a = new MagicItemIngredient(Items.SPECTRAL_ARROW,64,null);
      MagicItemIngredient b = new MagicItemIngredient(Items.STRING,64,null);
      MagicItemIngredient c = new MagicItemIngredient(Items.END_CRYSTAL,32,null);
      MagicItemIngredient d = new MagicItemIngredient(Items.BLAZE_POWDER,64,null);
      MagicItemIngredient h = new MagicItemIngredient(Items.FLETCHING_TABLE,64,null);
      GenericMagicIngredient m = new GenericMagicIngredient(ArcanaRegistry.RUNIC_MATRIX,1);
      
      MagicItemIngredient[][] ingredients = {
            {a,b,c,d,a},
            {b,a,h,a,d},
            {c,h,m,h,c},
            {d,a,h,a,b},
            {a,d,c,b,a}};
      return new MagicItemRecipe(ingredients);
   }
   
   private List<String> makeLore(){
      ArrayList<String> list = new ArrayList<>();
      list.add("{\"text\":\"   Radiant Fletchery\\n\\nRarity: Empowered\\n\\nI have yet to put my Runic Matrix to good use, fortunately this might be my chance.\\nThe Matrix should be able to take on the effect of potions to boost the amount of Tipped Arrows I can make from a single \"}");
      list.add("{\"text\":\"   Radiant Fletchery\\n\\nPotion. \\nThe Arrows themselves could be an excellent candidate for use of the Matrix once I master more of its capabilities. Perhaps if I make an arrow out of a Matrix it could activate powerful effects upon hitting a target.\"}");
      list.add("{\"text\":\"   Radiant Fletchery\\n\\nThe Fletchery boosts the amount of Tipped Arrows made per potion, as well as allowing normal potions to be used.\\n\\nThe Fletchery also unlocks a host of Archery related recipes for the Forge\"}");
      return list;
   }
   
   public class RadiantFletcheryItem extends MagicPolymerBlockItem {
      public RadiantFletcheryItem(Block block, Settings settings){
         super(getThis(),block, settings);
      }
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
   }
   
   public class RadiantFletcheryBlock extends MagicPolymerBlockEntity {
      public RadiantFletcheryBlock(Settings settings){
         super(settings);
      }
      
      @Override
      public Block getPolymerBlock(BlockState state) {
         return Blocks.FLETCHING_TABLE;
      }
      
      @Nullable
      public static RadiantFletcheryBlockEntity getEntity(World world, BlockPos pos) {
         BlockState state = world.getBlockState(pos);
         if (!(state.getBlock() instanceof RadiantFletcheryBlock)) {
            return null;
         }
         return world.getBlockEntity(pos) instanceof RadiantFletcheryBlockEntity fletchery ? fletchery : null;
      }
      
      @Override
      public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
         return new RadiantFletcheryBlockEntity(pos, state);
      }
      
      @Nullable
      @Override
      public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
         return checkType(type, ArcanaRegistry.RADIANT_FLETCHERY_BLOCK_ENTITY, RadiantFletcheryBlockEntity::ticker);
      }
      
      @Override
      public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity playerEntity, Hand hand, BlockHitResult hit){
         if(hand != Hand.MAIN_HAND) return ActionResult.PASS;
         RadiantFletcheryBlockEntity fletchery = (RadiantFletcheryBlockEntity) world.getBlockEntity(pos);
         if(fletchery != null){
            if(playerEntity instanceof ServerPlayerEntity player){
               if(fletchery.isAssembled()){
                  if(StarlightForge.findActiveForge(player.getServerWorld(),pos) == null){
                     player.sendMessage(Text.literal("The Fletchery must be within the range of an active Starlight Forge"));
                  }else{
                     fletchery.openGui(player);
                     player.getItemCooldownManager().set(playerEntity.getStackInHand(hand).getItem(),1);
                  }
               }else{
                  player.sendMessage(Text.literal("Multiblock not constructed."));
                  multiblock.displayStructure(fletchery.getMultiblockCheck());
               }
            }
         }
         return ActionResult.SUCCESS;
      }
      
      @Override
      public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
         if (world.getBlockEntity(pos) instanceof RadiantFletcheryBlockEntity fletchery) {
            dropBlockItem(world, pos, state, player, fletchery);
            
            DefaultedList<ItemStack> drops = DefaultedList.of();
            for(ItemStack stack : fletchery.getInventory()){
               drops.add(stack.copy());
            }
            
            ItemScatterer.spawn(world, pos.up(), drops);
            
            world.removeBlockEntity(pos);
         }
         
         world.removeBlock(pos, false);
         
         super.onBreak(world, pos, state, player);
      }
      
      @Override
      public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
         BlockEntity entity = world.getBlockEntity(pos);
         if (placer instanceof ServerPlayerEntity player && entity instanceof RadiantFletcheryBlockEntity fletchery) {
            initializeMagicBlock(stack,fletchery);
         }
      }
   }
}
