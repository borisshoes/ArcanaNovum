package net.borisshoes.arcananovum.core.polymer;

import eu.pb4.polymer.core.api.item.PolymerItem;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public abstract class NormalPolymerItem extends Item implements PolymerItem {
   
   private final String id;
   
   public NormalPolymerItem(String id, Item.Properties settings){
      super(settings.setId(ResourceKey.create(Registries.ITEM, ArcanaRegistry.arcanaId(id))));
      this.id = id;
   }
   
   @Override
   public @Nullable Identifier getPolymerItemModel(ItemStack stack, PacketContext context, HolderLookup.Provider lookup){
      if(PolymerResourcePackUtils.hasMainPack(context)){
         return ArcanaRegistry.arcanaId(this.id);
      }else{
         return BuiltInRegistries.ITEM.getResourceKey(getPolymerItem(stack, context)).get().identifier();
      }
   }
   
   @Override
   public abstract Item getPolymerItem(ItemStack itemStack, PacketContext context);
}
