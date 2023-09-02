package net.borisshoes.arcananovum.items;

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
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;

public class ContainmentCirclet extends MagicItem {
   
   public ContainmentCirclet(){
      id = "containment_circlet";
      name = "Containment Circlet";
      rarity = MagicRarity.EMPOWERED;
      categories = new ArcaneTome.TomeFilter[]{ArcaneTome.TomeFilter.EMPOWERED, ArcaneTome.TomeFilter.ITEMS};
      itemVersion = 0;
      vanillaItem = Items.HEART_OF_THE_SEA;
      item = new ContainmentCircletItem(new FabricItemSettings().maxCount(1).fireproof());
      
      ItemStack stack = new ItemStack(item);
      NbtCompound tag = stack.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList loreList = new NbtList();
      NbtList enchants = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name","[{\"text\":\"Containment Circlet\",\"italic\":false,\"color\":\"dark_aqua\",\"bold\":true}]");
      loreList.add(NbtString.of("[{\"text\":\"Animals \",\"italic\":false,\"color\":\"dark_green\"},{\"text\":\"often have a \",\"color\":\"green\"},{\"text\":\"mind of their own\",\"color\":\"aqua\"},{\"text\":\"; They must be \",\"color\":\"green\"},{\"text\":\"contained\",\"color\":\"dark_aqua\"},{\"text\":\".\",\"color\":\"green\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"The Circlet\",\"italic\":false,\"color\":\"dark_aqua\"},{\"text\":\" \",\"color\":\"green\"},{\"text\":\"stores animals\",\"color\":\"dark_green\"},{\"text\":\" for safe \",\"color\":\"green\"},{\"text\":\"keeping \",\"color\":\"aqua\"},{\"text\":\"and easy \",\"color\":\"green\"},{\"text\":\"transport\",\"color\":\"aqua\"},{\"text\":\".\",\"color\":\"green\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Right Click\",\"italic\":false,\"color\":\"dark_aqua\"},{\"text\":\" a \",\"color\":\"green\"},{\"text\":\"passive animal\",\"color\":\"dark_green\"},{\"text\":\" to \",\"color\":\"green\"},{\"text\":\"contain \",\"color\":\"aqua\"},{\"text\":\"them.\",\"color\":\"green\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Right Click\",\"italic\":false,\"color\":\"dark_aqua\"},{\"text\":\" again to \",\"color\":\"green\"},{\"text\":\"release \",\"color\":\"aqua\"},{\"text\":\"them.\",\"color\":\"green\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Contains\",\"italic\":false,\"color\":\"dark_aqua\"},{\"text\":\" - \",\"color\":\"aqua\"},{\"text\":\"Nothing\",\"color\":\"green\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Empowered \",\"italic\":false,\"color\":\"green\",\"bold\":true},{\"text\":\"Magic Item\",\"color\":\"dark_purple\",\"bold\":false}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
      tag.put("Enchantments",enchants);
      
      setBookLore(makeLore());
      setRecipe(makeRecipe());
      tag = addMagicNbt(tag);
      NbtCompound magicTag = tag.getCompound("arcananovum");
      magicTag.put("contents",new NbtCompound());
      magicTag.putFloat("hp",-1);
      magicTag.putFloat("maxHp",-1);
      prefNBT = tag;
      stack.setNbt(prefNBT);
      prefItem = stack;
   }
   
   @Override
   public ItemStack updateItem(ItemStack stack, MinecraftServer server){
      NbtCompound itemNbt = stack.getNbt();
      NbtCompound magicTag = itemNbt.getCompound("arcananovum");
      NbtCompound contents = magicTag.getCompound("contents");
      float hp = magicTag.getFloat("hp");
      float maxHp = magicTag.getFloat("maxHp");
      NbtCompound newTag = super.updateItem(stack,server).getNbt();
      newTag.getCompound("arcananovum").put("contents",contents);
      newTag.getCompound("arcananovum").putFloat("hp",hp);
      newTag.getCompound("arcananovum").putFloat("maxHp",maxHp);
      stack.setNbt(newTag);
      redoLore(stack);
      return stack;
   }
   
   private void redoLore(ItemStack stack){
      NbtCompound itemNbt = stack.getNbt();
      NbtCompound magicNbt = itemNbt.getCompound("arcananovum");
      NbtCompound contents = magicNbt.getCompound("contents");
      int hp = (int) magicNbt.getFloat("hp");
      int maxHp = (int) magicNbt.getFloat("maxHp");
      NbtList loreList = itemNbt.getCompound("display").getList("Lore", NbtElement.STRING_TYPE);
      Optional<EntityType<?>> entity = EntityType.fromNbt(contents);
      if(!contents.isEmpty() && entity.isPresent()){
         String entityTypeName = entity.get().getName().getString();
         loreList.set(5,NbtString.of("[{\"text\":\"Contains\",\"italic\":false,\"color\":\"dark_aqua\"},{\"text\":\" - \",\"color\":\"aqua\"},{\"text\":\""+entityTypeName+" ("+hp+"/"+maxHp+")\",\"color\":\"green\"}]"));
      }else{
         loreList.set(5,NbtString.of("[{\"text\":\"Contains\",\"italic\":false,\"color\":\"dark_aqua\"},{\"text\":\" - \",\"color\":\"aqua\"},{\"text\":\"Nothing\",\"color\":\"green\"}]"));
      }
   }
   
   // Normal override in item class doesn't work because tamed animals consume the item interaction
   public ActionResult useOnEntity(PlayerEntity user, LivingEntity entity, Hand hand){
      ItemStack stack = user.getStackInHand(hand);
      if(!MagicItemUtils.isMagic(stack)) return ActionResult.PASS;
      NbtCompound itemNbt = stack.getNbt();
      NbtCompound magicNbt = itemNbt.getCompound("arcananovum");
      NbtCompound contents = magicNbt.getCompound("contents");
      
      if(!contents.isEmpty()){
         user.sendMessage(Text.literal("The Circlet is occupied").formatted(Formatting.DARK_GREEN,Formatting.ITALIC),true);
         SoundUtils.playSongToPlayer((ServerPlayerEntity) user, SoundEvents.BLOCK_FIRE_EXTINGUISH, 1, .5f);
         return ActionResult.SUCCESS;
      }
      
      boolean hostiles = ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.CONFINEMENT.id) > 0;
      
      if((entity instanceof HostileEntity && hostiles) || entity instanceof PassiveEntity){
         NbtCompound data = entity.writeNbt(new NbtCompound());
         data.putString("id", EntityType.getId(entity.getType()).toString());
         magicNbt.put("contents",data);
         magicNbt.putFloat("hp",entity.getHealth());
         magicNbt.putFloat("maxHp",entity.getMaxHealth());
         entity.discard();
         user.sendMessage(Text.literal("The Circlet contains the creature").formatted(Formatting.DARK_GREEN,Formatting.ITALIC),true);
         SoundUtils.playSongToPlayer((ServerPlayerEntity) user, SoundEvents.ITEM_FIRECHARGE_USE, 1, 1.5f);
         PLAYER_DATA.get(user).addXP(5); // Add xp
         redoLore(stack);
      }else if(entity instanceof HostileEntity){
         user.sendMessage(Text.literal("This Circlet cannot capture hostile creatures").formatted(Formatting.DARK_GREEN,Formatting.ITALIC),true);
         SoundUtils.playSongToPlayer((ServerPlayerEntity) user, SoundEvents.BLOCK_FIRE_EXTINGUISH, 1, .5f);
      }
      
      return ActionResult.SUCCESS;
   }
   
   private MagicItemRecipe makeRecipe(){
      MagicItemIngredient a = new MagicItemIngredient(Items.OBSIDIAN,64,null);
      MagicItemIngredient b = new MagicItemIngredient(Items.IRON_BARS,64,null);
      MagicItemIngredient c = new MagicItemIngredient(Items.COBWEB,16,null);
      ItemStack potion7 = new ItemStack(Items.POTION);
      MagicItemIngredient h = new MagicItemIngredient(Items.POTION,1, PotionUtil.setPotion(potion7, Potions.STRONG_HEALING).getNbt());
      MagicItemIngredient m = new MagicItemIngredient(Items.ENDER_CHEST,16,null);
      
      MagicItemIngredient[][] ingredients = {
            {a,b,c,b,a},
            {b,c,h,c,b},
            {c,h,m,h,c},
            {b,c,h,c,b},
            {a,b,c,b,a}};
      return new MagicItemRecipe(ingredients);
   }
   
   private List<String> makeLore(){
      ArrayList<String> list = new ArrayList<>();
      list.add("{\"text\":\"  Containment Circlet\\n\\nRarity: Empowered\\n\\nPets are amazing companions. They're also idiots who love dying and are a pain to move.\\nIf only I had some sort of pocket ball, a pokeb... a Containment Circlet to keep them safe with me.\"}");
      list.add("{\"text\":\"  Containment Circlet\\n\\nUsing the Circlet of a passive or tamed mob captures it.\\n\\nUsing the Circlet again releases the creature.\"}");
      return list;
   }
   
   public class ContainmentCircletItem extends MagicPolymerItem {
      public ContainmentCircletItem(Settings settings){
         super(getThis(),settings);
      }
      
      @Override
      public ActionResult useOnBlock(ItemUsageContext context){
         ItemStack stack = context.getStack();
         if(!MagicItemUtils.isMagic(stack)) return ActionResult.PASS;
         
         NbtCompound itemNbt = stack.getNbt();
         NbtCompound magicNbt = itemNbt.getCompound("arcananovum");
         NbtCompound contents = magicNbt.getCompound("contents");
         float hp = magicNbt.getFloat("hp");
         if(contents.isEmpty()) return ActionResult.PASS;
         
         Optional<Entity> optional = EntityType.getEntityFromNbt(contents,context.getWorld());
         Vec3d summonPos = context.getHitPos().add(0,0.5,0);
         
         if(optional.isPresent() && context.getWorld() instanceof ServerWorld serverWorld){
            Entity newEntity = optional.get();
            newEntity.refreshPositionAndAngles(summonPos.getX(), summonPos.getY(), summonPos.getZ(), newEntity.getYaw(), newEntity.getPitch());
            
            if(newEntity instanceof MobEntity mobEntity){
               mobEntity.initialize(serverWorld, serverWorld.getLocalDifficulty(newEntity.getBlockPos()), SpawnReason.MOB_SUMMONED, null, null);
            }
            if(newEntity instanceof LivingEntity living){
               living.setHealth(hp);
            }
            
            serverWorld.spawnEntity(newEntity);
            magicNbt.put("contents",new NbtCompound());
            
            if(context.getPlayer() instanceof ServerPlayerEntity player){
               player.sendMessage(Text.literal("The Circlet releases its captive").formatted(Formatting.DARK_GREEN,Formatting.ITALIC),true);
               SoundUtils.playSongToPlayer(player, SoundEvents.ITEM_FIRECHARGE_USE, 1, 1.5f);
               PLAYER_DATA.get(player).addXP(10); // Add xp
               
               if(newEntity instanceof TameableEntity tameable && tameable.isOwner(player)){
                  ArcanaAchievements.grant(player,ArcanaAchievements.I_CHOOSE_YOU.id);
               }
            }
            redoLore(stack);
            return ActionResult.SUCCESS;
         }
         
         return ActionResult.PASS;
      }
      
      @Override
      public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected){
         if(!MagicItemUtils.isMagic(stack)) return;
         if(!(world instanceof ServerWorld serverWorld && entity instanceof ServerPlayerEntity player)) return;
         NbtCompound itemNbt = stack.getNbt();
         NbtCompound magicNbt = itemNbt.getCompound("arcananovum");
         
         float hp = magicNbt.getFloat("hp");
         float maxHp = magicNbt.getFloat("maxHp");
         boolean heals = ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.HEALING_CIRCLET.id) > 0;
         
         if(heals && player.getServer().getTicks() % 1200 == 0){
            magicNbt.putFloat("hp",Math.min(maxHp,hp+1));
            redoLore(stack);
         }
      }
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
   }
}