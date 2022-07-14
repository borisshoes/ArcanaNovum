package net.borisshoes.arcananovum.mixins;

import net.borisshoes.arcananovum.cardinalcomponents.MagicEntity;
import net.borisshoes.arcananovum.items.MagicItem;
import net.borisshoes.arcananovum.items.RunicArrow;
import net.borisshoes.arcananovum.items.RunicBow;
import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.borisshoes.arcananovum.utils.SoundUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import static net.borisshoes.arcananovum.cardinalcomponents.MagicEntityComponentInitializer.MAGIC_ENTITY_LIST;
import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;

@Mixin(BowItem.class)
public class BowMixin {
   @Inject(method="onStoppedUsing",at=@At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnEntity(Lnet/minecraft/entity/Entity;)Z"), locals = LocalCapture.CAPTURE_FAILHARD)
   private void bowFired(ItemStack stack, World world, LivingEntity user, int remainingUseTicks, CallbackInfo ci, PlayerEntity playerEntity, boolean bl, ItemStack itemStack, int i, float f, boolean bl2, ArrowItem arrowItem, PersistentProjectileEntity persistentProjectileEntity){
      // itemStack = arrow | stack = bow
      
      if(MagicItemUtils.isMagic(stack) && MagicItemUtils.isMagic(itemStack)){
         MagicItem magicBow = MagicItemUtils.identifyItem(stack);
         MagicItem magicArrow = MagicItemUtils.identifyItem(itemStack);
         if(magicBow instanceof RunicBow && magicArrow instanceof RunicArrow){
            NbtCompound arrowData = new NbtCompound();
            arrowData.putString("UUID", magicArrow.getUUID(itemStack));
            arrowData.putString("id",magicArrow.getId());
            MagicEntity magicPearl = new MagicEntity(persistentProjectileEntity.getUuidAsString(), arrowData);
            MAGIC_ENTITY_LIST.get(world).addEntity(magicPearl);
   
            SoundUtils.playSound(world,playerEntity.getBlockPos(),SoundEvents.ITEM_TRIDENT_THROW,SoundCategory.PLAYERS,0.8f,1.0F / (world.getRandom().nextFloat() * 0.4F + 1.2F) + f * 0.5F);
            PLAYER_DATA.get(playerEntity).addXP(50);
            
            //System.out.println("Firing magic bow with magic arrow ("+magicArrow.getId()+"): "+persistentProjectileEntity.getUuidAsString());
         }
      }
   }
}
