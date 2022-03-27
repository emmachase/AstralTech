package io.astralforge.astraltech.tile

import io.astralforge.astralitems.block.tile.InventoryHolder
import io.astralforge.astralitems.block.tile.ItemHandler
import io.astralforge.astralitems.block.tile.ItemTransferHandler
import io.astralforge.astralitems.block.tile.SidedInventory
import io.astralforge.astraltech.network.NetworkEnergyProvider
import io.astralforge.astraltech.network.NetworkNodeTile
import org.bukkit.Bukkit
import org.bukkit.event.block.Action
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

class CreativeGeneratorTile: BufferedMachineTile(maxBuffer=50000, maxChargeRate=0), TechInventoryListener, NetworkEnergyProvider {
  private val inventory = Bukkit.createInventory(null, 3*9, "Creative Generator").registerWithTech(this)

  companion object : Builder {
    override fun build(): CreativeGeneratorTile {
      return CreativeGeneratorTile()
    }
  }

  override fun onLoad(container: PersistentDataContainer) {
    super.onLoad(container)

    // Load inventory
    fillWithBackground(inventory)

  }

  override fun tick() {
    super.tick()

    buffer += minOf(500, maxBuffer - buffer)
    network?.providePower(this, this,500)
    paneLoadingBar(inventory, (10..16).toList(), buffer, maxBuffer, "Energy Buffer")
  }

  override fun onInteract(event: PlayerInteractEvent) {
    super.onInteract(event)

    if (event.action == Action.RIGHT_CLICK_BLOCK) {
      if (!event.player.isSneaking) {
        event.isCancelled = true
        event.player.openInventory(inventory)
      }
    }
  }

  override fun onInventoryInteract(event: InventoryClickEvent) {
    if (event.clickedInventory == inventory) {
      event.isCancelled = true
    }
  }

  override fun onInventoryDrag(event: InventoryDragEvent) {
    for (slot in event.rawSlots) {
      if (event.view.getInventory(slot)?.equals(inventory) == true) {
        event.isCancelled = true
      }
    }
  }

  override fun onOfferedPowerResults(amountRemaining: Long) {
    return
  }
}
