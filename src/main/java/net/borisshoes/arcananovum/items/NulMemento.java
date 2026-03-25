package net.borisshoes.arcananovum.items;

import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.borisshoes.arcananovum.ArcanaConfig;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugment;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.core.EnergyItem;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerItem;
import net.borisshoes.arcananovum.damage.ArcanaDamageTypes;
import net.borisshoes.arcananovum.events.NulMementoEvent;
import net.borisshoes.arcananovum.events.special.CeptyusOpenEvent;
import net.borisshoes.arcananovum.gui.arcanetome.ArcaneTomeGui;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.*;
import net.borisshoes.borislib.BorisLib;
import net.borisshoes.borislib.conditions.ConditionInstance;
import net.borisshoes.borislib.conditions.Conditions;
import net.borisshoes.borislib.events.Event;
import net.borisshoes.borislib.timers.GenericTimer;
import net.borisshoes.borislib.utils.MinecraftUtils;
import net.borisshoes.borislib.utils.SoundUtils;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityEvent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.equipment.ArmorMaterials;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.Equippable;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;
import static net.borisshoes.arcananovum.ArcanaRegistry.arcanaId;

public class NulMemento extends EnergyItem {
	public static final String ID = "nul_memento";
   
   public static final String HEAD_TAG = "onHead";
   private static final Item textureItem = Items.TINTED_GLASS;
   
   public NulMemento(){
      id = ID;
      name = "Nul Memento";
      rarity = ArcanaRarity.DIVINE;
      categories = new ArcaneTomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), ArcaneTomeGui.TomeFilter.EQUIPMENT};
      vanillaItem = Items.WITHER_SKELETON_SKULL;
      item = new NulMementoItem();
      displayName = Component.translatableWithFallback("item."+MOD_ID+"."+ID,name).withStyle(ChatFormatting.BOLD).withColor(ArcanaColors.NUL_COLOR);
      researchTasks = new ResourceKey[]{ResearchTasks.OBTAIN_DIVINE_CATALYST,ResearchTasks.KILL_CONSTRUCT};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getDefaultMaxStackSize());
      putProperty(stack,ACTIVE_TAG,false);
      putProperty(stack,HEAD_TAG,false);
      setPrefStack(stack);
   }
   
   @Override
   public void finalizePrefItem(MinecraftServer server){
      super.finalizePrefItem(server);
      ItemStack curPrefItem = this.getPrefItem();
      curPrefItem.set(DataComponents.ENCHANTMENTS, MinecraftUtils.makeEnchantComponent(
            new EnchantmentInstance(MinecraftUtils.getEnchantment(server.registryAccess(), Enchantments.PROTECTION),4)
      ));
      this.prefItem = buildItemLore(curPrefItem, server);
   }
   
   @Override
   public List<Component> getItemLore(@Nullable ItemStack itemStack){
      List<MutableComponent> lore = new ArrayList<>();
      lore.add(Component.literal("")
            .append(Component.literal("A strange, ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("withered skull").withStyle(ChatFormatting.GRAY))
            .append(Component.literal(" with a distinctive gash.").withStyle(ChatFormatting.DARK_GRAY)));
      lore.add(Component.literal("")
            .append(Component.literal("The ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("Aspect of Death").withStyle(ChatFormatting.BLUE))
            .append(Component.literal(" has granted you his ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("favor").withStyle(ChatFormatting.GRAY))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_GRAY)));
      lore.add(Component.literal(""));
      lore.add(Component.literal("")
            .append(Component.literal("You have ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("seen ").withStyle(ChatFormatting.BLUE))
            .append(Component.literal("things that most ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("Players ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("never will.").withStyle(ChatFormatting.DARK_GRAY)));
      lore.add(Component.literal("")
            .append(Component.literal("The ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("curse of knowledge").withStyle(ChatFormatting.BLUE))
            .append(Component.literal(" binds ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("tighter ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("than any other.").withStyle(ChatFormatting.DARK_GRAY)));
      lore.add(Component.literal("")
            .append(Component.literal("There are some ").withStyle(ChatFormatting.ITALIC, ChatFormatting.DARK_GRAY))
            .append(Component.literal("Skills ").withStyle(ChatFormatting.ITALIC, ChatFormatting.BLUE))
            .append(Component.literal("that are better left ").withStyle(ChatFormatting.ITALIC, ChatFormatting.DARK_GRAY))
            .append(Component.literal("forgotten").withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY))
            .append(Component.literal("...").withStyle(ChatFormatting.ITALIC, ChatFormatting.DARK_GRAY)));
      lore.add(Component.literal(""));
      lore.add(Component.literal("")
            .append(Component.literal("It ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("calls ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("to your ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("mind ").withStyle(ChatFormatting.BLUE))
            .append(Component.literal("with a familiar ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("burn of concentration").withStyle(ChatFormatting.DARK_RED))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_GRAY)));
      lore.add(Component.literal("")
            .append(Component.literal("It ").withStyle(ChatFormatting.ITALIC, ChatFormatting.DARK_GRAY))
            .append(Component.literal("yearns ").withStyle(ChatFormatting.ITALIC, ChatFormatting.BLUE))
            .append(Component.literal("to be worn... To protect you from ").withStyle(ChatFormatting.ITALIC, ChatFormatting.DARK_GRAY))
            .append(Component.literal("death").withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY))
            .append(Component.literal("...").withStyle(ChatFormatting.ITALIC, ChatFormatting.DARK_GRAY)));
      lore.add(Component.literal(""));
      lore.add(Component.literal("")
            .append(Component.literal("When ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("activated").withStyle(ChatFormatting.GRAY))
            .append(Component.literal(", all ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("Skill Points").withStyle(ChatFormatting.BLUE))
            .append(Component.literal(" will be ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("deallocated").withStyle(ChatFormatting.GRAY))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_GRAY)));
      
      if(itemStack != null){
         int energy = getEnergy(itemStack);
         if(energy == 0){
            lore.add(Component.literal(""));
            lore.add(Component.literal("")
                  .append(Component.literal("Nul's Ward").withStyle(ChatFormatting.GRAY))
                  .append(Component.literal(" - ").withStyle(ChatFormatting.DARK_GRAY))
                  .append(Component.literal("Ready").withStyle(ChatFormatting.BLUE)));
         }else{
            energy /= 20;
            String duration;
            if(energy >= 100){
               duration = ((energy/60)+1)+" Minutes";
            }else{
               duration = energy+" Seconds";
            }
            lore.add(Component.literal(""));
            lore.add(Component.literal("")
                  .append(Component.literal("Nul's Ward").withStyle(ChatFormatting.GRAY))
                  .append(Component.literal(" - ").withStyle(ChatFormatting.DARK_GRAY))
                  .append(Component.literal("Recharging: ").withStyle(ChatFormatting.BLUE))
                  .append(Component.literal(duration).withStyle(ChatFormatting.DARK_GRAY)));
         }
      }
      
      return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   public ItemStack onAugment(ItemStack stack, ArcanaAugment augment, int level){
      if(ArcanaItemUtils.identifyItem(stack) instanceof NulMemento && augment == ArcanaAugments.DEATHS_CHAMPION && level >= 1){
         EnhancedStatUtils.enhanceItem(stack,1);
      }
      return stack;
   }
   
   @Override
   public int getMaxEnergy(ItemStack item){
      int baseCooldown = ArcanaNovum.CONFIG.getInt(ArcanaConfig.NUL_MEMENTO_WARD_COOLDOWN);
      int cooldownReduction = ArcanaNovum.CONFIG.getIntList(ArcanaConfig.NUL_MEMENTO_WARD_COOLDOWN_PER_LVL).get(ArcanaAugments.getAugmentOnItem(item,ArcanaAugments.TEMPO_MORTUUS));
      return Math.max(1, baseCooldown - cooldownReduction);
   }
   
   public boolean protectFromDeath(ItemStack stack, LivingEntity living, DamageSource source, boolean constructInterference){
      if(source.is(DamageTypeTags.BYPASSES_INVULNERABILITY) && !source.is(ArcanaDamageTypes.VENGEANCE_TOTEM) && !source.is(ArcanaDamageTypes.CONCENTRATION)){
         return false;
      }
      if(getEnergy(stack) > 0 && !isActive(stack)){
         return false;
      }
      
      if(living.level() instanceof ServerLevel world){
         world.sendParticles(ParticleTypes.LARGE_SMOKE,living.getX(),living.getY()+living.getBbHeight()/2,living.getZ(),100,.4,.4,.4,0.07);
      }
      
      if(living instanceof ServerPlayer player){
         DialogHelper dialogHelper = new DialogHelper();
         
         if(isActive(stack)){
            ArcanaAchievements.grant(player,ArcanaAchievements.DEATHS_DOOR);
            
            dialogHelper.sendDialog(List.of(player),new Dialog(new ArrayList<>(Arrays.asList(
                  Component.literal("\n")
                        .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                        .append(Component.literal("Nul").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
                        .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD)),
                  Component.literal("")
                        .append(Component.literal("Now is no time to die on me. Pull yourself together!").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC))
            )),new ArrayList<>(Arrays.asList(new Dialog.DialogSound(SoundEvents.WITHER_AMBIENT,0.3f,1.4f))),new int[]{},1,1,-1),true);
            living.setHealth(1.0f);
            living.addEffect(new MobEffectInstance(ArcanaRegistry.DEATH_WARD_EFFECT, 600, 0));
            return true;
         }
         
         if(!constructInterference && Event.getEventsOfType(CeptyusOpenEvent.class).stream().noneMatch(c -> c.getPlayer().equals(player))){
            DialogHelper.sendDialog(List.of(player),new Dialog(new ArrayList<>(Arrays.asList(
                  Component.literal("\n")
                        .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                        .append(Component.literal("Nul").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
                        .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD)),
                  Component.literal("")
                        .append(Component.literal("Let my gift offer you a second chance.").withStyle(ChatFormatting.DARK_GRAY))
            )),new ArrayList<>(Arrays.asList(new Dialog.DialogSound(SoundEvents.WITHER_AMBIENT,0.3f,0.7f))),new int[]{},1,1,-1),true);
         }
         ArcanaNovum.data(player).addXP(ArcanaNovum.CONFIG.getInt(ArcanaConfig.XP_NUL_MEMENTO_PROTECT));
      }
      
      setEnergy(stack,getMaxEnergy(stack));
      
      living.setHealth(1.0f);
      living.removeAllEffects();
      living.addEffect(new MobEffectInstance(ArcanaRegistry.DEATH_WARD_EFFECT, constructInterference ? 300/2 : 300, 0));
      living.addEffect(new MobEffectInstance(ArcanaRegistry.GREATER_INVISIBILITY_EFFECT, 100, 0));
      living.level().broadcastEntityEvent(living, EntityEvent.PROTECTED_FROM_DEATH);
      return true;
   }
   
   public boolean isActive(ItemStack stack){
      return getBooleanProperty(stack,ACTIVE_TAG);
   }
   
   public void forgor(ItemStack stack, ServerPlayer player){
      putProperty(stack,ACTIVE_TAG,true);
      
      int increments = 100;
      ConditionInstance nearsight = new ConditionInstance(Conditions.NEARSIGHT,arcanaId(ID),increments*5,1.0f,false,true,true, AttributeModifier.Operation.ADD_VALUE,null);
      MobEffectInstance slow = new MobEffectInstance(MobEffects.SLOWNESS, increments*5, 9, false, false, true);
      MobEffectInstance fatigue = new MobEffectInstance(MobEffects.MINING_FATIGUE, increments*5 , 4, false, false, true);
      MobEffectInstance weakness = new MobEffectInstance(MobEffects.WEAKNESS,increments*5 , 4, false, false, true);
      Conditions.addCondition(player.level().getServer(),player,nearsight);
      player.addEffect(slow);
      player.addEffect(fatigue);
      player.addEffect(weakness);
      
      final boolean[] cont = {true};
      int resolve = ArcanaNovum.data(player).getAugmentLevel(ArcanaAugments.RESOLVE);
      final int maxConc = LevelUtils.concFromXp(ArcanaNovum.data(player).getXP(),resolve);
      DialogHelper dialogHelper = new DialogHelper();
      
      dialogHelper.sendDialog(List.of(player),new Dialog(new ArrayList<>(Arrays.asList(
            Component.literal("\n\n\n\n")
                  .append(Component.literal("As the crushing weight of ").withStyle(ChatFormatting.DARK_GRAY))
                  .append(Component.literal("concentration").withStyle(ChatFormatting.RED))
                  .append(Component.literal(" takes your mind you hear the ").withStyle(ChatFormatting.DARK_GRAY))
                  .append(Component.literal("Nul Memento").withStyle(ChatFormatting.BOLD).withColor(ArcanaColors.NUL_COLOR))
                  .append(Component.literal(" whisper...")).withStyle(ChatFormatting.DARK_GRAY)
      )),new ArrayList<>(Arrays.asList(new Dialog.DialogSound(SoundEvents.WITHER_AMBIENT,0.3f,0.7f))),new int[]{},1,1,-1),false);
      Event.addEvent(new NulMementoEvent(increments,player,stack));
      
      BorisLib.addTickTimerCallback(new GenericTimer(increments*1, () -> {
         if(cont[0]){
            ItemStack headStack = player.getItemBySlot(EquipmentSlot.HEAD);
            if(!(ArcanaItemUtils.identifyItem(headStack) instanceof NulMemento) || !(ArcanaItemUtils.getUsedConcentration(player) > maxConc)){
               cont[0] = false;
               processHalted(player);
            }else{
               Event.addEvent(new NulMementoEvent(increments,player,stack));
               dialogHelper.sendDialog(List.of(player),new Dialog(new ArrayList<>(Arrays.asList(
                     Component.literal("\n\n\n\n")
                           .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                           .append(Component.literal("Nul").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
                           .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                           .append(Component.literal("\nFeel the weight, embrace it... let me in...").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC))
               )),new ArrayList<>(Arrays.asList(new Dialog.DialogSound(SoundEvents.WITHER_AMBIENT,0.3f,0.7f))),new int[]{},1,1,-1),true);
            }
         }
      }));
      BorisLib.addTickTimerCallback(new GenericTimer(increments*2, () -> {
         if(cont[0]){
            ItemStack headStack = player.getItemBySlot(EquipmentSlot.HEAD);
            if(!(ArcanaItemUtils.identifyItem(headStack) instanceof NulMemento) || !(ArcanaItemUtils.getUsedConcentration(player) > maxConc)){
               cont[0] = false;
               processHalted(player);
            }else{
               Event.addEvent(new NulMementoEvent(increments,player,stack));
               dialogHelper.sendDialog(List.of(player),new Dialog(new ArrayList<>(Arrays.asList(
                     Component.literal("\n\n\n\n")
                           .append(Component.literal("You feel as though your ").withStyle(ChatFormatting.DARK_GRAY))
                           .append(Component.literal("skull").withStyle(ChatFormatting.GRAY))
                           .append(Component.literal(" is about to ").withStyle(ChatFormatting.DARK_GRAY))
                           .append(Component.literal("collapse").withStyle(ChatFormatting.RED))
                           .append(Component.literal(" when an ").withStyle(ChatFormatting.DARK_GRAY))
                           .append(Component.literal("encouraging voice").withStyle(ChatFormatting.GRAY))
                           .append(Component.literal(" lifts you.").withStyle(ChatFormatting.DARK_GRAY))
               )),new ArrayList<>(Arrays.asList(new Dialog.DialogSound(SoundEvents.WITHER_AMBIENT,0.3f,0.7f))),new int[]{},1,1,-1),false);
            }
         }
      }));
      BorisLib.addTickTimerCallback(new GenericTimer(increments*3, () -> {
         if(cont[0]){
            ItemStack headStack = player.getItemBySlot(EquipmentSlot.HEAD);
            if(!(ArcanaItemUtils.identifyItem(headStack) instanceof NulMemento) || !(ArcanaItemUtils.getUsedConcentration(player) > maxConc)){
               cont[0] = false;
               processHalted(player);
            }else{
               Event.addEvent(new NulMementoEvent(increments,player,stack));
               dialogHelper.sendDialog(List.of(player),new Dialog(new ArrayList<>(Arrays.asList(
                     Component.literal("\n\n\n\n")
                           .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                           .append(Component.literal("Nul").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
                           .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                           .append(Component.literal("\nYour secrets are safe with me. Be free of this burden, for I now bear it alone.").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC))
               )),new ArrayList<>(Arrays.asList(new Dialog.DialogSound(SoundEvents.WITHER_AMBIENT,0.3f,0.7f))),new int[]{},1,1,-1),true);
            }
         }
      }));
      BorisLib.addTickTimerCallback(new GenericTimer(increments*4, () -> {
         if(cont[0]){
            ItemStack headStack = player.getItemBySlot(EquipmentSlot.HEAD);
            if(!(ArcanaItemUtils.identifyItem(headStack) instanceof NulMemento) || !(ArcanaItemUtils.getUsedConcentration(player) > maxConc)){
               cont[0] = false;
               processHalted(player);
            }else{
               Event.addEvent(new NulMementoEvent(increments,player,stack));
               dialogHelper.sendDialog(List.of(player),new Dialog(new ArrayList<>(Arrays.asList(
                     Component.literal("\n\n\n\n")
                           .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                           .append(Component.literal("Nul").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
                           .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                           .append(Component.literal("\nThat is, until we meet again...").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC))
               )),new ArrayList<>(Arrays.asList(new Dialog.DialogSound(SoundEvents.WITHER_AMBIENT,0.3f,0.7f))),new int[]{},1,1,-1),true);
            }
         }
      }));
      BorisLib.addTickTimerCallback(new GenericTimer(increments*5, () -> {
         if(cont[0]){
            ItemStack headStack = player.getItemBySlot(EquipmentSlot.HEAD);
            if(!(ArcanaItemUtils.identifyItem(headStack) instanceof NulMemento) || !(ArcanaItemUtils.getUsedConcentration(player) > maxConc)){
               cont[0] = false;
               processHalted(player);
            }else{
               Event.addEvent(new NulMementoEvent(increments,player,stack));
               dialogHelper.sendDialog(List.of(player),new Dialog(new ArrayList<>(Arrays.asList(
                     Component.literal("\n\n\n\n")
                           .append(Component.literal("The ").withStyle(ChatFormatting.DARK_GRAY))
                           .append(Component.literal("Nul Memento").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                           .append(Component.literal(" crumbles ").withStyle(ChatFormatting.GRAY))
                           .append(Component.literal("into").withStyle(ChatFormatting.DARK_GRAY))
                           .append(Component.literal(" ash ").withStyle(ChatFormatting.GRAY))
                           .append(Component.literal("around your head, your mind still ").withStyle(ChatFormatting.DARK_GRAY))
                           .append(Component.literal("burning").withStyle(ChatFormatting.RED))
                           .append(Component.literal(" from the ").withStyle(ChatFormatting.DARK_GRAY))
                           .append(Component.literal("overwhelming ").withStyle(ChatFormatting.RED))
                           .append(Component.literal("Arcana.").withStyle(ChatFormatting.LIGHT_PURPLE))
               )),new ArrayList<>(Arrays.asList(new Dialog.DialogSound(SoundEvents.WITHER_AMBIENT,0.3f,0.7f))),new int[]{},1,1,-1),false);
               
               MobEffectInstance nausea = new MobEffectInstance(MobEffects.NAUSEA,200, 4, false, false, true);
               MobEffectInstance ward = new MobEffectInstance(ArcanaRegistry.DEATH_WARD_EFFECT,increments*2, 0, false, false, true);
               player.addEffect(nausea);
               player.addEffect(ward);
               headStack.shrink(headStack.getCount());
               SoundUtils.playSongToPlayer(player, SoundEvents.ZOMBIE_VILLAGER_CURE, 1f, 1f);
            }
         }
      }));
      BorisLib.addTickTimerCallback(new GenericTimer(increments*6, () -> {
         if(cont[0]){
            Event.addEvent(new NulMementoEvent(increments,player,stack));
            dialogHelper.sendDialog(List.of(player),new Dialog(new ArrayList<>(Arrays.asList(
                  Component.literal("\n\n\n\n")
                        .append(Component.literal("All of your Skill Points have been deallocated.").withStyle(ChatFormatting.AQUA))
            )),new ArrayList<>(Arrays.asList(new Dialog.DialogSound(SoundEvents.WITHER_AMBIENT,0.3f,0.7f))),new int[]{},1,1,-1),false);
            
            ArcanaNovum.data(player).removeAllAugments();
            ArcanaNovum.data(player).setCanAttemptCeptyus(true);
            ArcanaNovum.data(player).addXP(ArcanaNovum.CONFIG.getInt(ArcanaConfig.XP_NUL_MEMENTO_DEALLOCATE));
            ArcanaAchievements.grant(player,ArcanaAchievements.LOST_KNOWLEDGE);
            ArcanaAchievements.progress(player,ArcanaAchievements.AMNESIAC,1);
         }
      }));
   }
   
   private void processHalted(ServerPlayer player){
      player.displayClientMessage(Component.literal(""),false);
      player.displayClientMessage(Component.literal(""),false);
      player.displayClientMessage(Component.literal(""),false);
      player.displayClientMessage(Component.literal("")
            .append(Component.literal("The weight of the ").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC))
            .append(Component.literal("Nul Memento").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD, ChatFormatting.ITALIC))
            .append(Component.literal(" becomes too much to bare, perhaps you aren't ready...").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)), false);
      player.removeAllEffects();
   }
   
   public void inventoryDialog(ServerPlayer player){
      ArrayList<Dialog> dialogOptions = new ArrayList<>();
      // Conditions: 0 - Crafted Wings, 1 - Crafted Aequalis, 2 - Has Ceptyus Pickaxe, 3 - Has Aequalis, 4 - Has Egg, 5 - Has Greaves, 6 - Has Spear
      boolean[] conditions = new boolean[]{
            ArcanaNovum.data(player).hasCrafted(ArcanaRegistry.WINGS_OF_ENDERIA),
            ArcanaNovum.data(player).hasCrafted(ArcanaRegistry.AEQUALIS_SCIENTIA),
            ArcanaItemUtils.hasItemInInventory(player,ArcanaRegistry.PICKAXE_OF_CEPTYUS.getItem()),
            ArcanaItemUtils.hasItemInInventory(player,ArcanaRegistry.AEQUALIS_SCIENTIA.getItem()),
            ArcanaItemUtils.hasItemInInventory(player, Items.DRAGON_EGG),
            ArcanaItemUtils.hasItemInInventory(player,ArcanaRegistry.GREAVES_OF_GAIALTUS.getItem()),
            ArcanaItemUtils.hasItemInInventory(player,ArcanaRegistry.SPEAR_OF_TENBROUS.getItem()),
      };
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Component.literal("\n")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("Nul").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD)),
            Component.literal("")
                  .append(Component.literal("My Chosen... Will you continue show the courage that your peers lack?").withStyle(ChatFormatting.DARK_GRAY))
      )),new ArrayList<>(Arrays.asList(new Dialog.DialogSound(SoundEvents.WITHER_AMBIENT,0.3f,0.7f))),new int[]{},1,1,0b0));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Component.literal("\n")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("Nul").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD)),
            Component.literal("")
                  .append(Component.literal("My dear Player, will you help us revitalize these realms to new heights?").withStyle(ChatFormatting.DARK_GRAY))
      )),new ArrayList<>(Arrays.asList(new Dialog.DialogSound(SoundEvents.WITHER_AMBIENT,0.3f,0.7f))),new int[]{},1,1,0b0));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Component.literal("\n")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("Nul").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD)),
            Component.literal("")
                  .append(Component.literal("Everything faces me once... Few have ever faced me twice.").withStyle(ChatFormatting.DARK_GRAY))
      )),new ArrayList<>(Arrays.asList(new Dialog.DialogSound(SoundEvents.WITHER_AMBIENT,0.3f,0.7f))),new int[]{},1,1,0b0));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Component.literal("\n")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("Nul").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD)),
            Component.literal("")
                  .append(Component.literal("Take care in the dark secrets you seek. For only I can ever take them away.").withStyle(ChatFormatting.DARK_GRAY))
      )),new ArrayList<>(Arrays.asList(new Dialog.DialogSound(SoundEvents.WITHER_AMBIENT,0.3f,0.7f))),new int[]{},1,1,0b0));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Component.literal("\n")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("Nul").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD)),
            Component.literal("")
                  .append(Component.literal("What do you think of my Sister's realm? Do you see the need for my mission now?").withStyle(ChatFormatting.DARK_GRAY))
      )),new ArrayList<>(Arrays.asList(new Dialog.DialogSound(SoundEvents.WITHER_AMBIENT,0.3f,0.7f))),new int[]{},0,1,0b1));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Component.literal("\n")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("Nul").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD)),
            Component.literal("")
                  .append(Component.literal("So you've met my kin, Equayus? What sort of exchange did you have?").withStyle(ChatFormatting.DARK_GRAY))
      )),new ArrayList<>(Arrays.asList(new Dialog.DialogSound(SoundEvents.WITHER_AMBIENT,0.3f,0.7f))),new int[]{},0,1,0b10));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Component.literal("\n")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("Nul").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD)),
            Component.literal("")
                  .append(Component.literal("I sense my kin's Arcana on you... I trust your deal was worthwhile.").withStyle(ChatFormatting.DARK_GRAY))
      )),new ArrayList<>(Arrays.asList(new Dialog.DialogSound(SoundEvents.WITHER_AMBIENT,0.3f,0.7f))),new int[]{},0,1,0b10));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Component.literal("\n")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("Nul").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD)),
            Component.literal("")
                  .append(Component.literal("Equayus and I might not always see eye to eye, but we both have these realms' best intentions at heart.").withStyle(ChatFormatting.DARK_GRAY))
      )),new ArrayList<>(Arrays.asList(new Dialog.DialogSound(SoundEvents.WITHER_AMBIENT,0.3f,0.7f))),new int[]{},0,1,0b10));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Component.literal("\n")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("Nul").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD)),
            Component.literal("")
                  .append(Component.literal("Your ingenuity in acquiring that Pick did not go unnoticed. But remember that it was my gift that saved you.").withStyle(ChatFormatting.DARK_GRAY))
      )),new ArrayList<>(Arrays.asList(new Dialog.DialogSound(SoundEvents.WITHER_AMBIENT,0.3f,0.7f))),new int[]{},0,1,0b100));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Component.literal("\n")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("Nul").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD)),
            Component.literal("")
                  .append(Component.literal("Gaialtus turned its back on this world and left it to fall to ruin. What good is a creator that abandons their creation?").withStyle(ChatFormatting.DARK_GRAY))
      )),new ArrayList<>(Arrays.asList(new Dialog.DialogSound(SoundEvents.WITHER_AMBIENT,0.3f,0.7f))),new int[]{},0,1,0b100000));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Component.literal("\n")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("Nul").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD)),
            Component.literal("")
                  .append(Component.literal("If Gaialtus was still here, none of my work would be necessary. Yet here I am, picking up its slack to maintain peace.").withStyle(ChatFormatting.DARK_GRAY))
      )),new ArrayList<>(Arrays.asList(new Dialog.DialogSound(SoundEvents.WITHER_AMBIENT,0.3f,0.7f))),new int[]{},0,1,0b100000));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Component.literal("\n")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("Nul").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("\nGaialtus's negligence is responsible for the decay that has befallen our world. Don't let those trousers sway your mind.\n").withStyle(ChatFormatting.DARK_GRAY)),
            Component.literal("")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                  .append(Component.literal("Equayus").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                  .append(Component.literal("\nBrother, you know not of what you speak. Decay is sometimes a necessary precursor to greater regrowth.").withStyle(ChatFormatting.AQUA))
      )),new ArrayList<>(Arrays.asList(
            new Dialog.DialogSound(SoundEvents.WITHER_AMBIENT,0.3f,0.7f),
            new Dialog.DialogSound(SoundEvents.ALLAY_AMBIENT_WITHOUT_ITEM,0.5f,0.7f))
      ),new int[]{0,90},0,1,0b101000));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Component.literal("\n")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("Nul").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("\nEquayus? Have you also taken an interest in this Player?\n").withStyle(ChatFormatting.DARK_GRAY)),
            Component.literal("")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                  .append(Component.literal("Equayus").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                  .append(Component.literal("\nBrother, it is always good to hear your voice again...").withStyle(ChatFormatting.AQUA))
      )),new ArrayList<>(Arrays.asList(
            new Dialog.DialogSound(SoundEvents.WITHER_AMBIENT,0.3f,0.7f),
            new Dialog.DialogSound(SoundEvents.ALLAY_AMBIENT_WITHOUT_ITEM,0.5f,0.7f))
      ),new int[]{0,60},0,1,0b1000));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Component.literal("\n")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("Nul").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD)),
            Component.literal("")
                  .append(Component.literal("That Spear you wield harnesses shadow Arcana far darker than anything I have seen, It's almost frightening. Be cautious with it.").withStyle(ChatFormatting.DARK_GRAY))
      )),new ArrayList<>(Arrays.asList(new Dialog.DialogSound(SoundEvents.WITHER_AMBIENT,0.3f,0.7f))),new int[]{},0,1,0b1000000));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Component.literal("\n")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("Nul").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD)),
            Component.literal("")
                  .append(Component.literal("I never knew such vitriol could come from a mere Spear. Tenbrous must've been quite powerful.").withStyle(ChatFormatting.DARK_GRAY))
      )),new ArrayList<>(Arrays.asList(new Dialog.DialogSound(SoundEvents.WITHER_AMBIENT,0.3f,0.7f))),new int[]{},0,1,0b1000000));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Component.literal("\n")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("Nul").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("\nYou carry an intriguing weapon... I know little of Tenbrous, I wish we had the chance to meet.\n").withStyle(ChatFormatting.DARK_GRAY)),
            Component.literal("")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("Enderia").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("\nNo one will have to meet that monster EVER again! I made sure of it!\n").withStyle(ChatFormatting.DARK_PURPLE)),
            Component.literal("")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("Nul").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("\nAre you sure whatever you did was truly permanent? Such a shame all that power went to waste on you...").withStyle(ChatFormatting.DARK_GRAY))
      )),new ArrayList<>(Arrays.asList(
            new Dialog.DialogSound(SoundEvents.WITHER_AMBIENT,0.3f,0.7f),
            new Dialog.DialogSound(SoundEvents.ENDER_DRAGON_GROWL,0.5f,1.4f),
            new Dialog.DialogSound(SoundEvents.WITHER_AMBIENT,0.3f,0.7f))
      ),new int[]{0,60,60},0,1,0b1010000));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Component.literal("\n")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("Nul").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("\nSo you too, have taken an interest in this Player now? Do you now share my vision?\n").withStyle(ChatFormatting.DARK_GRAY)),
            Component.literal("")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                  .append(Component.literal("Equayus").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                  .append(Component.literal("\nThis one is quite curious. I do believe they will help restore this world.\n").withStyle(ChatFormatting.AQUA)),
            Component.literal("")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("Nul").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("\nAt last, our goals united again.").withStyle(ChatFormatting.DARK_GRAY))
      )),new ArrayList<>(Arrays.asList(
            new Dialog.DialogSound(SoundEvents.WITHER_AMBIENT,0.3f,0.7f),
            new Dialog.DialogSound(SoundEvents.ALLAY_AMBIENT_WITHOUT_ITEM,0.5f,0.7f),
            new Dialog.DialogSound(SoundEvents.WITHER_AMBIENT,0.3f,1.2f))
      ),new int[]{0,60,60},0,1,0b1000));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Component.literal("\n")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("Nul").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("\nHeh heh heh, How does it feel to be carried around like a prize, dear Sister?\n").withStyle(ChatFormatting.DARK_GRAY)),
            Component.literal("")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("Enderia").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("\nNUL! YOU DID THIS TO ME! I WILL END YOU!\n").withStyle(ChatFormatting.DARK_PURPLE)),
            Component.literal("")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("Nul").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("\nIt was not my actions that led to this. This Player did it of their own power. Perhaps in time your arrogance will turn to humility.").withStyle(ChatFormatting.DARK_GRAY))
      )),new ArrayList<>(Arrays.asList(
            new Dialog.DialogSound(SoundEvents.WITHER_AMBIENT,0.3f,0.7f),
            new Dialog.DialogSound(SoundEvents.ENDER_DRAGON_GROWL,0.5f,1.4f),
            new Dialog.DialogSound(SoundEvents.WITHER_AMBIENT,0.3f,0.7f))
      ),new int[]{0,60,60},0,1,0b10000));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Component.literal("\n")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("Nul").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("\nI'm sorry it had to come to this, Sister. You had so long to realize your mistakes.\n").withStyle(ChatFormatting.DARK_GRAY)),
            Component.literal("")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("Enderia").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("\nGods do not make mistakes! Our mere thoughts become the laws of our realm!\n").withStyle(ChatFormatting.DARK_PURPLE)),
            Component.literal("")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("Nul").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("\nThen I suppose your mistake was defying Tenbrous, and ascending to take its place.\n").withStyle(ChatFormatting.DARK_GRAY)),
            Component.literal("")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("Enderia").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("\n...\n").withStyle(ChatFormatting.DARK_PURPLE)),
            Component.literal("")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("Nul").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("\nFood for thought, my Sister.").withStyle(ChatFormatting.DARK_GRAY))
      )),new ArrayList<>(Arrays.asList(
            new Dialog.DialogSound(SoundEvents.WITHER_AMBIENT,0.3f,0.7f),
            new Dialog.DialogSound(SoundEvents.ENDER_DRAGON_GROWL,0.5f,1.4f),
            new Dialog.DialogSound(SoundEvents.WITHER_AMBIENT,0.3f,0.7f),
            new Dialog.DialogSound(SoundEvents.ENDER_DRAGON_GROWL,0.1f,0.6f),
            new Dialog.DialogSound(SoundEvents.WITHER_AMBIENT,0.3f,0.7f))
      ),new int[]{0,80,100,80,60},0,1,0b10000));
      
      
      DialogHelper helper = new DialogHelper(dialogOptions,conditions);
      helper.sendDialog(List.of(player),helper.getWeightedResult(),true);
   }
   
   @Override
   public List<List<Component>> getBookLore(){
      List<List<Component>> list = new ArrayList<>();
      list.add(List.of(Component.literal("    Nul Memento").withStyle(ChatFormatting.BOLD).withColor(ArcanaColors.NUL_COLOR), Component.literal("\nRarity: ").withStyle(ChatFormatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(),false)), Component.literal("\nThis entity of Death that I have acquired a passing familiarity with is most intriguing. He wanted me to prove my fighting prowess by dueling his creation, and I believe I succeeded. I was gifted this strange ").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("    Nul Memento").withStyle(ChatFormatting.BOLD).withColor(ArcanaColors.NUL_COLOR), Component.literal("\nskull, and was informed that I have become one of his ‘chosen’.\nI’m not sure what to think of this. What machinations could a Deity of Death be planning such that he needs help from me? The Memento whispers to me every so often.").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("    Nul Memento").withStyle(ChatFormatting.BOLD).withColor(ArcanaColors.NUL_COLOR), Component.literal("\nI have come to learn the entity calls himself Nul, the God of Death and Knowledge. He speaks of Arcana, and secrets that I have yet to learn. He warns that one mind can only hold so much knowledge at one time. ").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("    Nul Memento").withStyle(ChatFormatting.BOLD).withColor(ArcanaColors.NUL_COLOR), Component.literal("\nHowever, he offers his aid in circumventing this limitation. \nThis Memento reacts to an overburdened mind when worn and will make me forget all of the skills I have learned. ").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("    Nul Memento").withStyle(ChatFormatting.BOLD).withColor(ArcanaColors.NUL_COLOR), Component.literal("\nAs long as I use those skills before forgetting them, I should be able to take advantage of new knowledge with a new limit to what I can learn.\nThe Memento also offers incredible protection, as if it ").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("    Nul Memento").withStyle(ChatFormatting.BOLD).withColor(ArcanaColors.NUL_COLOR), Component.literal("\nwere made of enchanted Netherite!\n\nNul himself stated that by wearing his Memento, he may be willing to spare me from death every now and again.").withStyle(ChatFormatting.BLACK)));
      return list;
   }
   
   public class NulMementoItem extends ArcanaPolymerItem {
      public NulMementoItem(){
         super(getThis(), getEquipmentArcanaItemComponents()
               .humanoidArmor(ArmorMaterials.NETHERITE, ArmorType.HELMET)
         );
      }
      
      @Override
      public Item getPolymerItem(ItemStack itemStack, PacketContext context){
         if(PolymerResourcePackUtils.hasMainPack(context)){
            return textureItem;
         }else{
            return super.getPolymerItem(itemStack, context);
         }
      }
      
      @Override
      public ItemStack getPolymerItemStack(ItemStack itemStack, TooltipFlag tooltipType, PacketContext context){
         ItemStack baseStack = super.getPolymerItemStack(itemStack, tooltipType, context);
         Equippable equippableComponent = baseStack.get(DataComponents.EQUIPPABLE);
         Equippable newComp = Equippable.builder(equippableComponent.slot()).setEquipSound(equippableComponent.equipSound()).build();
         baseStack.set(DataComponents.EQUIPPABLE,newComp);
         return baseStack;
      }
      
      @Override
      public void inventoryTick(ItemStack stack, ServerLevel world, Entity entity, @Nullable EquipmentSlot slot){
         if(!ArcanaItemUtils.isArcane(stack)) return;
         if(!(entity instanceof ServerPlayer player)) return;
         
         boolean nowOnHead = player.getItemBySlot(EquipmentSlot.HEAD).equals(stack);
         boolean wasOnHead = getBooleanProperty(stack,HEAD_TAG);
         if(nowOnHead != wasOnHead){
            putProperty(stack,HEAD_TAG,nowOnHead);
         }
         if(nowOnHead && getEnergy(stack) > 0){
            addEnergy(stack,-1);
            buildItemLore(stack,entity.level().getServer());
         }
         
         double dialogChance = ArcanaNovum.CONFIG.getDouble(ArcanaConfig.MEMENTO_DIALOG_CHANCE);
         if(Math.random() < dialogChance){
            inventoryDialog(player);
         }
      }
      
      @Override
      public ItemStack getDefaultInstance(){
         return prefItem;
      }
   }
}

