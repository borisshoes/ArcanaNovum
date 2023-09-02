package net.borisshoes.arcananovum.mixins;

import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.items.charms.FelidaeCharm;
import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.spawner.PhantomSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PhantomSpawner.class)
public class PhantomSpawnerMixin {
   
   @Redirect(method="spawn",at=@At(value="INVOKE",target="Lnet/minecraft/server/network/ServerPlayerEntity;isSpectator()Z"))
   private boolean arcananovum_fuckPhantoms(ServerPlayerEntity instance){
      PlayerInventory inv = instance.getInventory();
      for(int i=0; i<inv.size();i++){
         ItemStack item = inv.getStack(i);
         if(item.isEmpty()){
            continue;
         }
      
         if(MagicItemUtils.identifyItem(item) instanceof FelidaeCharm charm){
            if(ArcanaAugments.getAugmentOnItem(item,ArcanaAugments.PANTHERA.id) >= 1){
               return true;
            }
         }
      }
      
      return instance.isSpectator();
   }
}
