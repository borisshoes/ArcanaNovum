package net.borisshoes.arcananovum.items.core;

import com.mojang.authlib.GameProfile;
import net.borisshoes.arcananovum.augments.ArcanaAugment;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.items.ArcaneTome;
import net.borisshoes.arcananovum.recipes.MagicItemRecipe;
import net.borisshoes.arcananovum.utils.LevelUtils;
import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.borisshoes.arcananovum.utils.MagicRarity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public abstract class MagicItem implements Comparable<MagicItem>{
   protected String name;
   protected String id;
   protected MagicRarity rarity;
   protected ItemStack prefItem;
   protected NbtCompound prefNBT;
   protected MagicItemRecipe recipe;
   protected NbtCompound bookLore;
   protected ArcaneTome.TomeFilter[] categories;
   public static int version = 10;
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
   
   public NbtCompound getBookLore(){ return bookLore; }
   
   public MagicItemRecipe getRecipe(){ return recipe; }
   
   public ArcaneTome.TomeFilter[] getCategories(){ return categories; }
   
   // Returns item stack with preferred attributes but without a unique UUID
   public ItemStack getPrefItem(){
      return prefItem.copy();
   }
   
   // Returns item stack with preferred attributes and a unique UUID
   public ItemStack getNewItem(){
      ItemStack stack = getPrefItem();
      NbtCompound tag = getPrefNBT();
      tag.put("arcananovum",addUUID(tag.getCompound("arcananovum")));
      stack.setNbt(tag);
      return stack;
   }
   
   public ItemStack addCrafter(ItemStack stack, String player, boolean synthetic, MinecraftServer server){
      NbtCompound itemNbt = stack.getNbt();
      NbtCompound magicTag = itemNbt.getCompound("arcananovum");
      magicTag.putString("crafter", player);
      magicTag.putBoolean("synthetic",synthetic);
      NbtList loreList = itemNbt.getCompound("display").getList("Lore", NbtElement.STRING_TYPE);
      if(player.isBlank()) return stack;
      String crafterName = server.getUserCache().getByUuid(UUID.fromString(player)).orElse(new GameProfile(UUID.fromString(player),"???")).getName();
      MagicItem magicItem = MagicItemUtils.identifyItem(stack);
      MagicRarity rarity = magicItem == null ? MagicRarity.MUNDANE : magicItem.getRarity();
      
      int index = -1;
      for(int i = 0; i < loreList.size(); i++){
         NbtString nbtString = (NbtString) loreList.get(i);
         if(nbtString.asString().contains("Crafted by ")){
            loreList.set(i,NbtString.of("[{\"text\":\"Crafted by \",\"italic\":true,\"color\":\"dark_purple\"},{\"text\":\""+crafterName+"\",\"color\":\"light_purple\"}]"));
            index = -1;
            break;
         }else if(nbtString.asString().contains("Synthesized by ")){
            loreList.set(i,NbtString.of("[{\"text\":\"Synthesized by \",\"italic\":true,\"color\":\"dark_purple\"},{\"text\":\""+crafterName+"\",\"color\":\"light_purple\"}]"));
            index = -1;
            break;
         }else if(nbtString.asString().contains("Earned by ")){
            loreList.set(i,NbtString.of("[{\"text\":\"Earned by \",\"italic\":true,\"color\":\"dark_purple\"},{\"text\":\""+crafterName+"\",\"color\":\"light_purple\"}]"));
            index = -1;
            break;
         }else if(nbtString.asString().contains("Magic Item")){
            index = i+1;
         }
      }
      if(index != -1){
         String crafted = synthetic ? "Synthesized by" : rarity == MagicRarity.MYTHICAL ? "Earned by" : "Crafted by";
         loreList.add(index,NbtString.of("[{\"text\":\""+crafted+" \",\"italic\":true,\"color\":\"dark_purple\"},{\"text\":\""+crafterName+"\",\"color\":\"light_purple\"}]"));
      }
      
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
   
   public boolean isSynthetic(ItemStack item){
      if(!MagicItemUtils.isMagic(item))
         return false;
      NbtCompound itemNbt = item.getNbt();
      NbtCompound magicTag = itemNbt.getCompound("arcananovum");
      return magicTag.getBoolean("synthetic");
   }
   
   public NbtCompound getPrefNBT(){
      return prefNBT.copy();
   }
   
   public ItemStack updateItem(ItemStack stack, MinecraftServer server){
      NbtCompound itemNbt = stack.getNbt();
      NbtCompound magicTag = itemNbt.getCompound("arcananovum");
      // For default just replace everything but UUID and crafter and update version
      NbtCompound newTag = getPrefNBT();
      newTag.getCompound("arcananovum").putString("UUID",magicTag.getString("UUID"));
      newTag.getCompound("arcananovum").putInt("Version",MagicItem.version + getItemVersion());
      if(magicTag.contains("augments")) newTag.getCompound("arcananovum").put("augments",magicTag.getCompound("augments"));
      stack.setNbt(newTag);
      addCrafter(stack,magicTag.getString("crafter"),magicTag.getBoolean("synthetic"),server);
      redoAugmentLore(stack);
   
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
   
   public void redoAugmentLore(ItemStack item){
      if(!MagicItemUtils.isMagic(item)) return;
      NbtCompound itemNbt = item.getNbt();
      NbtCompound magicTag = itemNbt.getCompound("arcananovum");
      NbtList loreList = itemNbt.getCompound("display").getList("Lore", NbtElement.STRING_TYPE);
      if(magicTag.contains("augments")){
         NbtCompound augmentTag = magicTag.getCompound("augments");
   
         int index = -1;
         for(int i = 0; i < loreList.size(); i++){
            NbtString nbtString = (NbtString) loreList.get(i);
            if(nbtString.asString().contains("Augmentations:")){
               index = i;
               break;
            }
         }
         if(index != -1){
            while(loreList.size() > index-1){
               loreList.remove(index-1);
            }
         }
         loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":true,\"color\":\"light_purple\"}]"));
         loreList.add(NbtString.of("[{\"text\":\"Augmentations:\",\"italic\":false,\"color\":\"dark_aqua\"}]"));
         for(String key : augmentTag.getKeys()){
            ArcanaAugment augment = ArcanaAugments.registry.get(key);
            String str = augment.name;
            if(augment.getTiers().length > 1){
               str += " "+ LevelUtils.intToRoman(augmentTag.getInt(key));
            }
            loreList.add(NbtString.of("[{\"text\":\""+str+"\",\"italic\":false,\"color\":\"blue\"}]"));
         }
      }
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
