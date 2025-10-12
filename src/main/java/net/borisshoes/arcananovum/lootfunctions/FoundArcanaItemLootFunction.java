package net.borisshoes.arcananovum.lootfunctions;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunctionType;

import java.util.List;

public class FoundArcanaItemLootFunction extends ConditionalLootFunction {
   
   public static final MapCodec<FoundArcanaItemLootFunction> CODEC = RecordCodecBuilder.mapCodec(
         instance -> addConditionsField(instance).apply(instance, FoundArcanaItemLootFunction::new)
   );
   
   
   protected FoundArcanaItemLootFunction(List<LootCondition> conditions){
      super(conditions);
   }
   
   @SuppressWarnings("unchecked")
   @Override
   public LootFunctionType<? extends ConditionalLootFunction> getType(){
      return (LootFunctionType<FoundArcanaItemLootFunction>) ArcanaRegistry.FOUND_ARCANA_ITEM_LOOT_FUNCTION;
   }
   
   @Override
   protected ItemStack process(ItemStack stack, LootContext context){
      for(ArcanaItem arcanaItem : ArcanaRegistry.ARCANA_ITEMS){
         if(stack.isOf(arcanaItem.getItem())){
            stack = arcanaItem.addCrafter(arcanaItem.getNewItem(),null,2,context.getWorld().getServer());
            arcanaItem.buildItemLore(stack,context.getWorld().getServer());
            return stack;
         }
      }
      
      return stack;
   }
}