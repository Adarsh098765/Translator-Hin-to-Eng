package com.demo.translatehintoeng

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler

class SplashActivity : AppCompatActivity() {
    private val SPLASH_DELAY: Long = 4000 // 3 seconds delay
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)


    }

    override fun onStart() {
        super.onStart()
        Handler().postDelayed({
            // Start MainActivity after the delay
            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            finish() // Finish SplashActivity so that it's not accessible when MainActivity is shown
        }, SPLASH_DELAY)
    }
}