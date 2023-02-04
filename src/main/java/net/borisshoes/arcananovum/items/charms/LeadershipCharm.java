package net.borisshoes.arcananovum.items.charms;

import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.items.ArcaneTome;
import net.borisshoes.arcananovum.items.core.MagicItem;
import net.borisshoes.arcananovum.items.core.TickingItem;
import net.borisshoes.arcananovum.utils.MagicRarity;
import net.borisshoes.arcananovum.utils.ParticleEffectUtils;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class LeadershipCharm extends MagicItem implements TickingItem {
   
   private int tickCount;
   
   public LeadershipCharm(){
      id = "leadership_charm";
      name = "Charm of Leadership";
      rarity = MagicRarity.MYTHICAL;
      categories = new ArcaneTome.TomeFilter[]{ArcaneTome.TomeFilter.MYTHICAL, ArcaneTome.TomeFilter.CHARMS, ArcaneTome.TomeFilter.ITEMS};
      
      ItemStack item = new ItemStack(Items.AMETHYST_SHARD);
      NbtCompound tag = item.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList loreList = new NbtList();
      NbtList enchants = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name","[{\"text\":\"Charm of Leadership\",\"italic\":false,\"bold\":true,\"color\":\"light_purple\"}]");
      loreList.add(NbtString.of("[{\"text\":\"The \",\"italic\":false,\"color\":\"yellow\"},{\"text\":\"Gods \",\"color\":\"light_purple\"},{\"text\":\"have acknowledged \"},{\"text\":\"your \",\"color\":\"aqua\"},{\"text\":\"boundless \"},{\"text\":\"courage\",\"color\":\"gold\"},{\"text\":\"!\",\"color\":\"yellow\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"They grant this \",\"italic\":false,\"color\":\"yellow\"},{\"text\":\"Charm \",\"color\":\"light_purple\"},{\"text\":\"to empower \"},{\"text\":\"you \",\"color\":\"aqua\"},{\"text\":\"and your \"},{\"text\":\"allies \",\"color\":\"aqua\"},{\"text\":\"in the coming fight!\",\"color\":\"yellow\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Grants AoE \",\"italic\":false,\"color\":\"yellow\"},{\"text\":\"regen\",\"color\":\"light_purple\"},{\"text\":\", \"},{\"text\":\"resistance\",\"color\":\"blue\"},{\"text\":\", \"},{\"text\":\"strength \",\"color\":\"dark_red\"},{\"text\":\"and \"},{\"text\":\"mends gear\",\"color\":\"green\"},{\"text\":\".\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Mythical \",\"italic\":false,\"color\":\"light_purple\",\"bold\":true},{\"text\":\"Magic Item\",\"italic\":false,\"color\":\"dark_purple\",\"bold\":false}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
      tag.put("Enchantments",enchants);
      
      setBookLore(makeLore());
      prefNBT = addMagicNbt(tag);
      item.setNbt(prefNBT);
      prefItem = item;
      
      tickCount = 0;
   }
   
   @Override
   public void onTick(ServerWorld world, ServerPlayerEntity player, ItemStack itemStack){
      //System.out.println("Ticking Charm of Leadership");
      // Give AoE resistance, regen, and strength, and repair gear.
      int invigor = Math.max(0,ArcanaAugments.getAugmentOnItem(itemStack,"invigoration"));
      
      double effectRange = 8.5+invigor;
      Vec3d playerPos = player.getPos();
      List<ServerPlayerEntity> inRangePlayers = world.getPlayers(p -> p.squaredDistanceTo(playerPos) <= effectRange*effectRange);
      
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
         if(tickCount % 20 == 0){
            // Check each player's inventory for gear that needs repairing
            PlayerInventory inv = plyr.getInventory();
            for(int i = 0; i < inv.size(); i++){
               ItemStack item = inv.getStack(i);
               if(item.isEmpty())
                  continue;
               NbtCompound nbt = item.getNbt();
               int durability = nbt != null ? nbt.getInt("Damage") : 0;
               if(durability <= 0)
                  continue;
               durability = MathHelper.clamp(durability - 15*(1+invigor), 0, Integer.MAX_VALUE);
               
               nbt.putInt("Damage", durability);
               item.setNbt(nbt);
            }
         }
      }
      if(inRangePlayers.size() >= 6) ArcanaAchievements.grant(player,"raid_leader");
      
      // Particle effects
      if(tickCount % 10 == 0){
         double theta = Math.PI/(80)*(tickCount%160); // 8 second duration
         ParticleEffectUtils.sphere(world,null,player.getPos(),ParticleTypes.HAPPY_VILLAGER,effectRange,100,1,0.1,0,theta);
         ParticleEffectUtils.circle(world,null,player.getPos(),ParticleTypes.HAPPY_VILLAGER,effectRange,100,1,0.1,0);
         for(ServerPlayerEntity plyr : inRangePlayers){
            if(plyr.equals(player))
               continue;
            world.spawnParticles(ParticleTypes.HAPPY_VILLAGER, plyr.getX(), plyr.getY()+.75, plyr.getZ(), 4, .2, .2, .2, 10);
            world.spawnParticles(plyr,ParticleTypes.HAPPY_VILLAGER, false, player.getX(), player.getY()+3, player.getZ(), 5, .1, .3, .1, 10);
         }
      }
      
      if(tickCount > 360)
         tickCount = 0;
      tickCount++;
   }
   
   private List<String> makeLore(){
      ArrayList<String> list = new ArrayList<>();
      list.add("{\"text\":\" Charm of Leadership\\n\\nRarity: Mythical\\n\\nOne of the few known Mythical Artifacts gifted by the Gods. \\n\\nIt grants the wielder and all those nearby Regeneration 2, Strength 2, and Resistance 2. It also mends the gear of all\"}");
      list.add("{\"text\":\" Charm of Leadership\\n\\npeople in the radius.\\n\\nThe wielder's confidence empowers them to the point of radiating a visible aura.\\n\\nThey say anything is possible when following a glowing Leader...\"}");
      return list;
   }
}
