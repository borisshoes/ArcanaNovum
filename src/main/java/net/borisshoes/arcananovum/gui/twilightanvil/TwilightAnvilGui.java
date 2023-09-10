package net.borisshoes.arcananovum.gui.twilightanvil;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.borisshoes.arcananovum.Arcananovum;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugment;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.blocks.forge.TwilightAnvilBlockEntity;
import net.borisshoes.arcananovum.cardinalcomponents.IArcanaProfileComponent;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.core.MagicItem;
import net.borisshoes.arcananovum.gui.WatchedGui;
import net.borisshoes.arcananovum.utils.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerInventory;
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

import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;

public class TwilightAnvilGui extends SimpleGui implements WatchedGui {
   private final TwilightAnvilBlockEntity blockEntity;
   private TinkerInventory inv;
   private TinkerInventoryListener listener;
   private final int mode; // 0 - Menu (hopper), 1 - Anvil (9x3), 2 - Augmenting (9x4)
   private final int[][] dynamicSlots = {{},{3},{1,5},{1,3,5},{0,2,4,6},{1,2,3,4,5},{0,1,2,4,5,6},{0,1,2,3,4,5,6}};
   
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
         
         if(index == 31){
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
                     Arcananovum.log(3, "Magic item errored in Tinker's Screen: " + magicItem.getId());
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
                        inv.setStack(0,item);
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
         if(ArcanaAugments.applyAugment(item,augment.id,level)){
            if(!creative) playerInv.removeStack(catalystSlot);
            return true;
         }else{
            Arcananovum.log(3,"Error applying augment "+augment.id+" to "+MagicItemUtils.identifyItem(item).getId());
         }
      }
      
      return false;
   }
   
   @Override
   public void onClose(){
      if(mode == 4){
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
