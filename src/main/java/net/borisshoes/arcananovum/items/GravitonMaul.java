package net.borisshoes.arcananovum.items;

import com.google.common.collect.Lists;
import net.borisshoes.arcananovum.ArcanaConfig;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.blocks.forge.StarlightForgeBlockEntity;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerMaceItem;
import net.borisshoes.arcananovum.gui.arcanetome.ArcaneTomeGui;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaColors;
import net.borisshoes.arcananovum.utils.ArcanaEffectUtils;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.arcananovum.utils.EnhancedStatUtils;
import net.borisshoes.borislib.BorisLib;
import net.borisshoes.borislib.conditions.ConditionInstance;
import net.borisshoes.borislib.conditions.Conditions;
import net.borisshoes.borislib.utils.MinecraftUtils;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.Tuple;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;
import static net.borisshoes.arcananovum.ArcanaRegistry.arcanaId;
import static net.borisshoes.borislib.utils.MinecraftUtils.makeEnchantComponent;

public class GravitonMaul extends ArcanaItem {
   public static final String ID = "graviton_maul";
   
   public static final String FALL_START_HEIGHT_TAG = "fallStart";
   
   public GravitonMaul(){
      id = ID;
      name = "Graviton Maul";
      rarity = ArcanaRarity.SOVEREIGN;
      categories = new ArcaneTomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), ArcaneTomeGui.TomeFilter.EQUIPMENT};
      itemVersion = 0;
      vanillaItem = Items.MACE;
      item = new GravitonMaulItem();
      displayName = Component.translatableWithFallback("item." + MOD_ID + "." + ID, name).withStyle(ChatFormatting.BOLD).withColor(ArcanaColors.BETTER_DARK_BLUE);
      researchTasks = new ResourceKey[]{ResearchTasks.OBTAIN_MACE, ResearchTasks.OBTAIN_NETHERITE_INGOT, ResearchTasks.OBTAIN_NETHER_STAR, ResearchTasks.UNLOCK_STELLAR_CORE, ResearchTasks.UNLOCK_MIDNIGHT_ENCHANTER, ResearchTasks.ADVANCEMENT_OVER_OVERKILL};
   }
   
   @Override
   public ItemStack initializeArcanaTag(ItemStack stack){
      super.initializeArcanaTag(stack);
      putProperty(stack, MODE_TAG, 0); // 0 jump boost, 1 gravity amp, 2 channel
      putProperty(stack, FALL_START_HEIGHT_TAG, Double.NaN);
      return stack;
   }
   
   @Override
   public ItemStack updateItem(ItemStack stack, MinecraftServer server){
      int mode = getIntProperty(stack, MODE_TAG);
      double fallHeight = getDoubleProperty(stack, FALL_START_HEIGHT_TAG);
      ItemStack newStack = super.updateItem(stack, server);
      putProperty(newStack, MODE_TAG, mode);
      putProperty(newStack, FALL_START_HEIGHT_TAG, fallHeight);
      return buildItemLore(newStack, server);
   }
   
   @Override
   public List<Component> getItemLore(@Nullable ItemStack itemStack){
      List<MutableComponent> lore = new ArrayList<>();
      lore.add(Component.literal("")
            .append(Component.literal("The ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("heavy core").withStyle(ChatFormatting.BLUE))
            .append(Component.literal(" has been brought to its ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("limit").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(", becoming a ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("singularity").withColor(ArcanaColors.BETTER_DARK_BLUE))
            .append(Component.literal(".").withStyle(ChatFormatting.GRAY)));
      lore.add(Component.literal("")
            .append(Component.literal("The ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("maul ").withColor(ArcanaColors.BETTER_DARK_BLUE))
            .append(Component.literal("acts as a ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("mace ").withStyle(ChatFormatting.AQUA))
            .append(Component.literal("combined with an ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("axe").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(".").withStyle(ChatFormatting.GRAY)));
      lore.add(Component.literal("")
            .append(Component.literal("The ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("maul ").withColor(ArcanaColors.BETTER_DARK_BLUE))
            .append(Component.literal("can ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("disable shields").withStyle(ChatFormatting.BLUE))
            .append(Component.literal(" and deal ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("massive damage").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(" while falling.").withStyle(ChatFormatting.GRAY)));
      lore.add(Component.literal("")
            .append(Component.literal("The ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("maul's ").withColor(ArcanaColors.BETTER_DARK_BLUE))
            .append(Component.literal("gravity well").withStyle(ChatFormatting.BLUE))
            .append(Component.literal(" can ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("fell trees").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(" in a single blow.").withStyle(ChatFormatting.GRAY)));
      lore.add(Component.literal("")
            .append(Component.literal("Right Clicking").withStyle(ChatFormatting.BLUE))
            .append(Component.literal(" with the ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("maul ").withColor(ArcanaColors.BETTER_DARK_BLUE))
            .append(Component.literal("launches ").withStyle(ChatFormatting.AQUA))
            .append(Component.literal("you in the ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("air").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(".").withStyle(ChatFormatting.GRAY)));
      lore.add(Component.literal("")
            .append(Component.literal("Right Clicking in the air").withStyle(ChatFormatting.BLUE))
            .append(Component.literal(" ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("rockets ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("you ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("downward").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(", causing a ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("powerful impact").withColor(ArcanaColors.BETTER_DARK_BLUE))
            .append(Component.literal(".").withStyle(ChatFormatting.GRAY)));
      
      return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   public ItemStack forgeItem(Container inv, List<Integer> centerpieces, StarlightForgeBlockEntity starlightForge){
      ItemStack newArcanaItem = getNewItem();
      if(centerpieces.isEmpty()) return newArcanaItem;
      ItemStack maceStack = inv.getItem(centerpieces.getFirst()); // Should be the Mace
      
      newArcanaItem.enchant(MinecraftUtils.getEnchantment(Enchantments.BREACH), 5);
      if(maceStack.isEnchanted()){
         for(Holder<Enchantment> enchantment : maceStack.getEnchantments().keySet()){
            if(EnchantmentHelper.isEnchantmentCompatible(newArcanaItem.getEnchantments().keySet(), enchantment)){
               newArcanaItem.enchant(enchantment, maceStack.getEnchantments().getLevel(enchantment));
            }
         }
      }
      
      if(hasProperty(maceStack, EnhancedStatUtils.ENHANCED_STAT_TAG)){
         EnhancedStatUtils.enhanceItem(newArcanaItem, getDoubleProperty(maceStack, EnhancedStatUtils.ENHANCED_STAT_TAG));
      }
      
      return newArcanaItem;
   }
   
   public void treeFell(Level world, Player player, ItemStack item, BlockPos pos){
      if(!(world instanceof ServerLevel serverWorld)) return;
      Block type = world.getBlockState(pos).getBlock();
      if(!world.getBlockState(pos).is(BlockTags.LOGS)) return;
      
      int maxBlocks = 128;
      
      Queue<Tuple<BlockPos, Integer>> queue = Lists.newLinkedList();
      Queue<BlockPos> visited = Lists.newLinkedList();
      queue.add(new Tuple<>(pos, 0));
      ArrayList<BlockPos> toMine = new ArrayList<>();
      
      while(!queue.isEmpty()){
         Tuple<BlockPos, Integer> pair = queue.poll();
         BlockPos blockPos = pair.getA();
         int depth = pair.getB();
         visited.add(blockPos);
         Block curType = world.getBlockState(blockPos).getBlock();
         
         if(curType == type){
            if(toMine.contains(blockPos)) continue;
            toMine.add(blockPos);
            if(toMine.size() >= maxBlocks) break;
            // Add Surrounding Blocks to Queue
            for(int i = -1; i <= 1; i++){
               for(int j = -1; j <= 1; j++){
                  for(int k = -1; k <= 1; k++){
                     if(!(i == 0 && j == 0 && k == 0)){
                        BlockPos pos2 = blockPos.offset(i, j, k);
                        if(queue.stream().noneMatch(p -> p.getA().equals(pos2)) && !visited.contains(pos2)){
                           queue.add(new Tuple<>(pos2, depth + 1));
                        }
                     }
                  }
               }
            }
         }
      }
      
      List<ItemStack> drops = new ArrayList<>();
      ItemStack veinAxe = new ItemStack(Items.NETHERITE_AXE);
      
      for(BlockPos blockPos : toMine){
         drops.addAll(Block.getDrops(world.getBlockState(blockPos), serverWorld, blockPos, null, player, veinAxe));
         world.destroyBlock(blockPos, false, player);
         if(type instanceof DropExperienceBlock experienceBlock){
            experienceBlock.spawnAfterBreak(world.getBlockState(blockPos), serverWorld, pos, veinAxe, true);
         }
      }
      for(ItemStack stack : drops){
         Block.popResource(world, pos, stack);
      }
   }
   
   private List<Entity> getAffectedEntities(ServerPlayer player, boolean includePlayer, double range){
      List<Entity> entities = new ArrayList<>(player.level().getEntities(player, player.getBoundingBox().inflate(range * 2), e -> e.distanceTo(player) <= range));
      if(includePlayer) entities.add(player);
      return entities;
   }
   
   
   private void gravityEffects(ServerPlayer player, ItemStack stack){
      int mode = getIntProperty(stack, MODE_TAG); // 0 - boost, 1 - fall, 2 - maelstrom
      boolean domain = ArcanaAugments.getAugmentOnItem(stack, ArcanaAugments.GRAVITIC_DOMAIN) > 0;
      double domainRange = ArcanaNovum.CONFIG.getDouble(ArcanaConfig.GRAVITON_MAUL_AOE_RANGE);
      float crushDmg = ArcanaNovum.CONFIG.getFloat(ArcanaConfig.GRAVITON_MAUL_CRUSH_DMG);
      
      if(mode == 0){
         for(Entity affectedEntity : domain ? getAffectedEntities(player, true, domainRange) : List.of(player)){
            affectedEntity.push(0, 0.085, 0);
            if(affectedEntity instanceof ServerPlayer affectedPlayer){
               affectedPlayer.connection.send(new ClientboundSetEntityMotionPacket(affectedPlayer));
               affectedPlayer.connection.aboveGroundTickCount = 0;
            }
            
            if(affectedEntity.verticalCollision && !affectedEntity.verticalCollisionBelow && affectedEntity instanceof LivingEntity livingEntity){
               livingEntity.hurtServer(player.level(), player.damageSources().flyIntoWall(), crushDmg);
               if(livingEntity.getHealth() < 2.0 && livingEntity instanceof ServerPlayer affectedPlayer){
                  ArcanaAchievements.grant(affectedPlayer, ArcanaAchievements.RAISE_THE_ROOF);
               }
            }
            
            player.level().sendParticles(ParticleTypes.TRIAL_SPAWNER_DETECTED_PLAYER_OMINOUS, affectedEntity.getX(), affectedEntity.getY() + affectedEntity.getBbHeight() / 2, affectedEntity.getZ(), 5, affectedEntity.getBbWidth() / 2, affectedEntity.getBbHeight() / 2, affectedEntity.getBbWidth() / 2, 0.01);
         }
      }else if(mode == 1){
         for(Entity affectedEntity : domain ? getAffectedEntities(player, true, domainRange) : List.of(player)){
            player.level().sendParticles(ParticleTypes.TRIAL_SPAWNER_DETECTED_PLAYER_OMINOUS, affectedEntity.getX(), affectedEntity.getY() + affectedEntity.getBbHeight() / 2, affectedEntity.getZ(), 5, affectedEntity.getBbWidth() / 2, affectedEntity.getBbHeight() / 2, affectedEntity.getBbWidth() / 2, 0.01);
         }
         
         if(player.onGround()){
            if(player.getTicksUsingItem() >= 11){
               double rangePerSpeed = ArcanaNovum.CONFIG.getDouble(ArcanaConfig.GRAVITON_MAUL_SLAM_RANGE_PER_SPEED);
               double impactVel = -Mth.clamp(BorisLib.PLAYER_MOVEMENT_TRACKER.get(player).velocity().y(), -3, -0.5);
               double radius = 3 + rangePerSpeed * impactVel; // 3.5 - 6 block range
               float dmgPerSpeed = ArcanaNovum.CONFIG.getFloat(ArcanaConfig.GRAVITON_MAUL_SLAM_DMG_PER_SPEED);
               float dmg = (float) (2f + dmgPerSpeed * impactVel);
               double totalDmg = 0;
               for(Entity affectedEntity : getAffectedEntities(player, false, radius)){
                  Vec3 diff = player.position().subtract(affectedEntity.position());
                  double multiplier = Mth.clamp(diff.length() * .2, .03, 2);
                  Vec3 motion = diff.normalize().multiply(-multiplier, 0, -multiplier).add(0, radius * 0.15, 0);
                  affectedEntity.push(motion.x, motion.y, motion.z);
                  if(affectedEntity instanceof ServerPlayer affectedPlayer){
                     affectedPlayer.connection.send(new ClientboundSetEntityMotionPacket(affectedPlayer));
                  }
                  if(affectedEntity instanceof LivingEntity livingEntity){
                     livingEntity.hurtServer(player.level(), player.damageSources().fallingBlock(player), dmg);
                     totalDmg += dmg;
                  }
               }
               ArcanaNovum.data(player).addXP((int) Math.min(ArcanaNovum.CONFIG.getInt(ArcanaConfig.XP_GRAVITON_MAUL_IMPACT_DAMAGE_PER_10) * totalDmg / 10, ArcanaNovum.CONFIG.getInt(ArcanaConfig.XP_GRAVITON_MAUL_IMPACT_DAMAGE_CAP)));
               ArcanaEffectUtils.gravitonMaulSlam(player.level(), player.getOnPos(), radius, 0);
               player.releaseUsingItem();
               player.getCooldowns().addCooldown(stack, 40);
            }else{
               player.releaseUsingItem();
               player.getCooldowns().addCooldown(stack, 5);
            }
            double fallStart = getDoubleProperty(stack, FALL_START_HEIGHT_TAG);
            if(!Double.isNaN(fallStart)){
               if(fallStart - player.getY() >= 300) ArcanaAchievements.grant(player, ArcanaAchievements.QUICK_WAY_DOWN);
               putProperty(stack, FALL_START_HEIGHT_TAG, Double.NaN);
            }
         }
      }else if(mode == 2){
         float vortexDmg = ArcanaNovum.CONFIG.getFloat(ArcanaConfig.GRAVITON_MAUL_VORTEX_DMG);
         float vortexAmp = ArcanaNovum.CONFIG.getFloat(ArcanaConfig.GRAVITON_MAUL_VORTEX_DMG_AMP);
         double vortexRange = ArcanaNovum.CONFIG.getDouble(ArcanaConfig.GRAVITON_MAUL_VORTEX_RANGE);
         double vortexSuck = ArcanaNovum.CONFIG.getDouble(ArcanaConfig.GRAVITON_MAUL_VORTEX_SUCK_POWER);
         
         for(Entity affectedEntity : getAffectedEntities(player, false, vortexRange)){
            Vec3 diff = affectedEntity.position().subtract(player.position().add(0, 0.1, 0));
            double multiplier = Mth.clamp(diff.length() * vortexSuck, .03, 2);
            Vec3 motion = diff.add(0, 0, 0).normalize().scale(-multiplier);
            affectedEntity.setDeltaMovement(motion.x, motion.y, motion.z);
            if(affectedEntity instanceof ServerPlayer affectedPlayer){
               affectedPlayer.connection.send(new ClientboundSetEntityMotionPacket(affectedPlayer));
            }
            
            if(affectedEntity instanceof LivingEntity livingEntity){
               ConditionInstance vulnerability = new ConditionInstance(Conditions.VULNERABILITY, arcanaId(ID), 10, vortexAmp, true, true, true, AttributeModifier.Operation.ADD_VALUE, player.getUUID());
               Conditions.addCondition(player.level().getServer(), livingEntity, vulnerability);
               livingEntity.hurtServer(player.level(), player.damageSources().source(DamageTypes.CRAMMING,player), vortexDmg);
            }
         }
         ArcanaEffectUtils.gravitonMaulMaelstrom(player, player.getTicksUsingItem());
         
         
         float vortexFortitude = ArcanaNovum.CONFIG.getFloat(ArcanaConfig.GRAVITON_MAUL_VORTEX_FORTITUDE);
         ConditionInstance fortitude = new ConditionInstance(Conditions.FORTITUDE, arcanaId(ArcanaRegistry.GRAVITON_MAUL.getId()), 10, -(vortexFortitude), true, true, true, AttributeModifier.Operation.ADD_VALUE, player.getUUID());
         Conditions.addCondition(player.level().getServer(), player, fortitude);
      }
   }
   
   @Override
   public List<List<Component>> getBookLore(){
      List<List<Component>> list = new ArrayList<>();
      list.add(List.of(Component.literal("   Graviton Maul").withStyle(ChatFormatting.BOLD).withColor(ArcanaColors.BETTER_DARK_BLUE), Component.literal("\nRarity: ").withStyle(ChatFormatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(), false)), Component.literal("\nAfter much experimentation with the Heavy Core, I have brought it to its apex. A singularity, a point of infinite density, contained by an Arcane casing. Gravity itself is at my will.").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("   Graviton Maul").withStyle(ChatFormatting.BOLD).withColor(ArcanaColors.BETTER_DARK_BLUE), Component.literal("\nThe Maul is an empowered mace that has damage that scales with fall height. To aid in this ability, Using the Maul while on the ground causes a localized lapse in gravity, sending me into the air.").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("   Graviton Maul").withStyle(ChatFormatting.BOLD).withColor(ArcanaColors.BETTER_DARK_BLUE), Component.literal("\nHowever, I have to be careful not to hit my head on the ceiling. As to not give my foe time to react, I can Use the Maul in the air to amplify gravity and rapidly send me downwards. Impacting the ground causes damage to nearby creatures. ").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("   Graviton Maul").withStyle(ChatFormatting.BOLD).withColor(ArcanaColors.BETTER_DARK_BLUE), Component.literal("\nThe Maul’s immense gravity also gives it the heft to fell entire trees at once and bust through shields.").withStyle(ChatFormatting.BLACK)));
      return list;
   }
   
   public class GravitonMaulItem extends ArcanaPolymerMaceItem {
      public GravitonMaulItem(){
         super(getThis(), getEquipmentArcanaItemComponents()
               .component(DataComponents.TOOL, new Tool(List.of(), 1.0F, 2, false))
               .component(DataComponents.CONSUMABLE, Consumable.builder().consumeSeconds(72000).animation(ItemUseAnimation.BOW).sound(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.AMETHYST_BLOCK_CHIME)).build())
               .component(DataComponents.WEAPON, new Weapon(1, 7.5F))
               .component(DataComponents.USE_EFFECTS, new UseEffects(false, true, 0.01f))
               .delayedComponent(DataComponents.ENCHANTMENTS, ctx -> makeEnchantComponent(new EnchantmentInstance(ctx.getOrThrow(Enchantments.BREACH),5)))
               .attributes(ItemAttributeModifiers.builder()
                     .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, 5.0, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                     .add(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID, -3.4F, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                     .add(Attributes.FALL_DAMAGE_MULTIPLIER, new AttributeModifier(ArcanaRegistry.arcanaId(id), -0.80F, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL), EquipmentSlotGroup.MAINHAND)
                     .build())
               .enchantable(15)
         );
      }
      
      @Override
      public ItemStack getDefaultInstance(){
         return prefItem;
      }
      
      @Override
      public void inventoryTick(ItemStack stack, ServerLevel world, Entity entity, @Nullable EquipmentSlot slot){
         if(!ArcanaItemUtils.isArcane(stack)) return;
         if(!(entity instanceof ServerPlayer player)) return;
      }
      
      @Override
      public InteractionResult use(Level world, Player playerEntity, InteractionHand hand){
         ItemStack stack = playerEntity.getItemInHand(hand);
         if(!(playerEntity instanceof ServerPlayer player)) return InteractionResult.PASS;
         
         if(player.onGround()){
            boolean maelstrom = ArcanaAugments.getAugmentOnItem(stack, ArcanaAugments.SINGULARITY_MAELSTROM) > 0;
            if(maelstrom && player.isShiftKeyDown()){
               putProperty(stack, MODE_TAG, 2);
            }else{
               putProperty(stack, MODE_TAG, 0);
               boolean domain = ArcanaAugments.getAugmentOnItem(stack, ArcanaAugments.GRAVITIC_DOMAIN) > 0;
               double domainRange = ArcanaNovum.CONFIG.getDouble(ArcanaConfig.GRAVITON_MAUL_AOE_RANGE);
               double upSpeed = ArcanaNovum.CONFIG.getDouble(ArcanaConfig.GRAVITON_MAUL_UP_SPEED);
               for(Entity affectedEntity : domain ? getAffectedEntities(player, true, domainRange) : List.of(player)){
                  if(affectedEntity.getDeltaMovement().y() < 0.75)
                     affectedEntity.setDeltaMovement(affectedEntity.getDeltaMovement().multiply(1, 0, 1).add(0, upSpeed, 0));
                  if(affectedEntity instanceof ServerPlayer affectedPlayer){
                     affectedPlayer.connection.send(new ClientboundSetEntityMotionPacket(affectedPlayer));
                  }
               }
               ArcanaEffectUtils.circle(player.level(), null, player.position().add(0, 0.25, 0), ParticleTypes.TRIAL_SPAWNER_DETECTED_PLAYER_OMINOUS, domain ? 3.5 : 0.5, 40, 5, 0.1, 0.01);
            }
         }else{
            putProperty(stack, MODE_TAG, 1);
            putProperty(stack, FALL_START_HEIGHT_TAG, player.getY());
            boolean domain = ArcanaAugments.getAugmentOnItem(stack, ArcanaAugments.GRAVITIC_DOMAIN) > 0;
            double domainRange = ArcanaNovum.CONFIG.getDouble(ArcanaConfig.GRAVITON_MAUL_AOE_RANGE);
            double downSpeed = ArcanaNovum.CONFIG.getDouble(ArcanaConfig.GRAVITON_MAUL_DOWN_SPEED);
            for(Entity affectedEntity : domain ? getAffectedEntities(player, true, domainRange) : List.of(player)){
               if(affectedEntity.getDeltaMovement().y() > -1)
                  affectedEntity.setDeltaMovement(affectedEntity.getDeltaMovement().multiply(1, 0, 1).add(0, downSpeed, 0));
               if(affectedEntity instanceof ServerPlayer affectedPlayer){
                  affectedPlayer.connection.send(new ClientboundSetEntityMotionPacket(affectedPlayer));
               }
            }
            ArcanaEffectUtils.circle(player.level(), null, player.position().add(0, 0.25, 0), ParticleTypes.TRIAL_SPAWNER_DETECTED_PLAYER_OMINOUS, domain ? 3.5 : 0.5, 40, 5, 0.1, 0.01);
         }
         gravityEffects(player, stack);
         playerEntity.startUsingItem(hand);
         return InteractionResult.CONSUME;
      }
      
      @Override
      public void onUseTick(Level world, LivingEntity user, ItemStack stack, int remainingUseTicks){
         if(!(user instanceof ServerPlayer player)) return;
         gravityEffects(player, stack);
      }
      
      @Override
      public int getUseDuration(ItemStack stack, LivingEntity user){
         return 72000;
      }
      
      @Override
      public ItemUseAnimation getUseAnimation(ItemStack stack){
         return ItemUseAnimation.BOW;
      }
   }
}