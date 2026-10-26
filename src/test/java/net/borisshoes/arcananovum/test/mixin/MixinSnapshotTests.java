package net.borisshoes.arcananovum.test.mixin;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Static mixin checks that run in ./gradlew test without starting the game.
 * See {@link MixinSnapshot} for what the snapshot records.
 */
public class MixinSnapshotTests {
   
   private static final String MIXIN_PACKAGE = "net/borisshoes/arcananovum/mixins/";
   
   @Test
   public void mixinsMatchBaseline() throws IOException{
      Path baselineDir = Path.of(System.getProperty("arcananovum.test.mixinBaseline", "src/test/mixin-baseline"));
      Path reportDir = Path.of(System.getProperty("arcananovum.test.mixinReport", "build/reports/mixin-snapshot"));
      boolean update = Boolean.parseBoolean(System.getProperty("arcananovum.test.updateMixinBaseline", "false"));
      
      MixinSnapshotTool.Result result = MixinSnapshotTool.run(baselineDir, reportDir, null, update);
      String output = String.join("\n", result.output());
      if(!output.isBlank()) System.out.println(output);
      assertTrue(result.passed(), output);
   }
   
   @Test
   public void hookOnBranchIsConditional() throws IOException{
      // Keeps the reachability check honest: a call that only happens inside an if must not report as always reached
      ClassNode target = readClass("net/minecraft/world/entity/LivingEntity");
      assertNotNull(target, "LivingEntity not found on the classpath");
      MethodNode die = target.methods.stream().filter(m -> m.name.equals("die")).findFirst().orElseThrow();
      AbstractInsnNode isRemoved = null;
      AbstractInsnNode setPose = null;
      for(AbstractInsnNode insn : die.instructions){
         if(insn instanceof MethodInsnNode call){
            if(isRemoved == null && call.name.equals("isRemoved")) isRemoved = insn;
            if(setPose == null && call.name.equals("setPose")) setPose = insn;
         }
      }
      assertNotNull(isRemoved, "die() no longer calls isRemoved()");
      assertTrue(MixinSnapshot.isAlwaysReached(die, isRemoved), "The first check in die() should be reached on every path");
      assertNotNull(setPose, "die() no longer calls setPose()");
      assertFalse(MixinSnapshot.isAlwaysReached(die, setPose), "setPose() in die() is inside the !isRemoved() branch and should be conditional");
   }
   
   @Test
   public void everyMixinClassIsRegistered() throws IOException{
      Path classesDir = findClassesDir();
      assertNotNull(classesDir, "Could not find the compiled main classes on the test classpath");
      byte[] configBytes = Files.readAllBytes(findResource(MixinSnapshot.MIXIN_CONFIG));
      JsonObject config = JsonParser.parseString(new String(configBytes, StandardCharsets.UTF_8)).getAsJsonObject();
      Set<String> registered = new TreeSet<>();
      for(String section : List.of("mixins", "server", "client")){
         if(!config.has(section)) continue;
         for(JsonElement element : config.getAsJsonArray(section)) registered.add(element.getAsString());
      }
      
      Set<String> annotated = new TreeSet<>();
      try(Stream<Path> files = Files.walk(classesDir.resolve(MIXIN_PACKAGE))){
         for(Path file : files.filter(f -> f.toString().endsWith(".class")).toList()){
            ClassNode node = new ClassNode();
            new ClassReader(Files.readAllBytes(file)).accept(node, ClassReader.SKIP_CODE);
            if(hasMixinAnnotation(node)){
               annotated.add(node.name.substring(MIXIN_PACKAGE.length()).replace('/', '.'));
            }
         }
      }
      
      Set<String> unregistered = new TreeSet<>(annotated);
      unregistered.removeAll(registered);
      assertTrue(unregistered.isEmpty(), "Mixins missing from " + MixinSnapshot.MIXIN_CONFIG + " (they will never apply): " + unregistered);
   }
   
   private static boolean hasMixinAnnotation(ClassNode node){
      for(List<AnnotationNode> list : Arrays.asList(node.invisibleAnnotations, node.visibleAnnotations)){
         if(list == null) continue;
         for(AnnotationNode annotation : list){
            if(annotation.desc.equals("Lorg/spongepowered/asm/mixin/Mixin;")) return true;
         }
      }
      return false;
   }
   
   private static ClassNode readClass(String internalName){
      try(ClassSource source = ClassSource.fromClassPath(null)){
         return source.getClass(internalName);
      }
   }
   
   private static Path findClassesDir(){
      for(String entry : System.getProperty("java.class.path").split(File.pathSeparator)){
         Path path = Path.of(entry);
         if(Files.isDirectory(path.resolve(MIXIN_PACKAGE))) return path;
      }
      return null;
   }
   
   private static Path findResource(String name){
      for(String entry : System.getProperty("java.class.path").split(File.pathSeparator)){
         Path path = Path.of(entry);
         if(Files.isDirectory(path) && Files.isRegularFile(path.resolve(name))) return path.resolve(name);
      }
      throw new IllegalStateException(name + " not found on the classpath");
   }
}
