package net.borisshoes.arcananovum.items.arrows;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerArrowItem;
import net.borisshoes.arcananovum.entities.RunicArrowEntity;
import net.borisshoes.arcananovum.gui.arcanetome.TomeGui;
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
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ExpulsionArrows extends RunicArrow {
	public static final String ID = "expulsion_arrows";
   
   private static final String TXT = "item/runic_arrow";
   
   public ExpulsionArrows(){
      id = ID;
      name = "Expulsion Arrows";
      rarity = ArcanaRarity.EXOTIC;
      categories = new TomeGui.TomeFilter[]{TomeGui.TomeFilter.EXOTIC, TomeGui.TomeFilter.ARROWS};
      vanillaItem = Items.TIPPED_ARROW;
      item = new ExpulsionArrowsItem(new Item.Settings().maxCount(64).fireproof()
            .component(DataComponentTypes.ITEM_NAME, Text.literal("Runic Arrows - Expulsion").formatted(Formatting.BOLD,Formatting.BLUE))
            .component(DataComponentTypes.LORE, new LoreComponent(getItemLore(null)))
            .component(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true)
            .component(DataComponentTypes.POTION_CONTENTS, new PotionContentsComponent(Optional.empty(),Optional.of(889599),new ArrayList<>()))
      );
      models = new ArrayList<>();
      models.add(new Pair<>(vanillaItem,TXT));
      researchTasks = new RegistryKey[]{ResearchTasks.UNLOCK_RUNIC_MATRIX,ResearchTasks.UNLOCK_RADIANT_FLETCHERY,ResearchTasks.OBTAIN_SPECTRAL_ARROW, ResearchTasks.KILL_SLIME,ResearchTasks.OBTAIN_AMETHYST_SHARD,ResearchTasks.USE_ENDER_PEARL};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getMaxCount());
      setPrefStack(stack);
   }
   
   @Override
   public List<Text> getItemLore(@Nullable ItemStack itemStack){
      List<MutableText> lore = new ArrayList<>();
      addRunicArrowLore(lore);
      lore.add(Text.literal("Expulsion Arrows:").formatted(Formatting.BOLD,Formatting.BLUE));
      lore.add(Text.literal("")
            .append(Text.literal("These ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("Runic Arrows").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal(" repulse ").formatted(Formatting.BLUE))
            .append(Text.literal("entities").formatted(Formatting.AQUA))
            .append(Text.literal(" near the area of ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("impact").formatted(Formatting.AQUA))
            .append(Text.literal(".").formatted(Formatting.DARK_AQUA)));
      lore.add(Text.literal("")
            .append(Text.literal("A hit ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("entity ").formatted(Formatting.AQUA))
            .append(Text.literal("is ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("not affected").formatted(Formatting.BLUE))
            .append(Text.literal(".").formatted(Formatting.DARK_AQUA)));
     return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   public void entityHit(RunicArrowEntity arrow, EntityHitResult entityHitResult){
      if(arrow.getEntityWorld() instanceof ServerWorld serverWorld){
         int duration = (int) MathHelper.clamp(arrow.getVelocity().length()*7,2,20); // Measured in quarter seconds
         double range = 4 + 1.5*arrow.getAugment(ArcanaAugments.REPULSION.id);
         expulsionPulse(arrow, serverWorld,null,entityHitResult.getEntity(),duration,range,0);
      }
   }
   
   @Override
   public void blockHit(RunicArrowEntity arrow, BlockHitResult blockHitResult){
      if(arrow.getEntityWorld() instanceof ServerWorld serverWorld){
         int duration = (int) MathHelper.clamp(arrow.getVelocity().length()*7,2,20); // Measured in quarter seconds
         double range = 4 + 1.5*arrow.getAugment(ArcanaAugments.REPULSION.id);
         expulsionPulse(arrow, serverWorld,blockHitResult.getPos(),null,duration,range,0);
      }
   }
   
   private void expulsionPulse(PersistentProjectileEntity arrow, ServerWorld world, @Nullable Vec3d start, @Nullable Entity entity, int duration, double range, int calls){
      if(start == null && entity == null) return;
      Vec3d pos = entity == null ? start : entity.getPos();
      
      Box rangeBox = new Box(pos.x+8,pos.y+8,pos.z+8,pos.x-8,pos.y-8,pos.z-8);
      List<Entity> entities = world.getOtherEntities(entity,rangeBox, e -> !e.isSpectator() && e.squaredDistanceTo(pos) < 1.5*range*range && !(e instanceof PersistentProjectileEntity) && !(e instanceof EnderDragonEntity));
      for(Entity entity1 : entities){
         Vec3d diff = entity1.getPos().subtract(pos);
         double multiplier = MathHelper.clamp(range*.75-diff.length()*.5,.1,5);
         Vec3d motion = diff.add(0,0,0).normalize().multiply(multiplier);
         entity1.setVelocity(motion.x,motion.y,motion.z);
         if(entity1 instanceof ServerPlayerEntity player){
            player.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(player));
            
            if(arrow.getOwner() != null && arrow.getOwner().getUuid().equals(player.getUuid()) && motion.y > 2) ArcanaAchievements.grant(player,ArcanaAchievements.JUMP_PAD.id);
         }
      }
      
      if(calls % 5 == 0){
         ParticleEffectUtils.expulsionArrowEmit(world,pos,range,0);
         SoundUtils.playSound(world,BlockPos.ofFloored(pos), SoundEvents.ENTITY_ALLAY_ITEM_TAKEN, SoundCategory.PLAYERS,.5f,.5f);
      }
      if(calls % 10 == 1){
         SoundUtils.playSound(world,BlockPos.ofFloored(pos), SoundEvents.ENTITY_ALLAY_AMBIENT_WITHOUT_ITEM, SoundCategory.PLAYERS,.5f,.9f);
      }
      
      if(calls < duration){
         ArcanaNovum.addTickTimerCallback(world, new GenericTimer(5, () -> expulsionPulse(arrow, world, pos, entity,duration,range,calls + 1)));
      }
   }
   
   @Override
	protected ArcanaRecipe makeRecipe(){
      ArcanaIngredient a = ArcanaIngredient.EMPTY;
      ArcanaIngredient c = new ArcanaIngredient(Items.AMETHYST_SHARD,32);
      ArcanaIngredient g = new ArcanaIngredient(Items.SLIME_BLOCK,8);
      ArcanaIngredient h = new ArcanaIngredient(Items.SPECTRAL_ARROW,16);
      ArcanaIngredient k = new ArcanaIngredient(Items.ENDER_PEARL,16);
      GenericArcanaIngredient m = new GenericArcanaIngredient(ArcanaRegistry.RUNIC_MATRIX,1);
      
      ArcanaIngredient[][] ingredients = {
            {a,a,c,a,a},
            {a,g,h,g,a},
            {k,h,m,h,k},
            {a,g,h,g,a},
            {a,a,c,a,a}};
      return new ArcanaRecipe(ingredients,new ForgeRequirement().withFletchery());
   }
   
   @Override
   public List<List<Text>> getBookLore(){
      List<List<Text>> list = new ArrayList<>();
      list.add(List.of(Text.literal("   Expulsion Arrows\n\nRarity: Exotic\n\nThis Runic Matrix is configured to repulse anything nearby like bouncing on a slime block. Great for jump pads, zoning off monsters, or sending foes off a steep cliff.").formatted(Formatting.BLACK)));
      return list;
   }
   
   public class ExpulsionArrowsItem extends ArcanaPolymerArrowItem {
      public ExpulsionArrowsItem(Item.Settings settings){
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

