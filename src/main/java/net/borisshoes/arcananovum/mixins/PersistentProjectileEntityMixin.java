package net.borisshoes.arcananovum.mixins;

import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.items.QuiverItem;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PersistentProjectileEntity.class)
public class PersistentProjectileEntityMixin {
 
   // Todo: no redirect
   @Redirect(method = "<init>(Lnet/minecraft/entity/EntityType;DDDLnet/minecraft/world/World;Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;)V", at=@At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;copy()Lnet/minecraft/item/ItemStack;"))
   private ItemStack arcananovum_removeQuiverData(ItemStack instance){
      ItemStack stack = instance.copy();
      ArcanaItem.removeProperty(stack, QuiverItem.QUIVER_ID_TAG);
      ArcanaItem.removeProperty(stack, QuiverItem.QUIVER_SLOT_TAG);
      return stack;
   }
}
