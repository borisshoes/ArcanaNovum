package net.borisshoes.arcananovum.items.charms;

import net.borisshoes.arcananovum.ArcanaConfig;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerItem;
import net.borisshoes.arcananovum.gui.arcanetome.ArcaneTomeGui;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.borislib.utils.TextUtils;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
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
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomModelData;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

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
      displayName = Component.translatableWithFallback("item." + MOD_ID + "." + ID, name).withStyle(ChatFormatting.BOLD, ChatFormatting.GOLD);
      researchTasks = new ResourceKey[]{ResearchTasks.ADVANCEMENT_BARTER_PIGLIN, ResearchTasks.ADVANCEMENT_FIND_BASTION, ResearchTasks.ADVANCEMENT_TRADE};
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
      list.add(List.of(Component.literal("      Charm of\n     Negotiation").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), Component.literal("\nRarity: ").withStyle(ChatFormatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(), false)), Component.literal("\nVillagers and Piglins have one thing in common, they strike a hard bargain. This gilded emerald lapel should soften that bargain. They will be so enamoured with its ").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("      Charm of\n     Negotiation").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), Component.literal("\ndisplay of wealth that they won't notice the subtle Arcana warping their minds.\n\nVillagers under the Charm's influence will give me high discounts and will 'forgive' a transgression or two. ").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("      Charm of\n     Negotiation").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), Component.literal("\nPiglins that barter near the influence of the Charm will see my gold as more valuable and give more items in return, in addition to being neutral to my presence.").withStyle(ChatFormatting.BLACK)));
      return list;
   }
   
   // Normal override in item class doesn't work because tamed animals consume the item interaction
   public InteractionResult useOnEntity(Player user, LivingEntity entity, InteractionHand hand){
      ItemStack stack = user.getItemInHand(hand);
      boolean canForceRestock = ArcanaAugments.getAugmentOnItem(stack, ArcanaAugments.EXTORTION) > 0;
      if(canForceRestock && entity instanceof Villager villager && villager.level() instanceof ServerLevel serverLevel){
         villager.restock();
         double villagerWidth = villager.getBbWidth();
         double villagerHeight = villager.getBbHeight();
         serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, villager.getX(), villager.getY() + villagerHeight / 2.0, villager.getZ(), 25, villagerWidth / 2, villagerHeight / 4, villagerWidth / 2, 1);
         return InteractionResult.SUCCESS_SERVER;
      }
      return InteractionResult.PASS;
   }
   
   public class NegotiationCharmItem extends ArcanaPolymerItem {
      public NegotiationCharmItem(){
         super(getThis());
      }
      
      @Override
      public ItemStack getPolymerItemStack(ItemStack itemStack, TooltipFlag tooltipType, PacketContext context, HolderLookup.Provider lookup){
         ItemStack baseStack = super.getPolymerItemStack(itemStack, tooltipType, context, lookup);
         if(!ArcanaItemUtils.isArcane(itemStack)) return baseStack;
         
         List<String> stringList = new ArrayList<>();
         if(ArcanaAugments.getAugmentOnItem(itemStack, ArcanaAugments.EXTORTION) >= 1){
            stringList.add("extortion");
         }
         baseStack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(new ArrayList<>(), new ArrayList<>(), stringList, new ArrayList<>()));
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
            double baseRange = ArcanaNovum.CONFIG.getDouble(ArcanaConfig.NEGOTIATION_CHARM_RANGE);
            for(Villager villager : world.getEntitiesOfClass(Villager.class, player.getBoundingBox().inflate(baseRange))){
               int reputation = villager.getPlayerReputation(player);
               if(reputation < 100){
                  world.onReputationEvent(ReputationEventType.ZOMBIE_VILLAGER_CURED, player, villager);
                  world.onReputationEvent(ReputationEventType.TRADE, player, villager);
                  ArcanaNovum.data(player).addXP(ArcanaNovum.CONFIG.getInt(ArcanaConfig.XP_NEGOTIATION_CHARM_INFLUENCE));
               }
            }
         }
      }
   }
}

