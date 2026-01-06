package net.borisshoes.arcananovum.blocks;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.core.ArcanaBlock;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerBlockEntity;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerBlockItem;
import net.borisshoes.arcananovum.gui.arcanetome.ArcaneTomeGui;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaIngredient;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaRecipe;
import net.borisshoes.arcananovum.recipes.arcana.ForgeRequirement;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.borislib.utils.MinecraftUtils;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class IgneousCollider extends ArcanaBlock {
	public static final String ID = "igneous_collider";
   
   public static final int COOLDOWN = 15; /// Cooldown is 15 seconds
   
   public IgneousCollider(){
      id = ID;
      name = "Igneous Collider";
      rarity = ArcanaRarity.EMPOWERED;
      categories = new ArcaneTomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), ArcaneTomeGui.TomeFilter.BLOCKS};
      vanillaItem = Items.LODESTONE;
      block = new IgneousColliderBlock(BlockBehaviour.Properties.of().requiresCorrectToolForDrops().strength(3.5f, 1200.0f).sound(SoundType.LODESTONE));
      item = new IgneousColliderItem(this.block);
      displayName = Component.translatableWithFallback("item."+MOD_ID+"."+ID,name).withStyle(ChatFormatting.BOLD, ChatFormatting.DARK_PURPLE);
      researchTasks = new ResourceKey[]{ResearchTasks.OBTAIN_NETHERITE_PICKAXE,ResearchTasks.BREAK_OBSIDIAN,ResearchTasks.ADVANCEMENT_OBTAIN_CRYING_OBSIDIAN,ResearchTasks.ADVANCEMENT_ENCHANT_ITEM,ResearchTasks.UNLOCK_MIDNIGHT_ENCHANTER,ResearchTasks.UNLOCK_STELLAR_CORE};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getDefaultMaxStackSize());
      setPrefStack(stack);
   }
   
   @Override
   public List<Component> getItemLore(@Nullable ItemStack itemStack){
      List<MutableComponent> lore = new ArrayList<>();
      lore.add(Component.literal("")
            .append(Component.literal("Mining ").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal("obsidian ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("is a pain, now this ").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal("device ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("can make it ").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal("automatically").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal(".").withStyle(ChatFormatting.LIGHT_PURPLE)));
      lore.add(Component.literal("")
            .append(Component.literal("Place ").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal("lava ").withStyle(ChatFormatting.GOLD))
            .append(Component.literal("and ").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal("water ").withStyle(ChatFormatting.BLUE))
            .append(Component.literal("sources or cauldrons adjacent to the ").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal("Collider").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal(".").withStyle(ChatFormatting.LIGHT_PURPLE)));
      lore.add(Component.literal("")
            .append(Component.literal("Obsidian ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("will be ").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal("spat out").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal(" or into a ").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal("chest ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("above it ").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal("periodically").withStyle(ChatFormatting.BLUE))
            .append(Component.literal(".").withStyle(ChatFormatting.LIGHT_PURPLE)));
      lore.add(Component.literal("")
            .append(Component.literal("If a ").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal("netherite block").withStyle(ChatFormatting.DARK_RED))
            .append(Component.literal(" is below the ").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal("Collider").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal(", ").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal("crying obsidian").withColor(0x660066))
            .append(Component.literal(" will be made.").withStyle(ChatFormatting.LIGHT_PURPLE)));
     return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   public List<List<Component>> getBookLore(){
      List<List<Component>> list = new ArrayList<>();
      list.add(List.of(Component.literal(" Igneous Collider").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD), Component.literal("\nRarity: ").withStyle(ChatFormatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(),false)), Component.literal("\nMining Obsidian sucks. It's time intensive and mindlessly boring. Making a contraption to do it for me would be of great benefit. Some ductwork and enchanted pickaxes should work nicely.").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal(" Igneous Collider").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD), Component.literal("\nThe Igneous Collider takes water and lava from either a source block or a cauldron that is adjacent to its side and spits out an Obsidian above it every minute. A Netherite block below the Collider produces Crying Obsidian.").withStyle(ChatFormatting.BLACK)));
      return list;
   }
   
   public class IgneousColliderItem extends ArcanaPolymerBlockItem {
      public IgneousColliderItem(Block block){
         super(getThis(),block, getArcanaItemComponents());
      }
      
      @Override
      public ItemStack getDefaultInstance(){
         return prefItem;
      }
   }
   
   public class IgneousColliderBlock extends ArcanaPolymerBlockEntity {
      public IgneousColliderBlock(BlockBehaviour.Properties settings){
         super(getThis(), settings);
      }
      
      @Override
      public BlockState getPolymerBlockState(BlockState state, PacketContext context){
         return Blocks.LODESTONE.defaultBlockState();
      }
      
      @Nullable
      public static IgneousColliderBlockEntity getEntity(Level world, BlockPos pos){
         BlockState state = world.getBlockState(pos);
         if(!(state.getBlock() instanceof IgneousColliderBlock)){
            return null;
         }
         return world.getBlockEntity(pos) instanceof IgneousColliderBlockEntity collider ? collider : null;
      }
      
      @Override
      public BlockEntity newBlockEntity(BlockPos pos, BlockState state){
         return new IgneousColliderBlockEntity(pos, state);
      }
      
      @Nullable
      @Override
      public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type){
         return createTickerHelper(type, ArcanaRegistry.IGNEOUS_COLLIDER_BLOCK_ENTITY, IgneousColliderBlockEntity::ticker);
      }
      
      @Override
      public void setPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack){
         BlockEntity entity = world.getBlockEntity(pos);
         if(entity instanceof IgneousColliderBlockEntity collider){
            initializeArcanaBlock(stack,collider);
         }
      }
   }
}

