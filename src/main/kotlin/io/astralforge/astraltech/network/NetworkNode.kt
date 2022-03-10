package io.astralforge.astraltech.network

import io.astralforge.astralitems.block.tile.AstralTileEntity
import org.bukkit.persistence.PersistentDataContainer

abstract class NetworkNodeTile : AstralTileEntity() {
  var network: Network? = null

  override fun onLoad() {
    Network.getNetworkFromNodeBlock(location.block)
  }

  override fun onUnload() {
    network?.removeNode(this)
  }

  fun newNetwork(network: Network?) {
    this.network = network
  }

  override fun serialize(container: PersistentDataContainer) {
    super.serialize(container)
  }
}
