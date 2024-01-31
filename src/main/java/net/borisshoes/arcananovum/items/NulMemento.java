package net.borisshoes.arcananovum.items;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.MagicItem;
import net.borisshoes.arcananovum.core.polymer.MagicPolymerArmorItem;
import net.borisshoes.arcananovum.recipes.arcana.*;
import net.borisshoes.arcananovum.utils.*;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;

public class NulMemento extends MagicItem {
   
   private static final String TXT = "item/nul_memento";
   
   public NulMemento(){
      id = "nul_memento";
      name = "Nul Memento";
      rarity = MagicRarity.MYTHICAL;
      categories = new ArcaneTome.TomeFilter[]{ArcaneTome.TomeFilter.MYTHICAL, ArcaneTome.TomeFilter.ITEMS, ArcaneTome.TomeFilter.EQUIPMENT};
      vanillaItem = Items.WITHER_SKELETON_SKULL;
      item = new NulMementoItem(new FabricItemSettings().maxCount(1).fireproof());
      models = new ArrayList<>();
      models.add(new Pair<>(vanillaItem,TXT));
      
      ItemStack stack = new ItemStack(item);
      NbtCompound tag = stack.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList enchants = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name","[{\"text\":\"Nul Memento\",\"italic\":false,\"color\":\"black\",\"bold\":true}]");
      tag.put("display",display);
      tag.put("Enchantments",enchants);
      tag.putInt("HideFlags", 255);
      tag.putInt("Unbreakable",1);
      buildItemLore(stack, ArcanaNovum.SERVER);

      setBookLore(makeLore());
      setRecipe(makeRecipe());
      
      tag = this.addMagicNbt(tag);
      tag.getCompound("arcananovum").putBoolean("active",false);
      prefNBT = tag;
      
      stack.setNbt(prefNBT);
      prefItem = stack;
   }
   
   @Override
   public NbtList getItemLore(@Nullable ItemStack itemStack){
      NbtList loreList = new NbtList();
      loreList.add(NbtString.of("[{\"text\":\"A strange, \",\"italic\":false,\"color\":\"dark_gray\"},{\"text\":\"withered skull\",\"color\":\"gray\"},{\"text\":\", unlike others you have encountered.\",\"color\":\"dark_gray\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"The \",\"italic\":false,\"color\":\"dark_gray\"},{\"text\":\"Aspect of Death\",\"color\":\"blue\"},{\"text\":\" has granted you his \"},{\"text\":\"favor\",\"color\":\"gray\"},{\"text\":\".\",\"color\":\"dark_gray\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_gray\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"You have \",\"italic\":false,\"color\":\"dark_gray\"},{\"text\":\"seen \",\"color\":\"blue\"},{\"text\":\"things that most \"},{\"text\":\"mortals \",\"color\":\"gray\"},{\"text\":\"never will.\",\"color\":\"dark_gray\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"The \",\"italic\":false,\"color\":\"dark_gray\"},{\"text\":\"curse of knowledge\",\"color\":\"blue\"},{\"text\":\" binds \"},{\"text\":\"tighter \",\"color\":\"gray\"},{\"text\":\"than any other.\",\"color\":\"dark_gray\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"There are some \",\"italic\":true,\"color\":\"dark_gray\"},{\"text\":\"Skills \",\"color\":\"blue\"},{\"text\":\"that are better left \"},{\"text\":\"forgotten\",\"color\":\"gray\"},{\"text\":\"...\"},{\"text\":\"\",\"italic\":false}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_gray\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"It \",\"italic\":false,\"color\":\"dark_gray\"},{\"text\":\"calls \",\"color\":\"gray\"},{\"text\":\"to your \"},{\"text\":\"mind \",\"color\":\"blue\"},{\"text\":\"with a familiar \"},{\"text\":\"burn of concentration\",\"color\":\"dark_red\"},{\"text\":\".\",\"color\":\"dark_gray\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"It \",\"italic\":true,\"color\":\"dark_gray\"},{\"text\":\"yearns \",\"color\":\"blue\"},{\"text\":\"to be worn...\"},{\"text\":\"\",\"italic\":false}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_gray\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"When \",\"italic\":false,\"color\":\"dark_gray\"},{\"text\":\"activated\",\"color\":\"gray\"},{\"text\":\", all \"},{\"text\":\"Skill Points\",\"color\":\"blue\"},{\"text\":\" will be \"},{\"text\":\"deallocated\",\"color\":\"gray\"},{\"text\":\".\"}]"));
      return loreList;
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
   
      player.sendMessage(Text.literal(""),false);
      player.sendMessage(Text.literal(""),false);
      player.sendMessage(Text.literal(""),false);
      player.sendMessage(Text.literal("")
            .append(Text.literal("As the crushing weight of ").formatted(Formatting.DARK_GRAY))
            .append(Text.literal("concentration").formatted(Formatting.RED))
            .append(Text.literal(" takes your mind you hear the ").formatted(Formatting.DARK_GRAY))
            .append(Text.literal("Nul Memento").formatted(Formatting.BLACK,Formatting.BOLD))
            .append(Text.literal(" whisper...")).formatted(Formatting.DARK_GRAY), false);

      ArcanaNovum.addTickTimerCallback(new GenericTimer(increments*1, () -> {
         if(cont[0]){
            ItemStack headStack = player.getEquippedStack(EquipmentSlot.HEAD);
            if(!(MagicItemUtils.identifyItem(headStack) instanceof NulMemento) || !(MagicItemUtils.getUsedConcentration(player) > maxConc)){
               cont[0] = false;
               processHalted(player);
            }else{
               player.sendMessage(Text.literal(""),false);
               player.sendMessage(Text.literal(""),false);
               player.sendMessage(Text.literal(""),false);
               player.sendMessage(Text.literal("Feel the weight, embrace it... let me in...").formatted(Formatting.DARK_GRAY, Formatting.ITALIC), false);
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
               player.sendMessage(Text.literal(""),false);
               player.sendMessage(Text.literal(""),false);
               player.sendMessage(Text.literal(""),false);
               player.sendMessage(Text.literal("")
                     .append(Text.literal("You feel as though your ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("skull").formatted(Formatting.GRAY))
                     .append(Text.literal(" is about to ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("collapse").formatted(Formatting.RED))
                     .append(Text.literal(" when a ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("gentle breeze").formatted(Formatting.GRAY))
                     .append(Text.literal(" sweeps through you.").formatted(Formatting.DARK_GRAY)), false);
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
               player.sendMessage(Text.literal(""),false);
               player.sendMessage(Text.literal(""),false);
               player.sendMessage(Text.literal(""),false);
               player.sendMessage(Text.literal("Your secrets are safe with me. Be free of this burden, for I now bear it alone.").formatted(Formatting.DARK_GRAY, Formatting.ITALIC), false);
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
               player.sendMessage(Text.literal(""),false);
               player.sendMessage(Text.literal(""),false);
               player.sendMessage(Text.literal(""),false);
               player.sendMessage(Text.literal("That is, until we meet again...").formatted(Formatting.DARK_GRAY, Formatting.ITALIC), false);
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
               player.sendMessage(Text.literal(""),false);
               player.sendMessage(Text.literal(""),false);
               player.sendMessage(Text.literal(""),false);
               player.sendMessage(Text.literal("")
                     .append(Text.literal("The ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("Nul Memento").formatted(Formatting.BLACK,Formatting.BOLD))
                     .append(Text.literal(" crumbles ").formatted(Formatting.GRAY))
                     .append(Text.literal("into").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal(" ash ").formatted(Formatting.GRAY))
                     .append(Text.literal("around your head, your mind still ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("burning").formatted(Formatting.RED))
                     .append(Text.literal(" from the ").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("overwhelming ").formatted(Formatting.RED))
                     .append(Text.literal("Arcana.").formatted(Formatting.LIGHT_PURPLE)), false);
               StatusEffectInstance nausea = new StatusEffectInstance(StatusEffects.NAUSEA,200, 4, false, false, true);
               player.addStatusEffect(nausea);
            }
         }
      }));
      ArcanaNovum.addTickTimerCallback(new GenericTimer(increments*6, () -> {
         if(cont[0]){
            ItemStack headStack = player.getEquippedStack(EquipmentSlot.HEAD);
            if(!(MagicItemUtils.identifyItem(headStack) instanceof NulMemento) || !(MagicItemUtils.getUsedConcentration(player) > maxConc)){
               cont[0] = false;
               processHalted(player);
            }else{
               player.sendMessage(Text.literal(""),false);
               player.sendMessage(Text.literal(""),false);
               player.sendMessage(Text.literal(""),false);
               player.sendMessage(Text.literal("All of your Skill Points have been deallocated").formatted(Formatting.AQUA), false);
               headStack.decrement(headStack.getCount());
               headStack.setNbt(new NbtCompound());
               PLAYER_DATA.get(player).removeAllAugments();
               SoundUtils.playSongToPlayer(player, SoundEvents.ENTITY_ZOMBIE_VILLAGER_CURE, 1f, 1f);
               
               ArcanaAchievements.grant(player,ArcanaAchievements.LOST_KNOWLEDGE.id);
               ArcanaAchievements.progress(player,ArcanaAchievements.AMNESIAC.id,1);
            }
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
      list.add("\"      Nul Memento\\n\\nAs long as I use those skills before forgetting them. I should be able to take advantage of new knowledge with a new limit to what I can learn.\"");
      return list;
   }
   
   public class NulMementoItem extends MagicPolymerArmorItem {
      public NulMementoItem(Settings settings){
         super(getThis(),ArcanaRegistry.NON_PROTECTIVE_ARMOR_MATERIAL,Type.HELMET,settings);
      }
      
      @Override
      public int getPolymerCustomModelData(ItemStack itemStack, @Nullable ServerPlayerEntity player){
         return ArcanaRegistry.MODELS.get(TXT).value();
      }
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
   }
}
