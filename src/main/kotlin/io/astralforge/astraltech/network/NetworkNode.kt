package io.astralforge.astraltech.network

import io.astralforge.astralitems.block.tile.AstralTileEntity

abstract class NetworkNodeTile : AstralTileEntity() {
  var network: Network? = null

  override fun onLoad() {
    network = Network.getNetworkFromNodeBlock(location.block)
  }

  override fun onUnload() {
    network?.removeNode(this)
  }
}
