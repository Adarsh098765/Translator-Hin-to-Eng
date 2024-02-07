package com.demo.translatehintoeng

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.demo.translatehintoeng.databinding.ActivityMainBinding
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import java.util.Locale

class MainActivity : AppCompatActivity(),View.OnClickListener {
    private lateinit var binding: ActivityMainBinding
    private var englishHindiTranslator: Translator? = null
    private var hindiEnglishTranslator: Translator? = null
    private var clipboard: ClipboardManager? = null
    private var clip: ClipData? = null
    var flag = true
    private var dialog: Dialog? = null
    private var options_2: TranslatorOptions? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        // Set up click listeners
        binding.mic.setOnClickListener(this)
        binding.swap.setOnClickListener(this)
        binding.cp1.setOnClickListener(this)
        binding.cp2.setOnClickListener(this)

        clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        dialog = Dialog(this@MainActivity, android.R.style.Theme_Dialog)
        open_dialog()
        binding.et1.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

                // TODO Auto-generated method stub
                if (flag) translate_hin(binding.et1.text.toString()) else translate_eng(
                    binding.et1.text.toString()
                )
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

                // TODO Auto-generated method stub
            }

            override fun afterTextChanged(s: Editable) {

                // TODO Auto-generated method stub
            }
        })

        // Create an English-Hindi translator:
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.ENGLISH)
            .setTargetLanguage(TranslateLanguage.HINDI)
            .build()
        options_2 = TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.HINDI)
            .setTargetLanguage(TranslateLanguage.ENGLISH)
            .build()
        englishHindiTranslator = Translation.getClient(options)
        val conditions = DownloadConditions.Builder()
            .requireWifi()
            .build()
        englishHindiTranslator!!.downloadModelIfNeeded(conditions)
            .addOnSuccessListener { // Model downloaded successfully. Okay to start translating.
                download_hin_eng()
            }
            .addOnFailureListener { e -> // Model couldn’t be downloaded or other internal error.
                binding.txt.text = e.message
            }
    }

    private fun download_hin_eng() {
        hindiEnglishTranslator = Translation.getClient(options_2!!)
        hindiEnglishTranslator?.downloadModelIfNeeded()
            ?.addOnSuccessListener { // Model downloaded successfully. Okay to start translating.
                // (Set a flag, unhide the translation UI, etc.)
                //                                findViewById(R.id.btn).setEnabled(true);
                //                                Toast.makeText(getApplicationContext(),"Second done",Toast.LENGTH_SHORT).show();
                binding.txt.text = null
                dialog!!.dismiss()
            }
            ?.addOnFailureListener { e -> // Model couldn’t be downloaded or other internal error.
                // ...
                binding.txt.text = e.message
            }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.mic -> voice()
            R.id.swap -> swap()
            R.id.cp_1 -> try {
                copy(binding.et1.text.toString())
            } catch (e: Exception) {
                e.printStackTrace()
            }

            R.id.cp_2 -> try {
                copy(binding.txt.text.toString())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun toast(message: String?, type: Int) {
       Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

    }

    private fun voice() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        if (flag) intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE,
            Locale.ENGLISH
        ) else intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi")
        try {
            startActivityForResult(intent, 200)
        } catch (a: ActivityNotFoundException) {
            toast("Intent Problem", 3)
        }
    }

    @SuppressLint("StaticFieldLeak")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 200) {
            if (resultCode == RESULT_OK && data != null) {
                val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)!!
                binding.et1.setText(result[0])
                if (flag) translate_hin(
                    binding.et1.text.toString().trim { it <= ' ' }) else translate_eng(
                    binding.et1.text.toString().trim { it <= ' ' })
            }
        }
    }

    private fun open_dialog() {
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        dialog?.setContentView(R.layout.dialog_loader)
        dialog?.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        dialog?.setCancelable(false)
        dialog?.show()
    }

    private fun swap() {
        var a: String = binding.txtLan1.text.toString()
        var b: String = binding.txtLan2.text.toString()
        a += b
        b = a.substring(0, a.length - b.length)
        a = a.substring(b.length)
        binding.txtLan1.text = a
        binding.txtLan2.text = b
        flag = !flag
        binding.et1.setText(null)
        binding.txt.text = null
        toast("Language Changed", 1)
    }

    fun copy(text: String) {
        if (text != "") {
            clip = ClipData.newPlainText("text", text)
            clipboard?.setPrimaryClip(clip!!)
            toast("Text Copied", 1)
        } else {
            toast("Text Copied", 2)
        }

    }


    private fun translate_hin(text: String?) {
        englishHindiTranslator!!.translate(text!!)
            .addOnSuccessListener { translatedText -> // Translation successful.
                binding.txt.text = translatedText
            }
            .addOnFailureListener { e -> // Error.
                // ...
                binding.txt.text = e.message
            }
    }

    private fun translate_eng(text: String?) {
        hindiEnglishTranslator?.translate(text ?: "")
            ?.addOnSuccessListener { translatedText -> // Translation successful.
                binding.txt.text = translatedText
            }
            ?.addOnFailureListener { e -> // Error.
                // ...
                binding.txt.text = e.message
            }
    }
}