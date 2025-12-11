package com.titanbooster

import com.topjohnwu.superuser.Shell

object RootManager {
    
    private var isInitialized = false
    
    fun initialize() {
        if (!isInitialized) {
            Shell.enableVerboseLogging = true
            Shell.setDefaultBuilder(
                Shell.Builder.create()
                    .setFlags(Shell.FLAG_REDIRECT_STDERR)
                    .setTimeout(10)
            )
            isInitialized = true
        }
    }
    
    fun isRooted(): Boolean {
        return Shell.isAppGrantedRoot() == true
    }
    
    fun executeCommand(command: String): String? {
        if (!isRooted()) return null
        
        return try {
            val result = Shell.su(command).exec()
            if (result.isSuccess) {
                result.out.joinToString("\n")
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    fun cleanSystemCache(): Boolean {
        val commands = listOf(
            "rm -rf /data/dalvik-cache/*",
            "rm -rf /cache/*",
            "rm -rf /data/system/cache/*",
            "sync"
        )
        
        return executeCommands(commands) != null
    }
    
    fun freezeApp(packageName: String): Boolean {
        return executeCommand("pm disable $packageName") != null
    }
    
    fun unfreezeApp(packageName: String): Boolean {
        return executeCommand("pm enable $packageName") != null
    }
    
    fun setCPUFrequency(minFreq: String, maxFreq: String): Boolean {
        val commands = listOf(
            "echo $minFreq > /sys/devices/system/cpu/cpu0/cpufreq/scaling_min_freq",
            "echo $maxFreq > /sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq",
            "echo performance > /sys/devices/system/cpu/cpu0/cpufreq/scaling_governor"
        )
        
        return executeCommands(commands) != null
    }
    
    fun getCPUTemperature(): String? {
        return executeCommand("cat /sys/class/thermal/thermal_zone0/temp")?.let { tempStr ->
            val temp = tempStr.trim().toFloatOrNull() ?: return null
            "${temp / 1000}°C"
        }
    }
    
    private fun executeCommands(commands: List<String>): String? {
        if (!isRooted()) return null
        
        return try {
            val result = Shell.su(*commands.toTypedArray()).exec()
            if (result.isSuccess) {
                result.out.joinToString("\n")
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}