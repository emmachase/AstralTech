package io.astralforge.astraltech.tile

import io.astralforge.astralitems.recipe.AstralFurnaceRecipeStrategy
import io.astralforge.astralitems.recipe.AstralShapedRecipeStrategy
import io.astralforge.astralitems.recipe.AstralShapelessRecipeStrategy
import io.astralforge.astraltech.crafting.PulverizerStrategy
import org.bukkit.Material

class ElectricFurnaceMachineTile : CraftingMachineTile(
    25000L,
    80L,
    1000L,
    40L,
    craftingBox = Box( XY(2,2), XY(2,2) ),
    24,
    22,
    "Electric Furnace",
    Material.FLINT_AND_STEEL,
    listOf(
        AstralFurnaceRecipeStrategy()
    ),
    true
) {
  companion object : Builder {
    override fun build(): ElectricFurnaceMachineTile {
      return ElectricFurnaceMachineTile()
    }
  }
}
