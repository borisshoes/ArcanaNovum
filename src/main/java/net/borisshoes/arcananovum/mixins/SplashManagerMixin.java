package net.borisshoes.arcananovum.mixins;

import net.minecraft.client.resources.SplashManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Pseudo
@Mixin(SplashManager.class)
public class SplashManagerMixin {
   @Mutable
   @Shadow
   private List<String> splashes;
   
   @Inject(at = @At("RETURN"), method = "apply(Ljava/util/List;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V")
   public void addSplashes(List<String> splashes, ResourceManager resourceManager, ProfilerFiller profiler, CallbackInfo ci) {
      this.splashes = new ArrayList<>(this.splashes);
      this.splashes.add("Ascendant Origins: Just a summer away!");
      this.splashes.add("Translocating your chests when you sleep!");
      this.splashes.add("What's more unpronounceable? Aequalis Scientia or Itineranteur?");
      this.splashes.add("Also try Ancestral Archetypes!");
      this.splashes.add("Don't snort the Stardust!");
      this.splashes.add("§cBrimsüth§r is still §kfucking§r napping!");
      this.splashes.add("Don't trust Jeráld...");
      this.splashes.add("Game balancing by §bEquayus§r!");
      this.splashes.add("§8Nul§r is watching...");
      this.splashes.add("How Novum is the Arcana?");
      this.splashes.add("Is §5Enderia§r the bad guy?");
      this.splashes.add("Non-binary Blades!");
      this.splashes.add("Choose to live in the light.");
      this.splashes.add("Show me the way... stone!");
      this.splashes.add("Far Beyond the Stars...");
      this.splashes.add("When I void* to the Nul Construct.");
      this.splashes.add("What are we? Some kind of Arcana... Novum?");
   }
}