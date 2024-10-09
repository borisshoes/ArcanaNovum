package net.borisshoes.arcananovum.items.charms;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.EnergyItem;
import net.borisshoes.arcananovum.core.LeftClickItem;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerItem;
import net.borisshoes.arcananovum.gui.arcanetome.TomeGui;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaIngredient;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaRecipe;
import net.borisshoes.arcananovum.recipes.arcana.ForgeRequirement;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.*;
import net.minecraft.block.*;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.recipe.AbstractCookingRecipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.input.SingleStackRecipeInput;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Pair;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;
import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;

public class CindersCharm extends EnergyItem implements LeftClickItem {
	public static final String ID = "cinders_charm";
   
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
      id = ID;
      name = "Charm of Cinders";
      initEnergy = 100;
      rarity = ArcanaRarity.SOVEREIGN;
      categories = new TomeGui.TomeFilter[]{TomeGui.TomeFilter.SOVEREIGN, TomeGui.TomeFilter.CHARMS, TomeGui.TomeFilter.ITEMS};
      itemVersion = 1;
      vanillaItem = Items.BLAZE_POWDER;
      item = new CindersCharmItem(new Item.Settings().maxCount(1).fireproof()
            .component(DataComponentTypes.ITEM_NAME, Text.translatable("item."+MOD_ID+"."+ID).formatted(Formatting.BOLD,Formatting.GOLD))
            .component(DataComponentTypes.LORE, new LoreComponent(getItemLore(null)))
            .component(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true)
      );
      models = new ArrayList<>();
      models.add(new Pair<>(vanillaItem,TXT_ON));
      models.add(new Pair<>(vanillaItem,TXT_OFF));
      models.add(new Pair<>(vanillaItem,TXT_CREMATION_ON));
      models.add(new Pair<>(vanillaItem,TXT_CREMATION_OFF));
      researchTasks = new RegistryKey[]{ResearchTasks.OBTAIN_NETHERITE_INGOT,ResearchTasks.KILL_BLAZE,ResearchTasks.KILL_MAGMA_CUBE,ResearchTasks.EFFECT_FIRE_RESISTANCE,ResearchTasks.USE_FLINT_AND_STEEL,ResearchTasks.UNLOCK_STELLAR_CORE};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getMaxCount());
      putProperty(stack,ACTIVE_TAG, false);
      setPrefStack(stack);
   }
   
   @Override
   public List<Text> getItemLore(@Nullable ItemStack itemStack){
      List<MutableText> lore = new ArrayList<>();
      lore.add(Text.literal("")
            .append(Text.literal("The ").formatted(Formatting.RED))
            .append(Text.literal("charm ").formatted(Formatting.GOLD))
            .append(Text.literal("burns ").formatted(Formatting.DARK_RED))
            .append(Text.literal("with ").formatted(Formatting.RED))
            .append(Text.literal("focused intensity").formatted(Formatting.DARK_RED))
            .append(Text.literal(".").formatted(Formatting.RED)));
      lore.add(Text.literal("")
            .append(Text.literal("Flames ").formatted(Formatting.GOLD))
            .append(Text.literal("welcome you with a ").formatted(Formatting.RED))
            .append(Text.literal("warm embrace").formatted(Formatting.GOLD))
            .append(Text.literal(".").formatted(Formatting.RED)));
      lore.add(Text.literal("")
            .append(Text.literal("Left Click").formatted(Formatting.DARK_RED))
            .append(Text.literal(" a block or creature to set it ").formatted(Formatting.RED))
            .append(Text.literal("ablaze").formatted(Formatting.GOLD))
            .append(Text.literal(".").formatted(Formatting.RED)));
      lore.add(Text.literal("")
            .append(Text.literal("Hold Right Click").formatted(Formatting.DARK_RED))
            .append(Text.literal(" to ").formatted(Formatting.RED))
            .append(Text.literal("breathe ").formatted(Formatting.GOLD))
            .append(Text.literal("a ").formatted(Formatting.RED))
            .append(Text.literal("cone of fire").formatted(Formatting.GOLD))
            .append(Text.literal(" in front of you.").formatted(Formatting.RED)));
      lore.add(Text.literal("")
            .append(Text.literal("Sneak Right Click").formatted(Formatting.DARK_RED))
            .append(Text.literal(" to toggle ").formatted(Formatting.RED))
            .append(Text.literal("auto-smelting").formatted(Formatting.GOLD))
            .append(Text.literal(" of picked up items.").formatted(Formatting.RED)));
     return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
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
   
   public ItemStack smelt(ItemStack charm, PlayerEntity player, ItemStack stack){
      try{
         boolean active = getBooleanProperty(charm,ACTIVE_TAG);
         boolean cremation = ArcanaAugments.getAugmentOnItem(charm,ArcanaAugments.CREMATION.id) >= 1;
         Formatting color = cremation ? Formatting.AQUA : Formatting.RED;
         int energyToConsume = (int)Math.ceil(stack.getCount() / 2.0);
         if(active && getEnergy(charm) >= energyToConsume){
            // Smelting registry and auto smelt
            RecipeManager.MatchGetter<SingleStackRecipeInput, ? extends AbstractCookingRecipe> matchGetter = RecipeManager.createCachedMatchGetter(RecipeType.SMELTING);
            RecipeEntry<? extends AbstractCookingRecipe> recipeEntry = matchGetter.getFirstMatch(new SingleStackRecipeInput(stack),player.getEntityWorld()).orElse(null);
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
               
               int oldEnergy = getEnergy(charm);
               addEnergy(charm, -energyToConsume);
               int newEnergy = getEnergy(charm);
               if(oldEnergy/20 != newEnergy/20){
                  String message = "Cinders: ";
                  for(int i=1; i<=getMaxEnergy(charm)/20; i++){
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
   
   private ActionResult coneOfFlame(PlayerEntity playerEntity, World world, Hand hand){
      ItemStack itemStack = playerEntity.getStackInHand(hand);
      if(!(world instanceof ServerWorld serverWorld)) return ActionResult.PASS;
      
      int energy = getEnergy(itemStack);
      boolean cremation = ArcanaAugments.getAugmentOnItem(itemStack,ArcanaAugments.CREMATION.id) >= 1;
      SimpleParticleType particleType = cremation ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.FLAME;
      Formatting color = cremation ? Formatting.AQUA : Formatting.RED;
      
      if(energy < 12) {
         playerEntity.sendMessage(Text.literal("The Charm has no Cinders").formatted(color), true);
         return ActionResult.PASS;
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
               entity.damage(serverWorld, new DamageSource(entity.getDamageSources().onFire().getTypeRegistryEntry(),playerEntity),cremation ? 5f : 2.5f);
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
      return ActionResult.SUCCESS;
   }
   
   private ActionResult pyroblast(PlayerEntity playerEntity, World world, ItemStack itemStack, int lvl){
      if(!(world instanceof ServerWorld serverWorld && playerEntity instanceof ServerPlayerEntity player)) return ActionResult.PASS;
      
      int energy = getEnergy(itemStack);
      boolean cremation = ArcanaAugments.getAugmentOnItem(itemStack,ArcanaAugments.CREMATION.id) >= 1;
      SimpleParticleType particleType = cremation ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.FLAME;
      Formatting color = cremation ? Formatting.AQUA : Formatting.RED;
      
      if(energy < 50) {
         player.sendMessage(Text.literal("Not Enough Cinders").formatted(color), true);
         return ActionResult.PASS;
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
            entity.damage(serverWorld, new DamageSource(entity.getDamageSources().onFire().getTypeRegistryEntry(),playerEntity),cremation ? 2*dmg : dmg);
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
      
      return ActionResult.SUCCESS;
   }
   
   private ActionResult fireweb(PlayerEntity playerEntity, World world, ItemStack itemStack, int lvl){
      if(!(world instanceof ServerWorld serverWorld && playerEntity instanceof ServerPlayerEntity player)) return ActionResult.PASS;
      
      int energy = getEnergy(itemStack);
      boolean cremation = ArcanaAugments.getAugmentOnItem(itemStack,ArcanaAugments.CREMATION.id) >= 1;
      SimpleParticleType particleType = cremation ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.FLAME;
      Formatting color = cremation ? Formatting.AQUA : Formatting.RED;
      
      if(energy < 50) {
         player.sendMessage(Text.literal("Not Enough Cinders").formatted(color), true);
         return ActionResult.PASS;
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
         return ActionResult.PASS;
      }
      
      SoundUtils.playSound(world, playerEntity.getBlockPos(), SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.PLAYERS, 1f, 0.5f);
      
      List<LivingEntity> hits = new ArrayList<>();
      for(Entity e : entities){
         if(!(e instanceof LivingEntity entity)) continue;
         if(!entity.isFireImmune()){
            float dmg = (float) ((consumedEnergy/15.0) * (.8+lvl*.2));
            entity.setOnFireFor(consumedEnergy/20);
            entity.damage(serverWorld, new DamageSource(entity.getDamageSources().onFire().getTypeRegistryEntry(),playerEntity),cremation ? 2*dmg : dmg);
            
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
      
      return ActionResult.SUCCESS;
   }
   
   private ActionResult toggleActive(ServerPlayerEntity player, ItemStack item){
      boolean active = !getBooleanProperty(item,ACTIVE_TAG);
      boolean cremation = ArcanaAugments.getAugmentOnItem(item,ArcanaAugments.CREMATION.id) >= 1;
      Formatting color = cremation ? Formatting.AQUA : Formatting.RED;
      putProperty(item,ACTIVE_TAG, active);
      if(active){
         player.sendMessage(Text.literal("The Charm's Heat Intensifies").formatted(color,Formatting.ITALIC),true);
         SoundUtils.playSongToPlayer(player, SoundEvents.ENTITY_BLAZE_AMBIENT, .5f,1f);
      }else{
         player.sendMessage(Text.literal("The Charm's Heat Calms").formatted(color,Formatting.ITALIC),true);
         SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_FIRE_EXTINGUISH, .3f,.8f);
      }
      return ActionResult.SUCCESS;
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
      boolean active = getBooleanProperty(stack,ACTIVE_TAG);
      ItemStack newStack = super.updateItem(stack,server);
      putProperty(newStack,ACTIVE_TAG, active);
      return buildItemLore(newStack,server);
   }
   
   @Override
	protected ArcanaRecipe makeRecipe(){
      ArcanaIngredient a = new ArcanaIngredient(Items.NETHER_STAR,2);
      ArcanaIngredient b = new ArcanaIngredient(Items.BLAZE_ROD,24);
      ArcanaIngredient c = new ArcanaIngredient(Items.FIRE_CHARGE,32);
      ArcanaIngredient g = new ArcanaIngredient(Items.MAGMA_CREAM,32);
      ArcanaIngredient h = new ArcanaIngredient(Items.NETHERITE_INGOT,1);
      ArcanaIngredient m = new ArcanaIngredient(Items.COAL_BLOCK,32);
      
      ArcanaIngredient[][] ingredients = {
            {a,b,c,b,a},
            {b,g,h,g,b},
            {c,h,m,h,c},
            {b,g,h,g,b},
            {a,b,c,b,a}};
      return new ArcanaRecipe(ingredients,new ForgeRequirement().withCore().withAnvil());
   }
   
   @Override
   public List<List<Text>> getBookLore(){
      List<List<Text>> list = new ArrayList<>();
      list.add(List.of(Text.literal("   Charm of Cinders\n\nRarity: Sovereign\n\nHaving spent much time in the Nether has given me ample opportunity to study the fire dwelling creatures. \nI believe I can replicate many of their abilities and even make my own.").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal("   Charm of Cinders\n\nThis charm grants a variety of skills from base fire immunity, to a simple flint and steel, to flaming breath and even the precision needed to smelt items as I pick them up in an instant.\n\nLeft Clicking mimics a flint and steel and").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal("   Charm of Cinders\n\ncan even set creatures ablaze.\n\nRight Click sends a cone of flame out of the charm igniting creatures.\n\nSneak Right Clicking toggles the auto-smelt ability for gathered items.").formatted(Formatting.BLACK)));
      return list;
   }
   
   public class CindersCharmItem extends ArcanaPolymerItem {
      public CindersCharmItem(Item.Settings settings){
         super(getThis(),settings);
      }
      
      @Override
      public int getPolymerCustomModelData(ItemStack itemStack, @Nullable ServerPlayerEntity player){
         if(!ArcanaItemUtils.isArcane(itemStack)) return ArcanaRegistry.getModelData(TXT_OFF).value();
         boolean active = getBooleanProperty(itemStack,ACTIVE_TAG);
         boolean cremation = ArcanaAugments.getAugmentOnItem(itemStack,ArcanaAugments.CREMATION.id) >= 1;
         if(cremation){
            return active ? ArcanaRegistry.getModelData(TXT_CREMATION_ON).value() : ArcanaRegistry.getModelData(TXT_CREMATION_OFF).value();
         }else{
            return active ? ArcanaRegistry.getModelData(TXT_ON).value() : ArcanaRegistry.getModelData(TXT_OFF).value();
         }
      }
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
      
      @Override
      public ActionResult use(World world, PlayerEntity playerEntity, Hand hand){
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
         if(!ArcanaItemUtils.isArcane(stack)) return;
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

