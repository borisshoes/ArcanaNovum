package net.borisshoes.arcananovum.items.charms;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.EnergyItem;
import net.borisshoes.arcananovum.core.LeftClickItem;
import net.borisshoes.arcananovum.core.polymer.MagicPolymerItem;
import net.borisshoes.arcananovum.items.ArcaneTome;
import net.borisshoes.arcananovum.recipes.arcana.MagicItemIngredient;
import net.borisshoes.arcananovum.recipes.arcana.MagicItemRecipe;
import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.borisshoes.arcananovum.utils.MagicRarity;
import net.borisshoes.arcananovum.utils.ParticleEffectUtils;
import net.borisshoes.arcananovum.utils.SoundUtils;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.recipe.AbstractCookingRecipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Pair;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;

public class CindersCharm extends EnergyItem implements LeftClickItem {
   
   private static final String TXT_ON = "item/cinders_charm_on";
   private static final String TXT_OFF = "item/cinders_charm_off";
   private static final String TXT_CREMATION_ON = "item/cinders_charm_cremation_on";
   private static final String TXT_CREMATION_OFF = "item/cinders_charm_cremation_off";
   
   private final double range = 7.0;
   private final double closeW = 2.5;
   private final double farW = 6.5;
   // Delicious trigonometry at its finest
   private final double angle = 2*Math.atan2((.5*(farW-closeW)),range);
   private final double ha = angle/2;
   private final double ri = closeW / (2*Math.sin(ha)); // Cone characteristics from given parameters
   private final double ro = farW / (2*Math.sin(ha));
   
   public CindersCharm(){
      id = "cinders_charm";
      name = "Charm of Cinders";
      initEnergy = 100;
      rarity = MagicRarity.LEGENDARY;
      categories = new ArcaneTome.TomeFilter[]{ArcaneTome.TomeFilter.LEGENDARY, ArcaneTome.TomeFilter.CHARMS, ArcaneTome.TomeFilter.ITEMS};
      itemVersion = 1;
      vanillaItem = Items.BLAZE_POWDER;
      item = new CindersCharmItem(new FabricItemSettings().maxCount(1).fireproof());
      models = new ArrayList<>();
      models.add(new Pair<>(vanillaItem,TXT_ON));
      models.add(new Pair<>(vanillaItem,TXT_OFF));
      models.add(new Pair<>(vanillaItem,TXT_CREMATION_ON));
      models.add(new Pair<>(vanillaItem,TXT_CREMATION_OFF));
      
      ItemStack stack = new ItemStack(item);
      NbtCompound tag = stack.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList enchants = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name","[{\"text\":\"Charm of Cinders\",\"italic\":false,\"color\":\"gold\",\"bold\":true}]");
      tag.put("display",display);
      tag.put("Enchantments",enchants);
      
      setBookLore(makeLore());
      setRecipe(makeRecipe());
      addMagicNbt(tag);
      tag.getCompound("arcananovum").putBoolean("active",false);
      stack.setNbt(tag);
      setPrefStack(stack);
   }
   
   @Override
   public NbtList getItemLore(@Nullable ItemStack itemStack){
      NbtList loreList = new NbtList();
      loreList.add(NbtString.of("[{\"text\":\"The \",\"italic\":false,\"color\":\"red\"},{\"text\":\"charm \",\"color\":\"gold\"},{\"text\":\"burns \",\"color\":\"dark_red\"},{\"text\":\"with \"},{\"text\":\"focused intensity\",\"color\":\"dark_red\"},{\"text\":\".\",\"color\":\"red\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Flames \",\"italic\":false,\"color\":\"gold\"},{\"text\":\"welcome you with a \",\"color\":\"red\"},{\"text\":\"warm embrace\"},{\"text\":\".\",\"color\":\"red\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Left Click\",\"italic\":false,\"color\":\"dark_red\"},{\"text\":\" a block or creature to set it \",\"color\":\"red\"},{\"text\":\"ablaze\",\"color\":\"gold\"},{\"text\":\".\",\"color\":\"red\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Hold Right Click\",\"italic\":false,\"color\":\"dark_red\"},{\"text\":\" to \",\"color\":\"red\"},{\"text\":\"breathe \",\"color\":\"gold\"},{\"text\":\"a \",\"color\":\"red\"},{\"text\":\"cone of fire\",\"color\":\"gold\"},{\"text\":\" in front of you.\",\"color\":\"red\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Sneak Right Click\",\"italic\":false,\"color\":\"dark_red\"},{\"text\":\" to toggle \",\"color\":\"red\"},{\"text\":\"auto-smelting\",\"color\":\"gold\"},{\"text\":\" of picked up items.\",\"color\":\"red\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      return loreList;
   }
   
   @Override
   public boolean blocksHandInteractions(ItemStack item){
      return true;
   }
   
   @Override
   public boolean attackBlock(PlayerEntity playerEntity, World world, Hand hand, BlockPos blockPos, Direction direction){
      ItemStack itemStack = playerEntity.getStackInHand(hand);
      BlockState blockState = world.getBlockState(blockPos);
      boolean cremation = ArcanaAugments.getAugmentOnItem(itemStack,ArcanaAugments.CREMATION.id) >= 1;
      Formatting color = cremation ? Formatting.AQUA : Formatting.RED;
      
      if(blockState.isOf(Blocks.FIRE) || blockState.isOf(Blocks.SOUL_FIRE)){
         if(playerEntity instanceof ServerPlayerEntity player){
            player.networkHandler.sendPacket(new BlockUpdateS2CPacket(blockPos,blockState));
         }
         return false;
      }
      int cinderConsumption = ArcanaAugments.getAugmentOnItem(itemStack,ArcanaAugments.FIRESTARTER.id) >= 1 ? 0 : 5;
      if(getEnergy(itemStack) < cinderConsumption) {
         playerEntity.sendMessage(Text.literal("The Charm has no Cinders").formatted(color), true);
         return true;
      }
      
      if (!CampfireBlock.canBeLit(blockState) && !CandleBlock.canBeLit(blockState) && !CandleCakeBlock.canBeLit(blockState)) {
         BlockPos blockPos2 = blockPos.offset(direction);
         if(blockState.isOf(Blocks.TNT)){
            TntBlock.primeTnt(world,blockPos);
            world.setBlockState(blockPos, Blocks.AIR.getDefaultState(), 11);
            
            addEnergy(itemStack, -cinderConsumption);
            String message = "Cinders: ";
            for(int i=1; i<=getMaxEnergy(itemStack)/20; i++){
               message += getEnergy(itemStack) >= i*20 ? "✦ " : "✧ ";
            }
            playerEntity.sendMessage(Text.literal(message.toString()).formatted(color), true);
   
            if(playerEntity instanceof ServerPlayerEntity player){
               PLAYER_DATA.get(player).addXP(50*cinderConsumption); // Add xp
            }
   
            return !playerEntity.isCreative();
         }else if (AbstractFireBlock.canPlaceAt(world, blockPos2, direction)) {
            SoundUtils.playSound(world,blockPos,SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 1.0F, world.getRandom().nextFloat() * 0.4F + 0.8F);
            BlockState blockState2 = AbstractFireBlock.getState(world, blockPos2);
            world.setBlockState(blockPos2, blockState2, 11);
            world.emitGameEvent(playerEntity, GameEvent.BLOCK_PLACE, blockPos);
   
            addEnergy(itemStack, -cinderConsumption);
            String message = "Cinders: ";
            for(int i=1; i<=getMaxEnergy(itemStack)/20; i++){
               message += getEnergy(itemStack) >= i*20 ? "✦ " : "✧ ";
            }
            playerEntity.sendMessage(Text.literal(message.toString()).formatted(color), true);
            
            if(playerEntity instanceof ServerPlayerEntity player){
               PLAYER_DATA.get(player).addXP(15*cinderConsumption); // Add xp
            }
            
            return !playerEntity.isCreative();
         } else {
            return !playerEntity.isCreative();
         }
      } else {
         if(CandleCakeBlock.canBeLit(blockState) && playerEntity instanceof ServerPlayerEntity player) ArcanaAchievements.grant(player,ArcanaAchievements.CAKE_DAY.id);
         SoundUtils.playSound(world,blockPos,SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 1.0F, world.getRandom().nextFloat() * 0.4F + 0.8F);
         world.setBlockState(blockPos, (BlockState)blockState.with(Properties.LIT, true), 11);
         world.emitGameEvent(playerEntity, GameEvent.BLOCK_CHANGE, blockPos);
   
         addEnergy(itemStack, -cinderConsumption);
         String message = "Cinders: ";
         for(int i=1; i<=getMaxEnergy(itemStack)/20; i++){
            message += getEnergy(itemStack) >= i*20 ? "✦ " : "✧ ";
         }
         playerEntity.sendMessage(Text.literal(message.toString()).formatted(color), true);
   
         if(playerEntity instanceof ServerPlayerEntity player){
            PLAYER_DATA.get(player).addXP(15*cinderConsumption); // Add xp
         }
         
         return !playerEntity.isCreative();
      }
   }
   
   public ItemStack smelt(ItemStack item, PlayerEntity player, ItemStack stack){
      try{
         NbtCompound itemNbt = item.getNbt();
         NbtCompound magicNbt = itemNbt.getCompound("arcananovum");
         boolean cremation = ArcanaAugments.getAugmentOnItem(item,"cremation") >= 1;
         Formatting color = cremation ? Formatting.AQUA : Formatting.RED;
         if(MagicItemUtils.isMagic(stack)) return null;
         int energyToConsume = (int)Math.ceil(stack.getCount() / 2.0);
         if(magicNbt.getBoolean("active") && getEnergy(item) >= energyToConsume){
            // Smelting registry and auto smelt
            RecipeManager.MatchGetter<Inventory, ? extends AbstractCookingRecipe> matchGetter = RecipeManager.createCachedMatchGetter(RecipeType.SMELTING);
            SimpleInventory sInv = new SimpleInventory(stack);
            RecipeEntry<? extends AbstractCookingRecipe> recipeEntry = matchGetter.getFirstMatch(sInv,player.getEntityWorld()).orElse(null);
            if(recipeEntry == null) return null;
            AbstractCookingRecipe recipe = recipeEntry.value();
            if(recipe == null) return null;
            ItemStack recipeOutput = recipe.getResult(player.getWorld().getRegistryManager());
            if(recipeOutput.isEmpty()) return null;
            PlayerInventory inv = player.getInventory();
            ItemStack result = recipeOutput.copy();
            
            if(recipeOutput.getCount()*stack.getCount() <= recipeOutput.getItem().getMaxCount()){
               result.setCount(recipeOutput.getCount()*stack.getCount());
               if(inv.getOccupiedSlotWithRoomForStack(result) == -1 && inv.getEmptySlot() == -1) return null;
               
               player.addExperience(MathHelper.floor(recipe.getExperience()*stack.getCount()));
   
               int oldEnergy = getEnergy(item);
               addEnergy(item, -energyToConsume);
               int newEnergy = getEnergy(item);
               if(oldEnergy/20 != newEnergy/20){
                  String message = "Cinders: ";
                  for(int i=1; i<=getMaxEnergy(item)/20; i++){
                     message += newEnergy >= i*20 ? "✦ " : "✧ ";
                  }
                  player.sendMessage(Text.literal(message.toString()).formatted(color), true);
               }
               stack = result;
   
               if(player instanceof ServerPlayerEntity serverPlayer){
                  PLAYER_DATA.get(serverPlayer).addXP(energyToConsume*4); // Add xp
                  if(recipeOutput.isOf(Items.GLASS)) ArcanaAchievements.progress(serverPlayer,ArcanaAchievements.GLASSBLOWER.id,stack.getCount());
               }
               return stack;
            }
         }
      }catch(Exception e){
         e.printStackTrace();
      }
      return null;
   }
   
   private TypedActionResult<ItemStack> coneOfFlame(PlayerEntity playerEntity, World world, Hand hand){
      ItemStack itemStack = playerEntity.getStackInHand(hand);
      if(!(world instanceof ServerWorld serverWorld)) return TypedActionResult.pass(itemStack);
      
      int energy = getEnergy(itemStack);
      boolean cremation = ArcanaAugments.getAugmentOnItem(itemStack,ArcanaAugments.CREMATION.id) >= 1;
      DefaultParticleType particleType = cremation ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.FLAME;
      Formatting color = cremation ? Formatting.AQUA : Formatting.RED;
      
      if(energy < 12) {
         playerEntity.sendMessage(Text.literal("The Charm has no Cinders").formatted(color), true);
         return TypedActionResult.pass(itemStack);
      }
      addEnergy(itemStack,-3);
   
      if(energy/20 != getEnergy(itemStack)/20){
         energy = getEnergy(itemStack);
         StringBuilder message = new StringBuilder("Cinders: ");
         for(int i = 1; i <= getMaxEnergy(itemStack)/20; i++){
            message.append(energy >= i * 20 ? "✦ " : "✧ ");
         }
         playerEntity.sendMessage(Text.literal(message.toString()).formatted(color), true);
      }
   
      double mul = 1.5*range;
      Vec3d boxStart = playerEntity.getPos().subtract(mul,mul,mul);
      Vec3d boxEnd = playerEntity.getPos().add(mul,mul,mul);
      Box rangeBox = new Box(boxStart,boxEnd);
   
      SoundUtils.playSound(world, playerEntity.getBlockPos(), SoundEvents.ENTITY_BLAZE_SHOOT, SoundCategory.PLAYERS, 0.6f, (float) (Math.random() * .5 + .5));
   
      List<Entity> entities = serverWorld.getOtherEntities(playerEntity,rangeBox, e -> e instanceof LivingEntity);
      int ignited = 0;
      for(Entity e : entities){
         if(!(e instanceof LivingEntity entity)) continue;
         if(inCone(playerEntity,e.getEyePos())){
            if(!entity.isFireImmune()){
               entity.setOnFireFor((2*energy+60)/20);
               entity.damage(new DamageSource(entity.getDamageSources().onFire().getTypeRegistryEntry(),playerEntity),cremation ? 5f : 2.5f);
               if(entity instanceof MobEntity) ignited++;
            
               if(playerEntity instanceof ServerPlayerEntity serverPlayer){
                  PLAYER_DATA.get(serverPlayer).addXP(5); // Add xp
               }
            }
         }
      }
      if(playerEntity instanceof ServerPlayerEntity serverPlayer && ignited >= 12){
         ArcanaAchievements.grant(serverPlayer,ArcanaAchievements.PYROMANIAC.id);
      }
   

      double angle = 2*Math.atan2((.5*(farW-closeW)),range);
      float yaw = playerEntity.getRotationClient().y;
      Vec3d rot = Vec3d.fromPolar(0,yaw);
      double pC = Math.atan2(rot.z,rot.x);
      Vec3d rotVec = Vec3d.fromPolar(60,yaw).crossProduct(Vec3d.fromPolar(0,yaw)).normalize();
      Vec3d origin = playerEntity.getEyePos().add(playerEntity.getRotationVecClient().multiply(-ri*Math.cos(ha)));
      
      for(int i = 0; i < 40; i++){
         int tries = 0;
         Vec3d pos;
         do{
            double pD = (Math.random()*angle - angle/2);
            Vec3d offset = new Vec3d(Math.cos(pC+pD),0,Math.sin(pC+pD)).multiply(Math.random()*ro);
            
            float dT = (float) (Math.toRadians(-playerEntity.getRotationClient().x)+ (Math.random()*angle - angle/2));
            float a = (float) Math.cos(dT/2.0);
            float b = (float) (-rotVec.x * Math.sin(dT/2.0));
            float c = (float) (-rotVec.y * Math.sin(dT/2.0));
            float d = (float) (-rotVec.z * Math.sin(dT/2.0));
            float aa = a*a; // Euler-Rodrigues Formula for rotating a vector around an axis
            float bb = b*b;
            float cc = c*c;
            float dd = d*d;
            float bc = b*c;
            float ad = a*d;
            float ac = a*c;
            float ab = a*b;
            float bd = b*d;
            float cd = c*d;
            double nX = (aa+bb-cc-dd)*offset.x+(2*(bc+ad))*offset.y+(2*(bd-ac))*offset.z;
            double nY = (2*(bc-ad))*offset.x+(aa+cc-bb-dd)*offset.y+(2*(cd+ab))*offset.z;
            double nZ = (2*(bd+ac))*offset.x+(2*(cd-ab))*offset.y+(aa+dd-bb-cc)*offset.z;
            
            offset = new Vec3d(nX,nY,nZ);
            pos = offset.add(origin);
            
            tries++;
         }while(!inCone(playerEntity,pos) && tries < 12);
         //if(tries >= 11) System.out.println("Tries exceeded");
         
         serverWorld.spawnParticles(particleType,pos.getX(),pos.getY(),pos.getZ(),1,0.1,0.1,0.1,0);
      }
      return TypedActionResult.success(itemStack);
   }
   
   private TypedActionResult<ItemStack> pyroblast(PlayerEntity playerEntity, World world, ItemStack itemStack, int lvl){
      if(!(world instanceof ServerWorld serverWorld && playerEntity instanceof ServerPlayerEntity player)) return TypedActionResult.pass(itemStack);
      
      int energy = getEnergy(itemStack);
      boolean cremation = ArcanaAugments.getAugmentOnItem(itemStack,ArcanaAugments.CREMATION.id) >= 1;
      DefaultParticleType particleType = cremation ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.FLAME;
      Formatting color = cremation ? Formatting.AQUA : Formatting.RED;
      
      if(energy < 50) {
         player.sendMessage(Text.literal("Not Enough Cinders").formatted(color), true);
         return TypedActionResult.pass(itemStack);
      }
      int consumedEnergy = energy;
      addEnergy(itemStack,-energy);
      
      if(energy/20 != getEnergy(itemStack)/20){
         energy = getEnergy(itemStack);
         StringBuilder message = new StringBuilder("Cinders: ");
         for(int i = 1; i <= getMaxEnergy(itemStack)/20; i++){
            message.append(energy >= i * 20 ? "✦ " : "✧ ");
         }
         player.sendMessage(Text.literal(message.toString()).formatted(color), true);
      }
   
      Vec3d startPos = player.getEyePos();
      Vec3d view = player.getRotationVecClient();
      Vec3d rayEnd = startPos.add(view.multiply(35));
      BlockHitResult raycast = world.raycast(new RaycastContext(startPos,rayEnd, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.ANY,player));
      Vec3d center = raycast.getPos();
      double explosionRange = 3+lvl;
      Box rangeBox = new Box(center.x+12,center.y+12,center.z+12,center.x-12,center.y-12,center.z-12);
      List<Entity> entities = world.getOtherEntities(null,rangeBox, e -> !e.isSpectator() && e.squaredDistanceTo(center) < 1.25*explosionRange*explosionRange && e instanceof LivingEntity);
      
      SoundUtils.playSound(world, playerEntity.getBlockPos(), SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.PLAYERS, 0.6f, 1.7f);
      
      int ignited = 0;
      for(Entity e : entities){
         if(!(e instanceof LivingEntity entity)) continue;
         if(!entity.isFireImmune()){
            float dmg = (float) (Math.max(0,(1.2 - (entity.getPos().distanceTo(center)/explosionRange))) * (consumedEnergy/10.0) * (.8+lvl*.2));
            entity.setOnFireFor(consumedEnergy/20);
            entity.damage(new DamageSource(entity.getDamageSources().onFire().getTypeRegistryEntry(),playerEntity),cremation ? 2*dmg : dmg);
            ignited++;
   
            PLAYER_DATA.get(player).addXP(5); // Add xp
         }
      }
      
      if(ignited >= 12){
         ArcanaAchievements.grant(player,ArcanaAchievements.PYROMANIAC.id);
      }
   
      ParticleEffectUtils.pyroblastExplosion(serverWorld,particleType,center,explosionRange,0);
      ParticleEffectUtils.line(serverWorld,null,startPos.subtract(0,.3,0),center,particleType,(int)(center.distanceTo(startPos)*4),1,0,0);
      serverWorld.spawnParticles(particleType,center.getX(),center.getY(),center.getZ(),100,0.1,0.1,0.1,0.4);
      
      return TypedActionResult.success(itemStack);
   }
   
   private TypedActionResult<ItemStack> fireweb(PlayerEntity playerEntity, World world, ItemStack itemStack, int lvl){
      if(!(world instanceof ServerWorld serverWorld && playerEntity instanceof ServerPlayerEntity player)) return TypedActionResult.pass(itemStack);
      
      int energy = getEnergy(itemStack);
      boolean cremation = ArcanaAugments.getAugmentOnItem(itemStack,ArcanaAugments.CREMATION.id) >= 1;
      DefaultParticleType particleType = cremation ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.FLAME;
      Formatting color = cremation ? Formatting.AQUA : Formatting.RED;
      
      if(energy < 50) {
         player.sendMessage(Text.literal("Not Enough Cinders").formatted(color), true);
         return TypedActionResult.pass(itemStack);
      }
      int consumedEnergy = energy;
      
      Vec3d center = player.getPos();
      double effectRange = 2+lvl*2;
      int numTargets = 5*lvl;
      Box rangeBox = new Box(center.x+12,center.y+12,center.z+12,center.x-12,center.y-12,center.z-12);
      List<Entity> entities = world.getOtherEntities(player,rangeBox, e -> !e.isSpectator() && e.squaredDistanceTo(center) < 1.25*effectRange*effectRange && e instanceof LivingEntity);
      entities.sort(Comparator.comparingDouble(e->e.distanceTo(player)));
      
      if(entities.isEmpty()){
         SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_FIRE_EXTINGUISH, .3f,.8f);
         player.sendMessage(Text.literal("No Targets in Range").formatted(color), true);
         return TypedActionResult.pass(itemStack);
      }
      
      SoundUtils.playSound(world, playerEntity.getBlockPos(), SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.PLAYERS, 1f, 0.5f);
      
      List<LivingEntity> hits = new ArrayList<>();
      for(Entity e : entities){
         if(!(e instanceof LivingEntity entity)) continue;
         if(!entity.isFireImmune()){
            float dmg = (float) ((consumedEnergy/15.0) * (.8+lvl*.2));
            entity.setOnFireFor(consumedEnergy/20);
            entity.damage(new DamageSource(entity.getDamageSources().onFire().getTypeRegistryEntry(),playerEntity),cremation ? 2*dmg : dmg);
            
            PLAYER_DATA.get(player).addXP(5); // Add xp
            hits.add(entity);
         }
         if(hits.size() >= numTargets) break;
      }
      
      if(hits.size() >= 12){
         ArcanaAchievements.grant(player,ArcanaAchievements.PYROMANIAC.id);
      }
      
      ParticleEffectUtils.webOfFireCast(serverWorld,particleType,player,hits,effectRange,0);
   
      addEnergy(itemStack,-energy);
   
      if(energy/20 != getEnergy(itemStack)/20){
         energy = getEnergy(itemStack);
         StringBuilder message = new StringBuilder("Cinders: ");
         for(int i = 1; i <= getMaxEnergy(itemStack)/20; i++){
            message.append(energy >= i * 20 ? "✦ " : "✧ ");
         }
         player.sendMessage(Text.literal(message.toString()).formatted(color), true);
      }
      
      return TypedActionResult.success(itemStack);
   }
   
   private TypedActionResult<ItemStack> toggleActive(ServerPlayerEntity player, ItemStack item){
      NbtCompound itemNbt = item.getNbt();
      NbtCompound magicNbt = itemNbt.getCompound("arcananovum");
      boolean active = !magicNbt.getBoolean("active");
      boolean cremation = ArcanaAugments.getAugmentOnItem(item,"cremation") >= 1;
      Formatting color = cremation ? Formatting.AQUA : Formatting.RED;
      magicNbt.putBoolean("active",active);
      itemNbt.put("arcananovum",magicNbt);
      item.setNbt(itemNbt);
      if(active){
         player.sendMessage(Text.literal("The Charm's Heat Intensifies").formatted(color,Formatting.ITALIC),true);
         SoundUtils.playSongToPlayer(player, SoundEvents.ENTITY_BLAZE_AMBIENT, .5f,1f);
      }else{
         player.sendMessage(Text.literal("The Charm's Heat Calms").formatted(color,Formatting.ITALIC),true);
         SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_FIRE_EXTINGUISH, .3f,.8f);
      }
      return TypedActionResult.success(item);
   }
   
   private boolean inCone(PlayerEntity user, Vec3d targetPos){
      // Delicious trigonometry and linear algebra at its finest
      Vec3d origin = user.getEyePos().add(user.getRotationVecClient().multiply(-ri*Math.cos(ha)));
      Vec3d u = user.getEyePos().subtract(origin).normalize(); // Linear algebra black magic stuff which
      Vec3d uvr = targetPos.subtract(origin).normalize();      // finds the angle between cone axis and target
      double targetAngle = Math.acos(uvr.dotProduct(u));
      double dist = targetPos.distanceTo(origin);
      double scalProj = targetPos.subtract(user.getEyePos()).dotProduct(user.getRotationVecClient().normalize()); // Scalar projection to see if target is in front of player
      boolean inAngle = targetAngle <= ha;
      boolean inRadius = dist <= ro;
      boolean inFront = scalProj > 0;
      
      return inAngle && inRadius && inFront;
   }
   
   @Override
   public int getMaxEnergy(ItemStack item){
      int wildfireLevel = Math.max(0,ArcanaAugments.getAugmentOnItem(item,ArcanaAugments.WILDFIRE.id));
      return 100 + 20*wildfireLevel;
   }
   
   @Override
   public ItemStack updateItem(ItemStack stack, MinecraftServer server){
      NbtCompound itemNbt = stack.getNbt();
      NbtCompound magicTag = itemNbt.getCompound("arcananovum");
      boolean active = magicTag.getBoolean("active");
      NbtCompound newTag = super.updateItem(stack,server).getNbt();
      newTag.getCompound("arcananovum").putBoolean("active",active);
      stack.setNbt(newTag);
      return buildItemLore(stack,server);
   }
   
   private MagicItemRecipe makeRecipe(){
      MagicItemIngredient a = new MagicItemIngredient(Items.MAGMA_CREAM,64,null);
      MagicItemIngredient b = new MagicItemIngredient(Items.BLAZE_ROD,64,null);
      MagicItemIngredient c = new MagicItemIngredient(Items.FIRE_CHARGE,64,null);
      MagicItemIngredient g = new MagicItemIngredient(Items.NETHER_STAR,4,null);
      MagicItemIngredient h = new MagicItemIngredient(Items.NETHERITE_INGOT,2,null);
      MagicItemIngredient m = new MagicItemIngredient(Items.COAL_BLOCK,64,null);
   
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
      list.add("{\"text\":\"   Charm of Cinders\\n\\nRarity: Legendary\\n\\nHaving spent much time in the Nether has given me ample opportunity to study the fire dwelling creatures. \\nI believe I can replicate many of their abilities and even make my own.\"}");
      list.add("{\"text\":\"   Charm of Cinders\\n\\nThis charm grants a variety of skills from base fire immunity, to a simple flint and steel, to flaming breath and even the precision needed to smelt items as I pick them up in an instant.\\n\\nLeft Clicking mimics a flint and steel and\"}");
      list.add("{\"text\":\"   Charm of Cinders\\n\\ncan even set creatures ablaze.\\n\\nRight Click sends a cone of flame out of the charm igniting creatures.\\n\\nSneak Right Clicking toggles the auto-smelt ability for gathered items.\"}");
      return list;
   }
   
   public class CindersCharmItem extends MagicPolymerItem {
      public CindersCharmItem(Settings settings){
         super(getThis(),settings);
      }
      
      @Override
      public int getPolymerCustomModelData(ItemStack itemStack, @Nullable ServerPlayerEntity player){
         if(!MagicItemUtils.isMagic(itemStack)) return ArcanaRegistry.MODELS.get(TXT_OFF).value();
         NbtCompound magicNbt = itemStack.getNbt().getCompound("arcananovum");
         boolean active = magicNbt.getBoolean("active");;
         boolean cremation = ArcanaAugments.getAugmentOnItem(itemStack,ArcanaAugments.CREMATION.id) >= 1;
         if(cremation){
            return active ? ArcanaRegistry.MODELS.get(TXT_CREMATION_ON).value() : ArcanaRegistry.MODELS.get(TXT_CREMATION_OFF).value();
         }else{
            return active ? ArcanaRegistry.MODELS.get(TXT_ON).value() : ArcanaRegistry.MODELS.get(TXT_OFF).value();
         }
      }
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
      
      @Override
      public TypedActionResult<ItemStack> use(World world, PlayerEntity playerEntity, Hand hand){
         if(playerEntity.isSneaking()){
            return toggleActive((ServerPlayerEntity) playerEntity,playerEntity.getStackInHand(hand));
         }else{
            ItemStack itemStack = playerEntity.getStackInHand(hand);
            int pyroblast = Math.max(0,ArcanaAugments.getAugmentOnItem(itemStack,"pyroblast"));
            int fireweb = Math.max(0,ArcanaAugments.getAugmentOnItem(itemStack,"web_of_fire"));
            if(pyroblast > 0){
               return pyroblast(playerEntity, world, itemStack,pyroblast);
            }else if(fireweb > 0){
               return fireweb(playerEntity, world, itemStack,fireweb);
            }else{
               return coneOfFlame(playerEntity, world, hand);
            }
         }
      }
      
      @Override
      public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker){
         if(!(attacker instanceof ServerPlayerEntity player)) return false;
         
         boolean cremation = ArcanaAugments.getAugmentOnItem(stack,"cremation") >= 1;
         Formatting color = cremation ? Formatting.AQUA : Formatting.RED;
         
         if(getEnergy(stack) < 5) {
            player.sendMessage(Text.literal("The Charm has no Cinders").formatted(color), true);
            return true;
         }
         
         if(target instanceof CreeperEntity creeper){
            creeper.ignite();
            PLAYER_DATA.get(player).addXP(50); // Add xp
         }else{
            target.setOnFireFor(5);
            PLAYER_DATA.get(player).addXP(15); // Add xp
         }
         
         SoundUtils.playSound(player.getServerWorld(),target.getBlockPos(),SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 1.0F, player.getServerWorld().getRandom().nextFloat() * 0.4F + 0.8F);
         addEnergy(stack, -5);
         String message = "Cinders: ";
         for(int i=1; i<=getMaxEnergy(stack)/20; i++){
            message += getEnergy(stack) >= i*20 ? "✦ " : "✧ ";
         }
         player.sendMessage(Text.literal(message.toString()).formatted(color), true);
         return true;
      }
      
      @Override
      public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected){
         if(!MagicItemUtils.isMagic(stack)) return;
         if(!(world instanceof ServerWorld && entity instanceof ServerPlayerEntity player)) return;
         boolean cremation = ArcanaAugments.getAugmentOnItem(stack,"cremation") >= 1;
         Formatting color = cremation ? Formatting.AQUA : Formatting.RED;
         int oldEnergy = getEnergy(stack);
         if(oldEnergy < getMaxEnergy(stack) && world.getServer().getTicks() % 15 == 0){
            int bonusEnergy = ArcanaAugments.getAugmentOnItem(stack,"wildfire") == 5 ? 7 : 0;
            addEnergy(stack,3 + bonusEnergy);
            int newEnergy = getEnergy(stack);
            
            if(oldEnergy/20 != newEnergy/20){
               StringBuilder message = new StringBuilder("Cinders: ");
               for(int i=1; i<=getMaxEnergy(stack)/20; i++){
                  message.append(newEnergy >= i * 20 ? "✦ " : "✧ ");
               }
               player.sendMessage(Text.literal(message.toString().toString()).formatted(color), true);
            }
         }
         if(world.getServer().getTicks() % 20 == 0 && !cremation){
            StatusEffectInstance fireRes = new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 100, 0, false, false, false);
            player.addStatusEffect(fireRes);
            if(player.isOnFire()){
               player.extinguish();
            }
         }
      }
   }
}
