package io.astralforge.astraltech.tile

import io.astralforge.astralitems.AstralItems
import org.bukkit.inventory.ItemStack

interface Filter {
  fun matchesFilter(targetItem: ItemStack, filterItems: List<ItemStack>, matchNBT: Boolean): Boolean {
    if (filterItems.isNotEmpty()) {
      // We have a filter, need to make sure the item matches
      // If we must match NBT, check if any items in filter return true for isSimilar
      if (matchNBT && !filterItems.any { it.isSimilar(targetItem) }) return false
      // If we don't match NBT:
      if (!matchNBT) {
        if (AstralItems.getInstance().isAstralItem(targetItem)) {
          // Container item is an astral item, look for other astral items that match id
          val itemSpec = AstralItems.getInstance().getAstralItem(targetItem)
          if (!filterItems.any {
                if (AstralItems.getInstance().isAstralItem(it)) {
                  val itItemSpec = AstralItems.getInstance().getAstralItem(it)
                  return@any itItemSpec.id == itemSpec.id
                }
                false
              }) return false
        } else {
          // Container item is not an astral item, look for other non-astral items that match type
          if (!filterItems.any { !AstralItems.getInstance().isAstralItem(it) && targetItem.type == it.type }) return false
        }
      }
    }
    return true
  }
}