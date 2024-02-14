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
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Pair;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class GravitonArrows extends RunicArrow {
   
   private static final String TXT = "item/runic_arrow";
   
   public GravitonArrows(){
      id = "graviton_arrows";
      name = "Graviton Arrows";
      rarity = MagicRarity.EXOTIC;
      categories = new ArcaneTome.TomeFilter[]{ArcaneTome.TomeFilter.EXOTIC, ArcaneTome.TomeFilter.ARROWS};
      vanillaItem = Items.TIPPED_ARROW;
      item = new GravitonArrowsItem(new FabricItemSettings().maxCount(64).fireproof());
      models = new ArrayList<>();
      models.add(new Pair<>(vanillaItem,TXT));
      
      ItemStack stack = new ItemStack(item);
      NbtCompound tag = stack.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList enchants = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name","[{\"text\":\"Runic Arrows - Graviton\",\"italic\":false,\"color\":\"dark_blue\",\"bold\":true}]");
      tag.put("display",display);
      tag.put("Enchantments",enchants);
      tag.putInt("CustomPotionColor",869887);
      tag.putInt("HideFlags", 255);
      stack.setCount(64);
      
      setBookLore(makeLore());
      setRecipe(makeRecipe());
      stack.setNbt(addMagicNbt(tag));
      setPrefStack(stack);
   }
   
   @Override
   public NbtList getItemLore(@Nullable ItemStack itemStack){
      NbtList loreList = new NbtList();
      addRunicArrowLore(loreList);
      loreList.add(NbtString.of("[{\"text\":\"Graviton Arrows:\",\"italic\":false,\"color\":\"dark_blue\",\"bold\":true},{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\",\"bold\":false}]"));
      loreList.add(NbtString.of("[{\"text\":\"These \",\"italic\":false,\"color\":\"blue\"},{\"text\":\"Runic Arrows\",\"color\":\"light_purple\"},{\"text\":\" \"},{\"text\":\"attract\",\"color\":\"dark_aqua\"},{\"text\":\" \"},{\"text\":\"entities\",\"color\":\"aqua\"},{\"text\":\" near the area of \"},{\"text\":\"impact\",\"color\":\"aqua\"},{\"text\":\".\",\"color\":\"blue\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"A hit \",\"italic\":false,\"color\":\"blue\"},{\"text\":\"entity\",\"color\":\"aqua\"},{\"text\":\" is \"},{\"text\":\"not affected\",\"color\":\"dark_aqua\"},{\"text\":\".\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      return loreList;
   }
   
   @Override
   public void entityHit(RunicArrowEntity arrow, EntityHitResult entityHitResult){
      if(arrow.getEntityWorld() instanceof ServerWorld serverWorld){
         int duration = (int) MathHelper.clamp(arrow.getVelocity().length()*7,2,20);// Measured in quarter seconds
         double range = 3 + arrow.getAugment(ArcanaAugments.GRAVITY_WELL.id);
         gravitonPulse(arrow, serverWorld,null,entityHitResult.getEntity(),duration,range,0);
      }
   }
   
   @Override
   public void blockHit(RunicArrowEntity arrow, BlockHitResult blockHitResult){
      if(arrow.getEntityWorld() instanceof ServerWorld serverWorld){
         int duration = (int) MathHelper.clamp(arrow.getVelocity().length()*7,2,20); // Measured in quarter seconds
         double range = 3 + arrow.getAugment(ArcanaAugments.GRAVITY_WELL.id);
         gravitonPulse(arrow, serverWorld,blockHitResult.getPos(),null,duration,range,0);
      }
   }
   
   private void gravitonPulse(PersistentProjectileEntity arrow, ServerWorld world, @Nullable Vec3d start, @Nullable Entity entity, int duration, double range, int calls){
      if(start == null && entity == null) return;
      Vec3d pos = entity == null ? start : entity.getPos();
      int mobsHit = 0;
      
      Box rangeBox = new Box(pos.x+8,pos.y+8,pos.z+8,pos.x-8,pos.y-8,pos.z-8);
      List<Entity> entities = world.getOtherEntities(entity,rangeBox, e -> !e.isSpectator() && e.squaredDistanceTo(pos) < 2*range*range && !(e instanceof PersistentProjectileEntity));
      for(Entity entity1 : entities){
         Vec3d diff = entity1.getPos().subtract(pos);
         double multiplier = MathHelper.clamp(diff.length()*.2,.03,2);
         Vec3d motion = diff.add(0,0,0).normalize().multiply(-multiplier);
         entity1.setVelocity(motion.x,motion.y,motion.z);
         if(entity1 instanceof ServerPlayerEntity player){
            player.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(player));
         }
   
         if(entity1 instanceof LivingEntity e){
            if(e instanceof MobEntity) mobsHit++;
            
            int amp = (int) (5-diff.length());
            StatusEffectInstance slowness = new StatusEffectInstance(StatusEffects.SLOWNESS, 20, amp, false, false, true);
            e.addStatusEffect(slowness);
         }
      }
      if(arrow.getOwner() instanceof ServerPlayerEntity player && mobsHit >= 10) ArcanaAchievements.grant(player,ArcanaAchievements.BRING_TOGETHER.id);
      
      ParticleEffectUtils.gravitonArrowEmit(world,pos,entities);
      if(calls % 10 == 1){
         SoundUtils.playSound(world,BlockPos.ofFloored(pos), SoundEvents.BLOCK_PORTAL_AMBIENT, SoundCategory.PLAYERS,.5f,1.6f);
      }
      
      if(calls < duration){
         ArcanaNovum.addTickTimerCallback(world, new GenericTimer(5, () -> gravitonPulse(arrow, world, pos, entity,duration,range,calls + 1)));
      }
   }
   
   private MagicItemRecipe makeRecipe(){
      MagicItemIngredient a = MagicItemIngredient.EMPTY;
      MagicItemIngredient c = new MagicItemIngredient(Items.OBSIDIAN,64,null);
      MagicItemIngredient g = new MagicItemIngredient(Items.COBWEB,64,null);
      MagicItemIngredient h = new MagicItemIngredient(Items.SPECTRAL_ARROW,64,null);
      MagicItemIngredient k = new MagicItemIngredient(Items.CRYING_OBSIDIAN,64,null);
      GenericMagicIngredient m = new GenericMagicIngredient(ArcanaRegistry.RUNIC_MATRIX,1);
   
      MagicItemIngredient[][] ingredients = {
            {a,a,c,a,a},
            {a,g,h,g,a},
            {k,h,m,h,k},
            {a,g,h,g,a},
            {a,a,c,a,a}};
      return new MagicItemRecipe(ingredients, new ForgeRequirement().withFletchery());
   }
   
   private List<String> makeLore(){
      ArrayList<String> list = new ArrayList<>();
      list.add("{\"text\":\"   Graviton Arrows\\n\\nRarity: Exotic\\n\\nThis Runic Matrix amplifies gravity at a single point, drawing in everything nearby. Once at the center, things have a hard time leaving. Great for setting up a combo shot.\"}");
      return list;
   }
   
   public class GravitonArrowsItem extends MagicPolymerArrowItem {
      public GravitonArrowsItem(Settings settings){
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
