package io.astralforge.astraltech.itemnetwork

import io.astralforge.astralitems.block.tile.AstralTileEntity
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataContainer

abstract class ItemNetworkNodeTile : AstralTileEntity() {
  var itemNetwork: ItemNetwork? = null

  override fun onLoad(container: PersistentDataContainer) {
    super.onLoad(container);
    ItemNetwork.getNetworkFromNodeBlock(location.block)
  }

  override fun onUnload(container: PersistentDataContainer) {
    super.onUnload(container)
    itemNetwork?.removeNode(this);
  }

  fun newNetwork(itemNetwork: ItemNetwork?) {
    this.itemNetwork = itemNetwork
  }

  open fun reviewOffers(offers: MutableMap<Material, MutableList<ItemOffer>>) {

  }

  /* Returns an ItemStack, possibly null, of the offer's material type,
  up to a count of the offer's count or maxItems, whichever is lower */
  open fun fulfillOffer(offer: ItemOffer, maxItems: Int): ItemStack? {
    return null
  }

}
