package com.limhenry.androidthings.digitalclock;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.vision.Frame;
import com.google.android.things.device.ScreenManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class MainActivity extends Activity {
    boolean shouldExecuteOnResume;
    private static Context mContext;
    private OWMWeather owmWeather;
    private View.OnClickListener mWeatherListener = new View.OnClickListener() {
        public void onClick(View v) {
            owmWeather.getWeather();
        }
    };
    private Handler ambientModeHandler;
    private Runnable ambientModeRunnable;
    private CurrentPlaying currentPlaying;

    public static Context getContext() {
        return mContext;
    }

    public void startSpotify(View view) {
        ambientModeHandler.removeCallbacks(ambientModeRunnable);
        Intent intent = new Intent(this, SpotifyPlayerActivity.class);
        startActivity(intent);
    }

    public void startCalendar(View view) {
        ambientModeHandler.removeCallbacks(ambientModeRunnable);
        Intent intent = new Intent(this, CalendarActivity.class);
        startActivity(intent);
    }

    public void refreshMediaRoute(View view) {
        currentPlaying.refreshMediaRoute();
    }

    public void toggleAmbient(View view) {
        toggleAmbientMode();
    }

    public void toggleAmbientMode() {
        adjustBrightness(210);
        final LinearLayout layout_control = (LinearLayout) findViewById(R.id.layout_control);
        final LinearLayout layout_datetime = (LinearLayout) findViewById(R.id.layout_datetime);
        final ImageView ambient_bg = (ImageView) findViewById(R.id.ambient_bg);
        final LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) layout_datetime.getLayoutParams();

        ambient_bg.setVisibility(View.INVISIBLE);
        layout_control.setVisibility(View.VISIBLE);
        params.setMargins(0, 64, 0, 0);
        layout_datetime.setLayoutParams(params);

        ambientModeHandler = new Handler();
        ambientModeRunnable = new Runnable() {
            public void run() {
                adjustBrightness(1);
                ambient_bg.setVisibility(View.VISIBLE);
                layout_control.setVisibility(View.GONE);
                params.setMargins(0, 0, 0, 0);
                layout_datetime.setLayoutParams(params);
            }
        };
        ambientModeHandler.postDelayed(ambientModeRunnable, 15000);
    }

    public void updateTime() {
        final SimpleDateFormat time = new SimpleDateFormat("hh:mm");
        final TextView txt_clockText = (TextView) findViewById(R.id.txt_clock_time);
        time.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        txt_clockText.setText(time.format(new Date()));

        final SimpleDateFormat date = new SimpleDateFormat("EEEE, MMM dd");
        final TextView txt_clock_date = (TextView) findViewById(R.id.txt_clock_date);
        date.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        txt_clock_date.setText(date.format(new Date()));

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                txt_clockText.setText(time.format(new Date()));
                txt_clock_date.setText(date.format(new Date()));
                updateTime();
            }
        }, 30000);
    }

    public void adjustBrightness(int brightness) {
        DisplayManager displayManager = (DisplayManager) getApplicationContext().getSystemService(Context.DISPLAY_SERVICE);
        Display[] displays = displayManager.getDisplays();
        if (displays.length > 0) {
            int id = displays[0].getDisplayId();
            ScreenManager screenManager = new ScreenManager(id);
            screenManager.setBrightnessMode(ScreenManager.BRIGHTNESS_MODE_MANUAL);
            screenManager.setBrightness(brightness);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = getApplicationContext();

        owmWeather = new OWMWeather(this);
        owmWeather.getWeather();

        currentPlaying = new CurrentPlaying(this);

        updateTime();
        toggleAmbientMode();

        LinearLayout layout_time_weather = (LinearLayout) findViewById(R.id.layout_time_weather);
        layout_time_weather.setOnClickListener(mWeatherListener);

        LinearLayout main_linear = (LinearLayout) findViewById(R.id.main_linear);
        main_linear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ambientModeHandler.removeCallbacks(ambientModeRunnable);
                toggleAmbientMode();
            }
        });

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                owmWeather.getWeather();
            }
        }, 300000);

        shouldExecuteOnResume = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(shouldExecuteOnResume){
            toggleAmbientMode();
        } else{
            shouldExecuteOnResume = true;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}