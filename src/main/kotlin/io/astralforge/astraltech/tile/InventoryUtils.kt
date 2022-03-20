package io.astralforge.astraltech.tile

import org.bukkit.inventory.Inventory

fun Inventory.registerWithTech(holder: TechInventoryListener): Inventory {
  InventoryListener.knownInventories[this] = holder
  return this
}