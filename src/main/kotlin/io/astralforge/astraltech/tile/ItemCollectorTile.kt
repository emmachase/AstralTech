package io.astralforge.astraltech.tile

import io.astralforge.astralitems.AstralItems
import io.astralforge.astralitems.block.tile.*
import io.astralforge.astraltech.AstralTech
import org.bukkit.*
import org.bukkit.Registry.MATERIAL
import org.bukkit.block.BlockFace
import org.bukkit.block.Container
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.inventory.InventoryAction
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

class ItemCollectorTile: AstralTileEntity(), TechInventoryListener, InventoryHolder, ItemTransferHandler {
  private val inventory = Bukkit.createInventory(null, 3*9, "Item Collector").registerWithTech(this)
  private val outputBox = Box( XY(3,0), XY(5,2) )
  private val rangeSelectorBox = Box( XY(7,0), XY(7,2) )
  private val containerItemHandler = MappedInventoryItemHandler(inventory, outputBox.getBox(), this)
  private var wasPowered = false

  private var radius = 1
  private var activationCounter = 0

  companion object : Builder {
    override fun build(): ItemCollectorTile {
      return ItemCollectorTile()
    }
  }

  override fun onLoad(container: PersistentDataContainer) {
    super.onLoad(container)

    // Load inventory
    fillWithBackground(inventory)
    outputBox.getBox().forEach { slot ->
      inventory.clear(slot)
      getItemFromContainer(slot, container).let { inventory.setItem(slot, it) }
    }
    if (container.has(NamespacedKey(AstralTech.instance, "radius"), PersistentDataType.INTEGER)) {
      radius = container.get(NamespacedKey(AstralTech.instance, "radius"), PersistentDataType.INTEGER)!!
    }
    if (container.has(NamespacedKey(AstralTech.instance, "activation_counter"), PersistentDataType.INTEGER)) {
      activationCounter = container.get(NamespacedKey(AstralTech.instance, "activation_counter"), PersistentDataType.INTEGER)!!
    }

  }

  override fun onUnload(container: PersistentDataContainer) {
    super.onUnload(container)
    outputBox.getBox().forEach { slot ->
      writeItemToContainer(inventory.getItem(slot), slot, container)
    }
    container.set(NamespacedKey(AstralTech.instance, "radius"), PersistentDataType.INTEGER, radius)
    container.set(NamespacedKey(AstralTech.instance, "activation_counter"), PersistentDataType.INTEGER, activationCounter)
  }

  override fun tick() {
    super.tick()

    optionSelector(inventory, listOf(7, 16, 25), radius.toString(), listOf("8", "4", "1"), "Pickup Radius")

    if (this.location.block.isBlockIndirectlyPowered && !wasPowered) {
      wasPowered = true
      doCollection()
    } else {
      activationCounter++
      if (!this.location.block.isBlockIndirectlyPowered && wasPowered) {
        wasPowered = false
      }
      if (activationCounter > 20) {
        doCollection()
      }

    }
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

  private fun doCollection() {
    activationCounter = 0
    location.world?.getNearbyEntities(location, radius.toDouble() + 0.5, radius.toDouble() + 0.5, radius.toDouble() + 0.5)
      ?.filterIsInstance<Item>()
      ?.forEach { item ->
        val leftover = containerItemHandler.insertItem(item.itemStack)
        if (isNullOrAir(leftover) || leftover!!.amount <= 0) {
          item.remove()
        } else {
          item.itemStack = leftover
        }
      }
  }

  override fun onInteract(event: PlayerInteractEvent) {
    super.onInteract(event)

    if (event.action == Action.RIGHT_CLICK_BLOCK) {
      if (!event.player.isSneaking) {
        event.isCancelled = true
        event.player.openInventory(inventory)
      } else if (!event.isBlockInHand) {
        event.isCancelled = true
        event.player.openInventory(inventory)
      }
    }
  }

  override fun onInventoryInteract(event: InventoryClickEvent) {
    if (event.clickedInventory == inventory) {
      if ( event.slot !in outputBox) {
        event.isCancelled = true
      }
      if ( event.slot in rangeSelectorBox && event.action == InventoryAction.PICKUP_ALL) {
        if (event.whoClicked is Player) {
          (event.whoClicked as Player).playSound(event.whoClicked.location, Sound.BLOCK_NOTE_BLOCK_HAT, SoundCategory.PLAYERS, 1f, 1f)
        }
        radius = when (event.slot) {
          (7) -> {
            8
          }
          (7 + 1*9) -> {
            4
          }
          else -> {
            1
          }
        }
      }
    }
  }

  override fun onInventoryDrag(event: InventoryDragEvent) {
    for (slot in event.rawSlots) {
      val invSlot = event.view.convertSlot(slot)
      if (event.view.getInventory(slot)?.equals(inventory) == true && invSlot !in outputBox) {
        event.isCancelled = true
      }
    }
  }

  override fun getItemHandler(): ItemHandler {
    return containerItemHandler
  }

  override fun onItemsTransferred() {

  }
}
