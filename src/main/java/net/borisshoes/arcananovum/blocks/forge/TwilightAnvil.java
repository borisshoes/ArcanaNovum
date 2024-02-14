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
import net.borisshoes.arcananovum.recipes.arcana.MagicItemIngredient;
import net.borisshoes.arcananovum.recipes.arcana.MagicItemRecipe;
import net.borisshoes.arcananovum.utils.MagicRarity;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
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
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
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

public class TwilightAnvil extends MagicBlock implements MultiblockCore {
   
   private Multiblock multiblock;
   
   public TwilightAnvil(){
      id = "twilight_anvil";
      name = "Twilight Anvil";
      rarity = MagicRarity.EMPOWERED;
      categories = new ArcaneTome.TomeFilter[]{ArcaneTome.TomeFilter.EMPOWERED, ArcaneTome.TomeFilter.BLOCKS, ArcaneTome.TomeFilter.FORGE};
      itemVersion = 0;
      vanillaItem = Items.ANVIL;
      block = new TwilightAnvilBlock(FabricBlockSettings.create().mapColor(MapColor.IRON_GRAY).requiresTool().strength(5.0f, 1200.0f).sounds(BlockSoundGroup.ANVIL));
      item = new TwilightAnvilItem(this.block,new FabricItemSettings().maxCount(1).fireproof());
      
      ItemStack stack = new ItemStack(item);
      NbtCompound tag = stack.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList enchants = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name","[{\"text\":\"Twilight Anvil\",\"italic\":false,\"bold\":true,\"color\":\"blue\"}]");
      tag.put("display",display);
      tag.put("Enchantments",enchants);
      
      setBookLore(makeLore());
      setRecipe(makeRecipe());
      stack.setNbt(addMagicNbt(tag));
      setPrefStack(stack);
   }
   
   @Override
   public NbtList getItemLore(@Nullable ItemStack itemStack){
      NbtList loreList = new NbtList();
      loreList.add(NbtString.of("[{\"text\":\"A \",\"italic\":false,\"color\":\"dark_aqua\"},{\"text\":\"Forge Structure\",\"color\":\"light_purple\"},{\"text\":\" addon to the \"},{\"text\":\"Starlight Forge\",\"color\":\"light_purple\"},{\"text\":\".\",\"color\":\"dark_aqua\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"An improved \",\"italic\":false,\"color\":\"dark_aqua\"},{\"text\":\"anvil \",\"color\":\"blue\"},{\"text\":\"with \"},{\"text\":\"no XP limit\",\"color\":\"yellow\"},{\"text\":\".\",\"color\":\"dark_aqua\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"The \",\"italic\":false,\"color\":\"dark_aqua\"},{\"text\":\"Anvil \",\"color\":\"blue\"},{\"text\":\"can be used to rename \"},{\"text\":\"Magic Items\",\"color\":\"dark_purple\"},{\"text\":\", and apply \"},{\"text\":\"augments\",\"color\":\"yellow\"},{\"text\":\".\",\"color\":\"dark_aqua\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"The \",\"italic\":false,\"color\":\"dark_aqua\"},{\"text\":\"Anvil \",\"color\":\"blue\"},{\"text\":\"can also be used to combine \"},{\"text\":\"enhanced equipment\",\"color\":\"yellow\"},{\"text\":\".\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      addForgeLore(loreList);
      return loreList;
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
      MagicItemIngredient a = new MagicItemIngredient(Items.DIAMOND,16,null);
      MagicItemIngredient b = new MagicItemIngredient(Items.NETHERITE_SCRAP,2,null);
      MagicItemIngredient c = new MagicItemIngredient(Items.IRON_INGOT,64,null);
      MagicItemIngredient g = new MagicItemIngredient(Items.EXPERIENCE_BOTTLE,64,null);
      MagicItemIngredient h = new MagicItemIngredient(Items.NETHERITE_INGOT,1,null);
      MagicItemIngredient m = new MagicItemIngredient(Items.ANVIL,64,null);
      
      MagicItemIngredient[][] ingredients = {
            {a,b,c,b,a},
            {b,g,h,g,b},
            {c,h,m,h,c},
            {b,g,h,g,b},
            {a,b,c,b,a}};
      return new MagicItemRecipe(ingredients);
   }
   
   private List<String> makeLore(){
      ArrayList<String> list = new ArrayList<>();
      list.add("{\"text\":\"      Twilight Anvil\\n\\nRarity: Empowered\\n\\nAnvils made of Iron have their limits. They dont interact with Magic, and they are less durable than the diamond and netherite equipment used on them. An anvil reinforced with netherite and infused\"}");
      list.add("{\"text\":\"      Twilight Anvil\\n\\nwith Arcana will have no such weaknesses.\\n\\nThe Anvil can act as a normal anvil, with its XP limit removed as well as allowing for the Augmentation and renaming of Magic Items. It also is able to combine enhanced items from the Forge.\"}");
      return list;
   }
   
   public class TwilightAnvilItem extends MagicPolymerBlockItem {
      public TwilightAnvilItem(Block block, Settings settings){
         super(getThis(),block, settings);
      }
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
   }
   
   public class TwilightAnvilBlock extends MagicPolymerBlockEntity {
      public static final DirectionProperty HORIZONTAL_FACING = Properties.HORIZONTAL_FACING;
      
      public TwilightAnvilBlock(Settings settings){
         super(settings);
      }
      
      @Override
      public Block getPolymerBlock(BlockState state) {
         return Blocks.ANVIL;
      }
      
      @Override
      public BlockState getPolymerBlockState(BlockState state){
         return super.getPolymerBlockState(state).with(HORIZONTAL_FACING,state.get(HORIZONTAL_FACING));
      }
      
      @Nullable
      @Override
      public BlockState getPlacementState(ItemPlacementContext ctx) {
         return this.getDefaultState().with(HORIZONTAL_FACING,ctx.getHorizontalPlayerFacing().rotateYClockwise());
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
         return validateTicker(type, ArcanaRegistry.TWILIGHT_ANVIL_BLOCK_ENTITY, TwilightAnvilBlockEntity::ticker);
      }
      
      @Override
      public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity playerEntity, Hand hand, BlockHitResult hit){
         if(hand != Hand.MAIN_HAND) return ActionResult.PASS;
         TwilightAnvilBlockEntity anvil = (TwilightAnvilBlockEntity) world.getBlockEntity(pos);
         if(anvil != null){
            if(playerEntity instanceof ServerPlayerEntity player){
               if(anvil.isAssembled()){
                  if(StarlightForge.findActiveForge(player.getServerWorld(),pos) == null){
                     player.sendMessage(Text.literal("The Anvil must be within the range of an active Starlight Forge"));
                  }else{
                     anvil.openGui(0, player,"");
                  }
               }else{
                  player.sendMessage(Text.literal("Multiblock not constructed."));
                  multiblock.displayStructure(anvil.getMultiblockCheck());
               }
            }
         }
         return ActionResult.SUCCESS;
      }
      
      @Nullable
      public static TwilightAnvilBlockEntity getEntity(World world, BlockPos pos) {
         BlockState state = world.getBlockState(pos);
         if (!(state.getBlock() instanceof TwilightAnvilBlock)) {
            return null;
         }
         return world.getBlockEntity(pos) instanceof TwilightAnvilBlockEntity anvil ? anvil : null;
      }
      
      @Override
      public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
         return new TwilightAnvilBlockEntity(pos, state);
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
         if (placer instanceof ServerPlayerEntity player && entity instanceof TwilightAnvilBlockEntity anvil) {
            initializeMagicBlock(stack,anvil);
         }
      }
   }
}
