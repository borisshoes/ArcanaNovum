package net.borisshoes.arcananovum.augments;

import net.borisshoes.arcananovum.ArcanaConfig;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.utils.ConfigUnits;
import net.borisshoes.borislib.config.IConfigSetting;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class ArcanaAugment implements Comparable<ArcanaAugment> {
   public final String id;
   private final ItemStack displayItem;
   private final ArcanaItem arcanaItem;
   private final ArcanaRarity[] tiers;
   private Tuple<IConfigSetting<?>, ConfigUnits>[] relatedConfigs;
   
   protected ArcanaAugment(String id, ItemStack displayItem, ArcanaItem arcanaItem, ArcanaRarity... tiers){
      this.id = id;
      this.displayItem = displayItem;
      this.arcanaItem = arcanaItem;
      this.tiers = tiers;
   }
   
   @SafeVarargs
   public final ArcanaAugment setRelatedConfigs(Tuple<IConfigSetting<?>, ConfigUnits>... configs){
      relatedConfigs = configs;
      return this;
   }
   
   public String getTranslationKey(){
      return "augment." + MOD_ID + ".name." + this.id;
   }
   
   public String getDescriptionTranslationKey(){
      return "augment." + MOD_ID + ".description." + this.id;
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
      Object[] args = new Component[0];
      if(relatedConfigs != null && relatedConfigs.length > 0){
         args = new Component[relatedConfigs.length];
         for(int i = 0; i < relatedConfigs.length; i++){
            IConfigSetting<?> setting = relatedConfigs[i].getA();
            ConfigUnits displayUnits = relatedConfigs[i].getB();
            ConfigUnits nativeUnits = ArcanaConfig.CONFIG_UNITS.getOrDefault(ArcanaRegistry.arcanaId(setting.getId()), ConfigUnits.NONE);
            ConfigUnits targetUnits;
            double conversionFactor;
            if(nativeUnits.getBase() == displayUnits.getBase()){
               targetUnits = displayUnits;
               conversionFactor = nativeUnits.toOther(displayUnits);
            }else{
               targetUnits = nativeUnits;
               conversionFactor = 1.0;
            }
            
            Object rawValue = ArcanaNovum.CONFIG.getValue(setting);
            if(rawValue instanceof List<?> list){
               double[] values = new double[Math.max(0, list.size() - 1)];
               for(int j = 1; j < list.size(); j++){
                  if(list.get(j) instanceof Number n){
                     values[j - 1] = n.doubleValue() * conversionFactor;
                  }
               }
               args[i] = targetUnits.makeValue(values, 4);
            }else if(rawValue instanceof Number n){
               args[i] = targetUnits.makeValue(n.doubleValue() * conversionFactor, 4);
            }else{
               args[i] = Component.literal("?");
            }
         }
      }
      
      String fullText = Component.translatable(getDescriptionTranslationKey(), args).getString();
      String[] lines = fullText.split("\n");
      List<Component> components = new ArrayList<>();
      for(String line : lines){
         components.add(Component.literal(line));
      }
      return components;
   }
   
   public MutableComponent getTierDisplay(){
      MutableComponent text = Component.literal("")
            .append(Component.literal(tiers.length + "").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(tiers.length != 1 ? " Levels (" : " Level (").withStyle(ChatFormatting.DARK_AQUA));
      
      for(int i = 0; i < tiers.length; i++){
         ArcanaRarity tierRarity = tiers[i];
         text.append(Component.literal("❖").withStyle(ArcanaRarity.getColor(tierRarity)));
      }
      text.append(Component.literal(")").withStyle(ChatFormatting.DARK_AQUA));
      
      return text;
   }
   
   public Tuple<IConfigSetting<?>, ConfigUnits>[] getRelatedConfigs(){
      return relatedConfigs;
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
