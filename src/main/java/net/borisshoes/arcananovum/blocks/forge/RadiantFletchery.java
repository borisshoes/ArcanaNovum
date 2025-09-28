package net.borisshoes.arcananovum.blocks.forge;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.core.ArcanaBlock;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.core.Multiblock;
import net.borisshoes.arcananovum.core.MultiblockCore;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerBlockEntity;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerBlockItem;
import net.borisshoes.arcananovum.gui.arcanetome.TomeGui;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaIngredient;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaRecipe;
import net.borisshoes.arcananovum.recipes.arcana.ForgeRequirement;
import net.borisshoes.arcananovum.recipes.arcana.GenericArcanaIngredient;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class RadiantFletchery extends ArcanaBlock implements MultiblockCore {
	public static final String ID = "radiant_fletchery";
   
   private Multiblock multiblock;
   
   public RadiantFletchery(){
      id = ID;
      name = "Radiant Fletchery";
      rarity = ArcanaRarity.EMPOWERED;
      categories = new TomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), TomeGui.TomeFilter.BLOCKS, TomeGui.TomeFilter.FORGE};
      itemVersion = 0;
      vanillaItem = Items.FLETCHING_TABLE;
      block = new RadiantFletcheryBlock(AbstractBlock.Settings.create().strength(2.5f,1200.0f).sounds(BlockSoundGroup.WOOD));
      item = new RadiantFletcheryItem(this.block);
      displayName = Text.translatableWithFallback("item."+MOD_ID+"."+ID,name).formatted(Formatting.BOLD,Formatting.YELLOW);
      researchTasks = new RegistryKey[]{ResearchTasks.UNLOCK_RUNIC_MATRIX,ResearchTasks.ADVANCEMENT_SHOOT_ARROW,ResearchTasks.ADVANCEMENT_OL_BETSY,ResearchTasks.OBTAIN_TIPPED_ARROW,ResearchTasks.OBTAIN_SPECTRAL_ARROW,ResearchTasks.ADVANCEMENT_BREW_POTION,ResearchTasks.UNLOCK_STARLIGHT_FORGE};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getMaxCount());
      setPrefStack(stack);
   }
   
   @Override
   public List<Text> getItemLore(@Nullable ItemStack itemStack){
      List<MutableText> lore = new ArrayList<>();
      lore.add(Text.literal("")
            .append(Text.literal("A ").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal("Forge Structure").formatted(Formatting.YELLOW))
            .append(Text.literal(" addon to the ").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal("Starlight Forge").formatted(Formatting.YELLOW))
            .append(Text.literal(".").formatted(Formatting.LIGHT_PURPLE)));
      lore.add(Text.literal("")
            .append(Text.literal("The ").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal("Fletchery ").formatted(Formatting.YELLOW))
            .append(Text.literal("enables ").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal("efficient ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("creation of ").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal("tipped arrows").formatted(Formatting.YELLOW))
            .append(Text.literal(".").formatted(Formatting.LIGHT_PURPLE)));
      lore.add(Text.literal("")
            .append(Text.literal("The ").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal("Fletchery ").formatted(Formatting.YELLOW))
            .append(Text.literal("also ").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal("unlocks ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("the ability to make ").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal("Runic Arrows").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal(".").formatted(Formatting.LIGHT_PURPLE)));
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
      return new Vec3i(-1,-1,-1);
   }
   
   @Override
	protected ArcanaRecipe makeRecipe(){
      ArcanaIngredient a = new ArcanaIngredient(Items.BLAZE_POWDER,24);
      ArcanaIngredient b = new ArcanaIngredient(Items.GLOWSTONE_DUST,12);
      ArcanaIngredient c = new ArcanaIngredient(Items.END_CRYSTAL,8);
      ArcanaIngredient g = new ArcanaIngredient(Items.SPECTRAL_ARROW,16);
      ArcanaIngredient h = new ArcanaIngredient(Items.FLETCHING_TABLE,8);
      GenericArcanaIngredient m = new GenericArcanaIngredient(ArcanaRegistry.RUNIC_MATRIX,1);
      
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
      list.add(List.of(Text.literal(" Radiant Fletchery").formatted(Formatting.GOLD,Formatting.BOLD),Text.literal("\nRarity: ").formatted(Formatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(),false)),Text.literal("\nI have yet to put my Runic Matrix to good use. Fortunately, this might be my chance. The Matrix should be able to take on the effect of potions to boost the amount of Tipped Arrows I can make from a single ").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal(" Radiant Fletchery").formatted(Formatting.GOLD,Formatting.BOLD),Text.literal("\npotion. The Arrows themselves could make an excellent candidate for use of the Matrix once I master more of its capabilities. Perhaps if I make an arrow out of a Matrix it could activate powerful effects upon hitting a target.").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal(" Radiant Fletchery").formatted(Formatting.GOLD,Formatting.BOLD),Text.literal("\nThe Fletchery boosts the amount of Tipped Arrows made per potion, as well as allowing non-lingering potions to be used.\n\nThe Fletchery also unlocks a host of Archery related recipes for the Starlight Forge.\n").formatted(Formatting.BLACK)));
      return list;
   }
   
   public class RadiantFletcheryItem extends ArcanaPolymerBlockItem {
      public RadiantFletcheryItem(Block block){
         super(getThis(),block, getArcanaItemComponents());
      }
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
   }
   
   public class RadiantFletcheryBlock extends ArcanaPolymerBlockEntity {
      public RadiantFletcheryBlock(AbstractBlock.Settings settings){
         super(getThis(), settings);
      }
      
      @Override
      public BlockState getPolymerBlockState(BlockState state, PacketContext context){
         return Blocks.FLETCHING_TABLE.getDefaultState();
      }
      
      @Nullable
      public static RadiantFletcheryBlockEntity getEntity(World world, BlockPos pos){
         BlockState state = world.getBlockState(pos);
         if(!(state.getBlock() instanceof RadiantFletcheryBlock)){
            return null;
         }
         return world.getBlockEntity(pos) instanceof RadiantFletcheryBlockEntity fletchery ? fletchery : null;
      }
      
      @Override
      public BlockEntity createBlockEntity(BlockPos pos, BlockState state){
         return new RadiantFletcheryBlockEntity(pos, state);
      }
      
      @Nullable
      @Override
      public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type){
         return validateTicker(type, ArcanaRegistry.RADIANT_FLETCHERY_BLOCK_ENTITY, RadiantFletcheryBlockEntity::ticker);
      }
      
      @Override
      public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity playerEntity, BlockHitResult hit){
         RadiantFletcheryBlockEntity fletchery = (RadiantFletcheryBlockEntity) world.getBlockEntity(pos);
         if(fletchery != null){
            if(playerEntity instanceof ServerPlayerEntity player){
               if(fletchery.isAssembled()){
                  if(StarlightForge.findActiveForge(player.getWorld(),pos) == null){
                     player.sendMessage(Text.literal("The Fletchery must be within the range of an active Starlight Forge"));
                  }else{
                     fletchery.openGui(player);
                     player.getItemCooldownManager().set(playerEntity.getMainHandStack(),1);
                     player.getItemCooldownManager().set(playerEntity.getOffHandStack(),1);
                  }
               }else{
                  player.sendMessage(Text.literal("Multiblock not constructed."));
                  multiblock.displayStructure(fletchery.getMultiblockCheck(),player);
               }
            }
         }
         return ActionResult.SUCCESS_SERVER;
      }
      
      @Override
      public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack){
         BlockEntity entity = world.getBlockEntity(pos);
         if(entity instanceof RadiantFletcheryBlockEntity fletchery){
            initializeArcanaBlock(stack,fletchery);
         }
      }
   }
}

