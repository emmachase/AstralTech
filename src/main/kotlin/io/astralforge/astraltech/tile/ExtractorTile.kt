package io.astralforge.astraltech.tile

import io.astralforge.astralitems.AstralItems
import io.astralforge.astralitems.block.tile.AstralTileEntity
import io.astralforge.astralitems.block.tile.InventoryHolder
import io.astralforge.astralitems.block.tile.ItemHandler
import io.astralforge.astralitems.block.tile.SidedInventory
import io.astralforge.astraltech.AstralTech
import io.astralforge.astraltech.itemnetwork.ItemNetworkNodeTile
import io.astralforge.astraltech.itemnetwork.ItemOffer
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.Sound
import org.bukkit.SoundCategory
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

class ExtractorTile: ItemNetworkNodeTile(), TechInventoryListener, Filter {
  private val inventory = Bukkit.createInventory(null, 3*9, "Extractor").registerWithTech(this)
  private val filterBox = Box( XY(3,0), XY(5,2) )
  private val matchToggleSlot = 9 + 1

  private var matchNBT = false

  companion object : Builder {
    override fun build(): ExtractorTile {
      return ExtractorTile()
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
  }

  fun getAvailableItems(block: Block, face: BlockFace): List<ItemOffer> {
    val filterItems = mutableListOf<ItemStack>()
    filterBox.getBox().forEach { slot ->
      inventory.getItem(slot)?.let {
        if (!isNullOrAir(it)) {
          filterItems.add(it)
        }
      }
    }
    val optTileEntity: Optional<AstralTileEntity> = AstralItems.getInstance().getTileEntity(block)
    val offers = mutableListOf<ItemOffer>()
    if (optTileEntity.isPresent) {
      val astralTileEntity = optTileEntity.get()
      //println("Getting offers for astral block $astralTileEntity face $face")
      var handler: ItemHandler? = null
      if (astralTileEntity is SidedInventory) {
        handler = (astralTileEntity as SidedInventory).getItemHandler(face)
      } else if (astralTileEntity is InventoryHolder) {
        handler = (astralTileEntity as InventoryHolder).itemHandler
      }
      if (handler == null) return offers
      for (i in 0 until handler.size) {
        val item = handler.getItem(i)
        if (isNullOrAir(item)) continue
        if (!matchesFilter(item!!, filterItems, matchNBT)) continue
        val offer = ItemOffer(this, item, i)
        offers.add(offer)
      }
    } else if (block.state is Container) {
      val container = block.state as Container
      val inventory = container.inventory
      for (i in 0 until inventory.size) {
        val item = inventory.getItem(i)
        if (isNullOrAir(item)) continue
        if (!matchesFilter(item!!, filterItems, matchNBT)) continue
        val offer = ItemOffer(this, item.clone(), i)
        offers.add(offer)
      }
    }
    return offers
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

    val blockData = this.location.block.blockData
    if (blockData is Directional) {
      val targetBlock = location.clone().add(blockData.facing.direction).block
      val offers = getAvailableItems(targetBlock, blockData.facing.oppositeFace)
      itemNetwork?.addOffers(this, offers)
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

  override fun fulfillOffer(offer: ItemOffer, maxItems: Int): ItemStack? {
    val blockData = this.location.block.blockData
    if (blockData !is Directional) return null

    val block = location.clone().add(blockData.facing.direction).block
    val face = blockData.facing.oppositeFace
    val optTileEntity: Optional<AstralTileEntity> = AstralItems.getInstance().getTileEntity(block)
    if (optTileEntity.isPresent) {
      val astralTileEntity = optTileEntity.get()
      var handler: ItemHandler? = null
      if (astralTileEntity is SidedInventory) {
        handler = (astralTileEntity as SidedInventory).getItemHandler(face)
      } else if (astralTileEntity is InventoryHolder) {
        handler = (astralTileEntity as InventoryHolder).itemHandler
      }
      if (handler == null) return null

      val item = handler.getItem(offer.slot)
      if (item == null || item.type != offer.item.type) return null;

      if (item.amount <= minOf(offer.item.amount, maxItems)) {
        handler.setItem(offer.slot, null)
        return item.clone()
      }

      item.amount -= minOf(offer.item.amount, maxItems)
      handler.setItem(offer.slot, item)
      val returnItem = item.clone()
      returnItem.amount = minOf(offer.item.amount, maxItems)
      return returnItem

    } else if (block.state is Container) {
      val container = block.state as Container
      val inventory = container.inventory
      val item = inventory.getItem(offer.slot)
      if (item == null || item.type != offer.item.type) return null;

      if (item.amount <= minOf(offer.item.amount, maxItems)) {
        inventory.setItem(offer.slot, null)
        return item.clone()
      }

      item.amount -= minOf(offer.item.amount, maxItems)
      val returnItem = item.clone()
      returnItem.amount = minOf(offer.item.amount, maxItems)
      return returnItem
    }
    return null
  }
}