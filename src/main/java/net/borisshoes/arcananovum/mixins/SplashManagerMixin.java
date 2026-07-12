package net.borisshoes.arcananovum.mixins;

import net.borisshoes.arcananovum.utils.ArcanaColors;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.SplashManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Pseudo
@Mixin(SplashManager.class)
public class SplashManagerMixin {
   @Final
   @Shadow
   private static Style DEFAULT_STYLE;

   @Mutable
   @Shadow
   private List<Component> splashes;

   @Unique
   private Component splash(String text) {
      return Component.literal(text).setStyle(DEFAULT_STYLE);
   }

   @Inject(at = @At("RETURN"), method = "apply(Ljava/util/List;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V")
   public void addSplashes(List<Component> preparations, ResourceManager manager, ProfilerFiller profiler, CallbackInfo ci) {
      this.splashes = new ArrayList<>(this.splashes);
      this.splashes.add(splash("Ascendant Origins: Just a summer away!"));
      this.splashes.add(splash("Translocating your chests when you sleep!"));
      this.splashes.add(splash("What's more unpronounceable? Aequalis Scientia or Itineranteur?"));
      this.splashes.add(splash("Also try Ancestral Archetypes!"));
      this.splashes.add(splash("Don't snort the Stardust!"));
      this.splashes.add(Component.empty().setStyle(DEFAULT_STYLE)
            .append(Component.literal("Brimsüth").withStyle(ChatFormatting.RED))
            .append(Component.literal(" is still "))
            .append(Component.literal("fucking").withStyle(ChatFormatting.OBFUSCATED))
            .append(Component.literal(" napping!")));
      this.splashes.add(splash("Don't trust Jeráld..."));
      this.splashes.add(Component.empty().setStyle(DEFAULT_STYLE)
            .append(Component.literal("Game balancing by "))
            .append(Component.literal("Equayus").withStyle(ChatFormatting.AQUA))
            .append(Component.literal("!")));
      this.splashes.add(Component.empty().setStyle(DEFAULT_STYLE)
            .append(Component.literal("Nul").withColor(ArcanaColors.NUL_COLOR))
            .append(Component.literal(" is watching...")));
      this.splashes.add(splash("How Novum is the Arcana?"));
      this.splashes.add(Component.empty().setStyle(DEFAULT_STYLE)
            .append(Component.literal("Is "))
            .append(Component.literal("Enderia").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal(" the bad guy?")));
      this.splashes.add(splash("Non-binary Blades!"));
      this.splashes.add(splash("Choose to live in the light."));
      this.splashes.add(splash("Show me the way... stone!"));
      this.splashes.add(splash("Far Beyond the Stars..."));
      this.splashes.add(splash("When I void* to the Nul Construct."));
      this.splashes.add(splash("What are we? Some kind of Arcana... Novum?"));
   }
}