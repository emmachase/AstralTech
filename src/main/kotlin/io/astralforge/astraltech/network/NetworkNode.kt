package io.astralforge.astraltech.network

import io.astralforge.astralitems.block.tile.AstralTileEntity

abstract class NetworkNodeTile : AstralTileEntity() {
  var network: Network? = null

  override fun onLoad() {
    Network.getNetworkFromNodeBlock(location.block)
  }

  override fun onUnload() {
    network?.removeNode(this)
  }

  fun newNetwork(network: Network?) {
    println("new net $network")
    this.network = network
  }
}
