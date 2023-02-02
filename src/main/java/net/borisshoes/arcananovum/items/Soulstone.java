package net.borisshoes.arcananovum.items;

import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.items.core.AttackingItem;
import net.borisshoes.arcananovum.items.core.MagicItem;
import net.borisshoes.arcananovum.items.core.UsableItem;
import net.borisshoes.arcananovum.recipes.MagicItemIngredient;
import net.borisshoes.arcananovum.recipes.MagicItemRecipe;
import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.borisshoes.arcananovum.utils.MagicRarity;
import net.borisshoes.arcananovum.utils.SoundUtils;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;

public class Soulstone extends MagicItem implements AttackingItem, UsableItem {
   public static int[] tiers = {25,100,250,500,1000,5000,10000};
   
   public Soulstone(){
      id = "soulstone";
      name = "Soulstone";
      rarity = MagicRarity.EMPOWERED;
      categories = new ArcaneTome.TomeFilter[]{ArcaneTome.TomeFilter.EMPOWERED, ArcaneTome.TomeFilter.ITEMS};
      itemVersion = 1;
      
      ItemStack item = new ItemStack(Items.FIRE_CHARGE);
      NbtCompound tag = item.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList loreList = new NbtList();
      NbtList enchants = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name","[{\"text\":\"Soulstone\",\"italic\":false,\"color\":\"dark_red\",\"bold\":true}]");
      loreList.add(NbtString.of("[{\"text\":\"The dark stone \",\"italic\":false,\"color\":\"dark_gray\"},{\"text\":\"crackles\",\"italic\":false,\"color\":\"red\"},{\"text\":\" with \"},{\"text\":\"red energy\",\"color\":\"dark_red\"},{\"text\":\".\",\"color\":\"dark_gray\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"The \",\"italic\":false,\"color\":\"dark_gray\"},{\"text\":\"souls\",\"color\":\"dark_purple\"},{\"text\":\" of mobs \"},{\"text\":\"killed\",\"color\":\"red\",\"italic\":false},{\"text\":\" seems to get \"},{\"text\":\"trapped\",\"color\":\"blue\"},{\"text\":\" inside...\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Unattuned\",\"italic\":false,\"color\":\"light_purple\"},{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Tier 0 - (0 Mobs Killed)\",\"italic\":false,\"color\":\"gray\"},{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Empowered\",\"italic\":false,\"color\":\"green\",\"bold\":true},{\"text\":\" Magic Item\",\"italic\":false,\"color\":\"dark_purple\",\"bold\":false}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
      tag.put("Enchantments",enchants);
   
      setBookLore(makeLore());
      setRecipe(makeRecipe());
      tag = addMagicNbt(tag);
      NbtCompound magicTag = tag.getCompound("arcananovum");
      magicTag.putString("type","unattuned");
      magicTag.putInt("souls",0);
      magicTag.putInt("maxTier",0);
      prefNBT = tag;
      item.setNbt(prefNBT);
      prefItem = item;
   }
   
   @Override
   public ItemStack updateItem(ItemStack stack, MinecraftServer server){
      NbtCompound itemNbt = stack.getNbt();
      NbtCompound magicTag = itemNbt.getCompound("arcananovum");
      int souls = magicTag.getInt("souls");
      int maxTier = magicTag.getInt("maxTier");
      String type = magicTag.getString("type");
      NbtCompound newTag = super.updateItem(stack,server).getNbt();
      newTag.getCompound("arcananovum").putInt("souls",souls);
      newTag.getCompound("arcananovum").putInt("maxTier",maxTier);
      newTag.getCompound("arcananovum").putString("type",type);
      stack.setNbt(newTag);
      redoLore(stack);
      return stack;
   }
   
   private void redoLore(ItemStack stack){
      NbtCompound itemNbt = stack.getNbt();
      NbtCompound magicNbt = itemNbt.getCompound("arcananovum");
      String type = magicNbt.getString("type");
      int souls = magicNbt.getInt("souls");
      int tier = soulsToTier(souls);
      NbtList loreList = itemNbt.getCompound("display").getList("Lore", NbtType.STRING);
      if(!type.equals("unattuned")){
         String entityTypeName = EntityType.get(type).get().getName().getString();
         loreList.set(3,NbtString.of("[{\"text\":\"Attuned - "+entityTypeName+"\",\"italic\":false,\"color\":\"light_purple\"}]"));
      }
      if(souls != 0){
         loreList.set(4,NbtString.of("[{\"text\":\"Tier "+tier+" - ("+souls+" Mobs Killed)\",\"italic\":false,\"color\":\"gray\"}]"));
      }
   }
   
   @Override
   public boolean attackEntity(PlayerEntity playerEntity, World world, Hand hand, Entity entity, @Nullable EntityHitResult hitResult){
      ItemStack item = playerEntity.getStackInHand(hand);
      NbtCompound itemNbt = item.getNbt();
      NbtCompound magicNbt = itemNbt.getCompound("arcananovum");
      String type = magicNbt.getString("type");
      
      if(type.equals("unattuned") && entity instanceof MobEntity attackedEntity && playerEntity instanceof ServerPlayerEntity player){
         String entityTypeId = EntityType.getId(attackedEntity.getType()).toString();
         String entityTypeName = EntityType.get(entityTypeId).get().getName().getString();
         
         magicNbt.putString("type",entityTypeId);
         NbtList loreList = itemNbt.getCompound("display").getList("Lore", NbtType.STRING);
         loreList.set(3,NbtString.of("[{\"text\":\"Attuned - "+entityTypeName+"\",\"italic\":false,\"color\":\"light_purple\"}]"));
         player.sendMessage(Text.translatable("The Soulstone attunes to the essence of "+entityTypeName).formatted(Formatting.DARK_RED,Formatting.ITALIC),true);
         SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_RESPAWN_ANCHOR_SET_SPAWN, 1,.5f);
      }
      return true;
   }
   
   public void killedEntity(ServerWorld world, ServerPlayerEntity player, LivingEntity dead, ItemStack item){
      NbtCompound itemNbt = item.getNbt();
      NbtCompound magicNbt = itemNbt.getCompound("arcananovum");
      int souls = magicNbt.getInt("souls");
      int maxTier = magicNbt.getInt("maxTier");
      
      String entityTypeId = EntityType.getId(dead.getType()).toString();
      String entityTypeName = EntityType.get(entityTypeId).get().getName().getString();
   
      souls++;
      int tier = soulsToTier(souls);
      if(tier != soulsToTier(souls-1)){
         // Level up notification
         player.sendMessage(Text.translatable("Your Soulstone crackles with new power!").formatted(Formatting.DARK_RED,Formatting.ITALIC),true);
         SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_RESPAWN_ANCHOR_CHARGE, 1,1f);
         if(tier > maxTier){
            PLAYER_DATA.get(player).addXP(souls*50); // Add xp
            magicNbt.putInt("maxTier",tier);
         }
         if(tier == 7) ArcanaAchievements.grant(player,"prime_evil");
         if(tier == 5) ArcanaAchievements.grant(player,"philosopher_stone");
         if(tier == 3 && entityTypeId.equals("minecraft:villager")) ArcanaAchievements.grant(player,"took_a_village");
      }
      magicNbt.putInt("souls",souls);
      
      NbtList loreList = itemNbt.getCompound("display").getList("Lore", NbtType.STRING);
      loreList.set(4,NbtString.of("[{\"text\":\"Tier "+tier+" - ("+souls+" Mobs Killed)\",\"italic\":false,\"color\":\"gray\"}]"));
   }
   
   public static String getType(ItemStack item){
      if(!(MagicItemUtils.identifyItem(item) instanceof Soulstone)){
         return null;
      }
      NbtCompound itemNbt = item.getNbt();
      NbtCompound magicNbt = itemNbt.getCompound("arcananovum");
      String type = magicNbt.getString("type");
      if(type == null){
         return "unattuned";
      }else{
         return type;
      }
   }
   
   public static ItemStack setType(ItemStack stack, EntityType<? extends MobEntity> type){
      if(!(MagicItemUtils.identifyItem(stack) instanceof Soulstone)){
         return null;
      }
      ItemStack item = stack.copy();
   
      NbtCompound itemNbt = item.getNbt();
      NbtCompound magicNbt = itemNbt.getCompound("arcananovum");
   
      String entityTypeId = EntityType.getId(type).toString();
      String entityTypeName = EntityType.get(entityTypeId).get().getName().getString();
      magicNbt.putString("type",entityTypeId);
      NbtList loreList = itemNbt.getCompound("display").getList("Lore", NbtType.STRING);
      loreList.set(3,NbtString.of("[{\"text\":\"Attuned - "+entityTypeName+"\",\"italic\":false,\"color\":\"light_purple\"}]"));
      return item;
   }
   
   public static int getSouls(ItemStack item){
      if(!(MagicItemUtils.identifyItem(item) instanceof Soulstone)){
         return -1;
      }
      NbtCompound itemNbt = item.getNbt();
      NbtCompound magicNbt = itemNbt.getCompound("arcananovum");
      return magicNbt.getInt("souls");
   }
   
   public static ItemStack setSouls(ItemStack stack, int newSouls){
      if(!(MagicItemUtils.identifyItem(stack) instanceof Soulstone)){
         return null;
      }
      ItemStack item = stack.copy();
   
      NbtCompound itemNbt = item.getNbt();
      NbtCompound magicNbt = itemNbt.getCompound("arcananovum");
      int souls = magicNbt.getInt("souls");
      newSouls = MathHelper.clamp(newSouls,0,Integer.MAX_VALUE);
      
      int tier = soulsToTier(newSouls);
      magicNbt.putInt("souls",newSouls);
   
      NbtList loreList = itemNbt.getCompound("display").getList("Lore", NbtType.STRING);
      loreList.set(4,NbtString.of("[{\"text\":\"Tier "+tier+" - ("+newSouls+" Mobs Killed)\",\"italic\":false,\"color\":\"gray\"}]"));
      return item;
   }
   
   public static ItemStack getShowcaseItem(int souls, @Nullable String typeName){
      ItemStack item = new ItemStack(Items.FIRE_CHARGE);
      NbtCompound tag = item.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList loreList = new NbtList();
      NbtList enchants = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name","[{\"text\":\"Soulstone\",\"italic\":false,\"color\":\"dark_red\",\"bold\":true}]");
      loreList.add(NbtString.of("[{\"text\":\"The dark stone \",\"italic\":false,\"color\":\"dark_gray\"},{\"text\":\"crackles\",\"italic\":true,\"color\":\"red\"},{\"text\":\" with \"},{\"text\":\"red energy\",\"color\":\"dark_red\"},{\"text\":\".\",\"color\":\"dark_gray\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"The \",\"italic\":false,\"color\":\"dark_gray\"},{\"text\":\"souls\",\"color\":\"dark_purple\"},{\"text\":\" of mobs \"},{\"text\":\"killed\",\"color\":\"red\",\"italic\":true},{\"text\":\" seems to get \"},{\"text\":\"trapped\",\"color\":\"blue\"},{\"text\":\" inside...\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      if(typeName == null || typeName.equalsIgnoreCase("unattuned")){
         loreList.add(NbtString.of("[{\"text\":\"Unattuned\",\"italic\":false,\"color\":\"light_purple\"},{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      }else{
         loreList.add(NbtString.of("[{\"text\":\"Attuned - "+typeName+"\",\"italic\":false,\"color\":\"light_purple\"}]"));
      }
      int tier = soulsToTier(souls);
      loreList.add(NbtString.of("[{\"text\":\"Tier "+tier+" - ("+souls+" Mobs Killed)\",\"italic\":false,\"color\":\"gray\"},{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Empowered\",\"italic\":false,\"color\":\"green\",\"bold\":true},{\"text\":\" Magic Item\",\"italic\":false,\"color\":\"dark_purple\",\"bold\":false}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
      tag.put("Enchantments",enchants);
      
      item.setNbt(tag);
      return item;
   }
   
   public static int soulsToTier(int souls){
      for(int i=0; i<tiers.length; i++){
         if(souls >= tiers[i]){
            continue;
         }else if(souls < tiers[i]){
            return i;
         }
      }
      return tiers.length;
   }
   
   @Override
   public boolean useItem(PlayerEntity playerEntity, World world, Hand hand){
      return false;
   }
   
   @Override
   public boolean useItem(PlayerEntity playerEntity, World world, Hand hand, BlockHitResult result){
      return false;
   }
   
   private List<String> makeLore(){
      ArrayList<String> list = new ArrayList<>();
      list.add("{\"text\":\"       Soulstone\\n\\nRarity: Empowered\\n\\nHow to imprison a soul... A seemingly impossible task if it weren't for some materials the Nether-dwellers have been working with for millenia. Soulsand, Crying Obsidian and Netherite when put\"}");
      list.add("{\"text\":\"       Soulstone\\n\\ntogether seem to be capable of constructing a near inescapable bulk prison for souls of a single type.\\n\\nThe Soulstone should be able to attune to a type of mob by merely using it to draw blood. After that any souls\"}");
      list.add("{\"text\":\"       Soulstone\\n\\nof that type that are freed from their mortal existence should be sucked into the stone like a black hole, never to be released... \\n\\nUntil I find a way to use them...\"}");
      return list;
   }
   
   private MagicItemRecipe makeRecipe(){
      MagicItemIngredient b = new MagicItemIngredient(Items.NETHERITE_BLOCK,1,null);
      MagicItemIngredient p = new MagicItemIngredient(Items.CRYING_OBSIDIAN,32,null);
      MagicItemIngredient o = new MagicItemIngredient(Items.OBSIDIAN,64,null);
      MagicItemIngredient s = new MagicItemIngredient(Items.SOUL_SAND,64,null);
      MagicItemIngredient n = new MagicItemIngredient(Items.NETHERITE_INGOT,1,null);
      MagicItemIngredient r = new MagicItemIngredient(Items.RED_NETHER_BRICKS,64,null);
      
      MagicItemIngredient[][] ingredients = {
            {o,p,r,p,o},
            {p,n,s,n,p},
            {r,s,b,s,r},
            {p,n,s,n,p},
            {o,p,r,p,o}};
      return new MagicItemRecipe(ingredients);
   }
}
