package net.borisshoes.arcananovum.gui.shulkercore;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.core.EnergyItem;
import net.borisshoes.arcananovum.gui.ContainerWatcher;
import net.borisshoes.arcananovum.gui.WatchedContainer;
import net.borisshoes.arcananovum.items.ShulkerCore;
import net.borisshoes.arcananovum.items.Soulstone;
import net.borisshoes.arcananovum.utils.ArcanaColors;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.borislib.gui.GraphicalItem;
import net.borisshoes.borislib.utils.MinecraftUtils;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ShulkerCoreGui extends SimpleGui implements ContainerWatcher {
   private ShulkerCore core;
   private ItemStack item;
   private boolean valid;
   private final WatchedContainer inv;
   private volatile boolean updating = false;
   
   public ShulkerCoreGui(ServerPlayer player, ShulkerCore core, ItemStack item){
      super(MenuType.HOPPER, player, false);
      this.core = core;
      this.item = item;
      this.inv = new WatchedContainer(1);
      this.inv.addWatcher(this);
      buildGui();
   }
   
   private void buildGui(){
      for(int i = 0; i < getSize(); i++){
         clearSlot(i);
      }
      boolean hasStone = ArcanaItem.getBooleanProperty(item, ShulkerCore.STONE_TAG);
      GuiElementBuilder pane = GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_TOP, hasStone ? ArcanaColors.ARCANA_COLOR : ArcanaColors.DARK_COLOR));
      String paneText = hasStone ? TextUtils.readableInt(EnergyItem.getEnergy(item)) + " Shulker Souls" : "No Soulstone Inserted";
      ChatFormatting textColor = hasStone ? ChatFormatting.YELLOW : ChatFormatting.RED;
      
      setSlot(0, pane.setName(Component.literal(paneText).withStyle(textColor)));
      setSlot(1, pane.setName(Component.literal(paneText).withStyle(textColor)));
      setSlot(3, pane.setName(Component.literal(paneText).withStyle(textColor)));
      setSlot(4, pane.setName(Component.literal(paneText).withStyle(textColor)));
      
      setUpdating();
      setSlot(2, new Slot(inv, 0, 0, 0));
      if(hasStone){
         CompoundTag stoneData = ArcanaItem.getCompoundProperty(item, ShulkerCore.STONE_DATA_TAG);
         ItemStack stone;
         if(stoneData == null || stoneData.isEmpty()){
            stone = Soulstone.setType(ArcanaRegistry.SOULSTONE.getNewItem(), EntityType.SHULKER);
         }else{
            stone = ItemStack.CODEC.parse(RegistryOps.create(NbtOps.INSTANCE, player.registryAccess()), stoneData).result().orElse(ItemStack.EMPTY);
         }
         stone = Soulstone.setSouls(stone, EnergyItem.getEnergy(item));
         
         inv.setItem(0, stone);
         validStone(stone);
      }else{
         notValid();
      }
      setTitle(Component.literal("Shulker Core"));
      finishUpdate();
   }
   
   @Override
   public boolean onAnyClick(int index, ClickType type, ContainerInput action){
      final int slotCount = 5;
      if(type == ClickType.OFFHAND_SWAP || action == ContainerInput.SWAP){
         close();
      }else if(index >= slotCount){
         int invSlot = index >= 27 + slotCount ? index - (27 + slotCount) : index;
         ItemStack stack = player.getInventory().getItem(invSlot);
         if(ItemStack.isSameItemSameComponents(item, stack)){
            close();
            return false;
         }
      }
      return super.onAnyClick(index, type, action);
   }
   
   @Override
   public void afterRemoval(){
      if(!valid){
         // Return invalid item
         if(!player.isDeadOrDying() && !player.isSpectator()){
            MinecraftUtils.returnItems(inv, player);
         }else{
            Containers.dropContents(player.level(), player.getOnPos().above(1), inv);
         }
         inv.clearContent();
      }
      core.buildItemLore(item, player.level().getServer());
   }
   
   public void validStone(ItemStack newStone){
      valid = true;
      core.setStone(item, newStone);
   }
   
   public void notValid(){
      valid = false;
      core.setStone(item, null);
   }
   
   @Override
   public void onChanged(WatchedContainer inv){
      if(!updating){
         updating = true;
         
         //Check Soulstone, and update item
         boolean hasStone = isValidSoulstone(inv);
         
         GuiElementBuilder pane = GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_TOP, hasStone ? ArcanaColors.ARCANA_COLOR : ArcanaColors.DARK_COLOR));
         String paneText = hasStone ? TextUtils.readableInt(EnergyItem.getEnergy(item)) + " Shulker Souls" : "No Soulstone Inserted";
         ChatFormatting textColor = hasStone ? ChatFormatting.YELLOW : ChatFormatting.RED;
         
         setSlot(0, pane.setName(Component.literal(paneText).withStyle(textColor)));
         setSlot(1, pane.setName(Component.literal(paneText).withStyle(textColor)));
         setSlot(3, pane.setName(Component.literal(paneText).withStyle(textColor)));
         setSlot(4, pane.setName(Component.literal(paneText).withStyle(textColor)));
         
         finishUpdate();
      }
   }
   
   public void finishUpdate(){
      updating = false;
   }
   
   public void setUpdating(){
      updating = true;
   }
   
   public boolean isValidSoulstone(Container inv){
      ItemStack item = inv.getItem(0);
      
      if(ArcanaItemUtils.isArcane(item)){
         if(ArcanaItemUtils.identifyItem(item) instanceof Soulstone stone){
            if(Soulstone.getType(item).equals(EntityType.getKey(EntityType.SHULKER).toString())){
               validStone(item);
               return true;
            }
         }
      }
      notValid();
      return false;
   }
}
