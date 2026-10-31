package net.borisshoes.arcananovum.test.gametest;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.achievements.ArcanaAchievement;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.achievements.ProgressAchievement;
import net.borisshoes.arcananovum.achievements.TimedAchievement;
import net.borisshoes.arcananovum.augments.ArcanaAugment;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.entities.RunicArrowEntity;
import net.borisshoes.borislib.config.ConfigValue;
import net.borisshoes.borislib.config.IConfigSetting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/** Shared setup for the achievement and augment game tests */
final class TestKit {
   private TestKit(){}

   static ServerPlayer player(GameTestHelper helper){
      return RegressionGameTests.survivalPlayer(helper, new BlockPos(1, 2, 1));
   }

   /** A new stack of the item with one augment applied */
   static ItemStack item(ArcanaItem item, ArcanaAugment augment, int level){
      ItemStack stack = item.getNewItem();
      if(level > 0 && !ArcanaAugments.applyAugment(stack, augment, level, false)){
         throw new IllegalStateException("Could not apply " + augment.id + " " + level + " to " + item.getId());
      }
      return stack;
   }

   /** A mob that stays where it was put, floating so that gravity and knockback checks start from zero */
   static <T extends Mob> T mob(GameTestHelper helper, EntityType<T> type, double x, double y, double z){
      T mob = helper.spawn(type, new Vec3(x, y, z));
      mob.setNoAi(true);
      mob.setNoGravity(true);
      mob.setDeltaMovement(Vec3.ZERO);
      return mob;
   }

   static <T extends Mob> T mobWithHealth(GameTestHelper helper, EntityType<T> type, double x, double y, double z, float health){
      T mob = mob(helper, type, x, y, z);
      mob.setHealth(health);
      return mob;
   }

   /** A runic arrow at a test-relative spot, not added to the level. Speed 3 is a fully drawn bow */
   static RunicArrowEntity arrow(GameTestHelper helper, ServerPlayer owner, ArcanaItem type, ArcanaAugment augment, int level, double x, double y, double z){
      ItemStack stack = augment == null ? type.getNewItem() : item(type, augment, level);
      RunicArrowEntity arrow = new RunicArrowEntity(helper.getLevel(), owner, stack, null);
      Vec3 pos = helper.absoluteVec(new Vec3(x, y, z));
      arrow.snapTo(pos.x, pos.y, pos.z, 0, 0);
      arrow.setDeltaMovement(0, 0, 3);
      return arrow;
   }

   static BlockHitResult blockHit(GameTestHelper helper, double x, double y, double z){
      Vec3 pos = helper.absoluteVec(new Vec3(x, y, z));
      return new BlockHitResult(pos, Direction.UP, BlockPos.containing(pos), false);
   }

   static EntityHitResult entityHit(Entity entity){
      return new EntityHitResult(entity, entity.position());
   }

   static InteractionResult use(GameTestHelper helper, ServerPlayer player, ItemStack stack){
      player.setItemInHand(InteractionHand.MAIN_HAND, stack);
      return stack.getItem().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
   }
   
   static void inventoryTick(GameTestHelper helper, ServerPlayer player, ItemStack stack){
      stack.getItem().inventoryTick(stack, helper.getLevel(), player, null);
   }
   
   static InteractionResult useOn(GameTestHelper helper, ServerPlayer player, ItemStack stack, BlockPos relative, Direction face){
      player.setItemInHand(InteractionHand.MAIN_HAND, stack);
      BlockPos pos = helper.absolutePos(relative);
      return stack.getItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(pos), face, pos, false)));
   }
   
   /** Keeps ticking the item in the player's inventory until the check passes, then removes the player and passes the test */
   static void tickUntil(GameTestHelper helper, ServerPlayer player, ItemStack stack, Runnable check){
      helper.succeedWhen(() -> {
         inventoryTick(helper, player, stack);
         check.run();
         helper.getLevel().getServer().getPlayerList().remove(player);
      });
   }
   
   static boolean achieved(ServerPlayer player, ArcanaAchievement achievement){
      return ArcanaNovum.data(player).hasAcheivement(achievement);
   }

   static void assertAchieved(GameTestHelper helper, ServerPlayer player, ArcanaAchievement achievement){
      helper.assertTrue(achieved(player, achievement), achievement.id + " was not granted");
   }

   static void assertNotAchieved(GameTestHelper helper, ServerPlayer player, ArcanaAchievement achievement){
      helper.assertFalse(achieved(player, achievement), achievement.id + " was granted too early");
   }

   /** Puts a progress or timed achievement one step away from its goal, so one real trigger has to finish it */
   static void almostComplete(ServerPlayer player, ArcanaAchievement achievement, int stepsLeft){
      int goal = achievement instanceof ProgressAchievement progress ? progress.getGoal() : ((TimedAchievement) achievement).getGoal();
      ArcanaAchievements.progress(player, achievement, goal - stepsLeft);
   }

   @SuppressWarnings("unchecked")
   static void withConfig(IConfigSetting<?> setting, Object value, Runnable action){
      ConfigValue<Object> config = (ConfigValue<Object>) ArcanaNovum.CONFIG.values.stream().filter(v -> v.getName().equals(setting.getName())).findFirst().orElseThrow();
      Object old = ArcanaNovum.CONFIG.getValue(setting);
      config.setValue(value);
      try{
         action.run();
      }finally{
         config.setValue(old);
      }
   }

   /** Calls a private or protected method by name, for code that has no public way in */
   static Object call(Object target, String name, Object... args){
      for(Class<?> type = target.getClass(); type != null; type = type.getSuperclass()){
         for(java.lang.reflect.Method method : type.getDeclaredMethods()){
            if(!method.getName().equals(name) || method.getParameterCount() != args.length) continue;
            try{
               method.setAccessible(true);
               return method.invoke(target, args);
            }catch(ReflectiveOperationException e){
               throw new RuntimeException("Calling " + name + " failed", e.getCause() != null ? e.getCause() : e);
            }
         }
      }
      throw new IllegalStateException(target.getClass().getSimpleName() + " has no method " + name + " with " + args.length + " arguments, update this test");
   }
   
   static Object callStatic(Class<?> type, String name, Object... args){
      for(java.lang.reflect.Method method : type.getDeclaredMethods()){
         if(!method.getName().equals(name) || method.getParameterCount() != args.length) continue;
         try{
            method.setAccessible(true);
            return method.invoke(null, args);
         }catch(ReflectiveOperationException e){
            throw new RuntimeException("Calling " + name + " failed", e.getCause() != null ? e.getCause() : e);
         }
      }
      throw new IllegalStateException(type.getSimpleName() + " has no method " + name + ", update this test");
   }

   /** Calls a mixin handler, which Mixin renames to something ending in the name it has in the mixin class */
   static Object callMixin(Object target, String name, Object... args){
      for(java.lang.reflect.Method method : target.getClass().getDeclaredMethods()){
         if(!method.getName().endsWith(name) || method.getParameterCount() != args.length) continue;
         try{
            method.setAccessible(true);
            return method.invoke(target, args);
         }catch(ReflectiveOperationException e){
            throw new RuntimeException("Calling " + name + " failed", e.getCause() != null ? e.getCause() : e);
         }
      }
      throw new IllegalStateException(target.getClass().getSimpleName() + " has no mixin handler " + name + ", update this test");
   }

   /** Reads a private field by name, for state that has no getter */
   static Object field(Object target, String name){
      for(Class<?> type = target.getClass(); type != null; type = type.getSuperclass()){
         try{
            java.lang.reflect.Field field = type.getDeclaredField(name);
            field.setAccessible(true);
            return field.get(target);
         }catch(NoSuchFieldException ignored){
         }catch(ReflectiveOperationException e){
            throw new RuntimeException(e);
         }
      }
      throw new IllegalStateException(target.getClass().getSimpleName() + " has no field " + name + ", update this test");
   }

   /** Writes a private field by name, for state that has no setter */
   static void setField(Object target, String name, Object value){
      for(Class<?> type = target.getClass(); type != null; type = type.getSuperclass()){
         try{
            java.lang.reflect.Field field = type.getDeclaredField(name);
            field.setAccessible(true);
            field.set(target, value);
            return;
         }catch(NoSuchFieldException ignored){
         }catch(ReflectiveOperationException e){
            throw new RuntimeException(e);
         }
      }
      throw new IllegalStateException(target.getClass().getSimpleName() + " has no field " + name + ", update this test");
   }

   /** Fails with a list of what is missing, so every uncovered id shows up in one run */
   static void assertCoverage(GameTestHelper helper, String what, List<Class<?>> testClasses, Set<String> ids, Set<String> skipped){
      Set<String> covered = new TreeSet<>();
      for(Class<?> tests : testClasses){
         for(var method : tests.getDeclaredMethods()){
            Covers covers = method.getAnnotation(Covers.class);
            if(covers != null) covered.addAll(Arrays.asList(covers.value()));
         }
      }
      covered.retainAll(ids); // The two id sets share a Covers annotation
      List<String> uncovered = ids.stream().filter(id -> !covered.contains(id) && !skipped.contains(id)).sorted().toList();
      List<String> stale = skipped.stream().filter(id -> covered.contains(id) || !ids.contains(id)).sorted().toList();
      helper.assertTrue(uncovered.isEmpty(), what + " without a test or a reason: " + uncovered);
      helper.assertTrue(stale.isEmpty(), what + " listed as untested but covered or gone: " + stale);
   }
}
