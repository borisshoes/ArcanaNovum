package net.borisshoes.arcananovum.items;

import net.borisshoes.arcananovum.gui.quivers.QuiverGui;
import net.borisshoes.arcananovum.items.core.MagicItem;
import net.borisshoes.arcananovum.items.core.QuiverItem;
import net.borisshoes.arcananovum.items.core.TickingItem;
import net.borisshoes.arcananovum.items.core.UsableItem;
import net.borisshoes.arcananovum.recipes.MagicItemRecipe;
import net.borisshoes.arcananovum.utils.MagicRarity;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class OverflowingQuiver extends QuiverItem implements UsableItem, TickingItem{
   
   public OverflowingQuiver(){
      id = "overflowing_quiver";
      name = "Overflowing Quiver";
      rarity = MagicRarity.EXOTIC;
      categories = new ArcaneTome.TomeFilter[]{ArcaneTome.TomeFilter.EXOTIC, ArcaneTome.TomeFilter.ITEMS};
      color = Formatting.DARK_AQUA;
      refillMod = 1200; // Ticks between arrow refill, once per minute
      
      ItemStack item = new ItemStack(Items.RABBIT_HIDE);
      NbtCompound tag = item.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList loreList = new NbtList();
      NbtList enchants = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name","[{\"text\":\"Overflowing Quiver\",\"italic\":false,\"color\":\"dark_aqua\",\"bold\":true}]");
      loreList.add(NbtString.of("[{\"text\":\"One can never have enough \",\"italic\":false,\"color\":\"aqua\"},{\"text\":\"arrows\",\"color\":\"dark_aqua\"},{\"text\":\"...\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Tipped Arrows\",\"italic\":false,\"color\":\"dark_aqua\"},{\"text\":\" placed within the \",\"color\":\"aqua\"},{\"text\":\"quiver \"},{\"text\":\"restock \",\"color\":\"blue\"},{\"text\":\"over \",\"color\":\"aqua\"},{\"text\":\"time\",\"color\":\"blue\"},{\"text\":\".\",\"color\":\"aqua\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Right Click\",\"italic\":false,\"color\":\"blue\"},{\"text\":\" to put \",\"color\":\"aqua\"},{\"text\":\"arrows \",\"color\":\"dark_aqua\"},{\"text\":\"in the \",\"color\":\"aqua\"},{\"text\":\"quiver\",\"color\":\"dark_aqua\"},{\"text\":\".\",\"color\":\"aqua\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Left Click \",\"italic\":false,\"color\":\"blue\"},{\"text\":\"with a \",\"color\":\"aqua\"},{\"text\":\"bow \"},{\"text\":\"to \",\"color\":\"aqua\"},{\"text\":\"swap \",\"color\":\"dark_aqua\"},{\"text\":\"which type of \",\"color\":\"aqua\"},{\"text\":\"arrow \",\"color\":\"dark_aqua\"},{\"text\":\"will be shot.\",\"color\":\"aqua\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Exotic \",\"italic\":false,\"color\":\"aqua\",\"bold\":true},{\"text\":\"Magic Item\",\"color\":\"dark_purple\",\"bold\":false}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
      tag.put("Enchantments",enchants);
      
      setBookLore(makeLore());
      //setRecipe(makeRecipe());
      tag = addMagicNbt(tag);
      NbtCompound magicTag = tag.getCompound("arcananovum");
      NbtList storedArrows = new NbtList();
      magicTag.putInt("slot",0);
      magicTag.put("arrows",storedArrows);
      prefNBT = tag;
   
      item.setNbt(prefNBT);
      prefItem = item;
   }
   
   @Override
   public boolean useItem(PlayerEntity playerEntity, World world, Hand hand){
      // Open GUI
      if(playerEntity instanceof ServerPlayerEntity player){
         ItemStack stack = playerEntity.getStackInHand(hand);
         QuiverGui gui = new QuiverGui(player,this,stack,false);
         gui.build();
         gui.open();
      }
      return false;
   }
   
   @Override
   public void onTick(ServerWorld world, ServerPlayerEntity player, ItemStack item){
      if(world.getServer().getTicks() % refillMod == 0) refillArrow(player, item);
   }
   
   @Override
   public boolean useItem(PlayerEntity playerEntity, World world, Hand hand, BlockHitResult result){
      return false;
   }
   
   //TODO: Make Recipe
   private MagicItemRecipe makeRecipe(){
      return null;
   }
   
   //TODO: Make Lore
   private List<String> makeLore(){
      ArrayList<String> list = new ArrayList<>();
      list.add("{\"text\":\"TODO\"}");
      return list;
   }
}
