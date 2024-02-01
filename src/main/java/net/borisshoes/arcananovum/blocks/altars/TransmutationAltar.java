package net.borisshoes.arcananovum.blocks.altars;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.core.*;
import net.borisshoes.arcananovum.core.polymer.MagicPolymerBlockEntity;
import net.borisshoes.arcananovum.core.polymer.MagicPolymerBlockItem;
import net.borisshoes.arcananovum.core.polymer.MagicPolymerItem;
import net.borisshoes.arcananovum.items.ArcaneTome;
import net.borisshoes.arcananovum.recipes.arcana.ForgeRequirement;
import net.borisshoes.arcananovum.recipes.arcana.MagicItemIngredient;
import net.borisshoes.arcananovum.recipes.arcana.MagicItemRecipe;
import net.borisshoes.arcananovum.utils.MagicItemUtils;
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
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class TransmutationAltar extends MagicBlock implements MultiblockCore {
   
   private Multiblock multiblock;
   
   public TransmutationAltar(){
      id = "transmutation_altar";
      name = "Transmutation Altar";
      rarity = MagicRarity.LEGENDARY;
      categories = new ArcaneTome.TomeFilter[]{ArcaneTome.TomeFilter.LEGENDARY,ArcaneTome.TomeFilter.BLOCKS,ArcaneTome.TomeFilter.ALTARS};
      vanillaItem = Items.DIAMOND_BLOCK;
      block = new TransmutationAltarBlock(FabricBlockSettings.create().mapColor(MapColor.DIAMOND_BLUE).strength(5.0f,1200.0f).requiresTool().sounds(BlockSoundGroup.AMETHYST_BLOCK));
      item = new TransmutationAltarItem(this.block,new FabricItemSettings().maxCount(1).fireproof());
      
      ItemStack stack = new ItemStack(item);
      NbtCompound tag = stack.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList enchants = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name","[{\"text\":\"Transmutation Altar\",\"italic\":false,\"bold\":true,\"color\":\"aqua\"}]");
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
      addAltarLore(loreList);
      loreList.add(NbtString.of("[{\"text\":\"Transmutation Altar:\",\"italic\":false,\"bold\":true,\"color\":\"aqua\"},{\"text\":\"\",\"italic\":false,\"bold\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"The \",\"italic\":false,\"color\":\"gray\"},{\"text\":\"Altar\",\"color\":\"aqua\"},{\"text\":\" beckons to an \"},{\"text\":\"ancient entity\",\"color\":\"aqua\"},{\"text\":\" of \"},{\"text\":\"balance\",\"color\":\"blue\"},{\"text\":\" and \"},{\"text\":\"trades\",\"color\":\"blue\"},{\"text\":\".\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"The \",\"italic\":false,\"color\":\"gray\"},{\"text\":\"Altar\",\"color\":\"aqua\"},{\"text\":\" can be called upon to\"},{\"text\":\" \",\"color\":\"aqua\"},{\"text\":\"exchange \",\"color\":\"dark_aqua\"},{\"text\":\"equivalent materials\",\"color\":\"blue\"},{\"text\":\".\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Every \",\"italic\":false,\"color\":\"gray\"},{\"text\":\"barter\",\"color\":\"dark_aqua\"},{\"text\":\" comes with its own \"},{\"text\":\"price.\",\"color\":\"blue\"}]"));
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
      MagicItemIngredient a = new MagicItemIngredient(Items.BLACKSTONE,64,null);
      MagicItemIngredient c = new MagicItemIngredient(Items.AMETHYST_BLOCK,64,null);
      MagicItemIngredient d = new MagicItemIngredient(Items.QUARTZ_BLOCK,64,null);
      MagicItemIngredient g = new MagicItemIngredient(Items.NETHERITE_INGOT,2,null);
      MagicItemIngredient h = new MagicItemIngredient(Items.NETHER_STAR,4,null);
      MagicItemIngredient m = new MagicItemIngredient(Items.DIAMOND_BLOCK,16,null);
      
      MagicItemIngredient[][] ingredients = {
            {a,a,c,d,d},
            {a,g,h,g,d},
            {c,h,m,h,c},
            {d,g,h,g,a},
            {d,d,c,a,a}};
      return new MagicItemRecipe(ingredients,new ForgeRequirement());
   }
   
   private List<String> makeLore(){
      ArrayList<String> list = new ArrayList<>();
      list.add("\" Transmutation Altar\\n\\nRarity: Legendary\\n\\nThrough my research into leylines, I have discovered the essence of a living entity entwined within the leylines of the Overworld.\\n\\nFrom what I can tell, the entity is old, very\"");
      list.add("\" Transmutation Altar\\n\\nold. And most likely of divine nature.\\n\\nI believe I can construct an altar that taps into the specific energy of this entity who's energy rides the leylines. I wonder what capabilities this structure could yield.\"");
      list.add("\" Transmutation Altar\\n\\nThe Altar I have created seems to mutate items when I drop them in specific configurations.\\nThe mutations are consistent and follow some rules of conservation, although it consumes reagents in the process.\"");
      list.add("\" Transmutation Altar\\n\\nI will catalog all of the mutations I can find and document them within the keystone of the altar.\\n\\nThe points of interest are that there are 5 placement locations for items:\\nThere is a positive input, marked by\"");
      list.add("\" Transmutation Altar\\n\\na quartz block. A negative input, marked by a blackstone block.\\nTwo reagent inputs, marked by amethyst. And lastly, the keystone itself seems like it can channel energy into an item, however I haven't found anything that works yet.\"");
      list.add("\" Transmutation Altar\\n\\nWhen activated, the altar will charge up and perform the set transmutation.\\nA gentle voice whispers with each activation.\\nThe invoked entity reminds me of the fae creatures that Illagers keep. \"");
      return list;
   }
   
   public class TransmutationAltarItem extends MagicPolymerBlockItem {
      public TransmutationAltarItem(Block block, Settings settings){
         super(getThis(),block,settings);
      }
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
      
   }
   
   public class TransmutationAltarBlock extends MagicPolymerBlockEntity {
      public static final DirectionProperty HORIZONTAL_FACING = Properties.HORIZONTAL_FACING;
      
      public TransmutationAltarBlock(Settings settings){
         super(settings);
      }
      
      @Override
      public Block getPolymerBlock(BlockState state) {
         return Blocks.DIAMOND_BLOCK;
      }
      
      @Nullable
      public static TransmutationAltarBlockEntity getEntity(World world, BlockPos pos) {
         BlockState state = world.getBlockState(pos);
         if (!(state.getBlock() instanceof TransmutationAltarBlock)) {
            return null;
         }
         return world.getBlockEntity(pos) instanceof TransmutationAltarBlockEntity altar ? altar : null;
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
         return new TransmutationAltarBlockEntity(pos, state);
      }
      
      @Nullable
      @Override
      public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
         return validateTicker(type, ArcanaRegistry.TRANSMUTATION_ALTAR_BLOCK_ENTITY, TransmutationAltarBlockEntity::ticker);
      }
      
      @Override
      public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity playerEntity, Hand hand, BlockHitResult hit){
         if(hand != Hand.MAIN_HAND) return ActionResult.PASS;
         TransmutationAltarBlockEntity altar = (TransmutationAltarBlockEntity) world.getBlockEntity(pos);
         if(altar != null){
            if(playerEntity instanceof ServerPlayerEntity player){
               Multiblock.MultiblockCheck check = new Multiblock.MultiblockCheck(player.getServerWorld(),pos,state,new BlockPos(-5,0,-5),world.getBlockState(pos).get(HORIZONTAL_FACING));
               if(multiblock.matches(check)){
                  altar.openGui(0,player,"");
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
      public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
         if (state.isOf(newState.getBlock())) {
            return;
         }
         BlockEntity blockEntity = world.getBlockEntity(pos);
         if(!(blockEntity instanceof MagicBlockEntity mbe)) return;
         DefaultedList<ItemStack> drops = DefaultedList.of();
         drops.add(getDroppedBlockItem(state,world,null,blockEntity));
         ItemScatterer.spawn(world, pos, drops);
         super.onStateReplaced(state, world, pos, newState, moved);
      }
      
      @Override
      public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
         BlockEntity entity = world.getBlockEntity(pos);
         if (placer instanceof ServerPlayerEntity player && entity instanceof TransmutationAltarBlockEntity altar) {
            initializeMagicBlock(stack,altar);
         }
      }
   }
}
