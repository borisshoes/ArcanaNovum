package net.borisshoes.arcananovum.items.catalysts;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerItem;
import net.borisshoes.arcananovum.datastorage.ArcanaPlayerData;
import net.borisshoes.arcananovum.gui.arcanetome.ArcaneTomeGui;
import net.borisshoes.arcananovum.gui.transmogrification.TransmogrificationGui;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.skins.ArcanaSkin;
import net.borisshoes.arcananovum.utils.ArcanaColors;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class TransmogrificationCatalyst extends ArcanaItem {
   public static final String ID = "transmogrification_catalyst";
   
   public static final String SELECTED_SKIN_TAG = "selected_skin_tag";
   
   public TransmogrificationCatalyst(){
      id = ID;
      name = "Transmogrification Catalyst";
      rarity = ArcanaRarity.MUNDANE;
      categories = new ArcaneTomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), ArcaneTomeGui.TomeFilter.CATALYSTS};
      itemVersion = 0;
      vanillaItem = Items.NETHER_STAR;
      item = new TransmogrificationCatalyst.TransmogrificationCatalystItem();
      displayName = Component.translatableWithFallback("item." + MOD_ID + "." + ID, name).withStyle(ChatFormatting.BOLD, ChatFormatting.WHITE);
      researchTasks = new ResourceKey[]{ResearchTasks.UNLOCK_RUNIC_MATRIX, ResearchTasks.UNLOCK_CATALYTIC_MATRIX, ResearchTasks.UNLOCK_TWILIGHT_ANVIL, ResearchTasks.HAVE_A_SKIN};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      putProperty(stack, SELECTED_SKIN_TAG, "");
      setPrefStack(stack);
   }
   
   @Override
   public List<Component> getItemLore(@Nullable ItemStack itemStack){
      List<MutableComponent> lore = new ArrayList<>();
      lore.add(Component.literal("")
            .append(Component.literal("An adapted ").withStyle(ChatFormatting.AQUA))
            .append(Component.literal("Catalytic Matrix").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal(" for a more ").withStyle(ChatFormatting.AQUA))
            .append(Component.literal("c").withStyle(ChatFormatting.DARK_RED))
            .append(Component.literal("o").withStyle(ChatFormatting.RED))
            .append(Component.literal("l").withStyle(ChatFormatting.GOLD))
            .append(Component.literal("o").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("r").withStyle(ChatFormatting.GREEN))
            .append(Component.literal("f").withStyle(ChatFormatting.AQUA))
            .append(Component.literal("u").withStyle(ChatFormatting.BLUE))
            .append(Component.literal("l").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal(" purpose.").withStyle(ChatFormatting.AQUA)));
      lore.add(Component.literal("")
            .append(Component.literal("The Matrix").withStyle(ChatFormatting.WHITE))
            .append(Component.literal(" can be ").withStyle(ChatFormatting.AQUA))
            .append(Component.literal("attuned").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal(" to a ").withStyle(ChatFormatting.AQUA))
            .append(Component.literal("skin").withStyle(ChatFormatting.WHITE))
            .append(Component.literal(" for an ").withStyle(ChatFormatting.AQUA))
            .append(Component.literal("Arcana Item").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(".").withStyle(ChatFormatting.AQUA)));
      lore.add(Component.literal("")
            .append(Component.literal("Once ").withStyle(ChatFormatting.AQUA))
            .append(Component.literal("attuned").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal(", use the ").withStyle(ChatFormatting.AQUA))
            .append(Component.literal("Transmutation Altar").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal(" to apply it.").withStyle(ChatFormatting.AQUA)));
      lore.add(Component.literal("")
            .append(Component.literal("Right Click").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(" to select a ").withStyle(ChatFormatting.AQUA))
            .append(Component.literal("skin").withStyle(ChatFormatting.WHITE))
            .append(Component.literal(".").withStyle(ChatFormatting.AQUA)));
      
      if(itemStack != null){
         ArcanaSkin skin = ArcanaSkin.getSkinFromString(getStringProperty(itemStack, SELECTED_SKIN_TAG));
         lore.add(Component.literal(""));
         if(skin != null){
            lore.add(Component.translatable("text.arcananovum.attuned_item_skin",skin.getName()).withColor(skin.getPrimaryColor()));
         }else{
            lore.add(Component.translatable("text.arcananovum.attuned_item_skin",Component.translatable("text.arcananovum.default")).withStyle(ChatFormatting.WHITE));
         }
      }
      return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   public List<List<Component>> getBookLore(){
      List<List<Component>> list = new ArrayList<>();
      list.add(List.of(Component.literal("Transmogrification\n      Catalyst").withStyle(ChatFormatting.BOLD).withColor(ArcanaColors.STARLIGHT_FORGE_COLOR),Component.literal("\nRarity: ").withStyle(ChatFormatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(),false)),Component.literal("\nMy contributions to Arcana seem to have not gone unnoticed, or unrewarded. To fully capitalize on my new ability to reskin, or transmogrify, my creations, I must make a new form of ").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("Transmogrification\n      Catalyst").withStyle(ChatFormatting.BOLD).withColor(ArcanaColors.STARLIGHT_FORGE_COLOR),Component.literal("\npseudo-augmentation catalyst. It shouldn't need much Arcana, so a simple adaptation of my Catalytic Matrix will suffice.\n\nEach Transmogrification Catalyst can be Used to select one of my ").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("Transmogrification\n      Catalyst").withStyle(ChatFormatting.BOLD).withColor(ArcanaColors.STARLIGHT_FORGE_COLOR),Component.literal("\navailable skins.\n\nTo apply my new aesthetic choice, I must consult my book of transmutations and use a Transmutation Altar to apply the Catalyst as a focus, to my applicable Arcana Item.").withStyle(ChatFormatting.BLACK)));
      return list;
   }
   
   public class TransmogrificationCatalystItem extends ArcanaPolymerItem {
      public TransmogrificationCatalystItem(){
         super(getThis(), getArcanaItemComponents().stacksTo(1));
      }
      
      @Override
      public ItemStack getDefaultInstance(){
         return prefItem;
      }
      
      @Override
      public InteractionResult use(Level level, Player playerEntity, InteractionHand hand){
         ItemStack stack = playerEntity.getItemInHand(hand);
         if(!(playerEntity instanceof ServerPlayer player)) return InteractionResult.SUCCESS_SERVER;
         ArcanaPlayerData data = ArcanaNovum.data(player);
         if(!data.hasAnySkin()) return InteractionResult.PASS;
         List<ArcanaSkin> skins = new ArrayList<>(data.getAllSkins());
         skins.addFirst(null);
         TransmogrificationGui gui = new TransmogrificationGui(player,skins,(skin) -> {
            if(skin != null){
               putProperty(stack,SELECTED_SKIN_TAG,skin.getSerializedName());
            }else{
               removeProperty(stack,SELECTED_SKIN_TAG);
            }
            buildItemLore(stack,player.level().getServer());
         });
         gui.open();
         return InteractionResult.SUCCESS_SERVER;
      }
   }
}
