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
import net.borisshoes.borislib.utils.MinecraftUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static net.minecraft.world.item.ProjectileWeaponItem.ARROW_ONLY;

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
   protected ChatFormatting color;
   
   @Override
   public ItemStack updateItem(ItemStack stack, MinecraftServer server){
      if(getIntProperty(stack,VERSION_TAG) <= 12){ // Migrate from ARROWS_TAG to ContainerComponent
         ListTag arrowsList = getListProperty(stack,ARROWS_TAG).copy();
         stack.set(DataComponents.CONTAINER, DataFixer.nbtListToComponent(arrowsList,server));
         removeProperty(stack,ARROWS_TAG);
      }
      ItemStack newStack = super.updateItem(stack,server);
      return buildItemLore(newStack,server);
   }
   
   public ChatFormatting getColor(){
      return color;
   }
   
   protected abstract int getRefillMod(ItemStack item);
   
   protected abstract double getEfficiencyMod(ItemStack item);
   
   protected void refillArrow(ServerPlayer player, ItemStack item){
      ItemContainerContents arrows = item.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
      ArrayList<ItemStack> eligible = new ArrayList<>();
      for(ItemStack stack : arrows.nonEmptyItems()){
         if(stack.getCount() < stack.getMaxStackSize() && !EnchantmentHelper.hasAnyEnchantments(stack)){
            eligible.add(stack);
         }
      }
      
      if(eligible.isEmpty()) return;
      eligible.get((int)(Math.random()*eligible.size())).grow(1);
      
      ArcanaNovum.data(player).addXP(ArcanaNovum.CONFIG.getInt(ArcanaRegistry.XP_QUIVER_REFILL)); // Add xp
      if(this instanceof OverflowingQuiver){
         ArcanaAchievements.progress(player,ArcanaAchievements.SPARE_STOCK.id,1);
      }else if(this instanceof RunicQuiver){
         ArcanaAchievements.progress(player,ArcanaAchievements.UNLIMITED_STOCK.id,1);
      }
      buildItemLore(item,player.level().getServer());
   }
   
   public boolean shootArrow(ItemStack item, int slot, ServerPlayer player, ItemStack bow){
      ItemContainerContents arrowComp = item.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
      boolean runic = ArcanaItemUtils.identifyItem(bow) instanceof RunicBow || (bow.is(ArcanaRegistry.ALCHEMICAL_ARBALEST.getItem()) && ArcanaAugments.getAugmentOnItem(bow,ArcanaAugments.RUNIC_ARBALEST.id) >= 1);
      
      NonNullList<ItemStack> arrows = NonNullList.withSize(9, ItemStack.EMPTY);
      arrowComp.copyInto(arrows);
      
      ItemStack stack = arrows.get(slot);
      if(stack.isEmpty()) return false;
      int count = stack.getCount();
      
      if(EnchantmentHelper.getItemEnchantmentLevel(MinecraftUtils.getEnchantment(Enchantments.INFINITY), bow) > 0 && item.is(Items.ARROW)) return true;
      if(Math.random() >= getEfficiencyMod(item)) count--;
      
      if(count == 0){
         arrows.set(slot, ItemStack.EMPTY);
         switchArrowOption(player,runic,true);
      }else{
         stack.setCount(count);
      }
      
      Inventory inv = player.getInventory();
//      for(int j = 0; j < inv.size(); j++){
//         player.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(player.playerScreenHandler.syncId, player.playerScreenHandler.nextRevision(), j, inv.getStack(j)));
//      }
      player.connection.send(new ClientboundContainerSetSlotPacket(player.inventoryMenu.containerId, player.inventoryMenu.incrementStateId(), 45, inv.getItem(Inventory.SLOT_OFFHAND)));
      
      item.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(arrows));
      buildItemLore(item,player.level().getServer());
      return true;
   }
   
   public static Tuple<String,Integer> getArrowOption(ServerPlayer player, boolean runic, boolean display){
      IArcanaProfileComponent profile = ArcanaNovum.data(player);
      String invId = profile.getMiscData(runic ? RUNIC_INV_ID_TAG : ARROW_INV_ID_TAG).asString().orElse("");
      int invSlot = ((IntTag)profile.getMiscData(runic ? RUNIC_INV_SLOT_TAG : ARROW_INV_SLOT_TAG)).intValue();
   
      List<Tuple<String,Integer>> options = getArrowOptions(player,runic);
      if(options.isEmpty()){
         return null;
      }
      for(Tuple<String, Integer> option : options){
         if(invId.equals(option.getA()) && invSlot == option.getB()){
            return option;
         }
      }
      return switchArrowOption(player,runic,display);
   }
   
   public static Tuple<String,Integer> switchArrowOption(ServerPlayer player, boolean runic, boolean display){
      IArcanaProfileComponent profile = ArcanaNovum.data(player);
      String invId = profile.getMiscData(runic ? RUNIC_INV_ID_TAG : ARROW_INV_ID_TAG).asString().orElse("");
      int invSlot = ((IntTag)profile.getMiscData(runic ? RUNIC_INV_SLOT_TAG : ARROW_INV_SLOT_TAG)).intValue();
   
      List<Tuple<String,Integer>> options = getArrowOptions(player,runic);
      if(options.isEmpty()){
         return null;
      }
      
      int ind = 0;
      boolean found = false;
      for(Tuple<String, Integer> option : options){
         if(invId.equals(option.getA()) && invSlot == option.getB()){
            found = true;
            break;
         }
         ind++;
      }
      Tuple<String,Integer> option;
      int add = player.isShiftKeyDown() ? -1 : 1;
      if(found){
         ind = (ind+add) % options.size();
         if(ind < 0) ind = options.size() + ind;
         option = options.get(ind);
      }else{
         option = options.get(0);
      }
      profile.addMiscData(runic ? RUNIC_INV_ID_TAG : ARROW_INV_ID_TAG, StringTag.valueOf(option.getA()));
      profile.addMiscData(runic ? RUNIC_INV_SLOT_TAG : ARROW_INV_SLOT_TAG, IntTag.valueOf(option.getB()));
      getArrowStack(player,runic,display);
      return option;
   }
   
   public static ItemStack getArrowStack(ServerPlayer player, boolean runic, boolean display){
      Tuple<String,Integer> option = getArrowOption(player,runic,display);
      Predicate<ItemStack> PROJECTILES = runic ? RunicBow.RunicBowItem.RUNIC_BOW_PROJECTILES : ARROW_ONLY;
      if(option == null){ // No arrows accessible
         if(display) player.displayClientMessage(Component.literal("No Arrows Available").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC), true);
         return null;
      }else{ // getArrowOption always returns a verified slot, but just in case...
         String invId = option.getA();
         int invSlot = option.getB();
         
         Inventory inv = player.getInventory();
         if(invId.equals("inventory")){
            ItemStack stack = inv.getItem(invSlot);
            if(PROJECTILES.test(stack)){
               if(display){
                  Component name = stack.getHoverName();
                  ArcanaItem arcanaArrow = ArcanaItemUtils.identifyItem(stack);
                  if(arcanaArrow instanceof RunicArrow runicArrow){
                     name = runicArrow.getArrowName(stack);
                  }
                  player.displayClientMessage(Component.literal("")
                              .append(Component.literal("Switched Arrows To: ").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC))
                              .append(name.copy().withStyle(ChatFormatting.ITALIC))
                              .append(Component.literal(" ("+stack.getCount()+")").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC))
                        ,true);
               }
               return stack;
            }else{
               ArcanaNovum.log(2,"Quiver Error Occurred, this shouldn't be possible! Player: "+player.getDisplayName().getString());
               return null;
            }
         }
         
         
         for(int i = 0; i<inv.getContainerSize(); i++){ // Scan for quiver of correct id
            ItemStack item = inv.getItem(i);
            if(item.isEmpty()){
               continue;
            }
   
            ArcanaItem arcanaItem = ArcanaItemUtils.identifyItem(item);
            if(arcanaItem == null) continue;
            if(getUUID(item).equals(invId)){
               if(arcanaItem instanceof RunicQuiver quiver){
                  ItemStack stack = quiver.getArrow(item,invSlot);
                  if(display){
                     Component name = stack.getHoverName();
                     ArcanaItem arcanaArrow = ArcanaItemUtils.identifyItem(stack);
                     if(arcanaArrow instanceof RunicArrow runicArrow){
                        name = runicArrow.getArrowName(stack);
                     }
                     player.displayClientMessage(Component.literal("")
                                 .append(Component.literal("Switched Arrows To: ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC))
                                 .append(name.copy().withStyle(ChatFormatting.ITALIC))
                                 .append(Component.literal(" ("+stack.getCount()+")").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC))
                           ,true);
                  }
                  return stack;
               }else if(arcanaItem instanceof OverflowingQuiver quiver){
                  ItemStack stack = quiver.getArrow(item,invSlot);
                  if(display){
                     Component name = stack.getHoverName();
                     ArcanaItem arcanaArrow = ArcanaItemUtils.identifyItem(stack);
                     if(arcanaArrow instanceof RunicArrow runicArrow){
                        name = runicArrow.getArrowName(stack);
                     }
                     player.displayClientMessage(Component.literal("")
                                 .append(Component.literal("Switched Arrows To: ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.ITALIC))
                                 .append(name.copy().withStyle(ChatFormatting.ITALIC))
                                 .append(Component.literal(" ("+stack.getCount()+")").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.ITALIC))
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
   
   public static List<Tuple<String,Integer>> getArrowOptions(ServerPlayer player, boolean runic){
      List<Tuple<String,Integer>> options = new ArrayList<>();
      Predicate<ItemStack> PROJECTILES = runic ? RunicBow.RunicBowItem.RUNIC_BOW_PROJECTILES : ARROW_ONLY;
      // Makes a list of available arrows with the UUID of the quiver containing them or "inventory" and the slot of the inventory they are in
      
      Inventory inv = player.getInventory();
      for(int i = 0; i<inv.getContainerSize(); i++){
         ItemStack item = inv.getItem(i);
         if(item.isEmpty()){
            continue;
         }
      
         ArcanaItem arcanaItem = ArcanaItemUtils.identifyItem(item);
         if(arcanaItem instanceof RunicQuiver quiver){
            ItemContainerContents arrowComp = item.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
            NonNullList<ItemStack> arrows = NonNullList.withSize(9, ItemStack.EMPTY);
            arrowComp.copyInto(arrows);
            for(int j = 0; j < arrows.size(); j++){
               ItemStack arrow = arrows.get(j);
               if(arrow.isEmpty()) continue;
               if(ArcanaItemUtils.isRunicArrow(arrow) && !runic) continue;
               options.add(new Tuple<>(getUUID(item),j));
            }
            
         }else if(arcanaItem instanceof OverflowingQuiver quiver){
            ItemContainerContents arrowComp = item.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
            NonNullList<ItemStack> arrows = NonNullList.withSize(9, ItemStack.EMPTY);
            arrowComp.copyInto(arrows);
            for(int j = 0; j < arrows.size(); j++){
               ItemStack arrow = arrows.get(j);
               if(arrow.isEmpty()) continue;
               options.add(new Tuple<>(getUUID(item),j));
            }
         }
   
         if(PROJECTILES.test(item)){
            if(!runic && arcanaItem instanceof RunicArrow){
               continue;
            }
            options.add(new Tuple<>("inventory",i));
         }
      }
      
      return options;
   }
   
   public ItemStack getArrow(ItemStack quiver, int slot){
      ItemContainerContents arrowComp = quiver.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
      NonNullList<ItemStack> arrows = NonNullList.withSize(9, ItemStack.EMPTY);
      arrowComp.copyInto(arrows);
      
      ItemStack arrow = arrows.get(slot);
      if(arrow.isEmpty()) return null;
      putProperty(arrow,QUIVER_SLOT_TAG,slot);
      putProperty(arrow,QUIVER_ID_TAG,getUUID(quiver));
      return arrow;
   }
   
   public static void decreaseQuiver(ItemStack bow, ItemStack arrow, Player playerEntity){
      if(!(playerEntity instanceof ServerPlayer player)) return;
      if(hasProperty(arrow,QUIVER_SLOT_TAG) && hasProperty(arrow,QUIVER_ID_TAG)){
         String quiverId = getStringProperty(arrow,QUIVER_ID_TAG);
         int slot = getIntProperty(arrow,QUIVER_SLOT_TAG);
         boolean isInvArrow = quiverId.equals("inventory");
         
         Inventory inv = playerEntity.getInventory();
         if(isInvArrow){
            ItemStack arrowStack = inv.getItem(slot);
            arrowStack.shrink(1);
            if(arrowStack.isEmpty()){
               playerEntity.getInventory().removeItem(arrowStack);
            }
         }else{
            for(int invSlot = 0; invSlot<inv.getContainerSize(); invSlot++){
               ItemStack item = inv.getItem(invSlot);
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
