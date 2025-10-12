package net.borisshoes.arcananovum.recipes.arcana;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.items.Waystone;
import net.borisshoes.arcananovum.utils.ArcanaUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class WaystoneIngredient extends ArcanaIngredient {
   
   private boolean consumed;
   private boolean requireUnattuned;
   private boolean requireAttuned;
   private RegistryKey<World> worldKey;
   
   private WaystoneIngredient(int count, boolean consumed, boolean requireUnattuned, boolean requireAttuned, RegistryKey<World> worldKey){
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
   
   public WaystoneIngredient requireAttuned(RegistryKey<World> world){
      this.requireUnattuned = false;
      this.requireAttuned = true;
      this.worldKey = world;
      return this;
   }
   
   @Override
   public boolean validStack(ItemStack stack){
      if(!stack.isOf(ArcanaRegistry.WAYSTONE.getItem())) return false;
      if(requireUnattuned){
         return Waystone.isUnattuned(stack);
      }else if(requireAttuned){
         if(Waystone.isUnattuned(stack) || Waystone.getTarget(stack) == null) return false;
         if(this.worldKey != null && !Waystone.getTarget(stack).world().getValue().equals(this.worldKey.getValue())) return false;
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
         Waystone.saveTarget(waystone,new Waystone.WaystoneTarget(worldKey != null ? worldKey : ServerWorld.OVERWORLD,new Vec3d(0,0,0),0,0));
         return waystone;
      }else{
         return ArcanaRegistry.WAYSTONE.getPrefItem().copy();
      }
   }
}
