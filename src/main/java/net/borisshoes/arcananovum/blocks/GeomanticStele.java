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

public class GeomanticStele extends ArcanaBlock {
   public static final String ID = "geomantic_stele";
   
   public GeomanticStele(){
      id = ID;
      name = "Geomantic Stele";
      rarity = ArcanaRarity.SOVEREIGN;
      categories = new ArcaneTomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), ArcaneTomeGui.TomeFilter.BLOCKS};
      itemVersion = 0;
      vanillaItem = Items.REINFORCED_DEEPSLATE;
      block = new GeomanticSteleBlock(BlockBehaviour.Properties.of().requiresCorrectToolForDrops().strength(6.0f, 1200.0f).sound(SoundType.LODESTONE));
      item = new GeomanticSteleItem(block);
      displayName = Component.translatableWithFallback("item."+MOD_ID+"."+ID,name).withStyle(ChatFormatting.BOLD, ChatFormatting.GRAY);
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
            .append(Component.literal("This ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("keystone ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("activates a ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("multiblock construct").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_GRAY)));
      lore.add(Component.literal("")
            .append(Component.literal("The ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("monolith ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("channels ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("Arcana ").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal("into a single ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("item").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_GRAY)));
      lore.add(Component.literal("")
            .append(Component.literal("Arcana Items").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(" become ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("activated ").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal("by the ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("keystone").withStyle(ChatFormatting.GRAY))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_GRAY)));
      lore.add(Component.literal("")
            .append(Component.literal("A ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("redstone signal").withStyle(ChatFormatting.RED))
            .append(Component.literal(" ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("deactivates ").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal("the ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("construct").withStyle(ChatFormatting.GRAY))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_GRAY)));
      lore.add(Component.literal("")
            .append(Component.literal("Right Click").withStyle(ChatFormatting.GRAY))
            .append(Component.literal(" a finished ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("Stele ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("with an ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("Arcana Item").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_GRAY)));
      
      return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   public List<List<Component>> getBookLore(){
      List<List<Component>> list = new ArrayList<>();
      list.add(List.of(Component.literal("TODO").withStyle(ChatFormatting.BLACK))); // TODO
      return list;
   }
   
   public class GeomanticSteleItem extends ArcanaPolymerBlockItem {
      public GeomanticSteleItem(Block block){
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
   
   public class GeomanticSteleBlock extends ArcanaPolymerBlockEntity {
      public GeomanticSteleBlock(BlockBehaviour.Properties settings){
         super(getThis(), settings);
      }
      
      @Override
      public BlockState getPolymerBlockState(BlockState state, PacketContext context){
         return Blocks.REINFORCED_DEEPSLATE.defaultBlockState();
      }
   }
}


