package net.borisshoes.arcananovum.items;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerItem;
import net.borisshoes.arcananovum.gui.arcanetome.TomeGui;
import net.borisshoes.arcananovum.items.normal.GraphicItems;
import net.borisshoes.arcananovum.items.normal.GraphicalItem;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaRecipe;
import net.borisshoes.arcananovum.recipes.arcana.ExplainIngredient;
import net.borisshoes.arcananovum.recipes.arcana.ExplainRecipe;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaColors;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.arcananovum.utils.ArcanaRarity;
import net.borisshoes.arcananovum.utils.TextUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class ArcaneTome extends ArcanaItem {
	public static final String ID = "arcane_tome";
   
   public static final String DISPLAY_TAG = "arcanaItemId";
   public static final String FORGE_TAG = "forgeCraftTick";
   public static final String TOME_TAG = "tomeCraftTick";
   
   public ArcaneTome(){
      id = ID;
      name = "Tome of Arcana Novum";
      rarity = ArcanaRarity.MUNDANE;
      categories = new TomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), TomeGui.TomeFilter.ITEMS};
      itemVersion = 1;
      vanillaItem = Items.KNOWLEDGE_BOOK;
      item = new ArcaneTomeItem(addArcanaItemComponents(new Item.Settings().maxCount(1)));
      displayName = Text.translatableWithFallback("item."+MOD_ID+"."+ID,name).formatted(Formatting.BOLD,Formatting.DARK_PURPLE);
      researchTasks = new RegistryKey[]{ResearchTasks.OBTAIN_EYE_OF_ENDER,ResearchTasks.ADVANCEMENT_ENCHANT_ITEM};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getMaxCount());
      setPrefStack(stack);
   }
   
   @Override
   public List<Text> getItemLore(@Nullable ItemStack itemStack){
      List<MutableText> lore = new ArrayList<>();
      lore.add(Text.literal("")
            .append(Text.literal("The knowledge within shall be your ").formatted(Formatting.GREEN))
            .append(Text.literal("guide").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal(".").formatted(Formatting.GREEN)));
      lore.add(Text.literal("")
            .append(Text.literal("There is so much ").formatted(Formatting.GREEN))
            .append(Text.literal("new magic").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal(" to explore...").formatted(Formatting.GREEN)));
      lore.add(Text.literal("")
            .append(Text.literal("Right click").formatted(Formatting.YELLOW))
            .append(Text.literal(" to open the tome.").formatted(Formatting.GREEN)));
     return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   public boolean blocksHandInteractions(ItemStack item){
      return true;
   }
   
   public void openGui(PlayerEntity playerEntity, TomeGui.TomeMode mode, TomeGui.CompendiumSettings settings){
      openGui(playerEntity,mode,settings,"");
   }
   
   public void openGui(PlayerEntity playerEntity, TomeGui.TomeMode mode, TomeGui.CompendiumSettings settings, String data){
      if(!(playerEntity instanceof ServerPlayerEntity))
         return;
      ServerPlayerEntity player = (ServerPlayerEntity) playerEntity;
      TomeGui gui = null;
      boolean open = false;
      if(mode == TomeGui.TomeMode.PROFILE){
         gui = new TomeGui(ScreenHandlerType.GENERIC_9X6,player,mode,this,settings);
         gui.buildProfileGui(player);
         open = true;
      }else if(mode == TomeGui.TomeMode.COMPENDIUM){
         gui = new TomeGui(ScreenHandlerType.GENERIC_9X6,player,mode,this,settings);
         TomeGui.buildCompendiumGui(gui,player,settings);
         open = true;
      }else if(mode == TomeGui.TomeMode.ITEM){
         gui = new TomeGui(ScreenHandlerType.GENERIC_9X6,player,mode,this,settings);
         TomeGui.buildItemGui(gui,player,data);
         open = true;
      }else if(mode == TomeGui.TomeMode.RECIPE){
         if(ArcanaItemUtils.getItemFromId(data) != null){
            openRecipeGui(player,settings,ArcanaItemUtils.getItemFromId(data));
         }
      }else if(mode == TomeGui.TomeMode.ACHIEVEMENTS){
         gui = new TomeGui(ScreenHandlerType.GENERIC_9X6,player,mode,this,settings);
         gui.buildAchievementsGui(player,settings);
         open = true;
      }else if(mode == TomeGui.TomeMode.LEADERBOARD){
         gui = new TomeGui(ScreenHandlerType.GENERIC_9X6,player,mode,this,settings);
         gui.buildLeaderboardGui(player,settings);
         open = true;
      }else if(mode == TomeGui.TomeMode.RESEARCH){
         gui = new TomeGui(ScreenHandlerType.GENERIC_9X6,player,mode,this,settings);
         gui.buildResearchGui(player,settings,data);
         open = true;
      }
      if(open){
         gui.setMode(mode);
         gui.open();
      }
   }
   
   public void openRecipeGui(ServerPlayerEntity player, TomeGui.CompendiumSettings settings, ArcanaItem arcanaItem){
      openRecipeGui(player,settings,arcanaItem.getTranslatedName(),arcanaItem.getRecipe(),arcanaItem.getPrefItem());
   }
   
   public void openRecipeGui(ServerPlayerEntity player, TomeGui.CompendiumSettings settings, Text name, ArcanaRecipe recipe, ItemStack output){
      TomeGui gui = new TomeGui(ScreenHandlerType.GENERIC_9X5,player, TomeGui.TomeMode.RECIPE,this,settings);
      gui.buildRecipeGui(gui,name,recipe,output);
      gui.setMode(TomeGui.TomeMode.RECIPE);
      gui.open();
   }
   
   public void openItemGui(ServerPlayerEntity player, TomeGui.CompendiumSettings settings, String id){
      TomeGui gui = new TomeGui(ScreenHandlerType.GENERIC_9X6,player,TomeGui.TomeMode.ITEM,this,settings);
      TomeGui.buildItemGui(gui,player,id);
      gui.setMode(TomeGui.TomeMode.ITEM);
      gui.open();
   }
   
   public void openResearchGui(ServerPlayerEntity player, TomeGui.CompendiumSettings settings, String id){
      TomeGui gui = new TomeGui(ScreenHandlerType.GENERIC_9X6,player,TomeGui.TomeMode.RESEARCH,this,settings);
      gui.buildResearchGui(player,settings,id);
      gui.setMode(TomeGui.TomeMode.RESEARCH);
      gui.open();
   }
   
   @Override
   public List<List<Text>> getBookLore(){
      List<List<Text>> list = new ArrayList<>();
      list.add(List.of(Text.literal("  Tome of Arcana           Novum").formatted(Formatting.DARK_PURPLE,Formatting.BOLD),Text.literal("\nRarity: ").formatted(Formatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(),false)),Text.literal("\nThis new type of paper has quite a few interesting properties.\n\nIt allows the inscription of active arcane elements. It makes an excellent parchment for this ").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal("  Tome of Arcana           Novum").formatted(Formatting.DARK_PURPLE,Formatting.BOLD),Text.literal("\nnew notebook.\n\nEnchanting the pages together with an additional Eye of Ender should bind the whole Tome together nicely.\n\nIt is here that I shall \nscribe all the secrets ").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal("  Tome of Arcana           Novum").formatted(Formatting.DARK_PURPLE,Formatting.BOLD),Text.literal("\nof this Arcana Novum that I seek to uncover.").formatted(Formatting.BLACK)));
      return list;
   }
   
   @Override
	protected ArcanaRecipe makeRecipe(){
      ExplainIngredient a = new ExplainIngredient(GraphicalItem.withColor(GraphicItems.PAGE_BG, ArcanaColors.DARK_COLOR),1,"",false)
            .withName(Text.literal("In World Recipe").formatted(Formatting.BLUE,Formatting.BOLD))
            .withLore(List.of(Text.literal("Do this in the World").formatted(Formatting.DARK_PURPLE)));
      ExplainIngredient t = new ExplainIngredient(Items.ENCHANTING_TABLE,1,"Enchanting Table")
            .withName(Text.literal("Enchanting Table").formatted(Formatting.DARK_AQUA,Formatting.BOLD))
            .withLore(List.of(Text.literal("Place an Enchanting Table in the World").formatted(Formatting.DARK_PURPLE)));
      ExplainIngredient p = new ExplainIngredient(ArcanaRegistry.MUNDANE_ARCANE_PAPER,4,"Mundane Arcane Paper")
            .withName(Text.literal("Mundane Arcane Paper").formatted(Formatting.AQUA))
            .withLore(List.of(Text.literal("Place the Paper onto the Enchanting Table").formatted(Formatting.DARK_PURPLE)));
      ExplainIngredient e = new ExplainIngredient(Items.ENDER_EYE,1,"Eye of Ender")
            .withName(Text.literal("Eye of Ender").formatted(Formatting.GREEN,Formatting.BOLD))
            .withLore(List.of(Text.literal("Place an Eye of Ender onto the Enchanting Table").formatted(Formatting.DARK_PURPLE)));
      
      ExplainIngredient[][] ingredients = {
            {a,a,a,a,a},
            {a,a,e,a,a},
            {a,a,p,a,a},
            {a,a,t,a,a},
            {a,a,a,a,a}};
      return new ExplainRecipe(ingredients);
   }
   
   public class ArcaneTomeItem extends ArcanaPolymerItem {
      public ArcaneTomeItem(Item.Settings settings){
         super(getThis(),settings);
      }
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
      
      @Override
      public ActionResult use(World world, PlayerEntity playerEntity, Hand hand){
         openGui(playerEntity, TomeGui.TomeMode.PROFILE,new TomeGui.CompendiumSettings(0,0));
         return ActionResult.SUCCESS_SERVER;
      }
      
      @Override
      public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected){
         if(!ArcanaItemUtils.isArcane(stack)) return;
         
         removeProperty(stack,FORGE_TAG);
      }
   }
}

