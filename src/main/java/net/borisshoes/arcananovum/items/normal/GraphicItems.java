package net.borisshoes.arcananovum.items.normal;

public enum GraphicItems{
   CONFIRM("confirm"),
   CANCEL("cancel"),
   LEFT_ARROW("left_arrow"),
   RIGHT_ARROW("right_arrow"),
   SORT("sort"),
   FILTER("filter"),
   BLACK("black"),
   MENU_HORIZONTAL("menu_horizontal"),
   MENU_VERTICAL("menu_vertical"),
   MENU_TOP("menu_top"),
   MENU_BOTTOM("menu_bottom"),
   MENU_LEFT("menu_left"),
   MENU_RIGHT("menu_right"),
   MENU_TOP_RIGHT("menu_top_right"),
   MENU_TOP_LEFT("menu_top_left"),
   MENU_BOTTOM_LEFT("menu_bottom_left"),
   MENU_BOTTOM_RIGHT("menu_bottom_right"),
   MENU_RIGHT_CONNECTOR("menu_right_connector"),
   MENU_LEFT_CONNECTOR("menu_left_connector"),
   MENU_TOP_CONNECTOR("menu_top_connector"),
   MENU_BOTTOM_CONNECTOR("menu_bottom_connector"),
   TRANSMUTATION_BOOK("transmutation_book"),
   PAGE_BG("page_bg"),
   CASINO_CHIP("casino_chip")
   ;
   
   public final String id;
   
   GraphicItems(String id){
      this.id = id;
   }
}
