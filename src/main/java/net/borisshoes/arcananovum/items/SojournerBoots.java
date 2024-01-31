package net.borisshoes.arcananovum.items;

import com.google.common.collect.Multimap;
import eu.pb4.polymer.core.mixin.client.item.packet.CreativeInventoryActionC2SPacketMixin;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.EnergyItem;
import net.borisshoes.arcananovum.core.polymer.MagicPolymerArmorItem;
import net.borisshoes.arcananovum.recipes.arcana.ForgeRequirement;
import net.borisshoes.arcananovum.recipes.arcana.MagicItemIngredient;
import net.borisshoes.arcananovum.recipes.arcana.MagicItemRecipe;
import net.borisshoes.arcananovum.utils.EnhancedStatUtils;
import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.borisshoes.arcananovum.utils.MagicRarity;
import net.borisshoes.arcananovum.utils.SoundUtils;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryListener;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ArmorMaterials;
import net.minecraft.item.DyeableItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Pair;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;

public class SojournerBoots extends EnergyItem {
   
   private static final String TXT = "item/sojourner_boots";
   
   public SojournerBoots(){
      id = "sojourner_boots";
      name = "Sojourner's Boots";
      rarity = MagicRarity.LEGENDARY;
      categories = new ArcaneTome.TomeFilter[]{ArcaneTome.TomeFilter.LEGENDARY, ArcaneTome.TomeFilter.EQUIPMENT};
      vanillaItem = Items.LEATHER_BOOTS;
      item = new SojournerBootsItem(new FabricItemSettings().maxCount(1).fireproof());
      models = new ArrayList<>();
      models.add(new Pair<>(vanillaItem,TXT));
      
      ItemStack stack = new ItemStack(item);
      NbtCompound tag = stack.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList enchants = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name","[{\"text\":\"Sojourner\\'s Boots\",\"italic\":false,\"color\":\"dark_green\",\"bold\":true}]");
      display.putInt("color",3385600);
      tag.put("display",display);
      tag.put("Enchantments",enchants);
      tag.putInt("HideFlags", 255);
      tag.putInt("Unbreakable",1);
      buildItemLore(stack, ArcanaNovum.SERVER);
   
      setBookLore(makeLore());
      setRecipe(makeRecipe());
      tag = this.addMagicNbt(tag);
      tag.getCompound("arcananovum").putBoolean("active",true);
      prefNBT = tag;
      
      stack.setNbt(prefNBT);
      prefItem = stack;
   }
   
   @Override
   public NbtList getItemLore(@Nullable ItemStack itemStack){
      NbtList loreList = new NbtList();
      loreList.add(NbtString.of("[{\"text\":\"These \",\"italic\":false,\"color\":\"green\"},{\"text\":\"Boots \",\"color\":\"dark_green\"},{\"text\":\"shall take you to see the \"},{\"text\":\"world\",\"color\":\"dark_aqua\"},{\"text\":\"...\",\"color\":\"green\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Merely \",\"italic\":false,\"color\":\"green\"},{\"text\":\"wearing \",\"color\":\"blue\"},{\"text\":\"them makes you want to go on an \"},{\"text\":\"adventure\",\"color\":\"dark_aqua\"},{\"text\":\".\",\"color\":\"green\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"These \",\"italic\":false,\"color\":\"green\"},{\"text\":\"Boots \",\"color\":\"dark_green\"},{\"text\":\"are \"},{\"text\":\"unbreakable \",\"color\":\"blue\"},{\"text\":\"and equal to \"},{\"text\":\"unenchanted netherite\",\"color\":\"dark_red\"},{\"text\":\".\",\"color\":\"green\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Wearing them gives \",\"italic\":false,\"color\":\"green\"},{\"text\":\"ramping move speed\",\"color\":\"dark_aqua\"},{\"text\":\" and \"},{\"text\":\"uphill step assist\",\"color\":\"blue\"},{\"text\":\".\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Sneak Right Click\",\"italic\":false,\"color\":\"dark_aqua\"},{\"text\":\" the \",\"color\":\"green\"},{\"text\":\"Boots \",\"color\":\"dark_green\"},{\"text\":\"to \",\"color\":\"green\"},{\"text\":\"toggle \"},{\"text\":\"their \",\"color\":\"green\"},{\"text\":\"step assist\",\"color\":\"blue\"}]"));
      return loreList;
   }
   
   @Override
   public int getMaxEnergy(ItemStack item){ // +250% speed base
      int boostLvl = Math.max(0, ArcanaAugments.getAugmentOnItem(item,ArcanaAugments.MARATHON_RUNNER.id));
      return 250 + 50*boostLvl;
   }
   
   @Override
   public ItemStack updateItem(ItemStack stack, MinecraftServer server){
      NbtCompound itemNbt = stack.getNbt();
      NbtList enchants = itemNbt.getList("Enchantments", NbtElement.COMPOUND_TYPE);
      NbtCompound newTag = super.updateItem(stack,server).getNbt();
      if(enchants != null) newTag.put("Enchantments", enchants);
      stack.setNbt(newTag);
      return buildItemLore(stack,server);
   }
   
   // Holy fuck, the fact that doing this server-side is so unbelievably difficult is absurd.
   // This can be done in a SINGLE line of code client-side...
   public void attemptStepAssist(ItemStack stack, ServerPlayerEntity player, Vec3d playerVel){
      NbtCompound itemNbt = stack.getNbt();
      NbtCompound magicNbt = itemNbt.getCompound("arcananovum");
      boolean active = magicNbt.getBoolean("active");
      if(!player.isSneaking() && active){
         ServerWorld world = player.getServerWorld();
         Vec3d playerPos = player.getPos();
         double maxHeight = 1.3 + Math.max(0,ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.HIKING_BOOTS.id));
         double height = findHeight(player,playerVel, maxHeight);
         
         if(height > 0.55){
            Set<PositionFlag> set = new HashSet<>();
            set.add(PositionFlag.X);
            set.add(PositionFlag.Y);
            set.add(PositionFlag.Z);
            
            player.networkHandler.requestTeleport(playerPos.getX(), playerPos.getY()+height+0.1, playerPos.getZ(), player.getYaw(), player.getPitch(), set);
            PLAYER_DATA.get(player).addXP(2); // Add xp
         }
      }
   }
   
   private double findHeight(ServerPlayerEntity player, Vec3d velocity, double maxHeight){
      ServerWorld world = player.getServerWorld();
      Vec3d predictXZ = player.getRotationVector().multiply(1,0,1).normalize().multiply(0.2);
      if(velocity.multiply(1,0,1).normalize().dotProduct(player.getRotationVector().multiply(1,0,1).normalize()) < -0.5) return -1;
      
      for(double y = 0; y < maxHeight; y += 0.05){
         boolean noCollisions = true;
         Box predictBox = player.getBoundingBox().offset(predictXZ.x,y,predictXZ.z);
         for (VoxelShape voxelShape : world.getBlockCollisions(player, predictBox)) {
            if (!voxelShape.isEmpty()){
               noCollisions = false;
               break;
            }
         }
         if(!noCollisions) continue;
         Box jumpBox = player.getBoundingBox().offset(0,y,0);
         for (VoxelShape voxelShape : world.getBlockCollisions(player, jumpBox)) {
            if (!voxelShape.isEmpty()){
               noCollisions = false;
               break;
            }
         }
         if(!noCollisions) continue;
         Box midpointBox = player.getBoundingBox().offset(predictXZ.x/2.0,y,predictXZ.z/2.0);
         for (VoxelShape voxelShape : world.getBlockCollisions(player, midpointBox)) {
            if (!voxelShape.isEmpty()){
               noCollisions = false;
               break;
            }
         }
         
         if(noCollisions){
            return y;
         }
      }
      return -1;
   }
   
   @Override
   public ItemStack forgeItem(Inventory inv){
      ItemStack toolStack = inv.getStack(12); // Should be the Boots
      ItemStack newMagicItem = getNewItem();
      NbtCompound nbt = toolStack.getNbt();
      if(nbt == null) return newMagicItem;
      NbtCompound newNbt = newMagicItem.getOrCreateNbt();
      if(nbt.contains("Enchantments")){
         NbtList enchants = nbt.getList("Enchantments", NbtElement.COMPOUND_TYPE);
         newNbt.put("Enchantments",enchants);
      }
      if(nbt.contains("Trim")) newNbt.put("Trim",nbt.getCompound("Trim"));
      if(nbt.contains("ArcanaStats")){
         double percentile = nbt.getDouble("ArcanaStats");
         newNbt.putDouble("ArcanaStats",percentile);
         EnhancedStatUtils.enhanceItem(newMagicItem,percentile);
      }
      return newMagicItem;
   }
   
   private List<String> makeLore(){
      ArrayList<String> list = new ArrayList<>();
      list.add("{\"text\":\"  Sojourner's Boots\\n\\nRarity: Legendary\\n\\nInstead on focusing of the combative properties of the Wings of Enderia, I tried to see how I could take inspiration from its storage of energy to enhance the wearer while also keeping the desirable\\n\"}");
      list.add("{\"text\":\"  Sojourner's Boots\\n\\nbasic protection of the netherite boots I am trying to infuse.\\n\\nThe result are a pair of boots equal to unenchanted netherite, although I believe I can add enchantments through books with an anvil.\\n\"}");
      list.add("{\"text\":\"  Sojourner's Boots\\n\\nThe boots themselves store kinetic energy like the Wings but output it immediately as a speed boost that conserves inertia. I believe my movement can be increased up to 500%. On top of that, the momentum carries me up short hills without effort.\"}");
      return list;
   }
   
   private MagicItemRecipe makeRecipe(){
      MagicItemIngredient b = new MagicItemIngredient(Items.NETHERITE_BOOTS,1,null, true);
      MagicItemIngredient o = new MagicItemIngredient(Items.CRYING_OBSIDIAN,64,null);
      MagicItemIngredient s = new MagicItemIngredient(Items.NETHER_STAR,4,null);
      MagicItemIngredient n = new MagicItemIngredient(Items.NETHERITE_INGOT,4,null);
      MagicItemIngredient g = new MagicItemIngredient(Items.GRASS_BLOCK,64,null);
      MagicItemIngredient t = new MagicItemIngredient(Items.TERRACOTTA,64,null);
      MagicItemIngredient m = new MagicItemIngredient(Items.PACKED_MUD,64,null);
      MagicItemIngredient z = new MagicItemIngredient(Items.SAND,64,null);
      
      ItemStack p1 = new ItemStack(Items.POTION);
      MagicItemIngredient x = new MagicItemIngredient(Items.POTION,1, PotionUtil.setPotion(p1, Potions.STRONG_SWIFTNESS).getNbt());
      ItemStack p2 = new ItemStack(Items.POTION);
      MagicItemIngredient l = new MagicItemIngredient(Items.POTION,1, PotionUtil.setPotion(p2, Potions.STRONG_LEAPING).getNbt());
      
      MagicItemIngredient[][] ingredients = {
            {s,o,z,o,s},
            {o,n,x,n,o},
            {m,l,b,l,t},
            {o,n,x,n,o},
            {s,o,g,o,s}};
      return new MagicItemRecipe(ingredients, new ForgeRequirement().withAnvil().withEnchanter().withCore());
   }
   
   public class SojournerBootsItem extends MagicPolymerArmorItem implements DyeableItem {
      public SojournerBootsItem(Settings settings){
         super(getThis(),ArmorMaterials.NETHERITE,Type.BOOTS,settings);
      }
      
      @Override
      public int getPolymerCustomModelData(ItemStack itemStack, @Nullable ServerPlayerEntity player){
         return ArcanaRegistry.MODELS.get(TXT).value();
      }
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
      
      @Override
      public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
         if(user.isSneaking()){
            ItemStack stack = user.getStackInHand(hand);
            NbtCompound itemNbt = stack.getNbt();
            NbtCompound magicNbt = itemNbt.getCompound("arcananovum");
            boolean active = !magicNbt.getBoolean("active");
            magicNbt.putBoolean("active",active);
            
            if(active){
               user.sendMessage(Text.literal("The Boots become energized with Arcana").formatted(Formatting.DARK_GREEN,Formatting.ITALIC),true);
               SoundUtils.playSongToPlayer((ServerPlayerEntity)user, SoundEvents.BLOCK_BEACON_POWER_SELECT, 0.8f,2f);
            }else{
               user.sendMessage(Text.literal("The Boots' energy fades").formatted(Formatting.DARK_GREEN,Formatting.ITALIC),true);
               SoundUtils.playSongToPlayer((ServerPlayerEntity)user, SoundEvents.BLOCK_BEACON_DEACTIVATE, 2,.8f);
            }
            
            return TypedActionResult.success(stack);
         }else{
            return super.use(world,user,hand);
         }
      }
      
      @Override
      public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected){
         if(!MagicItemUtils.isMagic(stack)) return;
         if(!(world instanceof ServerWorld && entity instanceof ServerPlayerEntity player)) return;
         try{
            if(stack == player.getEquippedStack(EquipmentSlot.FEET)){
               if(player.isSprinting()){
                  if(player.isOnGround()){
                     int curEnergy = getEnergy(stack);
                     int sprintLvl = Math.max(0, ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.SPRINTER.id));
                     addEnergy(stack,2*(1+sprintLvl));
                     int newEnergy = getEnergy(stack);
                     if((newEnergy % 50 == 0 || newEnergy % 50 == 1) && curEnergy != newEnergy)
                        player.sendMessage(Text.translatable("Sojourner Boots Energy: "+newEnergy).formatted(Formatting.DARK_GREEN),true);
                     PLAYER_DATA.get(player).addXP(1); // Add xp
                  }
                  if(getEnergy(stack) >= getMaxEnergy(stack)){
                     ArcanaAchievements.progress(player,ArcanaAchievements.RUNNING.id, 1);
                  }
               }else{
                  addEnergy(stack,-10);
               }
               NbtCompound nbt = stack.getNbt();
               ArrayList<Pair<EntityAttribute, EntityAttributeModifier>> newAttrs = new ArrayList<>();
               Multimap<EntityAttribute, EntityAttributeModifier> attributes = (nbt != null && nbt.contains("AttributeModifiers", NbtElement.LIST_TYPE)) ? stack.getAttributeModifiers(EquipmentSlot.FEET) : getAttributeModifiers(stack,EquipmentSlot.FEET);
               for(Map.Entry<EntityAttribute, EntityAttributeModifier> entry : attributes.entries()){
                  if(entry.getValue().toNbt().getString("Name").equals("Sojourner Speed")) continue;
                  newAttrs.add(new Pair<>(entry.getKey(),entry.getValue()));
                  //System.out.println(entry.getValue().getName()+" "+entry.getValue().getValue());
               }
               
               nbt.remove("AttributeModifiers");
               for(Pair<EntityAttribute, EntityAttributeModifier> newAttr : newAttrs){
                  stack.addAttributeModifier(newAttr.getLeft(),newAttr.getRight(),EquipmentSlot.FEET);
               }
               
               stack.addAttributeModifier(EntityAttributes.GENERIC_MOVEMENT_SPEED, new EntityAttributeModifier(UUID.randomUUID(), "Sojourner Speed", getEnergy(stack)/100.0, EntityAttributeModifier.Operation.MULTIPLY_BASE),EquipmentSlot.FEET);
            }else{
               if(getEnergy(stack) != 0){
                  setEnergy(stack, 0);
               }
            }
         }catch(Exception e){
            e.printStackTrace();
         }
      }
   }
}
