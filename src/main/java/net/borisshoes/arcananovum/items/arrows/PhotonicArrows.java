package net.borisshoes.arcananovum.items.arrows;

import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerArrowItem;
import net.borisshoes.arcananovum.damage.ArcanaDamageTypes;
import net.borisshoes.arcananovum.entities.RunicArrowEntity;
import net.borisshoes.arcananovum.gui.arcanetome.ArcaneTomeGui;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaEffectUtils;
import net.borisshoes.arcananovum.utils.ArcanaUtils;
import net.borisshoes.borislib.utils.MinecraftUtils;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class PhotonicArrows extends RunicArrow {
	public static final String ID = "photonic_arrows";
   
   public PhotonicArrows(){
      id = ID;
      name = "Photonic Arrows";
      rarity = ArcanaRarity.EXOTIC;
      categories = new ArcaneTomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), ArcaneTomeGui.TomeFilter.ARROWS};
      vanillaItem = Items.TIPPED_ARROW;
      item = new PhotonicArrowsItem();
      displayName = Component.translatableWithFallback("item."+MOD_ID+"."+ID,name).withStyle(ChatFormatting.BOLD, ChatFormatting.AQUA);
      researchTasks = new ResourceKey[]{ResearchTasks.UNLOCK_RUNIC_MATRIX,ResearchTasks.UNLOCK_RADIANT_FLETCHERY,ResearchTasks.OBTAIN_SPECTRAL_ARROW, ResearchTasks.ADVANCEMENT_CREATE_FULL_BEACON,ResearchTasks.OBTAIN_AMETHYST_CLUSTER,ResearchTasks.UNLOCK_MIDNIGHT_ENCHANTER};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getDefaultMaxStackSize());
      setPrefStack(stack);
   }
   
   @Override
   public List<Component> getItemLore(@Nullable ItemStack itemStack){
      List<MutableComponent> lore = new ArrayList<>();
      addRunicArrowLore(lore);
      lore.add(Component.literal("Photonic Arrows:").withStyle(ChatFormatting.BOLD, ChatFormatting.AQUA));
      lore.add(Component.literal("")
            .append(Component.literal("These ").withStyle(ChatFormatting.WHITE))
            .append(Component.literal("Runic Arrows").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(" fly perfectly ").withStyle(ChatFormatting.WHITE))
            .append(Component.literal("straight ").withStyle(ChatFormatting.AQUA))
            .append(Component.literal("through the air.").withStyle(ChatFormatting.WHITE)));
      lore.add(Component.literal("")
            .append(Component.literal("The ").withStyle(ChatFormatting.WHITE))
            .append(Component.literal("arrows ").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal("pierce ").withStyle(ChatFormatting.AQUA))
            .append(Component.literal("all ").withStyle(ChatFormatting.WHITE))
            .append(Component.literal("entities ").withStyle(ChatFormatting.AQUA))
            .append(Component.literal("before hitting a ").withStyle(ChatFormatting.WHITE))
            .append(Component.literal("block").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(".").withStyle(ChatFormatting.WHITE)));
     return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   public void shoot(Level world, LivingEntity entity, Projectile proj, int alignmentLvl){
      if(!(world instanceof ServerLevel serverWorld)) return;
      MinecraftUtils.LasercastResult lasercast = MinecraftUtils.lasercast(world, entity.getEyePosition(), entity.getForward(), 100, true, entity);
      
      float damage = (float) Mth.clamp(proj.getDeltaMovement().length()*5,1,20);
      if(alignmentLvl == 5) damage += (float) (4 + damage*0.2);
      float bonusDmg = 0;
      
      int killCount = 0;
      for(Entity hit : lasercast.sortedHits()){
         float finalDmg = (float) ((damage+bonusDmg) * Math.min(1,-0.01*(hit.position().distanceTo(lasercast.startPos())-100)+0.25)) * (hit instanceof ServerPlayer ? 0.5f : 1f);
         if(hit instanceof ServerPlayer hitPlayer && hitPlayer.isBlocking()){
            double dp = hitPlayer.getForward().normalize().dot(lasercast.direction().normalize());
            if(dp < -0.6){
               ArcanaUtils.blockWithShield(hitPlayer,finalDmg);
               continue;
            }
         }
         hit.hurtServer(serverWorld, ArcanaDamageTypes.of(entity.level(),ArcanaDamageTypes.PHOTONIC,proj,entity), finalDmg);
         
         if(hit instanceof Mob mob && mob.isDeadOrDying()){
            killCount++;
         }
         bonusDmg = Math.min(25,bonusDmg + alignmentLvl);
      }
      
      if(proj.getOwner() instanceof ServerPlayer player && killCount >= 10) ArcanaAchievements.grant(player,ArcanaAchievements.X.id);
      
      if(proj.getOwner() instanceof ServerPlayer player && !lasercast.sortedHits().isEmpty() && proj instanceof RunicArrowEntity runicArrowEntity){
         runicArrowEntity.incArrowForEveryFoe(player);
      }
      
      ArcanaEffectUtils.photonArrowShot(serverWorld,entity.getEyePosition().subtract(0,entity.getBbHeight()/4,0),lasercast.endPos(), Mth.clamp(damage/15,.4f,1f),0);
   }
   
   @Override
   public void entityHit(RunicArrowEntity arrow, EntityHitResult entityHitResult){}
   
   @Override
   public void blockHit(RunicArrowEntity arrow, BlockHitResult blockHitResult){}
   
   @Override
   public List<List<Component>> getBookLore(){
      List<List<Component>> list = new ArrayList<>();
      list.add(List.of(Component.literal(" Photonic Arrows").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD), Component.literal("\nRarity: ").withStyle(ChatFormatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(),false)), Component.literal("\n‘Straight as an arrow’. What a joke of a saying, I’ll show them what straight looks like. Some solar runes coupled with a focusing prism makes quite the combo. This brings a new meaning to ‘Shooting Lazers’.").withStyle(ChatFormatting.BLACK)));
      return list;
   }
   
   public class PhotonicArrowsItem extends ArcanaPolymerArrowItem {
      public PhotonicArrowsItem(){
         super(getThis(),getArcanaArrowItemComponents(11271167));
      }
      
      @Override
      public ItemStack getDefaultInstance(){
         return prefItem;
      }
   }
}

