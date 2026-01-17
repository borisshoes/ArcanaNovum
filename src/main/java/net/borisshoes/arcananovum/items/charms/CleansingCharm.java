package net.borisshoes.arcananovum.items.charms;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.core.EnergyItem;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerItem;
import net.borisshoes.arcananovum.events.CleansingCharmEvent;
import net.borisshoes.arcananovum.gui.arcanetome.ArcaneTomeGui;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.borislib.events.Event;
import net.borisshoes.borislib.utils.SoundUtils;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
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
      categories = new ArcaneTomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), ArcaneTomeGui.TomeFilter.ITEMS, ArcaneTomeGui.TomeFilter.CHARMS};
      itemVersion = 0;
      vanillaItem = Items.PRISMARINE_CRYSTALS;
      item = new CleansingCharmItem();
      displayName = Component.translatableWithFallback("item."+MOD_ID+"."+ID,name).withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD);
      researchTasks = new ResourceKey[]{ResearchTasks.MILK_CLEANSE, ResearchTasks.HONEY_CLEANSE, ResearchTasks.EFFECT_POISON, ResearchTasks.EFFECT_NAUSEA, ResearchTasks.EFFECT_BLINDNESS, ResearchTasks.ADVANCEMENT_FURIOUS_COCKTAIL};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getDefaultMaxStackSize());
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
   
   public void cleanseEffect(ServerPlayer player, ItemStack stack){
      if(!(ArcanaItemUtils.identifyItem(stack) instanceof CleansingCharm)) return;
      if(getEnergy(stack) > 0) return;
      
      List<Map.Entry<Holder<MobEffect>, MobEffectInstance>> canCleanse = new ArrayList<>(player.getActiveEffectsMap().entrySet().stream().filter(entry ->
            entry.getKey().value().getCategory() == MobEffectCategory.HARMFUL && !entry.getKey().equals(ArcanaRegistry.GREATER_BLINDNESS_EFFECT)
      ).toList());
      Collections.shuffle(canCleanse);
      
      if(canCleanse.size() >= 10){
         ArcanaAchievements.grant(player,ArcanaAchievements.SEPTIC_SHOCK);
      }
      
      int toRemove = ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.ANTIDOTE) > 0 ? 2 : 1;
      for(int i = 0; i < toRemove; i++){
         if(canCleanse.isEmpty()) break;
         Holder<MobEffect> effect = canCleanse.removeFirst().getKey();
         player.removeEffect(effect);
         Event.addEvent(new CleansingCharmEvent(player,effect));
         
         if(ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.REJUVENATION) > 0){
            MobEffectInstance regen = new MobEffectInstance(MobEffects.REGENERATION, 100, 1, false, false, true);
            player.addEffect(regen);
         }
         
         if(effect.equals(MobEffects.HUNGER)){
            ArcanaAchievements.grant(player,ArcanaAchievements.FOOD_POISONT);
         }
         
         setEnergy(stack,getMaxEnergy(stack));
         ArcanaNovum.data(player).addXP(ArcanaNovum.CONFIG.getInt(ArcanaRegistry.XP_CLEANSING_CHARM_CLEANSE));
      }
   }
   
   @Override
   public List<Component> getItemLore(@Nullable ItemStack itemStack){
      List<MutableComponent> lore = new ArrayList<>();
      lore.add(Component.literal("")
            .append(Component.literal("The ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("charm ").withStyle(ChatFormatting.AQUA))
            .append(Component.literal("emanates ").withStyle(ChatFormatting.WHITE))
            .append(Component.literal("a smell of ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("freshly washed clothes").withStyle(ChatFormatting.WHITE))
            .append(Component.literal(" and ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("clean air").withStyle(ChatFormatting.WHITE))
            .append(Component.literal(".").withStyle(ChatFormatting.GRAY)));
      lore.add(Component.literal("")
            .append(Component.literal("The ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("charm ").withStyle(ChatFormatting.AQUA))
            .append(Component.literal("will periodically ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("remove ").withStyle(ChatFormatting.WHITE))
            .append(Component.literal("one ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("negative effect").withStyle(ChatFormatting.WHITE))
            .append(Component.literal(".").withStyle(ChatFormatting.GRAY)));
      lore.add(Component.literal("")
            .append(Component.literal("Sneak Right Click").withStyle(ChatFormatting.WHITE))
            .append(Component.literal(" to toggle the ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("charm's").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(" ability.").withStyle(ChatFormatting.GRAY)));
      
      return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   public List<List<Component>> getBookLore(){
      List<List<Component>> list = new ArrayList<>();
      list.add(List.of(Component.literal("Charm of Cleansing").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD), Component.literal("\nRarity: ").withStyle(ChatFormatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(),false)), Component.literal("\nBy coalescing the cleansing effects of milk and honey into a pure carbon and silica matrix, I have made their effects renewable. \n\nWhile active, the Charm will cleanse a negative  ").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("Charm of Cleansing").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD), Component.literal("\neffect when it is applied, or a currently active effect. \n\nThis ability takes about a minute to recharge.\n\nSneak Use the Charm to toggle its effect.\n").withStyle(ChatFormatting.BLACK)));
      return list;
   }
   
   public class CleansingCharmItem extends ArcanaPolymerItem {
      public CleansingCharmItem(){
         super(getThis());
      }
      
      @Override
      public ItemStack getPolymerItemStack(ItemStack itemStack, TooltipFlag tooltipType, PacketContext context){
         ItemStack baseStack = super.getPolymerItemStack(itemStack, tooltipType, context);
         if(!ArcanaItemUtils.isArcane(itemStack)) return baseStack;
         boolean active = getBooleanProperty(itemStack,ACTIVE_TAG);
         
         List<String> stringList = new ArrayList<>();
         if(active){
            stringList.add("on");
         }else{
            stringList.add("off");
         }
         baseStack.set(DataComponents.CUSTOM_MODEL_DATA,new CustomModelData(new ArrayList<>(),new ArrayList<>(),stringList,new ArrayList<>()));
         return baseStack;
      }
      
      @Override
      public ItemStack getDefaultInstance(){
         return prefItem;
      }
      
      @Override
      public void inventoryTick(@NonNull ItemStack stack, @NonNull ServerLevel world, @NonNull Entity entity, @Nullable EquipmentSlot slot){
         if(!ArcanaItemUtils.isArcane(stack)) return;
         if(!(entity instanceof ServerPlayer player)) return;
         
         if(getBooleanProperty(stack,ACTIVE_TAG)) cleanseEffect(player,stack);
         
         if(world.getServer().getTickCount() % 20 == 0){
            addEnergy(stack, -1); // Recharge
         }
      }
      
      @Override
      public @NonNull InteractionResult use(@NonNull Level world, Player playerEntity, @NonNull InteractionHand hand){
         ItemStack stack = playerEntity.getItemInHand(hand);
         if(!(playerEntity instanceof ServerPlayer player)) return InteractionResult.PASS;
         
         if(player.isShiftKeyDown()){
            boolean active = !getBooleanProperty(stack,ACTIVE_TAG);
            putProperty(stack,ACTIVE_TAG,active);
            
            if(active){
               player.displayClientMessage(Component.literal("The Charm glows with iridescence").withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC),true);
               SoundUtils.playSongToPlayer(player, SoundEvents.BEACON_POWER_SELECT, 0.5f,2f);
            }else{
               player.displayClientMessage(Component.literal("The Charm's glow fades").withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC),true);
               SoundUtils.playSongToPlayer(player, SoundEvents.BEACON_DEACTIVATE, 0.5f,.8f);
            }
            
            return InteractionResult.SUCCESS_SERVER;
         }
         
         return InteractionResult.PASS;
      }
   }
}
