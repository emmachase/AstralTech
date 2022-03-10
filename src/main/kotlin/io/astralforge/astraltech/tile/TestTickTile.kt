package io.astralforge.astraltech.tile

import io.astralforge.astralitems.block.tile.AstralTileEntity
import io.astralforge.astralitems.block.tile.RandomTick
import org.bukkit.Particle

class TestTickTile : AstralTileEntity() {
  companion object : Builder {
    override fun build(): TestTickTile {
      return TestTickTile()
    }
  }

  @RandomTick(chance = 0.2)
  override fun tick() {
    location.world?.spawnParticle(
        Particle.HEART,
        location,
        10
    )
  }
}
