package net.borisshoes.arcananovum.blocks.altars;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.ArcanaBlock;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.core.Multiblock;
import net.borisshoes.arcananovum.core.MultiblockCore;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerBlockEntity;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerBlockItem;
import net.borisshoes.arcananovum.gui.arcanetome.TomeGui;
import net.borisshoes.arcananovum.items.Waystone;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaIngredient;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaRecipe;
import net.borisshoes.arcananovum.recipes.arcana.ForgeRequirement;
import net.borisshoes.arcananovum.recipes.arcana.WaystoneIngredient;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaColors;
import net.borisshoes.arcananovum.utils.ArcanaUtils;
import net.borisshoes.borislib.utils.MinecraftUtils;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
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
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraft.world.block.WireOrientation;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;
import static net.borisshoes.arcananovum.blocks.altars.StarpathAltarBlockEntity.COST;

public class StarpathAltar extends ArcanaBlock implements MultiblockCore {
	public static final String ID = "starpath_altar";
   
   public static final String TARGETS_TAG = "targets";
   
   private Multiblock multiblock;
   
   public StarpathAltar(){
      id = ID;
      name = "Starpath Altar";
      rarity = ArcanaRarity.SOVEREIGN;
      categories = new TomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), TomeGui.TomeFilter.BLOCKS, TomeGui.TomeFilter.ALTARS};
      itemVersion = 0;
      vanillaItem = Items.SCULK_CATALYST;
      block = new StarpathAltarBlock(AbstractBlock.Settings.create().mapColor(MapColor.BLACK).strength(3.0f,1200.0f).luminance(state -> 6).sounds(BlockSoundGroup.SCULK_CATALYST));
      item = new StarpathAltarItem(this.block);
      displayName = Text.translatableWithFallback("item."+MOD_ID+"."+ID,name).formatted(Formatting.BOLD,Formatting.WHITE);
      researchTasks = new RegistryKey[]{ResearchTasks.OBTAIN_STARDUST,ResearchTasks.USE_ENDER_EYE,ResearchTasks.USE_ENDER_PEARL,ResearchTasks.ADVANCEMENT_OBTAIN_CRYING_OBSIDIAN,ResearchTasks.UNLOCK_WAYSTONE};
      
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
         int size = getListProperty(itemStack,TARGETS_TAG).size();
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
      NbtList targetsList = getListProperty(stack,TARGETS_TAG);
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
   public Vec3i getCheckOffset(){
      return new Vec3i(-5,0,-5);
   }
   
   @Override
	protected ArcanaRecipe makeRecipe(){
      ArcanaIngredient a = new ArcanaIngredient(Items.ENDER_EYE,24);
      ArcanaIngredient b = new ArcanaIngredient(Items.OBSIDIAN,16);
      ArcanaIngredient c = new ArcanaIngredient(Items.CRYING_OBSIDIAN,16);
      ArcanaIngredient h = new ArcanaIngredient(ArcanaRegistry.STARDUST,8);
      ArcanaIngredient m = new ArcanaIngredient(Items.NETHER_STAR,1);
      ArcanaIngredient o = new WaystoneIngredient(true);
      
      ArcanaIngredient[][] ingredients = {
            {a,b,c,b,a},
            {b,h,o,h,b},
            {c,o,m,o,c},
            {b,h,o,h,b},
            {a,b,c,b,a}};
      return new ArcanaRecipe(ingredients,new ForgeRequirement());
   }
   
   @Override
   public List<List<Text>> getBookLore(){
      List<List<Text>> list = new ArrayList<>();
      list.add(List.of(Text.literal("   Starpath Altar").formatted(Formatting.BOLD).withColor(ArcanaColors.STARLIGHT_FORGE_COLOR),Text.literal("\nRarity: ").formatted(Formatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(),false)),Text.literal("\nThe leylines flow like rivers through the world, yet they are almost indistinct and would be impossible to navigate. However, the stars above pull on them like the moon on the tide. By charting the stars, and using ").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal("   Starpath Altar").formatted(Formatting.BOLD).withColor(ArcanaColors.STARLIGHT_FORGE_COLOR),Text.literal("\nWaystones to mark them, I should be able to send teleportation energy through the leylines along a charted course to exactly where I want to go in the world. It should even be capable of taking a group of creatures").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal("   Starpath Altar").formatted(Formatting.BOLD).withColor(ArcanaColors.STARLIGHT_FORGE_COLOR),Text.literal("\nall at once to the same destination!\n\nUnfortunately, an Eye of Ender only contains so much teleportation energy, so the farther I wish to travel, the more Eyes I need to provide to have ").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal("   Starpath Altar").formatted(Formatting.BOLD).withColor(ArcanaColors.STARLIGHT_FORGE_COLOR),Text.literal("\nenough energy to create a continuous pathway along the leylines.\n\nI can Sneak Use a Waystone to encode a location into the Altar, or enter one manually.").formatted(Formatting.BLACK)));
      return list;
   }
   
   public class StarpathAltarItem extends ArcanaPolymerBlockItem {
      public StarpathAltarItem(Block block){
         super(getThis(),block, getArcanaItemComponents());
      }
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
   }
   
   public class StarpathAltarBlock extends ArcanaPolymerBlockEntity {
      public static final BooleanProperty BLOOM = Properties.BLOOM;
      public static final BooleanProperty ACTIVATABLE = BooleanProperty.of("activatable");
      public StarpathAltarBlock(AbstractBlock.Settings settings){
         super(getThis(), settings);
      }
      
      @Override
      public BlockState getPolymerBlockState(BlockState state, PacketContext context){
         return Blocks.SCULK_CATALYST.getDefaultState().with(BLOOM,state.get(BLOOM));
      }
      
      @Nullable
      public static StarpathAltarBlockEntity getEntity(World world, BlockPos pos){
         BlockState state = world.getBlockState(pos);
         if(!(state.getBlock() instanceof StarpathAltarBlock)){
            return null;
         }
         return world.getBlockEntity(pos) instanceof StarpathAltarBlockEntity altar ? altar : null;
      }
      
      @Override
      public BlockEntity createBlockEntity(BlockPos pos, BlockState state){
         return new StarpathAltarBlockEntity(pos, state);
      }
      
      @Nullable
      @Override
      public BlockState getPlacementState(ItemPlacementContext ctx){
         return this.getDefaultState().with(BLOOM,false).with(ACTIVATABLE,false);
      }
      
      @Override
      protected void appendProperties(StateManager.Builder<Block, BlockState> stateManager){
         stateManager.add(BLOOM,ACTIVATABLE);
      }
      
      @Nullable
      @Override
      public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type){
         return validateTicker(type, ArcanaRegistry.STARPATH_ALTAR_BLOCK_ENTITY, StarpathAltarBlockEntity::ticker);
      }
      
      @Override
      protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity playerEntity, BlockHitResult hit){
         StarpathAltarBlockEntity altar = (StarpathAltarBlockEntity) world.getBlockEntity(pos);
         if(altar != null){
            if(playerEntity instanceof ServerPlayerEntity player){
               if(altar.isAssembled()){
                  altar.openGui(player);
                  player.getItemCooldownManager().set(playerEntity.getMainHandStack(),1);
                  player.getItemCooldownManager().set(playerEntity.getOffHandStack(),1);
               }else{
                  player.sendMessage(Text.literal("Multiblock not constructed."));
                  multiblock.displayStructure(altar.getMultiblockCheck(),player);
               }
            }
         }
         return ActionResult.SUCCESS_SERVER;
      }
      
      @Override
      public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack){
         BlockEntity entity = world.getBlockEntity(pos);
         if(entity instanceof StarpathAltarBlockEntity altar){
            initializeArcanaBlock(stack,altar);
            altar.readTargets(getListProperty(stack,TARGETS_TAG));
         }
      }
      
      private void tryActivate(BlockState state, World world, BlockPos pos){
         BlockEntity entity = world.getBlockEntity(pos);
         if(entity instanceof StarpathAltarBlockEntity altar && world instanceof ServerWorld serverWorld){
            boolean stargate = ArcanaAugments.getAugmentFromMap(altar.getAugments(),ArcanaAugments.STARGATE.id) > 0;
            Optional<ItemEntity> waystone = serverWorld.getEntitiesByClass(ItemEntity.class,new Box(pos.up()), e ->
                  e.getStack().isOf(ArcanaRegistry.WAYSTONE.getItem())
                        && Waystone.getTarget(e.getStack()) != null
                        && (stargate || Waystone.getTarget(e.getStack()).world().getValue().equals(world.getRegistryKey().getValue()))).stream().findAny();
            if(waystone.isPresent()){
               Waystone.WaystoneTarget target = Waystone.getTarget(waystone.get().getStack());
               altar.setTarget(new StarpathAltarBlockEntity.TargetEntry(
                     ArcanaUtils.getFormattedDimName(target.world()).getString()+" "+BlockPos.ofFloored(target.position()).toShortString(),
                     target.world().getValue().toString(),
                     (int) target.position().getX(),
                     (int) target.position().getY(),
                     (int) target.position().getZ()
               ));
            }
            
            boolean paid = MinecraftUtils.removeItemEntities(serverWorld,new Box(pos.up()),(itemStack) -> itemStack.isOf(COST),altar.calculateCost());
            if(paid) altar.startTeleport(null);
         }
      }
      
      @Override
      protected void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, @Nullable WireOrientation wireOrientation, boolean notify) {
         boolean bl = world.isReceivingRedstonePower(pos);
         boolean bl2 = state.getOrEmpty(ACTIVATABLE).orElse(false);
         if (bl && bl2) {
            this.tryActivate(state, world, pos);
            world.setBlockState(pos, state.with(ACTIVATABLE, false), Block.NOTIFY_LISTENERS);
         }
      }
   }
}

