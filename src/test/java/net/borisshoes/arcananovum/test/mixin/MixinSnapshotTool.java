package net.borisshoes.arcananovum.test.mixin;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Command line entry for ./gradlew mixinSnapshot.
 * <p>
 * Args: {@code <baselineDir> <reportDir> [--mc-jar <path>] [--update]}
 * <p>
 * With --mc-jar the mixin targets are read from that jar instead of the current Minecraft jar, which shows what a
 * version update would break before any code is ported. Loom caches merged jars for every version it has set up under
 * .gradle/loom-cache/minecraftMaven/net/minecraft/.
 */
public class MixinSnapshotTool {
   
   public static void main(String[] args) throws IOException{
      Path baselineDir = Path.of(args[0]);
      Path reportDir = Path.of(args[1]);
      Path mcJar = null;
      boolean update = false;
      for(int i = 2; i < args.length; i++){
         if(args[i].equals("--mc-jar")) mcJar = Path.of(args[++i]);
         if(args[i].equals("--update")) update = true;
      }
      
      Result result = run(baselineDir, reportDir, mcJar, update);
      for(String line : result.output()) System.out.println(line);
      if(!result.passed()) System.exit(1);
   }
   
   public record Result(boolean passed, List<String> output) {}
   
   public static Result run(Path baselineDir, Path reportDir, Path mcJar, boolean update) throws IOException{
      MixinSnapshot current;
      try(ClassSource source = ClassSource.fromClassPath(mcJar)){
         current = MixinSnapshot.capture(source);
      }
      Files.createDirectories(reportDir);
      current.write(reportDir.resolve("current"));
      
      List<String> output = new java.util.ArrayList<>();
      if(!current.errors.isEmpty()){
         output.add("Mixins that cannot apply" + (mcJar == null ? "" : " to " + mcJar.getFileName()) + ":");
         for(String error : current.errors) output.add("  " + error);
         output.add("");
      }
      
      if(update){
         current.write(baselineDir);
         output.add("Wrote mixin baseline to " + baselineDir + " (" + current.entries.size() + " entries, " + current.methods.size() + " method listings)");
         return new Result(current.errors.isEmpty(), output);
      }
      
      MixinSnapshot baseline = MixinSnapshot.read(baselineDir);
      if(baseline == null){
         output.add("No mixin baseline at " + baselineDir + ". Create one with ./gradlew test -PupdateMixinBaseline");
         return new Result(false, output);
      }
      
      List<String> report = MixinSnapshot.compare(baseline, current);
      Path reportFile = reportDir.resolve("report.txt");
      Files.write(reportFile, report.isEmpty() ? List.of("No changes from the baseline.") : report, StandardCharsets.UTF_8);
      if(!report.isEmpty()){
         int end = report.indexOf("---------------- details ----------------");
         output.addAll(report.subList(0, end < 0 ? report.size() : end));
         output.add("Full diff: " + reportFile);
      }
      return new Result(current.errors.isEmpty() && report.isEmpty(), output);
   }
}
