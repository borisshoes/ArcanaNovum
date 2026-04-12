package net.borisshoes.arcananovum.items;

import net.borisshoes.arcananovum.ArcanaConfig;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.augments.ArcanaAugment;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.core.EnergyItem;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerItem;
import net.borisshoes.arcananovum.gui.arcanetome.ArcaneTomeGui;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.borislib.BorisLib;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class ExoticMatter extends EnergyItem {
   public static final String ID = "exotic_matter";
   
   public ExoticMatter(){
      id = ID;
      name = "Exotic Matter";
      rarity = ArcanaRarity.MUNDANE;
      categories = new ArcaneTomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), ArcaneTomeGui.TomeFilter.ITEMS};
      initEnergy = ArcanaNovum.CONFIG.getInt(ArcanaConfig.EXOTIC_MATTER_DURATION);
      vanillaItem = Items.STRUCTURE_BLOCK;
      item = new ExoticMatterItem();
      displayName = Component.translatableWithFallback("item." + MOD_ID + "." + ID, name).withStyle(ChatFormatting.BOLD, ChatFormatting.BLUE);
      researchTasks = new ResourceKey[]{ResearchTasks.UNLOCK_TEMPORAL_MOMENT, ResearchTasks.OBTAIN_CLOCK, ResearchTasks.ADVANCEMENT_SLEEP_IN_BED, ResearchTasks.OBTAIN_END_CRYSTAL};
   }
   
   @Override
   public List<Component> getItemLore(@Nullable ItemStack itemStack){
      List<MutableComponent> lore = new ArrayList<>();
      lore.add(Component.literal("")
            .append(Component.literal("This ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("strange matter").withStyle(ChatFormatting.BLUE))
            .append(Component.literal(" seems to warp ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("spacetime").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_GRAY)));
      lore.add(Component.literal("")
            .append(Component.literal("Perhaps it could be ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("useful").withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY))
            .append(Component.literal(" for something...").withStyle(ChatFormatting.DARK_GRAY)));
      lore.add(Component.literal("")
            .append(Component.literal("Used as ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("fuel").withStyle(ChatFormatting.GOLD))
            .append(Component.literal(" for the ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("Continuum Anchor").withStyle(ChatFormatting.DARK_BLUE)));
      lore.add(Component.literal(""));
      
      String timeText = itemStack == null ? "7 Days" : getDuration(itemStack);
      lore.add(Component.literal("")
            .append(Component.literal("Fuel - ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal(timeText).withStyle(ChatFormatting.BLUE)));
      return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   public ItemStack updateItem(ItemStack stack, MinecraftServer server){
      ItemStack newStack = super.updateItem(stack, server);
      setFuel(newStack, getEnergy(newStack));
      return buildItemLore(newStack, server);
   }
   
   @Override
   public ItemStack onAugment(ItemStack stack, ArcanaAugment augment, int level){
      if(ArcanaItemUtils.identifyItem(stack) instanceof ExoticMatter && augment == ArcanaAugments.TIME_IN_A_BOTTLE){
         setFuel(stack, getMaxEnergy(stack));
      }
      return stack;
   }
   
   @Override
   public int getMaxEnergy(ItemStack item){
      // Maximum seconds of chunk loading per exotic matter fuel (1 week baseline)
      int baseDuration = ArcanaNovum.CONFIG.getInt(ArcanaConfig.EXOTIC_MATTER_DURATION);
      int extraDuration = ArcanaNovum.CONFIG.getIntList(ArcanaConfig.EXOTIC_MATTER_DURATION_PER_LVL).get(ArcanaAugments.getAugmentOnItem(item, ArcanaAugments.TIME_IN_A_BOTTLE));
      return baseDuration + extraDuration;
   }
   
   public int useFuel(ItemStack item, int fuel){
      int newFuel = Mth.clamp(getEnergy(item) - fuel, 0, getMaxEnergy(item));
      setEnergy(item, newFuel);
      buildItemLore(item, BorisLib.SERVER);
      return newFuel;
   }
   
   public void setFuel(ItemStack item, int fuel){
      int newFuel = Mth.clamp(fuel, 0, getMaxEnergy(item));
      setEnergy(item, newFuel);
      buildItemLore(item, BorisLib.SERVER);
   }
   
   public String getDuration(ItemStack item){
      int energy = getEnergy(item);
      String duration;
      if(energy >= 172800){
         duration = ((energy / 86400) + 1) + " Days";
      }else if(energy >= 6000){
         duration = ((energy / 3600) + 1) + " Hours";
      }else if(energy >= 100){
         duration = ((energy / 60) + 1) + " Minutes";
      }else{
         duration = energy + " Seconds";
      }
      return duration;
   }
   
   @Override
   public List<List<Component>> getBookLore(){
      List<List<Component>> list = new ArrayList<>();
      list.add(List.of(Component.literal("    Exotic Matter").withStyle(ChatFormatting.BLUE, ChatFormatting.BOLD), Component.literal("\nRarity: ").withStyle(ChatFormatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(), false)), Component.literal("\nThe components of this matter seem to spontaneously warp spacetime when combined with a Temporal Moment. Perhaps this can be exploited…").withStyle(ChatFormatting.BLACK)));
      return list;
   }
   
   public class ExoticMatterItem extends ArcanaPolymerItem {
      public ExoticMatterItem(){
         super(getThis());
      }
      
      @Override
      public ItemStack getDefaultInstance(){
         return prefItem;
      }
   }
}

