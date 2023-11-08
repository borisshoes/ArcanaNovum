package net.borisshoes.arcananovum.core;

import com.mojang.authlib.GameProfile;
import net.borisshoes.arcananovum.augments.ArcanaAugment;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.items.ArcaneTome;
import net.borisshoes.arcananovum.recipes.arcana.MagicItemRecipe;
import net.borisshoes.arcananovum.utils.LevelUtils;
import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.borisshoes.arcananovum.utils.MagicRarity;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
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
   public static int version = 11;
   public int itemVersion;
   protected Item item;
   protected Item vanillaItem;
   
   public int getItemVersion() { return itemVersion; }
   
   public String getNameString(){
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
   
   public abstract NbtList getItemLore(@Nullable ItemStack itemStack);
   
   public boolean hasCategory(ArcaneTome.TomeFilter category){
      for(ArcaneTome.TomeFilter tomeFilter : categories){
         if(category == tomeFilter) return true;
      }
      return false;
   }
   
   public Item getItem(){
      return item;
   }
   
   public Item getVanillaItem(){
      return vanillaItem;
   }
   
   protected MagicItem getThis(){
      return this;
   }
   
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
      player = player == null ? "" : player;
      magicTag.putString("crafter", player);
      magicTag.putBoolean("synthetic",synthetic);
      return buildItemLore(stack, server);
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
      if(itemNbt.contains("display")){
         NbtCompound display = itemNbt.getCompound("display");
         if(display.contains("color")) newTag.getCompound("display").putInt("color",display.getInt("color"));
      }
      if(itemNbt.contains("Trim")) newTag.put("Trim",itemNbt.getCompound("Trim"));
      if(itemNbt.contains("ArcanaStats")){
         newTag.putDouble("ArcanaStats",itemNbt.getDouble("ArcanaStats"));
         if(itemNbt.contains("AttributeModifiers")) newTag.put("AttributeModifiers",itemNbt.getList("AttributeModifiers",NbtElement.COMPOUND_TYPE));
      }
      stack.setNbt(newTag);
      addCrafter(stack,magicTag.getString("crafter"),magicTag.getBoolean("synthetic"),server);
   
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
   
   public static String getUUID(ItemStack item){
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
   
   public ItemStack buildItemLore(ItemStack item, @Nullable MinecraftServer server){
      if(!MagicItemUtils.isMagic(item)) return item;
      NbtCompound itemNbt = item.getNbt();
      NbtCompound magicTag = itemNbt.getCompound("arcananovum");
      
      // Item Lore / Info (From Item's class)
      // Crafter (optional)
      // Rarity Tag
      // Enchantments
      // Augmentations
      NbtList loreList = getItemLore(item);
      String player = magicTag.getString("crafter");
      player = player == null ? "" : player;
      boolean synthetic = magicTag.getBoolean("synthetic");
      MagicItem magicItem = MagicItemUtils.identifyItem(item);
      MagicRarity rarity = magicItem == null ? MagicRarity.MUNDANE : magicItem.getRarity();
      
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_gray\"}]"));
      if(!player.isBlank() && server != null){
         String crafterName = server.getUserCache().getByUuid(UUID.fromString(player)).orElse(new GameProfile(UUID.fromString(player),"???")).getName();
         String crafted = synthetic ? "Synthesized by" : rarity == MagicRarity.MYTHICAL ? "Earned by" : "Crafted by";
         loreList.add(NbtString.of("[{\"text\":\""+crafted+" \",\"italic\":true,\"color\":\"dark_purple\"},{\"text\":\""+crafterName+"\",\"color\":\"light_purple\"}]"));
      }
      
      loreList.add(NbtString.of("[{\"text\":\""+rarity.label+" \",\"italic\":false,\"color\":\""+MagicRarity.getColor(rarity).getName()+"\",\"bold\":true},{\"text\":\"Magic Item\",\"italic\":false,\"color\":\"dark_purple\",\"bold\":false}]"));
      
      Map<Enchantment,Integer> enchants = EnchantmentHelper.fromNbt(item.getEnchantments());
      if(getUUID(item) == null || getUUID(item).length() < 30){
         enchants.remove(Enchantments.LUCK_OF_THE_SEA,1);
      }
      
      if(!enchants.isEmpty()){
         loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":true,\"color\":\"light_purple\"}]"));
         loreList.add(NbtString.of("[{\"text\":\"Enchantments:\",\"italic\":false,\"color\":\"aqua\"}]"));
         
         for(Map.Entry<Enchantment, Integer> entry : enchants.entrySet()){
            loreList.add(NbtString.of("[{\"text\":\""+entry.getKey().getName(entry.getValue()).getString()+"\",\"italic\":false,\"color\":\"blue\"}]"));
         }
      }
      
      if(magicTag.contains("augments")){
         NbtCompound augmentTag = magicTag.getCompound("augments");
         if(!augmentTag.getKeys().isEmpty()){
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
      
      itemNbt.getCompound("display").put("Lore", loreList);
      return item;
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
