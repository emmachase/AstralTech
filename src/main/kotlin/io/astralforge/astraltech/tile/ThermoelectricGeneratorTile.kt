package io.astralforge.astraltech.tile

import io.astralforge.astralitems.AstralItems
import io.astralforge.astralitems.block.tile.*
import io.astralforge.astraltech.AstralTech
import io.astralforge.astraltech.network.NetworkEnergyProvider
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.event.block.Action
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

class ThermoelectricGeneratorTile: BufferedMachineTile(maxBuffer=100000, maxChargeRate=0, handleRequests = false), TechInventoryListener, InventoryHolder, ItemTransferHandler, NetworkEnergyProvider {
  private val tankKey = NamespacedKey(AstralTech.instance, "tank")
  private val inventory = Bukkit.createInventory(null, 6*9, "Thermoelectric Generator").registerWithTech(this)
  private val inputSlot = 21
  private val progressSlot = 23
  private val energyPerTick = 100L
  private val outputRate = 500L
  private val containerItemHandler = MappedInventoryItemHandler(inventory, listOf(inputSlot), this)

  private val tankCapacity = 6 * 1000L

  private var tank = 0L

  companion object : Builder {
    override fun build(): ThermoelectricGeneratorTile {
      return ThermoelectricGeneratorTile()
    }
  }

  override fun onLoad(container: PersistentDataContainer) {
    super.onLoad(container)

    container.get(tankKey, PersistentDataType.LONG)?.let {
      tank = it
    }
    // Load inventory
    fillWithBackground(inventory)
    getItemFromContainer(inputSlot, container).let { inventory.setItem(inputSlot, it) }
  }

  override fun onUnload(container: PersistentDataContainer) {
    super.onUnload(container)

    container.set(tankKey, PersistentDataType.LONG, tank)
    writeItemToContainer(inventory.getItem(inputSlot), inputSlot, container)
  }

  override fun tick() {
    super.tick()

    if (tank > 0 && buffer <= maxBuffer) {
      buffer += minOf(energyPerTick, maxBuffer - buffer)
      tank -= 1
    }
    val offer = minOf(buffer, outputRate)
    buffer -= offer
    network?.providePower(this, this, offer)

    paneLoadingBar(inventory, (47..51).toList(), buffer, maxBuffer, "Energy Buffer")
    paneLoadingBar(inventory, listOf(53, 44, 35, 26, 17, 8), tank, tankCapacity, "Fuel Tank","mB",
        Material.ORANGE_STAINED_GLASS_PANE,
        Material.RED_STAINED_GLASS_PANE,
        Material.YELLOW_STAINED_GLASS_PANE,
        Material.GRAY_STAINED_GLASS_PANE)
    durabilityLoadingBar(inventory, progressSlot, Material.FLINT_AND_STEEL, tank, tankCapacity, "Fuel Tank", "mB")
  }

  override fun onDestroy() {
    super.onDestroy()
    for (slot in 0 until containerItemHandler.size) {
      containerItemHandler.getItem(slot)?.let {
        location.world?.dropItem(location, it)
        containerItemHandler.setItem(slot, null)
      }
    }
  }

  private fun attemptFuelConsumption() {
    if (tank > (tankCapacity - 1000)) return
    val fuel = inventory.getItem(inputSlot)
    if (isNullOrAir(fuel)) return
    if (AstralItems.getInstance().isAstralItem(fuel)) return

    if (fuel!!.type == Material.LAVA_BUCKET) {
      tank += 1000
      inventory.setItem(inputSlot, ItemStack(Material.BUCKET))
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
    if (event.clickedInventory == inventory && event.slot != inputSlot) {
      event.isCancelled = true
    }
  }

  override fun onInventoryDrag(event: InventoryDragEvent) {
    for (slot in event.rawSlots) {
      val invSlot = event.view.convertSlot(slot)
      if (event.view.getInventory(slot)?.equals(inventory) == true && invSlot != inputSlot) {
        event.isCancelled = true
      }
    }
  }

  override fun getItemHandler(): ItemHandler {
    return containerItemHandler;
  }

  override fun onItemsTransferred() {
    attemptFuelConsumption()
  }

  override fun onOfferedPowerResults(amountRemaining: Long) {
    buffer += amountRemaining
    attemptFuelConsumption()
  }
}
