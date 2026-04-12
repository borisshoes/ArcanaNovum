package net.borisshoes.arcananovum.lootfunctions;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;

public class FoundArcanaItemLootFunction extends LootItemConditionalFunction {
   
   public static final MapCodec<FoundArcanaItemLootFunction> CODEC = RecordCodecBuilder.mapCodec(
         instance -> commonFields(instance).apply(instance, FoundArcanaItemLootFunction::new)
   );
   
   
   protected FoundArcanaItemLootFunction(List<LootItemCondition> conditions){
      super(conditions);
   }
   
   @SuppressWarnings("unchecked")
   @Override
   public MapCodec<? extends LootItemConditionalFunction> codec(){
      return (MapCodec<FoundArcanaItemLootFunction>) ArcanaRegistry.FOUND_ARCANA_ITEM_LOOT_FUNCTION;
   }
   
   @Override
   protected ItemStack run(ItemStack stack, LootContext context){
      for(ArcanaItem arcanaItem : ArcanaRegistry.ARCANA_ITEMS){
         if(stack.is(arcanaItem.getItem())){
            stack = arcanaItem.addCrafter(arcanaItem.getNewItem(), null, 2, context.getLevel().getServer());
            arcanaItem.buildItemLore(stack, context.getLevel().getServer());
            return stack;
         }
      }
      
      return stack;
   }
}