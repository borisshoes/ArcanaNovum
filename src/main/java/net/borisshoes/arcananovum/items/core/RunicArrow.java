package net.borisshoes.arcananovum.items.core;

import net.borisshoes.arcananovum.cardinalcomponents.MagicEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;

public abstract class RunicArrow extends MagicItem{
   public abstract void entityHit(PersistentProjectileEntity arrow, EntityHitResult entityHitResult, MagicEntity magicEntity);
   public abstract void blockHit(PersistentProjectileEntity arrow, BlockHitResult blockHitResult, MagicEntity magicEntity);
   
   public Text getArrowName(ItemStack arrow){
      Formatting color = Formatting.byName(getPrefItem().getName().getStyle().getColor().getName());
      NbtCompound prefNbt = getPrefNBT();
      String prefName = prefNbt.getCompound("display").getString("Name");
      NbtCompound arrowNbt = arrow.getNbt();
      if(prefName.equals(arrowNbt.getCompound("display").getString("Name"))){
         return Text.literal(getName()).formatted(color,Formatting.BOLD);
      }else{
         return Text.literal(arrow.getName().getString()).formatted(color,Formatting.BOLD);
      }
   }
}
