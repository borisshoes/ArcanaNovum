package net.borisshoes.arcananovum.items.arrows;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.core.polymer.MagicPolymerArrowItem;
import net.borisshoes.arcananovum.entities.RunicArrowEntity;
import net.borisshoes.arcananovum.items.ArcaneTome;
import net.borisshoes.arcananovum.recipes.arcana.ForgeRequirement;
import net.borisshoes.arcananovum.recipes.arcana.GenericMagicIngredient;
import net.borisshoes.arcananovum.recipes.arcana.MagicItemIngredient;
import net.borisshoes.arcananovum.recipes.arcana.MagicItemRecipe;
import net.borisshoes.arcananovum.utils.GenericTimer;
import net.borisshoes.arcananovum.utils.MagicRarity;
import net.borisshoes.arcananovum.utils.ParticleEffectUtils;
import net.borisshoes.arcananovum.utils.SoundUtils;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.MooshroomEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Pair;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class StormArrows extends RunicArrow {
   
   private static final double[] stormChance = {.1,.2,.4,.6,.8,1};
   private static final String TXT = "item/runic_arrow";
   
   public StormArrows(){
      id = "storm_arrows";
      name = "Storm Arrows";
      rarity = MagicRarity.EXOTIC;
      categories = new ArcaneTome.TomeFilter[]{ArcaneTome.TomeFilter.EXOTIC, ArcaneTome.TomeFilter.ARROWS};
      vanillaItem = Items.TIPPED_ARROW;
      item = new StormArrowsItem(new FabricItemSettings().maxCount(64).fireproof());
      models = new ArrayList<>();
      models.add(new Pair<>(vanillaItem,TXT));
      
      ItemStack stack = new ItemStack(item);
      NbtCompound tag = stack.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList enchants = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name","[{\"text\":\"Runic Arrows - Storm\",\"italic\":false,\"color\":\"gray\",\"bold\":true}]");
      tag.put("display",display);
      tag.put("Enchantments",enchants);
      tag.putInt("CustomPotionColor",12040354);
      tag.putInt("HideFlags", 255);
      stack.setCount(64);
      buildItemLore(stack, ArcanaNovum.SERVER);
      
      setBookLore(makeLore());
      setRecipe(makeRecipe());
      prefNBT = addMagicNbt(tag);
      
      stack.setNbt(prefNBT);
      prefItem = stack;
   }
   
   @Override
   public NbtList getItemLore(@Nullable ItemStack itemStack){
      NbtList loreList = new NbtList();
      addRunicArrowLore(loreList);
      loreList.add(NbtString.of("[{\"text\":\"Storm Arrows:\",\"italic\":false,\"color\":\"gray\",\"bold\":true},{\"text\":\"\",\"italic\":false,\"color\":\"gray\",\"bold\":false}]"));
      loreList.add(NbtString.of("[{\"text\":\"These \",\"italic\":false,\"color\":\"gray\"},{\"text\":\"Runic Arrows\",\"color\":\"light_purple\"},{\"text\":\" channel \"},{\"text\":\"lightning \",\"color\":\"yellow\"},{\"text\":\"from the \"},{\"text\":\"clouds \",\"color\":\"white\"},{\"text\":\"above.\",\"color\":\"gray\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Only a \",\"italic\":false,\"color\":\"gray\"},{\"text\":\"small chance\",\"color\":\"yellow\"},{\"text\":\" to work when not \"},{\"text\":\"raining\",\"color\":\"blue\"},{\"text\":\".\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      return loreList;
   }
   
   @Override
   public void entityHit(RunicArrowEntity arrow, EntityHitResult entityHitResult){
      int stableLvl = arrow.getAugment(ArcanaAugments.STORM_STABILIZATION.id);
      int chainLvl = arrow.getAugment(ArcanaAugments.CHAIN_LIGHTNING.id);
      int shockLvl = arrow.getAugment(ArcanaAugments.AFTERSHOCK.id);
      strike(arrow,entityHitResult.getPos(),stableLvl,shockLvl);
      if(chainLvl > 0) chainLightning(arrow,entityHitResult.getEntity(),chainLvl);
   }
   
   @Override
   public void blockHit(RunicArrowEntity arrow, BlockHitResult blockHitResult){
      int stableLvl = arrow.getAugment(ArcanaAugments.STORM_STABILIZATION.id);
      int shockLvl = arrow.getAugment(ArcanaAugments.AFTERSHOCK.id);
      strike(arrow,blockHitResult.getPos(),stableLvl,shockLvl);
   }
   
   private void strike(PersistentProjectileEntity arrow, Vec3d pos, int stableLevel, int shockLvl){
      World world = arrow.getEntityWorld();
      if(arrow.isCritical() && (world.isRaining() || world.isThundering() || Math.random() < stormChance[stableLevel])){
         LightningEntity lightning = new LightningEntity(EntityType.LIGHTNING_BOLT, arrow.getEntityWorld());
         lightning.setPosition(pos);
         world.spawnEntity(lightning);
   
         if(arrow.getOwner() instanceof ServerPlayerEntity player){
            ArcanaNovum.addTickTimerCallback(player.getServerWorld(), new GenericTimer(2, () -> {
               if(lightning.getStruckEntities().anyMatch(e -> e instanceof MooshroomEntity)) ArcanaAchievements.grant(player,ArcanaAchievements.SHOCK_THERAPY.id);
            }));
         }
   
         if(shockLvl > 0 && world instanceof ServerWorld serverWorld){
            aftershockPulse(arrow,serverWorld,pos,shockLvl,0);
            SoundUtils.playSound(world,BlockPos.ofFloored(pos), SoundEvents.ITEM_TRIDENT_THUNDER, SoundCategory.PLAYERS,.2f,1f);
         }
      }
   }
   
   private void chainLightning(PersistentProjectileEntity arrow, Entity hitEntity, int lvl){
      World world = arrow.getEntityWorld();
      Vec3d pos = hitEntity.getPos();
      double range = 5;
      
      Box rangeBox = new Box(pos.x+8,pos.y+8,pos.z+8,pos.x-8,pos.y-8,pos.z-8);
      List<Entity> entities = world.getOtherEntities(arrow.getOwner(),rangeBox, e -> !e.isSpectator() && e.squaredDistanceTo(pos) < range*range && !(e instanceof PersistentProjectileEntity));
      
      List<LivingEntity> hits = new ArrayList<>();
      if(hitEntity instanceof LivingEntity le) hits.add(le);
      for(Entity entity : entities){
         if(entity instanceof LivingEntity e && !entity.getUuidAsString().equals(hitEntity.getUuidAsString())){
            if(hits.size() < lvl+1){
               if(hits.size() > 0){
                  LivingEntity lastHit = hits.get(hits.size() - 1);
                  double dist = lastHit.getPos().distanceTo(e.getPos());
                  
                  if(world instanceof ServerWorld serverWorld)
                     ParticleEffectUtils.line(serverWorld,null,lastHit.getPos().add(0,lastHit.getHeight()/2,0),e.getPos().add(0,e.getHeight()/2,0),ParticleTypes.WAX_OFF,(int)(dist*8),2,0.05,0.05);
               }
               DamageSource source = new DamageSource(entity.getDamageSources().lightningBolt().getTypeRegistryEntry(), arrow.getOwner(),arrow.getOwner());
               e.damage(source,6);
               hits.add(e);
            }
         }
      }
      SoundUtils.playSound(world,BlockPos.ofFloored(pos), SoundEvents.ITEM_TRIDENT_THUNDER, SoundCategory.PLAYERS,.1f,2f);
   }
   
   private void aftershockPulse(PersistentProjectileEntity arrow, ServerWorld world, Vec3d pos, int level, int calls){
      double range = level >= 4 ? 4 : 2.5;
      float dmg = level >= 4 ? 4 : 2;
      int duration = 30 + 20*level;
      
      Box rangeBox = new Box(pos.x+8,pos.y+8,pos.z+8,pos.x-8,pos.y-8,pos.z-8);
      List<Entity> entities = world.getOtherEntities(null,rangeBox, e -> !e.isSpectator() && e.squaredDistanceTo(pos) < 1.25*range*range && !(e instanceof PersistentProjectileEntity));
      for(Entity entity : entities){
         if(entity instanceof LivingEntity e){
            DamageSource source = new DamageSource(entity.getDamageSources().lightningBolt().getTypeRegistryEntry(), arrow.getOwner(),arrow.getOwner());
            e.damage(source,dmg);
         }
      }
      
      world.spawnParticles(ParticleTypes.WAX_OFF,pos.x,pos.y,pos.z,30,range,1,range,.1);
      
      if(calls % 2 == 0){
         SoundUtils.playSound(world,BlockPos.ofFloored(pos), SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER, SoundCategory.PLAYERS,.07f,2f);
      }
      
      if(calls < duration/3){
         ArcanaNovum.addTickTimerCallback(world, new GenericTimer(3, () -> aftershockPulse(arrow, world, pos, level,calls + 1)));
      }
   }
   
   private MagicItemRecipe makeRecipe(){
      MagicItemIngredient a = MagicItemIngredient.EMPTY;
      MagicItemIngredient c = new MagicItemIngredient(Items.LIGHTNING_ROD,64,null);
      ItemStack enchantedBook6 = new ItemStack(Items.ENCHANTED_BOOK);
      EnchantedBookItem.addEnchantment(enchantedBook6,new EnchantmentLevelEntry(Enchantments.CHANNELING,1));
      MagicItemIngredient g = new MagicItemIngredient(Items.ENCHANTED_BOOK,1,enchantedBook6.getNbt());
      MagicItemIngredient h = new MagicItemIngredient(Items.SPECTRAL_ARROW,64,null);
      GenericMagicIngredient m = new GenericMagicIngredient(ArcanaRegistry.RUNIC_MATRIX,1);
   
      MagicItemIngredient[][] ingredients = {
            {a,a,c,a,a},
            {a,g,h,g,a},
            {c,h,m,h,c},
            {a,g,h,g,a},
            {a,a,c,a,a}};
      return new MagicItemRecipe(ingredients, new ForgeRequirement().withFletchery());
   }
   
   private List<String> makeLore(){
      ArrayList<String> list = new ArrayList<>();
      list.add("{\"text\":\"     Storm Arrows\\n\\nRarity: Exotic\\n\\nThe channeling enchantment requires a storm to use. Throwing a bit of Arcana into it seems to force a storm, abeit briefly. The Matrix doesn't always seem to succeed in activating though.\"}");
      return list;
   }
   
   public class StormArrowsItem extends MagicPolymerArrowItem {
      public StormArrowsItem(Settings settings){
         super(getThis(),settings);
      }
      
      @Override
      public int getPolymerCustomModelData(ItemStack itemStack, @Nullable ServerPlayerEntity player){
         return ArcanaRegistry.MODELS.get(TXT).value();
      }
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
   }
}
