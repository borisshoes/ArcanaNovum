package net.borisshoes.arcananovum.core.polymer;

import eu.pb4.polymer.resourcepack.extras.api.ResourcePackExtras;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.skins.ArcanaSkin;
import net.borisshoes.arcananovum.skins.ArcanaSkins;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;

public class BlockPartItem extends NormalPolymerItem {
   
   public static final String PART_TAG = "part";
   
   public BlockPartItem(String id, Item.Properties settings){
      super(id, settings);
   }
   
   public static ItemStack getPartStack(String part){
      ItemStack stack = new ItemStack(ArcanaRegistry.BLOCK_PART);
      ArcanaItem.putProperty(stack, PART_TAG, part);
      return stack;
   }
   
   public static boolean isPart(ItemStack stack){
      return stack != null && stack.is(ArcanaRegistry.BLOCK_PART);
   }
   
   @Override
   public Item getPolymerItem(ItemStack itemStack, PacketContext context){
      return Items.TRIAL_KEY;
   }
   
   @Override
   public Identifier getPolymerItemModel(ItemStack stack, PacketContext context, HolderLookup.Provider lookup){
      String part = ArcanaItem.getStringProperty(stack, PART_TAG);
      ArcanaSkin skin = ArcanaSkin.getSkinFromString(ArcanaItem.getStringProperty(stack, ArcanaItem.SKIN_TAG));
      if(skin != null && ArcanaSkins.playerHasDataForSkin(context, skin)){
         Identifier skinned = skin.getBlockPartModel(part);
         if(skinned != null) return ResourcePackExtras.bridgeModel(skinned);
      }
      return ResourcePackExtras.bridgeModel(ArcanaRegistry.arcanaId("block/" + part));
   }
   
   @Override
   public ItemStack getPolymerItemStack(ItemStack itemStack, TooltipFlag tooltipType, PacketContext context, HolderLookup.Provider lookup){
      ItemStack out = new ItemStack(Items.TRIAL_KEY);
      out.set(DataComponents.ITEM_MODEL, getPolymerItemModel(itemStack, context, lookup));
      return out;
   }
}
