package io.astralforge.astraltech.tile

import de.tr7zw.nbtapi.NBTItem
import io.astralforge.astraltech.AstralTech
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Particle
import org.bukkit.event.block.Action
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

class TestMachineTile: BufferedMachineTile(maxBuffer=25000L, maxChargeRate=50L), TechInventory {
  private val energyPerOperation = 10000L
  private val energyUseRate = 40L
  private val energyUsedKey = NamespacedKey(AstralTech.instance, "energy_used")
  private var energyUsed = 0L
  private val inventory = Bukkit.createInventory(null, 5*9, "Test Machine Tile").registerWithTech(this)

  companion object : Builder {
    override fun build(): TestMachineTile {
      return TestMachineTile()
    }
  }

  override fun onLoad(container: PersistentDataContainer) {
    super.onLoad(container)

    energyUsed = container.get(energyUsedKey, PersistentDataType.LONG)?: 0L

    // Load inventory
    val placeholder = ItemStack(Material.WHITE_STAINED_GLASS_PANE)
    for (i in 0 until inventory.size) {
      inventory.setItem(i, placeholder.clone())
    }
  }

  override fun onUnload(container: PersistentDataContainer) {
    super.onUnload(container)

    container.set(energyUsedKey, PersistentDataType.LONG, energyUsed);
  }

  override fun onInteract(event: PlayerInteractEvent) {
    super.onInteract(event)

    if (event.action == Action.RIGHT_CLICK_BLOCK) {
      if (!event.player.isSneaking) {
        event.isCancelled = true
        event.player.openInventory(inventory)
      }
    }
  }

  override fun receivePower(rf: Long): Long {
    val received = super.receivePower(rf)

    return received
  }

  override fun tick() {
    super.tick()

    val energyToConsume = minOf(energyUseRate, buffer, energyPerOperation - energyUsed)
    energyUsed += energyToConsume
    buffer -= energyToConsume

    if (energyUsed >= energyPerOperation) {
      location.world?.spawnParticle(
          Particle.HEART,
          location,
          1
      )
      energyUsed = 0L
    }

    displayPower()
    displayOperationProgress()
  }

  override fun onInventoryInteract(event: InventoryClickEvent) {
    if (event.clickedInventory == inventory) {
      event.isCancelled = true
    }
  }

  /*fun getPane(index: Int, total: Int): ItemStack {


    val pos = index / total.toDouble()
    val aPos = (index.toDouble() + 0.33) / total.toDouble()
    val bPos = (index.toDouble() + 0.66) / total.toDouble()

    val item = if (buffer.toDouble(7) / energyPerOperation < pos) {
      ItemStack(Material.RED_STAINED_GLASS_PANE)
    } else if (buffer.toDouble() / energyPerOperation < nextPos) {
      ItemStack(Material.ORANGE_STAINED_GLASS_PANE)
    } else {
      ItemStack(Material.LIME_STAINED_GLASS_PANE)
    }

    val nbti = NBTItem(item)
    nbti.addCompound("display").setString("Name", "{\"text\":\"Buffer: $buffer\", \"italic\":false}")
    return nbti.item;
  }*/

  fun getPane(index: Int, total: Int): ItemStack {
    val greens = ((buffer.toDouble() / maxBuffer) * total.toDouble());
    val progress = greens - index.toDouble();

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
    nbti.addCompound("display").setString("Name", "{\"text\":\"Buffer: $buffer\", \"italic\":false}")
    return nbti.item;
  }

  fun displayPower() {
    for (i in 28..34) {
      inventory.setItem(i, getPane(i - 28, 7))
    }
  }

  fun displayOperationProgress() {
    val item = ItemStack(Material.IRON_HOE)
    val meta = item.itemMeta as Damageable
    meta.damage = ((1 - (energyUsed.toDouble() / energyPerOperation)) * 250).toInt()
    item.itemMeta = meta

    val nbti = NBTItem(item)
    nbti.addCompound("display").setString("Name", "{\"text\":\"Progress: $energyUsed / $energyPerOperation\", \"italic\":false}")

    inventory.setItem(13, nbti.item)
  }

}