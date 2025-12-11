package com.titanbooster

import android.app.ActivityManager
import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PerformanceManager(private val context: Context) {
    
    private val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    private var isGameModeActive = false
    
    suspend fun cleanRAM(): Int = withContext(Dispatchers.IO) {
        val processes = activityManager.runningAppProcesses ?: return@withContext 0
        
        var cleanedCount = 0
        for (process in processes) {
            if (process.importance > ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE) {
                android.os.Process.killProcess(process.pid)
                cleanedCount++
            }
        }
        
        cleanedCount
    }
    
    fun cleanSystemCache(): Boolean {
        return RootManager.cleanSystemCache()
    }
    
    fun toggleGameMode(): Boolean {
        isGameModeActive = !isGameModeActive
        
        if (isGameModeActive) {
            // Enable game mode optimizations
            RootManager.setCPUFrequency("1800000", "2500000")
            closeBackgroundApps()
            disableAnimations()
        } else {
            // Restore normal mode
            RootManager.setCPUFrequency("300000", "1800000")
            enableAnimations()
        }
        
        return isGameModeActive
    }
    
    private fun closeBackgroundApps() {
        val packageManager = context.packageManager
        val packages = packageManager.getInstalledPackages(0)
        
        packages.forEach { packageInfo ->
            val packageName = packageInfo.packageName
            if (!isSystemApp(packageName)) {
                activityManager.killBackgroundProcesses(packageName)
            }
        }
    }
    
    private fun isSystemApp(packageName: String): Boolean {
        return packageName.startsWith("com.android") ||
               packageName.startsWith("com.google") ||
               packageName.contains("titanbooster")
    }
    
    private fun disableAnimations() {
        RootManager.executeCommand(
            "settings put global window_animation_scale 0\n" +
            "settings put global transition_animation_scale 0\n" +
            "settings put global animator_duration_scale 0"
        )
    }
    
    private fun enableAnimations() {
        RootManager.executeCommand(
            "settings put global window_animation_scale 1\n" +
            "settings put global transition_animation_scale 1\n" +
            "settings put global animator_duration_scale 1"
        )
    }
    
    fun startBoost() {
        // Start boost operations
        kotlinx.coroutines.GlobalScope.launch {
            cleanRAM()
            
            if (RootManager.isRooted()) {
                RootManager.cleanSystemCache()
                RootManager.setCPUFrequency("1800000", "2300000")
            }
        }
    }
    
    fun stopBoost() {
        if (RootManager.isRooted()) {
            RootManager.setCPUFrequency("300000", "1800000")
        }
    }
    
    fun getRunningProcesses(): Int {
        return activityManager.runningAppProcesses?.size ?: 0
    }
}