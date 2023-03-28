package com.example.coroutine_1

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnExec: Button = findViewById(R.id.btn_execute)
        btnExec.setOnClickListener {
            // You can't exec a suspend func on the main thread
            // exec("Task executed successfully!")
            // lifecycleScope : coroutine block built to properly handle coroutines in any Activity class
            lifecycleScope.launch {
                exec("Task executed successfully!")
            }
        }
    }

    // To stop exec() from blocking the UI thread
    // we need to add suspend in front of the function
    // and it should run on a different thread
    // suspend func is part of the coroutine class
    // and it has to invoke another suspend function
    private suspend fun exec(result: String) {
        // withContext() is to move an operation into a diff thread(=Dispatchers.IO=I/O thread)
        // until it completes its process, not blocking UI,
        // and then it is moved back into the original thread(UI thread)
        withContext(Dispatchers.IO) {
            for (i in 1..100000) {
                Log.e("delay : ", "" + i)
            }
            Toast.makeText(this@MainActivity, result, Toast.LENGTH_SHORT).show()
            // why app crashes?
            // we're trying to run this Toast on our background thread
            // but Toast is a foreground or a UI task
            // so such an UI-related action can only be run on the UI thread
            // but we're running it on Dispatchers.IO thread
        }


    }

}