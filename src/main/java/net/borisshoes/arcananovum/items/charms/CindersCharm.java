package net.borisshoes.arcananovum.items.charms;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.core.EnergyItem;
import net.borisshoes.arcananovum.core.LeftClickItem;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerItem;
import net.borisshoes.arcananovum.gui.arcanetome.ArcaneTomeGui;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaEffectUtils;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.borislib.utils.SoundUtils;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.Tuple;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class CindersCharm extends EnergyItem implements LeftClickItem {
	public static final String ID = "cinders_charm";
   
   private final double range = 7.0;
   private final double closeW = 2.5;
   private final double farW = 6.5;
   // Delicious trigonometry at its finest
   private final double angle = 2*Math.atan2((.5*(farW-closeW)),range);
   private final double ha = angle/2;
   private final double ri = closeW / (2*Math.sin(ha)); // Cone characteristics from given parameters
   private final double ro = farW / (2*Math.sin(ha));
   
   public CindersCharm(){
      id = ID;
      name = "Charm of Cinders";
      initEnergy = 100;
      rarity = ArcanaRarity.SOVEREIGN;
      categories = new ArcaneTomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), ArcaneTomeGui.TomeFilter.CHARMS, ArcaneTomeGui.TomeFilter.ITEMS};
      itemVersion = 1;
      vanillaItem = Items.BLAZE_POWDER;
      item = new CindersCharmItem();
      displayName = Component.translatableWithFallback("item."+MOD_ID+"."+ID,name).withStyle(ChatFormatting.BOLD, ChatFormatting.GOLD);
      researchTasks = new ResourceKey[]{ResearchTasks.OBTAIN_NETHERITE_INGOT,ResearchTasks.KILL_BLAZE,ResearchTasks.KILL_MAGMA_CUBE,ResearchTasks.EFFECT_FIRE_RESISTANCE,ResearchTasks.USE_FLINT_AND_STEEL,ResearchTasks.UNLOCK_STELLAR_CORE};
      attributions = new Tuple[]{new Tuple<>(Component.translatable("credits_and_attribution.arcananovum.inspired_by"), Component.literal("sarhecker"))};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getDefaultMaxStackSize());
      putProperty(stack,ACTIVE_TAG, false);
      setPrefStack(stack);
   }
   
   @Override
   public List<Component> getItemLore(@Nullable ItemStack itemStack){
      List<MutableComponent> lore = new ArrayList<>();
      lore.add(Component.literal("")
            .append(Component.literal("The ").withStyle(ChatFormatting.RED))
            .append(Component.literal("charm ").withStyle(ChatFormatting.GOLD))
            .append(Component.literal("burns ").withStyle(ChatFormatting.DARK_RED))
            .append(Component.literal("with ").withStyle(ChatFormatting.RED))
            .append(Component.literal("focused intensity").withStyle(ChatFormatting.DARK_RED))
            .append(Component.literal(".").withStyle(ChatFormatting.RED)));
      lore.add(Component.literal("")
            .append(Component.literal("Flames ").withStyle(ChatFormatting.GOLD))
            .append(Component.literal("welcome you with a ").withStyle(ChatFormatting.RED))
            .append(Component.literal("warm embrace").withStyle(ChatFormatting.GOLD))
            .append(Component.literal(".").withStyle(ChatFormatting.RED)));
      lore.add(Component.literal("")
            .append(Component.literal("Left Click").withStyle(ChatFormatting.DARK_RED))
            .append(Component.literal(" a block or creature to set it ").withStyle(ChatFormatting.RED))
            .append(Component.literal("ablaze").withStyle(ChatFormatting.GOLD))
            .append(Component.literal(".").withStyle(ChatFormatting.RED)));
      lore.add(Component.literal("")
            .append(Component.literal("Hold Right Click").withStyle(ChatFormatting.DARK_RED))
            .append(Component.literal(" to ").withStyle(ChatFormatting.RED))
            .append(Component.literal("breathe ").withStyle(ChatFormatting.GOLD))
            .append(Component.literal("a ").withStyle(ChatFormatting.RED))
            .append(Component.literal("cone of fire").withStyle(ChatFormatting.GOLD))
            .append(Component.literal(" in front of you.").withStyle(ChatFormatting.RED)));
      lore.add(Component.literal("")
            .append(Component.literal("Sneak Right Click").withStyle(ChatFormatting.DARK_RED))
            .append(Component.literal(" to toggle ").withStyle(ChatFormatting.RED))
            .append(Component.literal("auto-smelting").withStyle(ChatFormatting.GOLD))
            .append(Component.literal(" of picked up items.").withStyle(ChatFormatting.RED)));
     return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   public boolean blocksHandInteractions(ItemStack item){
      return true;
   }
   
   @Override
   public boolean attackBlock(Player playerEntity, Level world, InteractionHand hand, BlockPos blockPos, Direction direction){
      ItemStack itemStack = playerEntity.getItemInHand(hand);
      BlockState blockState = world.getBlockState(blockPos);
      boolean cremation = ArcanaAugments.getAugmentOnItem(itemStack,ArcanaAugments.CREMATION.id) >= 1;
      ChatFormatting color = cremation ? ChatFormatting.AQUA : ChatFormatting.RED;
      
      if(blockState.is(Blocks.FIRE) || blockState.is(Blocks.SOUL_FIRE)){
         if(playerEntity instanceof ServerPlayer player){
            player.connection.send(new ClientboundBlockUpdatePacket(blockPos,blockState));
         }
         return false;
      }
      int cinderConsumption = ArcanaAugments.getAugmentOnItem(itemStack,ArcanaAugments.FIRESTARTER.id) >= 1 ? 0 : 5;
      if(getEnergy(itemStack) < cinderConsumption){
         playerEntity.displayClientMessage(Component.literal("The Charm has no Cinders").withStyle(color), true);
         return true;
      }
      
      if(!CampfireBlock.canLight(blockState) && !CandleBlock.canLight(blockState) && !CandleCakeBlock.canLight(blockState)){
         BlockPos blockPos2 = blockPos.relative(direction);
         if(blockState.is(Blocks.TNT)){
            TntBlock.prime(world,blockPos);
            world.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 11);
            
            addEnergy(itemStack, -cinderConsumption);
            String message = "Cinders: ";
            for(int i=1; i<=getMaxEnergy(itemStack)/20; i++){
               message += getEnergy(itemStack) >= i*20 ? "✦ " : "✧ ";
            }
            playerEntity.displayClientMessage(Component.literal(message.toString()).withStyle(color), true);
            
            if(playerEntity instanceof ServerPlayer player){
               ArcanaNovum.data(player).addXP(ArcanaNovum.CONFIG.getInt(ArcanaRegistry.XP_CINDERS_CHARM_IGNITE_TNT)*cinderConsumption); // Add xp
            }
            
            return !playerEntity.isCreative();
         }else if(BaseFireBlock.canBePlacedAt(world, blockPos2, direction)){
            SoundUtils.playSound(world,blockPos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F, world.getRandom().nextFloat() * 0.4F + 0.8F);
            BlockState blockState2 = BaseFireBlock.getState(world, blockPos2);
            world.setBlock(blockPos2, blockState2, 11);
            world.gameEvent(playerEntity, GameEvent.BLOCK_PLACE, blockPos);
            
            addEnergy(itemStack, -cinderConsumption);
            String message = "Cinders: ";
            for(int i=1; i<=getMaxEnergy(itemStack)/20; i++){
               message += getEnergy(itemStack) >= i*20 ? "✦ " : "✧ ";
            }
            playerEntity.displayClientMessage(Component.literal(message.toString()).withStyle(color), true);
            
            if(playerEntity instanceof ServerPlayer player){
               ArcanaNovum.data(player).addXP(ArcanaNovum.CONFIG.getInt(ArcanaRegistry.XP_CINDERS_CHARM_IGNITE_BLOCK)*cinderConsumption); // Add xp
            }
            
            return !playerEntity.isCreative();
         }else{
            return !playerEntity.isCreative();
         }
      }else{
         if(CandleCakeBlock.canLight(blockState) && playerEntity instanceof ServerPlayer player) ArcanaAchievements.grant(player,ArcanaAchievements.CAKE_DAY.id);
         SoundUtils.playSound(world,blockPos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F, world.getRandom().nextFloat() * 0.4F + 0.8F);
         world.setBlock(blockPos, (BlockState)blockState.setValue(BlockStateProperties.LIT, true), 11);
         world.gameEvent(playerEntity, GameEvent.BLOCK_CHANGE, blockPos);
         
         addEnergy(itemStack, -cinderConsumption);
         String message = "Cinders: ";
         for(int i=1; i<=getMaxEnergy(itemStack)/20; i++){
            message += getEnergy(itemStack) >= i*20 ? "✦ " : "✧ ";
         }
         playerEntity.displayClientMessage(Component.literal(message.toString()).withStyle(color), true);
         
         if(playerEntity instanceof ServerPlayer player){
            ArcanaNovum.data(player).addXP(ArcanaNovum.CONFIG.getInt(ArcanaRegistry.XP_CINDERS_CHARM_LIGHT_BLOCK)*cinderConsumption); // Add xp
         }
         
         return !playerEntity.isCreative();
      }
   }
   
   public ItemStack smelt(ItemStack charm, Player player, ItemStack stack){
      if(!(player.level() instanceof ServerLevel serverWorld)) return null;
      try{
         boolean active = getBooleanProperty(charm,ACTIVE_TAG);
         boolean cremation = ArcanaAugments.getAugmentOnItem(charm,ArcanaAugments.CREMATION.id) >= 1;
         boolean smelter = ArcanaAugments.getAugmentOnItem(charm,ArcanaAugments.SUPERSMELTER.id) >= 1;
         ChatFormatting color = cremation ? ChatFormatting.AQUA : ChatFormatting.RED;
         int energyToConsume = (int)Math.ceil(stack.getCount() / (smelter ? 8.0 : 2.0));
         if(active && getEnergy(charm) >= energyToConsume){
            // Smelting registry and auto smelt
            
            RecipeManager.CachedCheck<SingleRecipeInput, ? extends AbstractCookingRecipe> matchGetter = RecipeManager.createCheck(RecipeType.SMELTING);
            RecipeHolder<? extends AbstractCookingRecipe> recipeEntry = matchGetter.getRecipeFor(new SingleRecipeInput(stack),serverWorld).orElse(null);
            if(recipeEntry == null) return null;
            AbstractCookingRecipe recipe = recipeEntry.value();
            if(recipe == null) return null;
            ItemStack recipeOutput = recipe.assemble(new SingleRecipeInput(stack),serverWorld.registryAccess());
            if(recipeOutput.isEmpty()) return null;
            Inventory inv = player.getInventory();
            ItemStack result = recipeOutput.copy();
            
            if(recipeOutput.getCount()*stack.getCount() <= recipeOutput.getItem().getDefaultMaxStackSize()){
               result.setCount(recipeOutput.getCount()*stack.getCount());
               if(inv.getSlotWithRemainingSpace(result) == -1 && inv.getFreeSlot() == -1) return null;
               
               player.giveExperiencePoints(Mth.floor(recipe.experience()*stack.getCount()));
               
               int oldEnergy = getEnergy(charm);
               addEnergy(charm, -energyToConsume);
               int newEnergy = getEnergy(charm);
               if(oldEnergy/20 != newEnergy/20){
                  String message = "Cinders: ";
                  for(int i=1; i<=getMaxEnergy(charm)/20; i++){
                     message += newEnergy >= i*20 ? "✦ " : "✧ ";
                  }
                  player.displayClientMessage(Component.literal(message.toString()).withStyle(color), true);
               }
               stack = result;
               
               if(player instanceof ServerPlayer serverPlayer){
                  ArcanaNovum.data(serverPlayer).addXP(ArcanaNovum.CONFIG.getInt(ArcanaRegistry.XP_CINDERS_CHARM_SMELT_PER_CINDER)*energyToConsume); // Add xp
                  if(recipeOutput.is(Items.GLASS)) ArcanaAchievements.progress(serverPlayer,ArcanaAchievements.GLASSBLOWER.id,stack.getCount());
               }
               return stack;
            }
         }
      }catch(Exception e){
         e.printStackTrace();
      }
      return null;
   }
   
   private InteractionResult coneOfFlame(Player playerEntity, Level world, InteractionHand hand){
      ItemStack itemStack = playerEntity.getItemInHand(hand);
      if(!(world instanceof ServerLevel serverWorld)) return InteractionResult.PASS;
      
      int energy = getEnergy(itemStack);
      boolean cremation = ArcanaAugments.getAugmentOnItem(itemStack,ArcanaAugments.CREMATION.id) >= 1;
      SimpleParticleType particleType = cremation ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.FLAME;
      ChatFormatting color = cremation ? ChatFormatting.AQUA : ChatFormatting.RED;
      
      if(energy < 12){
         playerEntity.displayClientMessage(Component.literal("The Charm has no Cinders").withStyle(color), true);
         return InteractionResult.PASS;
      }
      addEnergy(itemStack,-3);
      
      if(energy/20 != getEnergy(itemStack)/20){
         energy = getEnergy(itemStack);
         StringBuilder message = new StringBuilder("Cinders: ");
         for(int i = 1; i <= getMaxEnergy(itemStack)/20; i++){
            message.append(energy >= i * 20 ? "✦ " : "✧ ");
         }
         playerEntity.displayClientMessage(Component.literal(message.toString()).withStyle(color), true);
      }
      
      double mul = 1.5*range;
      Vec3 boxStart = playerEntity.position().subtract(mul,mul,mul);
      Vec3 boxEnd = playerEntity.position().add(mul,mul,mul);
      AABB rangeBox = new AABB(boxStart,boxEnd);
      
      SoundUtils.playSound(world, playerEntity.blockPosition(), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 0.6f, (float) (Math.random() * .5 + .5));
      
      List<Entity> entities = serverWorld.getEntities(playerEntity,rangeBox, e -> e instanceof LivingEntity);
      int ignited = 0;
      for(Entity e : entities){
         if(!(e instanceof LivingEntity entity)) continue;
         if(inCone(playerEntity,e.getEyePosition())){
            if(!entity.fireImmune()){
               entity.igniteForSeconds((2*energy+60)/20);
               entity.hurtServer(serverWorld, new DamageSource(entity.damageSources().onFire().typeHolder(),playerEntity),cremation ? 5f : 2.5f);
               if(entity instanceof Mob) ignited++;
               
               if(playerEntity instanceof ServerPlayer serverPlayer){
                  ArcanaNovum.data(serverPlayer).addXP(ArcanaNovum.CONFIG.getInt(ArcanaRegistry.XP_CINDERS_CHARM_CONE_PER_TARGET)); // Add xp
               }
            }
         }
      }
      if(playerEntity instanceof ServerPlayer serverPlayer && ignited >= 12){
         ArcanaAchievements.grant(serverPlayer,ArcanaAchievements.PYROMANIAC.id);
      }
      
      
      double angle = 2*Math.atan2((.5*(farW-closeW)),range);
      float yaw = playerEntity.getRotationVector().y;
      Vec3 rot = Vec3.directionFromRotation(0,yaw);
      double pC = Math.atan2(rot.z,rot.x);
      Vec3 rotVec = Vec3.directionFromRotation(60,yaw).cross(Vec3.directionFromRotation(0,yaw)).normalize();
      Vec3 origin = playerEntity.getEyePosition().add(playerEntity.getForward().scale(-ri*Math.cos(ha)));
      
      for(int i = 0; i < 40; i++){
         int tries = 0;
         Vec3 pos;
         do{
            double pD = (Math.random()*angle - angle/2);
            Vec3 offset = new Vec3(Math.cos(pC+pD),0,Math.sin(pC+pD)).scale(Math.random()*ro);
            
            float dT = (float) (Math.toRadians(-playerEntity.getRotationVector().x)+ (Math.random()*angle - angle/2));
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
            
            offset = new Vec3(nX,nY,nZ);
            pos = offset.add(origin);
            
            tries++;
         }while(!inCone(playerEntity,pos) && tries < 12);
         
         serverWorld.sendParticles(particleType,pos.x(),pos.y(),pos.z(),1,0.1,0.1,0.1,0);
      }
      return InteractionResult.SUCCESS_SERVER;
   }
   
   private InteractionResult pyroblast(Player playerEntity, Level world, ItemStack itemStack, int lvl){
      if(!(world instanceof ServerLevel serverWorld && playerEntity instanceof ServerPlayer player)) return InteractionResult.PASS;
      
      int energy = getEnergy(itemStack);
      boolean cremation = ArcanaAugments.getAugmentOnItem(itemStack,ArcanaAugments.CREMATION.id) >= 1;
      SimpleParticleType particleType = cremation ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.FLAME;
      ChatFormatting color = cremation ? ChatFormatting.AQUA : ChatFormatting.RED;
      
      if(energy < 50){
         player.displayClientMessage(Component.literal("Not Enough Cinders").withStyle(color), true);
         return InteractionResult.PASS;
      }
      int consumedEnergy = energy;
      addEnergy(itemStack,-energy);
      
      if(energy/20 != getEnergy(itemStack)/20){
         energy = getEnergy(itemStack);
         StringBuilder message = new StringBuilder("Cinders: ");
         for(int i = 1; i <= getMaxEnergy(itemStack)/20; i++){
            message.append(energy >= i * 20 ? "✦ " : "✧ ");
         }
         player.displayClientMessage(Component.literal(message.toString()).withStyle(color), true);
      }
      
      Vec3 startPos = player.getEyePosition();
      Vec3 view = player.getForward();
      Vec3 rayEnd = startPos.add(view.scale(35));
      BlockHitResult raycast = world.clip(new ClipContext(startPos,rayEnd, ClipContext.Block.COLLIDER, ClipContext.Fluid.ANY,player));
      Vec3 center = raycast.getLocation();
      double explosionRange = 3+lvl;
      AABB rangeBox = new AABB(center.x+12,center.y+12,center.z+12,center.x-12,center.y-12,center.z-12);
      List<Entity> entities = world.getEntities((Entity) null,rangeBox, e -> !e.isSpectator() && e.distanceToSqr(center) < 1.25*explosionRange*explosionRange && e instanceof LivingEntity);
      
      SoundUtils.playSound(world, playerEntity.blockPosition(), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 0.6f, 1.7f);
      
      int ignited = 0;
      for(Entity e : entities){
         if(!(e instanceof LivingEntity entity)) continue;
         if(!entity.fireImmune()){
            float dmg = (float) (Math.max(0,(1.2 - (entity.position().distanceTo(center)/explosionRange))) * (consumedEnergy/10.0) * (.8+lvl*.2));
            entity.igniteForSeconds((float) consumedEnergy /20);
            entity.hurtServer(serverWorld, new DamageSource(entity.damageSources().onFire().typeHolder(),playerEntity),cremation ? 2*dmg : dmg);
            ignited++;
            
            ArcanaNovum.data(player).addXP(ArcanaNovum.CONFIG.getInt(ArcanaRegistry.XP_CINDERS_CHARM_PYROBLAST_PER_TARGET)); // Add xp
         }
      }
      
      if(ignited >= 12){
         ArcanaAchievements.grant(player,ArcanaAchievements.PYROMANIAC.id);
      }
      
      ArcanaEffectUtils.pyroblastExplosion(serverWorld,particleType,center,explosionRange,0);
      ArcanaEffectUtils.line(serverWorld,null,startPos.subtract(0,.3,0),center,particleType,(int)(center.distanceTo(startPos)*4),1,0,0);
      serverWorld.sendParticles(particleType,center.x(),center.y(),center.z(),100,0.1,0.1,0.1,0.4);
      
      return InteractionResult.SUCCESS_SERVER;
   }
   
   private InteractionResult fireweb(Player playerEntity, Level world, ItemStack itemStack, int lvl){
      if(!(world instanceof ServerLevel serverWorld && playerEntity instanceof ServerPlayer player)) return InteractionResult.PASS;
      
      int energy = getEnergy(itemStack);
      boolean cremation = ArcanaAugments.getAugmentOnItem(itemStack,ArcanaAugments.CREMATION.id) >= 1;
      SimpleParticleType particleType = cremation ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.FLAME;
      ChatFormatting color = cremation ? ChatFormatting.AQUA : ChatFormatting.RED;
      
      if(energy < 50){
         player.displayClientMessage(Component.literal("Not Enough Cinders").withStyle(color), true);
         return InteractionResult.PASS;
      }
      int consumedEnergy = energy;
      
      Vec3 center = player.position();
      double effectRange = 2+lvl*2;
      int numTargets = 5*lvl;
      AABB rangeBox = new AABB(center.x+12,center.y+12,center.z+12,center.x-12,center.y-12,center.z-12);
      List<Entity> entities = world.getEntities(player,rangeBox, e -> !e.isSpectator() && e.distanceToSqr(center) < 1.25*effectRange*effectRange && e instanceof LivingEntity);
      entities.sort(Comparator.comparingDouble(e->e.distanceTo(player)));
      
      if(entities.isEmpty()){
         SoundUtils.playSongToPlayer(player, SoundEvents.FIRE_EXTINGUISH, .3f,.8f);
         player.displayClientMessage(Component.literal("No Targets in Range").withStyle(color), true);
         return InteractionResult.PASS;
      }
      
      SoundUtils.playSound(world, playerEntity.blockPosition(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1f, 0.5f);
      
      List<LivingEntity> hits = new ArrayList<>();
      for(Entity e : entities){
         if(!(e instanceof LivingEntity entity)) continue;
         if(!entity.fireImmune()){
            float dmg = (float) ((consumedEnergy/15.0) * (.8+lvl*.2));
            entity.igniteForSeconds((float) consumedEnergy /20);
            entity.hurtServer(serverWorld, new DamageSource(entity.damageSources().onFire().typeHolder(),playerEntity),cremation ? 2*dmg : dmg);
            
            ArcanaNovum.data(player).addXP(ArcanaNovum.CONFIG.getInt(ArcanaRegistry.XP_CINDERS_CHARM_WEB_PER_TARGET)); // Add xp
            hits.add(entity);
         }
         if(hits.size() >= numTargets) break;
      }
      
      if(hits.size() >= 12){
         ArcanaAchievements.grant(player,ArcanaAchievements.PYROMANIAC.id);
      }
      
      ArcanaEffectUtils.webOfFireCast(serverWorld,particleType,player,hits,effectRange,0);
      
      addEnergy(itemStack,-energy);
      
      if(energy/20 != getEnergy(itemStack)/20){
         energy = getEnergy(itemStack);
         StringBuilder message = new StringBuilder("Cinders: ");
         for(int i = 1; i <= getMaxEnergy(itemStack)/20; i++){
            message.append(energy >= i * 20 ? "✦ " : "✧ ");
         }
         player.displayClientMessage(Component.literal(message.toString()).withStyle(color), true);
      }
      
      return InteractionResult.SUCCESS_SERVER;
   }
   
   private InteractionResult toggleActive(ServerPlayer player, ItemStack item){
      boolean active = !getBooleanProperty(item,ACTIVE_TAG);
      boolean cremation = ArcanaAugments.getAugmentOnItem(item,ArcanaAugments.CREMATION.id) >= 1;
      ChatFormatting color = cremation ? ChatFormatting.AQUA : ChatFormatting.RED;
      putProperty(item,ACTIVE_TAG, active);
      if(active){
         player.displayClientMessage(Component.literal("The Charm's Heat Intensifies").withStyle(color, ChatFormatting.ITALIC),true);
         SoundUtils.playSongToPlayer(player, SoundEvents.BLAZE_AMBIENT, .5f,1f);
      }else{
         player.displayClientMessage(Component.literal("The Charm's Heat Calms").withStyle(color, ChatFormatting.ITALIC),true);
         SoundUtils.playSongToPlayer(player, SoundEvents.FIRE_EXTINGUISH, .3f,.8f);
      }
      return InteractionResult.SUCCESS_SERVER;
   }
   
   private boolean inCone(Player user, Vec3 targetPos){
      // Delicious trigonometry and linear algebra at its finest
      Vec3 origin = user.getEyePosition().add(user.getForward().scale(-ri*Math.cos(ha)));
      Vec3 u = user.getEyePosition().subtract(origin).normalize(); // Linear algebra black magic stuff which
      Vec3 uvr = targetPos.subtract(origin).normalize();      // finds the angle between cone axis and target
      double targetAngle = Math.acos(uvr.dot(u));
      double dist = targetPos.distanceTo(origin);
      double scalProj = targetPos.subtract(user.getEyePosition()).dot(user.getForward().normalize()); // Scalar projection to see if target is in front of player
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
      boolean active = getBooleanProperty(stack,ACTIVE_TAG);
      ItemStack newStack = super.updateItem(stack,server);
      putProperty(newStack,ACTIVE_TAG, active);
      return buildItemLore(newStack,server);
   }
   
   @Override
   public List<List<Component>> getBookLore(){
      List<List<Component>> list = new ArrayList<>();
      list.add(List.of(Component.literal(" Charm of Cinders").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), Component.literal("\nRarity: ").withStyle(ChatFormatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(),false)), Component.literal("\nHaving spent much time in the Nether has given me ample opportunity to study the fire dwelling creatures. I believe I can replicate many of their abilities and make them my own.").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal(" Charm of Cinders").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), Component.literal("\nThis Charm grants a variety of skills from base fire immunity to a simple flint and steel, to flaming breath and even the precise heat to smelt items as I pick them up.\n\nPunching mimics a flint and steel and can ").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal(" Charm of Cinders").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), Component.literal("\neven set creatures ablaze.\n\nUsing the Charm sends a cone of flame out in a wide area, igniting creatures.\n\nSneak Using it toggles the auto-smelt ability for gathered items.\n").withStyle(ChatFormatting.BLACK)));
      return list;
   }
   
   public class CindersCharmItem extends ArcanaPolymerItem {
      public CindersCharmItem(){
         super(getThis());
      }
      
      @Override
      public ItemStack getPolymerItemStack(ItemStack itemStack, TooltipFlag tooltipType, PacketContext context){
         ItemStack baseStack = super.getPolymerItemStack(itemStack, tooltipType, context);
         if(!ArcanaItemUtils.isArcane(itemStack)) return baseStack;
         boolean active = getBooleanProperty(itemStack,ACTIVE_TAG);
         boolean cremation = ArcanaAugments.getAugmentOnItem(itemStack,ArcanaAugments.CREMATION.id) >= 1;
         
         List<String> stringList = new ArrayList<>();
         if(active){
            if(cremation){
               stringList.add("cremation_on");
            }else{
               stringList.add("on");
            }
         }else{
            if(cremation){
               stringList.add("cremation_off");
            }else{
               stringList.add("off");
            }
         }
         baseStack.set(DataComponents.CUSTOM_MODEL_DATA,new CustomModelData(new ArrayList<>(),new ArrayList<>(),stringList,new ArrayList<>()));
         return baseStack;
      }
      
      @Override
      public ItemStack getDefaultInstance(){
         return prefItem;
      }
      
      @Override
      public InteractionResult use(Level world, Player playerEntity, InteractionHand hand){
         if(playerEntity.isShiftKeyDown()){
            return toggleActive((ServerPlayer) playerEntity,playerEntity.getItemInHand(hand));
         }else{
            ItemStack itemStack = playerEntity.getItemInHand(hand);
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
      public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker){
         if(!(attacker instanceof ServerPlayer player)) return;
         
         boolean cremation = ArcanaAugments.getAugmentOnItem(stack,"cremation") >= 1;
         ChatFormatting color = cremation ? ChatFormatting.AQUA : ChatFormatting.RED;
         
         if(getEnergy(stack) < 5){
            player.displayClientMessage(Component.literal("The Charm has no Cinders").withStyle(color), true);
            return;
         }
         
         if(target instanceof Creeper creeper){
            creeper.ignite();
            ArcanaNovum.data(player).addXP(ArcanaNovum.CONFIG.getInt(ArcanaRegistry.XP_CINDERS_CHARM_IGNITE_CREEPER)); // Add xp
         }else{
            target.igniteForSeconds(5);
            ArcanaNovum.data(player).addXP(ArcanaNovum.CONFIG.getInt(ArcanaRegistry.XP_CINDERS_CHARM_IGNITE_ENTITY)); // Add xp
         }
         
         SoundUtils.playSound(player.level(),target.blockPosition(), SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F, player.level().getRandom().nextFloat() * 0.4F + 0.8F);
         addEnergy(stack, -5);
         String message = "Cinders: ";
         for(int i=1; i<=getMaxEnergy(stack)/20; i++){
            message += getEnergy(stack) >= i*20 ? "✦ " : "✧ ";
         }
         player.displayClientMessage(Component.literal(message.toString()).withStyle(color), true);
         return;
      }
      
      @Override
      public void inventoryTick(ItemStack stack, ServerLevel world, Entity entity, @Nullable EquipmentSlot slot){
         if(!ArcanaItemUtils.isArcane(stack)) return;
         if(!(world instanceof ServerLevel && entity instanceof ServerPlayer player)) return;
         boolean cremation = ArcanaAugments.getAugmentOnItem(stack,"cremation") >= 1;
         ChatFormatting color = cremation ? ChatFormatting.AQUA : ChatFormatting.RED;
         int oldEnergy = getEnergy(stack);
         if(oldEnergy < getMaxEnergy(stack) && world.getServer().getTickCount() % 15 == 0){
            int bonusEnergy = ArcanaAugments.getAugmentOnItem(stack,"wildfire") == 5 ? 7 : 0;
            addEnergy(stack,3 + bonusEnergy);
            int newEnergy = getEnergy(stack);
            
            if(oldEnergy/20 != newEnergy/20){
               StringBuilder message = new StringBuilder("Cinders: ");
               for(int i=1; i<=getMaxEnergy(stack)/20; i++){
                  message.append(newEnergy >= i * 20 ? "✦ " : "✧ ");
               }
               player.displayClientMessage(Component.literal(message.toString().toString()).withStyle(color), true);
            }
         }
         if(world.getServer().getTickCount() % 20 == 0 && !cremation){
            MobEffectInstance fireRes = new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 100, 0, false, false, false);
            player.addEffect(fireRes);
            if(player.isOnFire()){
               player.clearFire();
            }
         }
      }
   }
}

