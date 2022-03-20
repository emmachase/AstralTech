package io.astralforge.astraltech.tile

import io.astralforge.astralitems.AstralItems
import io.astralforge.astralitems.recipe.AstralRecipeEvaluator
import io.astralforge.astralitems.recipe.AstralRecipeEvaluator.Strategy
import io.astralforge.astraltech.AstralTech
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.event.block.Action
import org.bukkit.event.inventory.InventoryAction
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.Recipe
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import org.bukkit.scheduler.BukkitRunnable

abstract class CraftingMachineTile constructor(
    maxBuffer: Long,
    maxChargeRate: Long,
    private val energyPerOperation: Long,
    private val energyUseRate: Long,
    private val craftingBox: Box,
    private val outputSlot: Int,
    inventoryName: String,
    private val progressMaterial: Material,
    private val strategies: List<Strategy<out Recipe>>,
    private val canFallbackToVanilla: Boolean
): BufferedMachineTile(maxBuffer=maxBuffer, maxChargeRate=maxChargeRate), TechInventoryListener {
  private val energyUsedKey = NamespacedKey(AstralTech.instance, "energy_used")
  private var energyUsed = 0L
  private val inventory = Bukkit.createInventory(null, 6*9, inventoryName).registerWithTech(this)
  private var currentCraftingMatrix: Array<ItemStack?>? = null
  private var active: Boolean = false
  set(value) {
    if (!value) energyUsed = 0
    field = value
  }

  private var matchedRecipe: Recipe? = null

  companion object : Builder {
    override fun build(): TestMachineTile {
      return TestMachineTile()
    }
  }

  override fun onLoad(container: PersistentDataContainer) {
    super.onLoad(container)

    energyUsed = container.get(energyUsedKey, PersistentDataType.LONG)?: 0L

    // Load inventory
    fillWithBackground(inventory)
    craftingBox.getBox().forEach { slot ->
      inventory.clear(slot)
      getItemFromContainer(slot, container).let { inventory.setItem(slot, it) }
    }
    getItemFromContainer(outputSlot, container).let { inventory.setItem(outputSlot, it) }
    applyNewRecipe()
  }

  override fun onUnload(container: PersistentDataContainer) {
    super.onUnload(container)

    container.set(energyUsedKey, PersistentDataType.LONG, energyUsed);
    craftingBox.getBox().forEach { slot ->
      writeItemToContainer(inventory.getItem(slot), slot, container)
    }
    writeItemToContainer(inventory.getItem(outputSlot), outputSlot, container)
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

    if (active) {
      val energyToConsume = minOf(energyUseRate, buffer, energyPerOperation - energyUsed)
      energyUsed += energyToConsume
      buffer -= energyToConsume

      if (energyUsed >= energyPerOperation) {

        val curOutput = inventory.getItem(outputSlot)

        if (isNullOrAir(curOutput)) {
          inventory.setItem(outputSlot, matchedRecipe?.result)
        } else {
          curOutput!!.amount += matchedRecipe?.result?.amount ?: 0
        }

        craftingBox.getBox().map {
          val item = inventory.getItem(it)
          if (!isNullOrAir(item)) {
            item!!.amount -= 1
          }
        }
        applyNewRecipe()
      }
    }

    paneLoadingBar(inventory, (46..52).toList(), buffer, maxBuffer, "Energy Buffer")
    durabilityLoadingBar(inventory, 23, progressMaterial, energyUsed, energyPerOperation, "Progress")
    //durabilityLoadingBar(inventory, 40, Material.IRON_HOE, energyUsed, energyPerOperation, "Progress")
    //paneLoadingBar(inventory, listOf(39, 29, 20, 11, 3, 4, 5, 15, 24, 33, 41), buffer, maxBuffer, "Energy Buffer")
  }

  private fun hasItemTypesChanged(): Boolean {
    val craftingMatrix = craftingBox.getBox().map { inventory.getItem(it) }.toTypedArray()
    return craftingMatrix.indices.notAll { // if not all elements are the same, then something has changed
      if (isNullOrAir(craftingMatrix[it]) && isNullOrAir(currentCraftingMatrix?.get(it))) {
        return@notAll true
      }
      craftingMatrix[it]?.isSimilar(currentCraftingMatrix?.get(it)) ?: false
    }
  }

  private fun canStackResult(result: ItemStack): Boolean {
    val outputItem = inventory.getItem(outputSlot)

    if (isNullOrAir(outputItem)) {
      return true
    }
    return (outputItem!!.isSimilar(result) && outputItem.amount+result.amount <= outputItem.maxStackSize)
  }

  private fun applyNewRecipe() {
      energyUsed = 0L

      currentCraftingMatrix = craftingBox.getBox().map { inventory.getItem(it)?.clone() }.toTypedArray()
      val evaluator: AstralRecipeEvaluator = AstralItems.getInstance().recipeEvaluator
      var matchResult = evaluator.matchRecipe(currentCraftingMatrix, strategies)
      if (matchResult.isEmpty && canFallbackToVanilla) {
        matchResult = evaluator.fallbackToVanilla(currentCraftingMatrix, strategies)
      }

      active = (!matchResult.isEmpty) && canStackResult(matchResult.get().result)

      matchedRecipe = matchResult.orElse(null)
  }

  override fun onInventoryInteract(event: InventoryClickEvent) {
    object : BukkitRunnable() {
      override fun run() {
        if (hasItemTypesChanged()) {
          applyNewRecipe()
        } else if (matchedRecipe != null) {
          active = canStackResult(matchedRecipe!!.result)
        }
      }
    }.runTask(AstralItems.getInstance())
    if (event.clickedInventory == inventory) {
      if ( event.slot !in craftingBox && event.slot != outputSlot) {
        event.isCancelled = true
      }
    }
  }

  override fun onInventoryDrag(event: InventoryDragEvent) {
    object : BukkitRunnable() {
      override fun run() {
        if (hasItemTypesChanged()) {
          applyNewRecipe()
        } else if (matchedRecipe != null) {
          active = canStackResult(matchedRecipe!!.result)
        }
      }
    }.runTask(AstralItems.getInstance())
    for (slot in event.rawSlots) {
      if (slot !in craftingBox && slot != outputSlot) {
        event.isCancelled = true
      }
    }
  }

}