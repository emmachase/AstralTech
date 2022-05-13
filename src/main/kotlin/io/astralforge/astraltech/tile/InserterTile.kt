package io.astralforge.astraltech.tile

import io.astralforge.astralitems.AstralItems
import io.astralforge.astralitems.block.tile.AstralTileEntity
import io.astralforge.astralitems.block.tile.InventoryHolder
import io.astralforge.astralitems.block.tile.ItemHandler
import io.astralforge.astralitems.block.tile.SidedInventory
import io.astralforge.astraltech.AstralTech
import io.astralforge.astraltech.itemnetwork.ItemNetworkNodeTile
import io.astralforge.astraltech.itemnetwork.ItemOffer
import org.bukkit.*
import org.bukkit.Registry.MATERIAL
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.Container
import org.bukkit.block.data.Directional
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.inventory.InventoryAction
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import java.util.*

class InserterTile: ItemNetworkNodeTile(), TechInventoryListener, Filter {
  private val inventory = Bukkit.createInventory(null, 3*9, "Inserter").registerWithTech(this)
  private val filterBox = Box( XY(3,0), XY(5,2) )
  private val matchToggleSlot = 9 + 1

  private var matchNBT = false
  private var priority = 5

  companion object : Builder {
    override fun build(): InserterTile {
      return InserterTile()
    }
  }


  override fun onLoad(container: PersistentDataContainer) {
    super.onLoad(container)

    fillWithBackground(inventory)
    filterBox.getBox().forEach { slot ->
      inventory.clear(slot)
      getItemFromContainer(slot, container).let { inventory.setItem(slot, it) }
    }
    if (container.has(NamespacedKey(AstralTech.instance, "match_nbt"), PersistentDataType.INTEGER)) {
      matchNBT = container.get(NamespacedKey(AstralTech.instance, "match_nbt"), PersistentDataType.INTEGER)!! != 0
    }
    if (container.has(NamespacedKey(AstralTech.instance, "priority"), PersistentDataType.INTEGER)) {
      priority = container.get(NamespacedKey(AstralTech.instance, "priority"), PersistentDataType.INTEGER)!!
    }
    inventory.setItem(7, getNamedItem(ItemStack(Material.LIME_WOOL), "Priority: +1"))
    inventory.setItem(9*2 + 7, getNamedItem(ItemStack(Material.RED_WOOL), "Priority: -1"))
  }

  override fun onUnload(container: PersistentDataContainer) {
    super.onUnload(container)

    filterBox.getBox().forEach { slot ->
      writeItemToContainer(inventory.getItem(slot), slot, container)
    }
    if (matchNBT) {
      container.set(NamespacedKey(AstralTech.instance, "match_nbt"), PersistentDataType.INTEGER, 1)
    } else {
      container.set(NamespacedKey(AstralTech.instance, "match_nbt"), PersistentDataType.INTEGER, 0)
    }
    container.set(NamespacedKey(AstralTech.instance, "priority"), PersistentDataType.INTEGER, priority)
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
      if (event.slot !in filterBox) {
        event.isCancelled = true
      }
      if (event.slot == matchToggleSlot && event.action == InventoryAction.PICKUP_ALL) {
        if (event.whoClicked is Player) {
          (event.whoClicked as Player).playSound(event.whoClicked.location, Sound.BLOCK_NOTE_BLOCK_HAT, SoundCategory.PLAYERS, 1f, 1f)
        }
        matchNBT = !matchNBT
      }
      if (event.slot == 7 && event.action == InventoryAction.PICKUP_ALL) {
        if (event.whoClicked is Player) {
          if (priority < 10) {
            (event.whoClicked as Player).playSound(event.whoClicked.location, Sound.BLOCK_NOTE_BLOCK_HAT, SoundCategory.PLAYERS, 1f, 1f)
          } else {
            (event.whoClicked as Player).playSound(event.whoClicked.location, Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE, SoundCategory.PLAYERS, 1f, 1f)
          }
        }
        priority = minOf(10, priority + 1)
      } else if (event.slot == 9*2 + 7 && event.action == InventoryAction.PICKUP_ALL) {
        if (event.whoClicked is Player) {
          if (priority > 0) {
            (event.whoClicked as Player).playSound(event.whoClicked.location, Sound.BLOCK_NOTE_BLOCK_HAT, SoundCategory.PLAYERS, 1f, 1f)
          } else {
            (event.whoClicked as Player).playSound(event.whoClicked.location, Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE, SoundCategory.PLAYERS, 1f, 1f)
          }
        }
        priority = maxOf(0, priority - 1)
      }
    }
  }

  override fun onInventoryDrag(event: InventoryDragEvent) {
    for (slot in event.rawSlots) {
      val invSlot = event.view.convertSlot(slot)
      if (event.view.getInventory(slot)?.equals(inventory) == true && invSlot !in filterBox) {
        event.isCancelled = true
      }
    }
  }

  override fun tick() {
    super.tick()
    displayToggle(inventory, listOf(matchToggleSlot), matchNBT, "Match NBT: On", "Match NBT: Off")
    inventory.setItem(9*1 + 7, getNamedItem(ItemStack(Material.GRAY_STAINED_GLASS_PANE), "Priority: $priority"))

    val blockData = this.location.block.blockData
    if (blockData is Directional) {
      val targetBlock = location.clone().add(blockData.facing.direction).block
      //val offers = getAvailableItems(targetBlock, blockData.facing.oppositeFace)
      itemNetwork?.requestItems(this, priority)
    } else {
      // Something wacky happened with our block
      itemNetwork?.addOffers(this, emptyList())
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    for (slot in filterBox.getBox()) {
      inventory.getItem(slot)?.let {
        location.world?.dropItem(location, it)
        inventory.setItem(slot, null)
      }
    }
  }

  override fun reviewOffers(offers: MutableMap<Material, MutableList<ItemOffer>>) {
    val blockData = this.location.block.blockData
    if (blockData !is Directional) return

    val filterItems = mutableListOf<ItemStack>()
    val filterMaterials = mutableSetOf<Material>()
    filterBox.getBox().forEach { slot ->
      inventory.getItem(slot)?.let {
        if (!isNullOrAir(it)) {
          filterItems.add(it)
          filterMaterials.add(it.type)
        }
      }
    }

    val block = location.clone().add(blockData.facing.direction).block
    val face = blockData.facing.oppositeFace
    val optTileEntity: Optional<AstralTileEntity> = AstralItems.getInstance().getTileEntity(block)
    if (optTileEntity.isPresent) {
      /* Is an astralTileEntity */
      val astralTileEntity = optTileEntity.get()
      var handler: ItemHandler? = null
      if (astralTileEntity is SidedInventory) {
        handler = (astralTileEntity as SidedInventory).getItemHandler(face)
      } else if (astralTileEntity is InventoryHolder) {
        handler = (astralTileEntity as InventoryHolder).itemHandler
      }
      if (handler == null) return
      inventoryLoop@ for (i in 0 until handler.size) {
        val item = handler.getItem(i)
        if (isNullOrAir(item)) {
          var offerKeys = offers.keys
          if (filterMaterials.size > 0) {
            offerKeys = filterMaterials
          }
          for (offerKey in offerKeys) {
            if (!offers.containsKey(offerKey)) continue
            for (offerSelected in offers[offerKey]!!) {
              if (!matchesFilter(offerSelected.item, filterItems, matchNBT)) continue
              val fulfilledItem = offerSelected.tile.fulfillOffer(offerSelected, offerKey.maxStackSize)
              offers[offerKey]?.remove(offerSelected)
              if (isNullOrAir(fulfilledItem)) continue
              handler.setItem(i, fulfilledItem)
              break@inventoryLoop
            }
          }
        } else {
          if (!matchesFilter(item!!, filterItems, matchNBT)) continue

          val maxToGet = item.type.maxStackSize - item.amount
          if (!offers.containsKey(item.type)) continue
          for (offerSelected in offers[item.type]!!) {
            val fulfilledItem = offerSelected.tile.fulfillOffer(offerSelected, maxToGet)
            if (isNullOrAir(fulfilledItem)) {
              offers[item.type]?.remove(offerSelected)
              continue
            }
            offerSelected.item.amount -= fulfilledItem!!.amount
            if (offerSelected.item.amount <= 0) offers[item.type]?.remove(offerSelected)
            item.amount += fulfilledItem.amount
            handler.setItem(i, item)
            break@inventoryLoop
          }
        }
      }
    } else if (block.state is Container) {
      /* Is a vanilla container */
      val container = block.state as Container
      val inventory = container.inventory
      inventoryLoop@ for (i in 0 until inventory.size) {
        val item = inventory.getItem(i)
        if (isNullOrAir(item)) {
          var offerKeys = offers.keys
          if (filterMaterials.size > 0) {
            offerKeys = filterMaterials
          }
          for (offerKey in offerKeys) {
            if (!offers.containsKey(offerKey)) continue
            val offersToRemove = mutableListOf<ItemOffer>()
            for (offerSelected in offers[offerKey]!!) {
              if (!matchesFilter(offerSelected.item, filterItems, matchNBT)) continue
              val fulfilledItem = offerSelected.tile.fulfillOffer(offerSelected, offerKey.maxStackSize)
              offersToRemove.add(offerSelected)
              if (isNullOrAir(fulfilledItem)) continue
              inventory.setItem(i, fulfilledItem)
              break@inventoryLoop
            }
            offers[offerKey]?.removeAll(offersToRemove)
          }
        } else if (item != null) {
          if (!matchesFilter(item, filterItems, matchNBT)) continue
          val maxToGet = item.type.maxStackSize - item.amount
          if (!offers.containsKey(item.type) || maxToGet <= 0) continue
          val offersToRemove = mutableListOf<ItemOffer>()
          for (offerSelected in offers[item.type]!!) {
            val fulfilledItem = offerSelected.tile.fulfillOffer(offerSelected, maxToGet)
            if (isNullOrAir(fulfilledItem)) {
              offersToRemove.add(offerSelected)
              continue
            }
            offersToRemove.add(offerSelected)
            offerSelected.item.amount -= fulfilledItem!!.amount
            if (offerSelected.item.amount <= 0) offers[item.type]?.remove(offerSelected)
            item.amount += fulfilledItem.amount
            inventory.setItem(i, item)
            break@inventoryLoop
          }
          offers[item.type]?.removeAll(offersToRemove)
        }
      }
    }
  }
}