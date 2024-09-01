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
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class GraphicalItem extends NormalPolymerItem {
   
   public static final String GRAPHICS_TAG = "graphic_id";
   
   private static final ArrayList<Pair<Item,String>> MODEL_LIST = new ArrayList<>(Arrays.asList(
         new Pair<>(Items.STRUCTURE_VOID,"item/gui/confirm"),
         new Pair<>(Items.BARRIER,"item/gui/cancel"),
         new Pair<>(Items.SPECTRAL_ARROW,"item/gui/right_arrow"),
         new Pair<>(Items.SPECTRAL_ARROW,"item/gui/left_arrow"),
         new Pair<>(Items.NETHER_STAR,"item/gui/sort"),
         new Pair<>(Items.HOPPER,"item/gui/filter"),
         new Pair<>(Items.BLACK_STAINED_GLASS_PANE,"item/gui/black"),
         new Pair<>(Items.NETHER_STAR,"item/gui/star"),
         new Pair<>(Items.GRAY_STAINED_GLASS_PANE,"item/gui/gas"),
         new Pair<>(Items.ORANGE_STAINED_GLASS_PANE,"item/gui/plasma"),
         new Pair<>(Items.ENDER_PEARL,"item/gui/black_hole"),
         new Pair<>(Items.BLAZE_POWDER,"item/gui/nova"),
         new Pair<>(Items.MAGMA_CREAM,"item/gui/supernova"),
         new Pair<>(Items.ENDER_EYE,"item/gui/quasar"),
         new Pair<>(Items.END_CRYSTAL,"item/gui/pulsar"),
         new Pair<>(Items.PURPLE_STAINED_GLASS_PANE,"item/gui/nebula"),
         new Pair<>(Items.HEAVY_CORE,"item/gui/planet"),
         new Pair<>(Items.KNOWLEDGE_BOOK,"item/gui/transmutation_book")
   ));
   
   private static final ArrayList<Pair<Item,Integer>> DYED_REPLACEMENTS = new ArrayList<>(Arrays.asList(
         new Pair<>(Items.BLACK_STAINED_GLASS_PANE,0x000000),
         new Pair<>(Items.BLUE_STAINED_GLASS_PANE,0x0000ff),
         new Pair<>(Items.BROWN_STAINED_GLASS_PANE,0x6b5341),
         new Pair<>(Items.GRAY_STAINED_GLASS_PANE,0x5c5c5c),
         new Pair<>(Items.CYAN_STAINED_GLASS_PANE,0x168e94),
         new Pair<>(Items.GREEN_STAINED_GLASS_PANE,0x04753a),
         new Pair<>(Items.LIGHT_BLUE_STAINED_GLASS_PANE,0x5ad2fa),
         new Pair<>(Items.LIGHT_GRAY_STAINED_GLASS_PANE,0xc7c7c7),
         new Pair<>(Items.LIME_STAINED_GLASS_PANE,0x4ded0e),
         new Pair<>(Items.MAGENTA_STAINED_GLASS_PANE,0xb306c9),
         new Pair<>(Items.ORANGE_STAINED_GLASS_PANE,0xff8800),
         new Pair<>(Items.PINK_STAINED_GLASS_PANE,0xff7dde),
         new Pair<>(Items.PURPLE_STAINED_GLASS_PANE,0x8502cc),
         new Pair<>(Items.RED_STAINED_GLASS_PANE,0xff000),
         new Pair<>(Items.WHITE_STAINED_GLASS_PANE,0xffffff),
         new Pair<>(Items.YELLOW_STAINED_GLASS_PANE,0xffff00)
   ));
   
   private static final ArrayList<String> DYEABLE_MODEL_LIST = new ArrayList<>(Arrays.asList(
         "item/gui/menu_horizontal",
         "item/gui/menu_vertical",
         "item/gui/menu_top",
         "item/gui/menu_bottom",
         "item/gui/menu_left",
         "item/gui/menu_right",
         "item/gui/menu_top_right",
         "item/gui/menu_top_left",
         "item/gui/menu_bottom_left",
         "item/gui/menu_bottom_right",
         "item/gui/menu_right_connector",
         "item/gui/menu_left_connector",
         "item/gui/menu_top_connector",
         "item/gui/menu_bottom_connector",
         "item/gui/page_bg"
   ));
   
   public GraphicalItem(Settings settings){
      super(settings);
   }
   
   @Override
   public ArrayList<Pair<Item, String>> getModels(){
      ArrayList<Pair<Item, String>> arrayList = new ArrayList<>(MODEL_LIST);
      for(String str : DYEABLE_MODEL_LIST){
         for(Pair<Item, Integer> pair : DYED_REPLACEMENTS){
            arrayList.add(new Pair<>(pair.getLeft(),str));
         }
         arrayList.add(new Pair<>(Items.GLASS_PANE,str));
         arrayList.add(new Pair<>(Items.LEATHER_CHESTPLATE,str));
      }
      return arrayList;
   }
   
   @Override
   public int getPolymerCustomModelData(ItemStack itemStack, @Nullable ServerPlayerEntity player){
      String id = ArcanaItem.getStringProperty(itemStack,GRAPHICS_TAG);
      for(Pair<Item, String> pair : getModels()){
         String[] split = pair.getRight().split("[/\\\\]");
         if(split[split.length-1].equals(id) && ArcanaRegistry.MODELS.containsId(Identifier.of(MOD_ID,pair.getRight()))){
            return ArcanaRegistry.getModelData(pair.getRight()).value();
         }
      }
      return -1;
   }
   
   @Override
   public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player){
      String id = ArcanaItem.getStringProperty(itemStack,GRAPHICS_TAG);
      
      for(Pair<Item, String> pair : MODEL_LIST){
         String[] split = pair.getRight().split("[/\\\\]");
         if(split[split.length-1].equals(id)){
            return pair.getLeft();
         }
      }
      
      for(String str : DYEABLE_MODEL_LIST){
         String[] split = str.split("[/\\\\]");
         if(split[split.length-1].equals(id)){
            if(PolymerResourcePackUtils.hasMainPack(player)){
               return Items.LEATHER_CHESTPLATE;
            }else{
               if(itemStack != null && itemStack.contains(DataComponentTypes.DYED_COLOR)){
                  return getItemFromColor(itemStack.get(DataComponentTypes.DYED_COLOR).rgb());
               }
               return Items.WHITE_STAINED_GLASS_PANE;
            }
         }
      }
      
      return Items.BARRIER;
   }
   
   private Item getItemFromColor(int colorRGB){
      Item closest = Items.GLASS_PANE;
      double cDist = Integer.MAX_VALUE;
      for(Pair<Item, Integer> pair : DYED_REPLACEMENTS){
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

