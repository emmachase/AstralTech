package io.astralforge.astraltech.crafting

import io.astralforge.astralitems.block.tile.AstralTileEntity
import io.astralforge.astralitems.recipe.AstralRecipeChoice
import io.astralforge.astraltech.tile.PulverizerMachineTile
import org.bukkit.Keyed
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.Recipe

class PulverizerRecipe constructor(
    private val key: NamespacedKey,
    private val input: AstralRecipeChoice,
    private val output: ItemStack
): Recipe, Keyed {

  override fun getResult(): ItemStack {
    return output
  }

  fun matches(input: ItemStack): Boolean {
    return this.input.test(input)
  }

  override fun getKey(): NamespacedKey {
    return key
  }
}