package net.borisshoes.arcananovum.recipes.arcana;

/**
 * A requirement an ingredient places on a stack beyond its item type like a Soulstone's soul count.
 *
 * @param type  key, unique per ingredient
 * @param value the typed requirement value (Integer, Boolean or String)
 * @param text  short English description of the requirement
 */
public record IngredientCondition(String type, Object value, String text) {
}
