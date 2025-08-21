package net.borisshoes.arcananovum.items;

import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.borisshoes.arcananovum.ArcanaConfig;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugment;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.EnergyItem;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerItem;
import net.borisshoes.arcananovum.damage.ArcanaDamageTypes;
import net.borisshoes.arcananovum.events.NulMementoEvent;
import net.borisshoes.arcananovum.gui.arcanetome.TomeGui;
import net.borisshoes.arcananovum.items.normal.GraphicItems;
import net.borisshoes.arcananovum.items.normal.GraphicalItem;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaRecipe;
import net.borisshoes.arcananovum.recipes.arcana.ExplainIngredient;
import net.borisshoes.arcananovum.recipes.arcana.ExplainRecipe;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.*;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.EquippableComponent;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.equipment.ArmorMaterials;
import net.minecraft.item.equipment.EquipmentType;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class NulMemento extends EnergyItem {
	public static final String ID = "nul_memento";
   
   public static final String HEAD_TAG = "onHead";
   private static final Item textureItem = Items.TINTED_GLASS;
   
   public NulMemento(){
      id = ID;
      name = "Nul Memento";
      rarity = ArcanaRarity.DIVINE;
      categories = new TomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), TomeGui.TomeFilter.EQUIPMENT};
      vanillaItem = Items.WITHER_SKELETON_SKULL;
      item = new NulMementoItem();
      displayName = TextUtils.withColor(Text.translatableWithFallback("item."+MOD_ID+"."+ID,name).formatted(Formatting.BOLD),ArcanaColors.NUL_COLOR);
      researchTasks = new RegistryKey[]{ResearchTasks.OBTAIN_DIVINE_CATALYST,ResearchTasks.KILL_CONSTRUCT};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getMaxCount());
      putProperty(stack,ACTIVE_TAG,false);
      putProperty(stack,HEAD_TAG,false);
      setPrefStack(stack);
   }
   
   @Override
   public void finalizePrefItem(MinecraftServer server){
      super.finalizePrefItem(server);
      ItemStack curPrefItem = this.getPrefItem();
      curPrefItem.set(DataComponentTypes.ENCHANTMENTS, MiscUtils.makeEnchantComponent(
            new EnchantmentLevelEntry(MiscUtils.getEnchantment(server.getRegistryManager(),Enchantments.PROTECTION),4)
      ));
      this.prefItem = buildItemLore(curPrefItem, server);
   }
   
   @Override
   public List<Text> getItemLore(@Nullable ItemStack itemStack){
      List<MutableText> lore = new ArrayList<>();
      lore.add(Text.literal("")
            .append(Text.literal("A strange, ").formatted(Formatting.DARK_GRAY))
            .append(Text.literal("withered skull").formatted(Formatting.GRAY))
            .append(Text.literal(" with a distinctive gash.").formatted(Formatting.DARK_GRAY)));
      lore.add(Text.literal("")
            .append(Text.literal("The ").formatted(Formatting.DARK_GRAY))
            .append(Text.literal("Aspect of Death").formatted(Formatting.BLUE))
            .append(Text.literal(" has granted you his ").formatted(Formatting.DARK_GRAY))
            .append(Text.literal("favor").formatted(Formatting.GRAY))
            .append(Text.literal(".").formatted(Formatting.DARK_GRAY)));
      lore.add(Text.literal(""));
      lore.add(Text.literal("")
            .append(Text.literal("You have ").formatted(Formatting.DARK_GRAY))
            .append(Text.literal("seen ").formatted(Formatting.BLUE))
            .append(Text.literal("things that most ").formatted(Formatting.DARK_GRAY))
            .append(Text.literal("Players ").formatted(Formatting.GRAY))
            .append(Text.literal("never will.").formatted(Formatting.DARK_GRAY)));
      lore.add(Text.literal("")
            .append(Text.literal("The ").formatted(Formatting.DARK_GRAY))
            .append(Text.literal("curse of knowledge").formatted(Formatting.BLUE))
            .append(Text.literal(" binds ").formatted(Formatting.DARK_GRAY))
            .append(Text.literal("tighter ").formatted(Formatting.GRAY))
            .append(Text.literal("than any other.").formatted(Formatting.DARK_GRAY)));
      lore.add(Text.literal("")
            .append(Text.literal("There are some ").formatted(Formatting.ITALIC,Formatting.DARK_GRAY))
            .append(Text.literal("Skills ").formatted(Formatting.ITALIC,Formatting.BLUE))
            .append(Text.literal("that are better left ").formatted(Formatting.ITALIC,Formatting.DARK_GRAY))
            .append(Text.literal("forgotten").formatted(Formatting.ITALIC,Formatting.GRAY))
            .append(Text.literal("...").formatted(Formatting.ITALIC,Formatting.DARK_GRAY)));
      lore.add(Text.literal(""));
      lore.add(Text.literal("")
            .append(Text.literal("It ").formatted(Formatting.DARK_GRAY))
            .append(Text.literal("calls ").formatted(Formatting.GRAY))
            .append(Text.literal("to your ").formatted(Formatting.DARK_GRAY))
            .append(Text.literal("mind ").formatted(Formatting.BLUE))
            .append(Text.literal("with a familiar ").formatted(Formatting.DARK_GRAY))
            .append(Text.literal("burn of concentration").formatted(Formatting.DARK_RED))
            .append(Text.literal(".").formatted(Formatting.DARK_GRAY)));
      lore.add(Text.literal("")
            .append(Text.literal("It ").formatted(Formatting.ITALIC,Formatting.DARK_GRAY))
            .append(Text.literal("yearns ").formatted(Formatting.ITALIC,Formatting.BLUE))
            .append(Text.literal("to be worn... To protect you from ").formatted(Formatting.ITALIC,Formatting.DARK_GRAY))
            .append(Text.literal("death").formatted(Formatting.ITALIC,Formatting.GRAY))
            .append(Text.literal("...").formatted(Formatting.ITALIC,Formatting.DARK_GRAY)));
      lore.add(Text.literal(""));
      lore.add(Text.literal("")
            .append(Text.literal("When ").formatted(Formatting.DARK_GRAY))
            .append(Text.literal("activated").formatted(Formatting.GRAY))
            .append(Text.literal(", all ").formatted(Formatting.DARK_GRAY))
            .append(Text.literal("Skill Points").formatted(Formatting.BLUE))
            .append(Text.literal(" will be ").formatted(Formatting.DARK_GRAY))
            .append(Text.literal("deallocated").formatted(Formatting.GRAY))
            .append(Text.literal(".").formatted(Formatting.DARK_GRAY)));
      
      if(itemStack != null){
         int energy = getEnergy(itemStack);
         if(energy == 0){
            lore.add(Text.literal(""));
            lore.add(Text.literal("")
                  .append(Text.literal("Nul's Ward").formatted(Formatting.GRAY))
                  .append(Text.literal(" - ").formatted(Formatting.DARK_GRAY))
                  .append(Text.literal("Ready").formatted(Formatting.BLUE)));
         }else{
            energy /= 20;
            String duration;
            if(energy >= 100){
               duration = ((energy/60)+1)+" Minutes";
            }else{
               duration = energy+" Seconds";
            }
            lore.add(Text.literal(""));
            lore.add(Text.literal("")
                  .append(Text.literal("Nul's Ward").formatted(Formatting.GRAY))
                  .append(Text.literal(" - ").formatted(Formatting.DARK_GRAY))
                  .append(Text.literal("Recharging: ").formatted(Formatting.BLUE))
                  .append(Text.literal(duration).formatted(Formatting.DARK_GRAY)));
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
      return 36000 - 12000*Math.max(ArcanaAugments.getAugmentOnItem(item,ArcanaAugments.TEMPO_MORTUUS.id),0); // 30 minutes - 10 per level
   }
   
   public boolean protectFromDeath(ItemStack stack, LivingEntity living, DamageSource source, boolean constructInterference){
      if(source.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY) && !source.isOf(ArcanaDamageTypes.VENGEANCE_TOTEM) && !source.isOf(ArcanaDamageTypes.CONCENTRATION)){
         return false;
      }
      if(getEnergy(stack) > 0 && !isActive(stack)){
         return false;
      }
      
      if(living.getEntityWorld() instanceof ServerWorld world){
         world.spawnParticles(ParticleTypes.LARGE_SMOKE,living.getX(),living.getY()+living.getHeight()/2,living.getZ(),100,.4,.4,.4,0.07);
      }
      
      if(living instanceof ServerPlayerEntity player){
         DialogHelper dialogHelper = new DialogHelper();
         
         if(isActive(stack)){
            ArcanaAchievements.grant(player,ArcanaAchievements.DEATHS_DOOR.id);
            
            dialogHelper.sendDialog(List.of(player),new Dialog(new ArrayList<>(Arrays.asList(
                  Text.literal("\n")
                        .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                        .append(Text.literal("Nul").formatted(Formatting.DARK_GRAY,Formatting.BOLD))
                        .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD)),
                  Text.literal("")
                        .append(Text.literal("Now is no time to die on me. Pull yourself together!").formatted(Formatting.DARK_GRAY, Formatting.ITALIC))
            )),new ArrayList<>(Arrays.asList(new Dialog.DialogSound(SoundEvents.ENTITY_WITHER_AMBIENT,0.3f,1.4f))),new int[]{},1,1,-1),true);
            living.setHealth(1.0f);
            living.addStatusEffect(new StatusEffectInstance(ArcanaRegistry.DEATH_WARD_EFFECT, 600, 0));
            return true;
         }
         
         if(!constructInterference){
            dialogHelper.sendDialog(List.of(player),new Dialog(new ArrayList<>(Arrays.asList(
                  Text.literal("\n")
                        .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                        .append(Text.literal("Nul").formatted(Formatting.DARK_GRAY,Formatting.BOLD))
                        .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD)),
                  Text.literal("")
                        .append(Text.literal("Let my gift offer you a second chance.").formatted(Formatting.DARK_GRAY))
            )),new ArrayList<>(Arrays.asList(new Dialog.DialogSound(SoundEvents.ENTITY_WITHER_AMBIENT,0.3f,0.7f))),new int[]{},1,1,-1),true);
         }
         ArcanaNovum.data(player).addXP(ArcanaConfig.getInt(ArcanaRegistry.NUL_MEMENTO_PROTECT));
      }
      
      setEnergy(stack,getMaxEnergy(stack));
      
      living.setHealth(1.0f);
      living.clearStatusEffects();
      living.addStatusEffect(new StatusEffectInstance(ArcanaRegistry.DEATH_WARD_EFFECT, constructInterference ? 300/2 : 300, 0));
      living.addStatusEffect(new StatusEffectInstance(ArcanaRegistry.GREATER_INVISIBILITY_EFFECT, 100, 0));
      living.getWorld().sendEntityStatus(living, EntityStatuses.USE_TOTEM_OF_UNDYING);
      return true;
   }
   
   public boolean isActive(ItemStack stack){
      return getBooleanProperty(stack,ACTIVE_TAG);
   }
   
   public void forgor(ItemStack stack, ServerPlayerEntity player){
      putProperty(stack,ACTIVE_TAG,true);
      
      int increments = 100;
      StatusEffectInstance blind = new StatusEffectInstance(ArcanaRegistry.GREATER_BLINDNESS_EFFECT,increments*5 , 0, false, false, true);
      StatusEffectInstance slow = new StatusEffectInstance(StatusEffects.SLOWNESS, increments*5, 9, false, false, true);
      StatusEffectInstance fatigue = new StatusEffectInstance(StatusEffects.MINING_FATIGUE, increments*5 , 4, false, false, true);
      StatusEffectInstance weakness = new StatusEffectInstance(StatusEffects.WEAKNESS,increments*5 , 4, false, false, true);
      player.addStatusEffect(blind);
      player.addStatusEffect(slow);
      player.addStatusEffect(fatigue);
      player.addStatusEffect(weakness);
      
      final boolean[] cont = {true};
      int resolve = ArcanaNovum.data(player).getAugmentLevel(ArcanaAugments.RESOLVE.id);
      final int maxConc = LevelUtils.concFromXp(ArcanaNovum.data(player).getXP(),resolve);
      DialogHelper dialogHelper = new DialogHelper();
      
      dialogHelper.sendDialog(List.of(player),new Dialog(new ArrayList<>(Arrays.asList(
            Text.literal("\n\n\n\n")
                  .append(Text.literal("As the crushing weight of ").formatted(Formatting.DARK_GRAY))
                  .append(Text.literal("concentration").formatted(Formatting.RED))
                  .append(Text.literal(" takes your mind you hear the ").formatted(Formatting.DARK_GRAY))
                  .append(TextUtils.withColor(Text.literal("Nul Memento").formatted(Formatting.BOLD),ArcanaColors.NUL_COLOR))
                  .append(Text.literal(" whisper...")).formatted(Formatting.DARK_GRAY)
      )),new ArrayList<>(Arrays.asList(new Dialog.DialogSound(SoundEvents.ENTITY_WITHER_AMBIENT,0.3f,0.7f))),new int[]{},1,1,-1),false);
      ArcanaNovum.addArcanaEvent(new NulMementoEvent(increments,player,stack));
      
      ArcanaNovum.addTickTimerCallback(new GenericTimer(increments*1, () -> {
         if(cont[0]){
            ItemStack headStack = player.getEquippedStack(EquipmentSlot.HEAD);
            if(!(ArcanaItemUtils.identifyItem(headStack) instanceof NulMemento) || !(ArcanaItemUtils.getUsedConcentration(player) > maxConc)){
               cont[0] = false;
               processHalted(player);
            }else{
               ArcanaNovum.addArcanaEvent(new NulMementoEvent(increments,player,stack));
               dialogHelper.sendDialog(List.of(player),new Dialog(new ArrayList<>(Arrays.asList(
                     Text.literal("\n\n\n\n")
                           .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                           .append(Text.literal("Nul").formatted(Formatting.DARK_GRAY,Formatting.BOLD))
                           .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                           .append(Text.literal("\nFeel the weight, embrace it... let me in...").formatted(Formatting.DARK_GRAY, Formatting.ITALIC))
               )),new ArrayList<>(Arrays.asList(new Dialog.DialogSound(SoundEvents.ENTITY_WITHER_AMBIENT,0.3f,0.7f))),new int[]{},1,1,-1),true);
            }
         }
      }));
      ArcanaNovum.addTickTimerCallback(new GenericTimer(increments*2, () -> {
         if(cont[0]){
            ItemStack headStack = player.getEquippedStack(EquipmentSlot.HEAD);
            if(!(ArcanaItemUtils.identifyItem(headStack) instanceof NulMemento) || !(ArcanaItemUtils.getUsedConcentration(player) > maxConc)){
               cont[0] = false;
               processHalted(player);
            }else{
               ArcanaNovum.addArcanaEvent(new NulMementoEvent(increments,player,stack));
               dialogHelper.sendDialog(List.of(player),new Dialog(new ArrayList<>(Arrays.asList(
                     Text.literal("\n\n\n\n")
                           .append(Text.literal("You feel as though your ").formatted(Formatting.DARK_GRAY))
                           .append(Text.literal("skull").formatted(Formatting.GRAY))
                           .append(Text.literal(" is about to ").formatted(Formatting.DARK_GRAY))
                           .append(Text.literal("collapse").formatted(Formatting.RED))
                           .append(Text.literal(" when a ").formatted(Formatting.DARK_GRAY))
                           .append(Text.literal("gentle breeze").formatted(Formatting.GRAY))
                           .append(Text.literal(" sweeps through you.").formatted(Formatting.DARK_GRAY))
               )),new ArrayList<>(Arrays.asList(new Dialog.DialogSound(SoundEvents.ENTITY_WITHER_AMBIENT,0.3f,0.7f))),new int[]{},1,1,-1),false);
            }
         }
      }));
      ArcanaNovum.addTickTimerCallback(new GenericTimer(increments*3, () -> {
         if(cont[0]){
            ItemStack headStack = player.getEquippedStack(EquipmentSlot.HEAD);
            if(!(ArcanaItemUtils.identifyItem(headStack) instanceof NulMemento) || !(ArcanaItemUtils.getUsedConcentration(player) > maxConc)){
               cont[0] = false;
               processHalted(player);
            }else{
               ArcanaNovum.addArcanaEvent(new NulMementoEvent(increments,player,stack));
               dialogHelper.sendDialog(List.of(player),new Dialog(new ArrayList<>(Arrays.asList(
                     Text.literal("\n\n\n\n")
                           .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                           .append(Text.literal("Nul").formatted(Formatting.DARK_GRAY,Formatting.BOLD))
                           .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                           .append(Text.literal("\nYour secrets are safe with me. Be free of this burden, for I now bear it alone.").formatted(Formatting.DARK_GRAY, Formatting.ITALIC))
               )),new ArrayList<>(Arrays.asList(new Dialog.DialogSound(SoundEvents.ENTITY_WITHER_AMBIENT,0.3f,0.7f))),new int[]{},1,1,-1),true);
            }
         }
      }));
      ArcanaNovum.addTickTimerCallback(new GenericTimer(increments*4, () -> {
         if(cont[0]){
            ItemStack headStack = player.getEquippedStack(EquipmentSlot.HEAD);
            if(!(ArcanaItemUtils.identifyItem(headStack) instanceof NulMemento) || !(ArcanaItemUtils.getUsedConcentration(player) > maxConc)){
               cont[0] = false;
               processHalted(player);
            }else{
               ArcanaNovum.addArcanaEvent(new NulMementoEvent(increments,player,stack));
               dialogHelper.sendDialog(List.of(player),new Dialog(new ArrayList<>(Arrays.asList(
                     Text.literal("\n\n\n\n")
                           .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                           .append(Text.literal("Nul").formatted(Formatting.DARK_GRAY,Formatting.BOLD))
                           .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                           .append(Text.literal("\nThat is, until we meet again...").formatted(Formatting.DARK_GRAY, Formatting.ITALIC))
               )),new ArrayList<>(Arrays.asList(new Dialog.DialogSound(SoundEvents.ENTITY_WITHER_AMBIENT,0.3f,0.7f))),new int[]{},1,1,-1),true);
            }
         }
      }));
      ArcanaNovum.addTickTimerCallback(new GenericTimer(increments*5, () -> {
         if(cont[0]){
            ItemStack headStack = player.getEquippedStack(EquipmentSlot.HEAD);
            if(!(ArcanaItemUtils.identifyItem(headStack) instanceof NulMemento) || !(ArcanaItemUtils.getUsedConcentration(player) > maxConc)){
               cont[0] = false;
               processHalted(player);
            }else{
               ArcanaNovum.addArcanaEvent(new NulMementoEvent(increments,player,stack));
               dialogHelper.sendDialog(List.of(player),new Dialog(new ArrayList<>(Arrays.asList(
                     Text.literal("\n\n\n\n")
                           .append(Text.literal("The ").formatted(Formatting.DARK_GRAY))
                           .append(Text.literal("Nul Memento").formatted(Formatting.BLACK,Formatting.BOLD))
                           .append(Text.literal(" crumbles ").formatted(Formatting.GRAY))
                           .append(Text.literal("into").formatted(Formatting.DARK_GRAY))
                           .append(Text.literal(" ash ").formatted(Formatting.GRAY))
                           .append(Text.literal("around your head, your mind still ").formatted(Formatting.DARK_GRAY))
                           .append(Text.literal("burning").formatted(Formatting.RED))
                           .append(Text.literal(" from the ").formatted(Formatting.DARK_GRAY))
                           .append(Text.literal("overwhelming ").formatted(Formatting.RED))
                           .append(Text.literal("Arcana.").formatted(Formatting.LIGHT_PURPLE))
               )),new ArrayList<>(Arrays.asList(new Dialog.DialogSound(SoundEvents.ENTITY_WITHER_AMBIENT,0.3f,0.7f))),new int[]{},1,1,-1),false);
               
               StatusEffectInstance nausea = new StatusEffectInstance(StatusEffects.NAUSEA,200, 4, false, false, true);
               StatusEffectInstance ward = new StatusEffectInstance(ArcanaRegistry.DEATH_WARD_EFFECT,increments*2, 0, false, false, true);
               player.addStatusEffect(nausea);
               player.addStatusEffect(ward);
               headStack.decrement(headStack.getCount());
               SoundUtils.playSongToPlayer(player, SoundEvents.ENTITY_ZOMBIE_VILLAGER_CURE, 1f, 1f);
            }
         }
      }));
      ArcanaNovum.addTickTimerCallback(new GenericTimer(increments*6, () -> {
         if(cont[0]){
            ArcanaNovum.addArcanaEvent(new NulMementoEvent(increments,player,stack));
            dialogHelper.sendDialog(List.of(player),new Dialog(new ArrayList<>(Arrays.asList(
                  Text.literal("\n\n\n\n")
                        .append(Text.literal("All of your Skill Points have been deallocated.").formatted(Formatting.AQUA))
            )),new ArrayList<>(Arrays.asList(new Dialog.DialogSound(SoundEvents.ENTITY_WITHER_AMBIENT,0.3f,0.7f))),new int[]{},1,1,-1),false);
            
            ArcanaNovum.data(player).removeAllAugments();
            ArcanaNovum.data(player).addXP(ArcanaConfig.getInt(ArcanaRegistry.NUL_MEMENTO_DEALLOCATE));
            ArcanaAchievements.grant(player,ArcanaAchievements.LOST_KNOWLEDGE.id);
            ArcanaAchievements.progress(player,ArcanaAchievements.AMNESIAC.id,1);
         }
      }));
   }
   
   private void processHalted(ServerPlayerEntity player){
      player.sendMessage(Text.literal(""),false);
      player.sendMessage(Text.literal(""),false);
      player.sendMessage(Text.literal(""),false);
      player.sendMessage(Text.literal("")
            .append(Text.literal("The weight of the ").formatted(Formatting.GRAY,Formatting.ITALIC))
            .append(Text.literal("Nul Memento").formatted(Formatting.BLACK,Formatting.BOLD,Formatting.ITALIC))
            .append(Text.literal(" becomes too much to bare, perhaps you aren't ready...").formatted(Formatting.GRAY,Formatting.ITALIC)), false);
      player.clearStatusEffects();
   }
   
   public void inventoryDialog(ServerPlayerEntity player){
      ArrayList<Dialog> dialogOptions = new ArrayList<>();
      // Conditions: 0 - Crafted Wings, 1 - Crafted Aequalis, 2 - Has Ceptyus Pickaxe, 3 - Has Aequalis, 4 - Has Egg, 5 - Has Greaves, 6 - Has Spear
      boolean[] conditions = new boolean[]{
            ArcanaNovum.data(player).hasCrafted(ArcanaRegistry.WINGS_OF_ENDERIA),
            ArcanaNovum.data(player).hasCrafted(ArcanaRegistry.AEQUALIS_SCIENTIA),
            ArcanaItemUtils.hasItemInInventory(player,ArcanaRegistry.PICKAXE_OF_CEPTYUS.getItem()),
            ArcanaItemUtils.hasItemInInventory(player,ArcanaRegistry.AEQUALIS_SCIENTIA.getItem()),
            ArcanaItemUtils.hasItemInInventory(player,Items.DRAGON_EGG),
            ArcanaItemUtils.hasItemInInventory(player,ArcanaRegistry.GREAVES_OF_GAIALTUS.getItem()),
            ArcanaItemUtils.hasItemInInventory(player,ArcanaRegistry.SPEAR_OF_TENBROUS.getItem()),
      };
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Text.literal("\n")
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(Text.literal("Nul").formatted(Formatting.DARK_GRAY,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD)),
            Text.literal("")
                  .append(Text.literal("My Chosen... Will you continue show the courage that your peers lack?").formatted(Formatting.DARK_GRAY))
      )),new ArrayList<>(Arrays.asList(new Dialog.DialogSound(SoundEvents.ENTITY_WITHER_AMBIENT,0.3f,0.7f))),new int[]{},1,1,0b0));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Text.literal("\n")
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(Text.literal("Nul").formatted(Formatting.DARK_GRAY,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD)),
            Text.literal("")
                  .append(Text.literal("My dear Player, will you help us revitalize these realms to new heights?").formatted(Formatting.DARK_GRAY))
      )),new ArrayList<>(Arrays.asList(new Dialog.DialogSound(SoundEvents.ENTITY_WITHER_AMBIENT,0.3f,0.7f))),new int[]{},1,1,0b0));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Text.literal("\n")
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(Text.literal("Nul").formatted(Formatting.DARK_GRAY,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD)),
            Text.literal("")
                  .append(Text.literal("Everything faces me once... Few have ever faced me twice.").formatted(Formatting.DARK_GRAY))
      )),new ArrayList<>(Arrays.asList(new Dialog.DialogSound(SoundEvents.ENTITY_WITHER_AMBIENT,0.3f,0.7f))),new int[]{},1,1,0b0));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Text.literal("\n")
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(Text.literal("Nul").formatted(Formatting.DARK_GRAY,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD)),
            Text.literal("")
                  .append(Text.literal("Take care in the dark secrets you seek. For only I can ever take them away.").formatted(Formatting.DARK_GRAY))
      )),new ArrayList<>(Arrays.asList(new Dialog.DialogSound(SoundEvents.ENTITY_WITHER_AMBIENT,0.3f,0.7f))),new int[]{},1,1,0b0));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Text.literal("\n")
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(Text.literal("Nul").formatted(Formatting.DARK_GRAY,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD)),
            Text.literal("")
                  .append(Text.literal("What do you think of my Sister's realm? Do you see the need for my mission now?").formatted(Formatting.DARK_GRAY))
      )),new ArrayList<>(Arrays.asList(new Dialog.DialogSound(SoundEvents.ENTITY_WITHER_AMBIENT,0.3f,0.7f))),new int[]{},0,1,0b1));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Text.literal("\n")
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(Text.literal("Nul").formatted(Formatting.DARK_GRAY,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD)),
            Text.literal("")
                  .append(Text.literal("So you've met my kin, Equayus? What sort of exchange did you have?").formatted(Formatting.DARK_GRAY))
      )),new ArrayList<>(Arrays.asList(new Dialog.DialogSound(SoundEvents.ENTITY_WITHER_AMBIENT,0.3f,0.7f))),new int[]{},0,1,0b10));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Text.literal("\n")
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(Text.literal("Nul").formatted(Formatting.DARK_GRAY,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD)),
            Text.literal("")
                  .append(Text.literal("I sense my kin's Arcana on you... I trust your deal was worthwhile.").formatted(Formatting.DARK_GRAY))
      )),new ArrayList<>(Arrays.asList(new Dialog.DialogSound(SoundEvents.ENTITY_WITHER_AMBIENT,0.3f,0.7f))),new int[]{},0,1,0b10));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Text.literal("\n")
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(Text.literal("Nul").formatted(Formatting.DARK_GRAY,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD)),
            Text.literal("")
                  .append(Text.literal("Equayus and I might not always see eye to eye, but we both have these realms' best intentions at heart.").formatted(Formatting.DARK_GRAY))
      )),new ArrayList<>(Arrays.asList(new Dialog.DialogSound(SoundEvents.ENTITY_WITHER_AMBIENT,0.3f,0.7f))),new int[]{},0,1,0b10));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Text.literal("\n")
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(Text.literal("Nul").formatted(Formatting.DARK_GRAY,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD)),
            Text.literal("")
                  .append(Text.literal("A relic of Ceptyus?! How fascinating! I wonder if it was forgotten as they fled.").formatted(Formatting.DARK_GRAY))
      )),new ArrayList<>(Arrays.asList(new Dialog.DialogSound(SoundEvents.ENTITY_WITHER_AMBIENT,0.3f,0.7f))),new int[]{},0,1,0b100));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Text.literal("\n")
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(Text.literal("Nul").formatted(Formatting.DARK_GRAY,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD)),
            Text.literal("")
                  .append(Text.literal("Gaialtus turned its back on this world and left it to fall to ruin. What good is a creator that abandons their creation?").formatted(Formatting.DARK_GRAY))
      )),new ArrayList<>(Arrays.asList(new Dialog.DialogSound(SoundEvents.ENTITY_WITHER_AMBIENT,0.3f,0.7f))),new int[]{},0,1,0b100000));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Text.literal("\n")
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(Text.literal("Nul").formatted(Formatting.DARK_GRAY,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD)),
            Text.literal("")
                  .append(Text.literal("If Gaialtus was still here, none of my work would be necessary. Yet here I am, picking up its slack to maintain peace.").formatted(Formatting.DARK_GRAY))
      )),new ArrayList<>(Arrays.asList(new Dialog.DialogSound(SoundEvents.ENTITY_WITHER_AMBIENT,0.3f,0.7f))),new int[]{},0,1,0b100000));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Text.literal("\n")
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(Text.literal("Nul").formatted(Formatting.DARK_GRAY,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(Text.literal("\nGaialtus's negligence is responsible for the decay that has befallen our world. Don't let those trousers sway your mind.\n").formatted(Formatting.DARK_GRAY)),
            Text.literal("")
                  .append(Text.literal(" ~ ").formatted(Formatting.DARK_AQUA,Formatting.BOLD))
                  .append(Text.literal("Equayus").formatted(Formatting.AQUA,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.DARK_AQUA,Formatting.BOLD))
                  .append(Text.literal("\nBrother, you know not of what you speak. Decay is sometimes a necessary precursor to greater regrowth.").formatted(Formatting.AQUA))
      )),new ArrayList<>(Arrays.asList(
            new Dialog.DialogSound(SoundEvents.ENTITY_WITHER_AMBIENT,0.3f,0.7f),
            new Dialog.DialogSound(SoundEvents.ENTITY_ALLAY_AMBIENT_WITHOUT_ITEM,0.5f,0.7f))
      ),new int[]{0,90},0,1,0b101000));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Text.literal("\n")
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(Text.literal("Nul").formatted(Formatting.DARK_GRAY,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(Text.literal("\nEquayus? Have you also taken an interest in this Player?\n").formatted(Formatting.DARK_GRAY)),
            Text.literal("")
                  .append(Text.literal(" ~ ").formatted(Formatting.DARK_AQUA,Formatting.BOLD))
                  .append(Text.literal("Equayus").formatted(Formatting.AQUA,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.DARK_AQUA,Formatting.BOLD))
                  .append(Text.literal("\nBrother, it is always good to hear your voice again...").formatted(Formatting.AQUA))
      )),new ArrayList<>(Arrays.asList(
            new Dialog.DialogSound(SoundEvents.ENTITY_WITHER_AMBIENT,0.3f,0.7f),
            new Dialog.DialogSound(SoundEvents.ENTITY_ALLAY_AMBIENT_WITHOUT_ITEM,0.5f,0.7f))
      ),new int[]{0,60},0,1,0b1000));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Text.literal("\n")
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(Text.literal("Nul").formatted(Formatting.DARK_GRAY,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD)),
            Text.literal("")
                  .append(Text.literal("That Spear you wield harnesses shadow Arcana far darker than anything I have seen, It's almost frightening. Be cautious with it.").formatted(Formatting.DARK_GRAY))
      )),new ArrayList<>(Arrays.asList(new Dialog.DialogSound(SoundEvents.ENTITY_WITHER_AMBIENT,0.3f,0.7f))),new int[]{},0,1,0b1000000));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Text.literal("\n")
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(Text.literal("Nul").formatted(Formatting.DARK_GRAY,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD)),
            Text.literal("")
                  .append(Text.literal("I never knew such vitriol could come from a mere Spear. Tenbrous must've been quite powerful.").formatted(Formatting.DARK_GRAY))
      )),new ArrayList<>(Arrays.asList(new Dialog.DialogSound(SoundEvents.ENTITY_WITHER_AMBIENT,0.3f,0.7f))),new int[]{},0,1,0b1000000));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Text.literal("\n")
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(Text.literal("Nul").formatted(Formatting.DARK_GRAY,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(Text.literal("\nYou carry an intriguing weapon... I know little of Tenbrous, I wish we had the chance to meet.\n").formatted(Formatting.DARK_GRAY)),
            Text.literal("")
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD))
                  .append(Text.literal("Enderia").formatted(Formatting.DARK_PURPLE,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD))
                  .append(Text.literal("\nNo one will have to meet that monster EVER again! I made sure of it!\n").formatted(Formatting.DARK_PURPLE)),
            Text.literal("")
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(Text.literal("Nul").formatted(Formatting.DARK_GRAY,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(Text.literal("\nAre you sure whatever you did was truly permanent? Such a shame all that power went to waste on you...").formatted(Formatting.DARK_GRAY))
      )),new ArrayList<>(Arrays.asList(
            new Dialog.DialogSound(SoundEvents.ENTITY_WITHER_AMBIENT,0.3f,0.7f),
            new Dialog.DialogSound(SoundEvents.ENTITY_ENDER_DRAGON_GROWL,0.5f,1.4f),
            new Dialog.DialogSound(SoundEvents.ENTITY_WITHER_AMBIENT,0.3f,0.7f))
      ),new int[]{0,60,60},0,1,0b1010000));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Text.literal("\n")
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(Text.literal("Nul").formatted(Formatting.DARK_GRAY,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(Text.literal("\nSo you too, have taken an interest in this Player now? Do you now share my vision?\n").formatted(Formatting.DARK_GRAY)),
            Text.literal("")
                  .append(Text.literal(" ~ ").formatted(Formatting.DARK_AQUA,Formatting.BOLD))
                  .append(Text.literal("Equayus").formatted(Formatting.AQUA,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.DARK_AQUA,Formatting.BOLD))
                  .append(Text.literal("\nThis one is quite curious. I do believe they will help restore this world.\n").formatted(Formatting.AQUA)),
            Text.literal("")
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(Text.literal("Nul").formatted(Formatting.DARK_GRAY,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(Text.literal("\nAt last, our goals united again.").formatted(Formatting.DARK_GRAY))
      )),new ArrayList<>(Arrays.asList(
            new Dialog.DialogSound(SoundEvents.ENTITY_WITHER_AMBIENT,0.3f,0.7f),
            new Dialog.DialogSound(SoundEvents.ENTITY_ALLAY_AMBIENT_WITHOUT_ITEM,0.5f,0.7f),
            new Dialog.DialogSound(SoundEvents.ENTITY_WITHER_AMBIENT,0.3f,1.2f))
      ),new int[]{0,60,60},0,1,0b1000));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Text.literal("\n")
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(Text.literal("Nul").formatted(Formatting.DARK_GRAY,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(Text.literal("\nHeh heh heh, How does it feel to be carried around like a prize, dear Sister?\n").formatted(Formatting.DARK_GRAY)),
            Text.literal("")
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD))
                  .append(Text.literal("Enderia").formatted(Formatting.DARK_PURPLE,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD))
                  .append(Text.literal("\nNUL! YOU DID THIS TO ME! I WILL END YOU!\n").formatted(Formatting.DARK_PURPLE)),
            Text.literal("")
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(Text.literal("Nul").formatted(Formatting.DARK_GRAY,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(Text.literal("\nIt was not my actions that led to this. This Player did it of their own power. Perhaps in time your arrogance will turn to humility.").formatted(Formatting.DARK_GRAY))
      )),new ArrayList<>(Arrays.asList(
            new Dialog.DialogSound(SoundEvents.ENTITY_WITHER_AMBIENT,0.3f,0.7f),
            new Dialog.DialogSound(SoundEvents.ENTITY_ENDER_DRAGON_GROWL,0.5f,1.4f),
            new Dialog.DialogSound(SoundEvents.ENTITY_WITHER_AMBIENT,0.3f,0.7f))
      ),new int[]{0,60,60},0,1,0b10000));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Text.literal("\n")
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(Text.literal("Nul").formatted(Formatting.DARK_GRAY,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(Text.literal("\nI'm sorry it had to come to this, Sister. You had so long to realize your mistakes.\n").formatted(Formatting.DARK_GRAY)),
            Text.literal("")
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD))
                  .append(Text.literal("Enderia").formatted(Formatting.DARK_PURPLE,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD))
                  .append(Text.literal("\nGods do not make mistakes! Our mere thoughts become the laws of our realm!\n").formatted(Formatting.DARK_PURPLE)),
            Text.literal("")
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(Text.literal("Nul").formatted(Formatting.DARK_GRAY,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(Text.literal("\nThen I suppose your mistake was defying Tenbrous, and ascending to take its place.\n").formatted(Formatting.DARK_GRAY)),
            Text.literal("")
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD))
                  .append(Text.literal("Enderia").formatted(Formatting.DARK_PURPLE,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD))
                  .append(Text.literal("\n...\n").formatted(Formatting.DARK_PURPLE)),
            Text.literal("")
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(Text.literal("Nul").formatted(Formatting.DARK_GRAY,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(Text.literal("\nFood for thought, my Sister.").formatted(Formatting.DARK_GRAY))
      )),new ArrayList<>(Arrays.asList(
            new Dialog.DialogSound(SoundEvents.ENTITY_WITHER_AMBIENT,0.3f,0.7f),
            new Dialog.DialogSound(SoundEvents.ENTITY_ENDER_DRAGON_GROWL,0.5f,1.4f),
            new Dialog.DialogSound(SoundEvents.ENTITY_WITHER_AMBIENT,0.3f,0.7f),
            new Dialog.DialogSound(SoundEvents.ENTITY_ENDER_DRAGON_GROWL,0.1f,0.6f),
            new Dialog.DialogSound(SoundEvents.ENTITY_WITHER_AMBIENT,0.3f,0.7f))
      ),new int[]{0,80,100,80,60},0,1,0b10000));
      
      
      DialogHelper helper = new DialogHelper(dialogOptions,conditions);
      helper.sendDialog(List.of(player),helper.getWeightedResult(),true);
   }
   
   @Override
	protected ArcanaRecipe makeRecipe(){
      ExplainIngredient a = new ExplainIngredient(GraphicalItem.withColor(GraphicItems.PAGE_BG, ArcanaColors.DARK_COLOR),1,"",false)
            .withName(Text.literal("In World Recipe").formatted(Formatting.BLUE,Formatting.BOLD))
            .withLore(List.of(Text.literal("Build this in the World").formatted(Formatting.DARK_PURPLE)));
      ExplainIngredient s = new ExplainIngredient(Items.SOUL_SAND,1,"Soul Sand or Soil")
            .withName(Text.literal("Soul Sand or Soil").formatted(Formatting.GRAY,Formatting.BOLD))
            .withLore(List.of(Text.literal("Construct a Wither Base with a heart of Netherite").formatted(Formatting.DARK_PURPLE)));
      ExplainIngredient k = new ExplainIngredient(Items.WITHER_SKELETON_SKULL,1,"Wither Skeleton Skull")
            .withName(Text.literal("Wither Skeleton Skull").formatted(Formatting.DARK_GRAY,Formatting.BOLD))
            .withLore(List.of(Text.literal("Construct a Wither Base with a heart of Netherite").formatted(Formatting.DARK_PURPLE)));
      ExplainIngredient n = new ExplainIngredient(Items.NETHERITE_BLOCK,1,"Netherite Block")
            .withName(Text.literal("Block of Netherite").formatted(Formatting.DARK_RED,Formatting.BOLD))
            .withLore(List.of(Text.literal("Construct a Wither Base with a heart of Netherite").formatted(Formatting.DARK_PURPLE)));
      ExplainIngredient c = new ExplainIngredient(ArcanaRegistry.DIVINE_CATALYST.getItem(),1,"Divine Augment Catalyst")
            .withName(Text.literal("Divine Augmentation Catalyst").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD))
            .withLore(List.of(
                  Text.literal("")
                        .append(Text.literal("Right Click").formatted(Formatting.BLUE))
                        .append(Text.literal(" the ").formatted(Formatting.DARK_PURPLE))
                        .append(Text.literal("Catalyst").formatted(Formatting.LIGHT_PURPLE))
                        .append(Text.literal(" on the ").formatted(Formatting.DARK_PURPLE))
                        .append(Text.literal("Netherite Heart").formatted(Formatting.DARK_RED)),
                  Text.literal("")
                        .append(Text.literal("Divine Energy").formatted(Formatting.LIGHT_PURPLE))
                        .append(Text.literal(" will flow into the ").formatted(Formatting.DARK_PURPLE))
                        .append(Text.literal("Exalted Construct").formatted(Formatting.DARK_GRAY))
                        .append(Text.literal(" empowering it").formatted(Formatting.DARK_PURPLE)),
                  Text.literal("")
                        .append(Text.literal("Defeat the ").formatted(Formatting.DARK_PURPLE))
                        .append(Text.literal("Exalted Construct").formatted(Formatting.DARK_GRAY))
                        .append(Text.literal(" without dying to receive a ").formatted(Formatting.DARK_PURPLE))
                        .append(Text.literal("Nul Memento").formatted(Formatting.BLACK)),
                  Text.literal(""),
                  Text.literal("WARNING!!! This fight is considerably harder than a Nul Construct. Attempt at your own peril.").formatted(Formatting.RED)
            ));
      
      ExplainIngredient[][] ingredients = {
            {a,a,a,a,a},
            {a,k,k,k,a},
            {a,s,n,s,c},
            {a,a,s,a,a},
            {a,a,a,a,a}};
      return new ExplainRecipe(ingredients);
   }
   
   @Override
   public List<List<Text>> getBookLore(){
      List<List<Text>> list = new ArrayList<>();
      list.add(List.of(TextUtils.withColor(Text.literal("    Nul Memento").formatted(Formatting.BOLD),ArcanaColors.NUL_COLOR),Text.literal("\nRarity: ").formatted(Formatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(),false)),Text.literal("\nThis entity of Death that I have acquired a passing familiarity with is most intriguing. He wanted me to prove my fighting prowess by dueling his creation, and I believe I succeeded. I was gifted this strange ").formatted(Formatting.BLACK)));
      list.add(List.of(TextUtils.withColor(Text.literal("    Nul Memento").formatted(Formatting.BOLD),ArcanaColors.NUL_COLOR),Text.literal("\nskull, and was informed that I have become one of his ‘chosen’.\nI’m not sure what to think of this. What machinations could a Deity of Death be planning such that he needs help from me? The Memento whispers to me every so often.").formatted(Formatting.BLACK)));
      list.add(List.of(TextUtils.withColor(Text.literal("    Nul Memento").formatted(Formatting.BOLD),ArcanaColors.NUL_COLOR),Text.literal("\nI have come to learn the entity calls himself Nul, the God of Death and Knowledge. He speaks of Arcana, and secrets that I have yet to learn. He warns that one mind can only hold so much knowledge at one time. ").formatted(Formatting.BLACK)));
      list.add(List.of(TextUtils.withColor(Text.literal("    Nul Memento").formatted(Formatting.BOLD),ArcanaColors.NUL_COLOR),Text.literal("\nHowever, he offers his aid in circumventing this limitation. \nThis Memento reacts to an overburdened mind when worn and will make me forget all of the skills I have learned. ").formatted(Formatting.BLACK)));
      list.add(List.of(TextUtils.withColor(Text.literal("    Nul Memento").formatted(Formatting.BOLD),ArcanaColors.NUL_COLOR),Text.literal("\nAs long as I use those skills before forgetting them, I should be able to take advantage of new knowledge with a new limit to what I can learn.\nThe Memento also offers incredible protection, as if it ").formatted(Formatting.BLACK)));
      list.add(List.of(TextUtils.withColor(Text.literal("    Nul Memento").formatted(Formatting.BOLD),ArcanaColors.NUL_COLOR),Text.literal("\nwere made of enchanted Netherite!\n\nNul himself stated that by wearing his Memento, he may be willing to spare me from death every now and again.").formatted(Formatting.BLACK)));
      return list;
   }
   
   public class NulMementoItem extends ArcanaPolymerItem {
      public NulMementoItem(){
         super(getThis(), getEquipmentArcanaItemComponents()
               .armor(ArmorMaterials.NETHERITE, EquipmentType.HELMET)
         );
      }
      
      @Override
      public Item getPolymerItem(ItemStack itemStack, PacketContext context){
         if(ArcanaItemUtils.isArcane(itemStack)){
            boolean onHead = getBooleanProperty(itemStack,HEAD_TAG);
            if(onHead && PolymerResourcePackUtils.hasMainPack(context)) return textureItem;
         }
         return super.getPolymerItem(itemStack, context);
      }
      
      @Override
      public ItemStack getPolymerItemStack(ItemStack itemStack, TooltipType tooltipType, PacketContext context){
         ItemStack baseStack = super.getPolymerItemStack(itemStack, tooltipType, context);
         EquippableComponent equippableComponent = baseStack.get(DataComponentTypes.EQUIPPABLE);
         EquippableComponent newComp = EquippableComponent.builder(equippableComponent.slot()).equipSound(equippableComponent.equipSound()).build();
         baseStack.set(DataComponentTypes.EQUIPPABLE,newComp);
         return baseStack;
      }
      
      @Override
      public void inventoryTick(ItemStack stack, ServerWorld world, Entity entity, @Nullable EquipmentSlot slot){
         if(!ArcanaItemUtils.isArcane(stack)) return;
         if(!(world instanceof ServerWorld && entity instanceof ServerPlayerEntity player)) return;
         
         boolean nowOnHead = player.getEquippedStack(EquipmentSlot.HEAD).equals(stack);
         boolean wasOnHead = getBooleanProperty(stack,HEAD_TAG);
         if(nowOnHead != wasOnHead){
            putProperty(stack,HEAD_TAG,nowOnHead);
         }
         if(nowOnHead && getEnergy(stack) > 0){
            addEnergy(stack,-1);
            buildItemLore(stack,entity.getServer());
         }
         
         // 0.0000075 ~ 120 minutes between voice lines
         if(Math.random() < 0.0000075){ // 0.0000075
            inventoryDialog(player);
         }
      }
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
   }
}

