package com.example.drawingapp

import android.app.Dialog
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.view.get
import codes.side.andcolorpicker.alpha.HSLAlphaColorPickerSeekBar
import codes.side.andcolorpicker.group.PickerGroup
import codes.side.andcolorpicker.group.registerPickers
import codes.side.andcolorpicker.hsl.HSLColorPickerSeekBar
import codes.side.andcolorpicker.model.IntegerHSLColor
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

    private var drawingView: DrawingView? = null
    private var mImageBtnCurPaint: ImageButton? = null

    private var ibPaintRandom: ImageButton? = null

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
            val hslAlpha: HSLAlphaColorPickerSeekBar = colorPickerDialog.findViewById(R.id.hsl_alpha)

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
                val hex: String = String.format("#%s%02x%02x%02x", AlphaToHex.alpha[alpha], red, green, blue)
                drawingView?.setColor(hex)

                colorPickerDialog.dismiss()
            }
        }
    }

}