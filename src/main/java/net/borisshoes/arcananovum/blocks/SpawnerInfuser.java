package net.borisshoes.arcananovum.blocks;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.core.ArcanaBlock;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerBlockEntity;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerBlockItem;
import net.borisshoes.arcananovum.gui.arcanetome.TomeGui;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaIngredient;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaRecipe;
import net.borisshoes.arcananovum.recipes.arcana.ForgeRequirement;
import net.borisshoes.arcananovum.recipes.arcana.GenericArcanaIngredient;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaRarity;
import net.borisshoes.arcananovum.utils.SoundUtils;
import net.borisshoes.arcananovum.utils.TextUtils;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
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
      block = new SpawnerInfuserBlock(AbstractBlock.Settings.create().mapColor(MapColor.BLACK).strength(3.0f, 1200.0f).sounds(BlockSoundGroup.SCULK_SHRIEKER));
      item = new SpawnerInfuserItem(this.block);
      displayName = Text.translatableWithFallback("item."+MOD_ID+"."+ID,name).formatted(Formatting.BOLD,Formatting.DARK_GREEN);
      researchTasks = new RegistryKey[]{ResearchTasks.UNLOCK_ARCANE_SINGULARITY,ResearchTasks.UNLOCK_MIDNIGHT_ENCHANTER,ResearchTasks.UNLOCK_SPAWNER_HARNESS,ResearchTasks.UNLOCK_STELLAR_CORE,ResearchTasks.OBTAIN_NETHERITE_INGOT,ResearchTasks.ADVANCEMENT_KILL_MOB_NEAR_SCULK_CATALYST};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getMaxCount());
      setPrefStack(stack);
   }
   
   @Override
   public List<Text> getItemLore(@Nullable ItemStack itemStack){
      List<MutableText> lore = new ArrayList<>();
      lore.add(Text.literal("")
            .append(Text.literal("Spawners ").formatted(Formatting.DARK_GREEN))
            .append(Text.literal("have their ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("natural limit").formatted(Formatting.YELLOW))
            .append(Text.literal(", ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("Arcana ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("can now push them ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("further").formatted(Formatting.ITALIC,Formatting.YELLOW))
            .append(Text.literal(".").formatted(Formatting.DARK_AQUA)));
      lore.add(Text.literal("")
            .append(Text.literal("Place ").formatted(Formatting.GREEN))
            .append(Text.literal("the ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("Infuser ").formatted(Formatting.DARK_GREEN))
            .append(Text.literal("two blocks ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("below ").formatted(Formatting.YELLOW))
            .append(Text.literal("a ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("spawner").formatted(Formatting.DARK_GREEN))
            .append(Text.literal(".").formatted(Formatting.DARK_AQUA)));
      lore.add(Text.literal("")
            .append(Text.literal("The ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("Infuser ").formatted(Formatting.DARK_GREEN))
            .append(Text.literal("requires a ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("soulstone ").formatted(Formatting.DARK_RED))
            .append(Text.literal("matching the ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("spawner ").formatted(Formatting.DARK_GREEN))
            .append(Text.literal("type").formatted(Formatting.YELLOW))
            .append(Text.literal(".").formatted(Formatting.DARK_AQUA)));
      lore.add(Text.literal("")
            .append(Text.literal("The ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("Infuser ").formatted(Formatting.DARK_GREEN))
            .append(Text.literal("also requires ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("Nether Stars").formatted(Formatting.AQUA))
            .append(Text.literal(" to unlock ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("enhanced ").formatted(Formatting.YELLOW))
            .append(Text.literal("infusions").formatted(Formatting.DARK_GREEN))
            .append(Text.literal(".").formatted(Formatting.DARK_AQUA)));
      lore.add(Text.literal("")
            .append(Text.literal("Apply ").formatted(Formatting.GREEN))
            .append(Text.literal("a ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("Redstone signal").formatted(Formatting.RED))
            .append(Text.literal(" to ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("activate ").formatted(Formatting.GREEN))
            .append(Text.literal("the ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("Infuser").formatted(Formatting.DARK_GREEN))
            .append(Text.literal(".").formatted(Formatting.DARK_AQUA)));
      lore.add(Text.literal("")
            .append(Text.literal("Right Click").formatted(Formatting.GREEN))
            .append(Text.literal(" the ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("Infuser ").formatted(Formatting.DARK_GREEN))
            .append(Text.literal("to ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("configure ").formatted(Formatting.YELLOW))
            .append(Text.literal("its ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("abilities").formatted(Formatting.DARK_GREEN))
            .append(Text.literal(".").formatted(Formatting.DARK_AQUA)));
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
   public List<List<Text>> getBookLore(){
      List<List<Text>> list = new ArrayList<>();
      list.add(List.of(Text.literal(" Spawner Infuser").formatted(Formatting.DARK_GREEN,Formatting.BOLD),Text.literal("\nRarity: ").formatted(Formatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(),false)),Text.literal("\nOne of my most intricate and powerful creations to date. This behemoth exploits a fascinating organism from the Deep Dark called Sculk. It acts as if soulsand was alive, growing, and feeding on souls.").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal(" Spawner Infuser").formatted(Formatting.DARK_GREEN,Formatting.BOLD),Text.literal("\nBy combining the tech from one of my earlier works, the Spawner Infuser, I can use Arcana to overload the innate magic that summons creatures. All the Sculk mechanisms need are some souls, provided easily from a").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal(" Spawner Infuser").formatted(Formatting.DARK_GREEN,Formatting.BOLD),Text.literal("\nSoulstone, and some crystalline structure combined with a lot of energy. Nether stars work both as a focusing crystal and a power source, so that should do nicely.\n \nA simple Redstone signal will activate it.").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal(" Spawner Infuser").formatted(Formatting.DARK_GREEN,Formatting.BOLD),Text.literal("\nAll aspects of the spawner can be configured, from range, to spawn delay, and a lot more. \nAs long as the Sculk has an adequate base of souls from the Soulstone, more and more upgrades can be added.").formatted(Formatting.BLACK)));
      return list;
   }
   
   public class SpawnerInfuserItem extends ArcanaPolymerBlockItem {
      public SpawnerInfuserItem(Block block){
         super(getThis(),block, getArcanaItemComponents());
      }
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
   }
   
   public class SpawnerInfuserBlock extends ArcanaPolymerBlockEntity {
      public static final BooleanProperty ACTIVE = BooleanProperty.of("active");
      
      public SpawnerInfuserBlock(AbstractBlock.Settings settings){
         super(getThis(), settings);
      }
      
      @Override
      public BlockState getPolymerBlockState(BlockState state, PacketContext context){
         return Blocks.SCULK_SHRIEKER.getDefaultState().with(Properties.CAN_SUMMON,state.get(ACTIVE));
      }
      
      @Nullable
      public static SpawnerInfuserBlockEntity getEntity(World world, BlockPos pos){
         BlockState state = world.getBlockState(pos);
         if(!(state.getBlock() instanceof SpawnerInfuserBlock)){
            return null;
         }
         return world.getBlockEntity(pos) instanceof SpawnerInfuserBlockEntity infuser ? infuser : null;
      }
      
      @Override
      public BlockEntity createBlockEntity(BlockPos pos, BlockState state){
         return new SpawnerInfuserBlockEntity(pos, state);
      }
      
      @Nullable
      @Override
      public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type){
         return validateTicker(type, ArcanaRegistry.SPAWNER_INFUSER_BLOCK_ENTITY, SpawnerInfuserBlockEntity::ticker);
      }
      
      @Nullable
      @Override
      public BlockState getPlacementState(ItemPlacementContext ctx){
         return this.getDefaultState().with(ACTIVE,false);
      }
      
      @Override
      protected void appendProperties(StateManager.Builder<Block, BlockState> stateManager){
         stateManager.add(ACTIVE);
      }
      
      @Override
      public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity playerEntity, BlockHitResult hit){
         SpawnerInfuserBlockEntity infuser = (SpawnerInfuserBlockEntity) world.getBlockEntity(pos);
         if(infuser != null){
            if(playerEntity instanceof ServerPlayerEntity player){
               infuser.openGui(player);
            }
         }
         return ActionResult.SUCCESS_SERVER;
      }
      
      @Override
      public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack){
         BlockEntity entity = world.getBlockEntity(pos);
         if(entity instanceof SpawnerInfuserBlockEntity infuser){
            initializeArcanaBlock(stack,infuser);
            
            if(placer instanceof ServerPlayerEntity player){
               SoundUtils.soulSounds(player.getServerWorld(),pos,5,30);
               SoundUtils.playSound(world,pos,SoundEvents.BLOCK_SCULK_SHRIEKER_SHRIEK,SoundCategory.BLOCKS,1,.6f);
               player.sendMessage(Text.literal("The Infuser makes a most unsettling sound...").formatted(Formatting.DARK_GREEN),true);
            }
         }
      }
      
      @Override
      public boolean canPathfindThrough(BlockState state, NavigationType type){
         return false;
      }
   }
}

