package io.astralforge.astraltech.tile

import io.astralforge.astralitems.AstralItems
import io.astralforge.astralitems.block.tile.AstralTileEntity
import io.astralforge.astralitems.block.tile.InventoryHolder
import io.astralforge.astralitems.block.tile.ItemHandler
import io.astralforge.astralitems.block.tile.SidedInventory
import io.astralforge.astraltech.itemnetwork.ItemNetworkNodeTile
import io.astralforge.astraltech.itemnetwork.ItemOffer
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.Container
import org.bukkit.block.data.Directional
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import java.util.*

class ExtractorTile: ItemNetworkNodeTile() {
  companion object : Builder {
    override fun build(): ExtractorTile {
      return ExtractorTile()
    }
  }

  fun getAvailableItems(block: Block, face: BlockFace): List<ItemOffer> {
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
        val offer = ItemOffer(this, item, i)
        offers.add(offer)
      }
    } else if (block.state is Container) {
      val container = block.state as Container
      val inventory = container.inventory
      for (i in 0 until inventory.size) {
        val item = inventory.getItem(i)
        if (isNullOrAir(item)) continue
        val offer = ItemOffer(this, item!!.clone(), i)
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
      }
    }
  }

  override fun tick() {
    super.tick()
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
      if (item.type != offer.item.type) return null;

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