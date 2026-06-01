package net.borisshoes.arcananovum.skins;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.Tuple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum ArcanaSkin implements StringRepresentable {
   
   VESTIGE_WINGS(ArcanaRegistry.WINGS_OF_ENDERIA, 0x7a04c9, 0x512E93, ArcanaRegistry.arcanaId("vestige_wings")),
   COLEOPTERA_WINGS(ArcanaRegistry.WINGS_OF_ENDERIA, 0x402cbf, 0x3E3779, ArcanaRegistry.arcanaId("coleoptera_wings")),
   FEATHER_WINGS(ArcanaRegistry.WINGS_OF_ENDERIA, 0x4FD6FF, 0x33988f, ArcanaRegistry.arcanaId("feather_wings")),
   LUNAR_BOW(ArcanaRegistry.RUNIC_BOW, 0x074bde, 0x152cd6, ArcanaRegistry.arcanaId("lunar_bow")),
   LUNAR_QUIVER(ArcanaRegistry.RUNIC_QUIVER, 0x074bde, 0x152cd6, ArcanaRegistry.arcanaId("lunar_quiver")),
   RESPLENDENT_HARNESS(ArcanaRegistry.LEVITATION_HARNESS, 0x7f31ff, 0x4747FF, ArcanaRegistry.arcanaId("resplendent_harness"), new Tuple[]{new Tuple<>(Component.translatable("credits_and_attribution.arcananovum.model_by"), Component.literal("ii_iridescent"))}),
   CATGIRL_MEMENTO(ArcanaRegistry.NUL_MEMENTO, 0xFF55FF, 0x8B2E8B, ArcanaRegistry.arcanaId("catgirl_memento")),
   ZEPHOS_LANCE(ArcanaRegistry.SPEAR_OF_TENBROUS, 0x18ceff, 0x0015e3, ArcanaRegistry.arcanaId("zephos_lance")),
   AEQUALIS_RIGHTS(ArcanaRegistry.AEQUALIS_SCIENTIA, 0xFF87C7, 0xBFEEFF, ArcanaRegistry.arcanaId("aequalis_rights")),
   AEQUALIS_RIGHTS_ACE(ArcanaRegistry.AEQUALIS_SCIENTIA, 0xBCB2C5, 0x946CFF, ArcanaRegistry.arcanaId("aequalis_rights_ace")),
   AEQUALIS_RIGHTS_ARO(ArcanaRegistry.AEQUALIS_SCIENTIA, 0xBCB2C5, 0x27AD43, ArcanaRegistry.arcanaId("aequalis_rights_aro")),
   AEQUALIS_RIGHTS_AROACE(ArcanaRegistry.AEQUALIS_SCIENTIA, 0xFFC55A, 0x5EC1FF, ArcanaRegistry.arcanaId("aequalis_rights_aroace")),
   AEQUALIS_RIGHTS_BI(ArcanaRegistry.AEQUALIS_SCIENTIA, 0xFF41D8, 0x152cd6, ArcanaRegistry.arcanaId("aequalis_rights_bi")),
   AEQUALIS_RIGHTS_ENBY(ArcanaRegistry.AEQUALIS_SCIENTIA, 0xFFF318, 0x7E33BF, ArcanaRegistry.arcanaId("aequalis_rights_enby")),
   AEQUALIS_RIGHTS_FLUID(ArcanaRegistry.AEQUALIS_SCIENTIA, 0xFF49F1, 0x5F37FF, ArcanaRegistry.arcanaId("aequalis_rights_fluid")),
   AEQUALIS_RIGHTS_GAY(ArcanaRegistry.AEQUALIS_SCIENTIA, 0x53FF9B, 0x946CFF, ArcanaRegistry.arcanaId("aequalis_rights_gay")),
   AEQUALIS_RIGHTS_INTERSEX(ArcanaRegistry.AEQUALIS_SCIENTIA, 0xFFCF16, 0x7E33BF, ArcanaRegistry.arcanaId("aequalis_rights_intersex")),
   AEQUALIS_RIGHTS_LESBIAN(ArcanaRegistry.AEQUALIS_SCIENTIA, 0xff5c9c, 0xFF9A30, ArcanaRegistry.arcanaId("aequalis_rights_lesbian")),
   AEQUALIS_RIGHTS_PAN(ArcanaRegistry.AEQUALIS_SCIENTIA, 0xff5c9c, 0x3C67FF, ArcanaRegistry.arcanaId("aequalis_rights_pan")),
   AEQUALIS_RIGHTS_PRIDE(ArcanaRegistry.AEQUALIS_SCIENTIA, 0xff5c9c, 0x33F3FF, ArcanaRegistry.arcanaId("aequalis_rights_pride")),
   AEQUALIS_RIGHTS_TRANS(ArcanaRegistry.AEQUALIS_SCIENTIA, 0xFF87C7, 0xBFEEFF, ArcanaRegistry.arcanaId("aequalis_rights_trans"));
   
   private final ArcanaItem arcanaItem;
   private final Identifier id;
   private final int primaryColor;
   private final int secondaryColor;
   private final Tuple<MutableComponent, MutableComponent>[] attributions;
   
   ArcanaSkin(ArcanaItem arcanaItem, int primaryColor, int secondaryColor, Identifier id){
      this.arcanaItem = arcanaItem;
      this.id = id;
      this.primaryColor = primaryColor;
      this.secondaryColor = secondaryColor;
      this.attributions = new Tuple[0];
   }
   
   ArcanaSkin(ArcanaItem arcanaItem, int primaryColor, int secondaryColor, Identifier id, Tuple<MutableComponent, MutableComponent>[] attributions){
      this.arcanaItem = arcanaItem;
      this.id = id;
      this.primaryColor = primaryColor;
      this.secondaryColor = secondaryColor;
      this.attributions = attributions;
   }
   
   public ArcanaItem getArcanaItem(){
      return arcanaItem;
   }
   
   public Identifier getId(){
      return id;
   }
   
   public Identifier getModelId(){
      return ArcanaRegistry.arcanaId("skins/" + id.getPath());
   }
   
   public int getPrimaryColor(){
      return primaryColor;
   }
   
   public int getSecondaryColor(){
      return secondaryColor;
   }
   
   public String getNameTranslationKey(){
      return "skin." + id.getNamespace() + "." + id.getPath() + ".name";
   }
   
   public String getDescriptionTranslationKey(){
      return "skin." + id.getNamespace() + "." + id.getPath() + ".description";
   }
   
   public MutableComponent getName(){
      return Component.translatable(getNameTranslationKey());
   }
   
   public List<MutableComponent> getDescription(){
      String fullText = Component.translatable(getDescriptionTranslationKey()).getString();
      String[] lines = fullText.split("\n");
      List<MutableComponent> components = new ArrayList<>();
      for(String line : lines){
         components.add(Component.literal(line));
      }
      return components;
   }
   
   public Tuple<MutableComponent, MutableComponent>[] getAttributions(){
      return attributions;
   }
   
   public static List<ArcanaSkin> getAllSkinsForItem(ArcanaItem item){
      return Arrays.stream(values()).filter(skin -> skin.arcanaItem.getId().equals(item.getId())).toList();
   }
   
   public static ArcanaSkin getSkinFromString(String str){
      for(ArcanaSkin value : values()){
         if(value.id.equals(Identifier.parse(str))) return value;
      }
      return null;
   }
   
   @Override
   public String getSerializedName(){
      return id.toString();
   }
}
