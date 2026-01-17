package net.borisshoes.arcananovum.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(PiglinAi.class)
public class PiglinAiMixin {
   
   @ModifyReturnValue(method = "getBarterResponseItems", at = @At("RETURN"))
   private static List<ItemStack> arcananovum$negotiatePiglins(List<ItemStack> original, Piglin piglin){
      if(!(piglin.level() instanceof ServerLevel level)) return original;
      for(ServerPlayer player : level.getPlayers(p -> p.distanceTo(piglin) <= 10)){
         if(!ArcanaItemUtils.hasItemInInventory(player, ArcanaRegistry.NEGOTIATION_CHARM.getItem())) continue;
         for(ItemStack itemStack : original){
            int count = itemStack.getCount();
            int newCount = Math.toIntExact(Math.round(Math.min(itemStack.getMaxStackSize(), count * (1 + player.random.nextFloat() * 2.5))));
            int diff = newCount - count;
            itemStack.setCount(newCount);
            ArcanaAchievements.progress(player,ArcanaAchievements.WOLF_OF_BLOCK_STREET, diff);
         }
         return original;
      }
      return original;
   }
}
