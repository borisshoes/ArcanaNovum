package net.borisshoes.arcananovum.items.core;

import net.borisshoes.arcananovum.items.ArcaneTome;
import net.borisshoes.arcananovum.recipes.MagicItemRecipe;
import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.borisshoes.arcananovum.utils.MagicRarity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public abstract class MagicItem implements Comparable<MagicItem>{
   protected String name;
   protected String id;
   protected MagicRarity rarity;
   protected ItemStack prefItem;
   protected NbtCompound prefNBT;
   protected int concentrationModifier;
   protected MagicItemRecipe recipe;
   protected NbtCompound bookLore;
   protected ArcaneTome.TomeFilter[] categories;
   public static int version = 9;
   public int itemVersion;
   
   public int getItemVersion() { return itemVersion; }
   
   public String getName(){
      return name;
   }
   
   public String getId(){
      return id;
   }
   
   public MagicRarity getRarity(){
      return rarity;
   }
   
   public int getConcMod(){ return concentrationModifier;}
   
   public NbtCompound getBookLore(){ return bookLore; }
   
   public MagicItemRecipe getRecipe(){ return recipe; }
   
   public ArcaneTome.TomeFilter[] getCategories(){ return categories; }
   
   // Returns item stack with preferred attributes but without a unique UUID
   public ItemStack getPrefItem(){
      return prefItem;
   }
   
   // Returns item stack with preferred attributes and a unique UUID
   public ItemStack getNewItem(){
      ItemStack stack = prefItem.copy();
      NbtCompound tag = prefNBT.copy();
      tag.put("arcananovum",addUUID(tag.getCompound("arcananovum")));
      stack.setNbt(tag);
      return stack;
   }
   
   public ItemStack addCrafter(ItemStack stack, String player){
      NbtCompound itemNbt = stack.getNbt();
      NbtCompound magicTag = itemNbt.getCompound("arcananovum");
      magicTag.putString("crafter", player);
      stack.setNbt(itemNbt);
      return stack;
   }
   
   public String getCrafter(ItemStack item){
      if(!MagicItemUtils.isMagic(item))
         return null;
      NbtCompound itemNbt = item.getNbt();
      NbtCompound magicTag = itemNbt.getCompound("arcananovum");
      return magicTag.getString("crafter");
   }
   
   public NbtCompound getPrefNBT(){
      return prefNBT;
   }
   
   public ItemStack updateItem(ItemStack stack){
      NbtCompound itemNbt = stack.getNbt();
      NbtCompound magicTag = itemNbt.getCompound("arcananovum");
      // For default just replace everything but UUID and crafter and update version
      NbtCompound newTag = prefNBT.copy();
      newTag.getCompound("arcananovum").putString("UUID",magicTag.getString("UUID"));
      newTag.getCompound("arcananovum").putString("crafter",magicTag.getString("crafter"));
      newTag.getCompound("arcananovum").putInt("Version",MagicItem.version + getItemVersion());
      stack.setNbt(newTag);
   
      return stack;
   }
   
   protected NbtCompound addMagicNbt(NbtCompound compound){
      NbtCompound magic = new NbtCompound();
      magic.putString("id",id);
      magic.putInt("Rarity",MagicRarity.getRarityInt(rarity));
      magic.putInt("Version",MagicItem.version + getItemVersion());
      magic.putString("UUID", "-");
      compound.put("arcananovum",magic);
      return compound;
   }
   
   public NbtCompound addUUID(NbtCompound compound){
      compound.putString("UUID", UUID.randomUUID().toString());
      return compound;
   }
   
   public String getUUID(ItemStack item){
      if(!MagicItemUtils.isMagic(item))
         return null;
      NbtCompound itemNbt = item.getNbt();
      NbtCompound magicTag = itemNbt.getCompound("arcananovum");
      return magicTag.getString("UUID");
   }
   
   public int compareTo(@NotNull MagicItem otherItem){
      int rarityCompare = (this.rarity.rarity - otherItem.rarity.rarity);
      if(rarityCompare == 0){
         return this.name.compareTo(otherItem.name);
      }else{
         return rarityCompare;
      }
   }
   
   public ItemStack forgeItem(Inventory inv){
      return getNewItem();
   }
   
   protected void addRunicArrowLore(NbtList loreList){
      loreList.add(NbtString.of("[{\"text\":\"Runic Arrows\",\"italic\":false,\"color\":\"light_purple\"},{\"text\":\" make use of the \",\"color\":\"dark_purple\"},{\"text\":\"Runic Matrix\"},{\"text\":\" to create \",\"color\":\"dark_purple\"},{\"text\":\"unique effects\",\"color\":\"aqua\"},{\"text\":\".\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Runic Arrows\",\"italic\":false,\"color\":\"light_purple\"},{\"text\":\" will \",\"color\":\"dark_purple\"},{\"text\":\"only \",\"color\":\"dark_aqua\",\"italic\":true},{\"text\":\"activate their effect when fired from a \",\"color\":\"dark_purple\"},{\"text\":\"Runic Bow\"},{\"text\":\".\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"The \",\"italic\":false,\"color\":\"dark_purple\"},{\"text\":\"arrows\",\"color\":\"light_purple\"},{\"text\":\" can be refilled inside a \"},{\"text\":\"Runic Quiver\",\"color\":\"light_purple\"},{\"text\":\".\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
   }
   
   protected void setBookLore(List<String> lines){
      bookLore = new NbtCompound();
      NbtList loreList = new NbtList();
      for(String e : lines){
         loreList.add(NbtString.of(e));
      }
      bookLore.put("pages",loreList);
      bookLore.putString("author","Arcana Novum");
      bookLore.putString("filtered_title",id);
      bookLore.putString("title",id);
   }
   
   protected void setRecipe(MagicItemRecipe recipe){
      this.recipe = recipe;
   }
}
