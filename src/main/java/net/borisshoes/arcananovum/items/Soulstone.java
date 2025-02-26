package net.borisshoes.arcananovum.items;

import net.borisshoes.arcananovum.ArcanaConfig;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerItem;
import net.borisshoes.arcananovum.gui.arcanetome.TomeGui;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaIngredient;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaRecipe;
import net.borisshoes.arcananovum.recipes.arcana.ForgeRequirement;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.*;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class Soulstone extends ArcanaItem {
	public static final String ID = "soulstone";
   public static final String TYPE_TAG = "type";
   public static final String SOULS_TAG = "souls";
   public static final String MAX_TIER_TAG = "maxTier";
   public static final String SOULS_FROM_SPEAR_TAG = "soulsFromSpear";
   
   public static int[] tiers = {25,100,250,500,1000,5000,10000};
   
   public Soulstone(){
      id = ID;
      name = "Soulstone";
      rarity = ArcanaRarity.EMPOWERED;
      categories = new TomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), TomeGui.TomeFilter.ITEMS};
      itemVersion = 1;
      vanillaItem = Items.FIRE_CHARGE;
      item = new SoulstoneItem(addArcanaItemComponents(new Item.Settings().maxCount(1)));
      displayName = Text.translatableWithFallback("item."+MOD_ID+"."+ID,name).formatted(Formatting.BOLD,Formatting.DARK_RED);
      researchTasks = new RegistryKey[]{ResearchTasks.ADVANCEMENT_KILL_A_MOB,ResearchTasks.OBTAIN_NETHERITE_INGOT,ResearchTasks.USE_SOUL_SPEED,ResearchTasks.UNLOCK_STELLAR_CORE};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getMaxCount());
      putProperty(stack,TYPE_TAG,"unattuned");
      putProperty(stack,SOULS_TAG,0);
      putProperty(stack,MAX_TIER_TAG,0);
      putProperty(stack,SOULS_FROM_SPEAR_TAG,0);
      setPrefStack(stack);
   }
   
   @Override
   public List<Text> getItemLore(@Nullable ItemStack itemStack){
      List<MutableText> lore = new ArrayList<>();
      lore.add(Text.literal("")
            .append(Text.literal("The dark stone ").formatted(Formatting.DARK_GRAY))
            .append(Text.literal("crackles").formatted(Formatting.RED))
            .append(Text.literal(" with ").formatted(Formatting.DARK_GRAY))
            .append(Text.literal("red energy").formatted(Formatting.DARK_RED))
            .append(Text.literal(".").formatted(Formatting.DARK_GRAY)));
      lore.add(Text.literal("")
            .append(Text.literal("The ").formatted(Formatting.DARK_GRAY))
            .append(Text.literal("souls").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal(" of mobs ").formatted(Formatting.DARK_GRAY))
            .append(Text.literal("killed").formatted(Formatting.RED))
            .append(Text.literal(" seems to get ").formatted(Formatting.DARK_GRAY))
            .append(Text.literal("trapped").formatted(Formatting.BLUE))
            .append(Text.literal(" inside...").formatted(Formatting.DARK_GRAY)));
      lore.add(Text.literal(""));
      
      
      String attunedString = "Unattuned";
      int souls = 0;
      int tier = 0;
      
      if(itemStack != null){
         String type = getType(itemStack);
         souls = getSouls(itemStack);
         tier = soulsToTier(souls);
         Optional<EntityType<?>> opt = EntityType.get(type);
         if(!type.equals("unattuned") && opt.isPresent()){
            String entityTypeName = opt.get().getName().getString();
            attunedString = "Attuned - "+entityTypeName;
         }
      }
      
      lore.add(Text.literal(attunedString).formatted(Formatting.LIGHT_PURPLE));
      lore.add(Text.literal("Tier "+tier+" - ("+ LevelUtils.readableInt(souls)+" Mobs Killed)").formatted(Formatting.GRAY));
     return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   public ItemStack updateItem(ItemStack stack, MinecraftServer server){
      int souls = getIntProperty(stack,SOULS_TAG);
      int maxTier = getIntProperty(stack,MAX_TIER_TAG);
      int soulsFromSpear = getIntProperty(stack,SOULS_FROM_SPEAR_TAG);
      String type = getStringProperty(stack,TYPE_TAG);
      ItemStack newStack = super.updateItem(stack,server);
      putProperty(newStack,TYPE_TAG,type);
      putProperty(newStack,SOULS_TAG,souls);
      putProperty(newStack,MAX_TIER_TAG,maxTier);
      putProperty(newStack,SOULS_FROM_SPEAR_TAG,soulsFromSpear);
      return buildItemLore(newStack,server);
   }
   
   public void killedEntity(ServerWorld world, ServerPlayerEntity player, LivingEntity dead, ItemStack stone, ItemStack weapon){
      int souls = getIntProperty(stone,SOULS_TAG);
      int maxTier = getIntProperty(stone,MAX_TIER_TAG);
      int soulsFromSpear = getIntProperty(stone,SOULS_FROM_SPEAR_TAG);
      if(weapon == null) weapon = ItemStack.EMPTY;
      
      String entityTypeId = EntityType.getId(dead.getType()).toString();
      
      int toAdd = new int[]{1,2,3,4,5,10}[Math.max(0,ArcanaAugments.getAugmentOnItem(stone,ArcanaAugments.SOUL_REAPER.id))];
      if(weapon.isOf(ArcanaRegistry.SPEAR_OF_TENBROUS.getItem())){
         if(ArcanaAugments.getAugmentOnItem(weapon,ArcanaAugments.ETERNAL_CRUELTY) > 0){
            toAdd *= 2;
         }
         soulsFromSpear += toAdd;
      }
      souls += toAdd;
      
      int tier = soulsToTier(souls);
      if(tier != soulsToTier(souls-toAdd)){
         // Level up notification
         player.sendMessage(Text.literal("Your Soulstone crackles with new power!").formatted(Formatting.DARK_RED,Formatting.ITALIC),true);
         SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_RESPAWN_ANCHOR_CHARGE, 1,1f);
         if(tier > maxTier){
            ArcanaNovum.data(player).addXP(ArcanaConfig.getInt(ArcanaRegistry.SOULSTONE_LEVEL_UP_PER_SOUL)*souls); // Add xp
            putProperty(stone,MAX_TIER_TAG,maxTier);
         }
         if(tier == 7){
            ArcanaAchievements.grant(player, ArcanaAchievements.PRIME_EVIL);
            if(soulsToTier(soulsFromSpear) == 7){
               ArcanaAchievements.grant(player, ArcanaAchievements.HISTORY_CARVED_IN_STONE);
            }
         }
         if(tier == 5) ArcanaAchievements.grant(player,ArcanaAchievements.PHILOSOPHER_STONE);
         if(tier == 3 && entityTypeId.equals(EntityType.getId(EntityType.VILLAGER).toString())) ArcanaAchievements.grant(player,ArcanaAchievements.TOOK_A_VILLAGE);
      }
      putProperty(stone,SOULS_FROM_SPEAR_TAG,soulsFromSpear);
      putProperty(stone,SOULS_TAG,souls);
      buildItemLore(stone,player.getServer());
   }
   
   public static String getType(ItemStack stack){
      if(!(ArcanaItemUtils.identifyItem(stack) instanceof Soulstone)){
         return "unattuned";
      }
      String type = getStringProperty(stack,TYPE_TAG);
      return type == null || type.isBlank() ? "unattuned" : type;
   }
   
   public static ItemStack setType(ItemStack stack, EntityType<?> type){
      if(!(ArcanaItemUtils.identifyItem(stack) instanceof Soulstone)){
         return null;
      }
      ItemStack item = stack.copy();
      String entityTypeId = EntityType.getId(type).toString();
      putProperty(item,TYPE_TAG,entityTypeId);
      return ArcanaRegistry.SOULSTONE.buildItemLore(item,ArcanaNovum.SERVER);
   }
   
   public static ItemStack setUnattuned(ItemStack stack){
      if(!(ArcanaItemUtils.identifyItem(stack) instanceof Soulstone)){
         return null;
      }
      ItemStack item = setSouls(stack,0);
      
      putProperty(item,TYPE_TAG,"unattuned");
      return ArcanaRegistry.SOULSTONE.buildItemLore(item,ArcanaNovum.SERVER);
   }
   
   public static int getSouls(ItemStack item){
      if(!(ArcanaItemUtils.identifyItem(item) instanceof Soulstone)){
         return -1;
      }
      return getIntProperty(item,SOULS_TAG);
   }
   
   public static ItemStack setSouls(ItemStack stack, int newSouls){
      if(!(ArcanaItemUtils.identifyItem(stack) instanceof Soulstone)){
         return null;
      }
      ItemStack item = stack.copy();
      
      newSouls = MathHelper.clamp(newSouls,0,Integer.MAX_VALUE);
      putProperty(item,SOULS_TAG,newSouls);
      putProperty(item,SOULS_FROM_SPEAR_TAG,Math.min(getIntProperty(item,SOULS_FROM_SPEAR_TAG),newSouls));
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
   
   @Override
   public List<List<Text>> getBookLore(){
      List<List<Text>> list = new ArrayList<>();
      list.add(List.of(Text.literal("     Soulstone").formatted(Formatting.DARK_RED,Formatting.BOLD),Text.literal("\nRarity: ").formatted(Formatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(),false)),Text.literal("\nI have found that souls contain a unique flavor of Arcana, a quite powerful one at that. If I am to be surrounded by death, I might as well use it. Now, how to imprison a soul…\nA seemingly impossible  ").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal("     Soulstone").formatted(Formatting.DARK_RED,Formatting.BOLD),Text.literal("\ntask if it weren’t for some materials the Nether-dwellers have been working with for eons. Soulsand seems to have souls naturally imbued in it, perhaps from ancient battles?\nCombining this with Crying Obsidian and Netherite yields a \n").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal("     Soulstone").formatted(Formatting.DARK_RED,Formatting.BOLD),Text.literal("\nStone that can act as an inescapable bulk prison for souls of a single variety. I know not why soul types refuse to combine.\n\nThe Soulstone should be able to attune to a type of mob by merely using it to draw blood.").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal("     Soulstone").formatted(Formatting.DARK_RED,Formatting.BOLD),Text.literal("\nAfter that, any souls of that type that are freed from their mortal prisons should be sucked into the Stone like a black hole, never to be released…\n\nUntil I find a way to use them…\n").formatted(Formatting.BLACK)));
      return list;
   }
   
   @Override
	protected ArcanaRecipe makeRecipe(){
      ArcanaIngredient a = new ArcanaIngredient(Items.CRYING_OBSIDIAN,16);
      ArcanaIngredient b = new ArcanaIngredient(Items.OBSIDIAN,16);
      ArcanaIngredient c = new ArcanaIngredient(Items.RED_NETHER_BRICKS,16);
      ArcanaIngredient h = new ArcanaIngredient(Items.SOUL_SAND,32);
      ArcanaIngredient m = new ArcanaIngredient(Items.NETHERITE_BLOCK,1, true);
      
      ArcanaIngredient[][] ingredients = {
            {a,b,c,b,a},
            {b,c,h,c,b},
            {c,h,m,h,c},
            {b,c,h,c,b},
            {a,b,c,b,a}};
      return new ArcanaRecipe(ingredients,new ForgeRequirement().withCore().withAnvil());
   }
   
   public class SoulstoneItem extends ArcanaPolymerItem {
      public SoulstoneItem(Item.Settings settings){
         super(getThis(),settings);
      }
      
      @Override
      public ItemStack getPolymerItemStack(ItemStack itemStack, TooltipType tooltipType, PacketContext context){
         ItemStack baseStack = super.getPolymerItemStack(itemStack, tooltipType, context);
         if(!ArcanaItemUtils.isArcane(itemStack) || getType(itemStack).equals("unattuned")) return baseStack;
         
         List<String> stringList = new ArrayList<>();
         int tier = soulsToTier(getSouls(itemStack));
         stringList.add(""+tier);
         baseStack.set(DataComponentTypes.CUSTOM_MODEL_DATA,new CustomModelDataComponent(new ArrayList<>(),new ArrayList<>(),stringList,new ArrayList<>()));
         return baseStack;
      }
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
      
      @Override
      public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker){
         if(!(attacker instanceof PlayerEntity playerEntity)) return false;
         String type = getStringProperty(stack,TYPE_TAG);
         
         if(type.equals("unattuned") && target instanceof MobEntity attackedEntity && playerEntity instanceof ServerPlayerEntity player){
            if(attackedEntity.getType().isIn(ArcanaRegistry.SOULSTONE_DISALLOWED)){
               player.sendMessage(Text.literal("The Soulstone cannot attune to this creature.").formatted(Formatting.DARK_RED,Formatting.ITALIC),true);
            }else{
               String entityTypeId = EntityType.getId(attackedEntity.getType()).toString();
               String entityTypeName = EntityType.get(entityTypeId).get().getName().getString();
               
               putProperty(stack,TYPE_TAG,entityTypeId);
               buildItemLore(stack,player.getServer());
               player.sendMessage(Text.literal("The Soulstone attunes to the essence of "+entityTypeName).formatted(Formatting.DARK_RED,Formatting.ITALIC),true);
               SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_RESPAWN_ANCHOR_SET_SPAWN, 1,.5f);
            }
         }
         
         return false;
      }
   }
}

