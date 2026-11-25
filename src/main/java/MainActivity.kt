package com.apagartv

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import dadb.AdbKeyPair
import dadb.Dadb
import java.io.File

class MainActivity : Activity() {

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    private fun pill(borde: Int, grosor: Int) = GradientDrawable(
        GradientDrawable.Orientation.TOP_BOTTOM,
        intArrayOf(Color.parseColor("#2F6BFF"), Color.parseColor("#1236C8"))
    ).apply {
        cornerRadius = dp(60).toFloat()
        setStroke(dp(grosor), borde)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val fondo = GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            intArrayOf(
                Color.parseColor("#0A1A5C"),
                Color.parseColor("#020617"),
                Color.parseColor("#3B1D8F")
            )
        )

        val icono = ImageView(this).apply {
            setImageResource(android.R.drawable.ic_lock_power_off)
            setColorFilter(Color.WHITE)
            setPadding(dp(30), dp(30), dp(30), dp(30))
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.parseColor("#0B1F6E"))
                setStroke(dp(4), Color.parseColor("#3FA9FF"))
            }
        }

        val estado = TextView(this).apply {
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 28f)
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            text = "Presiona el botón para apagar la TV"
        }

        val boton = Button(this).apply {
            text = "APAGAR TV"
            setTextColor(Color.WHITE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 28f)
            background = pill(Color.parseColor("#7CC4FF"), 3)
            setOnFocusChangeListener { _, enfocado ->
                background = if (enfocado) pill(Color.WHITE, 6)
                else pill(Color.parseColor("#7CC4FF"), 3)
            }
        }

        val raiz = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            background = fondo
            setPadding(dp(48), dp(48), dp(48), dp(48))
            addView(icono, LinearLayout.LayoutParams(dp(140), dp(140)))
            addView(estado, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(32)
                bottomMargin = dp(32)
            })
            addView(boton, LinearLayout.LayoutParams(dp(520), dp(72)))
        }
        setContentView(raiz)
        boton.requestFocus()

        boton.setOnClickListener {
            boton.isEnabled = false
            estado.text = "Conectando… (la primera vez, acepta el mensaje en la tele)"
            Thread {
                var conectado = false
                try {
                    val priv = File(filesDir, "adbkey")
                    val pub = File(filesDir, "adbkey.pub")
                    if (!priv.exists()) AdbKeyPair.generate(priv, pub)
                    val keys = AdbKeyPair.read(priv, pub)
                    Dadb.create("127.0.0.1", 5555, keys).use { adb ->
                        conectado = true
                        runOnUiThread { estado.text = "Apagando…" }
                        adb.shell("reboot -p")
                    }
                } catch (e: Exception) {
                    if (!conectado) runOnUiThread {
                        estado.text = "No se pudo conectar: ${e.message}"
                        boton.isEnabled = true
                    }
                }
            }.start()
        }
    }
}
