package io.astralforge.astraltech.tile

import io.astralforge.astralitems.AstralItems
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

fun Inventory.registerWithTech(holder: TechInventoryListener): Inventory {
  InventoryListener.knownInventories[this] = holder
  return this
}