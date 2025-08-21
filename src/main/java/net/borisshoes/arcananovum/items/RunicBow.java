package net.borisshoes.arcananovum.items;

import net.borisshoes.arcananovum.ArcanaConfig;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.blocks.forge.StarlightForgeBlockEntity;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerBowItem;
import net.borisshoes.arcananovum.gui.arcanetome.TomeGui;
import net.borisshoes.arcananovum.items.arrows.PhotonicArrows;
import net.borisshoes.arcananovum.items.arrows.RunicArrow;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaIngredient;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaRecipe;
import net.borisshoes.arcananovum.recipes.arcana.ForgeRequirement;
import net.borisshoes.arcananovum.recipes.arcana.GenericArcanaIngredient;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.*;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class RunicBow extends ArcanaItem {
	public static final String ID = "runic_bow";

   public RunicBow(){
      id = ID;
      name = "Runic Bow";
      rarity = ArcanaRarity.SOVEREIGN;
      categories = new TomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), TomeGui.TomeFilter.EQUIPMENT};
      vanillaItem = Items.BOW;
      item = new RunicBowItem();
      displayName = Text.translatableWithFallback("item."+MOD_ID+"."+ID,name).formatted(Formatting.BOLD,Formatting.LIGHT_PURPLE);
      researchTasks = new RegistryKey[]{ResearchTasks.ADVANCEMENT_SHOOT_ARROW,ResearchTasks.OBTAIN_NETHERITE_INGOT,ResearchTasks.UNLOCK_RADIANT_FLETCHERY,ResearchTasks.UNLOCK_MIDNIGHT_ENCHANTER,ResearchTasks.UNLOCK_STELLAR_CORE,ResearchTasks.UNLOCK_RUNIC_MATRIX,ResearchTasks.ADVANCEMENT_SNIPER_DUEL,ResearchTasks.ADVANCEMENT_BULLSEYE};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getMaxCount());
      setPrefStack(stack);
   }
   
   @Override
   public void finalizePrefItem(MinecraftServer server){
      super.finalizePrefItem(server);
      ItemStack curPrefItem = this.getPrefItem();
      curPrefItem.set(DataComponentTypes.ENCHANTMENTS, MiscUtils.makeEnchantComponent(
            new EnchantmentLevelEntry(MiscUtils.getEnchantment(server.getRegistryManager(),Enchantments.POWER),7)
      ));
      this.prefItem = buildItemLore(curPrefItem, server);
   }
   
   @Override
   public List<Text> getItemLore(@Nullable ItemStack itemStack){
      List<MutableText> lore = new ArrayList<>();
      lore.add(Text.literal("")
            .append(Text.literal("The ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("Runic Bow").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal(" makes use of the ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("Runic Matrix").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal(" to create ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("unique effects").formatted(Formatting.AQUA))
            .append(Text.literal(".").formatted(Formatting.DARK_PURPLE)));
      lore.add(Text.literal("")
            .append(Text.literal("The ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("Runic Bow").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal(" can fire and ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("activate ").formatted(Formatting.ITALIC,Formatting.DARK_AQUA))
            .append(Text.literal("the effects of ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("Runic Arrows").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal(".").formatted(Formatting.DARK_PURPLE)));
      lore.add(Text.literal("")
            .append(Text.literal("The ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("bow").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal(" also acts as a ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("normal bow").formatted(Formatting.YELLOW))
            .append(Text.literal(" with ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("Power VII").formatted(Formatting.AQUA))
            .append(Text.literal(" and is ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("unbreakable").formatted(Formatting.BLUE))
            .append(Text.literal(".").formatted(Formatting.DARK_PURPLE)));
     return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   public ItemStack updateItem(ItemStack stack, MinecraftServer server){
      ItemStack newStack = super.updateItem(stack,server);
      return buildItemLore(newStack,server);
   }
   
   @Override
   public ItemStack forgeItem(Inventory inv, StarlightForgeBlockEntity starlightForge){
      ItemStack bowStack = inv.getStack(12); // Should be the Bow
      ItemStack newArcanaItem = getNewItem();
      
      if(bowStack.hasEnchantments()){
         EnchantmentHelper.set(newArcanaItem,bowStack.getEnchantments());
      }
      newArcanaItem.addEnchantment(MiscUtils.getEnchantment(Enchantments.POWER),7);
      
      if(hasProperty(bowStack, EnhancedStatUtils.ENHANCED_STAT_TAG)){
         EnhancedStatUtils.enhanceItem(newArcanaItem,getDoubleProperty(bowStack,EnhancedStatUtils.ENHANCED_STAT_TAG));
      }
      
      return newArcanaItem;
   }
   
   @Override
	protected ArcanaRecipe makeRecipe(){
      ArcanaIngredient a = new ArcanaIngredient(Items.NETHER_STAR,2);
      ArcanaIngredient b = new ArcanaIngredient(Items.AMETHYST_SHARD,32);
      ArcanaIngredient c = new ArcanaIngredient(Items.ENCHANTED_BOOK,1).withEnchantments(new EnchantmentLevelEntry(MiscUtils.getEnchantment(Enchantments.POWER),5));
      ArcanaIngredient d = new ArcanaIngredient(Items.END_CRYSTAL,24);
      ArcanaIngredient g = new ArcanaIngredient(Items.NETHERITE_INGOT,1);
      GenericArcanaIngredient h = new GenericArcanaIngredient(ArcanaRegistry.RUNIC_MATRIX,1);
      ArcanaIngredient m = new ArcanaIngredient(Items.BOW,1, true);
      
      ArcanaIngredient[][] ingredients = {
            {a,b,c,d,a},
            {b,g,h,g,d},
            {c,h,m,h,c},
            {d,g,h,g,b},
            {a,d,c,b,a}};
      return new ArcanaRecipe(ingredients,new ForgeRequirement().withAnvil().withFletchery().withEnchanter().withCore());
   }
   
   @Override
   public List<List<Text>> getBookLore(){
      List<List<Text>> list = new ArrayList<>();
      list.add(List.of(Text.literal("      Runic Bow").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD),Text.literal("\nRarity: ").formatted(Formatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(),false)),Text.literal("\nThe Runic Bow is truly a masterpiece of adaptive Arcana. The integrated Runic Matrices reconfigure the Bow’s ethereal structure based on the projectile being fired to unlock its Arcane effects.").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal("      Runic Bow").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD),Text.literal("\nThe Runic Bow is capable of utilizing Runic Arrows and activating their special abilities. \n\nThe Bow also enhances normal arrows to do more damage than a traditional enchanted bow.").formatted(Formatting.BLACK)));
      return list;
   }
   
   public class RunicBowItem extends ArcanaPolymerBowItem {
      public static final Predicate<ItemStack> RUNIC_BOW_PROJECTILES = stack -> (stack.isIn(ItemTags.ARROWS) || ArcanaItemUtils.identifyItem(stack) instanceof RunicArrow);
      public static final float[] STABILITY = new float[]{1f, .75f, .5f, 0f};
      
      public RunicBowItem(){
         super(getThis(),getEquipmentArcanaItemComponents());
      }
      
      @Override
      public ItemStack getPolymerItemStack(ItemStack itemStack, TooltipType tooltipType, PacketContext context){
         ItemStack baseStack = super.getPolymerItemStack(itemStack, tooltipType, context);
         if(!ArcanaItemUtils.isArcane(itemStack)) return baseStack;
         
         List<String> stringList = new ArrayList<>();
         int tier = ArcanaAugments.getAugmentOnItem(itemStack,ArcanaAugments.BOW_ACCELERATION.id);
         if(tier > 0) stringList.add("accel_"+tier);
         baseStack.set(DataComponentTypes.CUSTOM_MODEL_DATA,new CustomModelDataComponent(new ArrayList<>(),new ArrayList<>(),stringList,new ArrayList<>()));
         return baseStack;
      }
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
      
      @Override
      public Predicate<ItemStack> getProjectiles(){
         return RUNIC_BOW_PROJECTILES;
      }
      
      @Override
      public boolean onStoppedUsing(ItemStack bow, World world, LivingEntity user, int remainingUseTicks){
         if(!(user instanceof PlayerEntity playerEntity)){
            return false;
         }
         ItemStack arrowStack = playerEntity.getProjectileType(bow);
         boolean arrowsRunic = ArcanaItemUtils.isRunicArrow(arrowStack);
         if(!arrowStack.isEmpty()){
            float pullPercent = getPullProgress(this.getMaxUseTime(bow, user) - remainingUseTicks, bow);
            if((double) pullPercent < 0.1){
               return false;
            }
            List<ItemStack> list = load(bow, arrowStack, playerEntity);
            if(world instanceof ServerWorld serverWorld && !list.isEmpty()){
               float divergence = STABILITY[Math.max(0, ArcanaAugments.getAugmentOnItem(bow,ArcanaAugments.BOW_STABILIZATION.id))];
               this.shootAll(serverWorld, playerEntity, playerEntity.getActiveHand(), bow, list, pullPercent * 3.0F, divergence, pullPercent == 1.0F, null);
            }
            
            SoundEvent sound = SoundEvents.ENTITY_ARROW_SHOOT;
            float volume = 1.0f;
            
            if(arrowsRunic){
               sound = SoundEvents.ITEM_TRIDENT_THROW.value();
               volume = 0.8f;
               
               RunicArrow runicArrow = ArcanaItemUtils.identifyRunicArrow(arrowStack);
               if(runicArrow instanceof PhotonicArrows photonArrows){
                  sound = SoundEvents.BLOCK_AMETHYST_BLOCK_HIT;
                  volume = 1.2f;
               }
               
               ArcanaNovum.data(playerEntity).addXP(ArcanaConfig.getInt(ArcanaRegistry.RUNIC_ARROW_SHOOT) * list.size());
               if(playerEntity instanceof ServerPlayerEntity player) ArcanaAchievements.progress(player,ArcanaAchievements.JUST_LIKE_ARCHER.id, list.size());
            }
            
            world.playSound(null, playerEntity.getX(), playerEntity.getY(), playerEntity.getZ(), sound, SoundCategory.PLAYERS,volume,1.0F / (world.getRandom().nextFloat() * 0.4F + 1.2F) + pullPercent * 0.5F);
            playerEntity.incrementStat(Stats.USED.getOrCreateStat(this));
            return true;
         }
         return false;
      }
      
      protected void shootAll(ServerWorld world, LivingEntity shooter, Hand hand, ItemStack bow, List<ItemStack> projectiles, float speed, float divergence, boolean critical, @Nullable LivingEntity target){
         float f = EnchantmentHelper.getProjectileSpread(world, bow, shooter, 0.0F);
         float g = projectiles.size() == 1 ? 0.0F : 2.0F * f / (float)(projectiles.size() - 1);
         float h = (float)((projectiles.size() - 1) % 2) * g / 2.0F;
         float i = 1.0F;
         
         for (int j = 0; j < projectiles.size(); j++){
            ItemStack arrowStack = (ItemStack)projectiles.get(j);
            if(!arrowStack.isEmpty()){
               float k = h + i * (float)((j + 1) / 2) * g;
               i = -i;
               ProjectileEntity projectileEntity = this.createArrowEntity(world, shooter, bow, arrowStack, critical);
               this.shoot(shooter, projectileEntity, j, speed, divergence, k, target);
               world.spawnEntity(projectileEntity);
               
               if(ArcanaItemUtils.identifyRunicArrow(arrowStack) instanceof PhotonicArrows photonArrows){
                  int alignmentLvl = Math.max(0, ArcanaAugments.getAugmentOnItem(arrowStack, ArcanaAugments.PRISMATIC_ALIGNMENT.id));
                  photonArrows.shoot(world, shooter, projectileEntity, alignmentLvl);
                  projectileEntity.kill(world);
               }
               
               bow.damage(this.getWeaponStackDamage(arrowStack), shooter, LivingEntity.getSlotForHand(hand));
               if(bow.isEmpty()){
                  break;
               }
            }
         }
      }
      
      @Override
      public void usageTick(World world, LivingEntity user, ItemStack bow, int remainingUseTicks){
         int accelLvl = ArcanaAugments.getAugmentOnItem(bow,ArcanaAugments.BOW_ACCELERATION.id);
         float prog = getPullProgress(getMaxUseTime(bow,user)-remainingUseTicks,bow);
         if(accelLvl > 0 && user instanceof ServerPlayerEntity player && prog >= 0.1){
            String t =  "▁▂▃▅▆▇۞";
            char c = t.charAt((int) (Math.max(0,prog*t.length()-1)));
            player.sendMessage(Text.literal("")
                  .append(Text.literal("\uD83C\uDFF9 (").formatted(Formatting.LIGHT_PURPLE))
                  .append(Text.literal(String.valueOf(c)).formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD))
                  .append(Text.literal(") \uD83C\uDFF9").formatted(Formatting.LIGHT_PURPLE)),true);
         }
         
         super.usageTick(world, user, bow, remainingUseTicks);
      }
      
      private float getPullProgress(int useTicks, ItemStack bow){
         float maxPullTicks = 20f;
         ArcanaItem arcanaBow = ArcanaItemUtils.identifyItem(bow);
         if(arcanaBow instanceof RunicBow){
            int accelLvl = Math.max(0, ArcanaAugments.getAugmentOnItem(bow,ArcanaAugments.BOW_ACCELERATION.id));
            final float[] accel = {20,18,17,16,15,10};
            maxPullTicks = accel[accelLvl];
         }
         
         float f = (float)useTicks / maxPullTicks;
         f = (f * f + f * 2.0F) / 3.0F;
         if(f > 1.0F){
            f = 1.0F;
         }
         
         return f;
      }
      
      @Override
      public int getMaxUseTime(ItemStack stack, LivingEntity user){
         return super.getMaxUseTime(stack,user);
      }
      
      @Override
      public ActionResult use(World world, PlayerEntity user, Hand hand){
         return super.use(world, user, hand);
      }
   }
}

