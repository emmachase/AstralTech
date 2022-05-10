package io.astralforge.astraltech.itemnetwork

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

class DiscoverySearch: Search<ItemNetwork>() {

  private val itemsPlugin: AstralItems get() = AstralItems.getInstance()

  private val foundNodes: MutableList<ItemNetworkNodeTile> = mutableListOf()

  private fun isEdgeBlock(block: Block): Boolean {
    return block.type == Material.END_STONE_BRICK_WALL
  }

  override fun visitNode(block: Block): Boolean {
    if (isEdgeBlock(block)) return true
    val blockType = itemsPlugin.getTileEntity(block)
    if (blockType.isEmpty) return false

    val nodeType = blockType.get() as? ItemNetworkNodeTile ?: return false
    foundNodes.add(nodeType)
    return true
  }

  override fun getResult(): ItemNetwork {
    var itemNetwork = ItemNetwork()
    for (node in foundNodes) {
      val nodeNet = node.itemNetwork
      if (nodeNet != null) {
        itemNetwork = ItemNetwork.unionNetwork(itemNetwork, nodeNet)
        node.newNetwork(itemNetwork)
      } else {
        itemNetwork.addNode(node)
      }
    }

    return itemNetwork
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
