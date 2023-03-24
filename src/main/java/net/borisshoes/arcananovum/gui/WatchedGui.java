package net.borisshoes.arcananovum.gui;

import eu.pb4.sgui.api.gui.SimpleGui;
import net.borisshoes.arcananovum.cardinalcomponents.MagicBlock;

public interface WatchedGui {
   public MagicBlock getMagicBlock();
   
   public void close();
   
   public SimpleGui getGui();
}
