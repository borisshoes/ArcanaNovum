package net.borisshoes.arcananovum.items;

import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.items.core.EnergyItem;
import net.borisshoes.arcananovum.items.core.TickingItem;
import net.borisshoes.arcananovum.items.core.UsableItem;
import net.borisshoes.arcananovum.recipes.MagicItemIngredient;
import net.borisshoes.arcananovum.recipes.MagicItemRecipe;
import net.borisshoes.arcananovum.utils.MagicRarity;
import net.borisshoes.arcananovum.utils.ParticleEffectUtils;
import net.borisshoes.arcananovum.utils.SoundUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.WardenEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
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
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;

public class ShadowStalkersGlaive extends EnergyItem implements TickingItem, UsableItem {
   private final int teleportLength = 10;
   
   public ShadowStalkersGlaive(){
      id = "shadow_stalkers_glaive";
      name = "Shadow Stalkers Glaive";
      rarity = MagicRarity.LEGENDARY;
      categories = new ArcaneTome.TomeFilter[]{ArcaneTome.TomeFilter.LEGENDARY, ArcaneTome.TomeFilter.EQUIPMENT};
   
      ItemStack item = new ItemStack(Items.NETHERITE_SWORD);
      NbtCompound tag = item.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList loreList = new NbtList();
      NbtList enchants = new NbtList();
      NbtList attributes = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name","[{\"text\":\"Shadow Stalker's Glaive\",\"italic\":false,\"bold\":true,\"color\":\"#222222\"}]");
      loreList.add(NbtString.of("[{\"text\":\"This \",\"italic\":false,\"color\":\"dark_gray\"},{\"text\":\"blade \",\"color\":\"gray\"},{\"text\":\"lets you move through your opponents \"},{\"text\":\"shadow\",\"color\":\"blue\"},{\"text\":\".\",\"color\":\"dark_gray\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"The \",\"italic\":false,\"color\":\"dark_gray\"},{\"text\":\"blade \",\"color\":\"gray\"},{\"text\":\"stores the \"},{\"text\":\"blood \",\"color\":\"dark_red\"},{\"text\":\"from every strike and uses it as \"},{\"text\":\"energy\",\"color\":\"aqua\"},{\"text\":\".\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Stride \",\"italic\":false,\"color\":\"aqua\"},{\"text\":\"through the \",\"color\":\"dark_gray\"},{\"text\":\"darkness \",\"color\":\"blue\"},{\"text\":\"behind your opponent or \",\"color\":\"dark_gray\"},{\"text\":\"blink forward\"},{\"text\":\".\",\"color\":\"dark_gray\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Right Click\",\"italic\":false,\"color\":\"gray\"},{\"text\":\" to \",\"color\":\"dark_gray\"},{\"text\":\"teleport \",\"color\":\"dark_aqua\"},{\"text\":\"behind \",\"color\":\"blue\"},{\"text\":\"your most recently attacked foe.\",\"color\":\"dark_gray\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Sneak Right Click\",\"italic\":false,\"color\":\"gray\"},{\"text\":\" to \",\"color\":\"dark_gray\"},{\"text\":\"teleport \",\"color\":\"dark_aqua\"},{\"text\":\"a \",\"color\":\"dark_gray\"},{\"text\":\"short distance\",\"color\":\"blue\"},{\"text\":\".\",\"color\":\"dark_gray\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Legendary \",\"italic\":false,\"color\":\"gold\",\"bold\":true},{\"text\":\"Magic Item\",\"italic\":false,\"color\":\"dark_purple\",\"bold\":false}]"));
      display.put("Lore",loreList);
      NbtCompound dmg = new NbtCompound();
      dmg.putDouble("Amount",9.0);
      dmg.putString("AttributeName","generic.attack_damage");
      dmg.putString("Name","generic.attack_damage");
      dmg.putString("Slot","mainhand");
      dmg.putIntArray("UUID", new int[]{-122610,15966,916,-31932});
      attributes.add(dmg);
      NbtCompound spd = new NbtCompound();
      spd.putDouble("Amount",-2.0);
      spd.putString("AttributeName","generic.attack_speed");
      spd.putString("Name","generic.attack_speed");
      spd.putString("Slot","mainhand");
      spd.putIntArray("UUID", new int[]{-122610,16066,916,-32132});
      attributes.add(spd);
      tag.put("display",display);
      tag.put("Enchantments",enchants);
      tag.put("AttributeModifiers",attributes);
      tag.putInt("HideFlags",7);
      tag.putInt("Unbreakable",1);
   
      setBookLore(makeLore());
      setRecipe(makeRecipe());
      
      tag = addMagicNbt(tag);
      NbtCompound magicTag = tag.getCompound("arcananovum");
      magicTag.putString("lastAttacked","");
      magicTag.putInt("tether",-1);
      prefNBT = tag;
      item.setNbt(prefNBT);
      prefItem = item;
      
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
      return stack;
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
   public void onTick(ServerWorld world, ServerPlayerEntity player, ItemStack item){
      if(world.getServer().getTicks() % (20) == 0){
         NbtCompound itemNbt = item.getNbt();
         NbtCompound magicNbt = itemNbt.getCompound("arcananovum");
         
         String lastAtk = magicNbt.getString("lastAttacked");
         if(lastAtk != null && !lastAtk.isEmpty()){
            Entity target = player.getWorld().getEntity(UUID.fromString(lastAtk));
            if(target == null || !target.isAlive() || player.getWorld().getRegistryKey() != target.getEntityWorld().getRegistryKey()){
               magicNbt.putString("lastAttacked","");
               magicNbt.putInt("tether",-1);
            }
         }
   
         int tether = magicNbt.getInt("tether");
         if(tether > 0){
            magicNbt.putInt("tether",tether-1);
         }else if(tether == 0){
            magicNbt.putInt("tether",-1);
            magicNbt.putString("lastAttacked","");
         }
   
         if(world.getServer().getTicks() % (100) == 0){
            int energy = getEnergy(item);
            if(energy < 20){
               setEnergy(item,20);
               StringBuilder message = new StringBuilder("Glaive Charges: ");
               for(int i=1; i<=5; i++){
                  message.append(20 >= i * 20 ? "✦ " : "✧ ");
               }
               player.sendMessage(Text.literal(message.toString()).formatted(Formatting.BLACK),true);
            }
         }
      }
   }
   
   @Override
   public boolean useItem(PlayerEntity playerEntity, World world, Hand hand){
      if(!(playerEntity instanceof ServerPlayerEntity player))
         return true;
      ItemStack item = player.getStackInHand(hand);
      NbtCompound itemNbt = item.getNbt();
      NbtCompound magicNbt = itemNbt.getCompound("arcananovum");
      int energy = getEnergy(item);
      String lastAtk = magicNbt.getString("lastAttacked");
      
      if(lastAtk != null && !lastAtk.isEmpty() && !player.isSneaking()){
         if(energy >= 80){
            Entity target = player.getWorld().getEntity(UUID.fromString(lastAtk));
            if(target == null || !target.isAlive() || player.getWorld().getRegistryKey() != target.getEntityWorld().getRegistryKey()){
               player.sendMessage(Text.literal("The Glaive Has No Target").formatted(Formatting.BLACK),true);
            }else{
               Vec3d targetPos = target.getPos();
               Vec3d targetView = target.getRotationVecClient();
               Vec3d tpPos = targetPos.add(targetView.multiply(-1.5,0,-1.5));
      
               ParticleEffectUtils.shadowGlaiveTp(player.getWorld(),player);
               player.teleport(player.getWorld(),tpPos.x,tpPos.y+0.25,tpPos.z,target.getYaw(),target.getPitch());
               ParticleEffectUtils.shadowGlaiveTp(player.getWorld(),player);
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
               
               int blindDur = new int[]{0,20,40,100}[Math.max(0, ArcanaAugments.getAugmentOnItem(item,"paranoia"))];
               int invisDur = new int[]{0,20,100,200}[Math.max(0, ArcanaAugments.getAugmentOnItem(item,"shadow_stride"))];
               StatusEffectInstance invis = new StatusEffectInstance(StatusEffects.INVISIBILITY, invisDur, 0, false, false, true);
               player.addStatusEffect(invis);
               if(target instanceof LivingEntity living){
                  StatusEffectInstance blind = new StatusEffectInstance(StatusEffects.BLINDNESS, blindDur, 0, false, true, true);
                  living.addStatusEffect(blind);
               }
               
               return false;
            }
         }else{
            player.sendMessage(Text.literal("The Glaive Needs At Least 4 Charges").formatted(Formatting.BLACK),true);
            SoundUtils.playSound(world,playerEntity.getBlockPos(),SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.PLAYERS, 1, 0.8f);
         }
      }else if(player.isSneaking()){
         if(energy >= 20){
            Vec3d playerPos = player.getPos();
            Vec3d view = player.getRotationVecClient();
            Vec3d tpPos = playerPos.add(view.multiply(teleportLength));
   
            ParticleEffectUtils.shadowGlaiveTp(player.getWorld(),player);
            player.teleport(player.getWorld(),tpPos.x,tpPos.y+0.25,tpPos.z,player.getYaw(),player.getPitch());
            ParticleEffectUtils.shadowGlaiveTp(player.getWorld(),player);
            SoundUtils.playSound(world,player.getBlockPos(), SoundEvents.ENTITY_ILLUSIONER_CAST_SPELL, SoundCategory.PLAYERS,.8f,.8f);
            addEnergy(item,-20);
            String message = "Glaive Charges: ";
            for(int i=1; i<=5; i++){
               message += getEnergy(item) >= i*20 ? "✦ " : "✧ ";
            }
            player.sendMessage(Text.literal(message).formatted(Formatting.BLACK),true);
            PLAYER_DATA.get(player).addXP(100); // Add xp
   
            int invisDur = new int[]{0,20,100,200}[Math.max(0, ArcanaAugments.getAugmentOnItem(item,"shadow_stride"))];
            StatusEffectInstance invis = new StatusEffectInstance(StatusEffects.INVISIBILITY, invisDur, 0, false, false, true);
            player.addStatusEffect(invis);
            
            return false;
         }else{
            player.sendMessage(Text.literal("The Glaive Needs At Least 1 Charge").formatted(Formatting.BLACK),true);
            SoundUtils.playSound(world,playerEntity.getBlockPos(),SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.PLAYERS, 1, 0.8f);
         }
      }
      
      return true;
   }
   
   @Override
   public boolean useItem(PlayerEntity playerEntity, World world, Hand hand, BlockHitResult result){
      return true;
   }
   
   @Override
   public boolean useItem(PlayerEntity playerEntity, World world, Hand hand, Entity entity, @Nullable EntityHitResult entityHitResult){
      return true;
   }
   
   @Override
   public ItemStack forgeItem(Inventory inv){
      ItemStack toolStack = inv.getStack(12); // Should be the Sword
      ItemStack newMagicItem = getNewItem();
      NbtCompound nbt = toolStack.getNbt();
      if(nbt != null && nbt.contains("Enchantments")){
         NbtList enchants = nbt.getList("Enchantments", NbtElement.COMPOUND_TYPE);
         newMagicItem.getOrCreateNbt().put("Enchantments",enchants);
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
      return new MagicItemRecipe(ingredients);
   }
   
   private List<String> makeLore(){
      ArrayList<String> list = new ArrayList<>();
      list.add("{\"text\":\"   Shadow Stalker's\\n          Glaive\\nRarity: Legendary\\n\\nThis Blade was forged to mimic the power of Endermen to stride through the shadows and relentlessly follow foes. However, instead of using ender particles to warp through dimensions, this sword\"}");
      list.add("{\"text\":\"   Shadow Stalker's\\n          Glaive\\nuses a force of which we know very little. To fall through the shadows and emerge elsewhere is far different than what it feels like to channel Ender Energy. Blood that is spilled on the sword gets soaked up by shadowy tendrils that eminate from it.\"}");
      list.add("{\"text\":\"   Shadow Stalker's\\n          Glaive\\nStriking foes grants charges.\\nSneak Right Click consumes one charge to blink forward.\\nThe Glaive remembers the last target it struck and Right Clicking consumes four charges to teleport behind that target. \"}");
      return list;
   }
}
