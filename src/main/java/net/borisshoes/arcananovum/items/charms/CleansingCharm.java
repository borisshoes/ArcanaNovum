package net.borisshoes.arcananovum.items.charms;

import com.mojang.datafixers.util.Either;
import net.borisshoes.arcananovum.ArcanaConfig;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.blocks.GeomanticStele;
import net.borisshoes.arcananovum.blocks.GeomanticSteleBlockEntity;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.core.EnergyItem;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerItem;
import net.borisshoes.arcananovum.events.CleansingCharmEvent;
import net.borisshoes.arcananovum.gui.arcanetome.ArcaneTomeGui;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.borislib.conditions.ConditionInstance;
import net.borisshoes.borislib.conditions.Conditions;
import net.borisshoes.borislib.events.Event;
import net.borisshoes.borislib.utils.SoundUtils;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import xyz.nucleoid.packettweaker.PacketContext;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;
import static net.borisshoes.arcananovum.ArcanaRegistry.arcanaId;

public class CleansingCharm extends EnergyItem implements GeomanticStele.Interaction {
   public static final String ID = "cleansing_charm";
   
   public CleansingCharm(){
      id = ID;
      name = "Charm of Cleansing";
      rarity = ArcanaRarity.EXOTIC;
      categories = new ArcaneTomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), ArcaneTomeGui.TomeFilter.ITEMS, ArcaneTomeGui.TomeFilter.CHARMS};
      itemVersion = 0;
      vanillaItem = Items.PRISMARINE_CRYSTALS;
      item = new CleansingCharmItem();
      displayName = Component.translatableWithFallback("item."+MOD_ID+"."+ID,name).withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD);
      researchTasks = new ResourceKey[]{ResearchTasks.MILK_CLEANSE, ResearchTasks.HONEY_CLEANSE, ResearchTasks.EFFECT_POISON, ResearchTasks.EFFECT_NAUSEA, ResearchTasks.EFFECT_BLINDNESS, ResearchTasks.ADVANCEMENT_FURIOUS_COCKTAIL};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getDefaultMaxStackSize());
      putProperty(stack,ACTIVE_TAG, true);
      setPrefStack(stack);
   }
   
   @Override
   public ItemStack updateItem(ItemStack stack, MinecraftServer server){
      boolean active = getBooleanProperty(stack,ACTIVE_TAG);
      ItemStack newStack = super.updateItem(stack,server);
      putProperty(newStack,ACTIVE_TAG,active);
      return buildItemLore(newStack,server);
   }
   
   @Override
   public int getMaxEnergy(ItemStack item){
      int cdLvl = ArcanaAugments.getAugmentOnItem(item,ArcanaAugments.INFUSED_CHARCOAL);
      int baseCD = ArcanaNovum.CONFIG.getInt(ArcanaConfig.CLEANSING_CHARM_COOLDOWN);
      int cdReduction = ArcanaNovum.CONFIG.getIntList(ArcanaConfig.CLEANSING_CHARM_CHARCOAL_COOLDOWN_PER_LVL).get(cdLvl);
      return baseCD - cdReduction;
   }
   
   public boolean cleanseEffect(LivingEntity living, ItemStack stack){
      if(!(ArcanaItemUtils.identifyItem(stack) instanceof CleansingCharm)) return false;
      if(getEnergy(stack) > 0) return false;
      
      boolean removed = false;
      List<Map.Entry<Holder<MobEffect>, MobEffectInstance>> canCleanseEffects = new ArrayList<>(living.getActiveEffectsMap().entrySet().stream().filter(entry ->
            entry.getKey().value().getCategory() == MobEffectCategory.HARMFUL
      ).toList());
      List<ConditionInstance> canCleanseConditions = new ArrayList<>(Conditions.getConditionInstances(living.getUUID()).stream().filter(cond -> !cond.isPersistent() && cond.getCondition().value().isHarmful()).toList());
      
      List<Either<Map.Entry<Holder<MobEffect>, MobEffectInstance>, ConditionInstance>> canCleanse = new ArrayList<>();
      canCleanseEffects.forEach(e -> canCleanse.add(Either.left(e)));
      canCleanseConditions.forEach(e -> canCleanse.add(Either.right(e)));
      Collections.shuffle(canCleanse);
      
      if(canCleanse.size() >= 10 && living instanceof ServerPlayer player){
         ArcanaAchievements.grant(player,ArcanaAchievements.SEPTIC_SHOCK);
      }
      
      int toRemove = ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.ANTIDOTE) > 0 ? 2 : 1;
      for(int i = 0; i < toRemove; i++){
         if(canCleanse.isEmpty()) break;
         Either<Map.Entry<Holder<MobEffect>, MobEffectInstance>, ConditionInstance> next = canCleanse.removeFirst();
         
         if(next.right().isPresent()){
            ConditionInstance condition = next.right().get();
            Conditions.removeCondition(living.level().getServer(),living,condition.getCondition(),condition.getId());
            if(living instanceof ServerPlayer player){
               Event.addEvent(new CleansingCharmEvent(player, Either.right(condition)));
            }
         }else if(next.left().isPresent()){
            Holder<MobEffect> effect = next.left().get().getKey();
            living.removeEffect(effect);
            if(living instanceof ServerPlayer player){
               Event.addEvent(new CleansingCharmEvent(player, Either.left(effect)));
               if(effect.equals(MobEffects.HUNGER)){
                  ArcanaAchievements.grant(player,ArcanaAchievements.FOOD_POISONT);
               }
            }
         }else{
            return false;
         }
         
         if(ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.REJUVENATION) > 0){
            int duration = ArcanaNovum.CONFIG.getInt(ArcanaConfig.CLEANSING_CHARM_REJUVENATION_DURATION);
            float hpPerTick = ArcanaNovum.CONFIG.getFloat(ArcanaConfig.CLEANSING_CHARM_REJUVENATION_HEALTH_PER_TICK);
            ConditionInstance rejuv = new ConditionInstance(Conditions.REJUVENATION,arcanaId(ArcanaRegistry.CLEANSING_CHARM.getId()),duration,hpPerTick,true,true,false, AttributeModifier.Operation.ADD_VALUE, living.getUUID());
            Conditions.addCondition(living.level().getServer(),living,rejuv);
         }
         
         removed = true;
         setEnergy(stack,getMaxEnergy(stack));
         if(living instanceof ServerPlayer player) ArcanaNovum.data(player).addXP(ArcanaNovum.CONFIG.getInt(ArcanaConfig.XP_CLEANSING_CHARM_CLEANSE));
      }
      return removed;
   }
   
   @Override
   public List<Component> getItemLore(@Nullable ItemStack itemStack){
      List<MutableComponent> lore = new ArrayList<>();
      lore.add(Component.literal("")
            .append(Component.literal("The ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("charm ").withStyle(ChatFormatting.AQUA))
            .append(Component.literal("emanates ").withStyle(ChatFormatting.WHITE))
            .append(Component.literal("a smell of ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("freshly washed clothes").withStyle(ChatFormatting.WHITE))
            .append(Component.literal(" and ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("clean air").withStyle(ChatFormatting.WHITE))
            .append(Component.literal(".").withStyle(ChatFormatting.GRAY)));
      lore.add(Component.literal("")
            .append(Component.literal("The ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("charm ").withStyle(ChatFormatting.AQUA))
            .append(Component.literal("will periodically ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("remove ").withStyle(ChatFormatting.WHITE))
            .append(Component.literal("one ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("negative effect").withStyle(ChatFormatting.WHITE))
            .append(Component.literal(".").withStyle(ChatFormatting.GRAY)));
      lore.add(Component.literal("")
            .append(Component.literal("Sneak Right Click").withStyle(ChatFormatting.WHITE))
            .append(Component.literal(" to toggle the ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("charm's").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(" ability.").withStyle(ChatFormatting.GRAY)));
      
      return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   public List<List<Component>> getBookLore(){
      List<List<Component>> list = new ArrayList<>();
      list.add(List.of(Component.literal("Charm of Cleansing").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD), Component.literal("\nRarity: ").withStyle(ChatFormatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(),false)), Component.literal("\nBy coalescing the cleansing effects of milk and honey into a pure carbon and silica matrix, I have made their effects renewable. \n\nWhile active, the Charm will cleanse a negative  ").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("Charm of Cleansing").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD), Component.literal("\neffect when it is applied, or a currently active effect. \n\nThis ability takes about a minute to recharge.\n\nSneak Use the Charm to toggle its effect.\n").withStyle(ChatFormatting.BLACK)));
      return list;
   }
   
   @Override
   public Vec3 getBaseRange(){
      return new Vec3(15,15,15);
   }
   
   @Override
   public void steleTick(ServerLevel world, GeomanticSteleBlockEntity stele, ItemStack stack, Vec3 range){
      AABB box = new AABB(stele.getBlockPos().getCenter().subtract(range), stele.getBlockPos().getCenter().add(range));
      Vec3 stackPos = stele.getBlockPos().getCenter().add(0, 1, 0);
      
      List<LivingEntity> inRangeEntities = world.getEntitiesOfClass(LivingEntity.class,box);
      for(LivingEntity living : inRangeEntities){
         if(cleanseEffect(living,stack) && living instanceof ServerPlayer player){
            stele.giveXP(ArcanaNovum.CONFIG.getInt(ArcanaConfig.XP_CLEANSING_CHARM_CLEANSE));
            stele.setChanged();
         }
      }
      
      if(world.getServer().getTickCount() % 20 == 0){
         addEnergy(stack, -5); // Recharge
         stele.setChanged();
      }
      
      if(world.random.nextFloat() < 0.35){
         int rgb = Color.HSBtoRGB(world.random.nextFloat(), 0.5f, 1.0f);
         float r = ((rgb >> 16) & 0xFF) / 255.0f;
         float g = ((rgb >> 8) & 0xFF) / 255.0f;
         float b = (rgb & 0xFF) / 255.0f;
         ColorParticleOption particle = ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, r, g, b);
         world.sendParticles(particle,stackPos.x(),stackPos.y(),stackPos.z(),2,0.25,0.25,0.25,.0);
      }
   }
   
   public class CleansingCharmItem extends ArcanaPolymerItem {
      public CleansingCharmItem(){
         super(getThis());
      }
      
      @Override
      public ItemStack getPolymerItemStack(ItemStack itemStack, TooltipFlag tooltipType, PacketContext context){
         ItemStack baseStack = super.getPolymerItemStack(itemStack, tooltipType, context);
         if(!ArcanaItemUtils.isArcane(itemStack)) return baseStack;
         boolean active = getBooleanProperty(itemStack,ACTIVE_TAG);
         
         List<String> stringList = new ArrayList<>();
         if(active){
            stringList.add("on");
         }else{
            stringList.add("off");
         }
         baseStack.set(DataComponents.CUSTOM_MODEL_DATA,new CustomModelData(new ArrayList<>(),new ArrayList<>(),stringList,new ArrayList<>()));
         return baseStack;
      }
      
      @Override
      public ItemStack getDefaultInstance(){
         return prefItem;
      }
      
      @Override
      public void inventoryTick(@NonNull ItemStack stack, @NonNull ServerLevel world, @NonNull Entity entity, @Nullable EquipmentSlot slot){
         if(!ArcanaItemUtils.isArcane(stack)) return;
         if(!(entity instanceof ServerPlayer player)) return;
         
         if(getBooleanProperty(stack,ACTIVE_TAG)) cleanseEffect(player,stack);
         
         if(world.getServer().getTickCount() % 20 == 0){
            addEnergy(stack, -1); // Recharge
         }
      }
      
      @Override
      public @NonNull InteractionResult use(@NonNull Level world, Player playerEntity, @NonNull InteractionHand hand){
         ItemStack stack = playerEntity.getItemInHand(hand);
         if(!(playerEntity instanceof ServerPlayer player)) return InteractionResult.PASS;
         
         if(player.isShiftKeyDown()){
            boolean active = !getBooleanProperty(stack,ACTIVE_TAG);
            putProperty(stack,ACTIVE_TAG,active);
            
            if(active){
               player.displayClientMessage(Component.literal("The Charm glows with iridescence").withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC),true);
               SoundUtils.playSongToPlayer(player, SoundEvents.BEACON_POWER_SELECT, 0.5f,2f);
            }else{
               player.displayClientMessage(Component.literal("The Charm's glow fades").withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC),true);
               SoundUtils.playSongToPlayer(player, SoundEvents.BEACON_DEACTIVATE, 0.5f,.8f);
            }
            
            return InteractionResult.SUCCESS_SERVER;
         }
         
         return InteractionResult.PASS;
      }
   }
}
