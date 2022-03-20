package io.astralforge.astraltech.tile

import io.astralforge.astralitems.recipe.AstralShapedRecipeStrategy
import io.astralforge.astralitems.recipe.AstralShapelessRecipeStrategy
import org.bukkit.Material

class AutoCraftingMachineTile : CraftingMachineTile(
    25000L,
    80L,
    1000L,
    40L,
    craftingBox = Box( XY(1,1), XY(3,3) ),
    25,
    "Auto Crafting Machine",
    Material.IRON_PICKAXE,
    listOf(
        AstralShapedRecipeStrategy(3, 3),
        AstralShapelessRecipeStrategy()
    ),
    true
) {
    companion object : Builder {
        override fun build(): AutoCraftingMachineTile {
            return AutoCraftingMachineTile()
        }
    }
}
