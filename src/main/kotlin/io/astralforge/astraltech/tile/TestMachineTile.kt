package io.astralforge.astraltech.tile

import io.astralforge.astraltech.network.NetworkNodeTile
import org.bukkit.Particle

interface Powerable {
  fun receivePower(complete: Boolean, rf: Int)
}

class TestMachineTile: NetworkNodeTile(), Powerable {
  companion object : Builder {
    override fun build(): TestMachineTile {
      return TestMachineTile()
    }
  }

  override fun receivePower(complete: Boolean, rf: Int) {
    if (complete) {
      location.world?.spawnParticle(
          Particle.REDSTONE,
          location,
          10
      )
    }
  }

  override fun tick() {
    super.tick()

    network?.requestPower(this, 10)
  }
}
