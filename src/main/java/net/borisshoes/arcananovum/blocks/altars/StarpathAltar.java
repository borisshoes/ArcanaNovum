package net.borisshoes.arcananovum.blocks.altars;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.core.ArcanaBlock;
import net.borisshoes.arcananovum.core.Multiblock;
import net.borisshoes.arcananovum.core.MultiblockCore;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerBlockEntity;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerBlockItem;
import net.borisshoes.arcananovum.gui.arcanetome.TomeGui;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaIngredient;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaRecipe;
import net.borisshoes.arcananovum.recipes.arcana.ForgeRequirement;
import net.borisshoes.arcananovum.research.ResearchTasks;
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
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class StarpathAltar extends ArcanaBlock implements MultiblockCore {
	public static final String ID = "starpath_altar";
   
   public static final String TARGETS_TAG = "targets";
   
   private Multiblock multiblock;
   
   public StarpathAltar(){
      id = ID;
      name = "Starpath Altar";
      rarity = ArcanaRarity.SOVEREIGN;
      categories = new TomeGui.TomeFilter[]{TomeGui.TomeFilter.SOVEREIGN, TomeGui.TomeFilter.BLOCKS, TomeGui.TomeFilter.ALTARS};
      itemVersion = 0;
      vanillaItem = Items.SCULK_CATALYST;
      block = new StarpathAltarBlock(AbstractBlock.Settings.create().mapColor(MapColor.BLACK).strength(3.0f,1200.0f).luminance(state -> 6).sounds(BlockSoundGroup.SCULK_CATALYST));
      item = new StarpathAltarItem(this.block,new Item.Settings().maxCount(1).fireproof()
            .component(DataComponentTypes.ITEM_NAME, Text.translatable("item."+MOD_ID+"."+ID).formatted(Formatting.BOLD,Formatting.WHITE))
            .component(DataComponentTypes.LORE, new LoreComponent(getItemLore(null)))
            .component(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true)
      );
      researchTasks = new RegistryKey[]{ResearchTasks.OBTAIN_STARDUST,ResearchTasks.USE_ENDER_EYE,ResearchTasks.USE_ENDER_PEARL,ResearchTasks.ADVANCEMENT_OBTAIN_CRYING_OBSIDIAN};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getMaxCount());
      putProperty(stack,TARGETS_TAG,new NbtList());
      setPrefStack(stack);
   }
   
   @Override
   public List<Text> getItemLore(@Nullable ItemStack itemStack){
      List<MutableText> lore = new ArrayList<>();
      addAltarLore(lore);
      lore.add(Text.literal("Starpath Altar:").formatted(Formatting.BOLD,Formatting.WHITE));
      lore.add(Text.literal("")
            .append(Text.literal("The ").formatted(Formatting.DARK_GRAY))
            .append(Text.literal("Altar ").formatted(Formatting.WHITE))
            .append(Text.literal("finds a ").formatted(Formatting.DARK_GRAY))
            .append(Text.literal("path ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("through the ").formatted(Formatting.DARK_GRAY))
            .append(Text.literal("stars ").formatted(Formatting.WHITE))
            .append(Text.literal("to ").formatted(Formatting.DARK_GRAY))
            .append(Text.literal("anywhere ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("in the world.").formatted(Formatting.DARK_GRAY)));
      lore.add(Text.literal("")
            .append(Text.literal("All creatures").formatted(Formatting.DARK_AQUA))
            .append(Text.literal(" standing in the ").formatted(Formatting.DARK_GRAY))
            .append(Text.literal("Altar ").formatted(Formatting.WHITE))
            .append(Text.literal("will be ").formatted(Formatting.DARK_GRAY))
            .append(Text.literal("teleported").formatted(Formatting.DARK_AQUA))
            .append(Text.literal(".").formatted(Formatting.DARK_GRAY)));
      lore.add(Text.literal("")
            .append(Text.literal("The ").formatted(Formatting.DARK_GRAY))
            .append(Text.literal("Altar ").formatted(Formatting.WHITE))
            .append(Text.literal("requires ").formatted(Formatting.DARK_GRAY))
            .append(Text.literal("Eyes of Ender").formatted(Formatting.DARK_AQUA))
            .append(Text.literal(" to ").formatted(Formatting.DARK_GRAY))
            .append(Text.literal("activate").formatted(Formatting.WHITE))
            .append(Text.literal(".").formatted(Formatting.DARK_GRAY)));
      
      if(itemStack != null){
         int size = getListProperty(itemStack,TARGETS_TAG,NbtElement.COMPOUND_TYPE).size();
         if(size > 0){
            lore.add(Text.literal(""));
            lore.add(Text.literal("")
                  .append(Text.literal("Targets Stored: ").formatted(Formatting.DARK_GRAY))
                  .append(Text.literal(""+size).formatted(Formatting.DARK_AQUA)));
         }
      }
     return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   public ItemStack updateItem(ItemStack stack, MinecraftServer server){
      NbtList targetsList = getListProperty(stack,TARGETS_TAG, NbtElement.COMPOUND_TYPE);
      ItemStack newStack = super.updateItem(stack,server);
      putProperty(newStack,TARGETS_TAG,targetsList);
      return buildItemLore(newStack,server);
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
	protected ArcanaRecipe makeRecipe(){
      ArcanaIngredient a = new ArcanaIngredient(Items.ENDER_EYE,16);
      ArcanaIngredient b = new ArcanaIngredient(Items.OBSIDIAN,16);
      ArcanaIngredient c = new ArcanaIngredient(Items.CRYING_OBSIDIAN,16);
      ArcanaIngredient h = new ArcanaIngredient(ArcanaRegistry.STARDUST,8);
      ArcanaIngredient m = new ArcanaIngredient(Items.NETHER_STAR,1);
      
      ArcanaIngredient[][] ingredients = {
            {a,b,c,b,a},
            {b,a,h,a,b},
            {c,h,m,h,c},
            {b,a,h,a,b},
            {a,b,c,b,a}};
      return new ArcanaRecipe(ingredients,new ForgeRequirement());
   }
   
   @Override
   public List<List<Text>> getBookLore(){
      List<List<Text>> list = new ArrayList<>();
      list.add(List.of(Text.literal("    Starpath Altar\n\nRarity: Sovereign\n\nThe leylines flow like rivers through the world, yet they are indistinct and would be impossible to navigate.\nHowever, the stars above pull on them like the moon on the tide. By charting the stars, I should be able to").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal("    Starpath Altar\n\nsend teleportation energy through the leylines along a charted course to exactly where I want to go in the world.\n\nIt should even be capable of taking a group of creatures all at once to the same destination!").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal("    Starpath Altar\n\nHowever, an Eye of Ender only contains so much teleportation energy, so the farther I wish to travel, the more Eyes I need to provide up front to create a continuous pathway along the leylines.").formatted(Formatting.BLACK)));
      return list;
   }
   
   public class StarpathAltarItem extends ArcanaPolymerBlockItem {
      public StarpathAltarItem(Block block, Settings settings){
         super(getThis(),block, settings);
      }
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
   }
   
   public class StarpathAltarBlock extends ArcanaPolymerBlockEntity {
      public static final BooleanProperty BLOOM = Properties.BLOOM;
      public StarpathAltarBlock(AbstractBlock.Settings settings){
         super(settings);
      }
      
      @Override
      public BlockState getPolymerBlockState(BlockState state) {
         return Blocks.SCULK_CATALYST.getDefaultState().with(BLOOM,state.get(BLOOM));
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
         return validateTicker(type, ArcanaRegistry.STARPATH_ALTAR_BLOCK_ENTITY, StarpathAltarBlockEntity::ticker);
      }
      
      @Override
      protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity playerEntity, BlockHitResult hit){
         StarpathAltarBlockEntity altar = (StarpathAltarBlockEntity) world.getBlockEntity(pos);
         if(altar != null){
            if(playerEntity instanceof ServerPlayerEntity player){
               if(altar.isAssembled()){
                  altar.openGui(player);
                  player.getItemCooldownManager().set(playerEntity.getMainHandStack().getItem(),1);
               }else{
                  player.sendMessage(Text.literal("Multiblock not constructed."));
                  multiblock.displayStructure(altar.getMultiblockCheck());
               }
            }
         }
         return ActionResult.SUCCESS;
      }
      
      @Override
      public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
         BlockEntity entity = world.getBlockEntity(pos);
         if (placer instanceof ServerPlayerEntity player && entity instanceof StarpathAltarBlockEntity altar) {
            initializeArcanaBlock(stack,altar);
            altar.readTargets(getListProperty(stack,TARGETS_TAG,NbtElement.COMPOUND_TYPE));
         }
      }
   }
}

