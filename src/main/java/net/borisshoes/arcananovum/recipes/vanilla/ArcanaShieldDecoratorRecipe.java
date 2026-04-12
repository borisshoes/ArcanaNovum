package net.borisshoes.arcananovum.recipes.vanilla;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShieldDecorationRecipe;

public class ArcanaShieldDecoratorRecipe extends ShieldDecorationRecipe {
   
   // Shadow fields for codec getters (parent fields are private)
   private final Ingredient banner;
   private final Ingredient target;
   private final ItemStackTemplate result;
   
   public static final MapCodec<ArcanaShieldDecoratorRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(
         i -> i.group(
                     Ingredient.CODEC.fieldOf("banner").forGetter(o -> o.banner),
                     Ingredient.CODEC.fieldOf("target").forGetter(o -> o.target),
                     ItemStackTemplate.CODEC.fieldOf("result").forGetter(o -> o.result)
               )
               .apply(i, ArcanaShieldDecoratorRecipe::new)
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, ArcanaShieldDecoratorRecipe> STREAM_CODEC = StreamCodec.composite(
         Ingredient.CONTENTS_STREAM_CODEC,
         o -> o.banner,
         Ingredient.CONTENTS_STREAM_CODEC,
         o -> o.target,
         ItemStackTemplate.STREAM_CODEC,
         o -> o.result,
         ArcanaShieldDecoratorRecipe::new
   );
   public static final RecipeSerializer<ArcanaShieldDecoratorRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);
   
   public ArcanaShieldDecoratorRecipe(final Ingredient banner, final Ingredient target, final ItemStackTemplate result){
      super(banner, target, result);
      this.banner = banner;
      this.target = target;
      this.result = result;
   }
   
   @SuppressWarnings("unchecked")
   @Override
   public RecipeSerializer<ShieldDecorationRecipe> getSerializer(){
      return (RecipeSerializer<ShieldDecorationRecipe>) (RecipeSerializer<?>) SERIALIZER;
   }
}
