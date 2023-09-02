package net.borisshoes.arcananovum.items;

import eu.pb4.polymer.core.api.item.PolymerItem;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.EnergyItem;
import net.borisshoes.arcananovum.core.MagicItem;
import net.borisshoes.arcananovum.core.polymer.MagicPolymerItem;
import net.borisshoes.arcananovum.recipes.arcana.MagicItemIngredient;
import net.borisshoes.arcananovum.recipes.arcana.MagicItemRecipe;
import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.borisshoes.arcananovum.utils.MagicRarity;
import net.borisshoes.arcananovum.utils.SoundUtils;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;

public class EverlastingRocket extends EnergyItem {
   
   public EverlastingRocket(){
      id = "everlasting_rocket";
      name = "Everlasting Rocket";
      rarity = MagicRarity.EMPOWERED;
      categories = new ArcaneTome.TomeFilter[]{ArcaneTome.TomeFilter.EMPOWERED, ArcaneTome.TomeFilter.ITEMS};
      itemVersion = 0;
      vanillaItem = Items.FIREWORK_ROCKET;
      item = new EverlastingRocketItem(new FabricItemSettings().maxCount(1).fireproof());
      initEnergy = 16;
      
      ItemStack stack = new ItemStack(item);
      NbtCompound tag = stack.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList loreList = new NbtList();
      NbtList enchants = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name","[{\"text\":\"Everlasting Rocket\",\"italic\":false,\"bold\":true,\"color\":\"yellow\"}]");
      loreList.add(NbtString.of("[{\"text\":\"A \",\"italic\":false,\"color\":\"dark_purple\"},{\"text\":\"Rocket\",\"color\":\"yellow\"},{\"text\":\" that has near \"},{\"text\":\"infinite \",\"color\":\"light_purple\"},{\"text\":\"uses.\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Can be used for \",\"italic\":false,\"color\":\"dark_purple\"},{\"text\":\"everything\",\"color\":\"light_purple\"},{\"text\":\" a \"},{\"text\":\"normal rocket\",\"color\":\"yellow\"},{\"text\":\" is used for.\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Stores \",\"italic\":false,\"color\":\"dark_purple\"},{\"text\":\"charges \",\"color\":\"yellow\"},{\"text\":\"that slowly \"},{\"text\":\"recharge \",\"color\":\"light_purple\"},{\"text\":\"over \"},{\"text\":\"time\",\"color\":\"blue\"},{\"text\":\".\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Charges \",\"italic\":false,\"color\":\"yellow\"},{\"text\":\"- \",\"color\":\"dark_purple\"},{\"text\":\"16 / 16\",\"color\":\"light_purple\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Empowered \",\"italic\":false,\"color\":\"green\",\"bold\":true},{\"text\":\"Magic Item\",\"color\":\"dark_purple\",\"bold\":false}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
      tag.put("Enchantments",enchants);
      
      setBookLore(makeLore());
      setRecipe(makeRecipe());
      prefNBT = addMagicNbt(tag);
      
      stack.setNbt(prefNBT);
      prefItem = stack;
   }
   
   @Override
   public ItemStack forgeItem(Inventory inv){
      ItemStack toolStack = inv.getStack(12); // Should be the rocket
      ItemStack newMagicItem = getNewItem();
      NbtCompound nbt = toolStack.getNbt();
      if(nbt == null) return newMagicItem;
      NbtCompound newNbt = newMagicItem.getOrCreateNbt();
      if(nbt.contains("Fireworks")){
         NbtCompound fireworks = nbt.getCompound("Fireworks");
         newNbt.getCompound("arcananovum").put("Fireworks",fireworks);
      }
      return newMagicItem;
   }
   
   @Override
   public ItemStack updateItem(ItemStack stack, MinecraftServer server){
      NbtCompound itemNbt = stack.getNbt();
      NbtCompound magicTag = itemNbt.getCompound("arcananovum");
      NbtCompound firework = magicTag.getCompound("Fireworks").copy();
      NbtCompound newTag = super.updateItem(stack,server).getNbt();
      newTag.getCompound("arcananovum").put("Fireworks",firework);
      stack.setNbt(newTag);
      redoLore(stack);
      return stack;
   }
   
   @Override
   public int getMaxEnergy(ItemStack item){
      return 16 + 3*Math.max(0,ArcanaAugments.getAugmentOnItem(item,ArcanaAugments.POWDER_PACKING.id));
   }
   
   public ItemStack getFireworkStack(ItemStack stack){
      ItemStack itemStack = new ItemStack(Items.FIREWORK_ROCKET, 3);
      NbtCompound magicTag = stack.getNbt().getCompound("arcananovum");
      if(magicTag == null) return ItemStack.EMPTY;
      NbtCompound nbtTag = itemStack.getOrCreateNbt();
      nbtTag.put("Fireworks",magicTag.getCompound("Fireworks"));
      nbtTag.putString("arcanaId",getUUID(stack));
      itemStack.setNbt(nbtTag);
      return itemStack;
   }
   
   public static void decreaseRocket(ItemStack stack, ServerPlayerEntity player){
      if(!(stack.hasNbt() && stack.getNbt().contains("arcanaId"))) return;
      String rocketId = stack.getNbt().getString("arcanaId");
      
      PlayerInventory inv = player.getInventory();
      for(int invSlot = 0; invSlot<inv.size(); invSlot++){
         ItemStack item = inv.getStack(invSlot);
         if(item.isEmpty()){
            continue;
         }
         
         MagicItem magicItem = MagicItemUtils.identifyItem(item);
         if(magicItem instanceof EverlastingRocket rocket && getUUID(item).equals(rocketId)){
            rocket.addEnergy(item,-1);
            rocket.redoLore(item);
            ArcanaAchievements.progress(player,ArcanaAchievements.MISSILE_LAUNCHER.id, 1);
            PLAYER_DATA.get(player).addXP(100); // Add xp
            return;
         }
      }
   }
   
   private void redoLore(ItemStack stack){
      NbtCompound itemNbt = stack.getNbt();
      NbtList loreList = itemNbt.getCompound("display").getList("Lore", NbtElement.STRING_TYPE);
      String chargeString = getEnergy(stack) + " / " + getMaxEnergy(stack);
      loreList.set(4,NbtString.of("[{\"text\":\"Charges \",\"italic\":false,\"color\":\"yellow\"},{\"text\":\"- \",\"color\":\"dark_purple\"},{\"text\":\""+chargeString+"\",\"color\":\"light_purple\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
   }
   
   private MagicItemRecipe makeRecipe(){
      MagicItemIngredient a = new MagicItemIngredient(Items.FIREWORK_ROCKET,64,null);
      MagicItemIngredient b = new MagicItemIngredient(Items.PAPER,64,null);
      MagicItemIngredient c = new MagicItemIngredient(Items.GUNPOWDER,64,null);
      ItemStack enchantedBook6 = new ItemStack(Items.ENCHANTED_BOOK);
      EnchantedBookItem.addEnchantment(enchantedBook6,new EnchantmentLevelEntry(Enchantments.MENDING,1));
      MagicItemIngredient g = new MagicItemIngredient(Items.ENCHANTED_BOOK,1,enchantedBook6.getNbt());
      MagicItemIngredient h = new MagicItemIngredient(Items.FIREWORK_STAR,64,null);
      ItemStack enchantedBook8 = new ItemStack(Items.ENCHANTED_BOOK);
      EnchantedBookItem.addEnchantment(enchantedBook8,new EnchantmentLevelEntry(Enchantments.UNBREAKING,3));
      MagicItemIngredient i = new MagicItemIngredient(Items.ENCHANTED_BOOK,1,enchantedBook8.getNbt());
      
      MagicItemIngredient[][] ingredients = {
            {a,b,c,b,a},
            {b,g,h,i,b},
            {c,h,a,h,c},
            {b,i,h,g,b},
            {a,b,c,b,a}};
      return new MagicItemRecipe(ingredients);
   }
   
   private List<String> makeLore(){
      ArrayList<String> list = new ArrayList<>();
      list.add("{\"text\":\"  Everlasting Rocket\\n\\nRarity: Empowered\\n\\nI have blown through so much gunpowder on rockets.\\nUsing a combination of Mending and Unbreaking enchantments I think I can extend one Rocket into hundreds.\"}");
      list.add("{\"text\":\"  Everlasting Rocket\\n\\nThe Everlasting Rocket is used the same way a normal rocket is used, however instead of being expended, it loses a charge.\\nCharges regenerate over time.\\nThe properties of the rocket come from the item used in crafting.\"}");
      return list;
   }
   
   
   public class EverlastingRocketItem extends MagicPolymerItem implements PolymerItem {
      public EverlastingRocketItem(Settings settings){
         super(getThis(),settings);
      }
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
      
      @Override
      public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected){
         if(!MagicItemUtils.isMagic(stack)) return;
         if(!(world instanceof ServerWorld serverWorld && entity instanceof ServerPlayerEntity player)) return;
         
         if(player.getServer().getTicks() % (600-(100*Math.max(0,ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.SULFUR_REPLICATION.id)))) == 0){
            addEnergy(stack,1);
            redoLore(stack);
            PLAYER_DATA.get(player).addXP(50); // Add xp
         }
      }
      
      @Override
      public ActionResult useOnBlock(ItemUsageContext context) {
         World world = context.getWorld();
         if (!world.isClient && context.getPlayer() instanceof ServerPlayerEntity player) {
            if(((EnergyItem)getThis()).getEnergy(context.getStack()) > 0){
               ItemStack itemStack = context.getStack();
               Vec3d vec3d = context.getHitPos();
               Direction direction = context.getSide();
               FireworkRocketEntity fireworkRocketEntity = new FireworkRocketEntity(world, context.getPlayer(), vec3d.x + (double)direction.getOffsetX() * 0.15, vec3d.y + (double)direction.getOffsetY() * 0.15, vec3d.z + (double)direction.getOffsetZ() * 0.15, getFireworkStack(itemStack));
               world.spawnEntity(fireworkRocketEntity);
               ((EnergyItem)getThis()).addEnergy(context.getStack(),-1);
               redoLore(itemStack);
               PLAYER_DATA.get(player).addXP(100); // Add xp
            }else{
               player.sendMessage(Text.literal("The Rocket is out of Charges").formatted(Formatting.YELLOW),true);
               SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_FIRE_EXTINGUISH, 1,0.8f);
            }
         }
         return ActionResult.success(world.isClient);
      }
      
      @Override
      public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
         ItemStack itemStack = user.getStackInHand(hand);
         if(user.isSneaking() && ArcanaAugments.getAugmentOnItem(itemStack, ArcanaAugments.ADJUSTABLE_FUSE.id) > 0 && user instanceof ServerPlayerEntity player){
            NbtCompound magicTag = itemStack.getNbt().getCompound("arcananovum");
            byte flight = magicTag.getCompound("Fireworks").getByte("Flight");
            flight = (byte) ((flight % 3) + 1);
            magicTag.getCompound("Fireworks").putByte("Flight", flight);
            player.sendMessage(Text.literal("Fuse Adjusted to "+flight).formatted(Formatting.YELLOW),true);
            SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_NOTE_BLOCK_SNARE, 1,0.8f);
         }else if (user.isFallFlying()) {
            if (!world.isClient && user instanceof ServerPlayerEntity player) {
               if(((EnergyItem)getThis()).getEnergy(itemStack) > 0){
                  FireworkRocketEntity fireworkRocketEntity = new FireworkRocketEntity(world, getFireworkStack(itemStack), user);
                  world.spawnEntity(fireworkRocketEntity);
                  if (!user.getAbilities().creativeMode) {
                     ((EnergyItem)getThis()).addEnergy(itemStack,-1);
                     redoLore(itemStack);
                     PLAYER_DATA.get(player).addXP(100); // Add xp
                  }
                  user.incrementStat(Stats.USED.getOrCreateStat(this));
                  if(player.getPos().getY() > 500){
                     ArcanaAchievements.grant(player,ArcanaAchievements.ROCKETMAN.id);
                  }
               }else{
                  player.sendMessage(Text.literal("The Rocket is out of Charges").formatted(Formatting.YELLOW),true);
                  SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_FIRE_EXTINGUISH, 1,0.8f);
               }
            }
            return TypedActionResult.success(user.getStackInHand(hand), world.isClient());
         }
         return TypedActionResult.pass(user.getStackInHand(hand));
      }
   }
}
