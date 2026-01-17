package net.borisshoes.arcananovum.recipes.arcana;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Either;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.borislib.utils.MinecraftUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Tuple;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.*;
import java.util.function.Predicate;

public class ArcanaIngredient {
   protected final List<Either<Item, TagKey<Item>>> acceptedItems = new ArrayList<>();
   protected final int count;
   protected Predicate<ItemStack> itemPredicate;
   protected final ItemStack exampleStack;
   protected final boolean ignoresResourceful;
   protected final List<Tuple<ResourceKey<Enchantment>,Integer>> enchantments = new ArrayList<>();
   protected Holder<Potion> potion;
   protected final List<MobEffectInstance> effects = new ArrayList<>();
   
   public static final ArcanaIngredient EMPTY = new ArcanaIngredient(List.of(Either.left(Items.AIR)),0, true, ItemStack.EMPTY, ItemStack::isEmpty);
   
   public ArcanaIngredient(Item itemType, int count, boolean ignoresResourceful){
      this.count = count;
      this.ignoresResourceful = ignoresResourceful;
      this.exampleStack = new ItemStack(itemType,count);
      this.acceptedItems.add(Either.left(itemType));
      this.itemPredicate = (stack) -> acceptedItems.stream().anyMatch(e -> (e.left().isPresent() && stack.is(e.left().get())) || (e.right().isPresent() && stack.is(e.right().get())));
   }
   
   public ArcanaIngredient(Item itemType, int count){
      this.count = count;
      this.exampleStack = new ItemStack(itemType,count);
      this.acceptedItems.add(Either.left(itemType));
      this.itemPredicate = (stack) -> acceptedItems.stream().anyMatch(e -> (e.left().isPresent() && stack.is(e.left().get())) || (e.right().isPresent() && stack.is(e.right().get())));
      this.ignoresResourceful = false;
   }
   
   private ArcanaIngredient(List<Either<Item, TagKey<Item>>> acceptedItems, int count, boolean ignoresResourceful, ItemStack exampleStack, Predicate<ItemStack> itemPredicate){
      this.acceptedItems.addAll(acceptedItems);
      this.count = count;
      this.exampleStack = exampleStack.copyWithCount(count);
      this.itemPredicate = itemPredicate;
      this.ignoresResourceful = ignoresResourceful;
   }
   
   public ArcanaIngredient copyWithCount(int newCount){
      return new ArcanaIngredient(acceptedItems,newCount,ignoresResourceful,exampleStack,itemPredicate);
   }
   
   public ArcanaIngredient withEnchantments(ArcanaIngredient.EnchantmentEntry... enchantments){
      this.itemPredicate = this.itemPredicate.and((stack) -> {
         ItemEnchantments enchants = EnchantmentHelper.getEnchantmentsForCrafting(stack);
         for(EnchantmentEntry enchantment : enchantments){
            if(enchants.getLevel(enchantment.enchantment()) < enchantment.level()) return false;
         }
         return true;
      });
      
      for(EnchantmentEntry enchantment : enchantments){
         this.exampleStack.enchant(enchantment.enchantment(),enchantment.level());
         this.enchantments.add(new Tuple<>(enchantment.enchantmentKey(),enchantment.level()));
      }
      
      return this;
   }
   
   public ArcanaIngredient withPotion(Holder<Potion> potion){
      this.itemPredicate = this.itemPredicate.and((stack) -> {
         PotionContents pots = stack.get(DataComponents.POTION_CONTENTS);
         for(MobEffectInstance reqEffect : potion.value().getEffects()){
            boolean found = false;
            for(MobEffectInstance effect : pots.getAllEffects()){
               if(reqEffect.getEffect().value().equals(effect.getEffect().value()) && effect.getAmplifier() >= reqEffect.getAmplifier() && effect.getDuration() >= reqEffect.getDuration()){
                  found = true;
                  break;
               }
            }
            if(!found) return false;
         }
         return true;
      });
      this.potion = potion;
      return this;
   }
   
   public ArcanaIngredient withEffects(MobEffectInstance... effects){
      this.itemPredicate = this.itemPredicate.and((stack) -> {
         PotionContents pots = stack.get(DataComponents.POTION_CONTENTS);
         
         for(MobEffectInstance reqEffect : effects){
            boolean found = false;
            for(MobEffectInstance effect : pots.getAllEffects()){
               if(reqEffect.getEffect().value().equals(effect.getEffect().value()) && effect.getAmplifier() >= reqEffect.getAmplifier() && effect.getDuration() >= reqEffect.getDuration()){
                  found = true;
                  break;
               }
            }
            if(!found) return false;
         }
         return true;
      });
      this.effects.addAll(Arrays.stream(effects).toList());
      return this;
   }
   
   public int getCount(){
      return count;
   }
   
   public boolean validStack(ItemStack stack){
      return this.itemPredicate.test(stack) && stack.getCount() >= this.count;
   }
   
   public boolean validStackIgnoreCount(ItemStack stack){
      return this.itemPredicate.test(stack);
   }
   
   public ItemStack getRemainder(ItemStack stack, int resourceLvl){
      int saved = 0;
      if(!ignoresResourceful){
         for(int i = 0; i < count; i++){
            if(Math.random() < 0.05 * resourceLvl) saved++;
         }
      }
      int newCount = ArcanaItemUtils.isArcane(stack) ? count : count - saved;
      
      if(stack.getCount() <= newCount){
         return ItemStack.EMPTY;
      }else{
         ItemStack stackCopy = stack.copy();
         stackCopy.shrink(newCount);
         return stackCopy;
      }
   }
   
   public ItemStack ingredientAsStack(){
      return this.exampleStack.copy();
   }
   
   public String getName(){
      return ingredientAsStack().getHoverName().getString();
   }
   
   public List<Tuple<ResourceKey<Enchantment>, Integer>> getEnchantments(){
      return new ArrayList<>(enchantments);
   }
   
   public Holder<Potion> getPotion(){
      return potion;
   }
   
   public List<MobEffectInstance> getEffects(){
      return new ArrayList<>(effects);
   }
   
   public boolean getIgnoresResourceful(){
      return ignoresResourceful;
   }
   
   public String getCodeString(char character){
      if(this.exampleStack.isEmpty()) return character+" = ArcanaIngredient.EMPTY;";
      StringBuilder builder = new StringBuilder(character+" = new ArcanaIngredient(Items.");
      String id = BuiltInRegistries.ITEM.getKey(this.exampleStack.getItem()).getPath().toUpperCase(Locale.ROOT);
      builder.append(id).append(", ").append(this.exampleStack.getCount());
      if(this.ignoresResourceful){
         builder.append(", true");
      }
      builder.append(")");
      
      if(!this.enchantments.isEmpty()){
         builder.append(".withEnchantments(");
         for(Tuple<ResourceKey<Enchantment>, Integer> enchantment : this.enchantments){
            builder.append("new ArcanaIngredient.EnchantmentEntry(Enchantments.");
            builder.append(enchantment.getA().identifier().getPath().toUpperCase(Locale.ROOT));
            builder.append(", ");
            builder.append(enchantment.getB());
            builder.append(")");
            if(this.enchantments.indexOf(enchantment) < this.enchantments.size()-1){
               builder.append(", ");
            }
         }
         builder.append(")");
      }
      
      if(this.potion != null){
         builder.append(".withPotion(Potions.");
         builder.append(this.potion.unwrapKey().get().identifier().getPath().toUpperCase(Locale.ROOT));
         builder.append(")");
      }
      
      return builder.append(";").toString();
   }
   
   @Override
   public boolean equals(Object other){
      if(other == this) return true;
      if(!(other instanceof ArcanaIngredient o)) return false;
      return o.itemPredicate.test(this.exampleStack) && this.itemPredicate.test(o.exampleStack);
   }
   
   public JsonObject toJson(){
      JsonObject json = new JsonObject();
      json.addProperty("type", "arcananovum:generic_ingredient");
      
      // Serialize item(s)
      if(acceptedItems.size() == 1){
         Either<Item, TagKey<Item>> entry = acceptedItems.getFirst();
         if(entry.left().isPresent()){
            json.addProperty("item", BuiltInRegistries.ITEM.getKey(entry.left().get()).toString());
         }else if(entry.right().isPresent()){
            json.addProperty("item", "#" + entry.right().get().location().toString());
         }
      }else{
         JsonArray itemArray = new JsonArray();
         for(Either<Item, TagKey<Item>> entry : acceptedItems){
            if(entry.left().isPresent()){
               itemArray.add(BuiltInRegistries.ITEM.getKey(entry.left().get()).toString());
            }else if(entry.right().isPresent()){
               itemArray.add("#" + entry.right().get().location().toString());
            }
         }
         json.add("item", itemArray);
      }
      
      json.addProperty("count", count);
      
      if(ignoresResourceful){
         json.addProperty("ignores_resourceful", true);
      }
      
      // Serialize enchantments
      if(!enchantments.isEmpty()){
         JsonObject enchantmentsJson = new JsonObject();
         for(Tuple<ResourceKey<Enchantment>,Integer> enchantTuple : enchantments){
            enchantmentsJson.addProperty(enchantTuple.getA().identifier().toString(), enchantTuple.getB());
         }
         json.add("enchantments", enchantmentsJson);
      }
      
      // Serialize potions - either as a potion ID or as a list of effects
      if(potion != null){
         // If we have a potion holder, serialize as a potion ID
         json.addProperty("potions", BuiltInRegistries.POTION.getKey(potion.value()).toString());
      }else if(!effects.isEmpty()){
         // If we have individual effects, serialize as an array
         JsonArray effectsArray = new JsonArray();
         for(MobEffectInstance effect : effects){
            JsonObject effectJson = new JsonObject();
            effectJson.addProperty("effect", BuiltInRegistries.MOB_EFFECT.getKey(effect.getEffect().value()).toString());
            effectJson.addProperty("duration", effect.getDuration());
            effectJson.addProperty("amplifier", effect.getAmplifier());
            effectsArray.add(effectJson);
         }
         json.add("potions", effectsArray);
      }
      
      return json;
   }
   
   public static ArcanaIngredient fromJson(JsonObject json){
      if(!json.get("type").getAsString().equals("arcananovum:generic_ingredient")) return null;
      
      // Parse item(s)
      List<Either<Item, TagKey<Item>>> acceptedItems = new ArrayList<>();
      JsonElement itemElement = json.get("item");
      if(itemElement.isJsonArray()){
         for(JsonElement element : itemElement.getAsJsonArray()){
            Either<Item, TagKey<Item>> e = MinecraftUtils.parseItemOrTag(element.getAsString());
            if(e == null) continue;
            acceptedItems.add(e);
         }
      }else{
         Either<Item, TagKey<Item>> e = MinecraftUtils.parseItemOrTag(itemElement.getAsString());
         if(e != null) acceptedItems.add(e);
      }
      
      int count = json.get("count").getAsInt();
      boolean ignoresResourceful = json.has("ignores_resourceful") && json.get("ignores_resourceful").getAsBoolean();
      
      // Get the first item for the example stack
      Item exampleItem = acceptedItems.getFirst().left().orElse(Items.AIR);
      ItemStack exampleStack = new ItemStack(exampleItem, count);
      
      // Build the base predicate
      Predicate<ItemStack> itemPredicate = (stack) -> acceptedItems.stream().anyMatch(e ->
            (e.left().isPresent() && stack.is(e.left().get())) || (e.right().isPresent() && stack.is(e.right().get())));
      
      ArcanaIngredient ingredient = new ArcanaIngredient(acceptedItems, count, ignoresResourceful, exampleStack, itemPredicate);
      
      // Parse enchantments
      if(json.has("enchantments")){
         JsonObject enchantmentsJson = json.getAsJsonObject("enchantments");
         for(Map.Entry<String, JsonElement> entry : enchantmentsJson.entrySet()){
            ResourceKey<Enchantment> enchantKey = ResourceKey.create(Registries.ENCHANTMENT, Identifier.parse(entry.getKey()));
            int level = entry.getValue().getAsInt();
            ingredient.enchantments.add(new Tuple<>(enchantKey, level));
            // Add enchantment predicate
            final ResourceKey<Enchantment> finalEnchantKey = enchantKey;
            final int finalLevel = level;
            ingredient.itemPredicate = ingredient.itemPredicate.and((stack) -> {
               ItemEnchantments enchants = EnchantmentHelper.getEnchantmentsForCrafting(stack);
               for(Object2IntMap.Entry<Holder<Enchantment>> e : enchants.entrySet()){
                  if(e.getKey().is(finalEnchantKey) && e.getIntValue() >= finalLevel) return true;
               }
               return false;
            });
         }
         if(!ingredient.enchantments.isEmpty()){
            ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(ingredient.exampleStack.getEnchantments());
            for(Tuple<ResourceKey<Enchantment>, Integer> enchantment : ingredient.enchantments){
               Holder<Enchantment> enchant = MinecraftUtils.getEnchantment(enchantment.getA());
               if(enchant == null) continue;
               mutable.set(enchant, enchantment.getB());
            }
            EnchantmentHelper.setEnchantments(ingredient.exampleStack,mutable.toImmutable());
         }
      }
      
      // Parse potions
      if(json.has("potions")){
         JsonElement potionsElement = json.get("potions");
         List<MobEffectInstance> effectsList = new ArrayList<>();
         
         if(potionsElement.isJsonPrimitive()){
            // It's a potion ID
            String potionId = potionsElement.getAsString();
            Holder<Potion> foundPotion = BuiltInRegistries.POTION.get(Identifier.parse(potionId)).orElse(null);
            if(foundPotion != null){
               ingredient.potion = foundPotion;
               effectsList.addAll(foundPotion.value().getEffects());
               ingredient.exampleStack.set(DataComponents.POTION_CONTENTS,new PotionContents(foundPotion));
            }
         }else if(potionsElement.isJsonArray()){
            // It's an array of effects
            JsonArray effectsArray = potionsElement.getAsJsonArray();
            for(JsonElement element : effectsArray){
               JsonObject effectJson = element.getAsJsonObject();
               String effectId = effectJson.get("effect").getAsString();
               int duration = effectJson.get("duration").getAsInt();
               int amplifier = effectJson.get("amplifier").getAsInt();
               
               Holder<MobEffect> effectHolder = BuiltInRegistries.MOB_EFFECT.get(Identifier.parse(effectId)).orElse(null);
               if(effectHolder != null){
                  MobEffectInstance effectInstance = new MobEffectInstance(effectHolder, duration, amplifier);
                  effectsList.add(effectInstance);
                  ingredient.effects.add(effectInstance);
               }
            }
            if(!effectsList.isEmpty()){
               OptionalInt colorOpt = PotionContents.getColorOptional(effectsList);
               PotionContents newComp = new PotionContents(Optional.empty(), colorOpt.isPresent() ? Optional.of(colorOpt.getAsInt()) : Optional.empty(), effectsList, Optional.empty());
               ingredient.exampleStack.set(DataComponents.POTION_CONTENTS, newComp);
               
               if(ingredient.exampleStack.is(Items.POTION)){
                  ingredient.exampleStack.set(DataComponents.CUSTOM_NAME, Items.POTION.getName().copy().setStyle(Style.EMPTY.withItalic(false)));
               }else if(ingredient.exampleStack.is(Items.SPLASH_POTION)){
                  ingredient.exampleStack.set(DataComponents.CUSTOM_NAME,Items.SPLASH_POTION.getName().copy().setStyle(Style.EMPTY.withItalic(false)));
               }else if(ingredient.exampleStack.is(Items.LINGERING_POTION)){
                  ingredient.exampleStack.set(DataComponents.CUSTOM_NAME, Items.LINGERING_POTION.getName().copy().setStyle(Style.EMPTY.withItalic(false)));
               }
            }
         }
         
         if(!effectsList.isEmpty()){
            ingredient.itemPredicate = ingredient.itemPredicate.and((stack) -> {
               PotionContents pots = stack.get(DataComponents.POTION_CONTENTS);
               if(pots == null) return false;
               
               for(MobEffectInstance reqEffect : effectsList){
                  boolean found = false;
                  for(MobEffectInstance effect : pots.getAllEffects()){
                     if(reqEffect.getEffect().value().equals(effect.getEffect().value()) &&
                           effect.getAmplifier() >= reqEffect.getAmplifier() &&
                           effect.getDuration() >= reqEffect.getDuration()){
                        found = true;
                        break;
                     }
                  }
                  if(!found) return false;
               }
               return true;
            });
         }
      }
      
      return ingredient;
   }
   
   public record EnchantmentEntry(ResourceKey<Enchantment> enchantmentKey, Holder<Enchantment> enchantment, int level){
      public EnchantmentEntry(ResourceKey<Enchantment> enchantmentKey, int level){
         this(enchantmentKey, MinecraftUtils.getEnchantment(enchantmentKey), level);
      }
   }
}
