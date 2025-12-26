package net.borisshoes.arcananovum.callbacks;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.bosses.BossFights;
import net.borisshoes.arcananovum.bosses.dragon.DragonBossFight;
import net.borisshoes.borislib.BorisLib;
import net.borisshoes.borislib.timers.GenericTimer;
import net.borisshoes.borislib.utils.MinecraftUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gamerules.GameRules;

import static net.borisshoes.arcananovum.cardinalcomponents.WorldDataComponentInitializer.BOSS_FIGHT;

public class PlayerDeathCallback {
   
   public static void afterRespawn(ServerPlayer oldPlayer, ServerPlayer newPlayer, boolean alive){
      Tuple<BossFights, CompoundTag> bossFight = BOSS_FIGHT.get(newPlayer.level().getServer().getLevel(Level.END)).getBossFight();
      if(bossFight == null) return;
      if(bossFight.getA() == BossFights.DRAGON){
         DragonBossFight.playerDied(newPlayer);
         BorisLib.addTickTimerCallback(new GenericTimer(20, () -> {
            // Give teleport option
            
            MutableComponent deathMsg1 = Component.literal("")
                  .append(Component.literal("You have ").withStyle(ChatFormatting.AQUA))
                  .append(Component.literal("died").withStyle(ChatFormatting.RED, ChatFormatting.BOLD, ChatFormatting.ITALIC))
                  .append(Component.literal("!").withStyle(ChatFormatting.AQUA));
            MutableComponent deathMsg2 = Component.literal("")
                  .append(Component.literal("Click ").withStyle(ChatFormatting.AQUA))
                  .append(Component.literal("[Here]").withStyle(s ->
                        s.withClickEvent(new ClickEvent.RunCommand("/arcana boss teleport"))
                              .withHoverEvent(new HoverEvent.ShowText(Component.literal("Click to Teleport!")))
                              .withColor(ChatFormatting.LIGHT_PURPLE).withBold(true)))
                  .append(Component.literal(" to Teleport back to the fight!").withStyle(ChatFormatting.AQUA));
            
            newPlayer.displayClientMessage(deathMsg1, false);
            newPlayer.displayClientMessage(deathMsg2, false);
         }));
      }
   }
   
   public static void onPlayerCopy(ServerPlayer oldPlayer, ServerPlayer newPlayer, boolean alive){
      if (!alive && !(oldPlayer.level().getGameRules().get(GameRules.KEEP_INVENTORY) || oldPlayer.isSpectator())) {
         for (int i = 0; i < oldPlayer.getInventory().getContainerSize(); i++) {
            ItemStack oldStack = oldPlayer.getInventory().getItem(i);
            ItemStack newStack = newPlayer.getInventory().getItem(i);
            if (EnchantmentHelper.getItemEnchantmentLevel(MinecraftUtils.getEnchantment(ArcanaRegistry.FATE_ANCHOR), oldStack) > 0 && !ItemStack.matches(oldStack, newStack)) {
               if (newStack.isEmpty()) {
                  newPlayer.getInventory().setItem(i, oldStack);
               } else {
                  newPlayer.getInventory().placeItemBackInInventory(oldStack);
               }
            }
         }
      }
   }
}
