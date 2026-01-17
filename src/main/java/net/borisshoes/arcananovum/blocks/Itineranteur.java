package net.borisshoes.arcananovum.blocks;

import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.ArcanaBlock;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerBlockEntity;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerBlockItem;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerItem;
import net.borisshoes.arcananovum.gui.arcanetome.ArcaneTomeGui;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class Itineranteur extends ArcanaBlock {
   public static final String ID = "itineranteur";
   
   public Itineranteur(){
      id = ID;
      name = "Itineranteur";
      rarity = ArcanaRarity.EMPOWERED;
      categories = new ArcaneTomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), ArcaneTomeGui.TomeFilter.BLOCKS};
      itemVersion = 0;
      vanillaItem = Items.LANTERN;
      block = new ItineranteurBlock(BlockBehaviour.Properties.of().requiresCorrectToolForDrops().strength(3.0f, 1200.0f).sound(SoundType.METAL));
      item = new ItineranteurItem(block);
      displayName = Component.translatableWithFallback("item."+MOD_ID+"."+ID,name).withStyle(ChatFormatting.BOLD, ChatFormatting.YELLOW);
      researchTasks = new ResourceKey[]{};  // TODO
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getDefaultMaxStackSize());
      //putProperty(stack,TAG,);
      setPrefStack(stack);
   }
   
   @Override
   public List<Component> getItemLore(@Nullable ItemStack itemStack){
      List<MutableComponent> lore = new ArrayList<>();
      lore.add(Component.literal("")
            .append(Component.literal("A seemingly simple ").withStyle(ChatFormatting.BLUE))
            .append(Component.literal("lantern ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("quickens ").withStyle(ChatFormatting.AQUA))
            .append(Component.literal("your ").withStyle(ChatFormatting.BLUE))
            .append(Component.literal("feet").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(".").withStyle(ChatFormatting.BLUE)));
      lore.add(Component.literal("")
            .append(Component.literal("Its ").withStyle(ChatFormatting.BLUE))
            .append(Component.literal("magical aura").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(" ").withStyle(ChatFormatting.BLUE))
            .append(Component.literal("enchants ").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal("a ").withStyle(ChatFormatting.BLUE))
            .append(Component.literal("designated path").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal(".").withStyle(ChatFormatting.BLUE)));
      lore.add(Component.literal("")
            .append(Component.literal("Travelers in its ").withStyle(ChatFormatting.BLUE))
            .append(Component.literal("light").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal(" find their ").withStyle(ChatFormatting.BLUE))
            .append(Component.literal("pace ").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal("hastened").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(".").withStyle(ChatFormatting.BLUE)));
      lore.add(Component.literal("")
            .append(Component.literal("Right Click").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(" a placed ").withStyle(ChatFormatting.BLUE))
            .append(Component.literal("Itineranteur ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("to ").withStyle(ChatFormatting.BLUE))
            .append(Component.literal("assign ").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal("a ").withStyle(ChatFormatting.BLUE))
            .append(Component.literal("path").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal(".").withStyle(ChatFormatting.BLUE)));
      
      return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   public List<List<Component>> getBookLore(){
      List<List<Component>> list = new ArrayList<>();
      list.add(List.of(Component.literal("TODO").withStyle(ChatFormatting.BLACK))); // TODO
      return list;
   }
   
   public class ItineranteurItem extends ArcanaPolymerBlockItem {
      public ItineranteurItem(Block block){
         super(getThis(), block, getArcanaItemComponents());
      }
      
      @Override
      public ItemStack getPolymerItemStack(ItemStack itemStack, TooltipFlag tooltipType, PacketContext context){
         ItemStack baseStack = super.getPolymerItemStack(itemStack, tooltipType, context);
         if(!ArcanaItemUtils.isArcane(itemStack)) return baseStack;
         
         List<String> stringList = new ArrayList<>();
//         if(ArcanaAugments.getAugmentOnItem(itemStack,ArcanaAugments.) >= 1){
//            stringList.add("");
//         }
         baseStack.set(DataComponents.CUSTOM_MODEL_DATA,new CustomModelData(new ArrayList<>(),new ArrayList<>(),stringList,new ArrayList<>()));
         return baseStack;
      }
      
      @Override
      public @NonNull ItemStack getDefaultInstance(){
         return prefItem;
      }
      
      @Override
      public void inventoryTick(@NonNull ItemStack stack, @NonNull ServerLevel world, @NonNull Entity entity, @Nullable EquipmentSlot slot){
         if(!ArcanaItemUtils.isArcane(stack)) return;
         if(!(entity instanceof ServerPlayer player)) return;
         
         
      }
      
      @Override
      public @NonNull InteractionResult use(@NonNull Level world, Player playerEntity, @NonNull InteractionHand hand){
         ItemStack stack = playerEntity.getItemInHand(hand);
         if(!(playerEntity instanceof ServerPlayer player)) return InteractionResult.PASS;
         
         return InteractionResult.PASS;
      }
   }
   
   public class ItineranteurBlock extends ArcanaPolymerBlockEntity {
      public ItineranteurBlock(BlockBehaviour.Properties settings){
         super(getThis(), settings);
      }
      
      @Override
      public BlockState getPolymerBlockState(BlockState state, PacketContext context){
         return Blocks.LANTERN.defaultBlockState();
      }
   }
}


