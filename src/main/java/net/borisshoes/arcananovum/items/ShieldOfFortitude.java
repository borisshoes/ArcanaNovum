package net.borisshoes.arcananovum.items;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.callbacks.ShieldTimerCallback;
import net.borisshoes.arcananovum.core.MagicItem;
import net.borisshoes.arcananovum.core.polymer.MagicPolymerShieldItem;
import net.borisshoes.arcananovum.recipes.arcana.ForgeRequirement;
import net.borisshoes.arcananovum.recipes.arcana.MagicItemIngredient;
import net.borisshoes.arcananovum.recipes.arcana.MagicItemRecipe;
import net.borisshoes.arcananovum.utils.MagicRarity;
import net.borisshoes.arcananovum.utils.MiscUtils;
import net.borisshoes.arcananovum.utils.SoundUtils;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeveledCauldronBlock;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ShieldOfFortitude extends MagicItem {
   public static final UUID EFFECT_UUID = UUID.fromString("cb7b7e36-0841-4d3c-bc94-4bbddfdaef4d");
   
   public ShieldOfFortitude(){
      id = "shield_of_fortitude";
      name = "Shield of Fortitude";
      rarity = MagicRarity.LEGENDARY;
      categories = new ArcaneTome.TomeFilter[]{ArcaneTome.TomeFilter.LEGENDARY, ArcaneTome.TomeFilter.EQUIPMENT};
      vanillaItem = Items.SHIELD;
      item = new ShieldOfFortitudeItem(new FabricItemSettings().maxCount(1).fireproof().maxDamage(336));
      
      ItemStack stack = new ItemStack(item);
      NbtCompound tag = stack.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList enchants = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name","[{\"text\":\"Shield of Fortitude\",\"italic\":false,\"bold\":true,\"color\":\"aqua\"}]");
      tag.put("display",display);
      tag.put("Enchantments",enchants);
      tag.putInt("HideFlags", 255);
      tag.putInt("Unbreakable",1);
      
      setBookLore(makeLore());
      setRecipe(makeRecipe());
      stack.setNbt(addMagicNbt(tag));
      setPrefStack(stack);
   }
   
   @Override
   public NbtList getItemLore(@Nullable ItemStack itemStack){
      NbtList loreList = new NbtList();
      loreList.add(NbtString.of("[{\"text\":\"This shield is \",\"italic\":false,\"color\":\"dark_purple\"},{\"text\":\"overflowing\",\"italic\":true,\"color\":\"light_purple\"},{\"text\":\" with powerful \",\"color\":\"dark_purple\"},{\"text\":\"defensive\",\"color\":\"blue\"},{\"text\":\" \",\"color\":\"dark_purple\"},{\"text\":\"magic\",\"color\":\"blue\"},{\"text\":\".\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Your will for \",\"italic\":false,\"color\":\"dark_purple\"},{\"text\":\"protection\",\"color\":\"aqua\"},{\"text\":\" becomes a tangible \"},{\"text\":\"fortitude\",\"color\":\"aqua\"},{\"text\":\".\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Damage\",\"italic\":false,\"color\":\"red\"},{\"text\":\" \",\"color\":\"dark_purple\"},{\"text\":\"blocked\",\"color\":\"blue\"},{\"text\":\" becomes \",\"color\":\"dark_purple\"},{\"text\":\"absorption\",\"color\":\"yellow\"},{\"text\":\" \",\"color\":\"dark_purple\"},{\"text\":\"hearts\",\"color\":\"yellow\"},{\"text\":\" and the shield is \",\"color\":\"dark_purple\"},{\"text\":\"unbreakable\",\"color\":\"aqua\"},{\"text\":\".\",\"color\":\"dark_purple\"}]"));
      return loreList;
   }
   
   @Override
   public ItemStack updateItem(ItemStack stack, MinecraftServer server){
      NbtCompound itemNbt = stack.getNbt();
      NbtList enchants = itemNbt.getList("Enchantments", NbtElement.COMPOUND_TYPE);
      NbtCompound banner = itemNbt.getCompound("BlockEntityTag");
      NbtCompound newTag = super.updateItem(stack,server).getNbt();
      if(enchants != null) newTag.put("Enchantments", enchants);
      if(banner != null) newTag.put("BlockEntityTag", banner);
      stack.setNbt(newTag);
      return buildItemLore(stack,server);
   }
   
   @Override
   public ItemStack forgeItem(Inventory inv){
      ItemStack toolStack = inv.getStack(12); // Should be the Sword
      ItemStack newMagicItem = getNewItem();
      NbtCompound nbt = toolStack.getNbt();
      if(nbt != null){
         if(nbt.contains("Enchantments")){
            NbtList enchants = nbt.getList("Enchantments", NbtElement.COMPOUND_TYPE);
            newMagicItem.getOrCreateNbt().put("Enchantments",enchants);
         }
         if(nbt.contains("BlockEntityTag")){
            newMagicItem.getOrCreateNbt().put("BlockEntityTag", nbt.getCompound("BlockEntityTag"));
         }
      }
      return newMagicItem;
   }
   
   public void shieldBlock(LivingEntity entity, ItemStack item, double amount){
      float maxAbs = 10 + 2*Math.max(0, ArcanaAugments.getAugmentOnItem(item,ArcanaAugments.SHIELD_OF_FAITH.id));
      float curAbs = entity.getAbsorptionAmount();
      float addedAbs = (float) Math.min(maxAbs,amount*.5);
      int duration = 200 + 100*Math.max(0,ArcanaAugments.getAugmentOnItem(item,ArcanaAugments.SHIELD_OF_RESILIENCE.id));
      if(entity instanceof ServerPlayerEntity player){
         ArcanaNovum.addTickTimerCallback(new ShieldTimerCallback(duration,item,player,addedAbs));
         SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, 1, 1.8f);
      }
      MiscUtils.addMaxAbsorption(entity, EFFECT_UUID,"arcananovum."+ ArcanaRegistry.SHIELD_OF_FORTITUDE.getId(),addedAbs);
      entity.setAbsorptionAmount((curAbs + addedAbs));
   }
   
   private List<String> makeLore(){
      ArrayList<String> list = new ArrayList<>();
      list.add("{\"text\":\"  Shield of Fortitude\\n\\nRarity: Legendary\\n\\nTaking after the Wings of Enderia I have successfully recreated their incredible durability. \\n\\nWhile keeping with the protective nature of the Wings I have been able to infuse extra\\n\"}");
      list.add("{\"text\":\"  Shield of Fortitude\\n\\nArcana into the four basic protection enchantments, fusing them with the ability of golden apples that grants the consumer a protective barrier.\\n\\nAs a result half of all damage blocked becomes a barrier lasting 10 seconds.\"}");
      return list;
   }
   
   private MagicItemRecipe makeRecipe(){
      MagicItemIngredient s = new MagicItemIngredient(Items.SHIELD,1,null, true);
      MagicItemIngredient o = new MagicItemIngredient(Items.OBSIDIAN,64,null);
      MagicItemIngredient n = new MagicItemIngredient(Items.NETHER_STAR,4,null);
      MagicItemIngredient g = new MagicItemIngredient(Items.GOLDEN_APPLE,64,null);
      MagicItemIngredient i = new MagicItemIngredient(Items.NETHERITE_INGOT,4,null);
      MagicItemIngredient p = new MagicItemIngredient(Items.ENCHANTED_BOOK,1,EnchantedBookItem.forEnchantment(new EnchantmentLevelEntry(Enchantments.PROTECTION,4)).getNbt());
      MagicItemIngredient j = new MagicItemIngredient(Items.ENCHANTED_BOOK,1,EnchantedBookItem.forEnchantment(new EnchantmentLevelEntry(Enchantments.PROJECTILE_PROTECTION,4)).getNbt());
      MagicItemIngredient b = new MagicItemIngredient(Items.ENCHANTED_BOOK,1,EnchantedBookItem.forEnchantment(new EnchantmentLevelEntry(Enchantments.BLAST_PROTECTION,4)).getNbt());
      MagicItemIngredient f = new MagicItemIngredient(Items.ENCHANTED_BOOK,1,EnchantedBookItem.forEnchantment(new EnchantmentLevelEntry(Enchantments.FIRE_PROTECTION,4)).getNbt());
      
      MagicItemIngredient[][] ingredients = {
            {n,o,g,o,n},
            {o,i,b,i,o},
            {g,f,s,j,g},
            {o,i,p,i,o},
            {n,o,g,o,n}};
      return new MagicItemRecipe(ingredients, new ForgeRequirement().withEnchanter().withCore());
   }
   
   public class ShieldOfFortitudeItem extends MagicPolymerShieldItem {
      public ShieldOfFortitudeItem(Settings settings){
         super(getThis(),settings);
      }
      
      
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
      
      @Override
      public ActionResult useOnBlock(ItemUsageContext context){
         ItemStack stack = context.getStack();
         BlockPos blockPos = context.getBlockPos();
         World world = context.getWorld();
         try{
            NbtCompound itemNbt = stack.getNbt();
            NbtCompound magicNbt = itemNbt.getCompound("arcananovum");
            
            BlockState blockState = world.getBlockState(blockPos);
            if(itemNbt.contains("BlockEntityTag") && blockState.getBlock() == Blocks.WATER_CAULDRON){
               itemNbt.remove("BlockEntityTag");
               LeveledCauldronBlock.decrementFluidLevel(blockState,world,blockPos);
               return ActionResult.SUCCESS;
            }
         }catch (Exception e){
            e.printStackTrace();
         }
         return ActionResult.PASS;
      }
   }
}
