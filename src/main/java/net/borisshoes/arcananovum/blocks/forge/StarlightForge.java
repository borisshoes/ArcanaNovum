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
import net.borisshoes.arcananovum.utils.ArcanaColors;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
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

public class StarlightForge extends ArcanaBlock implements MultiblockCore {
   public static final String SEED_USES_TAG = "seedUses";
   
	public static final String ID = "starlight_forge";
   
   private Multiblock multiblock;
   
   public StarlightForge(){
      id = ID;
      name = "Starlight Forge";
      rarity = ArcanaRarity.EMPOWERED;
      categories = new ArcaneTomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), ArcaneTomeGui.TomeFilter.BLOCKS, ArcaneTomeGui.TomeFilter.FORGE};
      itemVersion = 0;
      vanillaItem = Items.SMITHING_TABLE;
      block = new StarlightForgeBlock(BlockBehaviour.Properties.of().strength(2.5f,1200.0f).sound(SoundType.WOOD));
      item = new StarlightForgeItem(this.block);
      displayName = Component.translatableWithFallback("item."+MOD_ID+"."+ID,name).withStyle(ChatFormatting.BOLD).withColor(ArcanaColors.STARLIGHT_FORGE_COLOR);
      researchTasks = new ResourceKey[]{ResearchTasks.OBTAIN_ARCANE_TOME,ResearchTasks.OBTAIN_ENCHANTED_GOLDEN_APPLE};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getDefaultMaxStackSize());
      putProperty(stack,SEED_USES_TAG,0);
      setPrefStack(stack);
   }
   
   @Override
   public List<Component> getItemLore(@Nullable ItemStack itemStack){
      List<MutableComponent> lore = new ArrayList<>();
      lore.add(Component.literal("")
            .append(Component.literal("With the ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("stars ").withStyle(ChatFormatting.WHITE))
            .append(Component.literal("as your witness...").withStyle(ChatFormatting.DARK_PURPLE)));
      lore.add(Component.literal("")
            .append(Component.literal("Your ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("journey ").withStyle(ChatFormatting.WHITE))
            .append(Component.literal("of ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("forging ").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal("new ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("Arcana ").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal("begins!").withStyle(ChatFormatting.DARK_PURPLE)));
      lore.add(Component.literal(""));
      lore.add(Component.literal("")
            .append(Component.literal("The ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("Forge").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(" lets you craft ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("Arcana Items").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(" and ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("enhanced equipment").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_PURPLE)));
      lore.add(Component.literal("")
            .append(Component.literal("The ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("Forge").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(" acts as a ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("hub ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("for other ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("Forge Structures").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_PURPLE)));
      addForgeLore(lore);
     return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   public void loadMultiblock(){
      multiblock = Multiblock.loadFromFile(getId());
   }
   
   @Override
   public Vec3i getCheckOffset(){
      return new Vec3i(-1,-1,-1);
   }
   
   @Override
   public Multiblock getMultiblock(){
      return multiblock;
   }
   
   public static StarlightForgeBlockEntity findActiveForge(ServerLevel world, BlockPos searchingPos){
      BlockPos range = new BlockPos(15, 8, 15);
      for(BlockPos blockPos : BlockPos.betweenClosed(searchingPos.offset(range), searchingPos.subtract(range))){
         BlockEntity be = world.getBlockEntity(blockPos);
         if(be instanceof StarlightForgeBlockEntity forge && forge.isAssembled()){
            BlockPos offset = blockPos.subtract(searchingPos);
            BlockPos forgeRange = forge.getForgeRange();
            if(Math.abs(offset.getX()) <= forgeRange.getX() && Math.abs(offset.getY()) <= forgeRange.getY() && Math.abs(offset.getZ()) <= forgeRange.getZ()) return forge;
         }
      }
      return null;
   }
   
   @Override
   public List<List<Component>> getBookLore(){
      List<List<Component>> list = new ArrayList<>();
      list.add(List.of(Component.literal("  Starlight Forge").withStyle(ChatFormatting.BOLD).withColor(ArcanaColors.STARLIGHT_FORGE_COLOR), Component.literal("\nRarity: ").withStyle(ChatFormatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(),false)), Component.literal("\nEnchanted Golden Apples are a unique arcane artifact that I have discovered. Modern replicants do not seem to hold the same caliber of properties. My latest theories of Arcana suggest that the magic ").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("  Starlight Forge").withStyle(ChatFormatting.BOLD).withColor(ArcanaColors.STARLIGHT_FORGE_COLOR), Component.literal("\nof this land is far more versatile than the old scholars believed. I just need something to kickstart my new field of research. It is possible that I can use some energy from starlight to transfer the ancient enchantment of a ").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("  Starlight Forge").withStyle(ChatFormatting.BOLD).withColor(ArcanaColors.STARLIGHT_FORGE_COLOR), Component.literal("\nGolden Apple. If I am to be successful in my research, I will need a forge…\n\nThe Starlight Forge allows the creation of infused weapons, tools, and armor.\n\nIt creates a 17x11x17 workspace that can ").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("  Starlight Forge").withStyle(ChatFormatting.BOLD).withColor(ArcanaColors.STARLIGHT_FORGE_COLOR), Component.literal("\ninteract with additions to the forge that can be crafted as I advance my research.").withStyle(ChatFormatting.BLACK)));
      return list;
   }
   
   public class StarlightForgeItem extends ArcanaPolymerBlockItem {
      public StarlightForgeItem(Block block){
         super(getThis(),block, getArcanaItemComponents());
      }
      
      @Override
      public ItemStack getDefaultInstance(){
         return prefItem;
      }
   }
   
   public class StarlightForgeBlock extends ArcanaPolymerBlockEntity {
      public StarlightForgeBlock(BlockBehaviour.Properties settings){
         super(getThis(), settings);
      }
      
      @Override
      public BlockState getPolymerBlockState(BlockState state, PacketContext context){
         return Blocks.SMITHING_TABLE.defaultBlockState();
      }
      
      @Nullable
      public static StarlightForgeBlockEntity getEntity(Level world, BlockPos pos){
         BlockState state = world.getBlockState(pos);
         if(!(state.getBlock() instanceof StarlightForgeBlock)){
            return null;
         }
         return world.getBlockEntity(pos) instanceof StarlightForgeBlockEntity forge ? forge : null;
      }
      
      @Override
      public BlockEntity newBlockEntity(BlockPos pos, BlockState state){
         return new StarlightForgeBlockEntity(pos, state);
      }
      
      @Nullable
      @Override
      public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type){
         return createTickerHelper(type, ArcanaRegistry.STARLIGHT_FORGE_BLOCK_ENTITY, StarlightForgeBlockEntity::ticker);
      }
      
      @Override
      public InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player playerEntity, BlockHitResult hit){
         StarlightForgeBlockEntity forge = (StarlightForgeBlockEntity) world.getBlockEntity(pos);
         if(forge != null){
            if(playerEntity instanceof ServerPlayer player){
               if(forge.isAssembled()){
                  forge.openMainGui(player,null);
               }else{
                  player.sendSystemMessage(Component.literal("Multiblock not constructed."));
                  multiblock.displayStructure(forge.getMultiblockCheck(),player);
               }
            }
         }
         return InteractionResult.SUCCESS_SERVER;
      }
      
      @Override
      public void setPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack){
         BlockEntity entity = world.getBlockEntity(pos);
         if(entity instanceof StarlightForgeBlockEntity forge){
            initializeArcanaBlock(stack,forge);
            forge.setSeedUses(getIntProperty(stack,SEED_USES_TAG));
         }
      }
   }
}

