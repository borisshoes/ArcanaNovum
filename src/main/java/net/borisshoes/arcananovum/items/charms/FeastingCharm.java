package net.borisshoes.arcananovum.items.charms;

import com.mojang.datafixers.util.Pair;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.MagicItem;
import net.borisshoes.arcananovum.core.polymer.MagicPolymerItem;
import net.borisshoes.arcananovum.items.ArcaneTome;
import net.borisshoes.arcananovum.recipes.arcana.MagicItemIngredient;
import net.borisshoes.arcananovum.recipes.arcana.MagicItemRecipe;
import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.borisshoes.arcananovum.utils.MagicRarity;
import net.borisshoes.arcananovum.utils.SoundUtils;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;

public class FeastingCharm extends MagicItem {
   
   private static final int[] gluttonyFoodBoost = {0,2,4,8};
   private static final float[] gluttonySatBoost = {0,0.5f,1f,2f};
   
   public FeastingCharm(){
      id = "feasting_charm";
      name = "Charm of Feasting";
      rarity = MagicRarity.EMPOWERED;
      categories = new ArcaneTome.TomeFilter[]{ArcaneTome.TomeFilter.EMPOWERED, ArcaneTome.TomeFilter.CHARMS, ArcaneTome.TomeFilter.ITEMS};
      vanillaItem = Items.RABBIT_STEW;
      item = new FeastingCharmItem(new FabricItemSettings().maxCount(1).fireproof());
      
      ItemStack stack = new ItemStack(item);
      NbtCompound tag = stack.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList enchants = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name","[{\"text\":\"Charm of Feasting\",\"italic\":false,\"bold\":true,\"color\":\"gold\"}]");
      tag.put("display",display);
      tag.put("Enchantments",enchants);
      buildItemLore(stack, ArcanaNovum.SERVER);
   
      setBookLore(makeLore());
      setRecipe(makeRecipe());
      prefNBT = addMagicNbt(tag);
      prefNBT.getCompound("arcananovum").putInt("mode",0);
      
      stack.setNbt(prefNBT);
      prefItem = stack;
   }
   
   @Override
   public NbtList getItemLore(@Nullable ItemStack itemStack){
      NbtList loreList = new NbtList();
      loreList.add(NbtString.of("[{\"text\":\"Just carrying the charm makes you feel \",\"italic\":false,\"color\":\"dark_green\"},{\"text\":\"well nourished\",\"color\":\"gold\"},{\"text\":\".\",\"color\":\"dark_green\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"The charm \",\"italic\":false,\"color\":\"dark_green\"},{\"text\":\"feeds\",\"color\":\"gold\"},{\"text\":\" you from your\"},{\"text\":\" inventory.\",\"color\":\"gold\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Right click\",\"italic\":false,\"color\":\"light_purple\"},{\"text\":\" to\",\"color\":\"dark_green\"},{\"text\":\" toggle \",\"color\":\"gold\"},{\"text\":\"the charm between feeding modes.\",\"color\":\"dark_green\"}]"));
      return loreList;
   }
   
   // Mode 0 is optimal eating - Optimal Eating
   // Mode 1 is eat when below regen range - Eat for Regen
   // Mode 2 is eat if possible when below half HP, otherwise optimal - Optimal + Emergency Eating
   // Mode 3 is eat if possible when below half HP, otherwise when below 2 regen range - Regen + Emergency Eating
   public void toggleMode(ServerPlayerEntity player, ItemStack item){
      NbtCompound itemNbt = item.getNbt();
      NbtCompound magicNbt = itemNbt.getCompound("arcananovum");
      int mode = (magicNbt.getInt("mode")+1) % 4;
      magicNbt.putInt("mode",mode);
      itemNbt.put("arcananovum",magicNbt);
      item.setNbt(itemNbt);
      switch(mode){
         case 0 -> player.sendMessage(Text.translatable("Feasting Mode: Optimal").formatted(Formatting.YELLOW, Formatting.ITALIC), true);
         case 1 -> player.sendMessage(Text.translatable("Feasting Mode: Regen").formatted(Formatting.YELLOW, Formatting.ITALIC), true);
         case 2 -> player.sendMessage(Text.translatable("Feasting Mode: Optimal + Emergency").formatted(Formatting.YELLOW, Formatting.ITALIC), true);
         case 3 -> player.sendMessage(Text.translatable("Feasting Mode: Regen + Emergency").formatted(Formatting.YELLOW, Formatting.ITALIC), true);
      }
   }
   
   private List<String> makeLore(){
      ArrayList<String> list = new ArrayList<>();
      list.add("{\"text\":\"  Charm of Feasting\\n\\nRarity: Empowered\\n\\nA simple infusion of Arcana can go a long way. Infusing the already slightly magical Enchanted Golden Apple with enough calories to feed a nation for a year lets any food I have on me get \"}");
      list.add("{\"text\":\"  Charm of Feasting\\n\\nautomatically digested as soon as I can fit the full meal. \\n\\nThe charm will feed me any food that isn't magic, Enchanted Golden Apple included. \\nAlso wearing it for a while makes my appetite decrease...\"}");
      return list;
   }
   
   private MagicItemRecipe makeRecipe(){
      MagicItemIngredient god = new MagicItemIngredient(Items.ENCHANTED_GOLDEN_APPLE,1,null, true);
      MagicItemIngredient app = new MagicItemIngredient(Items.GOLDEN_APPLE,32,null);
      MagicItemIngredient car = new MagicItemIngredient(Items.GOLDEN_CARROT,32,null);
      MagicItemIngredient sal = new MagicItemIngredient(Items.COOKED_SALMON,64,null);
      MagicItemIngredient cod = new MagicItemIngredient(Items.COOKED_COD,64,null);
      MagicItemIngredient kel = new MagicItemIngredient(Items.DRIED_KELP,64,null);
      MagicItemIngredient chi = new MagicItemIngredient(Items.COOKED_CHICKEN,64,null);
      MagicItemIngredient rab = new MagicItemIngredient(Items.COOKED_RABBIT,64,null);
      MagicItemIngredient pot = new MagicItemIngredient(Items.BAKED_POTATO,64,null);
      MagicItemIngredient bre = new MagicItemIngredient(Items.BREAD,64,null);
      MagicItemIngredient pie = new MagicItemIngredient(Items.PUMPKIN_PIE,64,null);
      MagicItemIngredient cok = new MagicItemIngredient(Items.COOKIE,64,null);
      MagicItemIngredient mut = new MagicItemIngredient(Items.COOKED_MUTTON,64,null);
      MagicItemIngredient stk = new MagicItemIngredient(Items.COOKED_BEEF,64,null);
      MagicItemIngredient prk = new MagicItemIngredient(Items.COOKED_PORKCHOP,64,null);
      
      MagicItemIngredient[][] ingredients = {
            {sal,cod,car,chi,rab},
            {kel,car,app,car,pot},
            {car,app,god,app,car},
            {bre,car,app,car,prk},
            {cok,pie,car,mut,stk}};
      return new MagicItemRecipe(ingredients);
   }
   
   public class FeastingCharmItem extends MagicPolymerItem {
      public FeastingCharmItem(Settings settings){
         super(getThis(),settings);
      }
      
      
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
      
      @Override
      public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected){
         if(!MagicItemUtils.isMagic(stack)) return;
         if(!(world instanceof ServerWorld && entity instanceof ServerPlayerEntity player)) return;
         
         int mode = stack.getNbt().getCompound("arcananovum").getInt("mode");
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
               if(invItem.isEmpty())
                  continue;
               if(invItem.isFood() && !MagicItemUtils.isMagic(invItem) && invItem.getItem() != Items.ENCHANTED_GOLDEN_APPLE){
                  FoodComponent foodComponent = invItem.getItem().getFoodComponent();
                  availableFoods.add(new Pair<>(i,foodComponent));
                  if(bestFoodInd == -1 ||inv.getStack(bestFoodInd).getItem().getFoodComponent().getHunger() < foodComponent.getHunger()){
                     bestFoodInd = i;
                  }
               }
            }
            if(bestFoodInd != -1){
               ItemStack selectedFood = ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.PICKY_EATER.id) >= 1 ? inv.getStack(bestFoodInd) : inv.getStack(availableFoods.get(0).getFirst());
               FoodComponent foodComponent = selectedFood.getItem().getFoodComponent();
               int foodValue = foodComponent.getHunger();
               
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
                  player.sendMessage(Text.translatable("Your Feasting Charm consumes a "+selectedFood.getName().getString()).formatted(Formatting.GOLD,Formatting.ITALIC),true);
                  hunger.eat(selectedFood.getItem(),selectedFood);
                  player.getHungerManager().add(gluttonyFoodBoost[gluttony],gluttonySatBoost[gluttony]);
                  // Apply Status Effects
                  List<Pair<StatusEffectInstance, Float>> list = foodComponent.getStatusEffects();
                  for (Pair<StatusEffectInstance, Float> pair : list) {
                     if (world.isClient || pair.getFirst() == null || !(world.random.nextFloat() < pair.getSecond().floatValue())) continue;
                     player.addStatusEffect(new StatusEffectInstance(pair.getFirst()));
                  }
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
                  
                  selectedFood.decrement(1);
                  if(selectedFood.getCount() == 0)
                     selectedFood.setNbt(new NbtCompound());
                  SoundUtils.playSongToPlayer(player, SoundEvents.ENTITY_GENERIC_EAT, 1,.7f);
                  PLAYER_DATA.get(player).addXP(50*foodValue); // Add xp
               }
            }
         }
         if(world.getServer().getTicks() % (time*6) == 0){ // Give player a small hunger boost
            player.getHungerManager().add(1,2+gluttony*0.5f);
         }
      }
      
      @Override
      public TypedActionResult<ItemStack> use(World world, PlayerEntity playerEntity, Hand hand) {
         toggleMode((ServerPlayerEntity) playerEntity,playerEntity.getStackInHand(hand));
         return TypedActionResult.success(playerEntity.getStackInHand(hand));
      }
   }
}
