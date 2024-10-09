package net.borisshoes.arcananovum.items;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.EnergyItem;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerSwordItem;
import net.borisshoes.arcananovum.gui.arcanetome.TomeGui;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaIngredient;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaRecipe;
import net.borisshoes.arcananovum.recipes.arcana.ForgeRequirement;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.*;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.UnbreakableComponent;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.WardenEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.*;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Pair;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;
import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;

public class ShadowStalkersGlaive extends EnergyItem {
	public static final String ID = "shadow_stalkers_glaive";
   
   public static final String TETHER_TARGET_TAG = "tetherTarget";
   public static final String TETHER_TIME_TAG = "tetherTime";
   
   private final int teleportLength = 10;
   private static final String TXT = "item/shadow_stalkers_glaive";
   
   public ShadowStalkersGlaive(){
      id = ID;
      name = "Shadow Stalkers Glaive";
      rarity = ArcanaRarity.SOVEREIGN;
      categories = new TomeGui.TomeFilter[]{TomeGui.TomeFilter.SOVEREIGN, TomeGui.TomeFilter.EQUIPMENT};
      vanillaItem = Items.NETHERITE_SWORD;
      item = new ShadowStalkersGlaiveItem(5, -2.0f, new Item.Settings().maxCount(1).fireproof().maxDamage(1024)
            .component(DataComponentTypes.ITEM_NAME, TextUtils.withColor(Text.translatable("item."+MOD_ID+"."+ID).formatted(Formatting.BOLD),ArcanaColors.NUL_COLOR))
            .component(DataComponentTypes.LORE, new LoreComponent(getItemLore(null)))
            .component(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true)
            .component(DataComponentTypes.UNBREAKABLE,new UnbreakableComponent(false))
      );
      models = new ArrayList<>();
      models.add(new Pair<>(vanillaItem,TXT));
      researchTasks = new RegistryKey[]{ResearchTasks.OBTAIN_NETHERITE_SWORD,ResearchTasks.OBTAIN_NETHER_STAR,ResearchTasks.USE_ENDER_PEARL,ResearchTasks.ADVANCEMENT_KILL_A_MOB,ResearchTasks.UNLOCK_STELLAR_CORE};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getMaxCount());
      putProperty(stack,TETHER_TIME_TAG,-1);
      putProperty(stack,TETHER_TARGET_TAG,"");
      setPrefStack(stack);
   }
   
   @Override
   public List<Text> getItemLore(@Nullable ItemStack itemStack){
      List<MutableText> lore = new ArrayList<>();
      lore.add(Text.literal("")
            .append(Text.literal("This ").formatted(Formatting.DARK_GRAY))
            .append(Text.literal("blade ").formatted(Formatting.GRAY))
            .append(Text.literal("lets you move through your opponents ").formatted(Formatting.DARK_GRAY))
            .append(Text.literal("shadow").formatted(Formatting.BLUE))
            .append(Text.literal(".").formatted(Formatting.DARK_GRAY)));
      lore.add(Text.literal("")
            .append(Text.literal("The ").formatted(Formatting.DARK_GRAY))
            .append(Text.literal("blade ").formatted(Formatting.GRAY))
            .append(Text.literal("stores the ").formatted(Formatting.DARK_GRAY))
            .append(Text.literal("blood ").formatted(Formatting.DARK_RED))
            .append(Text.literal("from every strike and uses it as ").formatted(Formatting.DARK_GRAY))
            .append(Text.literal("energy").formatted(Formatting.AQUA))
            .append(Text.literal(".").formatted(Formatting.DARK_GRAY)));
      lore.add(Text.literal("")
            .append(Text.literal("Stride ").formatted(Formatting.AQUA))
            .append(Text.literal("through the ").formatted(Formatting.DARK_GRAY))
            .append(Text.literal("darkness ").formatted(Formatting.BLUE))
            .append(Text.literal("behind your opponent or ").formatted(Formatting.DARK_GRAY))
            .append(Text.literal("blink forward").formatted(Formatting.AQUA))
            .append(Text.literal(".").formatted(Formatting.DARK_GRAY)));
      lore.add(Text.literal("")
            .append(Text.literal("Right Click").formatted(Formatting.GRAY))
            .append(Text.literal(" to ").formatted(Formatting.DARK_GRAY))
            .append(Text.literal("teleport ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("behind ").formatted(Formatting.BLUE))
            .append(Text.literal("your most recently attacked foe.").formatted(Formatting.DARK_GRAY)));
      lore.add(Text.literal("")
            .append(Text.literal("Sneak Right Click").formatted(Formatting.GRAY))
            .append(Text.literal(" to ").formatted(Formatting.DARK_GRAY))
            .append(Text.literal("teleport ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("a ").formatted(Formatting.DARK_GRAY))
            .append(Text.literal("short distance").formatted(Formatting.BLUE))
            .append(Text.literal(".").formatted(Formatting.DARK_GRAY)));
     return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   public int getMaxEnergy(ItemStack item){
      return 100; // 100 damage stored
   }
   
   @Override
   public ItemStack updateItem(ItemStack stack, MinecraftServer server){
      String target = getStringProperty(stack,TETHER_TARGET_TAG);
      int time = getIntProperty(stack,TETHER_TIME_TAG);
      ItemStack newStack = super.updateItem(stack,server);
      putProperty(newStack,TETHER_TARGET_TAG,target);
      putProperty(newStack,TETHER_TIME_TAG,time);
      return buildItemLore(newStack,server);
   }
   
   public void entityAttacked(PlayerEntity player, ItemStack stack, Entity entity){
      if(entity instanceof MobEntity || entity instanceof PlayerEntity){
         putProperty(stack,TETHER_TARGET_TAG,entity.getUuidAsString());
         putProperty(stack,TETHER_TIME_TAG,60);
      }
   }
   
   @Override
   public ItemStack forgeItem(Inventory inv){
      ItemStack toolStack = inv.getStack(12); // Should be the Sword
      ItemStack newArcanaItem = getNewItem();
      
      if(toolStack.hasEnchantments()){
         EnchantmentHelper.set(newArcanaItem,toolStack.getEnchantments());
      }
      
      if(hasProperty(toolStack,EnhancedStatUtils.ENHANCED_STAT_TAG)){
         EnhancedStatUtils.enhanceItem(newArcanaItem,getDoubleProperty(toolStack,EnhancedStatUtils.ENHANCED_STAT_TAG));
      }
      return newArcanaItem;
   }
   
   @Override
	protected ArcanaRecipe makeRecipe(){
      ArcanaIngredient a = new ArcanaIngredient(Items.ENDER_EYE,12);
      ArcanaIngredient b = new ArcanaIngredient(Items.CRYING_OBSIDIAN,16);
      ArcanaIngredient c = new ArcanaIngredient(Items.RED_NETHER_BRICKS,32);
      ArcanaIngredient g = new ArcanaIngredient(Items.OBSIDIAN,24);
      ArcanaIngredient h = new ArcanaIngredient(Items.NETHERITE_INGOT,3);
      ArcanaIngredient l = new ArcanaIngredient(Items.NETHER_STAR,3);
      ArcanaIngredient m = new ArcanaIngredient(Items.NETHERITE_SWORD,1, true);
      
      ArcanaIngredient[][] ingredients = {
            {a,b,c,b,a},
            {b,g,h,g,b},
            {c,l,m,l,c},
            {b,g,h,g,b},
            {a,b,c,b,a}};
      return new ArcanaRecipe(ingredients,new ForgeRequirement().withAnvil().withCore());
   }
   
   @Override
   public List<List<Text>> getBookLore(){
      List<List<Text>> list = new ArrayList<>();
      list.add(List.of(Text.literal("   Shadow Stalker's\n          Glaive\nRarity: Sovereign\n\nThis Blade was forged to mimic the power of Endermen to stride through the shadows and relentlessly follow foes. However, instead of using ender particles to warp through dimensions, this sword").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal("   Shadow Stalker's\n          Glaive\nuses a force of which we know very little. To fall through the shadows and emerge elsewhere is far different than what it feels like to channel Ender Energy. Blood that is spilled on the sword gets soaked up by shadowy tendrils that eminate from it.").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal("   Shadow Stalker's\n          Glaive\nStriking foes grants charges.\nSneak Right Click consumes one charge to blink forward.\nThe Glaive remembers the last target it struck and Right Clicking consumes four charges to teleport behind that target. ").formatted(Formatting.BLACK)));
      return list;
   }
   
   public class ShadowStalkersGlaiveItem extends ArcanaPolymerSwordItem {
      public ShadowStalkersGlaiveItem(float attackDamage, float attackSpeed, Item.Settings settings){
         super(getThis(),ToolMaterial.NETHERITE,attackDamage,attackSpeed,settings);
      }
      
      @Override
      public int getPolymerCustomModelData(ItemStack itemStack, @Nullable ServerPlayerEntity player){
         return ArcanaRegistry.getModelData(TXT).value();
      }
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
      
      @Override
      public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected){
         if(!ArcanaItemUtils.isArcane(stack)) return;
         if(!(world instanceof ServerWorld && entity instanceof ServerPlayerEntity player)) return;
         if(world.getServer().getTicks() % (20) == 0){
            
            String targetID = getStringProperty(stack,TETHER_TARGET_TAG);
            if(targetID != null && !targetID.isEmpty()){
               Entity target = player.getServerWorld().getEntity(MiscUtils.getUUID(targetID));
               if(target == null || !target.isAlive() || player.getServerWorld().getRegistryKey() != target.getEntityWorld().getRegistryKey()){
                  putProperty(stack,TETHER_TIME_TAG,-1);
                  putProperty(stack,TETHER_TARGET_TAG,"");
               }
            }
            
            int tetherTime = getIntProperty(stack,TETHER_TIME_TAG);
            if(tetherTime > 0){
               putProperty(stack,TETHER_TIME_TAG,tetherTime-1);
            }else if(tetherTime == 0){
               putProperty(stack,TETHER_TIME_TAG,-1);
               putProperty(stack,TETHER_TARGET_TAG,"");
            }
            
            if(world.getServer().getTicks() % (100) == 0){
               int energy = getEnergy(stack);
               boolean recharge = false;
               if(energy < 20){
                  recharge = true;
               }else if(energy < getMaxEnergy(stack) && ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.BLOODLETTER.id) >= 1 && player.getHealth() > 2){
                  recharge = true;
                  if(!player.isCreative() && !player.isSpectator()) player.setHealth(player.getHealth() - 2);
               }
               if(recharge){
                  addEnergy(stack, 20);
                  StringBuilder message = new StringBuilder("Glaive Charges: ");
                  for(int i = 1; i <= 5; i++){
                     message.append(getEnergy(stack) >= i * 20 ? "✦ " : "✧ ");
                  }
                  player.sendMessage(Text.literal(message.toString()).formatted(Formatting.BLACK), true);
               }
            }
         }
      }
      
      @Override
      public ActionResult use(World world, PlayerEntity playerEntity, Hand hand) {
         ItemStack stack = playerEntity.getStackInHand(hand);
         if(!(playerEntity instanceof ServerPlayerEntity player))
            return ActionResult.PASS;
         
         int energy = getEnergy(stack);
         String tetherTarget = getStringProperty(stack,TETHER_TARGET_TAG);
         
         if(tetherTarget != null && !tetherTarget.isEmpty() && !player.isSneaking()){
            if(energy >= 80){
               Entity target = player.getServerWorld().getEntity(MiscUtils.getUUID(tetherTarget));
               if(target == null || !target.isAlive() || player.getServerWorld().getRegistryKey() != target.getEntityWorld().getRegistryKey()){
                  player.sendMessage(Text.literal("The Glaive Has No Target").formatted(Formatting.BLACK),true);
               }else{
                  Vec3d targetPos = target.getPos();
                  Vec3d targetView = target.getRotationVecClient();
                  Vec3d tpPos = targetPos.add(targetView.multiply(-1.5,0,-1.5));
                  
                  ParticleEffectUtils.shadowGlaiveTp(player.getServerWorld(),player);
                  player.teleport(player.getServerWorld(),tpPos.x,tpPos.y+0.25,tpPos.z,new HashSet(),target.getYaw(),target.getPitch(),false);
                  ParticleEffectUtils.shadowGlaiveTp(player.getServerWorld(),player);
                  SoundUtils.playSound(world,player.getBlockPos(), SoundEvents.ENTITY_ILLUSIONER_CAST_SPELL, SoundCategory.PLAYERS,.8f,.8f);
                  addEnergy(stack,-80);
                  String message = "Glaive Charges: ";
                  for(int i=1; i<=5; i++){
                     message += getEnergy(stack) >= i*20 ? "✦ " : "✧ ";
                  }
                  player.sendMessage(Text.literal(message).formatted(Formatting.BLACK),true);
                  PLAYER_DATA.get(player).addXP(500); // Add xp
                  
                  if(target instanceof ServerPlayerEntity || target instanceof WardenEntity) ArcanaAchievements.progress(player,ArcanaAchievements.OMAE_WA.id,0);
                  if(target instanceof MobEntity){
                     if(ArcanaAchievements.isTimerActive(player,ArcanaAchievements.SHADOW_FURY.id)){
                        if(ArcanaAchievements.getProgress(player,ArcanaAchievements.SHADOW_FURY.id) % 2 == 1){
                           ArcanaAchievements.progress(player,ArcanaAchievements.SHADOW_FURY.id,1);
                        }
                     }else{
                        ArcanaAchievements.progress(player,ArcanaAchievements.SHADOW_FURY.id,0);
                     }
                  }
                  
                  int blindDur = new int[]{0,20,40,100}[Math.max(0, ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.PARANOIA.id))];
                  int invisDur = new int[]{0,20,40,100}[Math.max(0, ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.SHADOW_STRIDE.id))];
                  StatusEffectInstance invis = new StatusEffectInstance(ArcanaRegistry.GREATER_INVISIBILITY_EFFECT, invisDur, 0, false, false, true);
                  player.addStatusEffect(invis);
                  if(target instanceof LivingEntity living){
                     StatusEffectInstance blind = new StatusEffectInstance(ArcanaRegistry.GREATER_BLINDNESS_EFFECT, blindDur, 5, false, true, true);
                     living.addStatusEffect(blind);
                  }
                  
                  return ActionResult.SUCCESS;
               }
            }else{
               player.sendMessage(Text.literal("The Glaive Needs At Least 4 Charges").formatted(Formatting.BLACK),true);
               SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_FIRE_EXTINGUISH, 1,0.8f);
            }
         }else if(player.isSneaking()){
            if(energy >= 20){
               Vec3d playerPos = player.getPos();
               Vec3d view = player.getRotationVecClient();
               Vec3d tpPos = playerPos.add(view.multiply(teleportLength));
               
               ParticleEffectUtils.shadowGlaiveTp(player.getServerWorld(),player);
               player.teleport(player.getServerWorld(),tpPos.x,tpPos.y+0.25,tpPos.z,new HashSet(),player.getYaw(),player.getPitch(),false);
               ParticleEffectUtils.shadowGlaiveTp(player.getServerWorld(),player);
               SoundUtils.playSound(world,player.getBlockPos(), SoundEvents.ENTITY_ILLUSIONER_CAST_SPELL, SoundCategory.PLAYERS,.8f,.8f);
               addEnergy(stack,-20);
               String message = "Glaive Charges: ";
               for(int i=1; i<=5; i++){
                  message += getEnergy(stack) >= i*20 ? "✦ " : "✧ ";
               }
               player.sendMessage(Text.literal(message).formatted(Formatting.BLACK),true);
               PLAYER_DATA.get(player).addXP(100); // Add xp
               
               int invisDur = new int[]{0,20,40,100}[Math.max(0, ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.SHADOW_STRIDE.id))];
               StatusEffectInstance invis = new StatusEffectInstance(ArcanaRegistry.GREATER_INVISIBILITY_EFFECT, invisDur, 0, false, false, true);
               player.addStatusEffect(invis);
               
               return ActionResult.SUCCESS;
            }else{
               player.sendMessage(Text.literal("The Glaive Needs At Least 1 Charge").formatted(Formatting.BLACK),true);
               SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_FIRE_EXTINGUISH, 1,0.8f);
            }
         }
         return ActionResult.PASS;
      }
   }
}

