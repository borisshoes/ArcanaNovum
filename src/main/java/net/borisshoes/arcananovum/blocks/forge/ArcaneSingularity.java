package net.borisshoes.arcananovum.blocks.forge;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.core.MagicBlock;
import net.borisshoes.arcananovum.core.MagicBlockEntity;
import net.borisshoes.arcananovum.core.Multiblock;
import net.borisshoes.arcananovum.core.MultiblockCore;
import net.borisshoes.arcananovum.core.polymer.MagicPolymerBlockEntity;
import net.borisshoes.arcananovum.core.polymer.MagicPolymerBlockItem;
import net.borisshoes.arcananovum.items.ArcaneTome;
import net.borisshoes.arcananovum.recipes.arcana.ForgeRequirement;
import net.borisshoes.arcananovum.recipes.arcana.GenericMagicIngredient;
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
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ArcaneSingularity extends MagicBlock implements MultiblockCore {
   
   private Multiblock multiblock;
   
   public ArcaneSingularity(){
      id = "arcane_singularity";
      name = "Arcane Singularity";
      rarity = MagicRarity.LEGENDARY;
      categories = new ArcaneTome.TomeFilter[]{ArcaneTome.TomeFilter.LEGENDARY, ArcaneTome.TomeFilter.BLOCKS, ArcaneTome.TomeFilter.FORGE};
      itemVersion = 0;
      vanillaItem = Items.LECTERN;
      block = new ArcaneSingularityBlock(FabricBlockSettings.create().strength(2.5f,1200.0f).sounds(BlockSoundGroup.WOOD));
      item = new ArcaneSingularityItem(this.block,new FabricItemSettings().maxCount(1).fireproof());
      
      ItemStack stack = new ItemStack(item);
      NbtCompound tag = stack.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList enchants = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name","[{\"text\":\"Arcane Singularity\",\"italic\":false,\"bold\":true,\"color\":\"light_purple\"}]");
      tag.put("display",display);
      tag.put("Enchantments",enchants);
      
      setBookLore(makeLore());
      setRecipe(makeRecipe());
      addMagicNbt(tag);
      NbtList booksList = new NbtList();
      tag.getCompound("arcananovum").put("books",booksList);
      stack.setNbt(tag);
      setPrefStack(stack);
   }
   
   @Override
   public NbtList getItemLore(@Nullable ItemStack itemStack){
      NbtList loreList = new NbtList();
      loreList.add(NbtString.of("[{\"text\":\"A \",\"italic\":false,\"color\":\"dark_aqua\"},{\"text\":\"Forge Structure\",\"color\":\"light_purple\"},{\"text\":\" addon to the\"},{\"text\":\" \",\"color\":\"light_purple\"},{\"text\":\"Starlight Forge\",\"color\":\"light_purple\"},{\"text\":\".\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Storing \",\"italic\":false,\"color\":\"dark_aqua\"},{\"text\":\"enchantments\",\"color\":\"light_purple\"},{\"text\":\" on books is\"},{\"text\":\" \",\"color\":\"light_purple\"},{\"text\":\"crude\",\"color\":\"green\"},{\"text\":\" and \"},{\"text\":\"primitive.\",\"color\":\"green\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Condensing \",\"italic\":false,\"color\":\"green\"},{\"text\":\"their \",\"color\":\"dark_aqua\"},{\"text\":\"essence \",\"color\":\"light_purple\"},{\"text\":\"into a \",\"color\":\"dark_aqua\"},{\"text\":\"singularity \",\"color\":\"light_purple\"},{\"text\":\"is much more \",\"color\":\"dark_aqua\"},{\"text\":\"modern\"},{\"text\":\".\",\"color\":\"dark_aqua\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Allows for the \",\"italic\":false,\"color\":\"dark_aqua\"},{\"text\":\"storing of \",\"color\":\"green\"},{\"text\":\"enchantments\",\"color\":\"light_purple\"},{\"text\":\" in a \"},{\"text\":\"virtual library.\",\"color\":\"green\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Works well\",\"italic\":false,\"color\":\"light_purple\"},{\"text\":\" with the \",\"color\":\"dark_aqua\"},{\"text\":\"Midnight Enchanter\",\"color\":\"blue\"},{\"text\":\".\",\"color\":\"dark_aqua\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      addForgeLore(loreList);
      return loreList;
   }
   
   @Override
   public ItemStack updateItem(ItemStack stack, MinecraftServer server){
      NbtCompound itemNbt = stack.getNbt();
      NbtCompound magicTag = itemNbt.getCompound("arcananovum");
      NbtList bookList = magicTag.getList("books",NbtElement.COMPOUND_TYPE);
      NbtCompound newTag = super.updateItem(stack,server).getNbt();
      newTag.getCompound("arcananovum").put("books",bookList);
      stack.setNbt(newTag);
      return buildItemLore(stack,server);
   }
   
   @Override
   public void loadMultiblock(){
      multiblock = Multiblock.loadFromFile(getId());
   }
   
   @Override
   public Multiblock getMultiblock(){
      return multiblock;
   }
   
   private MagicItemRecipe makeRecipe(){
      MagicItemIngredient a = new MagicItemIngredient(ArcanaRegistry.NEBULOUS_ESSENCE,64, null);
      MagicItemIngredient b = new MagicItemIngredient(Items.NETHERITE_INGOT,2,null);
      MagicItemIngredient c = new MagicItemIngredient(Items.NETHER_STAR,2,null);
      MagicItemIngredient g = new MagicItemIngredient(Items.ENDER_EYE,64,null);
      GenericMagicIngredient m = new GenericMagicIngredient(ArcanaRegistry.MIDNIGHT_ENCHANTER,1);
      
      MagicItemIngredient[][] ingredients = {
            {a,b,c,b,a},
            {b,g,a,g,b},
            {c,a,m,a,c},
            {b,g,a,g,b},
            {a,b,c,b,a}};
      return new MagicItemRecipe(ingredients, new ForgeRequirement().withEnchanter());
   }
   
   private List<String> makeLore(){
      ArrayList<String> list = new ArrayList<>();
      list.add("{\"text\":\"  Arcane Singularity\\n\\nRarity: Legendary\\n\\nThe Midnight Enchanter has proven more useful than I imagined. I now have more enchanted books than I can possibly store in a library.\\n\\nI need a new method of book-keeping.\"}");
      list.add("{\"text\":\"  Arcane Singularity\\n\\nBy condensing raw Nebulous Essence down, over and over, it forms a self- sustaining singularity.\\nAn Arcane black hole.\\nContaining it was no easy feat, but now I have a massive storage space for my books...\"}");
      list.add("{\"text\":\"  Arcane Singularity\\n\\nThe Singularity is a single purpose high density storage unit for Enchanted Books.\\nBooks can be sorted and filtered while stored, and can be extracted at any time.\\nBooks are bound to the Singularity, so they can be moved with the Singularity.\"}");
      return list;
   }
   
   public class ArcaneSingularityItem extends MagicPolymerBlockItem {
      
      public ArcaneSingularityItem(Block block, Settings settings){
         super(getThis(),block, settings);
      }
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
   }
   
   public class ArcaneSingularityBlock extends MagicPolymerBlockEntity {
      public static final DirectionProperty HORIZONTAL_FACING = Properties.HORIZONTAL_FACING;
      
      public ArcaneSingularityBlock(Settings settings){
         super(settings);
      }
      
      @Override
      public Block getPolymerBlock(BlockState state) {
         return Blocks.LECTERN;
      }
      
      @Override
      public BlockState getPolymerBlockState(BlockState state){
         return super.getPolymerBlockState(state).with(HORIZONTAL_FACING,state.get(HORIZONTAL_FACING));
      }
      
      @Nullable
      @Override
      public BlockState getPlacementState(ItemPlacementContext ctx) {
         return this.getDefaultState().with(HORIZONTAL_FACING,ctx.getHorizontalPlayerFacing().getOpposite());
      }
      
      @Override
      protected void appendProperties(StateManager.Builder<Block, BlockState> stateManager) {
         stateManager.add(HORIZONTAL_FACING);
      }
      
      @Override
      public BlockState rotate(BlockState state, BlockRotation rotation) {
         return state.with(HORIZONTAL_FACING, rotation.rotate(state.get(HORIZONTAL_FACING)));
      }
      
      @Override
      public BlockState mirror(BlockState state, BlockMirror mirror) {
         return state.rotate(mirror.getRotation(state.get(HORIZONTAL_FACING)));
      }
      
      @Nullable
      @Override
      public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
         return validateTicker(type, ArcanaRegistry.ARCANE_SINGULARITY_BLOCK_ENTITY, ArcaneSingularityBlockEntity::ticker);
      }
      
      @Override
      public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity playerEntity, Hand hand, BlockHitResult hit){
         if(hand != Hand.MAIN_HAND) return ActionResult.PASS;
         ArcaneSingularityBlockEntity singularity = (ArcaneSingularityBlockEntity) world.getBlockEntity(pos);
         if(singularity != null){
            if(playerEntity instanceof ServerPlayerEntity player){
               if(singularity.isAssembled()){
                  if(StarlightForge.findActiveForge(player.getServerWorld(),pos) == null){
                     player.sendMessage(Text.literal("The Enchanter must be within the range of an active Starlight Forge"));
                  }else{
                     singularity.openGui(player);
                  }
               }else{
                  player.sendMessage(Text.literal("Multiblock not constructed."));
                  multiblock.displayStructure(singularity.getMultiblockCheck());
               }
            }
         }
         return ActionResult.SUCCESS;
      }
      
      @Nullable
      public static ArcaneSingularityBlockEntity getEntity(World world, BlockPos pos) {
         BlockState state = world.getBlockState(pos);
         if (!(state.getBlock() instanceof ArcaneSingularityBlock)) {
            return null;
         }
         return world.getBlockEntity(pos) instanceof ArcaneSingularityBlockEntity singularity ? singularity : null;
      }
      
      @Override
      public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
         return new ArcaneSingularityBlockEntity(pos, state);
      }
      
      @Override
      public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
         if (state.isOf(newState.getBlock())) {
            return;
         }
         BlockEntity blockEntity = world.getBlockEntity(pos);
         if(!(blockEntity instanceof MagicBlockEntity mbe)) return;
         DefaultedList<ItemStack> drops = DefaultedList.of();
         drops.add(getDroppedBlockItem(state,world,null,blockEntity));
         ItemScatterer.spawn(world, pos, drops);
         super.onStateReplaced(state, world, pos, newState, moved);
      }
      
      @Override
      public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
         BlockEntity entity = world.getBlockEntity(pos);
         if (placer instanceof ServerPlayerEntity player && entity instanceof ArcaneSingularityBlockEntity singularity) {
            initializeMagicBlock(stack,singularity);
            singularity.readBooks(stack.getNbt().getCompound("arcananovum").getList("books", NbtElement.COMPOUND_TYPE));
         }
      }
   }
}
