package net.borisshoes.arcananovum.gui.twilightanvil;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.AnvilInputGui;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.blocks.forge.TwilightAnvilBlockEntity;
import net.borisshoes.arcananovum.gui.ContainerWatcher;
import net.borisshoes.arcananovum.gui.WatchedContainer;
import net.borisshoes.arcananovum.utils.ArcanaColors;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.borislib.gui.GraphicalItem;
import net.borisshoes.borislib.utils.MinecraftUtils;
import net.borisshoes.borislib.utils.SoundUtils;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.StringUtil;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RenameGui extends AnvilInputGui implements ContainerWatcher {
   private final TwilightAnvilBlockEntity blockEntity;
   private Component newName;
   private WatchedContainer inv;
   private ItemStack item;
   private volatile boolean updating = false;
   
   /**
    * Constructs a new input gui for the provided player.
    *
    * @param player the player to serve this gui to
    *               will be treated as slots of this gui
    */
   public RenameGui(ServerPlayer player, TwilightAnvilBlockEntity blockEntity){
      super(player, false);
      this.blockEntity = blockEntity;
   }
   
   public void setItem(ItemStack item){
      this.item = item;
   }
   
   public void build(){
      setTitle(Component.literal("Rename Item"));
      setSlot(1, GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.PAGE_BG, ArcanaColors.LAPIS_COLOR)).hideTooltip());
      
      inv = new WatchedContainer(2);
      inv.addWatcher(this);
      
      setSlot(0, new Slot(inv, 0, 0, 0));
   }
   
   @Override
   public void onInput(String input){
      if(item == null || item.isEmpty()){
         setSlot(2, GuiElementBuilder.from(ItemStack.EMPTY));
         return;
      }
      String string = sanitize(input);
      ItemStack newItem = item.copy();
      Component name = newItem.getHoverName();
      if(string == null || string.equals(name.getString())){
         setSlot(2, GuiElementBuilder.from(ItemStack.EMPTY));
         return;
      }
      if(StringUtil.isBlank(string)){
         newItem.remove(DataComponents.CUSTOM_NAME);
         newName = Component.literal("");
      }else{
         newName = Component.literal(string);
         if(ArcanaItemUtils.isArcane(newItem)){
            List<Component> textList = newName.toFlatList(newItem.getOrDefault(DataComponents.ITEM_NAME, Component.literal("")).getStyle().withItalic(false));
            if(!textList.isEmpty()){
               newName = textList.getFirst();
            }
            newItem.set(DataComponents.CUSTOM_NAME, newName);
         }else{
            newItem.set(DataComponents.CUSTOM_NAME, Component.literal(string));
         }
      }
      GuiElementBuilder newElem = GuiElementBuilder.from(newItem);
      newElem.setCallback((clickType) -> {
         if(item != null && !item.isEmpty() && !newItem.isEmpty()){
            if(newName.getString().isBlank()){
               item.remove(DataComponents.CUSTOM_NAME);
            }else{
               item.set(DataComponents.CUSTOM_NAME, newName);
               if(ArcanaItemUtils.isArcane(item)){
                  ArcanaAchievements.grant(player, ArcanaAchievements.TOUCH_OF_PERSONALITY);
               }
            }
            SoundUtils.playSound(player.level(), blockEntity.getBlockPos(), SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 1f, 0.75f * 0.5f * player.getRandom().nextFloat());
            this.close();
         }
      });
      setSlot(2, newElem);
   }
   
   @Nullable
   private static String sanitize(String name){
      String string = StringUtil.filterText(name);
      if(string.length() <= 50){
         return string;
      }
      return null;
   }
   
   @Override
   public void onTick(){
      Level world = blockEntity.getLevel();
      if(world == null || world.getBlockEntity(blockEntity.getBlockPos()) != blockEntity || !blockEntity.isAssembled()){
         this.close();
      }
      super.onTick();
   }
   
   @Override
   public void afterRemoval(){
      MinecraftUtils.returnItems(inv, player);
   }
   
   @Override
   public void onChanged(WatchedContainer inv){
      if(!updating){
         setUpdating();
         ItemStack stack = inv.getItem(0);
         if(!stack.isEmpty()){
            setItem(stack);
         }else{
            setItem(null);
            setSlot(2, ItemStack.EMPTY);
         }
         finishUpdate();
      }
   }
   
   
   public void finishUpdate(){
      updating = false;
   }
   
   public void setUpdating(){
      updating = true;
   }
}
