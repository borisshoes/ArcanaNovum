package net.borisshoes.arcananovum.gui.arcanetome;

import net.borisshoes.arcananovum.recipes.arcana.ExplainRecipe;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class IngredientCompendiumEntry extends CompendiumEntry{
   
   private final Item item;
   private final Component name;
   private final Optional<List<List<Component>>> bookLore;
   
   public IngredientCompendiumEntry(Component name, ItemStack displayStack, Item item){
      this(name,displayStack,item,null);
   }
   
   public IngredientCompendiumEntry(Component name, ItemStack displayStack, Item item, List<List<Component>> bookLore){
      super(new ArcaneTomeGui.TomeFilter[]{ArcaneTomeGui.TomeFilter.INGREDIENT}, displayStack);
      this.name = name;
      this.item = item;
      this.bookLore = bookLore == null ? Optional.empty() : Optional.of(bookLore);
      ItemLore lore = this.displayStack.getOrDefault(DataComponents.LORE, ItemLore.EMPTY);
      if(!lore.lines().isEmpty()){
         List<Component> lines = new ArrayList<>(lore.styledLines());
         lines.add(Component.literal(""));
         lines.add(TextUtils.removeItalics(Component.literal("Arcana Ingredient").withStyle(ChatFormatting.DARK_PURPLE)));
         this.displayStack.set(DataComponents.LORE, new ItemLore(lines,lines));
      }else{
         List<Component> lines = new ArrayList<>();
         lines.add(TextUtils.removeItalics(Component.literal("Arcana Ingredient").withStyle(ChatFormatting.DARK_PURPLE)));
         this.displayStack.set(DataComponents.LORE, new ItemLore(lines,lines));
      }
   }
   
   public Item getItem(){
      return item;
   }
   
   @Override
   public MutableComponent getName(){
      return name.copy();
   }
   
   @Override
   public int getRarityValue(){
      return -1;
   }
   
   public boolean hasBookLore(){
      return bookLore.isPresent();
   }
   
   public List<List<Component>> getBookLore(){
      return bookLore.orElse(null);
   }
}
