package io.astralforge.astraltech.tile

import io.astralforge.astralitems.block.tile.*
import org.bukkit.inventory.ItemStack

class ItemVoiderTile: AstralTileEntity(), InventoryHolder, ItemTransferHandler, ItemHandler {

  companion object : Builder {
    override fun build(): ItemVoiderTile {
      return ItemVoiderTile()
    }
  }

  override fun getItemHandler(): ItemHandler {
    return this
  }

  override fun onItemsTransferred() {}

  override fun getSize(): Int {
    return 6*9
  }

  override fun getItem(slot: Int): ItemStack? = null

  override fun setItem(slot: Int, stack: ItemStack?) {}

  override fun extractItem(slot: Int, amount: Int, simulate: Boolean): ItemStack? = null

  override fun extractItem(slot: Int, amount: Int): ItemStack? = null

  override fun extractItem(amount: Int): ItemStack? = null

  override fun insertItem(slot: Int, item: ItemStack?): ItemStack? = null

  override fun insertItem(slot: Int, item: ItemStack?, simulate: Boolean): ItemStack? = null

  override fun insertItem(item: ItemStack?): ItemStack? = null
}
