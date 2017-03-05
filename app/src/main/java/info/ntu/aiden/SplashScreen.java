package info.ntu.aiden;

import android.content.Intent;
import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import java.util.Timer;
import java.util.TimerTask;

public class SplashScreen extends Activity {

    //Set Duration of the splash screen
   long delay = 1200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Remove the Title Bar
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //Set Fullscreen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //Get layout
        setContentView(R.layout.screen_splash);
        //Create Timer
        Timer RunSplash = new Timer();
        //Task to do when the timer ends
        TimerTask ShowSplash = new TimerTask() {
            @Override
            public void run() {
                //CLose SplashScreen
                finish();
                //Start GetstartedAcitvity
                Intent getStarted = new Intent(SplashScreen.this, MainActivity.class);
                startActivity(getStarted);
            }
        };
        //Run
        RunSplash.schedule(ShowSplash, delay);
    }
}
