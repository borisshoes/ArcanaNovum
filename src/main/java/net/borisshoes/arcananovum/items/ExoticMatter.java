package net.borisshoes.arcananovum.items;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.augments.ArcanaAugment;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.EnergyItem;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerItem;
import net.borisshoes.arcananovum.gui.arcanetome.TomeGui;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaIngredient;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaRecipe;
import net.borisshoes.arcananovum.recipes.arcana.ForgeRequirement;
import net.borisshoes.arcananovum.recipes.arcana.GenericArcanaIngredient;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.arcananovum.utils.ArcanaRarity;
import net.borisshoes.arcananovum.utils.TextUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class ExoticMatter extends EnergyItem {
	public static final String ID = "exotic_matter";
   
   private static final double[] lvlMultiplier = {1,1.5,2,2.5,3,5};
   
   public ExoticMatter(){
      id = ID;
      name = "Exotic Matter";
      rarity = ArcanaRarity.MUNDANE;
      categories = new TomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), TomeGui.TomeFilter.ITEMS};
      initEnergy = 600000;
      vanillaItem = Items.STRUCTURE_BLOCK;
      item = new ExoticMatterItem();
      displayName = Text.translatableWithFallback("item."+MOD_ID+"."+ID,name).formatted(Formatting.BOLD,Formatting.BLUE);
      researchTasks = new RegistryKey[]{ResearchTasks.UNLOCK_TEMPORAL_MOMENT,ResearchTasks.OBTAIN_CLOCK,ResearchTasks.ADVANCEMENT_SLEEP_IN_BED,ResearchTasks.OBTAIN_END_CRYSTAL};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getMaxCount());
      setPrefStack(stack);
   }
   
   @Override
   public List<Text> getItemLore(@Nullable ItemStack itemStack){
      List<MutableText> lore = new ArrayList<>();
      lore.add(Text.literal("")
            .append(Text.literal("This ").formatted(Formatting.DARK_GRAY))
            .append(Text.literal("strange matter").formatted(Formatting.BLUE))
            .append(Text.literal(" seems to warp ").formatted(Formatting.DARK_GRAY))
            .append(Text.literal("spacetime").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal(".").formatted(Formatting.DARK_GRAY)));
      lore.add(Text.literal("")
            .append(Text.literal("Perhaps it could be ").formatted(Formatting.DARK_GRAY))
            .append(Text.literal("useful").formatted(Formatting.ITALIC,Formatting.GRAY))
            .append(Text.literal(" for something...").formatted(Formatting.DARK_GRAY)));
      lore.add(Text.literal("")
            .append(Text.literal("Used as ").formatted(Formatting.DARK_GRAY))
            .append(Text.literal("fuel").formatted(Formatting.GOLD))
            .append(Text.literal(" for the ").formatted(Formatting.DARK_GRAY))
            .append(Text.literal("Continuum Anchor").formatted(Formatting.DARK_BLUE)));
      lore.add(Text.literal(""));
      
      String timeText = itemStack == null ? "7 Days" : getDuration(itemStack);
      lore.add(Text.literal("")
            .append(Text.literal("Fuel - ").formatted(Formatting.DARK_GRAY))
            .append(Text.literal(timeText).formatted(Formatting.BLUE)));
     return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   public ItemStack updateItem(ItemStack stack, MinecraftServer server){
      ItemStack newStack = super.updateItem(stack,server);
      setFuel(newStack,getEnergy(newStack));
      return buildItemLore(newStack,server);
   }
   
   @Override
   public ItemStack onAugment(ItemStack stack, ArcanaAugment augment, int level){
      if(ArcanaItemUtils.identifyItem(stack) instanceof ExoticMatter && augment == ArcanaAugments.TIME_IN_A_BOTTLE){
         setFuel(stack,getMaxEnergy(stack));
      }
      return stack;
   }
   
   @Override
   public int getMaxEnergy(ItemStack item){
      // Maximum seconds of chunk loading per exotic matter fuel (1 week baseline)
      int level = Math.max(0, ArcanaAugments.getAugmentOnItem(item,ArcanaAugments.TIME_IN_A_BOTTLE.id));
      return (int) (600000 * lvlMultiplier[level]);
   }
   
   public int useFuel(ItemStack item, int fuel){
      int newFuel = MathHelper.clamp(getEnergy(item)-fuel, 0, getMaxEnergy(item));
      setEnergy(item,newFuel);
      buildItemLore(item,ArcanaNovum.SERVER);
      return newFuel;
   }
   
   public void setFuel(ItemStack item, int fuel){
      int newFuel = MathHelper.clamp(fuel, 0, getMaxEnergy(item));
      setEnergy(item,newFuel);
      buildItemLore(item,ArcanaNovum.SERVER);
   }
   
   public String getDuration(ItemStack item){
      int energy = getEnergy(item);
      String duration;
      if(energy >= 172800){
         duration = ((energy/86400)+1)+" Days";
      }else if(energy >= 6000){
         duration = ((energy/3600)+1)+" Hours";
      }else if(energy >= 100){
         duration = ((energy/60)+1)+" Minutes";
      }else{
         duration = energy+" Seconds";
      }
      return duration;
   }
   
   @Override
   public List<List<Text>> getBookLore(){
      List<List<Text>> list = new ArrayList<>();
      list.add(List.of(Text.literal("    Exotic Matter").formatted(Formatting.BLUE,Formatting.BOLD),Text.literal("\nRarity: ").formatted(Formatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(),false)),Text.literal("\nThe components of this matter seem to spontaneously warp spacetime when combined with a Temporal Moment. Perhaps this can be exploited…").formatted(Formatting.BLACK)));
      return list;
   }
   
   @Override
	protected ArcanaRecipe makeRecipe(){
      ArcanaIngredient a = new ArcanaIngredient(Items.CRYING_OBSIDIAN,8);
      ArcanaIngredient b = new ArcanaIngredient(Items.OBSIDIAN,8);
      ArcanaIngredient c = new ArcanaIngredient(Items.DIAMOND,2);
      ArcanaIngredient d = new ArcanaIngredient(Items.END_CRYSTAL,2);
      GenericArcanaIngredient m = new GenericArcanaIngredient(ArcanaRegistry.TEMPORAL_MOMENT,1);
      
      ArcanaIngredient[][] ingredients = {
            {a,b,c,d,a},
            {d,a,d,a,b},
            {c,d,m,d,c},
            {b,a,d,a,d},
            {a,d,c,b,a}};
      return new ArcanaRecipe(ingredients,new ForgeRequirement());
   }
   
   public class ExoticMatterItem extends ArcanaPolymerItem {
      public ExoticMatterItem(){
         super(getThis());
      }
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
   }
}

