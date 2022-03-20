package io.astralforge.astraltech.tile

import io.astralforge.astraltech.AstralTech
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Particle
import org.bukkit.event.block.Action
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

class TestMachineTile: BufferedMachineTile(maxBuffer=25000L, maxChargeRate=400L), TechInventoryListener {
  private val energyPerOperation = 10000L
  private val energyUseRate = 40L
  private val energyUsedKey = NamespacedKey(AstralTech.instance, "energy_used")
  private var energyUsed = 0L
  private val inventory = Bukkit.createInventory(null, 6*9, "Test Machine Tile").registerWithTech(this)
  private val craftingBox = Box(XY(1, 1), XY(3, 3))

  companion object : Builder {
    override fun build(): TestMachineTile {
      return TestMachineTile()
    }
  }

  override fun onLoad(container: PersistentDataContainer) {
    super.onLoad(container)

    energyUsed = container.get(energyUsedKey, PersistentDataType.LONG)?: 0L

    // Load inventory
    fillWithBackground(inventory)
    craftingBox.getBox().forEach { slot ->
      inventory.clear(slot)
      getItemFromContainer(slot, container).let { inventory.setItem(slot, it) }
    }

  }

  override fun onUnload(container: PersistentDataContainer) {
    super.onUnload(container)

    container.set(energyUsedKey, PersistentDataType.LONG, energyUsed);
    craftingBox.getBox().forEach { slot ->
      inventory.getItem(slot)?.let { writeItemToContainer(it, slot, container) }
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

  override fun onInventoryDrag(event: InventoryDragEvent) {
    event.isCancelled = true
  }

  override fun receivePower(rf: Long): Long {
    val received = super.receivePower(rf)

    return received
  }

  override fun tick() {
    super.tick()

    val energyToConsume = minOf(energyUseRate, buffer, energyPerOperation - energyUsed)
    energyUsed += energyToConsume
    buffer -= energyToConsume

    if (energyUsed >= energyPerOperation) {
      location.world?.spawnParticle(
          Particle.HEART,
          location,
          1
      )
      energyUsed = 0L
    }

    paneLoadingBar(inventory, (46..52).toList(), buffer, maxBuffer, "Energy Buffer")
    durabilityLoadingBar(inventory, 23, Material.IRON_PICKAXE, energyUsed, energyPerOperation, "Progress")
    //durabilityLoadingBar(inventory, 40, Material.IRON_HOE, energyUsed, energyPerOperation, "Progress")
    //paneLoadingBar(inventory, listOf(39, 29, 20, 11, 3, 4, 5, 15, 24, 33, 41), buffer, maxBuffer, "Energy Buffer")
  }

  override fun onInventoryInteract(event: InventoryClickEvent) {
    if (event.clickedInventory == inventory) {
      if (event.slot !in craftingBox) {
        event.isCancelled = true
      }
    }
  }

}