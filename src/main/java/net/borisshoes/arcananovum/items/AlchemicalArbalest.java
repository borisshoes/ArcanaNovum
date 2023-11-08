package net.borisshoes.arcananovum.items;

import com.google.common.collect.Lists;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.MagicItem;
import net.borisshoes.arcananovum.core.polymer.MagicPolymerCrossbowItem;
import net.borisshoes.arcananovum.entities.ArbalestArrowEntity;
import net.borisshoes.arcananovum.recipes.arcana.ForgeRequirement;
import net.borisshoes.arcananovum.recipes.arcana.MagicItemIngredient;
import net.borisshoes.arcananovum.recipes.arcana.MagicItemRecipe;
import net.borisshoes.arcananovum.utils.MagicRarity;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.CrossbowUser;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AlchemicalArbalest extends MagicItem {
   
   public AlchemicalArbalest(){
      id = "alchemical_arbalest";
      name = "Alchemical Arbalest";
      rarity = MagicRarity.LEGENDARY;
      categories = new ArcaneTome.TomeFilter[]{ArcaneTome.TomeFilter.LEGENDARY, ArcaneTome.TomeFilter.EQUIPMENT};
      itemVersion = 0;
      vanillaItem = Items.CROSSBOW;
      item = new AlchemicalArbalestItem(new FabricItemSettings().maxCount(1).fireproof().maxDamage(465));
      
      ItemStack stack = new ItemStack(item);
      NbtCompound tag = stack.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList enchants = new NbtList();
      NbtCompound power = new NbtCompound();
      power.putString("id","multishot");
      power.putInt("lvl",1);
      enchants.add(power);
      display.putString("Name","[{\"text\":\"Alchemical Arbalest\",\"italic\":false,\"color\":\"dark_aqua\",\"bold\":true}]");
      tag.put("display",display);
      tag.put("Enchantments",enchants);
      tag.putInt("Unbreakable",1);
      tag.putInt("HideFlags", 255);
      buildItemLore(stack, ArcanaNovum.SERVER);
      
      setBookLore(makeLore());
      setRecipe(makeRecipe());
      prefNBT = addMagicNbt(tag);
      
      stack.setNbt(prefNBT);
      prefItem = stack;
   }
   
   @Override
   public NbtList getItemLore(@Nullable ItemStack itemStack){
      NbtList loreList = new NbtList();
      loreList.add(NbtString.of("[{\"text\":\"This \",\"italic\":false,\"color\":\"yellow\"},{\"text\":\"Crossbow \",\"color\":\"dark_aqua\"},{\"text\":\"is outfitted with \"},{\"text\":\"enchanted \",\"color\":\"light_purple\"},{\"text\":\"clockwork mechanisms\",\"color\":\"blue\"},{\"text\":\".\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Tipped Arrows\",\"italic\":false,\"color\":\"blue\"},{\"text\":\" fired from the \",\"color\":\"yellow\"},{\"text\":\"bow \",\"color\":\"dark_aqua\"},{\"text\":\"create a \",\"color\":\"yellow\"},{\"text\":\"lingering \",\"color\":\"dark_aqua\"},{\"text\":\"field\",\"color\":\"light_purple\"},{\"text\":\".\",\"color\":\"yellow\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Spectral Arrows\",\"italic\":false,\"color\":\"blue\"},{\"text\":\" create a zone of \",\"color\":\"yellow\"},{\"text\":\"damage \",\"color\":\"dark_aqua\"},{\"text\":\"amplification\",\"color\":\"light_purple\"},{\"text\":\".\",\"color\":\"yellow\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"The \",\"italic\":false,\"color\":\"yellow\"},{\"text\":\"Crossbow \",\"color\":\"dark_aqua\"},{\"text\":\"is \"},{\"text\":\"Unbreakable \",\"color\":\"blue\"},{\"text\":\"and comes with \"},{\"text\":\"Multishot\",\"color\":\"light_purple\"},{\"text\":\".\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      return loreList;
   }
   
   @Override
   public ItemStack updateItem(ItemStack stack, MinecraftServer server){
      NbtCompound itemNbt = stack.getNbt();
      NbtList enchants = itemNbt.getList("Enchantments", NbtElement.COMPOUND_TYPE);
      NbtCompound newTag = super.updateItem(stack,server).getNbt();
      if(enchants != null) newTag.put("Enchantments", enchants);
      stack.setNbt(newTag);
      return buildItemLore(stack,server);
   }
   
   @Override
   public ItemStack forgeItem(Inventory inv){
      ItemStack toolStack = inv.getStack(12); // Should be the Crossbow
      ItemStack newMagicItem = getNewItem();
      NbtCompound nbt = toolStack.getNbt();
      if(nbt == null) return newMagicItem;
      NbtCompound newNbt = newMagicItem.getOrCreateNbt();
      if(nbt.contains("Enchantments")){
         NbtList enchants = nbt.getList("Enchantments", NbtElement.COMPOUND_TYPE);
         boolean hasMulti = false;
         int pierceInd = -1;
         for(int i = 0; i < enchants.size(); i++){
            String enchantId = ((NbtCompound)enchants.get(i)).getString("id");
            if(enchantId.equals(Registries.ENCHANTMENT.getId(Enchantments.PIERCING).toString())){
               pierceInd = i;
            }else if(enchantId.equals(Registries.ENCHANTMENT.getId(Enchantments.MULTISHOT).toString())){
               hasMulti = true;
            }
         }
         if(!hasMulti){
            NbtCompound multi = new NbtCompound();
            multi.putString("id","multishot");
            multi.putShort("lvl", (short) 1);
            enchants.add(multi);
         }
         if(pierceInd >= 0){
            enchants.remove(pierceInd);
         }
         newNbt.put("Enchantments",enchants);
      }
      return newMagicItem;
   }
   
   private MagicItemRecipe makeRecipe(){
      MagicItemIngredient a = new MagicItemIngredient(Items.SPECTRAL_ARROW,64,null);
      MagicItemIngredient b = new MagicItemIngredient(Items.GLOWSTONE_DUST,64,null);
      MagicItemIngredient c = new MagicItemIngredient(Items.DRAGON_BREATH,64,null);
      MagicItemIngredient f = new MagicItemIngredient(Items.NETHER_WART,64,null);
      ItemStack enchantedBook6 = new ItemStack(Items.ENCHANTED_BOOK);
      EnchantedBookItem.addEnchantment(enchantedBook6,new EnchantmentLevelEntry(Enchantments.MULTISHOT,1));
      MagicItemIngredient g = new MagicItemIngredient(Items.ENCHANTED_BOOK,1,enchantedBook6.getNbt());
      MagicItemIngredient h = new MagicItemIngredient(Items.NETHERITE_INGOT,8,null);
      MagicItemIngredient l = new MagicItemIngredient(Items.NETHER_STAR,8,null);
      MagicItemIngredient m = new MagicItemIngredient(Items.CROSSBOW,1,null);
      
      MagicItemIngredient[][] ingredients = {
            {a,b,c,b,a},
            {f,g,h,g,f},
            {c,l,m,l,c},
            {f,g,h,g,f},
            {a,b,c,b,a}};
      return new MagicItemRecipe(ingredients, new ForgeRequirement().withFletchery().withEnchanter().withCore());
   }
   
   private List<String> makeLore(){
      ArrayList<String> list = new ArrayList<>();
      list.add("{\"text\":\"  Alchemical Arbalest\\n\\nRarity: Legendary\\n\\nWhile bows are excellent for sustained damage, crossbows have always been good at bursts of damage, and area suppression. \\nI believe I can enhance this niche further...\\n\"}");
      list.add("{\"text\":\"  Alchemical Arbalest\\n\\nThe Arbalest overcharges Tipped Arrows so that their effects cover a wide space.\\nIt also comes with multishot pre-installed\\nSpectral Arrows are where things get interesting. They carry no discrete effect but cause \"}");
      list.add("{\"text\":\"  Alchemical Arbalest\\n\\ncreatures to glow.\\nTweaking this ability a bit when used in the Arbalest, Spectral Arrows now create a zone that makes weakpoints on enemies easier to see, causing them to take increased damage from all sources.\"}");
      return list;
   }
   
   public class AlchemicalArbalestItem extends MagicPolymerCrossbowItem {
      private static final String CHARGED_KEY = "Charged";
      private static final String CHARGED_PROJECTILES_KEY = "ChargedProjectiles";
      private boolean charged = false;
      private boolean loaded = false;
      
      public AlchemicalArbalestItem(Settings settings){
         super(getThis(),settings);
      }
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
      
      public void verifyEnchantments(ItemStack stack){
         boolean hasMulti = EnchantmentHelper.getLevel(Enchantments.MULTISHOT, stack) > 0;
         boolean hasPierce = EnchantmentHelper.getLevel(Enchantments.PIERCING, stack) > 0;
         boolean hasScatter = ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.SCATTERSHOT.id) > 0;
         boolean hasRunic = ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.RUNIC_ARBALEST.id) > 0;
         
         Map<Enchantment,Integer> enchants = EnchantmentHelper.get(stack);
         if(hasRunic && hasMulti){ // Remove multishot
            enchants.entrySet().removeIf(entry -> entry.getKey() == Enchantments.MULTISHOT);
            EnchantmentHelper.set(enchants,stack);
            verifyEnchantments(stack);
         }
         if(hasScatter && hasPierce){ // Remove pierce
            enchants.entrySet().removeIf(entry -> entry.getKey() == Enchantments.PIERCING);
            EnchantmentHelper.set(enchants,stack);
            verifyEnchantments(stack);
         }
         if(hasScatter && !hasMulti){ // Re-add multishot
            enchants.put(Enchantments.MULTISHOT,1);
            EnchantmentHelper.set(enchants,stack);
            verifyEnchantments(stack);
         }
      }
      
      @Override
      public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
         if (!world.isClient) {
            int i = EnchantmentHelper.getLevel(Enchantments.QUICK_CHARGE, stack);
            SoundEvent soundEvent = this.getQuickChargeSound(i);
            SoundEvent soundEvent2 = i == 0 ? SoundEvents.ITEM_CROSSBOW_LOADING_MIDDLE : null;
            float f = (float)(stack.getMaxUseTime() - remainingUseTicks) / (float)CrossbowItem.getPullTime(stack);
            if (f < 0.2f) {
               this.charged = false;
               this.loaded = false;
            }
            if (f >= 0.2f && !this.charged) {
               this.charged = true;
               world.playSound(null, user.getX(), user.getY(), user.getZ(), soundEvent, SoundCategory.PLAYERS, 0.5f, 1.0f);
            }
            if (f >= 0.5f && soundEvent2 != null && !this.loaded) {
               this.loaded = true;
               world.playSound(null, user.getX(), user.getY(), user.getZ(), soundEvent2, SoundCategory.PLAYERS, 0.5f, 1.0f);
            }
         }
      }
      
      @Override
      public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
         ItemStack itemStack = user.getStackInHand(hand);
         verifyEnchantments(itemStack);
         
         if (AlchemicalArbalestItem.isCharged(itemStack)) {
            AlchemicalArbalestItem.shootAll(world, user, hand, itemStack, AlchemicalArbalestItem.getSpeed(itemStack), 1.0f);
            AlchemicalArbalestItem.setCharged(itemStack, false);
            return TypedActionResult.consume(itemStack);
         }
         if (!user.getProjectileType(itemStack).isEmpty()) {
            if (!AlchemicalArbalestItem.isCharged(itemStack)) {
               this.charged = false;
               this.loaded = false;
               user.setCurrentHand(hand);
            }
            return TypedActionResult.consume(itemStack);
         }
         return TypedActionResult.fail(itemStack);
      }
      
      private static float getSpeed(ItemStack stack) {
         if (AlchemicalArbalestItem.hasProjectile(stack, Items.FIREWORK_ROCKET)) {
            return 1.6f;
         }
         return 3.15f;
      }
      
      @Override
      public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
         int i = this.getMaxUseTime(stack) - remainingUseTicks;
         float f = AlchemicalArbalestItem.getPullProgress(i, stack);
         if (f >= 1.0f && !AlchemicalArbalestItem.isCharged(stack) && AlchemicalArbalestItem.loadProjectiles(user, stack)) {
            AlchemicalArbalestItem.setCharged(stack, true);
            SoundCategory soundCategory = user instanceof PlayerEntity ? SoundCategory.PLAYERS : SoundCategory.HOSTILE;
            world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ITEM_CROSSBOW_LOADING_END, soundCategory, 1.0f, 1.0f / (world.getRandom().nextFloat() * 0.5f + 1.0f) + 0.2f);
         }
      }
      
      private static boolean loadProjectiles(LivingEntity shooter, ItemStack crossbow) {
         boolean multishot1 = EnchantmentHelper.getLevel(Enchantments.MULTISHOT, crossbow) > 0;
         boolean multishot2 = ArcanaAugments.getAugmentOnItem(crossbow,ArcanaAugments.SCATTERSHOT.id) > 0;
         int shots = multishot2 ? 5 : (multishot1 ? 3 : 1);
         boolean bl = shooter instanceof PlayerEntity && ((PlayerEntity)shooter).getAbilities().creativeMode;
         ItemStack itemStack = shooter.getProjectileType(crossbow);
         ItemStack itemStack2 = itemStack.copy();
         for (int k = 0; k < shots; ++k) {
            if (k > 0) {
               itemStack = itemStack2.copy();
            }
            if (itemStack.isEmpty() && bl) {
               itemStack = new ItemStack(Items.ARROW);
               itemStack2 = itemStack.copy();
            }
            if (AlchemicalArbalestItem.loadProjectile(shooter, crossbow, itemStack, k > 0, bl)) continue;
            return false;
         }
         return true;
      }
      
      private static boolean loadProjectile(LivingEntity shooter, ItemStack crossbow, ItemStack projectile, boolean simulated, boolean creative) {
         ItemStack itemStack;
         boolean bl;
         if (projectile.isEmpty()) {
            return false;
         }
         bl = creative && projectile.getItem() instanceof ArrowItem;
         if (!(bl || creative || simulated)) {
            if(shooter instanceof ServerPlayerEntity player){
               QuiverItem.decreaseQuiver(crossbow,projectile,player);
               EverlastingRocket.decreaseRocket(projectile,player);
            }
            itemStack = projectile.split(1);
            if (projectile.isEmpty() && shooter instanceof PlayerEntity) {
               ((PlayerEntity)shooter).getInventory().removeOne(projectile);
            }
         } else {
            itemStack = projectile.copy();
         }
         AlchemicalArbalestItem.putProjectile(crossbow, itemStack);
         return true;
      }
      
      public static boolean isCharged(ItemStack stack) {
         NbtCompound nbtCompound = stack.getNbt();
         return nbtCompound != null && nbtCompound.getBoolean(CHARGED_KEY);
      }
      
      public static void setCharged(ItemStack stack, boolean charged) {
         NbtCompound nbtCompound = stack.getOrCreateNbt();
         nbtCompound.putBoolean(CHARGED_KEY, charged);
      }
      
      private static void putProjectile(ItemStack crossbow, ItemStack projectile) {
         NbtCompound nbtCompound = crossbow.getOrCreateNbt();
         NbtList nbtList = nbtCompound.contains(CHARGED_PROJECTILES_KEY, NbtElement.LIST_TYPE) ? nbtCompound.getList(CHARGED_PROJECTILES_KEY, NbtElement.COMPOUND_TYPE) : new NbtList();
         NbtCompound nbtCompound2 = new NbtCompound();
         projectile.writeNbt(nbtCompound2);
         nbtList.add(nbtCompound2);
         nbtCompound.put(CHARGED_PROJECTILES_KEY, nbtList);
      }
      
      private static List<ItemStack> getProjectiles(ItemStack crossbow) {
         NbtList nbtList;
         ArrayList<ItemStack> list = Lists.newArrayList();
         NbtCompound nbtCompound = crossbow.getNbt();
         if (nbtCompound != null && nbtCompound.contains(CHARGED_PROJECTILES_KEY, NbtElement.LIST_TYPE) && (nbtList = nbtCompound.getList(CHARGED_PROJECTILES_KEY, NbtElement.COMPOUND_TYPE)) != null) {
            for (int i = 0; i < nbtList.size(); ++i) {
               NbtCompound nbtCompound2 = nbtList.getCompound(i);
               list.add(ItemStack.fromNbt(nbtCompound2));
            }
         }
         return list;
      }
      
      private static void clearProjectiles(ItemStack crossbow) {
         NbtCompound nbtCompound = crossbow.getNbt();
         if (nbtCompound != null) {
            NbtList nbtList = nbtCompound.getList(CHARGED_PROJECTILES_KEY, NbtElement.LIST_TYPE);
            nbtList.clear();
            nbtCompound.put(CHARGED_PROJECTILES_KEY, nbtList);
         }
      }
      
      public static boolean hasProjectile(ItemStack crossbow, Item projectile) {
         return AlchemicalArbalestItem.getProjectiles(crossbow).stream().anyMatch(s -> s.isOf(projectile));
      }
      
      private static void shoot(World world, LivingEntity shooter, Hand hand, ItemStack crossbow, ItemStack projectile, float soundPitch, boolean creative, float speed, float divergence, float simulated) {
         ProjectileEntity projectileEntity;
         if (world.isClient) {
            return;
         }
         boolean bl = projectile.isOf(Items.FIREWORK_ROCKET);
         if (bl) {
            projectileEntity = new FireworkRocketEntity(world, projectile, shooter, shooter.getX(), shooter.getEyeY() - (double)0.15f, shooter.getZ(), true);
         } else {
            projectileEntity = AlchemicalArbalestItem.createArrow(world, shooter, crossbow, projectile);
            if (creative || simulated != 0.0f) {
               ((PersistentProjectileEntity)projectileEntity).pickupType = PersistentProjectileEntity.PickupPermission.CREATIVE_ONLY;
            }
         }
         if (shooter instanceof CrossbowUser) {
            CrossbowUser crossbowUser = (CrossbowUser)((Object)shooter);
            crossbowUser.shoot(crossbowUser.getTarget(), crossbow, projectileEntity, simulated);
         } else {
            Vec3d vec3d = shooter.getOppositeRotationVector(1.0f);
            Quaternionf quaternionf = new Quaternionf().setAngleAxis((double)(simulated * ((float)Math.PI / 180)), vec3d.x, vec3d.y, vec3d.z);
            Vec3d vec3d2 = shooter.getRotationVec(1.0f);
            Vector3f vector3f = vec3d2.toVector3f().rotate(quaternionf);
            projectileEntity.setVelocity(vector3f.x(), vector3f.y(), vector3f.z(), speed, divergence);
         }
         crossbow.damage(bl ? 3 : 1, shooter, e -> e.sendToolBreakStatus(hand));
         world.spawnEntity(projectileEntity);
         world.playSound(null, shooter.getX(), shooter.getY(), shooter.getZ(), SoundEvents.ITEM_CROSSBOW_SHOOT, SoundCategory.PLAYERS, 1.0f, soundPitch);
      }
      
      private static PersistentProjectileEntity createArrow(World world, LivingEntity entity, ItemStack crossbow, ItemStack arrow) {
         ArrowItem arrowItem = (ArrowItem)(arrow.getItem() instanceof ArrowItem ? arrow.getItem() : Items.ARROW);
         PersistentProjectileEntity persistentProjectileEntity;
         if(arrow.isOf(Items.TIPPED_ARROW) || arrow.isOf(Items.SPECTRAL_ARROW)){
            int spectralLvl = Math.max(0, ArcanaAugments.getAugmentOnItem(crossbow,ArcanaAugments.SPECTRAL_AMPLIFICATION.id));
            int prolificLvl = Math.max(0, ArcanaAugments.getAugmentOnItem(crossbow,ArcanaAugments.PROLIFIC_POTIONS.id));
            ArbalestArrowEntity arrowEntity = new ArbalestArrowEntity(world, entity, spectralLvl,prolificLvl);
            arrowEntity.initFromStack(arrow);
            persistentProjectileEntity = arrowEntity;
         }else{
            persistentProjectileEntity = arrowItem.createArrow(world, arrow, entity);
         }
         
         if (entity instanceof PlayerEntity) {
            persistentProjectileEntity.setCritical(true);
         }
         persistentProjectileEntity.setSound(SoundEvents.ITEM_CROSSBOW_HIT);
         persistentProjectileEntity.setShotFromCrossbow(true);
         int i = EnchantmentHelper.getLevel(Enchantments.PIERCING, crossbow);
         if (i > 0) {
            persistentProjectileEntity.setPierceLevel((byte)i);
         }
         return persistentProjectileEntity;
      }
      
      public static void shootAll(World world, LivingEntity entity, Hand hand, ItemStack stack, float speed, float divergence) {
         List<ItemStack> list = AlchemicalArbalestItem.getProjectiles(stack);
         for (int i = 0; i < list.size(); ++i) {
            boolean bl;
            ItemStack itemStack = list.get(i);
            bl = entity instanceof PlayerEntity && ((PlayerEntity)entity).getAbilities().creativeMode;
            if (itemStack.isEmpty()) continue;
            if (i == 0) {
               AlchemicalArbalestItem.shoot(world, entity, hand, stack, itemStack, getSoundPitch(entity.getRandom()), bl, speed, divergence, 0.0f);
               continue;
            }
            float spread = 10*((i+1)/2) * (i%2==0 ? -1 : 1);
            AlchemicalArbalestItem.shoot(world, entity, hand, stack, itemStack, getSoundPitch(entity.getRandom()), bl, speed, divergence, spread);
         }
         AlchemicalArbalestItem.postShoot(world, entity, stack);
      }
      
      private static float getSoundPitch(Random random) {
         return 1.0f / (random.nextFloat() * 0.5f + 1.8f) + 0.5f;
      }
      
      private static void postShoot(World world, LivingEntity entity, ItemStack stack) {
         if (entity instanceof ServerPlayerEntity) {
            ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)entity;
            if (!world.isClient) {
               Criteria.SHOT_CROSSBOW.trigger(serverPlayerEntity, stack);
            }
            serverPlayerEntity.incrementStat(Stats.USED.getOrCreateStat(stack.getItem()));
         }
         AlchemicalArbalestItem.clearProjectiles(stack);
      }
      
      private static float getPullProgress(int useTicks, ItemStack stack) {
         float f = (float)useTicks / (float)CrossbowItem.getPullTime(stack);
         if (f > 1.0f) {
            f = 1.0f;
         }
         return f;
      }
      
      private SoundEvent getQuickChargeSound(int stage) {
         switch(stage){
            case 1 -> {
               return SoundEvents.ITEM_CROSSBOW_QUICK_CHARGE_1;
            }
            case 2 -> {
               return SoundEvents.ITEM_CROSSBOW_QUICK_CHARGE_2;
            }
            case 3 -> {
               return SoundEvents.ITEM_CROSSBOW_QUICK_CHARGE_3;
            }
         }
         return SoundEvents.ITEM_CROSSBOW_LOADING_START;
      }
   }
}
