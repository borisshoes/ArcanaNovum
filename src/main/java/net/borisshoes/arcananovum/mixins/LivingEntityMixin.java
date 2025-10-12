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
import net.borisshoes.arcananovum.utils.ArcanaEffectUtils;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.arcananovum.utils.ArcanaUtils;
import net.borisshoes.borislib.BorisLib;
import net.borisshoes.borislib.events.Event;
import net.borisshoes.borislib.timers.GenericTimer;
import net.borisshoes.borislib.timers.TickTimerCallback;
import net.borisshoes.borislib.utils.MinecraftUtils;
import net.borisshoes.borislib.utils.SoundUtils;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.component.type.DeathProtectionComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.damage.DamageRecord;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.event.GameEvent;
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
   
   @Shadow public abstract void enterCombat();
   
   @Inject(method = "onDeath", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;emitGameEvent(Lnet/minecraft/registry/entry/RegistryEntry;)V"))
   private void arcananovum$onEntityKilledOther(DamageSource damageSource, CallbackInfo ci, @Local Entity attacker, @Local ServerWorld serverWorld){
      LivingEntity thisEntity = (LivingEntity) (Object) this;
      EntityKilledCallback.killedEntity(serverWorld,damageSource,attacker,thisEntity);
   }
   
   @Inject(method="onDeath", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getDamageTracker()Lnet/minecraft/entity/damage/DamageTracker;"))
   private void arcananovum$witherOneHit(DamageSource source, CallbackInfo ci){
      if((LivingEntity)(Object)this instanceof WitherEntity wither && wither.isDead()){
         List<DamageRecord> record = wither.getDamageTracker().recentDamage;
         if(!record.isEmpty()){
            float actualDmg = record.getLast().damage();
            if(actualDmg >= wither.getMaxHealth() && source.getWeaponStack() != null && source.getWeaponStack().isOf(ArcanaRegistry.GRAVITON_MAUL.getItem()) && source.getAttacker() instanceof ServerPlayerEntity player){
               ArcanaAchievements.grant(player,ArcanaAchievements.BONE_SMASHER);
            }
         }
      }
   }
   
   @Inject(method = "addStatusEffect(Lnet/minecraft/entity/effect/StatusEffectInstance;Lnet/minecraft/entity/Entity;)Z", at = @At("RETURN"))
   private void arcananovum$onStatusEffectAdd(StatusEffectInstance effect, Entity source, CallbackInfoReturnable<Boolean> cir){
      if(cir.getReturnValueZ()){
         
         if(source instanceof ServerPlayerEntity player && Event.getEventsOfType(CleansingCharmEvent.class).stream().anyMatch(event -> event.getPlayer().equals(player) && event.getEffect().equals(effect.getEffectType()))){
            ArcanaAchievements.grant(player,ArcanaAchievements.CHRONIC_AILMENT);
         }
      }
   }
   
   @Inject(method="onDeath", at = @At("RETURN"))
   private void arcananovum$onDeath(DamageSource damageSource, CallbackInfo ci){
      LivingEntity livingEntity = (LivingEntity) (Object) this;
      for(TickTimerCallback t : SERVER_TIMER_CALLBACKS){
         if(t instanceof VengeanceTotemTimerCallback vt && vt.getAttacker() != null && vt.getAttacker().getUuidAsString().equals(livingEntity.getUuidAsString())){
            vt.setAvenged();
         }
      }
   }
   
   @ModifyExpressionValue(method = "getDamageBlockedAmount", at = @At(value = "INVOKE", target = "Lnet/minecraft/component/type/BlocksAttacksComponent;getDamageReductionAmount(Lnet/minecraft/entity/damage/DamageSource;FD)F"))
   private float arcananovum$whiteDwarfBlock(float original, ServerWorld world, DamageSource source, float damage, @Local ItemStack blockStack){
      LivingEntity entity = (LivingEntity) (Object) this;
      ItemStack mainhand = entity.getMainHandStack();
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
   @Inject(method="getDamageBlockedAmount",at=@At(value = "INVOKE", target = "Lnet/minecraft/component/type/BlocksAttacksComponent;onShieldHit(Lnet/minecraft/world/World;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/util/Hand;F)V"))
   private void arcananovum$shieldAbsorb(ServerWorld world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir, @Local ItemStack shieldStack, @Local(ordinal = 0, argsOnly = true) float blocked){
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
   
   @Inject(method="damage",at=@At(value = "INVOKE", target = "Lnet/minecraft/world/World;getTime()J"))
   private void arcananovum$playerDamaged(ServerWorld world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir){
      LivingEntity entity = (LivingEntity) (Object) this;
      if(entity instanceof ServerPlayerEntity player){
         PlayerInventory inv = player.getInventory();
         for(int i=0; i<inv.size();i++){
            ItemStack item = inv.getStack(i);
            if(item.isEmpty()){
               continue;
            }
      
            boolean isArcane = ArcanaItemUtils.isArcane(item);
            if(!isArcane)
               continue; // Item not arcane, skip
      
            
         }
   
         // Stall Levitation Harness
         ItemStack chestItem = entity.getEquippedStack(EquipmentSlot.CHEST);
         if(ArcanaItemUtils.isArcane(chestItem) && player.getAbilities().flying){
            if(ArcanaItemUtils.identifyItem(chestItem) instanceof LevitationHarness harness){
               int sturdyLvl = Math.max(0,ArcanaAugments.getAugmentOnItem(chestItem,ArcanaAugments.STURDY_CONSTRUCTION.id));
               final double[] sturdyChance = {0,.15,.35,.5};
               if(Math.random() >= sturdyChance[sturdyLvl]){
                  int rebootLvl = Math.max(0,ArcanaAugments.getAugmentOnItem(chestItem,ArcanaAugments.FAST_REBOOT.id));
                  harness.setStall(chestItem,15-2*rebootLvl);
                  player.setHealth(player.getHealth()/2);
                  player.sendMessage(Text.literal("Your Harness Stalls!").formatted(Formatting.YELLOW,Formatting.ITALIC),true);
                  SoundUtils.playSound(player.getWorld(),player.getBlockPos(),SoundEvents.ITEM_SHIELD_BREAK, SoundCategory.PLAYERS,1, 0.7f);
                  ArcanaEffectUtils.harnessStall(player.getWorld(),player.getPos().add(0,0.5,0));
                  
                  boolean eProt = Math.max(0,ArcanaAugments.getAugmentOnItem(chestItem,ArcanaAugments.EMERGENCY_PROTOCOL.id)) >= 1;
                  if(eProt){
                     StatusEffectInstance levit = new StatusEffectInstance(StatusEffects.LEVITATION, 100, 0, false, false, true);
                     player.addStatusEffect(levit);
                  }
               }
            }
         }
         
         if(source.isOf(DamageTypes.STARVE)){
            ArcanaNovum.data(player).setResearchTask(ResearchTasks.HUNGER_DAMAGE, true);
         }
         
         if(source.isOf(ArcanaDamageTypes.CONCENTRATION)){
            ArcanaNovum.data(player).setResearchTask(ResearchTasks.CONCENTRATION_DAMAGE, true);
         }
         
         if(source.isOf(DamageTypes.DROWN)){
            ArcanaNovum.data(player).setResearchTask(ResearchTasks.DROWNING_DAMAGE, true);
         }
      }
   }
   
   // Mixin for shadow stalker's glaive doing damage
   @Inject(method="damage",at=@At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;applyDamage(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/damage/DamageSource;F)V"))
   private void arcananovum$damageDealt(ServerWorld world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir){
      LivingEntity entity = (LivingEntity) (Object) this;
      Entity attacker = source.getAttacker();
      if(attacker instanceof ServerPlayerEntity player){
         ItemStack weapon = player.getEquippedStack(EquipmentSlot.MAINHAND);
   
         if(ArcanaItemUtils.identifyItem(weapon) instanceof ShadowStalkersGlaive glaive){
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
   
   @Inject(method = "damage", at = @At("HEAD"), cancellable = true, order = 892)
   private void arcananovum$cancelDamage(ServerWorld world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir){
      LivingEntity entity = (LivingEntity) (Object) this;
      
      if(entity instanceof ServerPlayerEntity player){
         if(source.isIn(DamageTypeTags.IS_FALL) && !ArcanaUtils.getArcanaItemsWithAug(player,ArcanaRegistry.FELIDAE_CHARM, ArcanaAugments.FELINE_GRACE,4).isEmpty()){
            SoundUtils.playSongToPlayer(player, SoundEvents.ENTITY_CAT_PURREOW, 1,1);
            ArcanaNovum.data(player).addXP((int) Math.min(ArcanaConfig.getInt(ArcanaRegistry.FELIDAE_CHARM_FALL_CAP),ArcanaConfig.getInt(ArcanaRegistry.FELIDAE_CHARM_FALL)*(amount))); // Add xp
            if(amount > player.getHealth()) ArcanaAchievements.grant(player,ArcanaAchievements.LAND_ON_FEET.id);
            cir.setReturnValue(false);
         }
      }
   }
   
   // Mixin for damage modifiers
   @ModifyReturnValue(method = "modifyAppliedDamage", at = @At("RETURN"))
   private float arcananovum$modifyDamage(float original, DamageSource source, float amount){
      float newReturn = original;
      LivingEntity entity = (LivingEntity) (Object) this;
      Entity attacker = source.getAttacker();
      
      if(attacker instanceof ServerPlayerEntity player){
         // Juggernaut Augment
         ItemStack boots = player.getEquippedStack(EquipmentSlot.FEET);
         if(ArcanaItemUtils.identifyItem(boots) instanceof SojournerBoots sojournerBoots && source.isDirect()){
            boolean juggernaut = ArcanaAugments.getAugmentOnItem(boots,ArcanaAugments.JUGGERNAUT.id) >= 1;
            int energy = sojournerBoots.getEnergy(boots);
            if(juggernaut && energy >= 200){
               StatusEffectInstance slow = new StatusEffectInstance(StatusEffects.SLOWNESS, 60, 4, false, false, true);
               StatusEffectInstance dmgAmp = new StatusEffectInstance(ArcanaRegistry.DAMAGE_AMP_EFFECT, 100, 1, false, true, false);
               entity.addStatusEffect(slow);
               entity.addStatusEffect(dmgAmp);
               sojournerBoots.setEnergy(boots,0);
               SoundUtils.playSound(player.getWorld(),entity.getBlockPos(),SoundEvents.ENTITY_IRON_GOLEM_HURT, SoundCategory.PLAYERS, .5f, .8f);
            }
         }
         
         
         // Shield Bash Augment
         ItemStack shieldStack = null;
         if(ArcanaItemUtils.identifyItem(player.getEquippedStack(EquipmentSlot.OFFHAND)) instanceof ShieldOfFortitude){
            shieldStack = player.getEquippedStack(EquipmentSlot.OFFHAND);
         }else if(ArcanaItemUtils.identifyItem(player.getEquippedStack(EquipmentSlot.MAINHAND)) instanceof ShieldOfFortitude){
            shieldStack = player.getEquippedStack(EquipmentSlot.MAINHAND);
         }
         
         if(shieldStack != null && ArcanaAugments.getAugmentOnItem(shieldStack,ArcanaAugments.SHIELD_BASH.id) >= 1 && !player.getItemCooldownManager().isCoolingDown(shieldStack) && source.isDirect()){
            ArrayList<ShieldTimerCallback> toRemove = new ArrayList<>();
            float shieldTotal = 0;
            float absAmt = player.getAbsorptionAmount();
            for(int i = 0; i < SERVER_TIMER_CALLBACKS.size(); i++){
               TickTimerCallback t = SERVER_TIMER_CALLBACKS.get(i);
               if(t instanceof ShieldTimerCallback st && st.getPlayer().getUuidAsString().equals(player.getUuidAsString())){
                  shieldTotal += st.getHearts();
                  toRemove.add(st);
               }
            }
            shieldTotal = Math.min(Math.min(absAmt,shieldTotal),50);
            if(shieldTotal >= 20){
               StatusEffectInstance slow = new StatusEffectInstance(StatusEffects.SLOWNESS, 60, 4, false, false, true);
               StatusEffectInstance dmgAmp = new StatusEffectInstance(ArcanaRegistry.DAMAGE_AMP_EFFECT, (int) (shieldTotal*5), (int) (shieldTotal/20), false, true, false);
               entity.addStatusEffect(slow);
               entity.addStatusEffect(dmgAmp);
               toRemove.forEach(ShieldTimerCallback::onTimer);
               SERVER_TIMER_CALLBACKS.removeIf(toRemove::contains); // Remove all absorption callbacks
               int duration = 200 + 100*Math.max(0,ArcanaAugments.getAugmentOnItem(shieldStack,ArcanaAugments.SHIELD_OF_RESILIENCE.id));
               BorisLib.addTickTimerCallback(new ShieldTimerCallback(duration,shieldStack,player,20)); // Put 10 hearts back
               MinecraftUtils.addMaxAbsorption(player, ShieldOfFortitude.EFFECT_ID,20f);
               player.setAbsorptionAmount(player.getAbsorptionAmount() + 20f);
               player.getItemCooldownManager().set(shieldStack,100);
               SoundUtils.playSound(player.getWorld(),entity.getBlockPos(),SoundEvents.ENTITY_IRON_GOLEM_HURT, SoundCategory.PLAYERS, .5f, .8f);
            }
         }
      }
      
      // Damage Amp
      if(entity.hasStatusEffect(ArcanaRegistry.DAMAGE_AMP_EFFECT)){
         StatusEffectInstance effect = entity.getStatusEffect(ArcanaRegistry.DAMAGE_AMP_EFFECT);
         DamageAmpEffect.tryTrackDamage(effect.getAmplifier(),newReturn,entity);
         newReturn = DamageAmpEffect.buffDamage(newReturn,effect.getAmplifier());
      }
   
      if(entity instanceof ServerPlayerEntity player){
         List<Pair<List<ItemStack>, ItemStack>> allItems = ArcanaUtils.getAllItems(player);
         boolean procdFelidae = false;
         
         for(int i = 0; i < allItems.size(); i++){
            List<ItemStack> itemList = allItems.get(i).getLeft();
            ItemStack carrier = allItems.get(i).getRight();
            SimpleInventory sinv = new SimpleInventory(itemList.size());
            
            for(int j = 0; j < itemList.size(); j++){
               ItemStack item = itemList.get(j);
               
               boolean isArcane = ArcanaItemUtils.isArcane(item);
               if(!isArcane){
                  sinv.setStack(j, item);
                  continue; // Item not arcane, skip
               }
               ArcanaItem arcanaItem = ArcanaItemUtils.identifyItem(item);
               
               if((arcanaItem instanceof FelidaeCharm) && source.isIn(DamageTypeTags.IS_FALL) && !procdFelidae){ // Felidae Charm
                  int graceLvl = Math.max(0, ArcanaAugments.getAugmentOnItem(item, ArcanaAugments.FELINE_GRACE.id));
                  float dmgMod = (float) (0.5 - 0.125 * graceLvl);
                  SoundUtils.playSongToPlayer(player, SoundEvents.ENTITY_CAT_PURREOW, 1, 1);
                  float oldReturn = newReturn;
                  newReturn = newReturn * dmgMod < 2 ? 0 : newReturn * dmgMod; // Reduce the damage, if the remaining damage is less than a heart, remove all of it.
                  ArcanaNovum.data(player).addXP((int) Math.min(ArcanaConfig.getInt(ArcanaRegistry.FELIDAE_CHARM_FALL_CAP), ArcanaConfig.getInt(ArcanaRegistry.FELIDAE_CHARM_FALL) * (oldReturn - newReturn))); // Add xp
                  if(oldReturn > player.getHealth() && newReturn < player.getHealth())
                     ArcanaAchievements.grant(player, ArcanaAchievements.LAND_ON_FEET.id);
                  procdFelidae = true; // Make it so multiple charms don't stack
                  
               }else if(arcanaItem instanceof PearlOfRecall pearl){ // Cancel all Pearls of Recall
                  int defenseLvl = Math.max(0, ArcanaAugments.getAugmentOnItem(item, ArcanaAugments.PHASE_DEFENSE.id));
                  final double[] defenseChance = {0, .15, .35, .5};
                  
                  
                  if(ArcanaItem.getIntProperty(item, PearlOfRecall.HEAT_TAG) > 0){
                     if(Math.random() >= defenseChance[defenseLvl]){
                        player.sendMessage(Text.literal("Your Recall Has Been Disrupted!").formatted(Formatting.RED, Formatting.ITALIC), true);
                        ArcanaItem.putProperty(item, PearlOfRecall.HEAT_TAG, -1);
                     }else{
                        newReturn = 0;
                     }
                  }
               }else if(arcanaItem instanceof Planeshifter){ // Cancel all Planeshifters
                  if(ArcanaItem.getIntProperty(item, Planeshifter.HEAT_TAG) > 0){
                     player.sendMessage(Text.literal("Your Plane-Shift Has Been Disrupted!").formatted(Formatting.RED, Formatting.ITALIC), true);
                     ArcanaItem.putProperty(item, Planeshifter.HEAT_TAG, -1);
                  }
               }else if(arcanaItem instanceof CindersCharm cinders && source.isIn(DamageTypeTags.IS_FIRE)){ // Cinders Charm Cremation
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
                     player.sendMessage(Text.literal(message.toString()).formatted(Formatting.AQUA), true);
                  }
               }
               
               sinv.setStack(j, item);
            }
            
            if(ArcanaItemUtils.identifyItem(carrier) instanceof ArcanistsBelt belt){
               belt.buildItemLore(carrier, BorisLib.SERVER);
            }
         }
      }
      
      ItemStack chestItem = entity.getEquippedStack(EquipmentSlot.CHEST);
      if(ArcanaItemUtils.identifyItem(chestItem) instanceof WingsOfEnderia wings){
         boolean canReduce = source.isIn(DamageTypeTags.IS_FALL) || source.isOf(DamageTypes.FLY_INTO_WALL) || ArcanaAugments.getAugmentOnItem(chestItem,ArcanaAugments.SCALES_OF_THE_CHAMPION.id) >= 2;
         if(canReduce){
            int energy = wings.getEnergy(chestItem);
            float maxDmgReduction = newReturn * .5f;
            float dmgReduction = (float) Math.min(energy / 100.0, maxDmgReduction);
            if(entity instanceof ServerPlayerEntity player){
               if(dmgReduction >= 4){
                  if(source.isIn(DamageTypeTags.IS_FALL) || source.isOf(DamageTypes.FLY_INTO_WALL)){
                     player.sendMessage(Text.literal("Your Armored Wings cushion your fall!").formatted(Formatting.DARK_PURPLE, Formatting.ITALIC), true);
                  }
                  SoundUtils.playSongToPlayer(player, SoundEvents.ENTITY_ENDER_DRAGON_FLAP, 1, 1.3f);
                  BorisLib.addTickTimerCallback(new GenericTimer(50, () -> player.sendMessage(Text.literal("Wing Energy Remaining: " + wings.getEnergy(chestItem)).formatted(Formatting.DARK_PURPLE), true)));
               }
               ArcanaNovum.data(player).addXP((int) Math.min(ArcanaConfig.getInt(ArcanaRegistry.WINGS_OF_ENDERIA_CUSHION_CAP),ArcanaConfig.getInt(ArcanaRegistry.WINGS_OF_ENDERIA_CUSHION)*dmgReduction)); // Add xp
               if(source.isOf(DamageTypes.FLY_INTO_WALL) && newReturn > player.getHealth() && (newReturn - dmgReduction) < player.getHealth())
                  ArcanaAchievements.grant(player, ArcanaAchievements.SEE_GLASS.id);
            }
            wings.addEnergy(chestItem, (int) -dmgReduction * 100);
            newReturn -= dmgReduction;
         }
         
         // Wing Buffet ability
         int buffetLvl = ArcanaAugments.getAugmentOnItem(chestItem,ArcanaAugments.WING_BUFFET);
         if(entity instanceof ServerPlayerEntity player && buffetLvl > 0){
            ServerWorld world = player.getWorld();
            Vec3d pos = player.getPos().add(0,player.getHeight()/2,0);
            Box rangeBox = new Box(pos.x+8,pos.y+8,pos.z+8,pos.x-8,pos.y-8,pos.z-8);
            int range = 3;
            List<Entity> entities = world.getOtherEntities(entity,rangeBox, e -> !e.isSpectator() && e.squaredDistanceTo(pos) < 1.5*range*range && (e instanceof MobEntity));
            boolean triggered = false;
            for(Entity entity1 : entities){
               if(wings.getEnergy(chestItem) < 50) break;
               Vec3d diff = entity1.getPos().subtract(pos);
               double multiplier = MathHelper.clamp(range*.75-diff.length()*.5,.1,3);
               Vec3d motion = diff.multiply(1,0,1).add(0,1,0).normalize().multiply(multiplier);
               if(entity1 instanceof ServerPlayerEntity otherPlayer){
                  if(buffetLvl >= 2){
                     otherPlayer.setVelocity(motion.x,motion.y,motion.z);
                     otherPlayer.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(otherPlayer));
                     wings.addEnergy(chestItem,-100);
                     triggered = true;
                  }
               }else{
                  entity1.setVelocity(motion.x,motion.y,motion.z);
                  wings.addEnergy(chestItem,-50);
                  triggered = true;
               }
            }
            if(triggered) SoundUtils.playSongToPlayer(player, SoundEvents.ENTITY_ENDER_DRAGON_FLAP, 1, .7f);
         }
      }
   
      
      // Enderia Boss health scale
      Pair<BossFights, NbtCompound> bossFight = BOSS_FIGHT.get(entity.getWorld()).getBossFight();
      int numPlayers = 0;
      if(bossFight != null){
         numPlayers = bossFight.getRight().getInt("numPlayers", 0);
      }
      if(numPlayers != 0){
         float scale = Math.max(2f/numPlayers, 0.1f);
         if(entity instanceof EnderDragonEntity){
            newReturn *= scale; //Effective Health Scale to bypass 1024 hp cap
            if(source.isIn(DamageTypeTags.BYPASSES_ARMOR) || source.isIn(DamageTypeTags.IS_EXPLOSION)) newReturn *= 0.25f; // Reduce damage from magic and explosive sources
         }
      }
      
      // Death Ward
      if(entity.hasStatusEffect(ArcanaRegistry.DEATH_WARD_EFFECT) && (!source.isIn(DamageTypeTags.BYPASSES_RESISTANCE) || source.isOf(ArcanaDamageTypes.CONCENTRATION))){
         StatusEffectInstance effect = entity.getStatusEffect(ArcanaRegistry.DEATH_WARD_EFFECT);
         if(entity.getHealth() < newReturn){
            float damageWarded = newReturn;
            newReturn = entity.getHealth() - 0.01f;
            damageWarded -= newReturn;
            
            if(entity instanceof ServerPlayerEntity player && ArcanaAchievements.isTimerActive(player, ArcanaAchievements.TOO_ANGRY_TO_DIE.id)){
               ArcanaAchievements.progress(player,ArcanaAchievements.TOO_ANGRY_TO_DIE.id, Math.round(damageWarded));
            }
         }
      }
      
      return newReturn;
   }
   
   @ModifyExpressionValue(method = "tryUseDeathProtector", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/damage/DamageSource;isIn(Lnet/minecraft/registry/tag/TagKey;)Z"))
   public boolean arcananovum$totemDamageCheck(boolean original, DamageSource source){
      if (!original) return false;
      return !source.isIn(ArcanaRegistry.ALLOW_TOTEM_USAGE);
   }
   
   @ModifyExpressionValue(method = "tryUseDeathProtector", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getStackInHand(Lnet/minecraft/util/Hand;)Lnet/minecraft/item/ItemStack;"))
   public ItemStack arcananovum$deathProtectorCooldown(ItemStack original){
      LivingEntity livingEntity = (LivingEntity) (Object) this;
      if(livingEntity instanceof ServerPlayerEntity player){
         if(player.getItemCooldownManager().isCoolingDown(original)){
            return ItemStack.EMPTY;
         }
      }
      return original;
   }
   
   @Inject(method = "tryUseDeathProtector", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;setHealth(F)V"), cancellable = true)
   public void arcananovum$vengeanceTrigger(DamageSource source, CallbackInfoReturnable<Boolean> cir, @Local(ordinal = 0) ItemStack itemStack){
      LivingEntity livingEntity = (LivingEntity) (Object) this;
      List<NulConstructEntity> constructs = livingEntity.getWorld().getEntitiesByClass(NulConstructEntity.class,livingEntity.getBoundingBox().expand(NulConstructEntity.FIGHT_RANGE*2),construct -> construct.getSummoner().getUuid().equals(livingEntity.getUuid()));
      
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
   
   @Inject(method = "tryUseDeathProtector", at = @At("RETURN"), cancellable = true)
   public void arcananovum$findTotem(DamageSource source, CallbackInfoReturnable<Boolean> cir){
      LivingEntity livingEntity = (LivingEntity) (Object) this;
      if(cir.getReturnValueZ()) return;
      
      if(source.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY) && !source.isIn(ArcanaRegistry.ALLOW_TOTEM_USAGE)) return;
      
      ItemStack itemStack = null;
      DeathProtectionComponent deathProtectionComponent = null;
      
      if(livingEntity instanceof ServerPlayerEntity player){
         ItemCooldownManager cooldowns = player.getItemCooldownManager();
         Inventory inv = player.getInventory();
         block: {
            for(int i = 0; i < inv.size(); i++){
               ItemStack beltStack = inv.getStack(i);
               if(ArcanaItemUtils.identifyItem(beltStack) instanceof ArcanistsBelt){
                  ContainerComponent beltItems = beltStack.getOrDefault(DataComponentTypes.CONTAINER,ContainerComponent.DEFAULT);
                  for(ItemStack stack : beltItems.iterateNonEmpty()){
                     if(cooldowns.isCoolingDown(stack)) continue;
                     deathProtectionComponent = stack.get(DataComponentTypes.DEATH_PROTECTION);
                     if (deathProtectionComponent != null) {
                        itemStack = stack.copy();
                        stack.decrement(1);
                        break block;
                     }
                  }
               }
            }
         }
      }
      
      List<NulConstructEntity> constructs = livingEntity.getWorld().getEntitiesByClass(NulConstructEntity.class,livingEntity.getBoundingBox().expand(NulConstructEntity.FIGHT_RANGE*2),construct -> construct.getSummoner() != null && construct.getSummoner().getUuid().equals(livingEntity.getUuid()));
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
         
         
         if(livingEntity instanceof ServerPlayerEntity player){
            player.incrementStat(Stats.USED.getOrCreateStat(itemStack.getItem()));
            Criteria.USED_TOTEM.trigger(player, itemStack);
            livingEntity.emitGameEvent(GameEvent.ITEM_INTERACT_FINISH);
         }
         
         livingEntity.setHealth(1.0F);
         if(ArcanaItemUtils.identifyItem(itemStack) instanceof TotemOfVengeance vengeance){
            vengeance.triggerTotem(itemStack, livingEntity, source); // TODO, maybe turn this into a death effect thing?
         }
         if(deathProtectionComponent != null){
            deathProtectionComponent.applyDeathEffects(itemStack, livingEntity);
         }
         livingEntity.getWorld().sendEntityStatus(livingEntity, EntityStatuses.USE_TOTEM_OF_UNDYING);
         cir.setReturnValue(true);
         return;
      }
      
      ItemStack headStack = livingEntity.getEquippedStack(EquipmentSlot.HEAD);
      if(ArcanaItemUtils.identifyItem(headStack) instanceof NulMemento memento && memento.protectFromDeath(headStack,livingEntity,source,!constructs.isEmpty())){
         constructs.forEach(construct -> construct.triggerAdaptation(NulConstructEntity.ConstructAdaptations.USED_MEMENTO));
         cir.setReturnValue(true);
         return;
      }
   }
   
   @Inject(method="onStatusEffectsRemoved",at=@At(value = "INVOKE", target = "Lnet/minecraft/entity/effect/StatusEffect;onRemoved(Lnet/minecraft/entity/attribute/AttributeContainer;)V"))
   private void arcananovum$effectRemoved(Collection<StatusEffectInstance> effects, CallbackInfo ci){
      LivingEntity livingEntity = (LivingEntity) (Object) this;
      for(StatusEffectInstance effect : effects){
         if(effect.getEffectType() == ArcanaRegistry.GREATER_INVISIBILITY_EFFECT && livingEntity.getServer() != null){
            GreaterInvisibilityEffect.removeInvis(livingEntity.getServer(),livingEntity);
         }
      }
   }
   
   @Inject(method="updatePotionVisibility", at=@At(value="INVOKE", target = "Lnet/minecraft/entity/LivingEntity;setInvisible(Z)V", ordinal = 1, shift = At.Shift.AFTER))
   private void arcananovum$greaterInvisibilityUpdate(CallbackInfo ci){
      LivingEntity livingEntity = (LivingEntity) (Object) this;
      livingEntity.setInvisible(livingEntity.isInvisible() || livingEntity.hasStatusEffect(ArcanaRegistry.GREATER_INVISIBILITY_EFFECT));
   }
   
   @ModifyReturnValue(method="getAttackDistanceScalingFactor", at=@At("RETURN"))
   private double arcananovum_greaterInvisibilityAttackRangeScale(double original, Entity attacker){
      LivingEntity livingEntity = (LivingEntity) (Object) this;
      if(attacker.getType().isIn(ArcanaRegistry.IGNORES_GREATER_INVISIBILITY)) return original;
      if(livingEntity.hasStatusEffect(ArcanaRegistry.GREATER_INVISIBILITY_EFFECT)){
         return original * 0.01;
      }
      return original;
   }
   
   @ModifyReturnValue(method="canTarget(Lnet/minecraft/entity/LivingEntity;)Z", at=@At("RETURN"))
   private boolean arcananovum_canTarget(boolean original, LivingEntity target){
      LivingEntity livingEntity = (LivingEntity) (Object) this;
      if(livingEntity.hasStatusEffect(ArcanaRegistry.GREATER_BLINDNESS_EFFECT) && !livingEntity.getType().isIn(ArcanaRegistry.IGNORES_GREATER_BLINDNESS)){
         return false;
      }
      if(target.hasStatusEffect(ArcanaRegistry.GREATER_INVISIBILITY_EFFECT) && !livingEntity.getType().isIn(ArcanaRegistry.IGNORES_GREATER_INVISIBILITY)){
         return false;
      }
      if(livingEntity.getType().isIn(ArcanaRegistry.NUL_CONSTRUCT_FRIENDS) && target instanceof NulConstructEntity){
         return false;
      }
      return original;
   }
   
   @Inject(method="onEquipStack", at=@At("HEAD"), cancellable = true)
   private void arcananovum_sojournerEquipBug(EquipmentSlot slot, ItemStack oldStack, ItemStack newStack, CallbackInfo ci){
      String uuid1,uuid2;
      if(oldStack.isOf(newStack.getItem()) && ArcanaItemUtils.isArcane(oldStack) && (uuid1 = ArcanaItem.getUUID(newStack)) != null && (uuid2 = ArcanaItem.getUUID(newStack)) != null && uuid1.equals(uuid2)){
         ci.cancel();
      }
   }
}
