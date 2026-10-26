package net.borisshoes.arcananovum.test.gametest;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievement;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugment;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.blocks.GeomanticSteleBlockEntity;
import net.borisshoes.arcananovum.blocks.altars.TransmutationAltarBlockEntity;
import net.borisshoes.arcananovum.core.ArcanaBlock;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.datastorage.ArcanaPlayerData;
import net.borisshoes.arcananovum.items.charms.WildGrowthCharm;
import net.borisshoes.arcananovum.recipes.RecipeManager;
import net.borisshoes.arcananovum.recipes.transmutation.AequalisSkillTransmutationRecipe;
import net.borisshoes.arcananovum.recipes.transmutation.CommutativeTransmutationRecipe;
import net.borisshoes.arcananovum.recipes.transmutation.TransmutationRecipe;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.lang.reflect.Method;
import java.util.*;

/** One test per fixed bug, so the same break is caught if a Minecraft update brings it back */
public class RegressionGameTests {
   
   // Bone Smasher was never granted because its hook sat on a call that 26.3 only makes for named mobs
   @GameTest
   @SuppressWarnings("removal")
   public void boneSmasherGrantedForOneHitWitherKill(GameTestHelper helper){
      ServerPlayer player = helper.makeMockServerPlayerInLevel();
      player.setItemInHand(InteractionHand.MAIN_HAND, ArcanaRegistry.GRAVITON_MAUL.getNewItem());
      WitherBoss wither = helper.spawn(EntityTypes.WITHER, new BlockPos(1, 2, 1));
      wither.setInvulnerableTicks(0);
      
      wither.hurtServer(helper.getLevel(), player.damageSources().playerAttack(player), 100_000f);
      
      helper.assertTrue(wither.isDeadOrDying(), "The wither survived the hit");
      ArcanaAchievement achievement = ArcanaNovum.data(player).getAchievement(ArcanaAchievements.BONE_SMASHER);
      helper.assertTrue(achievement != null && achievement.isAcquired(), "Bone Smasher was not granted");
      helper.succeed();
   }
   
   // #minecraft:boats includes chest boats, so the boats transmutation turned a plain boat into a chest boat for free
   @GameTest
   public void boatsTransmutationExcludesChestBoats(GameTestHelper helper){
      boolean found = false;
      for(TransmutationRecipe recipe : RecipeManager.TRANSMUTATION_RECIPES){
         if(!(recipe instanceof CommutativeTransmutationRecipe commutative) || !commutative.validCommunalInput(new ItemStack(Items.OAK_BOAT))) continue;
         found = true;
         for(Item item : BuiltInRegistries.ITEM){
            ItemStack stack = item.getDefaultInstance();
            helper.assertFalse(stack.is(ItemTags.CHEST_BOATS) && commutative.validCommunalInput(stack),
                  recipe.getId() + " accepts both plain boats and " + BuiltInRegistries.ITEM.getKey(item));
         }
      }
      helper.assertTrue(found, "No commutative transmutation accepts oak boats");
      helper.succeed();
   }

   // The Charm of Wild Growth skipped bamboo unless it had Charm of Blooming
   @GameTest(skyAccess = true)
   public void wildGrowthGrowsBamboo(GameTestHelper helper){
      BlockPos sapling = new BlockPos(1, 2, 1);
      BlockPos stalk = new BlockPos(3, 2, 1);
      helper.setBlock(sapling.below(), Blocks.DIRT);
      helper.setBlock(stalk.below(), Blocks.DIRT);
      helper.setBlock(sapling, Blocks.BAMBOO_SAPLING);
      helper.setBlock(stalk, Blocks.BAMBOO);
      
      tickWildGrowth(helper, sapling);
      tickWildGrowth(helper, stalk);
      
      helper.assertBlockPresent(Blocks.BAMBOO, sapling.above());
      helper.assertBlockPresent(Blocks.BAMBOO, stalk.above());
      helper.succeed();
   }
   
   private static void tickWildGrowth(GameTestHelper helper, BlockPos relative){
      BlockPos pos = helper.absolutePos(relative);
      AABB range = new AABB(pos.getX(), pos.getY(), pos.getZ(), pos.getX(), pos.getY(), pos.getZ()); // Only ever picks this block
      ItemStack charm = ArcanaRegistry.WILD_GROWTH_CHARM.getNewItem();
      try{
         Method passiveTick = WildGrowthCharm.class.getDeclaredMethod("passiveTick", ItemStack.class, ServerLevel.class, BlockPos.class, AABB.class, ServerPlayer.class, GeomanticSteleBlockEntity.class);
         passiveTick.setAccessible(true);
         for(int i = 0; i < 100 && helper.getLevel().getBlockState(pos.above()).isAir(); i++){
            passiveTick.invoke(ArcanaRegistry.WILD_GROWTH_CHARM, charm, helper.getLevel(), pos, range, null, null);
         }
      }catch(ReflectiveOperationException e){
         throw new RuntimeException("WildGrowthCharm.passiveTick changed signature, update this test", e);
      }
   }
   
   // Aequalis skill transfers could move skill points, throw, and skip taking the reagents, so Trade Agreement ran them again
   @GameTest(maxTicks = 100)
   @SuppressWarnings("removal")
   public void aequalisSkillTransferAlwaysConsumesReagents(GameTestHelper helper){
      ServerLevel level = helper.getLevel();
      ServerPlayer player = helper.makeMockServerPlayerInLevel();
      ArcanaPlayerData profile = ArcanaNovum.data(player);
      profile.addResearchedItem(ArcanaRegistry.AEQUALIS_SCIENTIA.getId());
      
      // Two items with a few augments each, where no augment is linked to the other item (linked augments are never traded)
      ArcanaItem source = null;
      ArcanaItem destination = null;
      List<ArcanaItem> items = ArcanaRegistry.ARCANA_ITEMS.stream().filter(item -> ArcanaAugments.getAugmentsForItem(item).size() >= 3).toList();
      search:
      for(ArcanaItem a : items){
         for(ArcanaItem b : items){
            if(a != b && !linked(a, b) && !linked(b, a)){
               source = a;
               destination = b;
               break search;
            }
         }
      }
      helper.assertTrue(source != null, "Need two arcana items whose augments are not linked to each other");
      List<ArcanaAugment> augments = new ArrayList<>(ArcanaAugments.getAugmentsForItem(source));
      augments.addAll(ArcanaAugments.getAugmentsForItem(destination));
      
      BlockPos altarPos = helper.absolutePos(new BlockPos(2, 1, 2));
      TransmutationAltarBlockEntity altar = new TransmutationAltarBlockEntity(altarPos, ((ArcanaBlock) ArcanaRegistry.TRANSMUTATION_ALTAR).getBlock().defaultBlockState());
      altar.setLevel(level);
      altar.initialize(new TreeMap<>(), player.getStringUUID(), UUID.randomUUID().toString(), 0, null, null);
      AequalisSkillTransmutationRecipe recipe = new AequalisSkillTransmutationRecipe();
      Vec3 spot = Vec3.atCenterOf(altarPos.above());
      RandomSource random = level.getRandom();
      
      int transfers = 0;
      for(int run = 0; run < 50; run++){
         for(ArcanaAugment augment : augments) profile.setAugmentLevel(augment, random.nextInt(augment.getTiers().length + 1));
         Map<ArcanaAugment, Integer> before = levels(profile, augments);
         
         ItemStack aequalisStack = ArcanaRegistry.AEQUALIS_SCIENTIA.addCrafter(ArcanaRegistry.AEQUALIS_SCIENTIA.getNewItem(), player.getStringUUID(), 0, level.getServer());
         ItemEntity input1 = spawnItem(level, spot, source.getNewItem());
         ItemEntity input2 = spawnItem(level, spot, destination.getNewItem());
         ItemEntity reagent1 = spawnItem(level, spot, new ItemStack(ArcanaRegistry.STARDUST, 64));
         ItemEntity reagent2 = spawnItem(level, spot, new ItemStack(ArcanaRegistry.NEBULOUS_ESSENCE, 64));
         ItemEntity aequalis = spawnItem(level, spot, aequalisStack);
         
         recipe.doTransmutation(input1, input2, reagent1, reagent2, aequalis, altar, player);
         
         boolean changed = !before.equals(levels(profile, augments));
         if(changed) transfers++;
         helper.assertFalse(changed && !(reagent1.isRemoved() && reagent2.isRemoved()),
               "Run " + run + ": skill points moved from " + source.getId() + " to " + destination.getId() + " but the reagents were not consumed");
         for(ItemEntity entity : List.of(input1, input2, reagent1, reagent2, aequalis)) entity.discard();
      }
      helper.assertTrue(transfers > 0, "No skill transfer happened in 50 runs, so the test proved nothing");
      helper.succeed();
   }
   
   private static boolean linked(ArcanaItem item, ArcanaItem other){
      for(ArcanaAugment augment : ArcanaAugments.getAugmentsForItem(item)){
         for(ArcanaAugment link : ArcanaAugments.getLinkedAugments(augment)){
            if(link.getArcanaItem().getId().equals(other.getId())) return true;
         }
      }
      return false;
   }
   
   private static Map<ArcanaAugment, Integer> levels(ArcanaPlayerData profile, List<ArcanaAugment> augments){
      Map<ArcanaAugment, Integer> map = new HashMap<>();
      for(ArcanaAugment augment : augments) map.put(augment, profile.getAugmentLevel(augment));
      return map;
   }
   
   private static ItemEntity spawnItem(ServerLevel level, Vec3 pos, ItemStack stack){
      ItemEntity entity = new ItemEntity(level, pos.x, pos.y, pos.z, stack, 0, 0, 0);
      level.addFreshEntity(entity);
      return entity;
   }
}
