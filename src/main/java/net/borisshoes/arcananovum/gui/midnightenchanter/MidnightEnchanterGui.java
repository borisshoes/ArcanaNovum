package net.borisshoes.arcananovum.gui.midnightenchanter;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.blocks.forge.ArcaneSingularityBlockEntity;
import net.borisshoes.arcananovum.blocks.forge.MidnightEnchanterBlockEntity;
import net.borisshoes.arcananovum.blocks.forge.StarlightForge;
import net.borisshoes.arcananovum.blocks.forge.StarlightForgeBlockEntity;
import net.borisshoes.arcananovum.gui.WatchedGui;
import net.borisshoes.arcananovum.utils.LevelUtils;
import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.borisshoes.arcananovum.utils.MiscUtils;
import net.borisshoes.arcananovum.utils.SoundUtils;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;
import net.minecraft.util.math.random.Random;

import java.util.*;

import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;

public class MidnightEnchanterGui extends SimpleGui implements WatchedGui {
   private final MidnightEnchanterBlockEntity blockEntity;
   private MidnightEnchanterInventory inv;
   private MidnightEnchanterInventoryListener listener;
   private ItemStack stack = ItemStack.EMPTY;
   private int page = 1;
   private int maxPages = 1;
   private int xpCost = 0;
   private int essenceCost = 0;
   private int bookshelves = -1; // Mode for enchanting unenchanted items
   private int lapisLevel = 0;
   private List<EnchantEntry> enchants = new ArrayList<>();
   
   public MidnightEnchanterGui(ServerPlayerEntity player, MidnightEnchanterBlockEntity blockEntity){
      super(ScreenHandlerType.GENERIC_9X6, player, false);
      this.blockEntity = blockEntity;
      setTitle(Text.literal("Midnight Enchanter"));
   }
   
   @Override
   public boolean onAnyClick(int index, ClickType type, SlotActionType action) {
      if(!(blockEntity.getWorld() instanceof ServerWorld serverWorld)) return true;
      boolean enchanted = !EnchantmentHelper.get(stack).isEmpty();
      boolean precision = ArcanaAugments.getAugmentFromMap(blockEntity.getAugments(),ArcanaAugments.PRECISION_DISENCHANTING.id) >= 1;
      
      if(index == 49){
         if(stack.isEnchantable() && !enchanted && bookshelves >= 0){
            if(lapisLevel == -1){
               player.sendMessage(Text.literal("You must select a Lapis Level").formatted(Formatting.RED,Formatting.ITALIC),true);
               SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_FIRE_EXTINGUISH, 1,1);
               return true;
            }
            if(player.experienceLevel < xpCost || player.isCreative()){
               player.sendMessage(Text.literal("You do not have enough levels").formatted(Formatting.RED,Formatting.ITALIC),true);
               SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_FIRE_EXTINGUISH, 1,1);
               return true;
            }
            
            if(MiscUtils.removeItems(player,Items.LAPIS_LAZULI,lapisLevel)){
               player.applyEnchantmentCosts(stack,0);
               applyEnchants();
               player.addExperienceLevels(-lapisLevel);
               MiscUtils.returnItems(inv,player);
               listener.setUpdating();
               inv.setStack(0,ItemStack.EMPTY);
               setItem(ItemStack.EMPTY);
               listener.finishUpdate();
            }else{
               player.sendMessage(Text.literal("You do not have enough Lapis").formatted(Formatting.RED,Formatting.ITALIC),true);
               SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_FIRE_EXTINGUISH, 1,1);
            }
         }else{
            if(getSelected().isEmpty()) return true;
            if(player.experienceLevel >= xpCost || player.isCreative()){
               if(MiscUtils.removeItems(player,ArcanaRegistry.NEBULOUS_ESSENCE,essenceCost)){
                  applyEnchants();
                  player.addExperienceLevels(-xpCost);
                  MiscUtils.returnItems(inv,player);
                  listener.setUpdating();
                  inv.setStack(0,ItemStack.EMPTY);
                  setItem(ItemStack.EMPTY);
                  listener.finishUpdate();
               }else{
                  player.sendMessage(Text.literal("You do not have enough Nebulous Essence").formatted(Formatting.RED,Formatting.ITALIC),true);
                  SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_FIRE_EXTINGUISH, 1,1);
               }
            }else{
               player.sendMessage(Text.literal("You do not have enough levels").formatted(Formatting.RED,Formatting.ITALIC),true);
               SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_FIRE_EXTINGUISH, 1,1);
            }
         }
         buildGui();
      }else if(index == 27){
         prevPage();
         buildGui();
      }else if(index == 35){
         nextPage();
         buildGui();
      }else if(index == 6 && precision && enchanted){
         if(MiscUtils.removeItems(player,Items.BOOK,1)){
            ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
            if(type == ClickType.MOUSE_RIGHT || type == ClickType.MOUSE_RIGHT_SHIFT){
               Map<Enchantment,Integer> map = EnchantmentHelper.get(stack);
               EnchantmentHelper.set(map,book);
               disenchantItem();
            }else{
               Map<Enchantment,Integer> map = new HashMap<>();
               Pair<Enchantment,Integer> entry = removeTopEnchant();
               map.put(entry.getLeft(),entry.getRight());
               EnchantmentHelper.set(map,book);
            }
            SimpleInventory sinv = new SimpleInventory(book);
            MiscUtils.returnItems(sinv,player);
         }else{
            player.sendMessage(Text.literal("You need a book to put the enchants on").formatted(Formatting.RED,Formatting.ITALIC),true);
            SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_FIRE_EXTINGUISH, 1,1);
         }
         buildGui();
      }else if(index == 7){
         if(enchanted){
            int essence = (int) (MiscUtils.calcEssenceFromEnchants(stack) * (1 + .15*ArcanaAugments.getAugmentFromMap(blockEntity.getAugments(),ArcanaAugments.ESSENCE_SUPERNOVA.id)));
            SimpleInventory sinv = new SimpleInventory(essence / 64 + 1);
            PLAYER_DATA.get(player).addXP(100*essence);
            if(essence > 0){
               while(essence > 64){
                  sinv.addStack(ArcanaRegistry.NEBULOUS_ESSENCE.getDefaultStack().copyWithCount(64));
                  essence -= 64;
               }
               if(essence > 0){
                  sinv.addStack(ArcanaRegistry.NEBULOUS_ESSENCE.getDefaultStack().copyWithCount(essence));
               }
            }
            MiscUtils.returnItems(sinv,player);
            
            int maxCount = 0;
            Map<Enchantment,Integer> map = EnchantmentHelper.get(stack);
            for(Map.Entry<Enchantment, Integer> entry : map.entrySet()){
               if(entry.getValue() == entry.getKey().getMaxLevel()) maxCount++;
            }
            if(maxCount >= 5){
               ArcanaAchievements.grant(player,ArcanaAchievements.MASTERPIECE_TO_NOTHING.id);
            }
            
            disenchantItem();
         }else{
            if(type == ClickType.MOUSE_MIDDLE){
               bookshelves = -1;
            }else if(type == ClickType.MOUSE_RIGHT){
               if(bookshelves == -1){
                  bookshelves = 15;
               }else if(bookshelves > -1){
                  bookshelves--;
               }
            }else{
               if(bookshelves == 15){
                  bookshelves = -1;
               }else{
                  bookshelves++;
               }
            }
            lapisLevel = -1;
            xpCost = -1;
         }
         buildGui();
      }else if(index == 8 && precision && enchanted){
         StarlightForgeBlockEntity forge = StarlightForge.findActiveForge(serverWorld,blockEntity.getPos());
         ArcaneSingularityBlockEntity singularity = null;
         if(forge != null && (singularity = (ArcaneSingularityBlockEntity) forge.getForgeAddition(serverWorld,ArcanaRegistry.ARCANE_SINGULARITY_BLOCK_ENTITY)) != null){
            if(!WatchedGui.guiInUse(singularity.getPos())){
               ArrayList<ItemStack> books = singularity.getBooks();
               if(books != null && books.size() < singularity.getCapacity()){
                  if(MiscUtils.removeItems(player,Items.BOOK,1)){
                     ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
                     if(type == ClickType.MOUSE_RIGHT || type == ClickType.MOUSE_RIGHT_SHIFT){
                        Map<Enchantment,Integer> map = EnchantmentHelper.get(stack);
                        EnchantmentHelper.set(map,book);
                        disenchantItem();
                     }else{
                        Map<Enchantment,Integer> map = new HashMap<>();
                        Pair<Enchantment,Integer> entry = removeTopEnchant();
                        map.put(entry.getLeft(),entry.getRight());
                        EnchantmentHelper.set(map,book);
                     }
                     singularity.getBooks().add(book);
                  }else{
                     player.sendMessage(Text.literal("You need a book to put the enchants on").formatted(Formatting.RED,Formatting.ITALIC),true);
                     SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_FIRE_EXTINGUISH, 1,1);
                  }
               }else{
                  player.sendMessage(Text.literal("The Singularity does not have enough space").formatted(Formatting.RED,Formatting.ITALIC),true);
                  SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_FIRE_EXTINGUISH, 1,1);
               }
            }else{
               player.sendMessage(Text.literal("Someone is using the Singularity").formatted(Formatting.RED,Formatting.ITALIC),true);
               SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_FIRE_EXTINGUISH, 1,1);
            }
         }else{
            player.sendMessage(Text.literal("The Enchanter's Forge does not have access to a Singularity").formatted(Formatting.RED,Formatting.ITALIC),true);
            SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_FIRE_EXTINGUISH, 1,1);
         }
         buildGui();
      }else if(index % 9 > 0 && index % 9 < 8 && index > 18 && index < 44){
         if(stack.isEnchantable() && !enchanted && bookshelves >= 0){
            if(index == 19){
               lapisLevel = 1;
            }else if(index == 28){
               lapisLevel = 2;
            }else if(index == 37){
               lapisLevel = 3;
            }
            buildGui();
         }else{
            int ind = (7*(index/9 - 2) + (index % 9 - 1)) + 21*(page-1);
            if(ind >= enchants.size()) return true;
            EnchantEntry entry = enchants.get(ind);
            if(isCompatible(entry.enchantment, entry.level)){
               enchants.set(ind,new EnchantEntry(entry.enchantment,entry.level,!entry.selected));
               calculateXPCost();
               buildGui();
            }
         }
      }
      
      return true;
   }
   
   public void buildGui(){
      boolean precision = ArcanaAugments.getAugmentFromMap(blockEntity.getAugments(),ArcanaAugments.PRECISION_DISENCHANTING.id) >= 1;
      
      GuiElementBuilder backgroundPane = new GuiElementBuilder(Items.PURPLE_STAINED_GLASS_PANE).hideFlags();
      backgroundPane.setName(Text.empty());
      for(int i=0; i<size; i++){
         if(i == 4) continue;
         setSlot(i,backgroundPane);
      }
      
      if(inv == null){
         inv = new MidnightEnchanterInventory();
         setSlotRedirect(4,new MidnightEnchanterSlot(inv,0,0,0));
      }
      if(listener == null){
         listener = new MidnightEnchanterInventoryListener(this,blockEntity);
         inv.addListener(listener);
      }
      
      boolean enchanted = !EnchantmentHelper.get(stack).isEmpty();
      
      
      GuiElementBuilder topPane;
      if(stack.isEmpty()){
         topPane = new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE).hideFlags();
         topPane.setName((Text.literal("")
               .append(Text.literal("Insert an Enchanted or Enchantable Item").formatted(Formatting.DARK_PURPLE))));
      }else{
         topPane = new GuiElementBuilder(Items.BLUE_STAINED_GLASS_PANE).hideFlags();
         topPane.setName((Text.literal("")
               .append(Text.literal("Add Enchantments or Disenchant Your Item").formatted(Formatting.LIGHT_PURPLE))));
      }
      for(int i=0; i<9; i++){
         setSlot(9+i,topPane);
      }
      setSlot(3,topPane);
      setSlot(5,topPane);
      
      GuiElementBuilder nextArrow = new GuiElementBuilder(Items.SPECTRAL_ARROW).hideFlags();
      nextArrow.setName((Text.literal("")
            .append(Text.literal("Next Page").formatted(Formatting.GOLD))));
      nextArrow.addLoreLine((Text.literal("")
            .append(Text.literal("("+page+" of "+maxPages+")").formatted(Formatting.DARK_PURPLE))));
      setSlot(35,nextArrow);
      
      GuiElementBuilder prevArrow = new GuiElementBuilder(Items.SPECTRAL_ARROW).hideFlags();
      prevArrow.setName((Text.literal("")
            .append(Text.literal("Prev Page").formatted(Formatting.GOLD))));
      prevArrow.addLoreLine((Text.literal("")
            .append(Text.literal("("+page+" of "+maxPages+")").formatted(Formatting.DARK_PURPLE))));
      setSlot(27,prevArrow);
      
      if(enchanted){
         GuiElementBuilder essenceItem = new GuiElementBuilder(Items.SCULK_VEIN).hideFlags();
         essenceItem.setName((Text.literal("")
               .append(Text.literal("Disenchant into Essence").formatted(Formatting.DARK_AQUA))));
         essenceItem.addLoreLine((Text.literal("")
               .append(Text.literal("Click").formatted(Formatting.AQUA))
               .append(Text.literal(" to disenchant this item").formatted(Formatting.DARK_PURPLE))));
         setSlot(7,essenceItem);
         
         if(precision){
            GuiElementBuilder disenchantBookItem = new GuiElementBuilder(Items.BOOK).hideFlags();
            disenchantBookItem.setName((Text.literal("")
                  .append(Text.literal("Disenchant onto a Book").formatted(Formatting.BLUE))));
            disenchantBookItem.addLoreLine((Text.literal("")
                  .append(Text.literal("Click").formatted(Formatting.AQUA))
                  .append(Text.literal(" to move the top enchant to a book").formatted(Formatting.DARK_PURPLE))));
            disenchantBookItem.addLoreLine((Text.literal("")
                  .append(Text.literal("Right Click").formatted(Formatting.AQUA))
                  .append(Text.literal(" to move all enchants to a book").formatted(Formatting.DARK_PURPLE))));
            setSlot(6,disenchantBookItem);
            
            GuiElementBuilder singularityItem = new GuiElementBuilder(Items.LECTERN).hideFlags();
            singularityItem.setName((Text.literal("")
                  .append(Text.literal("Disenchant into a Singularity").formatted(Formatting.LIGHT_PURPLE))));
            singularityItem.addLoreLine((Text.literal("")
                  .append(Text.literal("Click").formatted(Formatting.AQUA))
                  .append(Text.literal(" to move the top enchant to a Singularity").formatted(Formatting.DARK_PURPLE))));
            singularityItem.addLoreLine((Text.literal("")
                  .append(Text.literal("Right Click").formatted(Formatting.AQUA))
                  .append(Text.literal(" to move all enchants to a Singularity").formatted(Formatting.DARK_PURPLE))));
            setSlot(8,singularityItem);
         }
      }else{
         GuiElementBuilder essenceItem;
         if(bookshelves < 0){
            essenceItem = new GuiElementBuilder(Items.ENDER_EYE).hideFlags();
            essenceItem.setName((Text.literal("")
                  .append(Text.literal("Enlightened Enchantment").formatted(Formatting.DARK_AQUA))));
            essenceItem.addLoreLine((Text.literal("")
                  .append(Text.literal("Choose").formatted(Formatting.AQUA))
                  .append(Text.literal(" your enchantments").formatted(Formatting.DARK_PURPLE))));
            essenceItem.addLoreLine((Text.literal("")
                  .append(Text.literal("Click").formatted(Formatting.AQUA))
                  .append(Text.literal(" to go to ").formatted(Formatting.DARK_PURPLE))
                  .append(Text.literal("Regular Enchantment").formatted(Formatting.BLUE))));
         }else{
            if(bookshelves == 0){
               essenceItem = new GuiElementBuilder(Items.CHISELED_BOOKSHELF).hideFlags().setCount(1);
            }else{
               essenceItem = new GuiElementBuilder(Items.BOOKSHELF).hideFlags().setCount(bookshelves);
            }
            essenceItem.setName((Text.literal("")
                  .append(Text.literal("Regular Enchantment").formatted(Formatting.BLUE))));
            essenceItem.addLoreLine((Text.literal("")
                  .append(Text.literal(""+bookshelves).formatted(Formatting.AQUA))
                  .append(Text.literal(" Bookshelves").formatted(Formatting.DARK_PURPLE))));
            essenceItem.addLoreLine((Text.literal("")
                  .append(Text.literal("Left Click").formatted(Formatting.AQUA))
                  .append(Text.literal(" to increase bookshelves").formatted(Formatting.DARK_PURPLE))));
            essenceItem.addLoreLine((Text.literal("")
                  .append(Text.literal("Right Click").formatted(Formatting.AQUA))
                  .append(Text.literal(" to decrease bookshelves").formatted(Formatting.DARK_PURPLE))));
            essenceItem.addLoreLine((Text.literal("")
                  .append(Text.literal("Middle Click").formatted(Formatting.AQUA))
                  .append(Text.literal(" to go to ").formatted(Formatting.DARK_PURPLE))
                  .append(Text.literal("Enlightened Enchantment").formatted(Formatting.DARK_AQUA))));
         }
         setSlot(7,essenceItem);
      }
      
      if(stack.isEnchantable() && !enchanted && bookshelves >= 0){
         for(int i = 19; i < 26; i++){
            GuiElementBuilder bgPane2 = new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE).hideFlags();
            bgPane2.setName(Text.empty());
            setSlot(i,bgPane2);
            setSlot(i+9,bgPane2);
            setSlot(i+18,bgPane2);
         }
         setSlot(20,topPane);
         setSlot(29,topPane);
         setSlot(38,topPane);
         
         Random random = Random.create();
         int playerSeed = player.getEnchantmentTableSeed();
         random.setSeed(playerSeed);
         
         int[] enchPowers = new int[3];
         for(int i = 0; i < enchPowers.length; i++){
            enchPowers[i] = EnchantmentHelper.calculateRequiredExperienceLevel(random, i, bookshelves, stack);
         }
         
         for(int i = 0; i < enchPowers.length; i++){
            List<EnchantmentLevelEntry> list;
            if (enchPowers[i] >= i + 1 && (list = this.generateEnchantments(stack, i, enchPowers[i],random,playerSeed)) != null && !list.isEmpty()){
               GuiElementBuilder lapisItem = new GuiElementBuilder(Items.LAPIS_LAZULI).hideFlags().setCount(i+1);
               lapisItem.setName((Text.literal("")
                     .append(Text.literal("Slot "+(i+1)).formatted(Formatting.BLUE))));
               if(lapisLevel == i+1){
                  lapisItem.glow();
                  lapisItem.addLoreLine((Text.literal("")
                        .append(Text.literal("Selected").formatted(Formatting.AQUA))));
                  
                  setSelectedFromList(list);
                  xpCost = enchPowers[i];
               }else{
                  lapisItem.addLoreLine((Text.literal("")
                        .append(Text.literal("Click").formatted(Formatting.AQUA))
                        .append(Text.literal(" to select").formatted(Formatting.DARK_PURPLE))));
               }
               setSlot(19 + i*9,lapisItem);
               
               for(int j = 0; j < list.size() && j < 5; j++){
                  EnchantmentLevelEntry entry = list.get(j);
                  
                  GuiElementBuilder enchantBook = new GuiElementBuilder(Items.ENCHANTED_BOOK).glow().hideFlags();
                  enchantBook.setName((Text.literal("")
                        .append(Text.translatable(entry.enchantment.getTranslationKey()).formatted(Formatting.AQUA))
                        .append(Text.translatable(" "+ LevelUtils.intToRoman(entry.level)).formatted(Formatting.AQUA))));
                  setSlot(21 + i*9 + j,enchantBook);
               }
            }
         }
      }else{
         List<EnchantEntry> pageItems = getPage();
         int k = 0;
         for(int i = 0; i < 3; i++){
            for(int j = 0; j < 7; j++){
               if(k < pageItems.size()){
                  EnchantEntry pageItem = pageItems.get(k);
                  GuiElementBuilder enchantBook = new GuiElementBuilder(pageItem.selected ? Items.ENCHANTED_BOOK : Items.WRITTEN_BOOK).glow().hideFlags();
                  enchantBook.setName((Text.literal("")
                        .append(Text.translatable(pageItem.enchantment.getTranslationKey()).formatted(Formatting.AQUA))
                        .append(Text.translatable(" "+ LevelUtils.intToRoman(pageItem.level)).formatted(Formatting.AQUA))));
                  if(pageItem.selected){
                     enchantBook.addLoreLine(Text.literal("Selected").formatted(Formatting.YELLOW));
                     enchantBook.addLoreLine(Text.literal("").formatted(Formatting.YELLOW));
                  }
                  if(isCompatible(pageItem.enchantment, pageItem.level)){
                     enchantBook.addLoreLine((Text.literal("")
                           .append(Text.literal("Click").formatted(Formatting.BLUE))
                           .append(Text.literal(" to toggle selection").formatted(Formatting.DARK_PURPLE))));
                  }else{
                     enchantBook.addLoreLine(Text.literal("Incompatible Enchant").formatted(Formatting.RED));
                  }
                  
                  setSlot((i*9+19)+j,enchantBook);
               }else{
                  setSlot((i*9+19)+j,new GuiElementBuilder(stack.isEmpty() ? Items.BLACK_STAINED_GLASS_PANE : Items.BLUE_STAINED_GLASS_PANE).setName(Text.empty()));
               }
               k++;
            }
         }
      }
      
      List<EnchantEntry> selected = getSelected();
      
      GuiElementBuilder enchantItem = new GuiElementBuilder(Items.ENCHANTING_TABLE).hideFlags();
      enchantItem.setName((Text.literal("")
            .append(Text.literal("Enchant Item").formatted(Formatting.LIGHT_PURPLE))));
      enchantItem.addLoreLine((Text.literal("")
            .append(Text.literal("Click").formatted(Formatting.AQUA))
            .append(Text.literal(" to enchant the item").formatted(Formatting.DARK_PURPLE))));
      enchantItem.addLoreLine(Text.empty());
      
      if(!selected.isEmpty()){
         enchantItem.addLoreLine(Text.literal("Adding: ").formatted(Formatting.DARK_PURPLE));
         
         for(EnchantEntry entry : selected){
            enchantItem.addLoreLine((Text.literal("")
                  .append(Text.translatable(entry.enchantment.getTranslationKey()).formatted(Formatting.BLUE))
                  .append(Text.translatable(" "+ LevelUtils.intToRoman(entry.level)).formatted(Formatting.BLUE))));
         }
      }
      setSlot(49,enchantItem);
      
      GuiElementBuilder xpItem = new GuiElementBuilder(Items.EXPERIENCE_BOTTLE).hideFlags();
      String costStr = bookshelves >= 0 ? "Lapis" : "Essence";
      xpItem.setName((Text.literal("")
            .append(Text.literal("XP & "+costStr+" Cost").formatted(Formatting.GREEN))));
      if(xpCost > 0 && !selected.isEmpty()){
         xpItem.addLoreLine((Text.literal("")
               .append(Text.literal(xpCost+" Levels").formatted(Formatting.DARK_GREEN))));
         if(stack.isEnchantable() && !enchanted && bookshelves >= 0){
            xpItem.addLoreLine((Text.literal("")
                  .append(Text.literal(lapisLevel+" Lapis Lazuli").formatted(Formatting.BLUE))));
         }else{
            xpItem.addLoreLine((Text.literal("")
                  .append(Text.literal(essenceCost+" Nebulous Essence").formatted(Formatting.DARK_PURPLE))));
         }
      }else{
         xpItem.addLoreLine((Text.literal("")
               .append(Text.literal("Select Enchantments").formatted(Formatting.DARK_GREEN))));
      }
      setSlot(1,xpItem);
   }
   
   private boolean isCompatible(Enchantment enchant, int level){
      Map<Enchantment,Integer> enchantments = EnchantmentHelper.get(stack);
      boolean upgrade = enchantments.containsKey(enchant) && enchantments.get(enchant) < level;
      for(EnchantEntry entry : getSelected()){
         enchantments.put(entry.enchantment,entry.level);
         if(entry.enchantment == enchant && entry.level != level && upgrade){
            return false;
         }
         if(entry.enchantment == enchant && entry.level == level){
            return true;
         }
      }
      if(upgrade) return true;
      return EnchantmentHelper.isCompatible(enchantments.keySet(),enchant);
   }
   
   private void applyEnchants(){
      listener.setUpdating();
      Map<Enchantment,Integer> map = EnchantmentHelper.get(stack);
      for(EnchantEntry entry : getSelected()){
         map.remove(entry.enchantment);
         map.put(entry.enchantment, entry.level);
         if(entry.level == 1 && entry.enchantment.getMaxLevel() > 1){
            ArcanaAchievements.grant(player,ArcanaAchievements.ENCHANTING_OVERKILL.id);
         }
      }
      if(stack.isOf(Items.BOOK)){
         ItemStack newStack = new ItemStack(Items.ENCHANTED_BOOK);
         if (stack.hasCustomName()) {
            newStack.setCustomName(stack.getName());
         }
         stack = newStack;
      }
      EnchantmentHelper.set(map,stack);
      inv.setStack(0,stack);
      listener.finishUpdate();
   }
   
   private void disenchantItem(){
      listener.setUpdating();
      stack.removeSubNbt("Enchantments");
      stack.removeSubNbt("StoredEnchantments");
      if(MagicItemUtils.isMagic(stack)){
         NbtList eTag = new NbtList();
         eTag.add(new NbtCompound());
         stack.setSubNbt("Enchantments",eTag);
      }else if (stack.isOf(Items.ENCHANTED_BOOK)) {
         ItemStack newStack = new ItemStack(Items.BOOK);
         if (stack.hasCustomName()) {
            newStack.setCustomName(stack.getName());
         }
         stack = newStack;
      }
      if(MagicItemUtils.isMagic(stack)){
         MagicItemUtils.identifyItem(stack).buildItemLore(stack, ArcanaNovum.SERVER);
      }
      inv.setStack(0,stack);
      listener.finishUpdate();
   }
   
   private Pair<Enchantment,Integer> removeTopEnchant(){
      Map<Enchantment,Integer> map = EnchantmentHelper.get(stack);
      if(map.size() == 1){
         disenchantItem();
         for(Map.Entry<Enchantment, Integer> entry : map.entrySet()){
            return new Pair<>(entry.getKey(),entry.getValue());
         }
      }
      listener.setUpdating();
      Map.Entry<Enchantment, Integer> firstEntry = null;
      Iterator<Map.Entry<Enchantment, Integer>> iter = map.entrySet().iterator();
      if(iter.hasNext()){
         firstEntry = iter.next();
         iter.remove();
      }
      if(stack.isOf(Items.ENCHANTED_BOOK)){
         stack.removeSubNbt("StoredEnchantments");
      }
      EnchantmentHelper.set(map,stack);
      
      inv.setStack(0,stack);
      listener.finishUpdate();
      return new Pair<>(firstEntry.getKey(),firstEntry.getValue());
   }
   
   public void setItem(ItemStack stack){
      lapisLevel = -1;
      enchants = getEnchantsForItem(stack);
      page = 1;
      maxPages = (int) Math.ceil(enchants.size() / 21.0);
      this.stack = stack.copy();
      calculateXPCost();
   }
   
   private void setSelectedFromList(List<EnchantmentLevelEntry> entries){
      enchants = new ArrayList<>();
      entries.forEach(e -> enchants.add(new EnchantEntry(e.enchantment,e.level,true)));
   }
   
   public void calculateXPCost(){
      int cost = 0;
      int eCost = 0;
      Map<Enchantment,Integer> curEnchants = EnchantmentHelper.get(stack);
      
      for(Map.Entry<Enchantment, Integer> entry : curEnchants.entrySet()){
         int rarityMod = switch(entry.getKey().getRarity()){
            case COMMON -> 1;
            case UNCOMMON -> 2;
            case RARE -> 3;
            case VERY_RARE -> 5;
         };
         if(entry.getKey().isTreasure()){
            rarityMod *= 2;
         }
         cost += entry.getValue()*rarityMod;
      }
      cost /= 2; // Half cost for existing enchants
      
      for(EnchantEntry entry : getSelected()){
         int rarityMod = switch(entry.enchantment.getRarity()){
            case COMMON -> 1;
            case UNCOMMON -> 2;
            case RARE -> 3;
            case VERY_RARE -> 5;
         };
         if(entry.enchantment.isTreasure()){
            rarityMod *= 2;
         }
         cost += entry.level*rarityMod;
         eCost += (int) Math.ceil(MiscUtils.calcEssenceValue(entry.enchantment,entry.level)*1.5);
      }
      
      essenceCost = eCost;
      xpCost = cost;
   }
   
   public void nextPage(){
      if(page < maxPages){
         page++;
      }
   }
   
   public void prevPage(){
      if(page > 1){
         page--;
      }
   }
   
   private List<EnchantEntry> getSelected(){
      return enchants.stream().filter(e -> e.selected).toList();
   }
   
   private List<EnchantEntry> getPage(){
      List<EnchantEntry> pageItems = new ArrayList<>();
      
      for(int i = (page-1)*21; i < page*21 && i < enchants.size(); i++){
         pageItems.add(enchants.get(i));
      }
      return pageItems;
   }
   
   private List<EnchantEntry> getEnchantsForItem(ItemStack stack){
      if(stack.isEmpty() || stack.isOf(ArcanaRegistry.LEVITATION_HARNESS.getItem())) return new ArrayList<>();
      Map<Enchantment,Integer> curEnchants = EnchantmentHelper.get(stack);
      List<EnchantEntry> possibleAdditions = new ArrayList<>();
      
      for (Enchantment enchantment : Registries.ENCHANTMENT) {
         if(!EnchantmentHelper.isCompatible(curEnchants.keySet(),enchantment) && !curEnchants.containsKey(enchantment)) continue; // Remove incompatible enchants
         if(!stack.isOf(Items.BOOK) && !enchantment.isAcceptableItem(stack)) continue; // Remove enchants for wrong items
         
         if(curEnchants.containsKey(enchantment)){
            int curLevel = curEnchants.get(enchantment);
            int maxLevel = enchantment.getMaxLevel();
            if(curLevel < maxLevel){ // Allow level increases
               for(int i = curLevel+1; i <= maxLevel; i++){
                  
                  possibleAdditions.add(new EnchantEntry(enchantment,i,false));
               }
            }
         }else{ // Add possible additions
            for(int i = enchantment.getMinLevel(); i <= enchantment.getMaxLevel(); i++){
               possibleAdditions.add(new EnchantEntry(enchantment,i,false));
            }
         }
      }
      return possibleAdditions;
   }
   
   private List<EnchantmentLevelEntry> generateEnchantments(ItemStack stack, int slot, int level, Random random, int playerSeed) {
      random.setSeed(playerSeed + slot);
      List<EnchantmentLevelEntry> list = EnchantmentHelper.generateEnchantments(random, stack, level, false);
      if (stack.isOf(Items.BOOK) && list.size() > 1) {
         list.remove(random.nextInt(list.size()));
      }
      return list;
   }
   
   @Override
   public void onClose(){
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
   
   private record EnchantEntry(Enchantment enchantment, int level, boolean selected){}
}

