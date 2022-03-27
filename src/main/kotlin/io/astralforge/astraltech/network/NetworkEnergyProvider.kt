package io.astralforge.astraltech.network

interface NetworkEnergyProvider {
  fun onOfferedPowerResults(amountRemaining: Long)
}