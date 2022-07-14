package net.borisshoes.arcananovum.mixins;

import net.borisshoes.arcananovum.cardinalcomponents.MagicEntity;
import net.borisshoes.arcananovum.items.MagicItem;
import net.borisshoes.arcananovum.items.RunicArrow;
import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.borisshoes.arcananovum.cardinalcomponents.MagicEntityComponentInitializer.MAGIC_ENTITY_LIST;

@Mixin(PersistentProjectileEntity.class)
public class ProjectileMixin {
   
   @Inject(method = "onBlockHit", at = @At("HEAD"))
   private void onBlockHit(BlockHitResult blockHitResult, CallbackInfo ci){
      PersistentProjectileEntity arrow = (PersistentProjectileEntity) (Object) this;
   
      for(MagicEntity magicEntity : MAGIC_ENTITY_LIST.get(arrow.getEntityWorld()).getEntities()){
         if(arrow.getUuidAsString().equals(magicEntity.getUuid())){
            String id = magicEntity.getData().getString("id");
            MagicItem item = MagicItemUtils.getItemFromId(id);
            if(item instanceof RunicArrow runicArrow){
               runicArrow.blockHit(arrow,blockHitResult);
            }
            break;
         }
      }
   }
   
   @Inject(method = "onEntityHit", at = @At("HEAD"))
   private void onBlockHit(EntityHitResult entityHitResult, CallbackInfo ci){
      PersistentProjectileEntity arrow = (PersistentProjectileEntity) (Object) this;
   
      for(MagicEntity magicEntity : MAGIC_ENTITY_LIST.get(arrow.getEntityWorld()).getEntities()){
         if(arrow.getUuidAsString().equals(magicEntity.getUuid())){
            String id = magicEntity.getData().getString("id");
            MagicItem item = MagicItemUtils.getItemFromId(id);
            if(item instanceof RunicArrow runicArrow){
               runicArrow.entityHit(arrow,entityHitResult);
            }
            break;
         }
      }
   }
}
