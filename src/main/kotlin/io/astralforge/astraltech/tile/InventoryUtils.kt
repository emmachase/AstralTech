package io.astralforge.astraltech.tile

import org.bukkit.inventory.Inventory

fun Inventory.registerWithTech(holder: TechInventory): Inventory {
  InventoryListener.knownInventories[this] = holder
  return this
}