package net.borisshoes.arcananovum.core.polymer;

import eu.pb4.factorytools.api.util.LazyItemStack;
import eu.pb4.factorytools.api.virtualentity.BlockModel;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import eu.pb4.polymer.virtualentity.api.attachment.BlockBoundAttachment;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import eu.pb4.polymer.virtualentity.api.elements.VirtualElement;
import net.borisshoes.arcananovum.core.ArcanaBlockEntity;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.minecraft.core.BlockPos;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class PackAwareBlockModel extends BlockModel {
   
   private String skin = "";
   
   public static LazyItemStack part(String part){
      return new LazyItemStack(() -> BlockPartItem.getPartStack(part));
   }
   
   public static void refreshSkin(Level world, BlockPos pos){
      BlockBoundAttachment attachment = BlockBoundAttachment.get(world, pos);
      if(attachment != null && attachment.holder() instanceof PackAwareBlockModel model) model.refreshSkin();
   }
   
   public void refreshSkin(){
      if(!(this.getAttachment() instanceof BlockBoundAttachment attachment)) return;
      if(!(attachment.getChunk().getBlockEntity(attachment.getBlockPos()) instanceof ArcanaBlockEntity arcanaBlock)) return;
      String newSkin = arcanaBlock.getSkin() == null ? "" : arcanaBlock.getSkin();
      if(newSkin.equals(this.skin)) return;
      this.skin = newSkin;
      for(VirtualElement element : this.getElements()){
         if(applySkin(element)) element.tick();
      }
   }
   
   private boolean applySkin(VirtualElement element){
      if(!(element instanceof ItemDisplayElement display)) return false;
      ItemStack stack = display.getItem();
      if(!BlockPartItem.isPart(stack) || this.skin.equals(ArcanaItem.getStringProperty(stack, ArcanaItem.SKIN_TAG))) return false;
      ItemStack skinned = stack.copy();
      ArcanaItem.putProperty(skinned, ArcanaItem.SKIN_TAG, this.skin);
      display.setItem(skinned);
      return true;
   }
   
   @Override
   public <T extends VirtualElement> T addElement(T element){
      applySkin(element);
      return super.addElement(element);
   }
   
   @Override
   public boolean startWatching(ServerGamePacketListenerImpl player){
      if(PolymerResourcePackUtils.hasMainPack(player)){
         refreshSkin();
         return super.startWatching(player);
      }else{
         return false;
      }
   }
   
   @Override
   public void tick(){
      if(this.getTick() % 20 == 0) refreshSkin();
      if(!this.skin.isEmpty()){
         for(VirtualElement element : this.getElements()){
            applySkin(element);
         }
      }
      super.tick();
   }
}
