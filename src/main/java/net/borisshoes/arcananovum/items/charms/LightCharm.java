package net.borisshoes.arcananovum.items.charms;

import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.items.ArcaneTome;
import net.borisshoes.arcananovum.items.core.MagicItem;
import net.borisshoes.arcananovum.items.core.TickingItem;
import net.borisshoes.arcananovum.items.core.UsableItem;
import net.borisshoes.arcananovum.recipes.MagicItemIngredient;
import net.borisshoes.arcananovum.recipes.MagicItemRecipe;
import net.borisshoes.arcananovum.utils.MagicRarity;
import net.borisshoes.arcananovum.utils.SoundUtils;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

import java.util.ArrayList;
import java.util.List;

import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;

public class LightCharm extends MagicItem implements TickingItem, UsableItem {
   public LightCharm(){
      id = "light_charm";
      name = "Charm of Light";
      rarity = MagicRarity.EMPOWERED;
      categories = new ArcaneTome.TomeFilter[]{ArcaneTome.TomeFilter.EMPOWERED, ArcaneTome.TomeFilter.CHARMS, ArcaneTome.TomeFilter.ITEMS};
      
      ItemStack item = new ItemStack(Items.SUNFLOWER);
      NbtCompound tag = item.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList loreList = new NbtList();
      NbtList enchants = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name","[{\"text\":\"Charm of Light\",\"italic\":false,\"color\":\"yellow\",\"bold\":true}]");
      loreList.add(NbtString.of("[{\"text\":\"The charm \",\"italic\":false,\"color\":\"gold\"},{\"text\":\"radiates\",\"color\":\"yellow\"},{\"text\":\" a warm glow.\",\"color\":\"gold\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Its light seems to \",\"italic\":false,\"color\":\"gold\"},{\"text\":\"linger\",\"italic\":true,\"color\":\"red\"},{\"text\":\" behind you.\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Right click\",\"italic\":false,\"color\":\"light_purple\"},{\"text\":\" to see the magical \",\"color\":\"gold\"},{\"text\":\"light sources\",\"color\":\"yellow\"},{\"text\":\".\",\"color\":\"gold\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Sneak Right click\",\"italic\":false,\"color\":\"light_purple\"},{\"text\":\" to toggle the charm \",\"color\":\"gold\"},{\"text\":\"on or off\",\"color\":\"yellow\"},{\"text\":\".\",\"color\":\"gold\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Empowered\",\"italic\":false,\"color\":\"green\",\"bold\":true},{\"text\":\" Magic Item\",\"italic\":false,\"color\":\"dark_purple\",\"bold\":false}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
      tag.put("Enchantments",enchants);
   
      setBookLore(makeLore());
      setRecipe(makeRecipe());
      prefNBT = addMagicNbt(tag);
      prefNBT.getCompound("arcananovum").putBoolean("vision",false);
      prefNBT.getCompound("arcananovum").putBoolean("active",true);
      
      item.setNbt(prefNBT);
      prefItem = item;
   }
   
   @Override
   public void onTick(ServerWorld world, ServerPlayerEntity player, ItemStack item){
      boolean vision = item.getNbt().getCompound("arcananovum").getBoolean("vision");
      boolean active = item.getNbt().getCompound("arcananovum").getBoolean("active");
      
      if(world.getServer().getTicks() % 60 == 0){
         BlockPos pos = player.getBlockPos();
         if(active){
            if(world.getLightLevel(pos)<5 && world.getBlockState(pos).isAir()){
               world.setBlockState(pos,Blocks.LIGHT.getDefaultState(), Block.NOTIFY_ALL);
               world.emitGameEvent(player, GameEvent.BLOCK_PLACE, pos);
               SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_RESPAWN_ANCHOR_CHARGE, .3f,2f);
               PLAYER_DATA.get(player).addXP(100); // Add xp
               ArcanaAchievements.progress(player,"enlightened",1);
            }
         }
         if(vision){
            // Search 10x10x10 area around player for light blocks
            for(BlockPos block : BlockPos.iterateOutwards(pos, 10, 10, 10)){
               //System.out.println("looking at block "+block.toShortString());
               if(world.getBlockState(block).getBlock().equals(Blocks.LIGHT)){
                  world.spawnParticles(player, new BlockStateParticleEffect(ParticleTypes.BLOCK_MARKER, Blocks.LIGHT.getDefaultState()), true, block.getX()+.5,block.getY()+.5,block.getZ()+.5, 1,0,0,0,0);
               }
            }
         }
      }
   }
   
   @Override
   public boolean useItem(PlayerEntity playerEntity, World world, Hand hand){
      if(playerEntity.isSneaking()){
         toggleActive((ServerPlayerEntity) playerEntity,playerEntity.getStackInHand(hand));
      }else{
         toggleVision((ServerPlayerEntity) playerEntity,playerEntity.getStackInHand(hand));
      }
      return false;
   }
   
   @Override
   public boolean useItem(PlayerEntity playerEntity, World world, Hand hand, BlockHitResult result){
      return false;
   }
   
   public void toggleVision(ServerPlayerEntity player, ItemStack item){
      NbtCompound itemNbt = item.getNbt();
      NbtCompound magicNbt = itemNbt.getCompound("arcananovum");
      boolean vision = !magicNbt.getBoolean("vision");
      magicNbt.putBoolean("vision",vision);
      itemNbt.put("arcananovum",magicNbt);
      item.setNbt(itemNbt);
      if(vision){
         player.sendMessage(Text.translatable("You can now see the magical lights").formatted(Formatting.YELLOW,Formatting.ITALIC),true);
         SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_BEACON_ACTIVATE, 1,2f);
      }else{
         player.sendMessage(Text.translatable("You can no longer see the magical lights").formatted(Formatting.YELLOW,Formatting.ITALIC),true);
         SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_BEACON_DEACTIVATE, 1,.5f);
      }
      
   }
   
   public void toggleActive(ServerPlayerEntity player, ItemStack item){
      NbtCompound itemNbt = item.getNbt();
      NbtCompound magicNbt = itemNbt.getCompound("arcananovum");
      boolean active = !magicNbt.getBoolean("active");
      magicNbt.putBoolean("active",active);
      itemNbt.put("arcananovum",magicNbt);
      item.setNbt(itemNbt);
      if(active){
         player.sendMessage(Text.translatable("The Charm's Light Brightens").formatted(Formatting.YELLOW,Formatting.ITALIC),true);
         SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_BEACON_ACTIVATE, 1,2f);
      }else{
         player.sendMessage(Text.translatable("The Charm's Light Dims").formatted(Formatting.YELLOW,Formatting.ITALIC),true);
         SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_BEACON_DEACTIVATE, 1,.5f);
      }
   }
   
   private List<String> makeLore(){
      ArrayList<String> list = new ArrayList<>();
      list.add("{\"text\":\"    Charm of Light\\n\\nRarity: Empowered\\n\\nA Beacon's empowered light that has the ability to embue power seems like a solid base to start. After throwing in every light source under the sun and a couple of potions for good measure I have an \"}");
      list.add("{\"text\":\"    Charm of Light\\n\\nitem that will leave lingering and invisible magical lights behind me when it gets dark.\\n\\nThankfully those potions were added so I can see the lights by right clicking and remove them if they become a nuisance.\"}");
      return list;
   }
   
   private MagicItemRecipe makeRecipe(){
      MagicItemIngredient b = new MagicItemIngredient(Items.BEACON,1,null);
      MagicItemIngredient g = new MagicItemIngredient(Items.GLOWSTONE,64,null);
      MagicItemIngredient s = new MagicItemIngredient(Items.SHROOMLIGHT,64,null);
      MagicItemIngredient f = new MagicItemIngredient(Items.PEARLESCENT_FROGLIGHT,64,null);
      MagicItemIngredient o = new MagicItemIngredient(Items.OCHRE_FROGLIGHT,64,null);
      MagicItemIngredient v = new MagicItemIngredient(Items.VERDANT_FROGLIGHT,64,null);
      MagicItemIngredient l = new MagicItemIngredient(Items.SEA_LANTERN,64,null);
      MagicItemIngredient p = new MagicItemIngredient(Items.BLAZE_POWDER,64,null);
      ItemStack potion = new ItemStack(Items.POTION);
      MagicItemIngredient n = new MagicItemIngredient(Items.POTION,1,PotionUtil.setPotion(potion,Potions.LONG_NIGHT_VISION).getNbt());
      
      MagicItemIngredient[][] ingredients = {
            {n,p,s,p,n},
            {p,l,g,l,p},
            {f,g,b,g,o},
            {p,l,g,l,p},
            {n,p,v,p,n}};
      return new MagicItemRecipe(ingredients);
   }
}
