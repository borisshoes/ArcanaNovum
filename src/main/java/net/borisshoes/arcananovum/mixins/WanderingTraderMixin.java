package net.borisshoes.arcananovum.mixins;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.items.normal.ArcaneNotesItem;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.borislib.utils.AlgoUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTrader;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(WanderingTrader.class)
public class WanderingTraderMixin {
   
   @Inject(method = "updateTrades", at = @At("TAIL"))
   private void arcananovum$addArcaneNotesTrade(CallbackInfo ci){
      WanderingTrader trader = (WanderingTrader) (Object) this;
      if(trader.getRandom().nextDouble() < 0.1){
         List<Holder<Item>> items = new ArrayList<>();
         Registry<Item> itemRegistry = trader.registryAccess().lookupOrThrow(Registries.ITEM);
         itemRegistry.getTagOrEmpty(ArcanaRegistry.WORKSHOP_ITEMS).forEach(items::add);
         
         List<Tuple<ArcanaRarity, Integer>> weights = List.of(new Tuple<>(ArcanaRarity.MUNDANE, 10), new Tuple<>(ArcanaRarity.EMPOWERED, 25), new Tuple<>(ArcanaRarity.EXOTIC, 15), new Tuple<>(ArcanaRarity.SOVEREIGN, 5), new Tuple<>(ArcanaRarity.DIVINE, 1));
         ArcanaRarity rarity = AlgoUtils.getWeightedOption(weights, trader.getRandom().nextLong());
         items = items.stream().filter(entry -> {
            ArcanaItem arcanaItem = ArcanaItemUtils.getItemFromId(entry.getRegisteredName());
            return arcanaItem != null && arcanaItem.getRarity() == rarity;
         }).toList();
         if(items.isEmpty()) return;
         
         ItemStack notes = new ItemStack(ArcanaRegistry.ARCANE_NOTES);
         ArcanaItem arcanaItem = ArcanaItemUtils.getItemFromId(items.get(trader.getRandom().nextInt(items.size())).getRegisteredName());
         String arcanaId = arcanaItem.getId();
         ArcanaItem.putProperty(notes, ArcaneNotesItem.UNLOCK_ID_TAG, arcanaId);
         ArcaneNotesItem.buildLore(notes);
         
         ItemStack waystone = ArcanaRegistry.WAYSTONE.getNewItem();
         ArcanaRegistry.WAYSTONE.addCrafter(waystone, null, 2, trader.level().getServer());
         
         MerchantOffer offer1 = new MerchantOffer(new ItemCost(Items.EMERALD_BLOCK, trader.getRandom().nextIntBetweenInclusive(10, 16)), notes.copy(), 1, 1, 0.05f);
         MerchantOffer offer2 = new MerchantOffer(new ItemCost(ArcanaRegistry.STARDUST, trader.getRandom().nextIntBetweenInclusive(3, 16)), new ItemStack(Items.EMERALD, trader.getRandom().nextIntBetweenInclusive(1, 24)), 12, 1, 0.05f);
         MerchantOffer offer3 = new MerchantOffer(new ItemCost(Items.EMERALD, trader.getRandom().nextIntBetweenInclusive(32, 64)), waystone, 1, 1, 0.05f);
         trader.getOffers().add(offer1);
         trader.getOffers().add(offer2);
         trader.getOffers().add(offer3);
      }
   }
}
