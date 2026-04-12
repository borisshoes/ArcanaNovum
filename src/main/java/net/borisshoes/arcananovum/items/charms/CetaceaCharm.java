package net.borisshoes.arcananovum.items.charms;

import net.borisshoes.arcananovum.ArcanaConfig;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.blocks.GeomanticStele;
import net.borisshoes.arcananovum.blocks.GeomanticSteleBlockEntity;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerItem;
import net.borisshoes.arcananovum.gui.arcanetome.ArcaneTomeGui;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.borislib.utils.SoundUtils;
import net.borisshoes.borislib.utils.TextUtils;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Tuple;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class CetaceaCharm extends ArcanaItem implements GeomanticStele.Interaction {
   public static final String ID = "cetacea_charm";
   
   public CetaceaCharm(){
      id = ID;
      name = "Charm of Cetacea";
      rarity = ArcanaRarity.EMPOWERED;
      categories = new ArcaneTomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), ArcaneTomeGui.TomeFilter.ITEMS, ArcaneTomeGui.TomeFilter.CHARMS};
      itemVersion = 0;
      vanillaItem = Items.COD;
      item = new CetaceaCharmItem();
      displayName = Component.translatableWithFallback("item." + MOD_ID + "." + ID, name).withStyle(ChatFormatting.BLUE, ChatFormatting.BOLD);
      researchTasks = new ResourceKey[]{ResearchTasks.OBTAIN_CONDUIT, ResearchTasks.CATCH_FISH, ResearchTasks.EFFECT_DOLPHINS_GRACE, ResearchTasks.DROWNING_DAMAGE};
      attributions = new Tuple[]{new Tuple<>(Component.translatable("credits_and_attribution.arcananovum.texture_by"), Component.literal("Rookodzol"))};
   }
   
   @Override
   public ItemStack initializeArcanaTag(ItemStack stack){
      super.initializeArcanaTag(stack);
      putProperty(stack, ACTIVE_TAG, true);
      return stack;
   }
   
   @Override
   public ItemStack updateItem(ItemStack stack, MinecraftServer server){
      boolean active = getBooleanProperty(stack, ACTIVE_TAG);
      ItemStack newStack = super.updateItem(stack, server);
      putProperty(newStack, ACTIVE_TAG, active);
      return buildItemLore(newStack, server);
   }
   
   @Override
   public List<Component> getItemLore(@Nullable ItemStack itemStack){
      List<MutableComponent> lore = new ArrayList<>();
      lore.add(Component.literal("")
            .append(Component.literal("The ").withStyle(ChatFormatting.AQUA))
            .append(Component.literal("charm ").withStyle(ChatFormatting.BLUE))
            .append(Component.literal("is ").withStyle(ChatFormatting.AQUA))
            .append(Component.literal("slippery ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("and ").withStyle(ChatFormatting.AQUA))
            .append(Component.literal("wet ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("like it just went for a ").withStyle(ChatFormatting.AQUA))
            .append(Component.literal("swim").withStyle(ChatFormatting.BLUE))
            .append(Component.literal(".").withStyle(ChatFormatting.AQUA)));
      lore.add(Component.literal("")
            .append(Component.literal("Wearing the ").withStyle(ChatFormatting.AQUA))
            .append(Component.literal("charm ").withStyle(ChatFormatting.BLUE))
            .append(Component.literal("gives you the grace of a ").withStyle(ChatFormatting.AQUA))
            .append(Component.literal("dolphin ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("when in the ").withStyle(ChatFormatting.AQUA))
            .append(Component.literal("water").withStyle(ChatFormatting.BLUE))
            .append(Component.literal(".").withStyle(ChatFormatting.AQUA)));
      lore.add(Component.literal("")
            .append(Component.literal("Sneak Right Click").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal(" to ").withStyle(ChatFormatting.AQUA))
            .append(Component.literal("disable ").withStyle(ChatFormatting.BLUE))
            .append(Component.literal("the ").withStyle(ChatFormatting.AQUA))
            .append(Component.literal("charm's").withStyle(ChatFormatting.BLUE))
            .append(Component.literal(" effect.").withStyle(ChatFormatting.AQUA)));
      
      return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   public List<List<Component>> getBookLore(){
      List<List<Component>> list = new ArrayList<>();
      list.add(List.of(Component.literal(" Charm of Cetacea").withStyle(ChatFormatting.BLUE, ChatFormatting.BOLD), Component.literal("\nRarity: ").withStyle(ChatFormatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(), false)), Component.literal("\nDolphins are such graceful creatures, and this conduit that I have reconstructed opens up some possibilities. I believe I can miniaturize the conduit such that it gives me similar aquatic  ").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal(" Charm of Cetacea").withStyle(ChatFormatting.BLUE, ChatFormatting.BOLD), Component.literal("\nmaneuverability as dolphins.\n\nSneak Using the Charm toggles the aquatic buffs.\n").withStyle(ChatFormatting.BLACK)));
      
      return list;
   }
   
   @Override
   public void steleTick(ServerLevel world, GeomanticSteleBlockEntity stele, ItemStack stack, Vec3 range){
      AABB box = new AABB(stele.getBlockPos().getCenter().subtract(range), stele.getBlockPos().getCenter().add(range));
      List<LivingEntity> inRangeEntities = world.getEntitiesOfClass(LivingEntity.class, box);
      boolean delphinidae = ArcanaAugments.getAugmentOnItem(stack, ArcanaAugments.DELPHINIDAE) > 0;
      boolean gills = ArcanaAugments.getAugmentOnItem(stack, ArcanaAugments.GILLS) > 0;
      for(LivingEntity living : inRangeEntities){
         if(living.isSpectator()) continue;
         if(living.isInWater()){
            MobEffectInstance grace = new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 110, delphinidae ? 1 : 0, false, false, true);
            living.addEffect(grace);
            
            if(living instanceof ServerPlayer player && world.getServer().getTickCount() % 20 == 0){
               ArcanaNovum.data(player).addXP(ArcanaNovum.CONFIG.getInt(ArcanaConfig.XP_CETACEA_CHARM_PER_SECOND));
               stele.giveXP(ArcanaNovum.CONFIG.getInt(ArcanaConfig.XP_CETACEA_CHARM_PER_SECOND));
            }
         }
         if(living.isUnderWater() && gills){
            MobEffectInstance waterBreath = new MobEffectInstance(MobEffects.WATER_BREATHING, 110, 0, false, false, true);
            living.addEffect(waterBreath);
         }
      }
      
      if(world.getRandom().nextFloat() < 0.15){
         Vec3 stackPos = stele.getBlockPos().getCenter().add(0, 1, 0);
         world.sendParticles(ParticleTypes.NAUTILUS, stackPos.x(), stackPos.y() + 1, stackPos.z(), 5, 0.25, 0.25, 0.25, 1);
         world.sendParticles(ParticleTypes.DRIPPING_WATER, stackPos.x(), stackPos.y(), stackPos.z(), 5, 0.25, 0.25, 0.25, 1);
      }
   }
   
   @Override
   public Vec3 getBaseRange(){
      return new Vec3(20, 20, 20);
   }
   
   public class CetaceaCharmItem extends ArcanaPolymerItem {
      public CetaceaCharmItem(){
         super(getThis());
      }
      
      @Override
      public ItemStack getPolymerItemStack(ItemStack itemStack, TooltipFlag tooltipType, PacketContext context, HolderLookup.Provider lookup){
         ItemStack baseStack = super.getPolymerItemStack(itemStack, tooltipType, context, lookup);
         if(!ArcanaItemUtils.isArcane(itemStack)) return baseStack;
         boolean active = getBooleanProperty(itemStack, ACTIVE_TAG);
         boolean delphinidae = ArcanaAugments.getAugmentOnItem(itemStack, ArcanaAugments.DELPHINIDAE) > 0;
         
         List<String> stringList = new ArrayList<>();
         if(active){
            if(delphinidae){
               stringList.add("delphinidae_on");
            }else{
               stringList.add("on");
            }
         }else{
            if(delphinidae){
               stringList.add("delphinidae_off");
            }else{
               stringList.add("off");
            }
         }
         baseStack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(new ArrayList<>(), new ArrayList<>(), stringList, new ArrayList<>()));
         return baseStack;
      }
      
      @Override
      public ItemStack getDefaultInstance(){
         return prefItem;
      }
      
      @Override
      public void inventoryTick(ItemStack stack, ServerLevel world, Entity entity, @Nullable EquipmentSlot slot){
         if(!ArcanaItemUtils.isArcane(stack)) return;
         if(!(entity instanceof ServerPlayer player)) return;
         
         boolean active = getBooleanProperty(stack, ACTIVE_TAG);
         boolean delphinidae = ArcanaAugments.getAugmentOnItem(stack, ArcanaAugments.DELPHINIDAE) > 0;
         boolean gills = ArcanaAugments.getAugmentOnItem(stack, ArcanaAugments.GILLS) > 0;
         
         if(active){
            if(player.isInWater()){
               MobEffectInstance grace = new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 110, delphinidae ? 1 : 0, false, false, true);
               player.addEffect(grace);
               
               if(world.getServer().getTickCount() % 20 == 0){
                  ArcanaNovum.data(player).addXP(ArcanaNovum.CONFIG.getInt(ArcanaConfig.XP_CETACEA_CHARM_PER_SECOND));
               }
            }
            if(player.isUnderWater() && gills){
               MobEffectInstance waterBreath = new MobEffectInstance(MobEffects.WATER_BREATHING, 110, 0, false, false, true);
               player.addEffect(waterBreath);
            }
         }
      }
      
      @Override
      public InteractionResult use(Level world, Player playerEntity, InteractionHand hand){
         ItemStack stack = playerEntity.getItemInHand(hand);
         if(!(playerEntity instanceof ServerPlayer player)) return InteractionResult.PASS;
         
         if(player.isShiftKeyDown()){
            boolean active = !getBooleanProperty(stack, ACTIVE_TAG);
            putProperty(stack, ACTIVE_TAG, active);
            
            if(active){
               player.sendSystemMessage(Component.literal("The Charm moistens").withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC), true);
               SoundUtils.playSongToPlayer(player, SoundEvents.PUFFER_FISH_DEATH, 0.5f, 0.8f);
            }else{
               player.sendSystemMessage(Component.literal("The Charm dries out").withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC), true);
               SoundUtils.playSongToPlayer(player, SoundEvents.PUFFER_FISH_BLOW_OUT, 0.5f, 0.7f);
            }
            
            return InteractionResult.SUCCESS_SERVER;
         }
         
         return InteractionResult.PASS;
      }
   }
}
