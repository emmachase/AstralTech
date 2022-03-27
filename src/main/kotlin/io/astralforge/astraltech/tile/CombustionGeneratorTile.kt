package io.astralforge.astraltech.tile

import io.astralforge.astralitems.AstralItemSpec
import io.astralforge.astralitems.AstralItems
import io.astralforge.astralitems.block.tile.*
import io.astralforge.astraltech.AstralTech
import io.astralforge.astraltech.network.NetworkEnergyProvider
import io.astralforge.astraltech.network.NetworkNodeTile
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

class CombustionGeneratorTile: BufferedMachineTile(maxBuffer=50000, maxChargeRate=0, handleRequests = false), TechInventoryListener, InventoryHolder, ItemTransferHandler, NetworkEnergyProvider {
  private val burnTimeKey = NamespacedKey(AstralTech.instance, "burn_time")
  private val burnTimeTotalKey = NamespacedKey(AstralTech.instance, "burn_time_total")
  private val inventory = Bukkit.createInventory(null, 4*9, "Combustion Generator").registerWithTech(this)
  private val inputSlot = 12
  private val progressSlot = 14
  private val energyPerTick = 50L
  private val outputRate = 100L
  private val containerItemHandler = MappedInventoryItemHandler(inventory, listOf(inputSlot), this)
  private var burnTime = 0L
  private var burnTimeTotal = 0L

  companion object : Builder {
    override fun build(): CombustionGeneratorTile {
      return CombustionGeneratorTile()
    }
  }

  override fun onLoad(container: PersistentDataContainer) {
    super.onLoad(container)

    container.get(burnTimeKey, PersistentDataType.LONG)?.let {
      burnTime = it
    }
    container.get(burnTimeTotalKey, PersistentDataType.LONG)?.let {
      burnTimeTotal = it
    }
    // Load inventory
    fillWithBackground(inventory)
    getItemFromContainer(inputSlot, container).let { inventory.setItem(inputSlot, it) }
  }

  override fun onUnload(container: PersistentDataContainer) {
    super.onUnload(container)

    container.set(burnTimeKey, PersistentDataType.LONG, burnTime)
    container.set(burnTimeTotalKey, PersistentDataType.LONG, burnTimeTotal)
    writeItemToContainer(inventory.getItem(inputSlot), inputSlot, container)
  }

  override fun tick() {
    super.tick()

    if (burnTime > 0) {
      burnTime--
      buffer += minOf(energyPerTick, maxBuffer - buffer)
    }
    val offer = minOf(buffer, outputRate)
    buffer -= offer
    network?.providePower(this, this, offer)

    paneLoadingBar(inventory, (28..34).toList(), buffer, maxBuffer, "Energy Buffer")
    durabilityLoadingBar(inventory, progressSlot, Material.FLINT_AND_STEEL, burnTime, burnTimeTotal, "Burn Time")
  }

  private fun attemptFuelConsumption() {
    if (burnTime <= 0 && buffer < maxBuffer) {
      val itemBurnTime = inventory.getItem(inputSlot)?.let { getBurnTime(it) }
      if ((itemBurnTime != null) && (itemBurnTime > 0)) {
        burnTime = itemBurnTime
        burnTimeTotal = burnTime
        inventory.getItem(inputSlot)?.let {
          val item = it.clone()
          item.amount = item.amount - 1
          inventory.setItem(inputSlot, item)
        }
      }
    }
  }

  private fun getBurnTime(item: ItemStack): Long {
    if (AstralItems.getInstance().isAstralItem(item)) {
      val itemSpec = AstralItems.getInstance().getAstralItem(item)
      // TODO: Support for custom combustion fuels
      return 0;
    } else {
      return when (item.type) {
        Material.COAL -> 1600
        Material.CHARCOAL -> 1600
        Material.COAL_BLOCK -> 16000
        else -> {
          0
        }
      }
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
