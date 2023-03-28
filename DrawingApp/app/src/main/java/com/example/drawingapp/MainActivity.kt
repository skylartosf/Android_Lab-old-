package com.example.drawingapp

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.view.get
import codes.side.andcolorpicker.alpha.HSLAlphaColorPickerSeekBar
import codes.side.andcolorpicker.group.PickerGroup
import codes.side.andcolorpicker.group.registerPickers
import codes.side.andcolorpicker.hsl.HSLColorPickerSeekBar
import codes.side.andcolorpicker.model.IntegerHSLColor
import kotlin.math.roundToInt
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.provider.ContactsContract.CommonDataKinds.Im
import android.provider.MediaStore
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.app.ActivityCompat
import androidx.core.content.PackageManagerCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

    private var drawingView: DrawingView? = null
    private var mImageBtnCurPaint: ImageButton? = null

    private var ibPaintRandom: ImageButton? = null

    // (외부 저장소 접근 권한이 승인되면) 갤러리에 접근하고, 유저가 사진을 선택하면
    // 그 URI(내 저장소 내에서의 path/location)가 result.data.data에 담기는데
    // 그것으로 캔버스의 배경을 설정해준다
    // 이미지를 copy해서 가져오는 게 아니라, 기기 내 img를 그 위치를 가져와서 배경으로 설정해주는 식. (use it from there)
    val openGalleryLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                val imgBackground: ImageView = findViewById(R.id.iv_background)
                imgBackground.setImageURI(result.data?.data)
            }
        }

    // 접근 권한이 승인/거부됐는지 체크한다
    val reqPerm: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { // 나중에 여러 허가 요청할 걸 대비해서 미리 multiple
                perms ->
            perms.entries.forEach {
                val permName = it.key
                val isGranted = it.value

                if (isGranted) {
                    Toast.makeText(
                        this,
                        "Permission granted. You can read the storage files.",
                        Toast.LENGTH_SHORT
                    ).show()
                    // 외부 저장소 접근 권한이 설정됐으니, 갤러리에 접근 시도
                    val pickIntent =
                        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    openGalleryLauncher.launch(pickIntent)
                } else {
                    when (permName) {
                        // 이 Manifest는 자동으로 java.jar을 import하려 하는데, 지우고, android.Manifest를 import 해야 한다
                        Manifest.permission.READ_EXTERNAL_STORAGE ->
                            Toast.makeText(
                                this,
                                "Oops, you've just denied the permission.",
                                Toast.LENGTH_SHORT
                            ).show()
                    }
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        drawingView = findViewById(R.id.drawing_view)
        drawingView?.setSizeForBrush(20.toFloat())

        val llPaintColors = findViewById<LinearLayout>(R.id.ll_paint_colors)
        mImageBtnCurPaint = llPaintColors[1] as ImageButton
        mImageBtnCurPaint!!.setImageDrawable(
            ContextCompat.getDrawable(this, R.drawable.palette_pressed)
        )
        ibPaintRandom = findViewById(R.id.ib_paint_random)

        val ibBrush: ImageButton = findViewById(R.id.ib_brush)
        ibBrush.setOnClickListener {
            showBrushSizeChooserDialog()
        }

        val ibUndo: ImageButton = findViewById(R.id.ib_undo)
        ibUndo.setOnClickListener {
            drawingView?.onClickUndo()
        }

        val ibRedo: ImageButton = findViewById(R.id.ib_redo)
        ibRedo.setOnClickListener {
            drawingView?.onClickRedo()
        }

        val ibGallery: ImageButton = findViewById(R.id.ib_gallery)
        ibGallery.setOnClickListener {
            reqStoragePerm()
        }

        val ibSave: ImageButton = findViewById(R.id.ib_save)
        ibSave.setOnClickListener {
            if (isReadStorageAllowed()) {
                lifecycleScope.launch {
                    val flDrawingView: FrameLayout = findViewById(R.id.fl_drawing_view_container)
                    val myBitmap: Bitmap = getBitmapFromView(flDrawingView)
                    saveBitmapFile(myBitmap)
                }
            }
        }
    }

    private fun isReadStorageAllowed(): Boolean {
        var result = ContextCompat.checkSelfPermission(
            this, Manifest.permission.READ_EXTERNAL_STORAGE
        )
        return result == PackageManager.PERMISSION_GRANTED
    }

    private fun reqStoragePerm() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        ) {
            showRationaleDialog(
                "Drawing App",
                "Drawing App needs to access your external storage"
            )
        } else {
            reqPerm.launch(
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            )
        }
    }

    private fun showBrushSizeChooserDialog() {
        var brushDialog = Dialog(this)
        brushDialog.setContentView(R.layout.dialog_brush_size)
        brushDialog.setTitle("Bursh size: ")
        brushDialog.show()

        val smallBtn: ImageButton = brushDialog.findViewById(R.id.ib_small_brush)
        smallBtn.setOnClickListener {
            drawingView?.setSizeForBrush(10.toFloat())
            brushDialog.dismiss()
        }

        val mediumBtn: ImageButton = brushDialog.findViewById(R.id.ib_medium_brush)
        mediumBtn.setOnClickListener {
            drawingView?.setSizeForBrush(20.toFloat())
            brushDialog.dismiss()
        }

        val largeBtn: ImageButton = brushDialog.findViewById(R.id.ib_large_brush)
        largeBtn.setOnClickListener {
            drawingView?.setSizeForBrush(30.toFloat())
            brushDialog.dismiss()
        }
    }

    fun paintClicked(view: View) {
        if (view !== mImageBtnCurPaint) {
            val imgBtn = view as ImageButton
            imgBtn.setImageDrawable( // 방금 클릭한 imgBtn은 pressed 색깔로
                ContextCompat.getDrawable(this, R.drawable.palette_pressed)
            )
            mImageBtnCurPaint?.setImageDrawable( // 이전까지 클릭된 imgBtn은 normal 색깔로
                ContextCompat.getDrawable(this, R.drawable.palette_normal)
            )
            mImageBtnCurPaint = view

            if (view !== ibPaintRandom) {
                val colorTag = imgBtn.tag.toString() // colors.xml에 있는 #ff0000 이런 게 할당돼
                drawingView?.setColor(colorTag)
            }
        }

        if (view == ibPaintRandom) {
            var colorPickerDialog = Dialog(this)
            colorPickerDialog.setContentView(R.layout.dialog_color_picker)
            colorPickerDialog.setTitle("Picked color: ")
            colorPickerDialog.show()

            val hslHue: HSLColorPickerSeekBar = colorPickerDialog.findViewById(R.id.hsl_hue)
            val hslSat: HSLColorPickerSeekBar = colorPickerDialog.findViewById(R.id.hsl_saturation)
            val hslLight: HSLColorPickerSeekBar = colorPickerDialog.findViewById(R.id.hsl_lightness)
            val hslAlpha: HSLAlphaColorPickerSeekBar =
                colorPickerDialog.findViewById(R.id.hsl_alpha)

            val group = PickerGroup<IntegerHSLColor>().also {
                it.registerPickers(
                    hslHue,
                    hslSat,
                    hslLight,
                    hslAlpha
                )
            }

            val btnApply: Button = colorPickerDialog.findViewById(R.id.btn_apply)
            btnApply.setOnClickListener {

                val intColor = ColorUtils.HSLToColor(
                    floatArrayOf(
                        hslHue.pickedColor.floatH,
                        hslSat.pickedColor.floatS,
                        hslLight.pickedColor.floatL
                    )
                )

                val red = Color.red(intColor)
                val green = Color.green(intColor)
                val blue = Color.blue(intColor)
                val alpha = (hslAlpha.pickedColor.alpha * 100.0).roundToInt()
                val hex: String =
                    String.format("#%s%02x%02x%02x", AlphaToHex.alpha[alpha], red, green, blue)
                drawingView?.setColor(hex)

                colorPickerDialog.dismiss()
            }
        }
    }

    private fun showRationaleDialog(title: String, msg: String) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle(title)
            .setMessage(msg)
            .setPositiveButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
        builder.create().show()
    }

    private fun getBitmapFromView(view: View): Bitmap {
        val returnedBitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(returnedBitmap) // 이러면 향후 canvas에 그리는 모든 작업은 returnedBitmap에 반영된다
        val bgDrawable = view.background
        if (bgDrawable != null) { // 배경이 있으면 canvas에 배경을 그린다
            bgDrawable.draw(canvas)
        } else { // 배경이 없으면 canvas에 흰색으로 채운다
            canvas.drawColor(Color.WHITE)
        }

        view.draw(canvas) // the view is rendered to the canvas

        return returnedBitmap
    }

    private suspend fun saveBitmapFile(mBitmap: Bitmap?): String {
        var result = ""
        withContext(Dispatchers.IO) {
            if (mBitmap != null) {
                try {
                    val bytes = ByteArrayOutputStream()
                    mBitmap.compress(Bitmap.CompressFormat.PNG, 90, bytes)

                    // 첫번째: 우리 앱이 있는 위치 + 두번째: 우리 기기에서 이미지 저장할 위치
                    val f = File(
                        externalCacheDir?.absoluteFile.toString()
                                + File.separator + "DrawingApp_" +
                                System.currentTimeMillis() / 1000 + ".png"
                    )

                    val fo = FileOutputStream(f)
                    fo.write(bytes.toByteArray())
                    fo.close()

                    result = f.absolutePath

                    runOnUiThread {
                        if (result.isNotEmpty()) {
                            Toast.makeText(
                                this@MainActivity,
                                "File saved at $result",
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            Toast.makeText(
                                this@MainActivity,
                                "Something went wrong while saving the file",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
                catch (e: Exception) {
                    result = ""
                    e.printStackTrace()
                }
            }
        }

        return result
    }
}