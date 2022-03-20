package io.astralforge.astraltech.tile

import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.inventory.Inventory
import java.util.WeakHashMap

object InventoryListener: Listener {
  val knownInventories = WeakHashMap<Inventory, TechInventory>()

  @EventHandler(priority = EventPriority.NORMAL)
  fun onInventoryClickInteract(event: InventoryClickEvent) {
    knownInventories[event.inventory]?.onInventoryInteract(event)
  }

//  @EventHandler(priority = EventPriority.NORMAL)
//  fun onInventoryDragInteract(event: InventoryDragEvent) {
//    knownInventories[event.inventory]?.onInventoryInteract(event)
//  }
}
