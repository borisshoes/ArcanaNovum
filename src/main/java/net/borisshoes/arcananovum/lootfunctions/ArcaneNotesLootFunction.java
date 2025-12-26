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
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;

public class ArcaneNotesLootFunction extends LootItemConditionalFunction {
   public static final MapCodec<ArcaneNotesLootFunction> CODEC = RecordCodecBuilder.mapCodec(
         instance -> commonFields(instance)
               .and(
                     instance.group(
                           RegistryCodecs.homogeneousList(Registries.ITEM).fieldOf("items").forGetter(function -> function.itemTag),
                           Codec.INT.fieldOf("mundane_weight").forGetter(function -> function.mundaneWeight),
                           Codec.INT.fieldOf("empowered_weight").forGetter(function -> function.empoweredWeight),
                           Codec.INT.fieldOf("exotic_weight").forGetter(function -> function.exoticWeight),
                           Codec.INT.fieldOf("sovereign_weight").forGetter(function -> function.sovereignWeight),
                           Codec.INT.fieldOf("divine_weight").forGetter(function -> function.divineWeight)
                     )
               )
               .apply(instance, ArcaneNotesLootFunction::new)
   );
   
   private final HolderSet<Item> itemTag;
   private final int mundaneWeight,empoweredWeight,exoticWeight,sovereignWeight,divineWeight;
   
   protected ArcaneNotesLootFunction(List<LootItemCondition> conditions, HolderSet<Item> itemTag, int mundaneWeight, int empoweredWeight, int exoticWeight, int sovereignWeight, int divineWeight){
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
   public LootItemFunctionType<? extends LootItemConditionalFunction> getType(){
      return (LootItemFunctionType<ArcaneNotesLootFunction>) ArcanaRegistry.ARCANE_NOTES_LOOT_FUNCTION;
   }
   
   @Override
   protected ItemStack run(ItemStack stack, LootContext context){
      RandomSource random = context.getRandom();
      if(!stack.is(ArcanaRegistry.ARCANE_NOTES)) return stack;
      
      List<Tuple<ArcanaRarity,Integer>> weights = List.of(new Tuple<>(ArcanaRarity.MUNDANE,mundaneWeight),new Tuple<>(ArcanaRarity.EMPOWERED,empoweredWeight),new Tuple<>(ArcanaRarity.EXOTIC,exoticWeight),new Tuple<>(ArcanaRarity.SOVEREIGN,sovereignWeight),new Tuple<>(ArcanaRarity.DIVINE,divineWeight));
      ArcanaRarity rarity = AlgoUtils.getWeightedOption(weights,random.nextLong());
      List<Holder<Item>> items = this.itemTag.stream().filter(entry -> {
         ArcanaItem arcanaItem = ArcanaItemUtils.getItemFromId(entry.getRegisteredName());
         return arcanaItem != null && arcanaItem.getRarity() == rarity;
      }).toList();
      
      if(items.isEmpty()) return stack;
      ArcanaItem arcanaItem = ArcanaItemUtils.getItemFromId(items.get(random.nextInt(items.size())).getRegisteredName());
      String arcanaId = arcanaItem.getId();
      ArcanaItem.putProperty(stack, ArcaneNotesItem.UNLOCK_ID_TAG,arcanaId);
      ArcaneNotesItem.buildLore(stack);
      
      return stack;
   }
}
