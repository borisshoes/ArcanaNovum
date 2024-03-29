package net.borisshoes.arcananovum.gui.twilightanvil;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.AnvilInputGui;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.blocks.forge.TwilightAnvilBlockEntity;
import net.borisshoes.arcananovum.gui.WatchedGui;
import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.borisshoes.arcananovum.utils.MiscUtils;
import net.borisshoes.arcananovum.utils.SoundUtils;
import net.minecraft.SharedConstants;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RenameGui extends AnvilInputGui implements WatchedGui {
   private final TwilightAnvilBlockEntity blockEntity;
   private Text newName;
   private TinkerInventory inv;
   private TinkerInventoryListener listener;
   private ItemStack item;
   
   /**
    * Constructs a new input gui for the provided player.
    * @param player                the player to serve this gui to
    *                              will be treated as slots of this gui
    */
   public RenameGui(ServerPlayerEntity player, TwilightAnvilBlockEntity blockEntity){
      super(player, false);
      this.blockEntity = blockEntity;
   }
   
   public void setItem(ItemStack item){
      this.item = item;
   }
   
   public void build(){
      setTitle(Text.literal("Rename Item"));
      setSlot(1, GuiElementBuilder.from(Items.STRUCTURE_VOID.getDefaultStack()).setName(Text.literal("")));
      
      inv = new TinkerInventory();
      listener = new TinkerInventoryListener(this,1,blockEntity);
      inv.addListener(listener);
      
      setSlotRedirect(0, new Slot(inv,0,0,0));
   }
   
   @Override
   public boolean onAnyClick(int index, ClickType type, SlotActionType action) {
      if(index == 2){
         ItemStack outputStack = getSlot(2) == null ? ItemStack.EMPTY : getSlot(2).getItemStack();
         if(item != null && !item.isEmpty() && !outputStack.isEmpty()){
            item.setCustomName(newName);
            if(MagicItemUtils.isMagic(item)){
               ArcanaAchievements.grant(player,ArcanaAchievements.TOUCH_OF_PERSONALITY.id);
            }
            SoundUtils.playSound(player.getServerWorld(),blockEntity.getPos(), SoundEvents.BLOCK_ANVIL_USE, SoundCategory.BLOCKS, 1f, (float)(0.75f * 0.5f*Math.random()));
            this.close();
         }
      }
      return true;
   }
   
   @Override
   public void onInput(String input) {
      if(item != null && !item.isEmpty()){
         ItemStack newItem = item.copy();
         Text name = newItem.getName();
         input = sanitize(input);
         if (input == null || input.equals(name.getString())) {
            setSlot(2,GuiElementBuilder.from(ItemStack.EMPTY));
            return;
         }
         newName = Text.literal(input);
         if(MagicItemUtils.isMagic(newItem) || name.getStyle().isBold()){
            List<Text> textList = newName.getWithStyle(name.getStyle());
            if(!textList.isEmpty()){
               newName = textList.get(0);
            }
         }else{
            newName = Text.literal(input).formatted(newItem.getRarity().formatting, Formatting.ITALIC);
         }
         newItem.setCustomName(newName);
         setSlot(2, GuiElementBuilder.from(newItem));
      }
   }
   
   @Nullable
   private static String sanitize(String name) {
      String string = SharedConstants.stripInvalidChars(name);
      if (string.length() <= 50 && !Util.isBlank(string)) {
         return string;
      }
      return null;
   }
   
   @Override
   public void onClose(){
      MiscUtils.returnItems(inv,player);
   }
   
   @Override
   public void close(){
      super.close();
   }
   
   @Override
   public BlockEntity getBlockEntity(){
      return blockEntity;
   }
   
   @Override
   public SimpleGui getGui(){
      return this;
   }
}
