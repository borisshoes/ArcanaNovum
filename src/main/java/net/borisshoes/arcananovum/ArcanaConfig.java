package net.borisshoes.arcananovum;

import net.borisshoes.arcananovum.utils.ConfigUtils;
import net.borisshoes.borislib.utils.TextUtils;

import java.util.Objects;

import static net.borisshoes.arcananovum.ArcanaNovum.log;

public class ArcanaConfig{
   
   public static int getInt(ArcanaConfig.ConfigSetting<?> setting){
      try{
         return (int) ArcanaNovum.CONFIG.getValue(setting.getName());
      }catch(Exception e){
         log(3,"Failed to get Integer config for "+setting.getName());
         log(3,e.toString());
      }
      return 0;
   }
   
   public static boolean getBoolean(ArcanaConfig.ConfigSetting<?> setting){
      try{
         return (boolean) ArcanaNovum.CONFIG.getValue(setting.getName());
      }catch(Exception e){
         log(3,"Failed to get Boolean config for "+setting.getName());
         log(3,e.toString());
      }
      return false;
   }
   
   public record NormalConfigSetting<T>(ConfigUtils.IConfigValue<T> setting) implements ConfigSetting<T>{
      public NormalConfigSetting(ConfigUtils.IConfigValue<T> setting){
         this.setting = Objects.requireNonNull(setting);
      }
      
      public ConfigUtils.IConfigValue<T> makeConfigValue(){
         return setting;
      }
      
      public String getId(){
         return TextUtils.camelToSnake(setting.getName());
      }
      
      public String getName(){
         return setting.getName();
      }
   }
   
   public record XPConfigSetting(Integer defaultValue, String name, String description, boolean capConfig) implements ConfigSetting<Integer>{
      public XPConfigSetting(int defaultValue, String name, String description){
         this(defaultValue,name,description,false);
      }
      
      public XPConfigSetting(Integer defaultValue, String name, String description, boolean capConfig){
         this.defaultValue = Objects.requireNonNullElse(defaultValue, 1);
         this.name = Objects.requireNonNullElse(name, "");
         this.description = Objects.requireNonNullElse(description, "");
         this.capConfig = capConfig;
      }
      
      public String configGetSet(boolean setter){
         return (capConfig ? "Max XP for " : "XP for ") + description + (setter ? " is now: %s" : " is: %s");
      }
      
      public ConfigUtils.IConfigValue<Integer> makeConfigValue(){
         return new ConfigUtils.IntegerConfigValue("xp"+name, defaultValue, new ConfigUtils.IntegerConfigValue.IntLimits(0,100000), (capConfig ? "The max XP given for " : "The XP given for ") + description,
               new ConfigUtils.Command(configGetSet(false), configGetSet(true)));
      }
      
      public String getId(){
         return TextUtils.camelToSnake("xp"+name);
      }
      
      public String getName(){
         return "xp"+name;
      }
   }
   
   public interface ConfigSetting<T>{
      ConfigUtils.IConfigValue<T> makeConfigValue();
      String getId();
      String getName();
   }
}
