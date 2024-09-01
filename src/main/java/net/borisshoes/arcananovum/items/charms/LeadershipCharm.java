package net.borisshoes.arcananovum.items.charms;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerItem;
import net.borisshoes.arcananovum.gui.arcanetome.TomeGui;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.arcananovum.utils.ArcanaRarity;
import net.borisshoes.arcananovum.utils.ParticleEffectUtils;
import net.borisshoes.arcananovum.utils.TextUtils;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class LeadershipCharm extends ArcanaItem {
	public static final String ID = "leadership_charm";
   
   private static final String TXT = "item/leadership_charm";
   
   public LeadershipCharm(){
      id = ID;
      name = "Charm of Leadership";
      rarity = ArcanaRarity.DIVINE;
      categories = new TomeGui.TomeFilter[]{TomeGui.TomeFilter.DIVINE, TomeGui.TomeFilter.CHARMS, TomeGui.TomeFilter.ITEMS};
      vanillaItem = Items.AMETHYST_SHARD;
      item = new LeadershipCharmItem(new Item.Settings().maxCount(1).fireproof()
            .component(DataComponentTypes.ITEM_NAME, Text.literal("Charm of Leadership").formatted(Formatting.BOLD,Formatting.LIGHT_PURPLE))
            .component(DataComponentTypes.LORE, new LoreComponent(getItemLore(null)))
            .component(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true)
      );
      models = new ArrayList<>();
      models.add(new Pair<>(vanillaItem,TXT));
      researchTasks = new RegistryKey[]{ResearchTasks.OBTAIN_LEADERSHIP_CHARM};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getMaxCount());
      setPrefStack(stack);
   }
   
   @Override
   public List<Text> getItemLore(@Nullable ItemStack itemStack){
      List<MutableText> lore = new ArrayList<>();
      lore.add(Text.literal("")
            .append(Text.literal("The ").formatted(Formatting.YELLOW))
            .append(Text.literal("Gods ").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal("have acknowledged ").formatted(Formatting.YELLOW))
            .append(Text.literal("your ").formatted(Formatting.AQUA))
            .append(Text.literal("boundless ").formatted(Formatting.YELLOW))
            .append(Text.literal("courage").formatted(Formatting.GOLD))
            .append(Text.literal("!").formatted(Formatting.YELLOW)));
      lore.add(Text.literal("")
            .append(Text.literal("They grant this ").formatted(Formatting.YELLOW))
            .append(Text.literal("Charm ").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal("to empower ").formatted(Formatting.YELLOW))
            .append(Text.literal("you ").formatted(Formatting.AQUA))
            .append(Text.literal("and your ").formatted(Formatting.YELLOW))
            .append(Text.literal("allies ").formatted(Formatting.AQUA))
            .append(Text.literal("in the coming fight!").formatted(Formatting.YELLOW)));
      lore.add(Text.literal("")
            .append(Text.literal("Grants AoE ").formatted(Formatting.YELLOW))
            .append(Text.literal("regen").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal(", ").formatted(Formatting.YELLOW))
            .append(Text.literal("resistance").formatted(Formatting.BLUE))
            .append(Text.literal(", ").formatted(Formatting.YELLOW))
            .append(Text.literal("strength ").formatted(Formatting.DARK_RED))
            .append(Text.literal("and ").formatted(Formatting.YELLOW))
            .append(Text.literal("mends gear").formatted(Formatting.GREEN))
            .append(Text.literal(".").formatted(Formatting.YELLOW)));
     return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   public List<List<Text>> getBookLore(){
      List<List<Text>> list = new ArrayList<>();
      list.add(List.of(Text.literal(" Charm of Leadership\n\nRarity: Divine\n\nOne of the few known Divine Artifacts gifted by the Gods. \n\nIt grants the wielder and all those nearby Regeneration 2, Strength 2, and Resistance 2. It also mends the gear of all").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal(" Charm of Leadership\n\npeople in the radius.\n\nThe wielder's confidence empowers them to the point of radiating a visible aura.\n\nThey say anything is possible when following a glowing Leader...").formatted(Formatting.BLACK)));
      return list;
   }
   
   public class LeadershipCharmItem extends ArcanaPolymerItem {
      public LeadershipCharmItem(Item.Settings settings){
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
         if(!(world instanceof ServerWorld serverWorld && entity instanceof ServerPlayerEntity player)) return;
         //System.out.println("Ticking Charm of Leadership");
         // Give AoE resistance, regen, and strength, and repair gear.
         int invigor = Math.max(0,ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.INVIGORATION.id));
         
         double effectRange = 8.5+invigor;
         Vec3d playerPos = player.getPos();
         List<ServerPlayerEntity> inRangePlayers = serverWorld.getPlayers(p -> p.squaredDistanceTo(playerPos) <= effectRange*effectRange);
         
         StatusEffectInstance glow = new StatusEffectInstance(StatusEffects.GLOWING, 20 * 5 + 5, 0, false, false, true);
         player.addStatusEffect(glow);
         
         for(ServerPlayerEntity plyr: inRangePlayers){
            //System.out.println(world.isPlayerInRange(playerPos.getX(),playe));
            StatusEffectInstance str = new StatusEffectInstance(StatusEffects.STRENGTH, 20 * 5 + 5, 1+invigor, false, false, true);
            StatusEffectInstance res = new StatusEffectInstance(StatusEffects.RESISTANCE, 20 * 5 + 5, 1+invigor/2, false, false, true);
            StatusEffectInstance regen = new StatusEffectInstance(StatusEffects.REGENERATION, 20 * 5 + 5, 1+invigor, false, false, true);
            plyr.addStatusEffect(str);
            plyr.addStatusEffect(res);
            plyr.addStatusEffect(regen);
            
            // Repair Gear once per second
            if(serverWorld.getServer().getTicks() % 20 == 0){
               // Check each player's inventory for gear that needs repairing
               PlayerInventory inv = plyr.getInventory();
               for(int i = 0; i < inv.size(); i++){
                  ItemStack item = inv.getStack(i);
                  if(item.isEmpty())
                     continue;
                  
                  int durability = item.getDamage();
                  if(durability <= 0)
                     continue;
                  durability = MathHelper.clamp(durability - 15*(1+invigor), 0, Integer.MAX_VALUE);
                  
                  item.setDamage(durability);
               }
            }
         }
         if(inRangePlayers.size() >= 6) ArcanaAchievements.grant(player,ArcanaAchievements.RAID_LEADER.id);
         
         // Particle effects
         if(serverWorld.getServer().getTicks() % 10 == 0){
            double theta = Math.PI/(80)*(serverWorld.getServer().getTicks()%160); // 8 second duration
            ParticleEffectUtils.sphere(serverWorld,null,player.getPos(),ParticleTypes.HAPPY_VILLAGER,effectRange,100,1,0.1,0,theta);
            ParticleEffectUtils.circle(serverWorld,null,player.getPos(),ParticleTypes.HAPPY_VILLAGER,effectRange,100,1,0.1,0);
            for(ServerPlayerEntity plyr : inRangePlayers){
               if(plyr.equals(player))
                  continue;
               serverWorld.spawnParticles(ParticleTypes.HAPPY_VILLAGER, plyr.getX(), plyr.getY()+.75, plyr.getZ(), 4, .2, .2, .2, 10);
               serverWorld.spawnParticles(plyr,ParticleTypes.HAPPY_VILLAGER, false, player.getX(), player.getY()+3, player.getZ(), 5, .1, .3, .1, 10);
            }
         }
      }
   }
}

