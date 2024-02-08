package com.demo.translatehintoeng

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
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
    private lateinit var englishHindiTranslator: Translator
    private lateinit var hindiEnglishTranslator: Translator
    private lateinit var clipboard: ClipboardManager
    private lateinit var speechActivityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var dialog: Dialog
    private lateinit var options_2: TranslatorOptions
    private var flag = true
    private var clip: ClipData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        setupClickListeners()
        setupClipboard()
        setupTextChangeListener()
        setupTranslators()
        setupSpeechRecognition()
    }

    private fun setupClickListeners() {
        with(binding) {
            mic.setOnClickListener(this@MainActivity)
            swap.setOnClickListener(this@MainActivity)
            cp1.setOnClickListener(this@MainActivity)
            cp2.setOnClickListener(this@MainActivity)
        }
    }

    private fun setupClipboard() {
        clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        dialog = Dialog(this@MainActivity, android.R.style.Theme_Dialog)
        openDialog()
    }

    private fun setupTextChangeListener() {
        binding.et1.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (flag) translateHindi(binding.et1.text.toString()) else translateEnglish(binding.et1.text.toString())
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun afterTextChanged(s: Editable) {}
        })
    }

    private fun setupTranslators() {
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
        englishHindiTranslator.downloadModelIfNeeded(conditions)
            .addOnSuccessListener {
                download_hin_eng()
            }
            .addOnFailureListener { e ->
                binding.txt.text = e.message
            }
    }

    private fun setupSpeechRecognition() {
        val activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val resultCode = result.resultCode
            val data = result.data
            if (resultCode == Activity.RESULT_OK && data != null) {
                data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()?.let { result ->
                    binding.et1.setText(result)
                    if (flag) translateHindi(result.trim()) else translateEnglish(result.trim())
                }
            }
        }
        speechActivityResultLauncher = activityResultLauncher
    }


    private fun download_hin_eng() {
        hindiEnglishTranslator = Translation.getClient(options_2)
        hindiEnglishTranslator.downloadModelIfNeeded()
            .addOnSuccessListener { // Model downloaded successfully. Okay to start translating.
                // (Set a flag, unhide the translation UI, etc.)
                //                                findViewById(R.id.btn).setEnabled(true);
                //                                Toast.makeText(getApplicationContext(),"Second done",Toast.LENGTH_SHORT).show();
                binding.txt.text = null
                dialog.dismiss()
            }
            .addOnFailureListener { e -> // Model couldn’t be downloaded or other internal error.
                // ...
                binding.txt.text = e.message
            }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.mic -> voice()
            R.id.swap -> swapText()
            R.id.cp_1 -> try {
                copyText(binding.et1.text.toString())
            } catch (e: Exception) {
                e.printStackTrace()
            }

            R.id.cp_2 -> try {
                copyText(binding.txt.text.toString())
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
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE,
            if (flag) Locale.ENGLISH.language else "hi"
        )

        try {
            // Start the activity for result using the ActivityResultLauncher
            speechActivityResultLauncher.launch(intent)
        } catch (a: ActivityNotFoundException) {
            toast("Intent Problem", 3)
        }
    }
    private fun openDialog() {
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setContentView(R.layout.dialog_loader)
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        dialog.setCancelable(false)
        dialog.show()
    }

    private fun swapText() {
        var firstText: String = binding.txtLan1.text.toString()
        var secondText: String = binding.txtLan2.text.toString()
        val temp = firstText
        firstText = secondText
        secondText = temp
        binding.txtLan1.text = firstText
        binding.txtLan2.text = secondText
        flag = !flag
        binding.et1.text = null
        binding.txt.text = null
        toast("Language Changed", Toast.LENGTH_SHORT)
    }

    private fun copyText(text: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        if (text.isNotBlank()) {
            val clip = ClipData.newPlainText("text", text)
            clipboard.setPrimaryClip(clip)
            toast("Text Copied", Toast.LENGTH_SHORT)
        } else {
            toast("No text to copy", Toast.LENGTH_SHORT)
        }
    }

    private fun translateHindi(text: String?) {
        text?.let {
            englishHindiTranslator.translate(it)
                .addOnSuccessListener { translatedText ->
                    binding.txt.text = translatedText
                }
                .addOnFailureListener { e ->
                    binding.txt.text = e.message
                }
        }
    }

    private fun translateEnglish(text: String?) {
        text?.let {
            hindiEnglishTranslator.translate(it)
                .addOnSuccessListener { translatedText ->
                    binding.txt.text = translatedText
                }
                .addOnFailureListener { e ->
                    binding.txt.text = e.message
                }
        }
    }

}