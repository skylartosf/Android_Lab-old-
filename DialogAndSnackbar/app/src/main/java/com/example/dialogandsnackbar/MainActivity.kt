package com.example.dialogandsnackbar

import android.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {

    var btnSnackbar: Button? = null
    var btnAlertDial: Button? = null
    var btnCustomDial: Button? = null
    var btnCustomProgressDial: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnSnackbar = findViewById(R.id.btn_snackbar)
        btnAlertDial = findViewById(R.id.btn_alert_dialog)
        btnCustomDial = findViewById(R.id.btn_custom_dialog)
        btnCustomProgressDial = findViewById(R.id.btn_custom_progress_dialog)

        btnSnackbar?.setOnClickListener { view ->
            Snackbar.make(view, "You've clicked first button.", Snackbar.LENGTH_LONG).show()
        }

        btnAlertDial?.setOnClickListener { view ->
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Alert")
            builder.setMessage("This is Alert Dialog which is used to show alerts in our app.")
            builder.setIcon(android.R.drawable.ic_dialog_alert)

            builder.setPositiveButton("Yes") { dialogInterface, which ->
                Toast.makeText(applicationContext, "You've just clicked Yes!", Toast.LENGTH_SHORT).show()
                dialogInterface.dismiss() // Dialog will be dismissed
            }

            builder.setNeutralButton("Cancel") { dialogInterface, which ->
                Toast.makeText(applicationContext, "You've just clicked Cancel!", Toast.LENGTH_SHORT).show()
                dialogInterface.dismiss() // Dialog will be dismissed
            }

            builder.setNegativeButton("No") { dialogInterface, which ->
                Toast.makeText(applicationContext, "You've just clicked No!", Toast.LENGTH_SHORT).show()
                dialogInterface.dismiss() // Dialog will be dismissed
            }

            // create AlertDialog
            val alertDialog: AlertDialog = builder.create()
            // set other dialog properties
            alertDialog.setCancelable(false) // user cannot ignore after clicking on remaining screen area
            alertDialog.show()
        }
    }
}