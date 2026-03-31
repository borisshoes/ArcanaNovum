package net.borisshoes.arcananovum.events.special;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.utils.Dialog;
import net.borisshoes.arcananovum.utils.DialogHelper;
import net.borisshoes.borislib.BorisLib;
import net.borisshoes.borislib.callbacks.ItemReturnTimerCallback;
import net.borisshoes.borislib.events.Event;
import net.borisshoes.borislib.timers.GenericTimer;
import net.borisshoes.borislib.utils.MinecraftUtils;
import net.borisshoes.borislib.utils.ParticleEffectUtils;
import net.borisshoes.borislib.utils.SoundUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CeptyusOpenEvent extends Event {
   public static final Identifier ID = ArcanaRegistry.arcanaId("ceptyus_open");
   
   private final ServerPlayer player;
   private final ServerLevel world;
   private final Vec3 position;
   private boolean proceed = false;
   private boolean endPhase = false;
   
   public CeptyusOpenEvent(ServerPlayer player){
      super(ID, 1200);
      this.player = player;
      this.world = player.level();
      this.position = player.position();
   }
   
   public ServerPlayer getPlayer(){
      return player;
   }
   
   @Override
   public void onExpiry(){
      if(proceed){
         DialogHelper.sendDialog(List.of(player), new Dialog(new ArrayList<>(Arrays.asList(
               Component.literal("")
                     .append(Component.literal("The rift quickly seals itself.").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)),
               Component.literal("\n")
                     .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                     .append(Component.literal("Equayus").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                     .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                     .append(Component.literal("\nWe were too late, I fear this opportunity might not come up again...").withStyle(ChatFormatting.AQUA)),
               Component.literal("\n")
                     .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                     .append(Component.literal("Nul").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
                     .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                     .append(Component.literal("\nLet your complacency in this endeavor be a lesson to you, Player.").withStyle(ChatFormatting.DARK_GRAY))
         )), new ArrayList<>(Arrays.asList(
               null,
               new Dialog.DialogSound(SoundEvents.ALLAY_AMBIENT_WITHOUT_ITEM, 0.5f, 0.7f),
               new Dialog.DialogSound(SoundEvents.WITHER_AMBIENT, 0.5f, 0.7f)
         )), new int[]{0, 80, 80}, 1, 1, 0), true);
      }
   }
   
   @Override
   public void tick(){
      super.tick();
      
      if(proceed && !player.level().equals(this.world) || player.distanceToSqr(this.position) > 10000){
         markForRemoval();
         if(!player.isDeadOrDying()){
            onExpiry();
         }
         return;
      }
      
      if(endPhase){
         Vec3 pos = position.add(0, 2, 0);
         ParticleEffectUtils.spawnLongParticle(world, ParticleTypes.WITCH, pos.x(), pos.y(), pos.z(), 0.2, 1.25, 0.2, 0, 5);
         ParticleEffectUtils.spawnLongParticle(world, ParticleTypes.PORTAL, pos.x(), pos.y(), pos.z(), 0.5, 1.25, 0.5, 1, 5);
         world.sendParticles(ParticleTypes.WITCH, pos.x(), pos.y(), pos.z(), 10, 0.2, 1.25, 0.2, 0);
         world.sendParticles(ParticleTypes.DRIPPING_OBSIDIAN_TEAR, pos.x(), pos.y() + 1, pos.z(), 2, 0.4, 1.25, 0.4, 0);
         world.sendParticles(ParticleTypes.PORTAL, pos.x(), pos.y(), pos.z(), 10, 0.5, 1.25, 0.5, 1);
         world.sendParticles(ParticleTypes.PORTAL, pos.x(), pos.y(), pos.z(), 30, 2, 0.1, 2, 0.3);
         world.sendParticles(ParticleTypes.SONIC_BOOM, pos.x(), pos.y(), pos.z(), 1, 0.5, 1, 0.5, 0);
         SoundUtils.playSound(world, BlockPos.containing(position).above(2), SoundEvents.SOUL_ESCAPE, SoundSource.MASTER, 1f, world.getRandom().nextFloat() + 0.5f);
         SoundUtils.playSound(world, BlockPos.containing(position).above(2), SoundEvents.SCULK_CLICKING, SoundSource.MASTER, 0.25f, world.getRandom().nextFloat() + 0.5f);
         
         if(this.timeAlive > 80 && this.timeAlive < 100){
            world.sendParticles(ParticleTypes.SONIC_BOOM, pos.x(), pos.y(), pos.z(), 4, 0.5, 1, 0.5, 0);
         }
         
         if(this.timeAlive > 130) markForRemoval();
         return;
      }
      
      if(this.timeAlive > 260){
         Vec3 pos = position.add(0, 2, 0);
         ParticleEffectUtils.spawnLongParticle(world, ParticleTypes.WITCH, pos.x(), pos.y(), pos.z(), 0.2, 1.25, 0.2, 0, 5);
         ParticleEffectUtils.spawnLongParticle(world, ParticleTypes.PORTAL, pos.x(), pos.y(), pos.z(), 0.5, 1.25, 0.5, 1, 5);
         world.sendParticles(ParticleTypes.WITCH, pos.x(), pos.y(), pos.z(), 10, 0.2, 1.25, 0.2, 0);
         world.sendParticles(ParticleTypes.DRIPPING_OBSIDIAN_TEAR, pos.x(), pos.y() + 1, pos.z(), 2, 0.4, 1.25, 0.4, 0);
         world.sendParticles(ParticleTypes.PORTAL, pos.x(), pos.y(), pos.z(), 10, 0.5, 1.25, 0.5, 1);
         world.sendParticles(ParticleTypes.PORTAL, pos.x(), pos.y(), pos.z(), 30, 2, 0.1, 2, 0.3);
      }else if(this.timeAlive > 220){
         Vec3 pos = position.add(0, 2, 0);
         ParticleEffectUtils.spawnLongParticle(world, ParticleTypes.WITCH, pos.x(), pos.y(), pos.z(), 0.25, 1.25, 0.25, 0, 2);
         ParticleEffectUtils.spawnLongParticle(world, ParticleTypes.PORTAL, pos.x(), pos.y(), pos.z(), 0.25, 1.25, 0.25, 0.2, 3);
      }
      if(this.timeAlive > 360 && this.timeAlive < 380){
         Vec3 pos = position.add(0, 2, 0);
         world.sendParticles(ParticleTypes.SONIC_BOOM, pos.x(), pos.y(), pos.z(), 4, 0.5, 1, 0.5, 0);
      }else if(this.timeAlive > 370){
         Vec3 pos = position.add(0, 2, 0);
         world.sendParticles(ParticleTypes.SONIC_BOOM, pos.x(), pos.y(), pos.z(), 1, 0.5, 1, 0.5, 0);
      }
      
      if(this.timeAlive > 260){
         SoundUtils.playSound(world, BlockPos.containing(position).above(2), SoundEvents.SOUL_ESCAPE, SoundSource.MASTER, 1f, world.getRandom().nextFloat() + 0.5f);
         SoundUtils.playSound(world, BlockPos.containing(position).above(2), SoundEvents.SCULK_CLICKING, SoundSource.MASTER, 0.25f, world.getRandom().nextFloat() + 0.5f);
      }
      
      if(this.timeAlive == 370){
         blast();
         if(player.isDeadOrDying()){
            markForRemoval();
            DialogHelper.sendDialog(List.of(player), new Dialog(new ArrayList<>(Arrays.asList(
                  Component.literal("")
                        .append(Component.literal("The blast rips you apart and the rift quickly seals itself.").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)),
                  Component.literal("\n")
                        .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                        .append(Component.literal("Equayus").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                        .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                        .append(Component.literal("\nThat was a valiant effort, Player. However I suspect they won't let such an incursion happen again.").withStyle(ChatFormatting.AQUA))
            )), new ArrayList<>(Arrays.asList(
                  new Dialog.DialogSound(SoundEvents.PORTAL_TRIGGER, 0.5f, 2f),
                  new Dialog.DialogSound(SoundEvents.ALLAY_AMBIENT_WITHOUT_ITEM, 0.5f, 0.7f)
            )), new int[]{20, 80}, 1, 1, 0), true);
         }else if(player.getItemBySlot(EquipmentSlot.HEAD).is(ArcanaRegistry.NUL_MEMENTO.getItem())){
            proceed = true;
            DialogHelper.sendDialog(List.of(player), new Dialog(new ArrayList<>(Arrays.asList(
                  Component.literal("")
                        .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                        .append(Component.literal("Nul").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
                        .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                        .append(Component.literal("\nDo not fear, my chosen. My gift has offered you my blessing...").withStyle(ChatFormatting.DARK_GRAY)),
                  Component.literal("")
                        .append(Component.literal("     ... and a unique chance.").withStyle(ChatFormatting.DARK_GRAY)),
                  Component.literal("\n[Reach Into the Rift...]").withStyle(s ->
                        s.withBold(true).withColor(ChatFormatting.LIGHT_PURPLE).withClickEvent(new ClickEvent.RunCommand("/arcana specialEvent action c_reach")))
            )), new ArrayList<>(Arrays.asList(
                  new Dialog.DialogSound(SoundEvents.WITHER_AMBIENT, 0.5f, 0.7f),
                  new Dialog.DialogSound(SoundEvents.WITHER_AMBIENT, 0.5f, 1.3f),
                  null
            )), new int[]{120, 80, 40}, 1, 1, 0), true);
         }else{
            markForRemoval();
            DialogHelper.sendDialog(List.of(player), new Dialog(new ArrayList<>(Arrays.asList(
                  Component.literal("")
                        .append(Component.literal("The rift quickly seals itself.").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)),
                  Component.literal("\n")
                        .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                        .append(Component.literal("Equayus").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                        .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                        .append(Component.literal("\nI am impressed Player! Your ingenuity is unparalleled! However, I suspect they won't let such an incursion happen again.").withStyle(ChatFormatting.AQUA))
            )), new ArrayList<>(Arrays.asList(
                  new Dialog.DialogSound(SoundEvents.PORTAL_TRIGGER, 0.5f, 2f),
                  new Dialog.DialogSound(SoundEvents.ALLAY_AMBIENT_WITHOUT_ITEM, 0.5f, 0.7f)
            )), new int[]{80, 80}, 1, 1, 0), true);
         }
      }
   }
   
   public void complete(){
      endPhase = true;
      proceed = false;
      timeAlive = 0;
      player.addEffect(new MobEffectInstance(ArcanaRegistry.DEATH_WARD_EFFECT, 1000, 0, false, false, false));
      DialogHelper.sendDialog(List.of(player), new Dialog(new ArrayList<>(Arrays.asList(
            Component.literal("\n")
                  .append(Component.literal("The sonic barriers protecting and sealing the ").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC))
                  .append(Component.literal("rift").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC))
                  .append(Component.literal(" rip through your flesh, down the bone, but the ").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC))
                  .append(Component.literal("God of Death's Veil").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC))
                  .append(Component.literal(" has fallen over you.").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)),
            Component.literal("\n")
                  .append(Component.literal("Pushing through the pain, you feel around the other side of the ").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC))
                  .append(Component.literal("portal").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC))
                  .append(Component.literal(" and grab hold of ").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC))
                  .append(Component.literal("something").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.ITALIC))
                  .append(Component.literal(" and pull back.").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)),
            Component.literal("\n")
                  .append(Component.literal("Another blast ").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC))
                  .append(Component.literal("surges").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC))
                  .append(Component.literal(" through the ").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC))
                  .append(Component.literal("rift").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.ITALIC))
                  .append(Component.literal(" before it seals itself.").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)),
            Component.literal("\n")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                  .append(Component.literal("Equayus").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                  .append(Component.literal("\nI am impressed Player! Your ingenuity is unparalleled!").withStyle(ChatFormatting.AQUA)),
            Component.literal("\n")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("Nul").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("\nYou can see why I like this one.").withStyle(ChatFormatting.DARK_GRAY)),
            Component.literal("\n")
                  .append(Component.literal("You look down at your slowly healing, skeletal hand and see a ").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC))
                  .append(Component.literal("pickaxe").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC))
                  .append(Component.literal(" unlike any other.").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)),
            Component.literal("\n")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("Nul").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("\nYour bravery has earned this trophy. ").withStyle(ChatFormatting.DARK_GRAY))
                  .append(Component.literal("Ceptyus").withStyle(ChatFormatting.DARK_AQUA))
                  .append(Component.literal(" was right to fear us.").withStyle(ChatFormatting.DARK_GRAY)),
            Component.literal("\n")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                  .append(Component.literal("Equayus").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                  .append(Component.literal("\nA well earned reward, but I fear this has pushed ").withStyle(ChatFormatting.AQUA))
                  .append(Component.literal("Ceptyus's realm").withStyle(ChatFormatting.DARK_AQUA))
                  .append(Component.literal(" further beyond our reach.").withStyle(ChatFormatting.AQUA))
      )), new ArrayList<>(Arrays.asList(
            null,
            new Dialog.DialogSound(SoundEvents.WARDEN_SONIC_CHARGE, 2f, 0.7f),
            new Dialog.DialogSound(SoundEvents.WARDEN_SONIC_BOOM, 2f, 1.3f),
            new Dialog.DialogSound(SoundEvents.ALLAY_AMBIENT_WITHOUT_ITEM, 0.5f, 1.4f),
            new Dialog.DialogSound(SoundEvents.WITHER_AMBIENT, 0.5f, 0.7f),
            null,
            new Dialog.DialogSound(SoundEvents.WITHER_AMBIENT, 0.5f, 0.7f),
            new Dialog.DialogSound(SoundEvents.ALLAY_AMBIENT_WITHOUT_ITEM, 0.5f, 0.7f)
      )), new int[]{0, 60, 60, 100, 80, 60, 140, 80}, 1, 1, 0), true);
      BorisLib.addTickTimerCallback(new GenericTimer(120, this::blast));
      
      ItemStack pick = ArcanaRegistry.PICKAXE_OF_CEPTYUS.addCrafter(ArcanaRegistry.PICKAXE_OF_CEPTYUS.getNewItem(), player.getStringUUID(), 3, player.level().getServer());
      pick.enchant(MinecraftUtils.getEnchantment(ArcanaRegistry.FATE_ANCHOR), 1);
      BorisLib.addTickTimerCallback(new ItemReturnTimerCallback(pick, player, 380));
      BorisLib.addTickTimerCallback(new GenericTimer(380, () -> {
         ArcanaAchievements.grant(player, ArcanaAchievements.INTERLOPER);
         ArcanaNovum.data(player).completeCeptyus();
      }));
   }
   
   private void blast(){
      AABB rangeBox = new AABB(position.x + 5, position.y + 5, position.z + 5, position.x - 5, position.y - 5, position.z - 5);
      List<Entity> entities = world.getEntities((Entity) null, rangeBox, e -> !e.isSpectator() && e.distanceToSqr(position) < 1.5 * 4.5 * 4.5 && !(e instanceof EnderDragon));
      entities.add(player);
      for(Entity entity1 : entities){
         if(entity1 instanceof LivingEntity living){
            living.hurtServer(world, world.damageSources().sonicBoom(null), living.getMaxHealth() * 100);
         }
         Vec3 diff = entity1.position().subtract(position);
         double multiplier = Mth.clamp(5 - diff.length() * .5, 1, 7.5);
         Vec3 motion = diff.add(0, 0, 0).normalize().scale(multiplier);
         entity1.setDeltaMovement(motion.x, motion.y, motion.z);
         if(entity1 instanceof ServerPlayer serverPlayer){
            serverPlayer.connection.send(new ClientboundSetEntityMotionPacket(serverPlayer));
         }
      }
      
      Vec3 pos = position.add(0, 2, 0);
      world.sendParticles(ParticleTypes.SONIC_BOOM, pos.x(), pos.y(), pos.z(), 50, 1, 2, 1, 0);
   }
   
   public boolean canProceed(){
      return proceed;
   }
   
   public Vec3 getPosition(){
      return position;
   }
}
