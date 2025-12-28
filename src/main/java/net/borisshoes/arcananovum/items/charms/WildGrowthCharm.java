package net.borisshoes.arcananovum.items.charms;

import net.borisshoes.arcananovum.ArcanaConfig;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerItem;
import net.borisshoes.arcananovum.gui.arcanetome.TomeGui;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaIngredient;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaRecipe;
import net.borisshoes.arcananovum.recipes.arcana.ForgeRequirement;
import net.borisshoes.arcananovum.recipes.arcana.GenericArcanaIngredient;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.borislib.utils.SoundUtils;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.frog.Tadpole;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
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
      displayName = Component.translatableWithFallback("item."+MOD_ID+"."+ID,name).withStyle(ChatFormatting.BOLD, ChatFormatting.GREEN);
      researchTasks = new ResourceKey[]{ResearchTasks.UNLOCK_TEMPORAL_MOMENT,ResearchTasks.ADVANCEMENT_BREED_AN_ANIMAL,ResearchTasks.ADVANCEMENT_PLANT_ANY_SNIFFER_SEED,ResearchTasks.ADVANCEMENT_PLANT_SEED,ResearchTasks.ADVANCEMENT_OBTAIN_NETHERITE_HOE};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getDefaultMaxStackSize());
      putProperty(stack,ACTIVE_TAG,false);
      putProperty(stack,HARVEST_TAG,false);
      setPrefStack(stack);
   }
   
   @Override
   public List<Component> getItemLore(@Nullable ItemStack itemStack){
      List<MutableComponent> lore = new ArrayList<>();
      lore.add(Component.literal("")
            .append(Component.literal("The ").withStyle(ChatFormatting.DARK_GREEN))
            .append(Component.literal("charm").withStyle(ChatFormatting.GREEN))
            .append(Component.literal(" smells of a ").withStyle(ChatFormatting.DARK_GREEN))
            .append(Component.literal("cool").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(" spring").withStyle(ChatFormatting.DARK_GREEN))
            .append(Component.literal(" breeze.").withStyle(ChatFormatting.GRAY)));
      lore.add(Component.literal("")
            .append(Component.literal("Nearby ").withStyle(ChatFormatting.DARK_GREEN))
            .append(Component.literal("plants").withStyle(ChatFormatting.GREEN))
            .append(Component.literal(" and ").withStyle(ChatFormatting.DARK_GREEN))
            .append(Component.literal("animals").withStyle(ChatFormatting.GREEN))
            .append(Component.literal(" seem to be").withStyle(ChatFormatting.DARK_GREEN))
            .append(Component.literal(" invigorated").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(" by its ").withStyle(ChatFormatting.DARK_GREEN))
            .append(Component.literal("proximity").withStyle(ChatFormatting.GREEN))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_GREEN)));
      lore.add(Component.literal("")
            .append(Component.literal("Sneak Right Click").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(" the ").withStyle(ChatFormatting.DARK_GREEN))
            .append(Component.literal("charm ").withStyle(ChatFormatting.GREEN))
            .append(Component.literal("to toggle ").withStyle(ChatFormatting.DARK_GREEN))
            .append(Component.literal("its ").withStyle(ChatFormatting.DARK_GREEN))
            .append(Component.literal("effect").withStyle(ChatFormatting.GREEN))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_GREEN)));
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
      return new ArcanaRecipe(this, ingredients,new ForgeRequirement());
   }
   
   @Override
   public List<List<Component>> getBookLore(){
      List<List<Component>> list = new ArrayList<>();
      list.add(List.of(Component.literal("      Charm of\n     Wild Growth").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD), Component.literal("\nRarity: ").withStyle(ChatFormatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(),false)), Component.literal("\nLiving things constantly grow and change with time. It should be possible to accelerate this process in all organisms as long as adequate nutrients are supplied with a \n").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("      Charm of\n     Wild Growth").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD), Component.literal("\nfew temporal tricks.\n\nThe Charm causes nearby animals and plants to grow faster.\nThe Charm also causes animals to breed periodically.\n\nSneak Using toggles the Charm’s effect.\n").withStyle(ChatFormatting.BLACK)));
      return list;
   }
   
   public class WildGrowthCharmItem extends ArcanaPolymerItem {
      public WildGrowthCharmItem(){
         super(getThis());
      }
      
      @Override
      public ItemStack getPolymerItemStack(ItemStack itemStack, TooltipFlag tooltipType, PacketContext context){
         ItemStack baseStack = super.getPolymerItemStack(itemStack, tooltipType, context);
         if(!ArcanaItemUtils.isArcane(itemStack)) return baseStack;
         boolean active = getBooleanProperty(itemStack,ACTIVE_TAG);
         
         List<String> stringList = new ArrayList<>();
         if(active){
            stringList.add("on");
         }else{
            stringList.add("off");
         }
         baseStack.set(DataComponents.CUSTOM_MODEL_DATA,new CustomModelData(new ArrayList<>(),new ArrayList<>(),stringList,new ArrayList<>()));
         return baseStack;
      }
      
      @Override
      public ItemStack getDefaultInstance(){
         return prefItem;
      }
      
      @Override
      public void inventoryTick(ItemStack stack, ServerLevel world, Entity entity, @Nullable EquipmentSlot slot){
         if(!ArcanaItemUtils.isArcane(stack)) return;
         if(!(world instanceof ServerLevel serverWorld && entity instanceof ServerPlayer player)) return;
         
         try{
            boolean active = getBooleanProperty(stack,ACTIVE_TAG);
            boolean harvest = getBooleanProperty(stack,HARVEST_TAG);
            int fertLvl = Math.max(ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.FERTILIZATION.id),0);
            int tickTime = RATES[fertLvl];
            
            if(active && !world.isClientSide() && player.level().getServer().getTickCount() % tickTime == 0){
               boolean bloom = ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.CHARM_OF_BLOOMING.id) >= 1;
               int reaping = ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.REAPING.id);
               
               int count = 0;
               for(BlockPos blockPos : BlockPos.randomInCube(player.getRandom(), 100, player.blockPosition(), 4)){
                  BlockState state = world.getBlockState(blockPos);
                  Block block = state.getBlock();
                  if(count >= 2) break;
                  count++;
                  Vec3 blockCenter = blockPos.getCenter();
                  
                  if(block instanceof TallGrassBlock){
                     count--;
                  }else if(block instanceof SugarCaneBlock ||
                        block instanceof NetherWartBlock ||
                        block instanceof CactusBlock ||
                        block instanceof ChorusFlowerBlock ||
                        block instanceof StemBlock){
                     state.randomTick(player.level(),blockPos,world.getRandom());
                     world.levelEvent(LevelEvent.PARTICLES_AND_SOUND_PLANT_GROWTH, blockPos, 15);
                     world.sendParticles(ParticleTypes.HAPPY_VILLAGER, blockCenter.x,blockCenter.y,blockCenter.z,5,0.5,0.5,0.5,1);
                  }else if(block instanceof WeatheringCopper){
                     state.randomTick(player.level(),blockPos,world.getRandom());
                     world.sendParticles(ParticleTypes.HAPPY_VILLAGER, blockCenter.x,blockCenter.y,blockCenter.z,5,0.5,0.5,0.5,1);
                  }else if(block instanceof BuddingAmethystBlock){
                     for(int i = 0; i < 10; i++){
                        state.randomTick(player.level(),blockPos,world.getRandom());
                     }
                     world.sendParticles(ParticleTypes.HAPPY_VILLAGER, blockCenter.x,blockCenter.y,blockCenter.z,5,0.5,0.5,0.5,1);
                  }else if((block instanceof GrowingPlantHeadBlock ||
                        block instanceof CropBlock ||
                        block instanceof CocoaBlock ||
                        block instanceof StemBlock ||
                        block instanceof SweetBerryBushBlock) && BoneMealItem.growCrop(new ItemStack(Items.BONE_MEAL,64), world, blockPos)){
                     world.levelEvent(LevelEvent.PARTICLES_AND_SOUND_PLANT_GROWTH, blockPos, 15);
                     world.sendParticles(ParticleTypes.HAPPY_VILLAGER, blockCenter.x,blockCenter.y,blockCenter.z,5,0.5,0.5,0.5,1);
                     if(world.getBlockState(blockPos).getBlock() instanceof CropBlock crop && crop.isMaxAge(world.getBlockState(blockPos))){
                        ArcanaAchievements.progress(player,ArcanaAchievements.BOUNTIFUL_HARVEST.id,1);
                        ArcanaNovum.data(player).addXP(reaping >= 2 && harvest ? ArcanaNovum.CONFIG.getInt(ArcanaRegistry.WILD_GROWTH_CHARM_PER_REAPED_CROP) : ArcanaNovum.CONFIG.getInt(ArcanaRegistry.WILD_GROWTH_CHARM_PER_MATURE_CROP)); // Add xp
                     }
                  }else if(bloom && BoneMealItem.growCrop(new ItemStack(Items.BONE_MEAL,64), world, blockPos)){
                     world.levelEvent(LevelEvent.PARTICLES_AND_SOUND_PLANT_GROWTH, blockPos, 15);
                     world.sendParticles(ParticleTypes.HAPPY_VILLAGER, blockCenter.x,blockCenter.y,blockCenter.z,2,0.5,0.5,0.5,1);
                  }else if(bloom && BoneMealItem.growWaterPlant(new ItemStack(Items.BONE_MEAL,64), world, blockPos, Direction.getRandom(player.getRandom()))){
                     world.levelEvent(LevelEvent.PARTICLES_AND_SOUND_PLANT_GROWTH, blockPos, 15);
                     world.sendParticles(ParticleTypes.HAPPY_VILLAGER, blockCenter.x,blockCenter.y,blockCenter.z,2,0.5,0.5,0.5,1);
                  }else{
                     count--;
                  }
                  
                  if(reaping >= 1 && harvest){
                     if(block instanceof CropBlock crop && crop.isMaxAge(world.getBlockState(blockPos))){
                        world.destroyBlock(blockPos,true,player);
                        if(reaping >= 2 && crop.canSurvive(world.getBlockState(blockPos.below()),world,blockPos)){
                           world.setBlockAndUpdate(blockPos,block.defaultBlockState());
                        }
                        world.sendParticles(ParticleTypes.POOF, blockCenter.x,blockCenter.y,blockCenter.z,3,0.5,0.5,0.5,0.05);
                     }else if(block instanceof NetherWartBlock wart && world.getBlockState(blockPos).getValue(NetherWartBlock.AGE) == 3){
                        world.destroyBlock(blockPos,true,player);
                        if(reaping >= 2 && wart.canSurvive(world.getBlockState(blockPos.below()),world,blockPos)){
                           world.setBlockAndUpdate(blockPos,block.defaultBlockState());
                        }
                        world.sendParticles(ParticleTypes.POOF, blockCenter.x,blockCenter.y,blockCenter.z,3,0.5,0.5,0.5,0.05);
                     }
                  }
               }
               
               List<Entity> animals = world.getEntities(player,player.getBoundingBox().inflate(5), e -> e instanceof Animal || e instanceof Tadpole);
               Collections.shuffle(animals);
               for(Entity a : animals){
                  
                  if(a instanceof Animal animal){
                     int i = animal.getAge();
                     if(!animal.level().isClientSide() && i == 0 && animal.canFallInLove()){
                        animal.setInLove(player);
                        break;
                     }
                     if(animal.isBaby()){
                        animal.ageUp(Animal.getSpeedUpSecondsWhenFeeding(-i), true);
                        serverWorld.sendParticles(ParticleTypes.COMPOSTER,animal.getX(),animal.getY()+animal.getBbHeight()*0.5,animal.getZ(),3,0.2,0.2,0.2,1);
                        break;
                     }
                  }else if(a instanceof Tadpole tadpole){
                     tadpole.ageUp(AgeableMob.getSpeedUpSecondsWhenFeeding(tadpole.getTicksLeftUntilAdult()));
                     serverWorld.sendParticles(ParticleTypes.COMPOSTER,tadpole.getX(),tadpole.getY()+tadpole.getBbHeight()*0.5,tadpole.getZ(),3,0.2,0.2,0.2,1);
                     break;
                  }
               }
               
               if(animals.stream().filter(e -> (e instanceof Animal animal && animal.isBaby())).count() >= 5){
                  ArcanaAchievements.grant(player,ArcanaAchievements.THEY_GROW_UP_SO_FAST.id);
               }
               
               if(count >= 2){
                  ArcanaNovum.data(player).addXP(ArcanaNovum.CONFIG.getInt(ArcanaRegistry.WILD_GROWTH_CHARM_PASSIVE)); // Add xp
               }
            }
         }catch(Exception ignored){
            // Torchflowers are crops but without an age, so they kinda just crash you if you call isMature on them...
            // TODO there is probably a better way to deal with this...
         }
      }
      
      @Override
      public InteractionResult use(Level world, Player playerEntity, InteractionHand hand){
         ItemStack stack = playerEntity.getItemInHand(hand);
         if(!(playerEntity instanceof ServerPlayer player)) return InteractionResult.PASS;
         boolean active = getBooleanProperty(stack,ACTIVE_TAG);
         boolean harvest = getBooleanProperty(stack,HARVEST_TAG);
         int reaping = ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.REAPING.id);
         
         if(playerEntity.isShiftKeyDown()){
            if(reaping > 0 && hand == InteractionHand.OFF_HAND){
               harvest = !harvest;
               putProperty(stack,HARVEST_TAG,harvest);
               if(harvest){
                  player.displayClientMessage(Component.literal("The Charm Begins to Reap").withStyle(ChatFormatting.GREEN, ChatFormatting.ITALIC),true);
                  SoundUtils.playSongToPlayer(player, SoundEvents.GRASS_PLACE, .7f,.7f);
               }else{
                  player.displayClientMessage(Component.literal("The Charm Ceases Reaping").withStyle(ChatFormatting.GREEN, ChatFormatting.ITALIC),true);
                  SoundUtils.playSongToPlayer(player, SoundEvents.BONE_MEAL_USE, 2f,.5f);
               }
            }else{
               active = !active;
               putProperty(stack,ACTIVE_TAG,active);
               if(active){
                  player.displayClientMessage(Component.literal("The Charm Begins to Bloom").withStyle(ChatFormatting.GREEN, ChatFormatting.ITALIC),true);
                  SoundUtils.playSongToPlayer(player, SoundEvents.GRASS_PLACE, .7f,.7f);
               }else{
                  player.displayClientMessage(Component.literal("The Charm Recedes").withStyle(ChatFormatting.GREEN, ChatFormatting.ITALIC),true);
                  SoundUtils.playSongToPlayer(player, SoundEvents.BONE_MEAL_USE, 2f,.5f);
               }
            }
            return InteractionResult.SUCCESS_SERVER;
         }
         return InteractionResult.PASS;
      }
      
      @Override
      public InteractionResult useOn(UseOnContext context){
         Player playerEntity = context.getPlayer();
         Level world = context.getLevel();
         BlockPos blockPos = context.getClickedPos();
         BlockPos blockPos2 = blockPos.relative(context.getClickedFace());
         ItemStack stack = context.getItemInHand();
         
         if(playerEntity != null && playerEntity.isShiftKeyDown() && playerEntity instanceof ServerPlayer player){
            boolean active = getBooleanProperty(stack,ACTIVE_TAG);
            
            active = !active;
            putProperty(stack,ACTIVE_TAG,active);
            if(active){
               playerEntity.displayClientMessage(Component.literal("The Charm Begins to Bloom").withStyle(ChatFormatting.GREEN, ChatFormatting.ITALIC),true);
               SoundUtils.playSongToPlayer(player, SoundEvents.GRASS_PLACE, .7f,.7f);
            }else{
               playerEntity.displayClientMessage(Component.literal("The Charm Recedes").withStyle(ChatFormatting.GREEN, ChatFormatting.ITALIC),true);
               SoundUtils.playSongToPlayer(player, SoundEvents.BONE_MEAL_USE, 2f,.5f);
            }
            return InteractionResult.SUCCESS_SERVER;
         }
         
         if(ArcanaAugments.getAugmentOnItem(context.getItemInHand(),ArcanaAugments.CHARM_OF_BLOOMING.id) < 1){
            return InteractionResult.PASS;
         }
         
         if(BoneMealItem.growCrop(new ItemStack(Items.BONE_MEAL,64), world, blockPos)){
            if(!world.isClientSide()){
               world.levelEvent(LevelEvent.PARTICLES_AND_SOUND_PLANT_GROWTH, blockPos, 15);
            }
            return InteractionResult.SUCCESS_SERVER;
         }
         BlockState blockState = world.getBlockState(blockPos);
         boolean bl = blockState.isFaceSturdy(world, blockPos, context.getClickedFace());
         if(bl && BoneMealItem.growWaterPlant(new ItemStack(Items.BONE_MEAL,64), world, blockPos2, context.getClickedFace())){
            if(!world.isClientSide()){
               world.levelEvent(LevelEvent.PARTICLES_AND_SOUND_PLANT_GROWTH, blockPos2, 15);
            }
            return InteractionResult.SUCCESS_SERVER;
         }
         return InteractionResult.PASS;
      }
   }
}

