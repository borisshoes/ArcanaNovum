package net.borisshoes.arcananovum.items;

import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.callbacks.ShieldTimerCallback;
import net.borisshoes.arcananovum.callbacks.TickTimerCallback;
import net.borisshoes.arcananovum.items.core.AttackingItem;
import net.borisshoes.arcananovum.items.core.MagicItem;
import net.borisshoes.arcananovum.items.core.UsableItem;
import net.borisshoes.arcananovum.recipes.MagicItemIngredient;
import net.borisshoes.arcananovum.recipes.MagicItemRecipe;
import net.borisshoes.arcananovum.utils.MagicRarity;
import net.borisshoes.arcananovum.utils.SoundUtils;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static net.borisshoes.arcananovum.Arcananovum.SERVER_TIMER_CALLBACKS;

public class ShieldOfFortitude extends MagicItem implements AttackingItem, UsableItem {
   public ShieldOfFortitude(){
      id = "shield_of_fortitude";
      name = "Shield of Fortitude";
      rarity = MagicRarity.LEGENDARY;
      categories = new ArcaneTome.TomeFilter[]{ArcaneTome.TomeFilter.LEGENDARY, ArcaneTome.TomeFilter.EQUIPMENT};
      
      ItemStack item = new ItemStack(Items.SHIELD);
      NbtCompound tag = item.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList loreList = new NbtList();
      NbtList enchants = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name","[{\"text\":\"Shield of Fortitude\",\"italic\":false,\"bold\":true,\"color\":\"aqua\"}]");
      loreList.add(NbtString.of("[{\"text\":\"This shield is \",\"italic\":false,\"color\":\"dark_purple\"},{\"text\":\"overflowing\",\"italic\":true,\"color\":\"light_purple\"},{\"text\":\" with powerful \",\"color\":\"dark_purple\"},{\"text\":\"defensive\",\"color\":\"blue\"},{\"text\":\" \",\"color\":\"dark_purple\"},{\"text\":\"magic\",\"color\":\"blue\"},{\"text\":\".\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Your will for \",\"italic\":false,\"color\":\"dark_purple\"},{\"text\":\"protection\",\"color\":\"aqua\"},{\"text\":\" becomes a tangible \"},{\"text\":\"fortitude\",\"color\":\"aqua\"},{\"text\":\".\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Damage\",\"italic\":false,\"color\":\"red\"},{\"text\":\" \",\"color\":\"dark_purple\"},{\"text\":\"blocked\",\"color\":\"blue\"},{\"text\":\" becomes \",\"color\":\"dark_purple\"},{\"text\":\"absorption\",\"color\":\"yellow\"},{\"text\":\" \",\"color\":\"dark_purple\"},{\"text\":\"hearts\",\"color\":\"yellow\"},{\"text\":\" and the shield is \",\"color\":\"dark_purple\"},{\"text\":\"unbreakable\",\"color\":\"aqua\"},{\"text\":\".\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Legendary\",\"italic\":false,\"color\":\"gold\",\"bold\":true},{\"text\":\" Magic Item\",\"italic\":false,\"color\":\"dark_purple\",\"bold\":false}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
      tag.put("Enchantments",enchants);
      tag.putInt("HideFlags",103);
      tag.putInt("Unbreakable",1);
   
      setBookLore(makeLore());
      setRecipe(makeRecipe());
      prefNBT = addMagicNbt(tag);
      item.setNbt(prefNBT);
      prefItem = item;
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
      return stack;
   }
   
   @Override
   public boolean attackEntity(PlayerEntity player, World world, Hand hand, Entity entity, @Nullable EntityHitResult hitResult){
      ItemStack item = player.getStackInHand(hand);
      
      boolean bash = Math.max(0, ArcanaAugments.getAugmentOnItem(item,"shield_bash")) >= 1;
      if(bash && entity instanceof LivingEntity living){
         ArrayList<TickTimerCallback> toRemove = new ArrayList<>();
         float shieldTotal = 0;
         float absAmt = player.getAbsorptionAmount();
         for(int i = 0; i < SERVER_TIMER_CALLBACKS.size(); i++){
            TickTimerCallback t = SERVER_TIMER_CALLBACKS.get(i);
            if(t instanceof ShieldTimerCallback st){
               if(st.getPlayer().getUuidAsString().equals(player.getUuidAsString())){
                  shieldTotal += st.getHearts();
                  st.onTimer();
                  toRemove.add(st);
               }
            }
         }
         shieldTotal = Math.min(absAmt,shieldTotal);
         if(shieldTotal >= 2){
            living.damage(DamageSource.player(player), shieldTotal / 3);
            if(player instanceof ServerPlayerEntity serverPlayer)
               SoundUtils.playSongToPlayer(serverPlayer, SoundEvents.ENTITY_IRON_GOLEM_HURT, .7f, .8f);
         }
         SERVER_TIMER_CALLBACKS.removeIf(toRemove::contains);
      }
      return true;
   }
   
   @Override
   public boolean useItem(PlayerEntity playerEntity, World world, Hand hand){
      return true;
   }
   
   @Override
   public boolean useItem(PlayerEntity playerEntity, World world, Hand hand, BlockHitResult result){
      //TODO Wash in cauldron
      return false;
   }
   
   @Override
   public boolean useItem(PlayerEntity playerEntity, World world, Hand hand, Entity entity, @Nullable EntityHitResult entityHitResult){
      return true;
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
   
   private List<String> makeLore(){
      ArrayList<String> list = new ArrayList<>();
      list.add("{\"text\":\"  Shield of Fortitude\\n\\nRarity: Legendary\\n\\nTaking after the Wings of Zephyr I have successfully recreated their incredible durability. \\n\\nWhile keeping with the protective nature of the Wings I have been able to infuse extra\\n\"}");
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
      return new MagicItemRecipe(ingredients);
   }
}
