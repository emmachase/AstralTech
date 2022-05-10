package io.astralforge.astraltech.tile

import io.astralforge.astralitems.AstralItems
import io.astralforge.astralitems.block.AbstractAstralBlockSpec
import io.astralforge.astraltech.AstralTech
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.Block
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockRedstoneEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.inventory.Inventory
import org.bukkit.scheduler.BukkitRunnable
import java.util.WeakHashMap

object InventoryListener: Listener {
  val knownInventories = WeakHashMap<Inventory, TechInventoryListener>()

  @EventHandler(priority = EventPriority.NORMAL)
  fun onInventoryClickInteract(event: InventoryClickEvent) {
    knownInventories[event.inventory]?.onInventoryInteract(event)
  }

  @EventHandler(priority = EventPriority.NORMAL)
  fun onInventoryDragInteract(event: InventoryDragEvent) {
    knownInventories[event.inventory]?.onInventoryDrag(event)
  }

//  @EventHandler(priority = EventPriority.NORMAL)
//  fun onInventoryDragInteract(event: InventoryDragEvent) {
//    knownInventories[event.inventory]?.onInventoryInteract(event)
//  }
}
