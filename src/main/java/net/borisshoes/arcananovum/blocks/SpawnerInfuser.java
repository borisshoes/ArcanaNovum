package net.borisshoes.arcananovum.blocks;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.core.ArcanaBlock;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerBlockEntity;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerBlockItem;
import net.borisshoes.arcananovum.gui.arcanetome.TomeGui;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaIngredient;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaRecipe;
import net.borisshoes.arcananovum.recipes.arcana.ForgeRequirement;
import net.borisshoes.arcananovum.recipes.arcana.GenericArcanaIngredient;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.borislib.utils.SoundUtils;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class SpawnerInfuser extends ArcanaBlock {
	public static final String ID = "spawner_infuser";
   
   public static final int[] pointsFromTier = {0,16,32,64,128,256,512,1024};
   public static final Item POINTS_ITEM = Items.NETHER_STAR;
   
   public SpawnerInfuser(){
      id = ID;
      name = "Spawner Infuser";
      rarity = ArcanaRarity.SOVEREIGN;
      categories = new TomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), TomeGui.TomeFilter.BLOCKS};
      vanillaItem = Items.SCULK_SHRIEKER;
      block = new SpawnerInfuserBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).strength(3.0f, 1200.0f).sound(SoundType.SCULK_SHRIEKER));
      item = new SpawnerInfuserItem(this.block);
      displayName = Component.translatableWithFallback("item."+MOD_ID+"."+ID,name).withStyle(ChatFormatting.BOLD, ChatFormatting.DARK_GREEN);
      researchTasks = new ResourceKey[]{ResearchTasks.UNLOCK_ARCANE_SINGULARITY,ResearchTasks.UNLOCK_MIDNIGHT_ENCHANTER,ResearchTasks.UNLOCK_SPAWNER_HARNESS,ResearchTasks.UNLOCK_STELLAR_CORE,ResearchTasks.OBTAIN_NETHERITE_INGOT,ResearchTasks.ADVANCEMENT_KILL_MOB_NEAR_SCULK_CATALYST};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getDefaultMaxStackSize());
      setPrefStack(stack);
   }
   
   @Override
   public List<Component> getItemLore(@Nullable ItemStack itemStack){
      List<MutableComponent> lore = new ArrayList<>();
      lore.add(Component.literal("")
            .append(Component.literal("Spawners ").withStyle(ChatFormatting.DARK_GREEN))
            .append(Component.literal("have their ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("natural limit").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal(", ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("Arcana ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("can now push them ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("further").withStyle(ChatFormatting.ITALIC, ChatFormatting.YELLOW))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_AQUA)));
      lore.add(Component.literal("")
            .append(Component.literal("Place ").withStyle(ChatFormatting.GREEN))
            .append(Component.literal("the ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("Infuser ").withStyle(ChatFormatting.DARK_GREEN))
            .append(Component.literal("two blocks ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("below ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("a ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("spawner").withStyle(ChatFormatting.DARK_GREEN))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_AQUA)));
      lore.add(Component.literal("")
            .append(Component.literal("The ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("Infuser ").withStyle(ChatFormatting.DARK_GREEN))
            .append(Component.literal("requires a ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("soulstone ").withStyle(ChatFormatting.DARK_RED))
            .append(Component.literal("matching the ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("spawner ").withStyle(ChatFormatting.DARK_GREEN))
            .append(Component.literal("type").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_AQUA)));
      lore.add(Component.literal("")
            .append(Component.literal("The ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("Infuser ").withStyle(ChatFormatting.DARK_GREEN))
            .append(Component.literal("also requires ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("Nether Stars").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(" to unlock ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("enhanced ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("infusions").withStyle(ChatFormatting.DARK_GREEN))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_AQUA)));
      lore.add(Component.literal("")
            .append(Component.literal("Apply ").withStyle(ChatFormatting.GREEN))
            .append(Component.literal("a ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("Redstone signal").withStyle(ChatFormatting.RED))
            .append(Component.literal(" to ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("activate ").withStyle(ChatFormatting.GREEN))
            .append(Component.literal("the ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("Infuser").withStyle(ChatFormatting.DARK_GREEN))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_AQUA)));
      lore.add(Component.literal("")
            .append(Component.literal("Right Click").withStyle(ChatFormatting.GREEN))
            .append(Component.literal(" the ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("Infuser ").withStyle(ChatFormatting.DARK_GREEN))
            .append(Component.literal("to ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("configure ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("its ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("abilities").withStyle(ChatFormatting.DARK_GREEN))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_AQUA)));
     return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
	protected ArcanaRecipe makeRecipe(){
      ArcanaIngredient a = new ArcanaIngredient(Items.NETHER_STAR,2);
      ArcanaIngredient b = new ArcanaIngredient(Items.ECHO_SHARD,8);
      ArcanaIngredient c = new ArcanaIngredient(Items.SCULK_CATALYST,24);
      ArcanaIngredient g = new ArcanaIngredient(Items.SCULK_SHRIEKER,24);
      ArcanaIngredient h = new ArcanaIngredient(Items.NETHERITE_INGOT,2);
      GenericArcanaIngredient m = new GenericArcanaIngredient(ArcanaRegistry.SPAWNER_HARNESS,1);
      
      ArcanaIngredient[][] ingredients = {
            {a,b,c,b,a},
            {b,g,h,g,b},
            {c,h,m,h,c},
            {b,g,h,g,b},
            {a,b,c,b,a}};
      return new ArcanaRecipe(ingredients,new ForgeRequirement().withSingularity().withCore().withEnchanter().withAnvil());
   }
   
   @Override
   public List<List<Component>> getBookLore(){
      List<List<Component>> list = new ArrayList<>();
      list.add(List.of(Component.literal(" Spawner Infuser").withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.BOLD), Component.literal("\nRarity: ").withStyle(ChatFormatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(),false)), Component.literal("\nOne of my most intricate and powerful creations to date. This behemoth exploits a fascinating organism from the Deep Dark called Sculk. It acts as if soulsand was alive, growing, and feeding on souls.").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal(" Spawner Infuser").withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.BOLD), Component.literal("\nBy combining the tech from one of my earlier works, the Spawner Infuser, I can use Arcana to overload the innate magic that summons creatures. All the Sculk mechanisms need are some souls, provided easily from a").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal(" Spawner Infuser").withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.BOLD), Component.literal("\nSoulstone, and some crystalline structure combined with a lot of energy. Nether stars work both as a focusing crystal and a power source, so that should do nicely.\n \nA simple Redstone signal will activate it.").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal(" Spawner Infuser").withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.BOLD), Component.literal("\nAll aspects of the spawner can be configured, from range, to spawn delay, and a lot more. \nAs long as the Sculk has an adequate base of souls from the Soulstone, more and more upgrades can be added.").withStyle(ChatFormatting.BLACK)));
      return list;
   }
   
   public class SpawnerInfuserItem extends ArcanaPolymerBlockItem {
      public SpawnerInfuserItem(Block block){
         super(getThis(),block, getArcanaItemComponents());
      }
      
      @Override
      public ItemStack getDefaultInstance(){
         return prefItem;
      }
   }
   
   public class SpawnerInfuserBlock extends ArcanaPolymerBlockEntity {
      public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
      
      public SpawnerInfuserBlock(BlockBehaviour.Properties settings){
         super(getThis(), settings);
      }
      
      @Override
      public BlockState getPolymerBlockState(BlockState state, PacketContext context){
         return Blocks.SCULK_SHRIEKER.defaultBlockState().setValue(BlockStateProperties.CAN_SUMMON,state.getValue(ACTIVE));
      }
      
      @Nullable
      public static SpawnerInfuserBlockEntity getEntity(Level world, BlockPos pos){
         BlockState state = world.getBlockState(pos);
         if(!(state.getBlock() instanceof SpawnerInfuserBlock)){
            return null;
         }
         return world.getBlockEntity(pos) instanceof SpawnerInfuserBlockEntity infuser ? infuser : null;
      }
      
      @Override
      public BlockEntity newBlockEntity(BlockPos pos, BlockState state){
         return new SpawnerInfuserBlockEntity(pos, state);
      }
      
      @Nullable
      @Override
      public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type){
         return createTickerHelper(type, ArcanaRegistry.SPAWNER_INFUSER_BLOCK_ENTITY, SpawnerInfuserBlockEntity::ticker);
      }
      
      @Nullable
      @Override
      public BlockState getStateForPlacement(BlockPlaceContext ctx){
         return this.defaultBlockState().setValue(ACTIVE,false);
      }
      
      @Override
      protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> stateManager){
         stateManager.add(ACTIVE);
      }
      
      @Override
      public InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player playerEntity, BlockHitResult hit){
         SpawnerInfuserBlockEntity infuser = (SpawnerInfuserBlockEntity) world.getBlockEntity(pos);
         if(infuser != null){
            if(playerEntity instanceof ServerPlayer player){
               infuser.openGui(player);
            }
         }
         return InteractionResult.SUCCESS_SERVER;
      }
      
      @Override
      public void setPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack){
         BlockEntity entity = world.getBlockEntity(pos);
         if(entity instanceof SpawnerInfuserBlockEntity infuser){
            initializeArcanaBlock(stack,infuser);
            
            if(placer instanceof ServerPlayer player){
               SoundUtils.soulSounds(player.level(),pos,5,30);
               SoundUtils.playSound(world,pos, SoundEvents.SCULK_SHRIEKER_SHRIEK, SoundSource.BLOCKS,1,.6f);
               player.displayClientMessage(Component.literal("The Infuser makes a most unsettling sound...").withStyle(ChatFormatting.DARK_GREEN),true);
            }
         }
      }
      
      @Override
      public boolean isPathfindable(BlockState state, PathComputationType type){
         return false;
      }
   }
}

