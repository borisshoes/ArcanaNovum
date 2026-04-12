package net.borisshoes.arcananovum.recipes.vanilla;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.items.MagmaticEversource;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.borislib.BorisLib;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import java.util.Map;

import static java.util.Map.entry;

public class MagmaticEversourceFillRecipe extends CustomRecipe {
   public static final MapCodec<MagmaticEversourceFillRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(
         i -> i.group(
                     Ingredient.CODEC.fieldOf("item").forGetter(o -> o.item),
                     Ingredient.CODEC.fieldOf("eversource").forGetter(o -> o.eversource),
                     ItemStackTemplate.CODEC.fieldOf("result").forGetter(o -> o.result)
               )
               .apply(i, MagmaticEversourceFillRecipe::new)
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, MagmaticEversourceFillRecipe> STREAM_CODEC = StreamCodec.composite(
         Ingredient.CONTENTS_STREAM_CODEC,
         o -> o.item,
         Ingredient.CONTENTS_STREAM_CODEC,
         o -> o.eversource,
         ItemStackTemplate.STREAM_CODEC,
         o -> o.result,
         MagmaticEversourceFillRecipe::new
   );
   public static final RecipeSerializer<MagmaticEversourceFillRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);
   
   public static final Map<Item, ItemStackTemplate> FILLABLE = Map.ofEntries(
         entry(Items.BUCKET, new ItemStackTemplate(Items.LAVA_BUCKET))
   );
   
   private final Ingredient item;
   private final Ingredient eversource;
   private final ItemStackTemplate result;
   
   public MagmaticEversourceFillRecipe(Ingredient item, Ingredient eversource, ItemStackTemplate result){
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
      int eversourceSlot = -1;
      
      for(int i = 0; i < input.size(); ++i){
         ItemStack rStack = input.getItem(i);
         if(rStack.isEmpty()) continue;
         
         if(!hasEversource && this.eversource.test(rStack)){
            hasEversource = true;
            eversourceSlot = i;
            continue;
         }else if(!hasFillable && this.item.test(rStack)){
            hasFillable = true;
            continue;
         }
         return false;
      }
      if(!hasEversource || !hasFillable) return false;
      
      ItemStack eversource = input.getItem(eversourceSlot);
      int charges = ArcanaItem.getIntProperty(eversource, MagmaticEversource.USES_TAG);
      return charges >= 1;
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
            ItemStack source = rStack.copy();
            ArcanaItem.putProperty(source, MagmaticEversource.USES_TAG, ArcanaItem.getIntProperty(source, MagmaticEversource.USES_TAG) - 1);
            if(ArcanaItemUtils.identifyItem(rStack) instanceof MagmaticEversource arcanaItem) arcanaItem.buildItemLore(source, BorisLib.SERVER);
            stacks.set(i, source);
         }
      }
      return stacks;
   }
   
   @Override
   public RecipeSerializer<? extends CustomRecipe> getSerializer(){
      return SERIALIZER;
   }
}
