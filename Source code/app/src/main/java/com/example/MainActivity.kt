package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.AppDatabase
import com.example.data.FileRepository
import com.example.ui.FileFlowApp
import com.example.ui.FileViewModel
import com.example.ui.FileViewModelFactory
import com.example.ui.theme.FileFlowTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val database = AppDatabase.getDatabase(this)
        val repository = FileRepository(database.fileDao())
        
        setContent {
            FileFlowTheme {
                val viewModel: FileViewModel = viewModel(factory = FileViewModelFactory(repository))
                FileFlowApp(viewModel)
            }
        }
    }
}
