package net.borisshoes.arcananovum.items.core;

import net.borisshoes.arcananovum.Arcananovum;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.cardinalcomponents.IArcanaProfileComponent;
import net.borisshoes.arcananovum.gui.quivers.QuiverGui;
import net.borisshoes.arcananovum.items.OverflowingQuiver;
import net.borisshoes.arcananovum.items.RunicBow;
import net.borisshoes.arcananovum.items.RunicQuiver;
import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.*;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;
import static net.minecraft.item.RangedWeaponItem.BOW_PROJECTILES;

public abstract class QuiverItem extends MagicItem{
   public static final int size = 9;
   protected Formatting color;
   
   @Override
   public ItemStack updateItem(ItemStack stack, MinecraftServer server){
      NbtCompound itemNbt = stack.getNbt();
      NbtCompound magicTag = itemNbt.getCompound("arcananovum");
      NbtList arrowsList = magicTag.getList("arrows", NbtElement.COMPOUND_TYPE).copy();
      NbtCompound newTag = super.updateItem(stack,server).getNbt();
      newTag.getCompound("arcananovum").put("arrows",arrowsList);
      stack.setNbt(newTag);
      return stack;
   }
   
   public Formatting getColor(){
      return color;
   }
   
   protected abstract int getRefillMod(ItemStack item);
   
   protected abstract double getEfficiencyMod(ItemStack item);
   
   protected void refillArrow(ServerPlayerEntity player, ItemStack item){
      NbtCompound itemNbt = item.getNbt();
      NbtCompound magicNbt = itemNbt.getCompound("arcananovum");
      NbtList arrows = magicNbt.getList("arrows", NbtElement.COMPOUND_TYPE);
      ArrayList<Integer> eligible = new ArrayList<>();
      
      for(int i = 0; i < arrows.size(); i++){
         NbtCompound stack = arrows.getCompound(i);
         byte count = stack.getByte("Count");
         if(count <= 0 || stack.getString("id").equals("minecraft:air")) return;
         ItemStack itemStack = ItemStack.fromNbt(stack);
         
         if(count < itemStack.getMaxCount()){
            eligible.add(i);
         }
      }
      if(eligible.isEmpty()) return;
      int slot = eligible.get((int)(Math.random()*eligible.size()));
   
      NbtCompound stack = arrows.getCompound(slot);
      byte count = stack.getByte("Count");
      stack.putByte("Count", (byte) (count+1));
      PLAYER_DATA.get(player).addXP(50); // Add xp
      if(this instanceof OverflowingQuiver){
         ArcanaAchievements.progress(player,"spare_stock",1);
      }else if(this instanceof RunicQuiver){
         ArcanaAchievements.progress(player,"unlimited_stock",1);
      }
   }
   
   public boolean shootArrow(ItemStack item, int slot, ServerPlayerEntity player, ItemStack bow){
      NbtCompound itemNbt = item.getNbt();
      NbtCompound magicNbt = itemNbt.getCompound("arcananovum");
      NbtList arrows = magicNbt.getList("arrows", NbtElement.COMPOUND_TYPE);
      boolean runic = MagicItemUtils.identifyItem(bow) instanceof RunicBow;
      
      for(int i = 0; i < arrows.size(); i++){
         NbtCompound stack = arrows.getCompound(i);
         if(stack.getByte("Slot") == slot){
            byte count = stack.getByte("Count");
            if(count <= 0 || stack.getString("id").equals("minecraft:air")) return false;
            if(EnchantmentHelper.getLevel(Enchantments.INFINITY, bow) > 0 && item.isOf(Items.ARROW)) return true;
            if(Math.random() >= getEfficiencyMod(item)) count--;
            
            if(count == 0){
               arrows.remove(i);
               switchArrowOption(player,runic);
            }else{
               stack.putByte("Count",count);
            }
   
            PlayerInventory inv = player.getInventory();
            for(int j = 0; j < inv.size(); j++){
               player.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(-2, 0, j, inv.getStack(j)));
            }
            player.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(-2, 0, PlayerInventory.OFF_HAND_SLOT, inv.getStack(PlayerInventory.OFF_HAND_SLOT)));
           
            return true;
         }
      }
      return false;
   }
   
   public static Pair<String,Integer> getArrowOption(ServerPlayerEntity player, boolean runic){
      IArcanaProfileComponent profile = PLAYER_DATA.get(player);
      String invId = profile.getMiscData(runic ? "runicInvId" : "arrowInvId").asString();
      int invSlot = ((NbtInt)profile.getMiscData(runic ? "runicInvSlot" : "arrowInvSlot")).intValue();
   
      List<Pair<String,Integer>> options = getArrowOptions(player,runic);
      if(options.size() == 0){
         return null;
      }
      for(Pair<String, Integer> option : options){
         if(invId.equals(option.getLeft()) && invSlot == option.getRight()){
            return option;
         }
      }
      return switchArrowOption(player,runic);
   }
   
   public static Pair<String,Integer> switchArrowOption(ServerPlayerEntity player, boolean runic){
      IArcanaProfileComponent profile = PLAYER_DATA.get(player);
      String invId = profile.getMiscData(runic ? "runicInvId" : "arrowInvId").asString();
      int invSlot = ((NbtInt)profile.getMiscData(runic ? "runicInvSlot" : "arrowInvSlot")).intValue();
   
      List<Pair<String,Integer>> options = getArrowOptions(player,runic);
      if(options.size() == 0){
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
      profile.addMiscData(runic ? "runicInvId" : "arrowInvId",NbtString.of(option.getLeft()));
      profile.addMiscData(runic ? "runicInvSlot" : "arrowInvSlot",NbtInt.of(option.getRight()));
      getArrowStack(player,runic,true);
      return option;
   }
   
   public static ItemStack getArrowStack(ServerPlayerEntity player, boolean runic, boolean display){
      Pair<String,Integer> option = getArrowOption(player,runic);
      if(option == null){ // No arrows accessible
         if(display) player.sendMessage(Text.literal("No Arrows Available").formatted(Formatting.RED, Formatting.ITALIC), true);
         return null;
      }else{ // getArrowOption always returns a verified slot, but just in case...
         String invId = option.getLeft();
         int invSlot = option.getRight();
         
         PlayerInventory inv = player.getInventory();
         if(invId.equals("inventory")){
            ItemStack stack = inv.getStack(invSlot);
            if (BOW_PROJECTILES.test(stack)) {
               if(display){
                  Text name = stack.getName();
                  MagicItem magicArrow = MagicItemUtils.identifyItem(stack);
                  if(magicArrow instanceof RunicArrow runicArrow){
                     name = runicArrow.getArrowName(stack);
                  }
                  player.sendMessage(Text.literal("").append(Text.literal("Switched Arrows To: ").formatted(Formatting.GRAY,Formatting.ITALIC)).append(name).formatted(Formatting.ITALIC),true);
               }
               return stack;
            }else{
               Arcananovum.log(2,"Quiver Error Occurred, this shouldn't be possible! Player: "+player.getDisplayName().getString());
               return null;
            }
         }
         
         
         for(int i=0; i<inv.size();i++){ // Scan for quiver of correct id
            ItemStack item = inv.getStack(i);
            if(item.isEmpty()){
               continue;
            }
   
            MagicItem magicItem = MagicItemUtils.identifyItem(item);
            if(magicItem == null) continue;
            if(magicItem.getUUID(item).equals(invId)){
               if(magicItem instanceof RunicQuiver quiver){
                  ItemStack stack = quiver.getArrow(item,invSlot);
                  if(display){
                     Text name = stack.getName();
                     MagicItem magicArrow = MagicItemUtils.identifyItem(stack);
                     if(magicArrow instanceof RunicArrow runicArrow){
                        name = runicArrow.getArrowName(stack);
                     }
                     player.sendMessage(Text.literal("").append(Text.literal("Switched Arrows To: ").formatted(Formatting.LIGHT_PURPLE,Formatting.ITALIC)).append(name).formatted(Formatting.ITALIC),true);
                  }
                  return stack;
               }else if(magicItem instanceof OverflowingQuiver quiver){
                  ItemStack stack = quiver.getArrow(item,invSlot);
                  if(display){
                     Text name = stack.getName();
                     MagicItem magicArrow = MagicItemUtils.identifyItem(stack);
                     if(magicArrow instanceof RunicArrow runicArrow){
                        name = runicArrow.getArrowName(stack);
                     }
                     player.sendMessage(Text.literal("").append(Text.literal("Switched Arrows To: ").formatted(Formatting.DARK_AQUA,Formatting.ITALIC)).append(name).formatted(Formatting.ITALIC),true);
                  }
                  return stack;
               }else{
                  Arcananovum.log(2,"Quiver Error Occurred, this shouldn't be possible! Player: "+player.getDisplayName().getString());
                  return null;
               }
            }
         }
         
      }
      Arcananovum.log(2,"Quiver Error Occurred, this shouldn't be possible! Player: "+player.getDisplayName().getString());
      return null;
   }
   
   public static List<Pair<String,Integer>> getArrowOptions(ServerPlayerEntity player, boolean runic){
      List<Pair<String,Integer>> options = new ArrayList<>();
      // Makes a list of available arrows with the UUID of the quiver containing them or "inventory" and the slot of the inventory they are in
      
      PlayerInventory inv = player.getInventory();
      for(int i=0; i<inv.size();i++){
         ItemStack item = inv.getStack(i);
         if(item.isEmpty()){
            continue;
         }
      
         MagicItem magicItem = MagicItemUtils.identifyItem(item);
         if(magicItem instanceof RunicQuiver quiver && runic){
            NbtCompound itemNbt = item.getNbt();
            NbtCompound magicNbt = itemNbt.getCompound("arcananovum");
            NbtList arrows = magicNbt.getList("arrows", NbtElement.COMPOUND_TYPE);
            for(int j = 0; j < arrows.size(); j++){
               NbtCompound stack = arrows.getCompound(j);
               if(!stack.contains("Slot") || !stack.contains("Count") || !stack.contains("id")) continue;
               if(stack.getByte("Count") <= 0 || stack.getString("id").equals("minecraft:air")) continue;
               options.add(new Pair<>(quiver.getUUID(item),(int) stack.getByte("Slot")));
            }
         }else if(magicItem instanceof OverflowingQuiver quiver){
            NbtCompound itemNbt = item.getNbt();
            NbtCompound magicNbt = itemNbt.getCompound("arcananovum");
            NbtList arrows = magicNbt.getList("arrows", NbtElement.COMPOUND_TYPE);
            for(int j = 0; j < arrows.size(); j++){
               NbtCompound stack = arrows.getCompound(j);
               if(!stack.contains("Slot") || !stack.contains("Count") || !stack.contains("id")) continue;
               if(stack.getByte("Count") <= 0 || stack.getString("id").equals("minecraft:air")) continue;
               options.add(new Pair<>(quiver.getUUID(item),(int) stack.getByte("Slot")));
            }
         }
   
         if (BOW_PROJECTILES.test(item)) {
            if(!runic && magicItem instanceof RunicArrow){
               continue;
            }
            options.add(new Pair<>("inventory",i));
         }
      }
      
      return options;
   }
   
   public ItemStack getArrow(ItemStack quiver, int slot){
      NbtCompound itemNbt = quiver.getNbt();
      NbtCompound magicNbt = itemNbt.getCompound("arcananovum");
      NbtList arrows = magicNbt.getList("arrows", NbtElement.COMPOUND_TYPE);
   
      for(int i = 0; i < arrows.size(); i++){
         NbtCompound stack = arrows.getCompound(i);
         if(stack.getByte("Slot") == slot){
            if(stack.getByte("Count") <= 0 || stack.getString("id").equals("minecraft:air")) return null;
            ItemStack itemStack = ItemStack.fromNbt(stack).copy();
            NbtCompound tag = itemStack.getOrCreateNbt();
            tag.putInt("QuiverSlot",slot);
            tag.putString("QuiverId",getUUID(quiver));
            return itemStack;
         }
      }
      return null;
   }
   
   /*
   public boolean switchSlot(ItemStack item, ServerPlayerEntity player){
      NbtCompound itemNbt = item.getNbt();
      NbtCompound magicNbt = itemNbt.getCompound("arcananovum");
      int slot = magicNbt.getInt("slot");
      NbtList arrows = magicNbt.getList("arrows", NbtElement.COMPOUND_TYPE);
      ArrayList<Integer> slots = new ArrayList<>();
      for(int i = 0; i < arrows.size(); i++){
         NbtCompound stack = arrows.getCompound(i);
         if(!stack.contains("Slot") || !stack.contains("Count") || !stack.contains("id")) continue;
         if(stack.getByte("Count") <= 0 || stack.getString("id").equals("minecraft:air")) continue;
         slots.add((int) stack.getByte("Slot"));
      }
      Collections.sort(slots);
      int found = -1;
      for(int i = 0; i < slots.size(); i++){
         if(slots.get(i) == slot){
            found = i;
            break;
         }
      }
      int newSlot = -1;
      if(found == -1){
         if(slots.isEmpty()){
            magicNbt.putInt("slot",newSlot);
            player.sendMessage(Text.literal("Your Quiver is Empty!").formatted(color,Formatting.ITALIC),true);
            return false;
         }else{
            newSlot = slots.get(0);
         }
      }else{
         if(slots.size() == 1){
            newSlot = slots.get(0);
         }else{
            if(player.isSneaking()){
               if(found == 0){
                  newSlot = slots.get(slots.size()-1);
               }else{
                  newSlot = slots.get(found-1);
               }
            }else{
               if(found == slots.size()-1){
                  newSlot = slots.get(0);
               }else{
                  newSlot = slots.get(found+1);
               }
            }
         }
      }
      magicNbt.putInt("slot",newSlot);
      ItemStack arrow = ItemStack.EMPTY;
      for(int i = 0; i < arrows.size(); i++){
         NbtCompound stack = arrows.getCompound(i);
         if((int) stack.getByte("Slot") == newSlot) arrow = ItemStack.fromNbt(stack);
      }
      
      if(arrow.isEmpty()){
         player.sendMessage(Text.literal("Your Quiver is Empty!").formatted(color,Formatting.ITALIC),true);
         return false;
      }else{
         String name = arrow.getName().getString();
         MagicItem magicArrow = MagicItemUtils.identifyItem(arrow);
         if(magicArrow instanceof RunicArrow){
            name = magicArrow.getName();
         }
         player.sendMessage(Text.literal("Switched Arrows To: "+name).formatted(color,Formatting.ITALIC),true);
         return true;
      }
   }
    */
   
}
