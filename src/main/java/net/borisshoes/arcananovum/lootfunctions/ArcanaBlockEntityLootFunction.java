package net.borisshoes.arcananovum.lootfunctions;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.augments.ArcanaAugment;
import net.borisshoes.arcananovum.blocks.altars.StarpathAltar;
import net.borisshoes.arcananovum.blocks.altars.StarpathAltarBlockEntity;
import net.borisshoes.arcananovum.blocks.forge.ArcaneSingularity;
import net.borisshoes.arcananovum.blocks.forge.ArcaneSingularityBlockEntity;
import net.borisshoes.arcananovum.blocks.forge.StarlightForge;
import net.borisshoes.arcananovum.blocks.forge.StarlightForgeBlockEntity;
import net.borisshoes.arcananovum.core.ArcanaBlockEntity;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ArcanaBlockEntityLootFunction extends ConditionalLootFunction {
   
   public static final MapCodec<ArcanaBlockEntityLootFunction> CODEC = RecordCodecBuilder.mapCodec(
         instance -> addConditionsField(instance).apply(instance, ArcanaBlockEntityLootFunction::new)
   );
   
   
   protected ArcanaBlockEntityLootFunction(List<LootCondition> conditions){
      super(conditions);
   }
   
   @SuppressWarnings("unchecked")
   @Override
   public LootFunctionType<? extends ConditionalLootFunction> getType(){
      return (LootFunctionType<ArcanaBlockEntityLootFunction>) ArcanaRegistry.ARCANA_BLOCK_ENTITY_LOOT_FUNCTION;
   }
   
   @Override
   protected ItemStack process(ItemStack stack, LootContext context){
      BlockEntity blockEntity = context.get(LootContextParameters.BLOCK_ENTITY);
      if(!(blockEntity instanceof ArcanaBlockEntity arcanaBlockEntity)) return stack;
      ServerWorld world = context.getWorld();
      
      String uuid = arcanaBlockEntity.getUuid();
      if(uuid == null) uuid = UUID.randomUUID().toString();
      NbtCompound augmentsTag = new NbtCompound();
      if(arcanaBlockEntity.getAugments() != null){
         for(Map.Entry<ArcanaAugment, Integer> entry : arcanaBlockEntity.getAugments().entrySet()){
            augmentsTag.putInt(entry.getKey().id, entry.getValue());
         }
      }else{
         augmentsTag = null;
      }
      
      ArcanaItem arcanaItem = arcanaBlockEntity.getArcanaItem();
      arcanaItem.initializeArcanaTag(stack,false);
      
      stack = arcanaItem.addCrafter(stack, arcanaBlockEntity.getCrafterId(), arcanaBlockEntity.isSynthetic(),world.getServer());
      
      if(augmentsTag != null) {
         ArcanaItem.putProperty(stack, ArcanaItem.AUGMENTS_TAG,augmentsTag);
      }
      
      ArcanaItem.putProperty(stack, ArcanaItem.UUID_TAG,uuid);
      
      if(arcanaBlockEntity.getCustomArcanaName() != null && !arcanaBlockEntity.getCustomArcanaName().isEmpty()){
         stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal(arcanaBlockEntity.getCustomArcanaName()));
      }
      
      if(arcanaBlockEntity instanceof ArcaneSingularityBlockEntity singularity){
         ArcanaItem.putProperty(stack, ArcaneSingularity.BOOKS_TAG,singularity.saveBooks(world.getRegistryManager()));
      }
      if(arcanaBlockEntity instanceof StarpathAltarBlockEntity altar){
         ArcanaItem.putProperty(stack, StarpathAltar.TARGETS_TAG,altar.writeTargets());
      }
      if(arcanaBlockEntity instanceof StarlightForgeBlockEntity forge){
         ArcanaItem.putProperty(stack, StarlightForge.SEED_USES_TAG,forge.getSeedUses());
      }
      
      arcanaItem.buildItemLore(stack,world.getServer());
      
      return stack;
   }
}