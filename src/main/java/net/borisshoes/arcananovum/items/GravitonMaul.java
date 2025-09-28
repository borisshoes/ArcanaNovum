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
import net.borisshoes.arcananovum.gui.arcanetome.TomeGui;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaIngredient;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaRecipe;
import net.borisshoes.arcananovum.recipes.arcana.ForgeRequirement;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaColors;
import net.borisshoes.arcananovum.utils.ArcanaEffectUtils;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.arcananovum.utils.EnhancedStatUtils;
import net.borisshoes.borislib.BorisLib;
import net.borisshoes.borislib.utils.MinecraftUtils;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.block.Block;
import net.minecraft.block.ExperienceDroppingBlock;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.*;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.consume.UseAction;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class GravitonMaul extends ArcanaItem {
   public static final String ID = "graviton_maul";
   
   public static final String FALL_START_HEIGHT_TAG = "fallStart";
   
   public GravitonMaul(){
      id = ID;
      name = "Graviton Maul";
      rarity = ArcanaRarity.SOVEREIGN;
      categories = new TomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), TomeGui.TomeFilter.EQUIPMENT};
      itemVersion = 0;
      vanillaItem = Items.MACE;
      item = new GravitonMaulItem();
      displayName = Text.translatableWithFallback("item."+MOD_ID+"."+ID,name).formatted(Formatting.BOLD).withColor(ArcanaColors.BETTER_DARK_BLUE);
      researchTasks = new RegistryKey[]{ResearchTasks.OBTAIN_MACE, ResearchTasks.OBTAIN_NETHERITE_INGOT, ResearchTasks.OBTAIN_NETHER_STAR, ResearchTasks.UNLOCK_STELLAR_CORE, ResearchTasks.UNLOCK_MIDNIGHT_ENCHANTER, ResearchTasks.ADVANCEMENT_OVER_OVERKILL};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getMaxCount());
      putProperty(stack,MODE_TAG,0); // 0 jump boost, 1 gravity amp, 2 channel
      putProperty(stack,FALL_START_HEIGHT_TAG,Double.NaN);
      setPrefStack(stack);
   }
   
   @Override
   public ItemStack updateItem(ItemStack stack, MinecraftServer server){
      int mode = getIntProperty(stack,MODE_TAG);
      double fallHeight = getDoubleProperty(stack,FALL_START_HEIGHT_TAG);
      ItemStack newStack = super.updateItem(stack,server);
      putProperty(newStack,MODE_TAG,mode);
      putProperty(newStack,FALL_START_HEIGHT_TAG,fallHeight);
      return buildItemLore(newStack,server);
   }
   
   @Override
   public List<Text> getItemLore(@Nullable ItemStack itemStack){
      List<MutableText> lore = new ArrayList<>();
      lore.add(Text.literal("")
            .append(Text.literal("The ").formatted(Formatting.GRAY))
            .append(Text.literal("heavy core").formatted(Formatting.BLUE))
            .append(Text.literal(" has been brought to its ").formatted(Formatting.GRAY))
            .append(Text.literal("limit").formatted(Formatting.AQUA))
            .append(Text.literal(", becoming a ").formatted(Formatting.GRAY))
            .append(Text.literal("singularity").withColor(ArcanaColors.BETTER_DARK_BLUE))
            .append(Text.literal(".").formatted(Formatting.GRAY)));
      lore.add(Text.literal("")
            .append(Text.literal("The ").formatted(Formatting.GRAY))
            .append(Text.literal("maul ").withColor(ArcanaColors.BETTER_DARK_BLUE))
            .append(Text.literal("acts as a ").formatted(Formatting.GRAY))
            .append(Text.literal("mace ").formatted(Formatting.AQUA))
            .append(Text.literal("combined with an ").formatted(Formatting.GRAY))
            .append(Text.literal("axe").formatted(Formatting.AQUA))
            .append(Text.literal(".").formatted(Formatting.GRAY)));
      lore.add(Text.literal("")
            .append(Text.literal("The ").formatted(Formatting.GRAY))
            .append(Text.literal("maul ").withColor(ArcanaColors.BETTER_DARK_BLUE))
            .append(Text.literal("can ").formatted(Formatting.GRAY))
            .append(Text.literal("disable shields").formatted(Formatting.BLUE))
            .append(Text.literal(" and deal ").formatted(Formatting.GRAY))
            .append(Text.literal("massive damage").formatted(Formatting.AQUA))
            .append(Text.literal(" while falling.").formatted(Formatting.GRAY)));
      lore.add(Text.literal("")
            .append(Text.literal("The ").formatted(Formatting.GRAY))
            .append(Text.literal("maul's ").withColor(ArcanaColors.BETTER_DARK_BLUE))
            .append(Text.literal("gravity well").formatted(Formatting.BLUE))
            .append(Text.literal(" can ").formatted(Formatting.GRAY))
            .append(Text.literal("fell trees").formatted(Formatting.AQUA))
            .append(Text.literal(" in a single blow.").formatted(Formatting.GRAY)));
      lore.add(Text.literal("")
            .append(Text.literal("Right Clicking").formatted(Formatting.BLUE))
            .append(Text.literal(" with the ").formatted(Formatting.GRAY))
            .append(Text.literal("maul ").withColor(ArcanaColors.BETTER_DARK_BLUE))
            .append(Text.literal("launches ").formatted(Formatting.AQUA))
            .append(Text.literal("you in the ").formatted(Formatting.GRAY))
            .append(Text.literal("air").formatted(Formatting.AQUA))
            .append(Text.literal(".").formatted(Formatting.GRAY)));
      lore.add(Text.literal("")
            .append(Text.literal("Right Clicking in the air").formatted(Formatting.BLUE))
            .append(Text.literal(" ").formatted(Formatting.GRAY))
            .append(Text.literal("rockets ").formatted(Formatting.GRAY))
            .append(Text.literal("you ").formatted(Formatting.GRAY))
            .append(Text.literal("downward").formatted(Formatting.AQUA))
            .append(Text.literal(", causing a ").formatted(Formatting.GRAY))
            .append(Text.literal("powerful impact").withColor(ArcanaColors.BETTER_DARK_BLUE))
            .append(Text.literal(".").formatted(Formatting.GRAY)));
      
      return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   public void finalizePrefItem(MinecraftServer server){
      super.finalizePrefItem(server);
      ItemStack curPrefItem = this.getPrefItem();
      curPrefItem.set(DataComponentTypes.ENCHANTMENTS, MinecraftUtils.makeEnchantComponent(
            new EnchantmentLevelEntry(MinecraftUtils.getEnchantment(server.getRegistryManager(),Enchantments.BREACH),5)
      ));
      this.prefItem = buildItemLore(curPrefItem, server);
   }
   
   @Override
   public ItemStack forgeItem(Inventory inv, StarlightForgeBlockEntity starlightForge){
      ItemStack maceStack = inv.getStack(12); // Should be the Mace
      ItemStack newArcanaItem = getNewItem();
      
      newArcanaItem.addEnchantment(MinecraftUtils.getEnchantment(Enchantments.BREACH),5);
      if(maceStack.hasEnchantments()){
         for(RegistryEntry<Enchantment> enchantment : maceStack.getEnchantments().getEnchantments()){
            if(EnchantmentHelper.isCompatible(newArcanaItem.getEnchantments().getEnchantments(), enchantment)){
               newArcanaItem.addEnchantment(enchantment,maceStack.getEnchantments().getLevel(enchantment));
            }
         }
      }
      
      if(hasProperty(maceStack,EnhancedStatUtils.ENHANCED_STAT_TAG)){
         EnhancedStatUtils.enhanceItem(newArcanaItem,getDoubleProperty(maceStack,EnhancedStatUtils.ENHANCED_STAT_TAG));
      }
      
      return newArcanaItem;
   }
   
   public void treeFell(World world, PlayerEntity player, ItemStack item, BlockPos pos){
      if(!(world instanceof ServerWorld serverWorld)) return;
      Block type = world.getBlockState(pos).getBlock();
      if(!world.getBlockState(pos).isIn(BlockTags.LOGS)) return;
      
      int maxBlocks = 128;
      
      Queue<Pair<BlockPos, Integer>> queue = Lists.newLinkedList();
      Queue<BlockPos> visited = Lists.newLinkedList();
      queue.add(new Pair<>(pos, 0));
      ArrayList<BlockPos> toMine = new ArrayList<>();
      
      while(!queue.isEmpty()){
         Pair<BlockPos, Integer> pair = queue.poll();
         BlockPos blockPos = pair.getLeft();
         int depth = pair.getRight();
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
                     if(!(i==0 && j==0 && k==0)){
                        BlockPos pos2 = blockPos.add(i,j,k);
                        if(queue.stream().noneMatch(p -> p.getLeft().equals(pos2)) && !visited.contains(pos2)){
                           queue.add(new Pair<>(pos2,depth+1));
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
         drops.addAll(Block.getDroppedStacks(world.getBlockState(blockPos), serverWorld, blockPos, null, player, veinAxe));
         world.breakBlock(blockPos,false,player);
         if(type instanceof ExperienceDroppingBlock experienceBlock){
            experienceBlock.onStacksDropped(world.getBlockState(blockPos),serverWorld, pos, veinAxe,true);
         }
      }
      for(ItemStack stack : drops){
         Block.dropStack(world, pos, stack);
      }
   }
   
   private List<Entity> getAffectedEntities(ServerPlayerEntity player, boolean includePlayer, double range){
      List<Entity> entities = new ArrayList<>(player.getWorld().getOtherEntities(player, player.getBoundingBox().expand(range*2), e -> e.distanceTo(player) <= range));
      if(includePlayer) entities.add(player);
      return entities;
   }
   
   
   private void gravityEffects(ServerPlayerEntity player, ItemStack stack){
      int mode = getIntProperty(stack, MODE_TAG); // 0 - boost, 1 - fall, 2 - maelstrom
      boolean domain = ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.GRAVITIC_DOMAIN) > 0;
      
      if(mode == 0){
         for(Entity affectedEntity : domain ? getAffectedEntities(player,true, 3.5) : List.of(player)){
            affectedEntity.addVelocity(0,0.085,0);
            if(affectedEntity instanceof ServerPlayerEntity affectedPlayer){
               affectedPlayer.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(affectedPlayer));
               affectedPlayer.networkHandler.floatingTicks = 0;
            }
            
            if(affectedEntity.verticalCollision && !affectedEntity.groundCollision && affectedEntity instanceof LivingEntity livingEntity){
               livingEntity.damage(player.getWorld(),player.getDamageSources().flyIntoWall(),2.0f);
               if(livingEntity.getHealth() < 2.0 && livingEntity instanceof ServerPlayerEntity affectedPlayer){
                  ArcanaAchievements.grant(affectedPlayer,ArcanaAchievements.RAISE_THE_ROOF);
               }
            }
            
            player.getWorld().spawnParticles(ParticleTypes.TRIAL_SPAWNER_DETECTION_OMINOUS,affectedEntity.getX(),affectedEntity.getY()+affectedEntity.getHeight()/2,affectedEntity.getZ(),5,affectedEntity.getWidth()/2,affectedEntity.getHeight()/2,affectedEntity.getWidth()/2,0.01);
         }
      }else if(mode == 1){
         for(Entity affectedEntity : domain ? getAffectedEntities(player,true, 3.5) : List.of(player)){
            player.getWorld().spawnParticles(ParticleTypes.TRIAL_SPAWNER_DETECTION_OMINOUS,affectedEntity.getX(),affectedEntity.getY()+affectedEntity.getHeight()/2,affectedEntity.getZ(),5,affectedEntity.getWidth()/2,affectedEntity.getHeight()/2,affectedEntity.getWidth()/2,0.01);
         }
         
         if(player.isOnGround()){
            if(player.getItemUseTime() >= 11){
               double impactVel = MathHelper.clamp(BorisLib.PLAYER_MOVEMENT_TRACKER.get(player).velocity().getY(),-3,-0.5);
               double radius = 3 - impactVel; // 3.5 - 6 block range
               double totalDmg = 0;
               for(Entity affectedEntity : getAffectedEntities(player,false, radius)){
                  Vec3d diff = player.getPos().subtract(affectedEntity.getPos());
                  double multiplier = MathHelper.clamp(diff.length()*.2,.03,2);
                  Vec3d motion = diff.normalize().multiply(-multiplier,0,-multiplier).add(0,radius*0.15,0);
                  affectedEntity.addVelocity(motion.x,motion.y,motion.z);
                  if(affectedEntity instanceof ServerPlayerEntity affectedPlayer){
                     affectedPlayer.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(affectedPlayer));
                  }
                  if(affectedEntity instanceof LivingEntity livingEntity){
                     livingEntity.damage(player.getWorld(),player.getDamageSources().fallingBlock(player),(float)radius);
                     totalDmg += radius;
                  }
               }
               ArcanaNovum.data(player).addXP((int) Math.min(ArcanaConfig.getInt(ArcanaRegistry.GRAVITON_MAUL_IMPACT_DAMAGE_PER_10) * totalDmg / 10,ArcanaConfig.getInt(ArcanaRegistry.GRAVITON_MAUL_IMPACT_DAMAGE_CAP)));
               ArcanaEffectUtils.gravitonMaulSlam(player.getWorld(), player.getSteppingPos(),radius,0);
               player.stopUsingItem();
               player.getItemCooldownManager().set(stack,40);
            }else{
               player.stopUsingItem();
               player.getItemCooldownManager().set(stack,5);
            }
            double fallStart = getDoubleProperty(stack,FALL_START_HEIGHT_TAG);
            if(!Double.isNaN(fallStart)){
               if(fallStart - player.getY() >= 300) ArcanaAchievements.grant(player,ArcanaAchievements.QUICK_WAY_DOWN);
               putProperty(stack,FALL_START_HEIGHT_TAG,Double.NaN);
            }
         }
      }else if(mode == 2){
         for(Entity affectedEntity : getAffectedEntities(player,false, 5.5)){
            Vec3d diff = affectedEntity.getPos().subtract(player.getPos().add(0,0.1,0));
            double multiplier = MathHelper.clamp(diff.length()*.2,.03,2);
            Vec3d motion = diff.add(0,0,0).normalize().multiply(-multiplier);
            affectedEntity.setVelocity(motion.x,motion.y,motion.z);
            if(affectedEntity instanceof ServerPlayerEntity affectedPlayer){
               affectedPlayer.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(affectedPlayer));
            }
            
            if(affectedEntity instanceof LivingEntity livingEntity){
               StatusEffectInstance amp = new StatusEffectInstance(ArcanaRegistry.DAMAGE_AMP_EFFECT, 10, 1, false, true, true);
               livingEntity.addStatusEffect(amp);
               livingEntity.damage(player.getWorld(),player.getDamageSources().cramming(),1.0f);
            }
         }
         ArcanaEffectUtils.gravitonMaulMaelstrom(player,player.getItemUseTime());
         
         StatusEffectInstance res = new StatusEffectInstance(StatusEffects.RESISTANCE, 10, 2, false, false, true);
         StatusEffectInstance slow = new StatusEffectInstance(StatusEffects.SLOWNESS, 10, 3, false, false, true);
         player.addStatusEffect(res);
         player.addStatusEffect(slow);
      }
   }
   
   @Override
   protected ArcanaRecipe makeRecipe(){
      ArcanaIngredient a = new ArcanaIngredient(Items.CRYING_OBSIDIAN,32);
      ArcanaIngredient b = new ArcanaIngredient(Items.OBSIDIAN,32);
      ArcanaIngredient k = new ArcanaIngredient(Items.ENCHANTED_BOOK,1).withEnchantments(new EnchantmentLevelEntry(MinecraftUtils.getEnchantment(Enchantments.BREACH),4));
      ArcanaIngredient g = new ArcanaIngredient(Items.COBWEB,32);
      ArcanaIngredient h = new ArcanaIngredient(Items.NETHERITE_INGOT,3);
      ArcanaIngredient c = new ArcanaIngredient(Items.BREEZE_ROD,32);
      ArcanaIngredient l = new ArcanaIngredient(Items.NETHER_STAR,3);
      ArcanaIngredient m = new ArcanaIngredient(Items.MACE,1,true);
      
      ArcanaIngredient[][] ingredients = {
            {a,b,c,b,a},
            {b,g,h,g,b},
            {k,l,m,l,k},
            {b,g,h,g,b},
            {a,b,c,b,a}};
      return new ArcanaRecipe(ingredients,new ForgeRequirement().withAnvil().withCore().withEnchanter());
   }
   
   @Override
   public List<List<Text>> getBookLore(){
      List<List<Text>> list = new ArrayList<>();
      list.add(List.of(Text.literal("   Graviton Maul").formatted(Formatting.BOLD).withColor(ArcanaColors.BETTER_DARK_BLUE),Text.literal("\nRarity: ").formatted(Formatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(),false)),Text.literal("\nAfter much experimentation with the Heavy Core, I have brought it to its apex. A singularity, a point of infinite density, contained by an Arcane casing. Gravity itself is at my will.").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal("   Graviton Maul").formatted(Formatting.BOLD).withColor(ArcanaColors.BETTER_DARK_BLUE),Text.literal("\nThe Maul is an empowered mace that has damage that scales with fall height. To aid in this ability, Using the Maul while on the ground causes a localized lapse in gravity, sending me into the air.").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal("   Graviton Maul").formatted(Formatting.BOLD).withColor(ArcanaColors.BETTER_DARK_BLUE),Text.literal("\nHowever, I have to be careful not to hit my head on the ceiling. As to not give my foe time to react, I can Use the Maul in the air to amplify gravity and rapidly send me downwards. Impacting the ground causes damage to nearby creatures. ").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal("   Graviton Maul").formatted(Formatting.BOLD).withColor(ArcanaColors.BETTER_DARK_BLUE),Text.literal("\nThe Maul’s immense gravity also gives it the heft to fell entire trees at once and bust through shields.").formatted(Formatting.BLACK)));
      return list;
   }
   
   public class GravitonMaulItem extends ArcanaPolymerMaceItem {
      public GravitonMaulItem(){
         super(getThis(),getEquipmentArcanaItemComponents()
               .component(DataComponentTypes.TOOL, new ToolComponent(List.of(), 1.0F, 2, false))
               .component(DataComponentTypes.CONSUMABLE, ConsumableComponent.builder().consumeSeconds(72000).useAction(UseAction.BOW).sound(Registries.SOUND_EVENT.getEntry(SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME)).build())
               .component(DataComponentTypes.WEAPON, new WeaponComponent(1, 7.5F))
               .attributeModifiers(AttributeModifiersComponent.builder()
                     .add(EntityAttributes.ATTACK_DAMAGE, new EntityAttributeModifier(BASE_ATTACK_DAMAGE_MODIFIER_ID, 5.0, EntityAttributeModifier.Operation.ADD_VALUE), AttributeModifierSlot.MAINHAND)
                     .add(EntityAttributes.ATTACK_SPEED, new EntityAttributeModifier(BASE_ATTACK_SPEED_MODIFIER_ID, -3.4F, EntityAttributeModifier.Operation.ADD_VALUE), AttributeModifierSlot.MAINHAND)
                     .add(EntityAttributes.FALL_DAMAGE_MULTIPLIER, new EntityAttributeModifier(Identifier.of(MOD_ID,id), -0.80F, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL), AttributeModifierSlot.MAINHAND)
                     .build())
               .enchantable(15)
         );
      }
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
      
      @Override
      public void inventoryTick(ItemStack stack, ServerWorld world, Entity entity, @Nullable EquipmentSlot slot){
         if(!ArcanaItemUtils.isArcane(stack)) return;
         if(!(world instanceof ServerWorld && entity instanceof ServerPlayerEntity player)) return;
      }
      
      @Override
      public ActionResult use(World world, PlayerEntity playerEntity, Hand hand){
         ItemStack stack = playerEntity.getStackInHand(hand);
         if(!(playerEntity instanceof ServerPlayerEntity player)) return ActionResult.PASS;
         
         if(player.isOnGround()){
            boolean maelstrom = ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.SINGULARITY_MAELSTROM) > 0;
            if(maelstrom && player.isSneaking()){
               putProperty(stack,MODE_TAG,2);
            }else{
               putProperty(stack,MODE_TAG,0);
               boolean domain = ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.GRAVITIC_DOMAIN) > 0;
               for(Entity affectedEntity : domain ? getAffectedEntities(player,true, 3.5) : List.of(player)){
                  if(affectedEntity.getVelocity().getY() < 0.75) affectedEntity.setVelocity(affectedEntity.getVelocity().multiply(1,0,1).add(0,0.75,0));
                  if(affectedEntity instanceof ServerPlayerEntity affectedPlayer){
                     affectedPlayer.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(affectedPlayer));
                  }
               }
               ArcanaEffectUtils.circle(player.getWorld(),null,player.getPos().add(0,0.25,0), ParticleTypes.TRIAL_SPAWNER_DETECTION_OMINOUS,domain ? 3.5 : 0.5,40,5,0.1,0.01);
            }
         }else{
            putProperty(stack,MODE_TAG,1);
            putProperty(stack,FALL_START_HEIGHT_TAG,player.getY());
            boolean domain = ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.GRAVITIC_DOMAIN) > 0;
            for(Entity affectedEntity : domain ? getAffectedEntities(player,true, 3.5) : List.of(player)){
               if(affectedEntity.getVelocity().getY() > -1) affectedEntity.setVelocity(affectedEntity.getVelocity().multiply(1,0,1).add(0,-1,0));
               if(affectedEntity instanceof ServerPlayerEntity affectedPlayer){
                  affectedPlayer.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(affectedPlayer));
               }
            }
            ArcanaEffectUtils.circle(player.getWorld(),null,player.getPos().add(0,0.25,0), ParticleTypes.TRIAL_SPAWNER_DETECTION_OMINOUS,domain ? 3.5 : 0.5,40,5,0.1,0.01);
         }
         gravityEffects(player,stack);
         playerEntity.setCurrentHand(hand);
         return ActionResult.CONSUME;
      }
      
      @Override
      public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks){
         if(!(user instanceof ServerPlayerEntity player)) return;
         gravityEffects(player,stack);
      }
      
      @Override
      public int getMaxUseTime(ItemStack stack, LivingEntity user) {
         return 72000;
      }
      
      @Override
      public UseAction getUseAction(ItemStack stack) {
         return UseAction.BOW;
      }
   }
}