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
import net.borisshoes.arcananovum.recipes.arcana.ExplainIngredient;
import net.borisshoes.arcananovum.recipes.arcana.ExplainRecipe;
import net.borisshoes.arcananovum.recipes.arcana.MagicItemRecipe;
import net.borisshoes.arcananovum.utils.MagicRarity;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class StarlightForge extends MagicBlock implements MultiblockCore {
   
   private Multiblock multiblock;
   
   public StarlightForge(){
      id = "starlight_forge";
      name = "Starlight Forge";
      rarity = MagicRarity.EMPOWERED;
      categories = new ArcaneTome.TomeFilter[]{ArcaneTome.TomeFilter.EMPOWERED, ArcaneTome.TomeFilter.BLOCKS, ArcaneTome.TomeFilter.FORGE};
      itemVersion = 0;
      vanillaItem = Items.SMITHING_TABLE;
      block = new StarlightForgeBlock(FabricBlockSettings.create().strength(2.5f,1200.0f).sounds(BlockSoundGroup.WOOD));
      item = new StarlightForgeItem(this.block,new FabricItemSettings().maxCount(1).fireproof());
      
      ItemStack stack = new ItemStack(item);
      NbtCompound tag = stack.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList enchants = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name","[{\"text\":\"Starlight Forge\",\"italic\":false,\"color\":\"#ff99ff\",\"bold\":true}]");
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
      loreList.add(NbtString.of("[{\"text\":\"With the \",\"italic\":false,\"color\":\"dark_purple\"},{\"text\":\"stars \",\"color\":\"white\"},{\"text\":\"as your witness...\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Your \",\"italic\":false,\"color\":\"dark_purple\"},{\"text\":\"journey \",\"color\":\"white\"},{\"text\":\"of \"},{\"text\":\"forging \",\"color\":\"light_purple\"},{\"text\":\"new \"},{\"text\":\"Arcana \",\"color\":\"light_purple\"},{\"text\":\"begins!\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"The \",\"italic\":false,\"color\":\"dark_purple\"},{\"text\":\"Forge\",\"color\":\"light_purple\"},{\"text\":\" lets you craft \"},{\"text\":\"Magic Items\",\"color\":\"light_purple\"},{\"text\":\" and \"},{\"text\":\"enhanced equipment\",\"color\":\"dark_aqua\"},{\"text\":\".\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"The \",\"italic\":false,\"color\":\"dark_purple\"},{\"text\":\"Forge\",\"color\":\"light_purple\"},{\"text\":\" acts as a \"},{\"text\":\"hub \",\"color\":\"dark_aqua\"},{\"text\":\"for other \"},{\"text\":\"Forge Structures\",\"color\":\"light_purple\"},{\"text\":\".\",\"color\":\"dark_purple\"}]"));
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
   
   public static StarlightForgeBlockEntity findActiveForge(ServerWorld world, BlockPos searchingPos){
      BlockPos range = new BlockPos(15, 8, 15);
      for(BlockPos blockPos : BlockPos.iterate(searchingPos.add(range), searchingPos.subtract(range))){
         BlockEntity be = world.getBlockEntity(blockPos);
         if(be instanceof StarlightForgeBlockEntity forge && forge.isAssembled()){
            BlockPos offset = blockPos.subtract(searchingPos);
            BlockPos forgeRange = forge.getForgeRange();
            if(Math.abs(offset.getX()) <= forgeRange.getX() && Math.abs(offset.getY()) <= forgeRange.getY() && Math.abs(offset.getZ()) <= forgeRange.getZ()) return forge;
         }
      }
      return null;
   }
   
   private MagicItemRecipe makeRecipe(){
      ItemStack table = new ItemStack(Items.SMITHING_TABLE);
      NbtCompound tag = table.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList loreList = new NbtList();
      display.putString("Name","[{\"text\":\"Smithing Table\",\"italic\":false,\"color\":\"gray\",\"bold\":true}]");
      loreList.add(NbtString.of("[{\"text\":\"Place a Smithing Table in the World\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
      
      ItemStack moon = new ItemStack(Items.SEA_LANTERN);
      tag = moon.getOrCreateNbt();
      display = new NbtCompound();
      loreList = new NbtList();
      display.putString("Name","[{\"text\":\"Night of a New Moon\",\"italic\":false,\"color\":\"gray\",\"bold\":true}]");
      loreList.add(NbtString.of("[{\"text\":\"Follow this Recipe under the darkness of a New Moon\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
      
      ItemStack gapple = new ItemStack(Items.ENCHANTED_GOLDEN_APPLE);
      tag = gapple.getOrCreateNbt();
      display = new NbtCompound();
      loreList = new NbtList();
      display.putString("Name","[{\"text\":\"Enchanted Golden Apple\",\"italic\":false,\"color\":\"gold\",\"bold\":true}]");
      loreList.add(NbtString.of("[{\"text\":\"Place the Gapple upon the Smithing Table.\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
      
      ItemStack tome = ArcanaRegistry.ARCANE_TOME.getPrefItem().copy();
      tag = tome.getOrCreateNbt();
      display = new NbtCompound();
      loreList = new NbtList();
      display.putString("Name","[{\"text\":\"Tome of Arcana Novum\",\"italic\":false,\"color\":\"dark_aqua\",\"bold\":true}]");
      loreList.add(NbtString.of("[{\"text\":\"Place the Tome upon the Smithing Table.\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
      
      ItemStack pane = new ItemStack(Items.BLACK_STAINED_GLASS_PANE);
      tag = pane.getOrCreateNbt();
      display = new NbtCompound();
      loreList = new NbtList();
      display.putString("Name","[{\"text\":\"In World Recipe\",\"italic\":false,\"color\":\"blue\"}]");
      loreList.add(NbtString.of("[{\"text\":\"Do this in the World\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
      
      ExplainIngredient a = new ExplainIngredient(pane,"",false);
      ExplainIngredient s = new ExplainIngredient(table,"Smithing Table");
      ExplainIngredient m = new ExplainIngredient(moon,"",false);
      ExplainIngredient g = new ExplainIngredient(gapple,"Enchanted Golden Apple");
      ExplainIngredient t = new ExplainIngredient(tome,"Tome of Arcana Novum");
      
      ExplainIngredient[][] ingredients = {
            {m,a,a,a,a},
            {a,a,t,a,a},
            {a,a,g,a,a},
            {a,a,s,a,a},
            {a,a,a,a,a}};
      return new ExplainRecipe(ingredients);
   }
   
   private List<String> makeLore(){
      ArrayList<String> list = new ArrayList<>();
      list.add("{\"text\":\"    Starlight Forge\\n\\nRarity: Empowered\\n\\nSomething about an Enchanted Golden Apple is so ancient and powerful, that its power cannot be replicated. However, my new theories show that the Arcana of this land is far more versatile than old \"}");
      list.add("{\"text\":\"    Starlight Forge\\n\\nscholars believed. This could be the path to finally unlocking a novel field of Arcane research. Using the Arcane energy of Starlight, I can embue the ancient enchantment from this apple into a smithery. This shall become my new forge...\"}");
      list.add("{\"text\":\"    Starlight Forge\\n\\nThe Starlight Forge allows the creation of new Magic Items, and creating more powerful armor and weapons.\\nIt creates a 17x11x17 workspace that can interact with additions to the forge that can be made as you advance.\"}");
      return list;
   }
   
   public class StarlightForgeItem extends MagicPolymerBlockItem {
      public StarlightForgeItem(Block block, Settings settings){
         super(getThis(),block, settings);
      }
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
   }
   
   public class StarlightForgeBlock extends MagicPolymerBlockEntity {
      public StarlightForgeBlock(Settings settings){
         super(settings);
      }
      
      @Override
      public Block getPolymerBlock(BlockState state) {
         return Blocks.SMITHING_TABLE;
      }
      
      @Nullable
      public static StarlightForgeBlockEntity getEntity(World world, BlockPos pos) {
         BlockState state = world.getBlockState(pos);
         if (!(state.getBlock() instanceof StarlightForgeBlock)) {
            return null;
         }
         return world.getBlockEntity(pos) instanceof StarlightForgeBlockEntity forge ? forge : null;
      }
      
      @Override
      public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
         return new StarlightForgeBlockEntity(pos, state);
      }
      
      @Nullable
      @Override
      public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
         return validateTicker(type, ArcanaRegistry.STARLIGHT_FORGE_BLOCK_ENTITY, StarlightForgeBlockEntity::ticker);
      }
      
      @Override
      public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity playerEntity, Hand hand, BlockHitResult hit){
         if(hand != Hand.MAIN_HAND) return ActionResult.PASS;
         StarlightForgeBlockEntity forge = (StarlightForgeBlockEntity) world.getBlockEntity(pos);
         if(forge != null){
            if(playerEntity instanceof ServerPlayerEntity player){
               if(forge.isAssembled()){
                  forge.openGui(0, player,"",null);
               }else{
                  player.sendMessage(Text.literal("Multiblock not constructed."));
                  multiblock.displayStructure(forge.getMultiblockCheck());
               }
            }
         }
         return ActionResult.SUCCESS;
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
         if (placer instanceof ServerPlayerEntity player && entity instanceof StarlightForgeBlockEntity forge) {
            initializeMagicBlock(stack,forge);
         }
      }
   }
}
