package net.borisshoes.arcananovum.gui.midnightenchanter;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.borisshoes.arcananovum.ArcanaConfig;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.blocks.forge.ArcaneSingularityBlockEntity;
import net.borisshoes.arcananovum.blocks.forge.MidnightEnchanterBlockEntity;
import net.borisshoes.arcananovum.blocks.forge.StarlightForge;
import net.borisshoes.arcananovum.blocks.forge.StarlightForgeBlockEntity;
import net.borisshoes.arcananovum.gui.VirtualInventoryGui;
import net.borisshoes.arcananovum.utils.ArcanaColors;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.arcananovum.utils.ArcanaUtils;
import net.borisshoes.arcananovum.utils.LevelUtils;
import net.borisshoes.borislib.BorisLib;
import net.borisshoes.borislib.gui.GraphicalItem;
import net.borisshoes.borislib.gui.GuiHelper;
import net.borisshoes.borislib.utils.MinecraftUtils;
import net.borisshoes.borislib.utils.SoundUtils;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Tuple;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerListener;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MidnightEnchanterGui extends VirtualInventoryGui<SimpleContainer> implements ContainerListener {
   private final MidnightEnchanterBlockEntity blockEntity;
   private int page = 1;
   private int maxPages = 1;
   private int xpCost = 0;
   private int essenceCost = 0;
   private int bookshelves = -1; // Mode for enchanting unenchanted items
   private int lapisLevel = 0;
   private boolean updating = false;
   private List<EnchantEntry> enchants = new ArrayList<>();
   
   public MidnightEnchanterGui(ServerPlayer player, MidnightEnchanterBlockEntity blockEntity){
      super(MenuType.GENERIC_9x6, player, false);
      this.blockEntity = blockEntity;
      setTitle(Component.literal("Midnight Enchanter"));
      inventory = new SimpleContainer(1);
      inventory.addListener(this);
   }
   
   @Override
   public boolean onAnyClick(int index, ClickType type, net.minecraft.world.inventory.ClickType action){
      if(!(blockEntity.getLevel() instanceof ServerLevel serverWorld)) return true;
      boolean enchanted = EnchantmentHelper.hasAnyEnchantments(getStack());
      boolean precision = ArcanaAugments.getAugmentFromMap(blockEntity.getAugments(),ArcanaAugments.PRECISION_DISENCHANTING.id) >= 1;
      
      if(index == 49){
         if(getStack().isEnchantable() && !enchanted && bookshelves >= 0){
            if(lapisLevel == -1){
               player.displayClientMessage(Component.literal("You must select a Lapis Level").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC),true);
               SoundUtils.playSongToPlayer(player, SoundEvents.FIRE_EXTINGUISH, 1,1);
               return true;
            }
            if(player.experienceLevel < xpCost && !player.isCreative()){
               player.displayClientMessage(Component.literal("You do not have enough levels").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC),true);
               SoundUtils.playSongToPlayer(player, SoundEvents.FIRE_EXTINGUISH, 1,1);
               return true;
            }
            
            if(MinecraftUtils.removeItems(player, Items.LAPIS_LAZULI,lapisLevel)){
               player.onEnchantmentPerformed(getStack(),0);
               applyEnchants();
               removeXP(lapisLevel);
               MinecraftUtils.returnItems(inventory,player);
               SoundUtils.playSongToPlayer(player, SoundEvents.ENCHANTMENT_TABLE_USE, 1f, (float) (Math.random()*0.4f + 0.8f));
               setUpdating();
               setItem(ItemStack.EMPTY);
               finishUpdate();
            }else{
               player.displayClientMessage(Component.literal("You do not have enough ").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC)
                     .append(Component.translatable(Items.LAPIS_LAZULI.getDescriptionId()).withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC)),false);
               SoundUtils.playSongToPlayer(player, SoundEvents.FIRE_EXTINGUISH, 1,1);
            }
         }else if(getStack().is(ArcanaRegistry.EMPOWERED_ARCANE_PAPER)){
            if(player.experienceLevel >= xpCost || player.isCreative()){
               if(MinecraftUtils.removeItems(player,ArcanaRegistry.NEBULOUS_ESSENCE,essenceCost)){
                  setUpdating();
                  ItemStack newStack = new ItemStack(ArcanaRegistry.EXOTIC_ARCANE_PAPER);
                  newStack.setCount(getStack().getCount());
                  setItem(newStack);
                  removeXP(xpCost);
                  SoundUtils.playSongToPlayer(player, SoundEvents.ENCHANTMENT_TABLE_USE, 1f, (float) (Math.random()*0.4f + 0.8f));
                  MinecraftUtils.returnItems(inventory,player);
                  setItem(ItemStack.EMPTY);
                  finishUpdate();
               }else{
                  player.displayClientMessage(Component.literal("You do not have enough ").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC)
                        .append(Component.translatable(ArcanaRegistry.NEBULOUS_ESSENCE.getDescriptionId()).withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC)),false);
                  SoundUtils.playSongToPlayer(player, SoundEvents.FIRE_EXTINGUISH, 1,1);
               }
            }else{
               player.displayClientMessage(Component.literal("You do not have enough levels").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC),true);
               SoundUtils.playSongToPlayer(player, SoundEvents.FIRE_EXTINGUISH, 1,1);
            }
         }else{
            if(getSelected().isEmpty()) return true;
            if(player.experienceLevel >= xpCost || player.isCreative()){
               if(MinecraftUtils.removeItems(player,ArcanaRegistry.NEBULOUS_ESSENCE,essenceCost)){
                  applyEnchants();
                  removeXP(xpCost);
                  SoundUtils.playSongToPlayer(player, SoundEvents.ENCHANTMENT_TABLE_USE, 1f, (float) (Math.random()*0.4f + 0.8f));
                  MinecraftUtils.returnItems(inventory,player);
                  setUpdating();
                  setItem(ItemStack.EMPTY);
                  finishUpdate();
               }else{
                  player.displayClientMessage(Component.literal("You do not have enough ").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC)
                        .append(Component.translatable(ArcanaRegistry.NEBULOUS_ESSENCE.getDescriptionId()).withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC)),false);
                  SoundUtils.playSongToPlayer(player, SoundEvents.FIRE_EXTINGUISH, 1,1);
               }
            }else{
               player.displayClientMessage(Component.literal("You do not have enough levels").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC),true);
               SoundUtils.playSongToPlayer(player, SoundEvents.FIRE_EXTINGUISH, 1,1);
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
         if(MinecraftUtils.removeItems(player, Items.BOOK,1)){
            ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
            if(type == ClickType.MOUSE_RIGHT || type == ClickType.MOUSE_RIGHT_SHIFT){
               ItemEnchantments comp = EnchantmentHelper.getEnchantmentsForCrafting(getStack());
               EnchantmentHelper.setEnchantments(book,comp);
               disenchantItem();
            }else{
               ItemEnchantments.Mutable enchantBuilder = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
               Tuple<Holder<Enchantment>, Integer> entry = removeTopEnchant();
               enchantBuilder.upgrade(entry.getA(),entry.getB());
               EnchantmentHelper.setEnchantments(book, enchantBuilder.toImmutable());
            }
            SimpleContainer sinv = new SimpleContainer(book);
            SoundUtils.playSongToPlayer(player, SoundEvents.ENCHANTMENT_TABLE_USE, 1f, (float) (Math.random()*0.4f + 0.8f));
            MinecraftUtils.returnItems(sinv,player);
         }else{
            player.displayClientMessage(Component.literal("You need a book to put the enchants on").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC),true);
            SoundUtils.playSongToPlayer(player, SoundEvents.FIRE_EXTINGUISH, 1,1);
         }
         buildGui();
      }else if(index == 7){
         if(enchanted){
            int essence = (int) (ArcanaUtils.calcEssenceFromEnchants(getStack()) * (1 + .15*ArcanaAugments.getAugmentFromMap(blockEntity.getAugments(),ArcanaAugments.ESSENCE_SUPERNOVA.id)));
            SimpleContainer sinv = new SimpleContainer(essence / 64 + 1);
            ArcanaNovum.data(player).addXP(ArcanaConfig.getInt(ArcanaRegistry.MIDNIGHT_ENCHANTER_DISENCHANT_PER_ESSENCE)*essence);
            if(essence > 0){
               while(essence > 64){
                  sinv.addItem(ArcanaRegistry.NEBULOUS_ESSENCE.getDefaultInstance().copyWithCount(64));
                  essence -= 64;
               }
               if(essence > 0){
                  sinv.addItem(ArcanaRegistry.NEBULOUS_ESSENCE.getDefaultInstance().copyWithCount(essence));
               }
            }
            SoundUtils.playSongToPlayer(player, SoundEvents.ENCHANTMENT_TABLE_USE, 1f, (float) (Math.random()*0.4f + 0.8f));
            MinecraftUtils.returnItems(sinv,player);
            
            int maxCount = 0;
            
            ItemEnchantments comp = EnchantmentHelper.getEnchantmentsForCrafting(getStack());
            for(Object2IntMap.Entry<Holder<Enchantment>> entry : comp.entrySet()){
               if(entry.getIntValue() == entry.getKey().value().getMaxLevel()) maxCount++;
            }
            if(maxCount >= 5){
               ArcanaAchievements.grant(player,ArcanaAchievements.MASTERPIECE_TO_NOTHING.id);
            }
            
            disenchantItem();
            buildGui();
         }else{
            if(type == ClickType.MOUSE_LEFT_SHIFT){
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
            this.enchants = getEnchantsForItem(this.getStack());
            lapisLevel = -1;
            xpCost = -1;
         }
         buildGui();
      }else if(index == 8 && precision && enchanted){
         StarlightForgeBlockEntity forge = StarlightForge.findActiveForge(serverWorld,blockEntity.getBlockPos());
         ArcaneSingularityBlockEntity singularity;
         if(forge != null && (singularity = (ArcaneSingularityBlockEntity) forge.getForgeAddition(serverWorld,ArcanaRegistry.ARCANE_SINGULARITY_BLOCK_ENTITY)) != null){
            if(singularity.getNumBooks() < singularity.getCapacity()){
               if(MinecraftUtils.removeItems(player, Items.BOOK,1)){
                  ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
                  if(type == ClickType.MOUSE_RIGHT || type == ClickType.MOUSE_RIGHT_SHIFT){
                     ItemEnchantments comp = EnchantmentHelper.getEnchantmentsForCrafting(getStack());
                     EnchantmentHelper.setEnchantments(book,comp);
                     disenchantItem();
                  }else{
                     ItemEnchantments.Mutable enchantBuilder = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
                     Tuple<Holder<Enchantment>, Integer> entry = removeTopEnchant();
                     enchantBuilder.upgrade(entry.getA(),entry.getB());
                     EnchantmentHelper.setEnchantments(book, enchantBuilder.toImmutable());
                  }
                  singularity.addBook(book);
               }else{
                  player.displayClientMessage(Component.literal("You need a book to put the enchants on").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC),true);
                  SoundUtils.playSongToPlayer(player, SoundEvents.FIRE_EXTINGUISH, 1,1);
               }
            }else{
               player.displayClientMessage(Component.literal("The Singularity does not have enough space").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC),true);
               SoundUtils.playSongToPlayer(player, SoundEvents.FIRE_EXTINGUISH, 1,1);
            }
         }else{
            player.displayClientMessage(Component.literal("The Enchanter's Forge does not have access to a Singularity").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC),true);
            SoundUtils.playSongToPlayer(player, SoundEvents.FIRE_EXTINGUISH, 1,1);
         }
         buildGui();
      }else if(index % 9 > 0 && index % 9 < 8 && index > 18 && index < 44){
         if(getStack().isEnchantable() && !enchanted && bookshelves >= 0){
            int row = (index / 9) - 1;
            if(getSlot(index) != null && (getSlot(index).getItemStack().is(Items.ENCHANTED_BOOK) ||  getSlot(index).getItemStack().is(Items.LAPIS_LAZULI))){
               lapisLevel = row;
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
      boolean paperUpgrade = getStack().is(ArcanaRegistry.EMPOWERED_ARCANE_PAPER);
      
      GuiHelper.outlineGUI(this,ArcanaColors.ARCANA_COLOR, Component.empty());
      clearSlot(4);
      
      setSlotRedirect(4,new MidnightEnchanterSlot(inventory,0,0,0));
      
      boolean enchanted = EnchantmentHelper.hasAnyEnchantments(getStack());
      Component name = getStack().isEmpty() ? Component.literal("Insert an Enchanted or Enchantable Item").withStyle(ChatFormatting.DARK_PURPLE) : Component.literal("Add Enchantments or Disenchant Your Item").withStyle(ChatFormatting.LIGHT_PURPLE);
      int color = getStack().isEmpty() ? ArcanaColors.DARK_COLOR : ArcanaColors.LAPIS_COLOR;
      
      for(int i = 0; i < 9; i++){
         if(i == 0){
            setSlot(i+9, GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_LEFT_CONNECTOR,color)).hideDefaultTooltip().setName(name));
         }else if(i == 8){
            setSlot(i+9, GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_RIGHT_CONNECTOR,color)).hideDefaultTooltip().setName(name));
         }else if(i == 3){
            setSlot(i, GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_TOP_RIGHT,color)).hideDefaultTooltip().setName(name));
            setSlot(i+9, GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_BOTTOM_CONNECTOR,color)).hideDefaultTooltip().setName(name));
         }else if(i == 5){
            setSlot(i, GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_TOP_LEFT,color)).hideDefaultTooltip().setName(name));
            setSlot(i+9, GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_BOTTOM_CONNECTOR,color)).hideDefaultTooltip().setName(name));
         }else{
            setSlot(i+9, GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_HORIZONTAL,color)).hideDefaultTooltip().setName(name));
         }
      }
      
      if(maxPages > 1 && !(getStack().isEnchantable() && !enchanted && bookshelves >= 0)){
         GuiElementBuilder nextArrow = GuiElementBuilder.from(GraphicalItem.with(GraphicalItem.RIGHT_ARROW)).hideDefaultTooltip();
         nextArrow.setName((Component.literal("")
               .append(Component.literal("Next Page").withStyle(ChatFormatting.GOLD))));
         nextArrow.addLoreLine(TextUtils.removeItalics((Component.literal("")
               .append(Component.literal("("+page+" of "+maxPages+")").withStyle(ChatFormatting.DARK_PURPLE)))));
         setSlot(35,nextArrow);
         
         GuiElementBuilder prevArrow = GuiElementBuilder.from(GraphicalItem.with(GraphicalItem.LEFT_ARROW)).hideDefaultTooltip();
         prevArrow.setName((Component.literal("")
               .append(Component.literal("Prev Page").withStyle(ChatFormatting.GOLD))));
         prevArrow.addLoreLine(TextUtils.removeItalics((Component.literal("")
               .append(Component.literal("("+page+" of "+maxPages+")").withStyle(ChatFormatting.DARK_PURPLE)))));
         setSlot(27,prevArrow);
      }
      
      if(enchanted){
         GuiElementBuilder essenceItem = GuiElementBuilder.from(MinecraftUtils.removeLore(ArcanaRegistry.NEBULOUS_ESSENCE.getDefaultInstance())).hideDefaultTooltip();
         essenceItem.setName((Component.literal("")
               .append(Component.literal("Disenchant into Essence").withStyle(ChatFormatting.DARK_AQUA))));
         essenceItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
               .append(Component.literal("Click").withStyle(ChatFormatting.AQUA))
               .append(Component.literal(" to disenchant this item").withStyle(ChatFormatting.DARK_PURPLE)))));
         setSlot(7,essenceItem);
         
         if(precision){
            GuiElementBuilder disenchantBookItem = new GuiElementBuilder(Items.BOOK).hideDefaultTooltip();
            disenchantBookItem.setName((Component.literal("")
                  .append(Component.literal("Disenchant onto a Book").withStyle(ChatFormatting.BLUE))));
            disenchantBookItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
                  .append(Component.literal("Click").withStyle(ChatFormatting.AQUA))
                  .append(Component.literal(" to move the top enchant to a book").withStyle(ChatFormatting.DARK_PURPLE)))));
            disenchantBookItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
                  .append(Component.literal("Right Click").withStyle(ChatFormatting.AQUA))
                  .append(Component.literal(" to move all enchants to a book").withStyle(ChatFormatting.DARK_PURPLE)))));
            setSlot(6,disenchantBookItem);
            
            GuiElementBuilder singularityItem = new GuiElementBuilder(Items.LECTERN).hideDefaultTooltip();
            singularityItem.setName((Component.literal("")
                  .append(Component.literal("Disenchant into a Singularity").withStyle(ChatFormatting.LIGHT_PURPLE))));
            singularityItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
                  .append(Component.literal("Click").withStyle(ChatFormatting.AQUA))
                  .append(Component.literal(" to move the top enchant to a Singularity").withStyle(ChatFormatting.DARK_PURPLE)))));
            singularityItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
                  .append(Component.literal("Right Click").withStyle(ChatFormatting.AQUA))
                  .append(Component.literal(" to move all enchants to a Singularity").withStyle(ChatFormatting.DARK_PURPLE)))));
            setSlot(8,singularityItem);
         }
      }else if(paperUpgrade){
         GuiElementBuilder exoticPaper = GuiElementBuilder.from(MinecraftUtils.removeLore(ArcanaRegistry.EXOTIC_ARCANE_PAPER.getDefaultInstance()));
         setSlot(7,exoticPaper);
      }else{
         GuiElementBuilder essenceItem;
         if(bookshelves < 0){
            essenceItem = new GuiElementBuilder(Items.ENDER_EYE).hideDefaultTooltip();
            essenceItem.setName((Component.literal("")
                  .append(Component.literal("Enlightened Enchantment").withStyle(ChatFormatting.DARK_AQUA))));
            essenceItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
                  .append(Component.literal("Choose").withStyle(ChatFormatting.AQUA))
                  .append(Component.literal(" your enchantments").withStyle(ChatFormatting.DARK_PURPLE)))));
            essenceItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
                  .append(Component.literal("Click").withStyle(ChatFormatting.AQUA))
                  .append(Component.literal(" to go to ").withStyle(ChatFormatting.DARK_PURPLE))
                  .append(Component.literal("Regular Enchantment").withStyle(ChatFormatting.BLUE)))));
         }else{
            if(bookshelves == 0){
               essenceItem = new GuiElementBuilder(Items.CHISELED_BOOKSHELF).hideDefaultTooltip().setCount(1);
            }else{
               essenceItem = new GuiElementBuilder(Items.BOOKSHELF).hideDefaultTooltip().setCount(bookshelves);
            }
            essenceItem.setName((Component.literal("")
                  .append(Component.literal("Regular Enchantment").withStyle(ChatFormatting.BLUE))));
            essenceItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
                  .append(Component.literal(""+bookshelves).withStyle(ChatFormatting.AQUA))
                  .append(Component.literal(" Bookshelves").withStyle(ChatFormatting.DARK_PURPLE)))));
            essenceItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
                  .append(Component.literal("Left Click").withStyle(ChatFormatting.AQUA))
                  .append(Component.literal(" to increase bookshelves").withStyle(ChatFormatting.DARK_PURPLE)))));
            essenceItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
                  .append(Component.literal("Right Click").withStyle(ChatFormatting.AQUA))
                  .append(Component.literal(" to decrease bookshelves").withStyle(ChatFormatting.DARK_PURPLE)))));
            essenceItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
                  .append(Component.literal("Shift Left Click").withStyle(ChatFormatting.AQUA))
                  .append(Component.literal(" to go to ").withStyle(ChatFormatting.DARK_PURPLE))
                  .append(Component.literal("Enlightened Enchantment").withStyle(ChatFormatting.DARK_AQUA)))));
         }
         setSlot(7,essenceItem);
      }
      
      if(getStack().isEnchantable() && !enchanted && bookshelves >= 0){
         for(int i = 19; i < 26; i++){
            GuiElementBuilder bgPane2 = GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.PAGE_BG, ArcanaColors.DARK_COLOR)).hideDefaultTooltip();
            bgPane2.setName(Component.empty()).hideTooltip();
            setSlot(i,bgPane2);
            setSlot(i+9,bgPane2);
            setSlot(i+18,bgPane2);
         }
         setSlot(11,GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_TOP_CONNECTOR,color)).hideDefaultTooltip().setName(name));
         setSlot(20,GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_VERTICAL,lapisLevel == 1 ? ArcanaColors.EQUAYUS_COLOR : color)).hideDefaultTooltip().setName(name));
         setSlot(29,GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_VERTICAL,lapisLevel == 2 ? ArcanaColors.EQUAYUS_COLOR : color)).hideDefaultTooltip().setName(name));
         setSlot(38,GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_VERTICAL,lapisLevel == 3 ? ArcanaColors.EQUAYUS_COLOR : color)).hideDefaultTooltip().setName(name));
         setSlot(47,GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_BOTTOM_CONNECTOR,ArcanaColors.ARCANA_COLOR)).hideTooltip());
         
         RandomSource random = RandomSource.create();
         int playerSeed = player.getEnchantmentSeed();
         random.setSeed(playerSeed);
         
         int[] enchPowers = new int[3];
         for(int i = 0; i < enchPowers.length; i++){
            enchPowers[i] = EnchantmentHelper.getEnchantmentCost(random, i, bookshelves, getStack());
         }
         
         for(int i = 0; i < enchPowers.length; i++){
            List<EnchantmentInstance> list;
            if(enchPowers[i] >= i + 1 && (list = this.generateEnchantments(getStack(), i, enchPowers[i],random,playerSeed)) != null && !list.isEmpty()){
               GuiElementBuilder lapisItem = new GuiElementBuilder(Items.LAPIS_LAZULI).hideDefaultTooltip().setCount(i+1);
               lapisItem.setName((Component.literal("")
                     .append(Component.literal("Slot "+(i+1)).withStyle(ChatFormatting.BLUE))));
               if(lapisLevel == i+1){
                  lapisItem.glow();
                  lapisItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
                        .append(Component.literal("Selected").withStyle(ChatFormatting.AQUA)))));
                  
                  setSelectedFromList(list);
                  xpCost = enchPowers[i];
               }else{
                  lapisItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
                        .append(Component.literal("Click").withStyle(ChatFormatting.AQUA))
                        .append(Component.literal(" to select").withStyle(ChatFormatting.DARK_PURPLE)))));
               }
               setSlot(19 + i*9,lapisItem);
               
               for(int j = 0; j < list.size() && j < 5; j++){
                  EnchantmentInstance entry = list.get(j);
                  
                  GuiElementBuilder enchantBook = new GuiElementBuilder(Items.ENCHANTED_BOOK).hideDefaultTooltip();
                  enchantBook.glow(false);
                  enchantBook.setName((Component.literal("")
                        .append(Enchantment.getFullname(entry.enchantment(),entry.level())).withStyle(ChatFormatting.AQUA)));
                  if(lapisLevel == i+1){
                     enchantBook.glow();
                     enchantBook.addLoreLine(TextUtils.removeItalics((Component.literal("")
                           .append(Component.literal("Selected").withStyle(ChatFormatting.AQUA)))));
                  }
                  setSlot(21 + i*9 + j,enchantBook);
               }
            }
         }
      }else if(paperUpgrade){
         for(int i = 0; i < 3; i++){
            for(int j = 0; j < 7; j++){
               setSlot((i*9+19)+j,GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.PAGE_BG, ArcanaColors.DARK_COLOR)).hideTooltip());
            }
         }
         xpCost = 10;
         essenceCost = getStack().getCount();
      }else{
         List<EnchantEntry> pageItems = getPage();
         int k = 0;
         for(int i = 0; i < 3; i++){
            for(int j = 0; j < 7; j++){
               if(k < pageItems.size()){
                  EnchantEntry pageItem = pageItems.get(k);
                  GuiElementBuilder enchantBook = new GuiElementBuilder(pageItem.selected ? Items.ENCHANTED_BOOK : Items.WRITTEN_BOOK).glow().hideDefaultTooltip();
                  enchantBook.setName((Component.literal("")
                        .append(Enchantment.getFullname(pageItem.enchantment,pageItem.level)).withStyle(ChatFormatting.AQUA)));
                  if(pageItem.selected){
                     enchantBook.addLoreLine(TextUtils.removeItalics(Component.literal("Selected").withStyle(ChatFormatting.YELLOW)));
                     enchantBook.addLoreLine(TextUtils.removeItalics(Component.literal("").withStyle(ChatFormatting.YELLOW)));
                  }
                  if(isCompatible(pageItem.enchantment, pageItem.level)){
                     enchantBook.addLoreLine(TextUtils.removeItalics((Component.literal("")
                           .append(Component.literal("Click").withStyle(ChatFormatting.BLUE))
                           .append(Component.literal(" to toggle selection").withStyle(ChatFormatting.DARK_PURPLE)))));
                  }else{
                     enchantBook.addLoreLine(TextUtils.removeItalics(Component.literal("Incompatible Enchant").withStyle(ChatFormatting.RED)));
                  }
                  
                  setSlot((i*9+19)+j,enchantBook);
               }else{
                  setSlot((i*9+19)+j,GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.PAGE_BG, getStack().isEmpty() ? ArcanaColors.DARK_COLOR : ArcanaColors.LAPIS_COLOR)).setName(Component.empty()).hideTooltip());
               }
               k++;
            }
         }
      }
      
      List<EnchantEntry> selected = getSelected();
      
      GuiElementBuilder enchantItem = new GuiElementBuilder(Items.ENCHANTING_TABLE).hideDefaultTooltip();
      if(paperUpgrade){
         enchantItem.setName((Component.literal("")
               .append(Component.literal("Enchant Exotic Arcane Paper").withStyle(ChatFormatting.LIGHT_PURPLE))));
         enchantItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
               .append(Component.literal("Click").withStyle(ChatFormatting.AQUA))
               .append(Component.literal(" to upgrade your Arcane Paper").withStyle(ChatFormatting.DARK_PURPLE)))));
      }else{
         enchantItem.setName((Component.literal("")
               .append(Component.literal("Enchant ").withStyle(ChatFormatting.LIGHT_PURPLE))
               .append(Component.translatable(getStack().getItem().getDescriptionId()).withStyle(ChatFormatting.LIGHT_PURPLE))));
         enchantItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
               .append(Component.literal("Click").withStyle(ChatFormatting.AQUA))
               .append(Component.literal(" to enchant the ").withStyle(ChatFormatting.DARK_PURPLE))
               .append(Component.translatable(getStack().getItem().getDescriptionId()).withStyle(ChatFormatting.DARK_PURPLE)))));
      }
      
      
      if(!selected.isEmpty()){
         enchantItem.addLoreLine(TextUtils.removeItalics(Component.empty()));
         enchantItem.addLoreLine(TextUtils.removeItalics(Component.literal("Adding: ").withStyle(ChatFormatting.DARK_PURPLE)));
         
         for(EnchantEntry entry : selected){
            enchantItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
                  .append(Enchantment.getFullname(entry.enchantment,entry.level)).withStyle(ChatFormatting.AQUA))));
         }
         
         enchantItem.addLoreLine(TextUtils.removeItalics(Component.empty()));
         enchantItem.addLoreLine(TextUtils.removeItalics(Component.literal("Costs: ").withStyle(ChatFormatting.LIGHT_PURPLE)));
         if(xpCost > 0){
            enchantItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
                  .append(Component.literal(xpCost+" Levels").withStyle(ChatFormatting.DARK_GREEN)))));
            if(getStack().isEnchantable() && !enchanted && bookshelves >= 0){
               enchantItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
                     .append(Component.literal(lapisLevel+" Lapis Lazuli").withStyle(ChatFormatting.BLUE)))));
            }else{
               enchantItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
                     .append(Component.literal(essenceCost+" Nebulous Essence").withStyle(ChatFormatting.DARK_PURPLE)))));
            }
         }
      }
      
      GuiElementBuilder xpItem = new GuiElementBuilder(Items.EXPERIENCE_BOTTLE).hideDefaultTooltip();
      String costStr = bookshelves >= 0 && !paperUpgrade ? "Lapis" : "Essence";
      xpItem.setName((Component.literal("")
            .append(Component.literal("XP & "+costStr+" Cost").withStyle(ChatFormatting.GREEN))));
      if(xpCost > 0 && (!selected.isEmpty() || paperUpgrade)){
         xpItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
               .append(Component.literal(xpCost+" Levels").withStyle(ChatFormatting.DARK_GREEN)))));
         if(getStack().isEnchantable() && !enchanted && bookshelves >= 0){
            xpItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
                  .append(Component.literal(lapisLevel+" Lapis Lazuli").withStyle(ChatFormatting.BLUE)))));
         }else{
            xpItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
                  .append(Component.literal(essenceCost+" Nebulous Essence").withStyle(ChatFormatting.DARK_PURPLE)))));
         }
      }else{
         xpItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
               .append(Component.literal("Select Enchantments").withStyle(ChatFormatting.DARK_GREEN)))));
      }
      
      setSlot(49,enchantItem);
      setSlot(1,xpItem);
   }
   
   private void removeXP(int levels){
       boolean expertise = ArcanaAugments.getAugmentFromMap(blockEntity.getAugments(),ArcanaAugments.ENCHANTING_EXPERTISE.id) > 0;
       if(player.isCreative()) return;
      
      int points = LevelUtils.vanillaLevelToTotalXp(levels);
      if(expertise){
         player.giveExperiencePoints(-points);
      }else{
         player.giveExperienceLevels(-levels);
      }
   }
   
   private boolean isCompatible(Holder<Enchantment> enchant, int level){
      ItemEnchantments comp = EnchantmentHelper.getEnchantmentsForCrafting(getStack());
      Object2IntOpenHashMap<Holder<Enchantment>> enchants = new Object2IntOpenHashMap<>();
      comp.entrySet().forEach(entry -> enchants.addTo(entry.getKey(),entry.getIntValue()));
      
      boolean upgrade = comp.keySet().contains(enchant) && comp.getLevel(enchant) < level;
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
      return EnchantmentHelper.isEnchantmentCompatible(enchants.keySet(),enchant);
   }
   
   private void applyEnchants(){
      setUpdating();
      ItemEnchantments.Mutable enchantBuilder = new ItemEnchantments.Mutable(EnchantmentHelper.getEnchantmentsForCrafting(getStack()));
      for(EnchantEntry entry : getSelected()){
         enchantBuilder.removeIf(e -> e == entry.enchantment());
         enchantBuilder.upgrade(entry.enchantment(),entry.level());
         
         if(entry.level == 1 && entry.enchantment().value().getMaxLevel() > 1){
            ArcanaAchievements.grant(player,ArcanaAchievements.ENCHANTING_OVERKILL.id);
         }
      }
      if(getStack().is(Items.BOOK)){
         setItem(getStack().transmuteCopy(Items.ENCHANTED_BOOK));
      }
      
      EnchantmentHelper.setEnchantments(getStack(),enchantBuilder.toImmutable());
      setItem(getStack());
      finishUpdate();
   }
   
   private void disenchantItem(){
      setUpdating();
      
      EnchantmentHelper.updateEnchantments(getStack(), components -> components.removeIf(enchantment -> true));
      if(getStack().is(Items.ENCHANTED_BOOK)){
         setItem(getStack().transmuteCopy(Items.BOOK));
      }
      
      if(ArcanaItemUtils.isArcane(getStack())){
         ArcanaItemUtils.identifyItem(getStack()).buildItemLore(getStack(), BorisLib.SERVER);
      }
      setItem(getStack());
      finishUpdate();
   }
   
   private Tuple<Holder<Enchantment>,Integer> removeTopEnchant(){
      ItemEnchantments comp = EnchantmentHelper.getEnchantmentsForCrafting(getStack());
      Object2IntOpenHashMap<Holder<Enchantment>> enchants = new Object2IntOpenHashMap<>();
      comp.entrySet().forEach(entry -> enchants.addTo(entry.getKey(),entry.getIntValue()));
      
      if(enchants.size() == 1){
         disenchantItem();
         for(Holder<Enchantment> entry : enchants.keySet()){
            return new Tuple<>(entry, enchants.getInt(entry));
         }
      }
      setUpdating();
      
      HolderSet<Enchantment> registryEntryList = null;
      Optional<HolderSet.Named<Enchantment>> optional = player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(EnchantmentTags.TOOLTIP_ORDER);
      if(optional.isPresent()){
         registryEntryList = optional.get();
      }
      
      Holder<Enchantment> registryEntry = null;
      int value = 0;
      int index = Integer.MAX_VALUE;
      
      ObjectIterator<Object2IntMap.Entry<Holder<Enchantment>>> iter = enchants.object2IntEntrySet().fastIterator();
      while(iter.hasNext()){
         Object2IntMap.Entry<Holder<Enchantment>> entry = iter.next();
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
         Holder<Enchantment> finalRegistryEntry = registryEntry;
         int finalValue = value;
         enchants.object2IntEntrySet().removeIf(e -> e.getKey().value() == finalRegistryEntry.value() && finalValue == e.getIntValue());
      }
      
      ItemEnchantments.Mutable enchantBuilder = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
      enchants.forEach(enchantBuilder::upgrade);
      EnchantmentHelper.setEnchantments(getStack(),enchantBuilder.toImmutable());
      
      setItem(getStack());
      finishUpdate();
      return new Tuple<>(registryEntry,value);
   }
   
   public void setItem(ItemStack stack){
      lapisLevel = -1;
      enchants = getEnchantsForItem(stack);
      page = 1;
      maxPages = (int) Math.ceil(enchants.size() / 21.0);
      inventory.setItem(0,stack.copy());
      calculateXPCost();
   }
   
   private void setSelectedFromList(List<EnchantmentInstance> entries){
      enchants = new ArrayList<>();
      HolderLookup.RegistryLookup<Enchantment> impl = player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
      entries.forEach(e -> enchants.add(new EnchantEntry(impl.getOrThrow(e.enchantment().unwrapKey().get()), e.level(),true)));
   }
   
   public void calculateXPCost(){
      boolean paperUpgrade = getStack().is(ArcanaRegistry.EMPOWERED_ARCANE_PAPER);
      int cost = 0;
      int eCost = 0;
      if(paperUpgrade){
         xpCost = 10;
         essenceCost = getStack().getCount();
         return;
      }
      
      ItemEnchantments comp = EnchantmentHelper.getEnchantmentsForCrafting(getStack());
      Object2IntOpenHashMap<Holder<Enchantment>> curEnchants = new Object2IntOpenHashMap<>();
      comp.entrySet().forEach(entry -> curEnchants.addTo(entry.getKey(),entry.getIntValue()));
      
      for(Holder<Enchantment> entry : curEnchants.keySet()){
         int rarityMod = entry.value().getAnvilCost();
         if(entry.is(EnchantmentTags.TREASURE)){
            rarityMod *= 2;
         }
         cost += curEnchants.getInt(entry) * rarityMod;
      }
      cost /= 2; // Half cost for existing enchants
      
      for(EnchantEntry entry : getSelected()){
         int rarityMod = entry.enchantment().value().getAnvilCost();
         if(entry.enchantment().is(EnchantmentTags.TREASURE)){
            rarityMod *= 2;
         }
         cost += entry.level()*rarityMod;
         eCost += (int) Math.ceil(ArcanaUtils.calcEssenceValue(entry.enchantment(),entry.level())*1.5);
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
      if(stack.isEmpty()) return new ArrayList<>();
      ItemEnchantments comp = EnchantmentHelper.getEnchantmentsForCrafting(stack);
      Object2IntOpenHashMap<Holder<Enchantment>> curEnchants = new Object2IntOpenHashMap<>();
      comp.entrySet().forEach(entry -> curEnchants.addTo(entry.getKey(),entry.getIntValue()));
      
      List<EnchantEntry> possibleAdditions = new ArrayList<>();
      
      for(Holder<Enchantment> entry : player.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).asHolderIdMap()){
         Enchantment enchantment = entry.value();
         
         if(!EnchantmentHelper.isEnchantmentCompatible(curEnchants.keySet(),entry) && !curEnchants.containsKey(entry)) continue; // Remove incompatible enchants
         if(!stack.is(Items.BOOK) && !enchantment.isSupportedItem(stack)) continue; // Remove enchants for wrong items
         
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
   
   private List<EnchantmentInstance> generateEnchantments(ItemStack stack, int slot, int level, RandomSource random, int playerSeed){
      random.setSeed(playerSeed + slot);
      Optional<HolderSet.Named<Enchantment>> optional = player.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(EnchantmentTags.IN_ENCHANTING_TABLE);
      if(optional.isEmpty()){
         return List.of();
      }
      List<EnchantmentInstance> list = EnchantmentHelper.selectEnchantment(random, stack, level, optional.get().stream());
      if(stack.is(Items.BOOK) && list.size() > 1){
         list.remove(random.nextInt(list.size()));
      }
      return list;
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
      super.onClose();
   }
   
   @Override
   public void close(){
      super.close();
   }
   
   @Override
   public void containerChanged(Container inv){
      if(!updating){
         setUpdating();
         ItemStack mainStack = inv.getItem(0);
         buildGui();
         setItem(mainStack);
         //Update gui
         finishUpdate();
      }
   }
   
   private ItemStack getStack(){
      return this.inventory.getItem(0);
   }
   
   public void finishUpdate(){
      updating = false;
   }
   public void setUpdating(){
      updating = true;
   }
   
   private record EnchantEntry(Holder<Enchantment> enchantment, int level, boolean selected){}
}

