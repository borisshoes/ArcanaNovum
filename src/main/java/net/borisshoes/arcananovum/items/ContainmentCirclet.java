package net.borisshoes.arcananovum.items;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerItem;
import net.borisshoes.arcananovum.entities.DragonPhantomEntity;
import net.borisshoes.arcananovum.entities.DragonWizardEntity;
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
import net.minecraft.component.type.LoreComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.mob.WardenEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;
import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;

public class ContainmentCirclet extends ArcanaItem {
	public static final String ID = "containment_circlet";
   
   private static final String CONTENTS_TAG = "contents";
   private static final String HP_TAG = "hp";
   private static final String MAX_HP_TAG = "maxHP";
   
   private static final String TXT_EMPTY = "item/containment_circlet_empty";
   private static final String TXT_FILLED = "item/containment_circlet_filled";
   private static final String TXT_CONFINEMENT_EMPTY = "item/containment_circlet_confinement_empty";
   private static final String TXT_CONFINEMENT_FILLED = "item/containment_circlet_confinement_filled";
   
   public ContainmentCirclet(){
      id = ID;
      name = "Containment Circlet";
      rarity = ArcanaRarity.EMPOWERED;
      categories = new TomeGui.TomeFilter[]{TomeGui.TomeFilter.EMPOWERED, TomeGui.TomeFilter.ITEMS};
      itemVersion = 0;
      vanillaItem = Items.HEART_OF_THE_SEA;
      item = new ContainmentCircletItem(new Item.Settings().maxCount(1).fireproof()
            .component(DataComponentTypes.ITEM_NAME, Text.translatable("item."+MOD_ID+"."+ID).formatted(Formatting.BOLD,Formatting.DARK_AQUA))
            .component(DataComponentTypes.LORE, new LoreComponent(getItemLore(null)))
            .component(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true)
      );
      models = new ArrayList<>();
      models.add(new Pair<>(vanillaItem,TXT_EMPTY));
      models.add(new Pair<>(vanillaItem,TXT_FILLED));
      models.add(new Pair<>(vanillaItem,TXT_CONFINEMENT_EMPTY));
      models.add(new Pair<>(vanillaItem,TXT_CONFINEMENT_FILLED));
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
         
         Optional<EntityType<?>> entity = EntityType.fromNbt(contents);
         if(!contents.isEmpty() && entity.isPresent()){
            String entityTypeName = entity.get().getName().getString();
            
            lore.add(Text.literal("")
                  .append(Text.literal("Contains").formatted(Formatting.DARK_AQUA))
                  .append(Text.literal(" - ").formatted(Formatting.AQUA))
                  .append(Text.literal(entityTypeName+" ("+hp+"/"+maxHp+")").formatted(Formatting.GREEN)));
            
            hasCreature = true;
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
         return ActionResult.SUCCESS;
      }
      if(entity instanceof EnderDragonEntity || entity instanceof WitherEntity || entity instanceof WardenEntity || entity instanceof DragonWizardEntity || entity instanceof DragonPhantomEntity || entity.isDead()){
         user.sendMessage(Text.literal("The Circlet cannot contain this creature").formatted(Formatting.DARK_GREEN,Formatting.ITALIC),true);
         SoundUtils.playSongToPlayer((ServerPlayerEntity) user, SoundEvents.BLOCK_FIRE_EXTINGUISH, 1, .5f);
         return ActionResult.SUCCESS;
      }
      
      boolean hostiles = ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.CONFINEMENT.id) > 0;
      
      if(entity instanceof Monster && !hostiles){
         user.sendMessage(Text.literal("This Circlet cannot capture hostile creatures").formatted(Formatting.DARK_GREEN,Formatting.ITALIC),true);
         SoundUtils.playSongToPlayer((ServerPlayerEntity) user, SoundEvents.BLOCK_FIRE_EXTINGUISH, 1, .5f);
      }else if(entity instanceof MobEntity){
         NbtCompound data = entity.writeNbt(new NbtCompound());
         data.putString("id", EntityType.getId(entity.getType()).toString());
         putProperty(stack,CONTENTS_TAG,data);
         putProperty(stack,HP_TAG,entity.getHealth());
         putProperty(stack,MAX_HP_TAG,entity.getMaxHealth());
         entity.discard();
         user.sendMessage(Text.literal("The Circlet contains the creature").formatted(Formatting.DARK_GREEN,Formatting.ITALIC),true);
         SoundUtils.playSongToPlayer((ServerPlayerEntity) user, SoundEvents.ITEM_FIRECHARGE_USE, 1, 1.5f);
         PLAYER_DATA.get(user).addXP(5); // Add xp
         buildItemLore(stack,user.getServer());
      }
      
      return ActionResult.SUCCESS;
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
      list.add(List.of(Text.literal("  Containment Circlet\n\nRarity: Empowered\n\nPets are amazing companions. They're also idiots who love dying and are a pain to move.\nIf only I had some sort of pocket ball, a pokeb... a Containment Circlet to keep them safe with me.").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal("  Containment Circlet\n\nUsing the Circlet of a passive or tamed mob captures it.\n\nUsing the Circlet again releases the creature.").formatted(Formatting.BLACK)));
      return list;
   }
   
   public class ContainmentCircletItem extends ArcanaPolymerItem {
      public ContainmentCircletItem(Item.Settings settings){
         super(getThis(),settings);
      }
      
      @Override
      public int getPolymerCustomModelData(ItemStack itemStack, @Nullable ServerPlayerEntity player){
         if(!ArcanaItemUtils.isArcane(itemStack)) return ArcanaRegistry.getModelData(TXT_EMPTY).value();
         NbtCompound contents = getCompoundProperty(itemStack,CONTENTS_TAG);
         boolean confinement = ArcanaAugments.getAugmentOnItem(itemStack,ArcanaAugments.CONFINEMENT.id) >= 1;
         if(confinement){
            return contents.isEmpty() ? ArcanaRegistry.getModelData(TXT_CONFINEMENT_EMPTY).value() : ArcanaRegistry.getModelData(TXT_CONFINEMENT_FILLED).value();
         }else{
            return contents.isEmpty() ? ArcanaRegistry.getModelData(TXT_EMPTY).value() : ArcanaRegistry.getModelData(TXT_FILLED).value();
         }
      }
      
      @Override
      public ActionResult useOnBlock(ItemUsageContext context){
         ItemStack stack = context.getStack();
         if(!ArcanaItemUtils.isArcane(stack)) return ActionResult.PASS;
         
         NbtCompound contents = getCompoundProperty(stack,CONTENTS_TAG);
         float hp = getFloatProperty(stack,HP_TAG);
         if(contents.isEmpty()) return ActionResult.PASS;
         
         Optional<Entity> optional = EntityType.getEntityFromNbt(contents,context.getWorld(),SpawnReason.TRIGGERED);
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
               PLAYER_DATA.get(player).addXP(10); // Add xp
               
               if(newEntity instanceof TameableEntity tameable && tameable.isOwner(player)){
                  ArcanaAchievements.grant(player,ArcanaAchievements.I_CHOOSE_YOU.id);
               }
            }
            buildItemLore(stack,serverWorld.getServer());
            return ActionResult.SUCCESS;
         }
         
         return ActionResult.PASS;
      }
      
      @Override
      public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected){
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

