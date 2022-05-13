package io.astralforge.astraltech.tile

import io.astralforge.astralitems.AstralItems
import io.astralforge.astralitems.block.tile.*
import io.astralforge.astraltech.AstralTech
import org.bukkit.*
import org.bukkit.Registry.MATERIAL
import org.bukkit.block.BlockFace
import org.bukkit.block.Container
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.inventory.InventoryAction
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

class MiningLaserMachineTile: BufferedMachineTile(maxBuffer=50000, maxChargeRate=100), TechInventoryListener, InventoryHolder, ItemTransferHandler {
  private val energyPerBlock = 100
  private val inventory = Bukkit.createInventory(null, 6*9, "Mining Laser").registerWithTech(this)
  private val outputBox = Box( XY(3,1), XY(5,3) )
  private val depthSelectorBox = Box( XY(1,1), XY(1,3) )
  private val rangeSelectorBox = Box( XY(7,1), XY(7,3) )
  private val silencerBox = Box( XY(4,4), XY(4,4) )
  private val containerItemHandler = MappedInventoryItemHandler(inventory, outputBox.getBox(), this)
  private var wasPowered = false
  private val miningLaserTool = ItemStack(Material.DIAMOND_PICKAXE)

  private val laserWidth = 1
  private var laserDepth = 1
  private var laserRange = 8
  private var laserSilenced = false

  companion object : Builder {
    override fun build(): MiningLaserMachineTile {
      return MiningLaserMachineTile()
    }
  }

  private fun getLaserFace(): BlockFace? {
    val block = this.location.block
    if (block.getRelative(BlockFace.WEST).type == Material.END_ROD) {
      return BlockFace.WEST
    } else if (block.getRelative(BlockFace.EAST).type == Material.END_ROD) {
      return BlockFace.EAST
    } else if (block.getRelative(BlockFace.DOWN).type == Material.END_ROD) {
      return BlockFace.DOWN
    } else if (block.getRelative(BlockFace.UP).type == Material.END_ROD) {
      return BlockFace.UP
    } else if (block.getRelative(BlockFace.NORTH).type == Material.END_ROD) {
      return BlockFace.NORTH
    } else if (block.getRelative(BlockFace.SOUTH).type == Material.END_ROD) {
      return BlockFace.SOUTH
    }
    return null;
  }

  override fun onLoad(container: PersistentDataContainer) {
    super.onLoad(container)

    // Load inventory
    fillWithBackground(inventory)
    outputBox.getBox().forEach { slot ->
      inventory.clear(slot)
      getItemFromContainer(slot, container).let { inventory.setItem(slot, it) }
    }
    if (container.has(NamespacedKey(AstralTech.instance, "laser_depth"), PersistentDataType.INTEGER)) {
      laserDepth = container.get(NamespacedKey(AstralTech.instance, "laser_depth"), PersistentDataType.INTEGER)!!
    }
    if (container.has(NamespacedKey(AstralTech.instance, "laser_range"), PersistentDataType.INTEGER)) {
      laserRange = container.get(NamespacedKey(AstralTech.instance, "laser_range"), PersistentDataType.INTEGER)!!
    }
    if (container.has(NamespacedKey(AstralTech.instance, "laser_silenced"), PersistentDataType.INTEGER)) {
      laserSilenced = container.get(NamespacedKey(AstralTech.instance, "laser_silenced"), PersistentDataType.INTEGER)!! != 0
    }

  }

  override fun onUnload(container: PersistentDataContainer) {
    super.onUnload(container)
    outputBox.getBox().forEach { slot ->
      writeItemToContainer(inventory.getItem(slot), slot, container)
    }
    container.set(NamespacedKey(AstralTech.instance, "laser_depth"), PersistentDataType.INTEGER, laserDepth)
    container.set(NamespacedKey(AstralTech.instance, "laser_range"), PersistentDataType.INTEGER, laserRange)
    if (laserSilenced) {
      container.set(NamespacedKey(AstralTech.instance, "laser_silenced"), PersistentDataType.INTEGER, 1)
    } else {
      container.set(NamespacedKey(AstralTech.instance, "laser_silenced"), PersistentDataType.INTEGER, 0)
    }
  }

  override fun tick() {
    super.tick()

    paneLoadingBar(inventory, (46..52).toList(), buffer, maxBuffer, "Energy Buffer")
    optionSelector(inventory, listOf(10,19,28), laserDepth.toString(), listOf("5", "3", "1"), "Laser Depth")
    optionSelector(inventory, listOf(16,25,34), laserRange.toString(), listOf("32", "8", "1"), "Laser Range")
    displayToggle(inventory, silencerBox.getBox(), laserSilenced, "Silencer: On", "Silencer: Off")

    if (this.location.block.isBlockIndirectlyPowered && !wasPowered) {
      wasPowered = true
      if (this.buffer >= energyPerBlock) {
        // Fire laser
        fireLaser()
      }
    } else if (!this.location.block.isBlockIndirectlyPowered && wasPowered) {
      wasPowered = false
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    for (slot in 0..containerItemHandler.size) {
      containerItemHandler.getItem(slot)?.let {
        location.world?.dropItem(location, it)
        containerItemHandler.setItem(slot, null)
      }
    }
  }

  fun fireLaser() {
    val laserFace = getLaserFace() ?: return
    val laserLocation = this.location.clone().add(laserFace.direction.multiply(2))
    var layersMined = 0
    laserLoop@ for (i in 0 until laserRange) {
      var didLayer = false
      when (laserFace) {
        BlockFace.WEST, BlockFace.EAST -> {
          location.world?.spawnParticle(Particle.ELECTRIC_SPARK, laserLocation.clone().add(0.5, 0.5, 0.5), 10, 0.5, 0.0, 0.0, 0.0)
        }
        BlockFace.DOWN, BlockFace.UP -> {
          location.world?.spawnParticle(Particle.ELECTRIC_SPARK, laserLocation.clone().add(0.5, 0.5, 0.5), 10, 0.0, 0.5, 0.0, 0.0)
        }
        BlockFace.NORTH, BlockFace.SOUTH -> {
          location.world?.spawnParticle(Particle.ELECTRIC_SPARK, laserLocation.clone().add(0.5, 0.5, 0.5), 10, 0.0, 0.0, 0.5, 0.0)
        }
        else -> {}
      }
      for (j in -laserWidth..laserWidth) {
        for (k in -laserWidth..laserWidth) {
          val checkLocation = laserLocation.clone()
          when (laserFace) {
            BlockFace.WEST, BlockFace.EAST -> {
              checkLocation.add(0.0, j.toDouble(), k.toDouble())
            }
            BlockFace.DOWN, BlockFace.UP -> {
              checkLocation.add(j.toDouble(), 0.0, k.toDouble())
            }
            BlockFace.NORTH, BlockFace.SOUTH -> {
              checkLocation.add(j.toDouble(), k.toDouble(), 0.0)
            }
            else -> {}
          }
          val checkBlock = checkLocation.block
          if (checkBlock.type != Material.AIR) {
            if (checkBlock.state is Container) continue
            if (checkBlock.isLiquid) continue
            if (AstralItems.getInstance().getAstralBlock(checkBlock).isPresent) continue

            if (!didLayer) {
              didLayer = true
              layersMined++
            }
            if (this.buffer >= energyPerBlock) {
              val haveInventorySpace = checkBlock.getDrops(miningLaserTool).all {
                var canInsert = false
                for (slot in 0 until containerItemHandler.size) {
                  if (containerItemHandler.insertItem(slot, it, true) == null) {
                    canInsert = true
                    break
                  }
                }
                canInsert
              }
              if (haveInventorySpace) {
                buffer -= energyPerBlock
                checkBlock.getDrops(miningLaserTool).forEach {
                  containerItemHandler.insertItem(it)
                }
                checkBlock.type = Material.AIR
              } else {
                break@laserLoop
              }
            } else {
              break@laserLoop
            }
          }
        }
      }
      if (layersMined >= laserDepth) {
        break@laserLoop
      }
      laserLocation.add(laserFace.direction)
    }
    if (layersMined > 0 && !laserSilenced) {
      location.world?.playSound(location, Sound.ENTITY_LIGHTNING_BOLT_IMPACT, SoundCategory.BLOCKS, 1f, 2f)
    }
  }

  override fun onInteract(event: PlayerInteractEvent) {
    super.onInteract(event)

    if (event.action == Action.RIGHT_CLICK_BLOCK) {
      if (!event.player.isSneaking) {
        event.isCancelled = true
        event.player.openInventory(inventory)
      } else if (!event.isBlockInHand) {
        event.isCancelled = true
        event.player.openInventory(inventory)
      }
    }
  }

  override fun onInventoryInteract(event: InventoryClickEvent) {
    if (event.clickedInventory == inventory) {
      if ( event.slot !in outputBox) {
        event.isCancelled = true
      }
      if ( event.slot in depthSelectorBox && event.action == InventoryAction.PICKUP_ALL) {
        if (event.whoClicked is Player) {
          (event.whoClicked as Player).playSound(event.whoClicked.location, Sound.BLOCK_NOTE_BLOCK_HAT, SoundCategory.PLAYERS, 1f, 1f)
        }
        laserDepth = when (event.slot) {
          (1 + 1*9) -> {
            5
          }
          (1 + 2*9) -> {
            3
          }
          else -> {
            1
          }
        }
      } else if ( event.slot in rangeSelectorBox && event.action == InventoryAction.PICKUP_ALL) {
        if (event.whoClicked is Player) {
          (event.whoClicked as Player).playSound(event.whoClicked.location, Sound.BLOCK_NOTE_BLOCK_HAT, SoundCategory.PLAYERS, 1f, 1f)
        }
        laserRange = when (event.slot) {
          (7 + 1*9) -> {
            32
          }
          (7 + 2*9) -> {
            8
          }
          else -> {
            1
          }
        }
      } else if ( event.slot in silencerBox && event.action == InventoryAction.PICKUP_ALL) {
        if (event.whoClicked is Player) {
          (event.whoClicked as Player).playSound(event.whoClicked.location, Sound.BLOCK_NOTE_BLOCK_HAT, SoundCategory.PLAYERS, 1f, 1f)
        }
        laserSilenced = !laserSilenced
      }
    }
  }

  override fun onInventoryDrag(event: InventoryDragEvent) {
    for (slot in event.rawSlots) {
      val invSlot = event.view.convertSlot(slot)
      if (event.view.getInventory(slot)?.equals(inventory) == true && invSlot !in outputBox) {
        event.isCancelled = true
      }
    }
  }

  override fun getItemHandler(): ItemHandler {
    return containerItemHandler
  }

  override fun onItemsTransferred() {

  }
}
