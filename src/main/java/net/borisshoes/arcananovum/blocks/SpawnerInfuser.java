package net.borisshoes.arcananovum.blocks;

import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.core.MagicBlock;
import net.borisshoes.arcananovum.core.polymer.MagicPolymerBlockEntity;
import net.borisshoes.arcananovum.core.polymer.MagicPolymerBlockItem;
import net.borisshoes.arcananovum.gui.spawnerinfuser.SpawnerInfuserGui;
import net.borisshoes.arcananovum.items.ArcaneTome;
import net.borisshoes.arcananovum.recipes.arcana.ForgeRequirement;
import net.borisshoes.arcananovum.recipes.arcana.GenericMagicIngredient;
import net.borisshoes.arcananovum.recipes.arcana.MagicItemIngredient;
import net.borisshoes.arcananovum.recipes.arcana.MagicItemRecipe;
import net.borisshoes.arcananovum.utils.MagicRarity;
import net.borisshoes.arcananovum.utils.SoundUtils;
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
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SpawnerInfuser extends MagicBlock {
   
   public static final int[] pointsFromTier = {0,16,32,64,128,256,512,1024};
   public static final Item pointsItem = Items.NETHER_STAR;
   
   public SpawnerInfuser(){
      id = "spawner_infuser";
      name = "Spawner Infuser";
      rarity = MagicRarity.LEGENDARY;
      categories = new ArcaneTome.TomeFilter[]{ArcaneTome.TomeFilter.LEGENDARY, ArcaneTome.TomeFilter.BLOCKS};
      vanillaItem = Items.SCULK_SHRIEKER;
      block = new SpawnerInfuserBlock(FabricBlockSettings.create().mapColor(MapColor.BLACK).strength(3.0f, 1200.0f).sounds(BlockSoundGroup.SCULK_SHRIEKER));
      item = new SpawnerInfuserItem(this.block,new FabricItemSettings().maxCount(1).fireproof());
      
      ItemStack stack = new ItemStack(item);
      NbtCompound tag = stack.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList loreList = new NbtList();
      NbtList enchants = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name","[{\"text\":\"Spawner Infuser\",\"italic\":false,\"color\":\"dark_green\",\"bold\":true}]");
      loreList.add(NbtString.of("[{\"text\":\"Spawners \",\"italic\":false,\"color\":\"dark_green\"},{\"text\":\"have their \",\"color\":\"dark_aqua\"},{\"text\":\"natural limit\",\"color\":\"yellow\"},{\"text\":\", \",\"color\":\"dark_aqua\"},{\"text\":\"Arcana \",\"color\":\"dark_purple\"},{\"text\":\"can now push them \",\"color\":\"dark_aqua\"},{\"text\":\"further\",\"color\":\"yellow\",\"italic\":true},{\"text\":\".\",\"color\":\"dark_aqua\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Place \",\"italic\":false,\"color\":\"green\"},{\"text\":\"the \",\"color\":\"dark_aqua\"},{\"text\":\"Infuser \",\"color\":\"dark_green\"},{\"text\":\"two blocks \",\"color\":\"dark_aqua\"},{\"text\":\"below \",\"color\":\"yellow\"},{\"text\":\"a \",\"color\":\"dark_aqua\"},{\"text\":\"spawner\",\"color\":\"dark_green\"},{\"text\":\".\",\"color\":\"dark_aqua\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"The \",\"italic\":false,\"color\":\"dark_aqua\"},{\"text\":\"Infuser \",\"color\":\"dark_green\"},{\"text\":\"requires a \"},{\"text\":\"soulstone \",\"color\":\"dark_red\"},{\"text\":\"matching the \"},{\"text\":\"spawner \",\"color\":\"dark_green\"},{\"text\":\"type\",\"color\":\"yellow\"},{\"text\":\".\",\"color\":\"dark_aqua\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"The \",\"italic\":false,\"color\":\"dark_aqua\"},{\"text\":\"Infuser \",\"color\":\"dark_green\"},{\"text\":\"also requires \"},{\"text\":\"Nether Stars\",\"color\":\"aqua\"},{\"text\":\" to unlock \"},{\"text\":\"enhanced \",\"color\":\"yellow\"},{\"text\":\"infusions\",\"color\":\"dark_green\"},{\"text\":\".\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Apply \",\"italic\":false,\"color\":\"green\"},{\"text\":\"a \",\"color\":\"dark_aqua\"},{\"text\":\"Redstone signal\",\"color\":\"red\"},{\"text\":\" to \",\"color\":\"dark_aqua\"},{\"text\":\"activate \"},{\"text\":\"the \",\"color\":\"dark_aqua\"},{\"text\":\"Infuser\",\"color\":\"dark_green\"},{\"text\":\".\",\"color\":\"dark_aqua\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Right Click\",\"italic\":false,\"color\":\"green\"},{\"text\":\" the \",\"color\":\"dark_aqua\"},{\"text\":\"Infuser \",\"color\":\"dark_green\"},{\"text\":\"to \",\"color\":\"dark_aqua\"},{\"text\":\"configure \",\"color\":\"yellow\"},{\"text\":\"its \",\"color\":\"dark_aqua\"},{\"text\":\"abilities\",\"color\":\"dark_green\"},{\"text\":\".\",\"color\":\"dark_aqua\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Legendary \",\"italic\":false,\"color\":\"gold\",\"bold\":true},{\"text\":\"Magic Item\",\"italic\":false,\"color\":\"dark_purple\",\"bold\":false}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
      tag.put("Enchantments",enchants);
      
      setBookLore(makeLore());
      setRecipe(makeRecipe());
      prefNBT = addMagicNbt(tag);
      
      stack.setNbt(prefNBT);
      prefItem = stack;
   }
   
   private MagicItemRecipe makeRecipe(){
      MagicItemIngredient a = new MagicItemIngredient(Items.ECHO_SHARD,16,null);
      MagicItemIngredient b = new MagicItemIngredient(Items.SOUL_SAND,64,null);
      MagicItemIngredient c = new MagicItemIngredient(Items.SCULK_SHRIEKER,64,null);
      MagicItemIngredient g = new MagicItemIngredient(Items.NETHER_STAR,8,null);
      MagicItemIngredient h = new MagicItemIngredient(Items.NETHERITE_INGOT,4,null);
      MagicItemIngredient k = new MagicItemIngredient(Items.SCULK_CATALYST,64,null);
      GenericMagicIngredient m = new GenericMagicIngredient(ArcanaRegistry.SPAWNER_HARNESS,1);
   
      MagicItemIngredient[][] ingredients = {
            {a,b,c,b,a},
            {b,g,h,g,b},
            {k,h,m,h,k},
            {b,g,h,g,b},
            {a,b,c,b,a}};
      return new MagicItemRecipe(ingredients,new ForgeRequirement().withEnchanter().withSingularity());
   }
   
   private List<String> makeLore(){
      ArrayList<String> list = new ArrayList<>();
      list.add("{\"text\":\"   Spawner Infuser\\n\\nRarity: Legendary\\n\\nOne of my most intricate and powerful creations to date.\\nThis behemoth exploits a fascinating organism from the Deep Dark called Sculk. It acts as if soulsand became alive. It grows and feeds \"}");
      list.add("{\"text\":\"   Spawner Infuser\\n\\nfrom souls. \\nBy combining the tech from one of my earlier works, the Spawner Infuser, I believe I can use Arcana to overload the innate magic that summons creatures.\\n\\nAll the Sculk mechanisms need are \"}");
      list.add("{\"text\":\"   Spawner Infuser\\n\\nsome souls, provided easily from a Soulstone, and some crystalline structure combined with a lot of energy. Nether Stars work as both a focusing crystal and a power source so that should do nicely.\\nA simple Redstone signal will activate it.\"}");
      list.add("{\"text\":\"   Spawner Infuser\\n\\nAll aspects of the spawner can now be configured from range, to spawn delay, and a whole lot more.\\n\\nAs long as the Sculk has an adequate base of souls from the Soulstone, more and more upgrades can be added.\"}");
      return list;
   }
   
   public class SpawnerInfuserItem extends MagicPolymerBlockItem {
      public SpawnerInfuserItem(Block block, Settings settings){
         super(getThis(),block, settings);
      }
      
      
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
   }
   
   public class SpawnerInfuserBlock extends MagicPolymerBlockEntity {
      public static final BooleanProperty ACTIVE = BooleanProperty.of("active");
      
      public SpawnerInfuserBlock(Settings settings){
         super(settings);
      }
      
      @Override
      public Block getPolymerBlock(BlockState state) {
         return Blocks.SCULK_SHRIEKER;
      }
      
      @Override
      public BlockState getPolymerBlockState(BlockState state){
         return super.getPolymerBlockState(state).with(Properties.CAN_SUMMON,state.get(ACTIVE));
      }
      
      @Nullable
      public static SpawnerInfuserBlockEntity getEntity(World world, BlockPos pos) {
         BlockState state = world.getBlockState(pos);
         if (!(state.getBlock() instanceof SpawnerInfuserBlock)) {
            return null;
         }
         return world.getBlockEntity(pos) instanceof SpawnerInfuserBlockEntity infuser ? infuser : null;
      }
      
      @Override
      public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
         return new SpawnerInfuserBlockEntity(pos, state);
      }
      
      @Nullable
      @Override
      public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
         return checkType(type, ArcanaRegistry.SPAWNER_INFUSER_BLOCK_ENTITY, SpawnerInfuserBlockEntity::ticker);
      }
      
      @Nullable
      @Override
      public BlockState getPlacementState(ItemPlacementContext ctx) {
         return this.getDefaultState().with(ACTIVE,false);
      }
      
      @Override
      protected void appendProperties(StateManager.Builder<Block, BlockState> stateManager) {
         stateManager.add(ACTIVE);
      }
      
      @Override
      public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity playerEntity, Hand hand, BlockHitResult hit){
         SpawnerInfuserBlockEntity infuser = (SpawnerInfuserBlockEntity) world.getBlockEntity(pos);
         if(infuser != null){
            if(playerEntity instanceof ServerPlayerEntity player){
               SpawnerInfuserGui gui = new SpawnerInfuserGui(player,infuser,world);
               gui.build();
               player.getItemCooldownManager().set(playerEntity.getStackInHand(hand).getItem(),1);
               if(!gui.tryOpen(player)){
                  player.sendMessage(Text.literal("Someone else is using the Infuser").formatted(Formatting.RED),true);
               }
            }
         }
         return ActionResult.SUCCESS;
      }
      
      @Override
      public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
         if (world.getBlockEntity(pos) instanceof SpawnerInfuserBlockEntity infuser) {
            if (!player.isCreative() && player.canHarvest(world.getBlockState(pos)) && world instanceof ServerWorld serverWorld) {
               if (!world.isClient) {
                  dropBlockItem(world,pos,state,player,infuser);
                  
                  DefaultedList<ItemStack> drops = DefaultedList.of();
                  int ratio = (int) Math.pow(2,ArcanaAugments.getAugmentFromMap(infuser.getAugments(),ArcanaAugments.AUGMENTED_APPARATUS.id));
                  int points = infuser.getPoints();
                  if(points > 0){
                     while(points/ratio > 64){
                        ItemStack dropItem = new ItemStack(SpawnerInfuser.pointsItem);
                        dropItem.setCount(64);
                        drops.add(dropItem.copy());
                        points -= 64*ratio;
                     }
                     ItemStack dropItem = new ItemStack(SpawnerInfuser.pointsItem);
                     dropItem.setCount(points/ratio);
                     drops.add(dropItem.copy());
                  }
                  
                  ItemStack stone = infuser.getSoulstone();
                  if(!stone.isEmpty()) drops.add(stone.copy());
                  
                  ItemScatterer.spawn(world, pos.up(), drops);
               }
            }
            
            world.removeBlockEntity(pos);
         }
         
         world.removeBlock(pos, false);
         
         super.onBreak(world, pos, state, player);
      }
      
      @Override
      public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
         BlockEntity entity = world.getBlockEntity(pos);
         if (placer instanceof ServerPlayerEntity player && entity instanceof SpawnerInfuserBlockEntity infuser) {
            initializeMagicBlock(stack,infuser);
            
            SoundUtils.soulSounds(player.getServerWorld(),pos,5,30);
            SoundUtils.playSound(world,pos,SoundEvents.BLOCK_SCULK_SHRIEKER_SHRIEK,SoundCategory.BLOCKS,1,.6f);
            player.sendMessage(Text.literal("The Infuser makes a most unsettling sound...").formatted(Formatting.DARK_GREEN),true);
         }
      }
      
      @Override
      public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
         return false;
      }
   }
}
