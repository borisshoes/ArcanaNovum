package net.borisshoes.arcananovum.items.charms;

import net.borisshoes.arcananovum.items.ArcaneTome;
import net.borisshoes.arcananovum.items.core.MagicItem;
import net.borisshoes.arcananovum.items.core.TickingItem;
import net.borisshoes.arcananovum.items.core.UsableItem;
import net.borisshoes.arcananovum.recipes.MagicItemIngredient;
import net.borisshoes.arcananovum.recipes.MagicItemRecipe;
import net.borisshoes.arcananovum.utils.MagicRarity;
import net.borisshoes.arcananovum.utils.SoundUtils;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;


public class MagnetismCharm extends MagicItem implements TickingItem, UsableItem {
   
   private final int activeLength = 15;
   private final int activeRange = 3;
   private final int passiveRange = 5;
   
   public MagnetismCharm(){
      id = "magnetism_charm";
      name = "Charm of Magnetism";
      rarity = MagicRarity.EMPOWERED;
      categories = new ArcaneTome.TomeFilter[]{ArcaneTome.TomeFilter.EMPOWERED, ArcaneTome.TomeFilter.CHARMS, ArcaneTome.TomeFilter.ITEMS};
      itemVersion = 2;
   
      ItemStack item = new ItemStack(Items.IRON_INGOT);
      NbtCompound tag = item.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList loreList = new NbtList();
      NbtList enchants = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name","[{\"text\":\"Charm of Magnetism\",\"italic\":false,\"bold\":true,\"color\":\"gray\"}]");
      loreList.add(NbtString.of("[{\"text\":\"You can feel the \",\"italic\":false,\"color\":\"dark_gray\"},{\"text\":\"charm\",\"color\":\"gray\"},{\"text\":\" \"},{\"text\":\"tugging \",\"color\":\"dark_green\"},{\"text\":\"on surrounding objects.\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Right Click\",\"italic\":false,\"color\":\"dark_aqua\"},{\"text\":\" to \",\"color\":\"dark_gray\"},{\"text\":\"drag \",\"color\":\"gray\"},{\"text\":\"nearby items to you.\",\"color\":\"dark_gray\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Sneak Right Click\",\"italic\":false,\"color\":\"dark_aqua\"},{\"text\":\" to toggle the \",\"color\":\"dark_gray\"},{\"text\":\"magnetism \",\"color\":\"gray\"},{\"text\":\"passively\",\"color\":\"dark_green\"},{\"text\":\".\",\"color\":\"dark_gray\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Sneak\",\"italic\":false,\"color\":\"dark_aqua\"},{\"text\":\" to temporarily disable the \",\"color\":\"dark_gray\"},{\"text\":\"passive\",\"italic\":false,\"color\":\"dark_green\"},{\"text\":\" pull\",\"color\":\"gray\"},{\"text\":\".\",\"color\":\"dark_gray\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Empowered \",\"italic\":false,\"color\":\"green\",\"bold\":true},{\"text\":\"Magic Item\",\"italic\":false,\"color\":\"dark_purple\",\"bold\":false}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
      tag.put("Enchantments",enchants);
   
      setBookLore(makeLore());
      setRecipe(makeRecipe());
      tag = this.addMagicNbt(tag);
      tag.getCompound("arcananovum").putBoolean("active",false);
      prefNBT = tag;
      
      item.setNbt(prefNBT);
      prefItem = item;
   
   }
   
   @Override
   public void onTick(ServerWorld world, ServerPlayerEntity player, ItemStack charm){
      //log("Tick Check"+charm.getNbt().getCompound("arcananovum").getBoolean("active"));
      if(!player.isSneaking()){
         boolean active = charm.getNbt().getCompound("arcananovum").getBoolean("active");
   
         if(active && world.getServer().getTicks() % 6 == 0){
            Vec3d playerPos = player.getEyePos();
      
            Box box = new Box(playerPos,playerPos).expand(passiveRange);
            List<ItemEntity> items = world.getEntitiesByType(EntityType.ITEM, box, (entity) -> entity.getType() == EntityType.ITEM);
      
            for(ItemEntity item : items){
               double x = playerPos.getX() - item.getX();
               double y = playerPos.getY() - item.getY();
               double z = playerPos.getZ() - item.getZ();
               double speed = .06;
               double heightMod = .04;
               item.setVelocity(x * speed, y * speed + Math.sqrt(Math.sqrt(x * x + y * y + z * z)) * heightMod, z * speed);
            }
         }
      }
   }
   
   @Override
   public boolean useItem(PlayerEntity playerEntity, World world, Hand hand){
      if(playerEntity.isSneaking()){
         toggleActive((ServerPlayerEntity) playerEntity,playerEntity.getStackInHand(hand));
      }else{
         activeUse((ServerPlayerEntity) playerEntity, world, playerEntity.getStackInHand(hand));
      }
      return false;
   }
   
   @Override
   public boolean useItem(PlayerEntity playerEntity, World world, Hand hand, BlockHitResult result){
      return false;
   }
   
   public void activeUse(ServerPlayerEntity player, World world, ItemStack charm){
      Vec3d playerPos = player.getEyePos();
      Vec3d view = player.getRotationVecClient();
      Vec3d rayEnd = playerPos.add(view.multiply(activeLength));
      
      Box box = new Box(playerPos,playerPos).expand(activeLength+activeRange);
      List<ItemEntity> items = world.getEntitiesByType(EntityType.ITEM, box, (entity)->itemInRange(entity.getPos(),playerPos,rayEnd));
      SoundUtils.playSongToPlayer(player, SoundEvents.ENTITY_FOX_TELEPORT, 1,.9f);
   
      for(ItemEntity item : items){
         double x = playerPos.getX() - item.getX();
         double y = playerPos.getY() - item.getY();
         double z = playerPos.getZ() - item.getZ();
         double speed = .1;
         double heightMod = .08;
         item.setVelocity(x * speed, y * speed + Math.sqrt(Math.sqrt(x * x + y * y + z * z)) * heightMod, z * speed);
         PLAYER_DATA.get(player).addXP(5); // Add xp
      }
   }
   
   private boolean itemInRange(Vec3d itemPos, Vec3d start, Vec3d end){
      double dist = itemPos.subtract(start).crossProduct(end.subtract(start)).length() / end.subtract(start).length();
      return dist <= activeRange;
   }
   
   public void toggleActive(ServerPlayerEntity player, ItemStack item){
      NbtCompound itemNbt = item.getNbt();
      NbtCompound magicNbt = itemNbt.getCompound("arcananovum");
      boolean active = !magicNbt.getBoolean("active");
      magicNbt.putBoolean("active",active);
      itemNbt.put("arcananovum",magicNbt);
      item.setNbt(itemNbt);
      if(active){
         player.sendMessage(Text.translatable("The Charm's Pull Strengthens").formatted(Formatting.GRAY,Formatting.ITALIC),true);
         SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_ANVIL_LAND, 1,2f);
      }else{
         player.sendMessage(Text.translatable("The Charm's Pull Weakens").formatted(Formatting.GRAY,Formatting.ITALIC),true);
         SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_ANVIL_LAND, .3f,.5f);
      }
   }
   
   private MagicItemRecipe makeRecipe(){
      MagicItemIngredient r = new MagicItemIngredient(Items.LIGHTNING_ROD,64,null);
      MagicItemIngredient b = new MagicItemIngredient(Items.IRON_BARS,64,null);
      MagicItemIngredient i = new MagicItemIngredient(Items.IRON_INGOT,64,null);
      MagicItemIngredient c = new MagicItemIngredient(Items.COMPASS,64,null);
      MagicItemIngredient l = new MagicItemIngredient(Items.IRON_BLOCK,64,null);
      MagicItemIngredient a = new MagicItemIngredient(Items.ANVIL,64,null);
 
      MagicItemIngredient[][] ingredients = {
            {r,b,i,b,r},
            {b,c,l,c,b},
            {i,l,a,l,i},
            {b,c,l,c,b},
            {r,b,i,b,r}};
      return new MagicItemRecipe(ingredients);
   }
   
   private List<String> makeLore(){
      ArrayList<String> list = new ArrayList<>();
      list.add("{\"text\":\"  Charm of Magnetism\\n\\nRarity: Empowered\\n\\nMagnets, how do they work? Well, they pull stuff sometimes... \\nI think I can make one by condensing all the iron I can find and striking it with lightning to charge it, which will leave me with a permanent magnet.\"}");
      list.add("{\"text\":\"  Charm of Magnetism\\n\\nThe Charm can be toggled to passively pull in items around you.\\n\\nRight Clicking the charm pulls items from the direction you are looking towards you.\"}");
      return list;
   }
}
