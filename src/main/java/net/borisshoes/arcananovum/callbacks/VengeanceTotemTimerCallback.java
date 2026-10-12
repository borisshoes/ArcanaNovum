package net.borisshoes.arcananovum.callbacks;

import net.minecraft.util.Mth;
import net.minecraft.network.chat.Component;
import net.minecraft.data.AtlasIds;
import net.minecraft.ChatFormatting;
import net.borisshoes.borislib.utils.MinecraftUtils;
import net.borisshoes.arcananovum.items.TotemOfVengeance;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.ArcanaConfig;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.callbacks.login.VengeanceTotemLoginCallback;
import net.borisshoes.arcananovum.damage.ArcanaDamageTypes;
import net.borisshoes.borislib.BorisLib;
import net.borisshoes.borislib.timers.TickTimerCallback;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class VengeanceTotemTimerCallback extends TickTimerCallback {
   
   private final Entity attacker;
   private final int duration;
   private boolean avenged;
   
   public VengeanceTotemTimerCallback(int time, ItemStack item, ServerPlayer player, @Nullable Entity attacker){
      super(time, item, player);
      this.attacker = attacker;
      this.duration = Math.max(1, time);
   }
   
   public void displayTimer(){
      if(avenged || getTimer() % 5 != 0) return;
      ServerPlayer player1 = player.level().getServer().getPlayerList().getPlayer(player.getUUID());
      if(player1 == null) return;
      int segments = 10;
      int filled = Mth.clamp((int) Math.ceil((double) segments * getTimer() / duration), 0, segments);
      player1.sendSystemMessage(Component.literal("")
            .append(MinecraftUtils.getAtlasedTexture(AtlasIds.ITEMS, ArcanaRegistry.arcanaId("item/" + TotemOfVengeance.ID)))
            .append(Component.literal(" " + "█".repeat(filled)).withStyle(ChatFormatting.DARK_RED))
            .append(Component.literal("█".repeat(segments - filled) + " ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("⌛ ").withStyle(ChatFormatting.GOLD))
            .append(Component.literal((getTimer() + 19) / 20 + "s").withStyle(ChatFormatting.RED)), true);
   }
   
   @Override
   public void onTimer(){
      try{
         ServerPlayer player1 = player.level().getServer().getPlayerList().getPlayer(player.getUUID());
         if(player1 == null){
            if(attacker != null){
               if(avenged){
                  ArcanaNovum.data(player).addXP(ArcanaNovum.CONFIG.getInt(ArcanaConfig.XP_TOTEM_OF_VENGEANCE_SURVIVE)); // Give XP
               }else{
                  BorisLib.addLoginCallback(new VengeanceTotemLoginCallback(player));
               }
            }
         }else{
            if(attacker != null){
               if(avenged){
                  ArcanaNovum.data(player).addXP(ArcanaNovum.CONFIG.getInt(ArcanaConfig.XP_TOTEM_OF_VENGEANCE_SURVIVE)); // Give XP
               }else{
                  player1.hurtServer(player.level(), ArcanaDamageTypes.of(player1.level(), ArcanaDamageTypes.VENGEANCE_TOTEM, attacker), player1.getMaxHealth() * 10);
               }
            }
         }
      }catch(Exception e){
         e.printStackTrace();
      }
   }
   
   public Entity getAttacker(){
      return attacker;
   }
   
   public void setAvenged(){
      this.avenged = true;
   }
}
