package net.borisshoes.arcananovum.utils;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Tuple;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public enum ConfigUnits {
   NONE("none","",1),
   TICKS("ticks","time",1),
   QUARTER_SECONDS("quarter_seconds","time",5),
   SECONDS("seconds","time",20),
   MINUTES("minutes","time",1200),
   HOURS("hours","time",72000),
   DAYS("days","time",1728000),
   BLOCKS("blocks","length",1),
   KILOBLOCKS("kiloblocks","length",1000),
   CENTIBLOCKS("centiblocks","length",0.01),
   HP("hp","health",1),
   HEARTS("hearts","health",2),
   MULTIPLIER("multiplier","multiplier",1, true),
   PERCENT("percent","multiplier",0.01, true),
   DEGREES("degrees","angle",1),
   RADIANS("radians","angle",57.2957795),
   BLOCKS_PER_SECOND("blocks_per_second","velocity",0.05),
   BLOCKS_PER_TICK("blocks_per_tick","velocity",1),
   HP_PER_SECOND("health_per_second","health_over_time",0.05),
   HP_PER_TICK("health_per_tick","health_over_time",1),
   HEARTS_PER_SECOND("hearts_per_second","health_over_time",0.1),
   ARCANA_XP("arcana_xp","arcana_xp",1);
   
   private final boolean isBase;
   private final boolean perValueSuffix;
   private final double toBaseMultiplier;
   private final String type;
   private final String name;
   
   ConfigUnits(String name, String type, double multiplier){
      this.name = name;
      this.type = type;
      this.toBaseMultiplier = multiplier;
      this.isBase = multiplier == 1.0;
      this.perValueSuffix = false;
   }
   
   ConfigUnits(String name, String type, double multiplier, boolean perValueSuffix){
      this.name = name;
      this.type = type;
      this.toBaseMultiplier = multiplier;
      this.isBase = multiplier == 1.0;
      this.perValueSuffix = perValueSuffix;
   }
   
   public Component getName(){
      return Component.translatable("text.arcananovum.units."+name);
   }
   
   public Component makeValue(double value, int decimalPlaces) {
      DecimalFormat df = new DecimalFormat("#,##0." + "#".repeat(Math.max(0, decimalPlaces)), DecimalFormatSymbols.getInstance(Locale.ROOT));
      return Component.literal(df.format(value)).append(getName());
   }
   
   public Component makeValue(double[] values, int decimalPlaces) {
      DecimalFormat df = new DecimalFormat("#,##0." + "#".repeat(Math.max(0, decimalPlaces)), DecimalFormatSymbols.getInstance(Locale.ROOT));
      MutableComponent comp = Component.literal("");
      int size = values.length;
      if(perValueSuffix){
         for(int i = 0; i < size; i++){
            double value = values[i];
            comp.append(df.format(value));
            comp.append(getName());
            if(i != size - 1){
               comp.append("/");
            }
         }
      }else{
         for(int i = 0; i < size; i++){
            double value = values[i];
            comp.append(df.format(value));
            if(i != size - 1){
               comp.append("/");
            }
         }
         comp.append(getName());
      }
      return comp;
   }
   
   
   public ConfigUnits getBase(){
      for(ConfigUnits unit : values()){
         if(unit.type.equals(this.type) && unit.isBase){
            return unit;
         }
      }
      return this;
   }
   
   public double toBase(double amount){
      return amount * toBaseMultiplier;
   }
   
   public double toOther(ConfigUnits units){
      return this.toBaseMultiplier / units.toBaseMultiplier;
   }
}
