package net.borisshoes.arcananovum.items.arrows;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
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
import net.borisshoes.arcananovum.utils.TextUtils;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class DetonationArrows extends RunicArrow {
	public static final String ID = "detonation_arrows";
   
   public DetonationArrows(){
      id = ID;
      name = "Detonation Arrows";
      rarity = ArcanaRarity.EXOTIC;
      categories = new TomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), TomeGui.TomeFilter.ARROWS};
      vanillaItem = Items.TIPPED_ARROW;
      item = new DetonationArrowsItem(addArcanaItemComponents(new Item.Settings().maxCount(64)
            .component(DataComponentTypes.POTION_CONTENTS, new PotionContentsComponent(Optional.empty(),Optional.of(11035949),new ArrayList<>(),Optional.empty()))
      ));
      displayName = Text.translatableWithFallback("item."+MOD_ID+"."+ID,name).formatted(Formatting.BOLD,Formatting.DARK_RED);
      researchTasks = new RegistryKey[]{ResearchTasks.UNLOCK_RUNIC_MATRIX,ResearchTasks.UNLOCK_RADIANT_FLETCHERY,ResearchTasks.OBTAIN_SPECTRAL_ARROW,ResearchTasks.OBTAIN_TNT};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getMaxCount());
      setPrefStack(stack);
   }
   
   @Override
   public List<Text> getItemLore(@Nullable ItemStack itemStack){
      List<MutableText> lore = new ArrayList<>();
      addRunicArrowLore(lore);
      lore.add(Text.literal("Detonation Arrows:").formatted(Formatting.BOLD,Formatting.DARK_RED));
      lore.add(Text.literal("")
            .append(Text.literal("These ").formatted(Formatting.GOLD))
            .append(Text.literal("Runic Arrows").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal(" explode").formatted(Formatting.RED))
            .append(Text.literal(" on impact ").formatted(Formatting.GOLD))
            .append(Text.literal("destroying").formatted(Formatting.RED))
            .append(Text.literal(" nearby terrain.").formatted(Formatting.GOLD)));
      lore.add(Text.literal("")
            .append(Text.literal("The").formatted(Formatting.GOLD))
            .append(Text.literal(" explosion").formatted(Formatting.RED))
            .append(Text.literal(" does ").formatted(Formatting.GOLD))
            .append(Text.literal("reduced damage").formatted(Formatting.RED))
            .append(Text.literal(" to ").formatted(Formatting.GOLD))
            .append(Text.literal("players").formatted(Formatting.RED))
            .append(Text.literal(".").formatted(Formatting.GOLD)));
     return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   public void entityHit(RunicArrowEntity arrow, EntityHitResult entityHitResult){
      int blastLvl = arrow.getAugment(ArcanaAugments.BLAST_MINE.id);
      int personLvl = arrow.getAugment(ArcanaAugments.ANTI_PERSONNEL.id);
      explode(arrow,entityHitResult.getPos(),blastLvl,personLvl);
   }
   
   @Override
   public void blockHit(RunicArrowEntity arrow, BlockHitResult blockHitResult){
      int blastLvl = arrow.getAugment(ArcanaAugments.BLAST_MINE.id);
      int personLvl = arrow.getAugment(ArcanaAugments.ANTI_PERSONNEL.id);
      explode(arrow,blockHitResult.getPos(),blastLvl,personLvl);
   }
   
   private void explode(PersistentProjectileEntity arrow, Vec3d pos, int blastLvl, int personLvl){
      double power = MathHelper.clamp(2*arrow.getVelocity().length(),1.5,8);
      DamageSource source1 = ArcanaDamageTypes.of(arrow.getWorld(),ArcanaDamageTypes.DETONATION_TERRAIN,arrow.getOwner(),arrow);
      DamageSource source2 = ArcanaDamageTypes.of(arrow.getWorld(),ArcanaDamageTypes.DETONATION_DAMAGE,arrow.getOwner(),arrow);
      if(personLvl != 3){ // Terrain explosion except when personnel lvl 3
         arrow.getEntityWorld().createExplosion(null, source1, null,pos.x,pos.y,pos.z,(float)(power*(1+.4*blastLvl)),false, World.ExplosionSourceType.TNT);
      }
      if(blastLvl != 3){ // Damage explosion except when blast lvl 3
         arrow.getEntityWorld().createExplosion(null, source2, null,pos.x,pos.y,pos.z,(float)(power*0.75*(1+.25*personLvl)),false, World.ExplosionSourceType.NONE);
      }
      
      
      
      arrow.discard();
   }
   
   @Override
	protected ArcanaRecipe makeRecipe(){
      ArcanaIngredient a = ArcanaIngredient.EMPTY;
      ArcanaIngredient c = new ArcanaIngredient(Items.TNT,8);
      ArcanaIngredient h = new ArcanaIngredient(Items.SPECTRAL_ARROW,16);
      GenericArcanaIngredient m = new GenericArcanaIngredient(ArcanaRegistry.RUNIC_MATRIX,1);
      
      ArcanaIngredient[][] ingredients = {
            {a,a,c,a,a},
            {a,c,h,c,a},
            {c,h,m,h,c},
            {a,c,h,c,a},
            {a,a,c,a,a}};
      return new ArcanaRecipe(ingredients,new ForgeRequirement().withFletchery());
   }
   
   @Override
   public List<List<Text>> getBookLore(){
      List<List<Text>> list = new ArrayList<>();
      list.add(List.of(Text.literal("Detonation Arrows").formatted(Formatting.DARK_RED,Formatting.BOLD),Text.literal("\nRarity: ").formatted(Formatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(),false)),Text.literal("\nThis Runic Matrix has been stuffed full of volatile Arcana, ready to blow at the slightest impact. However, the blast seems to be less effective on players compared to other creatures.").formatted(Formatting.BLACK)));
      return list;
   }
   
   public class DetonationArrowsItem extends ArcanaPolymerArrowItem {
      public DetonationArrowsItem(Item.Settings settings){
         super(getThis(),settings);
      }
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
   }
}

