package net.borisshoes.arcananovum.items.normal;

import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.core.polymer.NormalPolymerItem;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.*;

public class GraphicalItem extends NormalPolymerItem {
   
   public static final String GRAPHICS_TAG = "graphic_id";
   
   private static final ArrayList<Pair<Item,String>> MODEL_LIST = new ArrayList<>(Arrays.asList(
         new Pair<>(Items.STRUCTURE_VOID,"gui/confirm"),
         new Pair<>(Items.BARRIER,"gui/cancel"),
         new Pair<>(Items.SPECTRAL_ARROW,"gui/right_arrow"),
         new Pair<>(Items.SPECTRAL_ARROW,"gui/left_arrow"),
         new Pair<>(Items.NETHER_STAR,"gui/sort"),
         new Pair<>(Items.HOPPER,"gui/filter"),
         new Pair<>(Items.BLACK_STAINED_GLASS_PANE,"gui/black"),
         new Pair<>(Items.NETHER_STAR,"gui/star"),
         new Pair<>(Items.GRAY_STAINED_GLASS_PANE,"gui/gas"),
         new Pair<>(Items.ORANGE_STAINED_GLASS_PANE,"gui/plasma"),
         new Pair<>(Items.ENDER_PEARL,"gui/black_hole"),
         new Pair<>(Items.BLAZE_POWDER,"gui/nova"),
         new Pair<>(Items.MAGMA_CREAM,"gui/supernova"),
         new Pair<>(Items.ENDER_EYE,"gui/quasar"),
         new Pair<>(Items.END_CRYSTAL,"gui/pulsar"),
         new Pair<>(Items.PURPLE_STAINED_GLASS_PANE,"gui/nebula"),
         new Pair<>(Items.HEAVY_CORE,"gui/planet"),
         new Pair<>(Items.KNOWLEDGE_BOOK,"gui/transmutation_book")
   ));
   
   private static final ArrayList<Pair<DyeColor, Integer>> DYED_REPLACEMENTS = new ArrayList<>(Arrays.asList(
         new Pair<>(DyeColor.BLACK, 0x000000),
         new Pair<>(DyeColor.BLUE, 0x0000ff),
         new Pair<>(DyeColor.BROWN, 0x6b5341),
         new Pair<>(DyeColor.GRAY, 0x5c5c5c),
         new Pair<>(DyeColor.CYAN, 0x168e94),
         new Pair<>(DyeColor.GREEN, 0x04753a),
         new Pair<>(DyeColor.LIGHT_BLUE, 0x5ad2fa),
         new Pair<>(DyeColor.LIGHT_GRAY, 0xc7c7c7),
         new Pair<>(DyeColor.LIME, 0x4ded0e),
         new Pair<>(DyeColor.MAGENTA, 0xb306c9),
         new Pair<>(DyeColor.ORANGE, 0xff8800),
         new Pair<>(DyeColor.PINK, 0xff7dde),
         new Pair<>(DyeColor.PURPLE, 0x8502cc),
         new Pair<>(DyeColor.RED, 0xff0000),
         new Pair<>(DyeColor.WHITE, 0xffffff),
         new Pair<>(DyeColor.YELLOW, 0xffff00)
   ));
   
   private static final HashMap<DyeColor, HashMap<Item, Item>> COLORED_ITEMS = new HashMap<>(Map.ofEntries(
         Map.entry(DyeColor.BLACK, new HashMap<>(Map.of(
               Items.GLASS_PANE, Items.BLACK_STAINED_GLASS_PANE,
               Items.WHITE_DYE, Items.BLACK_DYE
         ))),
         Map.entry(DyeColor.BLUE, new HashMap<>(Map.of(
               Items.GLASS_PANE, Items.BLUE_STAINED_GLASS_PANE,
               Items.WHITE_DYE, Items.BLUE_DYE
         ))),
         Map.entry(DyeColor.BROWN, new HashMap<>(Map.of(
               Items.GLASS_PANE, Items.BROWN_STAINED_GLASS_PANE,
               Items.WHITE_DYE, Items.BROWN_DYE
         ))),
         Map.entry(DyeColor.GRAY, new HashMap<>(Map.of(
               Items.GLASS_PANE, Items.GRAY_STAINED_GLASS_PANE,
               Items.WHITE_DYE, Items.GRAY_DYE
         ))),
         Map.entry(DyeColor.CYAN, new HashMap<>(Map.of(
               Items.GLASS_PANE, Items.CYAN_STAINED_GLASS_PANE,
               Items.WHITE_DYE, Items.CYAN_DYE
         ))),
         Map.entry(DyeColor.GREEN, new HashMap<>(Map.of(
               Items.GLASS_PANE, Items.GREEN_STAINED_GLASS_PANE,
               Items.WHITE_DYE, Items.GREEN_DYE
         ))),
         Map.entry(DyeColor.LIGHT_BLUE, new HashMap<>(Map.of(
               Items.GLASS_PANE, Items.LIGHT_BLUE_STAINED_GLASS_PANE,
               Items.WHITE_DYE, Items.LIGHT_BLUE_DYE
         ))),
         Map.entry(DyeColor.LIGHT_GRAY, new HashMap<>(Map.of(
               Items.GLASS_PANE, Items.LIGHT_GRAY_STAINED_GLASS_PANE,
               Items.WHITE_DYE, Items.LIGHT_GRAY_DYE
         ))),
         Map.entry(DyeColor.LIME, new HashMap<>(Map.of(
               Items.GLASS_PANE, Items.LIME_STAINED_GLASS_PANE,
               Items.WHITE_DYE, Items.LIME_DYE
         ))),
         Map.entry(DyeColor.MAGENTA, new HashMap<>(Map.of(
               Items.GLASS_PANE, Items.MAGENTA_STAINED_GLASS_PANE,
               Items.WHITE_DYE, Items.MAGENTA_DYE
         ))),
         Map.entry(DyeColor.ORANGE, new HashMap<>(Map.of(
               Items.GLASS_PANE, Items.ORANGE_STAINED_GLASS_PANE,
               Items.WHITE_DYE, Items.ORANGE_DYE
         ))),
         Map.entry(DyeColor.PINK, new HashMap<>(Map.of(
               Items.GLASS_PANE, Items.PINK_STAINED_GLASS_PANE,
               Items.WHITE_DYE, Items.PINK_DYE
         ))),
         Map.entry(DyeColor.PURPLE, new HashMap<>(Map.of(
               Items.GLASS_PANE, Items.PURPLE_STAINED_GLASS_PANE,
               Items.WHITE_DYE, Items.PURPLE_DYE
         ))),
         Map.entry(DyeColor.RED, new HashMap<>(Map.of(
               Items.GLASS_PANE, Items.RED_STAINED_GLASS_PANE,
               Items.WHITE_DYE, Items.RED_DYE
         ))),
         Map.entry(DyeColor.WHITE, new HashMap<>(Map.of(
               Items.GLASS_PANE, Items.WHITE_STAINED_GLASS_PANE,
               Items.WHITE_DYE, Items.WHITE_DYE
         ))),
         Map.entry(DyeColor.YELLOW, new HashMap<>(Map.of(
               Items.GLASS_PANE, Items.YELLOW_STAINED_GLASS_PANE,
               Items.WHITE_DYE, Items.YELLOW_DYE
         )))
   ));
   
   
   private static final HashMap<String,Item> DYEABLE_MODEL_LIST = new HashMap<>(Map.ofEntries(
         Map.entry("gui/menu_horizontal",Items.GLASS_PANE),
         Map.entry("gui/menu_vertical",Items.GLASS_PANE),
         Map.entry("gui/menu_top",Items.GLASS_PANE),
         Map.entry("gui/menu_bottom",Items.GLASS_PANE),
         Map.entry("gui/menu_left",Items.GLASS_PANE),
         Map.entry("gui/menu_right",Items.GLASS_PANE),
         Map.entry("gui/menu_top_right",Items.GLASS_PANE),
         Map.entry("gui/menu_top_left",Items.GLASS_PANE),
         Map.entry("gui/menu_bottom_left",Items.GLASS_PANE),
         Map.entry("gui/menu_bottom_right",Items.GLASS_PANE),
         Map.entry("gui/menu_right_connector",Items.GLASS_PANE),
         Map.entry("gui/menu_left_connector",Items.GLASS_PANE),
         Map.entry("gui/menu_top_connector",Items.GLASS_PANE),
         Map.entry("gui/menu_bottom_connector",Items.GLASS_PANE),
         Map.entry("gui/page_bg",Items.GLASS_PANE),
         Map.entry("casino_chip",Items.WHITE_DYE)
   ));
   
   public GraphicalItem(String id, Settings settings){
      super(id, settings);
   }
   
   private static HashSet<Item> getAllReplacedItems(){
      HashSet<Item> itemList = new HashSet<>();
      for (HashMap<Item, Item> innerMap : COLORED_ITEMS.values()) {
         itemList.addAll(innerMap.values());
      }
      itemList.add(Items.GLASS_PANE);
      itemList.add(Items.WHITE_DYE);
      itemList.add(Items.LEATHER_CHESTPLATE);
      return itemList;
   }
   
   private ArrayList<Pair<Item, String>> getModels(){
      ArrayList<Pair<Item, String>> arrayList = new ArrayList<>(MODEL_LIST);
      HashSet<Item> itemReplacements = getAllReplacedItems();
      for(String str : DYEABLE_MODEL_LIST.keySet()){
         for(Item itemReplacement : itemReplacements){
            arrayList.add(new Pair<>(itemReplacement,str));
         }
      }
      return arrayList;
   }
   
   @Override
   public @Nullable Identifier getPolymerItemModel(ItemStack stack, PacketContext context){
      String id = ArcanaItem.getStringProperty(stack,GRAPHICS_TAG);
      for(Pair<Item, String> pair : getModels()){
         String[] split = pair.getRight().split("[/\\\\]");
         if(split[split.length-1].equals(id)){
            if(PolymerResourcePackUtils.hasMainPack(context)){
               return ArcanaRegistry.arcanaIdentifier(pair.getRight());
            }else{
               return Registries.ITEM.getKey(pair.getLeft()).get().getValue();
            }
         }
      }
      return Identifier.ofVanilla("barrier");
   }
   
   @Override
   public Item getPolymerItem(ItemStack itemStack, PacketContext context){
      String id = ArcanaItem.getStringProperty(itemStack,GRAPHICS_TAG);
      
      for(Pair<Item, String> pair : MODEL_LIST){
         String[] split = pair.getRight().split("[/\\\\]");
         if(split[split.length-1].equals(id)){
            return pair.getLeft();
         }
      }
      
      for(String str : DYEABLE_MODEL_LIST.keySet()){
         String[] split = str.split("[/\\\\]");
         if(split[split.length-1].equals(id)){
            if(PolymerResourcePackUtils.hasMainPack(context.getPlayer())){
               return Items.LEATHER_CHESTPLATE;
            }else{
               if(itemStack != null && itemStack.contains(DataComponentTypes.DYED_COLOR)){
                  return getItemFromColor(itemStack.get(DataComponentTypes.DYED_COLOR).rgb(), DYEABLE_MODEL_LIST.get(str));
               }
               return Items.WHITE_STAINED_GLASS_PANE;
            }
         }
      }
      
      return Items.BARRIER;
   }
   
   private Item getItemFromColor(int colorRGB, Item keyItem){
      return getColoredItem(getDyeFromColor(colorRGB),keyItem);
   }
   
   private Item getColoredItem(DyeColor color, Item keyItem){
      HashMap<Item,Item> colorMap = COLORED_ITEMS.get(color);
      if(colorMap == null) return Items.GLASS_PANE;
      return colorMap.getOrDefault(keyItem,Items.GLASS_PANE);
   }
   
   private DyeColor getDyeFromColor(int colorRGB){
      DyeColor closest = DyeColor.WHITE;
      double cDist = Integer.MAX_VALUE;
      for(Pair<DyeColor, Integer> pair : DYED_REPLACEMENTS){
         int repColor = pair.getRight();
         double rDist = (((repColor>>16)&0xFF)-((colorRGB>>16)&0xFF))*0.30;
         double gDist = (((repColor>>8)&0xFF)-((colorRGB>>8)&0xFF))*0.59;
         double bDist = ((repColor&0xFF)-(colorRGB&0xFF))*0.11;
         double dist = rDist*rDist + gDist*gDist + bDist*bDist;
         if(dist < cDist){
            cDist = dist;
            closest = pair.getLeft();
         }
      }
      return closest;
   }
   
   public static ItemStack with(String id){
      ItemStack stack = new ItemStack(ArcanaRegistry.GRAPHICAL_ITEM);
      ArcanaItem.putProperty(stack,GRAPHICS_TAG,id);
      return stack;
   }
   
   public static ItemStack with(GraphicItems id){
      ItemStack stack = new ItemStack(ArcanaRegistry.GRAPHICAL_ITEM);
      ArcanaItem.putProperty(stack,GRAPHICS_TAG,id.id);
      return stack;
   }
   
   public static ItemStack withColor(GraphicItems id, int color){
      ItemStack stack = new ItemStack(ArcanaRegistry.GRAPHICAL_ITEM);
      ArcanaItem.putProperty(stack,GRAPHICS_TAG,id.id);
      stack.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(color, false));
      return stack;
   }
}

