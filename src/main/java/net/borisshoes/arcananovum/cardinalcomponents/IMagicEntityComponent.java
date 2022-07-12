package net.borisshoes.arcananovum.cardinalcomponents;

import dev.onyxstudios.cca.api.v3.component.ComponentV3;

import java.util.List;

public interface IMagicEntityComponent extends ComponentV3 {
   List<MagicEntity> getEntities();
   boolean addEntity(MagicEntity entity);
   boolean removeEntity(MagicEntity entity);
}
