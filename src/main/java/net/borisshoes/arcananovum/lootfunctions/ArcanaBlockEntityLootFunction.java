package net.borisshoes.arcananovum.lootfunctions;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.augments.ArcanaAugment;
import net.borisshoes.arcananovum.blocks.EnderCrate;
import net.borisshoes.arcananovum.blocks.EnderCrateBlockEntity;
import net.borisshoes.arcananovum.blocks.altars.StarpathAltar;
import net.borisshoes.arcananovum.blocks.altars.StarpathAltarBlockEntity;
import net.borisshoes.arcananovum.blocks.astralgateway.AstralGateway;
import net.borisshoes.arcananovum.blocks.astralgateway.AstralGatewayBlockEntity;
import net.borisshoes.arcananovum.blocks.forge.ArcaneSingularity;
import net.borisshoes.arcananovum.blocks.forge.ArcaneSingularityBlockEntity;
import net.borisshoes.arcananovum.blocks.forge.StarlightForge;
import net.borisshoes.arcananovum.blocks.forge.StarlightForgeBlockEntity;
import net.borisshoes.arcananovum.core.ArcanaBlockEntity;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.datastorage.EnderCrateChannel;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;
import java.util.Map;
import java.util.UUID;

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
      BlockEntity blockEntity = context.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
      if(!(blockEntity instanceof ArcanaBlockEntity arcanaBlockEntity)) return stack;
      ServerLevel world = context.getLevel();
      
      String uuid = arcanaBlockEntity.getUuid();
      if(uuid == null) uuid = UUID.randomUUID().toString();
      CompoundTag augmentsTag = new CompoundTag();
      if(arcanaBlockEntity.getAugments() != null){
         for(Map.Entry<ArcanaAugment, Integer> entry : arcanaBlockEntity.getAugments().entrySet()){
            augmentsTag.putInt(entry.getKey().id, entry.getValue());
         }
      }else{
         augmentsTag = null;
      }
      
      ArcanaItem arcanaItem = arcanaBlockEntity.getArcanaItem();
      arcanaItem.initializeArcanaTag(stack,false);
      
      stack = arcanaItem.addCrafter(stack, arcanaBlockEntity.getCrafterId(), arcanaBlockEntity.getOrigin(),world.getServer());
      
      if(augmentsTag != null){
         ArcanaItem.putProperty(stack, ArcanaItem.AUGMENTS_TAG,augmentsTag);
      }
      
      ArcanaItem.putProperty(stack, ArcanaItem.UUID_TAG,uuid);
      
      if(arcanaBlockEntity.getCustomArcanaName() != null && !arcanaBlockEntity.getCustomArcanaName().isEmpty()){
         stack.set(DataComponents.CUSTOM_NAME, Component.literal(arcanaBlockEntity.getCustomArcanaName()));
      }
      
      if(arcanaBlockEntity instanceof ArcaneSingularityBlockEntity singularity){
         ArcanaItem.putProperty(stack, ArcaneSingularity.BOOKS_TAG,singularity.saveBooks(world.registryAccess()));
      }
      if(arcanaBlockEntity instanceof StarpathAltarBlockEntity altar){
         ArcanaItem.putProperty(stack, StarpathAltar.TARGETS_TAG,altar.writeTargets());
      }
      if(arcanaBlockEntity instanceof StarlightForgeBlockEntity forge){
         ArcanaItem.putProperty(stack, StarlightForge.SEED_USES_TAG,forge.getSeedUses());
      }
      if(arcanaBlockEntity instanceof AstralGatewayBlockEntity gateway){
         ArcanaItem.putProperty(stack, AstralGateway.WAYSTONES_TAG,gateway.saveStones(world.registryAccess()));
         ArcanaItem.putProperty(stack, AstralGateway.STARDUST_TAG,gateway.getStardust());
      }
      if(arcanaBlockEntity instanceof EnderCrateBlockEntity crate){
         EnderCrateChannel channel = crate.getChannel();
         ArcanaItem.putProperty(stack, EnderCrate.CHANNEL_TAG, EnderCrate.colorsToTag(channel.getColors()));
         ArcanaItem.putProperty(stack, EnderCrate.LOCK_TAG, channel.isLocked() ? channel.getIdLock().toString() : "");
      }
      
      arcanaItem.buildItemLore(stack,world.getServer());
      
      return stack;
   }
}