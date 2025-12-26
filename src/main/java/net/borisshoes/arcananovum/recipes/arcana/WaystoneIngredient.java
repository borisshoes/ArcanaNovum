package net.borisshoes.arcananovum.recipes.arcana;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.items.Waystone;
import net.borisshoes.arcananovum.utils.ArcanaUtils;
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
}
