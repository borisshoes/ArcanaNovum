package net.borisshoes.arcananovum.items;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.callbacks.VengeanceTotemTimerCallback;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerItem;
import net.borisshoes.arcananovum.damage.ArcanaDamageTypes;
import net.borisshoes.arcananovum.gui.arcanetome.TomeGui;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaIngredient;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaRecipe;
import net.borisshoes.arcananovum.recipes.arcana.ForgeRequirement;
import net.borisshoes.arcananovum.recipes.arcana.SoulstoneIngredient;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.arcananovum.utils.ArcanaRarity;
import net.borisshoes.arcananovum.utils.TextUtils;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.potion.Potions;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stats;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Pair;
import net.minecraft.util.ActionResult;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;
import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;

public class TotemOfVengeance extends ArcanaItem {
	public static final String ID = "totem_of_vengeance";
   
   private static final String TXT = "item/totem_of_vengeance";
   
   public TotemOfVengeance(){
      id = ID;
      name = "Totem Of Vengeance";
      rarity = ArcanaRarity.SOVEREIGN;
      categories = new TomeGui.TomeFilter[]{TomeGui.TomeFilter.SOVEREIGN, TomeGui.TomeFilter.ITEMS,TomeGui.TomeFilter.EQUIPMENT};
      vanillaItem = Items.TOTEM_OF_UNDYING;
      item = new TotemOfVengeanceItem(new Item.Settings().maxCount(1).fireproof()
            .component(DataComponentTypes.ITEM_NAME, Text.translatable("item."+MOD_ID+"."+ID).formatted(Formatting.BOLD,Formatting.DARK_RED))
            .component(DataComponentTypes.LORE, new LoreComponent(getItemLore(null)))
            .component(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true)
      );
      models = new ArrayList<>();
      models.add(new Pair<>(vanillaItem,TXT));
      researchTasks = new RegistryKey[]{ResearchTasks.UNLOCK_SOULSTONE,ResearchTasks.ADVANCEMENT_TOTEM_OF_UNDYING,ResearchTasks.KILL_EVOKER,ResearchTasks.UNLOCK_MIDNIGHT_ENCHANTER,ResearchTasks.EFFECT_STRENGTH,ResearchTasks.EFFECT_FIRE_RESISTANCE,ResearchTasks.EFFECT_SWIFTNESS};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getMaxCount());
      setPrefStack(stack);
   }
   
   @Override
   public List<Text> getItemLore(@Nullable ItemStack itemStack){
      List<MutableText> lore = new ArrayList<>();
      lore.add(Text.literal("")
            .append(Text.literal("This ").formatted(Formatting.DARK_GRAY))
            .append(Text.literal("Totem's ").formatted(Formatting.DARK_RED))
            .append(Text.literal("benevolent protection").formatted(Formatting.GREEN))
            .append(Text.literal(" has been ").formatted(Formatting.DARK_GRAY))
            .append(Text.literal("twisted ").formatted(Formatting.RED))
            .append(Text.literal("by ").formatted(Formatting.DARK_GRAY))
            .append(Text.literal("violence").formatted(Formatting.DARK_RED))
            .append(Text.literal(".").formatted(Formatting.DARK_GRAY)));
      lore.add(Text.literal("")
            .append(Text.literal("Once ").formatted(Formatting.DARK_GRAY))
            .append(Text.literal("regenerative magic").formatted(Formatting.GREEN))
            .append(Text.literal(" is now ").formatted(Formatting.DARK_GRAY))
            .append(Text.literal("fueled ").formatted(Formatting.RED))
            .append(Text.literal("by ").formatted(Formatting.DARK_GRAY))
            .append(Text.literal("rage ").formatted(Formatting.DARK_RED))
            .append(Text.literal("for that which hunts you.").formatted(Formatting.DARK_GRAY)));
      lore.add(Text.literal("")
            .append(Text.literal("Upon ").formatted(Formatting.DARK_GRAY))
            .append(Text.literal("fatal damage").formatted(Formatting.RED))
            .append(Text.literal(", you become ").formatted(Formatting.DARK_GRAY))
            .append(Text.literal("Death Warded").formatted(Formatting.DARK_RED))
            .append(Text.literal(" for a ").formatted(Formatting.DARK_GRAY))
            .append(Text.literal("brief duration").formatted(Formatting.GREEN))
            .append(Text.literal(".").formatted(Formatting.DARK_GRAY)));
      lore.add(Text.literal("")
            .append(Text.literal("You will be ").formatted(Formatting.DARK_GRAY))
            .append(Text.literal("unable ").formatted(Formatting.RED))
            .append(Text.literal("to drop to ").formatted(Formatting.DARK_GRAY))
            .append(Text.literal("zero health").formatted(Formatting.GREEN))
            .append(Text.literal(" and gain an ").formatted(Formatting.DARK_GRAY))
            .append(Text.literal("offensive boost").formatted(Formatting.DARK_RED))
            .append(Text.literal(".").formatted(Formatting.DARK_GRAY)));
      lore.add(Text.literal("")
            .append(Text.literal("If you do not get ").formatted(Formatting.DARK_GRAY))
            .append(Text.literal("revenge ").formatted(Formatting.DARK_RED))
            .append(Text.literal("before the protection ").formatted(Formatting.DARK_GRAY))
            .append(Text.literal("fades").formatted(Formatting.RED))
            .append(Text.literal(", you will ").formatted(Formatting.DARK_GRAY))
            .append(Text.literal("perish").formatted(Formatting.DARK_RED))
            .append(Text.literal(".").formatted(Formatting.DARK_GRAY)));
     return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
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
         PLAYER_DATA.get(player).addXP(1000);
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
   
   
   @Override
	protected ArcanaRecipe makeRecipe(){
      SoulstoneIngredient r = new SoulstoneIngredient(100,false,false,true,null);
      ArcanaIngredient p = new ArcanaIngredient(Items.POTION,1).withPotions(Potions.STRONG_TURTLE_MASTER);
      ArcanaIngredient a = ArcanaIngredient.EMPTY;
      ArcanaIngredient b = new ArcanaIngredient(Items.OBSIDIAN,12);
      ArcanaIngredient t = new ArcanaIngredient(Items.POTION,1).withPotions(Potions.STRONG_SWIFTNESS);
      ArcanaIngredient v = new ArcanaIngredient(Items.POTION,1).withPotions(Potions.STRONG_STRENGTH);
      ArcanaIngredient g = new ArcanaIngredient(Items.NETHER_STAR,1);
      ArcanaIngredient x = new ArcanaIngredient(Items.POTION,1).withPotions(Potions.LONG_FIRE_RESISTANCE);
      ArcanaIngredient k = new ArcanaIngredient(Items.CRYING_OBSIDIAN,8);
      ArcanaIngredient m = new ArcanaIngredient(Items.TOTEM_OF_UNDYING,1, true);
      
      ArcanaIngredient[][] ingredients = {
            {a,b,b,b,a},
            {a,g,b,g,a},
            {k,b,m,b,k},
            {p,k,r,k,t},
            {a,v,k,x,a}};
      return new ArcanaRecipe(ingredients,new ForgeRequirement().withEnchanter());
   }
   
   @Override
   public List<List<Text>> getBookLore(){
      List<List<Text>> list = new ArrayList<>();
      list.add(List.of(Text.literal(" Totem of Vengeance\n\nRarity: Sovereign\n\nTotems of Undying are some of the oldest yet most advanced Arcana I have seen. Ancient, yet powerful.\nI wonder if I can push their capabilities further, perhaps even to the point of immortality?")));
      list.add(List.of(Text.literal(" Totem of Vengeance\n\nMy experiments have yielded disturbing results. The Totems gain their power through soul magic, and the only way to enhance them further is with more soul energy. However, this enhancement begins to twist the Arcana within into something sinister.")));
      list.add(List.of(Text.literal(" Totem of Vengeance\n\nMy new Totem has become overwhelmed by the violence of stolen souls and now seeks vengeance. It will not stop until the soul energy is expended or it succeeds in its task.\nIf it runs out of souls, it will consume mine in the process. ")));
      list.add(List.of(Text.literal(" Totem of Vengeance\n\nUpon taking fatal damage, the Totem prevents me from losing my last bit of health until I get my revenge or the totem expires.\n\nUpon expiration my soul is consumed by an unstoppable force.")));
      list.add(List.of(Text.literal(" Totem of Vengeance\n\nDuring my vengeful rage I become faster and stronger to aid in tracking down the creature that killed me.\nIf I die to environmental causes, there is nothing to get revenge on and the Totem will inevitably claim my soul.")));
      return list;
   }
   
   public class TotemOfVengeanceItem extends ArcanaPolymerItem {
      public TotemOfVengeanceItem(Item.Settings settings){
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
      
      @Override
      public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected){
         if(!ArcanaItemUtils.isArcane(stack)) return;
         if(!(world instanceof ServerWorld && entity instanceof ServerPlayerEntity player)) return;
         
      }
      
      @Override
      public ActionResult use(World world, PlayerEntity playerEntity, Hand hand) {
         return ActionResult.PASS;
      }
   }
}

