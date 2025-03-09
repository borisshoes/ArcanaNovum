package net.borisshoes.arcananovum.items.arrows;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerArrowItem;
import net.borisshoes.arcananovum.damage.ArcanaDamageTypes;
import net.borisshoes.arcananovum.entities.RunicArrowEntity;
import net.borisshoes.arcananovum.gui.arcanetome.TomeGui;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaIngredient;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaRecipe;
import net.borisshoes.arcananovum.recipes.arcana.ForgeRequirement;
import net.borisshoes.arcananovum.recipes.arcana.GenericArcanaIngredient;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaRarity;
import net.borisshoes.arcananovum.utils.MiscUtils;
import net.borisshoes.arcananovum.utils.ParticleEffectUtils;
import net.borisshoes.arcananovum.utils.TextUtils;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class PhotonicArrows extends RunicArrow {
	public static final String ID = "photonic_arrows";
   
   public PhotonicArrows(){
      id = ID;
      name = "Photonic Arrows";
      rarity = ArcanaRarity.EXOTIC;
      categories = new TomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), TomeGui.TomeFilter.ARROWS};
      vanillaItem = Items.TIPPED_ARROW;
      item = new PhotonicArrowsItem(addArcanaItemComponents(new Item.Settings().maxCount(64)
            .component(DataComponentTypes.POTION_CONTENTS, new PotionContentsComponent(Optional.empty(),Optional.of(11271167),new ArrayList<>(),Optional.empty()))
      ));
      displayName = Text.translatableWithFallback("item."+MOD_ID+"."+ID,name).formatted(Formatting.BOLD,Formatting.AQUA);
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
      if(!(world instanceof ServerWorld serverWorld)) return;
      MiscUtils.LasercastResult lasercast = MiscUtils.lasercast(world, entity.getEyePos(), entity.getRotationVecClient(), 100, true, entity);
      
      float damage = (float)MathHelper.clamp(proj.getVelocity().length()*5,1,20);
      if(alignmentLvl == 5) damage += (float) (4 + damage*0.2);
      float bonusDmg = 0;
      
      int killCount = 0;
      for(Entity hit : lasercast.sortedHits()){
         float finalDmg = (float) ((damage+bonusDmg) * Math.min(1,-0.01*(hit.getPos().distanceTo(lasercast.startPos())-100)+0.25)) * (hit instanceof ServerPlayerEntity ? 0.5f : 1f);
         if(hit instanceof ServerPlayerEntity hitPlayer && hitPlayer.isBlocking()){
            double dp = hitPlayer.getRotationVecClient().normalize().dotProduct(lasercast.direction().normalize());
            if(dp < -0.6){
               MiscUtils.blockWithShield(hitPlayer,finalDmg);
               continue;
            }
         }
         hit.damage(serverWorld, ArcanaDamageTypes.of(entity.getEntityWorld(),ArcanaDamageTypes.PHOTONIC,proj,entity), finalDmg);
         
         if(hit instanceof MobEntity mob && mob.isDead()){
            killCount++;
         }
         bonusDmg = Math.min(25,bonusDmg + alignmentLvl);
      }
      
      if(proj.getOwner() instanceof ServerPlayerEntity player && killCount >= 10) ArcanaAchievements.grant(player,ArcanaAchievements.X.id);
      
      if(proj.getOwner() instanceof ServerPlayerEntity player && !lasercast.sortedHits().isEmpty() && proj instanceof RunicArrowEntity runicArrowEntity){
         runicArrowEntity.incArrowForEveryFoe(player);
      }
      
      ParticleEffectUtils.photonArrowShot(serverWorld,entity.getEyePos().subtract(0,entity.getHeight()/4,0),lasercast.endPos(), MathHelper.clamp(damage/15,.4f,1f),0);
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
      list.add(List.of(Text.literal(" Photonic Arrows").formatted(Formatting.AQUA,Formatting.BOLD),Text.literal("\nRarity: ").formatted(Formatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(),false)),Text.literal("\n‘Straight as an arrow’. What a joke of a saying, I’ll show them what straight looks like. Some solar runes coupled with a focusing prism makes quite the combo. This brings a new meaning to ‘Shooting Lazers’.").formatted(Formatting.BLACK)));
      return list;
   }
   
   public class PhotonicArrowsItem extends ArcanaPolymerArrowItem {
      public PhotonicArrowsItem(Item.Settings settings){
         super(getThis(),settings);
      }
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
   }
}

