package net.borisshoes.arcananovum.augments;

import net.borisshoes.arcananovum.core.MagicItem;
import net.borisshoes.arcananovum.utils.MagicRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;

public class ArcanaAugment implements Comparable<ArcanaAugment>{
   public final String name;
   public final String id;
   private final ItemStack displayItem;
   private final MagicItem magicItem;
   private final String[] description;
   private final MagicRarity[] tiers;
   
   protected ArcanaAugment(String name, String id, ItemStack displayItem, MagicItem magicItem, String[] description, MagicRarity[] tiers){
      this.name = name;
      this.id = id;
      this.displayItem = displayItem;
      this.magicItem = magicItem;
      this.description = description;
      this.tiers = tiers;
   }
   
   public ItemStack getDisplayItem(){
      return displayItem;
   }
   
   public MagicItem getMagicItem(){
      return magicItem;
   }
   
   public String[] getDescription(){
      return description;
   }
   
   public MutableText getTierDisplay(){
      MutableText text = Text.literal("")
            .append(Text.literal(tiers.length +"").formatted(Formatting.AQUA))
            .append(Text.literal(tiers.length != 1 ? " Levels (" : " Level (").formatted(Formatting.DARK_AQUA));
   
      for(int i = 0; i < tiers.length; i++){
         MagicRarity tierRarity = tiers[i];
         text.append(Text.literal("❖").formatted(MagicRarity.getColor(tierRarity)));
      }
      text.append(Text.literal(")").formatted(Formatting.DARK_AQUA));
      
      return text;
   }
   
   public MagicRarity[] getTiers(){
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
