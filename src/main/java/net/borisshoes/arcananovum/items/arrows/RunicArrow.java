package net.borisshoes.arcananovum.items.arrows;

import net.borisshoes.arcananovum.core.MagicItem;
import net.borisshoes.arcananovum.entities.RunicArrowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;

public abstract class RunicArrow extends MagicItem {
   public abstract void entityHit(RunicArrowEntity arrow, EntityHitResult entityHitResult);
   public abstract void blockHit(RunicArrowEntity arrow, BlockHitResult blockHitResult);
   
   public Text getArrowName(ItemStack arrow){
      Formatting color = Formatting.byName(getPrefItem().getName().getStyle().getColor().getName());
      NbtCompound prefNbt = getPrefNBT();
      String prefName = prefNbt.getCompound("display").getString("Name");
      NbtCompound arrowNbt = arrow.getNbt();
      if(prefName.equals(arrowNbt.getCompound("display").getString("Name"))){
         return Text.literal(getNameString()).formatted(color,Formatting.BOLD);
      }else{
         return Text.literal(arrow.getName().getString()).formatted(color,Formatting.BOLD);
      }
   }
}
