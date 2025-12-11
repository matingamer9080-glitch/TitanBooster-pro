package com.titanbooster

import android.animation.ValueAnimator
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.github.mikephil.charting.data.Entry
import com.titanbooster.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var performanceManager: PerformanceManager
    private lateinit var systemInfoManager: SystemInfoManager
    
    private var isBoostActive = false
    private val handler = Handler(Looper.getMainLooper())
    private val updateInterval = 2000L
    private val scope = CoroutineScope(Dispatchers.Main)
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        initialize()
        setupUI()
        setupListeners()
        startLiveUpdates()
    }
    
    private fun initialize() {
        performanceManager = PerformanceManager(this)
        systemInfoManager = SystemInfoManager(this)
        RootManager.initialize()
    }
    
    private fun setupUI() {
        window.statusBarColor = Color.TRANSPARENT
        setupCpuChart()
        setupRamChart()
        updateSystemInfo()
        checkRootStatus()
        updateBoostButton()
    }
    
    private fun setupCpuChart() {
        with(binding.chartCpuUsage) {
            setTouchEnabled(false)
            setDragEnabled(false)
            setScaleEnabled(false)
            setPinchZoom(false)
            description.isEnabled = false
            legend.isEnabled = false
            xAxis.isEnabled = false
            axisLeft.isEnabled = false
            axisRight.isEnabled = false
            
            val dataSet = com.github.mikephil.charting.data.LineDataSet(ArrayList<Entry>(), "CPU").apply {
                color = Color.parseColor("#00FFAA")
                setDrawCircles(false)
                lineWidth = 3f
                setDrawFilled(true)
                fillColor = Color.parseColor("#2200FFAA")
            }
            
            data = com.github.mikephil.charting.data.LineData(dataSet)
        }
    }
    
    private fun setupRamChart() {
        with(binding.chartRamUsage) {
            setTouchEnabled(false)
            setDragEnabled(false)
            setScaleEnabled(false)
            setPinchZoom(false)
            description.isEnabled = false
            legend.isEnabled = false
            xAxis.isEnabled = false
            axisLeft.isEnabled = false
            axisRight.isEnabled = false
            
            val dataSet = com.github.mikephil.charting.data.LineDataSet(ArrayList<Entry>(), "RAM").apply {
                color = Color.parseColor("#FFAA00")
                setDrawCircles(false)
                lineWidth = 3f
                setDrawFilled(true)
                fillColor = Color.parseColor("#22FFAA00")
            }
            
            data = com.github.mikephil.charting.data.LineData(dataSet)
        }
    }
    
    private fun setupListeners() {
        binding.btnBoostMain.setOnClickListener { toggleBoost() }
        binding.cardCleanRam.setOnClickListener { cleanRAM() }
        binding.cardCleanCache.setOnClickListener { cleanCache() }
        binding.cardDeviceInfo.setOnClickListener { showDeviceInfo() }
        binding.cardGameMode.setOnClickListener { toggleGameMode() }
        binding.cardTemperature.setOnClickListener { showTemperature() }
        binding.btnSettings.setOnClickListener { rotateView(it) }
    }
    
    private fun toggleBoost() {
        isBoostActive = !isBoostActive
        
        if (isBoostActive) {
            performanceManager.startBoost()
            binding.animationBoost.playAnimation()
            binding.tvBoostStatus.text = "BOOST ACTIVE"
            binding.tvBoostStatus.setTextColor(Color.GREEN)
            pulseAnimation(binding.btnBoostMain, 1000L)
        } else {
            performanceManager.stopBoost()
            binding.animationBoost.pauseAnimation()
            binding.tvBoostStatus.text = "READY TO BOOST"
            binding.tvBoostStatus.setTextColor(Color.WHITE)
        }
        
        updateBoostButton()
    }
    
    private fun cleanRAM() {
        scope.launch {
            binding.animationCleaning.isVisible = true
            binding.animationCleaning.playAnimation()
            
            val cleaned = performanceManager.cleanRAM()
            
            Handler(Looper.getMainLooper()).postDelayed({
                binding.animationCleaning.pauseAnimation()
                binding.animationCleaning.isVisible = false
                
                Toast.makeText(
                    this@MainActivity,
                    "✅ $cleaned پردازش بسته شدند",
                    Toast.LENGTH_SHORT
                ).show()
                
                updateSystemInfo()
            }, 1000)
        }
    }
    
    private fun cleanCache() {
        if (RootManager.isRooted()) {
            scope.launch {
                binding.animationCleaning.isVisible = true
                binding.animationCleaning.playAnimation()
                
                val success = performanceManager.cleanSystemCache()
                
                Handler(Looper.getMainLooper()).postDelayed({
                    binding.animationCleaning.pauseAnimation()
                    binding.animationCleaning.isVisible = false
                    
                    if (success) {
                        Toast.makeText(
                            this@MainActivity,
                            "✅ کش سیستم پاک شد",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }, 1500)
            }
        } else {
            showRootRequiredDialog()
        }
    }
    
    private fun showDeviceInfo() {
        val info = """
            📱 مدل: ${android.os.Build.MODEL}
            🔢 اندروید: ${android.os.Build.VERSION.RELEASE}
            🏭 سازنده: ${android.os.Build.MANUFACTURER}
            🧠 CPU: ${android.os.Build.HARDWARE}
            🎮 GPU: ${android.os.Build.BOARD}
            💾 RAM: ${systemInfoManager.getTotalRAM()}GB
        """.trimIndent()
        
        android.app.AlertDialog.Builder(this)
            .setTitle("📊 مشخصات دستگاه")
            .setMessage(info)
            .setPositiveButton("متوجه شدم", null)
            .show()
    }
    
    private fun toggleGameMode() {
        val isGameMode = performanceManager.toggleGameMode()
        
        if (isGameMode) {
            binding.tvGameModeStatus.text = "ACTIVE"
            binding.tvGameModeStatus.setTextColor(Color.GREEN)
            binding.animationGameMode.playAnimation()
            Toast.makeText(this, "🎮 حالت بازی فعال شد", Toast.LENGTH_SHORT).show()
        } else {
            binding.tvGameModeStatus.text = "INACTIVE"
            binding.tvGameModeStatus.setTextColor(Color.RED)
            binding.animationGameMode.pauseAnimation()
        }
    }
    
    private fun showTemperature() {
        val temp = RootManager.getCPUTemperature() ?: "نامشخص"
        
        android.app.AlertDialog.Builder(this)
            .setTitle("🌡️ دمای CPU")
            .setMessage("دمای فعلی: $temp")
            .setPositiveButton("متوجه شدم", null)
            .show()
    }
    
    private fun updateSystemInfo() {
        scope.launch {
            val cpuUsage = systemInfoManager.getCpuUsage()
            val cpuTemp = RootManager.getCPUTemperature() ?: "نامشخص"
            
            binding.tvCpuUsage.text = String.format(Locale.US, "%.1f%%", cpuUsage)
            binding.tvCpuTemp.text = cpuTemp
            
            val ramUsage = systemInfoManager.getRamUsage()
            val totalRam = systemInfoManager.getTotalRAM()
            val freeRam = systemInfoManager.getFreeRAM()
            
            binding.tvRamUsage.text = String.format(Locale.US, "%.1f%%", ramUsage)
            binding.tvRamTotal.text = "${totalRam}GB"
            binding.tvRamFree.text = "${freeRam}GB"
            
            updateCharts(cpuUsage, ramUsage)
            updateProgressBar(binding.progressCpu, cpuUsage)
            updateProgressBar(binding.progressRam, ramUsage)
        }
    }
    
    private fun updateCharts(cpuUsage: Float, ramUsage: Float) {
        val cpuDataSet = binding.chartCpuUsage.data.getDataSetByIndex(0)
        val cpuEntries = cpuDataSet.values
        if (cpuEntries.size > 20) cpuEntries.removeAt(0)
        cpuEntries.add(Entry(cpuEntries.size.toFloat(), cpuUsage))
        cpuDataSet.notifyDataSetChanged()
        binding.chartCpuUsage.data.notifyDataChanged()
        binding.chartCpuUsage.invalidate()
        
        val ramDataSet = binding.chartRamUsage.data.getDataSetByIndex(0)
        val ramEntries = ramDataSet.values
        if (ramEntries.size > 20) ramEntries.removeAt(0)
        ramEntries.add(Entry(ramEntries.size.toFloat(), ramUsage))
        ramDataSet.notifyDataSetChanged()
        binding.chartRamUsage.data.notifyDataChanged()
        binding.chartRamUsage.invalidate()
    }
    
    private fun updateProgressBar(progressView: View, progress: Float) {
        ValueAnimator.ofFloat(0f, progress).apply {
            duration = 500
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { animator ->
                val value = animator.animatedValue as Float
                progressView.layoutParams.width = (value / 100 * 300).toInt()
                progressView.requestLayout()
            }
            start()
        }
    }
    
    private fun checkRootStatus() {
        val isRooted = RootManager.isRooted()
        binding.tvRootStatus.text = if (isRooted) "ROOTED ✓" else "UNROOTED ✗"
        binding.tvRootStatus.setTextColor(if (isRooted) Color.GREEN else Color.RED)
        
        binding.cardCleanCache.isEnabled = isRooted
        binding.cardCleanCache.alpha = if (isRooted) 1f else 0.5f
    }
    
    private fun updateBoostButton() {
        val color = if (isBoostActive) "#FF5555" else "#00BCD4"
        val text = if (isBoostActive) "STOP BOOST" else "BOOST NOW"
        
        binding.btnBoostMain.setCardBackgroundColor(Color.parseColor(color))
        binding.tvBoostButton.text = text
        
        if (isBoostActive) {
            binding.animationBoost.playAnimation()
        } else {
            binding.animationBoost.pauseAnimation()
        }
    }
    
    private fun showRootRequiredDialog() {
        android.app.AlertDialog.Builder(this)
            .setTitle("🔒 نیاز به دسترسی روت")
            .setMessage("این قابلیت نیازمند دسترسی Superuser است.\nدستگاه شما روت نیست یا دسترسی لازم داده نشده است.")
            .setPositiveButton("متوجه شدم", null)
            .show()
    }
    
    private fun pulseAnimation(view: View, duration: Long = 500L) {
        view.animate()
            .scaleX(0.9f)
            .scaleY(0.9f)
            .setDuration(duration / 2)
            .withEndAction {
                view.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(duration / 2)
                    .start()
            }
            .start()
    }
    
    private fun rotateView(view: View) {
        view.animate()
            .rotationBy(360f)
            .setDuration(500)
            .start()
    }
    
    private fun startLiveUpdates() {
        handler.post(object : Runnable {
            override fun run() {
                updateSystemInfo()
                handler.postDelayed(this, updateInterval)
            }
        })
    }
    
    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        performanceManager.stopBoost()
    }
}