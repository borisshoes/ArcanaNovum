package net.borisshoes.arcananovum.items.catalysts;

import net.borisshoes.arcananovum.Arcananovum;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.bosses.nulconstruct.NulConstructDialog;
import net.borisshoes.arcananovum.cardinalcomponents.MagicEntity;
import net.borisshoes.arcananovum.items.ArcaneTome;
import net.borisshoes.arcananovum.items.WingsOfZephyr;
import net.borisshoes.arcananovum.items.core.MagicItem;
import net.borisshoes.arcananovum.items.core.MagicItems;
import net.borisshoes.arcananovum.items.core.UsableItem;
import net.borisshoes.arcananovum.recipes.GenericMagicIngredient;
import net.borisshoes.arcananovum.recipes.MagicItemIngredient;
import net.borisshoes.arcananovum.recipes.MagicItemRecipe;
import net.borisshoes.arcananovum.utils.GenericTimer;
import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.borisshoes.arcananovum.utils.MagicRarity;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.*;
import net.minecraft.block.pattern.BlockPattern;
import net.minecraft.block.pattern.BlockPatternBuilder;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.predicate.block.BlockStatePredicate;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.function.MaterialPredicate;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TimerTask;

import static net.borisshoes.arcananovum.cardinalcomponents.WorldDataComponentInitializer.MAGIC_ENTITY_LIST;

public class CatalystLegendary extends MagicItem implements UsableItem {
   
   public CatalystLegendary(){
      id = "catalyst_legendary";
      name = "Legendary Augment Catalyst";
      categories = new ArcaneTome.TomeFilter[]{ArcaneTome.TomeFilter.MUNDANE, ArcaneTome.TomeFilter.CATALYSTS};
      rarity = MagicRarity.MUNDANE;
      
      ItemStack item = new ItemStack(Items.GOLD_INGOT);
      NbtCompound tag = item.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList loreList = new NbtList();
      NbtList enchants = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name","[{\"text\":\"Legendary Augment Catalyst\",\"italic\":false,\"color\":\"gold\",\"bold\":true}]");
      loreList.add(NbtString.of("[{\"text\":\"Augment \",\"italic\":false,\"color\":\"dark_aqua\"},{\"text\":\"Catalysts\",\"color\":\"blue\"},{\"text\":\" can be used to \",\"color\":\"gray\"},{\"text\":\"augment \"},{\"text\":\"your \",\"color\":\"gray\"},{\"text\":\"Magic Items\",\"color\":\"dark_purple\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Augments \",\"italic\":false,\"color\":\"dark_aqua\"},{\"text\":\"require more \",\"color\":\"gray\"},{\"text\":\"powerful \",\"color\":\"green\"},{\"text\":\"Catalysts \",\"color\":\"blue\"},{\"text\":\"at higher levels\",\"color\":\"gray\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Apply \",\"italic\":false,\"color\":\"green\"},{\"text\":\"these \",\"color\":\"gray\"},{\"text\":\"Catalysts \",\"color\":\"blue\"},{\"text\":\"in the \",\"color\":\"gray\"},{\"text\":\"Tinkering Menu\",\"color\":\"blue\"},{\"text\":\" of your \",\"color\":\"gray\"},{\"text\":\"Tome\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Mundane \",\"italic\":false,\"color\":\"gray\",\"bold\":true},{\"text\":\"Magic Item\",\"italic\":false,\"color\":\"dark_purple\",\"bold\":false}]"));
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
      return false;
   }
   
   // Summon Nul Construct Mini Boss
   @Override
   public boolean useItem(PlayerEntity playerEntity, World world, Hand hand, BlockHitResult result){
      BlockPos pos = result.getBlockPos();
      BlockState state = world.getBlockState(pos);
      if(state.isOf(Blocks.NETHERITE_BLOCK) && pos.getY() >= world.getBottomY()){ // Check construct
         BlockPattern.Result patternResult = getWitherBossPattern().searchAround(world, pos);
         if (patternResult != null) {
            WitherEntity witherEntity = (WitherEntity) EntityType.WITHER.create(world);
            if (witherEntity != null && world instanceof ServerWorld serverWorld) {
               CarvedPumpkinBlock.breakPatternBlocks(world, patternResult);
               BlockPos blockPos = patternResult.translate(1, 1, 0).getBlockPos();
               witherEntity.refreshPositionAndAngles((double)blockPos.getX() + 0.5, (double)blockPos.getY() + 0.55, (double)blockPos.getZ() + 0.5, patternResult.getForwards().getAxis() == Direction.Axis.X ? 0.0F : 90.0F, 0.0F);
               witherEntity.bodyYaw = patternResult.getForwards().getAxis() == Direction.Axis.X ? 0.0F : 90.0F;
               witherEntity.onSummoned();
               
               MutableText witherName = Text.literal("")
                     .append(Text.literal("-").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("=").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("-").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal(" "))
                     .append(Text.literal("Nul Construct").formatted(Formatting.DARK_GRAY, Formatting.BOLD, Formatting.UNDERLINE))
                     .append(Text.literal(" "))
                     .append(Text.literal("-").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("=").formatted(Formatting.DARK_GRAY))
                     .append(Text.literal("-").formatted(Formatting.DARK_GRAY));
               witherEntity.setCustomName(witherName);
               witherEntity.setCustomNameVisible(true);
               witherEntity.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(2500f);
               witherEntity.setHealth(2500f);
               witherEntity.setPersistent();
               witherEntity.getAttributeInstance(EntityAttributes.GENERIC_FLYING_SPEED).setBaseValue(0.85f);
               witherEntity.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).setBaseValue(0.85f);
               witherEntity.getAttributeInstance(EntityAttributes.GENERIC_ARMOR).setBaseValue(10.0f);
               witherEntity.getAttributeInstance(EntityAttributes.GENERIC_ARMOR_TOUGHNESS).setBaseValue(4.0f);
               witherEntity.getAttributeInstance(EntityAttributes.GENERIC_FOLLOW_RANGE).setBaseValue(100.0f);
               //Iterator var8 = world.getNonSpectatingEntities(ServerPlayerEntity.class, witherEntity.getBoundingBox().expand(50.0)).iterator();
   
               boolean hasMythical = false;
               boolean hasWings = false;
               PlayerInventory inv = playerEntity.getInventory();
               for(int i = 0; i < inv.size(); i++){
                  ItemStack stack = inv.getStack(i);
                  MagicItem magicItem = MagicItemUtils.identifyItem(stack);
                  if(magicItem == null) continue;
                  if(magicItem.getRarity() == MagicRarity.MYTHICAL) hasMythical = true;
                  if(magicItem instanceof WingsOfZephyr) hasWings = true;
               }
               
               NbtCompound witherData = new NbtCompound();
               witherData.putString("id","nul_construct");
               witherData.putBoolean("dead",false);
               witherData.putString("summonerId", playerEntity.getUuidAsString());
               witherData.putString("summonerName", playerEntity.getEntityName());
               witherData.putBoolean("summonerHasMythical", hasMythical);
               witherData.putBoolean("summonerHasWings", hasWings);
               witherData.putFloat("prevHP", witherEntity.getHealth());
               witherData.putInt("castCD",220);
               NbtCompound abilities = new NbtCompound();
               // Necrotic Shroud and Reflexive Blast activate automatically
               // Summon Goons, Curse of Decay, Reflective Armor, Withering Ray and Dark Conversion all have favor weights
               abilities.put("necrotic_shroud",makeAbilityNbt());
               abilities.put("reflexive_blast",makeAbilityNbt());
               abilities.put("summon_goons",makeAbilityNbt());
               abilities.put("curse_of_decay",makeAbilityNbt());
               abilities.put("reflective_armor",makeAbilityNbt());
               abilities.put("withering_ray",makeAbilityNbt());
               abilities.put("dark_conversion",makeAbilityNbt());
               witherData.put("abilities",abilities);
               witherData.put("activeAbilities",new NbtCompound());
               
               MagicEntity magicEntity = new MagicEntity(witherEntity.getUuidAsString(),witherData);
               MAGIC_ENTITY_LIST.get(serverWorld).addEntity(magicEntity);
               
               NulConstructDialog.announce(playerEntity.getServer(),playerEntity,witherEntity, NulConstructDialog.Announcements.SUMMON_TEXT);
               boolean finalHasMythical = hasMythical;
               boolean finalHasWings = hasWings;
               Arcananovum.addTickTimerCallback(serverWorld, new GenericTimer(witherEntity.getInvulnerableTimer(), new TimerTask() {
                  @Override
                  public void run(){
                     NulConstructDialog.announce(playerEntity.getServer(),playerEntity,witherEntity, NulConstructDialog.Announcements.SUMMON_DIALOG, new boolean[]{finalHasMythical,finalHasWings});
                  }
               }));
               
         
               world.spawnEntity(witherEntity);
               CarvedPumpkinBlock.updatePatternBlocks(world, patternResult);
   
               if(playerEntity instanceof ServerPlayerEntity player){
                  ArcanaAchievements.grant(player,ArcanaAchievements.DOOR_OF_DIVINITY.id);
               }
               
               playerEntity.getStackInHand(hand).setCount(0);
            }
      
         }
      }
      return false;
   }
   
   private NbtCompound makeAbilityNbt(){
      NbtCompound compound = new NbtCompound();
      compound.putInt("cooldown",0);
      compound.putInt("weight",(int)(Math.random()*10+1));
      return compound;
   }
   
   private static BlockPattern getWitherBossPattern() {
      return BlockPatternBuilder.start().aisle("^^^", "#@#", "~#~")
            .where('#', (pos) -> pos.getBlockState().isIn(BlockTags.WITHER_SUMMON_BASE_BLOCKS))
            .where('^', CachedBlockPosition.matchesBlockState(BlockStatePredicate.forBlock(Blocks.WITHER_SKELETON_SKULL).or(BlockStatePredicate.forBlock(Blocks.WITHER_SKELETON_WALL_SKULL))))
            .where('~', CachedBlockPosition.matchesBlockState(MaterialPredicate.create(Material.AIR)))
            .where('@', CachedBlockPosition.matchesBlockState(BlockStatePredicate.forBlock(Blocks.NETHERITE_BLOCK)))
            .build();
   }
   
   private MagicItemRecipe makeRecipe(){
      MagicItemIngredient a = new MagicItemIngredient(Items.CRYING_OBSIDIAN,64,null);
      MagicItemIngredient b = new MagicItemIngredient(Items.OBSIDIAN,64,null);
      MagicItemIngredient g = new MagicItemIngredient(Items.GOLD_INGOT,64,null);
      MagicItemIngredient h = new MagicItemIngredient(Items.NETHER_STAR,2,null);
      GenericMagicIngredient m = new GenericMagicIngredient(MagicItems.CATALYST_EXOTIC,1);
   
      MagicItemIngredient[][] ingredients = {
            {a,b,a,b,a},
            {b,g,h,g,b},
            {a,h,m,h,a},
            {b,g,h,g,b},
            {a,b,a,b,a}};
      return new MagicItemRecipe(ingredients);
   
   }
   
   private List<String> makeLore(){
      ArrayList<String> list = new ArrayList<>();
      list.add("{\"text\":\"  Legendary Augment\\n         Catalyst\\n\\nRarity: Mundane\\n\\nGOLD! The gemstones already provide enough reinforcement. Gold lets the energy be more malleable to more creative applications. But, I think there's a little potential left here...\"}");
      return list;
   }
}
