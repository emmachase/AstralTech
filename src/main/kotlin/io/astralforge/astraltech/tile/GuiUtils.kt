package io.astralforge.astraltech.tile

import de.tr7zw.nbtapi.NBTItem
import io.astralforge.astraltech.AstralTech
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

fun optionSelector(inv: Inventory, slots: List<Int>, selected: String, values: List<String>, label: String) {
  for (i in slots.indices) {
    val slot = slots[i]
    if (values.size <= i) continue
    val value = values[i]

    val item = if (selected == value) {
      ItemStack(Material.LIME_STAINED_GLASS_PANE)
    } else {
      ItemStack(Material.GRAY_STAINED_GLASS_PANE)
    }

    val nbti = NBTItem(item)
    nbti.addCompound("display").setString("Name", "{\"text\":\"$label: $value\", \"italic\":false}")
    inv.setItem(slot, nbti.item)
  }
}

fun displayToggle(inv: Inventory, slots: List<Int>, toggled: Boolean, active: String, inactive: String ) {
  for (i in slots.indices) {
    val slot = slots[i]

    val item = if (toggled) {
      ItemStack(Material.LIME_STAINED_GLASS_PANE)
    } else {
      ItemStack(Material.RED_STAINED_GLASS_PANE)
    }

    val nbti = NBTItem(item)
    if (toggled) {
      nbti.addCompound("display").setString("Name", "{\"text\":\"$active\", \"italic\":false}")
    } else {
      nbti.addCompound("display").setString("Name", "{\"text\":\"$inactive\", \"italic\":false}")
    }
    inv.setItem(slot, nbti.item)
  }
}

fun paneLoadingBar(inv: Inventory, slots: List<Int>, current: Long, total: Long, label: String) {
  return paneLoadingBar(inv, slots, current, total, label, "")
}

fun paneLoadingBar(inv: Inventory, slots: List<Int>, current: Long, total: Long, label: String, units: String) {
  return paneLoadingBar(inv, slots, current, total, label, units, Material.LIME_STAINED_GLASS_PANE, Material.ORANGE_STAINED_GLASS_PANE, Material.RED_STAINED_GLASS_PANE, Material.GRAY_STAINED_GLASS_PANE)
}

fun paneLoadingBar(inv: Inventory, slots: List<Int>, current: Long, total: Long, label: String, units: String, fullMaterial: Material, mostlyFullMaterial: Material, notEmptyMaterial: Material, emptyMaterial: Material) {
  val fullSlots = ((current.toDouble() / total) * slots.size)
  for (i in slots.indices) {
    val slot = slots[i]
    val progress = fullSlots - i

    val item = if (progress >= 0.9) {
      ItemStack(fullMaterial)
    } else if (progress >= 0.5) {
      ItemStack(mostlyFullMaterial)
    } else if (progress >= 0.1) {
      ItemStack(notEmptyMaterial)
    } else {
      ItemStack(emptyMaterial)
    }

    val nbti = NBTItem(item)
    nbti.addCompound("display").setString("Name", "{\"text\":\"$label: $current$units / $total$units\", \"italic\":false}")
    inv.setItem(slot, nbti.item)
  }
}

fun getNamedItem(item: ItemStack, name: String): ItemStack {
  val nbti = NBTItem(item)
  nbti.addCompound("display").setString("Name", "{\"text\":\"$name\", \"italic\":false}")
  return nbti.item
}

fun durabilityLoadingBar(inv: Inventory, slot: Int, material: Material, current: Long, total: Long, label: String) {
  return durabilityLoadingBar(inv, slot, material, current, total, label, "")
}

fun durabilityLoadingBar(inv: Inventory, slot: Int, material: Material, current: Long, total: Long, label: String, units: String) {
  val item = ItemStack(material)
  val meta = item.itemMeta as Damageable
  meta.damage = ((1 - (current.toDouble() / total)) * item.type.maxDurability).toInt()
  item.itemMeta = meta

  val nbti = NBTItem(item)
  nbti.addCompound("display").setString("Name", "{\"text\":\"$label: $current$units / $total$units\", \"italic\":false}")

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