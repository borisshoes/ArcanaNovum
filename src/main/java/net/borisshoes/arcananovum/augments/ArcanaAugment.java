package net.borisshoes.arcananovum.augments;

import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.utils.ArcanaRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class ArcanaAugment implements Comparable<ArcanaAugment>{
   public final String name;
   public final String id;
   private final ItemStack displayItem;
   private final ArcanaItem arcanaItem;
   private final String[] description;
   private final ArcanaRarity[] tiers;
   
   protected ArcanaAugment(String name, String id, ItemStack displayItem, ArcanaItem arcanaItem, String[] description, ArcanaRarity[] tiers){
      this.name = name;
      this.id = id;
      this.displayItem = displayItem;
      this.arcanaItem = arcanaItem;
      this.description = description;
      this.tiers = tiers;
   }
   
   public String getTranslationKey(){
      return "augment."+MOD_ID+".name."+this.id;
   }
   
   public MutableText getTranslatedName(){
      return Text.translatableWithFallback(getTranslationKey(),name);
   }
   
   public ItemStack getDisplayItem(){
      return displayItem;
   }
   
   public ArcanaItem getArcanaItem(){
      return arcanaItem;
   }
   
   public String[] getDescription(){
      return description;
   }
   
   public MutableText getTierDisplay(){
      MutableText text = Text.literal("")
            .append(Text.literal(tiers.length +"").formatted(Formatting.AQUA))
            .append(Text.literal(tiers.length != 1 ? " Levels (" : " Level (").formatted(Formatting.DARK_AQUA));
   
      for(int i = 0; i < tiers.length; i++){
         ArcanaRarity tierRarity = tiers[i];
         text.append(Text.literal("❖").formatted(ArcanaRarity.getColor(tierRarity)));
      }
      text.append(Text.literal(")").formatted(Formatting.DARK_AQUA));
      
      return text;
   }
   
   public ArcanaRarity[] getTiers(){
      return tiers;
   }
   
   public boolean matches(ArcanaAugment other){
      return other.id.matches(this.id);
   }
   
   @Override
   public int compareTo(@NotNull ArcanaAugment other){
      return other.id.compareTo(this.id);
   }
}
