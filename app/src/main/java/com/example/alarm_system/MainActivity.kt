package com.example.alarm_system

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.alarm_system.navigation.NavGraph
import com.example.alarm_system.core.alarm.data.service.AlarmService
import com.example.alarm_system.core.common.AlarmConstants
import com.example.alarm_system.features.alarm_ringing.AlarmRingingScreen
import com.example.alarm_system.features.alarm_ringing.AlarmRingingViewModel
import com.example.alarm_system.ui.components.GlassPillNav
import com.example.alarm_system.ui.theme.Alarm_systemTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val ringingViewModel: AlarmRingingViewModel by viewModels()
    private var isRingingState by mutableStateOf(false)
    private var navigationEvent by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val prefs = getSharedPreferences(AlarmConstants.PREFS_NAME, Context.MODE_PRIVATE)
        val onboardingCompleted = prefs.getBoolean(AlarmConstants.PREF_ONBOARDING_COMPLETED, false)
        val startDestination = if (onboardingCompleted) "alarm_list" else "onboarding"

        // Handle intent immediately to set flags if ringing
        val isRinging = intent.getBooleanExtra("is_ringing", false)
        if (isRinging) {
            showOverLockScreen()
            isRingingState = true
        }
        
        handleIntent(intent)
        enableEdgeToEdge()
        
        setContent {
            Alarm_systemTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val alarm by ringingViewModel.alarm.collectAsState()
                    
                    // Use a local variable to ensure consistency during a single composition pass
                    val currentlyRinging = isRingingState
                    
                    if (currentlyRinging) {
                        AlarmRingingScreen(
                            alarm = alarm,
                            onSnooze = {
                                ringingViewModel.snooze()
                                stopAlarmService()
                                finish()
                            },
                            onDismiss = {
                                ringingViewModel.dismiss()
                                stopAlarmService()
                                finish()
                            }
                        )
                    } else {
                        val navController = rememberNavController()
                        val navBackStackEntry by navController.currentBackStackEntryAsState()
                        val currentDestination = navBackStackEntry?.destination?.route

                        LaunchedEffect(navigationEvent) {
                            navigationEvent?.let { route ->
                                navController.navigate(route)
                                navigationEvent = null
                            }
                        }

                        Box(modifier = Modifier.fillMaxSize()) {
                            NavGraph(
                                navController = navController,
                                startDestination = startDestination
                            )
                            
                            // Only show Pill Nav on main screens (not Edit or Onboarding)
                            if (currentDestination != "alarm_edit/{alarmId}" && currentDestination != "onboarding") {
                                val selectedIndex = when (currentDestination) {
                                    "alarm_list" -> 0
                                    "analytics" -> 1
                                    "timer" -> 2
                                    "alarm_edit/-1" -> 3
                                    "stopwatch" -> 4
                                    else -> 0
                                }
                                
                                GlassPillNav(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .navigationBarsPadding()
                                        .padding(bottom = 10.dp),
                                    selectedIndex = selectedIndex,
                                    onItemSelected = { index ->
                                        when (index) {
                                            0 -> if (currentDestination != "alarm_list") {
                                                navController.navigate("alarm_list") {
                                                    popUpTo("alarm_list") { inclusive = true }
                                                }
                                            }
                                            1 -> if (currentDestination != "analytics") navController.navigate("analytics")
                                            2 -> if (currentDestination != "timer") navController.navigate("timer")
                                            3 -> navController.navigate("alarm_edit/-1")
                                            4 -> if (currentDestination != "stopwatch") navController.navigate("stopwatch")
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        val isRinging = intent.getBooleanExtra("is_ringing", false)
        val alarmId = intent.getIntExtra(AlarmConstants.EXTRA_ALARM_ID, -1)
        val navigateTo = intent.getStringExtra("navigate_to")

        isRingingState = isRinging

        if (isRinging && alarmId != -1) {
            showOverLockScreen()
            ringingViewModel.loadAlarm(alarmId)
        }

        if (navigateTo != null) {
            navigationEvent = navigateTo
        }
    }

    private fun showOverLockScreen() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }
        
        val keyguardManager = getSystemService(android.content.Context.KEYGUARD_SERVICE) as android.app.KeyguardManager
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            keyguardManager.requestDismissKeyguard(this, null)
        }
        
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )
    }

    private fun stopAlarmService() {
        val stopIntent = Intent(this, AlarmService::class.java).apply {
            action = "STOP_ALARM_ACTION" // dummy action to ensure service is reached
        }
        stopService(stopIntent)
    }
}
