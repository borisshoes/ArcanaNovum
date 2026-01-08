package net.borisshoes.arcananovum.items.arrows;

import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerArrowItem;
import net.borisshoes.arcananovum.entities.RunicArrowEntity;
import net.borisshoes.arcananovum.gui.arcanetome.ArcaneTomeGui;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaColors;
import net.borisshoes.arcananovum.utils.ArcanaEffectUtils;
import net.borisshoes.borislib.BorisLib;
import net.borisshoes.borislib.timers.GenericTimer;
import net.borisshoes.borislib.utils.SoundUtils;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class GravitonArrows extends RunicArrow {
	public static final String ID = "graviton_arrows";
   
   public GravitonArrows(){
      id = ID;
      name = "Graviton Arrows";
      rarity = ArcanaRarity.EXOTIC;
      categories = new ArcaneTomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), ArcaneTomeGui.TomeFilter.ARROWS};
      vanillaItem = Items.TIPPED_ARROW;
      item = new GravitonArrowsItem();
      displayName = Component.translatableWithFallback("item."+MOD_ID+"."+ID,name).withStyle(ChatFormatting.BOLD).withColor(ArcanaColors.BETTER_DARK_BLUE);
      researchTasks = new ResourceKey[]{ResearchTasks.UNLOCK_RUNIC_MATRIX,ResearchTasks.UNLOCK_RADIANT_FLETCHERY,ResearchTasks.OBTAIN_SPECTRAL_ARROW, ResearchTasks.ADVANCEMENT_DRAGON_BREATH,ResearchTasks.EFFECT_SLOWNESS};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getDefaultMaxStackSize());
      setPrefStack(stack);
   }
   
   @Override
   public List<Component> getItemLore(@Nullable ItemStack itemStack){
      List<MutableComponent> lore = new ArrayList<>();
      addRunicArrowLore(lore);
      lore.add(Component.literal("Graviton Arrows:").withStyle(ChatFormatting.BOLD).withColor(ArcanaColors.BETTER_DARK_BLUE));
      lore.add(Component.literal("")
            .append(Component.literal("These ").withStyle(ChatFormatting.BLUE))
            .append(Component.literal("Runic Arrows").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(" attract").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal(" entities").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(" near the area of ").withStyle(ChatFormatting.BLUE))
            .append(Component.literal("impact").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(".").withStyle(ChatFormatting.BLUE)));
      lore.add(Component.literal("")
            .append(Component.literal("A hit ").withStyle(ChatFormatting.BLUE))
            .append(Component.literal("entity").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(" is ").withStyle(ChatFormatting.BLUE))
            .append(Component.literal("not affected").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal(".").withStyle(ChatFormatting.BLUE)));
     return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   public void entityHit(RunicArrowEntity arrow, EntityHitResult entityHitResult){
      if(arrow.level() instanceof ServerLevel serverWorld){
         int duration = (int) Mth.clamp(arrow.getDeltaMovement().length()*7,2,20);// Measured in quarter seconds
         double range = 3 + arrow.getAugment(ArcanaAugments.GRAVITY_WELL.id);
         gravitonPulse(arrow, serverWorld,null,entityHitResult.getEntity(),duration,range,0);
      }
   }
   
   @Override
   public void blockHit(RunicArrowEntity arrow, BlockHitResult blockHitResult){
      if(arrow.level() instanceof ServerLevel serverWorld){
         int duration = (int) Mth.clamp(arrow.getDeltaMovement().length()*7,2,20); // Measured in quarter seconds
         double range = 3 + arrow.getAugment(ArcanaAugments.GRAVITY_WELL.id);
         gravitonPulse(arrow, serverWorld,blockHitResult.getLocation(),null,duration,range,0);
      }
   }
   
   private void gravitonPulse(AbstractArrow arrow, ServerLevel world, @Nullable Vec3 start, @Nullable Entity entity, int duration, double range, int calls){
      if(start == null && entity == null) return;
      Vec3 pos = entity == null ? start : entity.position();
      int mobsHit = 0;
      
      AABB rangeBox = new AABB(pos.x+8,pos.y+8,pos.z+8,pos.x-8,pos.y-8,pos.z-8);
      List<Entity> entities = world.getEntities(entity,rangeBox, e -> !e.isSpectator() && e.distanceToSqr(pos) < 2*range*range && !(e instanceof AbstractArrow));
      for(Entity entity1 : entities){
         Vec3 diff = entity1.position().subtract(pos);
         double multiplier = Mth.clamp(diff.length()*.2,.03,2);
         Vec3 motion = diff.add(0,0,0).normalize().scale(-multiplier);
         entity1.setDeltaMovement(motion.x,motion.y,motion.z);
         if(entity1 instanceof ServerPlayer player){
            player.connection.send(new ClientboundSetEntityMotionPacket(player));
         }
         
         if(entity1 instanceof LivingEntity e){
            if(e instanceof Mob) mobsHit++;
            
            int amp = (int) (5-diff.length());
            MobEffectInstance slowness = new MobEffectInstance(MobEffects.SLOWNESS, 20, amp, false, false, true);
            e.addEffect(slowness);
         }
      }
      if(arrow.getOwner() instanceof ServerPlayer player && mobsHit >= 10) ArcanaAchievements.grant(player,ArcanaAchievements.BRING_TOGETHER.id);
      
      ArcanaEffectUtils.gravitonArrowEmit(world,pos,entities);
      if(calls % 10 == 1){
         SoundUtils.playSound(world, BlockPos.containing(pos), SoundEvents.PORTAL_AMBIENT, SoundSource.PLAYERS,.5f,1.6f);
      }
      
      if(calls < duration){
         BorisLib.addTickTimerCallback(world, new GenericTimer(5, () -> gravitonPulse(arrow, world, pos, entity,duration,range,calls + 1)));
      }
   }
   
   @Override
   public List<List<Component>> getBookLore(){
      List<List<Component>> list = new ArrayList<>();
      list.add(List.of(Component.literal(" Graviton Arrows").withStyle(ChatFormatting.BOLD).withColor(ArcanaColors.BETTER_DARK_BLUE), Component.literal("\nRarity: ").withStyle(ChatFormatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(),false)), Component.literal("\nThis Runic Matrix amplifies gravity at a single point, drawing in everything nearby. Once at the center, things have a hard time leaving. Great for setting up a combo shot.").withStyle(ChatFormatting.BLACK)));
      return list;
   }
   
   public class GravitonArrowsItem extends ArcanaPolymerArrowItem {
      public GravitonArrowsItem(){
         super(getThis(),getArcanaArrowItemComponents(869887));
      }
      
      @Override
      public ItemStack getDefaultInstance(){
         return prefItem;
      }
   }
}

