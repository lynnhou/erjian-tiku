package com.erjian.tiku

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.erjian.tiku.ui.AppNavHost
import com.erjian.tiku.ui.ExamViewModel
import com.erjian.tiku.ui.theme.ErJianTiKuTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ErJianTiKuTheme {
                val app = application as TiKuApp
                val examVm: ExamViewModel = viewModel(
                    factory = ExamViewModel.Factory(application, app.userStore),
                )
                AppNavHost(examVm)
            }
        }
    }
}
