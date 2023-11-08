package net.borisshoes.arcananovum.blocks.forge;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.core.MagicBlock;
import net.borisshoes.arcananovum.core.Multiblock;
import net.borisshoes.arcananovum.core.MultiblockCore;
import net.borisshoes.arcananovum.core.polymer.MagicPolymerBlockEntity;
import net.borisshoes.arcananovum.core.polymer.MagicPolymerBlockItem;
import net.borisshoes.arcananovum.items.ArcaneTome;
import net.borisshoes.arcananovum.recipes.arcana.MagicItemIngredient;
import net.borisshoes.arcananovum.recipes.arcana.MagicItemRecipe;
import net.borisshoes.arcananovum.utils.MagicRarity;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MidnightEnchanter extends MagicBlock implements MultiblockCore {
   
   private Multiblock multiblock;
   
   public MidnightEnchanter(){
      id = "midnight_enchanter";
      name = "Midnight Enchanter";
      rarity = MagicRarity.EXOTIC;
      categories = new ArcaneTome.TomeFilter[]{ArcaneTome.TomeFilter.EXOTIC, ArcaneTome.TomeFilter.BLOCKS, ArcaneTome.TomeFilter.FORGE};
      itemVersion = 0;
      vanillaItem = Items.ENCHANTING_TABLE;
      block = new MidnightEnchanterBlock(FabricBlockSettings.create().mapColor(MapColor.RED).strength(5.0f, 1200.0f).requiresTool().luminance(state -> 7));
      item = new MidnightEnchanterItem(this.block,new FabricItemSettings().maxCount(1).fireproof());
      
      ItemStack stack = new ItemStack(item);
      NbtCompound tag = stack.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList enchants = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name","[{\"text\":\"Midnight Enchanter\",\"italic\":false,\"color\":\"dark_aqua\",\"bold\":true}]");
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
      loreList.add(NbtString.of("[{\"text\":\"A \",\"italic\":false,\"color\":\"blue\"},{\"text\":\"Forge Structure\",\"color\":\"light_purple\"},{\"text\":\" addon to the \"},{\"text\":\"Starlight Forge\",\"color\":\"light_purple\"},{\"text\":\".\",\"color\":\"blue\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Normal \",\"italic\":false,\"color\":\"blue\"},{\"text\":\"Enchanting Tables\",\"color\":\"dark_aqua\"},{\"text\":\" are \"},{\"text\":\"unpredictable \",\"color\":\"aqua\"},{\"text\":\"and \"},{\"text\":\"inconsistent\",\"color\":\"aqua\"},{\"text\":\".\",\"color\":\"blue\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"This \",\"italic\":false,\"color\":\"blue\"},{\"text\":\"Table \",\"color\":\"dark_aqua\"},{\"text\":\"not only enables \"},{\"text\":\"precise control \",\"color\":\"light_purple\"},{\"text\":\"of \"},{\"text\":\"enchantments\",\"color\":\"dark_aqua\"},{\"text\":\"...\",\"color\":\"blue\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"It also allows for \",\"italic\":false,\"color\":\"blue\"},{\"text\":\"enchantments \",\"color\":\"dark_aqua\"},{\"text\":\"to be \"},{\"text\":\"removed \",\"color\":\"aqua\"},{\"text\":\"and \"},{\"text\":\"placed \",\"color\":\"aqua\"},{\"text\":\"onto \"},{\"text\":\"books\",\"color\":\"light_purple\"},{\"text\":\".\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Enchantments \",\"italic\":false,\"color\":\"dark_aqua\"},{\"text\":\"can also be \",\"color\":\"blue\"},{\"text\":\"broken down\",\"color\":\"light_purple\"},{\"text\":\" into \",\"color\":\"blue\"},{\"text\":\"Nebulous Essence\",\"color\":\"dark_purple\"},{\"text\":\".\",\"color\":\"blue\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Forge Structures:\",\"italic\":false,\"color\":\"light_purple\",\"bold\":true},{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\",\"bold\":false}]"));
      loreList.add(NbtString.of("[{\"text\":\"Are \",\"italic\":false,\"color\":\"dark_purple\"},{\"text\":\"multiblock structures\",\"color\":\"light_purple\"},{\"text\":\" that must be \"},{\"text\":\"built\",\"color\":\"aqua\"},{\"text\":\" in the \"},{\"text\":\"world\",\"color\":\"dark_aqua\"},{\"text\":\".\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Must \",\"italic\":false,\"color\":\"dark_aqua\"},{\"text\":\"be \",\"color\":\"dark_purple\"},{\"text\":\"placed \",\"color\":\"aqua\"},{\"text\":\"within a \",\"color\":\"dark_purple\"},{\"text\":\"17x11x17\"},{\"text\":\" \",\"color\":\"dark_purple\"},{\"text\":\"cube around a \",\"color\":\"dark_purple\"},{\"text\":\"Starlight Forge\",\"color\":\"light_purple\"},{\"text\":\".\",\"color\":\"dark_purple\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Right Click\",\"italic\":false,\"color\":\"dark_aqua\"},{\"text\":\" a \",\"color\":\"dark_purple\"},{\"text\":\"completed \",\"color\":\"aqua\"},{\"text\":\"Forge Structure\",\"color\":\"light_purple\"},{\"text\":\" to \",\"color\":\"dark_purple\"},{\"text\":\"use\",\"color\":\"aqua\"},{\"text\":\" it.\",\"color\":\"dark_purple\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Right Click\",\"italic\":false,\"color\":\"dark_aqua\"},{\"text\":\" a \",\"color\":\"dark_purple\"},{\"text\":\"Forge Structure\",\"color\":\"light_purple\"},{\"text\":\" to see a \",\"color\":\"dark_purple\"},{\"text\":\"hologram \",\"color\":\"aqua\"},{\"text\":\"of the \",\"color\":\"dark_purple\"},{\"text\":\"structure\",\"color\":\"light_purple\"},{\"text\":\".\",\"color\":\"dark_purple\"}]"));
      return loreList;
   }
   
   @Override
   public void loadMultiblock(){
      multiblock = Multiblock.loadFromFile(getId());
   }
   
   @Override
   public Multiblock getMultiblock(){
      return multiblock;
   }
   
   private MagicItemRecipe makeRecipe(){
      MagicItemIngredient a = new MagicItemIngredient(Items.CRYING_OBSIDIAN,64,null);
      ItemStack enchantedBook1 = new ItemStack(Items.ENCHANTED_BOOK);
      EnchantedBookItem.addEnchantment(enchantedBook1,new EnchantmentLevelEntry(Enchantments.BINDING_CURSE,1));
      MagicItemIngredient b = new MagicItemIngredient(Items.ENCHANTED_BOOK,1,enchantedBook1.getNbt());
      ItemStack enchantedBook2 = new ItemStack(Items.ENCHANTED_BOOK);
      EnchantedBookItem.addEnchantment(enchantedBook2,new EnchantmentLevelEntry(Enchantments.FORTUNE,3));
      MagicItemIngredient c = new MagicItemIngredient(Items.ENCHANTED_BOOK,1,enchantedBook2.getNbt());
      ItemStack enchantedBook3 = new ItemStack(Items.ENCHANTED_BOOK);
      EnchantedBookItem.addEnchantment(enchantedBook3,new EnchantmentLevelEntry(Enchantments.VANISHING_CURSE,1));
      MagicItemIngredient d = new MagicItemIngredient(Items.ENCHANTED_BOOK,1,enchantedBook3.getNbt());
      ItemStack enchantedBook5 = new ItemStack(Items.ENCHANTED_BOOK);
      EnchantedBookItem.addEnchantment(enchantedBook5,new EnchantmentLevelEntry(Enchantments.SWIFT_SNEAK,3));
      MagicItemIngredient f = new MagicItemIngredient(Items.ENCHANTED_BOOK,1,enchantedBook5.getNbt());
      MagicItemIngredient g = new MagicItemIngredient(Items.EXPERIENCE_BOTTLE,64,null);
      MagicItemIngredient h = new MagicItemIngredient(Items.LAPIS_BLOCK,64,null);
      ItemStack enchantedBook9 = new ItemStack(Items.ENCHANTED_BOOK);
      EnchantedBookItem.addEnchantment(enchantedBook9,new EnchantmentLevelEntry(Enchantments.SHARPNESS,5));
      MagicItemIngredient j = new MagicItemIngredient(Items.ENCHANTED_BOOK,1,enchantedBook9.getNbt());
      ItemStack enchantedBook10 = new ItemStack(Items.ENCHANTED_BOOK);
      EnchantedBookItem.addEnchantment(enchantedBook10,new EnchantmentLevelEntry(Enchantments.LURE,3));
      MagicItemIngredient k = new MagicItemIngredient(Items.ENCHANTED_BOOK,1,enchantedBook10.getNbt());
      MagicItemIngredient m = new MagicItemIngredient(Items.ENCHANTING_TABLE,64,null);
      ItemStack enchantedBook14 = new ItemStack(Items.ENCHANTED_BOOK);
      EnchantedBookItem.addEnchantment(enchantedBook14,new EnchantmentLevelEntry(Enchantments.LOOTING,3));
      MagicItemIngredient o = new MagicItemIngredient(Items.ENCHANTED_BOOK,1,enchantedBook14.getNbt());
      ItemStack enchantedBook15 = new ItemStack(Items.ENCHANTED_BOOK);
      EnchantedBookItem.addEnchantment(enchantedBook15,new EnchantmentLevelEntry(Enchantments.SOUL_SPEED,3));
      MagicItemIngredient p = new MagicItemIngredient(Items.ENCHANTED_BOOK,1,enchantedBook15.getNbt());
      ItemStack enchantedBook19 = new ItemStack(Items.ENCHANTED_BOOK);
      EnchantedBookItem.addEnchantment(enchantedBook19,new EnchantmentLevelEntry(Enchantments.EFFICIENCY,5));
      MagicItemIngredient t = new MagicItemIngredient(Items.ENCHANTED_BOOK,1,enchantedBook19.getNbt());
      ItemStack enchantedBook21 = new ItemStack(Items.ENCHANTED_BOOK);
      EnchantedBookItem.addEnchantment(enchantedBook21,new EnchantmentLevelEntry(Enchantments.MENDING,1));
      MagicItemIngredient v = new MagicItemIngredient(Items.ENCHANTED_BOOK,1,enchantedBook21.getNbt());
      ItemStack enchantedBook22 = new ItemStack(Items.ENCHANTED_BOOK);
      EnchantedBookItem.addEnchantment(enchantedBook22,new EnchantmentLevelEntry(Enchantments.UNBREAKING,3));
      MagicItemIngredient w = new MagicItemIngredient(Items.ENCHANTED_BOOK,1,enchantedBook22.getNbt());
      ItemStack enchantedBook23 = new ItemStack(Items.ENCHANTED_BOOK);
      EnchantedBookItem.addEnchantment(enchantedBook23,new EnchantmentLevelEntry(Enchantments.PROTECTION,4));
      MagicItemIngredient x = new MagicItemIngredient(Items.ENCHANTED_BOOK,1,enchantedBook23.getNbt());
      
      MagicItemIngredient[][] ingredients = {
            {a,b,c,d,a},
            {f,g,h,g,j},
            {k,h,m,h,o},
            {p,g,h,g,t},
            {a,v,w,x,a}};
      return new MagicItemRecipe(ingredients);
      
   }
   
   private List<String> makeLore(){
      ArrayList<String> list = new ArrayList<>();
      list.add("{\"text\":\"  Midnight Enchanter\\n\\nRarity: Exotic\\n\\nAn enchanting table is an old, but not ancient, design. It only scratches the surface of how Arcana can be bound to equipment, and relies too much on the random fluctuations of the environment.\"}");
      list.add("{\"text\":\"  Midnight Enchanter\\n\\nIf my predictive equations are correct, I should be able to cancel out the environmental noise in the Enchantment matrix and reduce Enchantment Arcana to a pure form, afterwhich I can make it take any shape of my choosing. \"}");
      list.add("{\"text\":\"  Midnight Enchanter\\n\\nThe Enchanter allows for disenchanting of items to gain Nebulous Essence, which can then be spent to choose exact enchantments and levels to place on items. The Enchanter also gives access to normally unavailable enchantments.\"}");
      return list;
   }
   
   public class MidnightEnchanterItem extends MagicPolymerBlockItem {
      public MidnightEnchanterItem(Block block, Settings settings){
         super(getThis(),block, settings);
      }
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
   }
   
   public class MidnightEnchanterBlock extends MagicPolymerBlockEntity {
      public MidnightEnchanterBlock(Settings settings){
         super(settings);
      }
      
      @Override
      public Block getPolymerBlock(BlockState state) {
         return Blocks.ENCHANTING_TABLE;
      }
      
      @Nullable
      public static MidnightEnchanterBlockEntity getEntity(World world, BlockPos pos) {
         BlockState state = world.getBlockState(pos);
         if (!(state.getBlock() instanceof MidnightEnchanterBlock)) {
            return null;
         }
         return world.getBlockEntity(pos) instanceof MidnightEnchanterBlockEntity enchanter ? enchanter : null;
      }
      
      @Override
      public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
         return new MidnightEnchanterBlockEntity(pos, state);
      }
      
      @Nullable
      @Override
      public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
         return checkType(type, ArcanaRegistry.MIDNIGHT_ENCHANTER_BLOCK_ENTITY, MidnightEnchanterBlockEntity::ticker);
      }
      
      @Override
      public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity playerEntity, Hand hand, BlockHitResult hit){
         if(hand != Hand.MAIN_HAND) return ActionResult.PASS;
         MidnightEnchanterBlockEntity enchanter = (MidnightEnchanterBlockEntity) world.getBlockEntity(pos);
         if(enchanter != null){
            if(playerEntity instanceof ServerPlayerEntity player){
               if(enchanter.isAssembled()){
                  if(StarlightForge.findActiveForge(player.getServerWorld(),pos) == null){
                     player.sendMessage(Text.literal("The Enchanter must be within the range of an active Starlight Forge"));
                  }else{
                     if(enchanter.hasBooks()){
                        enchanter.openGui(player);
                     }else{
                        player.sendMessage(Text.literal("The Enchanter needs at least 20 bookshelves nearby"));
                     }
                  }
               }else{
                  player.sendMessage(Text.literal("Multiblock not constructed."));
                  multiblock.displayStructure(enchanter.getMultiblockCheck());
               }
            }
         }
         return ActionResult.SUCCESS;
      }
      
      @Override
      public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
         if (world.getBlockEntity(pos) instanceof MidnightEnchanterBlockEntity enchanter) {
            dropBlockItem(world, pos, state, player, enchanter);
            world.removeBlockEntity(pos);
         }
         
         world.removeBlock(pos, false);
         
         super.onBreak(world, pos, state, player);
      }
      
      @Override
      public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
         BlockEntity entity = world.getBlockEntity(pos);
         if (placer instanceof ServerPlayerEntity player && entity instanceof MidnightEnchanterBlockEntity enchanter) {
            initializeMagicBlock(stack,enchanter);
         }
      }
   }
}
