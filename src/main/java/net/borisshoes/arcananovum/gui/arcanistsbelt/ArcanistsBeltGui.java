package net.borisshoes.arcananovum.gui.arcanistsbelt;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.core.MagicItem;
import net.borisshoes.arcananovum.gui.quivers.QuiverInventory;
import net.borisshoes.arcananovum.items.ArcaneTome;
import net.borisshoes.arcananovum.items.ArcanistsBelt;
import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class ArcanistsBeltGui extends SimpleGui {
   
   private final ArcanistsBelt belt;
   private final ItemStack beltStack;
   private QuiverInventory inv;
   private final int slotCount;
   
   /**
    * Constructs a new simple container gui for the supplied player.
    *
    * @param player                the player to server this gui to
    *                              will be treated as slots of this gui
    */
   public ArcanistsBeltGui(ServerPlayerEntity player, ArcanistsBelt belt, ItemStack beltStack, int slotCount){
      super(ScreenHandlerType.GENERIC_9X1, player, false);
      this.belt = belt;
      this.beltStack = beltStack;
      this.slotCount = slotCount;
   }
   
   public void build(){
      inv = new QuiverInventory();
      for(int i = 0; i < inv.size(); i++){
         if(i < slotCount){
            setSlotRedirect(i, new ArcanistsBeltSlot(inv,i,i,0));
         }else{
            setSlot(i,new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                  .setName(Text.literal("Slot Locked").formatted(Formatting.DARK_PURPLE))
                  .addLoreLine(Text.literal("")
                        .append(Text.literal("Unlock this slot with Augments").formatted(Formatting.LIGHT_PURPLE))));
         }
      }
      
      NbtCompound tag = beltStack.getNbt();
      NbtCompound magicTag = tag.getCompound("arcananovum");
      NbtList items = magicTag.getList("items", NbtElement.COMPOUND_TYPE);
      for(int i = 0; i < items.size(); i++){
         NbtCompound item = items.getCompound(i);
         int slot = item.getByte("Slot");
         ItemStack stack = ItemStack.fromNbt(item);
         if(stack.getCount() > 0 && !stack.isEmpty())
            inv.setStack(slot,stack);
      }
      
      setTitle(Text.literal(belt.getNameString()));
   }
   
   @Override
   public boolean onAnyClick(int index, ClickType type, SlotActionType action){
      if(type == ClickType.OFFHAND_SWAP || action == SlotActionType.SWAP){
         close();
      }else if(index > 9){
         int invSlot = index >= 36 ? index - 36 : index;
         ItemStack stack = player.getInventory().getStack(invSlot);
         if(ItemStack.canCombine(beltStack,stack)){
            close();
            return false;
         }
      }
      
      return super.onAnyClick(index, type, action);
   }
   
   @Override
   public void onClose(){
      NbtCompound tag = beltStack.getNbt();
      NbtCompound magicTag = tag.getCompound("arcananovum");
      NbtList items = new NbtList();
      
      int charmCount = 0;
      for(int i = 0; i < inv.size(); i++){
         ItemStack itemStack = inv.getStack(i);
         if(itemStack.isEmpty()) continue;
         NbtCompound item = itemStack.writeNbt(new NbtCompound());
         item.putByte("Slot", (byte) i);
         items.add(item);
         
         MagicItem magicItem = MagicItemUtils.identifyItem(itemStack);
         if(magicItem != null && magicItem.hasCategory(ArcaneTome.TomeFilter.CHARMS)) charmCount++;
      }
      magicTag.put("items",items);
      
      if(charmCount >= slotCount){
         ArcanaAchievements.grant(player,ArcanaAchievements.BELT_CHARMING.id);
      }
   }
}