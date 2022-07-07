package net.borisshoes.arcananovum.items;

import net.borisshoes.arcananovum.cardinalcomponents.MagicBlock;
import net.borisshoes.arcananovum.recipes.MagicItemIngredient;
import net.borisshoes.arcananovum.recipes.MagicItemRecipe;
import net.borisshoes.arcananovum.utils.MagicRarity;
import net.borisshoes.arcananovum.utils.Utils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

import static net.borisshoes.arcananovum.cardinalcomponents.MagicBlocksComponentInitializer.MAGIC_BLOCK_LIST;
import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;

public class ContinuumAnchor extends MagicItem implements UsableItem, BlockItem{
   public ContinuumAnchor(){
      id = "continuum_anchor";
      name = "Continuum Anchor";
      rarity = MagicRarity.LEGENDARY;
      
      ItemStack item = new ItemStack(Items.RESPAWN_ANCHOR);
      NbtCompound tag = item.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList loreList = new NbtList();
      NbtList enchants = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name","[{\"text\":\"Continuum Anchor\",\"italic\":false,\"bold\":true,\"color\":\"dark_blue\"}]");
      loreList.add(NbtString.of("[{\"text\":\"The \",\"italic\":false,\"color\":\"dark_gray\"},{\"text\":\"anchor\",\"color\":\"dark_blue\"},{\"text\":\" \",\"color\":\"dark_purple\"},{\"text\":\"has the extraordinary ability to manipulate \"},{\"text\":\"spacetime\",\"color\":\"dark_purple\"},{\"text\":\".\",\"color\":\"dark_gray\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"It just needs the \",\"italic\":false,\"color\":\"dark_gray\"},{\"text\":\"right type\",\"color\":\"gray\"},{\"text\":\" of\"},{\"text\":\" \",\"color\":\"dark_purple\"},{\"text\":\"fuel\",\"color\":\"gold\"},{\"text\":\"...\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"The \",\"italic\":false,\"color\":\"dark_gray\"},{\"text\":\"continuum anchor\",\"color\":\"dark_blue\"},{\"text\":\" consumes \"},{\"text\":\"exotic matter\",\"color\":\"blue\"},{\"text\":\" to \"},{\"text\":\"chunk load\",\"color\":\"aqua\"},{\"text\":\" a \"},{\"text\":\"3x3 area\",\"color\":\"dark_green\"},{\"text\":\".\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Legendary\",\"italic\":false,\"color\":\"gold\",\"bold\":true},{\"text\":\" Magic Item\",\"italic\":false,\"color\":\"dark_purple\",\"bold\":false}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
      tag.put("Enchantments",enchants);
   
      setBookLore(makeLore());
      setRecipe(makeRecipe());
      prefNBT = addMagicNbt(tag);
      item.setNbt(prefNBT);
      prefItem = item;
   }
   
   @Override
   public boolean useItem(PlayerEntity playerEntity, World world, Hand hand){
      return false;
   }
   
   @Override
   public boolean useItem(PlayerEntity playerEntity, World world, Hand hand, BlockHitResult result){
      ItemStack item = playerEntity.getStackInHand(hand);
      Direction side = result.getSide();
      BlockPos placePos = result.getBlockPos().add(side.getVector());
      boolean placeable = world.getBlockState(placePos).isAir() || world.getBlockState(placePos).canReplace(new ItemPlacementContext(playerEntity, hand, item, result));
      if(placeable && playerEntity instanceof ServerPlayerEntity player){
         placeAnchor(player, world, item, placePos);
      }else{
         playerEntity.sendMessage(new LiteralText("The sponge cannot be placed here.").formatted(Formatting.RED,Formatting.ITALIC),true);
         Utils.playSongToPlayer((ServerPlayerEntity) playerEntity, SoundEvents.BLOCK_FIRE_EXTINGUISH, 1,1);
      }
      return false;
   }
   
   private void placeAnchor(ServerPlayerEntity player, World world, ItemStack item, BlockPos pos){
      try{
         MagicBlock anchorBlock = new MagicBlock(pos);
         NbtCompound anchorData = new NbtCompound();
         anchorData.putString("id",this.id);
         anchorData.putBoolean("active",false);
         anchorData.putInt("fuel",0);
         anchorData.putInt("range",2);
         anchorBlock.setData(anchorData);
         world.setBlockState(pos, Blocks.RESPAWN_ANCHOR.getDefaultState(), Block.NOTIFY_ALL);
         MAGIC_BLOCK_LIST.get(world).addBlock(anchorBlock);
      
         player.sendMessage(new LiteralText("Placing the Continuum Anchor sends a ripple across spacetime.").formatted(Formatting.DARK_BLUE),true);
         Utils.playSongToPlayer(player, SoundEvents.BLOCK_RESPAWN_ANCHOR_AMBIENT, 5,.8f);
         item.decrement(item.getCount());
         item.setNbt(new NbtCompound());
      }catch(Exception e){
         e.printStackTrace();
      }
   }
   
   @Override
   public List<ItemStack> dropFromBreak(World world, PlayerEntity playerEntity, BlockPos blockPos, BlockState blockState, BlockEntity blockEntity, NbtCompound blockData){
      List<ItemStack> drops = new ArrayList<>();
      drops.add(getNewItem());
      int fuel = blockData.getInt("fuel");
      if(fuel > 0){
         ExoticMatter exoticMatter = (ExoticMatter) MagicItems.EXOTIC_MATTER;
         ItemStack fuelDrop = MagicItems.EXOTIC_MATTER.getNewItem();
         exoticMatter.setFuel(fuelDrop,fuel);
         drops.add(fuelDrop);
      }
      return drops;
   }
   
   //TODO: Update lore: anchor is now placeable
   private List<String> makeLore(){
      ArrayList<String> list = new ArrayList<>();
      list.add("{\"text\":\"   Continuum Anchor\\n\\nRarity: Legendary\\n\\nExotic Matter has given useful insight into warping spacetime. On top of being more practiced in constructing sturdy casings that can withstand the flow of Arcana, I have made additional efforts to \"}");
      list.add("{\"text\":\"   Continuum Anchor\\n\\nreinforce this chassis against dimensional shear.\\nBy combining all known techniques of manipulating dimensional energy I believe I can cause a section of space to be locked in time so that the world cannot be unloaded.\"}");
      list.add("{\"text\":\"   Continuum Anchor\\n\\nWhen fed with Exotic Matter the Anchor chunk loads a 5x5 chunk area with Entity Processing ticks. The Anchor can be turned off with a redstone signal and its fuel can be removed by an empty hand. It can be refueled in use, but fuel might be lost.\"}");
      list.add("{\"text\":\"   Continuum Anchor\\n\\nThe Anchor locks itself in spacetime, hence the name 'anchor' and any attempt to move it will result in its destruction.\\n\\nOnce placed the anchor CANNOT BE RE-OBTAINED. \\n\\n\"}");
      return list;
   }
   
   private MagicItemRecipe makeRecipe(){
      MagicItemIngredient s = new MagicItemIngredient(Items.NETHER_STAR,16,null);
      MagicItemIngredient p = new MagicItemIngredient(Items.CRYING_OBSIDIAN,64,null);
      MagicItemIngredient o = new MagicItemIngredient(Items.OBSIDIAN,64,null);
      MagicItemIngredient a = new MagicItemIngredient(Items.RESPAWN_ANCHOR,32,null);
      MagicItemIngredient e = new MagicItemIngredient(Items.ENDER_EYE,64,null);
      MagicItemIngredient n = new MagicItemIngredient(Items.NETHERITE_INGOT,4,null);
      
      MagicItemIngredient[][] ingredients = {
            {o,p,n,p,o},
            {p,e,a,e,p},
            {n,a,s,a,n},
            {p,e,a,e,p},
            {o,p,n,p,o}};
      return new MagicItemRecipe(ingredients);
   }
}
