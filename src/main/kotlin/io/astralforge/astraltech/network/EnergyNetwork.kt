package io.astralforge.astraltech.network

import io.astralforge.astraltech.tile.Powerable
import org.bukkit.Material
import org.bukkit.block.Block

class EnergyNetwork: Network() {
  val spokenTiles: MutableSet<NetworkNodeTile> = mutableSetOf()
  val requests: MutableMap<NetworkNodeTile, Long> = mutableMapOf()
  val offers: MutableMap<NetworkEnergyProvider, Long> = mutableMapOf()
  var provisionedPower = 0L

  fun requestPower(node: NetworkNodeTile, rf: Long) {
    requests[node] = rf
    speak(node)
  }

  fun providePower(node: NetworkNodeTile, provider: NetworkEnergyProvider, rf: Long) {
    offers[provider] = rf
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
        var consumed = machine.receivePower(allocatedPower)
        // Leftover power
        provisionedPower -= consumed
        requests[req.key] = req.value - consumed
        for (offer in offers) {
          if (offer.value > 0) {
            val offerPowerConsumed = minOf(offer.value, consumed)
            consumed -= offerPowerConsumed
            offers[offer.key] = offer.value - offerPowerConsumed
            if (offers[offer.key] == 0L) {
              offer.key.onOfferedPowerResults(0L)
              offers.remove(offer.key)
            }
          }
        }
        // Remove if request is fulfilled
        return@removeAll requests[req.key]!! <= 0L
      }

    }

    // Cleanup
    spokenTiles.clear()
    requests.clear()
    for (offer in offers) {
      offer.key.onOfferedPowerResults(offer.value)
    }
    offers.clear()
    provisionedPower = 0L
  }

  override fun isEdgeBlock(block: Block): Boolean
    = itemsPlugin.getAstralBlock(block).isEmpty
      && block.type == Material.BLACKSTONE_WALL
}
