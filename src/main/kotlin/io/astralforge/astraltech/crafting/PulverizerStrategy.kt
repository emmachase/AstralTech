package io.astralforge.astraltech.crafting

import io.astralforge.astralitems.recipe.AstralRecipeEvaluator
import io.astralforge.astraltech.tile.isNullOrAir
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapelessRecipe

class PulverizerStrategy : AstralRecipeEvaluator.Strategy<PulverizerRecipe> {
  override fun test(craftingMatrix: Array<ItemStack>, recipe: PulverizerRecipe): Boolean {
    if (craftingMatrix.size != 1 || isNullOrAir(craftingMatrix[0])) {
      return false
    }
    return recipe.matches(craftingMatrix[0])
  }

  override fun getRecipeType(): Class<PulverizerRecipe> {
    return PulverizerRecipe::class.java
  }
}