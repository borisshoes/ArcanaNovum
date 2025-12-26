package net.borisshoes.arcananovum.core.polymer;

import eu.pb4.polymer.core.api.item.PolymerItem;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public abstract class NormalPolymerItem extends Item implements PolymerItem {
   
   private final String id;
   
   public NormalPolymerItem(String id, Item.Properties settings){
      super(settings.setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID,id))));
      this.id = id;
   }
   
   @Override
   public @Nullable Identifier getPolymerItemModel(ItemStack stack, PacketContext context){
      if(PolymerResourcePackUtils.hasMainPack(context)){
         return Identifier.fromNamespaceAndPath(MOD_ID,this.id);
      }else{
         return BuiltInRegistries.ITEM.getResourceKey(getPolymerItem(stack,context)).get().identifier();
      }
   }
   
   @Override
   public abstract Item getPolymerItem(ItemStack itemStack, PacketContext context);
}
