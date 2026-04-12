package net.borisshoes.arcananovum.items;

import net.borisshoes.arcananovum.ArcanaConfig;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.blocks.forge.StarlightForgeBlockEntity;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerItem;
import net.borisshoes.arcananovum.gui.arcanetome.ArcaneTomeGui;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.borislib.BorisLib;
import net.borisshoes.borislib.utils.SoundUtils;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Spawner;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class EssenceEgg extends ArcanaItem {
   public static final String ID = "essence_egg";
   
   public static final String USES_TAG = "uses";
   
   public EssenceEgg(){
      id = ID;
      name = "Essence Egg";
      rarity = ArcanaRarity.EMPOWERED;
      categories = new ArcaneTomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), ArcaneTomeGui.TomeFilter.ITEMS};
      vanillaItem = Items.GHAST_SPAWN_EGG;
      item = new EssenceEggItem();
      displayName = Component.translatableWithFallback("item." + MOD_ID + "." + ID, name).withStyle(ChatFormatting.BOLD, ChatFormatting.AQUA);
      researchTasks = new ResourceKey[]{ResearchTasks.UNLOCK_SOULSTONE, ResearchTasks.FIND_SPAWNER, ResearchTasks.OBTAIN_EGG};
   }
   
   @Override
   public ItemStack initializeArcanaTag(ItemStack stack){
      super.initializeArcanaTag(stack);
      putProperty(stack, Soulstone.TYPE_TAG, "unattuned");
      putProperty(stack, USES_TAG, 0);
      return stack;
   }
   
   @Override
   public List<Component> getItemLore(@Nullable ItemStack itemStack){
      List<MutableComponent> lore = new ArrayList<>();
      lore.add(Component.literal("")
            .append(Component.literal("Harness the power of a filled ").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal("Soulstone").withStyle(ChatFormatting.DARK_RED))
            .append(Component.literal("...").withStyle(ChatFormatting.LIGHT_PURPLE)));
      lore.add(Component.literal("")
            .append(Component.literal("With enough ").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal("souls").withStyle(ChatFormatting.DARK_RED))
            .append(Component.literal(" a new form can be ").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal("spawned").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal(".").withStyle(ChatFormatting.LIGHT_PURPLE)));
      lore.add(Component.literal("")
            .append(Component.literal("Spawns").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal(" a mob of the ").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal("attuned type").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal(".").withStyle(ChatFormatting.LIGHT_PURPLE)));
      lore.add(Component.literal(""));
      
      String attunedString = "Unattuned";
      int uses = 0;
      
      if(itemStack != null){
         String type = getType(itemStack);
         uses = getUses(itemStack);
         Optional<EntityType<?>> opt = EntityType.byString(type);
         if(!type.equals("unattuned") && opt.isPresent()){
            String entityTypeName = opt.get().getDescription().getString();
            attunedString = "Attuned - " + entityTypeName;
         }
      }
      
      lore.add(Component.literal(attunedString).withStyle(ChatFormatting.LIGHT_PURPLE));
      lore.add(Component.literal(TextUtils.readableInt(uses) + " Uses Left").withStyle(ChatFormatting.GRAY));
      return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   public ItemStack updateItem(ItemStack stack, MinecraftServer server){
      int uses = getIntProperty(stack, USES_TAG);
      String type = getStringProperty(stack, Soulstone.TYPE_TAG);
      ItemStack newStack = super.updateItem(stack, server);
      putProperty(newStack, USES_TAG, uses);
      putProperty(newStack, Soulstone.TYPE_TAG, type);
      return buildItemLore(newStack, server);
   }
   
   @Override
   public boolean blocksHandInteractions(ItemStack item){
      return true;
   }
   
   public static void setType(ItemStack stack, String entityId){
      putProperty(stack, Soulstone.TYPE_TAG, entityId);
      ArcanaRegistry.ESSENCE_EGG.buildItemLore(stack, BorisLib.SERVER);
   }
   
   public static String getType(ItemStack stack){
      return getStringProperty(stack, Soulstone.TYPE_TAG);
   }
   
   public static int getUses(ItemStack stack){
      return getIntProperty(stack, USES_TAG);
   }
   
   public static void setUses(ItemStack stack, int uses){
      uses = Mth.clamp(uses, 0, 1000000);
      
      if(uses <= 0){
         stack.shrink(stack.getCount());
      }else{
         putProperty(stack, USES_TAG, uses);
         ArcanaRegistry.ESSENCE_EGG.buildItemLore(stack, BorisLib.SERVER);
      }
   }
   
   public static void addUses(ItemStack stack, int uses){
      int curUses = getIntProperty(stack, USES_TAG);
      int newUses = Mth.clamp(uses + curUses, 0, 1000000);
      if(newUses <= 0){
         stack.shrink(stack.getCount());
      }else{
         putProperty(stack, USES_TAG, newUses);
         ArcanaRegistry.ESSENCE_EGG.buildItemLore(stack, BorisLib.SERVER);
      }
   }
   
   @Override
   public ItemStack forgeItem(Container inv, List<Integer> centerpieces, StarlightForgeBlockEntity starlightForge){
      ItemStack newArcanaItem = getNewItem();
      if(centerpieces.isEmpty()) return newArcanaItem;
      ItemStack soulstoneStack = inv.getItem(centerpieces.getFirst()); // Should be the Soulstone
      // Souls n stuff
      if(ArcanaItemUtils.identifyItem(soulstoneStack) instanceof Soulstone){
         int uses = (Soulstone.getSouls(soulstoneStack) / Soulstone.tiers[0]);
         String essenceType = Soulstone.getType(soulstoneStack);
         
         newArcanaItem = getNewItem();
         setType(newArcanaItem, essenceType);
         setUses(newArcanaItem, uses);
      }
      return newArcanaItem;
   }
   
   @Override
   public List<List<Component>> getBookLore(){
      List<List<Component>> list = new ArrayList<>();
      list.add(List.of(Component.literal("    Essence Egg").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD), Component.literal("\nRarity: ").withStyle(ChatFormatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(), false)), Component.literal("\nAs making Soulstones has taught me, keeping souls imprisoned is hard. Making them so they can be released controllably is even harder. Thankfully, I can build off of a Soulstone’s solid foundation.").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("    Essence Egg").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD), Component.literal("\nThis ‘Egg’ should take the souls from a Soulstone and keep them captive just long enough for me to release them into a new form. Although, it takes 25 souls to form an entirely new creature.\n\nThe Essence Egg can ").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("    Essence Egg").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD), Component.literal("\nbe used one at a time to spawn a mob, or 5 uses (125 souls) can be used on a spawner to change its attunement to the type of essence contained within the Egg.\n").withStyle(ChatFormatting.BLACK)));
      return list;
   }
   
   public class EssenceEggItem extends ArcanaPolymerItem {
      public EssenceEggItem(){
         super(getThis());
      }
      
      @Override
      public ItemStack getDefaultInstance(){
         return prefItem;
      }
      
      @Override
      public InteractionResult useOn(UseOnContext context){
         ItemStack stack = context.getItemInHand();
         try{
            Level world = context.getLevel();
            Player playerEntity = context.getPlayer();
            
            if(!getType(stack).equals("unattuned")){
               // Check for use on spawner
               BlockPos blockPos = context.getClickedPos();
               BlockEntity blockEntity;
               BlockState blockState = world.getBlockState(blockPos);
               if(blockState.is(Blocks.SPAWNER) && (blockEntity = world.getBlockEntity(blockPos)) instanceof Spawner spawner){
                  int captiveLevel = ArcanaAugments.getAugmentOnItem(stack, ArcanaAugments.WILLING_CAPTIVE);
                  int baseConvertCost = ArcanaNovum.CONFIG.getInt(ArcanaConfig.ESSENCE_EGG_SPAWNER_USES);
                  int decrease = ArcanaNovum.CONFIG.getIntList(ArcanaConfig.ESSENCE_EGG_WILLING_CAPTIVE_DECREASE).get(captiveLevel);
                  int cost = Math.max(0, baseConvertCost - decrease);
                  if(getUses(stack) >= cost){
                     EntityType<?> entityType = EntityType.byString(getType(stack)).get();
                     spawner.setEntityId(entityType, world.getRandom());
                     world.sendBlockUpdated(blockPos, blockState, blockState, Block.UPDATE_ALL);
                     blockEntity.setChanged();
                     
                     if(playerEntity instanceof ServerPlayer player){
                        player.sendSystemMessage(Component.literal("The Spawner Assumes the Essence of " + EntityType.byString(getType(stack)).get().getDescription().getString()).withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.ITALIC), true);
                        SoundUtils.playSongToPlayer(player, SoundEvents.ZOMBIE_VILLAGER_CURE, 1, .7f);
                        int xp = ArcanaNovum.CONFIG.getInt(ArcanaConfig.XP_ESSENCE_EGG_CONVERT);
                        ArcanaNovum.data(playerEntity).addXP(Math.min(0, xp * cost / Math.max(1, baseConvertCost))); // Add xp
                        ArcanaAchievements.grant(player, ArcanaAchievements.SOUL_CONVERSION);
                     }
                     addUses(stack, -cost);
                  }
               }else{
                  int splitLevel = ArcanaAugments.getAugmentOnItem(stack, ArcanaAugments.SOUL_SPLIT);
                  double splitChance = ArcanaNovum.CONFIG.getDoubleList(ArcanaConfig.ESSENCE_EGG_SOUL_SPLIT_CHANCE).get(splitLevel);
                  int efficiencyLevel = ArcanaAugments.getAugmentOnItem(stack, ArcanaAugments.DETERMINED_SPIRIT);
                  double efficiencyChance = ArcanaNovum.CONFIG.getDoubleList(ArcanaConfig.ESSENCE_EGG_EFFICIENCY_PER_LVL).get(efficiencyLevel);
                  if(getUses(stack) > 0){
                     ServerLevel serverWorld = world.getServer().getLevel(world.dimension());
                     Vec3 summonPos = context.getClickLocation().add(0, 0.5, 0);
                     
                     CompoundTag nbtCompound = new CompoundTag();
                     nbtCompound.putString("id", getType(stack));
                      int spawns = serverWorld.getRandom().nextDouble() >= splitChance ? 1 : 2;
                     
                     for(int i = 0; i < spawns; i++){
                        Entity newEntity = EntityType.loadEntityRecursive(nbtCompound, serverWorld, EntitySpawnReason.SPAWN_ITEM_USE, entity -> {
                           entity.snapTo(summonPos.x(), summonPos.y(), summonPos.z(), entity.getYRot(), entity.getXRot());
                           return entity;
                        });
                        if(newEntity instanceof Mob mobEntity){
                           mobEntity.finalizeSpawn(serverWorld, serverWorld.getCurrentDifficultyAt(newEntity.blockPosition()), EntitySpawnReason.SPAWN_ITEM_USE, null);
                        }
                        serverWorld.tryAddFreshEntityWithPassengers(newEntity);
                     }
                     
                     if(serverWorld.getRandom().nextDouble() >= efficiencyChance){
                        addUses(stack, -1);
                     }
                     if(playerEntity instanceof ServerPlayer player){
                        SoundUtils.playSongToPlayer(player, SoundEvents.FIRECHARGE_USE, 1, 1.5f);
                        ArcanaNovum.data(playerEntity).addXP(ArcanaNovum.CONFIG.getInt(ArcanaConfig.XP_ESSENCE_EGG_SPAWN)); // Add xp
                        ArcanaAchievements.progress(player, ArcanaAchievements.SOUL_FOR_SOUL, 1);
                     }
                  }
               }
            }
            
            return InteractionResult.SUCCESS_SERVER;
         }catch(Exception e){
            e.printStackTrace();
            return InteractionResult.PASS;
         }
      }
      
      @Override
      public InteractionResult use(Level world, Player playerEntity, InteractionHand hand){
         ItemStack item = playerEntity.getItemInHand(hand);
         if(playerEntity.isCreative()){
            if(!getType(item).equals("unattuned"))
               addUses(item, 1);
            return InteractionResult.SUCCESS_SERVER;
         }
         return InteractionResult.PASS;
      }
      
      @Override
      public void hurtEnemy(ItemStack stack, LivingEntity entity, LivingEntity attacker){
         if(!(attacker instanceof Player playerEntity)) return;
         
         if(playerEntity.isCreative()){
            String type = getType(stack);
            
            if(type.equals("unattuned") && entity instanceof Mob attackedEntity && playerEntity instanceof ServerPlayer player){
               if(entity.is(ArcanaRegistry.ESSENCE_EGG_DISALLOWED)){
                  player.sendSystemMessage(Component.literal("The Essence Egg cannot attune to this creature.").withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC), true);
               }else{
                  String entityTypeId = EntityType.getKey(attackedEntity.getType()).toString();
                  String entityTypeName = EntityType.byString(entityTypeId).get().getDescription().getString();
                  
                  setType(stack, entityTypeId);
                  player.sendSystemMessage(Component.literal("The Essence Egg attunes to the essence of " + entityTypeName).withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC), true);
                  SoundUtils.playSongToPlayer(player, SoundEvents.RESPAWN_ANCHOR_SET_SPAWN, 1, .5f);
               }
            }
         }
      }
   }
}

