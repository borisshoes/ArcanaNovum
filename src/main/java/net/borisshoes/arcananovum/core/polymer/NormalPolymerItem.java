package net.borisshoes.arcananovum.core.polymer;

import eu.pb4.polymer.core.api.item.PolymerItem;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public abstract class NormalPolymerItem extends Item implements PolymerItem {
   
   private final String id;
   
   public NormalPolymerItem(String id, Settings settings){
      super(settings.registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID,id))));
      this.id = id;
   }
   
   @Override
   public @Nullable Identifier getPolymerItemModel(ItemStack stack, PacketContext context){
      if(PolymerResourcePackUtils.hasMainPack(context)){
         return Identifier.of(MOD_ID,this.id);
      }else{
         return Registries.ITEM.getKey(getPolymerItem(stack,context)).get().getValue();
      }
   }
   
   @Override
   public abstract Item getPolymerItem(ItemStack itemStack, PacketContext context);
}
