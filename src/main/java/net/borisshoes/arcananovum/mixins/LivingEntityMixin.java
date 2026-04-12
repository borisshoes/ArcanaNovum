package net.borisshoes.arcananovum.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.borisshoes.arcananovum.ArcanaConfig;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.blocks.GeomanticSteleBlockEntity;
import net.borisshoes.arcananovum.bosses.BossFights;
import net.borisshoes.arcananovum.callbacks.EntityKilledCallback;
import net.borisshoes.arcananovum.callbacks.ShieldTimerCallback;
import net.borisshoes.arcananovum.callbacks.VengeanceTotemTimerCallback;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.core.EnergyItem;
import net.borisshoes.arcananovum.damage.ArcanaDamageTypes;
import net.borisshoes.arcananovum.datastorage.BossFightData;
import net.borisshoes.arcananovum.effects.GreaterInvisibilityEffect;
import net.borisshoes.arcananovum.entities.NulConstructEntity;
import net.borisshoes.arcananovum.events.CleansingCharmEvent;
import net.borisshoes.arcananovum.items.*;
import net.borisshoes.arcananovum.items.charms.CindersCharm;
import net.borisshoes.arcananovum.items.charms.FelidaeCharm;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.*;
import net.borisshoes.borislib.BorisLib;
import net.borisshoes.borislib.conditions.ConditionInstance;
import net.borisshoes.borislib.conditions.Conditions;
import net.borisshoes.borislib.datastorage.DataAccess;
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
import net.minecraft.world.damagesource.CombatEntry;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DeathProtection;
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
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

import static net.borisshoes.arcananovum.ArcanaRegistry.arcanaId;
import static net.borisshoes.borislib.BorisLib.SERVER_TIMER_CALLBACKS;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
   
   @Shadow
   protected abstract void playBlockFallSound();
   
   @Shadow
   protected abstract void playHurtSound(DamageSource source);
   
   @Shadow
   public abstract void onEnterCombat();
   
   @Inject(method = "baseTick", at = @At("HEAD"))
   private void arcananovum$onTick(CallbackInfo ci){
      LivingEntity thisEntity = (LivingEntity) (Object) this;
      if(thisEntity.isDeadOrDying()) return;
      if(thisEntity.tickCount % 3 != 0) return;
      double stardustParticleRate = ArcanaNovum.CONFIG.getDouble(ArcanaConfig.STARDUST_PARTICLE_RATE);
      if(stardustParticleRate <= 0) return;
      EnhancedStatUtils.glowInfusedGear(thisEntity, stardustParticleRate);
   }
   
   @Inject(method = "die", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;gameEvent(Lnet/minecraft/core/Holder;)V"))
   private void arcananovum$onEntityKilledOther(DamageSource damageSource, CallbackInfo ci, @Local Entity attacker, @Local ServerLevel serverWorld){
      LivingEntity thisEntity = (LivingEntity) (Object) this;
      EntityKilledCallback.killedEntity(serverWorld, damageSource, attacker, thisEntity);
   }
   
   @Inject(method = "die", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getCombatTracker()Lnet/minecraft/world/damagesource/CombatTracker;"))
   private void arcananovum$witherOneHit(DamageSource source, CallbackInfo ci){
      if((LivingEntity) (Object) this instanceof WitherBoss wither && wither.isDeadOrDying()){
         List<CombatEntry> record = wither.getCombatTracker().entries;
         if(!record.isEmpty()){
            float actualDmg = record.getLast().damage();
            if(actualDmg >= wither.getMaxHealth() && source.getWeaponItem() != null && source.getWeaponItem().is(ArcanaRegistry.GRAVITON_MAUL.getItem()) && source.getEntity() instanceof ServerPlayer player){
               ArcanaAchievements.grant(player, ArcanaAchievements.BONE_SMASHER);
            }
         }
      }
   }
   
   @Inject(method = "addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z", at = @At("RETURN"))
   private void arcananovum$onStatusEffectAdd(MobEffectInstance effect, Entity source, CallbackInfoReturnable<Boolean> cir){
      if(cir.getReturnValueZ()){
         LivingEntity livingEntity = (LivingEntity) (Object) this;
         if(livingEntity instanceof ServerPlayer player && Event.getEventsOfType(CleansingCharmEvent.class).stream().anyMatch(event -> event.getPlayer().equals(player) && event.isEffect() && event.getEffect().equals(effect.getEffect()))){
            ArcanaAchievements.grant(player, ArcanaAchievements.CHRONIC_AILMENT);
         }
      }
   }
   
   @Inject(method = "die", at = @At("RETURN"))
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
      int whiteDwarf = ArcanaAugments.getAugmentOnItem(mainhand, ArcanaAugments.WHITE_DWARF_BLADES);
      if(whiteDwarf < 1) return original;
      int energy = EnergyItem.getEnergy(mainhand);
      float damagePerEnergy = ArcanaNovum.CONFIG.getFloat(ArcanaConfig.BINARY_BLADES_WHITE_DWARF_DMG_PER_ENERGY_BLOCK);
      float dmgReduction = Math.min(energy * damagePerEnergy, original);
      if(dmgReduction > 0){
         int delay = ArcanaNovum.CONFIG.getInt(ArcanaConfig.BINARY_BLADES_ENERGY_GRACE_PERIOD);
         ArcanaItem.putProperty(mainhand, BinaryBlades.LAST_HIT_TAG, delay);
         blades.addEnergy(mainhand, (int) (-dmgReduction / damagePerEnergy));
         return dmgReduction;
      }else{
         return 0;
      }
   }
   
   
   // Mixin for Shield of Fortitude giving absorption hearts
   @Inject(method = "applyItemBlocking", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/component/BlocksAttacks;hurtBlockingItem(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/InteractionHand;F)V"))
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
         shield.shieldBlock(entity, item, blocked);
      }
   }
   
   @Inject(method = "hurtServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getGameTime()J"))
   private void arcananovum$playerDamaged(ServerLevel world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir){
      LivingEntity entity = (LivingEntity) (Object) this;
      if(entity instanceof ServerPlayer player){
         Inventory inv = player.getInventory();
         for(int i = 0; i < inv.getContainerSize(); i++){
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
               int sturdyLvl = ArcanaAugments.getAugmentOnItem(chestItem, ArcanaAugments.STURDY_CONSTRUCTION);
               final double sturdyChance = ArcanaNovum.CONFIG.getDoubleList(ArcanaConfig.LEVITATION_HARNESS_DURABILITY_CHANCE).get(sturdyLvl);
               if(player.getRandom().nextDouble() >= sturdyChance){
                  int rebootLvl = ArcanaAugments.getAugmentOnItem(chestItem, ArcanaAugments.FAST_REBOOT);
                  int rebootDuration = ArcanaNovum.CONFIG.getIntList(ArcanaConfig.LEVITATION_HARNESS_REBOOT_SPEED_PER_LVL).get(rebootLvl);
                  harness.setStall(chestItem, rebootDuration);
                  player.setHealth(player.getHealth() / 2);
                  player.sendSystemMessage(Component.literal("Your Harness Stalls!").withStyle(ChatFormatting.YELLOW, ChatFormatting.ITALIC), true);
                  SoundUtils.playSound(player.level(), player.blockPosition(), SoundEvents.SHIELD_BREAK, SoundSource.PLAYERS, 1, 0.7f);
                  ArcanaEffectUtils.harnessStall(player.level(), player.position().add(0, 0.5, 0));
                  
                  boolean eProt = ArcanaAugments.getAugmentOnItem(chestItem, ArcanaAugments.EMERGENCY_PROTOCOL) >= 1;
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
   
   @Inject(method = "hurtServer", at = @At("HEAD"), cancellable = true, order = 892)
   private void arcananovum$cancelDamage(ServerLevel world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir){
      LivingEntity entity = (LivingEntity) (Object) this;
      
      if(source.is(DamageTypeTags.IS_FALL)){
         GeomanticSteleBlockEntity.SteleZone felidaeStele = GeomanticSteleBlockEntity.getZoneAtEntity(entity, (item) -> item.is(ArcanaRegistry.FELIDAE_CHARM.getItem()) && ArcanaAugments.getAugmentOnItem(item, ArcanaAugments.FELINE_GRACE) >= 4);
         if(felidaeStele != null){
            int xp = (int) Math.min(ArcanaNovum.CONFIG.getInt(ArcanaConfig.XP_FELIDAE_CHARM_FALL_CAP), ArcanaNovum.CONFIG.getInt(ArcanaConfig.XP_FELIDAE_CHARM_FALL) * (amount));
            if(entity instanceof ServerPlayer player){
               SoundUtils.playSongToPlayer(player, SoundUtils.getSound("entity.cat.purreow"), 1, 1);
               ArcanaNovum.data(player).addXP(xp); // Add xp
               if(amount > player.getHealth()) ArcanaAchievements.grant(player, ArcanaAchievements.LAND_ON_FEET);
               felidaeStele.getBlockEntity().giveXP(xp);
            }
            cir.setReturnValue(false);
         }
      }
      
      if(entity instanceof ServerPlayer player){
         if(source.is(DamageTypeTags.IS_FALL) && !ArcanaUtils.getArcanaItemsWithAug(player, ArcanaRegistry.FELIDAE_CHARM, ArcanaAugments.FELINE_GRACE, 4).isEmpty()){
            SoundUtils.playSongToPlayer(player, SoundUtils.getSound("entity.cat.purreow"), 1, 1);
            ArcanaNovum.data(player).addXP((int) Math.min(ArcanaNovum.CONFIG.getInt(ArcanaConfig.XP_FELIDAE_CHARM_FALL_CAP), ArcanaNovum.CONFIG.getInt(ArcanaConfig.XP_FELIDAE_CHARM_FALL) * (amount))); // Add xp
            if(amount > player.getHealth()) ArcanaAchievements.grant(player, ArcanaAchievements.LAND_ON_FEET);
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
            boolean juggernaut = ArcanaAugments.getAugmentOnItem(boots, ArcanaAugments.JUGGERNAUT) >= 1;
            int energy = EnergyItem.getEnergy(boots);
            if(juggernaut && energy >= 200){
               int slowDuration = ArcanaNovum.CONFIG.getInt(ArcanaConfig.SOJOURNERS_BOOTS_JUGGERNAUT_SLOWNESS_DURATION);
               int slowStr = ArcanaNovum.CONFIG.getInt(ArcanaConfig.SOJOURNERS_BOOTS_JUGGERNAUT_SLOWNESS);
               int vulnDuration = ArcanaNovum.CONFIG.getInt(ArcanaConfig.SOJOURNERS_BOOTS_JUGGERNAUT_VULNERABILITY_DURATION);
               float vulnStr = ArcanaNovum.CONFIG.getFloat(ArcanaConfig.SOJOURNERS_BOOTS_JUGGERNAUT_VULNERABILITY);
               
               MobEffectInstance slow = new MobEffectInstance(MobEffects.SLOWNESS, slowDuration, slowStr, false, false, true);
               entity.addEffect(slow);
               ConditionInstance vulnerability = new ConditionInstance(Conditions.VULNERABILITY, arcanaId(ArcanaRegistry.SOJOURNER_BOOTS.getId()), vulnDuration, vulnStr, true, true, false, AttributeModifier.Operation.ADD_VALUE, player.getUUID());
               Conditions.addCondition(entity.level().getServer(), entity, vulnerability);
               sojournerBoots.setEnergy(boots, 0);
               SoundUtils.playSound(player.level(), entity.blockPosition(), SoundEvents.IRON_GOLEM_HURT, SoundSource.PLAYERS, .5f, .8f);
            }
         }
         
         
         // Shield Bash Augment
         ItemStack shieldStack = null;
         if(ArcanaItemUtils.identifyItem(player.getItemBySlot(EquipmentSlot.OFFHAND)) instanceof ShieldOfFortitude){
            shieldStack = player.getItemBySlot(EquipmentSlot.OFFHAND);
         }else if(ArcanaItemUtils.identifyItem(player.getItemBySlot(EquipmentSlot.MAINHAND)) instanceof ShieldOfFortitude){
            shieldStack = player.getItemBySlot(EquipmentSlot.MAINHAND);
         }
         
         if(shieldStack != null && ArcanaAugments.getAugmentOnItem(shieldStack, ArcanaAugments.SHIELD_BASH) >= 1 && !player.getCooldowns().isOnCooldown(shieldStack) && source.isDirect()){
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
            ArcanaItem.putProperty(shieldStack, ShieldOfFortitude.ABSORPTION_TAG, Math.min(absAmt, shieldTotal));
            shieldTotal = Math.min(Math.min(absAmt, shieldTotal), 50);
            if(shieldTotal >= 20){
               int slowDur = ArcanaNovum.CONFIG.getInt(ArcanaConfig.SHIELD_OF_FORTITUDE_SHIELD_BASH_SLOWNESS_DURATION);
               int slowLvl = ArcanaNovum.CONFIG.getInt(ArcanaConfig.SHIELD_OF_FORTITUDE_SHIELD_BASH_SLOWNESS);
               double vulnDurPerAbs = ArcanaNovum.CONFIG.getDouble(ArcanaConfig.SHIELD_OF_FORTITUDE_SHIELD_BASH_VULNERABILITY_DURATION_PER_ABSORPTION);
               float vulnPerAbs = ArcanaNovum.CONFIG.getFloat(ArcanaConfig.SHIELD_OF_FORTITUDE_SHIELD_BASH_VULNERABILITY_PER_ABSORPTION);
               
               MobEffectInstance slow = new MobEffectInstance(MobEffects.SLOWNESS, slowDur, slowLvl, false, false, true);
               entity.addEffect(slow);
               ConditionInstance vulnerability = new ConditionInstance(Conditions.VULNERABILITY, arcanaId(ArcanaRegistry.SHIELD_OF_FORTITUDE.getId()), (int) (shieldTotal * vulnDurPerAbs), shieldTotal * vulnPerAbs, true, true, false, AttributeModifier.Operation.ADD_VALUE, player.getUUID());
               Conditions.addCondition(entity.level().getServer(), entity, vulnerability);
               
               toRemove.forEach(ShieldTimerCallback::onTimer);
               SERVER_TIMER_CALLBACKS.removeIf(toRemove::contains); // Remove all absorption callbacks
               int duration = 200 + 100 * ArcanaAugments.getAugmentOnItem(shieldStack, ArcanaAugments.SHIELD_OF_RESILIENCE);
               BorisLib.addTickTimerCallback(new ShieldTimerCallback(duration, shieldStack, player, 20)); // Put 10 hearts back
               MinecraftUtils.addMaxAbsorption(player, ShieldOfFortitude.EFFECT_ID, 20f);
               player.setAbsorptionAmount(player.getAbsorptionAmount() + 20f);
               player.getCooldowns().addCooldown(shieldStack, 100);
               SoundUtils.playSound(player.level(), entity.blockPosition(), SoundEvents.IRON_GOLEM_HURT, SoundSource.PLAYERS, .5f, .8f);
               ArcanaItem.putProperty(shieldStack, ShieldOfFortitude.ABSORPTION_TAG, 20);
            }
         }
      }
      
      ServerPlayer player = entity instanceof ServerPlayer p ? p : null;
      ArcanaInventory arcanaInventory = player != null ? ArcanaInventory.getPlayerItems(player) : null;
      
      if(source.is(DamageTypeTags.IS_FIRE)){
         if(arcanaInventory != null){
            List<ArcanaInventory.Entry> cremationCharms = arcanaInventory.getMatchingItems(invStack ->
                  ArcanaItemUtils.identifyItem(invStack) instanceof CindersCharm && ArcanaAugments.getAugmentOnItem(invStack, ArcanaAugments.CREMATION) > 0);
            
            for(ArcanaInventory.Entry entry : cremationCharms){
               if(newReturn <= 0) break;
               ItemStack item = entry.getStack();
               final double damagePerEnergy = ArcanaNovum.CONFIG.getDouble(ArcanaConfig.CINDERS_CHARM_CREMATION_DAMAGE_PER_ENERGY);
               float oldReturn = newReturn;
               int energy = EnergyItem.getEnergy(item);
               float dmgReduction = (float) Math.min(energy * damagePerEnergy, oldReturn);
               newReturn = oldReturn - dmgReduction;
               CindersCharm cinders = (CindersCharm) ArcanaItemUtils.identifyItem(item);
               cinders.addEnergy(item, (int) (-dmgReduction / damagePerEnergy));
               cinders.sendEnergyMessage(player, energy, cinders.getMaxEnergy(item), ChatFormatting.AQUA);
               entry.setModified();
            }
         }
         
         while(newReturn > 0){
            GeomanticSteleBlockEntity.SteleZone cremationStele = GeomanticSteleBlockEntity.getZoneAtEntity(entity, (item) -> item.is(ArcanaRegistry.CINDERS_CHARM.getItem()) && ArcanaAugments.getAugmentOnItem(item, ArcanaAugments.CREMATION) > 0 && EnergyItem.getEnergy(item) > 0);
            if(cremationStele != null && ArcanaItemUtils.identifyItem(cremationStele.getBlockEntity().getItem()) instanceof CindersCharm cinders){
               ItemStack item = cremationStele.getBlockEntity().getItem();
               final double damagePerEnergy = ArcanaNovum.CONFIG.getDouble(ArcanaConfig.CINDERS_CHARM_CREMATION_DAMAGE_PER_ENERGY);
               float oldReturn = newReturn;
               int energy = EnergyItem.getEnergy(item);
               float dmgReduction = (float) Math.min(energy * damagePerEnergy, oldReturn);
               newReturn = oldReturn - dmgReduction;
               cinders.addEnergy(item, (int) (-dmgReduction / damagePerEnergy));
            }else{
               break;
            }
         }
      }
      
      if(source.is(DamageTypeTags.IS_FALL)){
         int felidaeLvl = -1;
         Consumer<Float> felidaeXpCallback = null;
         if(arcanaInventory != null){
            List<ArcanaInventory.Entry> felidaeEntries = arcanaInventory.getMatchingItems(invStack -> ArcanaItemUtils.identifyItem(invStack) instanceof FelidaeCharm);
            for(ArcanaInventory.Entry entry : felidaeEntries){
               ItemStack item = entry.getStack();
               int graceLvl = ArcanaAugments.getAugmentOnItem(item, ArcanaAugments.FELINE_GRACE);
               if(graceLvl > felidaeLvl){
                  felidaeLvl = graceLvl;
                  felidaeXpCallback = (dmg) -> {
                     if(player != null)
                        ArcanaNovum.data(player).addXP((int) Math.min(ArcanaNovum.CONFIG.getInt(ArcanaConfig.XP_FELIDAE_CHARM_FALL_CAP), ArcanaNovum.CONFIG.getInt(ArcanaConfig.XP_FELIDAE_CHARM_FALL) * dmg)); // Add xp
                  };
               }
            }
         }
         
         List<GeomanticSteleBlockEntity.SteleZone> felidaeSteles = GeomanticSteleBlockEntity.getZonesAtEntity(entity, (item) -> item.is(ArcanaRegistry.FELIDAE_CHARM.getItem()));
         if(!felidaeSteles.isEmpty()){
            felidaeSteles.sort(Comparator.comparingInt((stele) -> -ArcanaAugments.getAugmentOnItem(stele.getBlockEntity().getItem(), ArcanaAugments.FELINE_GRACE)));
            GeomanticSteleBlockEntity.SteleZone zone = felidaeSteles.getFirst();
            int graceLvl = ArcanaAugments.getAugmentOnItem(zone.getBlockEntity().getItem(), ArcanaAugments.FELINE_GRACE);
            if(graceLvl > felidaeLvl){
               felidaeLvl = graceLvl;
               felidaeXpCallback = (dmg) -> {
                  int xp = (int) Math.min(ArcanaNovum.CONFIG.getInt(ArcanaConfig.XP_FELIDAE_CHARM_FALL_CAP), ArcanaNovum.CONFIG.getInt(ArcanaConfig.XP_FELIDAE_CHARM_FALL) * dmg);
                  if(player != null) ArcanaNovum.data(player).addXP(xp); // Add xp
                  zone.getBlockEntity().giveXP(xp);
               };
            }
         }
         if(felidaeLvl >= 0){
            float baseRed = ArcanaNovum.CONFIG.getFloat(ArcanaConfig.FELIDAE_CHARM_REDUCTION);
            float extraRed = ArcanaNovum.CONFIG.getFloatList(ArcanaConfig.FELIDAE_CHARM_GRACE_REDUCTION_PER_LEVEL).get(felidaeLvl);
            float dmgMod = 1.0f - (baseRed + extraRed);
            float oldReturn = newReturn;
            newReturn = newReturn * dmgMod < 2 ? 0 : newReturn * dmgMod; // Reduce the damage, if the remaining damage is less than a heart, remove all of it.
            if(player != null){
               SoundUtils.playSongToPlayer(player, SoundUtils.getSound("entity.cat.purreow"), 1, 1);
               if(oldReturn > player.getHealth() && newReturn < player.getHealth())
                  ArcanaAchievements.grant(player, ArcanaAchievements.LAND_ON_FEET);
            }
            felidaeXpCallback.accept(oldReturn - newReturn);
         }
      }
      
      if(arcanaInventory != null && newReturn > 0){
         List<ArcanaInventory.Entry> recallPearlEntries = arcanaInventory.getMatchingItems(invStack ->
               ArcanaItemUtils.identifyItem(invStack) instanceof PearlOfRecall);
         
         List<ArcanaInventory.Entry> planeshifterEntries = arcanaInventory.getMatchingItems(invStack ->
               ArcanaItemUtils.identifyItem(invStack) instanceof Planeshifter);
         
         for(ArcanaInventory.Entry entry : recallPearlEntries){
            ItemStack item = entry.getStack();
            int defenseLvl = ArcanaAugments.getAugmentOnItem(item, ArcanaAugments.PHASE_DEFENSE);
            double defenseChance = ArcanaNovum.CONFIG.getDoubleList(ArcanaConfig.PEARL_OF_RECALL_PHASE_DEFENSE_CHANCE).get(defenseLvl);
            
            if(ArcanaItem.getIntProperty(item, PearlOfRecall.HEAT_TAG) > 0){
               if(entity.getRandom().nextFloat() >= defenseChance){
                  player.sendSystemMessage(Component.literal("Your Recall Has Been Disrupted!").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC), true);
                  ArcanaItem.putProperty(item, PearlOfRecall.HEAT_TAG, -1);
                  entry.setModified();
               }else{
                  newReturn = 0;
               }
            }
         }
         
         for(ArcanaInventory.Entry entry : planeshifterEntries){
            ItemStack item = entry.getStack();
            if(ArcanaItem.getIntProperty(item, Planeshifter.HEAT_TAG) > 0){
               player.sendSystemMessage(Component.literal("Your Plane-Shift Has Been Disrupted!").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC), true);
               ArcanaItem.putProperty(item, Planeshifter.HEAT_TAG, -1);
               entry.setModified();
            }
         }
      }
      
      ItemStack chestItem = entity.getItemBySlot(EquipmentSlot.CHEST);
      if(ArcanaItemUtils.identifyItem(chestItem) instanceof WingsOfEnderia wings){
         boolean canReduce = source.is(DamageTypeTags.IS_FALL) || source.is(DamageTypes.FLY_INTO_WALL) || ArcanaAugments.getAugmentOnItem(chestItem, ArcanaAugments.SCALES_OF_THE_CHAMPION) >= 2;
         if(canReduce){
            int energy = EnergyItem.getEnergy(chestItem);
            float energyPerDmg = ArcanaNovum.CONFIG.getFloat(ArcanaConfig.WINGS_OF_ENDERIA_ENERGY_PER_DMG);
            float maxDmgReduction = newReturn * .5f;
            float dmgReduction = Math.min(energy / energyPerDmg, maxDmgReduction);
            if(player != null){
               if(dmgReduction >= 4){
                  if(source.is(DamageTypeTags.IS_FALL) || source.is(DamageTypes.FLY_INTO_WALL)){
                     player.sendSystemMessage(Component.literal("Your Armored Wings cushion your fall!").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC), true);
                  }
                  SoundUtils.playSongToPlayer(player, SoundEvents.ENDER_DRAGON_FLAP, 1, 1.3f);
                  BorisLib.addTickTimerCallback(new GenericTimer(50, () -> player.sendSystemMessage(Component.literal("Wing Energy Remaining: " + EnergyItem.getEnergy(chestItem)).withStyle(ChatFormatting.DARK_PURPLE), true)));
               }
               ArcanaNovum.data(player).addXP((int) Math.min(ArcanaNovum.CONFIG.getInt(ArcanaConfig.XP_WINGS_OF_ENDERIA_CUSHION_CAP), ArcanaNovum.CONFIG.getInt(ArcanaConfig.XP_WINGS_OF_ENDERIA_CUSHION) * dmgReduction)); // Add xp
               if(source.is(DamageTypes.FLY_INTO_WALL) && newReturn > player.getHealth() && (newReturn - dmgReduction) < player.getHealth())
                  ArcanaAchievements.grant(player, ArcanaAchievements.SEE_GLASS);
            }
            wings.addEnergy(chestItem, (int) -dmgReduction * 100);
            newReturn -= dmgReduction;
         }
         
         // Wing Buffet ability
         int buffetLvl = ArcanaAugments.getAugmentOnItem(chestItem, ArcanaAugments.WING_BUFFET);
         if(player != null && buffetLvl > 0){
            double buffetRange = ArcanaNovum.CONFIG.getDouble(ArcanaConfig.WINGS_OF_ENDERIA_BUFFET_RANGE);
            double buffetPower = ArcanaNovum.CONFIG.getDouble(ArcanaConfig.WINGS_OF_ENDERIA_BUFFET_POWER);
            ServerLevel world = player.level();
            Vec3 pos = player.position().add(0, player.getBbHeight() / 2, 0);
            AABB rangeBox = new AABB(pos.x + buffetRange * 1.5, pos.y + buffetRange * 1.5, pos.z + buffetRange * 1.5, pos.x - buffetRange * 1.5, pos.y - buffetRange * 1.5, pos.z - buffetRange * 1.5);
            List<Entity> entities = world.getEntities(entity, rangeBox, e -> !e.isSpectator() && e.distanceToSqr(pos) < 1.25 * buffetRange * buffetRange && (e instanceof Mob));
            boolean triggered = false;
            for(Entity entity1 : entities){
               if(EnergyItem.getEnergy(chestItem) < 50) break;
               Vec3 diff = entity1.position().subtract(pos);
               double multiplier = buffetPower * Mth.clamp(buffetRange * .75 - diff.length() * .5, .1, 3);
               Vec3 motion = diff.multiply(1, 0, 1).add(0, 1, 0).normalize().scale(multiplier);
               if(entity1 instanceof ServerPlayer otherPlayer){
                  if(buffetLvl >= 2){
                     otherPlayer.setDeltaMovement(motion.x, motion.y, motion.z);
                     otherPlayer.connection.send(new ClientboundSetEntityMotionPacket(otherPlayer));
                     wings.addEnergy(chestItem, -100);
                     triggered = true;
                  }
               }else{
                  entity1.setDeltaMovement(motion.x, motion.y, motion.z);
                  wings.addEnergy(chestItem, -50);
                  triggered = true;
               }
            }
            if(triggered) SoundUtils.playSongToPlayer(player, SoundEvents.ENDER_DRAGON_FLAP, 1, .7f);
         }
      }
      
      
      // Enderia Boss health scale
      Tuple<BossFights, CompoundTag> bossFight = DataAccess.getWorld(entity.level().dimension(), BossFightData.KEY).getBossFight();
      int numPlayers = 0;
      if(bossFight != null){
         numPlayers = bossFight.getB().getIntOr("numPlayers", 0);
      }
      if(numPlayers != 0){
         float scale = Math.max(2f / numPlayers, 0.1f);
         if(entity instanceof EnderDragon){
            newReturn *= scale; //Effective Health Scale to bypass 1024 hp cap
            if(source.is(DamageTypeTags.BYPASSES_ARMOR) || source.is(DamageTypeTags.IS_EXPLOSION))
               newReturn *= 0.25f; // Reduce damage from magic and explosive sources
         }
      }
      
      // Death Ward
      if(entity.hasEffect(ArcanaRegistry.DEATH_WARD_EFFECT) && (!source.is(DamageTypeTags.BYPASSES_RESISTANCE) || source.is(ArcanaDamageTypes.CONCENTRATION))){
         MobEffectInstance effect = entity.getEffect(ArcanaRegistry.DEATH_WARD_EFFECT);
         if(entity.getHealth() < newReturn){
            float damageWarded = newReturn;
            newReturn = entity.getHealth() - 0.01f;
            damageWarded -= newReturn;
            
            if(player != null && ArcanaAchievements.isTimerActive(player, ArcanaAchievements.TOO_ANGRY_TO_DIE)){
               ArcanaAchievements.progress(player, ArcanaAchievements.TOO_ANGRY_TO_DIE, Math.round(damageWarded));
            }
         }
      }
      
      if(arcanaInventory != null) arcanaInventory.close();
      return newReturn;
   }
   
   @ModifyExpressionValue(method = "checkTotemDeathProtection", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/damagesource/DamageSource;is(Lnet/minecraft/tags/TagKey;)Z"))
   public boolean arcananovum$totemDamageCheck(boolean original, DamageSource source){
      if(!original) return false;
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
      List<NulConstructEntity> constructs = livingEntity.level().getEntitiesOfClass(NulConstructEntity.class, livingEntity.getBoundingBox().inflate(NulConstructEntity.FIGHT_RANGE * 2), construct -> construct.getSummoner().getUUID().equals(livingEntity.getUUID()));
      
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
         ArcanaInventory inventory = ArcanaInventory.getPlayerItems(player);
         List<ArcanaInventory.Entry> totems = inventory.getMatchingEntries((entry) -> {
            if(!entry.getContainerItem().is(ArcanaRegistry.ARCANISTS_BELT.getItem())) return false;
            ItemStack stack = entry.getStack();
            if(cooldowns.isOnCooldown(stack)) return false;
            return stack.has(DataComponents.DEATH_PROTECTION);
         });
         if(!totems.isEmpty()){
            ArcanaInventory.Entry entry = totems.getFirst();
            ItemStack stack = entry.getStack();
            deathProtectionComponent = stack.get(DataComponents.DEATH_PROTECTION);
            itemStack = stack.copy();
            stack.shrink(1);
            entry.setModified();
            inventory.close();
         }
      }
      
      List<NulConstructEntity> constructs = livingEntity.level().getEntitiesOfClass(NulConstructEntity.class, livingEntity.getBoundingBox().inflate(NulConstructEntity.FIGHT_RANGE * 2), construct -> construct.getSummoner() != null && construct.getSummoner().getUUID().equals(livingEntity.getUUID()));
      if(itemStack != null){
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
      if(ArcanaItemUtils.identifyItem(headStack) instanceof NulMemento memento && memento.protectFromDeath(headStack, livingEntity, source, !constructs.isEmpty())){
         constructs.forEach(construct -> construct.triggerAdaptation(NulConstructEntity.ConstructAdaptations.USED_MEMENTO));
         cir.setReturnValue(true);
         return;
      }
   }
   
   @Inject(method = "onEffectsRemoved", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/effect/MobEffect;removeAttributeModifiers(Lnet/minecraft/world/entity/ai/attributes/AttributeMap;)V"))
   private void arcananovum$effectRemoved(Collection<MobEffectInstance> effects, CallbackInfo ci){
      LivingEntity livingEntity = (LivingEntity) (Object) this;
      for(MobEffectInstance effect : effects){
         if(effect.getEffect() == ArcanaRegistry.GREATER_INVISIBILITY_EFFECT && livingEntity.level().getServer() != null){
            GreaterInvisibilityEffect.removeInvis(livingEntity.level().getServer(), livingEntity);
         }
      }
   }
   
   @Inject(method = "updateInvisibilityStatus", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;setInvisible(Z)V", ordinal = 1, shift = At.Shift.AFTER))
   private void arcananovum$greaterInvisibilityUpdate(CallbackInfo ci){
      LivingEntity livingEntity = (LivingEntity) (Object) this;
      livingEntity.setInvisible(livingEntity.isInvisible() || livingEntity.hasEffect(ArcanaRegistry.GREATER_INVISIBILITY_EFFECT));
   }
   
   @ModifyReturnValue(method = "getVisibilityPercent", at = @At("RETURN"))
   private double arcananovum$greaterInvisibilityAttackRangeScale(double original, Entity attacker){
      LivingEntity livingEntity = (LivingEntity) (Object) this;
      if(attacker.is(ArcanaRegistry.IGNORES_GREATER_INVISIBILITY)) return original;
      if(livingEntity.hasEffect(ArcanaRegistry.GREATER_INVISIBILITY_EFFECT)){
         return original * 0.01;
      }
      return original;
   }
   
   @ModifyReturnValue(method = "canAttack(Lnet/minecraft/world/entity/LivingEntity;)Z", at = @At("RETURN"))
   private boolean arcananovum$canTarget(boolean original, LivingEntity target){
      LivingEntity livingEntity = (LivingEntity) (Object) this;
      if(target.hasEffect(ArcanaRegistry.GREATER_INVISIBILITY_EFFECT) && !livingEntity.is(ArcanaRegistry.IGNORES_GREATER_INVISIBILITY)){
         return false;
      }
      if(livingEntity.is(ArcanaRegistry.NUL_CONSTRUCT_FRIENDS) && target instanceof NulConstructEntity){
         return false;
      }
      if(target instanceof ServerPlayer player && livingEntity instanceof AbstractPiglin){
         if(ArcanaItemUtils.hasItemInInventory(player, ArcanaRegistry.NEGOTIATION_CHARM.getItem())) return false;
      }
      return original;
   }
   
   @Inject(method = "onEquipItem", at = @At("HEAD"), cancellable = true)
   private void arcananovum$sojournerEquipBug(EquipmentSlot slot, ItemStack oldStack, ItemStack newStack, CallbackInfo ci){
      String uuid1, uuid2;
      if(oldStack.is(newStack.getItem()) && ArcanaItemUtils.isArcane(oldStack) && (uuid1 = ArcanaItem.getUUID(newStack)) != null && (uuid2 = ArcanaItem.getUUID(newStack)) != null && uuid1.equals(uuid2)){
         ci.cancel();
      }
   }
}
