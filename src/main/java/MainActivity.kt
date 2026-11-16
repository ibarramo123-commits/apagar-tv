package com.apagartv

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import dadb.AdbKeyPair
import dadb.Dadb
import java.io.File

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val status = TextView(this).apply {
            textSize = 24f
            gravity = Gravity.CENTER
            text = "Presiona el botón para apagar la TV"
        }
        val button = Button(this).apply {
            textSize = 28f
            text = "APAGAR TV"
        }
        setContentView(LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(80, 80, 80, 80)
            addView(status)
            addView(button)
        })
        button.requestFocus()

        button.setOnClickListener {
            button.isEnabled = false
            status.text = "Conectando… (la primera vez, acepta el mensaje en la tele)"
            Thread {
                var conectado = false
                try {
                    val priv = File(filesDir, "adbkey")
                    val pub = File(filesDir, "adbkey.pub")
                    if (!priv.exists()) AdbKeyPair.generate(priv, pub)
                    val keys = AdbKeyPair.read(priv, pub)
                    Dadb.create("127.0.0.1", 5555, keys).use { adb ->
                        conectado = true
                        runOnUiThread { status.text = "Apagando…" }
                        adb.shell("reboot -p")
                    }
                } catch (e: Exception) {
                    if (!conectado) runOnUiThread {
                        status.text = "No se pudo conectar: ${e.message}"
