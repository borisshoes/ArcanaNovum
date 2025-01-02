package net.borisshoes.arcananovum.utils;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.StringIdentifiable;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class ConfigUtils {
   public List<IConfigValue> values;
   private final File file;
   private final Logger logger;
   
   public ConfigUtils(File file, Logger logger, List<IConfigValue> values){
      this.file = file;
      this.logger = logger;
      this.values = values;
      this.read();
      this.save();
   }
   
   public void read(){
      try(BufferedReader input = new BufferedReader(new InputStreamReader(new FileInputStream(file)))){
         logger.debug("Reading Arcana Novum config...");
         
         for(IConfigValue value : this.values){
            value.value = value.defaultValue;
         }
         
         while(input.ready()){
            String configLine = input.readLine();
            String trimmed = configLine.trim();
            if(trimmed.isEmpty()) continue;
            char firstChar = trimmed.charAt(0);
            
            if(firstChar == '!' || firstChar == '#') continue;
            if(!configLine.contains("=")) continue;
            
            int splitIndex = configLine.indexOf('=');
            String valueName = configLine.substring(0, splitIndex);
            String valueValue = configLine.substring(splitIndex + 1);
            
            for(IConfigValue value : this.values){
               if(!valueName.equals(value.name)) continue;
               Object defaultValue = value.defaultValue;
               try{
                  value.value = value.getFromString(valueValue);
               }catch(Exception e){
                  value.value = defaultValue;
               }
            }
         }
      }catch(FileNotFoundException ignored){
         logger.debug("Initialising Arcana Novum config...");
         this.values.forEach(value -> value.value = value.defaultValue);
      }catch(IOException e){
         logger.fatal("Failed to load Arcana Novum config file!");
         e.printStackTrace();
      }catch(Exception e){
         logger.fatal("Failed to parse Arcana Novum config");
         e.printStackTrace();
      }
   }
   
   public void save(){
      logger.debug("Updating Arcana Novum config...");
      try(BufferedWriter output = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)))){
         output.write("# Arcana Novum Configuration File" + " | " + new Date());
         output.newLine();
         output.newLine();
         
         for(IConfigValue value : this.values){
            if(value.comment != null){
               output.write("# "+value.comment);
               output.newLine();
            }
            output.write(value.name + " = " + value.value);
            output.newLine();
         }
      }catch(IOException e){
         logger.fatal("Failed to save Arcana Novum config file!");
         e.printStackTrace();
      }
   }
   
   public LiteralArgumentBuilder<ServerCommandSource> generateCommand(){
      LiteralArgumentBuilder<ServerCommandSource> out =
            literal("arcana").then(literal("config").requires(source -> source.hasPermissionLevel(4))
                  .executes(ctx -> {
                     values.stream().filter(v -> v.command != null).forEach(value ->
                           ctx.getSource().sendFeedback(()->MutableText.of(new TranslatableTextContent(value.command.getterText, null, new Object[] {String.valueOf(value.value)})), false));
                     return 1;
                  }));
      values.stream().filter(v -> v.command != null).forEach(value ->
            out.then(literal("config").then(literal(value.name)
                  .executes(ctx -> {
                     ctx.getSource().sendFeedback(()->MutableText.of(new TranslatableTextContent(value.command.getterText, null, new Object[] {String.valueOf(value.value)})), false);
                     return 1;
                  })
                  .then(argument(value.name, value.getArgumentType()).suggests(value::getSuggestions)
                        .executes(ctx -> {
                           value.value = value.parseArgumentValue(ctx);
                           ((CommandContext<ServerCommandSource>) ctx).getSource().sendFeedback(()->MutableText.of(new TranslatableTextContent(value.command.setterText, null, new Object[] {String.valueOf(value.value)})), true);
                           this.save();
                           return 1;
                        })))));
      return out;
   }
   
   public Object getValue(String name){
      return values.stream().filter(value -> value.name.equals(name)).findFirst().map(iConfigValue -> iConfigValue.value).orElse(null);
   }
   
   public abstract static class IConfigValue<T>{
      protected final T defaultValue;
      protected final String name;
      protected final String comment;
      protected final Command command;
      protected T value;
      
      public IConfigValue(@NotNull String name, T defaultValue, @Nullable String comment, @Nullable Command command){
         this.name = name;
         this.defaultValue = defaultValue;
         this.comment = comment;
         this.command = command;
      }
      
      public String getName(){
         return name;
      }
      
      public abstract T getFromString(String value);
      
      public abstract ArgumentType<?> getArgumentType();
      
      public abstract T parseArgumentValue(CommandContext<ServerCommandSource> ctx);
      
      public abstract CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> ctx, SuggestionsBuilder builder);
   }
   
   public static class IntegerConfigValue extends IConfigValue<Integer> {
      protected final int defaultValue;
      private final IntLimits limits;
      
      public IntegerConfigValue(@NotNull String name, Integer defaultValue, IntLimits limits, @Nullable String comment, @Nullable Command command){
         super(name, defaultValue, comment, command);
         this.defaultValue = defaultValue;
         this.limits = limits;
      }
      
      public IntegerConfigValue(@NotNull String name, Integer defaultValue, IntLimits limits, @Nullable Command command){
         this(name, defaultValue, limits, null, command);
      }
      
      @Override
      public Integer getFromString(String value){
         return Integer.parseInt(value);
      }
      
      @Override
      public ArgumentType<Integer> getArgumentType(){
         return IntegerArgumentType.integer(limits.min, limits.max);
      }
      
      @Override
      public Integer parseArgumentValue(CommandContext<ServerCommandSource> ctx){
         return IntegerArgumentType.getInteger(ctx, name);
      }
      
      public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder){
         if(limits.max - limits.min < 10000){
            String start = builder.getRemaining().toLowerCase(Locale.ROOT);
            Set<String> nums = new HashSet<>();
            for(int i = limits.min; i <= limits.max; i++){
               nums.add(String.valueOf(i));
            }
            nums.stream().filter(s -> s.startsWith(start)).forEach(builder::suggest);
         }
         return builder.buildFuture();
      }
      
      public static class IntLimits {
         int min = Integer.MIN_VALUE, max = Integer.MAX_VALUE;
         
         public IntLimits(){}
         
         public IntLimits(int min){
            this.min = min;
         }
         
         public IntLimits(int min, int max){
            this.min = min;
            this.max = max;
         }
      }
   }
   
   public static class BooleanConfigValue extends IConfigValue<Boolean> {
      protected final boolean defaultValue;
      
      public BooleanConfigValue(@NotNull String name, boolean defaultValue, @Nullable String comment, @Nullable Command command){
         super(name, defaultValue, comment, command);
         this.defaultValue = defaultValue;
      }
      
      @Override
      public Boolean getFromString(String value){
         return Boolean.parseBoolean(value);
      }
      
      @Override
      public ArgumentType<Boolean> getArgumentType(){
         return BoolArgumentType.bool();
      }
      
      @Override
      public Boolean parseArgumentValue(CommandContext<ServerCommandSource> ctx){
         return BoolArgumentType.getBool(ctx, name);
      }
      
      public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder){
         Set<String> options = new HashSet<>();
         options.add("true");
         options.add("false");
         String start = builder.getRemaining().toLowerCase(Locale.ROOT);
         options.stream().filter(s -> s.toLowerCase(Locale.ROOT).startsWith(start)).forEach(builder::suggest);
         return builder.buildFuture();
      }
   }
   
   public static class StringConfigValue extends IConfigValue<String> {
      protected final String defaultValue;
      protected final String[] options;
      
      public StringConfigValue(@NotNull String name, String defaultValue, @Nullable String comment, @Nullable Command command, @Nullable String... options){
         super(name, defaultValue, comment, command);
         this.defaultValue = defaultValue;
         this.options = options;
      }
      
      @Override
      public String getFromString(String value){
         return value;
      }
      
      @Override
      public ArgumentType<String> getArgumentType(){
         return StringArgumentType.greedyString();
      }
      
      @Override
      public String parseArgumentValue(CommandContext<ServerCommandSource> ctx){
         return StringArgumentType.getString(ctx, name);
      }
      
      public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder){
         String start = builder.getRemaining().toLowerCase(Locale.ROOT);
         Arrays.stream(options).filter(s -> s.toLowerCase(Locale.ROOT).startsWith(start)).forEach(builder::suggest);
         return builder.buildFuture();
      }
   }
   
   public static class EnumConfigValue<K extends Enum<K> & StringIdentifiable> extends IConfigValue<K>{
      protected final K defaultValue;
      private final Class<K> typeClass;
      
      public EnumConfigValue(@NotNull String name, K defaultValue, @Nullable String comment, @Nullable Command command, Class<K> typeClass){
         super(name, defaultValue, comment, command);
         this.defaultValue = defaultValue;
         this.typeClass = typeClass;
      }
      
      public EnumConfigValue(@NotNull String name, K defaultValue, @Nullable Command command, Class<K> typeClass){
         this(name, defaultValue, null, command, typeClass);
      }
      
      @Override
      public K getFromString(String value){
         for(K k : EnumSet.allOf(typeClass)){
            if(k.asString().equalsIgnoreCase(value)){
               return k;
            }
         }
         throw new IllegalArgumentException("Could not map "+ value +" to enum "+typeClass.getName());
      }
      
      @Override
      public ArgumentType<String> getArgumentType(){
         return StringArgumentType.string();
      }
      
      @Override
      public K parseArgumentValue(CommandContext<ServerCommandSource> ctx){
         String parsedString = StringArgumentType.getString(ctx, name);
         return K.valueOf(this.typeClass,parsedString);
      }
      
      public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder){
         Set<String> options = new HashSet<>();
         for(K k : EnumSet.allOf(typeClass)){
            options.add(k.asString());
         }
         String start = builder.getRemaining().toLowerCase(Locale.ROOT);
         options.stream().filter(s -> s.toLowerCase(Locale.ROOT).startsWith(start)).forEach(builder::suggest);
         return builder.buildFuture();
      }
   }
   
   public static class Command {
      protected String setterText;
      protected String getterText;
      protected String errorText;
      
      public Command(String getterText, String setterText, @Nullable String errorText){
         this.getterText = getterText;
         this.setterText = setterText;
         this.errorText = errorText;
      }
      
      public Command(String getterText, String setterText){
         this(getterText, setterText, null);
      }
   }
   
   public static <K extends Enum<K> & StringIdentifiable> CompletableFuture<Suggestions> getEnumSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder, Class<K> enumClass){
      Set<String> options = new HashSet<>();
      for(K k : EnumSet.allOf(enumClass)){
         options.add(k.asString());
      }
      String start = builder.getRemaining().toLowerCase(Locale.ROOT);
      options.stream().filter(s -> s.toLowerCase(Locale.ROOT).startsWith(start)).forEach(builder::suggest);
      return builder.buildFuture();
   }
   
   public static <K extends Enum<K> & StringIdentifiable> K parseEnum(String string, Class<K> enumClass){
      Optional<K> opt = EnumSet.allOf(enumClass).stream().filter(en -> en.asString().equals(string)).findFirst();
      return opt.orElse(null);
   }
}
