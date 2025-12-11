package com.titanbooster

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.StatFs
import java.io.File

class SystemInfoManager(private val context: Context) {
    
    private val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    
    fun getCpuUsage(): Float {
        return try {
            val cpuStatFile = File("/proc/stat")
            if (cpuStatFile.exists()) {
                val lines = cpuStatFile.readText().split("\n")
                val cpuLine = lines.first { it.startsWith("cpu ") }
                val values = cpuLine.split(" ").filter { it.isNotEmpty() }
                
                val user = values[1].toLong()
                val nice = values[2].toLong()
                val system = values[3].toLong()
                val idle = values[4].toLong()
                
                val total = user + nice + system + idle
                val used = total - idle
                
                (used.toFloat() / total.toFloat() * 100)
            } else {
                0f
            }
        } catch (e: Exception) {
            0f
        }
    }
    
    fun getCpuTemperature(): Float {
        return try {
            val tempFile = File("/sys/class/thermal/thermal_zone0/temp")
            if (tempFile.exists()) {
                val temp = tempFile.readText().trim().toFloatOrNull() ?: 0f
                temp / 1000
            } else {
                0f
            }
        } catch (e: Exception) {
            0f
        }
    }
    
    fun getCpuFrequency(): Float {
        return try {
            val freqFile = File("/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq")
            if (freqFile.exists()) {
                val freq = freqFile.readText().trim().toFloatOrNull() ?: 0f
                freq / 1000000
            } else {
                0f
            }
        } catch (e: Exception) {
            0f
        }
    }
    
    fun getRamUsage(): Float {
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        
        val total = memoryInfo.totalMem
        val available = memoryInfo.availMem
        val used = total - available
        
        return (used.toFloat() / total.toFloat() * 100)
    }
    
    fun getTotalRAM(): Float {
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        return (memoryInfo.totalMem / (1024.0 * 1024.0 * 1024.0)).toFloat()
    }
    
    fun getFreeRAM(): Float {
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        return (memoryInfo.availMem / (1024.0 * 1024.0 * 1024.0)).toFloat()
    }
    
    fun getBatteryTemperature(): Float {
        return try {
            val tempFile = File("/sys/class/power_supply/battery/temp")
            if (tempFile.exists()) {
                val temp = tempFile.readText().trim().toFloatOrNull() ?: 0f
                temp / 10
            } else {
                0f
            }
        } catch (e: Exception) {
            0f
        }
    }
    
    fun getStorageInfo(): Pair<Float, Float> {
        val stat = StatFs(context.filesDir.path)
        val totalBytes = stat.totalBytes.toFloat()
        val freeBytes = stat.availableBytes.toFloat()
        val usedBytes = totalBytes - freeBytes
        
        val totalGB = totalBytes / (1024 * 1024 * 1024)
        val usedGB = usedBytes / (1024 * 1024 * 1024)
        
        return Pair(usedGB, totalGB)
    }
    
    fun getDeviceModel(): String {
        return Build.MODEL
    }
    
    fun getAndroidVersion(): String {
        return Build.VERSION.RELEASE
    }
    
    fun getManufacturer(): String {
        return Build.MANUFACTURER
    }
    
    fun getCpuModel(): String {
        return Build.HARDWARE
    }
    
    fun getCoresCount(): Int {
        return Runtime.getRuntime().availableProcessors()
    }
}