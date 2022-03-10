package io.astralforge.astraltech.network

import io.astralforge.astralitems.AstralItems
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace.*

abstract class Search<V> {

  abstract fun visitNode(block: Block): Boolean

  abstract fun getResult(): V

  fun run(origin: Block): V {
    val queue = mutableListOf(origin)
    val visited = mutableSetOf(origin.location)

    while (queue.isNotEmpty()) {
      val current = queue.removeAt(0)

      visited.add(current.location)
      if (visitNode(current)) {
        for (face in listOf(NORTH, SOUTH, EAST, WEST, UP, DOWN)) {
          val neighbor = current.getRelative(face)
          if (visited.contains(neighbor.location)) {
            continue
          }

          queue.add(neighbor)
        }
      }
    }

    return getResult()
  }

}

class DiscoverySearch: Search<Network>() {

  private val itemsPlugin: AstralItems get() = AstralItems.getInstance()

  private val foundNodes: MutableList<NetworkNodeTile> = mutableListOf()

  private fun isEdgeBlock(block: Block): Boolean {
    return block.type == Material.BLACKSTONE_WALL
  }

  override fun visitNode(block: Block): Boolean {
    if (isEdgeBlock(block)) return true
    val blockType = itemsPlugin.getTileEntity(block)
    if (blockType.isEmpty) return false

    val nodeType = blockType.get() as? NetworkNodeTile ?: return false
    foundNodes.add(nodeType)
    return true
  }

  override fun getResult(): Network {
    var network = Network()
    for (node in foundNodes) {
      val nodeNet = node.network
      if (nodeNet != null) {
        network = Network.unionNetwork(network, nodeNet)
        node.newNetwork(network)
      } else {
        network.addNode(node)
      }
    }

    return network
  }

}

//
//class UnionSearch: Search<List<Network>>() {
//
//  private val itemsPlugin: AstralItems get() = AstralItems.getInstance()
//
//  private val foundNodes: MutableList<NetworkNodeTile> = mutableListOf()
//
//  private fun isEdgeBlock(block: Block): Boolean {
//    return block.type == Material.BLACKSTONE_WALL
//  }
//
//  override fun visitNode(block: Block): Boolean {
//    if (isEdgeBlock(block)) return true
//
//    val blockType = itemsPlugin.getTileEntity(block)
//    if (blockType.isEmpty) return false
//
//    val nodeType = blockType.get() as? NetworkNodeTile ?: return false
//    foundNodes.add(nodeType)
//    return false
//  }
//
//  override fun getResult(): List<Network> {
//    val networks = mutableSetOf<Network>()
//    for (node in foundNodes) {
//      networks.add(node.network)
//    }
//
//    return networks.toList()
//  }
//
//}
