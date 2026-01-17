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

public class Interdictor extends ArcanaBlock {
   public static final String ID = "interdictor";
   
   public Interdictor(){
      id = ID;
      name = "Interdictor";
      rarity = ArcanaRarity.EXOTIC;
      categories = new ArcaneTomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), ArcaneTomeGui.TomeFilter.BLOCKS};
      itemVersion = 0;
      vanillaItem = Items.BEACON;
      block = new InterdictorBlock(BlockBehaviour.Properties.of().requiresCorrectToolForDrops().strength(6.0f, 1200.0f).sound(SoundType.VAULT));
      item = new InterdictorItem(block);
      displayName = Component.translatableWithFallback("item."+MOD_ID+"."+ID,name).withStyle(ChatFormatting.BOLD, ChatFormatting.AQUA);
      researchTasks = new ResourceKey[]{};  // TODO
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getDefaultMaxStackSize());
      setPrefStack(stack);
   }
   
   @Override
   public List<Component> getItemLore(@Nullable ItemStack itemStack){
      List<MutableComponent> lore = new ArrayList<>();
      lore.add(Component.literal("")
            .append(Component.literal("A ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("beacon ").withStyle(ChatFormatting.AQUA))
            .append(Component.literal("that has been modified with ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("dimensional energy").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(".").withStyle(ChatFormatting.YELLOW)));
      lore.add(Component.literal("")
            .append(Component.literal("It ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("blocks ").withStyle(ChatFormatting.RED))
            .append(Component.literal("the coalescence of new ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("mob essence").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(".").withStyle(ChatFormatting.YELLOW)));
      lore.add(Component.literal("")
            .append(Component.literal("The ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("Interdictor ").withStyle(ChatFormatting.AQUA))
            .append(Component.literal("is comprised of a ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("3x3x3").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(" ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("multiblock").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(".").withStyle(ChatFormatting.YELLOW)));
      lore.add(Component.literal("")
            .append(Component.literal("When active, it ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("stops ").withStyle(ChatFormatting.RED))
            .append(Component.literal("new ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("mob spawns").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(" in a ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("large area").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(".").withStyle(ChatFormatting.YELLOW)));
      lore.add(Component.literal("")
            .append(Component.literal("A ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("redstone ").withStyle(ChatFormatting.RED))
            .append(Component.literal("signal to the ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("Interdictor ").withStyle(ChatFormatting.AQUA))
            .append(Component.literal("disables ").withStyle(ChatFormatting.RED))
            .append(Component.literal("it.").withStyle(ChatFormatting.YELLOW)));
      
      return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   public List<List<Component>> getBookLore(){
      List<List<Component>> list = new ArrayList<>();
      list.add(List.of(Component.literal("TODO").withStyle(ChatFormatting.BLACK))); // TODO
      return list;
   }
   
   public class InterdictorItem extends ArcanaPolymerBlockItem {
      public InterdictorItem(Block block){
         super(getThis(), block, getArcanaItemComponents());
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
   
   public class InterdictorBlock extends ArcanaPolymerBlockEntity {
      public InterdictorBlock(BlockBehaviour.Properties settings){
         super(getThis(), settings);
      }
      
      @Override
      public BlockState getPolymerBlockState(BlockState state, PacketContext context){
         return Blocks.BEACON.defaultBlockState();
      }
   }
}

