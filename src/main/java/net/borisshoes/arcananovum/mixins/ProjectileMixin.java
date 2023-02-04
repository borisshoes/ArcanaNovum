package net.borisshoes.arcananovum.mixins;

import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.achievements.TimedAchievement;
import net.borisshoes.arcananovum.cardinalcomponents.IArcanaProfileComponent;
import net.borisshoes.arcananovum.cardinalcomponents.MagicEntity;
import net.borisshoes.arcananovum.items.core.MagicItem;
import net.borisshoes.arcananovum.items.core.RunicArrow;
import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;
import static net.borisshoes.arcananovum.cardinalcomponents.WorldDataComponentInitializer.MAGIC_ENTITY_LIST;

@Mixin(PersistentProjectileEntity.class)
public class ProjectileMixin {
   
   @Inject(method = "onBlockHit", at = @At("HEAD"))
   private void arcananovum_onBlockHit(BlockHitResult blockHitResult, CallbackInfo ci){
      PersistentProjectileEntity arrow = (PersistentProjectileEntity) (Object) this;
   
      for(MagicEntity magicEntity : MAGIC_ENTITY_LIST.get(arrow.getEntityWorld()).getEntities()){
         if(arrow.getUuidAsString().equals(magicEntity.getUuid())){
            String id = magicEntity.getData().getString("id");
            MagicItem item = MagicItemUtils.getItemFromId(id);
            if(item instanceof RunicArrow runicArrow){
               runicArrow.blockHit(arrow,blockHitResult,magicEntity);
               arrow.discard();
            }
            break;
         }
      }
   }
   
   @Inject(method = "onEntityHit", at = @At("HEAD"))
   private void arcananovum_onEntityHit(EntityHitResult entityHitResult, CallbackInfo ci){
      PersistentProjectileEntity arrow = (PersistentProjectileEntity) (Object) this;
   
      for(MagicEntity magicEntity : MAGIC_ENTITY_LIST.get(arrow.getEntityWorld()).getEntities()){
         if(arrow.getUuidAsString().equals(magicEntity.getUuid())){
            String id = magicEntity.getData().getString("id");
            MagicItem item = MagicItemUtils.getItemFromId(id);
            if(item instanceof RunicArrow runicArrow){
               runicArrow.entityHit(arrow,entityHitResult,magicEntity);
               
               if(arrow.getOwner() instanceof ServerPlayerEntity player){
                  if(player.getPos().distanceTo(arrow.getPos()) >= 100) ArcanaAchievements.grant(player, "aimbot");
   
                  // Do this bit manually so extra data can be saved
                  IArcanaProfileComponent profile = PLAYER_DATA.get(player);
                  if(ArcanaAchievements.registry.get("arrow_for_every_foe") instanceof TimedAchievement baseAch){
                     String itemId = baseAch.getMagicItem().getId();
                     TimedAchievement achievement = (TimedAchievement) profile.getAchievement(itemId, baseAch.id);
                     if(achievement == null){
                        TimedAchievement newAch = baseAch.makeNew();
                        NbtCompound comp = new NbtCompound();
                        comp.putBoolean(item.getId(),true);
                        baseAch.setData(comp);
                        profile.setAchievement(itemId, newAch);
                        ArcanaAchievements.progress(player, "arrow_for_every_foe",1);
                     }else{
                        if(achievement.isAcquired()) return;
                        NbtCompound comp = achievement.getData();
                        if(!comp.contains(item.getId())){
                           comp.putBoolean(item.getId(),true);
                           baseAch.setData(comp);
                           profile.setAchievement(itemId, achievement);
                           ArcanaAchievements.progress(player, "arrow_for_every_foe",1);
                        }
                     }
                  }
               }
            }
            break;
         }
      }
   }
}
