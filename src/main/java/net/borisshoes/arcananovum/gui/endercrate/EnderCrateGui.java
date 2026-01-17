package net.borisshoes.arcananovum.gui.endercrate;

import eu.pb4.sgui.api.gui.SimpleGui;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.blocks.EnderCrateBlockEntity;
import net.borisshoes.arcananovum.datastorage.EnderCrateChannel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.level.Level;

public class EnderCrateGui extends SimpleGui {
   
   private final EnderCrateChannel channel;
   private EnderCrateBlockEntity block;
   
   public EnderCrateGui(ServerPlayer player, EnderCrateBlockEntity block){
      this(player,block.getChannel(),block.getBandwidth());
      this.block = block;
   }
   
   public EnderCrateGui(ServerPlayer player, EnderCrateChannel channel, int bandwidth){
      super(getMenuFromBandwidth(bandwidth), player, false);
      this.channel = channel;
      setTitle(ArcanaRegistry.ENDER_CRATE.getTranslatedName());
      build();
   }
   
   public void build(){
      for(int i = 0; i < size; i++){
         setSlotRedirect(i,new Slot(channel.getInventory(),i,i%9,i/9));
      }
   }
   
   private static MenuType<?> getMenuFromBandwidth(int i){
      if(i == 0) return MenuType.GENERIC_9x3;
      if(i == 1) return MenuType.GENERIC_9x4;
      if(i == 2) return MenuType.GENERIC_9x5;
      if(i == 3) return MenuType.GENERIC_9x6;
      return MenuType.GENERIC_9x6;
   }
   
   @Override
   public void onTick(){
      if(block != null){
         Level world = block.getLevel();
         if(world == null || world.getBlockEntity(block.getBlockPos()) != block){
            this.close();
         }
      }
      super.onTick();
   }
}
