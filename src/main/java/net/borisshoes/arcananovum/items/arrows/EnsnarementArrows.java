package net.borisshoes.arcananovum.items.arrows;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.MagicItem;
import net.borisshoes.arcananovum.core.polymer.MagicPolymerArrowItem;
import net.borisshoes.arcananovum.core.polymer.MagicPolymerItem;
import net.borisshoes.arcananovum.entities.RunicArrowEntity;
import net.borisshoes.arcananovum.items.ArcaneTome;
import net.borisshoes.arcananovum.recipes.arcana.ForgeRequirement;
import net.borisshoes.arcananovum.recipes.arcana.GenericMagicIngredient;
import net.borisshoes.arcananovum.recipes.arcana.MagicItemIngredient;
import net.borisshoes.arcananovum.recipes.arcana.MagicItemRecipe;
import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.borisshoes.arcananovum.utils.MagicRarity;
import net.borisshoes.arcananovum.utils.RepeatTimer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.Pair;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;

public class EnsnarementArrows extends RunicArrow {
   
   private static final String TXT = "item/runic_arrow";
   
   public EnsnarementArrows(){
      id = "ensnarement_arrows";
      name = "Ensnarement Arrows";
      rarity = MagicRarity.EXOTIC;
      categories = new ArcaneTome.TomeFilter[]{ArcaneTome.TomeFilter.EXOTIC,ArcaneTome.TomeFilter.ARROWS};
      vanillaItem = Items.TIPPED_ARROW;
      item = new EnsnarementArrowsItem(new FabricItemSettings().maxCount(64).fireproof());
      models = new ArrayList<>();
      models.add(new Pair<>(vanillaItem,TXT));
      
      ItemStack stack = new ItemStack(item);
      NbtCompound tag = stack.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList enchants = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name","[{\"text\":\"Runic Arrows - Ensnarement\",\"italic\":false,\"bold\":true,\"color\":\"dark_purple\"}]");
      tag.put("display",display);
      tag.put("Enchantments",enchants);
      tag.putInt("CustomPotionColor",5046527);
      tag.putInt("HideFlags",255);
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
      loreList.add(NbtString.of("[{\"text\":\"Ensnarement Arrows:\",\"italic\":false,\"color\":\"dark_purple\",\"bold\":true},{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\",\"bold\":false}]"));
      loreList.add(NbtString.of("[{\"text\":\"These \",\"italic\":false,\"color\":\"gray\"},{\"text\":\"Runic Arrows\",\"color\":\"light_purple\"},{\"text\":\" \"},{\"text\":\"restrain \",\"color\":\"dark_purple\"},{\"text\":\"a \"},{\"text\":\"target \",\"color\":\"yellow\"},{\"text\":\"from \"},{\"text\":\"moving \",\"color\":\"dark_purple\"},{\"text\":\"of their own will.\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"The \",\"italic\":false,\"color\":\"gray\"},{\"text\":\"Arrows \",\"color\":\"light_purple\"},{\"text\":\"have a \"},{\"text\":\"reduced effect\",\"color\":\"dark_purple\"},{\"text\":\" on \"},{\"text\":\"players\",\"color\":\"yellow\"},{\"text\":\".\"}]"));
      return loreList;
   }
   
   @Override
   public void entityHit(RunicArrowEntity arrow, EntityHitResult entityHitResult){
      int entrapment = arrow.getAugment(ArcanaAugments.ENTRAPMENT.id);
      boolean anchor = arrow.getAugment(ArcanaAugments.ETHEREAL_ANCHOR.id) > 0;
      if(entityHitResult.getEntity() instanceof LivingEntity living){
         int duration = living instanceof ServerPlayerEntity ? (entrapment+1) : (entrapment+1)*5;
         living.addStatusEffect(new StatusEffectInstance(ArcanaRegistry.ENSNAREMENT_EFFECT, (int) (duration*20),anchor ? 1 : 0),arrow.getOwner());
         
         if(arrow.getOwner() instanceof ServerPlayerEntity player){
            if(living.getAir() <= 0){
               ArcanaAchievements.grant(player, ArcanaAchievements.WATERBOARDING.id);
            }
            
            if(!ArcanaAchievements.isTimerActive(player, ArcanaAchievements.SHACKLED.id)){
               ArcanaAchievements.progress(player, ArcanaAchievements.SHACKLED.id,10);
               ArcanaNovum.addTickTimerCallback(new RepeatTimer(10,120, ()->{
                  if(living.isAlive() && living.getStatusEffect(ArcanaRegistry.ENSNAREMENT_EFFECT) != null){
                     ArcanaAchievements.progress(player, ArcanaAchievements.SHACKLED.id,10);
                  }else{
                     ArcanaAchievements.reset(player,ArcanaAchievements.SHACKLED.id);
                  }
               },null));
            }
         }
      }
   }
   
   @Override
   public void blockHit(RunicArrowEntity arrow, BlockHitResult blockHitResult){}
   
   
   private MagicItemRecipe makeRecipe(){
      MagicItemIngredient a = MagicItemIngredient.EMPTY;
      MagicItemIngredient c = new MagicItemIngredient(Items.COBWEB,64,null);
      MagicItemIngredient g = new MagicItemIngredient(Items.CRYING_OBSIDIAN,64,null);
      MagicItemIngredient h = new MagicItemIngredient(Items.SPECTRAL_ARROW,64,null);
      MagicItemIngredient k = new MagicItemIngredient(Items.ENDER_PEARL,16,null);
      GenericMagicIngredient m = new GenericMagicIngredient(ArcanaRegistry.RUNIC_MATRIX,1);
      
      MagicItemIngredient[][] ingredients = {
            {a,a,c,a,a},
            {a,g,h,g,a},
            {k,h,m,h,k},
            {a,g,h,g,a},
            {a,a,c,a,a}};
      return new MagicItemRecipe(ingredients,new ForgeRequirement().withFletchery());
      
   }
   
   private List<String> makeLore(){
      ArrayList<String> list = new ArrayList<>();
      list.add("\" Ensnarement Arrows\\n\\nRarity: Exotic\\n\\nThese arrows unleash Arcane chains around the target creature.\\nThese chains fully stop the creature from moving, while still letting them shift due to environmental factors. Players are affected less.\"");
      return list;
   }
   
   public class EnsnarementArrowsItem extends MagicPolymerArrowItem {
      public EnsnarementArrowsItem(Settings settings){
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
