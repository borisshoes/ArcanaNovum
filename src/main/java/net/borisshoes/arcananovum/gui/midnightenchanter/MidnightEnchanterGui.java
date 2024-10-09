package net.borisshoes.arcananovum.gui.midnightenchanter;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.blocks.forge.ArcaneSingularityBlockEntity;
import net.borisshoes.arcananovum.blocks.forge.MidnightEnchanterBlockEntity;
import net.borisshoes.arcananovum.blocks.forge.StarlightForge;
import net.borisshoes.arcananovum.blocks.forge.StarlightForgeBlockEntity;
import net.borisshoes.arcananovum.items.normal.GraphicItems;
import net.borisshoes.arcananovum.items.normal.GraphicalItem;
import net.borisshoes.arcananovum.utils.*;
import net.minecraft.block.GrindstoneBlock;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.EnchantmentTags;
import net.minecraft.screen.GrindstoneScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;

public class MidnightEnchanterGui extends SimpleGui {
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
      boolean enchanted = EnchantmentHelper.hasEnchantments(stack);
      boolean precision = ArcanaAugments.getAugmentFromMap(blockEntity.getAugments(),ArcanaAugments.PRECISION_DISENCHANTING.id) >= 1;
      
      if(index == 49){
         if(stack.isEnchantable() && !enchanted && bookshelves >= 0){
            if(lapisLevel == -1){
               player.sendMessage(Text.literal("You must select a Lapis Level").formatted(Formatting.RED,Formatting.ITALIC),true);
               SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_FIRE_EXTINGUISH, 1,1);
               return true;
            }
            if(player.experienceLevel < xpCost && !player.isCreative()){
               player.sendMessage(Text.literal("You do not have enough levels").formatted(Formatting.RED,Formatting.ITALIC),true);
               SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_FIRE_EXTINGUISH, 1,1);
               return true;
            }
            
            if(MiscUtils.removeItems(player,Items.LAPIS_LAZULI,lapisLevel)){
               player.applyEnchantmentCosts(stack,0);
               applyEnchants();
               removeXP(lapisLevel);
               MiscUtils.returnItems(inv,player);
               SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, 1f, (float) (Math.random()*0.4f + 0.8f));
               listener.setUpdating();
               inv.setStack(0,ItemStack.EMPTY);
               setItem(ItemStack.EMPTY);
               listener.finishUpdate();
            }else{
               player.sendMessage(Text.literal("You do not have enough ").formatted(Formatting.RED,Formatting.ITALIC)
                     .append(Text.translatable(Items.LAPIS_LAZULI.getTranslationKey()).formatted(Formatting.BLUE,Formatting.ITALIC)),false);
               SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_FIRE_EXTINGUISH, 1,1);
            }
         }else if(stack.isOf(ArcanaRegistry.EMPOWERED_ARCANE_PAPER)){
            if(player.experienceLevel >= xpCost || player.isCreative()){
               if(MiscUtils.removeItems(player,ArcanaRegistry.NEBULOUS_ESSENCE,essenceCost)){
                  listener.setUpdating();
                  ItemStack newStack = new ItemStack(ArcanaRegistry.EXOTIC_ARCANE_PAPER);
                  newStack.setCount(stack.getCount());
                  inv.setStack(0,newStack);
                  removeXP(xpCost);
                  SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, 1f, (float) (Math.random()*0.4f + 0.8f));
                  MiscUtils.returnItems(inv,player);
                  inv.setStack(0,ItemStack.EMPTY);
                  setItem(ItemStack.EMPTY);
                  listener.finishUpdate();
               }else{
                  player.sendMessage(Text.literal("You do not have enough ").formatted(Formatting.RED,Formatting.ITALIC)
                        .append(Text.translatable(ArcanaRegistry.NEBULOUS_ESSENCE.getTranslationKey()).formatted(Formatting.DARK_PURPLE,Formatting.ITALIC)),false);
                  SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_FIRE_EXTINGUISH, 1,1);
               }
            }else{
               player.sendMessage(Text.literal("You do not have enough levels").formatted(Formatting.RED,Formatting.ITALIC),true);
               SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_FIRE_EXTINGUISH, 1,1);
            }
         }else{
            if(getSelected().isEmpty()) return true;
            if(player.experienceLevel >= xpCost || player.isCreative()){
               if(MiscUtils.removeItems(player,ArcanaRegistry.NEBULOUS_ESSENCE,essenceCost)){
                  applyEnchants();
                  removeXP(xpCost);
                  SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, 1f, (float) (Math.random()*0.4f + 0.8f));
                  MiscUtils.returnItems(inv,player);
                  listener.setUpdating();
                  inv.setStack(0,ItemStack.EMPTY);
                  setItem(ItemStack.EMPTY);
                  listener.finishUpdate();
               }else{
                  player.sendMessage(Text.literal("You do not have enough ").formatted(Formatting.RED,Formatting.ITALIC)
                        .append(Text.translatable(ArcanaRegistry.NEBULOUS_ESSENCE.getTranslationKey()).formatted(Formatting.DARK_PURPLE,Formatting.ITALIC)),false);
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
               ItemEnchantmentsComponent comp = EnchantmentHelper.getEnchantments(stack);
               EnchantmentHelper.set(book,comp);
               disenchantItem();
            }else{
               ItemEnchantmentsComponent.Builder enchantBuilder = new ItemEnchantmentsComponent.Builder(ItemEnchantmentsComponent.DEFAULT);
               Pair<RegistryEntry<Enchantment>, Integer> entry = removeTopEnchant();
               enchantBuilder.add(entry.getLeft(),entry.getRight());
               EnchantmentHelper.set(book, enchantBuilder.build());
            }
            SimpleInventory sinv = new SimpleInventory(book);
            SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, 1f, (float) (Math.random()*0.4f + 0.8f));
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
            SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, 1f, (float) (Math.random()*0.4f + 0.8f));
            MiscUtils.returnItems(sinv,player);
            
            int maxCount = 0;
            
            ItemEnchantmentsComponent comp = EnchantmentHelper.getEnchantments(stack);
            for(Object2IntMap.Entry<RegistryEntry<Enchantment>> entry : comp.getEnchantmentEntries()){
               if(entry.getIntValue() == entry.getKey().value().getMaxLevel()) maxCount++;
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
         ArcaneSingularityBlockEntity singularity;
         if(forge != null && (singularity = (ArcaneSingularityBlockEntity) forge.getForgeAddition(serverWorld,ArcanaRegistry.ARCANE_SINGULARITY_BLOCK_ENTITY)) != null){
            if(singularity.getNumBooks() < singularity.getCapacity()){
               if(MiscUtils.removeItems(player,Items.BOOK,1)){
                  ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
                  if(type == ClickType.MOUSE_RIGHT || type == ClickType.MOUSE_RIGHT_SHIFT){
                     ItemEnchantmentsComponent comp = EnchantmentHelper.getEnchantments(stack);
                     EnchantmentHelper.set(book,comp);
                     disenchantItem();
                  }else{
                     ItemEnchantmentsComponent.Builder enchantBuilder = new ItemEnchantmentsComponent.Builder(ItemEnchantmentsComponent.DEFAULT);
                     Pair<RegistryEntry<Enchantment>, Integer> entry = removeTopEnchant();
                     enchantBuilder.add(entry.getLeft(),entry.getRight());
                     EnchantmentHelper.set(book, enchantBuilder.build());
                  }
                  singularity.addBook(book);
               }else{
                  player.sendMessage(Text.literal("You need a book to put the enchants on").formatted(Formatting.RED,Formatting.ITALIC),true);
                  SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_FIRE_EXTINGUISH, 1,1);
               }
            }else{
               player.sendMessage(Text.literal("The Singularity does not have enough space").formatted(Formatting.RED,Formatting.ITALIC),true);
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
      boolean paperUpgrade = stack.isOf(ArcanaRegistry.EMPOWERED_ARCANE_PAPER);
      
      MiscUtils.outlineGUI(this,ArcanaColors.ARCANA_COLOR,Text.empty());
      clearSlot(4);
      
      if(inv == null){
         inv = new MidnightEnchanterInventory();
      }
      setSlotRedirect(4,new MidnightEnchanterSlot(inv,0,0,0));
      if(listener == null){
         listener = new MidnightEnchanterInventoryListener(this,blockEntity);
         inv.addListener(listener);
      }
      
      boolean enchanted = EnchantmentHelper.hasEnchantments(stack);
      Text name = stack.isEmpty() ? Text.literal("Insert an Enchanted or Enchantable Item").formatted(Formatting.DARK_PURPLE) : Text.literal("Add Enchantments or Disenchant Your Item").formatted(Formatting.LIGHT_PURPLE);
      int color = stack.isEmpty() ? ArcanaColors.DARK_COLOR : ArcanaColors.LAPIS_COLOR;
      
      for(int i = 0; i < 9; i++){
         if(i == 0){
            setSlot(i+9, GuiElementBuilder.from(GraphicalItem.withColor(GraphicItems.MENU_LEFT_CONNECTOR,color)).hideDefaultTooltip().setName(name));
         }else if(i == 8){
            setSlot(i+9, GuiElementBuilder.from(GraphicalItem.withColor(GraphicItems.MENU_RIGHT_CONNECTOR,color)).hideDefaultTooltip().setName(name));
         }else if(i == 3){
            setSlot(i, GuiElementBuilder.from(GraphicalItem.withColor(GraphicItems.MENU_TOP_RIGHT,color)).hideDefaultTooltip().setName(name));
            setSlot(i+9, GuiElementBuilder.from(GraphicalItem.withColor(GraphicItems.MENU_BOTTOM_CONNECTOR,color)).hideDefaultTooltip().setName(name));
         }else if(i == 5){
            setSlot(i, GuiElementBuilder.from(GraphicalItem.withColor(GraphicItems.MENU_TOP_LEFT,color)).hideDefaultTooltip().setName(name));
            setSlot(i+9, GuiElementBuilder.from(GraphicalItem.withColor(GraphicItems.MENU_BOTTOM_CONNECTOR,color)).hideDefaultTooltip().setName(name));
         }else{
            setSlot(i+9, GuiElementBuilder.from(GraphicalItem.withColor(GraphicItems.MENU_HORIZONTAL,color)).hideDefaultTooltip().setName(name));
         }
      }
      
      GuiElementBuilder nextArrow = GuiElementBuilder.from(GraphicalItem.with(GraphicItems.RIGHT_ARROW)).hideDefaultTooltip();
      nextArrow.setName((Text.literal("")
            .append(Text.literal("Next Page").formatted(Formatting.GOLD))));
      nextArrow.addLoreLine(TextUtils.removeItalics((Text.literal("")
            .append(Text.literal("("+page+" of "+maxPages+")").formatted(Formatting.DARK_PURPLE)))));
      setSlot(35,nextArrow);
      
      GuiElementBuilder prevArrow = GuiElementBuilder.from(GraphicalItem.with(GraphicItems.LEFT_ARROW)).hideDefaultTooltip();
      prevArrow.setName((Text.literal("")
            .append(Text.literal("Prev Page").formatted(Formatting.GOLD))));
      prevArrow.addLoreLine(TextUtils.removeItalics((Text.literal("")
            .append(Text.literal("("+page+" of "+maxPages+")").formatted(Formatting.DARK_PURPLE)))));
      setSlot(27,prevArrow);
      
      if(enchanted){
         GuiElementBuilder essenceItem = GuiElementBuilder.from(MiscUtils.removeLore(ArcanaRegistry.NEBULOUS_ESSENCE.getDefaultStack())).hideDefaultTooltip();
         essenceItem.setName((Text.literal("")
               .append(Text.literal("Disenchant into Essence").formatted(Formatting.DARK_AQUA))));
         essenceItem.addLoreLine(TextUtils.removeItalics((Text.literal("")
               .append(Text.literal("Click").formatted(Formatting.AQUA))
               .append(Text.literal(" to disenchant this item").formatted(Formatting.DARK_PURPLE)))));
         setSlot(7,essenceItem);
         
         if(precision){
            GuiElementBuilder disenchantBookItem = new GuiElementBuilder(Items.BOOK).hideDefaultTooltip();
            disenchantBookItem.setName((Text.literal("")
                  .append(Text.literal("Disenchant onto a Book").formatted(Formatting.BLUE))));
            disenchantBookItem.addLoreLine(TextUtils.removeItalics((Text.literal("")
                  .append(Text.literal("Click").formatted(Formatting.AQUA))
                  .append(Text.literal(" to move the top enchant to a book").formatted(Formatting.DARK_PURPLE)))));
            disenchantBookItem.addLoreLine(TextUtils.removeItalics((Text.literal("")
                  .append(Text.literal("Right Click").formatted(Formatting.AQUA))
                  .append(Text.literal(" to move all enchants to a book").formatted(Formatting.DARK_PURPLE)))));
            setSlot(6,disenchantBookItem);
            
            GuiElementBuilder singularityItem = new GuiElementBuilder(Items.LECTERN).hideDefaultTooltip();
            singularityItem.setName((Text.literal("")
                  .append(Text.literal("Disenchant into a Singularity").formatted(Formatting.LIGHT_PURPLE))));
            singularityItem.addLoreLine(TextUtils.removeItalics((Text.literal("")
                  .append(Text.literal("Click").formatted(Formatting.AQUA))
                  .append(Text.literal(" to move the top enchant to a Singularity").formatted(Formatting.DARK_PURPLE)))));
            singularityItem.addLoreLine(TextUtils.removeItalics((Text.literal("")
                  .append(Text.literal("Right Click").formatted(Formatting.AQUA))
                  .append(Text.literal(" to move all enchants to a Singularity").formatted(Formatting.DARK_PURPLE)))));
            setSlot(8,singularityItem);
         }
      }else if(paperUpgrade){
         GuiElementBuilder exoticPaper = GuiElementBuilder.from(MiscUtils.removeLore(ArcanaRegistry.EXOTIC_ARCANE_PAPER.getDefaultStack()));
         setSlot(7,exoticPaper);
      }else{
         GuiElementBuilder essenceItem;
         if(bookshelves < 0){
            essenceItem = new GuiElementBuilder(Items.ENDER_EYE).hideDefaultTooltip();
            essenceItem.setName((Text.literal("")
                  .append(Text.literal("Enlightened Enchantment").formatted(Formatting.DARK_AQUA))));
            essenceItem.addLoreLine(TextUtils.removeItalics((Text.literal("")
                  .append(Text.literal("Choose").formatted(Formatting.AQUA))
                  .append(Text.literal(" your enchantments").formatted(Formatting.DARK_PURPLE)))));
            essenceItem.addLoreLine(TextUtils.removeItalics((Text.literal("")
                  .append(Text.literal("Click").formatted(Formatting.AQUA))
                  .append(Text.literal(" to go to ").formatted(Formatting.DARK_PURPLE))
                  .append(Text.literal("Regular Enchantment").formatted(Formatting.BLUE)))));
         }else{
            if(bookshelves == 0){
               essenceItem = new GuiElementBuilder(Items.CHISELED_BOOKSHELF).hideDefaultTooltip().setCount(1);
            }else{
               essenceItem = new GuiElementBuilder(Items.BOOKSHELF).hideDefaultTooltip().setCount(bookshelves);
            }
            essenceItem.setName((Text.literal("")
                  .append(Text.literal("Regular Enchantment").formatted(Formatting.BLUE))));
            essenceItem.addLoreLine(TextUtils.removeItalics((Text.literal("")
                  .append(Text.literal(""+bookshelves).formatted(Formatting.AQUA))
                  .append(Text.literal(" Bookshelves").formatted(Formatting.DARK_PURPLE)))));
            essenceItem.addLoreLine(TextUtils.removeItalics((Text.literal("")
                  .append(Text.literal("Left Click").formatted(Formatting.AQUA))
                  .append(Text.literal(" to increase bookshelves").formatted(Formatting.DARK_PURPLE)))));
            essenceItem.addLoreLine(TextUtils.removeItalics((Text.literal("")
                  .append(Text.literal("Right Click").formatted(Formatting.AQUA))
                  .append(Text.literal(" to decrease bookshelves").formatted(Formatting.DARK_PURPLE)))));
            essenceItem.addLoreLine(TextUtils.removeItalics((Text.literal("")
                  .append(Text.literal("Middle Click").formatted(Formatting.AQUA))
                  .append(Text.literal(" to go to ").formatted(Formatting.DARK_PURPLE))
                  .append(Text.literal("Enlightened Enchantment").formatted(Formatting.DARK_AQUA)))));
         }
         setSlot(7,essenceItem);
      }
      
      if(stack.isEnchantable() && !enchanted && bookshelves >= 0){
         for(int i = 19; i < 26; i++){
            GuiElementBuilder bgPane2 = GuiElementBuilder.from(GraphicalItem.withColor(GraphicItems.PAGE_BG, ArcanaColors.DARK_COLOR)).hideDefaultTooltip();
            bgPane2.setName(Text.empty()).hideTooltip();
            setSlot(i,bgPane2);
            setSlot(i+9,bgPane2);
            setSlot(i+18,bgPane2);
         }
         setSlot(11,GuiElementBuilder.from(GraphicalItem.withColor(GraphicItems.MENU_TOP_CONNECTOR,color)).hideDefaultTooltip().setName(name));
         setSlot(20,GuiElementBuilder.from(GraphicalItem.withColor(GraphicItems.MENU_VERTICAL,color)).hideDefaultTooltip().setName(name));
         setSlot(29,GuiElementBuilder.from(GraphicalItem.withColor(GraphicItems.MENU_VERTICAL,color)).hideDefaultTooltip().setName(name));
         setSlot(38,GuiElementBuilder.from(GraphicalItem.withColor(GraphicItems.MENU_VERTICAL,color)).hideDefaultTooltip().setName(name));
         setSlot(47,GuiElementBuilder.from(GraphicalItem.withColor(GraphicItems.MENU_BOTTOM_CONNECTOR,ArcanaColors.ARCANA_COLOR)).hideTooltip());
         
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
               GuiElementBuilder lapisItem = new GuiElementBuilder(Items.LAPIS_LAZULI).hideDefaultTooltip().setCount(i+1);
               lapisItem.setName((Text.literal("")
                     .append(Text.literal("Slot "+(i+1)).formatted(Formatting.BLUE))));
               if(lapisLevel == i+1){
                  lapisItem.glow();
                  lapisItem.addLoreLine(TextUtils.removeItalics((Text.literal("")
                        .append(Text.literal("Selected").formatted(Formatting.AQUA)))));
                  
                  setSelectedFromList(list);
                  xpCost = enchPowers[i];
               }else{
                  lapisItem.addLoreLine(TextUtils.removeItalics((Text.literal("")
                        .append(Text.literal("Click").formatted(Formatting.AQUA))
                        .append(Text.literal(" to select").formatted(Formatting.DARK_PURPLE)))));
               }
               setSlot(19 + i*9,lapisItem);
               
               for(int j = 0; j < list.size() && j < 5; j++){
                  EnchantmentLevelEntry entry = list.get(j);
                  
                  GuiElementBuilder enchantBook = new GuiElementBuilder(Items.ENCHANTED_BOOK).glow().hideDefaultTooltip();
                  enchantBook.setName((Text.literal("")
                        .append(Enchantment.getName(entry.enchantment,entry.level)).formatted(Formatting.AQUA)));
                  setSlot(21 + i*9 + j,enchantBook);
               }
            }
         }
      }else if(paperUpgrade){
         for(int i = 0; i < 3; i++){
            for(int j = 0; j < 7; j++){
               setSlot((i*9+19)+j,GuiElementBuilder.from(GraphicalItem.withColor(GraphicItems.PAGE_BG, ArcanaColors.DARK_COLOR)).hideTooltip());
            }
         }
         xpCost = 10;
         essenceCost = stack.getCount();
      }else{
         List<EnchantEntry> pageItems = getPage();
         int k = 0;
         for(int i = 0; i < 3; i++){
            for(int j = 0; j < 7; j++){
               if(k < pageItems.size()){
                  EnchantEntry pageItem = pageItems.get(k);
                  GuiElementBuilder enchantBook = new GuiElementBuilder(pageItem.selected ? Items.ENCHANTED_BOOK : Items.WRITTEN_BOOK).glow().hideDefaultTooltip();
                  enchantBook.setName((Text.literal("")
                        .append(Enchantment.getName(pageItem.enchantment,pageItem.level)).formatted(Formatting.AQUA)));
                  if(pageItem.selected){
                     enchantBook.addLoreLine(TextUtils.removeItalics(Text.literal("Selected").formatted(Formatting.YELLOW)));
                     enchantBook.addLoreLine(TextUtils.removeItalics(Text.literal("").formatted(Formatting.YELLOW)));
                  }
                  if(isCompatible(pageItem.enchantment, pageItem.level)){
                     enchantBook.addLoreLine(TextUtils.removeItalics((Text.literal("")
                           .append(Text.literal("Click").formatted(Formatting.BLUE))
                           .append(Text.literal(" to toggle selection").formatted(Formatting.DARK_PURPLE)))));
                  }else{
                     enchantBook.addLoreLine(TextUtils.removeItalics(Text.literal("Incompatible Enchant").formatted(Formatting.RED)));
                  }
                  
                  setSlot((i*9+19)+j,enchantBook);
               }else{
                  setSlot((i*9+19)+j,GuiElementBuilder.from(GraphicalItem.withColor(GraphicItems.PAGE_BG, stack.isEmpty() ? ArcanaColors.DARK_COLOR : ArcanaColors.LAPIS_COLOR)).setName(Text.empty()).hideTooltip());
               }
               k++;
            }
         }
      }
      
      List<EnchantEntry> selected = getSelected();
      
      GuiElementBuilder enchantItem = new GuiElementBuilder(Items.ENCHANTING_TABLE).hideDefaultTooltip();
      if(paperUpgrade){
         enchantItem.setName((Text.literal("")
               .append(Text.literal("Enchant Exotic Arcane Paper").formatted(Formatting.LIGHT_PURPLE))));
         enchantItem.addLoreLine(TextUtils.removeItalics((Text.literal("")
               .append(Text.literal("Click").formatted(Formatting.AQUA))
               .append(Text.literal(" to upgrade your Arcane Paper").formatted(Formatting.DARK_PURPLE)))));
      }else{
         enchantItem.setName((Text.literal("")
               .append(Text.literal("Enchant ").formatted(Formatting.LIGHT_PURPLE))
               .append(Text.translatable(stack.getItem().getTranslationKey()).formatted(Formatting.LIGHT_PURPLE))));
         enchantItem.addLoreLine(TextUtils.removeItalics((Text.literal("")
               .append(Text.literal("Click").formatted(Formatting.AQUA))
               .append(Text.literal(" to enchant the ").formatted(Formatting.DARK_PURPLE))
               .append(Text.translatable(stack.getItem().getTranslationKey()).formatted(Formatting.DARK_PURPLE)))));
      }
      
      
      if(!selected.isEmpty()){
         enchantItem.addLoreLine(TextUtils.removeItalics(Text.empty()));
         enchantItem.addLoreLine(TextUtils.removeItalics(Text.literal("Adding: ").formatted(Formatting.DARK_PURPLE)));
         
         for(EnchantEntry entry : selected){
            enchantItem.addLoreLine(TextUtils.removeItalics((Text.literal("")
                  .append(Enchantment.getName(entry.enchantment,entry.level)).formatted(Formatting.AQUA))));
         }
      }
      setSlot(49,enchantItem);
      
      GuiElementBuilder xpItem = new GuiElementBuilder(Items.EXPERIENCE_BOTTLE).hideDefaultTooltip();
      String costStr = bookshelves >= 0 && !paperUpgrade ? "Lapis" : "Essence";
      xpItem.setName((Text.literal("")
            .append(Text.literal("XP & "+costStr+" Cost").formatted(Formatting.GREEN))));
      if(xpCost > 0 && (!selected.isEmpty() || paperUpgrade)){
         xpItem.addLoreLine(TextUtils.removeItalics((Text.literal("")
               .append(Text.literal(xpCost+" Levels").formatted(Formatting.DARK_GREEN)))));
         if(stack.isEnchantable() && !enchanted && bookshelves >= 0){
            xpItem.addLoreLine(TextUtils.removeItalics((Text.literal("")
                  .append(Text.literal(lapisLevel+" Lapis Lazuli").formatted(Formatting.BLUE)))));
         }else{
            xpItem.addLoreLine(TextUtils.removeItalics((Text.literal("")
                  .append(Text.literal(essenceCost+" Nebulous Essence").formatted(Formatting.DARK_PURPLE)))));
         }
      }else{
         xpItem.addLoreLine(TextUtils.removeItalics((Text.literal("")
               .append(Text.literal("Select Enchantments").formatted(Formatting.DARK_GREEN)))));
      }
      setSlot(1,xpItem);
   }
   
   private void removeXP(int levels){
       boolean expertise = ArcanaAugments.getAugmentFromMap(blockEntity.getAugments(),ArcanaAugments.ENCHANTING_EXPERTISE.id) > 0;
       if(player.isCreative()) return;
      
      int points = LevelUtils.vanillaLevelToTotalXp(levels);
      if(expertise){
         player.addExperience(-points);
      }else{
         player.addExperienceLevels(-levels);
      }
   }
   
   private boolean isCompatible(RegistryEntry<Enchantment> enchant, int level){
      ItemEnchantmentsComponent comp = EnchantmentHelper.getEnchantments(stack);
      Object2IntOpenHashMap<RegistryEntry<Enchantment>> enchants = new Object2IntOpenHashMap<>();
      comp.getEnchantmentEntries().forEach(entry -> enchants.addTo(entry.getKey(),entry.getIntValue()));
      
      boolean upgrade = comp.getEnchantments().contains(enchant) && comp.getLevel(enchant) < level;
      for(EnchantEntry entry : getSelected()){
         enchants.addTo(entry.enchantment(),entry.level());
         
         if(entry.enchantment().value() == enchant.value() && entry.level() != level && upgrade){
            return false;
         }
         if(entry.enchantment().value() == enchant.value() && entry.level() == level){
            return true;
         }
      }
      if(upgrade) return true;
      return EnchantmentHelper.isCompatible(enchants.keySet(),enchant);
   }
   
   private void applyEnchants(){
      listener.setUpdating();
      ItemEnchantmentsComponent.Builder enchantBuilder = new ItemEnchantmentsComponent.Builder(EnchantmentHelper.getEnchantments(stack));
      for(EnchantEntry entry : getSelected()){
         enchantBuilder.remove(e -> e == entry.enchantment());
         enchantBuilder.add(entry.enchantment(),entry.level());
         
         if(entry.level == 1 && entry.enchantment().value().getMaxLevel() > 1){
            ArcanaAchievements.grant(player,ArcanaAchievements.ENCHANTING_OVERKILL.id);
         }
      }
      if(stack.isOf(Items.BOOK)){
         stack = stack.withItem(Items.ENCHANTED_BOOK);
      }
      
      EnchantmentHelper.set(stack,enchantBuilder.build());
      inv.setStack(0,stack);
      listener.finishUpdate();
   }
   
   private void disenchantItem(){
      listener.setUpdating();
      
      EnchantmentHelper.apply(stack, components -> components.remove(enchantment -> true));
      if(stack.isOf(Items.ENCHANTED_BOOK)){
         stack = stack.withItem(Items.BOOK);
      }
      
      if(ArcanaItemUtils.isArcane(stack)){
         ArcanaItemUtils.identifyItem(stack).buildItemLore(stack, ArcanaNovum.SERVER);
      }
      inv.setStack(0,stack);
      listener.finishUpdate();
   }
   
   private Pair<RegistryEntry<Enchantment>,Integer> removeTopEnchant(){
      ItemEnchantmentsComponent comp = EnchantmentHelper.getEnchantments(stack);
      Object2IntOpenHashMap<RegistryEntry<Enchantment>> enchants = new Object2IntOpenHashMap<>();
      comp.getEnchantmentEntries().forEach(entry -> enchants.addTo(entry.getKey(),entry.getIntValue()));
      
      if(enchants.size() == 1){
         disenchantItem();
         for(RegistryEntry<Enchantment> entry : enchants.keySet()){
            return new Pair<>(entry,enchants.getInt(entry));
         }
      }
      listener.setUpdating();
      
      RegistryEntryList<Enchantment> registryEntryList = null;
      Optional<RegistryEntryList.Named<Enchantment>> optional = player.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT).getOptional(EnchantmentTags.TOOLTIP_ORDER);
      if (optional.isPresent()) {
         registryEntryList = optional.get();
      }
      
      RegistryEntry<Enchantment> registryEntry = null;
      int value = 0;
      int index = Integer.MAX_VALUE;
      
      ObjectIterator<Object2IntMap.Entry<RegistryEntry<Enchantment>>> iter = enchants.object2IntEntrySet().fastIterator();
      while(iter.hasNext()){
         Object2IntMap.Entry<RegistryEntry<Enchantment>> entry = iter.next();
         if(registryEntryList == null){
            registryEntry = entry.getKey();
            value = entry.getIntValue();
            iter.remove();
            break;
         }
         
         for(int i = 0; i < registryEntryList.size(); i++){
            if(registryEntryList.get(i).value() == entry.getKey().value() && i < index){
               index = i;
               registryEntry = entry.getKey();
               value = entry.getIntValue();
            }
         }
      }
      
      if(index != Integer.MAX_VALUE && registryEntry != null){
         RegistryEntry<Enchantment> finalRegistryEntry = registryEntry;
         int finalValue = value;
         enchants.object2IntEntrySet().removeIf(e -> e.getKey().value() == finalRegistryEntry.value() && finalValue == e.getIntValue());
      }
      
      ItemEnchantmentsComponent.Builder enchantBuilder = new ItemEnchantmentsComponent.Builder(ItemEnchantmentsComponent.DEFAULT);
      enchants.forEach(enchantBuilder::add);
      EnchantmentHelper.set(stack,enchantBuilder.build());
      
      inv.setStack(0,stack);
      listener.finishUpdate();
      return new Pair<>(registryEntry,value);
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
      RegistryWrapper.Impl<Enchantment> impl = player.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT);
      entries.forEach(e -> enchants.add(new EnchantEntry(impl.getOrThrow(e.enchantment.getKey().get()), e.level,true)));
   }
   
   public void calculateXPCost(){
      boolean paperUpgrade = stack.isOf(ArcanaRegistry.EMPOWERED_ARCANE_PAPER);
      int cost = 0;
      int eCost = 0;
      if(paperUpgrade){
         xpCost = 10;
         essenceCost = stack.getCount();
         return;
      }
      
      ItemEnchantmentsComponent comp = EnchantmentHelper.getEnchantments(stack);
      Object2IntOpenHashMap<RegistryEntry<Enchantment>> curEnchants = new Object2IntOpenHashMap<>();
      comp.getEnchantmentEntries().forEach(entry -> curEnchants.addTo(entry.getKey(),entry.getIntValue()));
      
      for(RegistryEntry<Enchantment> entry : curEnchants.keySet()){
         int rarityMod = entry.value().getAnvilCost();
         if(entry.isIn(EnchantmentTags.TREASURE)){
            rarityMod *= 2;
         }
         cost += curEnchants.getInt(entry)*rarityMod;
      }
      cost /= 2; // Half cost for existing enchants
      
      for(EnchantEntry entry : getSelected()){
         int rarityMod = entry.enchantment().value().getAnvilCost();
         if(entry.enchantment().isIn(EnchantmentTags.TREASURE)){
            rarityMod *= 2;
         }
         cost += entry.level()*rarityMod;
         eCost += (int) Math.ceil(MiscUtils.calcEssenceValue(entry.enchantment(),entry.level())*1.5);
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
      ItemEnchantmentsComponent comp = EnchantmentHelper.getEnchantments(stack);
      Object2IntOpenHashMap<RegistryEntry<Enchantment>> curEnchants = new Object2IntOpenHashMap<>();
      comp.getEnchantmentEntries().forEach(entry -> curEnchants.addTo(entry.getKey(),entry.getIntValue()));
      
      List<EnchantEntry> possibleAdditions = new ArrayList<>();
      
      for(RegistryEntry<Enchantment> entry : player.getWorld().getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT).getIndexedEntries()){
         Enchantment enchantment = entry.value();
         
         if(!EnchantmentHelper.isCompatible(curEnchants.keySet(),entry) && !curEnchants.containsKey(entry)) continue; // Remove incompatible enchants
         if(!stack.isOf(Items.BOOK) && !enchantment.isSupportedItem(stack)) continue; // Remove enchants for wrong items
         
         if(curEnchants.containsKey(entry)){
            int curLevel = curEnchants.getInt(entry);
            int maxLevel = enchantment.getMaxLevel();
            if(curLevel < maxLevel){ // Allow level increases
               for(int i = curLevel+1; i <= maxLevel; i++){
                  
                  possibleAdditions.add(new EnchantEntry(entry,i,false));
               }
            }
         }else{ // Add possible additions
            for(int i = enchantment.getMinLevel(); i <= enchantment.getMaxLevel(); i++){
               possibleAdditions.add(new EnchantEntry(entry,i,false));
            }
         }
      }
      
      return possibleAdditions;
   }
   
   private List<EnchantmentLevelEntry> generateEnchantments(ItemStack stack, int slot, int level, Random random, int playerSeed) {
      random.setSeed(playerSeed + slot);
      Optional<RegistryEntryList.Named<Enchantment>> optional = player.getWorld().getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT).getOptional(EnchantmentTags.IN_ENCHANTING_TABLE);
      if (optional.isEmpty()){
         return List.of();
      }
      List<EnchantmentLevelEntry> list = EnchantmentHelper.generateEnchantments(random, stack, level, optional.get().stream());
      if (stack.isOf(Items.BOOK) && list.size() > 1) {
         list.remove(random.nextInt(list.size()));
      }
      return list;
   }
   
   @Override
   public void onTick(){
      World world = blockEntity.getWorld();
      if(world == null || world.getBlockEntity(blockEntity.getPos()) != blockEntity || !blockEntity.isAssembled()){
         this.close();
      }
      
      super.onTick();
   }
   
   @Override
   public void onClose(){
      MiscUtils.returnItems(inv,player);
   }
   
   @Override
   public void close(){
      super.close();
   }
   
   private record EnchantEntry(RegistryEntry<Enchantment> enchantment, int level, boolean selected){}
}

