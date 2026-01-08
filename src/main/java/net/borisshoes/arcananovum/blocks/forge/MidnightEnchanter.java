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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class MidnightEnchanter extends ArcanaBlock implements MultiblockCore {
	public static final String ID = "midnight_enchanter";
   
   private Multiblock multiblock;
   
   public MidnightEnchanter(){
      id = ID;
      name = "Midnight Enchanter";
      rarity = ArcanaRarity.EMPOWERED;
      categories = new ArcaneTomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), ArcaneTomeGui.TomeFilter.BLOCKS, ArcaneTomeGui.TomeFilter.FORGE};
      itemVersion = 0;
      vanillaItem = Items.ENCHANTING_TABLE;
      block = new MidnightEnchanterBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).strength(5.0f, 1200.0f).requiresCorrectToolForDrops().lightLevel(state -> 7));
      item = new MidnightEnchanterItem(this.block);
      displayName = Component.translatableWithFallback("item."+MOD_ID+"."+ID,name).withStyle(ChatFormatting.BOLD, ChatFormatting.DARK_AQUA);
      researchTasks = new ResourceKey[]{ResearchTasks.ADVANCEMENT_ENCHANT_ITEM,ResearchTasks.OBTAIN_BOTTLES_OF_ENCHANTING,ResearchTasks.ADVANCEMENT_READ_POWER_OF_CHISELED_BOOKSHELF,ResearchTasks.ADVANCEMENT_OBTAIN_CRYING_OBSIDIAN,ResearchTasks.UNLOCK_STARLIGHT_FORGE};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getDefaultMaxStackSize());
      setPrefStack(stack);
   }
   
   @Override
   public List<Component> getItemLore(@Nullable ItemStack itemStack){
      List<MutableComponent> lore = new ArrayList<>();
      lore.add(Component.literal("")
            .append(Component.literal("A ").withStyle(ChatFormatting.BLUE))
            .append(Component.literal("Forge Structure").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(" addon to the ").withStyle(ChatFormatting.BLUE))
            .append(Component.literal("Starlight Forge").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(".").withStyle(ChatFormatting.BLUE)));
      lore.add(Component.literal("")
            .append(Component.literal("Normal ").withStyle(ChatFormatting.BLUE))
            .append(Component.literal("Enchanting Tables").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal(" are ").withStyle(ChatFormatting.BLUE))
            .append(Component.literal("unpredictable ").withStyle(ChatFormatting.AQUA))
            .append(Component.literal("and ").withStyle(ChatFormatting.BLUE))
            .append(Component.literal("inconsistent").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(".").withStyle(ChatFormatting.BLUE)));
      lore.add(Component.literal("")
            .append(Component.literal("This ").withStyle(ChatFormatting.BLUE))
            .append(Component.literal("Table ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("not only enables ").withStyle(ChatFormatting.BLUE))
            .append(Component.literal("precise control ").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal("of ").withStyle(ChatFormatting.BLUE))
            .append(Component.literal("enchantments").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("...").withStyle(ChatFormatting.BLUE)));
      lore.add(Component.literal("")
            .append(Component.literal("It also allows for ").withStyle(ChatFormatting.BLUE))
            .append(Component.literal("enchantments ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("to be ").withStyle(ChatFormatting.BLUE))
            .append(Component.literal("removed ").withStyle(ChatFormatting.AQUA))
            .append(Component.literal("and ").withStyle(ChatFormatting.BLUE))
            .append(Component.literal("placed ").withStyle(ChatFormatting.AQUA))
            .append(Component.literal("onto ").withStyle(ChatFormatting.BLUE))
            .append(Component.literal("books").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(".").withStyle(ChatFormatting.BLUE)));
      lore.add(Component.literal("")
            .append(Component.literal("Enchantments ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("can also be ").withStyle(ChatFormatting.BLUE))
            .append(Component.literal("broken down").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(" into ").withStyle(ChatFormatting.BLUE))
            .append(Component.literal("Nebulous Essence").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal(".").withStyle(ChatFormatting.BLUE)));
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
      return new Vec3i(-2,-1,-2);
   }
   
   @Override
   public List<List<Component>> getBookLore(){
      List<List<Component>> list = new ArrayList<>();
      list.add(List.of(Component.literal("Midnight Enchanter").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD), Component.literal("\nRarity: ").withStyle(ChatFormatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(),false)), Component.literal("\nEnchanting tables are an old design. It only scratches the surface of how Arcana can be bound to equipment, and relies too much on random environmental fluctuations. If my predictive equations ").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("Midnight Enchanter").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD), Component.literal("\nare correct, I should be able to cancel out the noise in the enchantment matrix and reduce Enchantment Arcana to a pure form, after which it can take any shape of my choosing.\n\nThe Enchanter allows").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("Midnight Enchanter").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD), Component.literal("\ndisenchanting of items to gain Nebulous Essence, which can be spent to choose exact enchantments to place on items. The Enchanter also gives access to normally unavailable enchantments.").withStyle(ChatFormatting.BLACK)));
      return list;
   }
   
   public class MidnightEnchanterItem extends ArcanaPolymerBlockItem {
      public MidnightEnchanterItem(Block block){
         super(getThis(),block, getArcanaItemComponents());
      }
      
      @Override
      public ItemStack getDefaultInstance(){
         return prefItem;
      }
   }
   
   public class MidnightEnchanterBlock extends ArcanaPolymerBlockEntity {
      public MidnightEnchanterBlock(BlockBehaviour.Properties settings){
         super(getThis(), settings);
      }
      
      @Override
      public BlockState getPolymerBlockState(BlockState state, PacketContext context){
         return Blocks.ENCHANTING_TABLE.defaultBlockState();
      }
      
      @Nullable
      public static MidnightEnchanterBlockEntity getEntity(Level world, BlockPos pos){
         BlockState state = world.getBlockState(pos);
         if(!(state.getBlock() instanceof MidnightEnchanterBlock)){
            return null;
         }
         return world.getBlockEntity(pos) instanceof MidnightEnchanterBlockEntity enchanter ? enchanter : null;
      }
      
      @Override
      public BlockEntity newBlockEntity(BlockPos pos, BlockState state){
         return new MidnightEnchanterBlockEntity(pos, state);
      }
      
      @Nullable
      @Override
      public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type){
         return createTickerHelper(type, ArcanaRegistry.MIDNIGHT_ENCHANTER_BLOCK_ENTITY, MidnightEnchanterBlockEntity::ticker);
      }
      
      @Override
      public InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player playerEntity, BlockHitResult hit){
         MidnightEnchanterBlockEntity enchanter = (MidnightEnchanterBlockEntity) world.getBlockEntity(pos);
         if(enchanter != null){
            if(playerEntity instanceof ServerPlayer player){
               if(enchanter.isAssembled()){
                  if(StarlightForge.findActiveForge(player.level(),pos) == null){
                     player.sendSystemMessage(Component.literal("The Enchanter must be within the range of an active Starlight Forge"));
                  }else{
                     if(enchanter.hasBooks()){
                        enchanter.openGui(player);
                     }else{
                        player.sendSystemMessage(Component.literal("The Enchanter needs at least 20 bookshelves nearby"));
                     }
                  }
               }else{
                  player.sendSystemMessage(Component.literal("Multiblock not constructed."));
                  multiblock.displayStructure(enchanter.getMultiblockCheck(),player);
               }
            }
         }
         return InteractionResult.SUCCESS_SERVER;
      }
      
      @Override
      public void setPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack){
         BlockEntity entity = world.getBlockEntity(pos);
         if(entity instanceof MidnightEnchanterBlockEntity enchanter){
            initializeArcanaBlock(stack,enchanter);
         }
      }
   }
}

