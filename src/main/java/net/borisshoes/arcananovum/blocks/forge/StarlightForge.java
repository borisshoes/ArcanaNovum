package net.borisshoes.arcananovum.blocks.forge;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.core.ArcanaBlock;
import net.borisshoes.arcananovum.core.ArcanaBlockEntity;
import net.borisshoes.arcananovum.core.Multiblock;
import net.borisshoes.arcananovum.core.MultiblockCore;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerBlockEntity;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerBlockItem;
import net.borisshoes.arcananovum.gui.arcanetome.TomeGui;
import net.borisshoes.arcananovum.items.normal.GraphicItems;
import net.borisshoes.arcananovum.items.normal.GraphicalItem;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaRecipe;
import net.borisshoes.arcananovum.recipes.arcana.ExplainIngredient;
import net.borisshoes.arcananovum.recipes.arcana.ExplainRecipe;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaColors;
import net.borisshoes.arcananovum.utils.ArcanaRarity;
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
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class StarlightForge extends ArcanaBlock implements MultiblockCore {
   public static final String SEED_USES_TAG = "seedUses";
   
	public static final String ID = "starlight_forge";
   
   private Multiblock multiblock;
   
   public StarlightForge(){
      id = ID;
      name = "Starlight Forge";
      rarity = ArcanaRarity.EMPOWERED;
      categories = new TomeGui.TomeFilter[]{TomeGui.TomeFilter.EMPOWERED, TomeGui.TomeFilter.BLOCKS, TomeGui.TomeFilter.FORGE};
      itemVersion = 0;
      vanillaItem = Items.SMITHING_TABLE;
      block = new StarlightForgeBlock(AbstractBlock.Settings.create().strength(2.5f,1200.0f).sounds(BlockSoundGroup.WOOD));
      item = new StarlightForgeItem(this.block,new Item.Settings().maxCount(1).fireproof()
            .component(DataComponentTypes.ITEM_NAME, TextUtils.withColor(Text.literal("Starlight Forge").formatted(Formatting.BOLD),0xFF99FF))
            .component(DataComponentTypes.LORE, new LoreComponent(getItemLore(null)))
            .component(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true)
      );
      researchTasks = new RegistryKey[]{ResearchTasks.OBTAIN_ARCANE_TOME,ResearchTasks.OBTAIN_ENCHANTED_GOLDEN_APPLE};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getMaxCount());
      putProperty(stack,SEED_USES_TAG,0);
      setPrefStack(stack);
   }
   
   @Override
   public List<Text> getItemLore(@Nullable ItemStack itemStack){
      List<MutableText> lore = new ArrayList<>();
      lore.add(Text.literal("")
            .append(Text.literal("With the ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("stars ").formatted(Formatting.WHITE))
            .append(Text.literal("as your witness...").formatted(Formatting.DARK_PURPLE)));
      lore.add(Text.literal("")
            .append(Text.literal("Your ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("journey ").formatted(Formatting.WHITE))
            .append(Text.literal("of ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("forging ").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal("new ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("Arcana ").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal("begins!").formatted(Formatting.DARK_PURPLE)));
      lore.add(Text.literal(""));
      lore.add(Text.literal("")
            .append(Text.literal("The ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("Forge").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal(" lets you craft ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("Arcana Items").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal(" and ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("enhanced equipment").formatted(Formatting.DARK_AQUA))
            .append(Text.literal(".").formatted(Formatting.DARK_PURPLE)));
      lore.add(Text.literal("")
            .append(Text.literal("The ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("Forge").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal(" acts as a ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("hub ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("for other ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("Forge Structures").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal(".").formatted(Formatting.DARK_PURPLE)));
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
   
   @Override
	protected ArcanaRecipe makeRecipe(){
      ExplainIngredient a = new ExplainIngredient(GraphicalItem.withColor(GraphicItems.PAGE_BG, ArcanaColors.DARK_COLOR),1,"",false)
            .withName(Text.literal("In World Recipe").formatted(Formatting.BLUE))
            .withLore(List.of(Text.literal("Do this in the World").formatted(Formatting.DARK_PURPLE)));
      ExplainIngredient s = new ExplainIngredient(Items.SMITHING_TABLE,1,"Smithing Table")
            .withName(Text.literal("Smithing Table").formatted(Formatting.BOLD,Formatting.GRAY))
            .withLore(List.of(Text.literal("Place a Smithing Table in the World").formatted(Formatting.DARK_PURPLE)));
      ExplainIngredient m = new ExplainIngredient(Items.SEA_LANTERN,1,"",false)
            .withName(Text.literal("Night of a New Moon").formatted(Formatting.BOLD,Formatting.GRAY))
            .withLore(List.of(Text.literal("Follow this Recipe under the darkness of a New Moon").formatted(Formatting.DARK_PURPLE)));
      ExplainIngredient g = new ExplainIngredient(Items.ENCHANTED_GOLDEN_APPLE,1,"Enchanted Golden Apple")
            .withName(Text.literal("Enchanted Golden Apple").formatted(Formatting.BOLD,Formatting.GOLD))
            .withLore(List.of(Text.literal("Place the apple upon the Smithing Table.").formatted(Formatting.DARK_PURPLE)));
      ExplainIngredient t = new ExplainIngredient(ArcanaRegistry.ARCANE_TOME.getItem(),1,"Tome of Arcana Novum")
            .withName(Text.literal("Tome of Arcana Novum").formatted(Formatting.BOLD,Formatting.DARK_AQUA))
            .withLore(List.of(Text.literal("Place the Tome upon the Smithing Table.").formatted(Formatting.DARK_PURPLE)));
      
      ExplainIngredient[][] ingredients = {
            {m,a,a,a,a},
            {a,a,t,a,a},
            {a,a,g,a,a},
            {a,a,s,a,a},
            {a,a,a,a,a}};
      return new ExplainRecipe(ingredients);
   }
   
   @Override
   public List<List<Text>> getBookLore(){
      List<List<Text>> list = new ArrayList<>();
      list.add(List.of(Text.literal("    Starlight Forge\n\nRarity: Empowered\n\nSomething about an Enchanted Golden Apple is so ancient and powerful, that its power cannot be replicated. However, my new theories show that the Arcana of this land is far more versatile than old ").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal("    Starlight Forge\n\nscholars believed. This could be the path to finally unlocking a novel field of Arcane research. Using the Arcane energy of Starlight, I can embue the ancient enchantment from this apple into a smithery. This shall become my new forge...").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal("    Starlight Forge\n\nThe Starlight Forge allows the creation of new Arcana Items, and creating more powerful armor and weapons.\nIt creates a 17x11x17 workspace that can interact with additions to the forge that can be made as you advance.").formatted(Formatting.BLACK)));
      return list;
   }
   
   public class StarlightForgeItem extends ArcanaPolymerBlockItem {
      public StarlightForgeItem(Block block, Settings settings){
         super(getThis(),block, settings);
      }
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
   }
   
   public class StarlightForgeBlock extends ArcanaPolymerBlockEntity {
      public StarlightForgeBlock(AbstractBlock.Settings settings){
         super(settings);
      }
      
      @Override
      public BlockState getPolymerBlockState(BlockState state) {
         return Blocks.SMITHING_TABLE.getDefaultState();
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
      public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity playerEntity, BlockHitResult hit){
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
      public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
         BlockEntity entity = world.getBlockEntity(pos);
         if (placer instanceof ServerPlayerEntity player && entity instanceof StarlightForgeBlockEntity forge) {
            initializeArcanaBlock(stack,forge);
            forge.setSeedUses(getIntProperty(stack,SEED_USES_TAG));
         }
      }
   }
}

