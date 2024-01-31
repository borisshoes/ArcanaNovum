package net.borisshoes.arcananovum.items;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.EnergyItem;
import net.borisshoes.arcananovum.core.polymer.MagicPolymerSwordItem;
import net.borisshoes.arcananovum.recipes.arcana.ForgeRequirement;
import net.borisshoes.arcananovum.recipes.arcana.MagicItemIngredient;
import net.borisshoes.arcananovum.recipes.arcana.MagicItemRecipe;
import net.borisshoes.arcananovum.utils.*;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.WardenEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.ToolMaterials;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Pair;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;

public class ShadowStalkersGlaive extends EnergyItem {
   private final int teleportLength = 10;
   private static final String TXT = "item/shadow_stalkers_glaive";
   
   public ShadowStalkersGlaive(){
      id = "shadow_stalkers_glaive";
      name = "Shadow Stalkers Glaive";
      rarity = MagicRarity.LEGENDARY;
      categories = new ArcaneTome.TomeFilter[]{ArcaneTome.TomeFilter.LEGENDARY, ArcaneTome.TomeFilter.EQUIPMENT};
      vanillaItem = Items.NETHERITE_SWORD;
      item = new ShadowStalkersGlaiveItem(new FabricItemSettings().maxCount(1).fireproof());
      models = new ArrayList<>();
      models.add(new Pair<>(vanillaItem,TXT));
   
      ItemStack stack = new ItemStack(item);
      NbtCompound tag = stack.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList enchants = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name","[{\"text\":\"Shadow Stalker's Glaive\",\"italic\":false,\"bold\":true,\"color\":\"#222222\"}]");
      tag.put("display",display);
      tag.put("Enchantments",enchants);
      tag.putInt("HideFlags", 255);
      tag.putInt("Unbreakable",1);
      buildItemLore(stack, ArcanaNovum.SERVER);
   
      setBookLore(makeLore());
      setRecipe(makeRecipe());
      
      tag = addMagicNbt(tag);
      NbtCompound magicTag = tag.getCompound("arcananovum");
      magicTag.putString("lastAttacked","");
      magicTag.putInt("tether",-1);
      prefNBT = tag;
      stack.setNbt(prefNBT);
      prefItem = stack;
   }
   
   @Override
   public NbtList getItemLore(@Nullable ItemStack itemStack){
      NbtList loreList = new NbtList();
      loreList.add(NbtString.of("[{\"text\":\"This \",\"italic\":false,\"color\":\"dark_gray\"},{\"text\":\"blade \",\"color\":\"gray\"},{\"text\":\"lets you move through your opponents \"},{\"text\":\"shadow\",\"color\":\"blue\"},{\"text\":\".\",\"color\":\"dark_gray\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"The \",\"italic\":false,\"color\":\"dark_gray\"},{\"text\":\"blade \",\"color\":\"gray\"},{\"text\":\"stores the \"},{\"text\":\"blood \",\"color\":\"dark_red\"},{\"text\":\"from every strike and uses it as \"},{\"text\":\"energy\",\"color\":\"aqua\"},{\"text\":\".\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Stride \",\"italic\":false,\"color\":\"aqua\"},{\"text\":\"through the \",\"color\":\"dark_gray\"},{\"text\":\"darkness \",\"color\":\"blue\"},{\"text\":\"behind your opponent or \",\"color\":\"dark_gray\"},{\"text\":\"blink forward\"},{\"text\":\".\",\"color\":\"dark_gray\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Right Click\",\"italic\":false,\"color\":\"gray\"},{\"text\":\" to \",\"color\":\"dark_gray\"},{\"text\":\"teleport \",\"color\":\"dark_aqua\"},{\"text\":\"behind \",\"color\":\"blue\"},{\"text\":\"your most recently attacked foe.\",\"color\":\"dark_gray\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Sneak Right Click\",\"italic\":false,\"color\":\"gray\"},{\"text\":\" to \",\"color\":\"dark_gray\"},{\"text\":\"teleport \",\"color\":\"dark_aqua\"},{\"text\":\"a \",\"color\":\"dark_gray\"},{\"text\":\"short distance\",\"color\":\"blue\"},{\"text\":\".\",\"color\":\"dark_gray\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      return loreList;
   }
   
   @Override
   public int getMaxEnergy(ItemStack item){
      return 100; // 100 damage stored
   }
   
   @Override
   public ItemStack updateItem(ItemStack stack, MinecraftServer server){
      NbtCompound itemNbt = stack.getNbt();
      NbtCompound magicTag = itemNbt.getCompound("arcananovum");
      NbtList enchants = itemNbt.getList("Enchantments", NbtElement.COMPOUND_TYPE);
      String lastAttacked = magicTag.getString("lastAttacked");
      int tether = magicTag.getInt("tether");
      NbtCompound newTag = super.updateItem(stack,server).getNbt();
      if(enchants != null) newTag.put("Enchantments", enchants);
      newTag.getCompound("arcananovum").putInt("tether",tether);
      newTag.getCompound("arcananovum").putString("lastAttacked",lastAttacked);
      stack.setNbt(newTag);
      return buildItemLore(stack,server);
   }
   
   public void entityAttacked(PlayerEntity player, ItemStack item, Entity entity){
      if(entity instanceof MobEntity || entity instanceof PlayerEntity){
         NbtCompound itemNbt = item.getNbt();
         NbtCompound magicNbt = itemNbt.getCompound("arcananovum");
         magicNbt.putString("lastAttacked",entity.getUuidAsString());
         magicNbt.putInt("tether",60);
      }
   }
   
   @Override
   public ItemStack forgeItem(Inventory inv){
      ItemStack toolStack = inv.getStack(12); // Should be the Sword
      ItemStack newMagicItem = getNewItem();
      NbtCompound nbt = toolStack.getNbt();
      if(nbt == null) return newMagicItem;
      NbtCompound newNbt = newMagicItem.getOrCreateNbt();
      if(nbt.contains("Enchantments")){
         NbtList enchants = nbt.getList("Enchantments", NbtElement.COMPOUND_TYPE);
         newNbt.put("Enchantments",enchants);
      }
      if(nbt.contains("ArcanaStats")){
         double percentile = nbt.getDouble("ArcanaStats");
         newNbt.putDouble("ArcanaStats",percentile);
         EnhancedStatUtils.enhanceItem(newMagicItem,percentile);
      }
      return newMagicItem;
   }
   
   private MagicItemRecipe makeRecipe(){
      MagicItemIngredient n = new MagicItemIngredient(Items.NETHERITE_INGOT,4,null);
      MagicItemIngredient s = new MagicItemIngredient(Items.NETHER_STAR,4,null);
      MagicItemIngredient e = new MagicItemIngredient(Items.ENDER_EYE,64,null);
      MagicItemIngredient p = new MagicItemIngredient(Items.CRYING_OBSIDIAN,64,null);
      MagicItemIngredient o = new MagicItemIngredient(Items.OBSIDIAN,64,null);
      MagicItemIngredient t = new MagicItemIngredient(Items.NETHERITE_SWORD,1,null, true);
      
      MagicItemIngredient[][] ingredients = {
            {e,o,s,o,e},
            {o,p,n,p,o},
            {s,n,t,n,s},
            {o,p,n,p,o},
            {e,o,s,o,e}};
      return new MagicItemRecipe(ingredients, new ForgeRequirement().withAnvil().withCore());
   }
   
   private List<String> makeLore(){
      ArrayList<String> list = new ArrayList<>();
      list.add("{\"text\":\"   Shadow Stalker's\\n          Glaive\\nRarity: Legendary\\n\\nThis Blade was forged to mimic the power of Endermen to stride through the shadows and relentlessly follow foes. However, instead of using ender particles to warp through dimensions, this sword\"}");
      list.add("{\"text\":\"   Shadow Stalker's\\n          Glaive\\nuses a force of which we know very little. To fall through the shadows and emerge elsewhere is far different than what it feels like to channel Ender Energy. Blood that is spilled on the sword gets soaked up by shadowy tendrils that eminate from it.\"}");
      list.add("{\"text\":\"   Shadow Stalker's\\n          Glaive\\nStriking foes grants charges.\\nSneak Right Click consumes one charge to blink forward.\\nThe Glaive remembers the last target it struck and Right Clicking consumes four charges to teleport behind that target. \"}");
      return list;
   }
   
   public class ShadowStalkersGlaiveItem extends MagicPolymerSwordItem {
      public ShadowStalkersGlaiveItem(Settings settings){
         super(getThis(),ToolMaterials.NETHERITE,5,-2.0f,settings);
      }
      
      @Override
      public int getPolymerCustomModelData(ItemStack itemStack, @Nullable ServerPlayerEntity player){
         return ArcanaRegistry.MODELS.get(TXT).value();
      }
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
      
      @Override
      public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected){
         if(!MagicItemUtils.isMagic(stack)) return;
         if(!(world instanceof ServerWorld && entity instanceof ServerPlayerEntity player)) return;
         if(world.getServer().getTicks() % (20) == 0){
            NbtCompound itemNbt = stack.getNbt();
            NbtCompound magicNbt = itemNbt.getCompound("arcananovum");
            
            String lastAtk = magicNbt.getString("lastAttacked");
            if(lastAtk != null && !lastAtk.isEmpty()){
               Entity target = player.getServerWorld().getEntity(UUID.fromString(lastAtk));
               if(target == null || !target.isAlive() || player.getServerWorld().getRegistryKey() != target.getEntityWorld().getRegistryKey()){
                  magicNbt.putString("lastAttacked", "");
                  magicNbt.putInt("tether", -1);
               }
            }
            
            int tether = magicNbt.getInt("tether");
            if(tether > 0){
               magicNbt.putInt("tether", tether - 1);
            }else if(tether == 0){
               magicNbt.putInt("tether", -1);
               magicNbt.putString("lastAttacked", "");
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
      public TypedActionResult<ItemStack> use(World world, PlayerEntity playerEntity, Hand hand) {
         ItemStack item = playerEntity.getStackInHand(hand);
         if(!(playerEntity instanceof ServerPlayerEntity player))
            return TypedActionResult.pass(item);
         
         NbtCompound itemNbt = item.getNbt();
         NbtCompound magicNbt = itemNbt.getCompound("arcananovum");
         int energy = getEnergy(item);
         String lastAtk = magicNbt.getString("lastAttacked");
         
         if(lastAtk != null && !lastAtk.isEmpty() && !player.isSneaking()){
            if(energy >= 80){
               Entity target = player.getServerWorld().getEntity(UUID.fromString(lastAtk));
               if(target == null || !target.isAlive() || player.getServerWorld().getRegistryKey() != target.getEntityWorld().getRegistryKey()){
                  player.sendMessage(Text.literal("The Glaive Has No Target").formatted(Formatting.BLACK),true);
               }else{
                  Vec3d targetPos = target.getPos();
                  Vec3d targetView = target.getRotationVecClient();
                  Vec3d tpPos = targetPos.add(targetView.multiply(-1.5,0,-1.5));
                  
                  ParticleEffectUtils.shadowGlaiveTp(player.getServerWorld(),player);
                  player.teleport(player.getServerWorld(),tpPos.x,tpPos.y+0.25,tpPos.z,target.getYaw(),target.getPitch());
                  ParticleEffectUtils.shadowGlaiveTp(player.getServerWorld(),player);
                  SoundUtils.playSound(world,player.getBlockPos(), SoundEvents.ENTITY_ILLUSIONER_CAST_SPELL, SoundCategory.PLAYERS,.8f,.8f);
                  addEnergy(item,-80);
                  String message = "Glaive Charges: ";
                  for(int i=1; i<=5; i++){
                     message += getEnergy(item) >= i*20 ? "✦ " : "✧ ";
                  }
                  player.sendMessage(Text.literal(message).formatted(Formatting.BLACK),true);
                  PLAYER_DATA.get(player).addXP(500); // Add xp
                  
                  if(target instanceof ServerPlayerEntity || target instanceof WardenEntity) ArcanaAchievements.progress(player,"omae_wa",0);
                  if(target instanceof MobEntity) ArcanaAchievements.progress(player,"shadow_fury",0);
                  
                  int blindDur = new int[]{0,20,40,100}[Math.max(0, ArcanaAugments.getAugmentOnItem(item,ArcanaAugments.PARANOIA.id))];
                  int invisDur = new int[]{0,20,40,100}[Math.max(0, ArcanaAugments.getAugmentOnItem(item,ArcanaAugments.SHADOW_STRIDE.id))];
                  StatusEffectInstance invis = new StatusEffectInstance(ArcanaRegistry.GREATER_INVISIBILITY_EFFECT, invisDur, 0, false, false, true);
                  player.addStatusEffect(invis);
                  if(target instanceof LivingEntity living){
                     StatusEffectInstance blind = new StatusEffectInstance(ArcanaRegistry.GREATER_BLINDNESS_EFFECT, blindDur, 5, false, true, true);
                     living.addStatusEffect(blind);
                  }
                  
                  return TypedActionResult.success(item);
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
               player.teleport(player.getServerWorld(),tpPos.x,tpPos.y+0.25,tpPos.z,player.getYaw(),player.getPitch());
               ParticleEffectUtils.shadowGlaiveTp(player.getServerWorld(),player);
               SoundUtils.playSound(world,player.getBlockPos(), SoundEvents.ENTITY_ILLUSIONER_CAST_SPELL, SoundCategory.PLAYERS,.8f,.8f);
               addEnergy(item,-20);
               String message = "Glaive Charges: ";
               for(int i=1; i<=5; i++){
                  message += getEnergy(item) >= i*20 ? "✦ " : "✧ ";
               }
               player.sendMessage(Text.literal(message).formatted(Formatting.BLACK),true);
               PLAYER_DATA.get(player).addXP(100); // Add xp
               
               int invisDur = new int[]{0,20,40,100}[Math.max(0, ArcanaAugments.getAugmentOnItem(item,ArcanaAugments.SHADOW_STRIDE.id))];
               StatusEffectInstance invis = new StatusEffectInstance(ArcanaRegistry.GREATER_INVISIBILITY_EFFECT, invisDur, 0, false, false, true);
               player.addStatusEffect(invis);
               
               return TypedActionResult.success(item);
            }else{
               player.sendMessage(Text.literal("The Glaive Needs At Least 1 Charge").formatted(Formatting.BLACK),true);
               SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_FIRE_EXTINGUISH, 1,0.8f);
            }
         }
         return TypedActionResult.pass(item);
      }
   }
}
