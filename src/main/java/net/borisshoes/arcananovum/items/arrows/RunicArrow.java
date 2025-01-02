package net.borisshoes.arcananovum.items.arrows;

import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.entities.RunicArrowEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;

public abstract class RunicArrow extends ArcanaItem {
   public abstract void entityHit(RunicArrowEntity arrow, EntityHitResult entityHitResult);
   public abstract void blockHit(RunicArrowEntity arrow, BlockHitResult blockHitResult);
   
   public static final String TXT = "runic_arrow";
   
   public Text getArrowName(ItemStack arrow){
      TextColor textColor = getPrefItem().getName().getStyle().getColor();

      if(arrow.contains(DataComponentTypes.CUSTOM_NAME)){
         return Text.literal(arrow.getName().getString()).formatted(Formatting.BOLD).withColor(textColor.getRgb());
      }else{
         return getTranslatedName().formatted(Formatting.BOLD).withColor(textColor.getRgb());
      }
   }
}
