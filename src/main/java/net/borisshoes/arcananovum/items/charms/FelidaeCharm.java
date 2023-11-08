package net.borisshoes.arcananovum.items.charms;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.core.MagicItem;
import net.borisshoes.arcananovum.core.polymer.MagicPolymerItem;
import net.borisshoes.arcananovum.items.ArcaneTome;
import net.borisshoes.arcananovum.recipes.arcana.ForgeRequirement;
import net.borisshoes.arcananovum.recipes.arcana.MagicItemIngredient;
import net.borisshoes.arcananovum.recipes.arcana.MagicItemRecipe;
import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.borisshoes.arcananovum.utils.MagicRarity;
import net.borisshoes.arcananovum.utils.SoundUtils;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class FelidaeCharm extends MagicItem {
   
   public FelidaeCharm(){
      id = "felidae_charm";
      name = "Charm of Felidae";
      rarity = MagicRarity.EXOTIC;
      categories = new ArcaneTome.TomeFilter[]{ArcaneTome.TomeFilter.EXOTIC, ArcaneTome.TomeFilter.CHARMS, ArcaneTome.TomeFilter.ITEMS};
      itemVersion = 1;
      vanillaItem = Items.STRING;
      item = new FelidaeCharmItem(new FabricItemSettings().maxCount(1).fireproof());
   
      ItemStack stack = new ItemStack(item);
      NbtCompound tag = stack.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList enchants = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name","[{\"text\":\"Charm of Felidae\",\"italic\":false,\"bold\":true,\"color\":\"yellow\"}]");
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
      loreList.add(NbtString.of("[{\"text\":\"The charm \",\"italic\":false,\"color\":\"gold\"},{\"text\":\"purrs \",\"color\":\"yellow\"},{\"text\":\"softly when worn.\",\"color\":\"gold\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Keeping this \",\"italic\":false,\"color\":\"gold\"},{\"text\":\"charm \",\"color\":\"yellow\"},{\"text\":\"on your person gives you \"},{\"text\":\"cat-like\",\"color\":\"gray\"},{\"text\":\" abilities.\",\"color\":\"gold\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Your \",\"italic\":false,\"color\":\"gold\"},{\"text\":\"falls \",\"color\":\"gray\"},{\"text\":\"become somewhat \"},{\"text\":\"graceful \",\"color\":\"aqua\"},{\"text\":\"and \"},{\"text\":\"cushioned\",\"color\":\"aqua\"},{\"text\":\".\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Creepers \",\"italic\":false,\"color\":\"dark_green\"},{\"text\":\"and \",\"color\":\"gold\"},{\"text\":\"Phantoms \",\"color\":\"blue\"},{\"text\":\"give you a \",\"color\":\"gold\"},{\"text\":\"wide berth\",\"color\":\"yellow\"},{\"text\":\".\",\"color\":\"gold\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      return loreList;
   }
   
   private MagicItemRecipe makeRecipe(){
      MagicItemIngredient m = new MagicItemIngredient(Items.PHANTOM_MEMBRANE,32,null);
      MagicItemIngredient s = new MagicItemIngredient(Items.STRING,64,null);
      MagicItemIngredient g = new MagicItemIngredient(Items.GUNPOWDER,64,null);
      MagicItemIngredient c = new MagicItemIngredient(Items.TROPICAL_FISH,64,null);
      MagicItemIngredient p = new MagicItemIngredient(Items.PUFFERFISH,64,null);
      MagicItemIngredient f = new MagicItemIngredient(Items.COD,64,null);
      MagicItemIngredient l = new MagicItemIngredient(Items.SALMON,64,null);
      MagicItemIngredient h = new MagicItemIngredient(Items.CREEPER_HEAD,1,null, true);
      MagicItemIngredient b = new MagicItemIngredient(Items.ENCHANTED_BOOK,1, EnchantedBookItem.forEnchantment(new EnchantmentLevelEntry(Enchantments.FEATHER_FALLING,4)).getNbt());
      
      MagicItemIngredient[][] ingredients = {
            {m,s,g,s,m},
            {s,b,c,b,s},
            {g,l,h,p,g},
            {s,b,f,b,s},
            {m,s,g,s,m}};
      return new MagicItemRecipe(ingredients, new ForgeRequirement().withEnchanter());
   }
   
   private List<String> makeLore(){
      ArrayList<String> list = new ArrayList<>();
      list.add("{\"text\":\"   Charm of Felidae\\n\\nRarity: Empowered\\n\\nCats are quite powerful creatures, managing to frighten phantoms and scare creepers. They can even fall from any height without care.\\nThis Charm seeks to mimic a fraction of that power.\"}");
      list.add("{\"text\":\"   Charm of Felidae\\n\\nThe Charm halves all fall damage, stops phantoms from swooping the holder, and gives creepers a good scare every now and then.\"}");
      return list;
   }
   
   public class FelidaeCharmItem extends MagicPolymerItem {
      public FelidaeCharmItem(Settings settings){
         super(getThis(),settings);
      }
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
      
      @Override
      public TypedActionResult<ItemStack> use(World world, PlayerEntity playerEntity, Hand hand){
         ItemStack stack = playerEntity.getStackInHand(hand);
         if(!(playerEntity instanceof ServerPlayerEntity player)) return TypedActionResult.pass(stack);
         SoundUtils.playSongToPlayer(player, SoundEvents.ENTITY_CAT_AMBIENT, 1f, (float) (0.5*(Math.random()-0.5)+1));
         return TypedActionResult.success(stack);
      }
      
      @Override
      public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected){
         if(!MagicItemUtils.isMagic(stack)) return;
         if(!(world instanceof ServerWorld && entity instanceof ServerPlayerEntity player)) return;
         if(world.getServer().getTicks() % 20 == 0 && !player.isSpectator()){
            Vec3d pos = player.getPos();
            Box rangeBox = new Box(pos.x+5,pos.y+3,pos.z+5,pos.x-5,pos.y-3,pos.z-5);
            List<Entity> entities = world.getOtherEntities(null,rangeBox, e -> !e.isSpectator() && e instanceof CreeperEntity);
            if(entities.size() >= 4) ArcanaAchievements.grant(player,ArcanaAchievements.INFILTRATION.id);
         }
      }
   }
}
