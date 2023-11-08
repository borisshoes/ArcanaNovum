package net.borisshoes.arcananovum.blocks;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.core.MagicBlock;
import net.borisshoes.arcananovum.core.polymer.MagicPolymerBlockEntity;
import net.borisshoes.arcananovum.core.polymer.MagicPolymerBlockItem;
import net.borisshoes.arcananovum.items.ArcaneTome;
import net.borisshoes.arcananovum.recipes.arcana.ForgeRequirement;
import net.borisshoes.arcananovum.recipes.arcana.MagicItemIngredient;
import net.borisshoes.arcananovum.recipes.arcana.MagicItemRecipe;
import net.borisshoes.arcananovum.utils.MagicRarity;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class IgneousCollider extends MagicBlock {
   
   public static final int COOLDOWN = 15; /// Cooldown is 15 seconds
   
   public IgneousCollider(){
      id = "igneous_collider";
      name = "Igneous Collider";
      rarity = MagicRarity.EXOTIC;
      categories = new ArcaneTome.TomeFilter[]{ArcaneTome.TomeFilter.EXOTIC, ArcaneTome.TomeFilter.BLOCKS};
      vanillaItem = Items.LODESTONE;
      block = new IgneousColliderBlock(FabricBlockSettings.create().requiresTool().strength(3.5f, 1200.0f).sounds(BlockSoundGroup.LODESTONE));
      item = new IgneousColliderItem(this.block,new FabricItemSettings().maxCount(1).fireproof());
   
      ItemStack stack = new ItemStack(item);
      NbtCompound tag = stack.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList enchants = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name","[{\"text\":\"Igneous Collider\",\"italic\":false,\"bold\":true,\"color\":\"dark_purple\"}]");
      tag.put("display",display);
      tag.put("Enchantments",enchants);
      buildItemLore(stack, ArcanaNovum.SERVER);
   
      setBookLore(makeLore());
      setRecipe(makeRecipe());
      prefNBT = addMagicNbt(tag);
   
      stack.setNbt(prefNBT);
      prefItem = stack;
   }
   
   @Override
   public NbtList getItemLore(@Nullable ItemStack itemStack){
      NbtList loreList = new NbtList();
      loreList.add(NbtString.of("[{\"text\":\"Mining \",\"italic\":false,\"color\":\"light_purple\"},{\"text\":\"obsidian \",\"color\":\"dark_purple\"},{\"text\":\"is a pain, now this \"},{\"text\":\"device \",\"color\":\"dark_aqua\"},{\"text\":\"can make it \"},{\"text\":\"automatically\",\"color\":\"dark_aqua\"},{\"text\":\".\",\"color\":\"light_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Place \",\"italic\":false,\"color\":\"light_purple\"},{\"text\":\"lava \",\"color\":\"gold\"},{\"text\":\"and \"},{\"text\":\"water \",\"color\":\"dark_blue\"},{\"text\":\"sources or cauldrons adjacent to the \"},{\"text\":\"Collider\",\"color\":\"dark_purple\"},{\"text\":\".\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Obsidian \",\"italic\":false,\"color\":\"dark_purple\"},{\"text\":\"will be \",\"color\":\"light_purple\"},{\"text\":\"spat out\",\"color\":\"dark_aqua\"},{\"text\":\" or into a \",\"color\":\"light_purple\"},{\"text\":\"chest \",\"color\":\"dark_aqua\"},{\"text\":\"above it \",\"color\":\"light_purple\"},{\"text\":\"periodically\",\"color\":\"blue\"},{\"text\":\".\",\"color\":\"light_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"If a \",\"italic\":false,\"color\":\"light_purple\"},{\"text\":\"netherite block\",\"color\":\"dark_red\"},{\"text\":\" is below the \"},{\"text\":\"Collider\",\"color\":\"dark_purple\"},{\"text\":\", \"},{\"text\":\"crying obsidian\",\"color\":\"#660066\"},{\"text\":\" will be made.\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      return loreList;
   }
   
   private MagicItemRecipe makeRecipe(){
      MagicItemIngredient o = new MagicItemIngredient(Items.OBSIDIAN,64,null);
      MagicItemIngredient p = new MagicItemIngredient(Items.CRYING_OBSIDIAN,64,null);
      MagicItemIngredient i = new MagicItemIngredient(Items.BLUE_ICE,64,null);
      MagicItemIngredient m = new MagicItemIngredient(Items.MAGMA_BLOCK,64,null);
      MagicItemIngredient c = new MagicItemIngredient(Items.CAULDRON,64,null);
      MagicItemIngredient l = new MagicItemIngredient(Items.LODESTONE,4,null);
      MagicItemIngredient d = new MagicItemIngredient(Items.DIAMOND_PICKAXE,1, MagicItemIngredient.getEnchantNbt(new Pair(Enchantments.EFFICIENCY,5)));
      
      MagicItemIngredient[][] ingredients = {
            {o,p,o,p,o},
            {p,i,c,m,p},
            {o,d,l,d,o},
            {p,i,c,m,p},
            {o,p,o,p,o}};
      return new MagicItemRecipe(ingredients,new ForgeRequirement().withEnchanter());
   }
   
   private List<String> makeLore(){
      ArrayList<String> list = new ArrayList<>();
      list.add("{\"text\":\"    Igneous Collider\\n\\nRarity: Exotic\\n\\nMining Obsidian sucks, its time intensive and boring.\\nMaking a contraption to do it for me would be of great benefit.\\nI guess was as simple as enchanting some pickaxes to move by themself.\"}");
      list.add("{\"text\":\"    Igneous Collider\\n\\nThe Igneous Collider takes water and lava from either a source block or a cauldron that is adjacent to its side and spits out an Obsidian into a chest above it every minute.\\nA Collider with a Netherite block below it produces Crying Obsidian. \"}");
      return list;
   }
   
   public class IgneousColliderItem extends MagicPolymerBlockItem {
      public IgneousColliderItem(Block block, Settings settings){
         super(getThis(),block, settings);
      }
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
   }
   
   public class IgneousColliderBlock extends MagicPolymerBlockEntity{
      public IgneousColliderBlock(Settings settings){
         super(settings);
      }
      
      @Override
      public Block getPolymerBlock(BlockState state) {
         return Blocks.LODESTONE;
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
         return checkType(type, ArcanaRegistry.IGNEOUS_COLLIDER_BLOCK_ENTITY, IgneousColliderBlockEntity::ticker);
      }
      
      @Override
      public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
         if (world.getBlockEntity(pos) instanceof IgneousColliderBlockEntity collider) {
            dropBlockItem(world,pos,state,player,collider);

            world.removeBlockEntity(pos);
         }
         
         world.removeBlock(pos, false);
         
         super.onBreak(world, pos, state, player);
      }
      
      @Override
      public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
         BlockEntity entity = world.getBlockEntity(pos);
         if (placer instanceof ServerPlayerEntity && entity instanceof IgneousColliderBlockEntity collider) {
            initializeMagicBlock(stack,collider);
         }
      }
   }
}
