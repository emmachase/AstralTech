package io.astralforge.astraltech

import io.astralforge.astralitems.AstralItemSpec
import io.astralforge.astralitems.AstralItems
import io.astralforge.astralitems.block.AstralBasicBlockSpec
import io.astralforge.astralitems.recipe.AstralRecipeChoice.MaterialChoice
import io.astralforge.astraltech.crafting.PulverizerRecipe
import io.astralforge.astraltech.tile.*
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.FurnaceRecipe
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin

class AstralTech: JavaPlugin() {
  companion object {
    val instance get() = Bukkit.getPluginManager().getPlugin("AstralTech")!!
  }

  override fun onEnable() {
    logger.info("AstralTech running!")

    server.pluginManager.registerEvents(InventoryListener, this)

    this.logger.info(Bukkit.getWorlds()[0].getBlockAt(20000, 16, 20000).chunk.isLoaded.toString())
    registerItemsAndRecipes()

  }

  fun registerItemsAndRecipes() {

    /*
    Blocks
    */
    val testMachine = AstralBasicBlockSpec.builder().itemSpec(AstralItemSpec.builder()
        .id(NamespacedKey(this, "test_machine"))
        .material(Material.DIAMOND_BLOCK)
        .displayName("Test Machine")
        .build()
    )
        .tileEntityBuilder(TestMachineTile)
        .build().register()

    val autoCraftingMachine = AstralBasicBlockSpec.builder().itemSpec(AstralItemSpec.builder()
        .id(NamespacedKey(this, "auto_crafting_machine"))
        .material(Material.OAK_PLANKS)
        .displayName("Auto Crafting Machine")
        .build()
    )
        .tileEntityBuilder(AutoCraftingMachineTile)
        .build().register()

    val creativeGenerator = AstralBasicBlockSpec.builder().itemSpec(AstralItemSpec.builder()
        .id(NamespacedKey(this, "creative_generator"))
        .material(Material.COAL_BLOCK)
        .displayName("Creative Generator")
        .build()
    )
        .tileEntityBuilder(CreativeGeneratorTile)
        .build().register()

    val steamGenerator = AstralBasicBlockSpec.builder().itemSpec(AstralItemSpec.builder()
        .id(NamespacedKey(this, "steam_generator"))
        .material(Material.COAL_BLOCK)
        .displayName("Steam Generator")
        .build()
    )
        .tileEntityBuilder(SteamGeneratorTile)
        .build().register()

    val thermoelectricGenerator = AstralBasicBlockSpec.builder().itemSpec(AstralItemSpec.builder()
        .id(NamespacedKey(this, "thermoelectric_generator"))
        .material(Material.GILDED_BLACKSTONE)
        .displayName("Thermoelectric Generator")
        .build()
    )
        .tileEntityBuilder(ThermoelectricGeneratorTile)
        .build().register()

    val solarPanel = AstralBasicBlockSpec.builder().itemSpec(AstralItemSpec.builder()
        .id(NamespacedKey(this, "solar_panel"))
        .material(Material.DAYLIGHT_DETECTOR)
        .displayName("Solar Panel")
        .build()
    )
        .tileEntityBuilder(SolarPanelTile)
        .build().register()

    val pulverizer = AstralBasicBlockSpec.builder().itemSpec(AstralItemSpec.builder()
        .id(NamespacedKey(this, "pulverizer"))
        .material(Material.SMOOTH_STONE)
        .displayName("Pulverizer")
        .build()
    )
        .tileEntityBuilder(PulverizerMachineTile)
        .build().register()

    val electricFurnace = AstralBasicBlockSpec.builder().itemSpec(AstralItemSpec.builder()
        .id(NamespacedKey(this, "electric_furnace"))
        .material(Material.DEEPSLATE_TILES)
        .displayName("Electric Furnace")
        .build()
    )
        .tileEntityBuilder(ElectricFurnaceMachineTile)
        .build().register()

    val testTick = AstralBasicBlockSpec.builder().itemSpec(AstralItemSpec.builder()
        .id(NamespacedKey(this, "test_tick"))
        .material(Material.DIAMOND_BLOCK)
        .displayName("Test Ticker")
        .build()
    )
        .tileEntityBuilder(TestTickTile)
        .build().register()

    val extractor = AstralBasicBlockSpec.builder().itemSpec(AstralItemSpec.builder()
        .id(NamespacedKey(this, "extractor"))
        .material(Material.DROPPER)
        .displayName("Extractor")
        .build()
    )
        .tileEntityBuilder(ExtractorTile)
        .build().register()

    val inserter = AstralBasicBlockSpec.builder().itemSpec(AstralItemSpec.builder()
        .id(NamespacedKey(this, "inserter"))
        .material(Material.DROPPER)
        .displayName("Inserter")
        .build()
    )
        .tileEntityBuilder(InserterTile)
        .build().register()

    val itemCollector = AstralBasicBlockSpec.builder().itemSpec(AstralItemSpec.builder()
        .id(NamespacedKey(this, "item_collector"))
        .material(Material.CHISELED_DEEPSLATE)
        .displayName("Item Collector")
        .build()
    )
        .tileEntityBuilder(ItemCollectorTile)
        .build().register()

    val itemVoider = AstralBasicBlockSpec.builder().itemSpec(AstralItemSpec.builder()
        .id(NamespacedKey(this, "item_voider"))
        .material(Material.CRACKED_DEEPSLATE_BRICKS)
        .displayName("Item Voider")
        .build()
    )
        .tileEntityBuilder(ItemVoiderTile)
        .build().register()

    val miningLaser = AstralBasicBlockSpec.builder().itemSpec(AstralItemSpec.builder()
        .id(NamespacedKey(this, "mining_laser"))
        .material(Material.PRISMARINE_BRICKS)
        .displayName("Mining Laser")
        .build()
    )
        .tileEntityBuilder(MiningLaserMachineTile)
        .build().register()

    /*
    Items
    */
    val ironDust = AstralItemSpec.builder()
        .id(NamespacedKey(this, "iron_dust"))
        .material(Material.GUNPOWDER)
        .displayName("Iron Dust")
        .build()
    ironDust.register()

    val goldDust = AstralItemSpec.builder()
        .id(NamespacedKey(this, "gold_dust"))
        .material(Material.GLOWSTONE_DUST)
        .displayName("Gold Dust")
        .build()
    goldDust.register()

    val copperDust = AstralItemSpec.builder()
        .id(NamespacedKey(this, "copper_dust"))
        .material(Material.REDSTONE)
        .displayName("Copper Dust")
        .build()
    copperDust.register()

    /*
    Recipes
     */

    AstralItems.getInstance().recipeEvaluator.registerNonVanillaRecipe(PulverizerRecipe(
        NamespacedKey(this, "pulverize_iron"),
        MaterialChoice(Material.RAW_IRON),
        ironDust.createItemStack(2)
    ))

    AstralItems.getInstance().recipeEvaluator.registerNonVanillaRecipe(PulverizerRecipe(
        NamespacedKey(this, "pulverize_gold"),
        MaterialChoice(Material.RAW_GOLD),
        goldDust.createItemStack(2)
    ))

    AstralItems.getInstance().recipeEvaluator.registerNonVanillaRecipe(PulverizerRecipe(
        NamespacedKey(this, "pulverize_copper"),
        MaterialChoice(Material.RAW_COPPER),
        copperDust.createItemStack(2)
    ))

    AstralItems.getInstance().recipeEvaluator.registerNonVanillaRecipe(PulverizerRecipe(
        NamespacedKey(this, "pulverize_stone"),
        MaterialChoice(Material.STONE),
        ItemStack(Material.COBBLESTONE)
    ))


    AstralItems.getInstance().recipeEvaluator.registerNonVanillaRecipe(PulverizerRecipe(
        NamespacedKey(this, "pulverize_cobble"),
        MaterialChoice(Material.COBBLESTONE),
        ItemStack(Material.GRAVEL)
    ))

    AstralItems.getInstance().recipeEvaluator.registerNonVanillaRecipe(PulverizerRecipe(
        NamespacedKey(this, "pulverize_gravel"),
        MaterialChoice(Material.GRAVEL),
        ItemStack(Material.SAND)
    ))

    AstralItems.getInstance().recipeEvaluator.registerNonVanillaRecipe(PulverizerRecipe(
        NamespacedKey(this, "pulverize_sandstone"),
        MaterialChoice(Material.SANDSTONE),
        ItemStack(Material.SAND, 2)
    ))

    AstralItems.getInstance().recipeEvaluator.registerRecipe(FurnaceRecipe(
        NamespacedKey(this, "smelt_iron_dust"),
        ItemStack(Material.IRON_INGOT),
        MaterialChoice(ironDust),
        0.35f,
        200
    ))

    AstralItems.getInstance().recipeEvaluator.registerRecipe(FurnaceRecipe(
        NamespacedKey(this, "smelt_gold_dust"),
        ItemStack(Material.GOLD_INGOT),
        MaterialChoice(goldDust),
        0.35f,
        200
    ))

    AstralItems.getInstance().recipeEvaluator.registerRecipe(FurnaceRecipe(
        NamespacedKey(this, "smelt_copper_dust"),
        ItemStack(Material.COPPER_INGOT),
        MaterialChoice(copperDust),
        0.35f,
        200
    ))
  }
}
