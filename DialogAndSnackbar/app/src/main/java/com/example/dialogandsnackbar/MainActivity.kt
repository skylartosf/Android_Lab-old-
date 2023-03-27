package com.example.dialogandsnackbar

import android.app.AlertDialog
import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import com.example.dialogandsnackbar.databinding.ActivityMainBinding
import com.example.dialogandsnackbar.databinding.DialogCustomBinding
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Snackbar
        binding.btnSnackbar.setOnClickListener { view ->
            Snackbar.make(view, "You've clicked first button.", Snackbar.LENGTH_LONG).show()
        }

        // 2. Alert Dialog
        binding.btnAlertDialog.setOnClickListener { view ->
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

        // 3. Custom Dialog
        binding.btnCustomDialog.setOnClickListener { view ->
            val customDialog = Dialog(this)
            var dialBind: DialogCustomBinding = DialogCustomBinding.inflate(layoutInflater)
            customDialog.setContentView(dialBind.root)
            dialBind.tvSubmit.setOnClickListener(View.OnClickListener {
                // applicationContext 대신 this를 써도 돼,
                // if you're within closure 같은 상황에서는 applicationContext를 써야 돼
                // 그래서 it's safer to use applicationContext
                Toast.makeText(applicationContext, "You've just clicked Submit!", Toast.LENGTH_SHORT).show()
                customDialog.dismiss()
            })
            dialBind.tvCancel.setOnClickListener(View.OnClickListener {
                Toast.makeText(applicationContext, "You've just clicked Cancel!", Toast.LENGTH_SHORT).show()
                customDialog.dismiss()
            })

            customDialog.show()
        }

        // 4. Custom Progress Dialog
    }
}