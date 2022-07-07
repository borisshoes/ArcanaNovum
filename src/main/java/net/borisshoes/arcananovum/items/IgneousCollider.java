package net.borisshoes.arcananovum.items;

import net.borisshoes.arcananovum.cardinalcomponents.MagicBlock;
import net.borisshoes.arcananovum.recipes.MagicItemRecipe;
import net.borisshoes.arcananovum.utils.MagicRarity;
import net.borisshoes.arcananovum.utils.SoundUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
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

public class IgneousCollider extends MagicItem implements UsableItem, BlockItem{
   
   public static final int COOLDOWN = 5; /// Cooldown is 60 seconds
   
   public IgneousCollider(){
      id = "igneous_collider";
      name = "Igneous Collider";
      rarity = MagicRarity.EXOTIC;
   
      ItemStack item = new ItemStack(Items.LODESTONE);
      NbtCompound tag = item.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList loreList = new NbtList();
      NbtList enchants = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name","[{\"text\":\"Igneous Collider\",\"italic\":false,\"bold\":true,\"color\":\"dark_purple\"}]");
      loreList.add(NbtString.of("[{\"text\":\"Mining \",\"italic\":false,\"color\":\"light_purple\"},{\"text\":\"obsidian \",\"color\":\"dark_purple\"},{\"text\":\"is a pain, now this \"},{\"text\":\"device \",\"color\":\"dark_aqua\"},{\"text\":\"can make it \"},{\"text\":\"automatically\",\"color\":\"dark_aqua\"},{\"text\":\".\",\"color\":\"light_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Place \",\"italic\":false,\"color\":\"light_purple\"},{\"text\":\"lava \",\"color\":\"gold\"},{\"text\":\"and \"},{\"text\":\"water \",\"color\":\"dark_blue\"},{\"text\":\"sources or cauldrons adjacent to the \"},{\"text\":\"Collider\",\"color\":\"dark_purple\"},{\"text\":\".\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Obsidian \",\"italic\":false,\"color\":\"dark_purple\"},{\"text\":\"will be \",\"color\":\"light_purple\"},{\"text\":\"spat out\",\"color\":\"dark_aqua\"},{\"text\":\" or into a \",\"color\":\"light_purple\"},{\"text\":\"chest \",\"color\":\"dark_aqua\"},{\"text\":\"above it \",\"color\":\"light_purple\"},{\"text\":\"periodically\",\"color\":\"blue\"},{\"text\":\".\",\"color\":\"light_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"If a \",\"italic\":false,\"color\":\"light_purple\"},{\"text\":\"netherite block\",\"color\":\"dark_red\"},{\"text\":\" is below the \"},{\"text\":\"Collider\",\"color\":\"dark_purple\"},{\"text\":\", \"},{\"text\":\"crying obsidian\",\"color\":\"#660066\"},{\"text\":\" will be made.\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Exotic \",\"italic\":false,\"color\":\"aqua\",\"bold\":true},{\"text\":\"Magic Item\",\"color\":\"dark_purple\",\"bold\":false}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
      tag.put("Enchantments",enchants);
   
      setBookLore(makeLore());
      //setRecipe(makeRecipe());
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
         placeCollider(player, world, item, placePos);
      }else{
         playerEntity.sendMessage(new LiteralText("The sponge cannot be placed here.").formatted(Formatting.RED,Formatting.ITALIC),true);
         SoundUtils.playSongToPlayer((ServerPlayerEntity) playerEntity, SoundEvents.BLOCK_FIRE_EXTINGUISH, 1,1);
      }
      return false;
   }
   
   private void placeCollider(ServerPlayerEntity player, World world, ItemStack item, BlockPos pos){
      try{
         MagicBlock colliderBlock = new MagicBlock(pos);
         NbtCompound colliderData = new NbtCompound();
         colliderData.putString("id",this.id);
         colliderData.putInt("cooldown", COOLDOWN);
         colliderBlock.setData(colliderData);
         world.setBlockState(pos, Blocks.LODESTONE.getDefaultState(), Block.NOTIFY_ALL);
         MAGIC_BLOCK_LIST.get(world).addBlock(colliderBlock);
         
         SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_STONE_PLACE, 1,1f);
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
      return drops;
   }
   
   private MagicItemRecipe makeRecipe(){
      //TODO make recipe
      return null;
   }
   
   private List<String> makeLore(){
      //TODO make lore
      ArrayList<String> list = new ArrayList<>();
      list.add("{\"text\":\" TODO \"}");
      return list;
   }
}
