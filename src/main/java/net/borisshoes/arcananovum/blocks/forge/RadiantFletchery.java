package net.borisshoes.arcananovum.blocks.forge;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.core.ArcanaBlock;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.core.Multiblock;
import net.borisshoes.arcananovum.core.MultiblockCore;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerBlockEntity;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerBlockItem;
import net.borisshoes.arcananovum.gui.arcanetome.ArcaneTomeGui;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class RadiantFletchery extends ArcanaBlock implements MultiblockCore {
	public static final String ID = "radiant_fletchery";
   
   private Multiblock multiblock;
   
   public RadiantFletchery(){
      id = ID;
      name = "Radiant Fletchery";
      rarity = ArcanaRarity.EMPOWERED;
      categories = new ArcaneTomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), ArcaneTomeGui.TomeFilter.BLOCKS, ArcaneTomeGui.TomeFilter.FORGE};
      itemVersion = 0;
      vanillaItem = Items.FLETCHING_TABLE;
      block = new RadiantFletcheryBlock(BlockBehaviour.Properties.of().strength(2.5f,1200.0f).sound(SoundType.WOOD));
      item = new RadiantFletcheryItem(this.block);
      displayName = Component.translatableWithFallback("item."+MOD_ID+"."+ID,name).withStyle(ChatFormatting.BOLD, ChatFormatting.YELLOW);
      researchTasks = new ResourceKey[]{ResearchTasks.UNLOCK_RUNIC_MATRIX,ResearchTasks.ADVANCEMENT_SHOOT_ARROW,ResearchTasks.ADVANCEMENT_OL_BETSY,ResearchTasks.OBTAIN_TIPPED_ARROW,ResearchTasks.OBTAIN_SPECTRAL_ARROW,ResearchTasks.ADVANCEMENT_BREW_POTION,ResearchTasks.UNLOCK_STARLIGHT_FORGE};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getDefaultMaxStackSize());
      setPrefStack(stack);
   }
   
   @Override
   public List<Component> getItemLore(@Nullable ItemStack itemStack){
      List<MutableComponent> lore = new ArrayList<>();
      lore.add(Component.literal("")
            .append(Component.literal("A ").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal("Forge Structure").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal(" addon to the ").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal("Starlight Forge").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal(".").withStyle(ChatFormatting.LIGHT_PURPLE)));
      lore.add(Component.literal("")
            .append(Component.literal("The ").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal("Fletchery ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("enables ").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal("efficient ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("creation of ").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal("tipped arrows").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal(".").withStyle(ChatFormatting.LIGHT_PURPLE)));
      lore.add(Component.literal("")
            .append(Component.literal("The ").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal("Fletchery ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("also ").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal("unlocks ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("the ability to make ").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal("Runic Arrows").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal(".").withStyle(ChatFormatting.LIGHT_PURPLE)));
      addForgeLore(lore);
     return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   public void loadMultiblock(){
      multiblock = Multiblock.loadFromFile(getId());
   }
   
   @Override
   public Multiblock getMultiblock(){
      return multiblock;
   }
   
   @Override
   public Vec3i getCheckOffset(){
      return new Vec3i(-1,-1,-1);
   }
   
   @Override
   public List<List<Component>> getBookLore(){
      List<List<Component>> list = new ArrayList<>();
      list.add(List.of(Component.literal(" Radiant Fletchery").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), Component.literal("\nRarity: ").withStyle(ChatFormatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(),false)), Component.literal("\nI have yet to put my Runic Matrix to good use. Fortunately, this might be my chance. The Matrix should be able to take on the effect of potions to boost the amount of Tipped Arrows I can make from a single ").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal(" Radiant Fletchery").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), Component.literal("\npotion. The Arrows themselves could make an excellent candidate for use of the Matrix once I master more of its capabilities. Perhaps if I make an arrow out of a Matrix it could activate powerful effects upon hitting a target.").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal(" Radiant Fletchery").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), Component.literal("\nThe Fletchery boosts the amount of Tipped Arrows made per potion, as well as allowing non-lingering potions to be used.\n\nThe Fletchery also unlocks a host of Archery related recipes for the Starlight Forge.\n").withStyle(ChatFormatting.BLACK)));
      return list;
   }
   
   public class RadiantFletcheryItem extends ArcanaPolymerBlockItem {
      public RadiantFletcheryItem(Block block){
         super(getThis(),block, getArcanaItemComponents());
      }
      
      @Override
      public ItemStack getDefaultInstance(){
         return prefItem;
      }
   }
   
   public class RadiantFletcheryBlock extends ArcanaPolymerBlockEntity {
      public RadiantFletcheryBlock(BlockBehaviour.Properties settings){
         super(getThis(), settings);
      }
      
      @Override
      public BlockState getPolymerBlockState(BlockState state, PacketContext context){
         return Blocks.FLETCHING_TABLE.defaultBlockState();
      }
      
      @Override
      public BlockEntity newBlockEntity(BlockPos pos, BlockState state){
         return new RadiantFletcheryBlockEntity(pos, state);
      }
      
      @Nullable
      @Override
      public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type){
         return createTickerHelper(type, ArcanaRegistry.RADIANT_FLETCHERY_BLOCK_ENTITY, RadiantFletcheryBlockEntity::ticker);
      }
      
      @Override
      public InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player playerEntity, BlockHitResult hit){
         RadiantFletcheryBlockEntity fletchery = (RadiantFletcheryBlockEntity) world.getBlockEntity(pos);
         if(fletchery != null){
            if(playerEntity instanceof ServerPlayer player){
               if(fletchery.isAssembled()){
                  if(StarlightForge.findActiveForge(player.level(),pos) == null){
                     player.sendSystemMessage(Component.literal("The Fletchery must be within the range of an active Starlight Forge"));
                  }else{
                     fletchery.openGui(player);
                     player.getCooldowns().addCooldown(playerEntity.getMainHandItem(),1);
                     player.getCooldowns().addCooldown(playerEntity.getOffhandItem(),1);
                  }
               }else{
                  player.sendSystemMessage(Component.literal("Multiblock not constructed."));
                  multiblock.displayStructure(fletchery.getMultiblockCheck(),player);
               }
            }
         }
         return InteractionResult.SUCCESS_SERVER;
      }
      
      @Override
      public void setPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack){
         BlockEntity entity = world.getBlockEntity(pos);
         if(entity instanceof RadiantFletcheryBlockEntity fletchery){
            initializeArcanaBlock(stack,fletchery);
         }
      }
   }
}

