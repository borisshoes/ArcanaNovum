package net.borisshoes.arcananovum.items.charms;

import net.borisshoes.arcananovum.ArcanaConfig;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.blocks.GeomanticStele;
import net.borisshoes.arcananovum.blocks.GeomanticSteleBlockEntity;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerItem;
import net.borisshoes.arcananovum.gui.arcanetome.ArcaneTomeGui;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaEffectUtils;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.borislib.conditions.ConditionInstance;
import net.borisshoes.borislib.conditions.Conditions;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;
import static net.borisshoes.arcananovum.ArcanaRegistry.arcanaId;

public class LeadershipCharm extends ArcanaItem implements GeomanticStele.Interaction {
	public static final String ID = "leadership_charm";
   
   public LeadershipCharm(){
      id = ID;
      name = "Charm of Leadership";
      rarity = ArcanaRarity.DIVINE;
      categories = new ArcaneTomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), ArcaneTomeGui.TomeFilter.CHARMS, ArcaneTomeGui.TomeFilter.ITEMS};
      vanillaItem = Items.AMETHYST_SHARD;
      item = new LeadershipCharmItem();
      displayName = Component.translatableWithFallback("item."+MOD_ID+"."+ID,name).withStyle(ChatFormatting.BOLD, ChatFormatting.LIGHT_PURPLE);
      researchTasks = new ResourceKey[]{ResearchTasks.OBTAIN_LEADERSHIP_CHARM};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getDefaultMaxStackSize());
      setPrefStack(stack);
   }
   
   @Override
   public List<Component> getItemLore(@Nullable ItemStack itemStack){
      List<MutableComponent> lore = new ArrayList<>();
      lore.add(Component.literal("")
            .append(Component.literal("The ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("Gods ").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal("have acknowledged ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("your ").withStyle(ChatFormatting.AQUA))
            .append(Component.literal("boundless ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("courage").withStyle(ChatFormatting.GOLD))
            .append(Component.literal("!").withStyle(ChatFormatting.YELLOW)));
      lore.add(Component.literal("")
            .append(Component.literal("They grant this ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("Charm ").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal("to empower ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("you ").withStyle(ChatFormatting.AQUA))
            .append(Component.literal("and your ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("allies ").withStyle(ChatFormatting.AQUA))
            .append(Component.literal("in the coming fight!").withStyle(ChatFormatting.YELLOW)));
      lore.add(Component.literal("")
            .append(Component.literal("Grants AoE ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("regen").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(", ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("resistance").withStyle(ChatFormatting.BLUE))
            .append(Component.literal(", ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("strength ").withStyle(ChatFormatting.DARK_RED))
            .append(Component.literal("and ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("mends gear").withStyle(ChatFormatting.GREEN))
            .append(Component.literal(".").withStyle(ChatFormatting.YELLOW)));
     return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   public List<List<Component>> getBookLore(){
      List<List<Component>> list = new ArrayList<>();
      list.add(List.of(Component.literal("      Charm of\n     Leadership").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD), Component.literal("\nRarity: ").withStyle(ChatFormatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(),false)), Component.literal("\nA rare pendant, gifted only by Divine Entities. Whoever holds the amulet is imbued with a surge of confidence powerful enough to radiate a visible aura. They say anything is possible when ").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("      Charm of\n     Leadership").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD), Component.literal("\nfollowing a glowing leader…\n\nThe Charm grants the wielder and all those nearby a bout of regeneration, strength and resistance. It also has the ability to mend ").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("      Charm of\n     Leadership").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD), Component.literal("\nthe gear of all players in its radius without need for the Mending enchantment. ").withStyle(ChatFormatting.BLACK)));
      return list;
   }
   
   @Override
   public void steleTick(ServerLevel world, GeomanticSteleBlockEntity stele, ItemStack stack, Vec3 range){
      int invigor = ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.INVIGORATION);
      AABB box = new AABB(stele.getBlockPos().getCenter().subtract(range),stele.getBlockPos().getCenter().add(range));
      List<ServerPlayer> inRangePlayers = world.getPlayers(p -> !p.isSpectator() && p.getBoundingBox().intersects(box));
      for(ServerPlayer plyr: inRangePlayers){
         applyEffect(plyr,invigor,null);
      }
      
      if(world.getServer().getTickCount() % 20 == 0){
         double theta = Math.PI/(80)*(world.getServer().getTickCount()%160);
         ArcanaEffectUtils.sphere(world,null,stele.getBlockPos().getCenter().add(0,1,0), ParticleTypes.HAPPY_VILLAGER,1.5,50,1,0.1,0,theta);
      }
   }
   
   @Override
   public Vec3 getBaseRange(){
      return new Vec3(12,8,12);
   }
   
   private void applyEffect(ServerPlayer player, int invigor, @Nullable UUID causingEntity){
      float mightStr = ArcanaNovum.CONFIG.getFloatList(ArcanaConfig.LEADERSHIP_CHARM_MIGHT_PER_LVL).get(invigor);
      float fortStr = ArcanaNovum.CONFIG.getFloatList(ArcanaConfig.LEADERSHIP_CHARM_FORTITUDE_PER_LVL).get(invigor);
      float rejuvStr = ArcanaNovum.CONFIG.getFloatList(ArcanaConfig.LEADERSHIP_CHARM_REJUVENATION_PER_LVL).get(invigor);
      ConditionInstance rejuv = new ConditionInstance(Conditions.REJUVENATION,arcanaId(ArcanaRegistry.LEADERSHIP_CHARM.getId()+"_"+invigor),100,rejuvStr,true,true,false, AttributeModifier.Operation.ADD_VALUE, causingEntity);
      ConditionInstance might = new ConditionInstance(Conditions.MIGHT,arcanaId(ArcanaRegistry.LEADERSHIP_CHARM.getId()+"_"+invigor),100,mightStr,true,true,false, AttributeModifier.Operation.ADD_VALUE, causingEntity);
      ConditionInstance fortitude = new ConditionInstance(Conditions.FORTITUDE,arcanaId(ArcanaRegistry.LEADERSHIP_CHARM.getId()+"_"+invigor),100, -(fortStr),true,true,false, AttributeModifier.Operation.ADD_VALUE, causingEntity);
      Conditions.addCondition(player.level().getServer(),player,rejuv);
      Conditions.addCondition(player.level().getServer(),player,might);
      Conditions.addCondition(player.level().getServer(),player,fortitude);
      
      MobEffectInstance str = new MobEffectInstance(MobEffects.STRENGTH, 20 * 5 + 5, 1+invigor, false, false, true);
      MobEffectInstance res = new MobEffectInstance(MobEffects.RESISTANCE, 20 * 5 + 5, 1+invigor/2, false, false, true);
      MobEffectInstance regen = new MobEffectInstance(MobEffects.REGENERATION, 20 * 5 + 5, 1+invigor, false, false, true);
      player.addEffect(str);
      player.addEffect(res);
      player.addEffect(regen);
      
      // Repair Gear once per second
      if(player.level().getServer().getTickCount() % 20 == 0){
         // Check each player's inventory for gear that needs repairing
         Inventory inv = player.getInventory();
         for(int i = 0; i < inv.getContainerSize(); i++){
            ItemStack item = inv.getItem(i);
            if(item.isEmpty())
               continue;
            
            int durability = item.getDamageValue();
            if(durability <= 0)
               continue;
            durability = Mth.clamp(durability - 15*(1+invigor), 0, Integer.MAX_VALUE);
            
            item.setDamageValue(durability);
         }
      }
      if(player.level().getServer().getTickCount() % 10 == 0){
         player.level().sendParticles(ParticleTypes.HAPPY_VILLAGER, player.getX(), player.getY()+.75, player.getZ(), 4, .2, .2, .2, 10);
      }
   }
   
   public class LeadershipCharmItem extends ArcanaPolymerItem {
      public LeadershipCharmItem(){
         super(getThis());
      }
      
      @Override
      public ItemStack getDefaultInstance(){
         return prefItem;
      }
      
      @Override
      public void inventoryTick(ItemStack stack, ServerLevel world, Entity entity, @Nullable EquipmentSlot slot){
         if(!ArcanaItemUtils.isArcane(stack)) return;
         if(!(world instanceof ServerLevel serverWorld && entity instanceof ServerPlayer player)) return;
         // Give AoE resistance, regen, and strength, and repair gear.
         int invigor = ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.INVIGORATION);
         double baseRange = ArcanaNovum.CONFIG.getDouble(ArcanaConfig.LEADERSHIP_CHARM_RADIUS);
         double extraRange = ArcanaNovum.CONFIG.getDoubleList(ArcanaConfig.LEADERSHIP_CHARM_INVIGORATION_RADIUS_PER_LVL).get(invigor);
         
         double effectRange = baseRange+extraRange;
         Vec3 playerPos = player.position();
         List<ServerPlayer> inRangePlayers = serverWorld.getPlayers(p -> !p.isSpectator() && p.distanceToSqr(playerPos) <= effectRange*effectRange);
         
         MobEffectInstance glow = new MobEffectInstance(MobEffects.GLOWING, 20 * 5 + 5, 0, false, false, true);
         player.addEffect(glow);
         
         for(ServerPlayer plyr: inRangePlayers){
            applyEffect(plyr,invigor,entity.getUUID());
            if(world.getServer().getTickCount() % 10 == 0){
               world.sendParticles(player, ParticleTypes.HAPPY_VILLAGER, false,true, player.getX(), player.getY()+3, player.getZ(), 5, .1, .3, .1, 10);
            }
         }
         if(inRangePlayers.size() >= 6) ArcanaAchievements.grant(player,ArcanaAchievements.RAID_LEADER);
         
         // Particle effects
         if(serverWorld.getServer().getTickCount() % 10 == 0){
            double theta = Math.PI/(80)*(serverWorld.getServer().getTickCount()%160); // 8 second duration
            ArcanaEffectUtils.sphere(serverWorld,null,player.position(), ParticleTypes.HAPPY_VILLAGER,effectRange,100,1,0.1,0,theta);
            ArcanaEffectUtils.circle(serverWorld,null,player.position(), ParticleTypes.HAPPY_VILLAGER,effectRange,100,1,0.1,0);
         }
      }
   }
}

