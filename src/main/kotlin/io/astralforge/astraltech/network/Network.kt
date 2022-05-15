package io.astralforge.astraltech.network

import io.astralforge.astralitems.AstralItems
import org.bukkit.block.Block
import org.bukkit.block.BlockFace.*

interface NetworkMechanism {
  fun isEdgeBlock(block: Block): Boolean
}

class Network<Subnet: NetworkMechanism> constructor(val subnet: Subnet) {
  fun getMechanism(): Subnet {
    return subnet
  }

  fun unionNetwork(network1: Network<Subnet>, network2: Network<Subnet>): Network<Subnet> {
    val newNetwork = Network(subnet)
    newNetwork.addAll(network1.nodes)
    newNetwork.addAll(network2.nodes)
    return newNetwork
  }

  companion object {
    private val itemsPlugin: AstralItems by lazy { AstralItems.getInstance() }
    private val mechanismRegistry = mutableSetOf<NetworkMechanism>()

    fun getNetworkFromNodeBlock(block: Block): Network<NetworkMechanism> {
      return DiscoverySearch().run(block)
    }
  }

  private val nodes: MutableSet<NetworkNodeTile> = mutableSetOf()

  fun removeNode(node: NetworkNodeTile) {
    for (n in nodes) {
      n.newNetwork(null)

    }
    nodes.clear()

    val block = node.location.block
    for (face in listOf(NORTH, SOUTH, EAST, WEST, UP, DOWN)) {
      val neighbor = block.getRelative(face)
      getNetworkFromNodeBlock(neighbor)
    }
  }

  fun addNode(node: NetworkNodeTile) {
    node.newNetwork(this)
    nodes.add(node)
  }

  fun addAll(nodes: Set<NetworkNodeTile>) {
    for (node in nodes) {
      addNode(node)
    }
  }
}
