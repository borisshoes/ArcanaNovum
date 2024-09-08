package net.borisshoes.arcananovum.items.catalysts;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerItem;
import net.borisshoes.arcananovum.entities.NulConstructEntity;
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
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CarvedPumpkinBlock;
import net.minecraft.block.pattern.BlockPattern;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
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

public class DivineCatalyst extends ArcanaItem {
	public static final String ID = "divine_catalyst";
   
   private static final String TXT = "item/divine_catalyst";
   
   public DivineCatalyst(){
      id = ID;
      name = "Divine Augment Catalyst";
      categories = new TomeGui.TomeFilter[]{TomeGui.TomeFilter.DIVINE, TomeGui.TomeFilter.CATALYSTS};
      rarity = ArcanaRarity.DIVINE;
      itemVersion = 1;
      vanillaItem = Items.AMETHYST_CLUSTER;
      item = new DivineCatalystItem(new Item.Settings().maxCount(1).fireproof()
            .component(DataComponentTypes.ITEM_NAME, Text.translatable("item."+MOD_ID+"."+ID).formatted(Formatting.BOLD,Formatting.LIGHT_PURPLE))
            .component(DataComponentTypes.LORE, new LoreComponent(getItemLore(null)))
            .component(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true)
      );
      models = new ArrayList<>();
      models.add(new Pair<>(vanillaItem,TXT));
      researchTasks = new RegistryKey[]{ResearchTasks.UNLOCK_SOVEREIGN_CATALYST,ResearchTasks.UNLOCK_TWILIGHT_ANVIL};
      
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
      ExplainIngredient a = new ExplainIngredient(GraphicalItem.withColor(GraphicItems.PAGE_BG, ArcanaColors.DARK_COLOR),1,"",false)
            .withName(Text.literal("In World Recipe").formatted(Formatting.BLUE,Formatting.BOLD))
            .withLore(List.of(Text.literal("Build this in the World").formatted(Formatting.DARK_PURPLE)));
      ExplainIngredient s = new ExplainIngredient(Items.SOUL_SAND,1,"Soul Sand or Soil")
            .withName(Text.literal("Soul Sand or Soil").formatted(Formatting.GRAY,Formatting.BOLD))
            .withLore(List.of(Text.literal("Construct a Wither Base with a heart of Netherite").formatted(Formatting.DARK_PURPLE)));
      ExplainIngredient k = new ExplainIngredient(Items.WITHER_SKELETON_SKULL,1,"Wither Skeleton Skull")
            .withName(Text.literal("Wither Skeleton Skull").formatted(Formatting.DARK_GRAY,Formatting.BOLD))
            .withLore(List.of(Text.literal("Construct a Wither Base with a heart of Netherite").formatted(Formatting.DARK_PURPLE)));
      ExplainIngredient n = new ExplainIngredient(Items.NETHERITE_BLOCK,1,"Netherite Block")
            .withName(Text.literal("Block of Netherite").formatted(Formatting.DARK_RED,Formatting.BOLD))
            .withLore(List.of(Text.literal("Construct a Wither Base with a heart of Netherite").formatted(Formatting.DARK_PURPLE)));
      ExplainIngredient c = new ExplainIngredient(ArcanaRegistry.SOVEREIGN_CATALYST.getItem(),1,"Sovereign Augment Catalyst")
            .withName(Text.literal("Sovereign Augmentation Catalyst").formatted(Formatting.GOLD,Formatting.BOLD))
            .withLore(List.of(
                  Text.literal("")
                        .append(Text.literal("Right Click").formatted(Formatting.BLUE))
                        .append(Text.literal(" the ").formatted(Formatting.DARK_PURPLE))
                        .append(Text.literal("Catalyst").formatted(Formatting.GOLD))
                        .append(Text.literal(" on the ").formatted(Formatting.DARK_PURPLE))
                        .append(Text.literal("Netherite Heart").formatted(Formatting.DARK_RED)),
                  Text.literal("")
                        .append(Text.literal("Divine Energy").formatted(Formatting.LIGHT_PURPLE))
                        .append(Text.literal(" will flow into the ").formatted(Formatting.DARK_PURPLE))
                        .append(Text.literal("Nul Construct").formatted(Formatting.DARK_GRAY))
                        .append(Text.literal(" empowering it").formatted(Formatting.DARK_PURPLE)),
                  Text.literal("")
                        .append(Text.literal("Defeat the ").formatted(Formatting.DARK_PURPLE))
                        .append(Text.literal("Nul Construct").formatted(Formatting.DARK_GRAY))
                        .append(Text.literal(" without dying to receive a ").formatted(Formatting.DARK_PURPLE))
                        .append(Text.literal("Divine Catalyst").formatted(Formatting.LIGHT_PURPLE)),
                  Text.literal(""),
                  Text.literal("Warning! This fight is difficult, preparation is necessary.").formatted(Formatting.RED)
            ));
      
      ExplainIngredient[][] ingredients = {
            {a,a,a,a,a},
            {a,k,k,k,a},
            {a,s,n,s,c},
            {a,a,s,a,a},
            {a,a,a,a,a}};
      return new ExplainRecipe(ingredients);
   }
   
   @Override
   public List<List<Text>> getBookLore(){
      List<List<Text>> list = new ArrayList<>();
      list.add(List.of(Text.literal(" Divine Augmentation\n         Catalyst\n\nRarity: Divine\n\nThe Divine Artifacts are examples of divine Arcana, the Runic Matrix should be able to replicate that divine magic to some degree. But how? Perhaps I need to expose my strongest").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal(" Divine Augmentation\n         Catalyst\n\nCatalyst to some divine Arcana. I just need to get the attention of a god without risking too much of their wrath. The Wither is an interesting creature with some divine connection, perhaps that is my answer.").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal(" Divine Augmentation\n         Catalyst\n\nI know not what God is responsible for the Wither, but the God of Death would be a good guess. Reinforcing the construct pattern of the Wither and placing my Catalyst inside as it comes to life should give it the divine energy it requires.").formatted(Formatting.BLACK)));
      return list;
   }
   
   public class DivineCatalystItem extends ArcanaPolymerItem {
      public DivineCatalystItem(Item.Settings settings){
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
            canSpawn = PLAYER_DATA.get(serverPlayer).hasResearched(ArcanaRegistry.NUL_MEMENTO);
         }
         
         if(state.isOf(Blocks.NETHERITE_BLOCK) && pos.getY() >= world.getBottomY() && canSpawn){ // Check construct
            BlockPattern.Result patternResult = getConstructPattern().searchAround(world, pos);
            if (patternResult != null) {
               NulConstructEntity constructEntity = (NulConstructEntity) ArcanaRegistry.NUL_CONSTRUCT_ENTITY.create(world);
               if (constructEntity != null && world instanceof ServerWorld serverWorld) {
                  CarvedPumpkinBlock.breakPatternBlocks(world, patternResult);
                  BlockPos blockPos = patternResult.translate(1, 1, 0).getBlockPos();
                  constructEntity.refreshPositionAndAngles((double)blockPos.getX() + 0.5, (double)blockPos.getY() + 0.55, (double)blockPos.getZ() + 0.5, patternResult.getForwards().getAxis() == Direction.Axis.X ? 0.0F : 90.0F, 0.0F);
                  constructEntity.bodyYaw = patternResult.getForwards().getAxis() == Direction.Axis.X ? 0.0F : 90.0F;
                  constructEntity.onSummoned(playerEntity,true);
                  
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

