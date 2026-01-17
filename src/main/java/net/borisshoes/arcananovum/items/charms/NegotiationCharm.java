package net.borisshoes.arcananovum.items.charms;

import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerItem;
import net.borisshoes.arcananovum.gui.arcanetome.ArcaneTomeGui;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.village.ReputationEventType;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class NegotiationCharm extends ArcanaItem {
   public static final String ID = "negotiation_charm";
   
   public NegotiationCharm(){
      id = ID;
      name = "Negotiation Charm";
      rarity = ArcanaRarity.EMPOWERED;
      categories = new ArcaneTomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), ArcaneTomeGui.TomeFilter.ITEMS, ArcaneTomeGui.TomeFilter.CHARMS};
      itemVersion = 0;
      vanillaItem = Items.YELLOW_DYE;
      item = new NegotiationCharmItem();
      displayName = Component.translatableWithFallback("item."+MOD_ID+"."+ID,name).withStyle(ChatFormatting.BOLD, ChatFormatting.GOLD);
      researchTasks = new ResourceKey[]{};  // TODO
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getDefaultMaxStackSize());
      //putProperty(stack,TAG,);
      setPrefStack(stack);
   }
   
   @Override
   public List<Component> getItemLore(@Nullable ItemStack itemStack){
      List<MutableComponent> lore = new ArrayList<>();
      lore.add(Component.literal("")
            .append(Component.literal("An ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("emerald").withStyle(ChatFormatting.GREEN))
            .append(Component.literal(" lapel pin ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("glistens ").withStyle(ChatFormatting.GOLD))
            .append(Component.literal("on a ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("golden flower").withStyle(ChatFormatting.GOLD))
            .append(Component.literal(".").withStyle(ChatFormatting.YELLOW)));
      lore.add(Component.literal("")
            .append(Component.literal("Those that would do ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("business ").withStyle(ChatFormatting.GOLD))
            .append(Component.literal("offer ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("better deals").withStyle(ChatFormatting.GREEN))
            .append(Component.literal(".").withStyle(ChatFormatting.YELLOW)));
      lore.add(Component.literal("")
            .append(Component.literal("The display of ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("wealth ").withStyle(ChatFormatting.GREEN))
            .append(Component.literal("impresses ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("Piglins").withStyle(ChatFormatting.GOLD))
            .append(Component.literal(".").withStyle(ChatFormatting.YELLOW)));
      
      return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   public List<List<Component>> getBookLore(){
      List<List<Component>> list = new ArrayList<>();
      list.add(List.of(Component.literal("TODO").withStyle(ChatFormatting.BLACK))); // TODO
      return list;
   }
   
   // Normal override in item class doesn't work because tamed animals consume the item interaction
   public InteractionResult useOnEntity(Player user, LivingEntity entity, InteractionHand hand){
      ItemStack stack = user.getItemInHand(hand);
      boolean canForceRestock = ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.EXTORTION) > 0;
      if(canForceRestock && entity instanceof Villager villager && villager.level() instanceof ServerLevel serverLevel){
         villager.restock();
         double villagerWidth = villager.getBbWidth();
         double villagerHeight = villager.getBbHeight();
         serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,villager.getX(),villager.getY()+villagerHeight/2.0,villager.getZ(),25,villagerWidth/2,villagerHeight/4,villagerWidth/2,1);
         return InteractionResult.SUCCESS_SERVER;
      }
      return InteractionResult.PASS;
   }
   
   public class NegotiationCharmItem extends ArcanaPolymerItem {
      public NegotiationCharmItem(){
         super(getThis());
      }
      
      @Override
      public ItemStack getPolymerItemStack(ItemStack itemStack, TooltipFlag tooltipType, PacketContext context){
         ItemStack baseStack = super.getPolymerItemStack(itemStack, tooltipType, context);
         if(!ArcanaItemUtils.isArcane(itemStack)) return baseStack;
         
         List<String> stringList = new ArrayList<>();
         if(ArcanaAugments.getAugmentOnItem(itemStack,ArcanaAugments.EXTORTION) >= 1){
            stringList.add("extortion");
         }
         baseStack.set(DataComponents.CUSTOM_MODEL_DATA,new CustomModelData(new ArrayList<>(),new ArrayList<>(),stringList,new ArrayList<>()));
         return baseStack;
      }
      
      @Override
      public @NonNull ItemStack getDefaultInstance(){
         return prefItem;
      }
      
      @Override
      public void inventoryTick(@NonNull ItemStack stack, @NonNull ServerLevel world, @NonNull Entity entity, @Nullable EquipmentSlot slot){
         if(!ArcanaItemUtils.isArcane(stack)) return;
         if(!(entity instanceof ServerPlayer player)) return;
         
         if(world.getServer().getTickCount() % 100 == 0){
            for(Villager villager : world.getEntitiesOfClass(Villager.class, player.getBoundingBox().inflate(25))){
               int reputation = villager.getPlayerReputation(player);
               if(reputation < 100){
                  world.onReputationEvent(ReputationEventType.ZOMBIE_VILLAGER_CURED, player, villager);
                  world.onReputationEvent(ReputationEventType.TRADE, player, villager);
               }
            }
         }
      }
   }
}

