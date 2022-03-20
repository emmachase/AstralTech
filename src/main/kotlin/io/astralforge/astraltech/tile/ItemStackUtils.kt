package io.astralforge.astraltech.tile

import org.bukkit.Material
import org.bukkit.inventory.ItemStack

fun isNullOrAir(item: ItemStack?): Boolean {
  return item == null || item.type == Material.AIR
}