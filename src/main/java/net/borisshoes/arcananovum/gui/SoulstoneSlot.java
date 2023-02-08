package net.borisshoes.arcananovum.gui;

import net.borisshoes.arcananovum.items.Soulstone;
import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import org.jetbrains.annotations.Nullable;


public class SoulstoneSlot extends Slot {
   
   public final boolean attuned;
   public final boolean souls;
   public final String type;
   
   public SoulstoneSlot(Inventory inventory, int index, int x, int y, boolean requireAttuned, boolean requireSouls, @Nullable String type){
      super(inventory, index, x, y);
      this.attuned = requireAttuned;
      this.souls = requireSouls;
      this.type = type;
   }
   
   @Override
   public boolean canInsert(ItemStack stack){
      if(MagicItemUtils.identifyItem(stack) instanceof Soulstone){
         if(attuned){
            String attunedType = Soulstone.getType(stack);
            if(attunedType.equals("unattuned")){
               return false;
            }else{
               if(type != null && !attunedType.equals(type)){
                  return false;
               }else{
                  return true;
               }
            }
         }
         if(souls && Soulstone.getSouls(stack) <= 0){
            return false;
         }
         return true;
      }else{
         return false;
      }
   }
}
