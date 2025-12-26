package net.borisshoes.arcananovum.items;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerItem;
import net.borisshoes.arcananovum.gui.arcanetome.TomeGui;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaRecipe;
import net.borisshoes.arcananovum.recipes.arcana.ExplainIngredient;
import net.borisshoes.arcananovum.recipes.arcana.ExplainRecipe;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaColors;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.borislib.gui.GraphicalItem;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
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
      item = new ArcaneTomeItem();
      displayName = Component.translatableWithFallback("item."+MOD_ID+"."+ID,name).withStyle(ChatFormatting.BOLD, ChatFormatting.DARK_PURPLE);
      researchTasks = new ResourceKey[]{ResearchTasks.OBTAIN_EYE_OF_ENDER,ResearchTasks.ADVANCEMENT_ENCHANT_ITEM};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getDefaultMaxStackSize());
      setPrefStack(stack);
   }
   
   @Override
   public List<Component> getItemLore(@Nullable ItemStack itemStack){
      List<MutableComponent> lore = new ArrayList<>();
      lore.add(Component.literal("")
            .append(Component.literal("The knowledge within shall be your ").withStyle(ChatFormatting.GREEN))
            .append(Component.literal("guide").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(".").withStyle(ChatFormatting.GREEN)));
      lore.add(Component.literal("")
            .append(Component.literal("There is so much ").withStyle(ChatFormatting.GREEN))
            .append(Component.literal("new magic").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(" to explore...").withStyle(ChatFormatting.GREEN)));
      lore.add(Component.literal("")
            .append(Component.literal("Right click").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal(" to open the tome.").withStyle(ChatFormatting.GREEN)));
     return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   public boolean blocksHandInteractions(ItemStack item){
      return true;
   }
   
   public void openGui(Player playerEntity, TomeGui.TomeMode mode, TomeGui.CompendiumSettings settings){
      openGui(playerEntity,mode,settings,"");
   }
   
   public void openGui(Player playerEntity, TomeGui.TomeMode mode, TomeGui.CompendiumSettings settings, String data){
      if(!(playerEntity instanceof ServerPlayer))
         return;
      ServerPlayer player = (ServerPlayer) playerEntity;
      TomeGui gui = null;
      boolean open = false;
      if(mode == TomeGui.TomeMode.PROFILE){
         gui = new TomeGui(MenuType.GENERIC_9x6,player,mode,this,settings);
         gui.buildProfileGui(player);
         open = true;
      }else if(mode == TomeGui.TomeMode.COMPENDIUM){
         gui = new TomeGui(MenuType.GENERIC_9x6,player,mode,this,settings);
         TomeGui.buildCompendiumGui(gui,player,settings);
         open = true;
      }else if(mode == TomeGui.TomeMode.ITEM){
         if(ArcanaItemUtils.getItemFromId(data) != null){
            gui = new TomeGui(MenuType.GENERIC_9x6,player,mode,this,settings);
            TomeGui.buildItemGui(gui,player,data);
            open = true;
         }else{
            gui = new TomeGui(MenuType.GENERIC_9x6,player,mode,this,settings);
            TomeGui.buildCompendiumGui(gui,player,settings);
            open = true;
         }
      }else if(mode == TomeGui.TomeMode.RECIPE){
         if(ArcanaItemUtils.getItemFromId(data) != null){
            openRecipeGui(player,settings,ArcanaItemUtils.getItemFromId(data));
         }else{
            gui = new TomeGui(MenuType.GENERIC_9x6,player,mode,this,settings);
            TomeGui.buildCompendiumGui(gui,player,settings);
            open = true;
         }
      }else if(mode == TomeGui.TomeMode.ACHIEVEMENTS){
         gui = new TomeGui(MenuType.GENERIC_9x6,player,mode,this,settings);
         gui.buildAchievementsGui(player,settings);
         open = true;
      }else if(mode == TomeGui.TomeMode.LEADERBOARD){
         gui = new TomeGui(MenuType.GENERIC_9x6,player,mode,this,settings);
         gui.buildLeaderboardGui(player,settings);
         open = true;
      }else if(mode == TomeGui.TomeMode.RESEARCH){
         gui = new TomeGui(MenuType.GENERIC_9x6,player,mode,this,settings);
         gui.buildResearchGui(player,settings,data);
         open = true;
      }
      if(open){
         gui.setMode(mode);
         gui.open();
      }
   }
   
   public void openRecipeGui(ServerPlayer player, TomeGui.CompendiumSettings settings, ArcanaItem arcanaItem){
      openRecipeGui(player,settings,arcanaItem.getTranslatedName(),arcanaItem.getRecipe(),arcanaItem.getPrefItem());
   }
   
   public void openRecipeGui(ServerPlayer player, TomeGui.CompendiumSettings settings, Component name, ArcanaRecipe recipe, ItemStack output){
      TomeGui gui = new TomeGui(MenuType.GENERIC_9x5,player, TomeGui.TomeMode.RECIPE,this,settings);
      gui.buildRecipeGui(gui,name,recipe,output);
      gui.setMode(TomeGui.TomeMode.RECIPE);
      gui.open();
   }
   
   public void openItemGui(ServerPlayer player, TomeGui.CompendiumSettings settings, String id){
      TomeGui gui = new TomeGui(MenuType.GENERIC_9x6,player,TomeGui.TomeMode.ITEM,this,settings);
      TomeGui.buildItemGui(gui,player,id);
      gui.setMode(TomeGui.TomeMode.ITEM);
      gui.open();
   }
   
   public void openResearchGui(ServerPlayer player, TomeGui.CompendiumSettings settings, String id){
      TomeGui gui = new TomeGui(MenuType.GENERIC_9x6,player,TomeGui.TomeMode.RESEARCH,this,settings);
      gui.buildResearchGui(player,settings,id);
      gui.setMode(TomeGui.TomeMode.RESEARCH);
      gui.open();
   }
   
   @Override
   public List<List<Component>> getBookLore(){
      List<List<Component>> list = new ArrayList<>();
      list.add(List.of(Component.literal("  Tome of Arcana           Novum").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD), Component.literal("\nRarity: ").withStyle(ChatFormatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(),false)), Component.literal("\nThis new type of paper has quite a few interesting properties.\n\nIt allows the inscription of active arcane elements. It makes an excellent parchment for this ").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("  Tome of Arcana           Novum").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD), Component.literal("\nnew notebook.\n\nEnchanting the pages together with an additional Eye of Ender should bind the whole Tome together nicely.\n\nIt is here that I shall \nscribe all the secrets ").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("  Tome of Arcana           Novum").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD), Component.literal("\nof this Arcana Novum that I seek to uncover.").withStyle(ChatFormatting.BLACK)));
      return list;
   }
   
   @Override
	protected ArcanaRecipe makeRecipe(){
      ExplainIngredient a = new ExplainIngredient(GraphicalItem.withColor(GraphicalItem.PAGE_BG, ArcanaColors.DARK_COLOR),1,"",false)
            .withName(Component.literal("In World Recipe").withStyle(ChatFormatting.BLUE, ChatFormatting.BOLD))
            .withLore(List.of(Component.literal("Do this in the World").withStyle(ChatFormatting.DARK_PURPLE)));
      ExplainIngredient t = new ExplainIngredient(Items.ENCHANTING_TABLE,1,"Enchanting Table")
            .withName(Component.literal("Enchanting Table").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
            .withLore(List.of(Component.literal("Place an Enchanting Table in the World").withStyle(ChatFormatting.DARK_PURPLE)));
      ExplainIngredient p = new ExplainIngredient(ArcanaRegistry.MUNDANE_ARCANE_PAPER,4,"Mundane Arcane Paper")
            .withName(Component.literal("Mundane Arcane Paper").withStyle(ChatFormatting.AQUA))
            .withLore(List.of(Component.literal("Place the Paper onto the Enchanting Table").withStyle(ChatFormatting.DARK_PURPLE)));
      ExplainIngredient e = new ExplainIngredient(Items.ENDER_EYE,1,"Eye of Ender")
            .withName(Component.literal("Eye of Ender").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
            .withLore(List.of(Component.literal("Place an Eye of Ender onto the Enchanting Table").withStyle(ChatFormatting.DARK_PURPLE)));
      
      ExplainIngredient[][] ingredients = {
            {a,a,a,a,a},
            {a,a,e,a,a},
            {a,a,p,a,a},
            {a,a,t,a,a},
            {a,a,a,a,a}};
      return new ExplainRecipe(ingredients);
   }
   
   public class ArcaneTomeItem extends ArcanaPolymerItem {
      public ArcaneTomeItem(){
         super(getThis());
      }
      
      @Override
      public ItemStack getDefaultInstance(){
         return prefItem;
      }
      
      @Override
      public InteractionResult use(Level world, Player playerEntity, InteractionHand hand){
         openGui(playerEntity, TomeGui.TomeMode.PROFILE,new TomeGui.CompendiumSettings(0,0));
         return InteractionResult.SUCCESS_SERVER;
      }
      
      @Override
      public void inventoryTick(ItemStack stack, ServerLevel world, Entity entity, @Nullable EquipmentSlot slot){
         if(!ArcanaItemUtils.isArcane(stack)) return;
         
         removeProperty(stack,FORGE_TAG);
      }
   }
}

