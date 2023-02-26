package net.borisshoes.arcananovum.items;

import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.items.core.EnergyItem;
import net.borisshoes.arcananovum.items.core.TickingItem;
import net.borisshoes.arcananovum.recipes.MagicItemIngredient;
import net.borisshoes.arcananovum.recipes.MagicItemRecipe;
import net.borisshoes.arcananovum.utils.MagicRarity;
import net.minecraft.block.BlockState;
import net.minecraft.block.StairsBlock;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardPlayerScore;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;
import static net.minecraft.block.StairsBlock.HALF;

public class SojournerBoots extends EnergyItem implements TickingItem {
   public SojournerBoots(){
      id = "sojourner_boots";
      name = "Sojourner's Boots";
      rarity = MagicRarity.LEGENDARY;
      categories = new ArcaneTome.TomeFilter[]{ArcaneTome.TomeFilter.LEGENDARY, ArcaneTome.TomeFilter.ARMOR};
      
      ItemStack item = new ItemStack(Items.LEATHER_BOOTS);
      NbtCompound tag = item.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList loreList = new NbtList();
      NbtList enchants = new NbtList();
      NbtList attributes = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name","[{\"text\":\"Sojourner\\'s Boots\",\"italic\":false,\"color\":\"dark_green\",\"bold\":true}]");
      loreList.add(NbtString.of("[{\"text\":\"These boots shall take you to see the \",\"italic\":false,\"color\":\"green\"},{\"text\":\"world\",\"color\":\"dark_green\"},{\"text\":\"...\",\"color\":\"green\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Merely wearing them makes you want to go on an \",\"italic\":false,\"color\":\"green\"},{\"text\":\"adventure\",\"color\":\"dark_aqua\"},{\"text\":\".\",\"color\":\"green\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Gives \",\"italic\":false,\"color\":\"green\"},{\"text\":\"ramping move speed\",\"color\":\"gray\"},{\"text\":\" and \"},{\"text\":\"uphill step assist\",\"color\":\"dark_aqua\"},{\"text\":\".\",\"color\":\"green\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"These boots are \",\"italic\":false,\"color\":\"green\"},{\"text\":\"unbreakable\",\"color\":\"blue\"},{\"text\":\" and equal to \"},{\"text\":\"unenchanted\",\"color\":\"gray\"},{\"text\":\" \",\"color\":\"blue\"},{\"text\":\"netherite\",\"color\":\"dark_red\"},{\"text\":\".\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Legendary\",\"italic\":false,\"color\":\"gold\",\"bold\":true},{\"text\":\" Magic Item\",\"italic\":false,\"color\":\"dark_purple\",\"bold\":false}]"));
      display.put("Lore",loreList);
      display.putInt("color",3385600);
      NbtCompound kbRes = new NbtCompound();
      kbRes.putDouble("Amount",0.1);
      kbRes.putString("AttributeName","generic.knockback_resistance");
      kbRes.putString("Name","generic.knockback_resistance");
      kbRes.putString("Slot","feet");
      kbRes.putIntArray("UUID", new int[]{-122122, 141691, 182346, -283382});
      attributes.add(kbRes);
      NbtCompound toughness = new NbtCompound();
      toughness.putInt("Amount",3);
      toughness.putString("AttributeName","generic.armor_toughness");
      toughness.putString("Name","generic.armor_toughness");
      toughness.putString("Slot","feet");
      toughness.putIntArray("UUID", new int[]{-122122, 141691, 182346, -283382});
      attributes.add(toughness);
      NbtCompound armor = new NbtCompound();
      armor.putInt("Amount",3);
      armor.putString("AttributeName","generic.armor");
      armor.putString("Name","generic.armor");
      armor.putString("Slot","feet");
      armor.putIntArray("UUID", new int[]{-122122, 141691, 182346, -283382});
      attributes.add(armor);
      NbtCompound speed = new NbtCompound();
      speed.putDouble("Amount",0.0);
      speed.putString("AttributeName","generic.movement_speed");
      speed.putString("Name","generic.movement_speed");
      speed.putInt("Operation",1);
      speed.putString("Slot","feet");
      speed.putIntArray("UUID", new int[]{-122122, 141691, 182346, -283382});
      attributes.add(speed);
      tag.put("display",display);
      tag.put("Enchantments",enchants);
      tag.put("AttributeModifiers",attributes);
      tag.putInt("HideFlags",103);
      tag.putInt("Unbreakable",1);
   
      setBookLore(makeLore());
      setRecipe(makeRecipe());
      prefNBT = addMagicNbt(tag);
      item.setNbt(prefNBT);
      prefItem = item;
   }
   
   @Override
   public int getMaxEnergy(ItemStack item){ // +500% speed base
      int boostLvl = Math.max(0, ArcanaAugments.getAugmentOnItem(item,"marathon_runner"));
      return 500 + 50*boostLvl;
   }
   
   @Override
   public ItemStack updateItem(ItemStack stack, MinecraftServer server){
      NbtCompound itemNbt = stack.getNbt();
      NbtList enchants = itemNbt.getList("Enchantments", NbtElement.COMPOUND_TYPE);
      NbtCompound newTag = super.updateItem(stack,server).getNbt();
      if(enchants != null) newTag.put("Enchantments", enchants);
      stack.setNbt(newTag);
      return stack;
   }
   
   @Override
   public void onTick(ServerWorld world, ServerPlayerEntity player, ItemStack item){
      try{
         if(item == player.getEquippedStack(EquipmentSlot.FEET)){
            // Step assist
            
            // Holy fuck, the fact that doing this server-side is so unbelievably difficult is absurd.
            // This can be done in a SINGLE line of code client-side...
            if(!player.isSneaking()){
               // If player is moving, check block in front of them, and 2 spaces above, teleport 1.2 blocks up
               ServerScoreboard scoreboard = world.getServer().getScoreboard();
               ScoreboardObjective walk = scoreboard.getNullableObjective("arcananovum_sojourn_walk");
               ScoreboardObjective sprint = scoreboard.getNullableObjective("arcananovum_sojourn_sprint");
               ScoreboardPlayerScore walkScore = scoreboard.getPlayerScore(player.getEntityName(),walk);
               ScoreboardPlayerScore sprintScore = scoreboard.getPlayerScore(player.getEntityName(),sprint);
               if(walkScore.getScore() + sprintScore.getScore() > 1){
                  Vec3d playerRot = player.getRotationVector();
                  Vec3d playerPos = player.getPos();
                  Vec3d vec3d3 = new Vec3d(playerRot.getX(),0,playerRot.getZ());
                  BlockPos pos = new BlockPos(playerPos.add(vec3d3.normalize().multiply(0.45)));
                  double height = checkHeight(world,pos,playerPos.y);
                  BlockPos aboveHeadPos = new BlockPos(playerPos.add(0,2.5,0));
                  int hikingBonus = Math.max(0,ArcanaAugments.getAugmentOnItem(item,"hiking_boots"));
                  
                  if(height > 0.5 && height < 1.3+hikingBonus && world.getBlockState(aboveHeadPos).isAir()){
                     EnumSet<PlayerPositionLookS2CPacket.Flag> set = EnumSet.noneOf(PlayerPositionLookS2CPacket.Flag.class);
                     set.add(PlayerPositionLookS2CPacket.Flag.X);
                     set.add(PlayerPositionLookS2CPacket.Flag.Y);
                     set.add(PlayerPositionLookS2CPacket.Flag.Z);
                     
                     player.networkHandler.requestTeleport(playerPos.getX(), playerPos.getY()+height+0.1, playerPos.getZ(), player.getYaw(), player.getPitch(), set);
                     PLAYER_DATA.get(player).addXP(2); // Add xp
                  }
                  walkScore.setScore(0);
                  sprintScore.setScore(0);
               }
            }
            
            if(player.isSprinting()){
               int curEnergy = getEnergy(item);
               int sprintLvl = Math.max(0, ArcanaAugments.getAugmentOnItem(item,"sprinter"));
               
               addEnergy(item,2*(1+sprintLvl));
               int newEnergy = getEnergy(item);
               if((newEnergy % 50 == 0 || newEnergy % 50 == 1) && curEnergy != newEnergy)
                  player.sendMessage(Text.translatable("Sojourner Boots Energy: "+newEnergy).formatted(Formatting.DARK_GREEN),true);
               PLAYER_DATA.get(player).addXP(1); // Add xp
               
               if(newEnergy == getMaxEnergy(item)){
                  ArcanaAchievements.progress(player,"running",1);
               }
            }else{
               addEnergy(item,-10);
            }
            NbtCompound nbt = item.getNbt();
            NbtList attributes = nbt.getList("AttributeModifiers", NbtElement.COMPOUND_TYPE);
            NbtCompound speed = new NbtCompound();
            speed.putDouble("Amount",getEnergy(item)/100.0);
            speed.putString("AttributeName","generic.movement_speed");
            speed.putString("Name","generic.movement_speed");
            speed.putInt("Operation",1);
            speed.putString("Slot","feet");
            speed.putIntArray("UUID", new int[]{-122122, 141691, 182346, -283382});
            attributes.set(3,speed);
            nbt.put("AttributeModifiers",attributes);
         }else{
            if(getEnergy(item) != 0){
               setEnergy(item, 0);
            }
         }
      }catch(Exception e){
         e.printStackTrace();
      }
   }
   
   private double checkHeight(World world, BlockPos pos, double curY){
      BlockPos[] poses = {pos,new BlockPos(pos.getX(),pos.getY()+1,pos.getZ()),new BlockPos(pos.getX(),pos.getY()+2,pos.getZ())};
      BlockState[] blocks = {world.getBlockState(poses[0]),world.getBlockState(poses[1]),world.getBlockState(poses[2])};
      VoxelShape[] shapes = {blocks[0].getCollisionShape(world,poses[0]),blocks[1].getCollisionShape(world,poses[1]),blocks[2].getCollisionShape(world,poses[2])};
      double heightDiff = 0;
      for(int i = 0; i <= 2; i++){
         double height;
         if(blocks[i].getBlock() instanceof StairsBlock && blocks[i].get(HALF).name().equals("BOTTOM")){
            height = 0.5;
         }else{
            height = shapes[i].getMax(Direction.Axis.Y);
         }
         heightDiff = height > 0 ? i+height : heightDiff;
      }
      heightDiff -= (curY-poses[0].getY());
      return heightDiff;
   }
   
   @Override
   public ItemStack forgeItem(Inventory inv){
      ItemStack toolStack = inv.getStack(12); // Should be the Boots
      ItemStack newMagicItem = getNewItem();
      NbtCompound nbt = toolStack.getNbt();
      if(nbt != null && nbt.contains("Enchantments")){
         NbtList enchants = nbt.getList("Enchantments", NbtElement.COMPOUND_TYPE);
         newMagicItem.getOrCreateNbt().put("Enchantments",enchants);
      }
      return newMagicItem;
   }
   
   private List<String> makeLore(){
      ArrayList<String> list = new ArrayList<>();
      list.add("{\"text\":\"  Sojourner's Boots\\n\\nRarity: Legendary\\n\\nInstead on focusing of the combative properties of the Wings of Zephyr, I tried to see how I could take inspiration from its storage of energy to enhance the wearer while also keeping the desirable\\n\"}");
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
      return new MagicItemRecipe(ingredients);
   }
}
