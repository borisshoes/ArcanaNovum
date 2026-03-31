package net.borisshoes.arcananovum.datastorage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.core.ArcanaItemContainer;
import net.borisshoes.borislib.gui.GraphicalItem;
import net.borisshoes.borislib.utils.CodecUtils;
import net.borisshoes.borislib.utils.MinecraftUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class EnderCrateChannel implements ArcanaItemContainer.ArcanaItemContainerHaver {
   private final UUID idLock;
   private final DyeColor[] colors = new DyeColor[9];
   private final SimpleContainer inventory = new SimpleContainer(54);
   private final int color;
   
   public static final Codec<EnderCrateChannel> CODEC = RecordCodecBuilder.create(instance -> instance.group(
         CodecUtils.UUID_CODEC.optionalFieldOf("idLock").forGetter(channel -> Optional.ofNullable(channel.idLock)),
         Codec.STRING.listOf().fieldOf("colors").forGetter(channel -> {
            List<String> colorNames = new ArrayList<>(9);
            for(DyeColor dyeColor : channel.colors){
               colorNames.add(dyeColor == null ? "" : dyeColor.getName());
            }
            return colorNames;
         }),
         ItemStack.OPTIONAL_CODEC.sizeLimitedListOf(54).fieldOf("inventory").forGetter(channel -> {
            List<ItemStack> items = new ArrayList<>(54);
            for(int i = 0; i < 54; i++){
               items.add(channel.inventory.getItem(i));
            }
            return items;
         })
   ).apply(instance, EnderCrateChannel::fromCodec));
   
   private static EnderCrateChannel fromCodec(Optional<UUID> idLock, List<String> colorNames, List<ItemStack> inventory){
      DyeColor[] colors = new DyeColor[colorNames.size()];
      for(int i = 0; i < colorNames.size(); i++){
         String name = colorNames.get(i);
         colors[i] = name.isEmpty() ? null : DyeColor.byName(name, null);
      }
      EnderCrateChannel channel = new EnderCrateChannel(idLock.orElse(null), colors);
      for(int i = 0; i < Math.min(inventory.size(), 54); i++){
         channel.inventory.setItem(i, inventory.get(i));
      }
      return channel;
   }
   
   public EnderCrateChannel(@Nullable UUID lock, DyeColor... colors){
      this.idLock = lock;
      for(int i = 0; i < 9; i++){
         if(i >= colors.length){
            this.colors[i] = null;
            continue;
         }
         this.colors[i] = colors[i];
      }
      
      ItemStack testStack = new ItemStack(Items.LEATHER_CHESTPLATE);
      testStack.remove(DataComponents.DYED_COLOR);
      ArrayList<DyeItem> colorList = new ArrayList<>();
      for(DyeColor dyeColor : this.colors){
         if(dyeColor != null) colorList.add(DyeItem.byColor(dyeColor));
      }
      if(colorList.isEmpty()){
         this.color = 0xFFFFFF; // Default white when no colors specified
      }else{
         ItemStack dyedStack = DyedItemColor.applyDyes(testStack, colorList);
         this.color = dyedStack.has(DataComponents.DYED_COLOR) ? dyedStack.get(DataComponents.DYED_COLOR).rgb() : 0xFFFFFF;
      }
   }
   
   public DyeColor[] getColors(){
      return Arrays.copyOf(colors, 9);
   }
   
   public SimpleContainer getInventory(){
      return inventory;
   }
   
   public int getColor(){
      return this.color;
   }
   
   @Nullable
   public UUID getIdLock(){
      return idLock;
   }
   
   public boolean isLocked(){
      return idLock != null;
   }
   
   @Override
   public boolean equals(Object o){
      if(this == o) return true;
      if(o == null || getClass() != o.getClass()) return false;
      EnderCrateChannel that = (EnderCrateChannel) o;
      return Arrays.equals(colors, that.colors) && Objects.equals(idLock, that.idLock);
   }
   
   @Override
   public int hashCode(){
      int result = Objects.hashCode(idLock);
      result = 31 * result + Arrays.hashCode(colors);
      return result;
   }
   
   @Override
   public ArcanaItemContainer getArcanaItemContainer(ItemStack item){
      SimpleContainer inv = new SimpleContainer(54);
      for(int i = 0; i < 54; i++){
         inv.setItem(i, inventory.getItem(i).copy());
      }
      
      MutableComponent name = ArcanaRegistry.ENDER_CRATE.getTranslatedName().append(" ");
      for(DyeColor color : colors){
         MutableComponent dyeComp = color == null ? MinecraftUtils.getAtlasedTexture(Blocks.GLASS) : MinecraftUtils.getAtlasedTexture(DyeItem.byColor(color));
         name.append(dyeComp.withStyle(ChatFormatting.WHITE));
      }
      
      return new ArcanaItemContainer(
            ArcanaRegistry.arcanaId(ArcanaRegistry.ENDER_CRATE.getId()),
            inv, 54, 101,
            Component.literal("ECr"),
            name,
            0.5);
   }
   
   public static GraphicalItem.GraphicElement colorToGraphicElement(@Nullable DyeColor color){
      if(color == null) return ArcanaRegistry.CHANNEL_BLANK;
      return switch(color){
         case WHITE -> ArcanaRegistry.CHANNEL_WHITE;
         case ORANGE -> ArcanaRegistry.CHANNEL_ORANGE;
         case MAGENTA -> ArcanaRegistry.CHANNEL_MAGENTA;
         case LIGHT_BLUE -> ArcanaRegistry.CHANNEL_LIGHT_BLUE;
         case YELLOW -> ArcanaRegistry.CHANNEL_YELLOW;
         case LIME -> ArcanaRegistry.CHANNEL_LIME;
         case PINK -> ArcanaRegistry.CHANNEL_PINK;
         case GRAY -> ArcanaRegistry.CHANNEL_GRAY;
         case LIGHT_GRAY -> ArcanaRegistry.CHANNEL_LIGHT_GRAY;
         case CYAN -> ArcanaRegistry.CHANNEL_CYAN;
         case PURPLE -> ArcanaRegistry.CHANNEL_PURPLE;
         case BLUE -> ArcanaRegistry.CHANNEL_BLUE;
         case BROWN -> ArcanaRegistry.CHANNEL_BROWN;
         case GREEN -> ArcanaRegistry.CHANNEL_GREEN;
         case RED -> ArcanaRegistry.CHANNEL_RED;
         case BLACK -> ArcanaRegistry.CHANNEL_BLACK;
      };
   }
}