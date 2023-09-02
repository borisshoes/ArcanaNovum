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
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class StarpathAltar extends MagicBlock implements MultiblockCore {
   
   private Multiblock multiblock;
   
   public StarpathAltar(){
      id = "starpath_altar";
      name = "Starpath Altar";
      rarity = MagicRarity.LEGENDARY;
      categories = new ArcaneTome.TomeFilter[]{ArcaneTome.TomeFilter.LEGENDARY, ArcaneTome.TomeFilter.BLOCKS, ArcaneTome.TomeFilter.ALTARS};
      itemVersion = 0;
      vanillaItem = Items.SCULK_CATALYST;
      block = new StarpathAltarBlock(FabricBlockSettings.create().mapColor(MapColor.BLACK).strength(3.0f,3.0f).luminance(state -> 6).sounds(BlockSoundGroup.SCULK_CATALYST));
      item = new StarpathAltarItem(this.block,new FabricItemSettings().maxCount(1).fireproof());
      
      ItemStack stack = new ItemStack(item);
      NbtCompound tag = stack.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList loreList = new NbtList();
      NbtList enchants = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name","[{\"text\":\"Starpath Altar\",\"italic\":false,\"color\":\"white\",\"bold\":true}]");
      loreList.add(NbtString.of("[{\"text\":\"Altars \",\"italic\":false,\"color\":\"aqua\"},{\"text\":\"are \",\"color\":\"blue\"},{\"text\":\"multiblock structures\",\"color\":\"dark_purple\"},{\"text\":\" that must be \",\"color\":\"blue\"},{\"text\":\"built \",\"color\":\"dark_purple\"},{\"text\":\"in the world.\",\"color\":\"blue\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Left click a block\",\"italic\":false,\"color\":\"aqua\"},{\"text\":\" with an \",\"color\":\"blue\"},{\"text\":\"Altar \"},{\"text\":\"to see a \",\"color\":\"blue\"},{\"text\":\"hologram \",\"color\":\"dark_purple\"},{\"text\":\"of the \",\"color\":\"blue\"},{\"text\":\"structure\",\"color\":\"dark_purple\"},{\"text\":\".\",\"color\":\"blue\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Right click\",\"italic\":false,\"color\":\"aqua\"},{\"text\":\" a \",\"color\":\"blue\"},{\"text\":\"completed \",\"color\":\"dark_purple\"},{\"text\":\"Altar \"},{\"text\":\"setup to \",\"color\":\"blue\"},{\"text\":\"activate \",\"color\":\"dark_purple\"},{\"text\":\"the \",\"color\":\"blue\"},{\"text\":\"Altar\"},{\"text\":\".\",\"color\":\"blue\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Starpath Altar:\",\"italic\":false,\"color\":\"white\",\"bold\":true},{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\",\"bold\":false}]"));
      loreList.add(NbtString.of("[{\"text\":\"The \",\"italic\":false,\"color\":\"dark_gray\"},{\"text\":\"Altar \",\"color\":\"white\"},{\"text\":\"finds a \"},{\"text\":\"path \",\"color\":\"dark_aqua\"},{\"text\":\"through the \"},{\"text\":\"stars \",\"color\":\"white\"},{\"text\":\"to \"},{\"text\":\"anywhere \",\"color\":\"dark_aqua\"},{\"text\":\"in the world.\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"All creatures\",\"italic\":false,\"color\":\"dark_aqua\"},{\"text\":\" standing in the \",\"color\":\"dark_gray\"},{\"text\":\"Altar \",\"color\":\"white\"},{\"text\":\"will be \",\"color\":\"dark_gray\"},{\"text\":\"teleported\"},{\"text\":\".\",\"color\":\"dark_gray\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"The \",\"italic\":false,\"color\":\"dark_gray\"},{\"text\":\"Altar \",\"color\":\"white\"},{\"text\":\"requires \"},{\"text\":\"Eyes of Ender\",\"color\":\"dark_aqua\"},{\"text\":\" to \"},{\"text\":\"activate\",\"color\":\"white\"},{\"text\":\".\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Legendary \",\"italic\":false,\"color\":\"gold\",\"bold\":true},{\"text\":\"Magic Item\",\"color\":\"dark_purple\",\"bold\":false}]"));
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
      MagicItemIngredient a = new MagicItemIngredient(Items.CRYING_OBSIDIAN,64,null);
      MagicItemIngredient b = new MagicItemIngredient(Items.ENDER_EYE,64,null);
      MagicItemIngredient c = new MagicItemIngredient(Items.ENDER_PEARL,16,null);
      MagicItemIngredient g = new MagicItemIngredient(Items.OBSIDIAN,64,null);
      MagicItemIngredient h = new MagicItemIngredient(Items.NETHER_STAR,8,null);
      MagicItemIngredient m = new MagicItemIngredient(Items.SCULK_CATALYST,64,null);
      
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
      list.add("{\"text\":\"    Starpath Altar\\n\\nRarity: Legendary\\n\\nThe leylines flow like rivers through the world, yet they are indistinct and would be impossible to navigate.\\nHowever, the stars above pull on them like the moon on the tide. By charting the stars, I should be able to\"}");
      list.add("{\"text\":\"    Starpath Altar\\n\\nsend teleportation energy through the leylines along a charted course to exactly where I want to go in the world.\\n\\nIt should even be capable of taking a group of creatures all at once to the same destination!\"}");
      list.add("{\"text\":\"    Starpath Altar\\n\\nHowever, an Eye of Ender only contains so much teleportation energy, so the farther I wish to travel, the more Eyes I need to provide up front to create a continuous pathway along the leylines.\"}");
      return list;
   }
   
   public class StarpathAltarItem extends MagicPolymerBlockItem {
      public StarpathAltarItem(Block block, Settings settings){
         super(getThis(),block, settings);
      }
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
   }
   
   public class StarpathAltarBlock extends MagicPolymerBlockEntity {
      public static final BooleanProperty BLOOM = Properties.BLOOM;
      public StarpathAltarBlock(Settings settings){
         super(settings);
      }
      
      @Override
      public Block getPolymerBlock(BlockState state) {
         return Blocks.SCULK_CATALYST;
      }
      
      @Override
      public BlockState getPolymerBlockState(BlockState state){
         return super.getPolymerBlockState(state).with(BLOOM,state.get(BLOOM));
      }
      
      @Nullable
      public static StarpathAltarBlockEntity getEntity(World world, BlockPos pos) {
         BlockState state = world.getBlockState(pos);
         if (!(state.getBlock() instanceof StarpathAltarBlock)) {
            return null;
         }
         return world.getBlockEntity(pos) instanceof StarpathAltarBlockEntity altar ? altar : null;
      }
      
      @Override
      public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
         return new StarpathAltarBlockEntity(pos, state);
      }
      
      @Nullable
      @Override
      public BlockState getPlacementState(ItemPlacementContext ctx) {
         return this.getDefaultState().with(BLOOM,false);
      }
      
      @Override
      protected void appendProperties(StateManager.Builder<Block, BlockState> stateManager) {
         stateManager.add(BLOOM);
      }
      
      @Nullable
      @Override
      public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
         return checkType(type, ArcanaRegistry.STARPATH_ALTAR_BLOCK_ENTITY, StarpathAltarBlockEntity::ticker);
      }
      
      @Override
      public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity playerEntity, Hand hand, BlockHitResult hit){
         if(hand != Hand.MAIN_HAND) return ActionResult.PASS;
         StarpathAltarBlockEntity altar = (StarpathAltarBlockEntity) world.getBlockEntity(pos);
         if(altar != null){
            if(playerEntity instanceof ServerPlayerEntity player){
               Multiblock.MultiblockCheck check = new Multiblock.MultiblockCheck(player.getServerWorld(),pos,state,new BlockPos(-5,0,-5),null);
               if(multiblock.matches(check)){
                  altar.openGui(player);
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
         if (world.getBlockEntity(pos) instanceof StarpathAltarBlockEntity altar) {
            dropBlockItem(world, pos, state, player, altar);
            world.removeBlockEntity(pos);
         }
         
         world.removeBlock(pos, false);
         
         super.onBreak(world, pos, state, player);
      }
      
      @Override
      public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
         BlockEntity entity = world.getBlockEntity(pos);
         if (placer instanceof ServerPlayerEntity player && entity instanceof StarpathAltarBlockEntity altar) {
            initializeMagicBlock(stack,altar);
         }
      }
   }
}
