package net.borisshoes.arcananovum.gui.astralgateway;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.blocks.astralgateway.AstralGateway;
import net.borisshoes.arcananovum.blocks.astralgateway.AstralGatewayBlockEntity;
import net.borisshoes.arcananovum.blocks.astralgateway.GatewayMode;
import net.borisshoes.arcananovum.blocks.astralgateway.GatewayState;
import net.borisshoes.arcananovum.gui.WaystoneSlot;
import net.borisshoes.arcananovum.gui.starlightforge.StardustSlot;
import net.borisshoes.arcananovum.utils.ArcanaColors;
import net.borisshoes.borislib.gui.GraphicalItem;
import net.borisshoes.borislib.utils.MinecraftUtils;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Tuple;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public class AstralGatewayGui extends SimpleGui {
   
   private final AstralGatewayBlockEntity gateway;
   
   public AstralGatewayGui(ServerPlayer player, AstralGatewayBlockEntity gateway){
      super(MenuType.GENERIC_9x6, player, false);
      this.gateway = gateway;
      build();
      open();
   }
   
   public void build(){
      rebuildFrame();
      
      GuiElementBuilder arrow = GuiElementBuilder.from(GraphicalItem.with(GraphicalItem.RIGHT_ARROW));
      arrow.setName(Component.literal("Insert Stardust here or via hopper").withStyle(ChatFormatting.GOLD));
      setSlot(6,arrow);
      
      for(int i = 0; i < gateway.getContainerSize(); i++){
         if(i == 0){
            continue;
         }else if(i == 1){
            setSlotRedirect(7, new StardustSlot(gateway.getInventory(),i,i,0));
         }else{
            int adjustedI = i-2;
            int guiIndex = 19+(adjustedI%7)+9*(adjustedI/7);
            WaystoneSlot slot = new WaystoneSlot(gateway.getInventory(),i,i,0).withForGateway(gateway.getBlockPos()).withAttunement(true);
            if(!this.gateway.isAstralStargate()){
               slot = slot.withMatchedWorld(this.gateway.getLevel().dimension());
            }
            setSlotRedirect(guiIndex,slot);
         }
      }
   }
   
   private void rebuildFrame(){
      Tuple<Component,Integer> info = getStateInfo();
      this.setSlot(9, GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_LEFT_CONNECTOR_LIGHT, info.getB())).setName(info.getA()));
      this.setSlot(17, GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_RIGHT_CONNECTOR_LIGHT, info.getB())).setName(info.getA()));
      this.setSlot(11, GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_BOTTOM_CONNECTOR_LIGHT, info.getB())).setName(info.getA()));
      this.setSlot(12, GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_BOTTOM_CONNECTOR_LIGHT, info.getB())).setName(info.getA()));
      this.setSlot(14, GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_BOTTOM_CONNECTOR_LIGHT, info.getB())).setName(info.getA()));
      this.setSlot(15, GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_BOTTOM_CONNECTOR_LIGHT, info.getB())).setName(info.getA()));
      this.setSlot(10, GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_HORIZONTAL_INVERTED, info.getB())).setName(info.getA()));
      this.setSlot(13, GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_HORIZONTAL_INVERTED, info.getB())).setName(info.getA()));
      this.setSlot(16, GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_HORIZONTAL_INVERTED, info.getB())).setName(info.getA()));
      this.setSlot(18, GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_VERTICAL_INVERTED, info.getB())).setName(info.getA()));
      this.setSlot(27, GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_VERTICAL_INVERTED, info.getB())).setName(info.getA()));
      this.setSlot(36, GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_VERTICAL_INVERTED, info.getB())).setName(info.getA()));
      this.setSlot(45, GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_BOTTOM_LEFT_LIGHT, info.getB())).setName(info.getA()));
      this.setSlot(26, GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_VERTICAL_INVERTED, info.getB())).setName(info.getA()));
      this.setSlot(35, GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_VERTICAL_INVERTED, info.getB())).setName(info.getA()));
      this.setSlot(44, GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_VERTICAL_INVERTED, info.getB())).setName(info.getA()));
      this.setSlot(53, GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_BOTTOM_RIGHT_LIGHT, info.getB())).setName(info.getA()));
      this.setSlot(46, GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_HORIZONTAL_INVERTED, info.getB())).setName(info.getA()));
      this.setSlot(47, GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_HORIZONTAL_INVERTED, info.getB())).setName(info.getA()));
      this.setSlot(48, GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_HORIZONTAL_INVERTED, info.getB())).setName(info.getA()));
      this.setSlot(49, GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_HORIZONTAL_INVERTED, info.getB())).setName(info.getA()));
      this.setSlot(50, GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_HORIZONTAL_INVERTED, info.getB())).setName(info.getA()));
      this.setSlot(51, GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_HORIZONTAL_INVERTED, info.getB())).setName(info.getA()));
      this.setSlot(52, GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_HORIZONTAL_INVERTED, info.getB())).setName(info.getA()));
      
      GuiElementBuilder receptacleLeft = GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_TOP_LEFT_LIGHT, info.getB()));
      receptacleLeft.setName(Component.literal("Insert a Gateway-targeting Waystone").withStyle(ChatFormatting.AQUA));
      setSlot(3,receptacleLeft);
      
      GuiElementBuilder receptacleRight = GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_TOP_RIGHT_LIGHT, info.getB()));
      receptacleRight.setName(Component.literal("Insert a Gateway-targeting Waystone").withStyle(ChatFormatting.AQUA));
      setSlot(5,receptacleRight);
      
      GuiElementBuilder modeLeft = GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_TOP_LEFT_LIGHT, info.getB()));
      modeLeft.setName(Component.literal("Gateway Mode Controls").withStyle(ChatFormatting.DARK_PURPLE));
      setSlot(0,modeLeft);
      
      GuiElementBuilder modeRight = GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_TOP_RIGHT_LIGHT, info.getB()));
      modeRight.setName(Component.literal("Gateway Mode Controls").withStyle(ChatFormatting.DARK_PURPLE));
      setSlot(2,modeRight);
      
      GatewayState state = gateway.getBlockState().getValue(AstralGateway.AstralGatewayBlock.STATE);
      if(state == GatewayState.OPEN){
         GuiElementBuilder forceClose = GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.CANCEL_COLOR,ArcanaColors.ARCANA_COLOR));
         forceClose.setName(Component.literal("Close Gateway").withStyle(ChatFormatting.RED));
         forceClose.addLoreLine(Component.literal("Click ").withStyle(ChatFormatting.DARK_RED).append(Component.literal("to close the gateway").withStyle(ChatFormatting.RED)));
         forceClose.setCallback((type) -> {
            GatewayState curState = gateway.getBlockState().getValue(AstralGateway.AstralGatewayBlock.STATE);
            if(curState != GatewayState.OPEN){
               build();
               return;
            }
            gateway.setState(GatewayState.COOLDOWN);
            AstralGatewayBlockEntity synced = gateway.getSyncedGateway();
            if(synced != null) synced.setState(GatewayState.COOLDOWN);
            build();
         });
         setSlot(1,forceClose);
      }else{
         GatewayMode mode = gateway.getBlockState().getValue(AstralGateway.AstralGatewayBlock.MODE);
         GuiElementBuilder modeSwitch = new GuiElementBuilder();
         modeSwitch.setName(Component.literal("Gateway Mode").withStyle(ChatFormatting.GREEN));
         modeSwitch.addLoreLine(Component.literal("Click ").withStyle(ChatFormatting.AQUA).append(Component.literal("to cycle the gateway mode").withStyle(ChatFormatting.DARK_PURPLE)));
         modeSwitch.addLoreLine(Component.literal("Shift Click ").withStyle(ChatFormatting.DARK_AQUA).append(Component.literal("to reverse-cycle the gateway mode").withStyle(ChatFormatting.DARK_PURPLE)));
         modeSwitch.addLoreLine(Component.literal(""));
         if(mode == GatewayMode.BOTH){
            modeSwitch.setItem(Items.ENDER_EYE);
            modeSwitch.addLoreLine(Component.literal("Current Mode: ").withStyle(ChatFormatting.GREEN).append(Component.literal("Both - Gateway can open and be opened by other Gateways").withStyle(ChatFormatting.DARK_PURPLE)));
         }else if(mode == GatewayMode.RECEIVE_ONLY){
            modeSwitch.setItem(Items.ENDER_PEARL);
            modeSwitch.addLoreLine(Component.literal("Current Mode: ").withStyle(ChatFormatting.GREEN).append(Component.literal("Receive Only - Gateway only be opened by other Gateways").withStyle(ChatFormatting.DARK_PURPLE)));
         }else{
            modeSwitch.setItem(Items.COPPER_BARS.oxidized());
            modeSwitch.addLoreLine(Component.literal("Current Mode: ").withStyle(ChatFormatting.GREEN).append(Component.literal("Send Only - Gateway can only open other Gateways").withStyle(ChatFormatting.DARK_PURPLE)));
         }
         modeSwitch.setCallback((type) -> {
            if(mode == GatewayMode.BOTH){
               gateway.getLevel().setBlock(gateway.getBlockPos(),gateway.getBlockState().setValue(AstralGateway.AstralGatewayBlock.MODE,type.shift ? GatewayMode.SEND_ONLY : GatewayMode.RECEIVE_ONLY), Block.UPDATE_ALL);
            }else if(mode == GatewayMode.RECEIVE_ONLY){
               gateway.getLevel().setBlock(gateway.getBlockPos(),gateway.getBlockState().setValue(AstralGateway.AstralGatewayBlock.MODE,type.shift ? GatewayMode.BOTH : GatewayMode.SEND_ONLY), Block.UPDATE_ALL);
            }else{
               gateway.getLevel().setBlock(gateway.getBlockPos(),gateway.getBlockState().setValue(AstralGateway.AstralGatewayBlock.MODE,type.shift ? GatewayMode.RECEIVE_ONLY : GatewayMode.BOTH), Block.UPDATE_ALL);
            }
            build();
         });
         setSlot(1,modeSwitch);
      }
      
      long stardust = gateway.getStardust();
      int stardustPerMin = gateway.getStardustPerMinute();
      int openingStardust = gateway.getOpeningStardust();
      GuiElementBuilder stardustCount = GuiElementBuilder.from(MinecraftUtils.removeLore(ArcanaRegistry.STARDUST.getDefaultInstance()));
      stardustCount.setName(Component.literal("Stored Stardust: "+TextUtils.readableInt((int) stardust)).withStyle(ChatFormatting.GOLD));
      stardustCount.addLoreLine(Component.literal("Maintaining the Gateway takes ").withStyle(ChatFormatting.YELLOW)
            .append(Component.literal(""+stardustPerMin).withStyle(ChatFormatting.GOLD))
            .append(Component.literal(" Stardust per minute").withStyle(ChatFormatting.YELLOW)));
      stardustCount.addLoreLine(Component.literal("Opening the Gateway requires at least ").withStyle(ChatFormatting.YELLOW)
            .append(Component.literal(""+openingStardust).withStyle(ChatFormatting.GOLD))
            .append(Component.literal(" stored Stardust").withStyle(ChatFormatting.YELLOW)));
      if(stardust == 0){
         stardustCount.setItem(Items.GUNPOWDER);
         stardustCount.addLoreLine(Component.literal(""));
         stardustCount.addLoreLine(Component.literal("There is no stored Stardust!").withStyle(ChatFormatting.RED));
      }else if(stardust < openingStardust){
         stardustCount.setItem(Items.REDSTONE);
         stardustCount.addLoreLine(Component.literal(""));
         stardustCount.addLoreLine(Component.literal("There is not enough stored Stardust to open the Gateway!").withStyle(ChatFormatting.RED));
      }
      setSlot(8,stardustCount);
      
      boolean hasFrame = gateway.getFrame() != null && gateway.getFrame().finishedAndValid();
      if(hasFrame && !(getSlotRedirect(4) instanceof WaystoneSlot)){
         clearSlot(4);
         WaystoneSlot slot = new WaystoneSlot(gateway.getInventory(),0,0,0).withForGateway(gateway.getBlockPos()).withAttunement(true);
         if(!this.gateway.isAstralStargate()){
            slot = slot.withMatchedWorld(this.gateway.getLevel().dimension());
         }
         setSlotRedirect(4,slot);
      }else if(!hasFrame){
         clearSlot(4);
         GuiElementBuilder frameFind = new GuiElementBuilder(Items.SPYGLASS).hideDefaultTooltip();
         frameFind.setName(Component.literal("No Valid Frame").withStyle(ChatFormatting.RED));
         frameFind.addLoreLine(Component.literal("Click ").withStyle(ChatFormatting.AQUA).append(Component.literal("to have the Gateway search for a frame").withStyle(ChatFormatting.YELLOW)));
         frameFind.setCallback((type) -> {
            gateway.tryFind();
         });
         setSlot(4,frameFind);
      }
   }
   
   private Tuple<Component,Integer> getStateInfo(){
      boolean hasStardust = gateway.getStardust() >= gateway.getOpeningStardust();
      boolean hasFrame = gateway.getFrame() != null && gateway.getFrame().finishedAndValid();
      boolean hasWaystone = !gateway.getInventory().getItem(0).isEmpty();
      boolean hasRedstone = gateway.getLevel().hasNeighborSignal(gateway.getBlockPos());
      GatewayState state = gateway.getBlockState().getValue(AstralGateway.AstralGatewayBlock.STATE);
      if(state == GatewayState.OPEN || state == GatewayState.LOCKED_OPEN){
         return new Tuple<>(Component.literal("Gateway Open").withStyle(ChatFormatting.DARK_PURPLE), ArcanaColors.STARLIGHT_FORGE_COLOR);
      }
      if(state == GatewayState.WARMUP){
         return new Tuple<>(Component.literal("Gateway Warming Up").withStyle(ChatFormatting.DARK_PURPLE), ArcanaColors.STARLIGHT_FORGE_COLOR);
      }
      if(state == GatewayState.COOLDOWN){
         return new Tuple<>(Component.literal("Gateway Cooling Down").withStyle(ChatFormatting.DARK_PURPLE), ArcanaColors.STARLIGHT_FORGE_COLOR);
      }
      
      if(!hasStardust){
         return new Tuple<>(Component.literal("Not Enough Stardust").withStyle(ChatFormatting.RED), ArcanaColors.ERROR_COLOR);
      }
      if(!hasFrame){
         return new Tuple<>(Component.literal("No Valid Frame").withStyle(ChatFormatting.RED), ArcanaColors.ERROR_COLOR);
      }
      if(!hasWaystone){
         return new Tuple<>(Component.literal("Use a Waystone to Sync to Another Gateway").withStyle(ChatFormatting.DARK_AQUA), ArcanaColors.ARCANE_PAGE_COLOR);
      }
      if(!hasRedstone){
         return new Tuple<>(Component.literal("Use a Redstone Signal to Activate the Gateway").withStyle(ChatFormatting.DARK_AQUA), ArcanaColors.ARCANE_PAGE_COLOR);
      }
      
      GatewayMode mode = gateway.getBlockState().getValue(AstralGateway.AstralGatewayBlock.MODE);
      if(mode == GatewayMode.RECEIVE_ONLY){
         return new Tuple<>(Component.literal("Ready to Receive").withStyle(ChatFormatting.LIGHT_PURPLE), ArcanaColors.STARLIGHT_FORGE_COLOR);
      }
      
      return new Tuple<>(Component.literal("Cannot Find Other Gateway").withStyle(ChatFormatting.RED), ArcanaColors.ERROR_COLOR);
   }
   
   @Override
   public void onTick(){
      Level world = gateway.getLevel();
      if(world == null || world.getBlockEntity(gateway.getBlockPos()) != gateway){
         this.close();
      }else{
         if(world.getServer().getTickCount() % 10 == 0){
            rebuildFrame();
         }
      }
      super.onTick();
   }
}
