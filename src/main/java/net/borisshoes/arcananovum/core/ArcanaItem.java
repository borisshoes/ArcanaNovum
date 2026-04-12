package net.borisshoes.arcananovum.core;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.augments.ArcanaAugment;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.blocks.forge.StarlightForgeBlockEntity;
import net.borisshoes.arcananovum.gui.arcanetome.ArcaneTomeGui;
import net.borisshoes.arcananovum.research.ResearchTask;
import net.borisshoes.arcananovum.skins.ArcanaSkin;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.arcananovum.utils.EnhancedStatUtils;
import net.borisshoes.borislib.utils.AlgoUtils;
import net.borisshoes.borislib.utils.MinecraftUtils;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.AtlasIds;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.NameAndId;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.util.Tuple;
import net.minecraft.util.Unit;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static net.borisshoes.arcananovum.ArcanaNovum.ITEM_DATA;

public abstract class ArcanaItem implements Comparable<ArcanaItem> {
   public static final String ORIGIN_TAG = "synthetic";
   public static final String CRAFTER_TAG = "crafter";
   public static final String UUID_TAG = "uuid";
   public static final String RARITY_TAG = "Rarity";
   public static final String VERSION_TAG = "Version";
   public static final String ID_TAG = "id";
   public static final String CATALYSTS_TAG = "catalysts";
   public static final String AUGMENTS_TAG = "augments";
   public static final String SKIN_TAG = "skin";
   public static final String UNINITIALIZED_TAG = "uninitialized";
   
   public static final String MODE_TAG = "mode";
   public static final String ACTIVE_TAG = "active";
   public static final String COOLDOWN_TAG = "cooldown";
   
   protected String name;
   protected String id;
   protected ArcanaRarity rarity;
   protected ItemStack prefItem;
   protected ArcaneTomeGui.TomeFilter[] categories;
   public static final int VERSION = 12;
   public int itemVersion;
   protected Item item;
   protected Item vanillaItem;
   protected int maxCount = 1;
   protected Component displayName;
   protected ResourceKey<ResearchTask>[] researchTasks = new ResourceKey[0];
   protected Tuple<MutableComponent, MutableComponent>[] attributions = new Tuple[0];
   
   public Tuple<MutableComponent, MutableComponent>[] getAttributions(){
      return attributions;
   }
   
   public boolean blocksHandInteractions(ItemStack item){
      return false;
   }
   
   public int getItemVersion(){
      return itemVersion;
   }
   
   public String getNameString(){
      return name;
   }
   
   public MutableComponent getTranslatedName(){
      return Component.translatableWithFallback(this.item.getDescriptionId(), getNameString());
   }
   
   public String getId(){
      return id;
   }
   
   public ArcanaRarity getRarity(){
      return rarity;
   }
   
   public abstract List<List<Component>> getBookLore();
   
   public ArcaneTomeGui.TomeFilter[] getCategories(){
      return categories;
   }
   
   public abstract List<Component> getItemLore(@Nullable ItemStack itemStack);
   
   public ResourceKey<ResearchTask>[] getResearchTasks(){
      return researchTasks;
   }
   
   public boolean hasCategory(ArcaneTomeGui.TomeFilter category){
      for(ArcaneTomeGui.TomeFilter tomeFilter : categories){
         if(category == tomeFilter) return true;
      }
      return false;
   }
   
   public Component getDisplayName(){
      return displayName;
   }
   
   public Item getItem(){
      return item;
   }
   
   public Item getVanillaItem(){
      return vanillaItem;
   }
   
   public int getMaxCount(){
      return maxCount;
   }
   
   protected ArcanaItem getThis(){
      return this;
   }
   
   // Returns item stack with preferred attributes but without a unique UUID
   public ItemStack getPrefItem(){
      if(prefItem == null){
         ArcanaNovum.log(2,"Tried to get pref item for "+id+" before it was available");
         return ItemStack.EMPTY;
      }
      return prefItem.copy();
   }
   
   public ItemStack getPrefItemNoLore(){
      if(prefItem == null){
         ArcanaNovum.log(2,"Tried to get pref item (no lore) for "+id+" before it was available");
         return ItemStack.EMPTY;
      }
      ItemStack stack = prefItem.copy();
      stack.remove(DataComponents.LORE);
      return stack;
   }
   
   public static CompoundTag getArcanaTag(ItemStack stack){
      return ITEM_DATA.getDataTag(stack);
   }
   
   public static int getIntProperty(ItemStack stack, String key){
      return ITEM_DATA.getIntProperty(stack, key);
   }
   
   public static String getStringProperty(ItemStack stack, String key){
      return ITEM_DATA.getStringProperty(stack, key);
   }
   
   public static boolean getBooleanProperty(ItemStack stack, String key){
      return ITEM_DATA.getBooleanProperty(stack, key);
   }
   
   public static double getDoubleProperty(ItemStack stack, String key){
      return ITEM_DATA.getDoubleProperty(stack, key);
   }
   
   public static float getFloatProperty(ItemStack stack, String key){
      return ITEM_DATA.getFloatProperty(stack, key);
   }
   
   public static long getLongProperty(ItemStack stack, String key){
      return ITEM_DATA.getLongProperty(stack, key);
   }
   
   public static ListTag getListProperty(ItemStack stack, String key){
      return ITEM_DATA.getListProperty(stack, key);
   }
   
   public static CompoundTag getCompoundProperty(ItemStack stack, String key){
      return ITEM_DATA.getCompoundProperty(stack, key);
   }
   
   public static void putProperty(ItemStack stack, String key, int property){
      ITEM_DATA.putProperty(stack, key, IntTag.valueOf(property));
   }
   
   public static void putProperty(ItemStack stack, String key, boolean property){
      ITEM_DATA.putProperty(stack, key, ByteTag.valueOf(property));
   }
   
   public static void putProperty(ItemStack stack, String key, double property){
      ITEM_DATA.putProperty(stack, key, DoubleTag.valueOf(property));
   }
   
   public static void putProperty(ItemStack stack, String key, float property){
      ITEM_DATA.putProperty(stack, key, FloatTag.valueOf(property));
   }
   
   public static void putProperty(ItemStack stack, String key, String property){
      ITEM_DATA.putProperty(stack, key, StringTag.valueOf(property));
   }
   
   public static void putProperty(ItemStack stack, String key, Tag property){
      ITEM_DATA.putProperty(stack, key, property);
   }
   
   public static boolean hasProperty(ItemStack stack, String key){
      return ITEM_DATA.hasProperty(stack, key);
   }
   
   public static boolean removeProperty(ItemStack stack, String key){
      return ITEM_DATA.removeProperty(stack, key);
   }
   
   // Returns item stack with preferred attributes and a unique UUID
   public ItemStack getNewItem(){
      ItemStack stack = getPrefItem();
      putProperty(stack, UUID_TAG, UUID.randomUUID().toString());
      removeProperty(stack, UNINITIALIZED_TAG);
      return stack;
   }
   
   // Origin: 0 - Crafted, 1 - Synthesized, 2 - Found, 3 - Earned
   public ItemStack addCrafter(ItemStack stack, String player, int origin, MinecraftServer server){
      player = player == null ? "" : player;
      putProperty(stack, CRAFTER_TAG, player);
      putProperty(stack, ORIGIN_TAG, origin);
      return buildItemLore(stack, server);
   }
   
   public String getCrafter(ItemStack item){
      if(!ArcanaItemUtils.isArcane(item))
         return null;
      return getStringProperty(item, CRAFTER_TAG);
   }
   
   public int getOrigin(ItemStack item){
      if(!ArcanaItemUtils.isArcane(item))
         return 0;
      return getIntProperty(item, ORIGIN_TAG);
   }
   
   public static ArcanaSkin getSkin(ItemStack item){
      if(!ArcanaItemUtils.isArcane(item))
         return null;
      return ArcanaSkin.getSkinFromString(getStringProperty(item, SKIN_TAG));
   }
   
   public void initializePrefItem(MinecraftServer server){
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getDefaultMaxStackSize());
      prefItem = buildItemLore(stack, server);
   }
   
   public ItemStack updateItem(ItemStack stack, MinecraftServer server){
      ItemStack newStack = getNewItem();
      String uuid = getStringProperty(stack, UUID_TAG);
      if(uuid.isEmpty() || uuid.equals("-") || uuid.equals(ArcanaNovum.BLANK_UUID)){
         putProperty(newStack, UUID_TAG, UUID.randomUUID().toString());
      }else{
         putProperty(newStack, UUID_TAG, uuid);
      }
      CompoundTag augments = getCompoundProperty(stack, AUGMENTS_TAG);
      ListTag catalysts = getListProperty(stack, CATALYSTS_TAG);
      ArcanaSkin skin = ArcanaSkin.getSkinFromString(getStringProperty(stack, SKIN_TAG));
      if(!augments.isEmpty()) putProperty(newStack, AUGMENTS_TAG, augments);
      if(!catalysts.isEmpty()) putProperty(newStack, CATALYSTS_TAG, catalysts);
      if(skin != null) putProperty(newStack, SKIN_TAG, skin.getSerializedName());
      addCrafter(newStack, getCrafter(stack), getOrigin(stack), server);
      
      EnchantmentHelper.setEnchantments(newStack, stack.getEnchantments());
      
      ArmorTrim trim = stack.get(DataComponents.TRIM);
      if(trim != null){
         newStack.set(DataComponents.TRIM, trim);
      }
      
      DyedItemColor dye = stack.get(DataComponents.DYED_COLOR);
      if(dye != null){
         newStack.set(DataComponents.DYED_COLOR, dye);
      }
      
      if(hasProperty(stack, EnhancedStatUtils.ENHANCED_STAT_TAG)){
         EnhancedStatUtils.enhanceItem(newStack, getDoubleProperty(stack, EnhancedStatUtils.ENHANCED_STAT_TAG));
      }
      
      if(stack.has(DataComponents.CUSTOM_NAME)){
         newStack.set(DataComponents.CUSTOM_NAME, stack.get(DataComponents.CUSTOM_NAME));
      }
      
      if(stack.has(DataComponents.CONTAINER)){
         newStack.set(DataComponents.CONTAINER, stack.get(DataComponents.CONTAINER));
      }
      
      return buildItemLore(newStack, server);
   }
   
   public ItemStack initializeArcanaTag(ItemStack stack, boolean creativeMenuItem){
      putProperty(stack, ID_TAG, id);
      putProperty(stack, RARITY_TAG, ArcanaRarity.getRarityInt(rarity));
      putProperty(stack, VERSION_TAG, ArcanaItem.VERSION + getItemVersion());
      putProperty(stack, UUID_TAG, ArcanaNovum.BLANK_UUID);
      putProperty(stack, AUGMENTS_TAG, new CompoundTag());
      putProperty(stack, CATALYSTS_TAG, new ListTag());
      if(creativeMenuItem){
         putProperty(stack, UNINITIALIZED_TAG, true);
      }else{
         removeProperty(stack, UNINITIALIZED_TAG);
      }
      if(displayName != null){
         stack.set(DataComponents.ITEM_NAME, displayName);
      }
      return stack;
   }
   
   public ItemStack initializeArcanaTag(ItemStack stack){
      return initializeArcanaTag(stack, true);
   }
   
   public ItemStack onAugment(ItemStack stack, ArcanaAugment augment, int level){
      return stack;
   }
   
   public Item.Properties getArcanaItemComponents(){
      return new Item.Properties().stacksTo(1)
            .component(DataComponents.LORE, new ItemLore(getItemLore(null)))
            .component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true)
            .delayedComponent(DataComponents.DAMAGE_RESISTANT, context -> new DamageResistant(context.getOrThrow(ArcanaRegistry.ARCANA_ITEM_IMMUNE_TO)))
            .component(DataComponents.TOOLTIP_DISPLAY, getTooltipDisplayComponent())
            ;
   }
   
   public Item.Properties getArcanaArrowItemComponents(int color){
      this.maxCount = 64;
      return getArcanaItemComponents().stacksTo(64).component(DataComponents.POTION_CONTENTS, new PotionContents(Optional.empty(), Optional.of(color), new ArrayList<>(), Optional.empty()));
   }
   
   public Item.Properties getEquipmentArcanaItemComponents(){
      return getArcanaItemComponents().durability(8192).component(DataComponents.UNBREAKABLE, Unit.INSTANCE);
   }
   
   public static String getUUID(ItemStack item){
      if(!ArcanaItemUtils.isArcane(item))
         return null;
      return getStringProperty(item, UUID_TAG);
   }
   
   public int compareTo(@NotNull ArcanaItem otherItem){
      int rarityCompare = (this.rarity.rarity - otherItem.rarity.rarity);
      if(rarityCompare == 0){
         return this.name.compareTo(otherItem.name);
      }else{
         return rarityCompare;
      }
   }
   
   public ItemStack forgeItem(Container inv, List<Integer> centerpieces, StarlightForgeBlockEntity starlightForge){
      return getNewItem();
   }
   
   protected void addRunicArrowLore(List<MutableComponent> loreList){
      loreList.add(Component.literal("")
            .append(Component.literal("Runic Arrows").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(" make use of the ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("Runic Matrix").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(" to create ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("unique effects").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_PURPLE)));
      loreList.add(Component.literal("")
            .append(Component.literal("Runic Arrows").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(" will ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("only ").withStyle(ChatFormatting.ITALIC, ChatFormatting.DARK_AQUA))
            .append(Component.literal("activate their effect when fired from a ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("Runic Bow").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_PURPLE)));
      loreList.add(Component.literal("")
            .append(Component.literal("The ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("arrows").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(" can be refilled inside a ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("Runic Quiver").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_PURPLE)));
      loreList.add(Component.literal(""));
   }
   
   protected void addAltarLore(List<MutableComponent> loreList){
      loreList.add(Component.literal("")
            .append(Component.literal("Altars ").withStyle(ChatFormatting.AQUA))
            .append(Component.literal("are ").withStyle(ChatFormatting.BLUE))
            .append(Component.literal("multiblock structures").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal(" that must be ").withStyle(ChatFormatting.BLUE))
            .append(Component.literal("built ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("in the world.").withStyle(ChatFormatting.BLUE)));
      loreList.add(Component.literal("")
            .append(Component.literal("Left click a block").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(" with an ").withStyle(ChatFormatting.BLUE))
            .append(Component.literal("Altar ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("to see a ").withStyle(ChatFormatting.BLUE))
            .append(Component.literal("hologram ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("of the ").withStyle(ChatFormatting.BLUE))
            .append(Component.literal("structure").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal(".").withStyle(ChatFormatting.BLUE)));
      loreList.add(Component.literal("")
            .append(Component.literal("Right click").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(" a ").withStyle(ChatFormatting.BLUE))
            .append(Component.literal("completed ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("Altar ").withStyle(ChatFormatting.AQUA))
            .append(Component.literal("setup to ").withStyle(ChatFormatting.BLUE))
            .append(Component.literal("activate ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("the ").withStyle(ChatFormatting.BLUE))
            .append(Component.literal("Altar").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(".").withStyle(ChatFormatting.BLUE)));
      loreList.add(Component.literal(""));
   }
   
   protected void addForgeLore(List<MutableComponent> loreList){
      loreList.add(Component.literal(""));
      loreList.add(Component.literal("Forge Structures:").withStyle(ChatFormatting.BOLD, ChatFormatting.LIGHT_PURPLE));
      loreList.add(Component.literal("")
            .append(Component.literal("Are ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("multiblock structures").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(" that must be ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("built").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(" in the ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("world").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_PURPLE)));
      loreList.add(Component.literal("")
            .append(Component.literal("Must ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("be ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("placed ").withStyle(ChatFormatting.AQUA))
            .append(Component.literal("within a ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("17x11x17").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal(" cube around a ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("Starlight Forge").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_PURPLE)));
      loreList.add(Component.literal("")
            .append(Component.literal("Right Click").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal(" a ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("completed ").withStyle(ChatFormatting.AQUA))
            .append(Component.literal("Forge Structure").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(" to ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("use").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(" it.").withStyle(ChatFormatting.DARK_PURPLE)));
      loreList.add(Component.literal("")
            .append(Component.literal("Right Click").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal(" a ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("Forge Structure").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(" to see a ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("hologram ").withStyle(ChatFormatting.AQUA))
            .append(Component.literal("of the ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("structure").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_PURPLE)));
   }
   
   public ItemStack buildItemLore(ItemStack item, @Nullable MinecraftServer server){
      if(!ArcanaItemUtils.isArcane(item)) return item;
      
      // Item Lore / Info (From Item's class)
      // Crafter (optional)
      // Rarity Tag
      // Enchantments
      // Augmentations
      List<Component> loreList = getItemLore(item);
      String player = getCrafter(item);
      player = player == null ? "" : player;
      int origin = getOrigin(item); // Origin 0 - Crafted, 1 - Synthesized, 2 - Found, 3 - Earned
      ArcanaItem arcanaItem = ArcanaItemUtils.identifyItem(item);
      ArcanaRarity rarity = arcanaItem == null ? ArcanaRarity.MUNDANE : arcanaItem.getRarity();
      ArcanaSkin skin = getSkin(item);
      
      if(skin != null){
         loreList.add(Component.literal(""));
         loreList.add(TextUtils.removeItalics(Component.translatable("text.arcananovum.item_skin", skin.getName()).withColor(skin.getPrimaryColor())));
         List<MutableComponent> descLines = skin.getDescription();
         for(MutableComponent descLine : descLines){
            loreList.add(descLine.withStyle(ChatFormatting.ITALIC).withColor(skin.getSecondaryColor()));
         }
      }
      
      loreList.add(Component.literal(""));
      if(!player.isBlank() && server != null){
         Optional<NameAndId> optional = server.services().nameToIdCache().get(AlgoUtils.getUUID(player));
         String crafterName = optional.isPresent() ? optional.get().name() : "???";
         String crafted = switch(origin){
            case 0 -> rarity == ArcanaRarity.DIVINE ? "Earned by" : "Crafted by";
            case 1 -> "Synthesized by";
            case 2 -> "Found by";
            case 3 -> "Earned by";
            default -> "Crafted by";
         };
         loreList.add(TextUtils.removeItalics(Component.literal("")
               .append(Component.literal(crafted + " ").withStyle(ChatFormatting.ITALIC, ChatFormatting.DARK_PURPLE))
               .append(Component.literal(crafterName).withStyle(ChatFormatting.LIGHT_PURPLE))));
      }
      
      loreList.add(TextUtils.removeItalics(Component.literal("")
            .append(ArcanaRarity.getColoredLabel(rarity, true))
            .append(Component.literal(" Arcana Item").withStyle(ChatFormatting.DARK_PURPLE))));
      
      if(EnhancedStatUtils.isEnhanced(item)){
         loreList.add(Component.literal(""));
         
         double percentile = getDoubleProperty(item, EnhancedStatUtils.ENHANCED_STAT_TAG);
         DecimalFormat df = new DecimalFormat("#0.00");
         df.setRoundingMode(RoundingMode.DOWN);
         loreList.add(TextUtils.removeItalics(Component.literal("")
               .append(Component.literal("Stardust Infusion: ").withStyle(ChatFormatting.YELLOW))
               .append(Component.literal(df.format(percentile * 100) + "%").withStyle(ChatFormatting.GOLD))));
      }
      
      ItemEnchantments enchantComp = EnchantmentHelper.getEnchantmentsForCrafting(item);
      Object2IntOpenHashMap<Holder<Enchantment>> enchants = new Object2IntOpenHashMap<>();
      enchantComp.entrySet().forEach(entry -> enchants.addTo(entry.getKey(), entry.getIntValue()));
      
      if(!enchants.isEmpty()){
         loreList.add(Component.literal(""));
         loreList.add(TextUtils.removeItalics(Component.literal("Enchantments:").withStyle(ChatFormatting.AQUA)));
         
         HolderSet<Enchantment> registryEntryList = null;
         if(server != null){
            Optional<HolderSet.Named<Enchantment>> optional = server.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(EnchantmentTags.TOOLTIP_ORDER);
            if(optional.isPresent()){
               registryEntryList = optional.get();
            }
         }
         
         if(registryEntryList == null){
            for(Object2IntMap.Entry<Holder<Enchantment>> entry : enchants.object2IntEntrySet()){
               Holder<Enchantment> registryEntry = entry.getKey();
               int level = entry.getIntValue();
               loreList.add(TextUtils.removeItalics(Component.literal(Enchantment.getFullname(registryEntry, level).getString()).withStyle(ChatFormatting.BLUE)));
            }
         }else{
            for(int i = 0; i < registryEntryList.size(); i++){
               Holder<Enchantment> enchantment = registryEntryList.get(i);
               if(enchants.containsKey(enchantment)){
                  int level = enchants.getInt(enchantment);
                  loreList.add(TextUtils.removeItalics(Component.literal(Enchantment.getFullname(enchantment, level).getString()).withStyle(ChatFormatting.BLUE)));
               }
            }
         }
      }
      
      CompoundTag augmentTag = getCompoundProperty(item, AUGMENTS_TAG);
      if(!augmentTag.keySet().isEmpty()){
         loreList.add(Component.literal(""));
         loreList.add(TextUtils.removeItalics(Component.literal("Augmentations:").withStyle(ChatFormatting.DARK_AQUA)));
         for(String key : augmentTag.keySet()){
            ArcanaAugment augment = ArcanaAugments.registry.get(key);
            MutableComponent txt = augment.getTranslatedName();
            if(augment.getTiers().length > 1){
               txt.append(Component.literal(" " + TextUtils.intToRoman(augmentTag.getIntOr(key, 0))));
            }
            loreList.add(TextUtils.removeItalics(txt.withStyle(ChatFormatting.BLUE)));
         }
      }
      
      List<Component> statLines = new ArrayList<>();
      ItemAttributeModifiers attrs = item.get(DataComponents.ATTRIBUTE_MODIFIERS);
      // Armor: Armor + Toughness + KB Res + Max HP
      Equippable equip = item.get(DataComponents.EQUIPPABLE);
      if(equip != null && attrs != null){
         EquipmentSlot slot = equip.slot();
         double armor = attrs.compute(Attributes.ARMOR, 0, slot);
         double toughness = attrs.compute(Attributes.ARMOR_TOUGHNESS, 0, slot);
         double kbRes = attrs.compute(Attributes.KNOCKBACK_RESISTANCE, 0, slot);
         double health = attrs.compute(Attributes.MAX_HEALTH, 20.0, slot) - 20.0;
         boolean anyUnusual = false;
         MutableComponent text = Component.literal("").withStyle(ChatFormatting.GOLD);
         if(armor != 0 || toughness != 0){
            text.append(MinecraftUtils.getAtlasedTexture(Items.IRON_CHESTPLATE).withStyle(ChatFormatting.WHITE));
            text.append(Component.literal(" " + TextUtils.readableDouble(armor, 2) + " | "));
            text.append(MinecraftUtils.getAtlasedTexture(Items.DIAMOND_CHESTPLATE).withStyle(ChatFormatting.WHITE));
            text.append(Component.literal(" " + TextUtils.readableDouble(toughness, 2) + " | "));
            anyUnusual = true;
         }
         if(kbRes != 0){
            text.append(MinecraftUtils.getAtlasedTexture(Items.NETHERITE_CHESTPLATE).withStyle(ChatFormatting.WHITE));
            text.append(Component.literal(" " + TextUtils.readableDouble(kbRes, 2) + " | "));
            anyUnusual = true;
         }
         if(health != 0){
            text.append(MinecraftUtils.getAtlasedTexture(AtlasIds.GUI, Identifier.parse("hud/heart/full")).withStyle(ChatFormatting.WHITE));
            text.append(Component.literal((health > 0 ? " +" : " ") + TextUtils.readableDouble(health, 2) + " | "));
            anyUnusual = true;
         }
         if(anyUnusual) statLines.add(text);
      }
      
      // Weapon: Damage + Speed + Range Min + Range Max + Shield Disable
      if(attrs != null){
         boolean anyUnusual = false;
         double dmg = 1 + attrs.compute(Attributes.ATTACK_DAMAGE, 0, EquipmentSlot.MAINHAND);
         double speed = 4.0 + attrs.compute(Attributes.ATTACK_SPEED, 0, EquipmentSlot.MAINHAND);
         double rangeMin = 0, rangeMax = 3.0, shieldDisable = 0;
         AttackRange rangeComp = item.get(DataComponents.ATTACK_RANGE);
         if(rangeComp != null){
            rangeMin = rangeComp.minReach();
            rangeMax = rangeComp.maxReach();
         }
         Weapon weaponComp = item.get(DataComponents.WEAPON);
         if(weaponComp != null){
            shieldDisable = weaponComp.disableBlockingForSeconds();
         }
         MutableComponent text = Component.literal("").withStyle(ChatFormatting.GOLD);
         if(dmg != 1 || speed != 4.0){
            text.append(MinecraftUtils.getAtlasedTexture(Items.DIAMOND_SWORD).withStyle(ChatFormatting.WHITE));
            text.append(Component.literal(" " + TextUtils.readableDouble(dmg, 2) + " | "));
            text.append(MinecraftUtils.getAtlasedTexture(Items.GOLDEN_SWORD).withStyle(ChatFormatting.WHITE));
            text.append(Component.literal(" " + TextUtils.readableDouble(speed, 2) + " | "));
            anyUnusual = true;
         }
         if(rangeMin != 0 || rangeMax != 3.0){
            text.append(MinecraftUtils.getAtlasedTexture(Items.IRON_SPEAR).withStyle(ChatFormatting.WHITE));
            text.append(Component.literal(" " + TextUtils.readableDouble(rangeMin, 2) + "-" + TextUtils.readableDouble(rangeMax, 2) + " | "));
            anyUnusual = true;
         }
         if(shieldDisable != 0){
            text.append(MinecraftUtils.getAtlasedTexture(Items.COPPER_AXE).withStyle(ChatFormatting.WHITE));
            text.append(Component.literal(" " + TextUtils.readableDouble(shieldDisable, 2) + " | "));
            anyUnusual = true;
         }
         if(anyUnusual) statLines.add(text);
      }
      
      // Block Interaction Range + Entity Interaction Range + Mining Speed
      
      // Fall Damage Protection + Aqua Affinity + Movement Speed + Step Height
      
      if(!statLines.isEmpty()){
         loreList.add(Component.literal(""));
         loreList.add(TextUtils.removeItalics(Component.literal("Item Stats:")).withStyle(ChatFormatting.YELLOW));
         loreList.addAll(statLines.stream().map(TextUtils::removeItalics).toList());
      }
      
      // Projectiles / Firework Rocket Info
      ChargedProjectiles projs = item.get(DataComponents.CHARGED_PROJECTILES);
      Fireworks fireworks = item.get(DataComponents.FIREWORKS);
      // TODO
      
      item.set(DataComponents.LORE, new ItemLore(loreList, loreList));
      return item;
   }
   
   public static TooltipDisplay getTooltipDisplayComponent(){
      return TooltipDisplay.DEFAULT
            .withHidden(DataComponents.UNBREAKABLE, true)
            .withHidden(DataComponents.ENCHANTMENTS, true)
            .withHidden(DataComponents.BUNDLE_CONTENTS, true)
            .withHidden(DataComponents.CONTAINER, true)
            .withHidden(DataComponents.ATTRIBUTE_MODIFIERS, true)
            .withHidden(DataComponents.FIREWORK_EXPLOSION, true)
            .withHidden(DataComponents.POTION_CONTENTS, true)
            .withHidden(DataComponents.BASE_COLOR, true)
            .withHidden(DataComponents.DYED_COLOR, true)
            .withHidden(DataComponents.FIREWORKS, true)
            .withHidden(DataComponents.CHARGED_PROJECTILES, true)
            .withHidden(DataComponents.INTANGIBLE_PROJECTILE, true)
            ;
   }
}
