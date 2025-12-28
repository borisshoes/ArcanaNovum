package net.borisshoes.arcananovum.recipes.arcana;

import com.google.gson.JsonObject;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.items.Waystone;
import net.borisshoes.arcananovum.utils.ArcanaUtils;
import net.borisshoes.borislib.BorisLib;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class WaystoneIngredient extends ArcanaIngredient {
   
   private boolean consumed;
   private boolean requireUnattuned;
   private boolean requireAttuned;
   private ResourceKey<Level> worldKey;
   
   private WaystoneIngredient(int count, boolean consumed, boolean requireUnattuned, boolean requireAttuned, ResourceKey<Level> worldKey){
      super(ArcanaRegistry.WAYSTONE.getPrefItem().getItem(), count, true);
      this.requireAttuned = requireAttuned;
      this.requireUnattuned = requireUnattuned;
      this.worldKey = worldKey;
      this.consumed = consumed;
   }
   
   public WaystoneIngredient(boolean consumed){
      super(ArcanaRegistry.WAYSTONE.getPrefItem().getItem(), 1, true);
      this.consumed = consumed;
   }
   
   public WaystoneIngredient requireUnattuned(){
      this.requireUnattuned = true;
      this.requireAttuned = false;
      this.worldKey = null;
      return this;
   }
   
   public WaystoneIngredient requireAttuned(){
      this.requireUnattuned = false;
      this.requireAttuned = true;
      this.worldKey = null;
      return this;
   }
   
   public WaystoneIngredient requireAttuned(ResourceKey<Level> world){
      this.requireUnattuned = false;
      this.requireAttuned = true;
      this.worldKey = world;
      return this;
   }
   
   @Override
   public boolean validStack(ItemStack stack){
      if(!stack.is(ArcanaRegistry.WAYSTONE.getItem())) return false;
      if(requireUnattuned){
         return Waystone.isUnattuned(stack);
      }else if(requireAttuned){
         if(Waystone.isUnattuned(stack) || Waystone.getTarget(stack) == null) return false;
         if(this.worldKey != null && !Waystone.getTarget(stack).world().identifier().equals(this.worldKey.identifier())) return false;
      }
      return true;
   }
   
   @Override
   public ArcanaIngredient copyWithCount(int newCount){
      return new WaystoneIngredient(newCount,this.consumed,this.requireUnattuned,this.requireAttuned,this.worldKey);
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
         if(consumed){
            return ItemStack.EMPTY;
         }else{
            return stack;
         }
      }
   }
   
   @Override
   public String getName(){
      String name = ArcanaRegistry.WAYSTONE.getNameString();
      if(!consumed){
         name += " (Not Consumed)";
      }
      if(worldKey != null){
         name += " Attuned ["+ArcanaUtils.getFormattedDimName(worldKey).getString()+"]";
      }else if(requireAttuned){
         name += " Attuned";
      }else if(requireUnattuned){
         name += " Unattuned";
      }
      return name;
   }
   
   @Override
   public ItemStack ingredientAsStack(){
      if(requireAttuned){
         ItemStack waystone = ArcanaRegistry.WAYSTONE.getPrefItem().copy();
         Waystone.saveTarget(waystone,new Waystone.WaystoneTarget(worldKey != null ? worldKey : ServerLevel.OVERWORLD,new Vec3(0,0,0),0,0));
         return waystone;
      }else{
         return ArcanaRegistry.WAYSTONE.getPrefItem().copy();
      }
   }
   
   public JsonObject toJson(){
      JsonObject json = new JsonObject();
      json.addProperty("type", "arcananovum:waystone_ingredient");
      json.addProperty("consumed", consumed);
      json.addProperty("require_unattuned", requireUnattuned);
      json.addProperty("require_attuned", requireAttuned);
      json.addProperty("world_key", worldKey != null ? worldKey.identifier().toString() : null);
      return json;
   }
   
   public static WaystoneIngredient fromJson(JsonObject json){
      if(!json.get("type").getAsString().equals("arcananovum:waystone_ingredient")) return null;
      boolean consumed = json.get("consumed").getAsBoolean();
      boolean requireUnattuned = json.get("require_unattuned").getAsBoolean();
      boolean requireAttuned = json.get("require_attuned").getAsBoolean();
      ResourceKey<Level> worldKey = null;
      if(json.has("world_key") && !json.get("world_key").isJsonNull()){
         worldKey = ResourceKey.create(Registries.DIMENSION, Identifier.parse(json.get("world_key").getAsString()));
      }
      return new WaystoneIngredient(1, consumed, requireUnattuned, requireAttuned, worldKey);
   }
}
