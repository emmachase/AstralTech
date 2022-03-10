package io.astralforge.astraltech.network

import io.astralforge.astralitems.AstralItems
import io.astralforge.astraltech.tile.Powerable
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace.*

class Network {
  companion object {
    private val itemsPlugin: AstralItems get() = AstralItems.getInstance()

    private val blockToNetwork: MutableMap<Block, Network> = mutableMapOf()
//    private val nodeTypes: MutableSet<Class<NetworkNodeTile>> = mutableSetOf()

//    fun registerNodeType(nodeType: Class<NetworkNodeTile>) {
//      nodeTypes.add(nodeType)
//    }

    private fun isEdgeBlock(block: Block): Boolean {
      return block.type == Material.BLACKSTONE_WALL
    }

    fun getNetworkFromNodeBlock(block: Block): Network {
      return blockToNetwork[block] ?: DiscoverySearch().run(block)
    }

    fun unionNetwork(network1: Network, network2: Network): Network {
      val newNetwork = Network()
      newNetwork.addAll(network1.nodes)
      newNetwork.addAll(network2.nodes)
      return newNetwork
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

  private val nodes: MutableSet<NetworkNodeTile> = mutableSetOf()


//  enum class ELEMENT_TYPE {
//    NODE, EDGE
//  }

//  fun addEdge(block: Block) {
//    blockToNetwork[block] = this
//  }
//
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

  val spokenTiles: MutableSet<NetworkNodeTile> = mutableSetOf()
  val requests: MutableMap<NetworkNodeTile, Long> = mutableMapOf()
  var provisionedPower = 0L

  fun requestPower(node: NetworkNodeTile, rf: Long) {
    requests[node] = rf
    speak(node)
  }

  fun providePower(node: NetworkNodeTile, rf: Long) {
    provisionedPower += rf
    speak(node)
  }

  private fun speak(node: NetworkNodeTile) {
    spokenTiles.add(node)
    if (spokenTiles.size == nodes.size) settleNetwork()
  }

  private fun settleNetwork() {

    while (provisionedPower > 0 && requests.isNotEmpty()) {
      val split = maxOf(1L, provisionedPower / requests.size)
      requests.entries.removeAll { req ->
        if (provisionedPower == 0L) return@removeAll true
        val allocatedPower = minOf(split, req.value)
        val machine = req.key as? Powerable ?: return@removeAll true
        val consumed = machine.receivePower(allocatedPower)
        // Leftover power
        provisionedPower -= consumed
        requests[req.key] = req.value - consumed
        // Remove if request is fulfilled
        return@removeAll requests[req.key]!! <= 0L
      }

    }

    // Cleanup
    spokenTiles.clear()
    requests.clear()
    provisionedPower = 0L
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
