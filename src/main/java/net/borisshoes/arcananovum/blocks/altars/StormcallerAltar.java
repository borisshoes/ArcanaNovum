package net.borisshoes.arcananovum.blocks.altars;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.core.ArcanaBlock;
import net.borisshoes.arcananovum.core.ArcanaBlockEntity;
import net.borisshoes.arcananovum.core.Multiblock;
import net.borisshoes.arcananovum.core.MultiblockCore;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerBlockEntity;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerBlockItem;
import net.borisshoes.arcananovum.gui.arcanetome.TomeGui;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaIngredient;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaRecipe;
import net.borisshoes.arcananovum.recipes.arcana.ForgeRequirement;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaRarity;
import net.borisshoes.arcananovum.utils.TextUtils;
import net.minecraft.block.*;
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

public class StormcallerAltar extends ArcanaBlock implements MultiblockCore {
	public static final String ID = "stormcaller_altar";
   
   private Multiblock multiblock;
   
   public StormcallerAltar(){
      id = ID;
      name = "Altar of the Stormcaller";
      rarity = ArcanaRarity.EXOTIC;
      categories = new TomeGui.TomeFilter[]{TomeGui.TomeFilter.EXOTIC, TomeGui.TomeFilter.BLOCKS, TomeGui.TomeFilter.ALTARS};
      itemVersion = 0;
      vanillaItem = Items.RAW_COPPER_BLOCK;
      block = new StormcallerAltarBlock(AbstractBlock.Settings.create().mapColor(MapColor.ORANGE).strength(5.0f,1200.0f).requiresTool().sounds(BlockSoundGroup.METAL));
      item = new StormcallerAltarItem(this.block,new Item.Settings().maxCount(1).fireproof()
            .component(DataComponentTypes.ITEM_NAME, Text.literal("Altar of the Stormcaller").formatted(Formatting.BOLD,Formatting.AQUA))
            .component(DataComponentTypes.LORE, new LoreComponent(getItemLore(null)))
            .component(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true)
      );
      researchTasks = new RegistryKey[]{ResearchTasks.ADVANCEMENT_LIGHTNING_ROD_WITH_VILLAGER_NO_FIRE,ResearchTasks.OBTAIN_HEART_OF_THE_SEA,ResearchTasks.OBTAIN_LIGHTNING_ROD,ResearchTasks.ADVANCEMENT_WAX_ON,ResearchTasks.ADVANCEMENT_WAX_OFF,ResearchTasks.ADVANCEMENT_OBTAIN_CRYING_OBSIDIAN};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getMaxCount());
      setPrefStack(stack);
   }
   
   @Override
   public List<Text> getItemLore(@Nullable ItemStack itemStack){
      List<MutableText> lore = new ArrayList<>();
      addAltarLore(lore);
      lore.add(Text.literal("Stormcaller Altar:").formatted(Formatting.BOLD,Formatting.AQUA));
      lore.add(Text.literal("")
            .append(Text.literal("The ").formatted(Formatting.GRAY))
            .append(Text.literal("Altar ").formatted(Formatting.AQUA))
            .append(Text.literal("calls upon the ").formatted(Formatting.GRAY))
            .append(Text.literal("clouds ").formatted(Formatting.DARK_GRAY))
            .append(Text.literal("to ").formatted(Formatting.GRAY))
            .append(Text.literal("shift ").formatted(Formatting.AQUA))
            .append(Text.literal("and ").formatted(Formatting.GRAY))
            .append(Text.literal("darken").formatted(Formatting.DARK_GRAY))
            .append(Text.literal(".").formatted(Formatting.GRAY)));
      lore.add(Text.literal("")
            .append(Text.literal("The ").formatted(Formatting.GRAY))
            .append(Text.literal("Altar ").formatted(Formatting.AQUA))
            .append(Text.literal("can be used to ").formatted(Formatting.GRAY))
            .append(Text.literal("change ").formatted(Formatting.DARK_GRAY))
            .append(Text.literal("the ").formatted(Formatting.GRAY))
            .append(Text.literal("weather ").formatted(Formatting.AQUA))
            .append(Text.literal("to any state.").formatted(Formatting.GRAY)));
      lore.add(Text.literal("")
            .append(Text.literal("The ").formatted(Formatting.GRAY))
            .append(Text.literal("Altar ").formatted(Formatting.AQUA))
            .append(Text.literal("requires a ").formatted(Formatting.GRAY))
            .append(Text.literal("Diamond Block").formatted(Formatting.AQUA))
            .append(Text.literal(" to ").formatted(Formatting.GRAY))
            .append(Text.literal("activate").formatted(Formatting.DARK_GRAY))
            .append(Text.literal(".").formatted(Formatting.GRAY)));
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
	protected ArcanaRecipe makeRecipe(){
      ArcanaIngredient a = new ArcanaIngredient(Items.LIGHTNING_ROD,16);
      ArcanaIngredient b = new ArcanaIngredient(Items.OXIDIZED_COPPER,12);
      ArcanaIngredient c = new ArcanaIngredient(Items.DIAMOND,6);
      ArcanaIngredient g = new ArcanaIngredient(Items.HEART_OF_THE_SEA,1);
      ArcanaIngredient h = new ArcanaIngredient(Items.COPPER_BULB,8);
      ArcanaIngredient m = new ArcanaIngredient(Items.RAW_COPPER_BLOCK,16);
      
      ArcanaIngredient[][] ingredients = {
            {a,b,c,b,a},
            {b,g,h,g,b},
            {c,h,m,h,c},
            {b,g,h,g,b},
            {a,b,c,b,a}};
      return new ArcanaRecipe(ingredients,new ForgeRequirement());
   }
   
   @Override
   public List<List<Text>> getBookLore(){
      List<List<Text>> list = new ArrayList<>();
      list.add(List.of(Text.literal("      Altar of the\n      Stormcaller\n\nRarity: Exotic\n\nIn order to influence the world, I need to tap into the leylines. I have devised a structure capable of such, with this at its heart. It should be able to cause changes in the ").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal("      Altar of the\n      Stormcaller\n\natmosphere so that I can dictate the weather. \nThunder, Rain or Shine, at my command!\nHowever, in order to provide enough energy to the leylines, I need to use a Diamond Block as a catalyst.").formatted(Formatting.BLACK)));
      return list;
   }
   
   public class StormcallerAltarItem extends ArcanaPolymerBlockItem {
      public StormcallerAltarItem(Block block, Settings settings){
         super(getThis(),block, settings);
      }
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
   }
   
   public class StormcallerAltarBlock extends ArcanaPolymerBlockEntity {
      public StormcallerAltarBlock(AbstractBlock.Settings settings){
         super(settings);
      }
      
      @Override
      public BlockState getPolymerBlockState(BlockState state) {
         return Blocks.RAW_COPPER_BLOCK.getDefaultState();
      }
      
      @Nullable
      public static StormcallerAltarBlockEntity getEntity(World world, BlockPos pos) {
         BlockState state = world.getBlockState(pos);
         if (!(state.getBlock() instanceof StormcallerAltarBlock)) {
            return null;
         }
         return world.getBlockEntity(pos) instanceof StormcallerAltarBlockEntity altar ? altar : null;
      }
      
      @Override
      public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
         return new StormcallerAltarBlockEntity(pos, state);
      }
      
      @Nullable
      @Override
      public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
         return validateTicker(type, ArcanaRegistry.STORMCALLER_ALTAR_BLOCK_ENTITY, StormcallerAltarBlockEntity::ticker);
      }
      
      @Override
      protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity playerEntity, BlockHitResult hit){
         StormcallerAltarBlockEntity altar = (StormcallerAltarBlockEntity) world.getBlockEntity(pos);
         if(altar != null){
            if(playerEntity instanceof ServerPlayerEntity player){
               if(altar.isAssembled()){
                  altar.openGui(player);
                  player.getItemCooldownManager().set(playerEntity.getMainHandStack().getItem(),1);
               }else{
                  player.sendMessage(Text.literal("Multiblock not constructed."));
                  multiblock.displayStructure(altar.getMultiblockCheck());
               }
            }
         }
         return ActionResult.SUCCESS;
      }
      
      @Override
      public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
         BlockEntity entity = world.getBlockEntity(pos);
         if (placer instanceof ServerPlayerEntity player && entity instanceof StormcallerAltarBlockEntity altar) {
            initializeArcanaBlock(stack,altar);
         }
      }
   }
}

