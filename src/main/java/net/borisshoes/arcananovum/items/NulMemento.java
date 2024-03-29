package net.borisshoes.arcananovum.items;

import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.EnergyItem;
import net.borisshoes.arcananovum.core.MagicItem;
import net.borisshoes.arcananovum.core.polymer.MagicPolymerArmorItem;
import net.borisshoes.arcananovum.damage.ArcanaDamageTypes;
import net.borisshoes.arcananovum.recipes.arcana.*;
import net.borisshoes.arcananovum.utils.*;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ArmorMaterials;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;

public class NulMemento extends EnergyItem {
   
   private static final String TXT = "item/nul_memento";
   private static final Item textureItem = Items.BLACK_STAINED_GLASS;
   
   public NulMemento(){
      id = "nul_memento";
      name = "Nul Memento";
      rarity = MagicRarity.MYTHICAL;
      categories = new ArcaneTome.TomeFilter[]{ArcaneTome.TomeFilter.MYTHICAL, ArcaneTome.TomeFilter.ITEMS, ArcaneTome.TomeFilter.EQUIPMENT};
      vanillaItem = Items.WITHER_SKELETON_SKULL;
      item = new NulMementoItem(new FabricItemSettings().maxCount(1).fireproof());
      models = new ArrayList<>();
      models.add(new Pair<>(vanillaItem,TXT));
      models.add(new Pair<>(textureItem,TXT));
      
      ItemStack stack = new ItemStack(item);
      NbtCompound tag = stack.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList enchants = new NbtList();
      NbtCompound prot = new NbtCompound();
      prot.putString("id","protection");
      prot.putInt("lvl",4);
      enchants.add(prot);
      display.putString("Name","[{\"text\":\"Nul Memento\",\"italic\":false,\"color\":\"black\",\"bold\":true}]");
      tag.put("display",display);
      tag.put("Enchantments",enchants);
      tag.putInt("HideFlags", 255);
      tag.putInt("Unbreakable",1);
      
      setBookLore(makeLore());
      setRecipe(makeRecipe());
      addMagicNbt(tag);
      tag.getCompound("arcananovum").putBoolean("active",false);
      tag.getCompound("arcananovum").putBoolean("onHead",false);
      stack.setNbt(tag);
      setPrefStack(stack);
   }
   
   @Override
   public NbtList getItemLore(@Nullable ItemStack itemStack){
      NbtList loreList = new NbtList();
      loreList.add(NbtString.of("[{\"text\":\"A strange, \",\"italic\":false,\"color\":\"dark_gray\"},{\"text\":\"withered skull\",\"color\":\"gray\"},{\"text\":\", more protective than others you have found.\",\"color\":\"dark_gray\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"The \",\"italic\":false,\"color\":\"dark_gray\"},{\"text\":\"Aspect of Death\",\"color\":\"blue\"},{\"text\":\" has granted you his \"},{\"text\":\"favor\",\"color\":\"gray\"},{\"text\":\".\",\"color\":\"dark_gray\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_gray\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"You have \",\"italic\":false,\"color\":\"dark_gray\"},{\"text\":\"seen \",\"color\":\"blue\"},{\"text\":\"things that most \"},{\"text\":\"mortals \",\"color\":\"gray\"},{\"text\":\"never will.\",\"color\":\"dark_gray\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"The \",\"italic\":false,\"color\":\"dark_gray\"},{\"text\":\"curse of knowledge\",\"color\":\"blue\"},{\"text\":\" binds \"},{\"text\":\"tighter \",\"color\":\"gray\"},{\"text\":\"than any other.\",\"color\":\"dark_gray\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"There are some \",\"italic\":true,\"color\":\"dark_gray\"},{\"text\":\"Skills \",\"color\":\"blue\"},{\"text\":\"that are better left \"},{\"text\":\"forgotten\",\"color\":\"gray\"},{\"text\":\"...\"},{\"text\":\"\",\"italic\":false}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_gray\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"It \",\"italic\":false,\"color\":\"dark_gray\"},{\"text\":\"calls \",\"color\":\"gray\"},{\"text\":\"to your \"},{\"text\":\"mind \",\"color\":\"blue\"},{\"text\":\"with a familiar \"},{\"text\":\"burn of concentration\",\"color\":\"dark_red\"},{\"text\":\".\",\"color\":\"dark_gray\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"It \",\"italic\":true,\"color\":\"dark_gray\"},{\"text\":\"yearns \",\"color\":\"blue\"},{\"text\":\"to be worn... To protect you from \"},{\"text\":\"death\",\"color\":\"gray\"},{\"text\":\"...\",\"italic\":true}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_gray\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"When \",\"italic\":false,\"color\":\"dark_gray\"},{\"text\":\"activated\",\"color\":\"gray\"},{\"text\":\", all \"},{\"text\":\"Skill Points\",\"color\":\"blue\"},{\"text\":\" will be \"},{\"text\":\"deallocated\",\"color\":\"gray\"},{\"text\":\".\"}]"));
      return loreList;
   }
   
   @Override
   public int getMaxEnergy(ItemStack item){
      return 36000 - 12000*Math.max(ArcanaAugments.getAugmentOnItem(item,ArcanaAugments.TEMPO_MORTUUS.id),0); // 30 minutes - 10 per level
   }
   
   public boolean protectFromDeath(ItemStack stack, LivingEntity living, DamageSource source){
      if (source.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY) && !source.isOf(ArcanaDamageTypes.VENGEANCE_TOTEM) && !source.isOf(ArcanaDamageTypes.CONCENTRATION)) {
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
         
         
         dialogHelper.sendDialog(List.of(player),new Dialog(new ArrayList<>(Arrays.asList(
               Text.literal("\n")
                     .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                     .append(Text.literal("Nul").formatted(Formatting.DARK_GRAY,Formatting.BOLD))
                     .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD)),
               Text.literal("")
                     .append(Text.literal("Let my gift offer you a second chance.").formatted(Formatting.DARK_GRAY))
         )),new ArrayList<>(Arrays.asList(new Dialog.DialogSound(SoundEvents.ENTITY_WITHER_AMBIENT,0.3f,0.7f))),new int[]{},1,1,-1),true);
         PLAYER_DATA.get(player).addXP(5000);
      }
      
      setEnergy(stack,getMaxEnergy(stack));
      
      living.setHealth(1.0f);
      living.clearStatusEffects();
      living.addStatusEffect(new StatusEffectInstance(ArcanaRegistry.DEATH_WARD_EFFECT, 300, 0));
      living.addStatusEffect(new StatusEffectInstance(ArcanaRegistry.GREATER_INVISIBILITY_EFFECT, 100, 0));
      living.getWorld().sendEntityStatus(living, EntityStatuses.USE_TOTEM_OF_UNDYING);
      return true;
   }
   
   public boolean isActive(ItemStack item){
      return item.getNbt().getCompound("arcananovum").getBoolean("active");
   }
   
   public void forgor(ItemStack item, ServerPlayerEntity player){
      item.getNbt().getCompound("arcananovum").putBoolean("active",true);
   
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
      int resolve = PLAYER_DATA.get(player).getAugmentLevel(ArcanaAugments.RESOLVE.id);
      final int maxConc = LevelUtils.concFromXp(PLAYER_DATA.get(player).getXP(),resolve);
      DialogHelper dialogHelper = new DialogHelper();
      
      
      
      
      dialogHelper.sendDialog(List.of(player),new Dialog(new ArrayList<>(Arrays.asList(
            Text.literal("\n\n\n\n")
                  .append(Text.literal("As the crushing weight of ").formatted(Formatting.DARK_GRAY))
                  .append(Text.literal("concentration").formatted(Formatting.RED))
                  .append(Text.literal(" takes your mind you hear the ").formatted(Formatting.DARK_GRAY))
                  .append(Text.literal("Nul Memento").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(Text.literal(" whisper...")).formatted(Formatting.DARK_GRAY)
      )),new ArrayList<>(Arrays.asList(new Dialog.DialogSound(SoundEvents.ENTITY_WITHER_AMBIENT,0.3f,0.7f))),new int[]{},1,1,-1),false);

      ArcanaNovum.addTickTimerCallback(new GenericTimer(increments*1, () -> {
         if(cont[0]){
            ItemStack headStack = player.getEquippedStack(EquipmentSlot.HEAD);
            if(!(MagicItemUtils.identifyItem(headStack) instanceof NulMemento) || !(MagicItemUtils.getUsedConcentration(player) > maxConc)){
               cont[0] = false;
               processHalted(player);
            }else{
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
            if(!(MagicItemUtils.identifyItem(headStack) instanceof NulMemento) || !(MagicItemUtils.getUsedConcentration(player) > maxConc)){
               cont[0] = false;
               processHalted(player);
            }else{
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
            if(!(MagicItemUtils.identifyItem(headStack) instanceof NulMemento) || !(MagicItemUtils.getUsedConcentration(player) > maxConc)){
               cont[0] = false;
               processHalted(player);
            }else{
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
            if(!(MagicItemUtils.identifyItem(headStack) instanceof NulMemento) || !(MagicItemUtils.getUsedConcentration(player) > maxConc)){
               cont[0] = false;
               processHalted(player);
            }else{
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
            if(!(MagicItemUtils.identifyItem(headStack) instanceof NulMemento) || !(MagicItemUtils.getUsedConcentration(player) > maxConc)){
               cont[0] = false;
               processHalted(player);
            }else{
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
               player.addStatusEffect(nausea);
               headStack.decrement(headStack.getCount());
               headStack.setNbt(new NbtCompound());
               SoundUtils.playSongToPlayer(player, SoundEvents.ENTITY_ZOMBIE_VILLAGER_CURE, 1f, 1f);
            }
         }
      }));
      ArcanaNovum.addTickTimerCallback(new GenericTimer(increments*6, () -> {
         if(cont[0]){
            dialogHelper.sendDialog(List.of(player),new Dialog(new ArrayList<>(Arrays.asList(
                  Text.literal("\n\n\n\n")
                        .append(Text.literal("All of your Skill Points have been deallocated.").formatted(Formatting.AQUA))
            )),new ArrayList<>(Arrays.asList(new Dialog.DialogSound(SoundEvents.ENTITY_WITHER_AMBIENT,0.3f,0.7f))),new int[]{},1,1,-1),false);
            
            PLAYER_DATA.get(player).removeAllAugments();
            PLAYER_DATA.get(player).addXP(5000);
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
            .append(Text.literal(" becomes too much to bare, perhaps you arent ready...").formatted(Formatting.GRAY,Formatting.ITALIC)), false);
      player.clearStatusEffects();
   }
   
   public void inventoryDialog(ServerPlayerEntity player){
      ArrayList<Dialog> dialogOptions = new ArrayList<>();
      // Conditions: 0 - Crafted Wings, 1 - Crafted Aequalis, 2 - Has Ceptyus Pickaxe, 3 - Has Aequalis, 4 - Has Egg
      boolean[] conditions = new boolean[]{
            PLAYER_DATA.get(player).hasCrafted(ArcanaRegistry.WINGS_OF_ENDERIA),
            PLAYER_DATA.get(player).hasCrafted(ArcanaRegistry.AEQUALIS_SCIENTIA),
            MagicItemUtils.hasItemInInventory(player,ArcanaRegistry.PICKAXE_OF_CEPTYUS.getItem()),
            MagicItemUtils.hasItemInInventory(player,ArcanaRegistry.AEQUALIS_SCIENTIA.getItem()),
            MagicItemUtils.hasItemInInventory(player,Items.DRAGON_EGG),
      };
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Text.literal("\n")
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(Text.literal("Nul").formatted(Formatting.DARK_GRAY,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD)),
            Text.literal("")
                  .append(Text.literal("My Chosen... Will you continue show the courage that your peers lack?").formatted(Formatting.DARK_GRAY))
      )),new ArrayList<>(Arrays.asList(new Dialog.DialogSound(SoundEvents.ENTITY_WITHER_AMBIENT,0.3f,0.7f))),new int[]{},1,1,-1));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Text.literal("\n")
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(Text.literal("Nul").formatted(Formatting.DARK_GRAY,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD)),
            Text.literal("")
                  .append(Text.literal("My dear Mortal, will you help us revitalize these realms to new heights?").formatted(Formatting.DARK_GRAY))
      )),new ArrayList<>(Arrays.asList(new Dialog.DialogSound(SoundEvents.ENTITY_WITHER_AMBIENT,0.3f,0.7f))),new int[]{},1,1,-1));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Text.literal("\n")
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(Text.literal("Nul").formatted(Formatting.DARK_GRAY,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD)),
            Text.literal("")
                  .append(Text.literal("Every mortal faces me once... None have ever faced me twice.").formatted(Formatting.DARK_GRAY))
      )),new ArrayList<>(Arrays.asList(new Dialog.DialogSound(SoundEvents.ENTITY_WITHER_AMBIENT,0.3f,0.7f))),new int[]{},1,1,-1));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Text.literal("\n")
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(Text.literal("Nul").formatted(Formatting.DARK_GRAY,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD)),
            Text.literal("")
                  .append(Text.literal("Take care in the dark secrets you seek. For only I can ever take them away.").formatted(Formatting.DARK_GRAY))
      )),new ArrayList<>(Arrays.asList(new Dialog.DialogSound(SoundEvents.ENTITY_WITHER_AMBIENT,0.3f,0.7f))),new int[]{},1,1,-1));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Text.literal("\n")
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(Text.literal("Nul").formatted(Formatting.DARK_GRAY,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD)),
            Text.literal("")
                  .append(Text.literal("What do you think of my Sister's realm? Do you see the need for my lesson now?").formatted(Formatting.DARK_GRAY))
      )),new ArrayList<>(Arrays.asList(new Dialog.DialogSound(SoundEvents.ENTITY_WITHER_AMBIENT,0.3f,0.7f))),new int[]{},0,1,0));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Text.literal("\n")
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(Text.literal("Nul").formatted(Formatting.DARK_GRAY,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD)),
            Text.literal("")
                  .append(Text.literal("So you've met my kin, Equayus? What sort of exchange did you have?").formatted(Formatting.DARK_GRAY))
      )),new ArrayList<>(Arrays.asList(new Dialog.DialogSound(SoundEvents.ENTITY_WITHER_AMBIENT,0.3f,0.7f))),new int[]{},0,1,1));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Text.literal("\n")
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(Text.literal("Nul").formatted(Formatting.DARK_GRAY,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD)),
            Text.literal("")
                  .append(Text.literal("I sense my kin's Arcana on you... I trust your deal was worthwhile.").formatted(Formatting.DARK_GRAY))
      )),new ArrayList<>(Arrays.asList(new Dialog.DialogSound(SoundEvents.ENTITY_WITHER_AMBIENT,0.3f,0.7f))),new int[]{},0,1,1));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Text.literal("\n")
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(Text.literal("Nul").formatted(Formatting.DARK_GRAY,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD)),
            Text.literal("")
                  .append(Text.literal("Equayus and I might not always see eye to eye, but we both have these realm's best intentions at heart.").formatted(Formatting.DARK_GRAY))
      )),new ArrayList<>(Arrays.asList(new Dialog.DialogSound(SoundEvents.ENTITY_WITHER_AMBIENT,0.3f,0.7f))),new int[]{},0,1,1));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Text.literal("\n")
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(Text.literal("Nul").formatted(Formatting.DARK_GRAY,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD)),
            Text.literal("")
                  .append(Text.literal("A relic of Ceptyus?! How fascinating! I wonder if it was forgotten as they fled.").formatted(Formatting.DARK_GRAY))
      )),new ArrayList<>(Arrays.asList(new Dialog.DialogSound(SoundEvents.ENTITY_WITHER_AMBIENT,0.3f,0.7f))),new int[]{},0,1,2));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Text.literal("\n")
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(Text.literal("Nul").formatted(Formatting.DARK_GRAY,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(Text.literal("\nEquayus? Is that you? It has been so long... What have you been doing in this time?\n").formatted(Formatting.DARK_GRAY)),
            Text.literal("")
                  .append(Text.literal(" ~ ").formatted(Formatting.DARK_AQUA,Formatting.BOLD))
                  .append(Text.literal("Equayus").formatted(Formatting.AQUA,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.DARK_AQUA,Formatting.BOLD))
                  .append(Text.literal("\nBrother, it is good to hear your voice again...").formatted(Formatting.AQUA))
      )),new ArrayList<>(Arrays.asList(
            new Dialog.DialogSound(SoundEvents.ENTITY_WITHER_AMBIENT,0.3f,0.7f),
            new Dialog.DialogSound(SoundEvents.ENTITY_ALLAY_AMBIENT_WITHOUT_ITEM,0.5f,0.7f))
      ),new int[]{0,60},0,1,3));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Text.literal("\n")
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(Text.literal("Nul").formatted(Formatting.DARK_GRAY,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(Text.literal("\nSo you too, have taken an interest in the Mortals now? Do you now share my vision?\n").formatted(Formatting.DARK_GRAY)),
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
      ),new int[]{0,60,60},0,1,3));
      
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
                  .append(Text.literal("\nIt was not my actions that led to this. The Mortals did it all themselves. Perhaps in time your arrogance will turn to humility.").formatted(Formatting.DARK_GRAY))
      )),new ArrayList<>(Arrays.asList(
            new Dialog.DialogSound(SoundEvents.ENTITY_WITHER_AMBIENT,0.3f,0.7f),
            new Dialog.DialogSound(SoundEvents.ENTITY_ENDER_DRAGON_GROWL,0.5f,1.4f),
            new Dialog.DialogSound(SoundEvents.ENTITY_WITHER_AMBIENT,0.3f,0.7f))
      ),new int[]{0,60,60},0,1,4));
      
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
                  .append(Text.literal("\nThen I suppose your mistake was defying Tenbrous, and ascending to take their place.\n").formatted(Formatting.DARK_GRAY)),
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
      ),new int[]{0,80,100,80,60},0,1,4));
      
      
      DialogHelper helper = new DialogHelper(dialogOptions,conditions);
      helper.sendDialog(List.of(player),helper.getWeightedResult(),true);
   }
   
   private MagicItemRecipe makeRecipe(){
      ItemStack pane = new ItemStack(Items.GRAY_STAINED_GLASS_PANE).setCustomName(Text.literal("In World Recipe").formatted(Formatting.BLUE,Formatting.BOLD));
      MiscUtils.addLoreLine(pane,Text.literal("Build this in the World").formatted(Formatting.DARK_PURPLE));
      
      ItemStack soulSand = new ItemStack(Items.SOUL_SAND).setCustomName(Text.literal("Soul Sand or Soil").formatted(Formatting.GRAY,Formatting.BOLD));
      MiscUtils.addLoreLine(soulSand,Text.literal("Construct a Wither Base with a heart of Netherite").formatted(Formatting.DARK_PURPLE));
      
      ItemStack skull = new ItemStack(Items.WITHER_SKELETON_SKULL).setCustomName(Text.literal("Eye of Ender").formatted(Formatting.DARK_GRAY,Formatting.BOLD));
      MiscUtils.addLoreLine(skull,Text.literal("Construct a Wither Base with a heart of Netherite").formatted(Formatting.DARK_PURPLE));
      
      ItemStack netherite = new ItemStack(Items.NETHERITE_BLOCK).setCustomName(Text.literal("Block of Netherite").formatted(Formatting.DARK_RED,Formatting.BOLD));
      MiscUtils.addLoreLine(netherite,Text.literal("Construct a Wither Base with a heart of Netherite").formatted(Formatting.DARK_PURPLE));
      
      ItemStack catalyst = ArcanaRegistry.MYTHICAL_CATALYST.getItem().getDefaultStack().copy();
      MiscUtils.addLoreLine(catalyst,Text.literal("")
            .append(Text.literal("Right Click").formatted(Formatting.BLUE))
            .append(Text.literal(" the ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("Catalyst").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal(" on the ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("Netherite Heart").formatted(Formatting.DARK_RED)));
      MiscUtils.addLoreLine(catalyst,Text.literal("")
            .append(Text.literal("Divine Energy").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal(" will flow into the ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("Mythical Construct").formatted(Formatting.DARK_GRAY))
            .append(Text.literal(" empowering it").formatted(Formatting.DARK_PURPLE)));
      MiscUtils.addLoreLine(catalyst,Text.literal("")
            .append(Text.literal("Defeat the ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("Mythical Construct").formatted(Formatting.DARK_GRAY))
            .append(Text.literal(" without dying to have a chance at receiving a ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("Nul Memento").formatted(Formatting.BLACK)));
      MiscUtils.addLoreLine(catalyst,Text.literal("").formatted(Formatting.DARK_PURPLE));
      MiscUtils.addLoreLine(catalyst,Text.literal("WARNING!!! This fight is considerably harder than a Nul Construct. Attempt at your own peril.").formatted(Formatting.RED));
      
      ExplainIngredient a = new ExplainIngredient(pane,"",false);
      ExplainIngredient s = new ExplainIngredient(soulSand,"Soul Sand or Soil");
      ExplainIngredient k = new ExplainIngredient(skull,"Wither Skeleton Skull");
      ExplainIngredient n = new ExplainIngredient(netherite,"Netherite Block");
      ExplainIngredient c = new ExplainIngredient(catalyst,"Mythical Augment Catalyst");
      
      ExplainIngredient[][] ingredients = {
            {a,a,a,a,a},
            {a,k,k,k,a},
            {a,s,n,s,c},
            {a,a,s,a,a},
            {a,a,a,a,a}};
      return new ExplainRecipe(ingredients);
   }
   
   private List<String> makeLore(){
      ArrayList<String> list = new ArrayList<>();
      list.add("\"      Nul Memento\\n\\nRarity: Mythical\\n\\nThis entity of death that I have acquired a passing familiarity with is most intriguing.\\n\\nHe wanted me to prove my fighting prowess by dueling his creation, and I believe I succeeded.\"");
      list.add("\"      Nul Memento\\n\\nAs I was gifted this strange skull, the entity informed me that I have become one of his 'chosen'.\\nI'm not sure what to think of this. What machinations could a deity of death be planning such that he needs to choose mortals like me?\"");
      list.add("\"      Nul Memento\\n\\nThe Memento whispers to me every so often. I have come to learn the entity calls himself Nul, the God of Death.\\n\\nHe speaks of Arcana, and secrets that I have yet to learn.\\n\\nHe warns that one mind can only hold so\"");
      list.add("\"      Nul Memento\\n\\nmuch knowledge at one time. However, he offers his aid in circumventing this mortal limitation.\\n\\nThis Memento reacts to an overburdened mind when worn, and will make me forget some of the skills I have learned.\"");
      list.add("\"      Nul Memento\\n\\nAs long as I use those skills before forgetting them. I should be able to take advantage of new knowledge with a new limit to what I can learn.\\n\\nThe Memento also offers incredible protection, as if it was\"");
      list.add("\"      Nul Memento\\n\\nmade of enchanted Netherite!\\n\\nI believe wearing it may also encourage Nul to save me from any unfortunate circumstances that find me near death.\"");
      return list;
   }
   
   public class NulMementoItem extends MagicPolymerArmorItem {
      public NulMementoItem(Settings settings){
         super(getThis(),ArmorMaterials.NETHERITE,Type.HELMET,settings);
      }
      
      @Override
      public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player){
         if(MagicItemUtils.isMagic(itemStack)){
            NbtCompound itemNbt = itemStack.getNbt();
            NbtCompound magicNbt = itemNbt.getCompound("arcananovum");
            boolean onHead = magicNbt.getBoolean("onHead");
            if(onHead && PolymerResourcePackUtils.hasMainPack(player)) return textureItem;
         }
         return super.getPolymerItem(itemStack, player);
      }
      
      @Override
      public int getPolymerCustomModelData(ItemStack itemStack, @Nullable ServerPlayerEntity player){
         return ArcanaRegistry.MODELS.get(TXT+"@"+getPolymerItem(itemStack,player).getTranslationKey()).value();
      }
      
      @Override
      public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected){
         if(!MagicItemUtils.isMagic(stack)) return;
         if(!(world instanceof ServerWorld && entity instanceof ServerPlayerEntity player)) return;
         
         NbtCompound itemNbt = stack.getNbt();
         NbtCompound magicNbt = itemNbt.getCompound("arcananovum");
         boolean nowOnHead = player.getEquippedStack(EquipmentSlot.HEAD).equals(stack);
         boolean wasOnHead = magicNbt.getBoolean("onHead");
         if(nowOnHead != wasOnHead){
            magicNbt.putBoolean("onHead",nowOnHead);
         }
         if(nowOnHead && getEnergy(stack) > 0){
            addEnergy(stack,-1);
         }
         
         // 0.000015 ~ 60 minutes between voice lines
         if(Math.random() < 0.000015){
            inventoryDialog(player);
         }
      }
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
   }
}
