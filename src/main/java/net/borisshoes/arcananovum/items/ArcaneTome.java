package net.borisshoes.arcananovum.items;

import com.mojang.authlib.GameProfile;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.borisshoes.arcananovum.cardinalcomponents.IArcanaProfileComponent;
import net.borisshoes.arcananovum.gui.arcanetome.CraftingInventory;
import net.borisshoes.arcananovum.gui.arcanetome.CraftingInventoryListener;
import net.borisshoes.arcananovum.gui.arcanetome.TomeGui;
import net.borisshoes.arcananovum.recipes.MagicItemIngredient;
import net.borisshoes.arcananovum.recipes.MagicItemRecipe;
import net.borisshoes.arcananovum.utils.LevelUtils;
import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.borisshoes.arcananovum.utils.MagicRarity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;

import java.util.*;

import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;

public class ArcaneTome extends MagicItem implements UsableItem{
   private IArcanaProfileComponent profile;
   private final int[] craftingSlots = {1,2,3,4,5,10,11,12,13,14,19,20,21,22,23,28,29,30,31,32,37,38,39,40,41};
   
   public ArcaneTome(){
      id = "arcane_tome";
      name = "Tome of Arcana Novum";
      rarity = MagicRarity.EMPOWERED;
   
      ItemStack item = new ItemStack(Items.END_PORTAL_FRAME);
      NbtCompound tag = item.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList loreList = new NbtList();
      NbtList enchants = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name","[{\"text\":\"Tome of Arcana Novum\",\"italic\":false,\"bold\":true,\"color\":\"dark_purple\"}]");
      loreList.add(NbtString.of("[{\"text\":\"The knowledge within shall be your \",\"italic\":false,\"color\":\"green\"},{\"text\":\"guide\",\"color\":\"light_purple\"},{\"text\":\".\",\"color\":\"green\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"There is so much \",\"italic\":false,\"color\":\"green\"},{\"text\":\"new magic\",\"color\":\"light_purple\"},{\"text\":\" to explore...\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Right click\",\"italic\":false,\"color\":\"yellow\"},{\"text\":\" to open the tome.\",\"color\":\"green\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Empowered\",\"italic\":false,\"color\":\"green\",\"bold\":true},{\"text\":\" Magic Item\",\"italic\":false,\"color\":\"dark_purple\",\"bold\":false}]"));
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
      openGui(playerEntity,0);
      return false;
   }
   
   @Override
   public boolean useItem(PlayerEntity playerEntity, World world, Hand hand, BlockHitResult result){
      openGui(playerEntity,0);
      return false;
   }
   
   public void getOrCreateProfile(ServerPlayerEntity player){
      this.profile = PLAYER_DATA.get(player);
      if(profile.getLevel() == 0){
         // Profile needs initialization
         profile.setLevel(1);
         
         // Right now all recipes are unlocked
         for(MagicItem item : MagicItems.registry.values()){
            profile.addRecipe(item.getId());
         }
      }
      // update level from xp just in case levelling changed
      profile.setLevel(LevelUtils.levelFromXp(profile.getXP()));
   }
   
   public void openGui(PlayerEntity playerEntity, int mode){
      if(!(playerEntity instanceof ServerPlayerEntity))
         return;
      ServerPlayerEntity player = (ServerPlayerEntity) playerEntity;
      getOrCreateProfile(player);
      TomeGui gui = null;
      if(mode == 0){ // Profile
         gui = new TomeGui(ScreenHandlerType.GENERIC_9X6,player,mode,this);
         buildProfileGui(gui,player);
      }else if(mode == 1){ // Compendium
         gui = new TomeGui(ScreenHandlerType.GENERIC_9X6,player,mode,this);
         buildItemsGui(gui,player);
      }else if(mode == 2){ // Crafting
         gui = new TomeGui(ScreenHandlerType.GENERIC_9X5,player,mode,this);
         buildCraftingGui(gui,player);
      }
      gui.setMode(mode);
      gui.open();
   }
   
   public void openRecipeGui(ServerPlayerEntity player, String id){
      TomeGui gui = new TomeGui(ScreenHandlerType.GENERIC_9X5,player,3,this);
      buildRecipeGui(gui,player,id);
      gui.setMode(3);
      gui.open();
   }
   
   public void buildProfileGui(TomeGui gui, ServerPlayerEntity player){
      gui.setMode(0);
      for(int i = 0; i < gui.getSize(); i++){
         gui.clearSlot(i);
         gui.setSlot(i,new GuiElementBuilder(Items.PURPLE_STAINED_GLASS_PANE).setName(LiteralText.EMPTY.copy()));
      }
      
      GameProfile gameProfile = new GameProfile(player.getUuid(),null);
      GuiElementBuilder head = new GuiElementBuilder(Items.PLAYER_HEAD).setSkullOwner(gameProfile,player.server);
      head.setName((new LiteralText("").append(new LiteralText(player.getEntityName()+"'s ").formatted(Formatting.AQUA)).append(new LiteralText("Arcane Profile").formatted(Formatting.DARK_PURPLE))));
      head.addLoreLine((new LiteralText("").append(new LiteralText("Click").formatted(Formatting.YELLOW)).append(new LiteralText(" for a brief overview of Arcana Novum!").formatted(Formatting.LIGHT_PURPLE))));
      gui.setSlot(4,head);
      
      ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
      NbtCompound tag = book.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList loreList = new NbtList();
      display.putString("Name","[{\"text\":\"Magic Items\",\"italic\":false,\"color\":\"dark_purple\"}]");
      loreList.add(NbtString.of("[{\"text\":\"Click \",\"italic\":false,\"color\":\"yellow\"},{\"text\":\"to go to the Magic Items Page\",\"color\":\"light_purple\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
      tag.putInt("HideFlags",103);
      gui.setSlot(49,GuiElementBuilder.from(book));
      
      ItemStack lecturn = new ItemStack(Items.LECTERN);
      tag = lecturn.getOrCreateNbt();
      display = new NbtCompound();
      loreList = new NbtList();
      display.putString("Name","[{\"text\":\"Arcana Level\",\"italic\":false,\"color\":\"dark_green\"}]");
      loreList.add(NbtString.of("[{\"text\":\"Arcana Level: "+profile.getLevel()+"\",\"italic\":false,\"color\":\"green\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Experience: "+LevelUtils.getCurLevelXp(profile.getXP())+"/"+LevelUtils.nextLevelNewXp(profile.getLevel())+"\",\"italic\":false,\"color\":\"green\"},{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"You can increase your arcana by crafting and using magic items!\",\"italic\":false,\"color\":\"light_purple\"}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
      tag.putInt("HideFlags",103);
      gui.setSlot(10,GuiElementBuilder.from(lecturn));
      
      int filled = (int)Math.round((double)LevelUtils.getCurLevelXp(profile.getXP())/LevelUtils.nextLevelNewXp(profile.getLevel()) * 6.0);
      for(int i = 11; i <= 16; i++){
         if(i >= filled+11){
            gui.setSlot(i,new GuiElementBuilder(Items.GLASS_BOTTLE).setName(new LiteralText("XP: "+LevelUtils.getCurLevelXp(profile.getXP())+"/"+LevelUtils.nextLevelNewXp(profile.getLevel())).formatted(Formatting.GREEN)));
   
         }else{
            gui.setSlot(i,new GuiElementBuilder(Items.EXPERIENCE_BOTTLE).setName(new LiteralText("XP: "+LevelUtils.getCurLevelXp(profile.getXP())+"/"+LevelUtils.nextLevelNewXp(profile.getLevel())).formatted(Formatting.GREEN)));
         }
      }
   
      ItemStack crystal = new ItemStack(Items.END_CRYSTAL);
      tag = crystal.getOrCreateNbt();
      display = new NbtCompound();
      loreList = new NbtList();
      display.putString("Name","[{\"text\":\"Arcane Concentration\",\"italic\":false,\"color\":\"blue\"}]");
      loreList.add(NbtString.of("[{\"text\":\"Concentration: "+MagicItemUtils.getUsedConcentration(player)+"/"+LevelUtils.concFromLevel(profile.getLevel())+"\",\"italic\":false,\"color\":\"aqua\"},{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Your max concentration increases with your level!\",\"italic\":false,\"color\":\"light_purple\"},{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      
      List<String> concBreakdown = MagicItemUtils.getConcBreakdown(player);
      if(!concBreakdown.isEmpty()){
         loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
         loreList.add(NbtString.of("[{\"text\":\"Items Taking Concentration:\",\"italic\":false,\"color\":\"dark_aqua\"},{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
         for(String item : concBreakdown){
            loreList.add(NbtString.of("[{\"text\":\" - "+item+"\",\"italic\":false,\"color\":\"dark_aqua\"}]"));
         }
      }
      
      display.put("Lore",loreList);
      tag.put("display",display);
      tag.putInt("HideFlags",103);
      gui.setSlot(28,GuiElementBuilder.from(crystal));
   
      int used = (int)Math.floor((double)MagicItemUtils.getUsedConcentration(player)/LevelUtils.concFromLevel(profile.getLevel()) * 6.0);
      for(int i = 29; i <= 34; i++){
         if(i >= used+29){
            gui.setSlot(i,new GuiElementBuilder(Items.SLIME_BALL).setName(new LiteralText("Concentration: "+MagicItemUtils.getUsedConcentration(player)+"/"+LevelUtils.concFromLevel(profile.getLevel())).formatted(Formatting.AQUA)));
         }else{
            gui.setSlot(i,new GuiElementBuilder(Items.MAGMA_CREAM).setName(new LiteralText("Concentration: "+MagicItemUtils.getUsedConcentration(player)+"/"+LevelUtils.concFromLevel(profile.getLevel())).formatted(Formatting.AQUA)));
         }
         
      }
   
      gui.setTitle(new LiteralText("Arcane Profile"));
   }
   
   public void buildItemsGui(TomeGui gui, ServerPlayerEntity player){
      gui.setMode(1);
      for(int i = 0; i < gui.getSize(); i++){
         gui.clearSlot(i);
         gui.setSlot(i,new GuiElementBuilder(Items.PURPLE_STAINED_GLASS_PANE).setName(LiteralText.EMPTY.copy()));
      }
   
      ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
      NbtCompound tag = book.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList loreList = new NbtList();
      display.putString("Name","[{\"text\":\"Magic Items\",\"italic\":false,\"color\":\"dark_purple\"}]");
      loreList.add(NbtString.of("[{\"text\":\"Click here\",\"italic\":false,\"color\":\"yellow\"},{\"text\":\" to return to the Profile Page\",\"color\":\"light_purple\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Click an item\",\"italic\":false,\"color\":\"green\"},{\"text\":\" to learn about it!\",\"color\":\"light_purple\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Right Click an item\",\"italic\":false,\"color\":\"aqua\"},{\"text\":\" to see how to make it\",\"color\":\"light_purple\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
      tag.putInt("HideFlags",103);
      gui.setSlot(4,GuiElementBuilder.from(book));
   
      ItemStack table = new ItemStack(Items.CRAFTING_TABLE);
      tag = table.getOrCreateNbt();
      display = new NbtCompound();
      loreList = new NbtList();
      display.putString("Name","[{\"text\":\"Craft A Magic Item!\",\"italic\":false,\"color\":\"dark_purple\"}]");
      loreList.add(NbtString.of("[{\"text\":\"Click\",\"italic\":false,\"color\":\"yellow\"},{\"text\":\" to go to the Crafting Menu\",\"color\":\"light_purple\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Click an item\",\"italic\":false,\"color\":\"green\"},{\"text\":\" to learn about it!\",\"color\":\"light_purple\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Right Click an item\",\"italic\":false,\"color\":\"aqua\"},{\"text\":\" to see how to make it\",\"color\":\"light_purple\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
      tag.putInt("HideFlags",103);
      gui.setSlot(49,GuiElementBuilder.from(table));
   

      List<MagicItem> magicItems = new ArrayList<>(MagicItems.registry.values().stream().toList());
      Collections.sort(magicItems);
      
      int k = 0;
      for(int i = 0; i < 4; i++){
         for(int j = 0; j < 7; j++){
            if(k < magicItems.size()){
               gui.setSlot((i*9+10)+j,GuiElementBuilder.from(magicItems.get(k).getPrefItem()).glow());
            }else{
               gui.setSlot((i*9+10)+j,new GuiElementBuilder(Items.AIR));
            }
            k++;
         }
      }
      
      gui.setTitle(new LiteralText("Item Compendium"));
   }
   
   public void buildRecipeGui(TomeGui gui, ServerPlayerEntity player, String id){
      gui.setMode(3);
      MagicItem magicItem = MagicItemUtils.getItemFromId(id);
      if(magicItem == null){
         gui.close();
         return;
      }
      
      for(int i = 0; i < gui.getSize(); i++){
         gui.clearSlot(i);
         gui.setSlot(i,new GuiElementBuilder(Items.PURPLE_STAINED_GLASS_PANE).setName(LiteralText.EMPTY.copy()));
      }
   
      ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
      NbtCompound tag = book.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList loreList = new NbtList();
      display.putString("Name","[{\"text\":\"Magic Items\",\"italic\":false,\"color\":\"dark_purple\"}]");
      loreList.add(NbtString.of("[{\"text\":\"Click \",\"italic\":false,\"color\":\"yellow\"},{\"text\":\"to return to the Compendium.\",\"color\":\"light_purple\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
      tag.putInt("HideFlags",103);
      gui.setSlot(7,GuiElementBuilder.from(book));
   
      ItemStack table = new ItemStack(Items.CRAFTING_TABLE);
      tag = table.getOrCreateNbt();
      display = new NbtCompound();
      loreList = new NbtList();
      display.putString("Name","[{\"text\":\"Forge Item\",\"italic\":false,\"color\":\"dark_purple\"}]");
      loreList.add(NbtString.of("[{\"text\":\"Click Here\",\"italic\":false,\"color\":\"green\"},{\"text\":\" to go to the Forging Menu!\",\"color\":\"light_purple\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
      tag.putInt("HideFlags",103);
      gui.setSlot(43,GuiElementBuilder.from(table));
   
      gui.setSlot(25,GuiElementBuilder.from(magicItem.getPrefItem()).glow());
   
      MagicItemRecipe recipe = magicItem.getRecipe();
      MagicItemIngredient[][] ingredients = recipe.getIngredients();
      for(int i = 0; i < 25; i++){
         ItemStack ingredient = ingredients[i/5][i%5].ingredientAsStack();
         gui.setSlot(craftingSlots[i], GuiElementBuilder.from(ingredient));
      }
   
      gui.setTitle(new LiteralText("Recipe for "+magicItem.getName()));
   }
   
   public void buildCraftingGui(TomeGui gui, ServerPlayerEntity player){
      gui.setMode(2);
      for(int i = 0; i < gui.getSize(); i++){
         gui.clearSlot(i);
         gui.setSlot(i,new GuiElementBuilder(Items.PURPLE_STAINED_GLASS_PANE).setName(LiteralText.EMPTY.copy()));
      }
   
      GameProfile gameProfile = new GameProfile(player.getUuid(),null);
      GuiElementBuilder head = new GuiElementBuilder(Items.PLAYER_HEAD).setSkullOwner(gameProfile,player.server);
      head.setName((new LiteralText("").append(new LiteralText(player.getEntityName()+"'s ").formatted(Formatting.AQUA)).append(new LiteralText("Arcane Profile").formatted(Formatting.DARK_PURPLE))));
      head.addLoreLine((new LiteralText("").append(new LiteralText("Click").formatted(Formatting.YELLOW)).append(new LiteralText(" to go to your Profile").formatted(Formatting.LIGHT_PURPLE))));
      gui.setSlot(43,head);
   
      ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
      NbtCompound tag = book.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList loreList = new NbtList();
      display.putString("Name","[{\"text\":\"Magic Items\",\"italic\":false,\"color\":\"dark_purple\"}]");
      loreList.add(NbtString.of("[{\"text\":\"Click \",\"italic\":false,\"color\":\"yellow\"},{\"text\":\"to view a Magic Item Recipe\",\"color\":\"light_purple\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
      tag.putInt("HideFlags",103);
      gui.setSlot(7,GuiElementBuilder.from(book));
   
      ItemStack table = new ItemStack(Items.CRAFTING_TABLE);
      tag = table.getOrCreateNbt();
      display = new NbtCompound();
      loreList = new NbtList();
      display.putString("Name","[{\"text\":\"Forge Item\",\"italic\":false,\"color\":\"dark_purple\"}]");
      loreList.add(NbtString.of("[{\"text\":\"Click Here\",\"italic\":false,\"color\":\"green\"},{\"text\":\" to forge a Magic Item once a recipe is loaded!\",\"color\":\"light_purple\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"This slot will show a Magic Item once a valid recipe is loaded.\",\"italic\":true,\"color\":\"aqua\"}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
      tag.putInt("HideFlags",103);
      gui.setSlot(25,GuiElementBuilder.from(table));
   
      for(int i = 0; i < 25; i++){
         gui.setSlot(craftingSlots[i], new GuiElementBuilder(Items.AIR));
      }
      
      CraftingInventory inv = new CraftingInventory();
      CraftingInventoryListener listener = new CraftingInventoryListener(this,gui);
      inv.addListener(listener);
      ItemStack[] ingredients = new ItemStack[25];
      for(int i = 0; i<25;i++){
         ingredients[i] = ItemStack.EMPTY;
         gui.setSlotRedirect(craftingSlots[i], new Slot(inv,i,0,0));
      }
   
      gui.setTitle(new LiteralText("Forge Items"));
   }
   
   private List<String> makeLore(){
      ArrayList<String> list = new ArrayList<>();
      list.add("{\"text\":\"Tome of Arcana Novum\\n\\nRarity: Empowered\\n\\nStrangely enough, this Tome is incredibly easy to craft compared to most other Magic Items, like it wants to share its knowledge.\\n\\nThe way the Eye of Ender is so naturally \"}");
      list.add("{\"text\":\"Tome of Arcana Novum\\n\\nAttracted to the enchantment table is definitely curious.\\n\\nHowever, as a result of its ease of construction, it offers no Crafting XP like other Magic Items do.\\n\\nIt acts as a guide and forge for those who\"}");
      list.add("{\"text\":\"Tome of Arcana Novum\\n\\nseek the secrets of Arcana Novum.\"}");
      return list;
   }
   
   private MagicItemRecipe makeRecipe(){
      MagicItemIngredient a = new MagicItemIngredient(Items.AIR,1,null);
      MagicItemIngredient t = new MagicItemIngredient(Items.ENCHANTING_TABLE,1,null);
      MagicItemIngredient e = new MagicItemIngredient(Items.ENDER_EYE,1,null);
      
      MagicItemIngredient[][] ingredients = {
            {a,a,a,a,a},
            {a,a,e,a,a},
            {a,a,t,a,a},
            {a,a,a,a,a},
            {a,a,a,a,a}};
      return new MagicItemRecipe(ingredients);
   }
}
