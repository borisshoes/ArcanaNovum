package net.borisshoes.arcananovum.items.charms;

import com.mojang.datafixers.util.Pair;
import net.borisshoes.arcananovum.ArcanaConfig;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerItem;
import net.borisshoes.arcananovum.gui.arcanetome.TomeGui;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaIngredient;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaRecipe;
import net.borisshoes.arcananovum.recipes.arcana.ForgeRequirement;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ConsumableComponent;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
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
      categories = new TomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), TomeGui.TomeFilter.CHARMS, TomeGui.TomeFilter.ITEMS};
      vanillaItem = Items.RABBIT_STEW;
      item = new FeastingCharmItem();
      displayName = Text.translatableWithFallback("item."+MOD_ID+"."+ID,name).formatted(Formatting.BOLD,Formatting.GOLD);
      researchTasks = new RegistryKey[]{ResearchTasks.ADVANCEMENT_BALANCED_DIET,ResearchTasks.HUNGER_DAMAGE,ResearchTasks.OBTAIN_ENCHANTED_GOLDEN_APPLE};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getMaxCount());
      putProperty(stack,MODE_TAG,0);
      setPrefStack(stack);
   }
   
   @Override
   public List<Text> getItemLore(@Nullable ItemStack itemStack){
      List<MutableText> lore = new ArrayList<>();
      lore.add(Text.literal("")
            .append(Text.literal("Just carrying the charm makes you feel ").formatted(Formatting.DARK_GREEN))
            .append(Text.literal("well nourished").formatted(Formatting.GOLD))
            .append(Text.literal(".").formatted(Formatting.DARK_GREEN)));
      lore.add(Text.literal("")
            .append(Text.literal("The charm ").formatted(Formatting.DARK_GREEN))
            .append(Text.literal("feeds").formatted(Formatting.GOLD))
            .append(Text.literal(" you from your").formatted(Formatting.DARK_GREEN))
            .append(Text.literal(" inventory.").formatted(Formatting.GOLD)));
      lore.add(Text.literal("")
            .append(Text.literal("Right click").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal(" to").formatted(Formatting.DARK_GREEN))
            .append(Text.literal(" toggle ").formatted(Formatting.GOLD))
            .append(Text.literal("the charm between feeding modes.").formatted(Formatting.DARK_GREEN)));
     return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   // Mode 0 is optimal eating - Optimal Eating
   // Mode 1 is eat when below regen range - Eat for Regen
   // Mode 2 is eat if possible when below half HP, otherwise optimal - Optimal + Emergency Eating
   // Mode 3 is eat if possible when below half HP, otherwise when below 2 regen range - Regen + Emergency Eating
   public void toggleMode(ServerPlayerEntity player, ItemStack stack){
      int mode = (getIntProperty(stack,MODE_TAG)+1) % 4;
      putProperty(stack,MODE_TAG,mode);
      switch(mode){
         case 0 -> player.sendMessage(Text.literal("Feasting Mode: Optimal").formatted(Formatting.YELLOW, Formatting.ITALIC), true);
         case 1 -> player.sendMessage(Text.literal("Feasting Mode: Regen").formatted(Formatting.YELLOW, Formatting.ITALIC), true);
         case 2 -> player.sendMessage(Text.literal("Feasting Mode: Optimal + Emergency").formatted(Formatting.YELLOW, Formatting.ITALIC), true);
         case 3 -> player.sendMessage(Text.literal("Feasting Mode: Regen + Emergency").formatted(Formatting.YELLOW, Formatting.ITALIC), true);
      }
   }
   
   @Override
   public List<List<Text>> getBookLore(){
      List<List<Text>> list = new ArrayList<>();
      list.add(List.of(Text.literal(" Charm of Feasting").formatted(Formatting.GOLD,Formatting.BOLD),Text.literal("\nRarity: ").formatted(Formatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(),false)),Text.literal("\nA simple infusion of Arcana can go a long way. Returning to the Enchanted Golden Apple, a bit of extra Arcana supplemented it to become a pseudo-infinite food source.\n").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal(" Charm of Feasting").formatted(Formatting.GOLD,Formatting.BOLD),Text.literal("\nAny food in my inventory will get ingested by the Charm when I am hungry, as long as it is not an Enchanted Golden Apple.\n\nSimply wearing the Charm also causes my hunger to be satiated periodically.\n").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal(" Charm of Feasting").formatted(Formatting.GOLD,Formatting.BOLD),Text.literal("\nUsing the Charm switches the feeding mode.\n \nOptimal mode waits for me to be as hungry as the food will restore.\n\nRegen mode keeps me within the threshold for health regeneration. ").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal(" Charm of Feasting").formatted(Formatting.GOLD,Formatting.BOLD),Text.literal("\nEmergency mode feeds me whenever possible when I am below half health.").formatted(Formatting.BLACK)));
      return list;
   }
   
   @Override
	protected ArcanaRecipe makeRecipe(){
      ArcanaIngredient a = new ArcanaIngredient(Items.COOKED_SALMON,16);
      ArcanaIngredient b = new ArcanaIngredient(Items.GLOW_BERRIES,16);
      ArcanaIngredient c = new ArcanaIngredient(Items.COOKED_BEEF,16);
      ArcanaIngredient d = new ArcanaIngredient(Items.PUMPKIN_PIE,16);
      ArcanaIngredient e = new ArcanaIngredient(Items.BREAD,16);
      ArcanaIngredient f = new ArcanaIngredient(Items.COOKIE,16);
      ArcanaIngredient g = new ArcanaIngredient(Items.GOLDEN_CARROT,8);
      ArcanaIngredient h = new ArcanaIngredient(Items.GOLDEN_APPLE,4);
      ArcanaIngredient j = new ArcanaIngredient(Items.MELON_SLICE,16);
      ArcanaIngredient k = new ArcanaIngredient(Items.COOKED_CHICKEN,16);
      ArcanaIngredient m = new ArcanaIngredient(Items.ENCHANTED_GOLDEN_APPLE,1, true);
      ArcanaIngredient o = new ArcanaIngredient(Items.COOKED_MUTTON,16);
      ArcanaIngredient p = new ArcanaIngredient(Items.BEETROOT,16);
      ArcanaIngredient t = new ArcanaIngredient(Items.DRIED_KELP,16);
      ArcanaIngredient u = new ArcanaIngredient(Items.COOKED_RABBIT,16);
      ArcanaIngredient v = new ArcanaIngredient(Items.BAKED_POTATO,16);
      ArcanaIngredient w = new ArcanaIngredient(Items.COOKED_PORKCHOP,16);
      ArcanaIngredient x = new ArcanaIngredient(Items.SWEET_BERRIES,16);
      ArcanaIngredient y = new ArcanaIngredient(Items.COOKED_COD,16);
      
      ArcanaIngredient[][] ingredients = {
            {a,b,c,d,e},
            {f,g,h,g,j},
            {k,h,m,h,o},
            {p,g,h,g,t},
            {u,v,w,x,y}};
      return new ArcanaRecipe(ingredients,new ForgeRequirement());
   }
   
   public class FeastingCharmItem extends ArcanaPolymerItem {
      public FeastingCharmItem(){
         super(getThis());
      }
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
      
      @Override
      public void inventoryTick(ItemStack stack, ServerWorld world, Entity entity, @Nullable EquipmentSlot slot){
         if(!ArcanaItemUtils.isArcane(stack)) return;
         if(!(world instanceof ServerWorld && entity instanceof ServerPlayerEntity player)) return;
         
         int mode = getIntProperty(stack,MODE_TAG);
         int time = 400 - 100*Math.max(0,ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.ENZYMES.id));
         int gluttony = Math.max(0,ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.GLUTTONY.id));
         
         if(world.getServer().getTicks() % time == 0){ // Consume food
            //Scan for available food items
            PlayerInventory inv = player.getInventory();
            HungerManager hunger = player.getHungerManager();
            ArrayList<Pair<Integer,FoodComponent>> availableFoods = new ArrayList<>();
            int bestFoodInd = -1;
            for(int i=0; i<inv.size();i++){
               ItemStack invItem = inv.getStack(i);
               if(invItem.isEmpty() || !invItem.contains(DataComponentTypes.FOOD) || !invItem.contains(DataComponentTypes.CONSUMABLE))
                  continue;
               
               if(!ArcanaItemUtils.isArcane(invItem) && invItem.getItem() != Items.ENCHANTED_GOLDEN_APPLE){
                  FoodComponent foodComponent = invItem.get(DataComponentTypes.FOOD);
                  if(foodComponent.nutrition() < 1) continue;
                  availableFoods.add(new Pair<>(i,foodComponent));
                  if(bestFoodInd == -1 || inv.getStack(bestFoodInd).get(DataComponentTypes.FOOD).nutrition() < foodComponent.nutrition()){
                     bestFoodInd = i;
                  }
               }
            }
            if(bestFoodInd != -1){
               ItemStack selectedFood = ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.PICKY_EATER.id) >= 1 ? inv.getStack(bestFoodInd) : inv.getStack(availableFoods.getFirst().getFirst());
               FoodComponent foodComponent = selectedFood.get(DataComponentTypes.FOOD);
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
                  player.sendMessage(Text.literal("Your Feasting Charm consumes a "+selectedFood.getName().getString()).formatted(Formatting.GOLD,Formatting.ITALIC),true);
                  hunger.add(gluttonyFoodBoost[gluttony],gluttonySatBoost[gluttony]);
                  
                  if(selectedFood.isOf(Items.POISONOUS_POTATO)){
                     ArcanaAchievements.setCondition(player,ArcanaAchievements.TARRARE.id,"Poisonous Potato",true);
                  }else if(selectedFood.isOf(Items.SPIDER_EYE)){
                     ArcanaAchievements.setCondition(player,ArcanaAchievements.TARRARE.id,"Spider Eye",true);
                  }else if(selectedFood.isOf(Items.ROTTEN_FLESH)){
                     ArcanaAchievements.setCondition(player,ArcanaAchievements.TARRARE.id,"Rotten Flesh",true);
                  }else if(selectedFood.isOf(Items.SUSPICIOUS_STEW)){
                     ArcanaAchievements.setCondition(player,ArcanaAchievements.TARRARE.id,"Suspicious Stew",true);
                  }else if(selectedFood.isOf(Items.CHICKEN)){
                     ArcanaAchievements.setCondition(player,ArcanaAchievements.TARRARE.id,"Raw Chicken",true);
                  }else if(selectedFood.isOf(Items.PUFFERFISH)){
                     ArcanaAchievements.setCondition(player,ArcanaAchievements.TARRARE.id,"Pufferfish",true);
                  }
                  
                  ConsumableComponent consumableComponent = selectedFood.get(DataComponentTypes.CONSUMABLE);
                  consumableComponent.finishConsumption(world, player, selectedFood); // Handles effects, nutrition, and stack decrease
                  
                  ArcanaNovum.data(player).addXP(ArcanaConfig.getInt(ArcanaRegistry.FEASTING_CHARM_PER_FOOD_VALUE)*foodValue); // Add xp
               }
            }
         }
         if(world.getServer().getTicks() % (time*6) == 0){ // Give player a small hunger boost
            player.getHungerManager().add(1+gluttony,(1+gluttony)*0.25f);
         }
      }
      
      @Override
      public ActionResult use(World world, PlayerEntity playerEntity, Hand hand){
         toggleMode((ServerPlayerEntity) playerEntity,playerEntity.getStackInHand(hand));
         return ActionResult.SUCCESS_SERVER;
      }
   }
}

