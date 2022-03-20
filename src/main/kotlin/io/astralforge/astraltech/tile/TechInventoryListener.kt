package io.astralforge.astraltech.tile

import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryDragEvent

interface TechInventoryListener {
  fun onInventoryInteract(event: InventoryClickEvent)
  fun onInventoryDrag(event: InventoryDragEvent)
}