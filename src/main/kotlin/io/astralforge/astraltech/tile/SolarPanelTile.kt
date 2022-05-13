package io.astralforge.astraltech.tile

import io.astralforge.astralitems.AstralItems
import io.astralforge.astralitems.block.tile.InventoryHolder
import io.astralforge.astralitems.block.tile.ItemHandler
import io.astralforge.astralitems.block.tile.ItemTransferHandler
import io.astralforge.astralitems.block.tile.SidedInventory
import io.astralforge.astraltech.AstralTech
import io.astralforge.astraltech.network.NetworkEnergyProvider
import io.astralforge.astraltech.network.NetworkNodeTile
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.data.type.DaylightDetector
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import org.bukkit.scheduler.BukkitRunnable

class SolarPanelTile: BufferedMachineTile(maxBuffer=50000, maxChargeRate=0), TechInventoryListener, NetworkEnergyProvider {
  private val inventory = Bukkit.createInventory(null, 4*9, "Solar Panel").registerWithTech(this)
  private val indicatorBox = Box( XY(3,0), XY(5, 2) )
  private val chargeRate = 5L
  private val outputRate = 20L

  companion object : Builder {
    override fun build(): SolarPanelTile {
      return SolarPanelTile()
    }
  }

  override fun onLoad(container: PersistentDataContainer) {
    super.onLoad(container)

    // Load inventory
    fillWithBackground(inventory)

  }

  override fun tick() {
    super.tick()
    val time = location.world?.time ?: 0
    if (location.block.lightFromSky.toInt() == 15 && time >= 1000 && time < 12000 && (location.world?.isClearWeather == true || location.block.temperature > 0.95)) {
      buffer += minOf(chargeRate, maxBuffer - buffer)
      indicatorBox.getBox().forEach {
        inventory.setItem(it, getNamedItem(ItemStack(Material.YELLOW_STAINED_GLASS_PANE), "Receiving Sunlight"))
      }
    } else {
      indicatorBox.getBox().forEach {
        inventory.setItem(it, getNamedItem(ItemStack(Material.GRAY_STAINED_GLASS_PANE), "Not Receiving Sunlight"))
      }
    }
    val offer = minOf(buffer, outputRate)
    buffer -= offer

    network?.providePower(this, this, offer)
    paneLoadingBar(inventory, (28..34).toList(), buffer, maxBuffer, "Energy Buffer")
  }

  override fun onPlace(player: Player) {
    // TODO: Add functionality for this kind of block replacement automatically!!!!
    if (location.block.blockData is DaylightDetector) {
      val daylightDetector = location.block.blockData as DaylightDetector
      daylightDetector.isInverted = true
      location.block.blockData = daylightDetector
    }
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
    buffer += amountRemaining
  }
}
