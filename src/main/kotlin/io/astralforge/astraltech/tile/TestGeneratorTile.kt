package io.astralforge.astraltech.tile

import io.astralforge.astraltech.network.NetworkNodeTile

class TestGeneratorTile: NetworkNodeTile() {
  companion object : Builder {
    override fun build(): TestGeneratorTile {
      return TestGeneratorTile()
    }
  }

  override fun tick() {
    super.tick()

    network?.providePower(this, 50)
  }
}