package net.borisshoes.arcananovum.test.unit;

import net.borisshoes.arcananovum.items.arrows.TetherArrows.TetherPhysics;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tether Arrows missed by several blocks: the launch was solved with the wrong drag and separate flight times for height and distance.
 * These fly each solved launch through a copy of LivingEntity.travelInAir and check where it ends up
 */
public class TetherPhysicsTests {
   
   private static final double TOLERANCE = 0.01;
   private static final double[][] TARGETS = {{5, 0}, {10, 0}, {20, 0}, {30, 0}, {8, 5}, {15, 10}, {20, 20}, {6, 15}, {10, -10}, {25, -5}, {3, -12}, {0.5, 4}};
   
   private static Map<String, TetherPhysics> variants(){
      Map<String, TetherPhysics> map = new LinkedHashMap<>();
      map.put("air", new TetherPhysics(0.08, 0.08, false, 0, 0.98F, 0.91F, 0.91F, false));
      map.put("ground", new TetherPhysics(0.08, 0.08, false, 0, 0.98F, 0.91F, 0.6F * 0.91F, true));
      map.put("ice", new TetherPhysics(0.08, 0.08, false, 0, 0.98F, 0.91F, 0.98F * 0.91F, true));
      map.put("slow falling", new TetherPhysics(0.08, 0.01, false, 0, 0.98F, 0.91F, 0.6F * 0.91F, true));
      map.put("low gravity", new TetherPhysics(0.02, 0.02, false, 0, 0.98F, 0.91F, 0.91F, false));
      map.put("half drag", new TetherPhysics(0.08, 0.08, false, 0, 1 - 0.02F * 0.5F, 1 - 0.09F * 0.5F, 1 - 0.09F * 0.5F, false));
      map.put("flying", new TetherPhysics(0, 0, false, 0, 0.91F, 0.91F, 0.91F, false));
      return map;
   }
   
   @Test
   public void landsOnEveryTarget(){
      variants().forEach((name, physics) -> {
         for(double[] target : TARGETS){
            TetherPhysics.Launch launch = solve(physics, target[0], target[1], true, Double.POSITIVE_INFINITY);
            double[][] path = fly(physics, launch);
            String label = name + " to " + target[0] + ", " + target[1];
            assertEquals(target[0], path[launch.ticks()][0], TOLERANCE, label + ": wrong distance");
            assertEquals(target[1], path[launch.ticks()][1], TOLERANCE, label + ": wrong height");
            if(physics.gravity() > 0) assertTrue(path[launch.ticks() - 1][1] > target[1], label + ": was not coming down onto the target"); // Without gravity it can't come down
         }
      });
   }
   
   @Test
   public void staysAboveTheTargetBlockOnceOverIt(){
      double clearFrom = 7.2; // An 8 block shot onto the middle of a block, minus half a block and half a player
      variants().forEach((name, physics) -> {
         if(physics.gravity() == 0) return; // Can't come down onto the block, so it flies straight at the spot
         TetherPhysics.Launch launch = solve(physics, 8, 3, true, clearFrom);
         double[][] path = fly(physics, launch);
         for(int k = 1; k < launch.ticks(); k++){
            assertFalse(path[k][0] >= clearFrom && path[k][1] < 3, name + ": inside the side of the block on tick " + k);
         }
      });
   }
   
   @Test
   public void leavesTheGroundBeforeDropping(){
      TetherPhysics physics = variants().get("ground");
      TetherPhysics.Launch launch = solve(physics, 3, -6, true, Double.POSITIVE_INFINITY);
      double[][] path = fly(physics, launch);
      for(int k = 1; k < launch.ticks() && path[k][0] < 1; k++){
         assertTrue(path[k][1] > 0, "Back on its starting block on tick " + k);
      }
   }
   
   @Test
   public void neverRisesAboveACeiling(){
      variants().forEach((name, physics) -> {
         TetherPhysics.Launch launch = solve(physics, 4, 5, false, 0);
         double[][] path = fly(physics, launch);
         assertEquals(5, path[launch.ticks()][1], TOLERANCE, name + ": did not reach the ceiling");
         for(int k = 1; k <= launch.ticks(); k++){
            assertTrue(path[k][1] <= 5 + TOLERANCE, name + ": above the ceiling on tick " + k);
         }
      });
   }
   
   @Test
   public void levitationStillReachesTheTarget(){
      // Levitation can't come down onto a target above, so the landing rules are dropped
      TetherPhysics physics = new TetherPhysics(0.08, 0.08, true, 0.05, 0.98F, 0.91F, 0.91F, false);
      TetherPhysics.Launch launch = solve(physics, 10, 4, true, Double.POSITIVE_INFINITY);
      double[][] path = fly(physics, launch);
      assertEquals(10, path[launch.ticks()][0], TOLERANCE);
      assertEquals(4, path[launch.ticks()][1], TOLERANCE);
   }
   
   // An arrow held still (in a cobweb, say) while its shooter walked far away could fling the shooter at any speed
   @Test
   public void capsLaunchesAtTheSpeedLimit(){
      double cap = 15.0;
      TetherPhysics physics = variants().get("ground");
      TetherPhysics.Launch far = solve(physics, 200, 0, true, Double.POSITIVE_INFINITY);
      assertTrue(far.speed() > cap, "A 200 block shot from the ground should need more than " + cap + " blocks per tick");
      TetherPhysics.Launch capped = far.capped(cap);
      assertEquals(cap, capped.speed(), 1.0E-9, "Not slowed to the cap");
      assertEquals(far.vertical() / far.horizontal(), capped.vertical() / capped.horizontal(), 1.0E-9, "Slowing it changed its direction");
      double[][] path = fly(physics, capped);
      assertTrue(path[capped.ticks()][0] < 200, "A slowed launch should fall short");
      
      TetherPhysics.Launch near = solve(physics, 20, 0, true, Double.POSITIVE_INFINITY);
      assertSame(near, near.capped(cap), "The cap changed a launch that was already under it");
   }
   
   private static TetherPhysics.Launch solve(TetherPhysics physics, double distance, double height, boolean land, double clearFrom){
      TetherPhysics.Launch launch = physics.solve(distance, height, land, clearFrom);
      assertNotNull(launch, "No launch to " + distance + ", " + height);
      return launch;
   }
   
   // Position after each tick, following LivingEntity.aiStep and travelInAir in order
   private static double[][] fly(TetherPhysics physics, TetherPhysics.Launch launch){
      double[][] path = new double[launch.ticks() + 1][2];
      double x = 0;
      double y = 0;
      double vx = launch.horizontal();
      double vy = launch.vertical();
      for(int tick = 1; tick <= launch.ticks(); tick++){
         if(Math.abs(vy) < 0.003) vy = 0; // aiStep
         x += vx; // move
         y += vy;
         path[tick][0] = x;
         path[tick][1] = y;
         if(physics.levitating()){
            vy += (physics.levitation() - vy) * 0.2;
         }else{
            vy -= vy <= 0.0 ? physics.fallingGravity() : physics.gravity(); // getEffectiveGravity
         }
         vx *= tick == 1 ? physics.firstAirDrag() : physics.airDrag();
         vy *= physics.verticalDrag();
      }
      return path;
   }
}
