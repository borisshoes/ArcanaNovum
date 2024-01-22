package net.borisshoes.arcananovum.items;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.MagicItem;
import net.borisshoes.arcananovum.core.polymer.MagicPolymerItem;
import net.borisshoes.arcananovum.recipes.arcana.MagicItemIngredient;
import net.borisshoes.arcananovum.recipes.arcana.MagicItemRecipe;
import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.borisshoes.arcananovum.utils.MagicRarity;
import net.borisshoes.arcananovum.utils.SoundUtils;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
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
import net.minecraft.util.Pair;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;

public class Soulstone extends MagicItem {
   public static int[] tiers = {25,100,250,500,1000,5000,10000};
   private static final String UNATTUNED_TXT = "item/soulstone_unattuned";
   private static final String T0_TXT = "item/soulstone_0";
   private static final String T1_TXT = "item/soulstone_1";
   private static final String T2_TXT = "item/soulstone_2";
   private static final String T3_TXT = "item/soulstone_3";
   private static final String T4_TXT = "item/soulstone_4";
   private static final String T5_TXT = "item/soulstone_5";
   private static final String T6_TXT = "item/soulstone_6";
   private static final String T7_TXT = "item/soulstone_7";
   
   public Soulstone(){
      id = "soulstone";
      name = "Soulstone";
      rarity = MagicRarity.EMPOWERED;
      categories = new ArcaneTome.TomeFilter[]{ArcaneTome.TomeFilter.EMPOWERED, ArcaneTome.TomeFilter.ITEMS};
      itemVersion = 1;
      vanillaItem = Items.FIRE_CHARGE;
      item = new SoulstoneItem(new FabricItemSettings().maxCount(1).fireproof());
      models = new ArrayList<>();
      models.add(new Pair<>(vanillaItem,UNATTUNED_TXT));
      models.add(new Pair<>(vanillaItem,T0_TXT));
      models.add(new Pair<>(vanillaItem,T1_TXT));
      models.add(new Pair<>(vanillaItem,T2_TXT));
      models.add(new Pair<>(vanillaItem,T3_TXT));
      models.add(new Pair<>(vanillaItem,T4_TXT));
      models.add(new Pair<>(vanillaItem,T5_TXT));
      models.add(new Pair<>(vanillaItem,T6_TXT));
      models.add(new Pair<>(vanillaItem,T7_TXT));
      
      ItemStack stack = new ItemStack(item);
      NbtCompound tag = stack.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList enchants = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name","[{\"text\":\"Soulstone\",\"italic\":false,\"color\":\"dark_red\",\"bold\":true}]");
      tag.put("display",display);
      tag.put("Enchantments",enchants);
      buildItemLore(stack, ArcanaNovum.SERVER);

      setBookLore(makeLore());
      setRecipe(makeRecipe());
      tag = addMagicNbt(tag);
      NbtCompound magicTag = tag.getCompound("arcananovum");
      magicTag.putString("type","unattuned");
      magicTag.putInt("souls",0);
      magicTag.putInt("maxTier",0);
      prefNBT = tag;
      stack.setNbt(prefNBT);
      prefItem = stack;
   }
   
   @Override
   public NbtList getItemLore(@Nullable ItemStack itemStack){
      NbtCompound itemNbt = itemStack.getNbt();
      NbtCompound magicNbt = itemNbt.getCompound("arcananovum");
      String type = magicNbt.getString("type");
      int souls = magicNbt.getInt("souls");
      int tier = soulsToTier(souls);
      
      NbtList loreList = new NbtList();
      loreList.add(NbtString.of("[{\"text\":\"The dark stone \",\"italic\":false,\"color\":\"dark_gray\"},{\"text\":\"crackles\",\"italic\":false,\"color\":\"red\"},{\"text\":\" with \"},{\"text\":\"red energy\",\"color\":\"dark_red\"},{\"text\":\".\",\"color\":\"dark_gray\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"The \",\"italic\":false,\"color\":\"dark_gray\"},{\"text\":\"souls\",\"color\":\"dark_purple\"},{\"text\":\" of mobs \"},{\"text\":\"killed\",\"color\":\"red\",\"italic\":false},{\"text\":\" seems to get \"},{\"text\":\"trapped\",\"color\":\"blue\"},{\"text\":\" inside...\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      
      Optional<EntityType<?>> opt = EntityType.get(type);
      if(!type.equals("unattuned") && opt.isPresent()){
         String entityTypeName = opt.get().getName().getString();
         loreList.add(NbtString.of("[{\"text\":\"Attuned - "+entityTypeName+"\",\"italic\":false,\"color\":\"light_purple\"}]"));
      }else{
         loreList.add(NbtString.of("[{\"text\":\"Unattuned\",\"italic\":false,\"color\":\"light_purple\"},{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      }
      if(souls != 0){
         loreList.add(NbtString.of("[{\"text\":\"Tier "+tier+" - ("+souls+" Mobs Killed)\",\"italic\":false,\"color\":\"gray\"}]"));
      }else{
         loreList.add(NbtString.of("[{\"text\":\"Tier 0 - (0 Mobs Killed)\",\"italic\":false,\"color\":\"gray\"},{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      }
      
      return loreList;
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
      return buildItemLore(stack,server);
   }
   
   public void killedEntity(ServerWorld world, ServerPlayerEntity player, LivingEntity dead, ItemStack item){
      NbtCompound itemNbt = item.getNbt();
      NbtCompound magicNbt = itemNbt.getCompound("arcananovum");
      int souls = magicNbt.getInt("souls");
      int maxTier = magicNbt.getInt("maxTier");
      
      String entityTypeId = EntityType.getId(dead.getType()).toString();
   
      int toAdd = new int[]{1,2,3,4,5,10}[Math.max(0,ArcanaAugments.getAugmentOnItem(item,ArcanaAugments.SOUL_REAPER.id))];
      souls += toAdd;
      
      int tier = soulsToTier(souls);
      if(tier != soulsToTier(souls-toAdd)){
         // Level up notification
         player.sendMessage(Text.literal("Your Soulstone crackles with new power!").formatted(Formatting.DARK_RED,Formatting.ITALIC),true);
         SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_RESPAWN_ANCHOR_CHARGE, 1,1f);
         if(tier > maxTier){
            PLAYER_DATA.get(player).addXP(souls*50); // Add xp
            magicNbt.putInt("maxTier",tier);
         }
         if(tier == 7) ArcanaAchievements.grant(player,ArcanaAchievements.PRIME_EVIL.id);
         if(tier == 5) ArcanaAchievements.grant(player,ArcanaAchievements.PHILOSOPHER_STONE.id);
         if(tier == 3 && entityTypeId.equals("minecraft:villager")) ArcanaAchievements.grant(player,ArcanaAchievements.TOOK_A_VILLAGE.id);
      }
      magicNbt.putInt("souls",souls);
      buildItemLore(item,player.getServer());
   }
   
   public static String getType(ItemStack item){
      if(!(MagicItemUtils.identifyItem(item) instanceof Soulstone)){
         return null;
      }
      NbtCompound itemNbt = item.getNbt();
      NbtCompound magicNbt = itemNbt.getCompound("arcananovum");
      String type = magicNbt.getString("type");
      return Objects.requireNonNullElse(type, "unattuned");
   }
   
   public static ItemStack setType(ItemStack stack, EntityType<?> type){
      if(!(MagicItemUtils.identifyItem(stack) instanceof Soulstone)){
         return null;
      }
      ItemStack item = stack.copy();
      NbtCompound itemNbt = item.getNbt();
      NbtCompound magicNbt = itemNbt.getCompound("arcananovum");
      String entityTypeId = EntityType.getId(type).toString();
      magicNbt.putString("type",entityTypeId);
      return ArcanaRegistry.SOULSTONE.buildItemLore(item,ArcanaNovum.SERVER);
   }
   
   public static ItemStack setUnattuned(ItemStack stack){
      if(!(MagicItemUtils.identifyItem(stack) instanceof Soulstone)){
         return null;
      }
      ItemStack item = setSouls(stack,0);
      
      NbtCompound itemNbt = item.getNbt();
      NbtCompound magicNbt = itemNbt.getCompound("arcananovum");
      
      magicNbt.putString("type","unattuned");
      return ArcanaRegistry.SOULSTONE.buildItemLore(item,ArcanaNovum.SERVER);
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
      newSouls = MathHelper.clamp(newSouls,0,Integer.MAX_VALUE);
      
      magicNbt.putInt("souls",newSouls);
      return ArcanaRegistry.SOULSTONE.buildItemLore(item,ArcanaNovum.SERVER);
   }
   
   public static ItemStack getShowcaseItem(int souls, @Nullable String typeId){
      ItemStack item = ArcanaRegistry.SOULSTONE.getItem().getDefaultStack().copy();
      
      if(typeId != null && EntityType.get(typeId).isPresent()){
         EntityType<?> type = EntityType.get(typeId).get();
         item = setType(item, type);
      }
      item = setSouls(item,souls);
      return ArcanaRegistry.SOULSTONE.buildItemLore(item, ArcanaNovum.SERVER);
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
   
   private List<String> makeLore(){
      ArrayList<String> list = new ArrayList<>();
      list.add("{\"text\":\"       Soulstone\\n\\nRarity: Empowered\\n\\nHow to imprison a soul... A seemingly impossible task if it weren't for some materials the Nether-dwellers have been working with for millennia. Soulsand, Crying Obsidian and Netherite when put\"}");
      list.add("{\"text\":\"       Soulstone\\n\\ntogether seem to be capable of constructing a near inescapable bulk prison for souls of a single type.\\n\\nThe Soulstone should be able to attune to a type of mob by merely using it to draw blood. After that any souls\"}");
      list.add("{\"text\":\"       Soulstone\\n\\nof that type that are freed from their mortal existence should be sucked into the stone like a black hole, never to be released... \\n\\nUntil I find a way to use them...\"}");
      return list;
   }
   
   private MagicItemRecipe makeRecipe(){
      MagicItemIngredient b = new MagicItemIngredient(Items.NETHERITE_BLOCK,1,null, true);
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
   
   public class SoulstoneItem extends MagicPolymerItem {
      public SoulstoneItem(Settings settings){
         super(getThis(),settings);
      }
      
      @Override
      public int getPolymerCustomModelData(ItemStack itemStack, @Nullable ServerPlayerEntity player){
         if(getType(itemStack).equals("unattuned")) return ArcanaRegistry.MODELS.get(UNATTUNED_TXT).value();
         int tier = soulsToTier(getSouls(itemStack));
         if(tier == 0) return ArcanaRegistry.MODELS.get(T0_TXT).value();
         if(tier == 1) return ArcanaRegistry.MODELS.get(T1_TXT).value();
         if(tier == 2) return ArcanaRegistry.MODELS.get(T2_TXT).value();
         if(tier == 3) return ArcanaRegistry.MODELS.get(T3_TXT).value();
         if(tier == 4) return ArcanaRegistry.MODELS.get(T4_TXT).value();
         if(tier == 5) return ArcanaRegistry.MODELS.get(T5_TXT).value();
         if(tier == 6) return ArcanaRegistry.MODELS.get(T6_TXT).value();
         return ArcanaRegistry.MODELS.get(T7_TXT).value();
      }
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
      
      @Override
      public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker){
         if(!(attacker instanceof PlayerEntity playerEntity)) return false;
         NbtCompound itemNbt = stack.getNbt();
         NbtCompound magicNbt = itemNbt.getCompound("arcananovum");
         String type = magicNbt.getString("type");
         
         if(type.equals("unattuned") && target instanceof MobEntity attackedEntity && playerEntity instanceof ServerPlayerEntity player){
            String entityTypeId = EntityType.getId(attackedEntity.getType()).toString();
            String entityTypeName = EntityType.get(entityTypeId).get().getName().getString();
            
            magicNbt.putString("type",entityTypeId);
            buildItemLore(stack,player.getServer());
            player.sendMessage(Text.literal("The Soulstone attunes to the essence of "+entityTypeName).formatted(Formatting.DARK_RED,Formatting.ITALIC),true);
            SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_RESPAWN_ANCHOR_SET_SPAWN, 1,.5f);
         }
         
         return false;
      }
   }
}
