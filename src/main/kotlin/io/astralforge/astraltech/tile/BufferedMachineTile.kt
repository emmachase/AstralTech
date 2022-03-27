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

  fun paneLoadingBar(inv: Inventory, slots: List<Int>, current: Long, total: Long, label: String) {
    val fullSlots = ((current.toDouble() / total) * slots.size)
    for (i in slots.indices) {
      val slot = slots[i]
      val progress = fullSlots - i

      val item = if (progress >= 0.9) {
        ItemStack(Material.LIME_STAINED_GLASS_PANE)
      } else if (progress >= 0.5) {
        ItemStack(Material.ORANGE_STAINED_GLASS_PANE)
      } else if (progress >= 0.1) {
        ItemStack(Material.RED_STAINED_GLASS_PANE)
      } else {
        ItemStack(Material.GRAY_STAINED_GLASS_PANE)
      }

      val nbti = NBTItem(item)
      nbti.addCompound("display").setString("Name", "{\"text\":\"$label: $current / $total\", \"italic\":false}")
      inv.setItem(slot, nbti.item)
    }
  }

  fun durabilityLoadingBar(inv: Inventory, slot: Int, material: Material, current: Long, total: Long, label: String) {
    val item = ItemStack(material)
    val meta = item.itemMeta as Damageable
    meta.damage = ((1 - (current.toDouble() / total)) * item.type.maxDurability).toInt()
    item.itemMeta = meta

    val nbti = NBTItem(item)
    nbti.addCompound("display").setString("Name", "{\"text\":\"$label: $current / $total\", \"italic\":false}")

    inv.setItem(slot, nbti.item)
  }

  fun fillWithBackground(inv: Inventory) {
    val placeholder = ItemStack(Material.WHITE_STAINED_GLASS_PANE)
    val nbti = NBTItem(placeholder)
    nbti.addCompound("display").setString("Name", "{\"text\":\"\", \"italic\":false}")
    for (i in 0 until inv.size) {
      inv.setItem(i, nbti.item.clone())
    }
  }

  fun writeItemToContainer(item: ItemStack?, slot: Int, container: PersistentDataContainer) {
    val outputStream = ByteArrayOutputStream()
    val dataOutput = BukkitObjectOutputStream(outputStream)
    dataOutput.writeObject(item);
    dataOutput.close()
    container.set(NamespacedKey(AstralTech.instance, "item_$slot"), PersistentDataType.BYTE_ARRAY, outputStream.toByteArray())
  }

  fun getItemFromContainer(slot: Int, container: PersistentDataContainer): ItemStack? {
    if (!container.has(NamespacedKey(AstralTech.instance, "item_$slot"), PersistentDataType.BYTE_ARRAY)) {
      return null
    }
    val inputStream = ByteArrayInputStream(container.get(NamespacedKey(AstralTech.instance, "item_$slot"), PersistentDataType.BYTE_ARRAY))
    BukkitObjectInputStream(inputStream).use {
      return it.readObject() as? ItemStack ?: return null
    }
  }
}
