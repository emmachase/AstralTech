package io.astralforge.astraltech.tile

import io.astralforge.astralitems.recipe.AstralShapedRecipeStrategy
import io.astralforge.astralitems.recipe.AstralShapelessRecipeStrategy
import io.astralforge.astraltech.crafting.PulverizerStrategy
import org.bukkit.Material

class PulverizerMachineTile : CraftingMachineTile(
    25000L,
    80L,
    1000L,
    40L,
    craftingBox = Box( XY(2,2), XY(2,2) ),
    24,
    22,
    "Pulverizer",
    Material.STONE_PICKAXE,
    listOf(
        PulverizerStrategy()
    ),
    false
) {
  companion object : Builder {
    override fun build(): PulverizerMachineTile {
      return PulverizerMachineTile()
    }
  }
}
