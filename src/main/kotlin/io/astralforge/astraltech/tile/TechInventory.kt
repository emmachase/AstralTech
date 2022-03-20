package io.astralforge.astraltech.tile

import org.bukkit.event.inventory.InventoryClickEvent

interface TechInventory {
  fun onInventoryInteract(event: InventoryClickEvent)
}