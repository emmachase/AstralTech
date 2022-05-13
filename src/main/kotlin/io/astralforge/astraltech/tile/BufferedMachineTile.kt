package io.astralforge.astraltech.tile

import de.tr7zw.nbtapi.NBTItem
import io.astralforge.astraltech.AstralTech
import io.astralforge.astraltech.network.NetworkNodeTile
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import org.bukkit.util.io.BukkitObjectInputStream
import org.bukkit.util.io.BukkitObjectOutputStream
import java.io.ByteArrayInputStream

import java.io.ByteArrayOutputStream




interface Powerable {
  fun receivePower(rf: Long): Long
}

abstract class BufferedMachineTile constructor(
  val maxBuffer: Long,
  val maxChargeRate: Long,
  val handleRequests: Boolean = true
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
    if (handleRequests) {
      network?.requestPower(this, minOf(maxChargeRate, maxBuffer - buffer))
    }
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
