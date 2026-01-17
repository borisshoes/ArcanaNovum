package net.borisshoes.arcananovum.augments;

import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class ArcanaAugment implements Comparable<ArcanaAugment>{
   public final String id;
   private final ItemStack displayItem;
   private final ArcanaItem arcanaItem;
   private final ArcanaRarity[] tiers;
   
   protected ArcanaAugment(String id, ItemStack displayItem, ArcanaItem arcanaItem, ArcanaRarity... tiers){
      this.id = id;
      this.displayItem = displayItem;
      this.arcanaItem = arcanaItem;
      this.tiers = tiers;
   }
   
   public String getTranslationKey(){
      return "augment."+MOD_ID+".name."+this.id;
   }
   
   public String getDescriptionTranslationKey(){
      return "augment."+MOD_ID+".description."+this.id;
   }
   
   public MutableComponent getTranslatedName(){
      return Component.translatable(getTranslationKey());
   }
   
   public ItemStack getDisplayItem(){
      return displayItem;
   }
   
   public ArcanaItem getArcanaItem(){
      return arcanaItem;
   }
   
   public List<Component> getDescription(){
      String fullText = Component.translatable(getDescriptionTranslationKey()).getString();
      String[] lines = fullText.split("\n");
      List<Component> components = new ArrayList<>();
      for(String line : lines){
         components.add(Component.literal(line));
      }
      return components;
   }
   
   public MutableComponent getTierDisplay(){
      MutableComponent text = Component.literal("")
            .append(Component.literal(tiers.length +"").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(tiers.length != 1 ? " Levels (" : " Level (").withStyle(ChatFormatting.DARK_AQUA));
   
      for(int i = 0; i < tiers.length; i++){
         ArcanaRarity tierRarity = tiers[i];
         text.append(Component.literal("❖").withStyle(ArcanaRarity.getColor(tierRarity)));
      }
      text.append(Component.literal(")").withStyle(ChatFormatting.DARK_AQUA));
      
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
