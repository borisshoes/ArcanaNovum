package net.borisshoes.arcananovum.items;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugment;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.callbacks.VengeanceTotemTimerCallback;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerItem;
import net.borisshoes.arcananovum.damage.ArcanaDamageTypes;
import net.borisshoes.arcananovum.gui.arcanetome.ArcaneTomeGui;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.borislib.BorisLib;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.DeathProtection;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.item.consume_effects.ClearAllStatusEffectsConsumeEffect;
import net.minecraft.world.level.Level;
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
      categories = new ArcaneTomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), ArcaneTomeGui.TomeFilter.ITEMS,ArcaneTomeGui.TomeFilter.EQUIPMENT};
      vanillaItem = Items.TOTEM_OF_UNDYING;
      item = new TotemOfVengeanceItem();
      displayName = Component.translatableWithFallback("item."+MOD_ID+"."+ID,name).withStyle(ChatFormatting.BOLD, ChatFormatting.DARK_RED);
      researchTasks = new ResourceKey[]{ResearchTasks.UNLOCK_SOULSTONE,ResearchTasks.ADVANCEMENT_TOTEM_OF_UNDYING,ResearchTasks.KILL_EVOKER,ResearchTasks.UNLOCK_MIDNIGHT_ENCHANTER,ResearchTasks.EFFECT_STRENGTH,ResearchTasks.EFFECT_FIRE_RESISTANCE,ResearchTasks.EFFECT_SWIFTNESS};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getDefaultMaxStackSize());
      setPrefStack(stack);
   }
   
   @Override
   public List<Component> getItemLore(@Nullable ItemStack itemStack){
      List<MutableComponent> lore = new ArrayList<>();
      lore.add(Component.literal("")
            .append(Component.literal("This ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("Totem's ").withStyle(ChatFormatting.DARK_RED))
            .append(Component.literal("benevolent protection").withStyle(ChatFormatting.GREEN))
            .append(Component.literal(" has been ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("twisted ").withStyle(ChatFormatting.RED))
            .append(Component.literal("by ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("violence").withStyle(ChatFormatting.DARK_RED))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_GRAY)));
      lore.add(Component.literal("")
            .append(Component.literal("Once ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("regenerative magic").withStyle(ChatFormatting.GREEN))
            .append(Component.literal(" is now ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("fueled ").withStyle(ChatFormatting.RED))
            .append(Component.literal("by ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("rage ").withStyle(ChatFormatting.DARK_RED))
            .append(Component.literal("for that which hunts you.").withStyle(ChatFormatting.DARK_GRAY)));
      lore.add(Component.literal("")
            .append(Component.literal("Upon ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("fatal damage").withStyle(ChatFormatting.RED))
            .append(Component.literal(", you become ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("Death Warded").withStyle(ChatFormatting.DARK_RED))
            .append(Component.literal(" for a ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("brief duration").withStyle(ChatFormatting.GREEN))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_GRAY)));
      lore.add(Component.literal("")
            .append(Component.literal("You will be ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("unable ").withStyle(ChatFormatting.RED))
            .append(Component.literal("to drop to ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("zero health").withStyle(ChatFormatting.GREEN))
            .append(Component.literal(" and gain an ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("offensive boost").withStyle(ChatFormatting.DARK_RED))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_GRAY)));
      lore.add(Component.literal("")
            .append(Component.literal("If you do not get ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("revenge ").withStyle(ChatFormatting.DARK_RED))
            .append(Component.literal("before the protection ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("fades").withStyle(ChatFormatting.RED))
            .append(Component.literal(", you will ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("perish").withStyle(ChatFormatting.DARK_RED))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_GRAY)));
     return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   private static DeathProtection getTotemComponent(int furyLevel, boolean byPlayer){
      int duration = byPlayer ? 300*(furyLevel+1) / 2 : 300*(furyLevel+1);
      return new DeathProtection(
            List.of(
                  new ClearAllStatusEffectsConsumeEffect(),
                  new ApplyStatusEffectsConsumeEffect(
                        List.of(
                              new MobEffectInstance(ArcanaRegistry.DEATH_WARD_EFFECT, duration, 1),
                              new MobEffectInstance(MobEffects.STRENGTH, duration, furyLevel+1),
                              new MobEffectInstance(MobEffects.SPEED, duration, furyLevel+1)
                        )
                  )
            )
      );
   }
   
   public ItemStack upgradeLevel(ItemStack stack, boolean byPlayer){
      if(!(ArcanaItemUtils.identifyItem(stack) instanceof TotemOfVengeance)) return stack;
      int furyLevel = Math.max(0,ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.RETALIATIVE_FURY));
      stack.set(DataComponents.DEATH_PROTECTION, getTotemComponent(furyLevel, byPlayer));
      return stack;
   }
   
   public void triggerTotem(ItemStack stack, LivingEntity living, DamageSource source){
      if(living instanceof ServerPlayer player){
         int furyLvl = Math.max(0,ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.RETALIATIVE_FURY));
         Entity attacker = source.getEntity() != null ? source.getEntity() : player.getKillCredit() != null ? player.getKillCredit() : null;
         boolean byPlayer = attacker instanceof Player;
         stack.set(DataComponents.DEATH_PROTECTION, getTotemComponent(furyLvl, byPlayer));
         BorisLib.addTickTimerCallback(new VengeanceTotemTimerCallback(byPlayer ? 300*(furyLvl+1) / 2 : 300*(furyLvl+1),stack,player,attacker));
         player.level().sendParticles(ParticleTypes.ANGRY_VILLAGER,player.position().x,player.position().y+player.getBbHeight()/2,player.position().z,25,.5,.6,.5,0.05);
         
         if(source.is(ArcanaDamageTypes.VENGEANCE_TOTEM)){
            ArcanaAchievements.grant(player,ArcanaAchievements.REVENGEANCE);
         }
         
         ArcanaAchievements.progress(player,ArcanaAchievements.TOO_ANGRY_TO_DIE,0); // Start the timer
         ArcanaNovum.data(player).addXP(ArcanaNovum.CONFIG.getInt(ArcanaRegistry.XP_TOTEM_OF_VENGEANCE_ACTIVATE));
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
   public List<List<Component>> getBookLore(){
      List<List<Component>> list = new ArrayList<>();
      list.add(List.of(Component.literal("      Totem Of\n     Vengeance").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD), Component.literal("\nRarity: ").withStyle(ChatFormatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(),false)), Component.literal("\nTotems of Undying are some of the oldest, yet most advanced Arcana I have seen. Ancient, yet powerful. I wonder if I can push their capabilities further, perhaps even to the point of ").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("      Totem Of\n     Vengeance").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD), Component.literal("\nimmortality?\n\nMy experiments have yielded disturbing results. The Totems use soul magic, and the only way to enhance them is to use some of my tricks from the Nether to add more soul energy.  ").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("      Totem Of\n     Vengeance").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD), Component.literal("\nHowever, this condensation of Nether soul magic into the ancient design of the Totem twists it into something sinister. My new Totem has become overwhelmed by the violence of stolen souls and now it seeks vengeance.").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("      Totem Of\n     Vengeance").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD), Component.literal("\nIt will not stop until the soul energy is expended or it succeeds in its task. If it runs out of souls then it will consume mine in the process.\n\nUpon taking fatal damage, the Totem prevents me from ").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("      Totem Of\n     Vengeance").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD), Component.literal("\nlosing my last bit of health until I get my revenge, or the totem expires. Upon expiration, my soul is consumed by the Totem’s unstoppable rage. During my vengeful rage, I become faster and stronger to aid in ").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("      Totem Of\n     Vengeance").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD), Component.literal("\ntracking down the creature that killed me. A death to purely environmental causes activates the Totem, but does not claim my soul upon expiration.\nA death to a fellow player results in a halved duration of the Totem. ").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("      Totem Of\n     Vengeance").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD), Component.literal("\nA second Totem of Vengeance can be used to prolong the effect.").withStyle(ChatFormatting.BLACK)));
      return list;
   }
   
   public class TotemOfVengeanceItem extends ArcanaPolymerItem {
      public TotemOfVengeanceItem(){
         super(getThis(),getArcanaItemComponents()
               .component(DataComponents.DEATH_PROTECTION, getTotemComponent(0,false))
         );
      }
      
      @Override
      public ItemStack getDefaultInstance(){
         return prefItem;
      }
      
      @Override
      public void inventoryTick(ItemStack stack, ServerLevel world, Entity entity, @Nullable EquipmentSlot slot){
         if(!ArcanaItemUtils.isArcane(stack)) return;
         if(!(entity instanceof ServerPlayer player)) return;
         
      }
      
      @Override
      public InteractionResult use(Level world, Player playerEntity, InteractionHand hand){
         return InteractionResult.PASS;
      }
   }
}

