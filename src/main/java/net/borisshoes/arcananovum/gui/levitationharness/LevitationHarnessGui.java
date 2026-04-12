package net.borisshoes.arcananovum.gui.levitationharness;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.core.EnergyItem;
import net.borisshoes.arcananovum.gui.ContainerWatcher;
import net.borisshoes.arcananovum.gui.WatchedContainer;
import net.borisshoes.arcananovum.items.LevitationHarness;
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
import net.minecraft.world.item.Items;

public class LevitationHarnessGui extends SimpleGui implements ContainerWatcher {
   private LevitationHarness harness;
   private ItemStack item;
   private boolean validStone;
   private final WatchedContainer inv;
   private volatile boolean updating = false;
   
   public LevitationHarnessGui(ServerPlayer player, LevitationHarness harness, ItemStack item){
      super(MenuType.HOPPER, player, false);
      this.harness = harness;
      this.item = item;
      this.inv = new WatchedContainer(2);
      this.inv.addWatcher(this);
      buildGui();
   }
   
   private void buildGui(){
      int souls = (int) harness.getSouls(item);
      int glow = (int) harness.getGlow(item);
      int energy = EnergyItem.getEnergy(item);
      
      String soulText = souls > -1 ? TextUtils.readableInt(souls) + " Shulker Souls" : "No Soulstone Inserted";
      String durationText = energy > 0 ? "Flight Time Remaining: " + harness.getDuration(item) : "No Fuel!";
      String glowText = glow > 0 ? TextUtils.readableInt(glow) + " Glowstone Left" : "No Glowstone Remaining";
      GuiElementBuilder soulPane = GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_TOP, souls > -1 ? ArcanaColors.ARCANA_COLOR : ArcanaColors.DARK_COLOR));
      GuiElementBuilder durationPane = GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_TOP, energy > 0 ? ArcanaColors.LIGHT_COLOR : 0x880000));
      GuiElementBuilder glowPane = GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_TOP, glow > 0 ? 0xffdd00 : ArcanaColors.DARK_COLOR));
      ChatFormatting soulTextColor = souls > -1 ? ChatFormatting.LIGHT_PURPLE : ChatFormatting.RED;
      ChatFormatting durationTextColor = energy > 0 ? ChatFormatting.GRAY : ChatFormatting.RED;
      ChatFormatting glowTextColor = glow > 0 ? ChatFormatting.GOLD : ChatFormatting.RED;
      
      setSlot(0, soulPane.setName(Component.literal(soulText).withStyle(soulTextColor)));
      setSlot(2, durationPane.setName(Component.literal(durationText).withStyle(durationTextColor)));
      setSlot(4, glowPane.setName(Component.literal(glowText).withStyle(glowTextColor)));
      
      setUpdating();
      
      setSlot(1, new Slot(inv, 0, 0, 0));
      setSlot(3, new Slot(inv, 1, 0, 0));
      if(souls > -1){
         CompoundTag stoneData = ArcanaItem.getCompoundProperty(item, LevitationHarness.STONE_DATA_TAG);
         ItemStack stone;
         if(stoneData == null || stoneData.isEmpty()){
            stone = Soulstone.setType(ArcanaRegistry.SOULSTONE.getNewItem(), EntityType.SHULKER);
         }else{
            stone = ItemStack.CODEC.parse(RegistryOps.create(NbtOps.INSTANCE, player.registryAccess()), stoneData).result().orElse(ItemStack.EMPTY);
         }
         stone = Soulstone.setSouls(stone, souls);
         inv.setItem(0, stone);
         validStone(stone);
      }else{
         notValidStone();
      }
      setTitle(Component.literal("Levitation Harness"));
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
      // Return invalid items
      ItemStack savedStone = ItemStack.EMPTY;
      if(validStone) savedStone = inv.getItem(0).copyAndClear();
      if(!player.isDeadOrDying() && !player.isSpectator()){
         MinecraftUtils.returnItems(inv, player);
      }else{
         Containers.dropContents(player.level(), player.getOnPos().above(1), inv);
      }
      inv.clearContent();
      inv.setItem(0,savedStone);
      harness.recalculateEnergy(item);
   }
   
   public void validStone(ItemStack newStone){
      validStone = true;
      harness.setStone(item, newStone);
   }
   
   public void notValidStone(){
      validStone = false;
      harness.setStone(item, null);
   }
   
   @Override
   public void onChanged(WatchedContainer inv){
      if(!updating){
         updating = true;
         
         //Check Soulstone, glowstone, and update item
         isValidSoulstone(inv);
         ItemStack glowSlot = inv.getItem(1);
         if(glowSlot.is(Items.GLOWSTONE)){
            harness.addGlow(item, glowSlot.getCount() * 4);
            inv.setItem(1, ItemStack.EMPTY);
         }else if(glowSlot.is(Items.GLOWSTONE_DUST)){
            harness.addGlow(item, glowSlot.getCount());
            inv.setItem(1, ItemStack.EMPTY);
         }
         
         harness.recalculateEnergy(item);
         buildGui();
         
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
      notValidStone();
      return false;
   }
}