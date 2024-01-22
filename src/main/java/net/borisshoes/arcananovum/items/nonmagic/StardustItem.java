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

public class StardustItem extends NonMagicPolymerItem {
   
   private static final String TXT = "item/stardust";
   
   public StardustItem(Settings settings){
      super(settings);
   }
   
   @Override
   public int getPolymerCustomModelData(ItemStack itemStack, @Nullable ServerPlayerEntity player){
      return ArcanaRegistry.MODELS.get(TXT).value();
   }
   
   @Override
   public ArrayList<Pair<Item, String>> getModels(){
      ArrayList<Pair<Item, String>> models = new ArrayList<>();
      models.add(new Pair<>(Items.GLOWSTONE_DUST,TXT));
      return models;
   }
   
   @Override
   public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player){
      return Items.GLOWSTONE_DUST;
   }
   
   @Override
   public ItemStack getDefaultStack(){
      ItemStack stack = new ItemStack(ArcanaRegistry.STARDUST);
      NbtCompound tag = stack.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList loreList = new NbtList();
      NbtList enchants = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name","[{\"text\":\"Stardust\",\"italic\":false,\"color\":\"yellow\",\"bold\":true}]");
      loreList.add(NbtString.of("[{\"text\":\"The \",\"italic\":false,\"color\":\"gold\"},{\"text\":\"dust \",\"color\":\"yellow\"},{\"text\":\"sparkles \",\"color\":\"aqua\"},{\"text\":\"and \"},{\"text\":\"twinkles \",\"color\":\"aqua\"},{\"text\":\"like the \"},{\"text\":\"night sky\",\"color\":\"blue\"},{\"text\":\".\",\"color\":\"gold\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"The \",\"italic\":false,\"color\":\"gold\"},{\"text\":\"energy\",\"color\":\"aqua\"},{\"text\":\" from the \"},{\"text\":\"Stellar Core\",\"color\":\"yellow\"},{\"text\":\" seems to \"},{\"text\":\"fuse \",\"color\":\"aqua\"},{\"text\":\"with \"},{\"text\":\"enchantments\",\"color\":\"light_purple\"},{\"text\":\".\",\"color\":\"gold\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"This \",\"italic\":false,\"color\":\"gold\"},{\"text\":\"Arcana \",\"color\":\"light_purple\"},{\"text\":\"contained within should harbor \"},{\"text\":\"desirable properties\",\"color\":\"aqua\"},{\"text\":\"...\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Magic Ingredient\",\"italic\":false,\"color\":\"dark_purple\",\"bold\":false}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
      tag.put("Enchantments",enchants);
      return stack;
   }
}