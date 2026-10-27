# Hindi ⇄ English Translator: offline, on-device

![Kotlin](https://img.shields.io/badge/Kotlin-1.9-7F52FF?logo=kotlin&logoColor=white) ![ML Kit](https://img.shields.io/badge/ML%20Kit-Translation-FF6F00?logo=google&logoColor=white) ![Android](https://img.shields.io/badge/Android-minSdk%2024-3DDC84?logo=android&logoColor=white)

An Android app that translates between **Hindi and English entirely on the device** using Google ML Kit's on-device translation models. You can type or speak the text.

## Features

- **Two-way translation**: English → Hindi and Hindi → English.
- **Works offline**: language models are downloaded once (on Wi-Fi only), then translation runs locally.
- **Voice input** through Android speech recognition (`RecognizerIntent`).
- **Live translation** as you type, plus **copy to clipboard**.
- A splash screen while the app starts.

## How it works

```kotlin
val options = TranslatorOptions.Builder()
    .setSourceLanguage(TranslateLanguage.HINDI)
    .setTargetLanguage(TranslateLanguage.ENGLISH)
    .build()
val translator = Translation.getClient(options)

translator.downloadModelIfNeeded(DownloadConditions.Builder().requireWifi().build())
    .addOnSuccessListener { translator.translate(text).addOnSuccessListener(::showResult) }
```

## Tech stack

Kotlin · ML Kit Translation · Android Speech Recognition · View Binding

## Run it

Clone the repo, open it in Android Studio and run on a device (API 24+). The first launch needs Wi-Fi to download the translation models.

---

Built by **[Adarsh Thakur](https://adarsh098765.github.io/adarshthakur.github.io/)**, an Android engineer working with Kotlin and Jetpack Compose · [LinkedIn](https://www.linkedin.com/in/adarsh-thakur-b31281171/)

