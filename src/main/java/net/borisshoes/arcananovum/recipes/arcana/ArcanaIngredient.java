package net.borisshoes.arcananovum.recipes.arcana;

import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import java.util.*;
import java.util.function.Predicate;

public class ArcanaIngredient {
   protected final Item itemType;
   protected final int count;
   protected Predicate<ItemStack> itemPredicate;
   protected final ItemStack exampleStack;
   protected final boolean ignoresResourceful;
   
   public static final ArcanaIngredient EMPTY = new ArcanaIngredient(Items.AIR,0, true, ItemStack.EMPTY, ItemStack::isEmpty);
   
   public ArcanaIngredient(Item itemType, int count, boolean ignoresResourceful){
      this.count = count;
      this.itemType = itemType;
      this.ignoresResourceful = ignoresResourceful;
      this.exampleStack = new ItemStack(itemType,count);
      this.itemPredicate = (stack) -> stack.isOf(itemType);
   }
   
   public ArcanaIngredient(Item itemType, int count){
      this.count = count;
      this.itemType = itemType;
      this.exampleStack = new ItemStack(itemType,count);
      this.itemPredicate = (stack) -> stack.isOf(itemType);
      this.ignoresResourceful = false;
   }
   
   private ArcanaIngredient(Item itemType, int count, boolean ignoresResourceful, ItemStack exampleStack, Predicate<ItemStack> itemPredicate){
      this.count = count;
      this.itemType = itemType;
      this.exampleStack = exampleStack.copyWithCount(count);
      this.itemPredicate = itemPredicate;
      this.ignoresResourceful = ignoresResourceful;
   }
   
   public ArcanaIngredient copyWithCount(int newCount){
      return new ArcanaIngredient(itemType,newCount,ignoresResourceful,exampleStack,itemPredicate);
   }
   
   public ArcanaIngredient withEnchantments(EnchantmentLevelEntry... enchantments){
      this.itemPredicate = this.itemPredicate.and((stack) -> {
         ItemEnchantmentsComponent enchants = EnchantmentHelper.getEnchantments(stack);
         for(EnchantmentLevelEntry enchantment : enchantments){
            if(enchants.getLevel(enchantment.enchantment()) < enchantment.level()) return false;
         }
         return true;
      });
      
      for(EnchantmentLevelEntry enchantment : enchantments){
         this.exampleStack.addEnchantment(enchantment.enchantment(),enchantment.level());
      }
      
      return this;
   }
   
   public ArcanaIngredient withPotions(RegistryEntry<Potion>... potions){
      List<StatusEffectInstance> effects = new ArrayList<>();
      Arrays.stream(potions).forEach(potion -> effects.addAll(potion.value().getEffects()));
      return withEffects(effects.toArray(new StatusEffectInstance[0]));
   }
   
   public ArcanaIngredient withEffects(StatusEffectInstance... effects){
      this.itemPredicate = this.itemPredicate.and((stack) -> {
         PotionContentsComponent pots = stack.get(DataComponentTypes.POTION_CONTENTS);
         
         for(StatusEffectInstance reqEffect : effects){
            boolean found = false;
            for(StatusEffectInstance effect : pots.getEffects()){
               if(reqEffect.getEffectType().value().equals(effect.getEffectType().value()) && effect.getAmplifier() >= reqEffect.getAmplifier() && effect.getDuration() >= reqEffect.getDuration()){
                  found = true;
                  break;
               }
            }
            if(!found) return false;
         }
         return true;
      });
      
      OptionalInt colorOpt = PotionContentsComponent.mixColors(List.of(effects));
      PotionContentsComponent newComp = new PotionContentsComponent(Optional.empty(),colorOpt.isPresent() ? Optional.of(colorOpt.getAsInt()) : Optional.empty(),List.of(effects),Optional.empty());
      this.exampleStack.set(DataComponentTypes.POTION_CONTENTS,newComp);
      
      if(this.exampleStack.isOf(Items.POTION)){
         this.exampleStack.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Potion").setStyle(Style.EMPTY.withItalic(false)));
      }else if(this.exampleStack.isOf(Items.SPLASH_POTION)){
         this.exampleStack.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Splash Potion").setStyle(Style.EMPTY.withItalic(false)));
      }else if(this.exampleStack.isOf(Items.LINGERING_POTION)){
         this.exampleStack.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Lingering Potion").setStyle(Style.EMPTY.withItalic(false)));
      }
      
      return this;
   }
   
   
   
   public int getCount(){
      return count;
   }
   
   public Item getItemType(){
      return itemType;
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
         stackCopy.decrement(newCount);
         return stackCopy;
      }
   }
   
   public ItemStack ingredientAsStack(){
      return this.exampleStack.copy();
   }
   
   public String getName(){
      return ingredientAsStack().getName().getString();
   }
   
   @Override
   public boolean equals(Object other){
      if(other == this) return true;
      if(!(other instanceof ArcanaIngredient o)) return false;
      return o.itemPredicate.test(this.exampleStack) && this.itemPredicate.test(o.exampleStack);
   }
}
