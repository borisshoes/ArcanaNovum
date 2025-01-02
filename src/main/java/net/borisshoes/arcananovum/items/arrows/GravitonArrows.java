package net.borisshoes.arcananovum.items.arrows;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerArrowItem;
import net.borisshoes.arcananovum.entities.RunicArrowEntity;
import net.borisshoes.arcananovum.gui.arcanetome.TomeGui;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaIngredient;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaRecipe;
import net.borisshoes.arcananovum.recipes.arcana.ForgeRequirement;
import net.borisshoes.arcananovum.recipes.arcana.GenericArcanaIngredient;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.*;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.potion.Potions;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class GravitonArrows extends RunicArrow {
	public static final String ID = "graviton_arrows";
   
   public GravitonArrows(){
      id = ID;
      name = "Graviton Arrows";
      rarity = ArcanaRarity.EXOTIC;
      categories = new TomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), TomeGui.TomeFilter.ARROWS};
      vanillaItem = Items.TIPPED_ARROW;
      item = new GravitonArrowsItem(addArcanaItemComponents(new Item.Settings().maxCount(64)
            .component(DataComponentTypes.POTION_CONTENTS, new PotionContentsComponent(Optional.empty(),Optional.of(869887),new ArrayList<>(),Optional.empty()))
      ));
      displayName = TextUtils.withColor(Text.translatableWithFallback("item."+MOD_ID+"."+ID,name).formatted(Formatting.BOLD), ArcanaColors.BETTER_DARK_BLUE);
      researchTasks = new RegistryKey[]{ResearchTasks.UNLOCK_RUNIC_MATRIX,ResearchTasks.UNLOCK_RADIANT_FLETCHERY,ResearchTasks.OBTAIN_SPECTRAL_ARROW, ResearchTasks.ADVANCEMENT_DRAGON_BREATH,ResearchTasks.EFFECT_SLOWNESS};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getMaxCount());
      setPrefStack(stack);
   }
   
   @Override
   public List<Text> getItemLore(@Nullable ItemStack itemStack){
      List<MutableText> lore = new ArrayList<>();
      addRunicArrowLore(lore);
      lore.add(TextUtils.withColor(Text.literal("Graviton Arrows:").formatted(Formatting.BOLD), ArcanaColors.BETTER_DARK_BLUE));
      lore.add(Text.literal("")
            .append(Text.literal("These ").formatted(Formatting.BLUE))
            .append(Text.literal("Runic Arrows").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal(" attract").formatted(Formatting.DARK_AQUA))
            .append(Text.literal(" entities").formatted(Formatting.AQUA))
            .append(Text.literal(" near the area of ").formatted(Formatting.BLUE))
            .append(Text.literal("impact").formatted(Formatting.AQUA))
            .append(Text.literal(".").formatted(Formatting.BLUE)));
      lore.add(Text.literal("")
            .append(Text.literal("A hit ").formatted(Formatting.BLUE))
            .append(Text.literal("entity").formatted(Formatting.AQUA))
            .append(Text.literal(" is ").formatted(Formatting.BLUE))
            .append(Text.literal("not affected").formatted(Formatting.DARK_AQUA))
            .append(Text.literal(".").formatted(Formatting.BLUE)));
     return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   public void entityHit(RunicArrowEntity arrow, EntityHitResult entityHitResult){
      if(arrow.getEntityWorld() instanceof ServerWorld serverWorld){
         int duration = (int) MathHelper.clamp(arrow.getVelocity().length()*7,2,20);// Measured in quarter seconds
         double range = 3 + arrow.getAugment(ArcanaAugments.GRAVITY_WELL.id);
         gravitonPulse(arrow, serverWorld,null,entityHitResult.getEntity(),duration,range,0);
      }
   }
   
   @Override
   public void blockHit(RunicArrowEntity arrow, BlockHitResult blockHitResult){
      if(arrow.getEntityWorld() instanceof ServerWorld serverWorld){
         int duration = (int) MathHelper.clamp(arrow.getVelocity().length()*7,2,20); // Measured in quarter seconds
         double range = 3 + arrow.getAugment(ArcanaAugments.GRAVITY_WELL.id);
         gravitonPulse(arrow, serverWorld,blockHitResult.getPos(),null,duration,range,0);
      }
   }
   
   private void gravitonPulse(PersistentProjectileEntity arrow, ServerWorld world, @Nullable Vec3d start, @Nullable Entity entity, int duration, double range, int calls){
      if(start == null && entity == null) return;
      Vec3d pos = entity == null ? start : entity.getPos();
      int mobsHit = 0;
      
      Box rangeBox = new Box(pos.x+8,pos.y+8,pos.z+8,pos.x-8,pos.y-8,pos.z-8);
      List<Entity> entities = world.getOtherEntities(entity,rangeBox, e -> !e.isSpectator() && e.squaredDistanceTo(pos) < 2*range*range && !(e instanceof PersistentProjectileEntity));
      for(Entity entity1 : entities){
         Vec3d diff = entity1.getPos().subtract(pos);
         double multiplier = MathHelper.clamp(diff.length()*.2,.03,2);
         Vec3d motion = diff.add(0,0,0).normalize().multiply(-multiplier);
         entity1.setVelocity(motion.x,motion.y,motion.z);
         if(entity1 instanceof ServerPlayerEntity player){
            player.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(player));
         }
         
         if(entity1 instanceof LivingEntity e){
            if(e instanceof MobEntity) mobsHit++;
            
            int amp = (int) (5-diff.length());
            StatusEffectInstance slowness = new StatusEffectInstance(StatusEffects.SLOWNESS, 20, amp, false, false, true);
            e.addStatusEffect(slowness);
         }
      }
      if(arrow.getOwner() instanceof ServerPlayerEntity player && mobsHit >= 10) ArcanaAchievements.grant(player,ArcanaAchievements.BRING_TOGETHER.id);
      
      ParticleEffectUtils.gravitonArrowEmit(world,pos,entities);
      if(calls % 10 == 1){
         SoundUtils.playSound(world,BlockPos.ofFloored(pos), SoundEvents.BLOCK_PORTAL_AMBIENT, SoundCategory.PLAYERS,.5f,1.6f);
      }
      
      if(calls < duration){
         ArcanaNovum.addTickTimerCallback(world, new GenericTimer(5, () -> gravitonPulse(arrow, world, pos, entity,duration,range,calls + 1)));
      }
   }
   
   @Override
	protected ArcanaRecipe makeRecipe(){
      ArcanaIngredient a = ArcanaIngredient.EMPTY;
      ArcanaIngredient c = new ArcanaIngredient(Items.LINGERING_POTION,1).withPotions(Potions.STRONG_SLOWNESS);
      ArcanaIngredient g = new ArcanaIngredient(Items.COBWEB,16);
      ArcanaIngredient h = new ArcanaIngredient(Items.SPECTRAL_ARROW,16);
      GenericArcanaIngredient m = new GenericArcanaIngredient(ArcanaRegistry.RUNIC_MATRIX,1);
      
      ArcanaIngredient[][] ingredients = {
            {a,a,c,a,a},
            {a,g,h,g,a},
            {c,h,m,h,c},
            {a,g,h,g,a},
            {a,a,c,a,a}};
      return new ArcanaRecipe(ingredients,new ForgeRequirement().withFletchery());
   }
   
   @Override
   public List<List<Text>> getBookLore(){
      List<List<Text>> list = new ArrayList<>();
      list.add(List.of(TextUtils.withColor(Text.literal(" Graviton Arrows").formatted(Formatting.BOLD),ArcanaColors.BETTER_DARK_BLUE),Text.literal("\nRarity: ").formatted(Formatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(),false)),Text.literal("\nThis Runic Matrix amplifies gravity at a single point, drawing in everything nearby. Once at the center, things have a hard time leaving. Great for setting up a combo shot.").formatted(Formatting.BLACK)));
      return list;
   }
   
   public class GravitonArrowsItem extends ArcanaPolymerArrowItem {
      public GravitonArrowsItem(Item.Settings settings){
         super(getThis(),settings);
      }
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
   }
}

