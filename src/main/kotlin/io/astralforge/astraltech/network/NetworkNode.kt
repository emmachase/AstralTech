package io.astralforge.astraltech.network

import io.astralforge.astralitems.block.tile.AstralTileEntity
import org.bukkit.persistence.PersistentDataContainer

abstract class NetworkNodeTile : AstralTileEntity() {
  var network: Network? = null

  override fun onLoad(container: PersistentDataContainer) {
    super.onLoad(container);
    Network.getNetworkFromNodeBlock(location.block)
  }

  override fun onUnload(container: PersistentDataContainer) {
    super.onUnload(container)
    network?.removeNode(this);
  }

  fun newNetwork(network: Network?) {
    this.network = network
  }

}
