package net.borisshoes.arcananovum.items.charms;

import com.mojang.datafixers.util.Pair;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerItem;
import net.borisshoes.arcananovum.gui.arcanetome.ArcaneTomeGui;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class FeastingCharm extends ArcanaItem {
	public static final String ID = "feasting_charm";
   
   private static final int[] gluttonyFoodBoost = {0,2,4,6};
   private static final float[] gluttonySatBoost = {0,0.25f,0.5f,1f};
   
   public FeastingCharm(){
      id = ID;
      name = "Charm of Feasting";
      rarity = ArcanaRarity.EXOTIC;
      categories = new ArcaneTomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), ArcaneTomeGui.TomeFilter.CHARMS, ArcaneTomeGui.TomeFilter.ITEMS};
      vanillaItem = Items.RABBIT_STEW;
      item = new FeastingCharmItem();
      displayName = Component.translatableWithFallback("item."+MOD_ID+"."+ID,name).withStyle(ChatFormatting.BOLD, ChatFormatting.GOLD);
      researchTasks = new ResourceKey[]{ResearchTasks.USE_ENCHANTED_GOLDEN_APPLE,ResearchTasks.HUNGER_DAMAGE,ResearchTasks.OBTAIN_ENCHANTED_GOLDEN_APPLE};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getDefaultMaxStackSize());
      putProperty(stack,MODE_TAG,0);
      setPrefStack(stack);
   }
   
   @Override
   public List<Component> getItemLore(@Nullable ItemStack itemStack){
      List<MutableComponent> lore = new ArrayList<>();
      lore.add(Component.literal("")
            .append(Component.literal("Just carrying the charm makes you feel ").withStyle(ChatFormatting.DARK_GREEN))
            .append(Component.literal("well nourished").withStyle(ChatFormatting.GOLD))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_GREEN)));
      lore.add(Component.literal("")
            .append(Component.literal("The charm ").withStyle(ChatFormatting.DARK_GREEN))
            .append(Component.literal("feeds").withStyle(ChatFormatting.GOLD))
            .append(Component.literal(" you from your").withStyle(ChatFormatting.DARK_GREEN))
            .append(Component.literal(" inventory.").withStyle(ChatFormatting.GOLD)));
      lore.add(Component.literal("")
            .append(Component.literal("Right click").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(" to").withStyle(ChatFormatting.DARK_GREEN))
            .append(Component.literal(" toggle ").withStyle(ChatFormatting.GOLD))
            .append(Component.literal("the charm between feeding modes.").withStyle(ChatFormatting.DARK_GREEN)));
     return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   // Mode 0 is optimal eating - Optimal Eating
   // Mode 1 is eat when below regen range - Eat for Regen
   // Mode 2 is eat if possible when below half HP, otherwise optimal - Optimal + Emergency Eating
   // Mode 3 is eat if possible when below half HP, otherwise when below 2 regen range - Regen + Emergency Eating
   public void toggleMode(ServerPlayer player, ItemStack stack){
      int mode = (getIntProperty(stack,MODE_TAG)+1) % 4;
      putProperty(stack,MODE_TAG,mode);
      switch(mode){
         case 0 -> player.displayClientMessage(Component.literal("Feasting Mode: Optimal").withStyle(ChatFormatting.YELLOW, ChatFormatting.ITALIC), true);
         case 1 -> player.displayClientMessage(Component.literal("Feasting Mode: Regen").withStyle(ChatFormatting.YELLOW, ChatFormatting.ITALIC), true);
         case 2 -> player.displayClientMessage(Component.literal("Feasting Mode: Optimal + Emergency").withStyle(ChatFormatting.YELLOW, ChatFormatting.ITALIC), true);
         case 3 -> player.displayClientMessage(Component.literal("Feasting Mode: Regen + Emergency").withStyle(ChatFormatting.YELLOW, ChatFormatting.ITALIC), true);
      }
   }
   
   @Override
   public List<List<Component>> getBookLore(){
      List<List<Component>> list = new ArrayList<>();
      list.add(List.of(Component.literal(" Charm of Feasting").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), Component.literal("\nRarity: ").withStyle(ChatFormatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(),false)), Component.literal("\nA simple infusion of Arcana can go a long way. Returning to the Enchanted Golden Apple, a bit of extra Arcana supplemented it to become a pseudo-infinite food source.\n").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal(" Charm of Feasting").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), Component.literal("\nAny food in my inventory will get ingested by the Charm when I am hungry, as long as it is not an Enchanted Golden Apple.\n\nSimply wearing the Charm also causes my hunger to be satiated periodically.\n").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal(" Charm of Feasting").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), Component.literal("\nUsing the Charm switches the feeding mode.\n \nOptimal mode waits for me to be as hungry as the food will restore.\n\nRegen mode keeps me within the threshold for health regeneration. ").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal(" Charm of Feasting").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), Component.literal("\nEmergency mode feeds me whenever possible when I am below half health.").withStyle(ChatFormatting.BLACK)));
      return list;
   }
   
   public class FeastingCharmItem extends ArcanaPolymerItem {
      public FeastingCharmItem(){
         super(getThis());
      }
      
      @Override
      public ItemStack getDefaultInstance(){
         return prefItem;
      }
      
      @Override
      public void inventoryTick(ItemStack stack, ServerLevel world, Entity entity, @Nullable EquipmentSlot slot){
         if(!ArcanaItemUtils.isArcane(stack)) return;
         if(!(world instanceof ServerLevel && entity instanceof ServerPlayer player)) return;
         
         int mode = getIntProperty(stack,MODE_TAG);
         int time = 400 - 100*Math.max(0,ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.ENZYMES.id));
         int gluttony = Math.max(0,ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.GLUTTONY.id));
         
         if(world.getServer().getTickCount() % time == 0){ // Consume food
            //Scan for available food items
            Inventory inv = player.getInventory();
            FoodData hunger = player.getFoodData();
            ArrayList<Pair<Integer, FoodProperties>> availableFoods = new ArrayList<>();
            int bestFoodInd = -1;
            for(int i = 0; i<inv.getContainerSize(); i++){
               ItemStack invItem = inv.getItem(i);
               if(invItem.isEmpty() || !invItem.has(DataComponents.FOOD) || !invItem.has(DataComponents.CONSUMABLE))
                  continue;
               
               if(!ArcanaItemUtils.isArcane(invItem) && invItem.getItem() != Items.ENCHANTED_GOLDEN_APPLE){
                  FoodProperties foodComponent = invItem.get(DataComponents.FOOD);
                  if(foodComponent.nutrition() < 1) continue;
                  availableFoods.add(new Pair<>(i,foodComponent));
                  if(bestFoodInd == -1 || inv.getItem(bestFoodInd).get(DataComponents.FOOD).nutrition() < foodComponent.nutrition()){
                     bestFoodInd = i;
                  }
               }
            }
            if(bestFoodInd != -1){
               ItemStack selectedFood = ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.PICKY_EATER.id) >= 1 ? inv.getItem(bestFoodInd) : inv.getItem(availableFoods.getFirst().getFirst());
               FoodProperties foodComponent = selectedFood.get(DataComponents.FOOD);
               int foodValue = foodComponent.nutrition();
               
               boolean consume = switch(mode){
                  case 0 -> // Mode 0 is optimal eating - Optimal Eating
                        20 - hunger.getFoodLevel() >= foodValue;
                  case 1 -> // Mode 1 is eat when below regen range - Eat for Regen
                        hunger.getFoodLevel() < 18;
                  case 2 -> // Mode 2 is eat if possible when below half HP, otherwise optimal - Optimal + Emergency Eating
                        20 - hunger.getFoodLevel() >= foodValue || player.getHealth() < 10;
                  case 3 -> // Mode 3 is eat if possible when below half HP, otherwise when below 2 regen range - Regen + Emergency Eating
                        hunger.getFoodLevel() < 18 || player.getHealth() < 10;
                  default -> false;
               };
               
               if(consume){
                  player.displayClientMessage(Component.literal("Your Feasting Charm consumes a "+selectedFood.getHoverName().getString()).withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC),true);
                  hunger.eat(gluttonyFoodBoost[gluttony],gluttonySatBoost[gluttony]);
                  
                  if(selectedFood.is(Items.POISONOUS_POTATO)){
                     ArcanaAchievements.setCondition(player,ArcanaAchievements.TARRARE.id,"Poisonous Potato",true);
                  }else if(selectedFood.is(Items.SPIDER_EYE)){
                     ArcanaAchievements.setCondition(player,ArcanaAchievements.TARRARE.id,"Spider Eye",true);
                  }else if(selectedFood.is(Items.ROTTEN_FLESH)){
                     ArcanaAchievements.setCondition(player,ArcanaAchievements.TARRARE.id,"Rotten Flesh",true);
                  }else if(selectedFood.is(Items.SUSPICIOUS_STEW)){
                     ArcanaAchievements.setCondition(player,ArcanaAchievements.TARRARE.id,"Suspicious Stew",true);
                  }else if(selectedFood.is(Items.CHICKEN)){
                     ArcanaAchievements.setCondition(player,ArcanaAchievements.TARRARE.id,"Raw Chicken",true);
                  }else if(selectedFood.is(Items.PUFFERFISH)){
                     ArcanaAchievements.setCondition(player,ArcanaAchievements.TARRARE.id,"Pufferfish",true);
                  }
                  
                  Consumable consumableComponent = selectedFood.get(DataComponents.CONSUMABLE);
                  consumableComponent.onConsume(world, player, selectedFood); // Handles effects, nutrition, and stack decrease
                  
                  ArcanaNovum.data(player).addXP(ArcanaNovum.CONFIG.getInt(ArcanaRegistry.XP_FEASTING_CHARM_PER_FOOD_VALUE)*foodValue); // Add xp
               }
            }
         }
         if(world.getServer().getTickCount() % (time*6) == 0){ // Give player a small hunger boost
            player.getFoodData().eat(1+gluttony,(1+gluttony)*0.25f);
         }
      }
      
      @Override
      public InteractionResult use(Level world, Player playerEntity, InteractionHand hand){
         toggleMode((ServerPlayer) playerEntity,playerEntity.getItemInHand(hand));
         return InteractionResult.SUCCESS_SERVER;
      }
   }
}

