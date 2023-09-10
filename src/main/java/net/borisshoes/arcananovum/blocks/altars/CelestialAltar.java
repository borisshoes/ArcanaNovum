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
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CelestialAltar extends MagicBlock implements MultiblockCore {
   
   private Multiblock multiblock;
   
   public CelestialAltar(){
      id = "celestial_altar";
      name = "Celestial Altar";
      rarity = MagicRarity.EXOTIC;
      categories = new ArcaneTome.TomeFilter[]{ArcaneTome.TomeFilter.EXOTIC, ArcaneTome.TomeFilter.BLOCKS, ArcaneTome.TomeFilter.ALTARS};
      itemVersion = 0;
      vanillaItem = Items.PEARLESCENT_FROGLIGHT;
      block = new CelestialAltarBlock(FabricBlockSettings.create().mapColor(MapColor.PINK).strength(.3f).luminance(state -> 15).sounds(BlockSoundGroup.FROGLIGHT));
      item = new CelestialAltarItem(this.block,new FabricItemSettings().maxCount(1).fireproof());
      
      ItemStack stack = new ItemStack(item);
      NbtCompound tag = stack.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList loreList = new NbtList();
      NbtList enchants = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name","[{\"text\":\"Celestial Altar\",\"italic\":false,\"color\":\"blue\",\"bold\":true}]");
      loreList.add(NbtString.of("[{\"text\":\"Altars \",\"italic\":false,\"color\":\"aqua\"},{\"text\":\"are \",\"color\":\"blue\"},{\"text\":\"multiblock structures\",\"color\":\"dark_purple\"},{\"text\":\" that must be \",\"color\":\"blue\"},{\"text\":\"built \",\"color\":\"dark_purple\"},{\"text\":\"in the world.\",\"color\":\"blue\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Left click a block\",\"italic\":false,\"color\":\"aqua\"},{\"text\":\" with an \",\"color\":\"blue\"},{\"text\":\"Altar \"},{\"text\":\"to see a \",\"color\":\"blue\"},{\"text\":\"hologram \",\"color\":\"dark_purple\"},{\"text\":\"of the \",\"color\":\"blue\"},{\"text\":\"structure\",\"color\":\"dark_purple\"},{\"text\":\".\",\"color\":\"blue\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Right click\",\"italic\":false,\"color\":\"aqua\"},{\"text\":\" a \",\"color\":\"blue\"},{\"text\":\"completed \",\"color\":\"dark_purple\"},{\"text\":\"Altar \"},{\"text\":\"setup to \",\"color\":\"blue\"},{\"text\":\"activate \",\"color\":\"dark_purple\"},{\"text\":\"the \",\"color\":\"blue\"},{\"text\":\"Altar\"},{\"text\":\".\",\"color\":\"blue\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Celestial Altar:\",\"italic\":false,\"color\":\"blue\",\"bold\":true},{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\",\"bold\":false}]"));
      loreList.add(NbtString.of("[{\"text\":\"The \",\"italic\":false,\"color\":\"dark_gray\"},{\"text\":\"Altar \",\"color\":\"blue\"},{\"text\":\"glistens \",\"color\":\"yellow\"},{\"text\":\"in the \"},{\"text\":\"light \",\"color\":\"yellow\"},{\"text\":\"of the \"},{\"text\":\"Sun \",\"color\":\"yellow\"},{\"text\":\"and \"},{\"text\":\"Moon\",\"color\":\"blue\"},{\"text\":\".\",\"color\":\"dark_gray\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"The \",\"italic\":false,\"color\":\"dark_gray\"},{\"text\":\"Altar \",\"color\":\"blue\"},{\"text\":\"lets you change the \"},{\"text\":\"time of day\",\"color\":\"yellow\"},{\"text\":\" \",\"color\":\"yellow\"},{\"text\":\"and the \"},{\"text\":\"phase\",\"color\":\"blue\"},{\"text\":\" of the \"},{\"text\":\"Moon\",\"color\":\"blue\"},{\"text\":\".\",\"color\":\"dark_gray\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"The \",\"italic\":false,\"color\":\"dark_gray\"},{\"text\":\"Altar \",\"color\":\"blue\"},{\"text\":\"requires a \"},{\"text\":\"Nether Star\",\"color\":\"yellow\"},{\"text\":\" to \"},{\"text\":\"activate\",\"color\":\"blue\"},{\"text\":\".\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
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
      MagicItemIngredient p = new MagicItemIngredient(Items.GLOWSTONE,64,null);
      MagicItemIngredient a = new MagicItemIngredient(Items.CRYING_OBSIDIAN,64,null);
      MagicItemIngredient b = new MagicItemIngredient(Items.LAPIS_BLOCK,16,null);
      MagicItemIngredient r = new MagicItemIngredient(Items.GOLD_BLOCK,16,null);
      MagicItemIngredient d = new MagicItemIngredient(Items.SEA_LANTERN,64,null);
      MagicItemIngredient g = new MagicItemIngredient(Items.OBSIDIAN,64,null);
      MagicItemIngredient l = new MagicItemIngredient(Items.NETHER_STAR,8,null);
      MagicItemIngredient m = new MagicItemIngredient(Items.PEARLESCENT_FROGLIGHT,64,null);
      
      MagicItemIngredient[][] ingredients = {
            {a,b,b,d,d},
            {b,g,b,d,d},
            {g,l,m,l,g},
            {p,p,r,g,r},
            {p,p,r,r,a}};
      return new MagicItemRecipe(ingredients);
   }
   
   private List<String> makeLore(){
      ArrayList<String> list = new ArrayList<>();
      list.add("{\"text\":\"     Celestial Altar\\n\\nRarity: Exotic\\n\\nLeylines across the world have their influence extend into the orbit of the planet. If I can provide a sufficient energy source to the structure, I should be able to accelerate the planet, or the moon! \"}");
      list.add("{\"text\":\"     Celestial Altar\\n\\nA Nether Star should be sufficient to let this altar change the time of day and the phase of the moon by accelerating the planet and moon's orbital periods for a brief moment before the Star is depleted and the orbits normalize.\"}");
      return list;
   }
   
   public class CelestialAltarItem extends MagicPolymerBlockItem {
      public CelestialAltarItem(Block block, Settings settings){
         super(getThis(),block, settings);
      }
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
   }
   
   public class CelestialAltarBlock extends MagicPolymerBlockEntity {
      public static final DirectionProperty HORIZONTAL_FACING = Properties.HORIZONTAL_FACING;
      public CelestialAltarBlock(Settings settings){
         super(settings);
      }
      
      @Override
      public Block getPolymerBlock(BlockState state) {
         return Blocks.PEARLESCENT_FROGLIGHT;
      }
      
      @Nullable
      public static CelestialAltarBlockEntity getEntity(World world, BlockPos pos) {
         BlockState state = world.getBlockState(pos);
         if (!(state.getBlock() instanceof CelestialAltarBlock)) {
            return null;
         }
         return world.getBlockEntity(pos) instanceof CelestialAltarBlockEntity altar ? altar : null;
      }
      
      @Nullable
      @Override
      public BlockState getPlacementState(ItemPlacementContext ctx) {
         return this.getDefaultState().with(HORIZONTAL_FACING,ctx.getHorizontalPlayerFacing().getOpposite());
      }
      
      @Override
      protected void appendProperties(StateManager.Builder<Block, BlockState> stateManager) {
         stateManager.add(HORIZONTAL_FACING);
      }
      
      @Override
      public BlockState rotate(BlockState state, BlockRotation rotation) {
         return state.with(HORIZONTAL_FACING, rotation.rotate(state.get(HORIZONTAL_FACING)));
      }
      
      @Override
      public BlockState mirror(BlockState state, BlockMirror mirror) {
         return state.rotate(mirror.getRotation(state.get(HORIZONTAL_FACING)));
      }
      
      @Override
      public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
         return new CelestialAltarBlockEntity(pos, state);
      }
      
      @Nullable
      @Override
      public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
         return checkType(type, ArcanaRegistry.CELESTIAL_ALTAR_BLOCK_ENTITY, CelestialAltarBlockEntity::ticker);
      }
      
      @Override
      public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity playerEntity, Hand hand, BlockHitResult hit){
         if(hand != Hand.MAIN_HAND) return ActionResult.PASS;
         CelestialAltarBlockEntity altar = (CelestialAltarBlockEntity) world.getBlockEntity(pos);
         if(altar != null){
            if(playerEntity instanceof ServerPlayerEntity player){
               Multiblock.MultiblockCheck check = new Multiblock.MultiblockCheck(player.getServerWorld(),pos,state,new BlockPos(-5,0,-5),world.getBlockState(pos).get(HORIZONTAL_FACING));
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
         if (world.getBlockEntity(pos) instanceof CelestialAltarBlockEntity altar) {
            dropBlockItem(world, pos, state, player, altar);
            world.removeBlockEntity(pos);
         }
         
         world.removeBlock(pos, false);
         
         super.onBreak(world, pos, state, player);
      }
      
      @Override
      public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
         BlockEntity entity = world.getBlockEntity(pos);
         if (placer instanceof ServerPlayerEntity player && entity instanceof CelestialAltarBlockEntity altar) {
            initializeMagicBlock(stack,altar);
         }
      }
   }
}