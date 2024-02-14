package net.borisshoes.arcananovum.items;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.EnergyItem;
import net.borisshoes.arcananovum.core.polymer.MagicPolymerItem;
import net.borisshoes.arcananovum.recipes.arcana.GenericMagicIngredient;
import net.borisshoes.arcananovum.recipes.arcana.MagicItemIngredient;
import net.borisshoes.arcananovum.recipes.arcana.MagicItemRecipe;
import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.borisshoes.arcananovum.utils.MagicRarity;
import net.borisshoes.arcananovum.utils.ParticleEffectUtils;
import net.borisshoes.arcananovum.utils.SoundUtils;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Pair;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockLocating;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.dimension.NetherPortal;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;

public class Planeshifter extends EnergyItem {
   
   public static final int[] cdReduction = {0,60,120,240,360,480};
   private static final String TXT_NONE = "item/planeshifter_none";
   private static final String TXT_OVERWORLD = "item/planeshifter_overworld";
   private static final String TXT_NETHER = "item/planeshifter_nether";
   private static final String TXT_END = "item/planeshifter_end";
   
   public Planeshifter(){
      id = "planeshifter";
      name = "Planeshifter";
      rarity = MagicRarity.EXOTIC;
      categories = new ArcaneTome.TomeFilter[]{ArcaneTome.TomeFilter.EXOTIC, ArcaneTome.TomeFilter.ITEMS};
      itemVersion = 0;
      initEnergy = 600;
      vanillaItem = Items.RECOVERY_COMPASS;
      item = new PlaneshifterItem(new FabricItemSettings().maxCount(1).fireproof());
      models = new ArrayList<>();
      models.add(new Pair<>(vanillaItem,TXT_NONE));
      models.add(new Pair<>(vanillaItem,TXT_OVERWORLD));
      models.add(new Pair<>(vanillaItem,TXT_NETHER));
      models.add(new Pair<>(vanillaItem,TXT_END));
      
      ItemStack stack = new ItemStack(item);
      NbtCompound tag = stack.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList enchants = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name","[{\"text\":\"Planeshifter\",\"italic\":false,\"bold\":true,\"color\":\"dark_purple\"}]");
      tag.put("Enchantments",enchants);
      tag.put("display",display);
      
      setBookLore(makeLore());
      setRecipe(makeRecipe());
      stack.setNbt(addMagicNbt(tag));
      NbtCompound magicTag = tag.getCompound("arcananovum");
      magicTag.putBoolean("netherUnlocked",false);
      magicTag.putBoolean("endUnlocked",false);
      magicTag.putInt("mode",-1); // -1 disabled, 0 nether, 1 end
      magicTag.putInt("heat",0);
      stack.setNbt(tag);
      setPrefStack(stack);
   }
   
   @Override
   public NbtList getItemLore(@Nullable ItemStack itemStack){
      NbtList loreList = new NbtList();
      loreList.add(NbtString.of("[{\"text\":\"A handheld \",\"italic\":false,\"color\":\"dark_purple\"},{\"text\":\"End\",\"color\":\"dark_aqua\"},{\"text\":\" and \"},{\"text\":\"Nether \",\"color\":\"red\"},{\"text\":\"Portal\",\"color\":\"blue\"},{\"text\":\".\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"The \",\"italic\":false,\"color\":\"dark_purple\"},{\"text\":\"shifter \",\"color\":\"light_purple\"},{\"text\":\"must be taken to the \"},{\"text\":\"Nether \",\"color\":\"red\"},{\"text\":\"and \"},{\"text\":\"End \",\"color\":\"dark_aqua\"},{\"text\":\"to unlock their \"},{\"text\":\"modes\",\"color\":\"blue\"},{\"text\":\".\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"When in \",\"italic\":false,\"color\":\"dark_purple\"},{\"text\":\"Nether mode\",\"color\":\"red\"},{\"text\":\", the \"},{\"text\":\"shifter \",\"color\":\"light_purple\"},{\"text\":\"tries to find the \"},{\"text\":\"nearest portal\",\"color\":\"blue\"},{\"text\":\".\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"When in \",\"italic\":false,\"color\":\"dark_purple\"},{\"text\":\"End mode\",\"color\":\"dark_aqua\"},{\"text\":\", the \",\"italic\":false,\"color\":\"dark_purple\"},{\"text\":\"shifter teleports\",\"color\":\"light_purple\"},{\"text\":\" between \"},{\"text\":\"world spawns\",\"color\":\"blue\"},{\"text\":\".\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"It requires the \",\"italic\":false,\"color\":\"dark_purple\"},{\"text\":\"flowing of time\",\"color\":\"blue\"},{\"text\":\" to \",\"italic\":false,\"color\":\"dark_purple\"},{\"text\":\"recharge it.\",\"color\":\"dark_aqua\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Right Click\",\"italic\":false,\"color\":\"dark_aqua\"},{\"text\":\" to \",\"color\":\"dark_purple\"},{\"text\":\"activate \",\"color\":\"blue\"},{\"text\":\"the \",\"color\":\"dark_purple\"},{\"text\":\"shifter\",\"color\":\"light_purple\"},{\"text\":\".\",\"color\":\"dark_purple\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Sneak Right Click\",\"italic\":false,\"color\":\"dark_aqua\"},{\"text\":\" to \",\"color\":\"dark_purple\"},{\"text\":\"switch modes\",\"color\":\"blue\"},{\"text\":\".\",\"color\":\"dark_purple\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      
      if(itemStack != null){
         int charge = (getEnergy(itemStack)*100/getMaxEnergy(itemStack));
         String charging = charge == 100 ? "Charged" : "Charging";
         loreList.add(NbtString.of("[{\"text\":\""+charging+" - \",\"italic\":false,\"color\":\"dark_purple\"},{\"text\":\""+charge+"%\",\"color\":\"light_purple\",\"bold\":true},{\"text\":\"\",\"color\":\"dark_purple\",\"bold\":false}]"));
      }else{
         loreList.add(NbtString.of("[{\"text\":\"Charged - \",\"italic\":false,\"color\":\"dark_purple\"},{\"text\":\"100%\",\"color\":\"light_purple\",\"bold\":true},{\"text\":\"\",\"color\":\"dark_purple\",\"bold\":false}]"));
      }
      
      return loreList;
   }
   
   @Override
   public ItemStack updateItem(ItemStack stack, MinecraftServer server){
      NbtCompound itemNbt = stack.getNbt();
      NbtCompound magicTag = itemNbt.getCompound("arcananovum");
      boolean hasNether = magicTag.getBoolean("netherUnlocked");
      boolean hasEnd = magicTag.getBoolean("endUnlocked");
      int mode = magicTag.getInt("mode");
      int heat = magicTag.getInt("heat");
      NbtCompound newTag = super.updateItem(stack,server).getNbt();
      newTag.getCompound("arcananovum").putInt("heat",heat);
      newTag.getCompound("arcananovum").putInt("mode",mode);
      newTag.getCompound("arcananovum").putBoolean("netherUnlocked",hasNether);
      newTag.getCompound("arcananovum").putBoolean("endUnlocked",hasEnd);
      stack.setNbt(newTag);
      return buildItemLore(stack,server);
   }
   
   @Override
   public boolean blocksHandInteractions(ItemStack item){
      return true;
   }
   
   @Override
   public int getMaxEnergy(ItemStack item){ // 10 minute recharge time
      int cdLvl = Math.max(0, ArcanaAugments.getAugmentOnItem(item,ArcanaAugments.PLANAR_FLOW.id));
      return 600 - cdReduction[cdLvl];
   }
   
   private void findPortalAndTeleport(ServerPlayerEntity player, ServerWorld destWorld, boolean destIsNether){
      double scale = DimensionType.getCoordinateScaleFactor(player.getServerWorld().getDimension(), destWorld.getDimension());
      WorldBorder worldBorder = destWorld.getWorldBorder();
      BlockPos destPos = worldBorder.clamp(player.getX() * scale, player.getY(), player.getZ() * scale);
      Optional<BlockLocating.Rectangle> portalRect = destWorld.getPortalForcer().getPortalRect(destPos, destIsNether, worldBorder);
      if(portalRect.isPresent()){
         TeleportTarget target = NetherPortal.getNetherTeleportTarget(destWorld, portalRect.get(), Direction.Axis.X, new Vec3d(0.5, 0.0, 0.0), player, player.getVelocity(), player.getYaw(), player.getPitch());
         player.resetPortalCooldown();
         player.teleport(destWorld,target.position.x,target.position.y,target.position.z,target.yaw,target.pitch);
         player.sendMessage(Text.translatable("The Planeshifter syncs up with a Nether Portal").formatted(Formatting.DARK_AQUA,Formatting.ITALIC),true);
      }else{
         player.teleport(destWorld,destPos.getX(),destPos.getY(),destPos.getZ(),player.getYaw(),player.getPitch());
         player.sendMessage(Text.translatable("The Planeshifter could not find a Nether Portal").formatted(Formatting.DARK_AQUA,Formatting.ITALIC),true);
         
         for(int y = player.getBlockY(); y >= player.getBlockY()-destWorld.getHeight(); y--){
            BlockPos blockPos = new BlockPos(player.getBlockX(),y,player.getBlockZ());
            BlockState state = destWorld.getBlockState(blockPos);
            if(state.isOf(Blocks.LAVA)){
               ArcanaAchievements.grant(player,ArcanaAchievements.UNFORTUNATE_MATERIALIZATION.id);
               break;
            }else if(!(state.isAir() || state.getCollisionShape(destWorld,blockPos).isEmpty())){
               break;
            }
         }
      }
   }
   
   private void teleport(ItemStack item, ServerPlayerEntity player){
      NbtCompound itemNbt = item.getNbt();
      NbtCompound magicNbt = itemNbt.getCompound("arcananovum");
      int mode = magicNbt.getInt("mode");
      ServerWorld world = player.getServerWorld();
      
      String worldString = world.getRegistryKey().getValue().toString();
      boolean inOverworld = worldString.equals("minecraft:overworld");
      boolean inEnd = worldString.equals("minecraft:the_end");
      boolean inNether = worldString.equals("minecraft:the_nether");
      if(inNether) ArcanaAchievements.setCondition(player,ArcanaAchievements.PLANE_RIDER.id, "From The Nether",true);
      if(inEnd) ArcanaAchievements.setCondition(player,ArcanaAchievements.PLANE_RIDER.id,"From The End",true);
      if(inOverworld) ArcanaAchievements.setCondition(player,ArcanaAchievements.PLANE_RIDER.id,"From The Overworld",true);
      
      if(mode == 0) { // nether mode
         if(inNether){
            world = world.getServer().getWorld(World.OVERWORLD);
            findPortalAndTeleport(player,world,false);
            ArcanaAchievements.setCondition(player,ArcanaAchievements.PLANE_RIDER.id, "To The Overworld",true);
         }else{
            world = world.getServer().getWorld(World.NETHER);
            findPortalAndTeleport(player,world,true);
            ArcanaAchievements.setCondition(player,ArcanaAchievements.PLANE_RIDER.id, "To The Nether",true);
         }
      }else if(mode == 1){ // end mode
         if(inEnd){
            player.moveToWorld(player.getServer().getWorld(World.OVERWORLD));
            ArcanaAchievements.setCondition(player,ArcanaAchievements.PLANE_RIDER.id, "To The Overworld",true);
         }else{
            player.moveToWorld(player.getServer().getWorld(World.END));
            ArcanaAchievements.setCondition(player,ArcanaAchievements.PLANE_RIDER.id, "To The End",true);
         }
      }
      
      PLAYER_DATA.get(player).addXP(1000); // Add xp
      setEnergy(item,0);
      SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_PORTAL_TRAVEL,1,2f);
      ParticleEffectUtils.recallTeleport(world,player.getPos());
   }
   
   private MagicItemRecipe makeRecipe(){
      MagicItemIngredient a = new MagicItemIngredient(Items.ENDER_EYE,64,null);
      MagicItemIngredient b = new MagicItemIngredient(Items.NETHER_STAR,2,null);
      MagicItemIngredient c = new MagicItemIngredient(Items.OBSIDIAN,64,null);
      MagicItemIngredient g = new MagicItemIngredient(Items.CRYING_OBSIDIAN,64,null);
      MagicItemIngredient h = new MagicItemIngredient(Items.END_CRYSTAL,32,null);
      GenericMagicIngredient m = new GenericMagicIngredient(ArcanaRegistry.TEMPORAL_MOMENT,1);
      
      MagicItemIngredient[][] ingredients = {
            {a,b,c,b,a},
            {b,g,h,g,b},
            {c,h,m,h,c},
            {b,g,h,g,b},
            {a,b,c,b,a}};
      return new MagicItemRecipe(ingredients);
   }
   
   private List<String> makeLore(){
      ArrayList<String> list = new ArrayList<>();
      list.add("{\"text\":\"      Planeshifter\\n\\nRarity: Exotic\\n\\nPortals are nice, they create a stable connection between worlds. But they take too much setup, and safety isn't a concern for an Arcanist of my caliber. I can just make an unstable rift long enough to slip in.\"}");
      list.add("{\"text\":\"      Planeshifter\\n\\nHowever, the Shifter needs some dimensional energy of the destination before it can tear through the dimensional fabric.\\nFor some fraction of safety, if the shifter finds a Portal near the destination, it should sync my rift to its location.\"}");
      return list;
   }
   
   public class PlaneshifterItem extends MagicPolymerItem {
      public PlaneshifterItem(Settings settings){
         super(getThis(),settings);
      }
      
      @Override
      public int getPolymerCustomModelData(ItemStack itemStack, @Nullable ServerPlayerEntity player){
         if(player == null) return ArcanaRegistry.MODELS.get(TXT_NONE).value();
         NbtCompound itemNbt = itemStack.getNbt();
         NbtCompound magicNbt = itemNbt.getCompound("arcananovum");
         int mode = magicNbt.getInt("mode"); // 0 nether - 1 end
         ServerWorld world = player.getServerWorld();
         String worldString = world.getRegistryKey().getValue().toString();
         boolean inEnd = worldString.equals("minecraft:the_end");
         boolean inNether = worldString.equals("minecraft:the_nether");
         
         if(getEnergy(itemStack) < getMaxEnergy(itemStack)){
            return ArcanaRegistry.MODELS.get(TXT_NONE).value();
         }else if(mode == 0){
            return inNether ? ArcanaRegistry.MODELS.get(TXT_OVERWORLD).value() : ArcanaRegistry.MODELS.get(TXT_NETHER).value();
         }else if(mode == 1){
            return inEnd ? ArcanaRegistry.MODELS.get(TXT_OVERWORLD).value() : ArcanaRegistry.MODELS.get(TXT_END).value();
         }
         
         return ArcanaRegistry.MODELS.get(TXT_NONE).value();
      }
      
      @Override
      public TypedActionResult<ItemStack> use(World world, PlayerEntity playerEntity, Hand hand) {
         ItemStack stack = playerEntity.getStackInHand(hand);
         if(!MagicItemUtils.isMagic(stack)) return TypedActionResult.pass(stack);
         
         NbtCompound itemNbt = stack.getNbt();
         NbtCompound magicNbt = itemNbt.getCompound("arcananovum");
         int mode = magicNbt.getInt("mode");
         boolean nether = magicNbt.getBoolean("netherUnlocked");
         boolean end = magicNbt.getBoolean("endUnlocked");
         
         if(playerEntity.isSneaking()){
            if(!end && !nether){
               playerEntity.sendMessage(Text.literal("The Planeshifter has not unlocked any dimensions").formatted(Formatting.DARK_AQUA,Formatting.ITALIC),true);
            }else if(!end){
               playerEntity.sendMessage(Text.literal("The Planeshifter only has Nether mode").formatted(Formatting.DARK_AQUA,Formatting.ITALIC),true);
            }else if(!nether){
               playerEntity.sendMessage(Text.literal("The Planeshifter only has End mode").formatted(Formatting.DARK_AQUA,Formatting.ITALIC),true);
            }else{
               if(mode != 0){
                  playerEntity.sendMessage(Text.literal("The Planeshifter set to Nether mode").formatted(Formatting.DARK_AQUA,Formatting.ITALIC),true);
                  magicNbt.putInt("mode",0);
               }else{
                  playerEntity.sendMessage(Text.literal("The Planeshifter set to End mode").formatted(Formatting.DARK_AQUA,Formatting.ITALIC),true);
                  magicNbt.putInt("mode",1);
               }
            }
         }else{
            int curEnergy = getEnergy(stack);
            if(mode == -1){
               playerEntity.sendMessage(Text.literal("The Planeshifter has not unlocked any dimensions").formatted(Formatting.DARK_AQUA,Formatting.ITALIC),true);
               SoundUtils.playSongToPlayer((ServerPlayerEntity) playerEntity, SoundEvents.BLOCK_FIRE_EXTINGUISH, 1, .5f);
            }else if(mode == 0 && !playerEntity.getServer().isNetherAllowed()){
               playerEntity.sendMessage(Text.literal("The Nether is not enabled on this Server").formatted(Formatting.DARK_AQUA,Formatting.ITALIC),true);
               SoundUtils.playSongToPlayer((ServerPlayerEntity) playerEntity, SoundEvents.BLOCK_FIRE_EXTINGUISH, 1, .5f);
            }else if(curEnergy >= getMaxEnergy(stack)){
               magicNbt.putInt("heat", 1); // Starts the heat up process
               SoundUtils.playSound(playerEntity.getWorld(), playerEntity.getBlockPos(), SoundEvents.BLOCK_PORTAL_TRIGGER, SoundCategory.PLAYERS, 1, 1);
            }else{
               playerEntity.sendMessage(Text.literal("Planeshifter Recharging: " + (curEnergy * 100 / getMaxEnergy(stack)) + "%").formatted(Formatting.DARK_AQUA), true);
               SoundUtils.playSongToPlayer((ServerPlayerEntity) playerEntity, SoundEvents.BLOCK_FIRE_EXTINGUISH, 1, .5f);
            }
         }
         return TypedActionResult.success(stack);
      }
      
      @Override
      public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected){
         if(!MagicItemUtils.isMagic(stack)) return;
         if(!(world instanceof ServerWorld serverWorld && entity instanceof ServerPlayerEntity player)) return;
         NbtCompound itemNbt = stack.getNbt();
         NbtCompound magicNbt = itemNbt.getCompound("arcananovum");
         int heat = magicNbt.getInt("heat");
         int mode = magicNbt.getInt("mode");
         
         if(!magicNbt.getBoolean("netherUnlocked") && player.getServerWorld().getRegistryKey().equals(World.NETHER)){
            magicNbt.putBoolean("netherUnlocked",true);
            magicNbt.putInt("mode",0);
            player.sendMessage(Text.literal("The Planeshifter has Unlocked The Nether").formatted(Formatting.DARK_AQUA,Formatting.ITALIC),true);
            SoundUtils.playSongToPlayer(player, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 0.3f,2f);
         }
         if(!magicNbt.getBoolean("endUnlocked") && player.getServerWorld().getRegistryKey().equals(World.END)){
            magicNbt.putBoolean("endUnlocked",true);
            magicNbt.putInt("mode",1);
            player.sendMessage(Text.literal("The Planeshifter has Unlocked The End").formatted(Formatting.DARK_AQUA,Formatting.ITALIC),true);
            SoundUtils.playSongToPlayer(player, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 0.3f,2f);
         }
         
         if(heat == 100){
            teleport(stack,player);
            magicNbt.putInt("heat",0);
         }else if(heat > 0){
            magicNbt.putInt("heat",heat+1);
            ParticleEffectUtils.recallTeleportCharge(serverWorld,player.getPos());
         }else if(heat == -1){
            // Teleport was cancelled by damage
            ParticleEffectUtils.recallTeleportCancel(serverWorld,player.getPos());
            SoundUtils.playSound(player.getServerWorld(), player.getBlockPos(), SoundEvents.ENTITY_ENDERMAN_HURT, SoundCategory.PLAYERS, 8,0.8f);
            magicNbt.putInt("heat",0);
            setEnergy(stack,(int)(getMaxEnergy(stack)*0.75));
         }
         
         if(world.getServer().getTicks() % 20 == 0){
            addEnergy(stack, 1); // Recharge
            buildItemLore(stack,world.getServer());
         }
      }
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
   }
}
