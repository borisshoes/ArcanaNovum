package net.borisshoes.arcananovum.items.catalysts;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerItem;
import net.borisshoes.arcananovum.entities.NulConstructEntity;
import net.borisshoes.arcananovum.gui.arcanetome.TomeGui;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaIngredient;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaRecipe;
import net.borisshoes.arcananovum.recipes.arcana.ForgeRequirement;
import net.borisshoes.arcananovum.recipes.arcana.GenericArcanaIngredient;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaRarity;
import net.borisshoes.arcananovum.utils.TextUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CarvedPumpkinBlock;
import net.minecraft.block.pattern.BlockPattern;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;
import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;
import static net.borisshoes.arcananovum.entities.NulConstructEntity.getConstructPattern;

public class SovereignCatalyst extends ArcanaItem {
	public static final String ID = "sovereign_catalyst";
   
   private static final String TXT = "item/sovereign_catalyst";
   
   public SovereignCatalyst(){
      id = ID;
      name = "Sovereign Augment Catalyst";
      categories = new TomeGui.TomeFilter[]{TomeGui.TomeFilter.MUNDANE, TomeGui.TomeFilter.CATALYSTS};
      rarity = ArcanaRarity.MUNDANE;
      vanillaItem = Items.GOLD_INGOT;
      item = new SovereignCatalystItem(new Item.Settings().maxCount(1).fireproof()
            .component(DataComponentTypes.ITEM_NAME, Text.translatable("item."+MOD_ID+"."+ID).formatted(Formatting.BOLD,Formatting.GOLD))
            .component(DataComponentTypes.LORE, new LoreComponent(getItemLore(null)))
            .component(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true)
      );
      models = new ArrayList<>();
      models.add(new Pair<>(vanillaItem,TXT));
      researchTasks = new RegistryKey[]{ResearchTasks.UNLOCK_EXOTIC_CATALYST,ResearchTasks.OBTAIN_GOLD_INGOT,ResearchTasks.UNLOCK_TWILIGHT_ANVIL};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getMaxCount());
      setPrefStack(stack);
   }
   
   @Override
   public List<Text> getItemLore(@Nullable ItemStack itemStack){
      List<MutableText> lore = new ArrayList<>();
      lore.add(Text.literal("")
            .append(Text.literal("Augment ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("Catalysts").formatted(Formatting.BLUE))
            .append(Text.literal(" can be used to ").formatted(Formatting.GRAY))
            .append(Text.literal("augment ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("your ").formatted(Formatting.GRAY))
            .append(Text.literal("Arcana Items").formatted(Formatting.DARK_PURPLE)));
      lore.add(Text.literal("")
            .append(Text.literal("Augments ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("require more ").formatted(Formatting.GRAY))
            .append(Text.literal("powerful ").formatted(Formatting.GREEN))
            .append(Text.literal("Catalysts ").formatted(Formatting.BLUE))
            .append(Text.literal("at higher levels").formatted(Formatting.GRAY)));
      lore.add(Text.literal("")
            .append(Text.literal("Apply ").formatted(Formatting.GREEN))
            .append(Text.literal("these ").formatted(Formatting.GRAY))
            .append(Text.literal("Catalysts ").formatted(Formatting.BLUE))
            .append(Text.literal("in the ").formatted(Formatting.GRAY))
            .append(Text.literal("Tinkering Menu").formatted(Formatting.BLUE))
            .append(Text.literal(" of a ").formatted(Formatting.GRAY))
            .append(Text.literal("Twilight Anvil").formatted(Formatting.GREEN)));
     return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
	protected ArcanaRecipe makeRecipe(){
      ArcanaIngredient a = ArcanaIngredient.EMPTY;
      ArcanaIngredient c = new ArcanaIngredient(Items.OBSIDIAN,24);
      ArcanaIngredient g = new ArcanaIngredient(Items.CRYING_OBSIDIAN,24);
      ArcanaIngredient h = new ArcanaIngredient(Items.GOLD_INGOT,12);
      GenericArcanaIngredient m = new GenericArcanaIngredient(ArcanaRegistry.EXOTIC_CATALYST,1);
      
      ArcanaIngredient[][] ingredients = {
            {a,a,c,a,a},
            {a,g,h,g,a},
            {c,h,m,h,c},
            {a,g,h,g,a},
            {a,a,c,a,a}};
      return new ArcanaRecipe(ingredients,new ForgeRequirement().withAnvil());
   }
   
   @Override
   public List<List<Text>> getBookLore(){
      List<List<Text>> list = new ArrayList<>();
      list.add(List.of(Text.literal("  Sovereign Augment\n         Catalyst\n\nRarity: Mundane\n\nGOLD! The gemstones already provide enough reinforcement. Gold lets the energy be more malleable to more creative applications. But, I think there's a little potential left here...").formatted(Formatting.BLACK)));
      return list;
   }
   
   public class SovereignCatalystItem extends ArcanaPolymerItem {
      public SovereignCatalystItem(Item.Settings settings){
         super(getThis(),settings);
      }
      
      @Override
      public int getPolymerCustomModelData(ItemStack itemStack, @Nullable ServerPlayerEntity player){
         return ArcanaRegistry.getModelData(TXT).value();
      }
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
      
      @Override
      public ActionResult useOnBlock(ItemUsageContext context){
         World world = context.getWorld();
         PlayerEntity playerEntity = context.getPlayer();
         BlockPos pos = context.getBlockPos();
         BlockState state = world.getBlockState(pos);
         boolean canSpawn = true;
         if(playerEntity instanceof ServerPlayerEntity serverPlayer){
            canSpawn = PLAYER_DATA.get(serverPlayer).hasResearched(ArcanaRegistry.DIVINE_CATALYST);
         }
         
         if(state.isOf(Blocks.NETHERITE_BLOCK) && pos.getY() >= world.getBottomY() && canSpawn){ // Check construct
            BlockPattern.Result patternResult = getConstructPattern().searchAround(world, pos);
            if (patternResult != null) {
               NulConstructEntity constructEntity = (NulConstructEntity) ArcanaRegistry.NUL_CONSTRUCT_ENTITY.create(world, SpawnReason.TRIGGERED);
               if (constructEntity != null && world instanceof ServerWorld serverWorld) {
                  CarvedPumpkinBlock.breakPatternBlocks(world, patternResult);
                  BlockPos blockPos = patternResult.translate(1, 1, 0).getBlockPos();
                  constructEntity.refreshPositionAndAngles((double)blockPos.getX() + 0.5, (double)blockPos.getY() + 0.55, (double)blockPos.getZ() + 0.5, patternResult.getForwards().getAxis() == Direction.Axis.X ? 0.0F : 90.0F, 0.0F);
                  constructEntity.bodyYaw = patternResult.getForwards().getAxis() == Direction.Axis.X ? 0.0F : 90.0F;
                  constructEntity.onSummoned(playerEntity);
                  
                  world.spawnEntity(constructEntity);
                  CarvedPumpkinBlock.updatePatternBlocks(world, patternResult);
                  
                  if(playerEntity instanceof ServerPlayerEntity player){
                     ArcanaAchievements.grant(player,ArcanaAchievements.DOOR_OF_DIVINITY.id);
                  }
                  
                  context.getStack().decrement(1);
               }
               return ActionResult.SUCCESS;
            }
         }
         return ActionResult.PASS;
      }
   }
}

