package net.borisshoes.arcananovum.recipes.arcana;

import com.google.gson.JsonObject;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.items.Soulstone;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public class SoulstoneIngredient extends ArcanaIngredient {
   
   private final int souls;
   private final String type;
   private final boolean consume;
   private final boolean repeatable;
   private final boolean ignoreEssenceEggTypes;
   
   public SoulstoneIngredient(int souls, boolean repeatable, boolean consume, boolean ignoreEssenceEggTypes, @Nullable String type){
      super(ArcanaRegistry.SOULSTONE.getPrefItem().getItem(), 1, true);
      this.souls = souls;
      this.repeatable = repeatable;
      this.consume = consume;
      this.ignoreEssenceEggTypes = ignoreEssenceEggTypes;
      this.type = type;
   }
   
   @Override
   public ArcanaIngredient copyWithCount(int newCount){
      return new SoulstoneIngredient(souls,repeatable,consume, ignoreEssenceEggTypes,type);
   }
   
   @Override
   public boolean validStack(ItemStack stack){
      if(ArcanaItemUtils.identifyItem(stack) instanceof Soulstone){
         if(type != null){
            if(!type.equals(Soulstone.getType(stack)))
               return false;
         }

         if(!ignoreEssenceEggTypes){
            EntityType<?> eType = EntityType.byString(Soulstone.getType(stack)).orElse(null);
            if(eType != null && eType.is(ArcanaRegistry.ESSENCE_EGG_DISALLOWED)) return false;
         }
         return Soulstone.getSouls(stack) >= souls;
      }else{
         return false;
      }
   }
   
   @Override
   public boolean validStackIgnoreCount(ItemStack stack){
      return this.validStack(stack);
   }
   
   @Override
   public ItemStack getRemainder(ItemStack stack, int resourceLvl){
      if(!validStack(stack)){
         return ItemStack.EMPTY;
      }else{
         if(consume){
            return ItemStack.EMPTY;
         }else{
            if(souls == 0){
               return stack;
            }
            if(repeatable){
               return Soulstone.setSouls(stack,Soulstone.getSouls(stack) % souls);
            }else{
               return Soulstone.setSouls(stack,Soulstone.getSouls(stack)-souls);
            }
         }
      }
   }
   
   @Override
   public String getName(){
      String name = ArcanaRegistry.SOULSTONE.getNameString();
      if(!consume){
         name += " (Not Consumed)";
      }
      if(type != null){
         name += " ("+souls+"+ "+ EntityType.byString(type).get().getDescription().getString()+")";
      }
      return name;
   }
   
   @Override
   public String getCodeString(char character){
      return character + " = new SoulstoneIngredient();";
   }
   
   @Override
   public ItemStack ingredientAsStack(){
      return Soulstone.getShowcaseItem(souls,type);
   }
   
   public JsonObject toJson(){
      JsonObject json = new JsonObject();
      json.addProperty("type", "arcananovum:soulstone_ingredient");
      json.addProperty("souls", souls);
      json.addProperty("mob_type", type);
      json.addProperty("consumed", consume);
      json.addProperty("repeatable", repeatable);
      json.addProperty("ignore_essence_egg_types", ignoreEssenceEggTypes);
      return json;
   }
   
   public static SoulstoneIngredient fromJson(JsonObject json){
      if(!json.get("type").getAsString().equals("arcananovum:soulstone_ingredient")) return null;
      int souls = json.get("souls").getAsInt();
      String mobType = json.has("mob_type") && !json.get("mob_type").isJsonNull() ? json.get("mob_type").getAsString() : null;
      boolean consumed = json.get("consumed").getAsBoolean();
      boolean repeatable = json.get("repeatable").getAsBoolean();
      boolean ignoreEssenceEggTypes = json.get("ignore_essence_egg_types").getAsBoolean();
      return new SoulstoneIngredient(souls, repeatable, consumed, ignoreEssenceEggTypes, mobType);
   }
}
