package net.borisshoes.arcananovum.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.borisshoes.arcananovum.ArcanaConfig;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.bosses.BossFights;
import net.borisshoes.arcananovum.callbacks.EntityKilledCallback;
import net.borisshoes.arcananovum.callbacks.ShieldTimerCallback;
import net.borisshoes.arcananovum.callbacks.VengeanceTotemTimerCallback;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.damage.ArcanaDamageTypes;
import net.borisshoes.arcananovum.effects.DamageAmpEffect;
import net.borisshoes.arcananovum.effects.GreaterInvisibilityEffect;
import net.borisshoes.arcananovum.entities.NulConstructEntity;
import net.borisshoes.arcananovum.events.CleansingCharmEvent;
import net.borisshoes.arcananovum.items.*;
import net.borisshoes.arcananovum.items.charms.CindersCharm;
import net.borisshoes.arcananovum.items.charms.FelidaeCharm;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaColors;
import net.borisshoes.arcananovum.utils.ArcanaEffectUtils;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.arcananovum.utils.ArcanaUtils;
import net.borisshoes.borislib.BorisLib;
import net.borisshoes.borislib.events.Event;
import net.borisshoes.borislib.timers.GenericTimer;
import net.borisshoes.borislib.timers.TickTimerCallback;
import net.borisshoes.borislib.utils.MinecraftUtils;
import net.borisshoes.borislib.utils.SoundUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.Tuple;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.CombatEntry;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DeathProtection;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static net.borisshoes.arcananovum.cardinalcomponents.WorldDataComponentInitializer.BOSS_FIGHT;
import static net.borisshoes.borislib.BorisLib.SERVER_TIMER_CALLBACKS;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
   
   @Shadow protected abstract void playBlockFallSound();
   
   @Shadow protected abstract void playHurtSound(DamageSource source);
   
   @Shadow public abstract void onEnterCombat();
   
   @Inject(method = "die", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;gameEvent(Lnet/minecraft/core/Holder;)V"))
   private void arcananovum$onEntityKilledOther(DamageSource damageSource, CallbackInfo ci, @Local Entity attacker, @Local ServerLevel serverWorld){
      LivingEntity thisEntity = (LivingEntity) (Object) this;
      EntityKilledCallback.killedEntity(serverWorld,damageSource,attacker,thisEntity);
   }
   
   @Inject(method= "die", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getCombatTracker()Lnet/minecraft/world/damagesource/CombatTracker;"))
   private void arcananovum$witherOneHit(DamageSource source, CallbackInfo ci){
      if((LivingEntity)(Object)this instanceof WitherBoss wither && wither.isDeadOrDying()){
         List<CombatEntry> record = wither.getCombatTracker().entries;
         if(!record.isEmpty()){
            float actualDmg = record.getLast().damage();
            if(actualDmg >= wither.getMaxHealth() && source.getWeaponItem() != null && source.getWeaponItem().is(ArcanaRegistry.GRAVITON_MAUL.getItem()) && source.getEntity() instanceof ServerPlayer player){
               ArcanaAchievements.grant(player,ArcanaAchievements.BONE_SMASHER);
            }
         }
      }
   }
   
   @Inject(method = "addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z", at = @At("RETURN"))
   private void arcananovum$onStatusEffectAdd(MobEffectInstance effect, Entity source, CallbackInfoReturnable<Boolean> cir){
      if(cir.getReturnValueZ()){
         
         if(source instanceof ServerPlayer player && Event.getEventsOfType(CleansingCharmEvent.class).stream().anyMatch(event -> event.getPlayer().equals(player) && event.getEffect().equals(effect.getEffect()))){
            ArcanaAchievements.grant(player,ArcanaAchievements.CHRONIC_AILMENT);
         }
      }
   }
   
   @Inject(method= "die", at = @At("RETURN"))
   private void arcananovum$onDeath(DamageSource damageSource, CallbackInfo ci){
      LivingEntity livingEntity = (LivingEntity) (Object) this;
      for(TickTimerCallback t : SERVER_TIMER_CALLBACKS){
         if(t instanceof VengeanceTotemTimerCallback vt && vt.getAttacker() != null && vt.getAttacker().getStringUUID().equals(livingEntity.getStringUUID())){
            vt.setAvenged();
         }
      }
   }
   
   @ModifyExpressionValue(method = "applyItemBlocking", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/component/BlocksAttacks;resolveBlockedDamage(Lnet/minecraft/world/damagesource/DamageSource;FD)F"))
   private float arcananovum$whiteDwarfBlock(float original, ServerLevel world, DamageSource source, float damage, @Local ItemStack blockStack){
      LivingEntity entity = (LivingEntity) (Object) this;
      ItemStack mainhand = entity.getMainHandItem();
      if(!(ArcanaItemUtils.identifyItem(mainhand) instanceof BinaryBlades blades)) return original;
      int whiteDwarf = ArcanaAugments.getAugmentOnItem(mainhand, ArcanaAugments.WHITE_DWARF_BLADES.id);
      if(whiteDwarf < 1) return original;
      int energy = blades.getEnergy(mainhand);
      float dmgReduction = (float) Math.min(energy / 5.0, original);
      if(dmgReduction > 0){
         ArcanaItem.putProperty(mainhand,BinaryBlades.LAST_HIT_TAG,20);
         blades.addEnergy(mainhand, (int) -dmgReduction * 5);
         return dmgReduction;
      }else{
         return 0;
      }
   }
   
   
   // Mixin for Shield of Fortitude giving absorption hearts
   @Inject(method= "applyItemBlocking",at=@At(value = "INVOKE", target = "Lnet/minecraft/world/item/component/BlocksAttacks;hurtBlockingItem(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/InteractionHand;F)V"))
   private void arcananovum$shieldAbsorb(ServerLevel world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir, @Local ItemStack shieldStack, @Local(ordinal = 0, argsOnly = true) float blocked){
      LivingEntity entity = (LivingEntity) (Object) this;
      ArcanaItem arcaneItem;
      ItemStack item = null;
      
      if(ArcanaItemUtils.isArcane(shieldStack)){
         arcaneItem = ArcanaItemUtils.identifyItem(shieldStack);
         item = shieldStack;
      }else{
         return;
      }
      if(arcaneItem instanceof ShieldOfFortitude shield){
         shield.shieldBlock(entity,item,blocked);
      }
   }
   
   @Inject(method= "hurtServer",at=@At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getGameTime()J"))
   private void arcananovum$playerDamaged(ServerLevel world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir){
      LivingEntity entity = (LivingEntity) (Object) this;
      if(entity instanceof ServerPlayer player){
         Inventory inv = player.getInventory();
         for(int i = 0; i<inv.getContainerSize(); i++){
            ItemStack item = inv.getItem(i);
            if(item.isEmpty()){
               continue;
            }
      
            boolean isArcane = ArcanaItemUtils.isArcane(item);
            if(!isArcane)
               continue; // Item not arcane, skip
      
            
         }
   
         // Stall Levitation Harness
         ItemStack chestItem = entity.getItemBySlot(EquipmentSlot.CHEST);
         if(ArcanaItemUtils.isArcane(chestItem) && player.getAbilities().flying){
            if(ArcanaItemUtils.identifyItem(chestItem) instanceof LevitationHarness harness){
               int sturdyLvl = Math.max(0,ArcanaAugments.getAugmentOnItem(chestItem,ArcanaAugments.STURDY_CONSTRUCTION.id));
               final double[] sturdyChance = {0,.15,.35,.5};
               if(Math.random() >= sturdyChance[sturdyLvl]){
                  int rebootLvl = Math.max(0,ArcanaAugments.getAugmentOnItem(chestItem,ArcanaAugments.FAST_REBOOT.id));
                  harness.setStall(chestItem,15-2*rebootLvl);
                  player.setHealth(player.getHealth()/2);
                  player.displayClientMessage(Component.literal("Your Harness Stalls!").withStyle(ChatFormatting.YELLOW, ChatFormatting.ITALIC),true);
                  SoundUtils.playSound(player.level(),player.blockPosition(), SoundEvents.SHIELD_BREAK, SoundSource.PLAYERS,1, 0.7f);
                  ArcanaEffectUtils.harnessStall(player.level(),player.position().add(0,0.5,0));
                  
                  boolean eProt = Math.max(0,ArcanaAugments.getAugmentOnItem(chestItem,ArcanaAugments.EMERGENCY_PROTOCOL.id)) >= 1;
                  if(eProt){
                     MobEffectInstance levit = new MobEffectInstance(MobEffects.LEVITATION, 100, 0, false, false, true);
                     player.addEffect(levit);
                  }
               }
            }
         }
         
         if(source.is(DamageTypes.STARVE)){
            ArcanaNovum.data(player).setResearchTask(ResearchTasks.HUNGER_DAMAGE, true);
         }
         
         if(source.is(ArcanaDamageTypes.CONCENTRATION)){
            ArcanaNovum.data(player).setResearchTask(ResearchTasks.CONCENTRATION_DAMAGE, true);
         }
         
         if(source.is(DamageTypes.DROWN)){
            ArcanaNovum.data(player).setResearchTask(ResearchTasks.DROWNING_DAMAGE, true);
         }
      }
   }
   
   // Mixin for shadow stalker's glaive doing damage
   @Inject(method= "hurtServer",at=@At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;actuallyHurt(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)V"))
   private void arcananovum$damageDealt(ServerLevel world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir){
      LivingEntity entity = (LivingEntity) (Object) this;
      Entity attacker = source.getEntity();
      if(attacker instanceof ServerPlayer player){
         ItemStack weapon = player.getItemBySlot(EquipmentSlot.MAINHAND);
   
         if(ArcanaItemUtils.identifyItem(weapon) instanceof ShadowStalkersGlaive glaive){
            int oldEnergy = glaive.getEnergy(weapon);
            glaive.addEnergy(weapon, (int) amount);
            int newEnergy = glaive.getEnergy(weapon);
            if(oldEnergy/20 != newEnergy/20){
               String message = "Glaive Charges: ";
               for(int i=1; i<=5; i++){
                  message += newEnergy >= i*20 ? "✦ " : "✧ ";
               }
               player.displayClientMessage(Component.literal(message).withColor(ArcanaColors.NUL_COLOR),true);
            }
         }
      }
   }
   
   @Inject(method = "hurtServer", at = @At("HEAD"), cancellable = true, order = 892)
   private void arcananovum$cancelDamage(ServerLevel world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir){
      LivingEntity entity = (LivingEntity) (Object) this;
      
      if(entity instanceof ServerPlayer player){
         if(source.is(DamageTypeTags.IS_FALL) && !ArcanaUtils.getArcanaItemsWithAug(player,ArcanaRegistry.FELIDAE_CHARM, ArcanaAugments.FELINE_GRACE,4).isEmpty()){
            SoundUtils.playSongToPlayer(player, SoundEvents.CAT_PURREOW, 1,1);
            ArcanaNovum.data(player).addXP((int) Math.min(ArcanaNovum.CONFIG.getInt(ArcanaRegistry.FELIDAE_CHARM_FALL_CAP),ArcanaNovum.CONFIG.getInt(ArcanaRegistry.FELIDAE_CHARM_FALL)*(amount))); // Add xp
            if(amount > player.getHealth()) ArcanaAchievements.grant(player,ArcanaAchievements.LAND_ON_FEET.id);
            cir.setReturnValue(false);
         }
      }
   }
   
   // Mixin for damage modifiers
   @ModifyReturnValue(method = "getDamageAfterMagicAbsorb", at = @At("RETURN"))
   private float arcananovum$modifyDamage(float original, DamageSource source, float amount){
      float newReturn = original;
      LivingEntity entity = (LivingEntity) (Object) this;
      Entity attacker = source.getEntity();
      
      if(attacker instanceof ServerPlayer player){
         // Juggernaut Augment
         ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
         if(ArcanaItemUtils.identifyItem(boots) instanceof SojournerBoots sojournerBoots && source.isDirect()){
            boolean juggernaut = ArcanaAugments.getAugmentOnItem(boots,ArcanaAugments.JUGGERNAUT.id) >= 1;
            int energy = sojournerBoots.getEnergy(boots);
            if(juggernaut && energy >= 200){
               MobEffectInstance slow = new MobEffectInstance(MobEffects.SLOWNESS, 60, 4, false, false, true);
               MobEffectInstance dmgAmp = new MobEffectInstance(ArcanaRegistry.DAMAGE_AMP_EFFECT, 100, 1, false, true, false);
               entity.addEffect(slow);
               entity.addEffect(dmgAmp);
               sojournerBoots.setEnergy(boots,0);
               SoundUtils.playSound(player.level(),entity.blockPosition(), SoundEvents.IRON_GOLEM_HURT, SoundSource.PLAYERS, .5f, .8f);
            }
         }
         
         
         // Shield Bash Augment
         ItemStack shieldStack = null;
         if(ArcanaItemUtils.identifyItem(player.getItemBySlot(EquipmentSlot.OFFHAND)) instanceof ShieldOfFortitude){
            shieldStack = player.getItemBySlot(EquipmentSlot.OFFHAND);
         }else if(ArcanaItemUtils.identifyItem(player.getItemBySlot(EquipmentSlot.MAINHAND)) instanceof ShieldOfFortitude){
            shieldStack = player.getItemBySlot(EquipmentSlot.MAINHAND);
         }
         
         if(shieldStack != null && ArcanaAugments.getAugmentOnItem(shieldStack,ArcanaAugments.SHIELD_BASH.id) >= 1 && !player.getCooldowns().isOnCooldown(shieldStack) && source.isDirect()){
            ArrayList<ShieldTimerCallback> toRemove = new ArrayList<>();
            float shieldTotal = 0;
            float absAmt = player.getAbsorptionAmount();
            for(int i = 0; i < SERVER_TIMER_CALLBACKS.size(); i++){
               TickTimerCallback t = SERVER_TIMER_CALLBACKS.get(i);
               if(t instanceof ShieldTimerCallback st && st.getPlayer().getStringUUID().equals(player.getStringUUID())){
                  shieldTotal += st.getHearts();
                  toRemove.add(st);
               }
            }
            shieldTotal = Math.min(Math.min(absAmt,shieldTotal),50);
            if(shieldTotal >= 20){
               MobEffectInstance slow = new MobEffectInstance(MobEffects.SLOWNESS, 60, 4, false, false, true);
               MobEffectInstance dmgAmp = new MobEffectInstance(ArcanaRegistry.DAMAGE_AMP_EFFECT, (int) (shieldTotal*5), (int) (shieldTotal/20), false, true, false);
               entity.addEffect(slow);
               entity.addEffect(dmgAmp);
               toRemove.forEach(ShieldTimerCallback::onTimer);
               SERVER_TIMER_CALLBACKS.removeIf(toRemove::contains); // Remove all absorption callbacks
               int duration = 200 + 100*Math.max(0,ArcanaAugments.getAugmentOnItem(shieldStack,ArcanaAugments.SHIELD_OF_RESILIENCE.id));
               BorisLib.addTickTimerCallback(new ShieldTimerCallback(duration,shieldStack,player,20)); // Put 10 hearts back
               MinecraftUtils.addMaxAbsorption(player, ShieldOfFortitude.EFFECT_ID,20f);
               player.setAbsorptionAmount(player.getAbsorptionAmount() + 20f);
               player.getCooldowns().addCooldown(shieldStack,100);
               SoundUtils.playSound(player.level(),entity.blockPosition(), SoundEvents.IRON_GOLEM_HURT, SoundSource.PLAYERS, .5f, .8f);
            }
         }
      }
      
      // Damage Amp
      if(entity.hasEffect(ArcanaRegistry.DAMAGE_AMP_EFFECT)){
         MobEffectInstance effect = entity.getEffect(ArcanaRegistry.DAMAGE_AMP_EFFECT);
         DamageAmpEffect.tryTrackDamage(effect.getAmplifier(),newReturn,entity);
         newReturn = DamageAmpEffect.buffDamage(newReturn,effect.getAmplifier());
      }
   
      if(entity instanceof ServerPlayer player){
         List<Tuple<List<ItemStack>, ItemStack>> allItems = ArcanaUtils.getAllItems(player);
         boolean procdFelidae = false;
         
         for(int i = 0; i < allItems.size(); i++){
            List<ItemStack> itemList = allItems.get(i).getA();
            ItemStack carrier = allItems.get(i).getB();
            SimpleContainer sinv = new SimpleContainer(itemList.size());
            
            for(int j = 0; j < itemList.size(); j++){
               ItemStack item = itemList.get(j);
               
               boolean isArcane = ArcanaItemUtils.isArcane(item);
               if(!isArcane){
                  sinv.setItem(j, item);
                  continue; // Item not arcane, skip
               }
               ArcanaItem arcanaItem = ArcanaItemUtils.identifyItem(item);
               
               if((arcanaItem instanceof FelidaeCharm) && source.is(DamageTypeTags.IS_FALL) && !procdFelidae){ // Felidae Charm
                  int graceLvl = Math.max(0, ArcanaAugments.getAugmentOnItem(item, ArcanaAugments.FELINE_GRACE.id));
                  float dmgMod = (float) (0.5 - 0.125 * graceLvl);
                  SoundUtils.playSongToPlayer(player, SoundEvents.CAT_PURREOW, 1, 1);
                  float oldReturn = newReturn;
                  newReturn = newReturn * dmgMod < 2 ? 0 : newReturn * dmgMod; // Reduce the damage, if the remaining damage is less than a heart, remove all of it.
                  ArcanaNovum.data(player).addXP((int) Math.min(ArcanaNovum.CONFIG.getInt(ArcanaRegistry.FELIDAE_CHARM_FALL_CAP), ArcanaNovum.CONFIG.getInt(ArcanaRegistry.FELIDAE_CHARM_FALL) * (oldReturn - newReturn))); // Add xp
                  if(oldReturn > player.getHealth() && newReturn < player.getHealth())
                     ArcanaAchievements.grant(player, ArcanaAchievements.LAND_ON_FEET.id);
                  procdFelidae = true; // Make it so multiple charms don't stack
                  
               }else if(arcanaItem instanceof PearlOfRecall pearl){ // Cancel all Pearls of Recall
                  int defenseLvl = Math.max(0, ArcanaAugments.getAugmentOnItem(item, ArcanaAugments.PHASE_DEFENSE.id));
                  final double[] defenseChance = {0, .15, .35, .5};
                  
                  
                  if(ArcanaItem.getIntProperty(item, PearlOfRecall.HEAT_TAG) > 0){
                     if(Math.random() >= defenseChance[defenseLvl]){
                        player.displayClientMessage(Component.literal("Your Recall Has Been Disrupted!").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC), true);
                        ArcanaItem.putProperty(item, PearlOfRecall.HEAT_TAG, -1);
                     }else{
                        newReturn = 0;
                     }
                  }
               }else if(arcanaItem instanceof Planeshifter){ // Cancel all Planeshifters
                  if(ArcanaItem.getIntProperty(item, Planeshifter.HEAT_TAG) > 0){
                     player.displayClientMessage(Component.literal("Your Plane-Shift Has Been Disrupted!").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC), true);
                     ArcanaItem.putProperty(item, Planeshifter.HEAT_TAG, -1);
                  }
               }else if(arcanaItem instanceof CindersCharm cinders && source.is(DamageTypeTags.IS_FIRE)){ // Cinders Charm Cremation
                  boolean cremation = Math.max(0, ArcanaAugments.getAugmentOnItem(item, ArcanaAugments.CREMATION.id)) >= 1;
                  if(cremation){
                     final double energyPerDamage = 15;
                     float oldReturn = newReturn;
                     int energy = cinders.getEnergy(item);
                     float dmgReduction = (float) Math.min(energy / energyPerDamage, oldReturn);
                     newReturn = oldReturn - dmgReduction;
                     cinders.addEnergy(item, (int) (-dmgReduction * energyPerDamage));
                     
                     energy = cinders.getEnergy(item);
                     StringBuilder message = new StringBuilder("Cinders: ");
                     for(int k = 1; k <= cinders.getMaxEnergy(item) / 20; k++){
                        message.append(energy >= k * 20 ? "✦ " : "✧ ");
                     }
                     player.displayClientMessage(Component.literal(message.toString()).withStyle(ChatFormatting.AQUA), true);
                  }
               }
               
               sinv.setItem(j, item);
            }
            
            if(ArcanaItemUtils.identifyItem(carrier) instanceof ArcanistsBelt belt){
               belt.buildItemLore(carrier, BorisLib.SERVER);
            }
         }
      }
      
      ItemStack chestItem = entity.getItemBySlot(EquipmentSlot.CHEST);
      if(ArcanaItemUtils.identifyItem(chestItem) instanceof WingsOfEnderia wings){
         boolean canReduce = source.is(DamageTypeTags.IS_FALL) || source.is(DamageTypes.FLY_INTO_WALL) || ArcanaAugments.getAugmentOnItem(chestItem,ArcanaAugments.SCALES_OF_THE_CHAMPION.id) >= 2;
         if(canReduce){
            int energy = wings.getEnergy(chestItem);
            float maxDmgReduction = newReturn * .5f;
            float dmgReduction = (float) Math.min(energy / 100.0, maxDmgReduction);
            if(entity instanceof ServerPlayer player){
               if(dmgReduction >= 4){
                  if(source.is(DamageTypeTags.IS_FALL) || source.is(DamageTypes.FLY_INTO_WALL)){
                     player.displayClientMessage(Component.literal("Your Armored Wings cushion your fall!").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC), true);
                  }
                  SoundUtils.playSongToPlayer(player, SoundEvents.ENDER_DRAGON_FLAP, 1, 1.3f);
                  BorisLib.addTickTimerCallback(new GenericTimer(50, () -> player.displayClientMessage(Component.literal("Wing Energy Remaining: " + wings.getEnergy(chestItem)).withStyle(ChatFormatting.DARK_PURPLE), true)));
               }
               ArcanaNovum.data(player).addXP((int) Math.min(ArcanaNovum.CONFIG.getInt(ArcanaRegistry.WINGS_OF_ENDERIA_CUSHION_CAP),ArcanaNovum.CONFIG.getInt(ArcanaRegistry.WINGS_OF_ENDERIA_CUSHION)*dmgReduction)); // Add xp
               if(source.is(DamageTypes.FLY_INTO_WALL) && newReturn > player.getHealth() && (newReturn - dmgReduction) < player.getHealth())
                  ArcanaAchievements.grant(player, ArcanaAchievements.SEE_GLASS.id);
            }
            wings.addEnergy(chestItem, (int) -dmgReduction * 100);
            newReturn -= dmgReduction;
         }
         
         // Wing Buffet ability
         int buffetLvl = ArcanaAugments.getAugmentOnItem(chestItem,ArcanaAugments.WING_BUFFET);
         if(entity instanceof ServerPlayer player && buffetLvl > 0){
            ServerLevel world = player.level();
            Vec3 pos = player.position().add(0,player.getBbHeight()/2,0);
            AABB rangeBox = new AABB(pos.x+8,pos.y+8,pos.z+8,pos.x-8,pos.y-8,pos.z-8);
            int range = 3;
            List<Entity> entities = world.getEntities(entity,rangeBox, e -> !e.isSpectator() && e.distanceToSqr(pos) < 1.5*range*range && (e instanceof Mob));
            boolean triggered = false;
            for(Entity entity1 : entities){
               if(wings.getEnergy(chestItem) < 50) break;
               Vec3 diff = entity1.position().subtract(pos);
               double multiplier = Mth.clamp(range*.75-diff.length()*.5,.1,3);
               Vec3 motion = diff.multiply(1,0,1).add(0,1,0).normalize().scale(multiplier);
               if(entity1 instanceof ServerPlayer otherPlayer){
                  if(buffetLvl >= 2){
                     otherPlayer.setDeltaMovement(motion.x,motion.y,motion.z);
                     otherPlayer.connection.send(new ClientboundSetEntityMotionPacket(otherPlayer));
                     wings.addEnergy(chestItem,-100);
                     triggered = true;
                  }
               }else{
                  entity1.setDeltaMovement(motion.x,motion.y,motion.z);
                  wings.addEnergy(chestItem,-50);
                  triggered = true;
               }
            }
            if(triggered) SoundUtils.playSongToPlayer(player, SoundEvents.ENDER_DRAGON_FLAP, 1, .7f);
         }
      }
   
      
      // Enderia Boss health scale
      Tuple<BossFights, CompoundTag> bossFight = BOSS_FIGHT.get(entity.level()).getBossFight();
      int numPlayers = 0;
      if(bossFight != null){
         numPlayers = bossFight.getB().getIntOr("numPlayers", 0);
      }
      if(numPlayers != 0){
         float scale = Math.max(2f/numPlayers, 0.1f);
         if(entity instanceof EnderDragon){
            newReturn *= scale; //Effective Health Scale to bypass 1024 hp cap
            if(source.is(DamageTypeTags.BYPASSES_ARMOR) || source.is(DamageTypeTags.IS_EXPLOSION)) newReturn *= 0.25f; // Reduce damage from magic and explosive sources
         }
      }
      
      // Death Ward
      if(entity.hasEffect(ArcanaRegistry.DEATH_WARD_EFFECT) && (!source.is(DamageTypeTags.BYPASSES_RESISTANCE) || source.is(ArcanaDamageTypes.CONCENTRATION))){
         MobEffectInstance effect = entity.getEffect(ArcanaRegistry.DEATH_WARD_EFFECT);
         if(entity.getHealth() < newReturn){
            float damageWarded = newReturn;
            newReturn = entity.getHealth() - 0.01f;
            damageWarded -= newReturn;
            
            if(entity instanceof ServerPlayer player && ArcanaAchievements.isTimerActive(player, ArcanaAchievements.TOO_ANGRY_TO_DIE.id)){
               ArcanaAchievements.progress(player,ArcanaAchievements.TOO_ANGRY_TO_DIE.id, Math.round(damageWarded));
            }
         }
      }
      
      return newReturn;
   }
   
   @ModifyExpressionValue(method = "checkTotemDeathProtection", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/damagesource/DamageSource;is(Lnet/minecraft/tags/TagKey;)Z"))
   public boolean arcananovum$totemDamageCheck(boolean original, DamageSource source){
      if (!original) return false;
      return !source.is(ArcanaRegistry.ALLOW_TOTEM_USAGE);
   }
   
   @ModifyExpressionValue(method = "checkTotemDeathProtection", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getItemInHand(Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/item/ItemStack;"))
   public ItemStack arcananovum$deathProtectorCooldown(ItemStack original){
      LivingEntity livingEntity = (LivingEntity) (Object) this;
      if(livingEntity instanceof ServerPlayer player){
         if(player.getCooldowns().isOnCooldown(original)){
            return ItemStack.EMPTY;
         }
      }
      return original;
   }
   
   @Inject(method = "checkTotemDeathProtection", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;setHealth(F)V"), cancellable = true)
   public void arcananovum$vengeanceTrigger(DamageSource source, CallbackInfoReturnable<Boolean> cir, @Local(ordinal = 0) ItemStack itemStack){
      LivingEntity livingEntity = (LivingEntity) (Object) this;
      List<NulConstructEntity> constructs = livingEntity.level().getEntitiesOfClass(NulConstructEntity.class,livingEntity.getBoundingBox().inflate(NulConstructEntity.FIGHT_RANGE*2), construct -> construct.getSummoner().getUUID().equals(livingEntity.getUUID()));
      
      if(ArcanaItemUtils.identifyItem(itemStack) instanceof TotemOfVengeance vengeance){
         constructs.forEach(construct -> construct.triggerAdaptation(NulConstructEntity.ConstructAdaptations.USED_VENGEANCE_TOTEM));
         vengeance.triggerTotem(itemStack, livingEntity, source);
      }else{
         if(constructs.stream().anyMatch(construct -> construct.hasActivatedAdaptation(NulConstructEntity.ConstructAdaptations.USED_TOTEM))){
            cir.setReturnValue(false);
         }
         constructs.forEach(construct -> construct.triggerAdaptation(NulConstructEntity.ConstructAdaptations.USED_TOTEM));
      }
   }
   
   @Inject(method = "checkTotemDeathProtection", at = @At("RETURN"), cancellable = true)
   public void arcananovum$findTotem(DamageSource source, CallbackInfoReturnable<Boolean> cir){
      LivingEntity livingEntity = (LivingEntity) (Object) this;
      if(cir.getReturnValueZ()) return;
      
      if(source.is(DamageTypeTags.BYPASSES_INVULNERABILITY) && !source.is(ArcanaRegistry.ALLOW_TOTEM_USAGE)) return;
      
      ItemStack itemStack = null;
      DeathProtection deathProtectionComponent = null;
      
      if(livingEntity instanceof ServerPlayer player){
         ItemCooldowns cooldowns = player.getCooldowns();
         Container inv = player.getInventory();
         block: {
            for(int i = 0; i < inv.getContainerSize(); i++){
               ItemStack beltStack = inv.getItem(i);
               if(ArcanaItemUtils.identifyItem(beltStack) instanceof ArcanistsBelt){
                  ItemContainerContents beltItems = beltStack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
                  for(ItemStack stack : beltItems.nonEmptyItems()){
                     if(cooldowns.isOnCooldown(stack)) continue;
                     deathProtectionComponent = stack.get(DataComponents.DEATH_PROTECTION);
                     if (deathProtectionComponent != null) {
                        itemStack = stack.copy();
                        stack.shrink(1);
                        break block;
                     }
                  }
               }
            }
         }
      }
      
      List<NulConstructEntity> constructs = livingEntity.level().getEntitiesOfClass(NulConstructEntity.class,livingEntity.getBoundingBox().inflate(NulConstructEntity.FIGHT_RANGE*2), construct -> construct.getSummoner() != null && construct.getSummoner().getUUID().equals(livingEntity.getUUID()));
      if (itemStack != null) {
         if(ArcanaItemUtils.identifyItem(itemStack) instanceof TotemOfVengeance vengeance){
            constructs.forEach(construct -> construct.triggerAdaptation(NulConstructEntity.ConstructAdaptations.USED_VENGEANCE_TOTEM));
            vengeance.triggerTotem(itemStack, livingEntity, source);
         }else{
            if(constructs.stream().anyMatch(construct -> construct.hasActivatedAdaptation(NulConstructEntity.ConstructAdaptations.USED_TOTEM))){
               cir.setReturnValue(false);
               constructs.forEach(construct -> construct.triggerAdaptation(NulConstructEntity.ConstructAdaptations.USED_TOTEM));
               return;
            }else{
               constructs.forEach(construct -> construct.triggerAdaptation(NulConstructEntity.ConstructAdaptations.USED_TOTEM));
            }
         }
         
         
         if(livingEntity instanceof ServerPlayer player){
            player.awardStat(Stats.ITEM_USED.get(itemStack.getItem()));
            CriteriaTriggers.USED_TOTEM.trigger(player, itemStack);
            livingEntity.gameEvent(GameEvent.ITEM_INTERACT_FINISH);
         }
         
         livingEntity.setHealth(1.0F);
         if(ArcanaItemUtils.identifyItem(itemStack) instanceof TotemOfVengeance vengeance){
            vengeance.triggerTotem(itemStack, livingEntity, source); // TODO, maybe turn this into a death effect thing?
         }
         if(deathProtectionComponent != null){
            deathProtectionComponent.applyEffects(itemStack, livingEntity);
         }
         livingEntity.level().broadcastEntityEvent(livingEntity, EntityEvent.PROTECTED_FROM_DEATH);
         cir.setReturnValue(true);
         return;
      }
      
      ItemStack headStack = livingEntity.getItemBySlot(EquipmentSlot.HEAD);
      if(ArcanaItemUtils.identifyItem(headStack) instanceof NulMemento memento && memento.protectFromDeath(headStack,livingEntity,source,!constructs.isEmpty())){
         constructs.forEach(construct -> construct.triggerAdaptation(NulConstructEntity.ConstructAdaptations.USED_MEMENTO));
         cir.setReturnValue(true);
         return;
      }
   }
   
   @Inject(method= "onEffectsRemoved",at=@At(value = "INVOKE", target = "Lnet/minecraft/world/effect/MobEffect;removeAttributeModifiers(Lnet/minecraft/world/entity/ai/attributes/AttributeMap;)V"))
   private void arcananovum$effectRemoved(Collection<MobEffectInstance> effects, CallbackInfo ci){
      LivingEntity livingEntity = (LivingEntity) (Object) this;
      for(MobEffectInstance effect : effects){
         if(effect.getEffect() == ArcanaRegistry.GREATER_INVISIBILITY_EFFECT && livingEntity.level().getServer() != null){
            GreaterInvisibilityEffect.removeInvis(livingEntity.level().getServer(),livingEntity);
         }
      }
   }
   
   @Inject(method= "updateInvisibilityStatus", at=@At(value="INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;setInvisible(Z)V", ordinal = 1, shift = At.Shift.AFTER))
   private void arcananovum$greaterInvisibilityUpdate(CallbackInfo ci){
      LivingEntity livingEntity = (LivingEntity) (Object) this;
      livingEntity.setInvisible(livingEntity.isInvisible() || livingEntity.hasEffect(ArcanaRegistry.GREATER_INVISIBILITY_EFFECT));
   }
   
   @ModifyReturnValue(method= "getVisibilityPercent", at=@At("RETURN"))
   private double arcananovum_greaterInvisibilityAttackRangeScale(double original, Entity attacker){
      LivingEntity livingEntity = (LivingEntity) (Object) this;
      if(attacker.getType().is(ArcanaRegistry.IGNORES_GREATER_INVISIBILITY)) return original;
      if(livingEntity.hasEffect(ArcanaRegistry.GREATER_INVISIBILITY_EFFECT)){
         return original * 0.01;
      }
      return original;
   }
   
   @ModifyReturnValue(method= "canAttack(Lnet/minecraft/world/entity/LivingEntity;)Z", at=@At("RETURN"))
   private boolean arcananovum_canTarget(boolean original, LivingEntity target){
      LivingEntity livingEntity = (LivingEntity) (Object) this;
      if(livingEntity.hasEffect(ArcanaRegistry.GREATER_BLINDNESS_EFFECT) && !livingEntity.getType().is(ArcanaRegistry.IGNORES_GREATER_BLINDNESS)){
         return false;
      }
      if(target.hasEffect(ArcanaRegistry.GREATER_INVISIBILITY_EFFECT) && !livingEntity.getType().is(ArcanaRegistry.IGNORES_GREATER_INVISIBILITY)){
         return false;
      }
      if(livingEntity.getType().is(ArcanaRegistry.NUL_CONSTRUCT_FRIENDS) && target instanceof NulConstructEntity){
         return false;
      }
      return original;
   }
   
   @Inject(method= "onEquipItem", at=@At("HEAD"), cancellable = true)
   private void arcananovum_sojournerEquipBug(EquipmentSlot slot, ItemStack oldStack, ItemStack newStack, CallbackInfo ci){
      String uuid1,uuid2;
      if(oldStack.is(newStack.getItem()) && ArcanaItemUtils.isArcane(oldStack) && (uuid1 = ArcanaItem.getUUID(newStack)) != null && (uuid2 = ArcanaItem.getUUID(newStack)) != null && uuid1.equals(uuid2)){
         ci.cancel();
      }
   }
}
