package com.limhenry.androidthings.digitalclock

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Camera
import android.hardware.display.DisplayManager
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.util.Log
import android.view.Display
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth

import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

class MainActivity : Activity() {
    internal var shouldExecuteOnResume = false
    private var owmWeather: OWMWeather? = null
    private var mWeatherListener: View.OnClickListener? = null
    private var ambientModeHandler: Handler? = null
    private var ambientModeRunnable: Runnable? = null
    private var mAuth: FirebaseAuth? = null
    private var homeAlarm: HomeAlarm? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        context = applicationContext

        //        currentPlaying = new CurrentPlaying(this);

        val main_linear = findViewById<LinearLayout>(R.id.main_linear)
        main_linear.setOnClickListener {
            ambientModeHandler!!.removeCallbacks(ambientModeRunnable)
            toggleAmbientMode()
        }

        val app_spotify = findViewById<LinearLayout>(R.id.app_spotify)
        app_spotify.setOnClickListener {
            ambientModeHandler!!.removeCallbacks(ambientModeRunnable)
            val intent = Intent(this@MainActivity, SpotifyPlayerActivity::class.java)
            startActivity(intent)
        }

        val app_selfie = findViewById<LinearLayout>(R.id.app_selfie)
        app_selfie.setOnClickListener {
//            val intent = Intent(this@MainActivity, CameraActivity::class.java)
//            startActivity(intent)
        }
    }

    fun refreshMediaRoute(view: View) {
        homeAlarm!!.setScanNetworkHandler()
    }

    fun toggleAmbientMode() {
//        adjustBrightness(210);
        val layout_control = findViewById<LinearLayout>(R.id.layout_control)
        val layout_datetime = findViewById<LinearLayout>(R.id.layout_datetime)
        val ambient_bg = findViewById<ImageView>(R.id.ambient_bg)
        val params = layout_datetime.layoutParams as LinearLayout.LayoutParams

        ambient_bg.visibility = View.INVISIBLE
        layout_control.visibility = View.VISIBLE
        params.setMargins(0, 64, 0, 0)
        layout_datetime.layoutParams = params

        ambientModeHandler = Handler()
        ambientModeRunnable = Runnable {
            //                adjustBrightness(1);
            ambient_bg.visibility = View.VISIBLE
            layout_control.visibility = View.GONE
            params.setMargins(0, 0, 0, 0)
            layout_datetime.layoutParams = params
        }
        ambientModeHandler!!.postDelayed(ambientModeRunnable, 15000)
    }

    fun updateTime() {
        val time = SimpleDateFormat("hh:mm")
        val txt_clockText = findViewById<TextView>(R.id.txt_clock_time)
        time.timeZone = TimeZone.getTimeZone("America/Chicago")
        txt_clockText.text = time.format(Date())

        val date = SimpleDateFormat("EEEE, MMM dd")
        val txt_clock_date = findViewById<TextView>(R.id.txt_clock_date)
        date.timeZone = TimeZone.getTimeZone("America/Chicago")
        txt_clock_date.text = date.format(Date())

        val handler = Handler()
        handler.postDelayed({
            txt_clockText.text = time.format(Date())
            txt_clock_date.text = date.format(Date())
            updateTime()
        }, 30000)
    }

    fun adjustBrightness(brightness: Int) {

        val displayManager = applicationContext.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        val displays = displayManager.displays
        if (displays.size > 0) {
            // Change the screen brightness change mode to manual.
            Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL)
            // Apply the screen brightness value to the system, this will change the value in Settings ---> Display ---> Brightness level.
            // It will also change the screen brightness for the device.
            Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, brightness)

            val window = window
            val layoutParams = window.attributes
            layoutParams.screenBrightness = brightness / 255f
            window.attributes = layoutParams


            //            int id = displays[0].getDisplayId();
            //            ScreenManager screenManager = new ScreenManager(id);
            //            screenManager.setBrightnessMode(ScreenManager.BRIGHTNESS_MODE_MANUAL);
            //            screenManager.setBrightness(brightness);
        }
    }

    override fun onResume() {
        super.onResume()
        if (shouldExecuteOnResume) {
            toggleAmbientMode()
        } else {
            owmWeather = OWMWeather(this)
            owmWeather!!.getWeather()

            FirebaseApp.initializeApp(this)
            mAuth = FirebaseAuth.getInstance()

            mAuth!!.signInWithEmailAndPassword(getString(R.string.firebase_email_test), getString(R.string.firebase_password_test))
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            homeAlarm = HomeAlarm(this@MainActivity)
                        }
                    }

            mWeatherListener = View.OnClickListener { owmWeather!!.getWeather() }

            updateTime()
            toggleAmbientMode()

            val layout_time_weather = findViewById<LinearLayout>(R.id.layout_time_weather)
            layout_time_weather.setOnClickListener(mWeatherListener)

            val handler = Handler()
            handler.postDelayed({ owmWeather!!.getWeather() }, 150000)

            shouldExecuteOnResume = true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    companion object {
        public var context: Context? = null
            private set
    }
}