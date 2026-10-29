package net.borisshoes.arcananovum.test.gametest;

import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Either;
import io.netty.channel.embedded.EmbeddedChannel;
import net.borisshoes.arcananovum.ArcanaConfig;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievement;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.achievements.ConditionalsAchievement;
import net.borisshoes.arcananovum.achievements.TimedAchievement;
import net.borisshoes.arcananovum.areaeffects.AlchemicalArrowAreaEffectTracker;
import net.borisshoes.arcananovum.augments.ArcanaAugment;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.blocks.ContinuumAnchor;
import net.borisshoes.arcananovum.blocks.GeomanticSteleBlockEntity;
import net.borisshoes.arcananovum.blocks.altars.CelestialAltarBlockEntity;
import net.borisshoes.arcananovum.blocks.altars.TransmutationAltarBlockEntity;
import net.borisshoes.arcananovum.callbacks.VengeanceTotemTimerCallback;
import net.borisshoes.arcananovum.core.ArcanaBlock;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.datastorage.ArcanaPlayerData;
import net.borisshoes.arcananovum.events.TetherFlightEvent;
import net.borisshoes.arcananovum.items.Soulstone;
import net.borisshoes.arcananovum.items.charms.CindersCharm;
import net.borisshoes.arcananovum.items.arrows.TetherArrows;
import net.borisshoes.arcananovum.items.charms.WildGrowthCharm;
import net.borisshoes.arcananovum.recipes.RecipeManager;
import net.borisshoes.arcananovum.recipes.transmutation.AequalisSkillTransmutationRecipe;
import net.borisshoes.arcananovum.recipes.transmutation.CommutativeTransmutationRecipe;
import net.borisshoes.arcananovum.recipes.transmutation.TransmutationRecipe;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.borislib.BorisLib;
import net.borisshoes.borislib.conditions.ConditionInstance;
import net.borisshoes.borislib.conditions.Conditions;
import net.borisshoes.borislib.config.ConfigValue;
import net.borisshoes.borislib.config.IConfigSetting;
import net.borisshoes.borislib.utils.MinecraftUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.ServerboundCustomClickActionPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerLoadedPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.permissions.LevelBasedPermissionSet;
import net.minecraft.server.players.NameAndId;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.clock.WorldClock;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.fish.Cod;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;

import java.lang.reflect.Field;
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
   
   // A wood block crafts into more planks than a log, so logs and wood need separate transmutations.
   // #minecraft:logs holds both, so a new wood type that misses the c:natural_woods / c:stripped_woods tags would leak into logs.
   @GameTest
   public void logsAndWoodTransmuteSeparately(GameTestHelper helper){
      CommutativeTransmutationRecipe logs = commutative("logs");
      CommutativeTransmutationRecipe wood = commutative("wood");
      helper.assertTrue(logs != null && wood != null, "The logs and wood transmutations both need to exist");
      List<String> problems = new ArrayList<>();
      for(Item item : BuiltInRegistries.ITEM){
         ItemStack stack = item.getDefaultInstance();
         if(!stack.is(ItemTags.LOGS)) continue;
         String id = BuiltInRegistries.ITEM.getKey(item).getPath();
         boolean isWood = id.endsWith("_wood") || id.endsWith("_hyphae");
         CommutativeTransmutationRecipe expected = isWood ? wood : logs;
         CommutativeTransmutationRecipe other = isWood ? logs : wood;
         if(!expected.validCommunalInput(stack)) problems.add(id + " is missing from " + expected.getId());
         if(other.validCommunalInput(stack)) problems.add(id + " is also accepted by " + other.getId());
      }
      helper.assertTrue(problems.isEmpty(), String.join("\n", problems));
      helper.succeed();
   }
   
   private static CommutativeTransmutationRecipe commutative(String id){
      for(TransmutationRecipe recipe : RecipeManager.TRANSMUTATION_RECIPES){
         if(recipe instanceof CommutativeTransmutationRecipe commutative && commutative.getId().equals(id)) return commutative;
      }
      return null;
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
   
   // The 26.3 port cancelled all of FarmlandBlock.fallOn, so a player carrying the charm took no fall damage on farmland
   @GameTest
   public void wildGrowthCharmStopsTrampleButNotFallDamage(GameTestHelper helper){
      BlockPos farmland = new BlockPos(1, 1, 1);
      helper.setBlock(farmland, Blocks.FARMLAND);
      ServerPlayer withoutCharm = survivalPlayer(helper, farmland.above());
      fallOn(helper, farmland, withoutCharm);
      helper.assertBlockPresent(Blocks.DIRT, farmland); // Makes sure this fall tramples at all
      
      helper.setBlock(farmland, Blocks.FARMLAND);
      ServerPlayer player = survivalPlayer(helper, farmland.above());
      player.getInventory().add(ArcanaRegistry.WILD_GROWTH_CHARM.getNewItem());
      fallOn(helper, farmland, player);
      helper.assertBlockPresent(Blocks.FARMLAND, farmland);
      helper.assertTrue(player.getHealth() < player.getMaxHealth(), "The fall onto farmland did no damage");
      finish(helper, withoutCharm, player);
   }
   
   private static void fallOn(GameTestHelper helper, BlockPos relative, ServerPlayer player){
      BlockPos pos = helper.absolutePos(relative);
      BlockState state = helper.getLevel().getBlockState(pos);
      state.getBlock().fallOn(helper.getLevel(), state, pos, player, 10.0);
   }
   
   // With active Greaves, pick-block for an item the Greaves don't hold returned an empty slot, so vanilla selected it
   @GameTest
   public void greavesPickBlockKeepsSlotWhenGreavesAreEmpty(GameTestHelper helper){
      ServerPlayer player = survivalPlayer(helper, new BlockPos(1, 2, 1));
      ItemStack greaves = ArcanaRegistry.GREAVES_OF_GAIALTUS.getNewItem();
      ArcanaItem.putProperty(greaves, ArcanaItem.ACTIVE_TAG, true);
      greaves.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(List.of(new ItemStack(Items.STONE, 64))));
      player.setItemSlot(EquipmentSlot.LEGS, greaves);
      player.getInventory().setItem(0, new ItemStack(Items.STICK));
      player.getInventory().setSelectedSlot(0);
      
      tryPickItem(player, new ItemStack(Items.DIRT));
      helper.assertTrue(player.getInventory().getSelectedSlot() == 0 && player.getMainHandItem().is(Items.STICK),
            "Picking an item the Greaves don't hold moved the selection to slot " + player.getInventory().getSelectedSlot());
      
      tryPickItem(player, new ItemStack(Items.STONE));
      helper.assertTrue(player.getMainHandItem().is(Items.STONE), "Picking an item the Greaves hold did not pull it out");
      finish(helper, player);
   }
   
   private static void tryPickItem(ServerPlayer player, ItemStack stack){
      try{
         Method tryPickItem = ServerGamePacketListenerImpl.class.getDeclaredMethod("tryPickItem", ItemStack.class);
         tryPickItem.setAccessible(true);
         tryPickItem.invoke(player.connection, stack);
      }catch(ReflectiveOperationException e){
         throw new RuntimeException("ServerGamePacketListenerImpl.tryPickItem changed signature, update this test", e);
      }
   }
   
   // The research hook moved to HEAD of modifyDamageProtection, which runs for every armor enchantment on every hit
   @GameTest
   public void featherFallingResearchNeedsAReducedFall(GameTestHelper helper){
      ServerPlayer player = survivalPlayer(helper, new BlockPos(1, 2, 1));
      ItemStack boots = new ItemStack(Items.DIAMOND_BOOTS);
      boots.enchant(MinecraftUtils.getEnchantment(Enchantments.FEATHER_FALLING), 4);
      player.setItemSlot(EquipmentSlot.FEET, boots);
      String taskId = ResearchTasks.RESEARCH_TASKS.getValue(ResearchTasks.FEATHER_FALL).getId();
      
      player.hurtServer(helper.getLevel(), player.damageSources().generic(), 2f);
      helper.assertFalse(ArcanaNovum.data(player).completedResearchTask(taskId), "A hit that Feather Falling does not reduce completed the research");
      
      player.damageCooldownTime = 0;
      player.hurtServer(helper.getLevel(), player.damageSources().fall(), 5f);
      helper.assertTrue(ArcanaNovum.data(player).completedResearchTask(taskId), "A fall reduced by Feather Falling did not complete the research");
      finish(helper, player);
   }
   
   // The altar read the overworld clock but changed its own dimension's clock, so in the End it jumped the End clock to the overworld's day
   @GameTest
   public void celestialAltarUsesItsOwnDimensionsClock(GameTestHelper helper){
      MinecraftServer server = helper.getLevel().getServer();
      ServerLevel end = server.getLevel(Level.END);
      helper.assertTrue(end != null, "The game test server has no End");
      Holder<WorldClock> endClock = end.dimensionType().defaultClock().orElse(null);
      Holder<WorldClock> overworldClock = server.overworld().dimensionType().defaultClock().orElseThrow();
      helper.assertTrue(endClock != null && !endClock.equals(overworldClock), "The End no longer has its own clock, so this test proves nothing");
      
      long overworldBefore = server.clockManager().getInstance(overworldClock).totalTicks();
      server.clockManager().setTotalTicks(overworldClock, 24000L * 10 + 1000);
      server.clockManager().setTotalTicks(endClock, 24000L * 3 + 15000);
      long endBefore = server.clockManager().getInstance(endClock).totalTicks();
      
      BlockPos pos = new BlockPos(200, 120, 200);
      end.setBlockAndUpdate(pos, ((ArcanaBlock) ArcanaRegistry.CELESTIAL_ALTAR).getBlock().defaultBlockState());
      try{
         CelestialAltarBlockEntity altar = (CelestialAltarBlockEntity) end.getBlockEntity(pos);
         altar.setMode(0);
         altar.setPhase(0);
         Method changeTime = CelestialAltarBlockEntity.class.getDeclaredMethod("changeTime", ServerPlayer.class);
         changeTime.setAccessible(true);
         changeTime.invoke(altar, (Object) null);
      }catch(ReflectiveOperationException e){
         throw new RuntimeException("CelestialAltarBlockEntity.changeTime changed signature, update this test", e);
      }finally{
         end.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
         server.clockManager().setTotalTicks(overworldClock, overworldBefore);
      }
      
      long endAfter = server.clockManager().getInstance(endClock).totalTicks();
      helper.assertTrue(endAfter % 24000 == CelestialAltarBlockEntity.TIMES[0], "The End clock was set to " + endAfter % 24000 + " instead of " + CelestialAltarBlockEntity.TIMES[0]);
      helper.assertTrue(endAfter - endBefore >= 0 && endAfter - endBefore < 24000, "The End clock moved by " + (endAfter - endBefore) + " ticks instead of to the next matching time");
      helper.succeed();
   }
   
   // Auto-smelt ran on every Inventory.add, and a pickup whose output only partly fit lost the rest
   @GameTest(maxTicks = 40)
   public void cindersCharmOnlySmeltsPickupsAndLosesNothing(GameTestHelper helper){
      ServerLevel level = helper.getLevel();
      ServerPlayer player = survivalPlayer(helper, new BlockPos(1, 2, 1));
      ItemStack charm = ArcanaRegistry.CINDERS_CHARM.getNewItem();
      ArcanaItem.putProperty(charm, ArcanaItem.ACTIVE_TAG, true);
      ((CindersCharm) ArcanaRegistry.CINDERS_CHARM).setEnergy(charm, ((CindersCharm) ArcanaRegistry.CINDERS_CHARM).getMaxEnergy(charm));
      Inventory inv = player.getInventory();
      inv.setItem(0, charm);
      
      inv.add(new ItemStack(Items.RAW_IRON, 4));
      helper.assertTrue(inv.countItem(Items.RAW_IRON) == 4 && inv.countItem(Items.IRON_INGOT) == 0, "Items added straight to the inventory were smelted");
      
      // Room for only 2 of the 8 ingots
      List<ItemStack> items = inv.getNonEquipmentItems();
      items.set(1, new ItemStack(Items.IRON_INGOT, 62));
      for(int i = 2; i < items.size(); i++) items.set(i, new ItemStack(Items.DIRT, 64));
      ItemEntity rawIron = new ItemEntity(level, player.getX(), player.getY(), player.getZ(), new ItemStack(Items.RAW_IRON, 8));
      level.addFreshEntity(rawIron);
      rawIron.setNoPickUpDelay();
      rawIron.playerTouch(player);
      helper.assertTrue(rawIron.isRemoved(), "The raw iron was not picked up and smelted");
      helper.assertTrue(inv.countItem(Items.IRON_INGOT) == 64, "Expected the ingots that fit to arrive right away, found " + inv.countItem(Items.IRON_INGOT));
      
      for(int i = 2; i < 10; i++) items.set(i, ItemStack.EMPTY);
      helper.runAfterDelay(5, () -> {
         helper.assertTrue(inv.countItem(Items.IRON_INGOT) == 70, "Ingots were lost: expected 70, found " + inv.countItem(Items.IRON_INGOT));
         finish(helper, player);
      });
   }
   
   // BorisLib's returnItems only clears slot 0 of the source container, so the other slots kept their items
   @GameTest(maxTicks = 40)
   public void returnItemsEmptiesTheContainerAndWaitsForRoom(GameTestHelper helper){
      ServerPlayer player = survivalPlayer(helper, new BlockPos(1, 2, 1));
      List<ItemStack> items = player.getInventory().getNonEquipmentItems();
      for(int i = 0; i < items.size(); i++) items.set(i, new ItemStack(Items.DIRT, 64));
      SimpleContainer container = new SimpleContainer(new ItemStack(Items.DIAMOND, 3), new ItemStack(Items.EMERALD, 5));
      
      ArcanaItemUtils.returnItems(container, player);
      helper.assertTrue(container.isEmpty(), "The container still holds items after returning them");
      
      items.set(0, ItemStack.EMPTY);
      items.set(1, ItemStack.EMPTY);
      helper.runAfterDelay(5, () -> {
         helper.assertTrue(player.getInventory().countItem(Items.DIAMOND) == 3 && player.getInventory().countItem(Items.EMERALD) == 5,
               "Items given to a full inventory were not delivered once there was room");
         finish(helper, player);
      });
   }
   
   // Loading a conditionals achievement replaced its condition list with the saved one, so conditions added later
   // (Tracking and Ensnarement Arrows for Overly Equipped Archer) were skipped, and the status display threw on them
   @GameTest
   public void conditionalsAchievementKeepsConditionsAddedAfterSaving(GameTestHelper helper){
      ServerPlayer player = survivalPlayer(helper, new BlockPos(1, 2, 1));
      ConditionalsAchievement base = (ConditionalsAchievement) ArcanaAchievements.OVERLY_EQUIPPED_ARCHER;
      String tracking = ArcanaRegistry.TRACKING_ARROWS.getNameString();
      String ensnarement = ArcanaRegistry.ENSNAREMENT_ARROWS.getNameString();
      String smoke = ArcanaRegistry.SMOKE_ARROWS.getNameString();
      
      // Saved before the two new arrows were added, with everything but Smoke Arrows crafted
      CompoundTag conditions = new CompoundTag();
      for(String condition : base.getConditions().keySet()){
         if(!condition.equals(tracking) && !condition.equals(ensnarement)) conditions.putBoolean(condition, !condition.equals(smoke));
      }
      CompoundTag saved = base.makeNew().toNbt();
      saved.put("conditions", conditions);
      ConditionalsAchievement loaded = base.fromNbt(base.id, saved);
      helper.assertTrue(loaded.getConditions().keySet().equals(base.getConditions().keySet()), "The loaded achievement does not have the current conditions");
      ArcanaPlayerData profile = ArcanaNovum.data(player);
      profile.setAchievement(loaded);
      
      MutableComponent[] status = base.getStatusDisplay(player); // Threw an NPE before the fix
      helper.assertTrue(Arrays.stream(status).anyMatch(line -> line.getString().equals(tracking)), "The status display does not list " + tracking + " as missing");
      
      ArcanaAchievements.setCondition(player, base, smoke, true);
      helper.assertFalse(profile.hasAcheivement(base), "Granted without " + tracking + " and " + ensnarement);
      ArcanaAchievements.setCondition(player, base, tracking, true);
      ArcanaAchievements.setCondition(player, base, ensnarement, true);
      helper.assertTrue(profile.hasAcheivement(base), "Not granted after crafting everything");
      finish(helper, player);
   }
   
   // An open timed window was saved and restored, but its reset timer lives in memory, so after a restart it never closed
   @GameTest(maxTicks = 60)
   public void timedAchievementWindowIsNotRestoredFromSave(GameTestHelper helper){
      ServerPlayer player = survivalPlayer(helper, new BlockPos(1, 2, 1));
      TimedAchievement base = (TimedAchievement) ArcanaAchievements.OMAE_WA;
      CompoundTag saved = base.makeNew().toNbt();
      saved.putBoolean("active", true);
      helper.assertTrue(saved.getIntOr("type", -1) == 3, "Timed achievements should save type 3");
      
      TimedAchievement loaded = base.fromNbt(base.id, saved);
      helper.assertFalse(loaded.isActive(), "The saved window was restored without a timer to close it");
      ArcanaNovum.data(player).setAchievement(loaded);
      ArcanaAchievements.progress(player, base, 0);
      helper.assertTrue(ArcanaAchievements.isTimerActive(player, base), "A new window did not start after loading");
      helper.runAfterDelay(base.getTimeFrame() + 5, () -> {
         helper.assertFalse(ArcanaAchievements.isTimerActive(player, base), "The new window never closed");
         finish(helper, player);
      });
   }
   
   // Resetting a timed achievement left its old timer running, which then ended the next window early (Shackled)
   @GameTest(maxTicks = 80)
   public void timedAchievementResetIgnoresTheOldTimer(GameTestHelper helper){
      ServerPlayer player = survivalPlayer(helper, new BlockPos(1, 2, 1));
      TimedAchievement achievement = (TimedAchievement) ArcanaAchievements.OMAE_WA;
      int timeFrame = achievement.getTimeFrame();
      ArcanaAchievements.progress(player, achievement, 0);
      ArcanaAchievements.reset(player, achievement);
      helper.runAfterDelay(timeFrame / 2, () -> ArcanaAchievements.progress(player, achievement, 0));
      helper.runAfterDelay(timeFrame + 3, () -> helper.assertTrue(ArcanaAchievements.isTimerActive(player, achievement), "The first window's timer ended the second window"));
      helper.runAfterDelay(timeFrame / 2 + timeFrame + 5, () -> {
         helper.assertFalse(ArcanaAchievements.isTimerActive(player, achievement), "The second window never closed");
         finish(helper, player);
      });
   }
   
   // The shield gave absorption for the whole hit even when nothing was blocked (hits from behind)
   @GameTest
   public void shieldOfFortitudeOnlyAbsorbsBlockedDamage(GameTestHelper helper){
      ServerLevel level = helper.getLevel();
      ServerPlayer player = survivalPlayer(helper, new BlockPos(1, 2, 1));
      player.setYRot(0);
      player.setYHeadRot(0);
      player.setItemInHand(InteractionHand.MAIN_HAND, ArcanaRegistry.SHIELD_OF_FORTITUDE.getNewItem());
      player.startUsingItem(InteractionHand.MAIN_HAND);
      try{
         Field useItemRemaining = LivingEntity.class.getDeclaredField("useItemRemaining");
         useItemRemaining.setAccessible(true);
         useItemRemaining.setInt(player, player.getUseItem().getUseDuration(player) - 100); // Past the block delay
      }catch(ReflectiveOperationException e){
         throw new RuntimeException("LivingEntity.useItemRemaining was renamed, update this test", e);
      }
      helper.assertTrue(player.getItemBlockingWith() != null, "The player is not blocking");
      
      // Generic damage bypasses shields entirely, so use a mob behind the player to reach the unblocked path
      Pig behind = EntityTypes.PIG.create(level, EntitySpawnReason.TRIGGERED);
      behind.snapTo(player.getX(), player.getY(), player.getZ() - 2, 0, 0); // The player faces +Z
      level.addFreshEntity(behind);
      float blocked = player.applyItemBlocking(level, player.damageSources().mobAttack(behind), 10f);
      helper.assertTrue(blocked == 0, "A hit from behind was blocked");
      helper.assertTrue(player.getAbsorptionAmount() == 0, "Unblocked damage gave " + player.getAbsorptionAmount() + " absorption");
      
      Pig inFront = EntityTypes.PIG.create(level, EntitySpawnReason.TRIGGERED);
      inFront.snapTo(player.getX(), player.getY(), player.getZ() + 2, 0, 0);
      level.addFreshEntity(inFront);
      blocked = player.applyItemBlocking(level, player.damageSources().mobAttack(inFront), 10f);
      helper.assertTrue(blocked > 0, "A hit from the front was not blocked");
      helper.assertTrue(player.getAbsorptionAmount() > 0, "Blocked damage gave no absorption");
      behind.discard();
      inFront.discard();
      finish(helper, player);
   }
   
   // ServerPlayer.die never calls LivingEntity.die, so killing a player who triggered a Totem of Vengeance did not count as avenged
   @GameTest
   public void vengeanceTotemAvengedByKillingAPlayer(GameTestHelper helper){
      ServerPlayer holder = survivalPlayer(helper, new BlockPos(1, 2, 1));
      ServerPlayer attacker = survivalPlayer(helper, new BlockPos(3, 2, 1));
      VengeanceTotemTimerCallback timer = new VengeanceTotemTimerCallback(200, ArcanaRegistry.TOTEM_OF_VENGEANCE.getNewItem(), holder, attacker);
      BorisLib.addTickTimerCallback(timer);
      try{
         attacker.hurtServer(helper.getLevel(), holder.damageSources().playerAttack(holder), 1000f);
         helper.assertTrue(attacker.isDeadOrDying(), "The attacker survived");
         helper.assertTrue(timer.isAvenged(), "Killing the attacking player did not avenge the totem");
      }finally{
         BorisLib.SERVER_TIMER_CALLBACKS.remove(timer);
      }
      finish(helper, holder, attacker);
   }
   
   // Since 26.1 a mob in an anchored chunk was treated as just out of range even with a player right next to it, so it could randomly despawn
   @GameTest
   public void continuumAnchorKeepsVanillaDespawnNearPlayers(GameTestHelper helper){
      ServerLevel level = helper.getLevel();
      ServerPlayer player = survivalPlayer(helper, new BlockPos(1, 2, 1));
      Cod cod = EntityTypes.COD.create(level, EntitySpawnReason.NATURAL);
      Vec3 pos = helper.absoluteVec(new Vec3(3.5, 2, 1.5));
      cod.snapTo(pos.x, pos.y, pos.z, 0, 0);
      level.addFreshEntity(cod);
      ContinuumAnchor.addChunk(level, cod.chunkPosition());
      try{
         Field noActionTime = LivingEntity.class.getDeclaredField("noActionTime");
         noActionTime.setAccessible(true);
         // Each check has a 1 in 800 chance to despawn an idle mob that is out of range, so 4000 checks would almost surely despawn it
         for(int i = 0; i < 4000 && !cod.isRemoved(); i++){
            noActionTime.setInt(cod, 700);
            cod.checkDespawn();
         }
      }catch(ReflectiveOperationException e){
         throw new RuntimeException("LivingEntity.noActionTime was renamed, update this test", e);
      }
      helper.assertFalse(cod.isRemoved(), "A mob two blocks from a player despawned in an anchored chunk");
      cod.discard();
      finish(helper, player);
   }
   
   // Alchemical field Harming was unattributed magic damage, so its kills gave the shooter no kill credit (no XP, player loot or Arcana kill effects)
   @GameTest
   public void alchemicalFieldHarmingGivesTheShooterKillCredit(GameTestHelper helper){
      ServerLevel level = helper.getLevel();
      ServerPlayer shooter = survivalPlayer(helper, new BlockPos(1, 2, 1));
      Pig pig = EntityTypes.PIG.create(level, EntitySpawnReason.TRIGGERED);
      Vec3 pos = helper.absoluteVec(new Vec3(3.5, 2, 3.5));
      pig.snapTo(pos.x, pos.y, pos.z, 0, 0);
      level.addFreshEntity(pig);
      
      harmingField(level, shooter, pos).applyEffect(level, pig, harming(3));
      helper.assertTrue(pig.isDeadOrDying(), "Harming IV from the field did not kill the pig");
      helper.assertTrue(pig.getKillCredit() == shooter, "The field kill was credited to " + pig.getKillCredit() + " instead of the shooter");
      pig.discard();
      finish(helper, shooter);
   }
   
   // Like a vanilla Harming potion, the field must not hurt players (the shooter included) while PvP is off
   @GameTest
   public void alchemicalFieldHarmingFollowsThePvpRule(GameTestHelper helper){
      ServerLevel level = helper.getLevel();
      ServerPlayer shooter = survivalPlayer(helper, new BlockPos(1, 2, 1));
      ServerPlayer pvpOffTarget = survivalPlayer(helper, new BlockPos(3, 2, 1));
      ServerPlayer pvpOnTarget = survivalPlayer(helper, new BlockPos(5, 2, 1));
      AlchemicalArrowAreaEffectTracker.AlchemicalArrowSource field = harmingField(level, shooter, pvpOffTarget.position());
      boolean pvp = level.getGameRules().get(GameRules.PVP);
      try{ // The gamerule is server wide, so it is set back within this tick before any other test runs
         level.getGameRules().set(GameRules.PVP, false, level.getServer());
         field.applyEffect(level, pvpOffTarget, harming(0));
         field.applyEffect(level, shooter, harming(0));
         helper.assertTrue(pvpOffTarget.getHealth() == pvpOffTarget.getMaxHealth(), "With PvP off the field hurt another player");
         helper.assertTrue(shooter.getHealth() == shooter.getMaxHealth(), "With PvP off the field hurt the shooter");
         
         level.getGameRules().set(GameRules.PVP, true, level.getServer());
         field.applyEffect(level, pvpOnTarget, harming(0));
         field.applyEffect(level, shooter, harming(0));
         helper.assertTrue(pvpOnTarget.getHealth() < pvpOnTarget.getMaxHealth(), "With PvP on the field did not hurt another player");
         helper.assertTrue(shooter.getHealth() < shooter.getMaxHealth(), "With PvP on and no team the field did not hurt the shooter");
      }finally{
         level.getGameRules().set(GameRules.PVP, pvp, level.getServer());
      }
      finish(helper, shooter, pvpOffTarget, pvpOnTarget);
   }
   
   // Like a vanilla Harming potion, the field must spare the shooter's team (the shooter included) when friendly fire is off
   @GameTest
   public void alchemicalFieldHarmingFollowsFriendlyFire(GameTestHelper helper){
      ServerLevel level = helper.getLevel();
      helper.assertTrue(level.getGameRules().get(GameRules.PVP), "PvP is off on the test server, so this test proves nothing");
      ServerPlayer shooter = survivalPlayer(helper, new BlockPos(1, 2, 1));
      ServerPlayer teammate = survivalPlayer(helper, new BlockPos(3, 2, 1));
      ServerPlayer friendlyFireTeammate = survivalPlayer(helper, new BlockPos(5, 2, 1));
      ServerPlayer outsider = survivalPlayer(helper, new BlockPos(7, 2, 1));
      AlchemicalArrowAreaEffectTracker.AlchemicalArrowSource field = harmingField(level, shooter, teammate.position());
      Scoreboard scoreboard = level.getScoreboard();
      PlayerTeam team = scoreboard.addPlayerTeam("arcana-test-" + UUID.randomUUID().toString().substring(0, 8));
      try{
         for(ServerPlayer player : List.of(shooter, teammate, friendlyFireTeammate)) scoreboard.addPlayerToTeam(player.getScoreboardName(), team);
         team.setAllowFriendlyFire(false);
         field.applyEffect(level, teammate, harming(0));
         field.applyEffect(level, shooter, harming(0));
         field.applyEffect(level, outsider, harming(0));
         helper.assertTrue(teammate.getHealth() == teammate.getMaxHealth(), "With friendly fire off the field hurt a teammate");
         helper.assertTrue(shooter.getHealth() == shooter.getMaxHealth(), "With friendly fire off the field hurt the shooter");
         helper.assertTrue(outsider.getHealth() < outsider.getMaxHealth(), "The field did not hurt a player outside the team");
         
         team.setAllowFriendlyFire(true);
         field.applyEffect(level, friendlyFireTeammate, harming(0));
         helper.assertTrue(friendlyFireTeammate.getHealth() < friendlyFireTeammate.getMaxHealth(), "With friendly fire on the field did not hurt a teammate");
      }finally{
         scoreboard.removePlayerTeam(team);
      }
      finish(helper, shooter, teammate, friendlyFireTeammate, outsider);
   }
   
   // Lingering field effects and conditions (Poison, Vulnerability) reached every player whatever the PvP rule, unlike the field's Harming
   @GameTest
   public void alchemicalFieldLingeringEffectsFollowThePvpRule(GameTestHelper helper){
      ServerLevel level = helper.getLevel();
      ServerPlayer shooter = survivalPlayer(helper, new BlockPos(1, 2, 1));
      ServerPlayer target = survivalPlayer(helper, new BlockPos(3, 2, 1));
      AlchemicalArrowAreaEffectTracker.AlchemicalArrowSource field = harmingField(level, shooter, target.position());
      boolean pvp = level.getGameRules().get(GameRules.PVP);
      try{ // The gamerule is server wide, so it is set back within this tick before any other test runs
         level.getGameRules().set(GameRules.PVP, false, level.getServer());
         applyHarmfulFieldEffects(level, field, target);
         applyHarmfulFieldEffects(level, field, shooter);
         assertNotHarmed(helper, target, "With PvP off, another player");
         assertNotHarmed(helper, shooter, "With PvP off, the shooter");
         field.applyEffect(level, target, new MobEffectInstance(MobEffects.REGENERATION, 200, 0));
         helper.assertTrue(target.hasEffect(MobEffects.REGENERATION), "With PvP off a beneficial field effect no longer reached another player");
         
         level.getGameRules().set(GameRules.PVP, true, level.getServer());
         applyHarmfulFieldEffects(level, field, target);
         applyHarmfulFieldEffects(level, field, shooter);
         assertHarmed(helper, target, "With PvP on, another player");
         assertHarmed(helper, shooter, "With PvP on and no team, the shooter");
      }finally{
         level.getGameRules().set(GameRules.PVP, pvp, level.getServer());
      }
      finish(helper, shooter, target);
   }
   
   // Lingering field effects and conditions ignored friendly fire, unlike the field's Harming
   @GameTest
   public void alchemicalFieldLingeringEffectsFollowFriendlyFire(GameTestHelper helper){
      ServerLevel level = helper.getLevel();
      helper.assertTrue(level.getGameRules().get(GameRules.PVP), "PvP is off on the test server, so this test proves nothing");
      ServerPlayer shooter = survivalPlayer(helper, new BlockPos(1, 2, 1));
      ServerPlayer teammate = survivalPlayer(helper, new BlockPos(3, 2, 1));
      ServerPlayer friendlyFireTeammate = survivalPlayer(helper, new BlockPos(5, 2, 1));
      ServerPlayer outsider = survivalPlayer(helper, new BlockPos(7, 2, 1));
      AlchemicalArrowAreaEffectTracker.AlchemicalArrowSource field = harmingField(level, shooter, teammate.position());
      Scoreboard scoreboard = level.getScoreboard();
      PlayerTeam team = scoreboard.addPlayerTeam("arcana-test-" + UUID.randomUUID().toString().substring(0, 8));
      try{
         for(ServerPlayer player : List.of(shooter, teammate, friendlyFireTeammate)) scoreboard.addPlayerToTeam(player.getScoreboardName(), team);
         team.setAllowFriendlyFire(false);
         for(ServerPlayer player : List.of(teammate, shooter, outsider)) applyHarmfulFieldEffects(level, field, player);
         assertNotHarmed(helper, teammate, "With friendly fire off, a teammate");
         assertNotHarmed(helper, shooter, "With friendly fire off, the shooter");
         assertHarmed(helper, outsider, "A player outside the team");
         field.applyEffect(level, teammate, new MobEffectInstance(MobEffects.REGENERATION, 200, 0));
         helper.assertTrue(teammate.hasEffect(MobEffects.REGENERATION), "With friendly fire off a beneficial field effect no longer reached a teammate");
         
         team.setAllowFriendlyFire(true);
         applyHarmfulFieldEffects(level, field, friendlyFireTeammate);
         assertHarmed(helper, friendlyFireTeammate, "With friendly fire on, a teammate");
      }finally{
         scoreboard.removePlayerTeam(team);
      }
      finish(helper, shooter, teammate, friendlyFireTeammate, outsider);
   }
   
   // Wither damage has no attacker, so a mob the field withered to death gave the shooter no kill credit
   @GameTest
   public void alchemicalFieldWitherKillGivesTheShooterKillCredit(GameTestHelper helper){
      ServerLevel level = helper.getLevel();
      ServerPlayer shooter = survivalPlayer(helper, new BlockPos(1, 2, 1));
      Pig pig = EntityTypes.PIG.create(level, EntitySpawnReason.TRIGGERED);
      Vec3 pos = helper.absoluteVec(new Vec3(3.5, 2, 3.5));
      pig.snapTo(pos.x, pos.y, pos.z, 0, 0);
      level.addFreshEntity(pig);
      
      harmingField(level, shooter, pos).applyEffect(level, pig, new MobEffectInstance(MobEffects.WITHER, 200, 1));
      pig.hurtServer(level, pig.damageSources().wither(), 1000f); // What the Wither effect deals each tick
      helper.assertTrue(pig.isDeadOrDying(), "The pig survived");
      helper.assertTrue(pig.getKillCredit() == shooter, "The withered pig's kill was credited to " + pig.getKillCredit() + " instead of the shooter");
      pig.discard();
      finish(helper, shooter);
   }
   
   // The kill callback took the weapon from the main hand, so a kill from the Spear's offhand charge missed Eternal Cruelty's double souls
   @GameTest
   public void spearOffhandChargeKillGetsEternalCrueltySouls(GameTestHelper helper){
      ServerLevel level = helper.getLevel();
      ServerPlayer player = survivalPlayer(helper, new BlockPos(1, 2, 1));
      ItemStack spear = ArcanaRegistry.SPEAR_OF_TENBROUS.getNewItem();
      ArcanaAugments.applyAugment(spear, ArcanaAugments.ETERNAL_CRUELTY, 1, false);
      player.setItemSlot(EquipmentSlot.OFFHAND, spear);
      spear.getItem().inventoryTick(spear, level, player, EquipmentSlot.OFFHAND); // Gives the offhand Spear its charge attack
      player.getInventory().setItem(1, Soulstone.setType(ArcanaRegistry.SOULSTONE.getNewItem(), EntityTypes.PIG));
      player.startUsingItem(InteractionHand.OFF_HAND);
      helper.assertTrue(player.isUsingItem() && player.getMainHandItem().isEmpty(), "The player is not charging the offhand Spear");

      Pig pig = EntityTypes.PIG.create(level, EntitySpawnReason.TRIGGERED);
      Vec3 pos = helper.absoluteVec(new Vec3(3.5, 2, 1.5));
      pig.snapTo(pos.x, pos.y, pos.z, 0, 0);
      level.addFreshEntity(pig);
      player.stabAttack(EquipmentSlot.OFFHAND, pig, 1000f, true, false, false); // What the charge does to each entity it hits
      helper.assertTrue(pig.isDeadOrDying(), "The charge did not kill the pig");

      int expected = 2 * ArcanaNovum.CONFIG.getIntList(ArcanaConfig.SOULSTONE_SOULS_PER_LVL).get(0);
      int souls = Soulstone.getSouls(player.getInventory().getItem(1));
      helper.assertTrue(souls == expected, "The offhand charge kill gave " + souls + " souls instead of " + expected);
      pig.discard();
      finish(helper, player);
   }

   private static void applyHarmfulFieldEffects(ServerLevel level, AlchemicalArrowAreaEffectTracker.AlchemicalArrowSource field, ServerPlayer player){
      field.applyEffect(level, player, new MobEffectInstance(MobEffects.POISON, 200, 0));
      field.applyCondition(level, player, new ConditionInstance(Conditions.VULNERABILITY, ArcanaRegistry.arcanaId(ArcanaRegistry.ALCHEMICAL_ARBALEST.getId()), 200, 0.5f, true, true, true, AttributeModifier.Operation.ADD_VALUE, null));
   }
   
   private static void assertHarmed(GameTestHelper helper, ServerPlayer player, String who){
      helper.assertTrue(player.hasEffect(MobEffects.POISON), who + " was not poisoned by the field");
      helper.assertTrue(Conditions.getConditionValue(player, Conditions.VULNERABILITY) > 0, who + " did not get Vulnerability from the field");
   }
   
   private static void assertNotHarmed(GameTestHelper helper, ServerPlayer player, String who){
      helper.assertFalse(player.hasEffect(MobEffects.POISON), who + " was poisoned by the field");
      helper.assertFalse(Conditions.getConditionValue(player, Conditions.VULNERABILITY) > 0, who + " got Vulnerability from the field");
   }
   
   private static MobEffectInstance harming(int amplifier){
      return new MobEffectInstance(MobEffects.INSTANT_DAMAGE, 1, amplifier);
   }
   
   private static AlchemicalArrowAreaEffectTracker.AlchemicalArrowSource harmingField(ServerLevel level, ServerPlayer shooter, Vec3 pos){
      Arrow arrow = EntityTypes.ARROW.create(level, EntitySpawnReason.TRIGGERED); // Stands in for the arbalest arrow the field came from
      arrow.snapTo(pos.x, pos.y, pos.z, 0, 0);
      return AlchemicalArrowAreaEffectTracker.source(shooter, arrow, BlockPos.containing(pos), level, 3, 0, List.of(Either.left(harming(0))));
   }
   
   // The Arcana click action ran any command it carried, not only the /arcana ones it is meant for
   @GameTest
   public void arcanaClickActionOnlyRunsArcanaCommands(GameTestHelper helper){
      ServerPlayer player = survivalPlayer(helper, new BlockPos(1, 2, 1));
      MinecraftServer server = helper.getLevel().getServer();
      NameAndId nameAndId = new NameAndId(player.getGameProfile());
      server.getPlayerList().op(nameAndId, Optional.of(LevelBasedPermissionSet.OWNER), Optional.empty()); // Game test ops default to no permissions
      try{
         BlockPos target = new BlockPos(2, 2, 2);
         BlockPos abs = helper.absolutePos(target);
         String setblock = "/setblock " + abs.getX() + " " + abs.getY() + " " + abs.getZ() + " minecraft:gold_block";
         clickArcanaAction(player, setblock);
         helper.assertBlockNotPresent(Blocks.GOLD_BLOCK, target);
         
         int xp = ArcanaNovum.data(player).getXP();
         server.getCommands().performPrefixedCommand(player.createCommandSourceStack(), "/arcana xp add @s 1");
         helper.assertTrue(ArcanaNovum.data(player).getXP() == xp + 1, "The player can't run /arcana xp, so this test proves nothing");
         xp = ArcanaNovum.data(player).getXP();
         clickArcanaAction(player, "/arcana xp add @s 123");
         helper.assertTrue(ArcanaNovum.data(player).getXP() == xp + 123, "An /arcana click action did not run");
         
         server.getCommands().performPrefixedCommand(player.createCommandSourceStack(), setblock);
         helper.assertBlockPresent(Blocks.GOLD_BLOCK, target); // The player could run it, so the click action was what refused it
      }finally{
         server.getPlayerList().deop(nameAndId);
      }
      finish(helper, player);
   }
   
   private static void clickArcanaAction(ServerPlayer player, String command){
      CompoundTag payload = new CompoundTag();
      payload.putString(ArcanaNovum.ARCANA_CLICK_KEY, command);
      player.connection.handleCustomClickAction(new ServerboundCustomClickActionPacket(ArcanaNovum.ARCANA_CLICK_ACTION_ID, Optional.of(payload)));
   }
   
   // Tether Arrows missed by several blocks: the launch was solved with the wrong drag and separate flight times for height and distance.
   // These pigs fly through vanilla's own movement code: pulled across the ground, grappled onto a pillar, and pulled with Slow Falling and low gravity
   @GameTest(maxTicks = 120)
   public void tetherArrowsLandOnTheirTarget(GameTestHelper helper){
      for(int x = 0; x < 8; x++){
         for(int z = 0; z < 8; z++) helper.setBlock(new BlockPos(x, 1, z), Blocks.STONE);
      }
      for(int y = 2; y <= 4; y++) helper.setBlock(new BlockPos(6, y, 6), Blocks.STONE);
      
      // One lane each, so they can't bump into each other
      Pig pulled = tetherPig(helper, new Vec3(6.5, 2, 0.5));
      Pig floaty = tetherPig(helper, new Vec3(7.5, 2, 3));
      floaty.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 200));
      floaty.getAttribute(Attributes.GRAVITY).setBaseValue(0.04);
      Pig grappler = tetherPig(helper, new Vec3(1.5, 2, 6.5));
      Map<Pig, ArmorStand> shooters = Map.of(
            pulled, helper.spawn(EntityTypes.ARMOR_STAND, new Vec3(0.5, 2, 0.5)),
            floaty, helper.spawn(EntityTypes.ARMOR_STAND, new Vec3(0.5, 2, 3)));
      
      Map<Pig, Vec3> targets = new HashMap<>();
      Map<Pig, Vec3> landings = new HashMap<>();
      Set<Pig> flying = new HashSet<>();
      helper.runAfterDelay(10, () -> { // Settled on the ground, so the first tick has ground friction
         for(Pig pig : List.of(pulled, floaty, grappler)) helper.assertTrue(pig.onGround(), "A pig is not on the ground before its launch");
         
         shooters.forEach((pig, shooter) -> {
            double gap = (pig.getBbWidth() + shooter.getBbWidth()) / 2 + TetherArrows.PULL_GAP;
            targets.put(pig, helper.relativeVec(shooter.position()).add(gap, 0, 0));
            TetherArrows.launch(pig, inRange(helper, TetherArrows.pull(pig, shooter)));
         });
         
         BlockHitResult hit = new BlockHitResult(helper.absoluteVec(new Vec3(6, 3.5, 6.5)), Direction.WEST, helper.absolutePos(new BlockPos(6, 3, 6)), false);
         Vec3 spot = TetherArrows.landingSpot(grappler, hit);
         helper.assertTrue(spot != null && helper.relativeVec(spot).distanceTo(new Vec3(6.5, 5, 6.5)) < 1.0E-6, "The landing spot is not the top of the pillar: " + spot);
         targets.put(grappler, new Vec3(6.5, 5, 6.5));
         TetherArrows.launch(grappler, inRange(helper, TetherArrows.grapple(grappler, hit)));
      });
      helper.onEachTick(() -> {
         for(Pig pig : targets.keySet()){
            if(!pig.onGround()){
               flying.add(pig);
            }else if(flying.contains(pig)){
               landings.putIfAbsent(pig, helper.relativeVec(pig.position()));
            }
         }
      });
      helper.succeedWhen(() -> {
         helper.assertTrue(targets.size() == 3, "Not launched yet");
         targets.forEach((pig, target) -> {
            Vec3 landing = landings.get(pig);
            helper.assertTrue(landing != null, "A pig has not landed yet");
            double miss = landing.subtract(target).horizontalDistance();
            helper.assertTrue(miss < 0.3 && Math.abs(landing.y - target.y) < 0.1, "A pig aimed at " + target + " landed at " + landing + ", " + miss + " blocks away");
         });
      });
   }
   
   private static TetherArrows.Tether inRange(GameTestHelper helper, TetherArrows.Tether tether){
      helper.assertTrue(tether != null, "A short tether shot failed as out of range");
      return tether;
   }
   
   // An arrow held still (in a cobweb, say) while its shooter walked far away flung the shooter or the hit mob across the map
   @GameTest
   public void tetherArrowsFailOutOfRangeAndCapTheirSpeed(GameTestHelper helper){
      helper.setBlock(new BlockPos(1, 1, 1), Blocks.STONE);
      Pig pig = tetherPig(helper, new Vec3(1.5, 2, 1.5));
      ArmorStand shooter = EntityTypes.ARMOR_STAND.create(helper.getLevel(), EntitySpawnReason.TRIGGERED); // Never added, so its far away chunk stays unloaded
      helper.runAfterDelay(10, () -> helper.assertTrue(pig.onGround(), "The pig is not on the ground")); // Where long shots need the most speed
      // Pinned, since the test server's config file keeps whatever values it was first written with
      helper.runAfterDelay(11, () -> withConfig(ArcanaConfig.TETHER_ARROW_MAX_RANGE, 256.0, () -> withConfig(ArcanaConfig.TETHER_ARROW_MAX_SPEED, 15.0, () -> checkTetherReach(helper, pig, shooter, 256.0, 15.0))));
   }
   
   // Runs with a config value set, then puts the old one back
   @SuppressWarnings("unchecked")
   private static void withConfig(IConfigSetting<?> setting, Object value, Runnable action){
      ConfigValue<Object> config = (ConfigValue<Object>) ArcanaNovum.CONFIG.values.stream().filter(v -> v.getName().equals(setting.getName())).findFirst().orElseThrow();
      Object old = ArcanaNovum.CONFIG.getValue(setting);
      config.setValue(value);
      try{
         action.run();
      }finally{
         config.setValue(old);
      }
   }
   
   private static void checkTetherReach(GameTestHelper helper, Pig pig, ArmorStand shooter, double range, double maxSpeed){
      Vec3 start = pig.position();
      
      shooter.snapTo(start.x + range + 10, start.y, start.z, 0, 0);
      helper.assertTrue(TetherArrows.pull(pig, shooter) == null, "A pull from past tetherArrowMaxRange went ahead");
      shooter.snapTo(start.x + range * 0.8, start.y, start.z, 0, 0); // In range, but needs far more than the speed cap from the ground
      TetherArrows.Tether far = TetherArrows.pull(pig, shooter);
      helper.assertTrue(far != null && Math.abs(far.velocity().length() - maxSpeed) < 1.0E-6, "A long pull was not slowed to tetherArrowMaxSpeed: " + far);
      shooter.snapTo(start.x + 8, start.y, start.z, 0, 0);
      TetherArrows.Tether near = TetherArrows.pull(pig, shooter);
      helper.assertTrue(near != null && near.velocity().length() < maxSpeed, "A short pull failed or was capped: " + near);
      helper.succeed();
   }
   
   // Long tether shots rubber banded players: vanilla's "moved too quickly" check rejected the flight the server had launched
   @GameTest
   public void tetherFlightPassesTheMoveCheck(GameTestHelper helper){
      ServerPlayer flung = survivalPlayer(helper, new BlockPos(1, 2, 1));
      ServerPlayer walker = survivalPlayer(helper, new BlockPos(3, 2, 1));
      ServerPlayer strafer = survivalPlayer(helper, new BlockPos(5, 2, 1));
      Vec3 launch = new Vec3(0, 12, 0); // Straight up, so the accepted moves stay clear of the neighbouring tests
      TetherFlightEvent.start(flung, launch, 20);
      TetherFlightEvent.start(strafer, launch, 20);
      
      Vec3 up = new Vec3(0, 12.5, 0); // Over 10 blocks in one tick, which vanilla rejects
      helper.assertTrue(movePlayer(flung, up), "A move along the tether launch was rejected");
      helper.assertFalse(movePlayer(walker, up), "A fast move with no tether flight got past the check, so this test proves nothing");
      helper.assertFalse(movePlayer(strafer, new Vec3(9, 8, 0)), "A fast move across the tether launch got past the check");
      finish(helper, flung, walker, strafer);
   }
   
   // Sends one move packet as a client would. True when the server took the new position, false when it sent the player back
   private static boolean movePlayer(ServerPlayer player, Vec3 move){
      try{
         Field awaiting = ServerGamePacketListenerImpl.class.getDeclaredField("awaitingPositionFromClient");
         awaiting.setAccessible(true);
         awaiting.set(player.connection, null); // As if the client confirmed its spawn teleport
      }catch(ReflectiveOperationException e){
         throw new RuntimeException("ServerGamePacketListenerImpl.awaitingPositionFromClient changed, update this test", e);
      }
      player.setDeltaMovement(Vec3.ZERO); // The server's copy of the velocity has decayed by the time a fast packet arrives
      Vec3 start = player.position();
      Vec3 target = start.add(move);
      player.connection.handleMovePlayer(new ServerboundMovePlayerPacket.Pos(target, false, false));
      if(player.position().distanceTo(target) < 1.0E-6) return true;
      if(player.position().distanceTo(start) < 1.0E-6) return false;
      throw new AssertionError("The move ended somewhere else: " + player.position());
   }
   
   private static Pig tetherPig(GameTestHelper helper, Vec3 pos){
      Pig pig = helper.spawn(EntityTypes.PIG, pos);
      pig.removeFreeWill(); // Nothing steers it in the air
      return pig;
   }
   
   /** A connected player in survival, unlike makeMockServerPlayerInLevel which is always creative */
   static ServerPlayer survivalPlayer(GameTestHelper helper, BlockPos relative){
      ServerLevel level = helper.getLevel();
      UUID uuid = UUID.randomUUID();
      // Unique names, since scoreboard teams match players by name
      CommonListenerCookie cookie = CommonListenerCookie.createInitial(new GameProfile(uuid, "mock-" + uuid.toString().substring(0, 8)), false);
      ServerPlayer player = new ServerPlayer(level.getServer(), level, cookie.gameProfile(), cookie.clientInformation());
      Connection connection = new Connection(PacketFlow.SERVERBOUND);
      new EmbeddedChannel(connection);
      level.getServer().getPlayerList().placeNewPlayer(connection, player, cookie);
      player.connection.handleAcceptPlayerLoad(new ServerboundPlayerLoadedPacket()); // Players are invulnerable until their client has loaded
      player.setGameMode(GameType.SURVIVAL);
      Vec3 pos = helper.absoluteVec(Vec3.atBottomCenterOf(relative));
      player.teleportTo(level, pos.x, pos.y, pos.z, Set.of(), 0, 0, true);
      return player;
   }
   
   /** Removes the test's players so they can't be the nearest player in later tests, then passes the test */
   static void finish(GameTestHelper helper, ServerPlayer... players){
      for(ServerPlayer player : players) helper.getLevel().getServer().getPlayerList().remove(player);
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
