package net.borisshoes.arcananovum.mixins;

import net.borisshoes.arcananovum.Arcananovum;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.callbacks.ShieldTimerCallback;
import net.borisshoes.arcananovum.items.*;
import net.borisshoes.arcananovum.items.charms.CindersCharm;
import net.borisshoes.arcananovum.items.charms.FelidaeCharm;
import net.borisshoes.arcananovum.items.core.MagicItem;
import net.borisshoes.arcananovum.utils.GenericTimer;
import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.borisshoes.arcananovum.utils.ParticleEffectUtils;
import net.borisshoes.arcananovum.utils.SoundUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.TimerTask;

import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
   
   @Shadow protected abstract void playBlockFallSound();
   
   @Shadow protected abstract void playEquipSound(ItemStack stack);
   
   // Mixin for Shield of Fortitude giving absorption hearts
   @Inject(method="damage",at=@At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;damageShield(F)V"))
   private void arcananovum_shieldAbsorb(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir){
      LivingEntity entity = (LivingEntity) (Object) this;
      ItemStack main = entity.getEquippedStack(EquipmentSlot.MAINHAND);
      ItemStack off = entity.getEquippedStack(EquipmentSlot.OFFHAND);
      MagicItem magic;
      ItemStack item = null;
      if(MagicItemUtils.isMagic(main)){
         magic = MagicItemUtils.identifyItem(main);
         item = main;
      }else if(MagicItemUtils.isMagic(off) && main.getItem() != Items.SHIELD){
         magic = MagicItemUtils.identifyItem(off);
         item = off;
      }else{
         return;
      }
      if(magic instanceof ShieldOfFortitude shield){
         float maxAbs = 10 + 2*Math.max(0,ArcanaAugments.getAugmentOnItem(item,"shield_of_faith"));
         float curAbs = entity.getAbsorptionAmount();
         float addedAbs = (float) Math.min(maxAbs,amount*.5);
         int duration = 200 + 100*Math.max(0,ArcanaAugments.getAugmentOnItem(item,"shield_of_resilience"));
         if(entity instanceof ServerPlayerEntity player){
            Arcananovum.addTickTimerCallback(new ShieldTimerCallback(duration,item,player,addedAbs));
            SoundUtils.playSongToPlayer(player,SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, 1, 1.8f);
         }
         entity.setAbsorptionAmount((curAbs + addedAbs));
      }
   }
   
   @Inject(method="damage",at=@At(value = "INVOKE", target = "Lnet/minecraft/world/World;getTime()J"))
   private void arcananovum_playerDamaged(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir){
      LivingEntity entity = (LivingEntity) (Object) this;
      if(entity instanceof ServerPlayerEntity player){
         PlayerInventory inv = player.getInventory();
         for(int i=0; i<inv.size();i++){
            ItemStack item = inv.getStack(i);
            if(item.isEmpty()){
               continue;
            }
      
            boolean isMagic = MagicItemUtils.isMagic(item);
            if(!isMagic)
               continue; // Item not magic, skip
      
            
         }
   
         // Stall Levitation Harness
         ItemStack chestItem = entity.getEquippedStack(EquipmentSlot.CHEST);
         if(MagicItemUtils.isMagic(chestItem) && player.getAbilities().flying){
            if(MagicItemUtils.identifyItem(chestItem) instanceof LevitationHarness harness){
               int sturdyLvl = Math.max(0,ArcanaAugments.getAugmentOnItem(chestItem,"sturdy_construction"));
               final double[] sturdyChance = {0,.15,.35,.5};
               if(Math.random() >= sturdyChance[sturdyLvl]){
                  int rebootLvl = Math.max(0,ArcanaAugments.getAugmentOnItem(chestItem,"fast_reboot"));
                  harness.setStall(chestItem,10-2*rebootLvl);
                  player.setHealth(player.getHealth()/2);
                  player.sendMessage(Text.literal("Your Harness Stalls!").formatted(Formatting.YELLOW,Formatting.ITALIC),true);
                  SoundUtils.playSound(player.getWorld(),player.getBlockPos(),SoundEvents.ITEM_SHIELD_BREAK, SoundCategory.PLAYERS,1, 0.7f);
                  ParticleEffectUtils.harnessStall(player.getWorld(),player.getPos().add(0,0.5,0));
                  
                  boolean eProt = Math.max(0,ArcanaAugments.getAugmentOnItem(chestItem,"emergency_protocol")) >= 1;
                  if(eProt){
                     StatusEffectInstance levit = new StatusEffectInstance(StatusEffects.LEVITATION, 100, 0, false, false, true);
                     player.addStatusEffect(levit);
                  }
               }
            }
         }
      }
   }
   
   // Mixin for shadow stalker's glaive doing damage
   @Inject(method="damage",at=@At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;applyDamage(Lnet/minecraft/entity/damage/DamageSource;F)V"))
   private void arcananovum_damageDealt(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir){
      LivingEntity entity = (LivingEntity) (Object) this;
      Entity attacker = source.getAttacker();
      if(attacker instanceof ServerPlayerEntity player){
         ItemStack weapon = player.getEquippedStack(EquipmentSlot.MAINHAND);
   
         if(MagicItemUtils.identifyItem(weapon) instanceof ShadowStalkersGlaive glaive){
            int oldEnergy = glaive.getEnergy(weapon);
            glaive.addEnergy(weapon, (int) amount);
            int newEnergy = glaive.getEnergy(weapon);
            if(oldEnergy/20 != newEnergy/20){
               String message = "Glaive Charges: ";
               for(int i=1; i<=5; i++){
                  message += newEnergy >= i*20 ? "✦ " : "✧ ";
               }
               player.sendMessage(Text.literal(message).formatted(Formatting.BLACK),true);
            }
         }
      }
   }
   
   
   // Mixin for damage mitigation for Wings of Zephyr, Charm of Felidae
   @Inject(method = "modifyAppliedDamage", at = @At("RETURN"), cancellable = true)
   private void arcananovum_modifyDamage(DamageSource source, float amount, CallbackInfoReturnable<Float> cir){
      float reduced = cir.getReturnValueF();
      float newReturn = reduced;
      LivingEntity entity = (LivingEntity) (Object) this;
   
      ItemStack chestItem = entity.getEquippedStack(EquipmentSlot.CHEST);
      if(MagicItemUtils.identifyItem(chestItem) instanceof WingsOfZephyr wings){
         boolean canReduce = source.equals(DamageSource.FALL) || source.equals(DamageSource.FLY_INTO_WALL) || ArcanaAugments.getAugmentOnItem(chestItem,"scales_of_enderia") >= 1;
         if(canReduce){
            int energy = wings.getEnergy(chestItem);
            double maxDmgReduction = reduced * .5;
            double dmgReduction = Math.min(energy / 100.0, maxDmgReduction);
            if(entity instanceof ServerPlayerEntity player){
               if(dmgReduction == maxDmgReduction || dmgReduction > 12){
                  if(source.equals(DamageSource.FALL) || source.equals(DamageSource.FLY_INTO_WALL)){
                     player.sendMessage(Text.literal("Your Armored Wings cushion your fall!").formatted(Formatting.GRAY, Formatting.ITALIC), true);
                  }
                  SoundUtils.playSongToPlayer(player, SoundEvents.ENTITY_ENDER_DRAGON_FLAP, 1, 1.3f);
                  Arcananovum.addTickTimerCallback(new GenericTimer(50, new TimerTask() {
                     @Override
                     public void run(){
                        player.sendMessage(Text.literal("Wing Energy Remaining: " + wings.getEnergy(chestItem)).formatted(Formatting.GRAY), true);
                     }
                  }));
               }
               PLAYER_DATA.get(player).addXP((int) dmgReduction * 25); // Add xp
               if(source.equals(DamageSource.FLY_INTO_WALL) && reduced > player.getHealth() && (reduced - dmgReduction) < player.getHealth())
                  ArcanaAchievements.grant(player, "see_glass");
            }
            wings.addEnergy(chestItem, (int) -dmgReduction * 100);
            newReturn = (float) (reduced - dmgReduction);
         }
   
         // Wing Buffet ability
         double buffetChance = new double[]{0,.1,.2,.3,.4,1}[Math.max(0,ArcanaAugments.getAugmentOnItem(chestItem,"wing_buffet"))];
         if(entity instanceof ServerPlayerEntity player && Math.random() < buffetChance){
            ServerWorld world = player.getWorld();
            Vec3d pos = player.getPos().add(0,player.getHeight()/2,0);
            Box rangeBox = new Box(pos.x+8,pos.y+8,pos.z+8,pos.x-8,pos.y-8,pos.z-8);
            int range = 3;
            List<Entity> entities = world.getOtherEntities(entity,rangeBox, e -> !e.isSpectator() && e.squaredDistanceTo(pos) < 1.5*range*range && (e instanceof MobEntity));
            for(Entity entity1 : entities){
               if(wings.getEnergy(chestItem) < 50) break;
               Vec3d diff = entity1.getPos().subtract(pos);
               double multiplier = MathHelper.clamp(range*.75-diff.length()*.5,.1,3);
               Vec3d motion = diff.multiply(1,0,1).add(0,1,0).normalize().multiply(multiplier);
               entity1.setVelocity(motion.x,motion.y,motion.z);
               SoundUtils.playSongToPlayer(player, SoundEvents.ENTITY_ENDER_DRAGON_FLAP, 1, .7f);
               wings.addEnergy(chestItem,-50);
            }
         }
      }
   
      if(entity instanceof ServerPlayerEntity player){
         PlayerInventory inv = player.getInventory();
         for(int i=0; i<inv.size();i++){
            ItemStack item = inv.getStack(i);
            if(item.isEmpty()){
               continue;
            }
         
            boolean isMagic = MagicItemUtils.isMagic(item);
            if(!isMagic)
               continue; // Item not magic, skip
            MagicItem magicItem = MagicItemUtils.identifyItem(item);
         
            if(magicItem instanceof FelidaeCharm && source.equals(DamageSource.FALL)){ // Felidae Charm
               int graceLvl = Math.max(0,ArcanaAugments.getAugmentOnItem(item,"feline_grace"));
               float dmgMod = (float) (0.5 - 0.125*graceLvl);
               SoundUtils.playSongToPlayer(player, SoundEvents.ENTITY_CAT_PURREOW, 1,1);
               float oldReturn = newReturn;
               newReturn = newReturn * dmgMod < 2 ? 0 : newReturn * dmgMod; // Reduce the damage, if the remaining damage is less than a heart, remove all of it.
               PLAYER_DATA.get(player).addXP(10*(int)(oldReturn-newReturn)); // Add xp
               if(oldReturn > player.getHealth() && newReturn < player.getHealth()) ArcanaAchievements.grant(player,"land_on_feet");
               break; // Make it so multiple charms don't stack
               
            }else if(magicItem instanceof PearlOfRecall pearl){ // Cancel all Pearls of Recall
               NbtCompound itemNbt = item.getNbt();
               NbtCompound magicNbt = itemNbt.getCompound("arcananovum");
               int defenseLvl = Math.max(0, ArcanaAugments.getAugmentOnItem(item,"phase_defense"));
               final double[] defenseChance = {0,.15,.35,.5};
            
               if(magicNbt.getInt("heat") > 0){
                  if(Math.random() >= defenseChance[defenseLvl]){
                     player.sendMessage(Text.literal("Your Recall Has Been Disrupted!").formatted(Formatting.RED, Formatting.ITALIC), true);
                     magicNbt.putInt("heat", -1);
                  }else{
                     newReturn = 0;
                  }
               }
            }else if(magicItem instanceof CindersCharm cinders){ // Cinders Charm Cremation
               boolean cremation = Math.max(0,ArcanaAugments.getAugmentOnItem(item,"cremation")) >= 1;
               if(cremation){
                  float oldReturn = newReturn;
                  int energy = cinders.getEnergy(item);
                  float dmgReduction = (float) Math.min(energy / 4.0, oldReturn);
                  newReturn = oldReturn - dmgReduction;
                  cinders.addEnergy(item, (int) -dmgReduction * 4);
   
                  energy = cinders.getEnergy(item);
                  StringBuilder message = new StringBuilder("Cinders: ");
                  for(int j = 1; j <= cinders.getMaxEnergy(item)/20; j++){
                     message.append(energy >= j * 20 ? "✦ " : "✧ ");
                  }
                  player.sendMessage(Text.literal(message.toString()).formatted(Formatting.AQUA), true);
               }
            }
         }
      }
      cir.setReturnValue(newReturn);
   }
   
   
   /*@Inject(method = "swingHand(Lnet/minecraft/util/Hand;Z)V", at = @At("HEAD"))
   public void swingHand(Hand hand, boolean fromServerPlayer, CallbackInfo ci) {
      LivingEntity entity = (LivingEntity) (Object) this;
      System.out.println("This is a left click?");
      if (!entity.world.isClient) {
         if(entity instanceof ServerPlayerEntity player && entity.world instanceof ServerWorld world){
            LeftClickEvent.EVENT.invoker().onPlayerLeftClick(player,world,hand);
         }
      }
   }*/
}
