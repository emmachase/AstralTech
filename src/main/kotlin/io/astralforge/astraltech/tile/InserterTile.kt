package io.astralforge.astraltech.tile

import io.astralforge.astralitems.AstralItems
import io.astralforge.astralitems.block.tile.AstralTileEntity
import io.astralforge.astralitems.block.tile.InventoryHolder
import io.astralforge.astralitems.block.tile.ItemHandler
import io.astralforge.astralitems.block.tile.SidedInventory
import io.astralforge.astraltech.itemnetwork.ItemNetworkNodeTile
import io.astralforge.astraltech.itemnetwork.ItemOffer
import org.bukkit.Material
import org.bukkit.Registry.MATERIAL
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.Container
import org.bukkit.block.data.Directional
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import java.util.*

class InserterTile: ItemNetworkNodeTile() {
  companion object : Builder {
    override fun build(): InserterTile {
      return InserterTile()
    }
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
      //val offers = getAvailableItems(targetBlock, blockData.facing.oppositeFace)
      itemNetwork?.requestItems(this)
    } else {
      // Something wacky happened with our block
      itemNetwork?.addOffers(this, emptyList())
    }
  }

  override fun reviewOffers(offers: MutableMap<Material, MutableList<ItemOffer>>) {
    val blockData = this.location.block.blockData
    if (blockData !is Directional) return

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
          for (offerKey in offers.keys) {
            val materialToGet = offers.keys.first()
            for (offerSelected in offers[offerKey]!!) {
              val fulfilledItem = offerSelected.tile.fulfillOffer(offerSelected, materialToGet.maxStackSize)
              offers[offerKey]?.remove(offerSelected)
              if (isNullOrAir(fulfilledItem)) continue
              handler.setItem(i, fulfilledItem)
              break@inventoryLoop
            }
          }
        } else {
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
          for (offerKey in offers.keys) {
            val materialToGet = offers.keys.first()
            val offersToRemove = mutableListOf<ItemOffer>()
            for (offerSelected in offers[offerKey]!!) {
              val fulfilledItem = offerSelected.tile.fulfillOffer(offerSelected, materialToGet.maxStackSize)
              offersToRemove.add(offerSelected)
              if (isNullOrAir(fulfilledItem)) continue
              inventory.setItem(i, fulfilledItem)
              break@inventoryLoop
            }
            offers[offerKey]?.removeAll(offersToRemove)
          }
        } else if (item != null) {
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