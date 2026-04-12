package net.borisshoes.arcananovum.recipes.vanilla;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import java.util.Map;

import static java.util.Map.entry;

public class AquaticEversourceFillRecipe extends CustomRecipe {
   public static final MapCodec<AquaticEversourceFillRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(
         i -> i.group(
                     Ingredient.CODEC.fieldOf("item").forGetter(o -> o.item),
                     Ingredient.CODEC.fieldOf("eversource").forGetter(o -> o.eversource),
                     ItemStackTemplate.CODEC.fieldOf("result").forGetter(o -> o.result)
               )
               .apply(i, AquaticEversourceFillRecipe::new)
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, AquaticEversourceFillRecipe> STREAM_CODEC = StreamCodec.composite(
         Ingredient.CONTENTS_STREAM_CODEC,
         o -> o.item,
         Ingredient.CONTENTS_STREAM_CODEC,
         o -> o.eversource,
         ItemStackTemplate.STREAM_CODEC,
         o -> o.result,
         AquaticEversourceFillRecipe::new
   );
   public static final RecipeSerializer<AquaticEversourceFillRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);
   
   public static final Map<Item, ItemStackTemplate> FILLABLE = Map.ofEntries(
         entry(Items.DIRT, new ItemStackTemplate(Items.MUD)),
         entry(Items.GLASS_BOTTLE, new ItemStackTemplate(Items.POTION, DataComponentPatch.builder().set(DataComponents.POTION_CONTENTS, new PotionContents(Potions.WATER)).build())),
         entry(Items.BUCKET, new ItemStackTemplate(Items.WATER_BUCKET)),
         entry(Items.WHITE_CONCRETE_POWDER, new ItemStackTemplate(Items.WHITE_CONCRETE)),
         entry(Items.ORANGE_CONCRETE_POWDER, new ItemStackTemplate(Items.ORANGE_CONCRETE)),
         entry(Items.MAGENTA_CONCRETE_POWDER, new ItemStackTemplate(Items.MAGENTA_CONCRETE)),
         entry(Items.LIGHT_BLUE_CONCRETE_POWDER, new ItemStackTemplate(Items.LIGHT_BLUE_CONCRETE)),
         entry(Items.YELLOW_CONCRETE_POWDER, new ItemStackTemplate(Items.YELLOW_CONCRETE)),
         entry(Items.LIME_CONCRETE_POWDER, new ItemStackTemplate(Items.LIME_CONCRETE)),
         entry(Items.PINK_CONCRETE_POWDER, new ItemStackTemplate(Items.PINK_CONCRETE)),
         entry(Items.GRAY_CONCRETE_POWDER, new ItemStackTemplate(Items.GRAY_CONCRETE)),
         entry(Items.LIGHT_GRAY_CONCRETE_POWDER, new ItemStackTemplate(Items.LIGHT_GRAY_CONCRETE)),
         entry(Items.CYAN_CONCRETE_POWDER, new ItemStackTemplate(Items.CYAN_CONCRETE)),
         entry(Items.PURPLE_CONCRETE_POWDER, new ItemStackTemplate(Items.PURPLE_CONCRETE)),
         entry(Items.BLUE_CONCRETE_POWDER, new ItemStackTemplate(Items.BLUE_CONCRETE)),
         entry(Items.BROWN_CONCRETE_POWDER, new ItemStackTemplate(Items.BROWN_CONCRETE)),
         entry(Items.GREEN_CONCRETE_POWDER, new ItemStackTemplate(Items.GREEN_CONCRETE)),
         entry(Items.RED_CONCRETE_POWDER, new ItemStackTemplate(Items.RED_CONCRETE)),
         entry(Items.BLACK_CONCRETE_POWDER, new ItemStackTemplate(Items.BLACK_CONCRETE))
   );
   
   private final Ingredient item;
   private final Ingredient eversource;
   private final ItemStackTemplate result;
   
   public AquaticEversourceFillRecipe(Ingredient item, Ingredient eversource, ItemStackTemplate result){
      this.item = item;
      this.eversource = eversource;
      this.result = result;
   }
   
   @Override
   public CraftingBookCategory category() {
      return CraftingBookCategory.MISC;
   }
   
   @Override
   public boolean matches(CraftingInput input, Level world){
      boolean hasEversource = false;
      boolean hasFillable = false;
      
      for(int i = 0; i < input.size(); ++i){
         ItemStack rStack = input.getItem(i);
         if(rStack.isEmpty()) continue;
         
         if(!hasEversource && this.eversource.test(rStack)){
            hasEversource = true;
            continue;
         }else if(!hasFillable && this.item.test(rStack)){
            hasFillable = true;
            continue;
         }
         return false;
      }
      
      return hasEversource && hasFillable;
   }
   
   @Override
   public ItemStack assemble(CraftingInput input){
      return this.result.withCount(1).create();
   }
   
   
   @Override
   public NonNullList<ItemStack> getRemainingItems(CraftingInput input){
      NonNullList<ItemStack> stacks = NonNullList.withSize(input.size(), ItemStack.EMPTY);
      for(int i = 0; i < input.size(); ++i){
         ItemStack rStack = input.getItem(i);
         if(rStack.isEmpty()) continue;
         
         if(this.eversource.test(rStack)){
            stacks.set(i, rStack.copy());
         }
      }
      return stacks;
   }
   
   @Override
   public RecipeSerializer<? extends CustomRecipe> getSerializer(){
      return SERIALIZER;
   }
}
