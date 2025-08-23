package net.borisshoes.arcananovum.items;

import com.mojang.logging.LogUtils;
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
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.arcananovum.utils.ArcanaRarity;
import net.borisshoes.arcananovum.utils.SoundUtils;
import net.borisshoes.arcananovum.utils.TextUtils;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.entity.*;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.NbtReadView;
import net.minecraft.storage.NbtWriteView;
import net.minecraft.storage.ReadView;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ErrorReporter;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class ContainmentCirclet extends ArcanaItem {
	public static final String ID = "containment_circlet";
   
   public static final String CONTENTS_TAG = "contents";
   public static final String HP_TAG = "hp";
   public static final String MAX_HP_TAG = "maxHP";
   
   public ContainmentCirclet(){
      id = ID;
      name = "Containment Circlet";
      rarity = ArcanaRarity.EMPOWERED;
      categories = new TomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), TomeGui.TomeFilter.ITEMS};
      itemVersion = 0;
      vanillaItem = Items.HEART_OF_THE_SEA;
      item = new ContainmentCircletItem();
      displayName = Text.translatableWithFallback("item."+MOD_ID+"."+ID,name).formatted(Formatting.BOLD,Formatting.DARK_AQUA);
      researchTasks = new RegistryKey[]{ResearchTasks.ADVANCEMENT_TAME_AN_ANIMAL,ResearchTasks.USE_ENDER_CHEST};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getMaxCount());
      putProperty(stack,CONTENTS_TAG,new NbtCompound());
      putProperty(stack,HP_TAG,0.0f);
      putProperty(stack,MAX_HP_TAG,0.0f);
      setPrefStack(stack);
   }
   
   @Override
   public List<Text> getItemLore(@Nullable ItemStack itemStack){
      List<MutableText> lore = new ArrayList<>();
      lore.add(Text.literal("")
            .append(Text.literal("Animals ").formatted(Formatting.DARK_GREEN))
            .append(Text.literal("often have a ").formatted(Formatting.GREEN))
            .append(Text.literal("mind of their own").formatted(Formatting.AQUA))
            .append(Text.literal(" They must be ").formatted(Formatting.GREEN))
            .append(Text.literal("contained").formatted(Formatting.DARK_AQUA))
            .append(Text.literal(".").formatted(Formatting.GREEN)));
      lore.add(Text.literal("")
            .append(Text.literal("The Circlet").formatted(Formatting.DARK_AQUA))
            .append(Text.literal(" stores animals").formatted(Formatting.DARK_GREEN))
            .append(Text.literal(" for safe ").formatted(Formatting.GREEN))
            .append(Text.literal("keeping ").formatted(Formatting.AQUA))
            .append(Text.literal("and easy ").formatted(Formatting.GREEN))
            .append(Text.literal("transport").formatted(Formatting.AQUA))
            .append(Text.literal(".").formatted(Formatting.GREEN)));
      lore.add(Text.literal("")
            .append(Text.literal("Right Click").formatted(Formatting.DARK_AQUA))
            .append(Text.literal(" a ").formatted(Formatting.GREEN))
            .append(Text.literal("passive animal").formatted(Formatting.DARK_GREEN))
            .append(Text.literal(" to ").formatted(Formatting.GREEN))
            .append(Text.literal("contain ").formatted(Formatting.AQUA))
            .append(Text.literal("them.").formatted(Formatting.GREEN)));
      lore.add(Text.literal("")
            .append(Text.literal("Right Click").formatted(Formatting.DARK_AQUA))
            .append(Text.literal(" again to ").formatted(Formatting.GREEN))
            .append(Text.literal("release ").formatted(Formatting.AQUA))
            .append(Text.literal("them.").formatted(Formatting.GREEN)));
      lore.add(Text.literal(""));
      
      
      boolean hasCreature = false;
      if(itemStack != null){
         NbtCompound contents = getCompoundProperty(itemStack,CONTENTS_TAG);
         int hp = (int) getFloatProperty(itemStack,HP_TAG);
         int maxHp = (int) getFloatProperty(itemStack,MAX_HP_TAG);
         
         if(ArcanaNovum.SERVER != null){
            try (ErrorReporter.Logging logging = new ErrorReporter.Logging(LogUtils.getLogger())){
               ReadView readView = NbtReadView.create(logging, ArcanaNovum.SERVER.getRegistryManager(), contents);
               Optional<EntityType<?>> entity = EntityType.fromData(readView);
               if(!contents.isEmpty() && entity.isPresent()){
                  String entityTypeName = entity.get().getName().getString();
                  
                  lore.add(Text.literal("")
                        .append(Text.literal("Contains").formatted(Formatting.DARK_AQUA))
                        .append(Text.literal(" - ").formatted(Formatting.AQUA))
                        .append(Text.literal(entityTypeName+" ("+hp+"/"+maxHp+")").formatted(Formatting.GREEN)));
                  hasCreature = true;
               }
            }
         }
      }
      
      if(!hasCreature){
         lore.add(Text.literal("")
               .append(Text.literal("Contains").formatted(Formatting.DARK_AQUA))
               .append(Text.literal(" - ").formatted(Formatting.AQUA))
               .append(Text.literal("Nothing").formatted(Formatting.GREEN)));
      }
      
     return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   public ItemStack updateItem(ItemStack stack, MinecraftServer server){
      NbtCompound contents = getCompoundProperty(stack,CONTENTS_TAG);
      float hp = getFloatProperty(stack,HP_TAG);
      float maxHp = getFloatProperty(stack,MAX_HP_TAG);
      ItemStack newStack = super.updateItem(stack,server);
      putProperty(newStack,CONTENTS_TAG,contents);
      putProperty(newStack,HP_TAG,hp);
      putProperty(newStack,MAX_HP_TAG,maxHp);
      return buildItemLore(newStack,server);
   }
   
   // Normal override in item class doesn't work because tamed animals consume the item interaction
   public ActionResult useOnEntity(PlayerEntity user, LivingEntity entity, Hand hand){
      ItemStack stack = user.getStackInHand(hand);
      if(!ArcanaItemUtils.isArcane(stack)) return ActionResult.PASS;
      NbtCompound contents = getCompoundProperty(stack,CONTENTS_TAG);
      
      if(!contents.isEmpty()){
         user.sendMessage(Text.literal("The Circlet is occupied").formatted(Formatting.DARK_GREEN,Formatting.ITALIC),true);
         SoundUtils.playSongToPlayer((ServerPlayerEntity) user, SoundEvents.BLOCK_FIRE_EXTINGUISH, 1, .5f);
         return ActionResult.SUCCESS_SERVER;
      }
      if(entity.getType().isIn(ArcanaRegistry.CONTAINMENT_CIRCLET_DISALLOWED) || entity.isDead()){
         user.sendMessage(Text.literal("The Circlet cannot contain this creature").formatted(Formatting.DARK_GREEN,Formatting.ITALIC),true);
         SoundUtils.playSongToPlayer((ServerPlayerEntity) user, SoundEvents.BLOCK_FIRE_EXTINGUISH, 1, .5f);
         return ActionResult.SUCCESS_SERVER;
      }
      
      boolean hostiles = ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.CONFINEMENT.id) > 0;
      
      if(entity instanceof Monster && !hostiles){
         user.sendMessage(Text.literal("This Circlet cannot capture hostile creatures").formatted(Formatting.DARK_GREEN,Formatting.ITALIC),true);
         SoundUtils.playSongToPlayer((ServerPlayerEntity) user, SoundEvents.BLOCK_FIRE_EXTINGUISH, 1, .5f);
      }else if(entity instanceof MobEntity){
         try (ErrorReporter.Logging logging = new ErrorReporter.Logging(entity.getErrorReporterContext(),LogUtils.getLogger())){
            NbtWriteView writeView = NbtWriteView.create(logging, entity.getWorld().getRegistryManager());
            entity.writeData(writeView);
            NbtCompound data = writeView.getNbt();
            data.putString("id", EntityType.getId(entity.getType()).toString());
            putProperty(stack,CONTENTS_TAG,data);
            putProperty(stack,HP_TAG,entity.getHealth());
            putProperty(stack,MAX_HP_TAG,entity.getMaxHealth());
            entity.discard();
            user.sendMessage(Text.literal("The Circlet contains the creature").formatted(Formatting.DARK_GREEN,Formatting.ITALIC),true);
            SoundUtils.playSongToPlayer((ServerPlayerEntity) user, SoundEvents.ITEM_FIRECHARGE_USE, 1, 1.5f);
            ArcanaNovum.data(user).addXP(ArcanaConfig.getInt(ArcanaRegistry.CONTAINMENT_CIRCLET_USE)); // Add xp
            buildItemLore(stack,user.getServer());
         }
      }
      
      return ActionResult.SUCCESS_SERVER;
   }
   
   @Override
	protected ArcanaRecipe makeRecipe(){
      ArcanaIngredient a = new ArcanaIngredient(Items.CRYING_OBSIDIAN,4);
      ArcanaIngredient b = new ArcanaIngredient(Items.OBSIDIAN,4);
      ArcanaIngredient c = new ArcanaIngredient(Items.IRON_BARS,16);
      ArcanaIngredient g = new ArcanaIngredient(Items.CHAIN,12);
      ArcanaIngredient h = new ArcanaIngredient(Items.COBWEB,8);
      ArcanaIngredient m = new ArcanaIngredient(Items.ENDER_CHEST,4);
      
      ArcanaIngredient[][] ingredients = {
            {a,b,c,b,a},
            {b,g,h,g,b},
            {c,h,m,h,c},
            {b,g,h,g,b},
            {a,b,c,b,a}};
      return new ArcanaRecipe(ingredients,new ForgeRequirement());
   }
   
   @Override
   public List<List<Text>> getBookLore(){
      List<List<Text>> list = new ArrayList<>();
      list.add(List.of(Text.literal("Containment Circlet").formatted(Formatting.DARK_AQUA,Formatting.BOLD),Text.literal("\nRarity: ").formatted(Formatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(),false)),Text.literal("\nPets are amazing companions. They’re also masochistic idiots with a love for getting into trouble. If only I had some sort of pocket ball, a pokeb… a Containment Circlet to keep them safe.").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal("Containment Circlet").formatted(Formatting.DARK_AQUA,Formatting.BOLD),Text.literal("\nUsing the Circlet on a passive or tamed mob captures it.\n\nUsing the Circlet again releases the creature.\n").formatted(Formatting.BLACK)));
      return list;
   }
   
   public class ContainmentCircletItem extends ArcanaPolymerItem {
      public ContainmentCircletItem(){
         super(getThis());
      }
      
      @Override
      public ItemStack getPolymerItemStack(ItemStack itemStack, TooltipType tooltipType, PacketContext context){
         ItemStack baseStack = super.getPolymerItemStack(itemStack, tooltipType, context);
         if(!ArcanaItemUtils.isArcane(itemStack)) return baseStack;
         
         NbtCompound contents = getCompoundProperty(itemStack,CONTENTS_TAG);
         boolean confinement = ArcanaAugments.getAugmentOnItem(itemStack,ArcanaAugments.CONFINEMENT.id) >= 1;
         
         List<String> stringList = new ArrayList<>();
         if(confinement){
            if(contents.isEmpty()){
               stringList.add("confinement_empty");
            }else{
               stringList.add("confinement_filled");
            }
         }else{
            if(contents.isEmpty()){
               stringList.add("empty");
            }else{
               stringList.add("filled");
            }
         }
         baseStack.set(DataComponentTypes.CUSTOM_MODEL_DATA,new CustomModelDataComponent(new ArrayList<>(),new ArrayList<>(),stringList,new ArrayList<>()));
         return baseStack;
      }
      
      @Override
      public ActionResult useOnBlock(ItemUsageContext context){
         ItemStack stack = context.getStack();
         if(!ArcanaItemUtils.isArcane(stack) || context.getPlayer() == null) return ActionResult.PASS;
         
         NbtCompound contents = getCompoundProperty(stack,CONTENTS_TAG);
         float hp = getFloatProperty(stack,HP_TAG);
         if(contents.isEmpty()) return ActionResult.PASS;
         
         try (ErrorReporter.Logging logging = new ErrorReporter.Logging(context.getPlayer().getErrorReporterContext(),LogUtils.getLogger())) {
            ReadView readView = NbtReadView.create(logging, context.getWorld().getRegistryManager(),contents);
            Optional<Entity> optional = EntityType.getEntityFromData(readView,context.getWorld(), SpawnReason.MOB_SUMMONED);
            Vec3d summonPos = context.getHitPos().add(0,0.5,0);
            
            if(optional.isPresent() && context.getWorld() instanceof ServerWorld serverWorld){
               Entity newEntity = optional.get();
               newEntity.refreshPositionAndAngles(summonPos.getX(), summonPos.getY(), summonPos.getZ(), newEntity.getYaw(), newEntity.getPitch());
               
               if(newEntity instanceof LivingEntity living){
                  living.setHealth(hp);
               }
               
               serverWorld.spawnEntity(newEntity);
               putProperty(stack,CONTENTS_TAG,new NbtCompound());
               
               if(context.getPlayer() instanceof ServerPlayerEntity player){
                  player.sendMessage(Text.literal("The Circlet releases its captive").formatted(Formatting.DARK_GREEN,Formatting.ITALIC),true);
                  SoundUtils.playSongToPlayer(player, SoundEvents.ITEM_FIRECHARGE_USE, 1, 1.5f);
                  
                  if(newEntity instanceof TameableEntity tameable && tameable.isOwner(player)){
                     ArcanaAchievements.grant(player,ArcanaAchievements.I_CHOOSE_YOU.id);
                  }
               }
               buildItemLore(stack,serverWorld.getServer());
               return ActionResult.SUCCESS_SERVER;
            }
         }
         
         return ActionResult.PASS;
      }
      
      @Override
      public void inventoryTick(ItemStack stack, ServerWorld world, Entity entity, @Nullable EquipmentSlot slot){
         if(!ArcanaItemUtils.isArcane(stack)) return;
         if(!(world instanceof ServerWorld serverWorld && entity instanceof ServerPlayerEntity player)) return;
         
         float hp = getFloatProperty(stack,HP_TAG);
         float maxHp = getFloatProperty(stack,MAX_HP_TAG);
         boolean heals = ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.HEALING_CIRCLET.id) > 0;
         
         if(heals && player.getServer().getTicks() % 1200 == 0){
            putProperty(stack,HP_TAG,Math.min(maxHp,hp+1));
            buildItemLore(stack,serverWorld.getServer());
         }
      }
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
   }
}

