package net.borisshoes.arcananovum.items;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.borisshoes.arcananovum.gui.levitationharness.LevitationHarnessGui;
import net.borisshoes.arcananovum.gui.levitationharness.LevitationHarnessInventory;
import net.borisshoes.arcananovum.gui.levitationharness.LevitationHarnessInventoryListener;
import net.borisshoes.arcananovum.gui.shulkercore.ShulkerCoreGui;
import net.borisshoes.arcananovum.gui.shulkercore.ShulkerCoreInventory;
import net.borisshoes.arcananovum.gui.shulkercore.ShulkerCoreInventoryListener;
import net.borisshoes.arcananovum.recipes.MagicItemIngredient;
import net.borisshoes.arcananovum.recipes.MagicItemRecipe;
import net.borisshoes.arcananovum.recipes.ShulkerCoreIngredient;
import net.borisshoes.arcananovum.recipes.SoulstoneIngredient;
import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.borisshoes.arcananovum.utils.MagicRarity;
import net.borisshoes.arcananovum.utils.ParticleEffectUtils;
import net.borisshoes.arcananovum.utils.SoundUtils;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralTextContent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;

public class LevitationHarness extends EnergyItem implements UsableItem,TickingItem{
   
   public LevitationHarness(){
      id = "levitation_harness";
      name = "Levitation Harness";
      rarity = MagicRarity.LEGENDARY;
      maxEnergy = Integer.MAX_VALUE;
      initEnergy = 3600; // 1 hour of charge (1 soul + 16 glowstone dust = 60 seconds of flight)
   
      ItemStack item = new ItemStack(Items.LEATHER_CHESTPLATE);
      NbtCompound tag = item.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList loreList = new NbtList();
      NbtList enchants = new NbtList();
      NbtList attributes = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name","[{\"text\":\"Levitation Harness\",\"italic\":false,\"color\":\"gray\",\"bold\":true}]");
      loreList.add(NbtString.of("[{\"text\":\"Mastery over the nature of \",\"italic\":false,\"color\":\"white\"},{\"text\":\"Shulkers \",\"color\":\"yellow\"},{\"text\":\"has yielded the \"},{\"text\":\"Levitation Harness!\",\"color\":\"gray\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Grants \",\"italic\":false,\"color\":\"white\"},{\"text\":\"creative flight\",\"color\":\"aqua\"},{\"text\":\" while consuming \"},{\"text\":\"Shulker \",\"color\":\"yellow\"},{\"text\":\"souls \",\"color\":\"dark_red\"},{\"text\":\"and \"},{\"text\":\"Glowstone\",\"color\":\"yellow\"},{\"text\":\".\",\"color\":\"white\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"The \",\"italic\":false,\"color\":\"white\"},{\"text\":\"Harness \",\"color\":\"gray\"},{\"text\":\"is quite \"},{\"text\":\"fragile \",\"color\":\"yellow\"},{\"text\":\"and the slightest bump causes it to \"},{\"text\":\"stall\",\"color\":\"aqua\"},{\"text\":\".\",\"color\":\"white\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"The \",\"italic\":false,\"color\":\"white\"},{\"text\":\"Harness \",\"color\":\"gray\"},{\"text\":\"also provides \"},{\"text\":\"no protection\",\"color\":\"dark_red\"},{\"text\":\" against \"},{\"text\":\"damage\",\"color\":\"aqua\"},{\"text\":\".\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Sneak Right click\",\"italic\":false,\"color\":\"blue\"},{\"text\":\" while \",\"color\":\"white\"},{\"text\":\"holding \",\"color\":\"aqua\"},{\"text\":\"the \",\"color\":\"white\"},{\"text\":\"harness \",\"color\":\"gray\"},{\"text\":\"to open the refuelling menu.\",\"color\":\"white\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Flight Duration \",\"italic\":false,\"color\":\"aqua\"},{\"text\":\"- \",\"color\":\"white\"},{\"text\":\"60 Minutes\",\"color\":\"yellow\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Legendary \",\"italic\":false,\"color\":\"gold\",\"bold\":true},{\"text\":\"Magic Item\",\"italic\":false,\"color\":\"dark_purple\",\"bold\":false}]"));
      display.put("Lore",loreList);
      display.putInt("color",9857430);
      NbtCompound armor = new NbtCompound();
      armor.putInt("Amount",0);
      armor.putString("AttributeName","generic.armor");
      armor.putString("Name","generic.armor");
      armor.putString("Slot","chest");
      armor.putIntArray("UUID", new int[]{-122030, 92633, 23139, -185266});
      attributes.add(armor);
      tag.put("display",display);
      tag.put("Enchantments",enchants);
      tag.put("AttributeModifiers",attributes);
      tag.putInt("HideFlags",100);
      tag.putInt("Unbreakable",1);
   
      setBookLore(makeLore());
      setRecipe(makeRecipe());
      tag = addMagicNbt(tag);
      NbtCompound magicTag = tag.getCompound("arcananovum");
      magicTag.putInt("souls",500);
      magicTag.putInt("glowstone",960);
      magicTag.putInt("stall",-1);
      magicTag.putBoolean("wasFlying",false);
      prefNBT = tag;
      item.setNbt(prefNBT);
      prefItem = item;
   }
   
   @Override
   public ItemStack updateItem(ItemStack stack){
      NbtCompound itemNbt = stack.getNbt();
      NbtCompound magicTag = itemNbt.getCompound("arcananovum");
      // For default just replace everything but UUID
      NbtCompound newTag = prefNBT.copy();
      newTag.getCompound("arcananovum").putString("UUID",magicTag.getString("UUID"));
      newTag.getCompound("arcananovum").putInt("energy",magicTag.getInt("energy"));
      newTag.getCompound("arcananovum").putInt("souls",magicTag.getInt("souls"));
      newTag.getCompound("arcananovum").putInt("stall",magicTag.getInt("stall"));
      newTag.getCompound("arcananovum").putInt("glowstone",magicTag.getInt("glowstone"));
      newTag.getCompound("arcananovum").putBoolean("wasFlying",magicTag.getBoolean("wasFlying"));
      stack.setNbt(newTag);
      redoLore(stack);
      return stack;
   }
   
   public void redoLore(ItemStack stack){
      NbtCompound itemNbt = stack.getNbt();
      NbtList loreList = itemNbt.getCompound("display").getList("Lore", NbtType.STRING);
      String duration = getDuration(stack);
      loreList.set(6,NbtString.of("[{\"text\":\"Flight Duration \",\"italic\":false,\"color\":\"aqua\"},{\"text\":\"- \",\"color\":\"white\"},{\"text\":\""+duration+"\",\"color\":\"yellow\"}]"));
   }
   
   public String getDuration(ItemStack item){
      NbtCompound itemNbt = item.getNbt();
      int energy = getEnergy(item);
      String duration;
      if(energy >= 6000){
         duration = ((energy/3600)+1)+" Hours";
      }else if(energy >= 100){
         duration = ((energy/60)+1)+" Minutes";
      }else{
         duration = energy+" Seconds";
      }
      return duration;
   }
   
   public void recalculateEnergy(ItemStack item){
      NbtCompound itemNbt = item.getNbt();
      NbtCompound magicTag = itemNbt.getCompound("arcananovum");
      int souls = magicTag.getInt("souls");
      int glowstone = magicTag.getInt("glowstone") / 16;
      setEnergy(item,60*Math.min(souls,glowstone));
      redoLore(item);
   }
   
   public int getGlow(ItemStack item){
      NbtCompound itemNbt = item.getNbt();
      NbtCompound magicTag = itemNbt.getCompound("arcananovum");
      return magicTag.getInt("glowstone");
   }
   
   public int getSouls(ItemStack item){
      NbtCompound itemNbt = item.getNbt();
      NbtCompound magicTag = itemNbt.getCompound("arcananovum");
      return magicTag.getInt("souls");
   }
   
   public void setStone(ItemStack item, ItemStack stone){
      NbtCompound itemNbt = item.getNbt();
      NbtCompound magicNbt = itemNbt.getCompound("arcananovum");
      if(stone == null){
         magicNbt.putInt("souls",-1);
      }else{
         magicNbt.putInt("souls",Soulstone.getSouls(stone));
      }
   }
   
   public void addGlow(ItemStack item, int glow){
      NbtCompound itemNbt = item.getNbt();
      NbtCompound magicTag = itemNbt.getCompound("arcananovum");
      int newGlow = Math.max(0,magicTag.getInt("glowstone")+glow);
      magicTag.putInt("glowstone",newGlow);
   }
   
   public int getStall(ItemStack item){
      NbtCompound itemNbt = item.getNbt();
      NbtCompound magicTag = itemNbt.getCompound("arcananovum");
      return magicTag.getInt("stall");
   }
   
   public void setStall(ItemStack item, int seconds){
      NbtCompound itemNbt = item.getNbt();
      NbtCompound magicTag = itemNbt.getCompound("arcananovum");
      magicTag.putInt("stall",seconds);
   }
   
   @Override
   public void onTick(ServerWorld world, ServerPlayerEntity player, ItemStack item){
      NbtCompound itemNbt = item.getNbt();
      NbtCompound magicTag = itemNbt.getCompound("arcananovum");
      boolean chestItem = ItemStack.areNbtEqual(player.getEquippedStack(EquipmentSlot.CHEST),item);
      boolean survival = !(player.isCreative() || player.isSpectator());
      boolean flying = player.getAbilities().flying;
      boolean wasFlying = magicTag.getBoolean("wasFlying");
      if(world.getServer().getTicks() % 20 == 0){
         if(chestItem && flying && survival){
            addEnergy(item,-1);
            if(getEnergy(item) % 60 == 0){
               int souls = magicTag.getInt("souls");
               int glowstone = magicTag.getInt("glowstone");
               magicTag.putInt("souls",souls-1);
               magicTag.putInt("glowstone",glowstone-16);
            }
            ParticleEffectUtils.harnessFly(world,player,10);
            PLAYER_DATA.get(player).addXP(25);
            redoLore(item);
   
            if(world.getServer().getTicks() % 120 == 0){
               SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_BEACON_AMBIENT,1f,0.8f);
            }
         }
         int stall = magicTag.getInt("stall");
         if(stall > 0){
            if(stall == 1){
               SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_BEACON_POWER_SELECT,0.5f,1.6f);
               player.sendMessage(Text.translatable("Your Harness Reboots").formatted(Formatting.YELLOW,Formatting.ITALIC),true);
               magicTag.putInt("stall",-1);
            }else{
               magicTag.putInt("stall",stall-1);
            }
         }
      }
      
      if(!chestItem && wasFlying){
         magicTag.putBoolean("wasFlying",false);
      }else if(chestItem && survival){
         if(wasFlying && !flying){
            // Deactivate sound
            SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_BEACON_DEACTIVATE,0.5f,0.9f);
            magicTag.putBoolean("wasFlying",false);
         }else if(!wasFlying && flying){
            // Activate Sound
            SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_BEACON_ACTIVATE,0.5f,1.7f);
            magicTag.putBoolean("wasFlying",true);
         }
      }
   }
   
   public void buildGui(ItemStack item, LevitationHarnessGui gui){
      int souls = getSouls(item);
      int glow = getGlow(item);
      int energy = getEnergy(item);
      String soulText = souls > -1 ? souls + " Shulker Souls" : "No Soulstone Inserted";
      String durationText = energy > 0 ? "Flight Time Remaining: "+getDuration(item) : "No Fuel!";
      String glowText = glow > 0 ? glow + " Glowstone Left" : "No Glowstone Remaining";
      Item soulPane = souls > -1 ? Items.MAGENTA_STAINED_GLASS_PANE : Items.BLACK_STAINED_GLASS_PANE;
      Item durationPane = energy > 0 ? Items.WHITE_STAINED_GLASS_PANE : Items.RED_STAINED_GLASS_PANE;
      Item glowPane = glow > 0 ? Items.YELLOW_STAINED_GLASS_PANE : Items.BLACK_STAINED_GLASS_PANE;
      Formatting soulTextColor = souls > -1 ? Formatting.LIGHT_PURPLE : Formatting.RED;
      Formatting durationTextColor = energy > 0 ? Formatting.GRAY : Formatting.RED;
      Formatting glowTextColor = glow > 0 ? Formatting.GOLD : Formatting.RED;
   
      gui.setSlot(0,new GuiElementBuilder(soulPane).setName(Text.translatable(soulText).formatted(soulTextColor)));
      gui.setSlot(2,new GuiElementBuilder(durationPane).setName(Text.translatable(durationText).formatted(durationTextColor)));
      gui.setSlot(4,new GuiElementBuilder(glowPane).setName(Text.translatable(glowText).formatted(glowTextColor)));
   }
   
   public void openGui(PlayerEntity playerEntity, ItemStack item){
      if(!(playerEntity instanceof ServerPlayerEntity player))
         return;
      LevitationHarnessGui gui = new LevitationHarnessGui(ScreenHandlerType.HOPPER,player,this, item);
      
      int souls = getSouls(item);
      
      for(int i = 0; i < gui.getSize(); i++){
         gui.clearSlot(i);
      }
   
      buildGui(item, gui);
   
      LevitationHarnessInventory inv = new LevitationHarnessInventory();
      LevitationHarnessInventoryListener listener = new LevitationHarnessInventoryListener(this,gui,item);
      inv.addListener(listener);
      listener.setUpdating();
      
      gui.setSlotRedirect(1, new Slot(inv,0,0,0));
      gui.setSlotRedirect(3, new Slot(inv,1,0,0));
      if(souls > -1){
         ItemStack stone = Soulstone.setSouls(Soulstone.setType(MagicItems.SOULSTONE.getNewItem(), EntityType.SHULKER),souls);
         inv.setStack(0,stone);
         gui.validStone(stone);
      }else{
         gui.notValidStone();
      }
      gui.setTitle(Text.translatable("Levitation Harness"));
      listener.finishUpdate();
      
      gui.open();
   }
   
   @Override
   public boolean useItem(PlayerEntity playerEntity, World world, Hand hand){
      if(playerEntity.isSneaking()){
         ItemStack item = playerEntity.getStackInHand(hand);
         openGui(playerEntity,item);
         return false;
      }else{
         return true;
      }
   }
   
   @Override
   public boolean useItem(PlayerEntity playerEntity, World world, Hand hand, BlockHitResult result){
      return true;
   }
   
   @Override
   public ItemStack forgeItem(Inventory inv){
      // Souls n stuff
      ItemStack coreStack = inv.getStack(12); // Should be the Core
      ItemStack newMagicItem = null;
      if(MagicItemUtils.identifyItem(coreStack) instanceof ShulkerCore core){
         newMagicItem = getNewItem();
         setStone(newMagicItem,core.getStone(coreStack));
         redoLore(newMagicItem);
      }
      return newMagicItem;
   }
   
   private MagicItemRecipe makeRecipe(){
      ShulkerCoreIngredient c = new ShulkerCoreIngredient(true,500);
      MagicItemIngredient s = new MagicItemIngredient(Items.SHULKER_SHELL,64,null);
      MagicItemIngredient o = new MagicItemIngredient(Items.GLOWSTONE,64,null);
      MagicItemIngredient n = new MagicItemIngredient(Items.NETHER_STAR,16,null);
      MagicItemIngredient i = new MagicItemIngredient(Items.NETHERITE_INGOT,8,null);
      MagicItemIngredient e = new MagicItemIngredient(Items.ELYTRA,1,null);
      
      MagicItemIngredient[][] ingredients = {
            {o,e,s,e,o},
            {e,n,i,n,e},
            {s,i,c,i,s},
            {e,n,i,n,e},
            {o,e,s,e,o}};
      return new MagicItemRecipe(ingredients);
   }
   
   private List<String> makeLore(){
      ArrayList<String> list = new ArrayList<>();
      list.add("{\"text\":\"  Levitation Harness\\n\\nRarity: Legendary\\n\\nThe sheer amount of effort and research that has gone into this is incomparable. A crowning achievement to be sure. The ability to fly freely through the sky is at my command, albeit fueled by innocent souls. \"}");
      list.add("{\"text\":\"  Levitation Harness\\n\\nGlowstone was an\\nadequate moderator for the Shulker Core but now it is an absolute neccessity that is consumed in large quantities to stabilize the flight reaction. Even with more Glowstone, the reaction is incredibly sensitive to damage.\"}");
      list.add("{\"text\":\"  Levitation Harness\\n\\nWearing the Harness like a chestpiece grants creative flight. Although the Harness provides no armor value and taking even the slightest bump while in flight will destabilize the flight process. The harness then needs a couple seconds to reactivate.\"}");
      return list;
   }
}
