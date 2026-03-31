package net.borisshoes.arcananovum.items.arrows;

import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.entities.RunicArrowEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

public abstract class RunicArrow extends ArcanaItem {
   public abstract void entityHit(RunicArrowEntity arrow, EntityHitResult entityHitResult);
   
   public abstract void blockHit(RunicArrowEntity arrow, BlockHitResult blockHitResult);
   
   public static final String TXT = "runic_arrow";
   
   public Component getArrowName(ItemStack arrow){
      TextColor textColor = getPrefItem().getHoverName().getStyle().getColor();
      
      if(arrow.has(DataComponents.CUSTOM_NAME)){
         return Component.literal(arrow.getHoverName().getString()).withStyle(ChatFormatting.BOLD).withColor(textColor.getValue());
      }else{
         return getTranslatedName().withStyle(ChatFormatting.BOLD).withColor(textColor.getValue());
      }
   }
}
