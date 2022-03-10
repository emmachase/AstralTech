package io.astralforge.astraltech

import io.astralforge.astralitems.AstralItemSpec
import io.astralforge.astralitems.block.AstralBasicBlockSpec
import io.astralforge.astraltech.tile.TestGeneratorTile
import io.astralforge.astraltech.tile.TestMachineTile
import io.astralforge.astraltech.tile.TestTickTile
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.plugin.java.JavaPlugin

class AstralTech: JavaPlugin() {
  override fun onEnable() {
    logger.info("AstralTech running!")

    val testMachine = AstralBasicBlockSpec.builder().itemSpec(AstralItemSpec.builder()
        .id(NamespacedKey(this, "test_machine"))
        .material(Material.DIAMOND_BLOCK)
        .displayName("Test Machine")
        .build()
    )
        .tileEntityBuilder(TestMachineTile)
        .build().register()

    val testGenerator = AstralBasicBlockSpec.builder().itemSpec(AstralItemSpec.builder()
        .id(NamespacedKey(this, "test_generator"))
        .material(Material.COAL_BLOCK)
        .displayName("Test Generator")
        .build()
    )
        .tileEntityBuilder(TestGeneratorTile)
        .build().register()

    val testTick = AstralBasicBlockSpec.builder().itemSpec(AstralItemSpec.builder()
        .id(NamespacedKey(this, "test_tick"))
        .material(Material.DIAMOND_BLOCK)
        .displayName("Test Ticker")
        .build()
    )
        .tileEntityBuilder(TestTickTile)
        .build().register()
  }
}
