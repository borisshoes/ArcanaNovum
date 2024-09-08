package net.borisshoes.arcananovum.core;

import com.mojang.authlib.GameProfile;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.augments.ArcanaAugment;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.gui.arcanetome.TomeGui;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaRecipe;
import net.borisshoes.arcananovum.research.ResearchTask;
import net.borisshoes.arcananovum.utils.*;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.trim.ArmorTrim;
import net.minecraft.nbt.*;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public abstract class ArcanaItem implements Comparable<ArcanaItem>{
   public static final String SYNTHETIC_TAG = "synthetic";
   public static final String CRAFTER_TAG = "crafter";
   public static final String UUID_TAG = "uuid";
   public static final String RARITY_TAG = "Rarity";
   public static final String VERSION_TAG = "Version";
   public static final String ID_TAG = "id";
   public static final String CATALYSTS_TAG = "catalysts";
   public static final String AUGMENTS_TAG = "augments";
   
   public static final String MODE_TAG = "mode";
   public static final String ACTIVE_TAG = "active";
   public static final String COOLDOWN_TAG = "cooldown";
   
   protected String name;
   protected String id;
   protected ArcanaRarity rarity;
   protected ItemStack prefItem;
   protected ArcanaRecipe recipe;
   protected TomeGui.TomeFilter[] categories;
   public static final int VERSION = 12;
   public int itemVersion;
   protected Item item;
   protected Item vanillaItem;
   protected ArrayList<Pair<Item,String>> models;
   protected RegistryKey<ResearchTask>[] researchTasks = new RegistryKey[0];
   
   public ArrayList<Pair<Item,String>> getModels(){
      return models;
   }
   
   public boolean blocksHandInteractions(ItemStack item){
      return false;
   }
   
   public int getItemVersion() { return itemVersion; }
   
   public String getNameString(){
      return name;
   }
   
   public MutableText getTranslatedName(){
      return Text.translatableWithFallback(this.item.getTranslationKey(),getNameString());
   }
   
   public String getId(){
      return id;
   }
   
   public ArcanaRarity getRarity(){
      return rarity;
   }
   
   public abstract List<List<Text>> getBookLore();
   
   public ArcanaRecipe getRecipe(){ return recipe; }
   
   public TomeGui.TomeFilter[] getCategories(){ return categories; }
   
   public abstract List<Text> getItemLore(@Nullable ItemStack itemStack);
   
   public RegistryKey<ResearchTask>[] getResearchTasks(){
      return researchTasks;
   }
   
   public boolean hasCategory(TomeGui.TomeFilter category){
      for(TomeGui.TomeFilter tomeFilter : categories){
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
   
   protected ArcanaItem getThis(){
      return this;
   }
   
   // Returns item stack with preferred attributes but without a unique UUID
   public ItemStack getPrefItem(){
      return prefItem.copy();
   }
   
   public ItemStack getPrefItemNoLore(){
      ItemStack stack = prefItem.copy();
      stack.remove(DataComponentTypes.LORE);
      return stack;
   }
   
   public static NbtCompound getArcanaTag(ItemStack stack){
      if(stack == null) return new NbtCompound();
      NbtComponent nbtComponent = stack.get(DataComponentTypes.CUSTOM_DATA);
      if(nbtComponent == null) return new NbtCompound();
      NbtCompound data = nbtComponent.copyNbt();
      if(data != null && data.contains(ArcanaNovum.MOD_ID,NbtElement.COMPOUND_TYPE)){
         return data.getCompound(ArcanaNovum.MOD_ID);
      }
      return new NbtCompound();
   }
   
   public static int getIntProperty(ItemStack stack, String key){
      NbtCompound arcanaTag = getArcanaTag(stack);
      return  arcanaTag == null || !arcanaTag.contains(key, NbtElement.INT_TYPE) ? 0 : arcanaTag.getInt(key);
   }
   
   public static String getStringProperty(ItemStack stack, String key){
      NbtCompound arcanaTag = getArcanaTag(stack);
      return  arcanaTag == null || !arcanaTag.contains(key, NbtElement.STRING_TYPE) ? "" : arcanaTag.getString(key);
   }
   
   public static boolean getBooleanProperty(ItemStack stack, String key){
      NbtCompound arcanaTag = getArcanaTag(stack);
      return arcanaTag == null || !arcanaTag.contains(key, NbtElement.BYTE_TYPE) ? false : arcanaTag.getBoolean(key);
   }
   
   public static double getDoubleProperty(ItemStack stack, String key){
      NbtCompound arcanaTag = getArcanaTag(stack);
      return  arcanaTag == null || !arcanaTag.contains(key, NbtElement.DOUBLE_TYPE) ? 0.0 : arcanaTag.getDouble(key);
   }
   
   public static float getFloatProperty(ItemStack stack, String key){
      NbtCompound arcanaTag = getArcanaTag(stack);
      return  arcanaTag == null || !arcanaTag.contains(key, NbtElement.FLOAT_TYPE) ? 0.0f : arcanaTag.getFloat(key);
   }
   
   public static long getLongProperty(ItemStack stack, String key){
      NbtCompound arcanaTag = getArcanaTag(stack);
      return  arcanaTag == null || !arcanaTag.contains(key, NbtElement.LONG_TYPE) ? 0 : arcanaTag.getLong(key);
   }
   
   public static NbtList getListProperty(ItemStack stack, String key, int listType){
      NbtCompound arcanaTag = getArcanaTag(stack);
      return  arcanaTag == null || !arcanaTag.contains(key, NbtElement.LIST_TYPE) ? new NbtList() : arcanaTag.getList(key,listType);
   }
   
   public static NbtCompound getCompoundProperty(ItemStack stack, String key){
      NbtCompound arcanaTag = getArcanaTag(stack);
      return  arcanaTag == null || !arcanaTag.contains(key, NbtElement.COMPOUND_TYPE) ? new NbtCompound() : arcanaTag.getCompound(key);
   }
   
   public static void putProperty(ItemStack stack, String key, int property){
      putProperty(stack, key, NbtInt.of(property));
   }
   
   public static void putProperty(ItemStack stack, String key, boolean property){
      putProperty(stack, key, NbtByte.of(property));
   }
   
   public static void putProperty(ItemStack stack, String key, double property){
      putProperty(stack,key,NbtDouble.of(property));
   }
   
   public static void putProperty(ItemStack stack, String key, float property){
      putProperty(stack,key,NbtFloat.of(property));
   }
   
   public static void putProperty(ItemStack stack, String key, String property){
      putProperty(stack,key,NbtString.of(property));
   }
   
   public static void putProperty(ItemStack stack, String key, NbtElement property){
      NbtCompound arcanaTag = getArcanaTag(stack);
      arcanaTag.put(key,property);
      NbtComponent nbtComponent = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT);
      NbtCompound data = nbtComponent.copyNbt();
      data.put(ArcanaNovum.MOD_ID,arcanaTag);
      NbtComponent.set(DataComponentTypes.CUSTOM_DATA, stack, data);
   }
   
   public static boolean hasProperty(ItemStack stack, String key){
      NbtCompound arcanaTag = getArcanaTag(stack);
      return arcanaTag.contains(key);
   }
   
   public static boolean hasProperty(ItemStack stack, String key, int type){
      NbtCompound arcanaTag = getArcanaTag(stack);
      return arcanaTag.contains(key,type);
   }
   
   public static boolean removeProperty(ItemStack stack, String key){
      if(hasProperty(stack,key)){
         NbtCompound arcanaTag = getArcanaTag(stack);
         arcanaTag.remove(key);
         if(arcanaTag.isEmpty()){
            NbtComponent nbtComponent = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT);
            NbtCompound data = nbtComponent.copyNbt();
            data.remove(ArcanaNovum.MOD_ID);
            NbtComponent.set(DataComponentTypes.CUSTOM_DATA, stack, data);
         }else{
            NbtComponent nbtComponent = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT);
            NbtCompound data = nbtComponent.copyNbt();
            data.put(ArcanaNovum.MOD_ID,arcanaTag);
            NbtComponent.set(DataComponentTypes.CUSTOM_DATA, stack, data);
         }
         return true;
      }
      return false;
   }
   
   // Returns item stack with preferred attributes and a unique UUID
   public ItemStack getNewItem(){
      ItemStack stack = getPrefItem();
      putProperty(stack,UUID_TAG,UUID.randomUUID().toString());
      return stack;
   }
   
   public ItemStack addCrafter(ItemStack stack, String player, boolean synthetic, MinecraftServer server){
      player = player == null ? "" : player;
      putProperty(stack,CRAFTER_TAG,player);
      putProperty(stack,SYNTHETIC_TAG, synthetic);
      return buildItemLore(stack, server);
   }
   
   public String getCrafter(ItemStack item){
      if(!ArcanaItemUtils.isArcane(item))
         return null;
      return getStringProperty(item,CRAFTER_TAG);
   }
   
   public boolean isSynthetic(ItemStack item){
      if(!ArcanaItemUtils.isArcane(item))
         return false;
      return getBooleanProperty(item,SYNTHETIC_TAG);
   }
   
   
   protected void setPrefStack(ItemStack stack){
      prefItem = stack.copy();
   }
   
   public void initializePrefItem(){
      prefItem = buildItemLore(prefItem, ArcanaNovum.SERVER);
   }
   
   // Override to apply any default enchantments
   public void finalizePrefItem(MinecraftServer server){
      setRecipe(makeRecipe());
   }
   
   public ItemStack updateItem(ItemStack stack, MinecraftServer server){
      // For default just replace everything but UUID and crafter and update version
      ItemStack newStack = getNewItem();
      putProperty(newStack,UUID_TAG,getStringProperty(stack,UUID_TAG));
      NbtCompound augments = getCompoundProperty(stack,AUGMENTS_TAG);
      NbtList catalysts = getListProperty(stack,CATALYSTS_TAG,NbtElement.COMPOUND_TYPE);
      if(!augments.isEmpty()) putProperty(newStack, AUGMENTS_TAG, augments);
      if(!catalysts.isEmpty()) putProperty(newStack, CATALYSTS_TAG, catalysts);
      addCrafter(newStack,getCrafter(stack),isSynthetic(stack),server);
      
      EnchantmentHelper.set(newStack,stack.getEnchantments());
      
      ArmorTrim trim = stack.get(DataComponentTypes.TRIM);
      if(trim != null){
         newStack.set(DataComponentTypes.TRIM, trim);
      }
      
      DyedColorComponent dye = stack.get(DataComponentTypes.DYED_COLOR);
      if(dye != null){
         newStack.set(DataComponentTypes.DYED_COLOR,dye);
      }
      
      if(hasProperty(stack, EnhancedStatUtils.ENHANCED_STAT_TAG)){
         EnhancedStatUtils.enhanceItem(newStack,getDoubleProperty(stack,EnhancedStatUtils.ENHANCED_STAT_TAG));
      }
      
      if(stack.contains(DataComponentTypes.CUSTOM_NAME)){
         newStack.set(DataComponentTypes.CUSTOM_NAME,stack.get(DataComponentTypes.CUSTOM_NAME));
      }
   
      return buildItemLore(newStack,server);
   }
   
   public ItemStack initializeArcanaTag(ItemStack stack){
      putProperty(stack,ID_TAG,id);
      putProperty(stack,RARITY_TAG, ArcanaRarity.getRarityInt(rarity));
      putProperty(stack,VERSION_TAG, ArcanaItem.VERSION + getItemVersion());
      putProperty(stack,UUID_TAG,"-");
      putProperty(stack,AUGMENTS_TAG, new NbtCompound());
      putProperty(stack,CATALYSTS_TAG,new NbtList());
      return stack;
   }
   
   public static String getUUID(ItemStack item){
      if(!ArcanaItemUtils.isArcane(item))
         return null;
      return getStringProperty(item,UUID_TAG);
   }
   
   public int compareTo(@NotNull ArcanaItem otherItem){
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
   
   protected void addRunicArrowLore(List<MutableText> loreList){
      loreList.add(Text.literal("")
            .append(Text.literal("Runic Arrows").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal(" make use of the ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("Runic Matrix").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal(" to create ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("unique effects").formatted(Formatting.AQUA))
            .append(Text.literal(".").formatted(Formatting.DARK_PURPLE)));
      loreList.add(Text.literal("")
            .append(Text.literal("Runic Arrows").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal(" will ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("only ").formatted(Formatting.ITALIC,Formatting.DARK_AQUA))
            .append(Text.literal("activate their effect when fired from a ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("Runic Bow").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal(".").formatted(Formatting.DARK_PURPLE)));
      loreList.add(Text.literal("")
            .append(Text.literal("The ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("arrows").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal(" can be refilled inside a ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("Runic Quiver").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal(".").formatted(Formatting.DARK_PURPLE)));
      loreList.add(Text.literal(""));
   }
   
   protected void addAltarLore(List<MutableText> loreList){
      loreList.add(Text.literal("")
            .append(Text.literal("Altars ").formatted(Formatting.AQUA))
            .append(Text.literal("are ").formatted(Formatting.BLUE))
            .append(Text.literal("multiblock structures").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal(" that must be ").formatted(Formatting.BLUE))
            .append(Text.literal("built ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("in the world.").formatted(Formatting.BLUE)));
      loreList.add(Text.literal("")
            .append(Text.literal("Left click a block").formatted(Formatting.AQUA))
            .append(Text.literal(" with an ").formatted(Formatting.BLUE))
            .append(Text.literal("Altar ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("to see a ").formatted(Formatting.BLUE))
            .append(Text.literal("hologram ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("of the ").formatted(Formatting.BLUE))
            .append(Text.literal("structure").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal(".").formatted(Formatting.BLUE)));
      loreList.add(Text.literal("")
            .append(Text.literal("Right click").formatted(Formatting.AQUA))
            .append(Text.literal(" a ").formatted(Formatting.BLUE))
            .append(Text.literal("completed ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("Altar ").formatted(Formatting.AQUA))
            .append(Text.literal("setup to ").formatted(Formatting.BLUE))
            .append(Text.literal("activate ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("the ").formatted(Formatting.BLUE))
            .append(Text.literal("Altar").formatted(Formatting.AQUA))
            .append(Text.literal(".").formatted(Formatting.BLUE)));
      loreList.add(Text.literal(""));
   }
   
   protected void addForgeLore(List<MutableText> loreList){
      loreList.add(Text.literal(""));
      loreList.add(Text.literal("Forge Structures:").formatted(Formatting.BOLD,Formatting.LIGHT_PURPLE));
      loreList.add(Text.literal("")
            .append(Text.literal("Are ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("multiblock structures").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal(" that must be ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("built").formatted(Formatting.AQUA))
            .append(Text.literal(" in the ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("world").formatted(Formatting.DARK_AQUA))
            .append(Text.literal(".").formatted(Formatting.DARK_PURPLE)));
      loreList.add(Text.literal("")
            .append(Text.literal("Must ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("be ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("placed ").formatted(Formatting.AQUA))
            .append(Text.literal("within a ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("17x11x17").formatted(Formatting.DARK_AQUA))
            .append(Text.literal(" cube around a ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("Starlight Forge").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal(".").formatted(Formatting.DARK_PURPLE)));
      loreList.add(Text.literal("")
            .append(Text.literal("Right Click").formatted(Formatting.DARK_AQUA))
            .append(Text.literal(" a ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("completed ").formatted(Formatting.AQUA))
            .append(Text.literal("Forge Structure").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal(" to ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("use").formatted(Formatting.AQUA))
            .append(Text.literal(" it.").formatted(Formatting.DARK_PURPLE)));
      loreList.add(Text.literal("")
            .append(Text.literal("Right Click").formatted(Formatting.DARK_AQUA))
            .append(Text.literal(" a ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("Forge Structure").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal(" to see a ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("hologram ").formatted(Formatting.AQUA))
            .append(Text.literal("of the ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("structure").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal(".").formatted(Formatting.DARK_PURPLE)));
   }
   
   public ItemStack buildItemLore(ItemStack item, @Nullable MinecraftServer server){
      if(!ArcanaItemUtils.isArcane(item)) return item;
      
      // Item Lore / Info (From Item's class)
      // Crafter (optional)
      // Rarity Tag
      // Enchantments
      // Augmentations
      List<Text> loreList = getItemLore(item);
      String player = getCrafter(item);
      player = player == null ? "" : player;
      boolean synthetic = isSynthetic(item);
      ArcanaItem arcanaItem = ArcanaItemUtils.identifyItem(item);
      ArcanaRarity rarity = arcanaItem == null ? ArcanaRarity.MUNDANE : arcanaItem.getRarity();
      
      loreList.add(Text.literal(""));
      if(!player.isBlank() && server != null){
         String crafterName = server.getUserCache().getByUuid(MiscUtils.getUUID(player)).orElse(new GameProfile(MiscUtils.getUUID(player),"???")).getName();
         String crafted = synthetic ? "Synthesized by" : rarity == ArcanaRarity.DIVINE ? "Earned by" : "Crafted by";
         loreList.add(TextUtils.removeItalics(Text.literal("")
               .append(Text.literal(crafted+" ").formatted(Formatting.ITALIC,Formatting.DARK_PURPLE))
               .append(Text.literal(crafterName).formatted(Formatting.LIGHT_PURPLE))));
      }
      
      loreList.add(TextUtils.removeItalics(Text.literal("")
            .append(ArcanaRarity.getColoredLabel(rarity,true))
            .append(Text.literal(" Arcana Item").formatted(Formatting.DARK_PURPLE))));
      
      if(EnhancedStatUtils.isEnhanced(item)){
         loreList.add(Text.literal(""));
         
         double percentile = getDoubleProperty(item,EnhancedStatUtils.ENHANCED_STAT_TAG);
         DecimalFormat df = new DecimalFormat("#0.00");
         df.setRoundingMode(RoundingMode.DOWN);
         loreList.add(TextUtils.removeItalics(Text.literal("")
               .append(Text.literal("Stardust Infusion: ").formatted(Formatting.YELLOW))
               .append(Text.literal(df.format(percentile*100)+"%").formatted(Formatting.GOLD))));
      }
      
      ItemEnchantmentsComponent enchantComp = EnchantmentHelper.getEnchantments(item);
      Set<Object2IntMap.Entry<RegistryEntry<Enchantment>>> enchants = enchantComp.getEnchantmentEntries();
      
      if(!enchants.isEmpty()){
         loreList.add(Text.literal(""));
         loreList.add(TextUtils.removeItalics(Text.literal("Enchantments:").formatted(Formatting.AQUA)));
         
         for(Object2IntMap.Entry<RegistryEntry<Enchantment>> entry : enchants){
            RegistryEntry<Enchantment> registryEntry = entry.getKey();
            int level = entry.getIntValue();
            
            loreList.add(TextUtils.removeItalics(Text.literal(Enchantment.getName(registryEntry,level).getString()).formatted(Formatting.BLUE)));
         }
      }
      
      NbtCompound augmentTag = getCompoundProperty(item,AUGMENTS_TAG);
      if(!augmentTag.getKeys().isEmpty()){
         loreList.add(Text.literal(""));
         loreList.add(TextUtils.removeItalics(Text.literal("Augmentations:").formatted(Formatting.DARK_AQUA)));
         for(String key : augmentTag.getKeys()){
            ArcanaAugment augment = ArcanaAugments.registry.get(key);
            MutableText txt = augment.getTranslatedName();
            if(augment.getTiers().length > 1){
               txt.append(Text.literal(" "+LevelUtils.intToRoman(augmentTag.getInt(key))));
            }
            loreList.add(TextUtils.removeItalics(txt.formatted(Formatting.BLUE)));
         }
      }
      
      item.set(DataComponentTypes.LORE, new LoreComponent(loreList,loreList));
      return item;
   }
   
   protected void setRecipe(ArcanaRecipe recipe){
      this.recipe = recipe;
   }
   
   // Override to create a recipe
   protected ArcanaRecipe makeRecipe(){
      return null;
   }
}
