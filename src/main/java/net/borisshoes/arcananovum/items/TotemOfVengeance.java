package net.borisshoes.arcananovum.items;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.callbacks.ShieldTimerCallback;
import net.borisshoes.arcananovum.callbacks.VengeanceTotemTimerCallback;
import net.borisshoes.arcananovum.core.MagicItem;
import net.borisshoes.arcananovum.core.polymer.MagicPolymerItem;
import net.borisshoes.arcananovum.damage.ArcanaDamageTypes;
import net.borisshoes.arcananovum.recipes.arcana.*;
import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.borisshoes.arcananovum.utils.MagicRarity;
import net.borisshoes.arcananovum.utils.SoundUtils;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.Pair;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class TotemOfVengeance extends MagicItem {
   
   private static final String TXT = "item/totem_of_vengeance";
   
   public TotemOfVengeance(){
      id = "totem_of_vengeance";
      name = "Totem Of Vengeance";
      rarity = MagicRarity.LEGENDARY;
      categories = new ArcaneTome.TomeFilter[]{ArcaneTome.TomeFilter.LEGENDARY, ArcaneTome.TomeFilter.ITEMS,ArcaneTome.TomeFilter.EQUIPMENT};
      vanillaItem = Items.TOTEM_OF_UNDYING;
      item = new TotemOfVengeanceItem(new FabricItemSettings().maxCount(1).fireproof());
      models = new ArrayList<>();
      models.add(new Pair<>(vanillaItem,TXT));
      
      ItemStack stack = new ItemStack(item);
      NbtCompound tag = stack.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList enchants = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name","[{\"text\":\"Totem of Vengeance\",\"italic\":false,\"bold\":true,\"color\":\"dark_red\"}]");
      tag.put("display",display);
      tag.put("Enchantments",enchants);
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
      loreList.add(NbtString.of("[{\"text\":\"This \",\"italic\":false,\"color\":\"dark_gray\"},{\"text\":\"Totem's \",\"color\":\"dark_red\"},{\"text\":\"benevolent protection\",\"color\":\"green\"},{\"text\":\" has been \"},{\"text\":\"twisted \",\"color\":\"red\"},{\"text\":\"by \"},{\"text\":\"violence\",\"color\":\"dark_red\"},{\"text\":\".\",\"color\":\"dark_gray\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Once \",\"italic\":false,\"color\":\"dark_gray\"},{\"text\":\"regenerative magic\",\"color\":\"green\"},{\"text\":\" is now \"},{\"text\":\"fueled \",\"color\":\"red\"},{\"text\":\"by \"},{\"text\":\"rage \",\"color\":\"dark_red\"},{\"text\":\"for that which hunts you.\",\"color\":\"dark_gray\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Upon \",\"italic\":false,\"color\":\"dark_gray\"},{\"text\":\"fatal damage\",\"color\":\"red\"},{\"text\":\", you become \"},{\"text\":\"Death Warded\",\"color\":\"dark_red\"},{\"text\":\" for a \"},{\"text\":\"brief duration\",\"color\":\"green\"},{\"text\":\".\",\"color\":\"dark_gray\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"You will be \",\"italic\":false,\"color\":\"dark_gray\"},{\"text\":\"unable \",\"color\":\"red\"},{\"text\":\"to drop to \"},{\"text\":\"zero health\",\"color\":\"green\"},{\"text\":\" and gain an \"},{\"text\":\"offensive boost\",\"color\":\"dark_red\"},{\"text\":\".\",\"color\":\"dark_gray\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"If you do not get \",\"italic\":false,\"color\":\"dark_gray\"},{\"text\":\"revenge \",\"color\":\"dark_red\"},{\"text\":\"before the protection \"},{\"text\":\"fades\",\"color\":\"red\"},{\"text\":\", you will \"},{\"text\":\"perish\",\"color\":\"dark_red\"},{\"text\":\".\"}]"));
      return loreList;
   }
   
   public boolean tryUseTotem(ItemStack stack, LivingEntity living, DamageSource source){
      if (source.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY) && !source.isOf(ArcanaDamageTypes.VENGEANCE_TOTEM)) {
         return false;
      }
      
      int furyLvl = Math.max(0,ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.RETALIATIVE_FURY.id));
      if (living instanceof ServerPlayerEntity player) {
         player.incrementStat(Stats.USED.getOrCreateStat(ArcanaRegistry.TOTEM_OF_VENGEANCE.getItem()));
         Criteria.USED_TOTEM.trigger(player, stack);
         player.emitGameEvent(GameEvent.ITEM_INTERACT_FINISH);
         
         Entity attacker = source.getAttacker();
         ArcanaNovum.addTickTimerCallback(new VengeanceTotemTimerCallback(300*(furyLvl+1),stack,player,attacker));
         player.getServerWorld().spawnParticles(ParticleTypes.ANGRY_VILLAGER,player.getPos().x,player.getPos().y+player.getHeight()/2,player.getPos().z,25,.5,.6,.5,0.05);
         
         if(source.isOf(ArcanaDamageTypes.VENGEANCE_TOTEM)){
            ArcanaAchievements.grant(player,ArcanaAchievements.REVENGEANCE.id);
         }
         
         ArcanaAchievements.progress(player,ArcanaAchievements.TOO_ANGRY_TO_DIE.id,0); // Start the timer
      }
      stack.decrement(1);
      living.setHealth(1.0f);
      living.clearStatusEffects();
      living.addStatusEffect(new StatusEffectInstance(ArcanaRegistry.DEATH_WARD_EFFECT, 300*(furyLvl+1), 0));
      living.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 300*(furyLvl+1), furyLvl+1));
      living.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 300*(furyLvl+1), furyLvl+1));
      living.getWorld().sendEntityStatus(living, EntityStatuses.USE_TOTEM_OF_UNDYING);
      return true;
   }
   
   
   private MagicItemRecipe makeRecipe(){
      MagicItemIngredient a = new MagicItemIngredient(Items.NETHER_STAR,1,null);
      MagicItemIngredient b = new MagicItemIngredient(Items.CRYING_OBSIDIAN,16,null);
      ItemStack potion2 = new ItemStack(Items.POTION);
      MagicItemIngredient c = new MagicItemIngredient(Items.POTION,1, PotionUtil.setPotion(potion2, Potions.STRONG_TURTLE_MASTER).getNbt());
      MagicItemIngredient f = new MagicItemIngredient(Items.OBSIDIAN,16,null);
      ItemStack potion6 = new ItemStack(Items.POTION);
      MagicItemIngredient g = new MagicItemIngredient(Items.POTION,1, PotionUtil.setPotion(potion6, Potions.STRONG_STRENGTH).getNbt());
      MagicItemIngredient h = new MagicItemIngredient(Items.TOTEM_OF_UNDYING,1,null, true);
      ItemStack potion8 = new ItemStack(Items.POTION);
      MagicItemIngredient i = new MagicItemIngredient(Items.POTION,1, PotionUtil.setPotion(potion8, Potions.STRONG_SWIFTNESS).getNbt());
      ItemStack potion10 = new ItemStack(Items.POTION);
      MagicItemIngredient k = new MagicItemIngredient(Items.POTION,1, PotionUtil.setPotion(potion10, Potions.LONG_FIRE_RESISTANCE).getNbt());
      SoulstoneIngredient m = new SoulstoneIngredient(100,false,false,true,null);
      
      MagicItemIngredient[][] ingredients = {
            {a,b,c,b,a},
            {f,g,h,i,f},
            {k,h,m,h,k},
            {f,i,h,g,f},
            {a,b,c,b,a}};
      return new MagicItemRecipe(ingredients,new ForgeRequirement().withSingularity());
      
   }
   
   private List<String> makeLore(){
      ArrayList<String> list = new ArrayList<>();
      list.add("\" Totem of Vengeance\\n\\nRarity: Legendary\\n\\nTotems of Undying are some of the oldest yet most advanced Arcana I have seen. Ancient, yet powerful.\\nI wonder if I can push their capabilities further, perhaps even to the point of immortality?\"");
      list.add("\" Totem of Vengeance\\n\\nMy experiments have yielded disturbing results. The Totems gain their power through soul magic, and the only way to enhance them further is with more soul energy. However, this enhancement begins to twist the Arcana within into something sinister.\"");
      list.add("\" Totem of Vengeance\\n\\nMy new Totem has become overwhelmed by the violence of stolen souls and now seeks vengeance. It will not stop until the soul energy is expended or it succeeds in its task.\\nIf it runs out of souls, it will consume mine in the process. \"");
      list.add("\" Totem of Vengeance\\n\\nUpon taking fatal damage, the Totem prevents me from losing my last bit of health until I get my revenge or the totem expires.\\n\\nUpon expiration my soul is consumed by an unstoppable force.\"");
      list.add("\" Totem of Vengeance\\n\\nDuring my vengeful rage I become faster and stronger to aid in tracking down the creature that killed me.\\nIf I die to environmental causes, there is nothing to get revenge on and the Totem will inevitably claim my soul.\"");
      return list;
   }
   
   public class TotemOfVengeanceItem extends MagicPolymerItem {
      public TotemOfVengeanceItem(Settings settings){
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
      
      @Override
      public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected){
         if(!MagicItemUtils.isMagic(stack)) return;
         if(!(world instanceof ServerWorld && entity instanceof ServerPlayerEntity player)) return;
         
      }
      
      @Override
      public TypedActionResult<ItemStack> use(World world, PlayerEntity playerEntity, Hand hand) {
         return TypedActionResult.pass(playerEntity.getStackInHand(hand));
      }
   }
}
