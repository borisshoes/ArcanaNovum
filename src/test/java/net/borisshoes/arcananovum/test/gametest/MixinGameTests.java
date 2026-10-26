package net.borisshoes.arcananovum.test.gametest;

import net.borisshoes.arcananovum.test.mixin.ClassSource;
import net.borisshoes.arcananovum.test.mixin.MixinSnapshot;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.*;

/**
 * Checks the mixins inside the running game, where Mixin itself decides whether they apply.
 * The static checks (injection points, always vs conditional hooks, target method diffs) live in MixinSnapshotTests.
 */
public class MixinGameTests {
   
   @GameTest
   public void everyMixinAppliesToItsTargets(GameTestHelper helper){
      MixinSnapshot snapshot;
      try(ClassSource source = ClassSource.fromClassPath(null)){
         snapshot = MixinSnapshot.capture(source);
      }
      
      // Loading a target class runs every mixin on it, and with defaultRequire = 1 a hook with no match throws here
      List<String> problems = new ArrayList<>(snapshot.errors);
      ClassLoader loader = getClass().getClassLoader();
      for(Map.Entry<String, List<String>> entry : snapshot.serverTargets.entrySet()){
         if(snapshot.emptyMixins.contains(entry.getKey())) continue; // Nothing to apply, the snapshot lists these instead
         String mixinName = entry.getKey().replace('/', '.');
         for(String target : entry.getValue()){
            String targetName = target.replace('/', '.');
            Class<?> targetClass;
            try{
               targetClass = Class.forName(targetName, false, loader);
            }catch(Throwable e){
               problems.add(mixinName + ": loading " + targetName + " failed: " + e);
               continue;
            }
            if(!isApplied(targetClass, mixinName)){
               problems.add(mixinName + " did not apply to " + targetName);
            }
         }
      }
      
      try{
         MixinEnvironment.getCurrentEnvironment().audit();
      }catch(Throwable e){
         problems.add("Mixin audit failed: " + e);
      }
      
      helper.assertTrue(problems.isEmpty(), "Mixin problems:\n" + String.join("\n", problems));
      helper.succeed();
   }
   
   /**
    * A mixin left a trace on its target: an accessor mixin adds its interface, and merged members carry
    * {@code @MixinMerged(mixin = ...)}. Empty mixins leave nothing, so they are skipped before this is called.
    */
   private static boolean isApplied(Class<?> target, String mixinName){
      Set<IMixinInfo> info = Mixins.getMixinsForClass(target.getName());
      if(info.stream().anyMatch(i -> i.getClassName().equals(mixinName))) return true;
      for(Class<?> itf : target.getInterfaces()){
         if(itf.getName().equals(mixinName)) return true;
      }
      List<AnnotatedElement> members = new ArrayList<>(Arrays.asList(target.getDeclaredMethods()));
      members.addAll(Arrays.asList(target.getDeclaredFields()));
      for(AnnotatedElement member : members){
         for(Annotation annotation : member.getDeclaredAnnotations()){
            if(!annotation.annotationType().getName().equals("org.spongepowered.asm.mixin.transformer.meta.MixinMerged")) continue;
            try{
               if(mixinName.equals(annotation.annotationType().getMethod("mixin").invoke(annotation))) return true;
            }catch(ReflectiveOperationException ignored){
            }
         }
      }
      return false;
   }
}
