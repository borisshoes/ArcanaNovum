package net.borisshoes.arcananovum.items.core;

import net.borisshoes.arcananovum.gui.quivers.QuiverGui;
import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;

import java.util.ArrayList;
import java.util.Collections;

public abstract class QuiverItem extends MagicItem{
   public static final int size = 9;
   protected Formatting color;
   protected int refillMod;
   
   protected void refillArrow(ItemStack item){
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
   }
   
   public boolean shootArrow(ItemStack item, int slot, ServerPlayerEntity player, ItemStack bow){
      NbtCompound itemNbt = item.getNbt();
      NbtCompound magicNbt = itemNbt.getCompound("arcananovum");
      NbtList arrows = magicNbt.getList("arrows", NbtElement.COMPOUND_TYPE);
      
      for(int i = 0; i < arrows.size(); i++){
         NbtCompound stack = arrows.getCompound(i);
         if(stack.getByte("Slot") == slot){
            byte count = stack.getByte("Count");
            if(count <= 0 || stack.getString("id").equals("minecraft:air")) return false;
            if(EnchantmentHelper.getLevel(Enchantments.INFINITY, bow) > 0 && item.isOf(Items.ARROW)) return true;
            count--;
            
            if(count == 0){
               arrows.remove(i);
               switchSlot(item,player);
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
   
   public ItemStack getArrow(ItemStack item){
      NbtCompound itemNbt = item.getNbt();
      NbtCompound magicNbt = itemNbt.getCompound("arcananovum");
      int slot = magicNbt.getInt("slot");
      NbtList arrows = magicNbt.getList("arrows", NbtElement.COMPOUND_TYPE);
   
      for(int i = 0; i < arrows.size(); i++){
         NbtCompound stack = arrows.getCompound(i);
         if(stack.getByte("Slot") == slot){
            if(stack.getByte("Count") <= 0 || stack.getString("id").equals("minecraft:air")) return null;
            ItemStack itemStack = ItemStack.fromNbt(stack).copy();
            NbtCompound tag = itemStack.getOrCreateNbt();
            tag.putInt("QuiverSlot",slot);
            tag.putString("QuiverId",getUUID(item));
            return itemStack;
         }
      }
      return null;
   }
   
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
   
}
