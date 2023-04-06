package net.borisshoes.arcananovum.items.arrows;

import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.cardinalcomponents.MagicEntity;
import net.borisshoes.arcananovum.items.ArcaneTome;
import net.borisshoes.arcananovum.items.core.MagicItem;
import net.borisshoes.arcananovum.items.core.MagicItems;
import net.borisshoes.arcananovum.items.core.RunicArrow;
import net.borisshoes.arcananovum.recipes.GenericMagicIngredient;
import net.borisshoes.arcananovum.recipes.MagicItemIngredient;
import net.borisshoes.arcananovum.recipes.MagicItemRecipe;
import net.borisshoes.arcananovum.utils.MagicRarity;
import net.borisshoes.arcananovum.utils.ParticleEffectUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static net.borisshoes.arcananovum.items.core.MagicItems.RECOMMENDED_LIST;

public class PhotonicArrows extends RunicArrow {
   
   public PhotonicArrows(){
      id = "photonic_arrows";
      name = "Photonic Arrows";
      rarity = MagicRarity.LEGENDARY;
      categories = new ArcaneTome.TomeFilter[]{ArcaneTome.TomeFilter.LEGENDARY, ArcaneTome.TomeFilter.ARROWS};
      
      ItemStack item = new ItemStack(Items.TIPPED_ARROW);
      NbtCompound tag = item.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList loreList = new NbtList();
      NbtList enchants = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name","[{\"text\":\"Runic Arrows - Photonic\",\"italic\":false,\"color\":\"aqua\",\"bold\":true}]");
      addRunicArrowLore(loreList);
      loreList.add(NbtString.of("[{\"text\":\"Photonic Arrows:\",\"italic\":false,\"color\":\"aqua\",\"bold\":true},{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\",\"bold\":false}]"));
      loreList.add(NbtString.of("[{\"text\":\"These \",\"italic\":false,\"color\":\"white\"},{\"text\":\"Runic Arrows\",\"color\":\"light_purple\"},{\"text\":\" fly perfectly \"},{\"text\":\"straight \",\"color\":\"aqua\"},{\"text\":\"through the air.\",\"color\":\"white\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"The \",\"italic\":false,\"color\":\"white\"},{\"text\":\"arrows \",\"color\":\"light_purple\"},{\"text\":\"pierce \",\"color\":\"aqua\"},{\"text\":\"all \"},{\"text\":\"entities \",\"color\":\"aqua\"},{\"text\":\"before hitting a \"},{\"text\":\"block\",\"color\":\"aqua\"},{\"text\":\".\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Legendary\",\"italic\":false,\"color\":\"gold\",\"bold\":true},{\"text\":\" \",\"color\":\"dark_purple\"},{\"text\":\"Magic Item\",\"color\":\"dark_purple\",\"bold\":false}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
      tag.put("Enchantments",enchants);
      tag.putInt("CustomPotionColor",11271167);
      tag.putInt("HideFlags",127);
      item.setCount(64);
      
      setBookLore(makeLore());
      setRecipe(makeRecipe());
      prefNBT = addMagicNbt(tag);
      
      item.setNbt(prefNBT);
      prefItem = item;
   }
   
   public void shoot(World world, LivingEntity entity, PersistentProjectileEntity proj, int alignmentLvl){
      Vec3d startPos = entity.getEyePos();
      Vec3d view = entity.getRotationVecClient();
      Vec3d rayEnd = startPos.add(view.multiply(100));
      BlockHitResult raycast = world.raycast(new RaycastContext(startPos,rayEnd, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE,entity));
      EntityHitResult entityHit;
      List<Entity> hits = new ArrayList<>();
      Box box = new Box(startPos,raycast.getPos());
      box = box.expand(2);
      // Primary hitscan check
      do{
         entityHit = ProjectileUtil.raycast(entity,startPos,raycast.getPos(),box,e -> e instanceof LivingEntity && !e.isSpectator() && !hits.contains(e),100000);
         if(entityHit != null && entityHit.getType() == HitResult.Type.ENTITY){
            hits.add(entityHit.getEntity());
         }
      }while(entityHit != null && entityHit.getType() == HitResult.Type.ENTITY);
   
      // Secondary hitscan check to add lenience
      List<Entity> hits2 = world.getOtherEntities(entity, box, (e)-> e instanceof LivingEntity && !e.isSpectator() && !hits.contains(e) && inRange(e,startPos,raycast.getPos()));
      hits.addAll(hits2);
      hits.sort(Comparator.comparingDouble(e->e.distanceTo(entity)));
      
      float damage = (float)MathHelper.clamp(proj.getVelocity().length()*5,1,20);
      if(alignmentLvl == 5) damage += 4 + damage*0.2;
      float bonusDmg = 0;
      
      int killCount = 0;
      for(Entity hit : hits){
         float finalDmg = (float) ((damage+bonusDmg) * Math.min(1,-0.01*(hit.getPos().distanceTo(startPos)-100)+0.25)) * (hit instanceof ServerPlayerEntity ? 0.5f : 1f);
         hit.damage(DamageSource.magic(proj,entity), finalDmg);
         if(hit instanceof MobEntity mob && mob.isDead()) killCount++;
         bonusDmg += alignmentLvl;
      }
      if(proj.getOwner() instanceof ServerPlayerEntity player && killCount >= 10) ArcanaAchievements.grant(player,"x");
      
      if(world instanceof ServerWorld serverWorld){
         ParticleEffectUtils.photonArrowShot(serverWorld,entity,raycast.getPos(), MathHelper.clamp(damage/15,.4f,1f));
      }
   }
   
   private boolean inRange(Entity e, Vec3d start, Vec3d end){
      double range = .25;
      Box entityBox = e.getBoundingBox().expand((double)e.getTargetingMargin());
      double len = end.subtract(start).length();
      Vec3d trace = end.subtract(start).normalize().multiply(range);
      int i = 0;
      Vec3d t2 = trace.multiply(i);
      while(t2.length() < len){
         Vec3d t3 = start.add(t2);
         Box hitBox = new Box(t3.x-range,t3.y-range,t3.z-range,t3.x+range,t3.y+range,t3.z+range);
         if(entityBox.intersects(hitBox)){
            return true;
         }
         t2 = trace.multiply(i);
         i++;
      }
      return false;
   }
   
   @Override
   public void entityHit(PersistentProjectileEntity arrow, EntityHitResult entityHitResult, MagicEntity magicEntity){}
   
   @Override
   public void blockHit(PersistentProjectileEntity arrow, BlockHitResult blockHitResult, MagicEntity magicEntity){}
   
   private MagicItemRecipe makeRecipe(){
      MagicItemIngredient a = MagicItemIngredient.EMPTY;
      MagicItemIngredient c = new MagicItemIngredient(Items.AMETHYST_CLUSTER,32,null);
      MagicItemIngredient g = new MagicItemIngredient(Items.BEACON,4,null);
      MagicItemIngredient h = new MagicItemIngredient(Items.SPECTRAL_ARROW,64,null);
      MagicItemIngredient k = new MagicItemIngredient(Items.GLOW_INK_SAC,64,null);
      GenericMagicIngredient m = new GenericMagicIngredient(MagicItems.RUNIC_MATRIX,1);
   
      MagicItemIngredient[][] ingredients = {
            {a,a,c,a,a},
            {a,g,h,g,a},
            {k,h,m,h,k},
            {a,g,h,g,a},
            {a,a,c,a,a}};
      return new MagicItemRecipe(ingredients);
   }
   
   private List<String> makeLore(){
      ArrayList<String> list = new ArrayList<>();
      list.add("{\"text\":\"   Photonic Arrows\\n\\nRarity: Legendary\\n\\n'Straight as an arrow'. What a joke of a saying, I'll show them what straight looks like. Some solar runes coupled with a focusing prism makes a hell of a combo. This brings a new meaning to 'Shooting Lazers'.\"}");
      return list;
   }
}
