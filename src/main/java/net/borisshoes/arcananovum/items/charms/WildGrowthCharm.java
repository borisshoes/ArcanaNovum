package net.borisshoes.arcananovum.items.charms;

import net.borisshoes.arcananovum.ArcanaConfig;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerItem;
import net.borisshoes.arcananovum.gui.arcanetome.TomeGui;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaIngredient;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaRecipe;
import net.borisshoes.arcananovum.recipes.arcana.ForgeRequirement;
import net.borisshoes.arcananovum.recipes.arcana.GenericArcanaIngredient;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.arcananovum.utils.ArcanaRarity;
import net.borisshoes.arcananovum.utils.SoundUtils;
import net.borisshoes.arcananovum.utils.TextUtils;
import net.minecraft.block.*;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.TadpoleEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BoneMealItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class WildGrowthCharm extends ArcanaItem {
	public static final String ID = "wild_growth_charm";
   
   public static final String HARVEST_TAG = "harvest";
   
   private static final int[] RATES = new int[]{10, 7, 5, 3, 2};
   
   public WildGrowthCharm(){
      id = ID;
      name = "Charm of Wild Growth";
      rarity = ArcanaRarity.EXOTIC;
      categories = new TomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), TomeGui.TomeFilter.ITEMS, TomeGui.TomeFilter.CHARMS};
      itemVersion = 0;
      vanillaItem = Items.DARK_OAK_SAPLING;
      item = new WildGrowthCharmItem();
      displayName = Text.translatableWithFallback("item."+MOD_ID+"."+ID,name).formatted(Formatting.BOLD,Formatting.GREEN);
      researchTasks = new RegistryKey[]{ResearchTasks.UNLOCK_TEMPORAL_MOMENT,ResearchTasks.ADVANCEMENT_BREED_AN_ANIMAL,ResearchTasks.ADVANCEMENT_PLANT_ANY_SNIFFER_SEED,ResearchTasks.ADVANCEMENT_PLANT_SEED,ResearchTasks.ADVANCEMENT_OBTAIN_NETHERITE_HOE};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getMaxCount());
      putProperty(stack,ACTIVE_TAG,false);
      putProperty(stack,HARVEST_TAG,false);
      setPrefStack(stack);
   }
   
   @Override
   public List<Text> getItemLore(@Nullable ItemStack itemStack){
      List<MutableText> lore = new ArrayList<>();
      lore.add(Text.literal("")
            .append(Text.literal("The ").formatted(Formatting.DARK_GREEN))
            .append(Text.literal("charm").formatted(Formatting.GREEN))
            .append(Text.literal(" smells of a ").formatted(Formatting.DARK_GREEN))
            .append(Text.literal("cool").formatted(Formatting.AQUA))
            .append(Text.literal(" spring").formatted(Formatting.DARK_GREEN))
            .append(Text.literal(" breeze.").formatted(Formatting.GRAY)));
      lore.add(Text.literal("")
            .append(Text.literal("Nearby ").formatted(Formatting.DARK_GREEN))
            .append(Text.literal("plants").formatted(Formatting.GREEN))
            .append(Text.literal(" and ").formatted(Formatting.DARK_GREEN))
            .append(Text.literal("animals").formatted(Formatting.GREEN))
            .append(Text.literal(" seem to be").formatted(Formatting.DARK_GREEN))
            .append(Text.literal(" invigorated").formatted(Formatting.AQUA))
            .append(Text.literal(" by its ").formatted(Formatting.DARK_GREEN))
            .append(Text.literal("proximity").formatted(Formatting.GREEN))
            .append(Text.literal(".").formatted(Formatting.DARK_GREEN)));
      lore.add(Text.literal("")
            .append(Text.literal("Sneak Right Click").formatted(Formatting.AQUA))
            .append(Text.literal(" the ").formatted(Formatting.DARK_GREEN))
            .append(Text.literal("charm ").formatted(Formatting.GREEN))
            .append(Text.literal("to toggle ").formatted(Formatting.DARK_GREEN))
            .append(Text.literal("its ").formatted(Formatting.DARK_GREEN))
            .append(Text.literal("effect").formatted(Formatting.GREEN))
            .append(Text.literal(".").formatted(Formatting.DARK_GREEN)));
     return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
	protected ArcanaRecipe makeRecipe(){
      ArcanaIngredient a = new ArcanaIngredient(Items.SWEET_BERRIES,16);
      ArcanaIngredient b = new ArcanaIngredient(Items.JUNGLE_SAPLING,16);
      ArcanaIngredient c = new ArcanaIngredient(Items.SEA_PICKLE,16);
      ArcanaIngredient d = new ArcanaIngredient(Items.ACACIA_SAPLING,16);
      ArcanaIngredient e = new ArcanaIngredient(Items.CARROT,16);
      ArcanaIngredient f = new ArcanaIngredient(Items.BIRCH_SAPLING,16);
      ArcanaIngredient g = new ArcanaIngredient(Items.TORCHFLOWER,16);
      ArcanaIngredient h = new ArcanaIngredient(Items.BONE_BLOCK,8);
      ArcanaIngredient i = new ArcanaIngredient(Items.RED_MUSHROOM,16);
      ArcanaIngredient j = new ArcanaIngredient(Items.CHERRY_SAPLING,16);
      ArcanaIngredient k = new ArcanaIngredient(Items.VINE,16);
      GenericArcanaIngredient m = new GenericArcanaIngredient(ArcanaRegistry.TEMPORAL_MOMENT,1);
      ArcanaIngredient o = new ArcanaIngredient(Items.BAMBOO,16);
      ArcanaIngredient p = new ArcanaIngredient(Items.SPRUCE_SAPLING,16);
      ArcanaIngredient q = new ArcanaIngredient(Items.BROWN_MUSHROOM,16);
      ArcanaIngredient s = new ArcanaIngredient(Items.PITCHER_PLANT,16);
      ArcanaIngredient t = new ArcanaIngredient(Items.DARK_OAK_SAPLING,16);
      ArcanaIngredient u = new ArcanaIngredient(Items.POTATO,16);
      ArcanaIngredient v = new ArcanaIngredient(Items.OAK_SAPLING,16);
      ArcanaIngredient w = new ArcanaIngredient(Items.SUGAR_CANE,16);
      ArcanaIngredient x = new ArcanaIngredient(Items.MANGROVE_PROPAGULE,16);
      ArcanaIngredient y = new ArcanaIngredient(Items.GLOW_BERRIES,16);
      
      ArcanaIngredient[][] ingredients = {
            {a,b,c,d,e},
            {f,g,h,i,j},
            {k,h,m,h,o},
            {p,q,h,s,t},
            {u,v,w,x,y}};
      return new ArcanaRecipe(ingredients,new ForgeRequirement());
   }
   
   @Override
   public List<List<Text>> getBookLore(){
      List<List<Text>> list = new ArrayList<>();
      list.add(List.of(Text.literal("      Charm of\n     Wild Growth").formatted(Formatting.GREEN,Formatting.BOLD),Text.literal("\nRarity: ").formatted(Formatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(),false)),Text.literal("\nLiving things constantly grow and change with time. It should be possible to accelerate this process in all organisms as long as adequate nutrients are supplied with a \n").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal("      Charm of\n     Wild Growth").formatted(Formatting.GREEN,Formatting.BOLD),Text.literal("\nfew temporal tricks.\n\nThe Charm causes nearby animals and plants to grow faster.\nThe Charm also causes animals to breed periodically.\n\nSneak Using toggles the Charm’s effect.\n").formatted(Formatting.BLACK)));
      return list;
   }
   
   public class WildGrowthCharmItem extends ArcanaPolymerItem {
      public WildGrowthCharmItem(){
         super(getThis());
      }
      
      @Override
      public ItemStack getPolymerItemStack(ItemStack itemStack, TooltipType tooltipType, PacketContext context){
         ItemStack baseStack = super.getPolymerItemStack(itemStack, tooltipType, context);
         if(!ArcanaItemUtils.isArcane(itemStack)) return baseStack;
         boolean active = getBooleanProperty(itemStack,ACTIVE_TAG);
         
         List<String> stringList = new ArrayList<>();
         if(active){
            stringList.add("on");
         }else{
            stringList.add("off");
         }
         baseStack.set(DataComponentTypes.CUSTOM_MODEL_DATA,new CustomModelDataComponent(new ArrayList<>(),new ArrayList<>(),stringList,new ArrayList<>()));
         return baseStack;
      }
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
      
      @Override
      public void inventoryTick(ItemStack stack, ServerWorld world, Entity entity, @Nullable EquipmentSlot slot){
         if(!ArcanaItemUtils.isArcane(stack)) return;
         if(!(world instanceof ServerWorld serverWorld && entity instanceof ServerPlayerEntity player)) return;
         
         try{
            boolean active = getBooleanProperty(stack,ACTIVE_TAG);
            boolean harvest = getBooleanProperty(stack,HARVEST_TAG);
            int fertLvl = Math.max(ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.FERTILIZATION.id),0);
            int tickTime = RATES[fertLvl];
            
            if(active && !world.isClient && player.getServer().getTicks() % tickTime == 0){
               boolean bloom = ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.CHARM_OF_BLOOMING.id) >= 1;
               int reaping = ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.REAPING.id);
               
               int count = 0;
               for(BlockPos blockPos : BlockPos.iterateRandomly(player.getRandom(), 100, player.getBlockPos(), 4)){
                  BlockState state = world.getBlockState(blockPos);
                  Block block = state.getBlock();
                  if(count >= 2) break;
                  count++;
                  
                  if(block instanceof ShortPlantBlock){
                     count--;
                  }else if(block instanceof SugarCaneBlock ||
                        block instanceof NetherWartBlock ||
                        block instanceof CactusBlock ||
                        block instanceof ChorusFlowerBlock ||
                        block instanceof OxidizableBlock ||
                        block instanceof BuddingAmethystBlock){
                     state.randomTick(player.getServerWorld(),blockPos,world.getRandom());
                     world.syncWorldEvent(WorldEvents.BONE_MEAL_USED, blockPos, 15);
                  }else if((block instanceof AbstractPlantStemBlock ||
                        block instanceof CropBlock ||
                        block instanceof CocoaBlock ||
                        block instanceof StemBlock ||
                        block instanceof SweetBerryBushBlock) && BoneMealItem.useOnFertilizable(new ItemStack(Items.BONE_MEAL,64), world, blockPos)){
                     world.syncWorldEvent(WorldEvents.BONE_MEAL_USED, blockPos, 15);
                     
                     if(world.getBlockState(blockPos).getBlock() instanceof CropBlock crop && crop.isMature(world.getBlockState(blockPos))){
                        ArcanaAchievements.progress(player,ArcanaAchievements.BOUNTIFUL_HARVEST.id,1);
                        ArcanaNovum.data(player).addXP(reaping >= 2 && harvest ? ArcanaConfig.getInt(ArcanaRegistry.WILD_GROWTH_CHARM_PER_REAPED_CROP) : ArcanaConfig.getInt(ArcanaRegistry.WILD_GROWTH_CHARM_PER_MATURE_CROP)); // Add xp
                     }
                  }else if(bloom && BoneMealItem.useOnFertilizable(new ItemStack(Items.BONE_MEAL,64), world, blockPos)){
                     world.syncWorldEvent(WorldEvents.BONE_MEAL_USED, blockPos, 15);
                  }else if(bloom && BoneMealItem.useOnGround(new ItemStack(Items.BONE_MEAL,64), world, blockPos, Direction.random(player.getRandom()))){
                     world.syncWorldEvent(WorldEvents.BONE_MEAL_USED, blockPos, 15);
                  }else{
                     count--;
                  }
                  
                  if(reaping >= 1 && harvest){
                     if(block instanceof CropBlock crop && crop.isMature(world.getBlockState(blockPos))){
                        world.breakBlock(blockPos,true,player);
                        if(reaping >= 2 && crop.canPlaceAt(world.getBlockState(blockPos.down()),world,blockPos)){
                           world.setBlockState(blockPos,block.getDefaultState());
                        }
                     }else if(block instanceof NetherWartBlock wart && world.getBlockState(blockPos).get(NetherWartBlock.AGE) == 3){
                        world.breakBlock(blockPos,true,player);
                        if(reaping >= 2 && wart.canPlaceAt(world.getBlockState(blockPos.down()),world,blockPos)){
                           world.setBlockState(blockPos,block.getDefaultState());
                        }
                     }
                  }
               }
               
               List<Entity> animals = world.getOtherEntities(player,player.getBoundingBox().expand(5), e -> e instanceof AnimalEntity || e instanceof TadpoleEntity);
               Collections.shuffle(animals);
               for(Entity a : animals){
                  
                  if(a instanceof AnimalEntity animal){
                     int i = animal.getBreedingAge();
                     if(!animal.getWorld().isClient && i == 0 && animal.canEat()){
                        animal.lovePlayer(player);
                        break;
                     }
                     if(animal.isBaby()){
                        animal.growUp(AnimalEntity.toGrowUpAge(-i), true);
                        serverWorld.spawnParticles(ParticleTypes.COMPOSTER,animal.getX(),animal.getY()+animal.getHeight()*0.5,animal.getZ(),3,0.2,0.2,0.2,1);
                        break;
                     }
                  }else if(a instanceof TadpoleEntity tadpole){
                     tadpole.increaseAge(PassiveEntity.toGrowUpAge(tadpole.getTicksUntilGrowth()));
                     serverWorld.spawnParticles(ParticleTypes.COMPOSTER,tadpole.getX(),tadpole.getY()+tadpole.getHeight()*0.5,tadpole.getZ(),3,0.2,0.2,0.2,1);
                     break;
                  }
               }
               
               if(animals.stream().filter(e -> (e instanceof AnimalEntity animal && animal.isBaby())).count() >= 5){
                  ArcanaAchievements.grant(player,ArcanaAchievements.THEY_GROW_UP_SO_FAST.id);
               }
               
               if(count >= 2){
                  ArcanaNovum.data(player).addXP(ArcanaConfig.getInt(ArcanaRegistry.WILD_GROWTH_CHARM_PASSIVE)); // Add xp
               }
            }
         }catch(Exception ignored){
            // Torchflowers are crops but without an age, so they kinda just crash you if you call isMature on them...
            // TODO there is probably a better way to deal with this...
         }
      }
      
      @Override
      public ActionResult use(World world, PlayerEntity playerEntity, Hand hand){
         ItemStack stack = playerEntity.getStackInHand(hand);
         if(!(playerEntity instanceof ServerPlayerEntity player)) return ActionResult.PASS;
         boolean active = getBooleanProperty(stack,ACTIVE_TAG);
         boolean harvest = getBooleanProperty(stack,HARVEST_TAG);
         int reaping = ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.REAPING.id);
         
         if(playerEntity.isSneaking()){
            if(reaping > 0 && hand == Hand.OFF_HAND){
               harvest = !harvest;
               putProperty(stack,HARVEST_TAG,harvest);
               if(harvest){
                  player.sendMessage(Text.literal("The Charm Begins to Reap").formatted(Formatting.GREEN,Formatting.ITALIC),true);
                  SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_GRASS_PLACE, .7f,.7f);
               }else{
                  player.sendMessage(Text.literal("The Charm Ceases Reaping").formatted(Formatting.GREEN,Formatting.ITALIC),true);
                  SoundUtils.playSongToPlayer(player, SoundEvents.ITEM_BONE_MEAL_USE, 2f,.5f);
               }
            }else{
               active = !active;
               putProperty(stack,ACTIVE_TAG,active);
               if(active){
                  player.sendMessage(Text.literal("The Charm Begins to Bloom").formatted(Formatting.GREEN,Formatting.ITALIC),true);
                  SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_GRASS_PLACE, .7f,.7f);
               }else{
                  player.sendMessage(Text.literal("The Charm Recedes").formatted(Formatting.GREEN,Formatting.ITALIC),true);
                  SoundUtils.playSongToPlayer(player, SoundEvents.ITEM_BONE_MEAL_USE, 2f,.5f);
               }
            }
            return ActionResult.SUCCESS_SERVER;
         }
         return ActionResult.PASS;
      }
      
      @Override
      public ActionResult useOnBlock(ItemUsageContext context){
         PlayerEntity playerEntity = context.getPlayer();
         World world = context.getWorld();
         BlockPos blockPos = context.getBlockPos();
         BlockPos blockPos2 = blockPos.offset(context.getSide());
         ItemStack stack = context.getStack();
         
         if(playerEntity != null && playerEntity.isSneaking() && playerEntity instanceof ServerPlayerEntity player){
            boolean active = getBooleanProperty(stack,ACTIVE_TAG);
            
            active = !active;
            putProperty(stack,ACTIVE_TAG,active);
            if(active){
               playerEntity.sendMessage(Text.literal("The Charm Begins to Bloom").formatted(Formatting.GREEN,Formatting.ITALIC),true);
               SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_GRASS_PLACE, .7f,.7f);
            }else{
               playerEntity.sendMessage(Text.literal("The Charm Recedes").formatted(Formatting.GREEN,Formatting.ITALIC),true);
               SoundUtils.playSongToPlayer(player, SoundEvents.ITEM_BONE_MEAL_USE, 2f,.5f);
            }
            return ActionResult.SUCCESS_SERVER;
         }
         
         if(ArcanaAugments.getAugmentOnItem(context.getStack(),ArcanaAugments.CHARM_OF_BLOOMING.id) < 1){
            return ActionResult.PASS;
         }
         
         if(BoneMealItem.useOnFertilizable(new ItemStack(Items.BONE_MEAL,64), world, blockPos)){
            if(!world.isClient){
               world.syncWorldEvent(WorldEvents.BONE_MEAL_USED, blockPos, 15);
            }
            return ActionResult.SUCCESS_SERVER;
         }
         BlockState blockState = world.getBlockState(blockPos);
         boolean bl = blockState.isSideSolidFullSquare(world, blockPos, context.getSide());
         if(bl && BoneMealItem.useOnGround(new ItemStack(Items.BONE_MEAL,64), world, blockPos2, context.getSide())){
            if(!world.isClient){
               world.syncWorldEvent(WorldEvents.BONE_MEAL_USED, blockPos2, 15);
            }
            return ActionResult.SUCCESS_SERVER;
         }
         return ActionResult.PASS;
      }
   }
}

