package info.ntu.aiden;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

//import com.kyleduo.switchbutton.SwitchButton;

/**
 * Created by Aiden on 14/12/16.
 */
//implemets View.OnClickListener
public class SettingActivity extends Activity {
    //private SwitchButton volumeChk, pitchChk, speechrateChk, mfccChk;
    private static boolean isChkV, isChkP, isChkSR, isChkMFCC;

    static final String STATE_VOLUME = "volume selection";
    static final String STATE_PITCH = "pitch selection";
    //static final String STATE_SPEECHRATE = "speech rate selection";
    static final String STATE_MFCC = "mfcc selection";
    static final String STATE_IP = "ip address";
    static final String STATE_Thresh = "vol thresh";//yasir
    private static final String Preferences = "setting";

    private Button BtnDone;

    private Button BtnSubmit;
    private Button BtnThresh;//yasir
    private EditText EditTextIP;
    private EditText EditTextthresh;//yasir

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_setting);

        //volumeChk = (SwitchButton) findViewById(R.id.checkbox_v);
        //pitchChk = (SwitchButton) findViewById(R.id.checkbox_p);
        //speechrateChk = (SwitchButton) findViewById(R.id.checkbox_sr);
        //mfccChk = (SwitchButton) findViewById(R.id.checkbox_MFCC);
        EditTextIP = (EditText)findViewById(R.id.IP);
        EditTextthresh=(EditText)findViewById(R.id.th);//yasir
        BtnSubmit = (Button)findViewById(R.id.submit);
        BtnThresh = (Button)findViewById(R.id.button);//yasir

        SharedPreferences settings = getSharedPreferences(Preferences, 0);
        Boolean restoredVol = settings.getBoolean(STATE_VOLUME, true);
        Boolean restoredPitch = settings.getBoolean(STATE_PITCH, true);
        //Boolean restoredSR = settings.getBoolean(STATE_SPEECHRATE, true);
        Boolean restoredMFCC = settings.getBoolean(STATE_MFCC, true);
        String restoredIP = settings.getString(STATE_IP, "blabla");
        String restoredth = settings.getString(STATE_Thresh, "80");//yasir

        //volumeChk.setChecked(restoredVol);
        //pitchChk.setChecked(restoredPitch);
        //speechrateChk.setChecked(restoredSR);
        //mfccChk.setChecked(restoredMFCC);
        EditTextIP.setText(restoredIP);
        EditTextthresh.setText(restoredth);

        BtnDone = (Button)findViewById(R.id.done);
        BtnDone.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onStop();
                finish();
            }
        });

        BtnSubmit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if( EditTextIP.getText().toString() != null){
                    SharedPreferences settings = getSharedPreferences(Preferences, 0);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString(STATE_IP, EditTextIP.getText().toString());
                    //editor.putString(STATE_Thresh, EditTextthresh.getText().toString());
                    editor.commit();
                }
            }
        });

        BtnThresh.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if( EditTextthresh.getText().toString() != null){
                    SharedPreferences settings = getSharedPreferences(Preferences, 0);
                    SharedPreferences.Editor editor = settings.edit();
                    //editor.putString(STATE_IP, EditTextIP.getText().toString());
                    editor.putString(STATE_Thresh, EditTextthresh.getText().toString());
                    editor.commit();
                }
            }
        });
    }
    @Override
    protected void onStop(){
        super.onStop();

        // We need an Editor object to make preference changes.
        // All objects are from android.context.Context
        SharedPreferences settings = this.getSharedPreferences(Preferences, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(STATE_VOLUME, isChkV);
        editor.putBoolean(STATE_PITCH, isChkP);
        //editor.putBoolean(STATE_SPEECHRATE, isChkSR);
        editor.putBoolean(STATE_MFCC, isChkMFCC);
        //String cj=EditTextthresh.getText().toString();
        editor.putString(STATE_Thresh,EditTextthresh.getText().toString() );//yasir
        editor.putString(STATE_IP,EditTextIP.getText().toString() );//yasir
        // Commit the edits!
        editor.commit();
        //Log.d("st",settings.toString());
        Toast.makeText(SettingActivity.this, "Settings saved", Toast.LENGTH_SHORT).show();
    }

    /*
    @Override
    public void onClick(View v){
        int id = v.getId();
        switch (id){
            case R.id.checkbox_v:
                volumeChk.setChecked(!volumeChk.isChecked());
                volumeChk.toggle();
                isChkV=!isChkV;
                break;
            case R.id.checkbox_p:
                pitchChk.setChecked(!pitchChk.isChecked());
                pitchChk.toggle();
                isChkP=!isChkP;
                break;
//            case R.id.checkbox_sr:
//                speechrateChk.setChecked(!speechrateChk.isChecked());
//                speechrateChk.toggle();
//                isChkSR=!isChkSR;
//                break;
            case R.id.checkbox_MFCC:
                mfccChk.setChecked(!mfccChk.isChecked());
                mfccChk.toggle();
                isChkMFCC=!isChkMFCC;
                break;
            default:break;
        }

    } */

    /*
    public void onCheckboxClicked(View view) {
        // Is the view now checked?
        boolean checked = ((CheckBox) view).isChecked();

        // Check which checkbox was clicked
        switch(view.getId()) {
            case R.id.checkbox_v:
                if (checked) {
                    // enable volume detection
                    isChkV = true;
                }else {
                    // disable volume detection
                    isChkV = false;
                }
                break;
            case R.id.checkbox_p:
                if (checked) {
                    // enable pitch detection
                    isChkP = true;
                }else{
                    // disable pitch detection
                    isChkP = false;
                }
                break;
            case R.id.checkbox_sr:
                if (checked) {
                    // enable speech rate detection
                    isChkSR = true;
                }else {
                    // disable speech rate detection
                    isChkSR = false;
                }
                break;
            // TODO: Veggie sandwich
        }

    }
    */
}
