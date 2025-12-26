package net.borisshoes.arcananovum.recipes.arcana;

import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.ItemEnchantments;

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
      this.itemPredicate = (stack) -> stack.is(itemType);
   }
   
   public ArcanaIngredient(Item itemType, int count){
      this.count = count;
      this.itemType = itemType;
      this.exampleStack = new ItemStack(itemType,count);
      this.itemPredicate = (stack) -> stack.is(itemType);
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
   
   public ArcanaIngredient withEnchantments(EnchantmentInstance... enchantments){
      this.itemPredicate = this.itemPredicate.and((stack) -> {
         ItemEnchantments enchants = EnchantmentHelper.getEnchantmentsForCrafting(stack);
         for(EnchantmentInstance enchantment : enchantments){
            if(enchants.getLevel(enchantment.enchantment()) < enchantment.level()) return false;
         }
         return true;
      });
      
      for(EnchantmentInstance enchantment : enchantments){
         this.exampleStack.enchant(enchantment.enchantment(),enchantment.level());
      }
      
      return this;
   }
   
   public ArcanaIngredient withPotions(Holder<Potion>... potions){
      List<MobEffectInstance> effects = new ArrayList<>();
      Arrays.stream(potions).forEach(potion -> effects.addAll(potion.value().getEffects()));
      return withEffects(effects.toArray(new MobEffectInstance[0]));
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
      
      OptionalInt colorOpt = PotionContents.getColorOptional(List.of(effects));
      PotionContents newComp = new PotionContents(Optional.empty(),colorOpt.isPresent() ? Optional.of(colorOpt.getAsInt()) : Optional.empty(),List.of(effects),Optional.empty());
      this.exampleStack.set(DataComponents.POTION_CONTENTS,newComp);
      
      if(this.exampleStack.is(Items.POTION)){
         this.exampleStack.set(DataComponents.CUSTOM_NAME, Component.literal("Potion").setStyle(Style.EMPTY.withItalic(false)));
      }else if(this.exampleStack.is(Items.SPLASH_POTION)){
         this.exampleStack.set(DataComponents.CUSTOM_NAME, Component.literal("Splash Potion").setStyle(Style.EMPTY.withItalic(false)));
      }else if(this.exampleStack.is(Items.LINGERING_POTION)){
         this.exampleStack.set(DataComponents.CUSTOM_NAME, Component.literal("Lingering Potion").setStyle(Style.EMPTY.withItalic(false)));
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
   
   @Override
   public boolean equals(Object other){
      if(other == this) return true;
      if(!(other instanceof ArcanaIngredient o)) return false;
      return o.itemPredicate.test(this.exampleStack) && this.itemPredicate.test(o.exampleStack);
   }
}
