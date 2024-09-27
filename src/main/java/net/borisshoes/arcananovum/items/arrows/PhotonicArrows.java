package net.borisshoes.arcananovum.items.arrows;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerArrowItem;
import net.borisshoes.arcananovum.damage.ArcanaDamageTypes;
import net.borisshoes.arcananovum.entities.RunicArrowEntity;
import net.borisshoes.arcananovum.gui.arcanetome.TomeGui;
import net.borisshoes.arcananovum.items.ShieldOfFortitude;
import net.borisshoes.arcananovum.mixins.LivingEntityAccessor;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaIngredient;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaRecipe;
import net.borisshoes.arcananovum.recipes.arcana.ForgeRequirement;
import net.borisshoes.arcananovum.recipes.arcana.GenericArcanaIngredient;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.*;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class PhotonicArrows extends RunicArrow {
	public static final String ID = "photonic_arrows";
   
   private static final String TXT = "item/runic_arrow";
   
   public PhotonicArrows(){
      id = ID;
      name = "Photonic Arrows";
      rarity = ArcanaRarity.SOVEREIGN;
      categories = new TomeGui.TomeFilter[]{TomeGui.TomeFilter.SOVEREIGN, TomeGui.TomeFilter.ARROWS};
      vanillaItem = Items.TIPPED_ARROW;
      item = new PhotonicArrowsItem(new Item.Settings().maxCount(64).fireproof()
            .component(DataComponentTypes.ITEM_NAME, Text.translatable("item."+MOD_ID+"."+ID).formatted(Formatting.BOLD,Formatting.AQUA))
            .component(DataComponentTypes.LORE, new LoreComponent(getItemLore(null)))
            .component(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true)
            .component(DataComponentTypes.POTION_CONTENTS, new PotionContentsComponent(Optional.empty(),Optional.of(11271167),new ArrayList<>(),Optional.empty()))
      );
      models = new ArrayList<>();
      models.add(new Pair<>(vanillaItem,TXT));
      researchTasks = new RegistryKey[]{ResearchTasks.UNLOCK_RUNIC_MATRIX,ResearchTasks.UNLOCK_RADIANT_FLETCHERY,ResearchTasks.OBTAIN_SPECTRAL_ARROW, ResearchTasks.ADVANCEMENT_CREATE_FULL_BEACON,ResearchTasks.OBTAIN_AMETHYST_CLUSTER,ResearchTasks.UNLOCK_MIDNIGHT_ENCHANTER};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getMaxCount());
      setPrefStack(stack);
   }
   
   @Override
   public List<Text> getItemLore(@Nullable ItemStack itemStack){
      List<MutableText> lore = new ArrayList<>();
      addRunicArrowLore(lore);
      lore.add(Text.literal("Photonic Arrows:").formatted(Formatting.BOLD,Formatting.AQUA));
      lore.add(Text.literal("")
            .append(Text.literal("These ").formatted(Formatting.WHITE))
            .append(Text.literal("Runic Arrows").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal(" fly perfectly ").formatted(Formatting.WHITE))
            .append(Text.literal("straight ").formatted(Formatting.AQUA))
            .append(Text.literal("through the air.").formatted(Formatting.WHITE)));
      lore.add(Text.literal("")
            .append(Text.literal("The ").formatted(Formatting.WHITE))
            .append(Text.literal("arrows ").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal("pierce ").formatted(Formatting.AQUA))
            .append(Text.literal("all ").formatted(Formatting.WHITE))
            .append(Text.literal("entities ").formatted(Formatting.AQUA))
            .append(Text.literal("before hitting a ").formatted(Formatting.WHITE))
            .append(Text.literal("block").formatted(Formatting.AQUA))
            .append(Text.literal(".").formatted(Formatting.WHITE)));
     return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   public void shoot(World world, LivingEntity entity, ProjectileEntity proj, int alignmentLvl){
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
      if(alignmentLvl == 5) damage += (float) (4 + damage*0.2);
      float bonusDmg = 0;
      
      Vec3d endPoint = raycast.getPos();
      int killCount = 0;
      for(Entity hit : hits){
         float finalDmg = (float) ((damage+bonusDmg) * Math.min(1,-0.01*(hit.getPos().distanceTo(startPos)-100)+0.25)) * (hit instanceof ServerPlayerEntity ? 0.5f : 1f);
         boolean ignore = false;
         if(hit instanceof ServerPlayerEntity hitPlayer && hitPlayer.isBlocking()){
            double dp = hitPlayer.getRotationVecClient().normalize().dotProduct(view.normalize());
            ignore = dp < -0.6;
            if(ignore){
               ((LivingEntityAccessor) hitPlayer).invokeDamageShield(finalDmg);
               SoundUtils.playSound(world,hitPlayer.getBlockPos(), SoundEvents.ITEM_SHIELD_BLOCK, SoundCategory.PLAYERS,1f,1f);
               endPoint = startPos.add(view.normalize().multiply(view.normalize().dotProduct(hitPlayer.getPos().subtract(startPos)))).subtract(view.normalize());
               
               // Activate Shield of Fortitude
               ItemStack activeItem = hitPlayer.getActiveItem();
               if(ArcanaItemUtils.identifyItem(activeItem) instanceof ShieldOfFortitude shield){
                  shield.shieldBlock(hitPlayer, activeItem, finalDmg);
               }
            }
         }
         if(!ignore){
            hit.damage(ArcanaDamageTypes.of(entity.getEntityWorld(),ArcanaDamageTypes.PHOTONIC,entity,proj), finalDmg);
         }
         if(hit instanceof MobEntity mob && mob.isDead()){
            killCount++;
         }
         bonusDmg = Math.min(25,bonusDmg + alignmentLvl);
         if(ignore) break;
      }
      if(proj.getOwner() instanceof ServerPlayerEntity player && killCount >= 10) ArcanaAchievements.grant(player,ArcanaAchievements.X.id);
      
      if(proj.getOwner() instanceof ServerPlayerEntity player && !hits.isEmpty() && proj instanceof RunicArrowEntity runicArrowEntity){
         runicArrowEntity.incArrowForEveryFoe(player);
      }
      
      if(world instanceof ServerWorld serverWorld){
         ParticleEffectUtils.photonArrowShot(serverWorld,entity,endPoint, MathHelper.clamp(damage/15,.4f,1f));
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
   public void entityHit(RunicArrowEntity arrow, EntityHitResult entityHitResult){}
   
   @Override
   public void blockHit(RunicArrowEntity arrow, BlockHitResult blockHitResult){}
   
   @Override
	protected ArcanaRecipe makeRecipe(){
      ArcanaIngredient a = ArcanaIngredient.EMPTY;
      ArcanaIngredient c = new ArcanaIngredient(Items.AMETHYST_CLUSTER,32);
      ArcanaIngredient g = new ArcanaIngredient(Items.BEACON,1);
      ArcanaIngredient h = new ArcanaIngredient(Items.SPECTRAL_ARROW,16);
      ArcanaIngredient k = new ArcanaIngredient(Items.GLOW_INK_SAC,32);
      GenericArcanaIngredient m = new GenericArcanaIngredient(ArcanaRegistry.RUNIC_MATRIX,1);
      
      ArcanaIngredient[][] ingredients = {
            {a,a,c,a,a},
            {a,g,h,g,a},
            {k,h,m,h,k},
            {a,g,h,g,a},
            {a,a,c,a,a}};
      return new ArcanaRecipe(ingredients,new ForgeRequirement().withFletchery().withEnchanter());
   }
   
   @Override
   public List<List<Text>> getBookLore(){
      List<List<Text>> list = new ArrayList<>();
      list.add(List.of(Text.literal("   Photonic Arrows\n\nRarity: Sovereign\n\n'Straight as an arrow'. What a joke of a saying, I'll show them what straight looks like. Some solar runes coupled with a focusing prism makes a hell of a combo. This brings a new meaning to 'Shooting Lazers'.").formatted(Formatting.BLACK)));
      return list;
   }
   
   public class PhotonicArrowsItem extends ArcanaPolymerArrowItem {
      public PhotonicArrowsItem(Item.Settings settings){
         super(getThis(),settings);
      }
      
      @Override
      public int getPolymerCustomModelData(ItemStack itemStack, @Nullable ServerPlayerEntity player){
         return ArcanaRegistry.getModelData(TXT).value();
      }
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
   }
}

