package io.astralforge.astraltech.tile

import org.bukkit.Particle

class TestMachineTile: BufferedMachineTile(maxBuffer=1000L, maxChargeRate=10L) {
  private val powerPerOperation = 200L

  companion object : Builder {
    override fun build(): TestMachineTile {
      return TestMachineTile()
    }
  }

  override fun receivePower(rf: Long): Long {
    val received = super.receivePower(rf)


    if (super.buffer > powerPerOperation) {
      location.world?.spawnParticle(
          Particle.HEART,
          location,
          1
      )
      buffer -= powerPerOperation
    }

    return received
  }
}