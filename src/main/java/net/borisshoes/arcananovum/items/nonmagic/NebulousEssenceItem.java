package net.borisshoes.arcananovum.items.nonmagic;

import eu.pb4.polymer.core.api.item.PolymerItem;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.core.polymer.NonMagicPolymerItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class NebulousEssenceItem extends NonMagicPolymerItem {
   
   private static final String TXT = "item/nebulous_essence";
   
   public NebulousEssenceItem(Settings settings){
      super(settings);
   }
   
   @Override
   public int getPolymerCustomModelData(ItemStack itemStack, @Nullable ServerPlayerEntity player){
      return ArcanaRegistry.MODELS.get(TXT).value();
   }
   
   @Override
   public ArrayList<Pair<Item, String>> getModels(){
      ArrayList<Pair<Item, String>> models = new ArrayList<>();
      models.add(new Pair<>(Items.SCULK_VEIN,TXT));
      return models;
   }
   
   @Override
   public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player){
      return Items.SCULK_VEIN;
   }
   
   @Override
   public ItemStack getDefaultStack(){
      ItemStack stack = new ItemStack(ArcanaRegistry.NEBULOUS_ESSENCE);
      NbtCompound tag = stack.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList loreList = new NbtList();
      NbtList enchants = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name","[{\"text\":\"Nebulous Essence\",\"italic\":false,\"color\":\"dark_purple\",\"bold\":true}]");
      loreList.add(NbtString.of("[{\"text\":\"With precise \",\"italic\":false,\"color\":\"dark_aqua\"},{\"text\":\"deconstruction\",\"color\":\"dark_purple\"},{\"text\":\", an \"},{\"text\":\"amorphic essence\",\"color\":\"aqua\"},{\"text\":\" has been \"},{\"text\":\"distilled\",\"color\":\"dark_purple\"},{\"text\":\".\",\"color\":\"dark_aqua\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"This \",\"italic\":false,\"color\":\"dark_aqua\"},{\"text\":\"ethereal substance\",\"color\":\"aqua\"},{\"text\":\" must be pure \"},{\"text\":\"enchantment \",\"color\":\"dark_purple\"},{\"text\":\"Arcana\",\"color\":\"light_purple\"},{\"text\":\".\",\"color\":\"dark_aqua\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"It \",\"italic\":false,\"color\":\"dark_aqua\"},{\"text\":\"pulses \",\"color\":\"aqua\"},{\"text\":\"and \"},{\"text\":\"undulates\",\"color\":\"dark_purple\"},{\"text\":\", changing in \"},{\"text\":\"color \",\"color\":\"aqua\"},{\"text\":\"and \"},{\"text\":\"texture\",\"color\":\"dark_purple\"},{\"text\":\".\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Magic Ingredient\",\"italic\":false,\"color\":\"dark_purple\",\"bold\":false}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
      tag.put("Enchantments",enchants);
      return stack;
   }
}