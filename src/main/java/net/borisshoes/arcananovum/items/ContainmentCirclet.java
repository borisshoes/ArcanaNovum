package net.borisshoes.arcananovum.items;

import com.mojang.logging.LogUtils;
import net.borisshoes.arcananovum.ArcanaConfig;
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
import net.borisshoes.borislib.BorisLib;
import net.borisshoes.borislib.utils.SoundUtils;
import net.borisshoes.borislib.utils.TextUtils;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class ContainmentCirclet extends ArcanaItem {
   public static final String ID = "containment_circlet";
   
   public static final String CONTENTS_TAG = "contents";
   public static final String HP_TAG = "hp";
   public static final String MAX_HP_TAG = "maxHP";
   
   public ContainmentCirclet(){
      id = ID;
      name = "Containment Circlet";
      rarity = ArcanaRarity.EMPOWERED;
      categories = new ArcaneTomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), ArcaneTomeGui.TomeFilter.ITEMS};
      itemVersion = 0;
      vanillaItem = Items.HEART_OF_THE_SEA;
      item = new ContainmentCircletItem();
      displayName = Component.translatableWithFallback("item." + MOD_ID + "." + ID, name).withStyle(ChatFormatting.BOLD, ChatFormatting.DARK_AQUA);
      researchTasks = new ResourceKey[]{ResearchTasks.ADVANCEMENT_TAME_AN_ANIMAL, ResearchTasks.USE_ENDER_CHEST};
   }
   
   @Override
   public ItemStack initializeArcanaTag(ItemStack stack){
      super.initializeArcanaTag(stack);
      putProperty(stack, CONTENTS_TAG, new CompoundTag());
      putProperty(stack, HP_TAG, 0.0f);
      putProperty(stack, MAX_HP_TAG, 0.0f);
      return stack;
   }
   
   @Override
   public List<Component> getItemLore(@Nullable ItemStack itemStack){
      List<MutableComponent> lore = new ArrayList<>();
      lore.add(Component.literal("")
            .append(Component.literal("Animals ").withStyle(ChatFormatting.DARK_GREEN))
            .append(Component.literal("often have a ").withStyle(ChatFormatting.GREEN))
            .append(Component.literal("mind of their own").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(" They must be ").withStyle(ChatFormatting.GREEN))
            .append(Component.literal("contained").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal(".").withStyle(ChatFormatting.GREEN)));
      lore.add(Component.literal("")
            .append(Component.literal("The Circlet").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal(" stores animals").withStyle(ChatFormatting.DARK_GREEN))
            .append(Component.literal(" for safe ").withStyle(ChatFormatting.GREEN))
            .append(Component.literal("keeping ").withStyle(ChatFormatting.AQUA))
            .append(Component.literal("and easy ").withStyle(ChatFormatting.GREEN))
            .append(Component.literal("transport").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(".").withStyle(ChatFormatting.GREEN)));
      lore.add(Component.literal("")
            .append(Component.literal("Right Click").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal(" a ").withStyle(ChatFormatting.GREEN))
            .append(Component.literal("passive animal").withStyle(ChatFormatting.DARK_GREEN))
            .append(Component.literal(" to ").withStyle(ChatFormatting.GREEN))
            .append(Component.literal("contain ").withStyle(ChatFormatting.AQUA))
            .append(Component.literal("them.").withStyle(ChatFormatting.GREEN)));
      lore.add(Component.literal("")
            .append(Component.literal("Right Click").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal(" again to ").withStyle(ChatFormatting.GREEN))
            .append(Component.literal("release ").withStyle(ChatFormatting.AQUA))
            .append(Component.literal("them.").withStyle(ChatFormatting.GREEN)));
      lore.add(Component.literal(""));
      
      
      boolean hasCreature = false;
      if(itemStack != null){
         CompoundTag contents = getCompoundProperty(itemStack, CONTENTS_TAG);
         int hp = (int) getFloatProperty(itemStack, HP_TAG);
         int maxHp = (int) getFloatProperty(itemStack, MAX_HP_TAG);
         
         if(BorisLib.SERVER != null){
            try(ProblemReporter.ScopedCollector logging = new ProblemReporter.ScopedCollector(LogUtils.getLogger())){
               ValueInput readView = TagValueInput.create(logging, BorisLib.SERVER.registryAccess(), contents);
               Optional<EntityType<?>> entity = EntityType.by(readView);
               if(!contents.isEmpty() && entity.isPresent()){
                  String entityTypeName = entity.get().getDescription().getString();
                  
                  lore.add(Component.literal("")
                        .append(Component.literal("Contains").withStyle(ChatFormatting.DARK_AQUA))
                        .append(Component.literal(" - ").withStyle(ChatFormatting.AQUA))
                        .append(Component.literal(entityTypeName + " (" + hp + "/" + maxHp + ")").withStyle(ChatFormatting.GREEN)));
                  hasCreature = true;
               }
            }
         }
      }
      
      if(!hasCreature){
         lore.add(Component.literal("")
               .append(Component.literal("Contains").withStyle(ChatFormatting.DARK_AQUA))
               .append(Component.literal(" - ").withStyle(ChatFormatting.AQUA))
               .append(Component.literal("Nothing").withStyle(ChatFormatting.GREEN)));
      }
      
      return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   public ItemStack updateItem(ItemStack stack, MinecraftServer server){
      CompoundTag contents = getCompoundProperty(stack, CONTENTS_TAG);
      float hp = getFloatProperty(stack, HP_TAG);
      float maxHp = getFloatProperty(stack, MAX_HP_TAG);
      ItemStack newStack = super.updateItem(stack, server);
      putProperty(newStack, CONTENTS_TAG, contents);
      putProperty(newStack, HP_TAG, hp);
      putProperty(newStack, MAX_HP_TAG, maxHp);
      return buildItemLore(newStack, server);
   }
   
   // Normal override in item class doesn't work because tamed animals consume the item interaction
   public InteractionResult useOnEntity(Player user, LivingEntity entity, InteractionHand hand){
      if(!(user instanceof ServerPlayer player)) return InteractionResult.PASS;
      ItemStack stack = player.getItemInHand(hand);
      if(!ArcanaItemUtils.isArcane(stack)) return InteractionResult.PASS;
      CompoundTag contents = getCompoundProperty(stack, CONTENTS_TAG);
      
      if(!contents.isEmpty()){
         player.sendSystemMessage(Component.literal("The Circlet is occupied").withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.ITALIC), true);
         SoundUtils.playSongToPlayer(player, SoundEvents.FIRE_EXTINGUISH, 1, .5f);
         return InteractionResult.SUCCESS_SERVER;
      }
      if(entity.is(ArcanaRegistry.CONTAINMENT_CIRCLET_DISALLOWED) || entity.isDeadOrDying()){
         player.sendSystemMessage(Component.literal("The Circlet cannot contain this creature").withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.ITALIC), true);
         SoundUtils.playSongToPlayer(player, SoundEvents.FIRE_EXTINGUISH, 1, .5f);
         return InteractionResult.SUCCESS_SERVER;
      }
      
      boolean hostiles = ArcanaAugments.getAugmentOnItem(stack, ArcanaAugments.CONFINEMENT) > 0;
      
      if(entity instanceof Enemy && !hostiles){
         player.sendSystemMessage(Component.literal("This Circlet cannot capture hostile creatures").withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.ITALIC), true);
         SoundUtils.playSongToPlayer(player, SoundEvents.FIRE_EXTINGUISH, 1, .5f);
      }else if(entity instanceof Mob){
         try(ProblemReporter.ScopedCollector logging = new ProblemReporter.ScopedCollector(entity.problemPath(), LogUtils.getLogger())){
            TagValueOutput writeView = TagValueOutput.createWithContext(logging, entity.level().registryAccess());
            entity.saveWithoutId(writeView);
            CompoundTag data = writeView.buildResult();
            data.putString("id", EntityType.getKey(entity.getType()).toString());
            putProperty(stack, CONTENTS_TAG, data);
            putProperty(stack, HP_TAG, entity.getHealth());
            putProperty(stack, MAX_HP_TAG, entity.getMaxHealth());
            entity.discard();
            player.sendSystemMessage(Component.literal("The Circlet contains the creature").withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.ITALIC), true);
            SoundUtils.playSongToPlayer(player, SoundEvents.FIRECHARGE_USE, 1, 1.5f);
            ArcanaNovum.data(player).addXP(ArcanaNovum.CONFIG.getInt(ArcanaConfig.XP_CONTAINMENT_CIRCLET_USE)); // Add xp
            buildItemLore(stack, player.level().getServer());
         }
      }
      
      return InteractionResult.SUCCESS_SERVER;
   }
   
   @Override
   public List<List<Component>> getBookLore(){
      List<List<Component>> list = new ArrayList<>();
      list.add(List.of(Component.literal("Containment Circlet").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD), Component.literal("\nRarity: ").withStyle(ChatFormatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(), false)), Component.literal("\nPets are amazing companions. They’re also masochistic idiots with a love for getting into trouble. If only I had some sort of pocket ball, a pokeb… a Containment Circlet to keep them safe.").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("Containment Circlet").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD), Component.literal("\nUsing the Circlet on a passive or tamed mob captures it.\n\nUsing the Circlet again releases the creature.\n").withStyle(ChatFormatting.BLACK)));
      return list;
   }
   
   public class ContainmentCircletItem extends ArcanaPolymerItem {
      public ContainmentCircletItem(){
         super(getThis());
      }
      
      @Override
      public ItemStack getPolymerItemStack(ItemStack itemStack, TooltipFlag tooltipType, PacketContext context, HolderLookup.Provider lookup){
         ItemStack baseStack = super.getPolymerItemStack(itemStack, tooltipType, context, lookup);
         if(!ArcanaItemUtils.isArcane(itemStack)) return baseStack;
         
         CompoundTag contents = getCompoundProperty(itemStack, CONTENTS_TAG);
         boolean confinement = ArcanaAugments.getAugmentOnItem(itemStack, ArcanaAugments.CONFINEMENT) >= 1;
         
         List<String> stringList = new ArrayList<>();
         if(confinement){
            if(contents.isEmpty()){
               stringList.add("confinement_empty");
            }else{
               stringList.add("confinement_filled");
            }
         }else{
            if(contents.isEmpty()){
               stringList.add("empty");
            }else{
               stringList.add("filled");
            }
         }
         baseStack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(new ArrayList<>(), new ArrayList<>(), stringList, new ArrayList<>()));
         return baseStack;
      }
      
      @Override
      public InteractionResult useOn(UseOnContext context){
         ItemStack stack = context.getItemInHand();
         if(!ArcanaItemUtils.isArcane(stack) || context.getPlayer() == null) return InteractionResult.PASS;
         
         CompoundTag contents = getCompoundProperty(stack, CONTENTS_TAG);
         float hp = getFloatProperty(stack, HP_TAG);
         if(contents.isEmpty()) return InteractionResult.PASS;
         
         try(ProblemReporter.ScopedCollector logging = new ProblemReporter.ScopedCollector(context.getPlayer().problemPath(), LogUtils.getLogger())){
            ValueInput readView = TagValueInput.create(logging, context.getLevel().registryAccess(), contents);
            Optional<Entity> optional = EntityType.create(readView, context.getLevel(), EntitySpawnReason.MOB_SUMMONED);
            Vec3 summonPos = context.getClickLocation().add(0, 0.5, 0);
            
            if(optional.isPresent() && context.getLevel() instanceof ServerLevel serverWorld){
               Entity newEntity = optional.get();
               newEntity.snapTo(summonPos.x(), summonPos.y(), summonPos.z(), newEntity.getYRot(), newEntity.getXRot());
               
               if(newEntity instanceof LivingEntity living){
                  living.setHealth(hp);
               }
               
               serverWorld.addFreshEntity(newEntity);
               putProperty(stack, CONTENTS_TAG, new CompoundTag());
               
               if(context.getPlayer() instanceof ServerPlayer player){
                  player.sendSystemMessage(Component.literal("The Circlet releases its captive").withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.ITALIC), true);
                  SoundUtils.playSongToPlayer(player, SoundEvents.FIRECHARGE_USE, 1, 1.5f);
                  
                  if(newEntity instanceof TamableAnimal tameable && tameable.isOwnedBy(player)){
                     ArcanaAchievements.grant(player, ArcanaAchievements.I_CHOOSE_YOU);
                  }
               }
               buildItemLore(stack, serverWorld.getServer());
               return InteractionResult.SUCCESS_SERVER;
            }
         }
         
         return InteractionResult.PASS;
      }
      
      @Override
      public void inventoryTick(ItemStack stack, ServerLevel world, Entity entity, @Nullable EquipmentSlot slot){
         if(!ArcanaItemUtils.isArcane(stack)) return;
         if(!(world instanceof ServerLevel serverWorld && entity instanceof ServerPlayer player)) return;
         
         float hp = getFloatProperty(stack, HP_TAG);
         float maxHp = getFloatProperty(stack, MAX_HP_TAG);
         boolean heals = ArcanaAugments.getAugmentOnItem(stack, ArcanaAugments.HEALING_CIRCLET) > 0;
         double hpPerSecond = ArcanaNovum.CONFIG.getDouble(ArcanaConfig.CONTAINMENT_CIRCLET_HEALING_RATE);
         
         if(heals && player.level().getServer().getTickCount() % 20 == 0){
            putProperty(stack, HP_TAG, Math.min(maxHp, hp + hpPerSecond));
            buildItemLore(stack, serverWorld.getServer());
         }
      }
      
      @Override
      public ItemStack getDefaultInstance(){
         return prefItem;
      }
   }
}

