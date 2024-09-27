package net.borisshoes.arcananovum.items;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.cardinalcomponents.IArcanaProfileComponent;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.items.arrows.RunicArrow;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.arcananovum.utils.DataFixer;
import net.borisshoes.arcananovum.utils.MiscUtils;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.*;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;
import net.minecraft.util.collection.DefaultedList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;
import static net.minecraft.item.RangedWeaponItem.BOW_PROJECTILES;

public abstract class QuiverItem extends ArcanaItem {
   public static final String ARROWS_TAG = "arrows";
   public static final String QUIVER_SLOT_TAG = "QuiverSlot";
   public static final String QUIVER_ID_TAG = "QuiverId";
   public static final String QUIVER_CD_TAG = "QuiverCD";
   public static final String RUNIC_INV_ID_TAG = "runicInvId";
   public static final String ARROW_INV_ID_TAG = "arrowInvId";
   public static final String RUNIC_INV_SLOT_TAG = "runicInvSlot";
   public static final String ARROW_INV_SLOT_TAG = "arrowInvSlot";
   
   
   public static final int size = 9;
   protected Formatting color;
   
   @Override
   public ItemStack updateItem(ItemStack stack, MinecraftServer server){
      if(getIntProperty(stack,VERSION_TAG) <= 12){ // Migrate from ARROWS_TAG to ContainerComponent
         NbtList arrowsList = getListProperty(stack,ARROWS_TAG,NbtElement.COMPOUND_TYPE).copy();
         stack.set(DataComponentTypes.CONTAINER, DataFixer.nbtListToComponent(arrowsList,server));
         removeProperty(stack,ARROWS_TAG);
      }
      ItemStack newStack = super.updateItem(stack,server);
      return buildItemLore(newStack,server);
   }
   
   public Formatting getColor(){
      return color;
   }
   
   protected abstract int getRefillMod(ItemStack item);
   
   protected abstract double getEfficiencyMod(ItemStack item);
   
   protected void refillArrow(ServerPlayerEntity player, ItemStack item){
      ContainerComponent arrows = item.getOrDefault(DataComponentTypes.CONTAINER,ContainerComponent.DEFAULT);
      ArrayList<ItemStack> eligible = new ArrayList<>();
      for(ItemStack stack : arrows.iterateNonEmpty()){
         if(stack.getCount() < stack.getMaxCount()){
            eligible.add(stack);
         }
      }
      
      if(eligible.isEmpty()) return;
      eligible.get((int)(Math.random()*eligible.size())).increment(1);
      
      PLAYER_DATA.get(player).addXP(50); // Add xp
      if(this instanceof OverflowingQuiver){
         ArcanaAchievements.progress(player,ArcanaAchievements.SPARE_STOCK.id,1);
      }else if(this instanceof RunicQuiver){
         ArcanaAchievements.progress(player,ArcanaAchievements.UNLIMITED_STOCK.id,1);
      }
   }
   
   public boolean shootArrow(ItemStack item, int slot, ServerPlayerEntity player, ItemStack bow){
      ContainerComponent arrowComp = item.getOrDefault(DataComponentTypes.CONTAINER,ContainerComponent.DEFAULT);
      boolean runic = ArcanaItemUtils.identifyItem(bow) instanceof RunicBow || (bow.isOf(ArcanaRegistry.ALCHEMICAL_ARBALEST.getItem()) && ArcanaAugments.getAugmentOnItem(bow,ArcanaAugments.RUNIC_ARBALEST.id) >= 1);
      
      DefaultedList<ItemStack> arrows = DefaultedList.ofSize(9, ItemStack.EMPTY);
      arrowComp.copyTo(arrows);
      
      ItemStack stack = arrows.get(slot);
      if(stack.isEmpty()) return false;
      int count = stack.getCount();
      
      if(EnchantmentHelper.getLevel(MiscUtils.getEnchantment(Enchantments.INFINITY), bow) > 0 && item.isOf(Items.ARROW)) return true;
      if(Math.random() >= getEfficiencyMod(item)) count--;
      
      if(count == 0){
         arrows.set(slot,ItemStack.EMPTY);
         switchArrowOption(player,runic,true);
      }else{
         stack.setCount(count);
      }
      
      PlayerInventory inv = player.getInventory();
      for(int j = 0; j < inv.size(); j++){
         player.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(-2, 0, j, inv.getStack(j)));
      }
      player.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(-2, 0, PlayerInventory.OFF_HAND_SLOT, inv.getStack(PlayerInventory.OFF_HAND_SLOT)));
      
      item.set(DataComponentTypes.CONTAINER, ContainerComponent.fromStacks(arrows));
      return true;
   }
   
   public static Pair<String,Integer> getArrowOption(ServerPlayerEntity player, boolean runic, boolean display){
      IArcanaProfileComponent profile = PLAYER_DATA.get(player);
      String invId = profile.getMiscData(runic ? RUNIC_INV_ID_TAG : ARROW_INV_ID_TAG).asString();
      int invSlot = ((NbtInt)profile.getMiscData(runic ? RUNIC_INV_SLOT_TAG : ARROW_INV_SLOT_TAG)).intValue();
   
      List<Pair<String,Integer>> options = getArrowOptions(player,runic);
      if(options.isEmpty()){
         return null;
      }
      for(Pair<String, Integer> option : options){
         if(invId.equals(option.getLeft()) && invSlot == option.getRight()){
            return option;
         }
      }
      return switchArrowOption(player,runic,display);
   }
   
   public static Pair<String,Integer> switchArrowOption(ServerPlayerEntity player, boolean runic, boolean display){
      IArcanaProfileComponent profile = PLAYER_DATA.get(player);
      String invId = profile.getMiscData(runic ? RUNIC_INV_ID_TAG : ARROW_INV_ID_TAG).asString();
      int invSlot = ((NbtInt)profile.getMiscData(runic ? RUNIC_INV_SLOT_TAG : ARROW_INV_SLOT_TAG)).intValue();
   
      List<Pair<String,Integer>> options = getArrowOptions(player,runic);
      if(options.isEmpty()){
         return null;
      }
      
      int ind = 0;
      boolean found = false;
      for(Pair<String, Integer> option : options){
         if(invId.equals(option.getLeft()) && invSlot == option.getRight()){
            found = true;
            break;
         }
         ind++;
      }
      Pair<String,Integer> option;
      int add = player.isSneaking() ? -1 : 1;
      if(found){
         ind = (ind+add) % options.size();
         if(ind < 0) ind = options.size() + ind;
         option = options.get(ind);
      }else{
         option = options.get(0);
      }
      profile.addMiscData(runic ? RUNIC_INV_ID_TAG : ARROW_INV_ID_TAG,NbtString.of(option.getLeft()));
      profile.addMiscData(runic ? RUNIC_INV_SLOT_TAG : ARROW_INV_SLOT_TAG,NbtInt.of(option.getRight()));
      getArrowStack(player,runic,display);
      return option;
   }
   
   public static ItemStack getArrowStack(ServerPlayerEntity player, boolean runic, boolean display){
      Pair<String,Integer> option = getArrowOption(player,runic,display);
      Predicate<ItemStack> PROJECTILES = runic ? RunicBow.RunicBowItem.RUNIC_BOW_PROJECTILES : BOW_PROJECTILES;
      if(option == null){ // No arrows accessible
         if(display) player.sendMessage(Text.literal("No Arrows Available").formatted(Formatting.RED, Formatting.ITALIC), true);
         return null;
      }else{ // getArrowOption always returns a verified slot, but just in case...
         String invId = option.getLeft();
         int invSlot = option.getRight();
         
         PlayerInventory inv = player.getInventory();
         if(invId.equals("inventory")){
            ItemStack stack = inv.getStack(invSlot);
            if (PROJECTILES.test(stack)) {
               if(display){
                  Text name = stack.getName();
                  ArcanaItem arcanaArrow = ArcanaItemUtils.identifyItem(stack);
                  if(arcanaArrow instanceof RunicArrow runicArrow){
                     name = runicArrow.getArrowName(stack);
                  }
                  player.sendMessage(Text.literal("")
                              .append(Text.literal("Switched Arrows To: ").formatted(Formatting.GRAY,Formatting.ITALIC))
                              .append(name.copy().formatted(Formatting.ITALIC))
                              .append(Text.literal(" ("+stack.getCount()+")").formatted(Formatting.GRAY,Formatting.ITALIC))
                        ,true);
               }
               return stack;
            }else{
               ArcanaNovum.log(2,"Quiver Error Occurred, this shouldn't be possible! Player: "+player.getDisplayName().getString());
               return null;
            }
         }
         
         
         for(int i=0; i<inv.size();i++){ // Scan for quiver of correct id
            ItemStack item = inv.getStack(i);
            if(item.isEmpty()){
               continue;
            }
   
            ArcanaItem arcanaItem = ArcanaItemUtils.identifyItem(item);
            if(arcanaItem == null) continue;
            if(getUUID(item).equals(invId)){
               if(arcanaItem instanceof RunicQuiver quiver){
                  ItemStack stack = quiver.getArrow(item,invSlot);
                  if(display){
                     Text name = stack.getName();
                     ArcanaItem arcanaArrow = ArcanaItemUtils.identifyItem(stack);
                     if(arcanaArrow instanceof RunicArrow runicArrow){
                        name = runicArrow.getArrowName(stack);
                     }
                     player.sendMessage(Text.literal("")
                                 .append(Text.literal("Switched Arrows To: ").formatted(Formatting.LIGHT_PURPLE,Formatting.ITALIC))
                                 .append(name.copy().formatted(Formatting.ITALIC))
                                 .append(Text.literal(" ("+stack.getCount()+")").formatted(Formatting.LIGHT_PURPLE,Formatting.ITALIC))
                           ,true);
                  }
                  return stack;
               }else if(arcanaItem instanceof OverflowingQuiver quiver){
                  ItemStack stack = quiver.getArrow(item,invSlot);
                  if(display){
                     Text name = stack.getName();
                     ArcanaItem arcanaArrow = ArcanaItemUtils.identifyItem(stack);
                     if(arcanaArrow instanceof RunicArrow runicArrow){
                        name = runicArrow.getArrowName(stack);
                     }
                     player.sendMessage(Text.literal("")
                                 .append(Text.literal("Switched Arrows To: ").formatted(Formatting.DARK_AQUA,Formatting.ITALIC))
                                 .append(name.copy().formatted(Formatting.ITALIC))
                                 .append(Text.literal(" ("+stack.getCount()+")").formatted(Formatting.DARK_AQUA,Formatting.ITALIC))
                           ,true);
                  }
                  return stack;
               }else{
                  ArcanaNovum.log(2,"Quiver Error Occurred, this shouldn't be possible! Player: "+player.getDisplayName().getString());
                  return null;
               }
            }
         }
         
      }
      ArcanaNovum.log(2,"Quiver Error Occurred, this shouldn't be possible! Player: "+player.getDisplayName().getString());
      return null;
   }
   
   public static List<Pair<String,Integer>> getArrowOptions(ServerPlayerEntity player, boolean runic){
      List<Pair<String,Integer>> options = new ArrayList<>();
      Predicate<ItemStack> PROJECTILES = runic ? RunicBow.RunicBowItem.RUNIC_BOW_PROJECTILES : BOW_PROJECTILES;
      // Makes a list of available arrows with the UUID of the quiver containing them or "inventory" and the slot of the inventory they are in
      
      PlayerInventory inv = player.getInventory();
      for(int i=0; i<inv.size();i++){
         ItemStack item = inv.getStack(i);
         if(item.isEmpty()){
            continue;
         }
      
         ArcanaItem arcanaItem = ArcanaItemUtils.identifyItem(item);
         if(arcanaItem instanceof RunicQuiver quiver){
            ContainerComponent arrowComp = item.getOrDefault(DataComponentTypes.CONTAINER,ContainerComponent.DEFAULT);
            DefaultedList<ItemStack> arrows = DefaultedList.ofSize(9, ItemStack.EMPTY);
            arrowComp.copyTo(arrows);
            for(int j = 0; j < arrows.size(); j++){
               ItemStack arrow = arrows.get(j);
               if(arrow.isEmpty()) continue;
               if(ArcanaItemUtils.isRunicArrow(arrow) && !runic) continue;
               options.add(new Pair<>(getUUID(item),j));
            }
            
         }else if(arcanaItem instanceof OverflowingQuiver quiver){
            ContainerComponent arrowComp = item.getOrDefault(DataComponentTypes.CONTAINER,ContainerComponent.DEFAULT);
            DefaultedList<ItemStack> arrows = DefaultedList.ofSize(9, ItemStack.EMPTY);
            arrowComp.copyTo(arrows);
            for(int j = 0; j < arrows.size(); j++){
               ItemStack arrow = arrows.get(j);
               if(arrow.isEmpty()) continue;
               options.add(new Pair<>(getUUID(item),j));
            }
         }
   
         if (PROJECTILES.test(item)) {
            if(!runic && arcanaItem instanceof RunicArrow){
               continue;
            }
            options.add(new Pair<>("inventory",i));
         }
      }
      
      return options;
   }
   
   public ItemStack getArrow(ItemStack quiver, int slot){
      ContainerComponent arrowComp = quiver.getOrDefault(DataComponentTypes.CONTAINER,ContainerComponent.DEFAULT);
      DefaultedList<ItemStack> arrows = DefaultedList.ofSize(9, ItemStack.EMPTY);
      arrowComp.copyTo(arrows);
      
      ItemStack arrow = arrows.get(slot);
      if(arrow.isEmpty()) return null;
      putProperty(arrow,QUIVER_SLOT_TAG,slot);
      putProperty(arrow,QUIVER_ID_TAG,getUUID(quiver));
      return arrow;
   }
   
   public static void decreaseQuiver(ItemStack bow, ItemStack arrow, PlayerEntity playerEntity){
      if(!(playerEntity instanceof ServerPlayerEntity player)) return;
      if(hasProperty(arrow,QUIVER_SLOT_TAG) && hasProperty(arrow,QUIVER_ID_TAG)){
         String quiverId = getStringProperty(arrow,QUIVER_ID_TAG);
         int slot = getIntProperty(arrow,QUIVER_SLOT_TAG);
         boolean isInvArrow = quiverId.equals("inventory");
         
         PlayerInventory inv = playerEntity.getInventory();
         if(isInvArrow){
            ItemStack arrowStack = inv.getStack(slot);
            arrowStack.decrement(1);
            if (arrowStack.isEmpty()) {
               playerEntity.getInventory().removeOne(arrowStack);
            }
         }else{
            for(int invSlot = 0; invSlot<inv.size(); invSlot++){
               ItemStack item = inv.getStack(invSlot);
               if(item.isEmpty()){
                  continue;
               }
               
               ArcanaItem arcanaItem = ArcanaItemUtils.identifyItem(item);
               if(arcanaItem instanceof QuiverItem quiver){
                  if(getUUID(item).equals(quiverId)){
                     quiver.shootArrow(item,slot,player,bow);
                     return;
                  }
               }
            }
         }
      }
   }
}
