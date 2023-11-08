package net.borisshoes.arcananovum.items.catalysts;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.core.MagicItem;
import net.borisshoes.arcananovum.core.polymer.MagicPolymerItem;
import net.borisshoes.arcananovum.entities.NulConstructEntity;
import net.borisshoes.arcananovum.items.ArcaneTome;
import net.borisshoes.arcananovum.recipes.arcana.ForgeRequirement;
import net.borisshoes.arcananovum.recipes.arcana.GenericMagicIngredient;
import net.borisshoes.arcananovum.recipes.arcana.MagicItemIngredient;
import net.borisshoes.arcananovum.recipes.arcana.MagicItemRecipe;
import net.borisshoes.arcananovum.utils.MagicRarity;
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
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static net.borisshoes.arcananovum.entities.NulConstructEntity.getConstructPattern;

public class LegendaryCatalyst extends MagicItem {
   
   public LegendaryCatalyst(){
      id = "legendary_catalyst";
      name = "Legendary Augment Catalyst";
      categories = new ArcaneTome.TomeFilter[]{ArcaneTome.TomeFilter.MUNDANE, ArcaneTome.TomeFilter.CATALYSTS};
      rarity = MagicRarity.MUNDANE;
      vanillaItem = Items.GOLD_INGOT;
      item = new LegendaryCatalystItem(new FabricItemSettings().maxCount(1).fireproof());
      
      ItemStack stack = new ItemStack(item);
      NbtCompound tag = stack.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList enchants = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name","[{\"text\":\"Legendary Augment Catalyst\",\"italic\":false,\"color\":\"gold\",\"bold\":true}]");
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
      MagicItemIngredient a = new MagicItemIngredient(Items.CRYING_OBSIDIAN,64,null);
      MagicItemIngredient b = new MagicItemIngredient(Items.OBSIDIAN,64,null);
      MagicItemIngredient g = new MagicItemIngredient(Items.GOLD_INGOT,64,null);
      MagicItemIngredient h = new MagicItemIngredient(Items.NETHER_STAR,2,null);
      GenericMagicIngredient m = new GenericMagicIngredient(ArcanaRegistry.EXOTIC_CATALYST,1);
   
      MagicItemIngredient[][] ingredients = {
            {a,b,a,b,a},
            {b,g,h,g,b},
            {a,h,m,h,a},
            {b,g,h,g,b},
            {a,b,a,b,a}};
      return new MagicItemRecipe(ingredients, new ForgeRequirement().withAnvil());
   
   }
   
   private List<String> makeLore(){
      ArrayList<String> list = new ArrayList<>();
      list.add("{\"text\":\"  Legendary Augment\\n         Catalyst\\n\\nRarity: Mundane\\n\\nGOLD! The gemstones already provide enough reinforcement. Gold lets the energy be more malleable to more creative applications. But, I think there's a little potential left here...\"}");
      return list;
   }
   
   public class LegendaryCatalystItem extends MagicPolymerItem {
      public LegendaryCatalystItem(Settings settings){
         super(getThis(),settings);
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
