package net.borisshoes.arcananovum.items.charms;

import net.borisshoes.arcananovum.ArcanaConfig;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.core.EnergyItem;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerItem;
import net.borisshoes.arcananovum.events.CleansingCharmEvent;
import net.borisshoes.arcananovum.gui.arcanetome.TomeGui;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaIngredient;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaRecipe;
import net.borisshoes.arcananovum.recipes.arcana.ForgeRequirement;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.borislib.events.Event;
import net.borisshoes.borislib.utils.SoundUtils;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class CleansingCharm extends EnergyItem {
   public static final String ID = "cleansing_charm";
   
   public CleansingCharm(){
      id = ID;
      name = "Charm of Cleansing";
      rarity = ArcanaRarity.EXOTIC;
      categories = new TomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), TomeGui.TomeFilter.ITEMS, TomeGui.TomeFilter.CHARMS};
      itemVersion = 0;
      vanillaItem = Items.PRISMARINE_CRYSTALS;
      item = new CleansingCharmItem();
      displayName = Text.translatableWithFallback("item."+MOD_ID+"."+ID,name).formatted(Formatting.AQUA,Formatting.BOLD);
      researchTasks = new RegistryKey[]{ResearchTasks.MILK_CLEANSE, ResearchTasks.HONEY_CLEANSE, ResearchTasks.EFFECT_POISON, ResearchTasks.EFFECT_NAUSEA, ResearchTasks.EFFECT_BLINDNESS, ResearchTasks.ADVANCEMENT_FURIOUS_COCKTAIL};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getMaxCount());
      putProperty(stack,ACTIVE_TAG, true);
      setPrefStack(stack);
   }
   
   @Override
   public ItemStack updateItem(ItemStack stack, MinecraftServer server){
      boolean active = getBooleanProperty(stack,ACTIVE_TAG);
      ItemStack newStack = super.updateItem(stack,server);
      putProperty(newStack,ACTIVE_TAG,active);
      return buildItemLore(newStack,server);
   }
   
   @Override
   public int getMaxEnergy(ItemStack item){
      int cdLvl = Math.max(0, ArcanaAugments.getAugmentOnItem(item,ArcanaAugments.INFUSED_CHARCOAL.id));
      return 30 - 5*cdLvl;
   }
   
   public void cleanseEffect(ServerPlayerEntity player, ItemStack stack){
      if(!(ArcanaItemUtils.identifyItem(stack) instanceof CleansingCharm)) return;
      if(getEnergy(stack) > 0) return;
      
      List<Map.Entry<RegistryEntry<StatusEffect>, StatusEffectInstance>> canCleanse = new ArrayList<>(player.getActiveStatusEffects().entrySet().stream().filter(entry ->
            entry.getKey().value().getCategory() == StatusEffectCategory.HARMFUL && !entry.getKey().equals(ArcanaRegistry.GREATER_BLINDNESS_EFFECT)
      ).toList());
      Collections.shuffle(canCleanse);
      
      if(canCleanse.size() >= 10){
         ArcanaAchievements.grant(player,ArcanaAchievements.SEPTIC_SHOCK);
      }
      
      int toRemove = ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.ANTIDOTE) > 0 ? 2 : 1;
      for(int i = 0; i < toRemove; i++){
         if(canCleanse.isEmpty()) break;
         RegistryEntry<StatusEffect> effect = canCleanse.removeFirst().getKey();
         player.removeStatusEffect(effect);
         Event.addEvent(new CleansingCharmEvent(player,effect));
         
         if(ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.REJUVENATION) > 0){
            StatusEffectInstance regen = new StatusEffectInstance(StatusEffects.REGENERATION, 100, 1, false, false, true);
            player.addStatusEffect(regen);
         }
         
         if(effect.equals(StatusEffects.HUNGER)){
            ArcanaAchievements.grant(player,ArcanaAchievements.FOOD_POISONT);
         }
         
         setEnergy(stack,getMaxEnergy(stack));
         ArcanaNovum.data(player).addXP(ArcanaConfig.getInt(ArcanaRegistry.CLEANSING_CHARM_CLEANSE));
      }
   }
   
   @Override
   public List<Text> getItemLore(@Nullable ItemStack itemStack){
      List<MutableText> lore = new ArrayList<>();
      lore.add(Text.literal("")
            .append(Text.literal("The ").formatted(Formatting.GRAY))
            .append(Text.literal("charm ").formatted(Formatting.AQUA))
            .append(Text.literal("emanates ").formatted(Formatting.WHITE))
            .append(Text.literal("a smell of ").formatted(Formatting.GRAY))
            .append(Text.literal("freshly washed clothes").formatted(Formatting.WHITE))
            .append(Text.literal(" and ").formatted(Formatting.GRAY))
            .append(Text.literal("clean air").formatted(Formatting.WHITE))
            .append(Text.literal(".").formatted(Formatting.GRAY)));
      lore.add(Text.literal("")
            .append(Text.literal("The ").formatted(Formatting.GRAY))
            .append(Text.literal("charm ").formatted(Formatting.AQUA))
            .append(Text.literal("will periodically ").formatted(Formatting.GRAY))
            .append(Text.literal("remove ").formatted(Formatting.WHITE))
            .append(Text.literal("one ").formatted(Formatting.GRAY))
            .append(Text.literal("negative effect").formatted(Formatting.WHITE))
            .append(Text.literal(".").formatted(Formatting.GRAY)));
      lore.add(Text.literal("")
            .append(Text.literal("Sneak Right Click").formatted(Formatting.WHITE))
            .append(Text.literal(" to toggle the ").formatted(Formatting.GRAY))
            .append(Text.literal("charm's").formatted(Formatting.AQUA))
            .append(Text.literal(" ability.").formatted(Formatting.GRAY)));
      
      return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   protected ArcanaRecipe makeRecipe(){
      ArcanaIngredient a = new ArcanaIngredient(Items.MILK_BUCKET,1);
      ArcanaIngredient b = new ArcanaIngredient(Items.HONEY_BOTTLE,16);
      ArcanaIngredient c = new ArcanaIngredient(Items.CHARCOAL,48);
      ArcanaIngredient g = new ArcanaIngredient(Items.DIAMOND,8);
      ArcanaIngredient h = new ArcanaIngredient(Items.QUARTZ,32);
      ArcanaIngredient m = new ArcanaIngredient(Items.DIAMOND_BLOCK,2);
      
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
      list.add(List.of(Text.literal("Charm of Cleansing").formatted(Formatting.AQUA,Formatting.BOLD),Text.literal("\nRarity: ").formatted(Formatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(),false)),Text.literal("\nBy coalescing the cleansing effects of milk and honey into a pure carbon and silica matrix, I have made their effects renewable. \n\nWhile active, the Charm will cleanse a negative  ").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal("Charm of Cleansing").formatted(Formatting.AQUA,Formatting.BOLD),Text.literal("\neffect when it is applied, or a currently active effect. \n\nThis ability takes about a minute to recharge.\n\nSneak Use the Charm to toggle its effect.\n").formatted(Formatting.BLACK)));
      return list;
   }
   
   public class CleansingCharmItem extends ArcanaPolymerItem {
      public CleansingCharmItem(){
         super(getThis());
      }
      
      @Override
      public ItemStack getPolymerItemStack(ItemStack itemStack, TooltipType tooltipType, PacketContext context){
         ItemStack baseStack = super.getPolymerItemStack(itemStack, tooltipType, context);
         if(!ArcanaItemUtils.isArcane(itemStack)) return baseStack;
         boolean active = getBooleanProperty(itemStack,ACTIVE_TAG);
         
         List<String> stringList = new ArrayList<>();
         if(active){
            stringList.add("on");
         }else{
            stringList.add("off");
         }
         baseStack.set(DataComponentTypes.CUSTOM_MODEL_DATA,new CustomModelDataComponent(new ArrayList<>(),new ArrayList<>(),stringList,new ArrayList<>()));
         return baseStack;
      }
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
      
      @Override
      public void inventoryTick(ItemStack stack, ServerWorld world, Entity entity, @Nullable EquipmentSlot slot){
         if(!ArcanaItemUtils.isArcane(stack)) return;
         if(!(world instanceof ServerWorld && entity instanceof ServerPlayerEntity player)) return;
         
         if(getBooleanProperty(stack,ACTIVE_TAG)) cleanseEffect(player,stack);
         
         if(world.getServer().getTicks() % 20 == 0){
            addEnergy(stack, -1); // Recharge
         }
      }
      
      @Override
      public ActionResult use(World world, PlayerEntity playerEntity, Hand hand){
         ItemStack stack = playerEntity.getStackInHand(hand);
         if(!(playerEntity instanceof ServerPlayerEntity player)) return ActionResult.PASS;
         
         if(player.isSneaking()){
            boolean active = !getBooleanProperty(stack,ACTIVE_TAG);
            putProperty(stack,ACTIVE_TAG,active);
            
            if(active){
               player.sendMessage(Text.literal("The Charm glows with iridescence").formatted(Formatting.AQUA,Formatting.ITALIC),true);
               SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_BEACON_POWER_SELECT, 0.5f,2f);
            }else{
               player.sendMessage(Text.literal("The Charm's glow fades").formatted(Formatting.AQUA,Formatting.ITALIC),true);
               SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_BEACON_DEACTIVATE, 0.5f,.8f);
            }
            
            return ActionResult.SUCCESS_SERVER;
         }
         
         return ActionResult.PASS;
      }
   }
}
