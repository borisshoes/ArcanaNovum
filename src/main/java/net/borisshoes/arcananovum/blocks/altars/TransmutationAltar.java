package net.borisshoes.arcananovum.blocks.altars;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.ArcanaBlock;
import net.borisshoes.arcananovum.core.Multiblock;
import net.borisshoes.arcananovum.core.MultiblockCore;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerBlockEntity;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerBlockItem;
import net.borisshoes.arcananovum.gui.arcanetome.TomeGui;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaIngredient;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaRecipe;
import net.borisshoes.arcananovum.recipes.arcana.ForgeRequirement;
import net.borisshoes.arcananovum.recipes.transmutation.*;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.arcananovum.utils.ArcanaRarity;
import net.borisshoes.arcananovum.utils.TextUtils;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;
import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;

public class TransmutationAltar extends ArcanaBlock implements MultiblockCore {
	public static final String ID = "transmutation_altar";
   
   private Multiblock multiblock;
   
   public TransmutationAltar(){
      id = ID;
      name = "Transmutation Altar";
      rarity = ArcanaRarity.SOVEREIGN;
      categories = new TomeGui.TomeFilter[]{TomeGui.TomeFilter.SOVEREIGN,TomeGui.TomeFilter.BLOCKS,TomeGui.TomeFilter.ALTARS};
      vanillaItem = Items.DIAMOND_BLOCK;
      block = new TransmutationAltarBlock(AbstractBlock.Settings.create().mapColor(MapColor.DIAMOND_BLUE).strength(5.0f,1200.0f).requiresTool().sounds(BlockSoundGroup.AMETHYST_BLOCK));
      item = new TransmutationAltarItem(this.block,new Item.Settings().maxCount(1).fireproof()
            .component(DataComponentTypes.ITEM_NAME, Text.translatable("item."+MOD_ID+"."+ID).formatted(Formatting.BOLD,Formatting.AQUA))
            .component(DataComponentTypes.LORE, new LoreComponent(getItemLore(null)))
            .component(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true)
      );
      researchTasks = new RegistryKey[]{ResearchTasks.ADVANCEMENT_TRADE,ResearchTasks.OBTAIN_AMETHYST_SHARD,ResearchTasks.OBTAIN_DIAMOND,ResearchTasks.ADVANCEMENT_OBTAIN_CRYING_OBSIDIAN};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getMaxCount());
      setPrefStack(stack);
   }
   
   @Override
   public List<Text> getItemLore(@Nullable ItemStack itemStack){
      List<MutableText> lore = new ArrayList<>();
      addAltarLore(lore);
      lore.add(Text.literal("Transmutation Altar:").formatted(Formatting.BOLD,Formatting.AQUA));
      lore.add(Text.literal("")
            .append(Text.literal("The ").formatted(Formatting.GRAY))
            .append(Text.literal("Altar").formatted(Formatting.AQUA))
            .append(Text.literal(" beckons to an ").formatted(Formatting.GRAY))
            .append(Text.literal("ancient entity").formatted(Formatting.AQUA))
            .append(Text.literal(" of ").formatted(Formatting.GRAY))
            .append(Text.literal("balance").formatted(Formatting.BLUE))
            .append(Text.literal(" and ").formatted(Formatting.GRAY))
            .append(Text.literal("trades").formatted(Formatting.BLUE))
            .append(Text.literal(".").formatted(Formatting.GRAY)));
      lore.add(Text.literal("")
            .append(Text.literal("The ").formatted(Formatting.GRAY))
            .append(Text.literal("Altar").formatted(Formatting.AQUA))
            .append(Text.literal(" can be called upon to").formatted(Formatting.GRAY))
            .append(Text.literal(" exchange ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("equivalent materials").formatted(Formatting.BLUE))
            .append(Text.literal(".").formatted(Formatting.GRAY)));
      lore.add(Text.literal("")
            .append(Text.literal("Every ").formatted(Formatting.GRAY))
            .append(Text.literal("barter").formatted(Formatting.DARK_AQUA))
            .append(Text.literal(" comes with its own ").formatted(Formatting.GRAY))
            .append(Text.literal("price.").formatted(Formatting.BLUE)));
     return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   public static List<TransmutationRecipe> getUnlockedRecipes(ServerPlayerEntity player){
      return TransmutationRecipes.TRANSMUTATION_RECIPES.stream().filter(recipe -> {
         if(recipe instanceof InfusionTransmutationRecipe r && ArcanaItemUtils.isArcane(r.getOutput()) && !PLAYER_DATA.get(player).hasResearched(ArcanaItemUtils.identifyItem(r.getOutput()))){
            return false;
         }
         if(recipe instanceof AequalisCatalystTransmutationRecipe && !(PLAYER_DATA.get(player).hasResearched(ArcanaRegistry.AEQUALIS_SCIENTIA) && PLAYER_DATA.get(player).getAugmentLevel(ArcanaAugments.EQUIVALENT_EXCHANGE.id) > 0)){
            return false;
         }
         if(recipe instanceof AequalisSkillTransmutationRecipe && !PLAYER_DATA.get(player).hasResearched(ArcanaRegistry.AEQUALIS_SCIENTIA)){
            return false;
         }
         return true;
      }).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   public void loadMultiblock(){
      multiblock = Multiblock.loadFromFile(getId());
   }
   
   @Override
   public Multiblock getMultiblock(){
      return multiblock;
   }
   
   @Override
   public Vec3i getCheckOffset(){
      return new Vec3i(-5,0,-5);
   }
   
   
   @Override
	protected ArcanaRecipe makeRecipe(){
      ArcanaIngredient a = new ArcanaIngredient(Items.BLACKSTONE,24);
      ArcanaIngredient c = new ArcanaIngredient(Items.AMETHYST_BLOCK,16);
      ArcanaIngredient d = new ArcanaIngredient(Items.QUARTZ_BLOCK,24);
      ArcanaIngredient g = new ArcanaIngredient(Items.AMETHYST_SHARD,24);
      ArcanaIngredient h = new ArcanaIngredient(Items.DIAMOND,16);
      ArcanaIngredient m = new ArcanaIngredient(Items.DIAMOND_BLOCK,4);
      
      ArcanaIngredient[][] ingredients = {
            {a,a,c,d,d},
            {a,g,h,g,d},
            {c,h,m,h,c},
            {d,g,h,g,a},
            {d,d,c,a,a}};
      return new ArcanaRecipe(ingredients,new ForgeRequirement());
   }
   
   @Override
   public List<List<Text>> getBookLore(){
      List<List<Text>> list = new ArrayList<>();
      list.add(List.of(Text.literal(" Transmutation Altar\n\nRarity: Sovereign\n\nThrough my research into leylines, I have discovered the essence of a living entity entwined within the leylines of the Overworld.\n\nFrom what I can tell, the entity is old, very")));
      list.add(List.of(Text.literal(" Transmutation Altar\n\nold. And most likely of divine nature.\n\nI believe I can construct an altar that taps into the specific energy of this entity who's energy rides the leylines. I wonder what capabilities this structure could yield.")));
      list.add(List.of(Text.literal(" Transmutation Altar\n\nThe Altar I have created seems to mutate items when I drop them in specific configurations.\nThe mutations are consistent and follow some rules of conservation, although it consumes reagents in the process.")));
      list.add(List.of(Text.literal(" Transmutation Altar\n\nI will catalog all of the mutations I can find and document them within the keystone of the altar.\n\nThe points of interest are that there are 5 placement locations for items:\nThere is a positive input, marked by")));
      list.add(List.of(Text.literal(" Transmutation Altar\n\na quartz block. A negative input, marked by a blackstone block.\nTwo reagent inputs, marked by amethyst. And lastly, the keystone itself seems like it can channel energy into an item, however I haven't found anything that works yet.")));
      list.add(List.of(Text.literal(" Transmutation Altar\n\nWhen activated, the altar will charge up and perform the set transmutation.\nA gentle voice whispers with each activation.\nThe invoked entity reminds me of the fae creatures that Illagers keep. ")));
      return list;
   }
   
   public class TransmutationAltarItem extends ArcanaPolymerBlockItem {
      public TransmutationAltarItem(Block block, Settings settings){
         super(getThis(),block,settings);
      }
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
      
   }
   
   public class TransmutationAltarBlock extends ArcanaPolymerBlockEntity {
      public static final DirectionProperty HORIZONTAL_FACING = Properties.HORIZONTAL_FACING;
      
      public TransmutationAltarBlock(AbstractBlock.Settings settings){
         super(settings);
      }
      
      @Override
      public BlockState getPolymerBlockState(BlockState state) {
         return Blocks.DIAMOND_BLOCK.getDefaultState();
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
      public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity playerEntity, BlockHitResult hit){
         TransmutationAltarBlockEntity altar = (TransmutationAltarBlockEntity) world.getBlockEntity(pos);
         if(altar != null){
            if(playerEntity instanceof ServerPlayerEntity player){
               if(altar.isAssembled()){
                  altar.openGui(player);
                  player.getItemCooldownManager().set(playerEntity.getMainHandStack().getItem(),1);
               }else{
                  player.sendMessage(Text.literal("Multiblock not constructed."));
                  multiblock.displayStructure(altar.getMultiblockCheck(),player);
               }
            }
         }
         return ActionResult.SUCCESS;
      }
      
      @Override
      public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
         BlockEntity entity = world.getBlockEntity(pos);
         if (placer instanceof ServerPlayerEntity player && entity instanceof TransmutationAltarBlockEntity altar) {
            initializeArcanaBlock(stack,altar);
         }
      }
   }
}

