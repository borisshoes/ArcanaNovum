package net.borisshoes.arcananovum.lootfunctions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.items.normal.ArcaneNotesItem;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.borislib.utils.AlgoUtils;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.registry.RegistryCodecs;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.util.Pair;
import net.minecraft.util.math.random.Random;

import java.util.List;

public class ArcaneNotesLootFunction extends ConditionalLootFunction {
   public static final MapCodec<ArcaneNotesLootFunction> CODEC = RecordCodecBuilder.mapCodec(
         instance -> addConditionsField(instance)
               .and(
                     instance.group(
                           RegistryCodecs.entryList(RegistryKeys.ITEM).fieldOf("items").forGetter(function -> function.itemTag),
                           Codec.INT.fieldOf("mundane_weight").forGetter(function -> function.mundaneWeight),
                           Codec.INT.fieldOf("empowered_weight").forGetter(function -> function.empoweredWeight),
                           Codec.INT.fieldOf("exotic_weight").forGetter(function -> function.exoticWeight),
                           Codec.INT.fieldOf("sovereign_weight").forGetter(function -> function.sovereignWeight),
                           Codec.INT.fieldOf("divine_weight").forGetter(function -> function.divineWeight)
                     )
               )
               .apply(instance, ArcaneNotesLootFunction::new)
   );
   
   private final RegistryEntryList<Item> itemTag;
   private final int mundaneWeight,empoweredWeight,exoticWeight,sovereignWeight,divineWeight;
   
   protected ArcaneNotesLootFunction(List<LootCondition> conditions, RegistryEntryList<Item> itemTag, int mundaneWeight, int empoweredWeight, int exoticWeight, int sovereignWeight, int divineWeight){
      super(conditions);
      this.itemTag = itemTag;
      this.mundaneWeight = mundaneWeight;
      this.empoweredWeight = empoweredWeight;
      this.exoticWeight = exoticWeight;
      this.sovereignWeight = sovereignWeight;
      this.divineWeight = divineWeight;
   }
   
   
   @SuppressWarnings("unchecked")
   @Override
   public LootFunctionType<? extends ConditionalLootFunction> getType(){
      return (LootFunctionType<ArcaneNotesLootFunction>) ArcanaRegistry.ARCANE_NOTES_LOOT_FUNCTION;
   }
   
   @Override
   protected ItemStack process(ItemStack stack, LootContext context){
      Random random = context.getRandom();
      if(!stack.isOf(ArcanaRegistry.ARCANE_NOTES)) return stack;
      
      List<Pair<ArcanaRarity,Integer>> weights = List.of(new Pair<>(ArcanaRarity.MUNDANE,mundaneWeight),new Pair<>(ArcanaRarity.EMPOWERED,empoweredWeight),new Pair<>(ArcanaRarity.EXOTIC,exoticWeight),new Pair<>(ArcanaRarity.SOVEREIGN,sovereignWeight),new Pair<>(ArcanaRarity.DIVINE,divineWeight));
      ArcanaRarity rarity = AlgoUtils.getWeightedOption(weights,random.nextLong());
      List<RegistryEntry<Item>> items = this.itemTag.stream().filter(entry -> {
         ArcanaItem arcanaItem = ArcanaItemUtils.getItemFromId(entry.getIdAsString());
         return arcanaItem != null && arcanaItem.getRarity() == rarity;
      }).toList();
      
      if(items.isEmpty()) return stack;
      ArcanaItem arcanaItem = ArcanaItemUtils.getItemFromId(items.get(random.nextInt(items.size())).getIdAsString());
      String arcanaId = arcanaItem.getId();
      ArcanaItem.putProperty(stack, ArcaneNotesItem.UNLOCK_ID_TAG,arcanaId);
      ArcaneNotesItem.buildLore(stack);
      
      return stack;
   }
}
