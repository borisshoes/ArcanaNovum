package net.borisshoes.arcananovum.items;

import net.borisshoes.arcananovum.ArcanaConfig;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugment;
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
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.DeathProtectionComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.consume.ApplyEffectsConsumeEffect;
import net.minecraft.item.consume.ClearAllEffectsConsumeEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.potion.Potions;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class TotemOfVengeance extends ArcanaItem {
	public static final String ID = "totem_of_vengeance";
   
   public TotemOfVengeance(){
      id = ID;
      name = "Totem Of Vengeance";
      rarity = ArcanaRarity.SOVEREIGN;
      categories = new TomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), TomeGui.TomeFilter.ITEMS,TomeGui.TomeFilter.EQUIPMENT};
      vanillaItem = Items.TOTEM_OF_UNDYING;
      item = new TotemOfVengeanceItem(addArcanaItemComponents(new Item.Settings().maxCount(1)
            .component(DataComponentTypes.DEATH_PROTECTION, getTotemComponent(0,false))
      ));
      displayName = Text.translatableWithFallback("item."+MOD_ID+"."+ID,name).formatted(Formatting.BOLD,Formatting.DARK_RED);
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
   
   private static DeathProtectionComponent getTotemComponent(int furyLevel, boolean byPlayer){
      int duration = byPlayer ? 300*(furyLevel+1) / 2 : 300*(furyLevel+1);
      return new DeathProtectionComponent(
            List.of(
                  new ClearAllEffectsConsumeEffect(),
                  new ApplyEffectsConsumeEffect(
                        List.of(
                              new StatusEffectInstance(ArcanaRegistry.DEATH_WARD_EFFECT, duration, 1),
                              new StatusEffectInstance(StatusEffects.STRENGTH, duration, furyLevel+1),
                              new StatusEffectInstance(StatusEffects.SPEED, duration, furyLevel+1)
                        )
                  )
            )
      );
   }
   
   public ItemStack upgradeLevel(ItemStack stack, boolean byPlayer){
      if(!(ArcanaItemUtils.identifyItem(stack) instanceof TotemOfVengeance)) return stack;
      int furyLevel = Math.max(0,ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.RETALIATIVE_FURY.id));
      stack.set(DataComponentTypes.DEATH_PROTECTION, getTotemComponent(furyLevel, byPlayer));
      return stack;
   }
   
   public void triggerTotem(ItemStack stack, LivingEntity living, DamageSource source){
      if(living instanceof ServerPlayerEntity player){
         int furyLvl = Math.max(0,ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.RETALIATIVE_FURY.id));
         Entity attacker = source.getAttacker() != null ? source.getAttacker() : player.getPrimeAdversary() != null ? player.getPrimeAdversary() : null;
         boolean byPlayer = attacker instanceof PlayerEntity;
         stack.set(DataComponentTypes.DEATH_PROTECTION, getTotemComponent(furyLvl, byPlayer));
         ArcanaNovum.addTickTimerCallback(new VengeanceTotemTimerCallback(byPlayer ? 300*(furyLvl+1) / 2 : 300*(furyLvl+1),stack,player,attacker));
         player.getServerWorld().spawnParticles(ParticleTypes.ANGRY_VILLAGER,player.getPos().x,player.getPos().y+player.getHeight()/2,player.getPos().z,25,.5,.6,.5,0.05);
         
         if(source.isOf(ArcanaDamageTypes.VENGEANCE_TOTEM)){
            ArcanaAchievements.grant(player,ArcanaAchievements.REVENGEANCE.id);
         }
         
         ArcanaAchievements.progress(player,ArcanaAchievements.TOO_ANGRY_TO_DIE.id,0); // Start the timer
         ArcanaNovum.data(player).addXP(ArcanaConfig.getInt(ArcanaRegistry.TOTEM_OF_VENGEANCE_ACTIVATE));
      }
   }
   
   
   @Override
   public ItemStack onAugment(ItemStack stack, ArcanaAugment augment, int level){
      if(ArcanaItemUtils.identifyItem(stack) instanceof TotemOfVengeance && augment == ArcanaAugments.RETALIATIVE_FURY){
         return upgradeLevel(stack,false);
      }
      return stack;
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
      list.add(List.of(Text.literal("      Totem Of\n     Vengeance").formatted(Formatting.DARK_RED,Formatting.BOLD),Text.literal("\nRarity: ").formatted(Formatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(),false)),Text.literal("\nTotems of Undying are some of the oldest, yet most advanced Arcana I have seen. Ancient, yet powerful. I wonder if I can push their capabilities further, perhaps even to the point of ").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal("      Totem Of\n     Vengeance").formatted(Formatting.DARK_RED,Formatting.BOLD),Text.literal("\nimmortality?\n\nMy experiments have yielded disturbing results. The Totems use soul magic, and the only way to enhance them is to use some of my tricks from the Nether to add more soul energy.  ").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal("      Totem Of\n     Vengeance").formatted(Formatting.DARK_RED,Formatting.BOLD),Text.literal("\nHowever, this condensation of Nether soul magic into the ancient design of the Totem twists it into something sinister. My new Totem has become overwhelmed by the violence of stolen souls and now it seeks vengeance.").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal("      Totem Of\n     Vengeance").formatted(Formatting.DARK_RED,Formatting.BOLD),Text.literal("\nIt will not stop until the soul energy is expended or it succeeds in its task. If it runs out of souls then it will consume mine in the process.\n\nUpon taking fatal damage, the Totem prevents me from ").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal("      Totem Of\n     Vengeance").formatted(Formatting.DARK_RED,Formatting.BOLD),Text.literal("\nlosing my last bit of health until I get my revenge, or the totem expires. Upon expiration, my soul is consumed by the Totem’s unstoppable rage. During my vengeful rage, I become faster and stronger to aid in ").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal("      Totem Of\n     Vengeance").formatted(Formatting.DARK_RED,Formatting.BOLD),Text.literal("\ntracking down the creature that killed me. A death to purely environmental causes activates the Totem, but does not claim my soul upon expiration.\nA death to a fellow player results in a halved duration of the Totem. ").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal("      Totem Of\n     Vengeance").formatted(Formatting.DARK_RED,Formatting.BOLD),Text.literal("\nA second Totem of Vengeance can be used to prolong the effect.").formatted(Formatting.BLACK)));
      return list;
   }
   
   public class TotemOfVengeanceItem extends ArcanaPolymerItem {
      public TotemOfVengeanceItem(Item.Settings settings){
         super(getThis(),settings);
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
      public ActionResult use(World world, PlayerEntity playerEntity, Hand hand){
         return ActionResult.PASS;
      }
   }
}

