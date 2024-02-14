package net.borisshoes.arcananovum.gui.twilightanvil;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugment;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.blocks.forge.StarlightForge;
import net.borisshoes.arcananovum.blocks.forge.StarlightForgeBlockEntity;
import net.borisshoes.arcananovum.blocks.forge.TwilightAnvilBlockEntity;
import net.borisshoes.arcananovum.cardinalcomponents.IArcanaProfileComponent;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.core.MagicItem;
import net.borisshoes.arcananovum.gui.WatchedGui;
import net.borisshoes.arcananovum.utils.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.TreeMap;

import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;

public class TwilightAnvilGui extends SimpleGui implements WatchedGui {
   private final TwilightAnvilBlockEntity blockEntity;
   private TinkerInventory inv;
   private TinkerInventoryListener listener;
   private final int mode; // 0 - Menu (hopper), 1 - Anvil (9x3), 2 - Augmenting (9x4)
   private final int[][] dynamicSlots = {{},{3},{1,5},{1,3,5},{0,2,4,6},{1,2,3,4,5},{0,1,2,4,5,6},{0,1,2,3,4,5,6}};
   private int tinkerSlotType = 0; // 0 normal inventory, 1 connected forge, 2 this anvil
   
   public TwilightAnvilGui(ScreenHandlerType<?> type, ServerPlayerEntity player, TwilightAnvilBlockEntity blockEntity, int mode){
      super(type, player, false);
      this.blockEntity = blockEntity;
      this.mode = mode;
   }
   
   @Override
   public boolean onAnyClick(int index, ClickType type, SlotActionType action){
      if(mode == 0){ // Menu
         if(index == 0){
            blockEntity.openGui(3,player,"");
         }else if(index == 2){
            blockEntity.openGui(2,player,"");
         }else if(index == 4){
            blockEntity.openGui(1,player,"");
         }
      }else if(mode == 1 && inv != null){ // Anvil
         if(index == 14){
            ItemStack input1 = inv.getStack(0);
            ItemStack input2 = inv.getStack(1);
            TwilightAnvilBlockEntity.AnvilOutputSet outputSet = blockEntity.calculateOutput(input1,input2);
            if(outputSet.output().isEmpty()) return true;
            
            int points = LevelUtils.vanillaLevelToTotalXp(outputSet.levelCost());
            if (!player.getAbilities().creativeMode) {
               if(ArcanaAugments.getAugmentFromMap(blockEntity.getAugments(),ArcanaAugments.ANVIL_EXPERTISE.id) > 0){
                  if(player.totalExperience < points){
                     player.sendMessage(Text.literal("Not Enough Experience").formatted(Formatting.RED));
                     return true;
                  }
                  player.addExperience(-points);
               }else{
                  if(player.experienceLevel < outputSet.levelCost()){
                     player.sendMessage(Text.literal("Not Enough Experience").formatted(Formatting.RED));
                     return true;
                  }
                  player.addExperienceLevels(-outputSet.levelCost());
               }
               
            }
            if(outputSet.levelCost() > 40){
               ArcanaAchievements.grant(player,ArcanaAchievements.BEYOND_IRONS_LIMIT.id);
            }
            if(EnhancedStatUtils.isEnhanced(outputSet.output()) && outputSet.output().getNbt().getDouble("ArcanaStats") >= 1){
               ArcanaAchievements.grant(player,ArcanaAchievements.TINKER_TO_THE_TOP.id);
            }
            PLAYER_DATA.get(player).addXP(Math.min(1000,points));
            
            listener.setUpdating();
            inv.setStack(0, ItemStack.EMPTY);
            if (outputSet.itemRepairUsage() > 0) {
               ItemStack itemStack = inv.getStack(1);
               if (!itemStack.isEmpty() && itemStack.getCount() > outputSet.itemRepairUsage()) {
                  itemStack.decrement(outputSet.itemRepairUsage());
                  inv.setStack(1, itemStack);
               } else {
                  inv.setStack(1, ItemStack.EMPTY);
               }
            } else {
               inv.setStack(1, ItemStack.EMPTY);
            }
            setSlot(14,ItemStack.EMPTY);
            GuiElementBuilder xpItem = new GuiElementBuilder(Items.EXPERIENCE_BOTTLE).hideFlags();
            xpItem.setName((Text.literal("")
                  .append(Text.literal("XP Cost").formatted(Formatting.GREEN))));
            xpItem.addLoreLine((Text.literal("")
                  .append(Text.literal("XP Cost will be shown here").formatted(Formatting.DARK_GREEN))));
            setSlot(16,xpItem);
            
            block: {
               ItemEntity itemEntity;
               boolean bl = player.getInventory().insertStack(outputSet.output());
               if (!bl || !outputSet.output().isEmpty()) {
                  itemEntity = player.dropItem(outputSet.output(), false);
                  if (itemEntity == null) break block;
                  itemEntity.resetPickupDelay();
                  itemEntity.setOwner(player.getUuid());
                  break block;
               }
               outputSet.output().setCount(1);
               itemEntity = player.dropItem(outputSet.output(), false);
               if (itemEntity != null) {
                  itemEntity.setDespawnImmediately();
               }
            }
            
            SoundUtils.playSound(player.getServerWorld(),blockEntity.getPos(), SoundEvents.BLOCK_ANVIL_USE, SoundCategory.BLOCKS, 1f, (float)(0.75f * 0.5f*Math.random()));
            listener.finishUpdate();
         }
      }else if(mode == 2 && inv != null){ // Augmenting
         ItemStack item = inv.getStack(0);
         MagicItem magicItem = MagicItemUtils.identifyItem(item);
         
         if(tinkerSlotType == 1){
            StarlightForgeBlockEntity forge = StarlightForge.findActiveForge(player.getServerWorld(),blockEntity.getPos());
            if(forge != null){
               item = forge.getBlockEntityAsItem(forge,forge.getWorld());
               magicItem = MagicItemUtils.identifyItem(item);
               setSlot(4,GuiElementBuilder.from(item));
            }else{
               close();
            }
         }else if(tinkerSlotType == 2){
            item = blockEntity.getBlockEntityAsItem(blockEntity,blockEntity.getWorld());
            magicItem = MagicItemUtils.identifyItem(item);
            setSlot(4,GuiElementBuilder.from(item));
         }
         
         if(index == 0 && tinkerSlotType != 2){
            MiscUtils.returnItems(inv,player);
            clearSlot(4);
            tinkerSlotType = 2;
            redrawGui(inv);
         }else if(index == 8  && tinkerSlotType != 1){
            MiscUtils.returnItems(inv,player);
            clearSlot(4);
            tinkerSlotType = 1;
            redrawGui(inv);
         }else if(index == 4  && tinkerSlotType != 0){
            setSlotRedirect(4, new Slot(inv,0,0,0));
            tinkerSlotType = 0;
            redrawGui(inv);
         }else if(index == 31){
            if(magicItem != null){
               MiscUtils.returnItems(inv,player);
               blockEntity.openGui(4,player,magicItem.getId());
            }else{
               player.sendMessage(Text.literal("Insert an Item to Tinker").formatted(Formatting.RED),false);
            }
         }else if(index >= 10 && index <= 25){
            if(magicItem != null){
               List<ArcanaAugment> augments = ArcanaAugments.getAugmentsForItem(magicItem);
               int[] augmentSlots = dynamicSlots[augments.size()];
               ArcanaAugment augment = null;
               for(int i = 0; i < augmentSlots.length; i++){
                  if(index == 19 + augmentSlots[i]){
                     augment = augments.get(i);
                     break;
                  }
               }
               
               if(augment != null){
                  IArcanaProfileComponent profile = PLAYER_DATA.get(player);
                  int augmentLvl = profile.getAugmentLevel(augment.id);
                  MagicRarity[] tiers = augment.getTiers();
                  int curItemLevel = ArcanaAugments.getAugmentOnItem(item, augment.id);
                  if(curItemLevel == -2){
                     ArcanaNovum.log(3, "Magic item errored in Tinker's Screen: " + magicItem.getId());
                  }else if(curItemLevel == -1) curItemLevel = 0;
                  
                  boolean generic = magicItem.getId().equals(ArcanaRegistry.ARCANE_TOME.getId());
                  
                  if(generic){
                     player.sendMessage(Text.literal("These augments are active by default").formatted(Formatting.AQUA), false);
                  }else if(curItemLevel >= tiers.length){ // Item Level = max: End Crystal
                     player.sendMessage(Text.literal("You have already maxed this augment").formatted(Formatting.AQUA), false);
                  }else if(augmentLvl == 0 && curItemLevel == 0){ // Item & player lvl = 0: Obsidian
                     player.sendMessage(Text.literal("You must unlock this augment first").formatted(Formatting.RED), false);
                  }else if(curItemLevel >= augmentLvl){ // Item level != max & >= player level: Obsidian
                     player.sendMessage(Text.literal("You must unlock higher levels to augment further").formatted(Formatting.RED), false);
                  }else if(ArcanaAugments.isIncompatible(item, augment.id)){ // Incompatible augment: Structure Void
                     player.sendMessage(Text.literal("This augment is incompatible with existing augments").formatted(Formatting.RED), false);
                  }else{ // Item level = 0 | (Item level != max & < player level): Augment Catalyst
                     if(attemptAugment(item, augment, curItemLevel + 1)){
                        PLAYER_DATA.get(player).addXP(tiers[curItemLevel] == MagicRarity.MYTHICAL ? 10000 : MagicRarity.getCraftXp(tiers[curItemLevel])/10);
                        SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_NOTE_BLOCK_PLING, 1, (.5f+((float)(curItemLevel+1)/(tiers.length-1))));
                     }
                  }
               }
            }
         }
      }else if(mode == 4){ // Unlock augments
         ItemStack item = this.getSlot(4).getItemStack();
         MagicItem magicItem = MagicItemUtils.identifyItem(item);
         
         if(index == 4){
            close();
         }else if(index >= 28 && index <= 35){
            List<ArcanaAugment> augments = ArcanaAugments.getAugmentsForItem(magicItem);
            int[] augmentSlots = dynamicSlots[augments.size()];
            ArcanaAugment augment = null;
            for(int i = 0; i < augmentSlots.length; i++){
               if(index == 28+augmentSlots[i]){
                  augment = augments.get(i);
                  break;
               }
            }
            
            if(augment != null){
               IArcanaProfileComponent profile = PLAYER_DATA.get(player);
               int augmentLvl = profile.getAugmentLevel(augment.id);
               MagicRarity[] tiers = augment.getTiers();
               if(augmentLvl >= tiers.length) return true;
               int cost = tiers[augmentLvl].rarity+1;
               int unallocated = profile.getTotalSkillPoints() - profile.getSpentSkillPoints();
               if(cost <= unallocated){
                  profile.setAugmentLevel(augment.id,augmentLvl+1);
                  SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_NOTE_BLOCK_PLING, 1, (.5f+((float)(augmentLvl+1)/(tiers.length-1))));
                  blockEntity.openGui(4,player,magicItem.getId());
               }else{
                  player.sendMessage(Text.literal("Not Enough Skill Points").formatted(Formatting.RED),false);
               }
            }
         }
      }
      return true;
   }
   
   public void buildMenuGui(){
      for(int i = 0; i < getSize(); i++){
         clearSlot(i);
         setSlot(i,new GuiElementBuilder(Items.BLUE_STAINED_GLASS_PANE).setName(Text.literal("Twilight Anvil").formatted(Formatting.DARK_PURPLE)));
      }
      
      GuiElementBuilder equipmentItem = new GuiElementBuilder(Items.NAME_TAG).hideFlags();
      equipmentItem.setName((Text.literal("")
            .append(Text.literal("Rename Items").formatted(Formatting.AQUA))));
      setSlot(0,equipmentItem);
      
      GuiElementBuilder magicItem = new GuiElementBuilder(Items.END_CRYSTAL).hideFlags();
      magicItem.setName((Text.literal("")
            .append(Text.literal("Augment Magic Items").formatted(Formatting.LIGHT_PURPLE))));
      setSlot(2,magicItem);
      
      GuiElementBuilder anvilItem = new GuiElementBuilder(Items.ANVIL);
      anvilItem.setName((Text.literal("")
            .append(Text.literal("Enhanced Anvil").formatted(Formatting.YELLOW))));
      setSlot(4,anvilItem);
      
      setTitle(Text.literal("Twilight Anvil"));
   }
   
   public void buildAnvilGui(){
      for(int i = 0; i < getSize(); i++){
         clearSlot(i);
         setSlot(i, new GuiElementBuilder(Items.PURPLE_STAINED_GLASS_PANE).setName(Text.empty()));
      }
      
      GuiElementBuilder itemsPane = new GuiElementBuilder(Items.WHITE_STAINED_GLASS_PANE).hideFlags();
      itemsPane.setName((Text.literal("")
            .append(Text.literal("<- Place Items Here ->").formatted(Formatting.LIGHT_PURPLE))));
      setSlot(11,itemsPane);
      
      GuiElementBuilder xpItem = new GuiElementBuilder(Items.EXPERIENCE_BOTTLE).hideFlags();
      xpItem.setName((Text.literal("")
            .append(Text.literal("XP Cost").formatted(Formatting.GREEN))));
      xpItem.addLoreLine((Text.literal("")
            .append(Text.literal("XP Cost will be shown here").formatted(Formatting.DARK_GREEN))));
      setSlot(16,xpItem);
      
      inv = new TinkerInventory();
      listener = new TinkerInventoryListener(this,2,blockEntity);
      inv.addListener(listener);
      setSlotRedirect(10, new Slot(inv,0,0,0));
      setSlotRedirect(12, new Slot(inv,1,0,0));
      clearSlot(14);
      
      setTitle(Text.literal("Tinker Items"));
   }
   
   public void buildTinkerGui(){
      tinkerSlotType = 0;
      for(int i = 0; i < getSize(); i++){
         clearSlot(i);
         setSlot(i,new GuiElementBuilder(Items.PURPLE_STAINED_GLASS_PANE).setName(Text.literal("Insert a Magic Item to Augment it").formatted(Formatting.DARK_PURPLE)));
      }
      
      ItemStack itemPage = new ItemStack(Items.ANVIL);
      NbtCompound tag = itemPage.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList loreList = new NbtList();
      display.putString("Name","[{\"text\":\"Item Page\",\"italic\":false,\"color\":\"dark_purple\"}]");
      loreList.add(NbtString.of("[{\"text\":\"Click \",\"italic\":false,\"color\":\"green\"},{\"text\":\"to go to the Item Page and unlock Augments!\",\"color\":\"light_purple\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
      tag.putInt("HideFlags",103);
      setSlot(31,GuiElementBuilder.from(itemPage));
      
      ItemStack augmentPane = new ItemStack(Items.BLACK_STAINED_GLASS_PANE);
      tag = augmentPane.getOrCreateNbt();
      display = new NbtCompound();
      loreList = new NbtList();
      display.putString("Name","[{\"text\":\"Augments:\",\"italic\":false,\"color\":\"dark_purple\"}]");
      loreList.add(NbtString.of("[{\"text\":\"Unlocked augments can be applied to enhance Magic Items!\",\"color\":\"light_purple\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
      tag.putInt("HideFlags",103);
      
      GuiElementBuilder tinkerAnvil = new GuiElementBuilder(ArcanaRegistry.TWILIGHT_ANVIL.getItem()).hideFlags();
      tinkerAnvil.setName(Text.literal("Augment This Twilight Anvil").formatted(Formatting.BLUE));
      tinkerAnvil.addLoreLine((Text.literal("")
            .append(Text.literal("Click").formatted(Formatting.AQUA))
            .append(Text.literal(" to augment this Twilight Anvil").formatted(Formatting.DARK_PURPLE))));
      setSlot(0,tinkerAnvil);
      
      GuiElementBuilder tinkerForge = new GuiElementBuilder(ArcanaRegistry.STARLIGHT_FORGE.getItem()).hideFlags();
      tinkerForge.setName(Text.literal("Augment This Starlight Forge").formatted(Formatting.BLUE));
      tinkerForge.addLoreLine((Text.literal("")
            .append(Text.literal("Click").formatted(Formatting.AQUA))
            .append(Text.literal(" to augment this Starlight Forge").formatted(Formatting.DARK_PURPLE))));
      setSlot(8,tinkerForge);
      
      for(int i = 0; i < 7; i++){
         setSlot(10+i,GuiElementBuilder.from(augmentPane));
         setSlot(19+i,GuiElementBuilder.from(augmentPane));
      }
      
      inv = new TinkerInventory();
      listener = new TinkerInventoryListener(this,0,blockEntity);
      inv.addListener(listener);
      setSlotRedirect(4, new Slot(inv,0,0,0));
      
      setTitle(Text.literal("Augment Magic Items"));
   }
   
   private boolean attemptAugment(ItemStack item, ArcanaAugment augment, int level){
      PlayerInventory playerInv = player.getInventory();
      MagicRarity tier = augment.getTiers()[level-1];
      
      int catalystSlot = -1;
      boolean creative = player.isCreative();
      for(int i=0; i<playerInv.size(); i++){
         ItemStack cata = playerInv.getStack(i);
         MagicItem magicItem = MagicItemUtils.identifyItem(cata);
         if(magicItem != null && magicItem.getId().equals(MagicRarity.getAugmentCatalyst(tier).getId())){
            //Found catalyst
            catalystSlot = i;
            break;
         }
      }
      if(catalystSlot == -1 && !creative){
         player.sendMessage(Text.literal("No Augment Catalyst Found").formatted(Formatting.RED),false);
      }else{
         if(tinkerSlotType == 0){
            if(ArcanaAugments.applyAugment(item,augment.id,level,true)){
               if(!creative) playerInv.removeStack(catalystSlot);
               inv.setStack(0,item);
               return true;
            }else{
               ArcanaNovum.log(3,"Error applying augment "+augment.id+" to "+MagicItemUtils.identifyItem(item).getId());
            }
         }if(tinkerSlotType == 1){
            StarlightForgeBlockEntity forge = StarlightForge.findActiveForge(player.getServerWorld(),blockEntity.getPos());
            if(forge == null){
               player.sendMessage(Text.literal("No Starlight Forge Found").formatted(Formatting.RED),false);
               return false;
            }
            TreeMap<ArcanaAugment,Integer> forgeAugments = forge.getAugments();
            forgeAugments.put(augment,level);
            if(!creative) playerInv.removeStack(catalystSlot);
            redrawGui(inv);
            return true;
         }else if(tinkerSlotType == 2){
            TreeMap<ArcanaAugment,Integer> anvilAugments = blockEntity.getAugments();
            anvilAugments.put(augment,level);
            if(!creative) playerInv.removeStack(catalystSlot);
            redrawGui(inv);
            return true;
         }
      }
      
      return false;
   }
   
   public void redrawGui(Inventory inv){
      ItemStack item = inv.getStack(0);
      MagicItem magicItem = MagicItemUtils.identifyItem(item);
      
      if(mode == 2){ // Tinkering
         if(tinkerSlotType == 1){
            StarlightForgeBlockEntity forge = StarlightForge.findActiveForge(player.getServerWorld(),blockEntity.getPos());
            if(forge != null){
               item = forge.getBlockEntityAsItem(forge,forge.getWorld());
               magicItem = MagicItemUtils.identifyItem(item);
               setSlot(4,GuiElementBuilder.from(item));
            }else{
               close();
            }
         }else if(tinkerSlotType == 2){
            item = blockEntity.getBlockEntityAsItem(blockEntity,blockEntity.getWorld());
            magicItem = MagicItemUtils.identifyItem(item);
            setSlot(4,GuiElementBuilder.from(item));
         }
         
         GuiElementBuilder augmentPane = new GuiElementBuilder(magicItem == null ? Items.BLACK_STAINED_GLASS_PANE : Items.WHITE_STAINED_GLASS_PANE).hideFlags();
         augmentPane.setName((Text.literal("")
               .append(Text.literal("Unlocked augments can be applied to enhance Magic Items!").formatted(Formatting.LIGHT_PURPLE))));
         
         for(int i = 0; i < 7; i++){
            setSlot(10+i,augmentPane);
            setSlot(19+i,augmentPane);
         }
         
         if(magicItem != null){
            IArcanaProfileComponent profile = PLAYER_DATA.get(player);
            
            boolean generic = magicItem.getId().equals(ArcanaRegistry.ARCANE_TOME.getId());
            
            List<ArcanaAugment> augments = ArcanaAugments.getAugmentsForItem(magicItem);
            int[] augmentSlots = dynamicSlots[augments.size()];
            for(int i = 0; i < augmentSlots.length; i++){
               ArcanaAugment augment = augments.get(i);
               clearSlot(10+augmentSlots[i]);
               clearSlot(19+augmentSlots[i]);
               
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
                  ArcanaNovum.log(3,"Magic item errored in Tinker's Screen: "+magicItem.getId());
               }else if(curItemLevel == -1) curItemLevel = 0;
               
               GuiElementBuilder augmentItem2;
               if(generic){ // Generic
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
               
               setSlot(10+augmentSlots[i], augmentItem1);
               setSlot(19+augmentSlots[i], augmentItem2);
            }
         }
      }else if(mode == 1){ // Anvil
         ItemStack input1 = inv.getStack(0);
         ItemStack input2 = inv.getStack(1);
         TwilightAnvilBlockEntity.AnvilOutputSet outputSet = blockEntity.calculateOutput(input1,input2);
         GuiElementBuilder xpItem = new GuiElementBuilder(Items.EXPERIENCE_BOTTLE).hideFlags();
         xpItem.setName((Text.literal("")
               .append(Text.literal("XP Cost").formatted(Formatting.GREEN))));
         
         if(!outputSet.output().isEmpty()){
            setSlot(14,outputSet.output());
            
            if(outputSet.levelCost() <= 64) xpItem.setCount(outputSet.levelCost());
            
            xpItem.addLoreLine((Text.literal("")
                  .append(Text.literal(outputSet.levelCost()+" Levels ("+ LevelUtils.vanillaLevelToTotalXp(outputSet.levelCost()) +" Points)").formatted(Formatting.DARK_GREEN))));
            setSlot(16,xpItem);
         }else{
            setSlot(14,ItemStack.EMPTY);
            
            xpItem.addLoreLine((Text.literal("")
                  .append(Text.literal("XP Cost will be shown here").formatted(Formatting.DARK_GREEN))));
            setSlot(16,xpItem);
         }
         
      }
   }
   
   @Override
   public void onClose(){
      if(mode == 4){
         tinkerSlotType = 0;
         blockEntity.openGui(2,player,"");
      }
      MiscUtils.returnItems(inv,player);
   }
   
   @Override
   public void close(){
      super.close();
   }
   
   @Override
   public BlockEntity getBlockEntity(){
      return blockEntity;
   }
   
   @Override
   public SimpleGui getGui(){
      return this;
   }
}
