package net.borisshoes.arcananovum.lootfunctions;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.core.ArcanaBlockEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;

public class ArcanaBlockEntityLootFunction extends LootItemConditionalFunction {
   
   public static final MapCodec<ArcanaBlockEntityLootFunction> CODEC = RecordCodecBuilder.mapCodec(
         instance -> commonFields(instance).apply(instance, ArcanaBlockEntityLootFunction::new)
   );
   
   
   protected ArcanaBlockEntityLootFunction(List<LootItemCondition> conditions){
      super(conditions);
   }
   
   @SuppressWarnings("unchecked")
   @Override
   public LootItemFunctionType<? extends LootItemConditionalFunction> getType(){
      return (LootItemFunctionType<ArcanaBlockEntityLootFunction>) ArcanaRegistry.ARCANA_BLOCK_ENTITY_LOOT_FUNCTION;
   }
   
   @Override
   protected ItemStack run(ItemStack stack, LootContext context){
      return !(context.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof ArcanaBlockEntity arcanaBlockEntity) ? stack :
            ArcanaBlockEntity.getBlockEntityAsItem(arcanaBlockEntity, context.getLevel(), stack);
   }
}