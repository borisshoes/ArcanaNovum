package net.borisshoes.arcananovum.items;

import net.borisshoes.arcananovum.ArcanaConfig;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.achievements.TimedAchievement;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.blocks.forge.StarlightForgeBlockEntity;
import net.borisshoes.arcananovum.blocks.forge.TwilightAnvilBlockEntity;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.core.EnergyItem;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerSwordItem;
import net.borisshoes.arcananovum.damage.ArcanaDamageTypes;
import net.borisshoes.arcananovum.events.BinaryBladesMaxEnergyEvent;
import net.borisshoes.arcananovum.gui.arcanetome.TomeGui;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaIngredient;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaRecipe;
import net.borisshoes.arcananovum.recipes.arcana.ForgeRequirement;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.*;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.*;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.ToolMaterial;
import net.minecraft.item.consume.UseAction;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.network.packet.s2c.play.EntityAnimationS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.ArrayList;
import java.util.List;
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
      categories = new TomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), TomeGui.TomeFilter.EQUIPMENT};
      itemVersion = 0;
      vanillaItem = Items.IRON_SWORD;
      item = new BinaryBladesItem(addArcanaItemComponents(new Item.Settings().maxCount(1).maxDamage(1024)
            .component(DataComponentTypes.UNBREAKABLE,new UnbreakableComponent(false))
      ));
      displayName = Text.translatableWithFallback("item."+MOD_ID+"."+ID,name).formatted(Formatting.YELLOW,Formatting.BOLD);
      researchTasks = new RegistryKey[]{ResearchTasks.OBTAIN_STARDUST, ResearchTasks.INFUSE_ITEM, ResearchTasks.OBTAIN_NETHERITE_SWORD, ResearchTasks.OBTAIN_NETHER_STAR, ResearchTasks.UNLOCK_STELLAR_CORE};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getMaxCount());
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
   public List<Text> getItemLore(@Nullable ItemStack itemStack){
      List<MutableText> lore = new ArrayList<>();
      lore.add(Text.literal("")
            .append(Text.literal("Two ").formatted(Formatting.GRAY))
            .append(Text.literal("blades ").formatted(Formatting.RED))
            .append(Text.literal("forged by ").formatted(Formatting.GRAY))
            .append(Text.literal("starlight").formatted(Formatting.AQUA))
            .append(Text.literal(", bound together like ").formatted(Formatting.GRAY))
            .append(Text.literal("twin stars").formatted(Formatting.YELLOW))
            .append(Text.literal(".").formatted(Formatting.GRAY)));
      lore.add(Text.literal("")
            .append(Text.literal("Wielding the ").formatted(Formatting.GRAY))
            .append(Text.literal("blade ").formatted(Formatting.RED))
            .append(Text.literal("splits it in two").formatted(Formatting.YELLOW))
            .append(Text.literal(", and ").formatted(Formatting.GRAY))
            .append(Text.literal("rejoins ").formatted(Formatting.GOLD))
            .append(Text.literal("after ").formatted(Formatting.GRAY))
            .append(Text.literal("combat").formatted(Formatting.RED))
            .append(Text.literal(".").formatted(Formatting.GRAY)));
      lore.add(Text.literal("")
            .append(Text.literal("With each ").formatted(Formatting.GRAY))
            .append(Text.literal("strike ").formatted(Formatting.GOLD))
            .append(Text.literal("the ").formatted(Formatting.GRAY))
            .append(Text.literal("swords ").formatted(Formatting.RED))
            .append(Text.literal("harmonize together, ").formatted(Formatting.GRAY))
            .append(Text.literal("empowering ").formatted(Formatting.YELLOW))
            .append(Text.literal("your ").formatted(Formatting.GRAY))
            .append(Text.literal("strikes").formatted(Formatting.GOLD))
            .append(Text.literal(".").formatted(Formatting.GRAY)));
      lore.add(Text.literal("")
            .append(Text.literal("Attacking ").formatted(Formatting.RED))
            .append(Text.literal("an ").formatted(Formatting.GRAY))
            .append(Text.literal("enemy ").formatted(Formatting.GOLD))
            .append(Text.literal("grants ").formatted(Formatting.GRAY))
            .append(Text.literal("ramping ").formatted(Formatting.RED))
            .append(Text.literal("movement ").formatted(Formatting.AQUA))
            .append(Text.literal("and ").formatted(Formatting.GRAY))
            .append(Text.literal("attack speed").formatted(Formatting.YELLOW))
            .append(Text.literal(".").formatted(Formatting.GRAY)));
      
      return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   public ItemStack forgeItem(Inventory inv, StarlightForgeBlockEntity starlightForge){
      ItemStack sword1 = inv.getStack(6);
      ItemStack sword2 = inv.getStack(18);
      ItemStack combinedSword = sword1.copy();
      
      if(starlightForge.getWorld() instanceof ServerWorld serverWorld){
         TwilightAnvilBlockEntity twilightAnvil;
         if((twilightAnvil = (TwilightAnvilBlockEntity) starlightForge.getForgeAddition(serverWorld, ArcanaRegistry.TWILIGHT_ANVIL_BLOCK_ENTITY)) != null){
            TwilightAnvilBlockEntity.AnvilOutputSet outputSet = twilightAnvil.calculateOutput(sword1,sword2);
            if(!outputSet.output().isEmpty()){
               combinedSword = outputSet.output().copy();
            }
         }
      }
      
      ItemStack newArcanaItem = getNewItem();
      if(combinedSword.hasEnchantments()){
         EnchantmentHelper.set(newArcanaItem,combinedSword.getEnchantments());
      }
      
      if(hasProperty(combinedSword,EnhancedStatUtils.ENHANCED_STAT_TAG)){
         EnhancedStatUtils.enhanceItem(newArcanaItem,getDoubleProperty(combinedSword,EnhancedStatUtils.ENHANCED_STAT_TAG));
      }
      
      return newArcanaItem;
   }
   
   private ItemStack getFakeItem(ItemStack item){
      if(!(ArcanaItemUtils.identifyItem(item) instanceof BinaryBlades)) return item;
      ItemStack fake = item.copy();
      putProperty(fake,FAKE_TAG,true);
      putProperty(fake,UUID_TAG, ArcanaNovum.BLANK_UUID);
      
      boolean white = ArcanaAugments.getAugmentOnItem(fake, ArcanaAugments.WHITE_DWARF_BLADES.id) > 0;
      if(white){
         fake.set(DataComponentTypes.CONSUMABLE,ConsumableComponent.builder().consumeSeconds(72000).useAction(UseAction.BLOCK).sound(Registries.SOUND_EVENT.getEntry(SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME)).build());
      }
      fake.remove(DataComponentTypes.ATTRIBUTE_MODIFIERS);
      fake.remove(DataComponentTypes.USE_COOLDOWN);
      
      return fake;
   }
   
   public void rebuildAttributes(ItemStack stack){
      if(!stack.isOf(this.item)) return;
      int energy = getEnergy(stack);
      AttributeModifiersComponent modifiers = stack.getOrDefault(DataComponentTypes.ATTRIBUTE_MODIFIERS, AttributeModifiersComponent.DEFAULT);
      List<AttributeModifiersComponent.Entry> attributeList = new ArrayList<>();
      boolean redGiant = ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.RED_GIANT_BLADES) > 0;
      
      // Strip old movement and attack speed stats
      for(AttributeModifiersComponent.Entry entry : modifiers.modifiers()){
         EntityAttributeModifier modifier = entry.modifier();
         
         if(modifier.id().toString().contains(MOVE_SPEED_TAG) || modifier.id().toString().contains(ATTACK_SPEED_TAG) || modifier.id().toString().contains(ATTACK_DAMAGE_TAG)){
            continue;
         }
         attributeList.add(entry);
      }
      
      if(energy > 0){
         attributeList.add(new AttributeModifiersComponent.Entry(EntityAttributes.MOVEMENT_SPEED,new EntityAttributeModifier(Identifier.of(ArcanaNovum.MOD_ID,MOVE_SPEED_TAG),0.5 * getEnergy(stack) / getMaxEnergy(stack),EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE),AttributeModifierSlot.MAINHAND));
         attributeList.add(new AttributeModifiersComponent.Entry(EntityAttributes.ATTACK_SPEED,new EntityAttributeModifier(Identifier.of(ArcanaNovum.MOD_ID,ATTACK_SPEED_TAG),(double) getEnergy(stack) / getMaxEnergy(stack),EntityAttributeModifier.Operation.ADD_VALUE),AttributeModifierSlot.MAINHAND));
         if(redGiant && energy >= 50){
            attributeList.add(new AttributeModifiersComponent.Entry(EntityAttributes.ATTACK_DAMAGE,new EntityAttributeModifier(Identifier.of(ArcanaNovum.MOD_ID,ATTACK_DAMAGE_TAG),4 * (getEnergy(stack) - 50.0) / (getMaxEnergy(stack) - 50.0),EntityAttributeModifier.Operation.ADD_VALUE),AttributeModifierSlot.MAINHAND));
         }
      }
      
      AttributeModifiersComponent newComponent = new AttributeModifiersComponent(attributeList,false);
      stack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS,newComponent);
   }
   
   private Formatting getColor(ItemStack stack){
      boolean pulsar = ArcanaAugments.getAugmentOnItem(stack, ArcanaAugments.PULSAR_BLADES.id) > 0;
      boolean white = ArcanaAugments.getAugmentOnItem(stack, ArcanaAugments.WHITE_DWARF_BLADES.id) > 0;
      boolean red = ArcanaAugments.getAugmentOnItem(stack, ArcanaAugments.RED_GIANT_BLADES.id) > 0;
      if(pulsar) return Formatting.AQUA;
      if(white) return Formatting.WHITE;
      if(red) return Formatting.RED;
      return Formatting.YELLOW;
   }
   
   public static boolean isFakeBlade(ItemStack stack){
      return stack.isOf(ArcanaRegistry.BINARY_BLADES.getItem()) && ArcanaItem.getBooleanProperty(stack,FAKE_TAG);
   }
   
   @Override
   public int getMaxEnergy(ItemStack item){
      return 100;
   }
   
   @Override
   protected ArcanaRecipe makeRecipe(){
      ArcanaIngredient a = new ArcanaIngredient(Items.GLOWSTONE_DUST,32);
      ArcanaIngredient b = new ArcanaIngredient(ArcanaRegistry.STARDUST,32);
      ArcanaIngredient c = new ArcanaIngredient(Items.DIAMOND,8);
      ArcanaIngredient e = new ArcanaIngredient(Items.NETHER_STAR,4);
      ArcanaIngredient g = new ArcanaIngredient(Items.NETHERITE_SWORD,1,true);
      ArcanaIngredient h = new ArcanaIngredient(Items.BLAZE_POWDER,48);
      ArcanaIngredient m = new ArcanaIngredient(Items.NETHERITE_INGOT,4);
      
      ArcanaIngredient[][] ingredients = {
            {a,b,c,b,e},
            {b,g,h,a,b},
            {c,h,m,h,c},
            {b,a,h,g,b},
            {e,b,c,b,a}};
      return new ArcanaRecipe(ingredients,new ForgeRequirement().withAnvil().withCore());
   }
   
   @Override
   public List<List<Text>> getBookLore(){
      List<List<Text>> list = new ArrayList<>();
      list.add(List.of(Text.literal("   Binary Blades").formatted(Formatting.GOLD,Formatting.BOLD),Text.literal("\nRarity: ").formatted(Formatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(),false)),Text.literal("\nGazing up at the stars one night led me to observe two close stars dancing in the sky. Two stars harmoniously acting as one. Glancing over at my Forge gave me an idea.").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal("   Binary Blades").formatted(Formatting.GOLD,Formatting.BOLD),Text.literal("\nThe Binary Blades are two swords that act in unison, combining into one when stored and splitting in two when held. They grant an increased attack speed compared to normal weapons, with each strike acting like a note in a harmony. ").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal("   Binary Blades").formatted(Formatting.GOLD,Formatting.BOLD),Text.literal("\nSuccessive strikes grant the wielder increased movement and attack speed.").formatted(Formatting.BLACK)));
      return list;
   }
   
   public class BinaryBladesItem extends ArcanaPolymerSwordItem {
      public BinaryBladesItem(Item.Settings settings){
         super(getThis(), ToolMaterial.NETHERITE, 2,-1.2f,settings);
      }
      
      @Override
      public ItemStack getPolymerItemStack(ItemStack itemStack, TooltipType tooltipType, PacketContext context){
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
         
         baseStack.set(DataComponentTypes.CUSTOM_MODEL_DATA,new CustomModelDataComponent(new ArrayList<>(),new ArrayList<>(),stringList,new ArrayList<>()));
         return baseStack;
      }
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
      
      @Override
      public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected){
         if(!ArcanaItemUtils.isArcane(stack)) return;
         if(!(world instanceof ServerWorld && entity instanceof ServerPlayerEntity player)) return;
         
         boolean fake = getBooleanProperty(stack,FAKE_TAG);
         if(fake){
            if(!player.getOffHandStack().equals(stack) || !player.getMainHandStack().isOf(this)){
               stack.setCount(0);
               ArcanaNovum.data(player).restoreOffhand();
            }else{
               ItemStack mainStack = player.getMainHandStack();
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
         ItemStack handStack = player.getMainHandStack();
         int energy = getEnergy(stack);
         
         if(handStack.equals(stack)){
            if(!split){
               putProperty(stack,SPLIT_TAG,true);
            }
            if(!player.getOffHandStack().isOf(this)){
               ArcanaNovum.data(player).restoreOffhand();
               ArcanaNovum.data(player).storeOffhand(getFakeItem(stack));
            }
            
            if(world.getServer().getTicks() % 40 == 0 || (energy > 0 && energy < getMaxEnergy(stack))){
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
               player.sendMessage(Text.literal(message.toString()).formatted(getColor(stack)), true);
            }
         }else if(split){
            putProperty(stack,SPLIT_TAG,false);
         }
         
         if(world.getServer().getTicks() % 5 == 0){
            int redGiant = ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.RED_GIANT_BLADES);
            if(energy >= 50 && redGiant >= 2){
               List<Entity> entities = new ArrayList<>(player.getServerWorld().getOtherEntities(player, player.getBoundingBox().expand(8), e -> e.distanceTo(player) <= 3.5));
               for(Entity nearEntity : entities){
                  if(nearEntity instanceof LivingEntity living && !nearEntity.isFireImmune()){
                     living.setOnFireFor(3);
                  }
               }
               ParticleEffectUtils.circle(player.getServerWorld(),null,player.getPos().add(0,0.2,0), ParticleTypes.FLAME,0.5,8,2,0.1,0.05);
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
            ArcanaNovum.addArcanaEvent(new BinaryBladesMaxEnergyEvent(player));
            long count = ArcanaNovum.getEventsOfType(BinaryBladesMaxEnergyEvent.class).stream().filter(event -> event.getPlayer().equals(player)).count();
            if(count >= ((TimedAchievement) ArcanaAchievements.STARBURST_STREAM).getGoal()){
               ArcanaAchievements.grant(player,ArcanaAchievements.STARBURST_STREAM);
            }
            if(world.getServer().getTicks() % 20 == 0){
               ArcanaNovum.data(player).addXP(ArcanaConfig.getInt(ArcanaRegistry.BINARY_BLADES_MAX_ENERGY_PER_SECOND));
            }
         }
         
         rebuildAttributes(stack);
      }
      
      @Override
      public ActionResult use(World world, PlayerEntity playerEntity, Hand hand){
         ItemStack stack = playerEntity.getStackInHand(hand);
         if(!(playerEntity instanceof ServerPlayerEntity player)) return ActionResult.PASS;
         
         boolean fake = getBooleanProperty(stack,FAKE_TAG);
         boolean split = getBooleanProperty(stack,SPLIT_TAG);
         boolean whiteDwarf = ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.WHITE_DWARF_BLADES) > 0;
         if(fake && whiteDwarf){
            playerEntity.setCurrentHand(hand);
            return ActionResult.CONSUME;
         }else if(fake || !split){
            return ActionResult.PASS;
         }
         
         int pulsar = ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.PULSAR_BLADES);
         int energy = getEnergy(stack);
         int energyCost = (25*(3-pulsar));
         int energyGain = 0;
         
         if(pulsar > 0 && energy > energyCost){
            MiscUtils.LasercastResult lasercast = MiscUtils.lasercast(world, player.getEyePos(), player.getRotationVecClient(), 25, true, player);
            float damage = pulsar * 7;
            for(Entity hit : lasercast.sortedHits()){
               if(hit instanceof ServerPlayerEntity hitPlayer && hitPlayer.isBlocking()){
                  double dp = hitPlayer.getRotationVecClient().normalize().dotProduct(lasercast.direction().normalize());
                  if(dp < -0.6){
                     MiscUtils.blockWithShield(hitPlayer,damage);
                     continue;
                  }
               }
               hit.damage(player.getServerWorld(), ArcanaDamageTypes.of(player.getEntityWorld(),ArcanaDamageTypes.PHOTONIC,player), damage);
               ArcanaItem.putProperty(stack,BinaryBlades.LAST_HIT_TAG,20);
               energyGain += 10;
            }
            ParticleEffectUtils.pulsarBladeShoot(player.getServerWorld(),player.getEyePos().subtract(0,player.getHeight()/4,0),lasercast.endPos(),0);
            SoundUtils.playSound(player.getServerWorld(),player.getBlockPos(),SoundEvents.BLOCK_BEACON_DEACTIVATE, SoundCategory.PLAYERS,1.0f,2.0f);
            addEnergy(stack, energyGain-energyCost);
            player.getItemCooldownManager().set(stack,10);
            player.getServerWorld().getChunkManager().sendToNearbyPlayers(player, new EntityAnimationS2CPacket(player, EntityAnimationS2CPacket.SWING_OFF_HAND));
            return ActionResult.SUCCESS;
         }
         
         return ActionResult.PASS;
      }
      
      @Override
      public boolean onClicked(ItemStack stack, ItemStack otherStack, Slot slot, ClickType clickType, PlayerEntity player, StackReference cursorStackReference){
         boolean superRet = super.onClicked(stack, otherStack, slot, clickType, player, cursorStackReference);
         if(getBooleanProperty(stack,FAKE_TAG)){
            stack.copyAndEmpty();
            ArcanaNovum.data(player).restoreOffhand();
            return false;
         }
         return superRet;
      }
      
      @Override
      public int getMaxUseTime(ItemStack stack, LivingEntity user) {
         boolean whiteDwarf = ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.WHITE_DWARF_BLADES) > 0;
         boolean fake = getBooleanProperty(stack,FAKE_TAG);
         return whiteDwarf && fake ? 72000 : super.getMaxUseTime(stack,user);
      }
      
      @Override
      public UseAction getUseAction(ItemStack stack) {
         boolean whiteDwarf = ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.WHITE_DWARF_BLADES) > 0;
         boolean fake = getBooleanProperty(stack,FAKE_TAG);
         return whiteDwarf && fake ? UseAction.BLOCK : UseAction.NONE;
      }
      
      @Override
      public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks){
         super.usageTick(world, user, stack, remainingUseTicks);
      }
   }
}
