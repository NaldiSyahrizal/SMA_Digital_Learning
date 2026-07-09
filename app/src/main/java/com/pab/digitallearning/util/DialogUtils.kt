package com.pab.digitallearning.util

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.google.android.material.button.MaterialButton
import com.pab.digitallearning.R
import org.json.JSONObject

object DialogUtils {

    fun showSuccessDialog(
        context: Context,
        title: String = "Berhasil",
        message: String,
        onOkClicked: (() -> Unit)? = null
    ) {
        val view = LayoutInflater.from(context).inflate(R.layout.layout_dialog_info, null)
        val dialog = AlertDialog.Builder(context)
            .setView(view)
            .setCancelable(false)
            .create()
            
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        view.findViewById<TextView>(R.id.tvDialogTitle).apply {
            text = title
            setTextColor(Color.parseColor("#2E7D32"))
        }
        view.findViewById<TextView>(R.id.tvDialogMessage).text = message
        
        // Success style
        val flIconContainer = view.findViewById<View>(R.id.flIconContainer)
        flIconContainer.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#E8F5E9"))

        val ivIcon = view.findViewById<ImageView>(R.id.ivDialogIcon)
        ivIcon.setImageResource(R.drawable.ic_check_circle)
        ivIcon.setColorFilter(Color.parseColor("#2E7D32"))

        val btnOk = view.findViewById<MaterialButton>(R.id.btnDialogOk)
        btnOk.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#2E7D32"))
        btnOk.setOnClickListener {
            dialog.dismiss()
            onOkClicked?.invoke()
        }

        dialog.show()
    }

    fun showErrorDialog(
        context: Context,
        title: String = "Gagal",
        errorBodyString: String? = null,
        fallbackMessage: String = "Terjadi kesalahan",
        onOkClicked: (() -> Unit)? = null
    ) {
        var finalMessage = fallbackMessage

        // Try to parse Laravel Validation Error
        if (!errorBodyString.isNullOrEmpty()) {
            try {
                val jsonObject = JSONObject(errorBodyString)
                if (jsonObject.has("errors")) {
                    val errorsObject = jsonObject.getJSONObject("errors")
                    val keys = errorsObject.keys()
                    val errorMessageList = mutableListOf<String>()
                    
                    while (keys.hasNext()) {
                        val key = keys.next()
                        val errorArray = errorsObject.getJSONArray(key)
                        for (i in 0 until errorArray.length()) {
                            errorMessageList.add(errorArray.getString(i))
                        }
                    }
                    
                    if (errorMessageList.isNotEmpty()) {
                        finalMessage = errorMessageList.joinToString("\n")
                    } else if (jsonObject.has("message")) {
                        finalMessage = jsonObject.getString("message")
                    }
                } else if (jsonObject.has("message")) {
                    finalMessage = jsonObject.getString("message")
                }
            } catch (e: Exception) {
                // Not a JSON or unexpected format, just use raw string if possible
                finalMessage = errorBodyString
            }
        }

        val view = LayoutInflater.from(context).inflate(R.layout.layout_dialog_info, null)
        val dialog = AlertDialog.Builder(context)
            .setView(view)
            .setCancelable(false)
            .create()
            
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        view.findViewById<TextView>(R.id.tvDialogTitle).apply {
            text = title
            setTextColor(Color.parseColor("#C62828"))
        }
        
        view.findViewById<TextView>(R.id.tvDialogMessage).text = finalMessage
        
        // Error style
        val flIconContainer = view.findViewById<View>(R.id.flIconContainer)
        flIconContainer.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#FFEBEE"))

        val ivIcon = view.findViewById<ImageView>(R.id.ivDialogIcon)
        ivIcon.setImageResource(R.drawable.ic_error_circle)
        ivIcon.setColorFilter(Color.parseColor("#C62828"))

        val btnOk = view.findViewById<MaterialButton>(R.id.btnDialogOk)
        btnOk.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#C62828"))
        btnOk.setOnClickListener {
            dialog.dismiss()
            onOkClicked?.invoke()
        }

        dialog.show()
    }
}
