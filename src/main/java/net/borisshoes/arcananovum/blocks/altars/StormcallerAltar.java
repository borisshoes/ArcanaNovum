package net.borisshoes.arcananovum.blocks.altars;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.core.MagicBlock;
import net.borisshoes.arcananovum.core.Multiblock;
import net.borisshoes.arcananovum.core.MultiblockCore;
import net.borisshoes.arcananovum.core.polymer.MagicPolymerBlockEntity;
import net.borisshoes.arcananovum.core.polymer.MagicPolymerBlockItem;
import net.borisshoes.arcananovum.items.ArcaneTome;
import net.borisshoes.arcananovum.recipes.arcana.MagicItemIngredient;
import net.borisshoes.arcananovum.recipes.arcana.MagicItemRecipe;
import net.borisshoes.arcananovum.utils.MagicRarity;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
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
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class StormcallerAltar extends MagicBlock implements MultiblockCore {
   
   private Multiblock multiblock;
   
   public StormcallerAltar(){
      id = "stormcaller_altar";
      name = "Altar of the Stormcaller";
      rarity = MagicRarity.EXOTIC;
      categories = new ArcaneTome.TomeFilter[]{ArcaneTome.TomeFilter.EXOTIC, ArcaneTome.TomeFilter.BLOCKS, ArcaneTome.TomeFilter.ALTARS};
      itemVersion = 0;
      vanillaItem = Items.RAW_COPPER_BLOCK;
      block = new StormcallerAltarBlock(FabricBlockSettings.create().mapColor(MapColor.ORANGE).strength(5.0f,1200.0f).requiresTool().sounds(BlockSoundGroup.METAL));
      item = new StormcallerAltarItem(this.block,new FabricItemSettings().maxCount(1).fireproof());
      
      ItemStack stack = new ItemStack(item);
      NbtCompound tag = stack.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList loreList = new NbtList();
      NbtList enchants = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name","[{\"text\":\"Altar of the Stormcaller\",\"italic\":false,\"color\":\"aqua\",\"bold\":true}]");
      loreList.add(NbtString.of("[{\"text\":\"Altars \",\"italic\":false,\"color\":\"aqua\"},{\"text\":\"are \",\"color\":\"blue\"},{\"text\":\"multiblock structures\",\"color\":\"dark_purple\"},{\"text\":\" that must be \",\"color\":\"blue\"},{\"text\":\"built \",\"color\":\"dark_purple\"},{\"text\":\"in the world.\",\"color\":\"blue\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Left click a block\",\"italic\":false,\"color\":\"aqua\"},{\"text\":\" with an \",\"color\":\"blue\"},{\"text\":\"Altar \"},{\"text\":\"to see a \",\"color\":\"blue\"},{\"text\":\"hologram \",\"color\":\"dark_purple\"},{\"text\":\"of the \",\"color\":\"blue\"},{\"text\":\"structure\",\"color\":\"dark_purple\"},{\"text\":\".\",\"color\":\"blue\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Right click\",\"italic\":false,\"color\":\"aqua\"},{\"text\":\" a \",\"color\":\"blue\"},{\"text\":\"completed \",\"color\":\"dark_purple\"},{\"text\":\"Altar \"},{\"text\":\"setup to \",\"color\":\"blue\"},{\"text\":\"activate \",\"color\":\"dark_purple\"},{\"text\":\"the \",\"color\":\"blue\"},{\"text\":\"Altar\"},{\"text\":\".\",\"color\":\"blue\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Stormcaller Altar:\",\"italic\":false,\"color\":\"aqua\",\"bold\":true},{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\",\"bold\":false}]"));
      loreList.add(NbtString.of("[{\"text\":\"The \",\"italic\":false,\"color\":\"gray\"},{\"text\":\"Altar \",\"color\":\"aqua\"},{\"text\":\"calls upon the \"},{\"text\":\"clouds \",\"color\":\"dark_gray\"},{\"text\":\"to \"},{\"text\":\"shift \",\"color\":\"aqua\"},{\"text\":\"and \"},{\"text\":\"darken\",\"color\":\"dark_gray\"},{\"text\":\".\",\"color\":\"gray\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"The \",\"italic\":false,\"color\":\"gray\"},{\"text\":\"Altar \",\"color\":\"aqua\"},{\"text\":\"can be used to \"},{\"text\":\"change \",\"color\":\"dark_gray\"},{\"text\":\"the \"},{\"text\":\"weather \",\"color\":\"aqua\"},{\"text\":\"to any state.\",\"color\":\"gray\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"The \",\"italic\":false,\"color\":\"gray\"},{\"text\":\"Altar \",\"color\":\"aqua\"},{\"text\":\"requires a \"},{\"text\":\"Diamond Block\",\"color\":\"aqua\"},{\"text\":\" to \"},{\"text\":\"activate\",\"color\":\"dark_gray\"},{\"text\":\".\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Exotic \",\"italic\":false,\"color\":\"aqua\",\"bold\":true},{\"text\":\"Magic Item\",\"color\":\"dark_purple\",\"bold\":false}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
      tag.put("Enchantments",enchants);
      
      setBookLore(makeLore());
      setRecipe(makeRecipe());
      prefNBT = addMagicNbt(tag);
      
      stack.setNbt(prefNBT);
      prefItem = stack;
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
      MagicItemIngredient a = new MagicItemIngredient(Items.LIGHTNING_ROD,64,null);
      MagicItemIngredient b = new MagicItemIngredient(Items.OXIDIZED_COPPER,64,null);
      MagicItemIngredient c = new MagicItemIngredient(Items.DIAMOND,8,null);
      MagicItemIngredient h = new MagicItemIngredient(Items.HEART_OF_THE_SEA,1,null);
      MagicItemIngredient m = new MagicItemIngredient(Items.RAW_COPPER_BLOCK,64,null);
      
      MagicItemIngredient[][] ingredients = {
            {a,b,c,b,a},
            {b,c,h,c,b},
            {c,h,m,h,c},
            {b,c,h,c,b},
            {a,b,c,b,a}};
      return new MagicItemRecipe(ingredients);
   }
   
   private List<String> makeLore(){
      ArrayList<String> list = new ArrayList<>();
      list.add("{\"text\":\"      Altar of the\\n      Stormcaller\\n\\nRarity: Exotic\\n\\nIn order to influence the world, I need to tap into the leylines. I have devised a structure capable of such, with this at its heart. It should be able to cause changes in the \"}");
      list.add("{\"text\":\"      Altar of the\\n      Stormcaller\\n\\natmosphere so that I can dictate the weather. \\nThunder, Rain or Shine, at my command!\\nHowever, in order to provide enough energy to the leylines, I need to use a Diamond Block as a catalyst.\"}");
      return list;
   }
   
   public class StormcallerAltarItem extends MagicPolymerBlockItem {
      public StormcallerAltarItem(Block block, Settings settings){
         super(getThis(),block, settings);
      }
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
   }
   
   public class StormcallerAltarBlock extends MagicPolymerBlockEntity {
      public StormcallerAltarBlock(Settings settings){
         super(settings);
      }
      
      @Override
      public Block getPolymerBlock(BlockState state) {
         return Blocks.RAW_COPPER_BLOCK;
      }
      
      @Nullable
      public static StormcallerAltarBlockEntity getEntity(World world, BlockPos pos) {
         BlockState state = world.getBlockState(pos);
         if (!(state.getBlock() instanceof StormcallerAltarBlock)) {
            return null;
         }
         return world.getBlockEntity(pos) instanceof StormcallerAltarBlockEntity altar ? altar : null;
      }
      
      @Override
      public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
         return new StormcallerAltarBlockEntity(pos, state);
      }
      
      @Nullable
      @Override
      public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
         return checkType(type, ArcanaRegistry.STORMCALLER_ALTAR_BLOCK_ENTITY, StormcallerAltarBlockEntity::ticker);
      }
      
      @Override
      public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity playerEntity, Hand hand, BlockHitResult hit){
         if(hand != Hand.MAIN_HAND) return ActionResult.PASS;
         StormcallerAltarBlockEntity altar = (StormcallerAltarBlockEntity) world.getBlockEntity(pos);
         if(altar != null){
            if(playerEntity instanceof ServerPlayerEntity player){
               Multiblock.MultiblockCheck check = new Multiblock.MultiblockCheck(player.getServerWorld(),pos,state,new BlockPos(-5,0,-5),null);
               if(multiblock.matches(check)){
                  altar.openGui(player);
                  player.getItemCooldownManager().set(playerEntity.getStackInHand(hand).getItem(),1);
               }else{
                  player.sendMessage(Text.literal("Multiblock not constructed."));
                  multiblock.displayStructure(check);
               }
            }
         }
         return ActionResult.SUCCESS;
      }
      
      @Override
      public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
         if (world.getBlockEntity(pos) instanceof StormcallerAltarBlockEntity altar) {
            dropBlockItem(world, pos, state, player, altar);
            world.removeBlockEntity(pos);
         }
         
         world.removeBlock(pos, false);
         
         super.onBreak(world, pos, state, player);
      }
      
      @Override
      public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
         BlockEntity entity = world.getBlockEntity(pos);
         if (placer instanceof ServerPlayerEntity player && entity instanceof StormcallerAltarBlockEntity altar) {
            initializeMagicBlock(stack,altar);
         }
      }
   }
}
