package net.borisshoes.arcananovum.datastorage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.borisshoes.borislib.utils.CodecUtils;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.DyedItemColor;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class EnderCrateChannel {
   private final UUID idLock;
   private final DyeColor[] colors = new DyeColor[9];
   private final SimpleContainer inventory = new SimpleContainer(54);
   private final int color;
   
   private static final Codec<DyeColor> DYE_COLOR_CODEC = Codec.STRING.xmap(
         name -> name.isEmpty() ? null : DyeColor.byName(name, null),
         dyeColor -> dyeColor == null ? "" : dyeColor.getName()
   );
   
   public static final Codec<EnderCrateChannel> CODEC = RecordCodecBuilder.create(instance -> instance.group(
         CodecUtils.UUID_CODEC.optionalFieldOf("idLock").forGetter(channel -> Optional.ofNullable(channel.idLock)),
         DYE_COLOR_CODEC.listOf().fieldOf("colors").forGetter(channel -> Arrays.asList(channel.colors)),
         ItemStack.OPTIONAL_CODEC.sizeLimitedListOf(54).fieldOf("inventory").forGetter(channel -> {
            List<ItemStack> items = new ArrayList<>(54);
            for(int i = 0; i < 54; i++){
               items.add(channel.inventory.getItem(i));
            }
            return items;
         })
   ).apply(instance, EnderCrateChannel::fromCodec));
   
   private static EnderCrateChannel fromCodec(Optional<UUID> idLock, List<DyeColor> colors, List<ItemStack> inventory){
      EnderCrateChannel channel = new EnderCrateChannel(idLock.orElse(null), colors.toArray(new DyeColor[0]));
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
      ItemStack dyedStack = DyedItemColor.applyDyes(testStack, colorList);
      this.color = dyedStack.has(DataComponents.DYED_COLOR) ? dyedStack.get(DataComponents.DYED_COLOR).rgb() : 0xFFFFFF;
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
}