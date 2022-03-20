package io.astralforge.astraltech.tile

import io.astralforge.astraltech.AstralTech
import io.astralforge.astraltech.network.NetworkNodeTile
import org.bukkit.NamespacedKey
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

interface Powerable {
  fun receivePower(rf: Long): Long
}

abstract class BufferedMachineTile constructor(
  val maxBuffer: Long,
  val maxChargeRate: Long
): NetworkNodeTile(), Powerable {
  protected var buffer = 0L
  private val bufferKey = NamespacedKey(AstralTech.instance, "buffer")

  override fun receivePower(rf: Long): Long {
    val amountReceived = minOf(rf, maxChargeRate, maxBuffer - buffer)
    buffer += amountReceived
    return amountReceived
  }

  override fun tick() {
    super.tick()
    network?.requestPower(this, minOf(maxChargeRate, maxBuffer - buffer))
  }

  override fun onUnload(container: PersistentDataContainer) {
    super.onUnload(container)
    container.set(bufferKey, PersistentDataType.LONG, buffer)
  }

  override fun onLoad(container: PersistentDataContainer) {
    super.onLoad(container)
    buffer = container[bufferKey, PersistentDataType.LONG] ?: buffer
  }
}
