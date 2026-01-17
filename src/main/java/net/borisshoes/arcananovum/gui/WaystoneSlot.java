package net.borisshoes.arcananovum.gui;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.items.Waystone;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class WaystoneSlot extends Slot {
   private boolean needsAttunement = false;
   private BlockPos gatewayPos = null;
   private ResourceKey<Level> matchWorld = null;
   
   public WaystoneSlot(Container inventory, int index, int x, int y){
      super(inventory, index, x, y);
   }
   
   public WaystoneSlot withAttunement(boolean attunement){
      this.needsAttunement = attunement;
      return this;
   }
   
   public WaystoneSlot withForGateway(BlockPos gatewayPos){
      this.gatewayPos = gatewayPos;
      return this;
   }
   
   public WaystoneSlot withMatchedWorld(ResourceKey<Level> world){
      this.matchWorld = world;
      return this;
   }
   
   @Override
   public boolean mayPlace(ItemStack stack){
      if(!stack.is(ArcanaRegistry.WAYSTONE.getItem())) return false;
      Waystone.WaystoneTarget target = Waystone.getTarget(stack);
      if(needsAttunement && target == null) return false;
      if(matchWorld != null && target != null){
         if(!target.world().identifier().equals(matchWorld.identifier())) return false;
      }
      if(gatewayPos != null){
         if(!Waystone.isForGateway(stack)) return false;
         if(target != null && BlockPos.containing(target.position()).equals(gatewayPos)) return false;
      }
      return true;
   }
}
