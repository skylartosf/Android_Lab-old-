package com.example.permission

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    // ask for only 1 permission
    private val cameraResultLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                Toast.makeText(this, "Permission granted for camera", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permission denied for camera", Toast.LENGTH_SHORT).show()
            }
        }

    // ask for 2 permissions
    private val cameraAndLocationResultLauncher: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) {
            permissions ->
            permissions.entries.forEach {
                val permissionName = it.key
                val isGranted = it.value
                if (isGranted) {
                    when (permissionName) {
                        Manifest.permission.ACCESS_FINE_LOCATION ->
                            Toast.makeText(this, "Permission granted for Fine Location", Toast.LENGTH_SHORT).show()
                        Manifest.permission.ACCESS_COARSE_LOCATION ->
                            Toast.makeText(this, "Permission granted for Coarse Location", Toast.LENGTH_SHORT).show()
                        else ->
                            Toast.makeText(this, "Permission granted for Camera", Toast.LENGTH_SHORT).show()
                    }
                } else { // not granted
                    when (permissionName) {
                        Manifest.permission.ACCESS_FINE_LOCATION ->
                            Toast.makeText(this, "Permission denied for Fine Location", Toast.LENGTH_SHORT).show()
                        Manifest.permission.ACCESS_COARSE_LOCATION ->
                            Toast.makeText(this, "Permission denied for Coarse Location", Toast.LENGTH_SHORT).show()
                        else ->
                            Toast.makeText(this, "Permission denied for Camera", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnCamPermission: Button = findViewById(R.id.btn_camera_permission)
        btnCamPermission.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)
            ) {
                //TODO: showRationaleDialog() not working
            } else { // ask for permission
                //cameraResultLauncher.launch(Manifest.permission.CAMERA)
                cameraAndLocationResultLauncher.launch(
                    arrayOf(Manifest.permission.CAMERA,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }

    }
}