package io.astralforge.astraltech.itemnetwork

import io.astralforge.astralitems.AstralItems
import io.astralforge.astraltech.tile.Powerable
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace.*
import java.util.*

class ItemNetwork {
  companion object {
    private val itemsPlugin: AstralItems get() = AstralItems.getInstance()

    private val blockToItemNetwork: MutableMap<Block, ItemNetwork> = mutableMapOf()
//    private val nodeTypes: MutableSet<Class<NetworkNodeTile>> = mutableSetOf()

//    fun registerNodeType(nodeType: Class<NetworkNodeTile>) {
//      nodeTypes.add(nodeType)
//    }

    private fun isEdgeBlock(block: Block): Boolean {
      return block.type == Material.END_STONE_BRICK_WALL
    }

    fun getNetworkFromNodeBlock(block: Block): ItemNetwork {
      return blockToItemNetwork[block] ?: DiscoverySearch().run(block)
    }

    fun unionNetwork(itemNetwork1: ItemNetwork, itemNetwork2: ItemNetwork): ItemNetwork {
      val newItemNetwork = ItemNetwork()
      newItemNetwork.addAll(itemNetwork1.nodes)
      newItemNetwork.addAll(itemNetwork2.nodes)
      return newItemNetwork
    }

//    fun discoverNetwork(block: Block): Network {
//      val network = Network()
//
//      val queue = mutableListOf(block)
//      val visited = mutableSetOf(block)
//
//      while (queue.isNotEmpty()) {
//        val current = queue.removeAt(0)
//
////        if (isEdgeBlock(current)) {
////          network.addEdge(current)
////        } else {
////          val nodeType = itemsPlugin.getNodeType(current)
////          if (nodeType != null) {
////            network.addNode(current, nodeType)
////          }
////        }
//
//        for (face in BlockFace.values()) {
//          val neighbor = current.getRelative(face)
//          if (visited.contains(neighbor)) {
//            continue
//          }
//
//          if (isEdgeBlock(neighbor)) {
//            // Pass
//          } else {
//            val blockType = itemsPlugin.getTileEntity(neighbor)
//            if (blockType.isPresent) {
//              val nodeType = blockType.get() as? NetworkNodeTile ?: continue
//
//              if (nodeTypes.contains(nodeType.javaClass)) {
//                network.addNode(nodeType)
//              } else {
//                continue
//              }
//            } else {
//              continue
//            }
//          }
//
//          queue.add(neighbor)
//          visited.add(neighbor)
//        }
//      }
//
//      return network
//    }
  }

  private val nodes: MutableSet<ItemNetworkNodeTile> = mutableSetOf()


//  enum class ELEMENT_TYPE {
//    NODE, EDGE
//  }

//  fun addEdge(block: Block) {
//    blockToNetwork[block] = this
//  }
//
  fun removeNode(node: ItemNetworkNodeTile) {
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

  fun addNode(node: ItemNetworkNodeTile) {
    node.newNetwork(this)
    nodes.add(node)
  }

  fun addAll(nodes: Set<ItemNetworkNodeTile>) {
    for (node in nodes) {
      addNode(node)
    }
  }

  val spokenTiles: MutableSet<ItemNetworkNodeTile> = mutableSetOf()

  val offers: MutableMap<Material, MutableList<ItemOffer>> = mutableMapOf()
  val requests: PriorityQueue<ItemNodeRequest> = PriorityQueue()

  fun addOffer(offer: ItemOffer) {
    val material = offer.item.type
    if (!offers.containsKey(material)) {
      offers[material] = mutableListOf()
    }
    offers[material]!!.add(offer)
  }

  fun addOffers(node: ItemNetworkNodeTile, offers: List<ItemOffer>) {
    for (offer in offers) {
      addOffer(offer)
    }
    speak(node)
  }

  fun requestItems(node: ItemNetworkNodeTile, priority: Int) {
    requests.add(ItemNodeRequest(node, priority))
    speak(node)
  }

  private fun speak(node: ItemNetworkNodeTile) {
    spokenTiles.add(node)
    if (spokenTiles.size == nodes.size) settleNetwork()
  }

  private fun settleNetwork() {

    for (request in requests) {
      request.node.reviewOffers(offers)
    }

    // Cleanup
    spokenTiles.clear()
    requests.clear()
    for (offer in offers) {
      offer.value.clear()
    }
  }

  class ItemNodeRequest: Comparable<ItemNodeRequest> {
    val node: ItemNetworkNodeTile
    val priority: Int

    constructor(node: ItemNetworkNodeTile, priority: Int) {
      this.node = node
      this.priority = priority
    }

    override fun compareTo(other: ItemNodeRequest): Int {
      return priority.compareTo(other.priority)
    }
  }

//  private fun Block.getNetNeighbors(): List<Block> {
//    return listOf(
//        this.getRelative(BlockFace.NORTH),
//        this.getRelative(BlockFace.SOUTH),
//        this.getRelative(BlockFace.EAST),
//        this.getRelative(BlockFace.WEST),
//        this.getRelative(BlockFace.UP),
//        this.getRelative(BlockFace.DOWN)
//    )
//  }
}
