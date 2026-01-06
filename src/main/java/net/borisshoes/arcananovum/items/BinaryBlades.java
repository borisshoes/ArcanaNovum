package net.borisshoes.arcananovum.items;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.achievements.TimedAchievement;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.blocks.forge.StarlightForgeBlockEntity;
import net.borisshoes.arcananovum.blocks.forge.TwilightAnvilBlockEntity;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.core.EnergyItem;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerItem;
import net.borisshoes.arcananovum.damage.ArcanaDamageTypes;
import net.borisshoes.arcananovum.events.BinaryBladesMaxEnergyEvent;
import net.borisshoes.arcananovum.gui.arcanetome.ArcaneTomeGui;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaIngredient;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaRecipe;
import net.borisshoes.arcananovum.recipes.arcana.ForgeRequirement;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaEffectUtils;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.arcananovum.utils.ArcanaUtils;
import net.borisshoes.arcananovum.utils.EnhancedStatUtils;
import net.borisshoes.borislib.events.Event;
import net.borisshoes.borislib.utils.MinecraftUtils;
import net.borisshoes.borislib.utils.SoundUtils;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.BlocksAttacks;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class BinaryBlades extends EnergyItem {
   public static final String ID = "binary_blades";
   
   public static final String SPLIT_TAG = "split";
   public static final String FAKE_TAG = "fake";
   public static final String LAST_HIT_TAG = "last_hit";
   public static final String MOVE_SPEED_TAG = "binary_move_speed";
   public static final String ATTACK_SPEED_TAG = "binary_attack_speed";
   public static final String ATTACK_DAMAGE_TAG = "binary_attack_damage";
   
   public BinaryBlades(){
      id = ID;
      name = "Binary Blades";
      rarity = ArcanaRarity.SOVEREIGN;
      categories = new ArcaneTomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), ArcaneTomeGui.TomeFilter.EQUIPMENT};
      itemVersion = 0;
      vanillaItem = Items.IRON_SWORD;
      item = new BinaryBladesItem();
      displayName = Component.translatableWithFallback("item."+MOD_ID+"."+ID,name).withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD);
      researchTasks = new ResourceKey[]{ResearchTasks.OBTAIN_STARDUST, ResearchTasks.INFUSE_ITEM, ResearchTasks.OBTAIN_NETHERITE_SWORD, ResearchTasks.OBTAIN_NETHER_STAR, ResearchTasks.UNLOCK_STELLAR_CORE};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getDefaultMaxStackSize());
      putProperty(stack,SPLIT_TAG,false);
      putProperty(stack,FAKE_TAG,false);
      setPrefStack(stack);
   }
   
   @Override
   public ItemStack updateItem(ItemStack stack, MinecraftServer server){
      boolean split = getBooleanProperty(stack,SPLIT_TAG);
      boolean fake = getBooleanProperty(stack,FAKE_TAG);
      ItemStack newStack = super.updateItem(stack,server);
      putProperty(newStack,SPLIT_TAG,split);
      putProperty(newStack,FAKE_TAG,fake);
      return buildItemLore(newStack,server);
   }
   
   @Override
   public List<Component> getItemLore(@Nullable ItemStack itemStack){
      List<MutableComponent> lore = new ArrayList<>();
      lore.add(Component.literal("")
            .append(Component.literal("Two ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("blades ").withStyle(ChatFormatting.RED))
            .append(Component.literal("forged by ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("starlight").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(", bound together like ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("twin stars").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal(".").withStyle(ChatFormatting.GRAY)));
      lore.add(Component.literal("")
            .append(Component.literal("Wielding the ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("blade ").withStyle(ChatFormatting.RED))
            .append(Component.literal("splits it in two").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal(", and ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("rejoins ").withStyle(ChatFormatting.GOLD))
            .append(Component.literal("after ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("combat").withStyle(ChatFormatting.RED))
            .append(Component.literal(".").withStyle(ChatFormatting.GRAY)));
      lore.add(Component.literal("")
            .append(Component.literal("With each ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("strike ").withStyle(ChatFormatting.GOLD))
            .append(Component.literal("the ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("swords ").withStyle(ChatFormatting.RED))
            .append(Component.literal("harmonize together, ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("empowering ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("your ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("strikes").withStyle(ChatFormatting.GOLD))
            .append(Component.literal(".").withStyle(ChatFormatting.GRAY)));
      lore.add(Component.literal("")
            .append(Component.literal("Attacking ").withStyle(ChatFormatting.RED))
            .append(Component.literal("an ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("enemy ").withStyle(ChatFormatting.GOLD))
            .append(Component.literal("grants ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("ramping ").withStyle(ChatFormatting.RED))
            .append(Component.literal("movement ").withStyle(ChatFormatting.AQUA))
            .append(Component.literal("and ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("attack speed").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal(".").withStyle(ChatFormatting.GRAY)));
      
      return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   public ItemStack forgeItem(Container inv, StarlightForgeBlockEntity starlightForge){
      ItemStack sword1 = inv.getItem(6);
      ItemStack sword2 = inv.getItem(18);
      ItemStack combinedSword = sword1.copy();
      
      if(starlightForge.getLevel() instanceof ServerLevel serverWorld){
         TwilightAnvilBlockEntity twilightAnvil;
         if((twilightAnvil = (TwilightAnvilBlockEntity) starlightForge.getForgeAddition(serverWorld, ArcanaRegistry.TWILIGHT_ANVIL_BLOCK_ENTITY)) != null){
            TwilightAnvilBlockEntity.AnvilOutputSet outputSet = twilightAnvil.calculateOutput(sword1,sword2);
            if(!outputSet.output().isEmpty()){
               combinedSword = outputSet.output().copy();
            }
         }
      }
      
      ItemStack newArcanaItem = getNewItem();
      if(combinedSword.isEnchanted()){
         EnchantmentHelper.setEnchantments(newArcanaItem,combinedSword.getEnchantments());
      }
      
      if(hasProperty(combinedSword,EnhancedStatUtils.ENHANCED_STAT_TAG)){
         EnhancedStatUtils.enhanceItem(newArcanaItem,getDoubleProperty(combinedSword,EnhancedStatUtils.ENHANCED_STAT_TAG));
      }
      
      return newArcanaItem;
   }
   
   private BlocksAttacks getWhiteDwarfBlock(ItemStack item){
      if(!(ArcanaItemUtils.identifyItem(item) instanceof BinaryBlades)) return null;
      int whiteDwarf = ArcanaAugments.getAugmentOnItem(item, ArcanaAugments.WHITE_DWARF_BLADES.id);
      if(whiteDwarf < 1) return null;
      float[] reducePercentages = new float[]{0f,0.5f,0.75f,1.0f};
      
      BlocksAttacks blockComp = new BlocksAttacks(
            0.15F,
            0.5F,
            List.of(new BlocksAttacks.DamageReduction(60.0F, Optional.empty(), 0.0F, reducePercentages[whiteDwarf])),
            new BlocksAttacks.ItemDamageFunction(1.0F, 1.0F, 1.0F),
            Optional.of(DamageTypeTags.BYPASSES_SHIELD),
            Optional.of(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.HEAVY_CORE_BREAK)),
            Optional.of(SoundEvents.SHIELD_BREAK)
      );
      return blockComp;
   }
   
   private ItemStack getFakeItem(ItemStack item){
      if(!(ArcanaItemUtils.identifyItem(item) instanceof BinaryBlades)) return item;
      ItemStack fake = item.copy();
      putProperty(fake,FAKE_TAG,true);
      putProperty(fake,UUID_TAG, ArcanaNovum.BLANK_UUID);
      
      boolean white = ArcanaAugments.getAugmentOnItem(fake, ArcanaAugments.WHITE_DWARF_BLADES.id) > 0;
      if(white){
         fake.set(DataComponents.BLOCKS_ATTACKS,getWhiteDwarfBlock(item));
      }
      fake.remove(DataComponents.ATTRIBUTE_MODIFIERS);
      fake.remove(DataComponents.USE_COOLDOWN);
      
      return fake;
   }
   
   public void rebuildAttributes(ItemStack stack){
      if(!stack.is(this.item)) return;
      int energy = getEnergy(stack);
      ItemAttributeModifiers modifiers = stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
      List<ItemAttributeModifiers.Entry> attributeList = new ArrayList<>();
      boolean redGiant = ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.RED_GIANT_BLADES) > 0;
      
      // Strip old movement and attack speed stats
      for(ItemAttributeModifiers.Entry entry : modifiers.modifiers()){
         AttributeModifier modifier = entry.modifier();
         
         if(modifier.id().toString().contains(MOVE_SPEED_TAG) || modifier.id().toString().contains(ATTACK_SPEED_TAG) || modifier.id().toString().contains(ATTACK_DAMAGE_TAG)){
            continue;
         }
         attributeList.add(entry);
      }
      
      if(energy > 0){
         attributeList.add(new ItemAttributeModifiers.Entry(Attributes.MOVEMENT_SPEED,new AttributeModifier(Identifier.fromNamespaceAndPath(ArcanaNovum.MOD_ID,MOVE_SPEED_TAG),0.5 * getEnergy(stack) / getMaxEnergy(stack), AttributeModifier.Operation.ADD_MULTIPLIED_BASE), EquipmentSlotGroup.MAINHAND));
         attributeList.add(new ItemAttributeModifiers.Entry(Attributes.ATTACK_SPEED,new AttributeModifier(Identifier.fromNamespaceAndPath(ArcanaNovum.MOD_ID,ATTACK_SPEED_TAG),(double) getEnergy(stack) / getMaxEnergy(stack), AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND));
         if(redGiant && energy >= 50){
            attributeList.add(new ItemAttributeModifiers.Entry(Attributes.ATTACK_DAMAGE,new AttributeModifier(Identifier.fromNamespaceAndPath(ArcanaNovum.MOD_ID,ATTACK_DAMAGE_TAG),4 * (getEnergy(stack) - 50.0) / (getMaxEnergy(stack) - 50.0), AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND));
         }
      }
      
      ItemAttributeModifiers newComponent = new ItemAttributeModifiers(attributeList);
      stack.set(DataComponents.ATTRIBUTE_MODIFIERS,newComponent);
   }
   
   private ChatFormatting getColor(ItemStack stack){
      boolean pulsar = ArcanaAugments.getAugmentOnItem(stack, ArcanaAugments.PULSAR_BLADES.id) > 0;
      boolean white = ArcanaAugments.getAugmentOnItem(stack, ArcanaAugments.WHITE_DWARF_BLADES.id) > 0;
      boolean red = ArcanaAugments.getAugmentOnItem(stack, ArcanaAugments.RED_GIANT_BLADES.id) > 0;
      if(pulsar) return ChatFormatting.AQUA;
      if(white) return ChatFormatting.WHITE;
      if(red) return ChatFormatting.RED;
      return ChatFormatting.YELLOW;
   }
   
   public static boolean isFakeBlade(ItemStack stack){
      return stack.is(ArcanaRegistry.BINARY_BLADES.getItem()) && ArcanaItem.getBooleanProperty(stack,FAKE_TAG);
   }
   
   @Override
   public int getMaxEnergy(ItemStack item){
      return 100;
   }
   
   @Override
   public List<List<Component>> getBookLore(){
      List<List<Component>> list = new ArrayList<>();
      list.add(List.of(Component.literal("   Binary Blades").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), Component.literal("\nRarity: ").withStyle(ChatFormatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(),false)), Component.literal("\nGazing up at the stars one night led me to observe two close stars dancing in the sky. Two stars harmoniously acting as one. Glancing over at my Forge gave me an idea.").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("   Binary Blades").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), Component.literal("\nThe Binary Blades are two swords that act in unison, combining into one when stored and splitting in two when held. They grant an increased attack speed compared to normal weapons, with each strike acting like a note in a harmony. ").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("   Binary Blades").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), Component.literal("\nSuccessive strikes grant the wielder increased movement and attack speed.").withStyle(ChatFormatting.BLACK)));
      return list;
   }
   
   public class BinaryBladesItem extends ArcanaPolymerItem {
      public BinaryBladesItem(){
         super(getThis(),getEquipmentArcanaItemComponents()
               .sword(ToolMaterial.NETHERITE, 2,-1.2f)
         );
      }
      
      @Override
      public ItemStack getPolymerItemStack(ItemStack itemStack, TooltipFlag tooltipType, PacketContext context){
         ItemStack baseStack = super.getPolymerItemStack(itemStack, tooltipType, context);
         if(!ArcanaItemUtils.isArcane(itemStack)) return baseStack;
         
         List<String> stringList = new ArrayList<>();
         boolean split = getBooleanProperty(itemStack,SPLIT_TAG);
         boolean pulsar = ArcanaAugments.getAugmentOnItem(itemStack, ArcanaAugments.PULSAR_BLADES.id) > 0;
         boolean white = ArcanaAugments.getAugmentOnItem(itemStack, ArcanaAugments.WHITE_DWARF_BLADES.id) > 0;
         boolean red = ArcanaAugments.getAugmentOnItem(itemStack, ArcanaAugments.RED_GIANT_BLADES.id) > 0;
         
         if(pulsar) stringList.add(split ? "singular_pulsar" : "combined_pulsar");
         else if(white) stringList.add(split ? "singular_white" : "combined_white");
         else if(red) stringList.add(split ? "singular_red" : "combined_red");
         else stringList.add(split ? "singular" : "combined");
         
         baseStack.set(DataComponents.CUSTOM_MODEL_DATA,new CustomModelData(new ArrayList<>(),new ArrayList<>(),stringList,new ArrayList<>()));
         return baseStack;
      }
      
      @Override
      public ItemStack getDefaultInstance(){
         return prefItem;
      }
      
      @Override
      public void inventoryTick(ItemStack stack, ServerLevel world, Entity entity, @Nullable EquipmentSlot slot){
         if(!ArcanaItemUtils.isArcane(stack)) return;
         if(!(world instanceof ServerLevel && entity instanceof ServerPlayer player)) return;
         
         boolean fake = getBooleanProperty(stack,FAKE_TAG);
         if(fake){
            if(!player.getOffhandItem().equals(stack) || !player.getMainHandItem().is(this)){
               stack.setCount(0);
               ArcanaNovum.data(player).restoreOffhand();
            }else{
               ItemStack mainStack = player.getMainHandItem();
               boolean pulsar1 = ArcanaAugments.getAugmentOnItem(stack, ArcanaAugments.PULSAR_BLADES.id) > 0;
               boolean white1 = ArcanaAugments.getAugmentOnItem(stack, ArcanaAugments.WHITE_DWARF_BLADES.id) > 0;
               boolean red1 = ArcanaAugments.getAugmentOnItem(stack, ArcanaAugments.RED_GIANT_BLADES.id) > 0;
               boolean pulsar2 = ArcanaAugments.getAugmentOnItem(mainStack, ArcanaAugments.PULSAR_BLADES.id) > 0;
               boolean white2 = ArcanaAugments.getAugmentOnItem(mainStack, ArcanaAugments.WHITE_DWARF_BLADES.id) > 0;
               boolean red2 = ArcanaAugments.getAugmentOnItem(mainStack, ArcanaAugments.RED_GIANT_BLADES.id) > 0;
               if(pulsar1 ^ pulsar2 || white1 ^ white2 || red1 ^ red2){
                  ArcanaNovum.data(player).restoreOffhand();
                  ArcanaNovum.data(player).storeOffhand(getFakeItem(mainStack));
               }
            }
            return;
         }
         
         boolean split = getBooleanProperty(stack,SPLIT_TAG);
         ItemStack handStack = player.getMainHandItem();
         int energy = getEnergy(stack);
         
         if(handStack.equals(stack)){
            if(!split){
               putProperty(stack,SPLIT_TAG,true);
            }
            if(!player.getOffhandItem().is(this)){
               ArcanaNovum.data(player).restoreOffhand();
               ArcanaNovum.data(player).storeOffhand(getFakeItem(stack));
            }
            
            if(world.getServer().getTickCount() % 40 == 0 || (energy > 0 && energy < getMaxEnergy(stack))){
               char[] unicodeChars = {'▁', '▂', '▃', '▅', '▆', '▇', '▌'};
               StringBuilder message = new StringBuilder("\uD83D\uDDE1 ✦ ");
               for (int i = 0; i < 10; i++) {
                  int segmentValue = energy - (i * 10);
                  if (segmentValue <= 0) {
                     message.append(unicodeChars[0]);
                  } else if (segmentValue >= 10) {
                     message.append(unicodeChars[unicodeChars.length - 1]);
                  } else {
                     int charIndex = (int) ((double) segmentValue / 10 * (unicodeChars.length - 1));
                     message.append(unicodeChars[charIndex]);
                  }
               }
               message.append(" ✦ \uD83D\uDDE1");
               player.displayClientMessage(Component.literal(message.toString()).withStyle(getColor(stack)), true);
            }
         }else if(split){
            putProperty(stack,SPLIT_TAG,false);
         }
         
         if(world.getServer().getTickCount() % 5 == 0){
            int redGiant = ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.RED_GIANT_BLADES);
            if(energy >= 50 && redGiant >= 2){
               List<Entity> entities = new ArrayList<>(player.level().getEntities(player, player.getBoundingBox().inflate(8), e -> e.distanceTo(player) <= 3.5));
               for(Entity nearEntity : entities){
                  if(nearEntity instanceof LivingEntity living && !nearEntity.fireImmune()){
                     living.igniteForSeconds(3);
                  }
               }
               ArcanaEffectUtils.circle(player.level(),null,player.position().add(0,0.2,0), ParticleTypes.FLAME,0.5,8,2,0.1,0.05);
            }
            
            int lastHitTime = getIntProperty(stack, LAST_HIT_TAG);
            if(lastHitTime > 0){
               putProperty(stack, LAST_HIT_TAG, lastHitTime - 1);
            }else if(lastHitTime == 0){
               putProperty(stack, LAST_HIT_TAG, -1);
            }else{
               addEnergy(stack, -4);
            }
         }
         
         if(energy >= getMaxEnergy(stack)){
            Event.addEvent(new BinaryBladesMaxEnergyEvent(player));
            long count = Event.getEventsOfType(BinaryBladesMaxEnergyEvent.class).stream().filter(event -> event.getPlayer().equals(player)).count();
            if(count >= ((TimedAchievement) ArcanaAchievements.STARBURST_STREAM).getGoal()){
               ArcanaAchievements.grant(player,ArcanaAchievements.STARBURST_STREAM);
            }
            if(world.getServer().getTickCount() % 20 == 0){
               ArcanaNovum.data(player).addXP(ArcanaNovum.CONFIG.getInt(ArcanaRegistry.XP_BINARY_BLADES_MAX_ENERGY_PER_SECOND));
            }
         }
         
         rebuildAttributes(stack);
      }
      
      @Override
      public InteractionResult use(Level world, Player playerEntity, InteractionHand hand){
         ItemStack stack = playerEntity.getItemInHand(hand);
         if(!(playerEntity instanceof ServerPlayer player)) return InteractionResult.PASS;
         
         boolean fake = getBooleanProperty(stack,FAKE_TAG);
         boolean split = getBooleanProperty(stack,SPLIT_TAG);
         BlocksAttacks blocksAttacksComponent = stack.get(DataComponents.BLOCKS_ATTACKS);
         if(blocksAttacksComponent != null){
            playerEntity.startUsingItem(hand);
            return InteractionResult.CONSUME;
         }else if(fake || !split){
            return InteractionResult.PASS;
         }
         
         int pulsar = ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.PULSAR_BLADES);
         int energy = getEnergy(stack);
         int energyCost = (25*(3-pulsar));
         int energyGain = 0;
         
         if(pulsar > 0 && energy > energyCost){
            MinecraftUtils.LasercastResult lasercast = MinecraftUtils.lasercast(world, player.getEyePosition(), player.getForward(), 25, true, player);
            float damage = pulsar * 7;
            for(Entity hit : lasercast.sortedHits()){
               if(hit instanceof ServerPlayer hitPlayer && hitPlayer.isBlocking()){
                  double dp = hitPlayer.getForward().normalize().dot(lasercast.direction().normalize());
                  if(dp < -0.6){
                     ArcanaUtils.blockWithShield(hitPlayer,damage);
                     continue;
                  }
               }
               hit.hurtServer(player.level(), ArcanaDamageTypes.of(player.level(),ArcanaDamageTypes.PHOTONIC,player), damage);
               ArcanaItem.putProperty(stack,BinaryBlades.LAST_HIT_TAG,20);
               energyGain += 10;
            }
            ArcanaEffectUtils.pulsarBladeShoot(player.level(),player.getEyePosition().subtract(0,player.getBbHeight()/4,0),lasercast.endPos(),0);
            SoundUtils.playSound(player.level(),player.blockPosition(), SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS,1.0f,2.0f);
            addEnergy(stack, energyGain-energyCost);
            player.getCooldowns().addCooldown(stack,10);
            player.level().getChunkSource().sendToTrackingPlayersAndSelf(player, new ClientboundAnimatePacket(player, ClientboundAnimatePacket.SWING_OFF_HAND));
            return InteractionResult.SUCCESS_SERVER;
         }
         
         return InteractionResult.PASS;
      }
      
      @Override
      public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack otherStack, Slot slot, ClickAction clickType, Player player, SlotAccess cursorStackReference){
         boolean superRet = super.overrideOtherStackedOnMe(stack, otherStack, slot, clickType, player, cursorStackReference);
         if(getBooleanProperty(stack,FAKE_TAG)){
            stack.copyAndClear();
            ArcanaNovum.data(player).restoreOffhand();
            return false;
         }
         return superRet;
      }
      
      @Override
      public void onUseTick(Level world, LivingEntity user, ItemStack stack, int remainingUseTicks){
         super.onUseTick(world, user, stack, remainingUseTicks);
      }
   }
}
