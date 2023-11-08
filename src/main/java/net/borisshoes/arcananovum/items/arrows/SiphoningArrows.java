package net.borisshoes.arcananovum.items.arrows;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.callbacks.OverhealTimerCallback;
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
import net.borisshoes.arcananovum.utils.SoundUtils;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SiphoningArrows extends RunicArrow {
   
   private static final int[] overhealCap = {0,2,4,10};
   
   public SiphoningArrows(){
      id = "siphoning_arrows";
      name = "Siphoning Arrows";
      rarity = MagicRarity.EXOTIC;
      categories = new ArcaneTome.TomeFilter[]{ArcaneTome.TomeFilter.EXOTIC, ArcaneTome.TomeFilter.ARROWS};
      vanillaItem = Items.TIPPED_ARROW;
      item = new SiphoningArrowsItem(new FabricItemSettings().maxCount(64).fireproof());
      
      ItemStack stack = new ItemStack(item);
      NbtCompound tag = stack.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList enchants = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name","[{\"text\":\"Runic Arrows - Siphoning\",\"italic\":false,\"color\":\"dark_red\",\"bold\":true}]");
      tag.put("display",display);
      tag.put("Enchantments",enchants);
      tag.putInt("CustomPotionColor",15866018);
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
      loreList.add(NbtString.of("[{\"text\":\"Siphoning Arrows:\",\"italic\":false,\"color\":\"dark_red\",\"bold\":true},{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\",\"bold\":false}]"));
      loreList.add(NbtString.of("[{\"text\":\"These \",\"italic\":false,\"color\":\"red\"},{\"text\":\"Runic Arrows\",\"color\":\"light_purple\"},{\"text\":\" \"},{\"text\":\"siphon health \",\"color\":\"dark_red\"},{\"text\":\"from hit \"},{\"text\":\"entities\",\"color\":\"gold\"},{\"text\":\".\",\"color\":\"red\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"The amount \",\"italic\":false,\"color\":\"red\"},{\"text\":\"siphoned \",\"color\":\"dark_red\"},{\"text\":\"is proportional to the \"},{\"text\":\"damage \",\"color\":\"gold\"},{\"text\":\"dealt.\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      return loreList;
   }
   
   @Override
   public void entityHit(RunicArrowEntity arrow, EntityHitResult entityHitResult){
      if(arrow.getOwner() instanceof ServerPlayerEntity player){
         double damage = MathHelper.ceil(MathHelper.clamp(arrow.getVelocity().length() * arrow.getDamage(), 0.0, 2.147483647E9)) / 5.5;
         damage += arrow.isCritical() ? damage/4 : 0;
   
         if(player.getHealth() < 1.5f){
            ArcanaNovum.addTickTimerCallback(player.getServerWorld(), new GenericTimer(2, () -> {
               if(entityHitResult.getEntity() instanceof MobEntity mob && mob.isDead()) ArcanaAchievements.grant(player,ArcanaAchievements.CIRCLE_OF_LIFE.id);
            }));
         }
         
         int overhealLvl = arrow.getAugment(ArcanaAugments.OVERHEAL.id);
         float overheal = (float) MathHelper.clamp((damage+player.getHealth()) - player.getMaxHealth(),0,overhealCap[overhealLvl]);
         if(overheal > 0){
            float curAbs = player.getAbsorptionAmount();
            ArcanaNovum.addTickTimerCallback(new OverhealTimerCallback(100,player,overheal));
            SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, 1, 1.8f);
            player.setAbsorptionAmount((curAbs + overheal));
         }

         player.heal((float)damage);
         player.getServerWorld().spawnParticles(ParticleTypes.HEART,player.getX(),player.getY()+player.getHeight()/2,player.getZ(),(int)Math.ceil(damage), .5,.5,.5,1);
      }
   }
   
   @Override
   public void blockHit(RunicArrowEntity arrow, BlockHitResult blockHitResult){}
   
   private MagicItemRecipe makeRecipe(){
      MagicItemIngredient a = MagicItemIngredient.EMPTY;
      MagicItemIngredient c = new MagicItemIngredient(Items.GLISTERING_MELON_SLICE,64,null);
      ItemStack potion6 = new ItemStack(Items.POTION);
      MagicItemIngredient g = new MagicItemIngredient(Items.POTION,1, PotionUtil.setPotion(potion6, Potions.STRONG_HEALING).getNbt());
      MagicItemIngredient h = new MagicItemIngredient(Items.SPECTRAL_ARROW,64,null);
      ItemStack potion8 = new ItemStack(Items.POTION);
      MagicItemIngredient i = new MagicItemIngredient(Items.POTION,1, PotionUtil.setPotion(potion8, Potions.STRONG_HARMING).getNbt());
      MagicItemIngredient k = new MagicItemIngredient(Items.FERMENTED_SPIDER_EYE,64,null);
      GenericMagicIngredient m = new GenericMagicIngredient(ArcanaRegistry.RUNIC_MATRIX,1);
   
      MagicItemIngredient[][] ingredients = {
            {a,a,c,a,a},
            {a,g,h,i,a},
            {k,h,m,h,k},
            {a,i,h,g,a},
            {a,a,c,a,a}};
      return new MagicItemRecipe(ingredients, new ForgeRequirement().withFletchery());
   }
   
   private List<String> makeLore(){
      ArrayList<String> list = new ArrayList<>();
      list.add("{\"text\":\"   Siphoning Arrows\\n\\nRarity: Exotic\\n\\nLife force is something I have rarely explored. I've invoked some simple life runes to draw upon the health lost from my arrows and channel it back to me.\"}");
      return list;
   }
   
   public class SiphoningArrowsItem extends MagicPolymerArrowItem {
      public SiphoningArrowsItem(Settings settings){
         super(getThis(),settings);
      }
      
      
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
   }
}
