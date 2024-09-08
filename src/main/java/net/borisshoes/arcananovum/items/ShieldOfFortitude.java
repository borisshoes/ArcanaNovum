package net.borisshoes.arcananovum.items;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.callbacks.ShieldTimerCallback;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerShieldItem;
import net.borisshoes.arcananovum.gui.arcanetome.TomeGui;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaIngredient;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaRecipe;
import net.borisshoes.arcananovum.recipes.arcana.ForgeRequirement;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaRarity;
import net.borisshoes.arcananovum.utils.MiscUtils;
import net.borisshoes.arcananovum.utils.SoundUtils;
import net.borisshoes.arcananovum.utils.TextUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeveledCauldronBlock;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BannerPatternsComponent;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.UnbreakableComponent;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class ShieldOfFortitude extends ArcanaItem {
   public static final String ID = "shield_of_fortitude";
   public static final Identifier EFFECT_ID = Identifier.of(ArcanaNovum.MOD_ID,ID);
   
   public ShieldOfFortitude(){
      id = ID;
      name = "Shield of Fortitude";
      rarity = ArcanaRarity.SOVEREIGN;
      categories = new TomeGui.TomeFilter[]{TomeGui.TomeFilter.SOVEREIGN, TomeGui.TomeFilter.EQUIPMENT};
      vanillaItem = Items.SHIELD;
      item = new ShieldOfFortitudeItem(new Item.Settings().maxCount(1).fireproof().maxDamage(1024)
            .component(DataComponentTypes.ITEM_NAME, Text.translatable("item."+MOD_ID+"."+ID).formatted(Formatting.BOLD,Formatting.AQUA))
            .component(DataComponentTypes.LORE, new LoreComponent(getItemLore(null)))
            .component(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true)
            .component(DataComponentTypes.UNBREAKABLE,new UnbreakableComponent(false))
      );
      researchTasks = new RegistryKey[]{ResearchTasks.OBTAIN_NETHERITE_INGOT,ResearchTasks.EFFECT_ABSORPTION,ResearchTasks.ADVANCEMENT_DEFLECT_ARROW,ResearchTasks.UNLOCK_STELLAR_CORE,ResearchTasks.UNLOCK_MIDNIGHT_ENCHANTER};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getMaxCount());
      setPrefStack(stack);
   }
   
   @Override
   public List<Text> getItemLore(@Nullable ItemStack itemStack){
      List<MutableText> lore = new ArrayList<>();
      lore.add(Text.literal("")
            .append(Text.literal("This shield is ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("overflowing").formatted(Formatting.ITALIC,Formatting.LIGHT_PURPLE))
            .append(Text.literal(" with powerful ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("defensive magic").formatted(Formatting.BLUE))
            .append(Text.literal(".").formatted(Formatting.DARK_PURPLE)));
      lore.add(Text.literal("")
            .append(Text.literal("Your will for ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("protection").formatted(Formatting.AQUA))
            .append(Text.literal(" becomes a tangible ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("fortitude").formatted(Formatting.AQUA))
            .append(Text.literal(".").formatted(Formatting.DARK_PURPLE)));
      lore.add(Text.literal("")
            .append(Text.literal("Damage").formatted(Formatting.RED))
            .append(Text.literal(" blocked").formatted(Formatting.BLUE))
            .append(Text.literal(" becomes ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("absorption hearts").formatted(Formatting.YELLOW))
            .append(Text.literal(" and the shield is ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("unbreakable").formatted(Formatting.AQUA))
            .append(Text.literal(".").formatted(Formatting.DARK_PURPLE)));
     return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   public ItemStack updateItem(ItemStack stack, MinecraftServer server){
      BannerPatternsComponent patterns = stack.get(DataComponentTypes.BANNER_PATTERNS);
      DyeColor color = stack.get(DataComponentTypes.BASE_COLOR);
      ItemStack newStack = super.updateItem(stack,server);
      if(patterns != null) stack.set(DataComponentTypes.BANNER_PATTERNS,patterns);
      if(color != null) stack.set(DataComponentTypes.BASE_COLOR,color);
      return buildItemLore(newStack,server);
   }
   
   @Override
   public ItemStack forgeItem(Inventory inv){
      ItemStack shieldStack = inv.getStack(12); // Should be the Sword
      ItemStack newArcanaItem = getNewItem();
      
      if(shieldStack.hasEnchantments()){
         EnchantmentHelper.set(newArcanaItem,shieldStack.getEnchantments());
      }
      
      BannerPatternsComponent patterns = shieldStack.get(DataComponentTypes.BANNER_PATTERNS);
      DyeColor color = shieldStack.get(DataComponentTypes.BASE_COLOR);
      if(color != null){
         newArcanaItem.set(DataComponentTypes.BASE_COLOR,color);
      }
      if(patterns != null){
         newArcanaItem.set(DataComponentTypes.BANNER_PATTERNS,patterns);
      }

      return newArcanaItem;
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
      MiscUtils.addMaxAbsorption(entity, EFFECT_ID,addedAbs);
      entity.setAbsorptionAmount((curAbs + addedAbs));
   }
   
   @Override
   public List<List<Text>> getBookLore(){
      List<List<Text>> list = new ArrayList<>();
      list.add(List.of(Text.literal("  Shield of Fortitude\n\nRarity: Sovereign\n\nTaking after the Wings of Enderia I have successfully recreated their incredible durability. \n\nWhile keeping with the protective nature of the Wings I have been able to infuse extra\n").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal("  Shield of Fortitude\n\nArcana into the four basic protection enchantments, fusing them with the ability of golden apples that grants the consumer a protective barrier.\n\nAs a result half of all damage blocked becomes a barrier lasting 10 seconds.").formatted(Formatting.BLACK)));
      return list;
   }
   
   @Override
	protected ArcanaRecipe makeRecipe(){
      ArcanaIngredient a = new ArcanaIngredient(Items.NETHER_STAR,2);
      ArcanaIngredient b = new ArcanaIngredient(Items.OBSIDIAN,32);
      ArcanaIngredient r = new ArcanaIngredient(Items.ENCHANTED_BOOK,1).withEnchantments(new EnchantmentLevelEntry(MiscUtils.getEnchantment(Enchantments.FIRE_PROTECTION),4));
      ArcanaIngredient c = new ArcanaIngredient(Items.NETHERITE_INGOT,1);
      ArcanaIngredient g = new ArcanaIngredient(Items.GOLDEN_APPLE,16);
      ArcanaIngredient h = new ArcanaIngredient(Items.ENCHANTED_BOOK,1).withEnchantments(new EnchantmentLevelEntry(MiscUtils.getEnchantment(Enchantments.BLAST_PROTECTION),4));
      ArcanaIngredient l = new ArcanaIngredient(Items.ENCHANTED_BOOK,1).withEnchantments(new EnchantmentLevelEntry(MiscUtils.getEnchantment(Enchantments.PROTECTION),4));
      ArcanaIngredient m = new ArcanaIngredient(Items.SHIELD,1, true);
      ArcanaIngredient n = new ArcanaIngredient(Items.ENCHANTED_BOOK,1).withEnchantments(new EnchantmentLevelEntry(MiscUtils.getEnchantment(Enchantments.PROJECTILE_PROTECTION),4));
      
      ArcanaIngredient[][] ingredients = {
            {a,b,c,b,a},
            {b,g,h,g,b},
            {c,l,m,n,c},
            {b,g,r,g,b},
            {a,b,c,b,a}};
      return new ArcanaRecipe(ingredients,new ForgeRequirement().withAnvil().withCore().withEnchanter());
      
   }
   
   public class ShieldOfFortitudeItem extends ArcanaPolymerShieldItem {
      public ShieldOfFortitudeItem(Item.Settings settings){
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
            BlockState blockState = world.getBlockState(blockPos);
            
            if((stack.contains(DataComponentTypes.BANNER_PATTERNS) || stack.contains(DataComponentTypes.BASE_COLOR)) && blockState.getBlock() == Blocks.WATER_CAULDRON){
               stack.remove(DataComponentTypes.BANNER_PATTERNS);
               stack.remove(DataComponentTypes.BASE_COLOR);
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

