package net.borisshoes.arcananovum.gui.twilightanvil;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugment;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.blocks.forge.StarlightForge;
import net.borisshoes.arcananovum.blocks.forge.StarlightForgeBlockEntity;
import net.borisshoes.arcananovum.blocks.forge.TwilightAnvilBlockEntity;
import net.borisshoes.arcananovum.cardinalcomponents.IArcanaProfileComponent;
import net.borisshoes.arcananovum.core.ArcanaBlockEntity;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.gui.VirtualInventoryGui;
import net.borisshoes.arcananovum.gui.arcanetome.ArcaneTomeGui;
import net.borisshoes.arcananovum.items.ArcaneTome;
import net.borisshoes.arcananovum.utils.ArcanaColors;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.arcananovum.utils.EnhancedStatUtils;
import net.borisshoes.arcananovum.utils.LevelUtils;
import net.borisshoes.borislib.gui.GraphicalItem;
import net.borisshoes.borislib.gui.GuiHelper;
import net.borisshoes.borislib.utils.MinecraftUtils;
import net.borisshoes.borislib.utils.SoundUtils;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.TreeMap;

public class TwilightAnvilGui extends SimpleGui implements VirtualInventoryGui<SimpleContainer> {
   private final TwilightAnvilBlockEntity blockEntity;
   private SimpleContainer inventory;
   private TinkerInventoryListener listener;
   private final int mode; // 0 - Menu (hopper), 1 - Anvil (9x3), 2 - Augmenting (9x4)
   private final int[][] dynamicSlots = {{},{3},{1,5},{1,3,5},{0,2,4,6},{1,2,3,4,5},{0,1,2,4,5,6},{0,1,2,3,4,5,6}};
   private int tinkerSlotType = 0; // 0 normal inventory, 1 connected forge, 2 this anvil
   
   public TwilightAnvilGui(MenuType<?> type, ServerPlayer player, TwilightAnvilBlockEntity blockEntity, int mode){
      super(type, player, false);
      this.blockEntity = blockEntity;
      this.mode = mode;
   }
   
   public void openTomeItemPage(ArcanaItem item){
      ArcaneTomeGui gui = new ArcaneTomeGui(player, ArcaneTomeGui.TomeMode.ITEM);
      gui.addModes();
      gui.setReturnGui(this);
      gui.setGuiFlags(false,true,false,false);
      gui.buildGui(ArcaneTomeGui.TomeMode.ITEM,item,null);
      gui.buildAndOpen();
   }
   
   @Override
   public boolean onAnyClick(int index, ClickType type, net.minecraft.world.inventory.ClickType action){
      if(mode == 0){ // Menu
         if(index == 0){
            blockEntity.openGui(3,player,"");
         }else if(index == 2){
            blockEntity.openGui(2,player,"");
         }else if(index == 4){
            blockEntity.openGui(1,player,"");
         }
      }else if(mode == 1 && inventory != null){ // Anvil
         if(index == 14){
            ItemStack input1 = inventory.getItem(0);
            ItemStack input2 = inventory.getItem(1);
            TwilightAnvilBlockEntity.AnvilOutputSet outputSet = blockEntity.calculateOutput(input1,input2);
            if(outputSet.output().isEmpty()) return true;
            
            int points = LevelUtils.vanillaLevelToTotalXp(outputSet.levelCost());
            if(!player.isCreative()){
               if(ArcanaAugments.getAugmentFromMap(blockEntity.getAugments(),ArcanaAugments.ANVIL_EXPERTISE.id) > 0){
                  if(player.totalExperience < points){
                     player.sendSystemMessage(Component.literal("Not Enough Experience").withStyle(ChatFormatting.RED));
                     return true;
                  }
                  player.giveExperiencePoints(-points);
               }else{
                  if(player.experienceLevel < outputSet.levelCost()){
                     player.sendSystemMessage(Component.literal("Not Enough Experience").withStyle(ChatFormatting.RED));
                     return true;
                  }
                  player.giveExperienceLevels(-outputSet.levelCost());
               }
               
            }
            if(outputSet.levelCost() > 40){
               ArcanaAchievements.grant(player,ArcanaAchievements.BEYOND_IRONS_LIMIT.id);
            }

            boolean finalMaxEnhanced = EnhancedStatUtils.isEnhanced(outputSet.output()) && ArcanaItem.getDoubleProperty(outputSet.output(),EnhancedStatUtils.ENHANCED_STAT_TAG) >= 1;
            boolean input1MaxEnhanced = EnhancedStatUtils.isEnhanced(input1) && ArcanaItem.getDoubleProperty(input1,EnhancedStatUtils.ENHANCED_STAT_TAG) >= 1;
            boolean input2MaxEnhanced = EnhancedStatUtils.isEnhanced(input2) && ArcanaItem.getDoubleProperty(input2,EnhancedStatUtils.ENHANCED_STAT_TAG) >= 1;
            if(finalMaxEnhanced && !input1MaxEnhanced && !input2MaxEnhanced){
               ArcanaAchievements.grant(player,ArcanaAchievements.TINKER_TO_THE_TOP.id);
            }
            ArcanaNovum.data(player).addXP((int) Math.min(ArcanaNovum.CONFIG.getInt(ArcanaRegistry.XP_TWILIGHT_ANVIL_CAP),ArcanaNovum.CONFIG.getInt(ArcanaRegistry.XP_TWILIGHT_ANVIL_PER_10)*points/10.0));
            
            listener.setUpdating();
            inventory.setItem(0, ItemStack.EMPTY);
            if(outputSet.itemRepairUsage() > 0){
               ItemStack itemStack = inventory.getItem(1);
               if(!itemStack.isEmpty() && itemStack.getCount() > outputSet.itemRepairUsage()){
                  itemStack.shrink(outputSet.itemRepairUsage());
                  inventory.setItem(1, itemStack);
               }else{
                  inventory.setItem(1, ItemStack.EMPTY);
               }
            }else{
               inventory.setItem(1, ItemStack.EMPTY);
            }
            setSlot(14, ItemStack.EMPTY);
            GuiElementBuilder xpItem = new GuiElementBuilder(Items.EXPERIENCE_BOTTLE).hideDefaultTooltip();
            xpItem.setName((Component.literal("")
                  .append(Component.literal("XP Cost").withStyle(ChatFormatting.GREEN))));
            xpItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
                  .append(Component.literal("XP Cost will be shown here").withStyle(ChatFormatting.DARK_GREEN)))));
            setSlot(16,xpItem);
            
            block: {
               ItemEntity itemEntity;
               boolean bl = player.getInventory().add(outputSet.output());
               if(!bl || !outputSet.output().isEmpty()){
                  itemEntity = player.drop(outputSet.output(), false);
                  if(itemEntity == null) break block;
                  itemEntity.setNoPickUpDelay();
                  itemEntity.setTarget(player.getUUID());
                  break block;
               }
               outputSet.output().setCount(1);
               itemEntity = player.drop(outputSet.output(), false);
               if(itemEntity != null){
                  itemEntity.makeFakeItem();
               }
            }
            
            SoundUtils.playSound(player.level(),blockEntity.getBlockPos(), SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 1f, (float)(0.75f * 0.5f*Math.random()));
            listener.finishUpdate();
         }
      }else if(mode == 2 && inventory != null){ // Augmenting
         ItemStack item = inventory.getItem(0);
         ArcanaItem arcanaItem = ArcanaItemUtils.identifyItem(item);
         
         if(tinkerSlotType == 1){
            StarlightForgeBlockEntity forge = StarlightForge.findActiveForge(player.level(),blockEntity.getBlockPos());
            if(forge != null){
               item = ArcanaBlockEntity.getBlockEntityAsItem(forge,forge.getLevel());
               arcanaItem = ArcanaItemUtils.identifyItem(item);
               setSlot(4,GuiElementBuilder.from(item));
            }else{
               close();
            }
         }else if(tinkerSlotType == 2){
            item = ArcanaBlockEntity.getBlockEntityAsItem(blockEntity,blockEntity.getLevel());
            arcanaItem = ArcanaItemUtils.identifyItem(item);
            setSlot(4,GuiElementBuilder.from(item));
         }
         
         if(index == 0 && tinkerSlotType != 2){
            MinecraftUtils.returnItems(inventory,player);
            clearSlot(4);
            tinkerSlotType = 2;
            redrawGui(inventory);
         }else if(index == 8  && tinkerSlotType != 1){
            MinecraftUtils.returnItems(inventory,player);
            clearSlot(4);
            tinkerSlotType = 1;
            redrawGui(inventory);
         }else if(index == 4  && tinkerSlotType != 0){
            setSlotRedirect(4, new Slot(inventory,0,0,0));
            tinkerSlotType = 0;
            redrawGui(inventory);
         }else if(index == 31){
            if(arcanaItem != null){
               if(ArcanaNovum.data(player).hasResearched(arcanaItem)){
                  MinecraftUtils.returnItems(inventory,player);
                  openTomeItemPage(arcanaItem);
               }else{
                  player.displayClientMessage(Component.literal("You must research this item first!").withStyle(ChatFormatting.RED),false);
               }
            }else{
               player.displayClientMessage(Component.literal("Insert an Item to Tinker").withStyle(ChatFormatting.RED),false);
            }
         }else if(index >= 10 && index <= 25){
            if(arcanaItem != null){
               List<ArcanaAugment> augments = ArcanaAugments.getAugmentsForItem(arcanaItem);
               int[] augmentSlots = dynamicSlots[augments.size()];
               ArcanaAugment augment = null;
               for(int i = 0; i < augmentSlots.length; i++){
                  if(index == 19 + augmentSlots[i]){
                     augment = augments.get(i);
                     break;
                  }
               }
               
               if(augment != null){
                  IArcanaProfileComponent profile = ArcanaNovum.data(player);
                  int augmentLvl = profile.getAugmentLevel(augment.id);
                  ArcanaRarity[] tiers = augment.getTiers();
                  int curItemLevel = ArcanaAugments.getAugmentOnItem(item, augment.id);
                  if(curItemLevel == -1) curItemLevel = 0;
                  
                  boolean generic = arcanaItem.getId().equals(ArcaneTome.ID);
                  
                  if(generic){
                     player.displayClientMessage(Component.literal("These augments are active by default").withStyle(ChatFormatting.AQUA), false);
                  }else if(curItemLevel >= tiers.length){ // Item Level = max: End Crystal
                     player.displayClientMessage(Component.literal("You have already maxed this augment").withStyle(ChatFormatting.AQUA), false);
                  }else if(augmentLvl == 0 && curItemLevel == 0){ // Item & player lvl = 0: Obsidian
                     player.displayClientMessage(Component.literal("You must unlock this augment first").withStyle(ChatFormatting.RED), false);
                  }else if(curItemLevel >= augmentLvl){ // Item level != max & >= player level: Obsidian
                     player.displayClientMessage(Component.literal("You must unlock higher levels to augment further").withStyle(ChatFormatting.RED), false);
                  }else if(ArcanaAugments.isIncompatible(item, augment.id)){ // Incompatible augment: Structure Void
                     player.displayClientMessage(Component.literal("This augment is incompatible with existing augments").withStyle(ChatFormatting.RED), false);
                  }else{ // Item level = 0 | (Item level != max & < player level): Augment Catalyst
                     if(attemptAugment(item, augment, curItemLevel + 1)){
                        ArcanaNovum.data(player).addXP(tiers[curItemLevel] == ArcanaRarity.DIVINE ? 10000 : ArcanaRarity.getCraftXp(tiers[curItemLevel])/10);
                        SoundUtils.playSongToPlayer(player, SoundEvents.NOTE_BLOCK_PLING, 1, (.5f+((float)(curItemLevel+1)/(tiers.length-1))));
                     }
                  }
               }
            }
         }
      }
      return true;
   }
   
   public void buildMenuGui(){
      for(int i = 0; i < getSize(); i++){
         clearSlot(i);
         setSlot(i,GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_TOP, ArcanaColors.LAPIS_COLOR)).setName(Component.literal("Twilight Anvil").withStyle(ChatFormatting.DARK_PURPLE)));
      }
      
      GuiElementBuilder equipmentItem = new GuiElementBuilder(Items.NAME_TAG).hideDefaultTooltip();
      equipmentItem.setName((Component.literal("")
            .append(Component.literal("Rename Items").withStyle(ChatFormatting.AQUA))));
      setSlot(0,equipmentItem);
      
      GuiElementBuilder arcanaItem = new GuiElementBuilder(Items.END_CRYSTAL).hideDefaultTooltip();
      arcanaItem.setName((Component.literal("")
            .append(Component.literal("Augment Arcana Items").withStyle(ChatFormatting.LIGHT_PURPLE))));
      setSlot(2,arcanaItem);
      
      GuiElementBuilder anvilItem = new GuiElementBuilder(Items.ANVIL);
      anvilItem.setName((Component.literal("")
            .append(Component.literal("Enhanced Anvil").withStyle(ChatFormatting.YELLOW))));
      setSlot(4,anvilItem);
      
      setTitle(Component.literal("Twilight Anvil"));
   }
   
   public void buildAnvilGui(){
      GuiHelper.outlineGUI(this,ArcanaColors.ARCANA_COLOR, Component.empty());
      
      GuiElementBuilder itemsPane = GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_HORIZONTAL, ArcanaColors.LAPIS_COLOR)).hideDefaultTooltip();
      itemsPane.setName((Component.literal("")
            .append(Component.literal("<- Place Items Here ->").withStyle(ChatFormatting.LIGHT_PURPLE))));
      setSlot(11,itemsPane);
      setSlot(13,GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_VERTICAL, ArcanaColors.ARCANA_COLOR)).hideTooltip());
      setSlot(15,GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_VERTICAL, ArcanaColors.ARCANA_COLOR)).hideTooltip());
      setSlot(4,GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_TOP_CONNECTOR,ArcanaColors.ARCANA_COLOR)).hideTooltip());
      setSlot(6,GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_TOP_CONNECTOR,ArcanaColors.ARCANA_COLOR)).hideTooltip());
      setSlot(22,GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_BOTTOM_CONNECTOR,ArcanaColors.ARCANA_COLOR)).hideTooltip());
      setSlot(24,GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_BOTTOM_CONNECTOR,ArcanaColors.ARCANA_COLOR)).hideTooltip());
      
      
      GuiElementBuilder xpItem = new GuiElementBuilder(Items.EXPERIENCE_BOTTLE).hideDefaultTooltip();
      xpItem.setName((Component.literal("")
            .append(Component.literal("XP Cost").withStyle(ChatFormatting.GREEN))));
      xpItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
            .append(Component.literal("XP Cost will be shown here").withStyle(ChatFormatting.DARK_GREEN)))));
      setSlot(16,xpItem);
      
      inventory = new SimpleContainer(2);
      listener = new TinkerInventoryListener(this,2,blockEntity);
      inventory.addListener(listener);
      setSlotRedirect(10, new Slot(inventory,0,0,0));
      setSlotRedirect(12, new Slot(inventory,1,0,0));
      clearSlot(14);
      
      setTitle(Component.literal("Tinker Items"));
   }
   
   public void buildTinkerGui(){
      tinkerSlotType = 0;
      GuiHelper.outlineGUI(this,ArcanaColors.ARCANA_COLOR, Component.literal("Insert an Arcana Item to Augment it").withStyle(ChatFormatting.DARK_PURPLE));
      
      GuiElementBuilder itemPage = new GuiElementBuilder(Items.ANVIL).hideDefaultTooltip();
      itemPage.setName(Component.literal("Item Page").withStyle(ChatFormatting.DARK_PURPLE));
      itemPage.addLoreLine(TextUtils.removeItalics(Component.literal("")
            .append(Component.literal("Click ").withStyle(ChatFormatting.GREEN))
            .append(Component.literal("to go to the Item Page and unlock Augments!").withStyle(ChatFormatting.LIGHT_PURPLE))));
      setSlot(31,itemPage);
      
      
      GuiElementBuilder augmentPane = GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.PAGE_BG, ArcanaColors.DARK_COLOR)).hideDefaultTooltip();
      augmentPane.setName(Component.literal("Augments:").withStyle(ChatFormatting.DARK_PURPLE));
      augmentPane.addLoreLine(TextUtils.removeItalics(Component.literal("Unlocked augments can be applied to enhance Arcana Items!").withStyle(ChatFormatting.LIGHT_PURPLE)));
      
      GuiElementBuilder tinkerAnvil = GuiElementBuilder.from(ArcanaRegistry.TWILIGHT_ANVIL.getPrefItemNoLore()).hideDefaultTooltip();
      tinkerAnvil.setName(Component.literal("Augment This Twilight Anvil").withStyle(ChatFormatting.BLUE));
      tinkerAnvil.addLoreLine(TextUtils.removeItalics((Component.literal("")
            .append(Component.literal("Click").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(" to augment this Twilight Anvil").withStyle(ChatFormatting.DARK_PURPLE)))));
      setSlot(0,tinkerAnvil);
      
      GuiElementBuilder tinkerForge = GuiElementBuilder.from(ArcanaRegistry.STARLIGHT_FORGE.getPrefItemNoLore()).hideDefaultTooltip();
      tinkerForge.setName(Component.literal("Augment This Starlight Forge").withStyle(ChatFormatting.BLUE));
      tinkerForge.addLoreLine(TextUtils.removeItalics((Component.literal("")
            .append(Component.literal("Click").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(" to augment this Starlight Forge").withStyle(ChatFormatting.DARK_PURPLE)))));
      setSlot(8,tinkerForge);
      
      for(int i = 0; i < 7; i++){
         setSlot(10+i,augmentPane);
         setSlot(19+i,augmentPane);
      }
      
      inventory = new SimpleContainer(2);
      listener = new TinkerInventoryListener(this,0,blockEntity);
      inventory.addListener(listener);
      setSlotRedirect(4, new Slot(inventory,0,0,0));
      
      setTitle(Component.literal("Augment Arcana Items"));
   }
   
   private boolean attemptAugment(ItemStack item, ArcanaAugment augment, int level){
      Inventory playerInv = player.getInventory();
      ArcanaRarity tier = augment.getTiers()[level-1];
      
      int catalystSlot = -1;
      boolean creative = player.isCreative();
      for(int i = 0; i<playerInv.getContainerSize(); i++){
         ItemStack cata = playerInv.getItem(i);
         ArcanaItem arcanaItem = ArcanaItemUtils.identifyItem(cata);
         if(arcanaItem != null && arcanaItem.getId().equals(ArcanaRarity.getAugmentCatalyst(tier).getId())){
            //Found catalyst
            catalystSlot = i;
            break;
         }
      }
      if(catalystSlot == -1 && !creative){
         player.displayClientMessage(Component.literal("No Augment Catalyst Found").withStyle(ChatFormatting.RED),false);
      }else{
         if(tinkerSlotType == 0){
            if(ArcanaAugments.applyAugment(item,augment.id,level,true)){
               if(!creative) playerInv.removeItemNoUpdate(catalystSlot);
               inventory.setItem(0,item);
               return true;
            }else{
               ArcanaNovum.log(2,"Error applying augment "+augment.id+" to "+ ArcanaItemUtils.identifyItem(item).getId());
            }
         }if(tinkerSlotType == 1){
            StarlightForgeBlockEntity forge = StarlightForge.findActiveForge(player.level(),blockEntity.getBlockPos());
            if(forge == null){
               player.displayClientMessage(Component.literal("No Starlight Forge Found").withStyle(ChatFormatting.RED),false);
               return false;
            }
            TreeMap<ArcanaAugment,Integer> forgeAugments = forge.getAugments();
            forgeAugments.put(augment,level);
            if(!creative) playerInv.removeItemNoUpdate(catalystSlot);
            redrawGui(inventory);
            return true;
         }else if(tinkerSlotType == 2){
            TreeMap<ArcanaAugment,Integer> anvilAugments = blockEntity.getAugments();
            anvilAugments.put(augment,level);
            if(!creative) playerInv.removeItemNoUpdate(catalystSlot);
            redrawGui(inventory);
            return true;
         }
      }
      
      return false;
   }
   
   public void redrawGui(Container inv){
      ItemStack item = inv.getItem(0);
      ArcanaItem arcanaItem = ArcanaItemUtils.identifyItem(item);
      
      if(mode == 2){ // Tinkering
         if(tinkerSlotType == 1){
            StarlightForgeBlockEntity forge = StarlightForge.findActiveForge(player.level(),blockEntity.getBlockPos());
            if(forge != null){
               item = ArcanaBlockEntity.getBlockEntityAsItem(forge,forge.getLevel());
               arcanaItem = ArcanaItemUtils.identifyItem(item);
               setSlot(4,GuiElementBuilder.from(item));
            }else{
               close();
            }
         }else if(tinkerSlotType == 2){
            item = ArcanaBlockEntity.getBlockEntityAsItem(blockEntity,blockEntity.getLevel());
            arcanaItem = ArcanaItemUtils.identifyItem(item);
            setSlot(4,GuiElementBuilder.from(item));
         }
         
         GuiElementBuilder augmentPane = GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.PAGE_BG, arcanaItem == null ? ArcanaColors.DARK_COLOR : ArcanaColors.LIGHT_COLOR)).hideDefaultTooltip();
         augmentPane.setName((Component.literal("")
               .append(Component.literal("Unlocked augments can be applied to enhance Arcana Items!").withStyle(ChatFormatting.LIGHT_PURPLE))));
         
         for(int i = 0; i < 7; i++){
            setSlot(10+i,augmentPane);
            setSlot(19+i,augmentPane);
         }
         
         if(arcanaItem != null){
            IArcanaProfileComponent profile = ArcanaNovum.data(player);
            
            boolean unlockedItem = profile.hasResearched(arcanaItem);
            if(!unlockedItem){
               GuiElementBuilder lockedPane = GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.PAGE_BG, ArcanaColors.ERROR_COLOR)).hideDefaultTooltip();
               lockedPane.setName((Component.literal("")
                     .append(Component.literal("You must first research this Arcana Item!").withStyle(ChatFormatting.RED))));
               
               for(int i = 0; i < 7; i++){
                  setSlot(10+i,lockedPane);
                  setSlot(19+i,lockedPane);
               }
            }else{
               boolean generic = arcanaItem.getId().equals(ArcaneTome.ID);
               
               List<ArcanaAugment> augments = ArcanaAugments.getAugmentsForItem(arcanaItem);
               int[] augmentSlots = dynamicSlots[augments.size()];
               for(int i = 0; i < augmentSlots.length; i++){
                  ArcanaAugment augment = augments.get(i);
                  clearSlot(10+augmentSlots[i]);
                  clearSlot(19+augmentSlots[i]);
                  
                  int augmentLvl = profile.getAugmentLevel(augment.id);
                  ArcanaRarity[] tiers = augment.getTiers();
                  
                  GuiElementBuilder augmentItem1 = GuiElementBuilder.from(augment.getDisplayItem());
                  augmentItem1.hideDefaultTooltip().setName(augment.getTranslatedName().withStyle(ChatFormatting.DARK_PURPLE)).addLoreLine(TextUtils.removeItalics(augment.getTierDisplay()));
                  
                  for(String s : augment.getDescription()){
                     augmentItem1.addLoreLine(TextUtils.removeItalics(Component.literal(s).withStyle(ChatFormatting.GRAY)));
                  }
                  if(augmentLvl > 0) augmentItem1.glow();
                  
                  int curItemLevel = ArcanaAugments.getAugmentOnItem(item,augment.id);
                  if(curItemLevel == -1) curItemLevel = 0;
                  
                  GuiElementBuilder augmentItem2;
                  if(generic){ // Generic
                     augmentItem2 = new GuiElementBuilder(Items.TINTED_GLASS);
                     augmentItem2.hideDefaultTooltip().glow().setName(
                           Component.literal("Generic Augmentation").withStyle(ChatFormatting.DARK_PURPLE));
                     augmentItem2.addLoreLine(TextUtils.removeItalics(Component.literal("")
                           .append(Component.literal("These augments are always active").withStyle(ChatFormatting.AQUA))));
                     augmentItem2.addLoreLine(TextUtils.removeItalics(Component.literal("")
                           .append(Component.literal("You do not need to augment your Tome to receive their boons").withStyle(ChatFormatting.AQUA))));
                  }else if(curItemLevel >= tiers.length){ // Item Level = max: End Crystal
                     augmentItem2 = new GuiElementBuilder(Items.END_CRYSTAL);
                     augmentItem2.hideDefaultTooltip().glow().setName(
                           Component.literal("Level ").withStyle(ChatFormatting.DARK_PURPLE)
                                 .append(Component.literal(""+curItemLevel).withStyle(ChatFormatting.LIGHT_PURPLE)));
                     augmentItem2.addLoreLine(TextUtils.removeItalics(Component.literal("")
                           .append(Component.literal("Max Level").withStyle(ChatFormatting.AQUA))));
                  }else if(augmentLvl == 0 && curItemLevel == 0){ // Item & player lvl = 0: Obsidian
                     augmentItem2 = new GuiElementBuilder(Items.OBSIDIAN);
                     augmentItem2.hideDefaultTooltip().glow().setName(
                           Component.literal("Not Augmented").withStyle(ChatFormatting.DARK_PURPLE));
                     augmentItem2.addLoreLine(TextUtils.removeItalics(Component.literal("")
                           .append(Component.literal("Augment Locked!").withStyle(ChatFormatting.DARK_RED))));
                     augmentItem2.addLoreLine(TextUtils.removeItalics(Component.literal("")));
                     augmentItem2.addLoreLine(TextUtils.removeItalics(Component.literal("")
                           .append(Component.literal("Spend ").withStyle(ChatFormatting.DARK_AQUA))
                           .append(Component.literal("Skill Points").withStyle(ChatFormatting.AQUA))
                           .append(Component.literal(" to unlock this augment").withStyle(ChatFormatting.DARK_AQUA))));
                     augmentItem2.addLoreLine(TextUtils.removeItalics(Component.literal("")
                           .append(Component.literal("Unlock augments on the item's page").withStyle(ChatFormatting.DARK_AQUA))));
                  }else if(curItemLevel >= augmentLvl){ // Item level != max & >= player level: Obsidian
                     augmentItem2 = new GuiElementBuilder(Items.OBSIDIAN);
                     augmentItem2.hideDefaultTooltip().glow().setName(
                           Component.literal("Current Level: ").withStyle(ChatFormatting.DARK_PURPLE)
                                 .append(Component.literal(""+curItemLevel).withStyle(ChatFormatting.LIGHT_PURPLE)));
                     augmentItem2.addLoreLine(TextUtils.removeItalics(Component.literal("")
                           .append(Component.literal("You have only unlocked level ").withStyle(ChatFormatting.DARK_RED))
                           .append(Component.literal(""+augmentLvl).withStyle(ChatFormatting.RED))));
                     augmentItem2.addLoreLine(TextUtils.removeItalics(Component.literal("")));
                     augmentItem2.addLoreLine(TextUtils.removeItalics(Component.literal("")
                           .append(Component.literal("Spend ").withStyle(ChatFormatting.DARK_AQUA))
                           .append(Component.literal("Skill Points").withStyle(ChatFormatting.AQUA))
                           .append(Component.literal(" to unlock higher levels").withStyle(ChatFormatting.DARK_AQUA))));
                     augmentItem2.addLoreLine(TextUtils.removeItalics(Component.literal("")
                           .append(Component.literal("Unlock augments on the item's page").withStyle(ChatFormatting.DARK_AQUA))));
                  }else if(ArcanaAugments.isIncompatible(item,augment.id)){ // Incompatible augment: Structure Void
                     augmentItem2 = new GuiElementBuilder(Items.STRUCTURE_VOID);
                     augmentItem2.hideDefaultTooltip().glow().setName(
                           Component.literal("Incompatible Augment").withStyle(ChatFormatting.DARK_PURPLE));
                     augmentItem2.addLoreLine(TextUtils.removeItalics(Component.literal("")
                           .append(Component.literal("This augment is incompatible with present augments").withStyle(ChatFormatting.DARK_RED))));
                  }else if(curItemLevel == 0 || curItemLevel == -1){ // Item level = 0: Augment Catalyst
                     augmentItem2 = GuiElementBuilder.from(ArcanaRarity.getAugmentCatalyst(tiers[0]).getPrefItemNoLore());
                     augmentItem2.hideDefaultTooltip().setName(
                           Component.literal("Not Augmented").withStyle(ChatFormatting.DARK_PURPLE));
                     augmentItem2.addLoreLine(TextUtils.removeItalics(Component.literal("")
                           .append(Component.literal("Level: ").withStyle(ChatFormatting.BLUE))
                           .append(Component.literal("1").withStyle(ChatFormatting.DARK_AQUA))
                           .append(Component.literal(" (").withStyle(ChatFormatting.BLUE))
                           .append(ArcanaRarity.getColoredLabel(tiers[0],false))
                           .append(Component.literal(")").withStyle(ChatFormatting.BLUE))));
                     augmentItem2.addLoreLine(TextUtils.removeItalics(Component.literal("")));
                     augmentItem2.addLoreLine(TextUtils.removeItalics(Component.literal("")
                           .append(Component.literal("Applying augments requires an ").withStyle(ChatFormatting.DARK_AQUA))
                           .append(Component.literal("Augment Catalyst").withStyle(ArcanaRarity.getColor(tiers[0])))));
                     augmentItem2.addLoreLine(TextUtils.removeItalics(Component.literal("")
                           .append(Component.literal("Click").withStyle(ChatFormatting.AQUA))
                           .append(Component.literal(" to consume a ").withStyle(ChatFormatting.DARK_AQUA))
                           .append(Component.literal("Catalyst").withStyle(ArcanaRarity.getColor(tiers[0])))
                           .append(Component.literal(" to augment your item").withStyle(ChatFormatting.DARK_AQUA))));
                  }else{ // Item level != max & < player level: Augment Catalyst
                     augmentItem2 = GuiElementBuilder.from(ArcanaRarity.getAugmentCatalyst(tiers[curItemLevel]).getPrefItemNoLore());
                     augmentItem2.hideDefaultTooltip().setName(
                           Component.literal("Current Level: ").withStyle(ChatFormatting.DARK_PURPLE)
                                 .append(Component.literal(""+curItemLevel).withStyle(ChatFormatting.LIGHT_PURPLE)));
                     augmentItem2.addLoreLine(TextUtils.removeItalics(Component.literal("")
                           .append(Component.literal("Next Level: ").withStyle(ChatFormatting.BLUE))
                           .append(Component.literal((curItemLevel+1)+"").withStyle(ChatFormatting.DARK_AQUA))
                           .append(Component.literal(" (").withStyle(ChatFormatting.BLUE))
                           .append(ArcanaRarity.getColoredLabel(tiers[curItemLevel],false))
                           .append(Component.literal(")").withStyle(ChatFormatting.BLUE))));
                     augmentItem2.addLoreLine(TextUtils.removeItalics(Component.literal("")));
                     augmentItem2.addLoreLine(TextUtils.removeItalics(Component.literal("")
                           .append(Component.literal("Applying augments requires an ").withStyle(ChatFormatting.DARK_AQUA))
                           .append(Component.literal("Augment Catalyst").withStyle(ArcanaRarity.getColor(tiers[curItemLevel])))));
                     augmentItem2.addLoreLine(TextUtils.removeItalics(Component.literal("")
                           .append(Component.literal("Click").withStyle(ChatFormatting.AQUA))
                           .append(Component.literal(" to consume a ").withStyle(ChatFormatting.DARK_AQUA))
                           .append(Component.literal("Catalyst").withStyle(ArcanaRarity.getColor(tiers[curItemLevel])))
                           .append(Component.literal(" to augment your item").withStyle(ChatFormatting.DARK_AQUA))));
                  }
                  
                  setSlot(10+augmentSlots[i], augmentItem1);
                  setSlot(19+augmentSlots[i], augmentItem2);
               }
            }
         }
      }else if(mode == 1){ // Anvil
         ItemStack input1 = inv.getItem(0);
         ItemStack input2 = inv.getItem(1);
         TwilightAnvilBlockEntity.AnvilOutputSet outputSet = blockEntity.calculateOutput(input1,input2);
         GuiElementBuilder xpItem = new GuiElementBuilder(Items.EXPERIENCE_BOTTLE).hideDefaultTooltip();
         xpItem.setName((Component.literal("")
               .append(Component.literal("XP Cost").withStyle(ChatFormatting.GREEN))));
         
         if(!outputSet.output().isEmpty()){
            setSlot(14,outputSet.output());
            
            if(outputSet.levelCost() <= 64) xpItem.setCount(outputSet.levelCost());
            
            xpItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
                  .append(Component.literal(outputSet.levelCost()+" Levels ("+ LevelUtils.vanillaLevelToTotalXp(outputSet.levelCost()) +" Points)").withStyle(ChatFormatting.DARK_GREEN)))));
            setSlot(16,xpItem);
         }else{
            setSlot(14, ItemStack.EMPTY);
            
            xpItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
                  .append(Component.literal("XP Cost will be shown here").withStyle(ChatFormatting.DARK_GREEN)))));
            setSlot(16,xpItem);
         }
         
      }
   }
   
   @Override
   public void onTick(){
      Level world = blockEntity.getLevel();
      if(world == null || world.getBlockEntity(blockEntity.getBlockPos()) != blockEntity || !blockEntity.isAssembled()){
         this.close();
      }
      
      super.onTick();
   }
   
   @Override
   public void onClose(){
      MinecraftUtils.returnItems(inventory,player);
      onVirtualInventoryClose();
      super.onClose();
   }
   
   @Override
   public void onOpen() {
      onVirtualInventoryOpen();
      super.onOpen();
   }
   
   @Override
   public void close(){
      super.close();
   }
   
   @Override
   public SimpleContainer getInventory() { return inventory; }
   
   @Override
   public ServerPlayer getPlayer() { return player; }
}
