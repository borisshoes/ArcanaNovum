package net.borisshoes.arcananovum.recipes.transmutation;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.JsonOps;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.blocks.altars.TransmutationAltarBlockEntity;
import net.borisshoes.borislib.BorisLib;
import net.borisshoes.borislib.utils.MinecraftUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static net.borisshoes.arcananovum.ArcanaNovum.log;

public class CommutativeTransmutationRecipe extends TransmutationRecipe{
   
   private final List<Either<Item, TagKey<Item>>> communalInputs = new ArrayList<>();
   private ItemStack viewStack = new ItemStack(Items.BARRIER);
   
   public CommutativeTransmutationRecipe(String id, ItemStack reagent1, ItemStack reagent2){
      super(id,reagent1,reagent2);
   }
   
   private CommutativeTransmutationRecipe(String id, List<Either<Item, TagKey<Item>>> communalInputs, ItemStack viewStack, List<Either<Item, TagKey<Item>>> reagent1, int reagent1Count, List<Either<Item, TagKey<Item>>> reagent2, int reagent2Count){
      super(id, reagent1, reagent1Count, reagent2, reagent2Count);
      this.communalInputs.addAll(communalInputs);
      this.viewStack = viewStack;
   }
   
   public CommutativeTransmutationRecipe with(Item... items){
      for(Item item : items){
         this.communalInputs.add(Either.left(item));
      }
      return this;
   }
   
   public CommutativeTransmutationRecipe with(Either<TagKey<Item>, TagKey<Block>> tag){
      if(tag.left().isPresent()){
         this.communalInputs.add(Either.right(tag.left().get()));
      }else if(tag.right().isPresent()){
         for(Holder<Item> itemEntry : BuiltInRegistries.ITEM.asHolderIdMap()){
            try{
               if(itemEntry.value() instanceof BlockItem blockItem){
                  if(blockItem.getBlock().builtInRegistryHolder().is(tag.right().get())){
                     this.communalInputs.add(Either.left(itemEntry.value()));
                  }
               }
            }catch(Exception e){
               log(2,"Error getting tags for "+itemEntry.getRegisteredName());
            }
         }
      }
      return this;
   }
   
   public CommutativeTransmutationRecipe withViewStack(ItemStack viewStack){
      this.viewStack = viewStack;
      return this;
   }
   
   @Override
   public List<ItemStack> doTransmutation(ItemStack positiveInput, ItemStack negativeInput, ItemStack reagent1, ItemStack reagent2, ItemStack aequalisInput, ServerPlayer player){
      return List.of(negativeInput.copyWithCount(positiveInput.getCount()),negativeInput,aequalisInput);
   }
   
   @Override
   public List<Tuple<ItemStack,String>> doTransmutation(ItemEntity sourceEntity, ItemEntity focusEntity, ItemEntity reagent1Entity, ItemEntity reagent2Entity, ItemEntity aequalisEntity, TransmutationAltarBlockEntity altar, ServerPlayer player){
      int bargainLvl = ArcanaAugments.getAugmentFromMap(altar.getAugments(),ArcanaAugments.HASTY_BARGAIN);
      ItemStack sourceStack = sourceEntity != null ? sourceEntity.getItem() : ItemStack.EMPTY;
      ItemStack focusStack = focusEntity != null ? focusEntity.getItem() : ItemStack.EMPTY;
      ItemStack reagent1Stack = reagent1Entity != null ? reagent1Entity.getItem() : ItemStack.EMPTY;
      ItemStack reagent2Stack = reagent2Entity != null ? reagent2Entity.getItem() : ItemStack.EMPTY;
      if(!canTransmute(sourceStack,focusStack,reagent1Stack,reagent2Stack, ItemStack.EMPTY,altar)) return new ArrayList<>();
      
      ItemStack outputStack = focusStack.copyWithCount(sourceStack.getCount());
      if(sourceEntity != null) sourceEntity.discard();
      boolean m11 = validReagent1(reagent1Stack,bargainLvl), m22 = validReagent2(reagent2Stack,bargainLvl), m12 = validReagent1(reagent2Stack,bargainLvl), m21 = validReagent2(reagent1Stack,bargainLvl);
      boolean straight = m11 && m22;
      boolean cross = !straight && m12 && m21;
      if (!straight && !cross) return new ArrayList<>(); // should be impossible
      
      ItemStack reagent1 = straight ? getComputedReagent1(reagent1Stack,bargainLvl) : getComputedReagent2(reagent1Stack,bargainLvl);
      ItemStack reagent2 = straight ? getComputedReagent2(reagent2Stack,bargainLvl) : getComputedReagent1(reagent2Stack,bargainLvl);
      
      if (reagent1Entity != null) {
         int take = reagent1.isEmpty() ? 0 : reagent1.getCount();
         if(take > 0){
            if(reagent1Stack.getCount() == take){
               reagent1Entity.discard();
            }else{
               reagent1Stack.shrink(take);
               reagent1Entity.setItem(reagent1Stack);
            }
         }
      }
      
      if (reagent2Entity != null) {
         int take = reagent2.isEmpty() ? 0 : reagent2.getCount();
         if(take > 0){
            if(reagent2Stack.getCount() == take){
               reagent2Entity.discard();
            }else{
               reagent2Stack.shrink(take);
               reagent2Entity.setItem(reagent2Stack);
            }
         }
      }
      
      List<Tuple<ItemStack,String>> outputs = new ArrayList<>();
      outputs.add(new Tuple<>(outputStack,"negative"));
      return outputs;
   }
   
   @Override
   public boolean canTransmute(ItemStack sourceInput, ItemStack focusInput, ItemStack reagent1Input, ItemStack reagent2Input, ItemStack aequalisInput, TransmutationAltarBlockEntity altar){
      int bargainLvl = ArcanaAugments.getAugmentFromMap(altar.getAugments(),ArcanaAugments.HASTY_BARGAIN);
      boolean reagentCheck1 = validReagent1(reagent1Input,bargainLvl) && validReagent2(reagent2Input,bargainLvl);
      boolean reagentCheck2 = validReagent1(reagent2Input,bargainLvl) && validReagent2(reagent1Input,bargainLvl);
      if (!(reagentCheck1 || reagentCheck2)) return false;
      if(!validCommunalInput(sourceInput)) return false;
      if(!validCommunalInput(focusInput)) return false;
      if(sourceInput.is(focusInput.getItem())) return false;
      return true;
   }
   
   public boolean validCommunalInput(ItemStack provided){
      return this.communalInputs.stream().anyMatch(e -> (e.left().isPresent() && provided.is(e.left().get())) || (e.right().isPresent() && provided.is(e.right().get())));
   }
   
   public List<ItemStack> getDisplayStacks(){
      Set<Item> items = new HashSet<>();
      for(Item item : BuiltInRegistries.ITEM){
         ItemStack def = new ItemStack(item);
         if(validCommunalInput(def)){
            items.add(item);
         }
      }
      return items.stream().map(ItemStack::new).toList();
   }
   
   
   
   public List<Either<Item, TagKey<Item>>> getCommunalInputs(){
      return new ArrayList<>(this.communalInputs);
   }
   
   @Override
   public ItemStack getViewStack(){
      return viewStack.copy();
   }
   
   public JsonObject toJson(){
      JsonObject json = new JsonObject();
      json.addProperty("type", "arcananovum:commutative_transmutation");
      json.addProperty("id", id);
      
      // Serialize view_stack
      RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, BorisLib.SERVER.registryAccess());
      json.add("view_stack", ItemStack.CODEC.encodeStart(ops, viewStack).getOrThrow());
      
      // Serialize inputs
      JsonArray inputsArray = new JsonArray();
      for(Either<Item, TagKey<Item>> entry : communalInputs){
         if(entry.left().isPresent()){
            inputsArray.add(BuiltInRegistries.ITEM.getKey(entry.left().get()).toString());
         }else if(entry.right().isPresent()){
            inputsArray.add("#" + entry.right().get().location());
         }
      }
      json.add("inputs", inputsArray);
      
      // Serialize reagent 1
      if(!reagent1.isEmpty()){
         JsonArray reagent1Array = new JsonArray();
         for(Either<Item, TagKey<Item>> entry : reagent1){
            if(entry.left().isPresent()){
               reagent1Array.add(BuiltInRegistries.ITEM.getKey(entry.left().get()).toString());
            }else if(entry.right().isPresent()){
               reagent1Array.add("#" + entry.right().get().location());
            }
         }
         json.add("reagent_1", reagent1Array);
         json.addProperty("reagent_1_count", reagent1Count);
      }
      
      // Serialize reagent 2
      if(!reagent2.isEmpty()){
         JsonArray reagent2Array = new JsonArray();
         for(Either<Item, TagKey<Item>> entry : reagent2){
            if(entry.left().isPresent()){
               reagent2Array.add(BuiltInRegistries.ITEM.getKey(entry.left().get()).toString());
            }else if(entry.right().isPresent()){
               reagent2Array.add("#" + entry.right().get().location());
            }
         }
         json.add("reagent_2", reagent2Array);
         json.addProperty("reagent_2_count", reagent2Count);
      }
      
      return json;
   }
   
   public static CommutativeTransmutationRecipe fromJson(JsonObject json){
      if(!json.has("type") || !json.get("type").getAsString().equals("arcananovum:commutative_transmutation")) return null;
      
      String id = json.get("id").getAsString();
      
      // Parse view_stack
      RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, BorisLib.SERVER.registryAccess());
      ItemStack viewStack = ItemStack.CODEC.parse(ops, json.get("view_stack")).getOrThrow();
      
      // Parse inputs
      List<Either<Item, TagKey<Item>>> inputs = new ArrayList<>();
      JsonArray inputsArray = json.getAsJsonArray("inputs");
      for(JsonElement element : inputsArray){
         Either<Item, TagKey<Item>> parsed = MinecraftUtils.parseItemOrTag(element.getAsString());
         if(parsed != null) inputs.add(parsed);
      }
      
      // Parse reagent 1
      List<Either<Item, TagKey<Item>>> reagent1List = new ArrayList<>();
      int reagent1Count = 0;
      if(json.has("reagent_1")){
         JsonArray reagent1Array = json.getAsJsonArray("reagent_1");
         for(JsonElement element : reagent1Array){
            Either<Item, TagKey<Item>> parsed = MinecraftUtils.parseItemOrTag(element.getAsString());
            if(parsed != null) reagent1List.add(parsed);
         }
         reagent1Count = json.has("reagent_1_count") ? json.get("reagent_1_count").getAsInt() : 1;
      }
      
      // Parse reagent 2
      List<Either<Item, TagKey<Item>>> reagent2List = new ArrayList<>();
      int reagent2Count = 0;
      if(json.has("reagent_2")){
         JsonArray reagent2Array = json.getAsJsonArray("reagent_2");
         for(JsonElement element : reagent2Array){
            Either<Item, TagKey<Item>> parsed = MinecraftUtils.parseItemOrTag(element.getAsString());
            if(parsed != null) reagent2List.add(parsed);
         }
         reagent2Count = json.has("reagent_2_count") ? json.get("reagent_2_count").getAsInt() : 1;
      }
      
      return new CommutativeTransmutationRecipe(id, inputs, viewStack, reagent1List, reagent1Count, reagent2List, reagent2Count);
   }
}
