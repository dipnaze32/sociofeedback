package info.ntu.aiden;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;

/**
 * Created by Aiden on 14/12/16.
 */

public class MainActivity extends Activity {
    /*private static final String TAG = "feedback";
    static final String STATE_VOLUME = "volume selection";
    static final String STATE_PITCH = "pitch selection";
    static final String STATE_SPEECHRATE = "speech rate selection";
    static final String STATE_MFCC = "mfcc selection";
    static final String STATE_IP = "ip address";
    private static final String Preferences = "setting";*/

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_main);

        //ImageView background = (ImageView) findViewById(R.id.layout_background);
        //SETTINGS
        ImageButton settings = (ImageButton) findViewById(R.id.settings);
        settings.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent settings = new Intent(MainActivity.this, SettingActivity.class);
                startActivity(settings);

            }
        });
        //ABOUT US
        ImageButton aboutUs = (ImageButton) findViewById(R.id.aboutus);
        aboutUs.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent aboutUs = new Intent(MainActivity.this, AboutusActivity.class);
                startActivity(aboutUs);
            }
        });
        //Single Activity
        Button start = (Button) findViewById(R.id.start2);
        start.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent com = new Intent(MainActivity.this, SingleActivity.class);
                startActivity(com);
            }
        });
        //Conversation Activity
        Button start2 = (Button) findViewById(R.id.start);
        start2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent com2 = new Intent(MainActivity.this, DualActivity.class);
                startActivity(com2);
            }
        });
/*
        //"To become a better communicator"
        SecretTextView secretTextView;
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(background, "alpha", 1f, .3f);
        fadeOut.setDuration(2000);
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(background, "alpha", .3f, 1f);
        fadeIn.setDuration(2000);
        final AnimatorSet mAnimationSet = new AnimatorSet();
        mAnimationSet.play(fadeIn).after(fadeOut);
        mAnimationSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mAnimationSet.start();
            }
        });
        mAnimationSet.start();

        secretTextView = (SecretTextView)findViewById(R.id.description);
        secretTextView.setDuration(3000);

        secretTextView.show();    // fade in
        secretTextView.hide();    // fade out
        secretTextView.toggle();

*/

    }
}
