package net.borisshoes.arcananovum.gui.twilightanvil;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.AnvilInputGui;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.blocks.forge.TwilightAnvilBlockEntity;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.arcananovum.utils.MiscUtils;
import net.borisshoes.arcananovum.utils.SoundUtils;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.StringHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RenameGui extends AnvilInputGui {
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
      setSlot(1, GuiElementBuilder.from(Items.STRUCTURE_VOID.getDefaultStack()).setName(Text.literal("")).hideTooltip());
      
      inv = new TinkerInventory();
      listener = new TinkerInventoryListener(this,1,blockEntity);
      inv.addListener(listener);
      
      setSlotRedirect(0, new Slot(inv,0,0,0));
   }
   
   @Override
   public boolean onAnyClick(int index, ClickType type, SlotActionType action){
      if(index == 2){
         ItemStack outputStack = getSlot(2) == null ? ItemStack.EMPTY : getSlot(2).getItemStack();
         if(item != null && !item.isEmpty() && !outputStack.isEmpty()){
            if(newName.getString().isBlank()){
               item.remove(DataComponentTypes.CUSTOM_NAME);
            }else{
               item.set(DataComponentTypes.CUSTOM_NAME,newName);
               if(ArcanaItemUtils.isArcane(item)){
                  ArcanaAchievements.grant(player,ArcanaAchievements.TOUCH_OF_PERSONALITY.id);
               }
            }
            SoundUtils.playSound(player.getWorld(),blockEntity.getPos(), SoundEvents.BLOCK_ANVIL_USE, SoundCategory.BLOCKS, 1f, (float)(0.75f * 0.5f*Math.random()));
            this.close();
         }
      }
      return true;
   }
   
   @Override
   public void onInput(String input){
      if(item == null || item.isEmpty()){
         setSlot(2,GuiElementBuilder.from(ItemStack.EMPTY));
         return;
      }
      String string = sanitize(input);
      ItemStack newItem = item.copy();
      Text name = newItem.getName();
      if(string == null || string.equals(name.getString())){
         setSlot(2,GuiElementBuilder.from(ItemStack.EMPTY));
         return;
      }
      if(StringHelper.isBlank(string)){
         newItem.remove(DataComponentTypes.CUSTOM_NAME);
         newName = Text.literal("");
      }else{
         newName = Text.literal(string);
         if(ArcanaItemUtils.isArcane(newItem)){
            List<Text> textList = newName.getWithStyle(newItem.getOrDefault(DataComponentTypes.ITEM_NAME,Text.literal("")).getStyle().withItalic(false));
            if(!textList.isEmpty()){
               newName = textList.getFirst();
            }
            newItem.set(DataComponentTypes.CUSTOM_NAME, newName);
         }else{
            newItem.set(DataComponentTypes.CUSTOM_NAME, Text.literal(string));
         }
      }
      setSlot(2,GuiElementBuilder.from(newItem));
   }
   
   @Nullable
   private static String sanitize(String name){
      String string = StringHelper.stripInvalidChars(name);
      if(string.length() <= 50){
         return string;
      }
      return null;
   }
   
   @Override
   public void onTick(){
      World world = blockEntity.getWorld();
      if(world == null || world.getBlockEntity(blockEntity.getPos()) != blockEntity || !blockEntity.isAssembled()){
         this.close();
      }
      
      super.onTick();
   }
   
   @Override
   public void onClose(){
      MiscUtils.returnItems(inv,player);
   }
   
   @Override
   public void close(){
      super.close();
   }
}
