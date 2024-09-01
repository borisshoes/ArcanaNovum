package net.borisshoes.arcananovum.blocks;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.core.ArcanaBlock;
import net.borisshoes.arcananovum.core.ArcanaBlockEntity;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerBlockEntity;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerBlockItem;
import net.borisshoes.arcananovum.gui.arcanetome.TomeGui;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaIngredient;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaRecipe;
import net.borisshoes.arcananovum.recipes.arcana.ForgeRequirement;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaRarity;
import net.borisshoes.arcananovum.utils.MiscUtils;
import net.borisshoes.arcananovum.utils.TextUtils;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class IgneousCollider extends ArcanaBlock {
	public static final String ID = "igneous_collider";
   
   public static final int COOLDOWN = 15; /// Cooldown is 15 seconds
   
   public IgneousCollider(){
      id = ID;
      name = "Igneous Collider";
      rarity = ArcanaRarity.EXOTIC;
      categories = new TomeGui.TomeFilter[]{TomeGui.TomeFilter.EXOTIC, TomeGui.TomeFilter.BLOCKS};
      vanillaItem = Items.LODESTONE;
      block = new IgneousColliderBlock(AbstractBlock.Settings.create().requiresTool().strength(3.5f, 1200.0f).sounds(BlockSoundGroup.LODESTONE));
      item = new IgneousColliderItem(this.block,new Item.Settings().maxCount(1).fireproof()
            .component(DataComponentTypes.ITEM_NAME, Text.literal("Igneous Collider").formatted(Formatting.BOLD,Formatting.DARK_PURPLE))
            .component(DataComponentTypes.LORE, new LoreComponent(getItemLore(null)))
            .component(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true)
      );
      researchTasks = new RegistryKey[]{ResearchTasks.OBTAIN_NETHERITE_PICKAXE,ResearchTasks.BREAK_OBSIDIAN,ResearchTasks.ADVANCEMENT_OBTAIN_CRYING_OBSIDIAN,ResearchTasks.ADVANCEMENT_ENCHANT_ITEM,ResearchTasks.UNLOCK_MIDNIGHT_ENCHANTER,ResearchTasks.UNLOCK_STELLAR_CORE};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getMaxCount());
      setPrefStack(stack);
   }
   
   @Override
   public List<Text> getItemLore(@Nullable ItemStack itemStack){
      List<MutableText> lore = new ArrayList<>();
      lore.add(Text.literal("")
            .append(Text.literal("Mining ").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal("obsidian ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("is a pain, now this ").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal("device ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("can make it ").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal("automatically").formatted(Formatting.DARK_AQUA))
            .append(Text.literal(".").formatted(Formatting.LIGHT_PURPLE)));
      lore.add(Text.literal("")
            .append(Text.literal("Place ").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal("lava ").formatted(Formatting.GOLD))
            .append(Text.literal("and ").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal("water ").formatted(Formatting.DARK_BLUE))
            .append(Text.literal("sources or cauldrons adjacent to the ").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal("Collider").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal(".").formatted(Formatting.LIGHT_PURPLE)));
      lore.add(Text.literal("")
            .append(Text.literal("Obsidian ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("will be ").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal("spat out").formatted(Formatting.DARK_AQUA))
            .append(Text.literal(" or into a ").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal("chest ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("above it ").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal("periodically").formatted(Formatting.BLUE))
            .append(Text.literal(".").formatted(Formatting.LIGHT_PURPLE)));
      lore.add(Text.literal("")
            .append(Text.literal("If a ").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal("netherite block").formatted(Formatting.DARK_RED))
            .append(Text.literal(" is below the ").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal("Collider").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal(", ").formatted(Formatting.LIGHT_PURPLE))
            .append(TextUtils.withColor(Text.literal("crying obsidian"),0x660066))
            .append(Text.literal(" will be made.").formatted(Formatting.LIGHT_PURPLE)));
     return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
	protected ArcanaRecipe makeRecipe(){
      ArcanaIngredient a = new ArcanaIngredient(Items.CRYING_OBSIDIAN,12);
      ArcanaIngredient b = new ArcanaIngredient(Items.OBSIDIAN,16);
      ArcanaIngredient g = new ArcanaIngredient(Items.MAGMA_BLOCK,32);
      ArcanaIngredient h = new ArcanaIngredient(Items.CAULDRON,24);
      ArcanaIngredient i = new ArcanaIngredient(Items.BLUE_ICE,32);
      ArcanaIngredient l = new ArcanaIngredient(Items.NETHERITE_PICKAXE,1, true).withEnchantments(new EnchantmentLevelEntry(MiscUtils.getEnchantment(Enchantments.EFFICIENCY),5), new EnchantmentLevelEntry(MiscUtils.getEnchantment(Enchantments.UNBREAKING),3));
      ArcanaIngredient m = new ArcanaIngredient(Items.NETHERITE_INGOT,1);
      
      ArcanaIngredient[][] ingredients = {
            {a,b,a,b,a},
            {b,g,h,i,b},
            {a,l,m,l,a},
            {b,g,h,i,b},
            {a,b,a,b,a}};
      return new ArcanaRecipe(ingredients,new ForgeRequirement().withAnvil().withCore().withEnchanter());
   }
   
   @Override
   public List<List<Text>> getBookLore(){
      List<List<Text>> list = new ArrayList<>();
      list.add(List.of(Text.literal("    Igneous Collider\n\nRarity: Exotic\n\nMining Obsidian sucks, its time intensive and boring.\nMaking a contraption to do it for me would be of great benefit.\nI guess was as simple as enchanting some pickaxes to move by themself.").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal("    Igneous Collider\n\nThe Igneous Collider takes water and lava from either a source block or a cauldron that is adjacent to its side and spits out an Obsidian into a chest above it every minute.\nA Collider with a Netherite block below it produces Crying Obsidian. ").formatted(Formatting.BLACK)));
      return list;
   }
   
   public class IgneousColliderItem extends ArcanaPolymerBlockItem {
      public IgneousColliderItem(Block block, Settings settings){
         super(getThis(),block, settings);
      }
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
   }
   
   public class IgneousColliderBlock extends ArcanaPolymerBlockEntity {
      public IgneousColliderBlock(AbstractBlock.Settings settings){
         super(settings);
      }
      
      @Override
      public BlockState getPolymerBlockState(BlockState state) {
         return Blocks.LODESTONE.getDefaultState();
      }
      
      @Nullable
      public static IgneousColliderBlockEntity getEntity(World world, BlockPos pos) {
         BlockState state = world.getBlockState(pos);
         if (!(state.getBlock() instanceof IgneousColliderBlock)) {
            return null;
         }
         return world.getBlockEntity(pos) instanceof IgneousColliderBlockEntity collider ? collider : null;
      }
      
      @Override
      public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
         return new IgneousColliderBlockEntity(pos, state);
      }
      
      @Nullable
      @Override
      public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
         return validateTicker(type, ArcanaRegistry.IGNEOUS_COLLIDER_BLOCK_ENTITY, IgneousColliderBlockEntity::ticker);
      }
      
      @Override
      public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
         BlockEntity entity = world.getBlockEntity(pos);
         if (placer instanceof ServerPlayerEntity && entity instanceof IgneousColliderBlockEntity collider) {
            initializeArcanaBlock(stack,collider);
         }
      }
   }
}

