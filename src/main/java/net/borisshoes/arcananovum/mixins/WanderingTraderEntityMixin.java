package net.borisshoes.arcananovum.mixins;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.items.normal.ArcaneNotesItem;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.borislib.utils.AlgoUtils;
import net.minecraft.entity.passive.WanderingTraderEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Pair;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradedItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(WanderingTraderEntity.class)
public class WanderingTraderEntityMixin {
   
   @Inject(method = "fillRecipes", at = @At("TAIL"))
   private void arcananovum$addArcaneNotesTrade(CallbackInfo ci){
      WanderingTraderEntity trader = (WanderingTraderEntity) (Object) this;
      if(trader.getRandom().nextDouble() < 0.1){
         List<RegistryEntry<Item>> items = new ArrayList<>();
         Registry<Item> itemRegistry = trader.getRegistryManager().getOrThrow(RegistryKeys.ITEM);
         itemRegistry.iterateEntries(ArcanaRegistry.WORKSHOP_ITEMS).forEach(items::add);
         
         List<Pair<ArcanaRarity,Integer>> weights = List.of(new Pair<>(ArcanaRarity.MUNDANE,10),new Pair<>(ArcanaRarity.EMPOWERED,25),new Pair<>(ArcanaRarity.EXOTIC,15),new Pair<>(ArcanaRarity.SOVEREIGN,5),new Pair<>(ArcanaRarity.DIVINE,1));
         ArcanaRarity rarity = AlgoUtils.getWeightedOption(weights,trader.getRandom().nextLong());
         items = items.stream().filter(entry -> {
            ArcanaItem arcanaItem = ArcanaItemUtils.getItemFromId(entry.getIdAsString());
            return arcanaItem != null && arcanaItem.getRarity() == rarity;
         }).toList();
         if(items.isEmpty()) return;
         
         ItemStack notes = new ItemStack(ArcanaRegistry.ARCANE_NOTES);
         ArcanaItem arcanaItem = ArcanaItemUtils.getItemFromId(items.get(trader.getRandom().nextInt(items.size())).getIdAsString());
         String arcanaId = arcanaItem.getId();
         ArcanaItem.putProperty(notes, ArcaneNotesItem.UNLOCK_ID_TAG,arcanaId);
         ArcaneNotesItem.buildLore(notes);
         
         ItemStack waystone = ArcanaRegistry.WAYSTONE.getNewItem();
         ArcanaRegistry.WAYSTONE.addCrafter(waystone,null,2,trader.getServer());
         
         TradeOffer offer1 = new TradeOffer(new TradedItem(Items.EMERALD_BLOCK, trader.getRandom().nextBetween(10,16)), notes.copy(), 1, 1, 0.05f);
         TradeOffer offer2 = new TradeOffer(new TradedItem(ArcanaRegistry.STARDUST, trader.getRandom().nextBetween(3,16)), new ItemStack(Items.EMERALD,trader.getRandom().nextBetween(1,24)), 12, 1, 0.05f);
         TradeOffer offer3 = new TradeOffer(new TradedItem(Items.EMERALD, trader.getRandom().nextBetween(32,64)),waystone, 1, 1, 0.05f);
         trader.getOffers().add(offer1);
         trader.getOffers().add(offer2);
         trader.getOffers().add(offer3);
      }
   }
}
