package net.borisshoes.arcananovum.items.catalysts;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.core.MagicItem;
import net.borisshoes.arcananovum.core.polymer.MagicPolymerItem;
import net.borisshoes.arcananovum.entities.NulConstructEntity;
import net.borisshoes.arcananovum.items.ArcaneTome;
import net.borisshoes.arcananovum.recipes.arcana.ExplainIngredient;
import net.borisshoes.arcananovum.recipes.arcana.ExplainRecipe;
import net.borisshoes.arcananovum.recipes.arcana.MagicItemRecipe;
import net.borisshoes.arcananovum.utils.MagicRarity;
import net.borisshoes.arcananovum.utils.MiscUtils;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CarvedPumpkinBlock;
import net.minecraft.block.pattern.BlockPattern;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
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

import static net.borisshoes.arcananovum.entities.NulConstructEntity.getConstructPattern;

public class MythicalCatalyst extends MagicItem {
   
   private static final String TXT = "item/mythical_catalyst";
   
   public MythicalCatalyst(){
      id = "mythical_catalyst";
      name = "Mythical Augment Catalyst";
      categories = new ArcaneTome.TomeFilter[]{ArcaneTome.TomeFilter.MYTHICAL, ArcaneTome.TomeFilter.CATALYSTS};
      rarity = MagicRarity.MYTHICAL;
      itemVersion = 1;
      vanillaItem = Items.AMETHYST_CLUSTER;
      item = new MythicalCatalystItem(new FabricItemSettings().maxCount(1).fireproof());
      models = new ArrayList<>();
      models.add(new Pair<>(vanillaItem,TXT));
      
      ItemStack stack = new ItemStack(item);
      NbtCompound tag = stack.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList enchants = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name","[{\"text\":\"Mythical Augment Catalyst\",\"italic\":false,\"color\":\"light_purple\",\"bold\":true}]");
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
      loreList.add(NbtString.of("[{\"text\":\"Augment \",\"italic\":false,\"color\":\"dark_aqua\"},{\"text\":\"Catalysts\",\"color\":\"blue\"},{\"text\":\" can be used to \",\"color\":\"gray\"},{\"text\":\"augment \"},{\"text\":\"your \",\"color\":\"gray\"},{\"text\":\"Magic Items\",\"color\":\"dark_purple\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Augments \",\"italic\":false,\"color\":\"dark_aqua\"},{\"text\":\"require more \",\"color\":\"gray\"},{\"text\":\"powerful \",\"color\":\"green\"},{\"text\":\"Catalysts \",\"color\":\"blue\"},{\"text\":\"at higher levels\",\"color\":\"gray\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Apply \",\"italic\":false,\"color\":\"green\"},{\"text\":\"these \",\"color\":\"gray\"},{\"text\":\"Catalysts \",\"color\":\"blue\"},{\"text\":\"in the \",\"color\":\"gray\"},{\"text\":\"Tinkering Menu\",\"color\":\"blue\"},{\"text\":\" of a \",\"color\":\"gray\"},{\"text\":\"Twilight Anvil\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      return loreList;
   }
   
   private MagicItemRecipe makeRecipe(){
      ItemStack pane = new ItemStack(Items.GRAY_STAINED_GLASS_PANE).setCustomName(Text.literal("In World Recipe").formatted(Formatting.BLUE,Formatting.BOLD));
      MiscUtils.addLoreLine(pane,Text.literal("Build this in the World").formatted(Formatting.DARK_PURPLE));
      
      ItemStack soulSand = new ItemStack(Items.SOUL_SAND).setCustomName(Text.literal("Soul Sand or Soil").formatted(Formatting.GRAY,Formatting.BOLD));
      MiscUtils.addLoreLine(soulSand,Text.literal("Construct a Wither Base with a heart of Netherite").formatted(Formatting.DARK_PURPLE));
      
      ItemStack skull = new ItemStack(Items.WITHER_SKELETON_SKULL).setCustomName(Text.literal("Eye of Ender").formatted(Formatting.DARK_GRAY,Formatting.BOLD));
      MiscUtils.addLoreLine(skull,Text.literal("Construct a Wither Base with a heart of Netherite").formatted(Formatting.DARK_PURPLE));
      
      ItemStack netherite = new ItemStack(Items.NETHERITE_BLOCK).setCustomName(Text.literal("Block of Netherite").formatted(Formatting.DARK_RED,Formatting.BOLD));
      MiscUtils.addLoreLine(netherite,Text.literal("Construct a Wither Base with a heart of Netherite").formatted(Formatting.DARK_PURPLE));
      
      ItemStack catalyst = ArcanaRegistry.LEGENDARY_CATALYST.getItem().getDefaultStack().copy();
      MiscUtils.addLoreLine(catalyst,Text.literal("")
            .append(Text.literal("Right Click").formatted(Formatting.BLUE))
            .append(Text.literal(" the ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("Catalyst").formatted(Formatting.GOLD))
            .append(Text.literal(" on the ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("Netherite Heart").formatted(Formatting.DARK_RED)));
      MiscUtils.addLoreLine(catalyst,Text.literal("")
            .append(Text.literal("Divine Energy").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal(" will flow into the ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("Nul Construct").formatted(Formatting.DARK_GRAY))
            .append(Text.literal(" empowering it").formatted(Formatting.DARK_PURPLE)));
      MiscUtils.addLoreLine(catalyst,Text.literal("")
            .append(Text.literal("Defeat the ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("Nul Construct").formatted(Formatting.DARK_GRAY))
            .append(Text.literal(" without dying to receive a ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("Mythical Catalyst").formatted(Formatting.LIGHT_PURPLE)));
      MiscUtils.addLoreLine(catalyst,Text.literal("").formatted(Formatting.DARK_PURPLE));
      MiscUtils.addLoreLine(catalyst,Text.literal("Warning! This fight is difficult, preparation is necessary.").formatted(Formatting.RED));
      
      ExplainIngredient a = new ExplainIngredient(pane,"",false);
      ExplainIngredient s = new ExplainIngredient(soulSand,"Soul Sand or Soil");
      ExplainIngredient k = new ExplainIngredient(skull,"Wither Skeleton Skull");
      ExplainIngredient n = new ExplainIngredient(netherite,"Netherite Block");
      ExplainIngredient c = new ExplainIngredient(catalyst,"Legendary Augment Catalyst");
      
      ExplainIngredient[][] ingredients = {
            {a,a,a,a,a},
            {a,k,k,k,a},
            {a,s,n,s,c},
            {a,a,s,a,a},
            {a,a,a,a,a}};
      return new ExplainRecipe(ingredients);
   }
   
   private List<String> makeLore(){
      ArrayList<String> list = new ArrayList<>();
      list.add("{\"text\":\" Mythical Augmentation\\n         Catalyst\\n\\nRarity: Mythical\\n\\nThe Mythical Artifacts are examples of divine Arcana, the Runic Matrix should be able to replicate that divine magic to some degree. But how? Perhaps I need to expose my strongest\"}");
      list.add("{\"text\":\" Mythical Augmentation\\n         Catalyst\\n\\nCatalyst to some divine Arcana. I just need to get the attention of a god without risking too much of their wrath. The Wither is an interesting creature with some divine connection, perhaps that is my answer.\"}");
      list.add("{\"text\":\" Mythical Augmentation\\n         Catalyst\\n\\nI know not what God is responsible for the Wither, but the God of Death would be a good guess. Reinforcing the construct pattern of the Wither and placing my Catalyst inside as it comes to life should give it the divine energy it requires.\"}");
      return list;
   }
   
   public class MythicalCatalystItem extends MagicPolymerItem {
      public MythicalCatalystItem(Settings settings){
         super(getThis(),settings);
      }
      
      @Override
      public int getPolymerCustomModelData(ItemStack itemStack, @Nullable ServerPlayerEntity player){
         return ArcanaRegistry.MODELS.get(TXT).value();
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
         if(state.isOf(Blocks.NETHERITE_BLOCK) && pos.getY() >= world.getBottomY()){ // Check construct
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
