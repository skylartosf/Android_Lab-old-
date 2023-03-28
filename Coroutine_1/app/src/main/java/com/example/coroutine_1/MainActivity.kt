package com.example.coroutine_1

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.lifecycle.lifecycleScope

/* coroutine 사용법 !
- suspend func + withContext(그 func이 어디 thread에서 돌지)
- 어느 scope(시기)에서 실행? ViewModelScope/LifeCycleScope(/Livedata).launch { 그 func 호출 }
 */

class MainActivity : AppCompatActivity() {

    var customProgressDialog: Dialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnExec: Button = findViewById(R.id.btn_execute)
        btnExec.setOnClickListener {
            showProgressDialog()
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

            runOnUiThread {
                cancelProgressDialog()
                Toast.makeText(this@MainActivity, result, Toast.LENGTH_LONG).show()
            }
            Log.e("delay : ", "finally finished!")
            // why app crashes?
            // we're trying to run this Toast on our background thread
            // but Toast is a foreground or a UI task
            // so such an UI-related action can only be run on the UI thread
            // but we're running it on Dispatchers.IO thread
            // -> if we make it runOnUiThread, app will not crash
        }

    }

    private fun cancelProgressDialog() {
        if (customProgressDialog != null) {
            customProgressDialog?.dismiss()
            customProgressDialog = null
        }
    }

    private fun showProgressDialog() {
        customProgressDialog = Dialog(this@MainActivity)
        customProgressDialog?.setContentView(R.layout.dialog_custom_progress)
        customProgressDialog?.show()
    }

}