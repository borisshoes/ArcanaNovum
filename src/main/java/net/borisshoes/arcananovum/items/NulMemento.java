package net.borisshoes.arcananovum.items;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.MagicItem;
import net.borisshoes.arcananovum.core.polymer.MagicPolymerArmorItem;
import net.borisshoes.arcananovum.recipes.arcana.ForgeRequirement;
import net.borisshoes.arcananovum.recipes.arcana.GenericMagicIngredient;
import net.borisshoes.arcananovum.recipes.arcana.MagicItemIngredient;
import net.borisshoes.arcananovum.recipes.arcana.MagicItemRecipe;
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
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;

public class NulMemento extends MagicItem {
   
   public NulMemento(){
      id = "nul_memento";
      name = "Nul Memento";
      rarity = MagicRarity.LEGENDARY;
      categories = new ArcaneTome.TomeFilter[]{ArcaneTome.TomeFilter.LEGENDARY, ArcaneTome.TomeFilter.ITEMS};
      vanillaItem = Items.WITHER_SKELETON_SKULL;
      item = new NulMementoItem(new FabricItemSettings().maxCount(1).fireproof());
      
      ItemStack stack = new ItemStack(item);
      NbtCompound tag = stack.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList loreList = new NbtList();
      NbtList enchants = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name","[{\"text\":\"Nul Memento\",\"italic\":false,\"color\":\"black\",\"bold\":true}]");
      tag.put("display",display);
      tag.put("Enchantments",enchants);
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
      loreList.add(NbtString.of("[{\"text\":\"A \",\"italic\":false,\"color\":\"dark_gray\"},{\"text\":\"skull \",\"color\":\"gray\"},{\"text\":\"forged out of the \"},{\"text\":\"discoveries \",\"color\":\"blue\"},{\"text\":\"you have made\",\"color\":\"dark_gray\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"You have \",\"italic\":false,\"color\":\"dark_gray\"},{\"text\":\"seen\",\"color\":\"blue\"},{\"text\":\" things that most \"},{\"text\":\"mortals \",\"color\":\"gray\"},{\"text\":\"never will\",\"color\":\"dark_gray\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"The \",\"italic\":false,\"color\":\"dark_gray\"},{\"text\":\"curse of knowledge\",\"color\":\"blue\"},{\"text\":\" binds \"},{\"text\":\"tighter \",\"color\":\"gray\"},{\"text\":\"than any other\",\"color\":\"dark_gray\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"What has been \",\"italic\":true,\"color\":\"dark_gray\"},{\"text\":\"learned \",\"color\":\"blue\"},{\"text\":\"must now be \"},{\"text\":\"forgotten\",\"color\":\"gray\"},{\"text\":\"...\"},{\"text\":\"\",\"italic\":false}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_gray\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"It \",\"italic\":false,\"color\":\"dark_gray\"},{\"text\":\"calls \",\"color\":\"gray\"},{\"text\":\"to your \"},{\"text\":\"mind \",\"color\":\"blue\"},{\"text\":\"with a familiar \"},{\"text\":\"burn of concentration\",\"color\":\"dark_red\"},{\"text\":\".\",\"color\":\"dark_gray\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"It yearns to be worn...\",\"italic\":true,\"color\":\"dark_gray\"},{\"text\":\"\",\"italic\":false,\"color\":\"dark_gray\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_gray\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"When \",\"italic\":false,\"color\":\"dark_gray\"},{\"text\":\"activated\",\"color\":\"gray\"},{\"text\":\", all \"},{\"text\":\"Skill Points\",\"color\":\"blue\"},{\"text\":\" will be \"},{\"text\":\"deallocated\",\"color\":\"gray\"},{\"text\":\".\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
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
      GenericMagicIngredient a = new GenericMagicIngredient(ArcanaRegistry.EMPOWERED_CATALYST,1);
      GenericMagicIngredient b = new GenericMagicIngredient(ArcanaRegistry.EXOTIC_CATALYST,1);
      GenericMagicIngredient r = new GenericMagicIngredient(ArcanaRegistry.ARCANE_TOME,1);
      GenericMagicIngredient c = new GenericMagicIngredient(ArcanaRegistry.LEGENDARY_CATALYST,1);
      GenericMagicIngredient d = new GenericMagicIngredient(ArcanaRegistry.MUNDANE_CATALYST,1);
      GenericMagicIngredient g = new GenericMagicIngredient(ArcanaRegistry.MYTHICAL_CATALYST,1);
      GenericMagicIngredient h = new GenericMagicIngredient(ArcanaRegistry.EXOTIC_MATTER,1);
      GenericMagicIngredient l = new GenericMagicIngredient(ArcanaRegistry.RUNIC_MATRIX,1);
      MagicItemIngredient m = new MagicItemIngredient(Items.WITHER_SKELETON_SKULL,64,null,true);
      GenericMagicIngredient n = new GenericMagicIngredient(ArcanaRegistry.TEMPORAL_MOMENT,1);
   
      MagicItemIngredient[][] ingredients = {
            {a,b,c,d,a},
            {d,g,h,g,b},
            {c,l,m,n,c},
            {b,g,r,g,d},
            {a,d,c,b,a}};
      return new MagicItemRecipe(ingredients, new ForgeRequirement().withSingularity());
   }
   
   private List<String> makeLore(){
      ArrayList<String> list = new ArrayList<>();
      list.add("{\"text\":\"      Nul Memento\\n\\nRarity: Legendary\\n\\nMy mind feels so full of the knowledge I've uncovered. I feel like I can't learn any more, I need a way to free my head so that I can continue my studies... Was that wither skull on my desk always glowing?\\n\"}");
      list.add("{\"text\":\"      Nul Memento\\n\\nThe skull whispers to me, it says it can help me, but I need to prove my knowledge.\\n\\nI know not what entity speaks to me, but my curiosity is piqued.\\nI shall show it all I have mastered in the world of Arcana...\\n\"}");
      list.add("{\"text\":\"      Nul Memento\\n\\nThe Skull has accepted my offering.\\nIt calls itself 'Nul' and it hungers for knowledge to fill the void in its soul. Nul says that I have made a Memento of his, it will take the knowledge I no longer wish to have, but the process is demanding.\"}");
      list.add("{\"text\":\"      Nul Memento\\n\\nThe Nul Memento must be activated when worn by flooding my mind with Arcana. The only way I know how to do that is the painful process of overexerting my concentration. The process takes a while, so I better have some healing on hand.\"}");
      return list;
   }
   
   public class NulMementoItem extends MagicPolymerArmorItem {
      public NulMementoItem(Settings settings){
         super(getThis(),ArcanaRegistry.NON_PROTECTIVE_ARMOR_MATERIAL,Type.HELMET,settings);
      }
      
      
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
   }
}
