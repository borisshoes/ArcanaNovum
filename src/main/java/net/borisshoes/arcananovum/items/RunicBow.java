package net.borisshoes.arcananovum.items;

import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.core.MagicItem;
import net.borisshoes.arcananovum.core.polymer.MagicPolymerBowItem;
import net.borisshoes.arcananovum.items.arrows.PhotonicArrows;
import net.borisshoes.arcananovum.items.arrows.RunicArrow;
import net.borisshoes.arcananovum.recipes.arcana.ForgeRequirement;
import net.borisshoes.arcananovum.recipes.arcana.GenericMagicIngredient;
import net.borisshoes.arcananovum.recipes.arcana.MagicItemIngredient;
import net.borisshoes.arcananovum.recipes.arcana.MagicItemRecipe;
import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.borisshoes.arcananovum.utils.MagicRarity;
import net.borisshoes.arcananovum.utils.SoundUtils;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;

public class RunicBow extends MagicItem {
   
   public RunicBow(){
      id = "runic_bow";
      name = "Runic Bow";
      rarity = MagicRarity.LEGENDARY;
      categories = new ArcaneTome.TomeFilter[]{ArcaneTome.TomeFilter.LEGENDARY, ArcaneTome.TomeFilter.EQUIPMENT};
      vanillaItem = Items.BOW;
      item = new RunicBowItem(new FabricItemSettings().maxCount(1).fireproof().maxDamage(384));
   
      ItemStack stack = new ItemStack(item);
      NbtCompound tag = stack.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList loreList = new NbtList();
      NbtList enchants = new NbtList();
      NbtCompound power = new NbtCompound();
      power.putString("id","power");
      power.putInt("lvl",7);
      enchants.add(power);
      display.putString("Name","[{\"text\":\"Runic Bow\",\"italic\":false,\"bold\":true,\"color\":\"light_purple\"}]");
      loreList.add(NbtString.of("[{\"text\":\"The \",\"italic\":false,\"color\":\"dark_purple\"},{\"text\":\"Runic Bow\",\"color\":\"light_purple\"},{\"text\":\" makes use of the \",\"color\":\"dark_purple\"},{\"text\":\"Runic Matrix\",\"color\":\"light_purple\"},{\"text\":\" to create \",\"color\":\"dark_purple\"},{\"text\":\"unique effects\",\"color\":\"aqua\"},{\"text\":\".\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"The \",\"italic\":false,\"color\":\"dark_purple\"},{\"text\":\"Runic Bow\",\"color\":\"light_purple\"},{\"text\":\" can fire and \"},{\"text\":\"activate\",\"italic\":true,\"color\":\"dark_aqua\"},{\"text\":\" \",\"italic\":true},{\"text\":\"the effects of \"},{\"text\":\"Runic Arrows\",\"color\":\"light_purple\"},{\"text\":\".\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"The \",\"italic\":false,\"color\":\"dark_purple\"},{\"text\":\"bow\",\"color\":\"light_purple\"},{\"text\":\" also acts as a \"},{\"text\":\"normal bow\",\"color\":\"yellow\"},{\"text\":\" with \"},{\"text\":\"Power VII\",\"color\":\"aqua\"},{\"text\":\" and is \"},{\"text\":\"unbreakable\",\"color\":\"blue\"},{\"text\":\".\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Legendary \",\"italic\":false,\"color\":\"gold\",\"bold\":true},{\"text\":\"Magic Item\",\"italic\":false,\"color\":\"dark_purple\",\"bold\":false}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
      tag.put("Enchantments",enchants);
      tag.putInt("HideFlags", 255);
      tag.putInt("Unbreakable",1);
   
      setBookLore(makeLore());
      setRecipe(makeRecipe());
      prefNBT = addMagicNbt(tag);
   
      stack.setNbt(prefNBT);
      prefItem = stack;
   }
   
   @Override
   public ItemStack updateItem(ItemStack stack, MinecraftServer server){
      NbtCompound itemNbt = stack.getNbt();
      NbtList enchants = itemNbt.getList("Enchantments", NbtElement.COMPOUND_TYPE);
      NbtCompound newTag = super.updateItem(stack,server).getNbt();
      if(enchants != null) newTag.put("Enchantments", enchants);
      stack.setNbt(newTag);
      return stack;
   }
   
   @Override
   public ItemStack forgeItem(Inventory inv){
      ItemStack toolStack = inv.getStack(12); // Should be the Bow
      ItemStack newMagicItem = getNewItem();
      NbtCompound nbt = toolStack.getNbt();
      if(nbt != null && nbt.contains("Enchantments")){
         NbtList enchants = nbt.getList("Enchantments", NbtElement.COMPOUND_TYPE);
         for(int i = 0; i < enchants.size(); i++){
            if(((NbtCompound)enchants.get(i)).getString("id").equals(Registries.ENCHANTMENT.getId(Enchantments.POWER).toString())){
               NbtCompound power = new NbtCompound();
               power.putString("id","power");
               power.putShort("lvl", (short) 7);
               enchants.set(i,power);
            }
         }
         newMagicItem.getOrCreateNbt().put("Enchantments",enchants);
      }
      return newMagicItem;
   }
   
   private MagicItemRecipe makeRecipe(){
      GenericMagicIngredient m = new GenericMagicIngredient(ArcanaRegistry.RUNIC_MATRIX,1);
      MagicItemIngredient c = new MagicItemIngredient(Items.AMETHYST_SHARD,64,null);
      MagicItemIngredient n = new MagicItemIngredient(Items.NETHERITE_INGOT,4,null);
      MagicItemIngredient s = new MagicItemIngredient(Items.NETHER_STAR,4,null);
      MagicItemIngredient b = new MagicItemIngredient(Items.BOW,1,null, true);
      MagicItemIngredient e = new MagicItemIngredient(Items.ENCHANTED_BOOK,1, EnchantedBookItem.forEnchantment(new EnchantmentLevelEntry(Enchantments.POWER,5)).getNbt());
      
      MagicItemIngredient[][] ingredients = {
            {c,s,e,s,c},
            {s,n,m,n,s},
            {e,m,b,m,e},
            {s,n,m,n,s},
            {c,s,e,s,c}};
      return new MagicItemRecipe(ingredients, new ForgeRequirement().withFletchery().withEnchanter().withCore());
   }
   
   private List<String> makeLore(){
      ArrayList<String> list = new ArrayList<>();
      list.add("{\"text\":\"       Runic Bow\\n\\nRarity: Legendary\\n\\nThe Runic Bow is truely a masterpiece of adaptive Arcana. The integrated Runic Matrices reconfigure the Bow's ethereal structure based on the projectile being fired to unlock its Arcane effects. \"}");
      list.add("{\"text\":\"       Runic Bow\\n\\nThe Runic Bow is capable of utilizing Runic Arrows and activating their special abilities. The Bow also enhances normal arrows to do more damage than a traditional enchanted bow as well as being incredibly durable.\"}");
      return list;
   }
   
   public class RunicBowItem extends MagicPolymerBowItem {
      public static final Predicate<ItemStack> RUNIC_BOW_PROJECTILES = stack -> (stack.isIn(ItemTags.ARROWS) || MagicItemUtils.identifyItem(stack) instanceof RunicArrow);
      public static final float[] STABILITY = new float[]{1f, .75f, .5f, 0f};
      
      public RunicBowItem(Settings settings){
         super(getThis(),settings);
      }
      
      
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
      
      @Override
      public Predicate<ItemStack> getProjectiles() {
         return RUNIC_BOW_PROJECTILES;
      }
      
      @Override
      public void onStoppedUsing(ItemStack bow, World world, LivingEntity user, int remainingUseTicks){
         boolean dontConsumeArrow;
         float pullPercent;
         if (!(user instanceof PlayerEntity playerEntity)) {
            return;
         }
         boolean hasInfiniteArrows = playerEntity.getAbilities().creativeMode || EnchantmentHelper.getLevel(Enchantments.INFINITY, bow) > 0;
         ItemStack arrowStack = playerEntity.getProjectileType(bow);
         if (arrowStack.isEmpty() && !hasInfiniteArrows) {
            return;
         }
         if (arrowStack.isEmpty()) {
            arrowStack = new ItemStack(Items.ARROW);
         }
         if ((double)(pullPercent = getPullProgress(this.getMaxUseTime(bow) - remainingUseTicks, bow)) < 0.1) {
            return;
         }
         boolean arrowsRunic = MagicItemUtils.isRunicArrow(arrowStack);
         boolean hasEnhancedInfinity = Math.max(0, ArcanaAugments.getAugmentOnItem(bow,ArcanaAugments.ENHANCED_INFINITY.id)) >= 1;
         dontConsumeArrow = hasInfiniteArrows && (arrowStack.isOf(Items.ARROW) || (hasEnhancedInfinity && !arrowsRunic && (arrowStack.isOf(Items.SPECTRAL_ARROW) || arrowStack.isOf(Items.TIPPED_ARROW))));
         
         SoundEvent sound = SoundEvents.ENTITY_ARROW_SHOOT;
         float volume = 1.0f;
         if (!world.isClient) {
            int punchLvl;
            int powerLvl;
            float divergence = STABILITY[Math.max(0, ArcanaAugments.getAugmentOnItem(bow,ArcanaAugments.BOW_STABILIZATION.id))];
            
            ArrowItem arrowItem = (ArrowItem)(arrowStack.getItem() instanceof ArrowItem ? arrowStack.getItem() : Items.ARROW);
            PersistentProjectileEntity persistentProjectileEntity = arrowItem.createArrow(world, arrowStack, playerEntity);
            persistentProjectileEntity.setVelocity(playerEntity, playerEntity.getPitch(), playerEntity.getYaw(), 0.0f, pullPercent * 3.0f, divergence);
            if (pullPercent == 1.0f) {
               persistentProjectileEntity.setCritical(true);
            }
            if ((powerLvl = EnchantmentHelper.getLevel(Enchantments.POWER, bow)) > 0) {
               persistentProjectileEntity.setDamage(persistentProjectileEntity.getDamage() + (double)powerLvl * 0.5 + 0.5);
            }
            if ((punchLvl = EnchantmentHelper.getLevel(Enchantments.PUNCH, bow)) > 0) {
               persistentProjectileEntity.setPunch(punchLvl);
            }
            if (EnchantmentHelper.getLevel(Enchantments.FLAME, bow) > 0) {
               persistentProjectileEntity.setOnFireFor(100);
            }
            bow.damage(1, playerEntity, p -> p.sendToolBreakStatus(playerEntity.getActiveHand()));
            if (dontConsumeArrow || playerEntity.getAbilities().creativeMode && (arrowStack.isOf(Items.SPECTRAL_ARROW) || arrowStack.isOf(Items.TIPPED_ARROW))) {
               persistentProjectileEntity.pickupType = PersistentProjectileEntity.PickupPermission.CREATIVE_ONLY;
            }
            world.spawnEntity(persistentProjectileEntity);
            
            if(arrowsRunic){
               RunicArrow runicArrow = MagicItemUtils.identifyRunicArrow(arrowStack);
               NbtCompound magicTag = arrowStack.getNbt().getCompound("arcananovum");
               sound = SoundEvents.ITEM_TRIDENT_THROW;
               volume = 0.8f;
               
               if(playerEntity instanceof ServerPlayerEntity player) ArcanaAchievements.progress(player,ArcanaAchievements.JUST_LIKE_ARCHER.id, 1);
               if(runicArrow instanceof PhotonicArrows photonArrows){
                  int alignmentLvl = Math.max(0, ArcanaAugments.getAugmentFromCompound(magicTag, ArcanaAugments.PRISMATIC_ALIGNMENT.id));
                  photonArrows.shoot(world,user,persistentProjectileEntity,alignmentLvl);
                  persistentProjectileEntity.kill();
                  sound = SoundEvents.BLOCK_AMETHYST_BLOCK_HIT;
                  volume = 1.2f;
               }
               PLAYER_DATA.get(playerEntity).addXP(50);
            }
         }
         
         SoundUtils.playSound(world,playerEntity.getBlockPos(), sound, SoundCategory.PLAYERS, volume, 1.0f / (world.getRandom().nextFloat() * 0.4f + 1.2f) + pullPercent * 0.5f);
         
         if (!dontConsumeArrow && !playerEntity.getAbilities().creativeMode) {
            QuiverItem.decreaseQuiver(bow,arrowStack,playerEntity);
            arrowStack.decrement(1);
            if (arrowStack.isEmpty()) {
               playerEntity.getInventory().removeOne(arrowStack);
            }
         }
         playerEntity.incrementStat(Stats.USED.getOrCreateStat(this));
      }
      
      @Override
      public void usageTick(World world, LivingEntity user, ItemStack bow, int remainingUseTicks){
         int accelLvl = ArcanaAugments.getAugmentOnItem(bow,ArcanaAugments.BOW_ACCELERATION.id);
         float prog = getPullProgress(getMaxUseTime(bow)-remainingUseTicks,bow);
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
         MagicItem magicBow = MagicItemUtils.identifyItem(bow);
         if(magicBow instanceof RunicBow){
            int accelLvl = Math.max(0, ArcanaAugments.getAugmentOnItem(bow,ArcanaAugments.BOW_ACCELERATION.id));
            final float[] accel = {20,18,17,16,15,10};
            maxPullTicks = accel[accelLvl];
         }
         
         float f = (float)useTicks / maxPullTicks;
         f = (f * f + f * 2.0F) / 3.0F;
         if (f > 1.0F) {
            f = 1.0F;
         }
         
         return f;
      }
      
      @Override
      public int getMaxUseTime(ItemStack stack){
         return super.getMaxUseTime(stack);
      }
      
      @Override
      public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand){
         return super.use(world, user, hand);
      }
   }
}
