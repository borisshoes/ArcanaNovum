package net.borisshoes.arcananovum.gui.arcanetome;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.borisshoes.arcananovum.Arcananovum;
import net.borisshoes.arcananovum.cardinalcomponents.IArcanaProfileComponent;
import net.borisshoes.arcananovum.items.ArcaneTome;
import net.borisshoes.arcananovum.items.core.MagicItem;
import net.borisshoes.arcananovum.augments.ArcanaAugment;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.items.core.MagicItems;
import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.borisshoes.arcananovum.utils.MagicRarity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.InventoryChangedListener;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;

public class TinkerInventoryListener implements InventoryChangedListener {
   private final ArcaneTome tome;
   private final TomeGui gui;
   private boolean updating = false;
   private final int[][] dynamicSlots = {{},{3},{1,5},{1,3,5},{0,2,4,6},{1,2,3,4,5},{0,1,2,4,5,6},{0,1,2,3,4,5,6}};
   
   public TinkerInventoryListener(ArcaneTome tome, TomeGui gui){
      this.tome = tome;
      this.gui = gui;
   }
   
   @Override
   public void onInventoryChanged(Inventory inv){
      if(!updating){
         updating = true;
         for(int i = 0; i < inv.size(); i++){
            ItemStack stack = inv.getStack(i);
            if(stack.getCount() != 0){
               //System.out.println("Slot " + i + ": " + stack.getItem().getName().getString() + " (" + stack.getCount() + ")");
            }
         }
         //Update gui
         redraw(inv);
         finishUpdate();
      }
   }
   
   private void redraw(Inventory inv){
      ItemStack item = inv.getStack(0);
      MagicItem magicItem = MagicItemUtils.identifyItem(item);
   
      ItemStack augmentPane;
      if(magicItem == null){
         augmentPane = new ItemStack(Items.BLACK_STAINED_GLASS_PANE);
      }else{
         augmentPane = new ItemStack(Items.WHITE_STAINED_GLASS_PANE);
      }
      NbtCompound tag = augmentPane.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList loreList = new NbtList();
      display.putString("Name","[{\"text\":\"Augments:\",\"italic\":false,\"color\":\"dark_purple\"}]");
      loreList.add(NbtString.of("[{\"text\":\"Unlocked augments can be applied to enhance Magic Items!\",\"color\":\"light_purple\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
      tag.putInt("HideFlags",103);
      
      for(int i = 0; i < 7; i++){
         gui.setSlot(28+i,GuiElementBuilder.from(augmentPane));
         gui.setSlot(37+i,GuiElementBuilder.from(augmentPane));
      }
      
      ItemStack itemWindow;
      if(magicItem == null){
         itemWindow = new ItemStack(Items.BLACK_STAINED_GLASS_PANE);
      }else{
         ServerPlayerEntity player = gui.getPlayer();
         IArcanaProfileComponent profile = PLAYER_DATA.get(player);
         itemWindow = new ItemStack(Items.WHITE_STAINED_GLASS_PANE);
         
         boolean generic = magicItem.getId().equals(MagicItems.ARCANE_TOME.getId());
         
         List<ArcanaAugment> augments = ArcanaAugments.getAugmentsForItem(magicItem);
         int[] augmentSlots = dynamicSlots[augments.size()];
         for(int i = 0; i < augmentSlots.length; i++){
            ArcanaAugment augment = augments.get(i);
            gui.clearSlot(28+augmentSlots[i]);
            gui.clearSlot(37+augmentSlots[i]);
   
            int augmentLvl = profile.getAugmentLevel(augment.id);
            MagicRarity[] tiers = augment.getTiers();
   
            GuiElementBuilder augmentItem1 = new GuiElementBuilder(augment.getDisplayItem().getItem());
            augmentItem1.hideFlags().setName(Text.literal(augment.name).formatted(Formatting.DARK_PURPLE)).addLoreLine(augment.getTierDisplay());
   
            for(String s : augment.getDescription()){
               augmentItem1.addLoreLine(Text.literal(s).formatted(Formatting.GRAY));
            }
            if(augmentLvl > 0) augmentItem1.glow();
            
            int curItemLevel = ArcanaAugments.getAugmentOnItem(item,augment.id);
            if(curItemLevel == -2){
               Arcananovum.log(3,"Magic item errored in Tinker's Screen: "+magicItem.getId());
            }else if(curItemLevel == -1) curItemLevel = 0;
   
            GuiElementBuilder augmentItem2;
            if(generic && !augment.id.equals(ArcanaAugments.SKILL.id)){ // Generic
               augmentItem2 = new GuiElementBuilder(Items.TINTED_GLASS);
               augmentItem2.hideFlags().glow().setName(
                     Text.literal("Generic Augmentation").formatted(Formatting.DARK_PURPLE));
               augmentItem2.addLoreLine(Text.literal("")
                     .append(Text.literal("These augments are always active").formatted(Formatting.AQUA)));
               augmentItem2.addLoreLine(Text.literal("")
                     .append(Text.literal("You do not need to augment your Tome to receive their boons").formatted(Formatting.AQUA)));
            }else if(curItemLevel >= tiers.length){ // Item Level = max: End Crystal
               augmentItem2 = new GuiElementBuilder(Items.END_CRYSTAL);
               augmentItem2.hideFlags().glow().setName(
                     Text.literal("Level ").formatted(Formatting.DARK_PURPLE)
                           .append(Text.literal(""+curItemLevel).formatted(Formatting.LIGHT_PURPLE)));
               augmentItem2.addLoreLine(Text.literal("")
                     .append(Text.literal("Max Level").formatted(Formatting.AQUA)));
            }else if(augmentLvl == 0 && curItemLevel == 0){ // Item & player lvl = 0: Obsidian
               augmentItem2 = new GuiElementBuilder(Items.OBSIDIAN);
               augmentItem2.hideFlags().glow().setName(
                     Text.literal("Not Augmented").formatted(Formatting.DARK_PURPLE));
               augmentItem2.addLoreLine(Text.literal("")
                     .append(Text.literal("Augment Locked!").formatted(Formatting.DARK_RED)));
               augmentItem2.addLoreLine(Text.literal(""));
               augmentItem2.addLoreLine(Text.literal("")
                     .append(Text.literal("Spend ").formatted(Formatting.DARK_AQUA))
                     .append(Text.literal("Skill Points").formatted(Formatting.AQUA))
                     .append(Text.literal(" to unlock this augment").formatted(Formatting.DARK_AQUA)));
               augmentItem2.addLoreLine(Text.literal("")
                     .append(Text.literal("Unlock augments on the item's page").formatted(Formatting.DARK_AQUA)));
            }else if(curItemLevel >= augmentLvl){ // Item level != max & >= player level: Obsidian
               augmentItem2 = new GuiElementBuilder(Items.OBSIDIAN);
               augmentItem2.hideFlags().glow().setName(
                     Text.literal("Current Level: ").formatted(Formatting.DARK_PURPLE)
                           .append(Text.literal(""+curItemLevel).formatted(Formatting.LIGHT_PURPLE)));
               augmentItem2.addLoreLine(Text.literal("")
                     .append(Text.literal("You have only unlocked level ").formatted(Formatting.DARK_RED))
                     .append(Text.literal(""+augmentLvl).formatted(Formatting.RED)));
               augmentItem2.addLoreLine(Text.literal(""));
               augmentItem2.addLoreLine(Text.literal("")
                     .append(Text.literal("Spend ").formatted(Formatting.DARK_AQUA))
                     .append(Text.literal("Skill Points").formatted(Formatting.AQUA))
                     .append(Text.literal(" to unlock higher levels").formatted(Formatting.DARK_AQUA)));
               augmentItem2.addLoreLine(Text.literal("")
                     .append(Text.literal("Unlock augments on the item's page").formatted(Formatting.DARK_AQUA)));
            }else if(ArcanaAugments.isIncompatible(item,augment.id)){ // Incompatible augment: Structure Void
               augmentItem2 = new GuiElementBuilder(Items.STRUCTURE_VOID);
               augmentItem2.hideFlags().glow().setName(
                     Text.literal("Incompatible Augment").formatted(Formatting.DARK_PURPLE));
               augmentItem2.addLoreLine(Text.literal("")
                     .append(Text.literal("This augment is incompatible with present augments").formatted(Formatting.DARK_RED)));
            }else if(curItemLevel == 0 || curItemLevel == -1){ // Item level = 0: Augment Catalyst
               augmentItem2 = new GuiElementBuilder(MagicRarity.getAugmentCatalyst(tiers[0]).getPrefItem().getItem());
               augmentItem2.hideFlags().setName(
                     Text.literal("Not Augmented").formatted(Formatting.DARK_PURPLE));
               augmentItem2.addLoreLine(Text.literal("")
                     .append(Text.literal("Level: ").formatted(Formatting.BLUE))
                     .append(Text.literal("1").formatted(Formatting.DARK_AQUA))
                     .append(Text.literal(" (").formatted(Formatting.BLUE))
                     .append(MagicRarity.getColoredLabel(tiers[0],false))
                     .append(Text.literal(")").formatted(Formatting.BLUE)));
               augmentItem2.addLoreLine(Text.literal(""));
               augmentItem2.addLoreLine(Text.literal("")
                     .append(Text.literal("Applying augments requires an ").formatted(Formatting.DARK_AQUA))
                     .append(Text.literal("Augment Catalyst").formatted(MagicRarity.getColor(tiers[0]))));
               augmentItem2.addLoreLine(Text.literal("")
                     .append(Text.literal("Click").formatted(Formatting.AQUA))
                     .append(Text.literal(" to consume a ").formatted(Formatting.DARK_AQUA))
                     .append(Text.literal("Catalyst").formatted(MagicRarity.getColor(tiers[0])))
                     .append(Text.literal(" to augment your item").formatted(Formatting.DARK_AQUA)));
            }else{ // Item level != max & < player level: Augment Catalyst
               augmentItem2 = new GuiElementBuilder(MagicRarity.getAugmentCatalyst(tiers[curItemLevel]).getPrefItem().getItem());
               augmentItem2.hideFlags().setName(
                     Text.literal("Current Level: ").formatted(Formatting.DARK_PURPLE)
                           .append(Text.literal(""+curItemLevel).formatted(Formatting.LIGHT_PURPLE)));
               augmentItem2.addLoreLine(Text.literal("")
                     .append(Text.literal("Next Level: ").formatted(Formatting.BLUE))
                     .append(Text.literal((curItemLevel+1)+"").formatted(Formatting.DARK_AQUA))
                     .append(Text.literal(" (").formatted(Formatting.BLUE))
                     .append(MagicRarity.getColoredLabel(tiers[curItemLevel],false))
                     .append(Text.literal(")").formatted(Formatting.BLUE)));
               augmentItem2.addLoreLine(Text.literal(""));
               augmentItem2.addLoreLine(Text.literal("")
                     .append(Text.literal("Applying augments requires an ").formatted(Formatting.DARK_AQUA))
                     .append(Text.literal("Augment Catalyst").formatted(MagicRarity.getColor(tiers[curItemLevel]))));
               augmentItem2.addLoreLine(Text.literal("")
                     .append(Text.literal("Click").formatted(Formatting.AQUA))
                     .append(Text.literal(" to consume a ").formatted(Formatting.DARK_AQUA))
                     .append(Text.literal("Catalyst").formatted(MagicRarity.getColor(tiers[curItemLevel])))
                     .append(Text.literal(" to augment your item").formatted(Formatting.DARK_AQUA)));
            }
   
            gui.setSlot(28+augmentSlots[i], augmentItem1);
            gui.setSlot(37+augmentSlots[i], augmentItem2);
         }
      }
      
      tag = itemWindow.getOrCreateNbt();
      display = new NbtCompound();
      loreList = new NbtList();
      display.putString("Name","[{\"text\":\"Insert a Magic Item to Tinker with it\",\"italic\":false,\"color\":\"dark_purple\"}]");
      loreList.add(NbtString.of("[{\"text\":\"Apply augments or rename your item!\",\"color\":\"light_purple\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
      tag.putInt("HideFlags",103);
      gui.setSlot(3, GuiElementBuilder.from(itemWindow));
      gui.setSlot(5,GuiElementBuilder.from(itemWindow));
      gui.setSlot(12,GuiElementBuilder.from(itemWindow));
      gui.setSlot(13,GuiElementBuilder.from(itemWindow));
      gui.setSlot(14,GuiElementBuilder.from(itemWindow));
      
   }
   
   public void finishUpdate(){
      updating = false;
   }
}
