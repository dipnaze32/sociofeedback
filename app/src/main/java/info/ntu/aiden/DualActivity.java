package info.ntu.aiden;

import android.app.Activity;
import android.content.Intent;
import android.os.Vibrator;
//RecognizerIntent for speech rate

import java.text.DecimalFormat; //feedback
import java.util.ArrayList;

import android.graphics.Color; //feedback
import android.os.Bundle;
import android.speech.RecognizerIntent;

import android.speech.SpeechRecognizer;
import android.view.LayoutInflater; //feedback
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import android.util.Log; //new
import android.speech.RecognitionListener; //new
import android.os.SystemClock;//new
import android.os.Handler;//new

//extisting lib
import com.akexorcist.roundcornerprogressbar.IconRoundCornerProgressBar;
import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.mfcc.MFCC;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;
//import com.github.nkzawa.socketio.client.IO;
//import com.github.nkzawa.socketio.client.Socket;


public class DualActivity extends Activity {
	//Speech Recognition with Timer and speech rate
	private TextView timerValue;
	private long startTime= 0L;
	long timeInMilliseconds = 0L;
	long timeSwapBuff = 0L;
	long updatedTime = 0L;
    private Handler customHandler = new Handler();
	private TextView txtSpeechInput;
	private TextView speechrate;
	private ImageButton btnSpeak;
    private TextView mText;
    private SpeechRecognizer sr;
    private static final String SR = "Speech Recognition";
    private static final String TAG = "Speech Feedback";
    private static final String MAIN = "SYSTEM";

    //audiocapture
    private TextView volumeLabel;
    private IconRoundCornerProgressBar volume;
    private TextView lblvolume;
    private TextView textVolume;
    private TextView lblresvol;
    private TextView res_textVol;
    private TextView lblpitch;
    private TextView textPitch;
    private TextView lblrespitch;
    private TextView res_textPitch;
    private TextView lblmfcc;
    private TextView res_mfcc;
    private TextView ques; //yasir
    //low level features
    private double Vol;
    private float pitchInHz;
    private int count = 0;
    private int updateRate = 30; // the larger the lower update rate - for feedback message;
    private int noOfLLF = 2; //number of low level features
    private float[][] lowLevFeatures = new float[updateRate][noOfLLF];
    private int thresh=0;//yasir
    private int q_count=0;//yasir
    private int q_nmbr=0;//yasir
    //MFCC centerfrequency
    private int bufferSize = 1024;
    private int sampleRate = 8000;
    private MFCC mfcc = new MFCC(bufferSize, sampleRate);

    private Button Btnfeedback;
    private boolean mStartFeedback = true;
    private AudioDispatcher dispatcher;
    /*//permission
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    */
	//start MAIN ACTIVITY
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_dual);
        Log.d(MAIN,"System Initialized... ");
        //verifyStoragePermissions(this);

        volumeLabel=(TextView)findViewById(R.id.volume);
        volume = (IconRoundCornerProgressBar) findViewById(R.id.progressBarV);
        lblvolume = (TextView) findViewById(R.id.lbl_tvdBlevel);
        textVolume = (TextView) findViewById(R.id.tvdBlevel);
        lblresvol = (TextView) findViewById(R.id.lbl_res_tvdBlevel);
        res_textVol = (TextView) findViewById(R.id.result_tvdBlevel);
        lblmfcc = (TextView) findViewById(R.id.lbl_res_mfcc);
        res_mfcc = (TextView) findViewById(R.id.result_mfcc);

        //Pitch
        lblpitch = (TextView) findViewById(R.id.lbl_tvMessage);
        textPitch = (TextView) findViewById(R.id.tvMessage);
        lblrespitch = (TextView) findViewById(R.id.lbl_result_txtview);
        res_textPitch = (TextView) findViewById(R.id.result_txtview);
        ques = (TextView) findViewById(R.id.question); //yasir

        //Speech Recognition with Timer and speech rate
        timerValue = (TextView) findViewById(R.id.timerValue);
       	txtSpeechInput = (TextView) findViewById(R.id.txtSpeechInput);
		speechrate = (TextView) findViewById(R.id.speechrate);
		btnSpeak = (ImageButton) findViewById(R.id.btnSpeak);
        sr = SpeechRecognizer.createSpeechRecognizer(this);
        sr.setRecognitionListener(new listener());
        mText = (TextView) findViewById(R.id.mText);
        //MICbutton
        btnSpeak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                promptInput();
                Log.d(MAIN,"MIC Click ");
                //startFeedback();
                timeSwapBuff = 0L;
                startTime = SystemClock.uptimeMillis();
                customHandler.postDelayed(updateTimerThread, 0);
            }
        });
        Btnfeedback = (Button) findViewById(R.id.startfeedback);
        Btnfeedback.setOnClickListener(clicker);

	}
    //onclicklistener for btnFeedback
    View.OnClickListener clicker = new View.OnClickListener() {
        @Override
        public void onClick(View arg0) {
            onFeedback(mStartFeedback);
            mStartFeedback = !mStartFeedback;
        }
    };

    //timer
	private Runnable updateTimerThread = new Runnable() {

		public void run() {

			timeInMilliseconds = SystemClock.uptimeMillis() - startTime;
			updatedTime = timeSwapBuff + timeInMilliseconds;
            //Timer Setup
			int secs = (int) (updatedTime / 1000);
			int mins = secs / 60;
			secs = secs % 60;
			int milliseconds = (int) (updatedTime % 1000); //updatedTime mod 1000 last 3 digit
			timerValue.setText("" + String.format("%02d", mins) + ":"
					+ String.format("%02d", secs) + ":"
					+ String.format("%03d", milliseconds));
			customHandler.postDelayed(this, 0);
		}

	};

    private void promptInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,"voice.recognition.test");
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,5);

        sr.startListening(intent);
        Log.d(SR,"Speech Recognizer Initialised...");
        Vibrator v = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        // Vibrate for 250 milliseconds
        v.vibrate(250);
    }

	class listener implements RecognitionListener
	{
		public void onReadyForSpeech(Bundle params)
		{
			Log.d(SR, "onReadyForSpeech");
		}
		public void onBeginningOfSpeech()
		{
			Log.d(SR, "onBeginningOfSpeech");
		}
		public void onRmsChanged(float rmsdB)
        {
            Log.d(SR,"onRmsChanged");
        } //not called in log d
		public void onBufferReceived(byte[] buffer)
		{
			Log.d(SR, "onBufferReceived");
		}
		public void onEndOfSpeech()
		{
			Log.d(SR, "onEndofSpeech");
		}

        public void onError(int error)
		{
			Log.d(SR,  "error " +  error);
            Log.i(SR,"updatedETime " + updatedTime);
			//mText.setText("error " + error);
            timeSwapBuff += timeInMilliseconds;
            //Stop Timer
            customHandler.removeCallbacks(updateTimerThread);
            Vibrator v = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            long[] pattern = {0, 300, 100, 300};
            v.vibrate(pattern, -1); //-1 is important
            switch (error)
            {
                case 1: Toast.makeText(getApplicationContext(), "No Match Found, PLEASE TRY AGAIN",
                        Toast.LENGTH_SHORT).show();
                case 2:Toast.makeText(getApplicationContext(), "Client Error",
                        Toast.LENGTH_SHORT).show();
                case 3:Toast.makeText(getApplicationContext(), "Server Error",
                        Toast.LENGTH_SHORT).show();
                case 4:Toast.makeText(getApplicationContext(), "Network Error",
                        Toast.LENGTH_SHORT).show();
                case 5:Toast.makeText(getApplicationContext(), "Audio Error",
                        Toast.LENGTH_SHORT).show();
                default:
                    Toast.makeText(getApplicationContext(), "PLEASE TRY AGAIN",
                            Toast.LENGTH_SHORT).show();
            }
		}

        public void onResults(Bundle results)
		{
			Log.d(SR, "onResults " + results);
			ArrayList<String> data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
			txtSpeechInput.setText(data.get(0));

            //COUNT NO. OF WORDS
            String input = data.get(0);
            String[] words = input.split("\\s+");
            int wordCount = words.length;
            mText.setText("wordCount " + wordCount);
            Log.i(SR,"No. of words " + wordCount);
            double ut = (updatedTime/60000.0);
            Log.i(SR,"Time Elapsed (updatedTime) " + updatedTime);
            if (ut!=0)
            {
                double wpm = (wordCount / ut);
                Log.i(SR,"Total time " + ut);
                Log.i(SR,"Word Per Minute " + wpm);
                speechrate.setText("Speech Rate: " + String.format("%.1f", wpm) + "wpm");
            }
            else
            {
                int wpm = 0;
                Log.i(SR,"ut " + ut);
                Log.i(SR,"wpm " + wpm);
                speechrate.setText("Speechrate" + wpm);

            }
            //divide by 1000 sec then 60 for minutes
            //speechrate avg 130 words per minutes
            //Stop Timer
            timeSwapBuff += timeInMilliseconds;
            customHandler.removeCallbacks(updateTimerThread);
            Vibrator v = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            // Vibrate for 250 milliseconds
            v.vibrate(250);
        }
		public void onPartialResults(Bundle partialResults)
		{
			Log.d(SR, "onPartialResults" + partialResults);
            Toast.makeText(getApplicationContext(), "Results Incomplete",
                    Toast.LENGTH_SHORT).show();
		}
		public void onEvent(int eventType, Bundle params)
		{
			Log.d(SR, "onEvent " + eventType);
		}

	}


    private void onFeedback(boolean start) {
        if (start) {
            startFeedback();
            //Btnfeedback.setImageResource(R.drawable.on);
            //mSocket.connect();
        } else {
            stopFeedback();
           // Btnfeedback.setImageResource(R.drawable.off);
        }
    }

    private void stopFeedback(){
           Log.d(TAG,"stopFeedback ");
           dispatcher.stop();
           dispatcher = null;
           //mSocket.disconnect();
   }
   private void startFeedback(){
       //new method use the tarsos
       Log.d(TAG,"startFeedback ");
       dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(8000, 1024, 0);
       PitchDetectionHandler pdh = new PitchDetectionHandler() {
           @Override
           public void handlePitch(PitchDetectionResult result, AudioEvent e) {

               //Calculating MFCC
               float[] mfcc_buffer;
               float[] mfcc_val;
               float sum=0;
               mfcc_buffer = e.getFloatBuffer();
               float bin[] = mfcc.magnitudeSpectrum(mfcc_buffer);
               float fbank[] = mfcc.melFilter(bin, mfcc.getCenterFrequencies());
               float f[] = mfcc.nonLinearTransformation(fbank);
               mfcc_val = mfcc.cepCoefficients(f);

               float min_mfcc = mfcc_val[0];
               //for display purpose,  we take the minimum MFCC
               for (int i = 1; i < mfcc_val.length; i++) {
                   if (mfcc_val[i] < min_mfcc) {
                       min_mfcc = mfcc_val[i];
                   }
               }
               final float finalmfcc_val_float = min_mfcc;

               //Calculating SPL value in dB
               final Double dbSPLValue = calculate(e.getFloatBuffer());
               Vol = dbSPLValue + 85.0;  //+70.0   //!
               //Getting Pitch Frequency in Hz
               pitchInHz = result.getPitch();     //!

               runOnUiThread(new Runnable() {

                   @Override
                   public void run() {

                       textPitch.setText("" + pitchInHz);
                       if (pitchInHz >= 0) {
                           res_textPitch.setText("SPEECH DETECTED");
                           textPitch.setTextColor(Color.parseColor("#FFCC66"));
                           res_textPitch.setTextColor(Color.parseColor("#FFCC66"));
                           textVolume.setTextColor(Color.parseColor("#FFCC66"));
                           res_textVol.setTextColor(Color.parseColor("#FFCC66"));

                       } else if (pitchInHz == -1) {
                           res_textPitch.setText("SILENCE");
                           textPitch.setTextColor(Color.parseColor("#C0C0C0"));
                           res_textPitch.setTextColor(Color.parseColor("#C0C0C0"));
                           textVolume.setTextColor(Color.parseColor("#C0C0C0"));
                           res_textVol.setTextColor(Color.parseColor("#C0C0C0"));

                       }
                       textVolume.setText(String.valueOf(Math.round(dbSPLValue)));
                       res_textVol.setText(String.valueOf(Math.round(Vol)));

                       if(Vol >= 0 && Vol <= thresh-25){
                           volume.setProgressColor(Color.parseColor("#8dcdc1"));//35
                       }else
                       if(Vol > thresh-25 && Vol <= thresh-15){
                           volume.setProgressColor(Color.parseColor("#fff5c3"));
                       }else
                       if(Vol > thresh-15 && Vol <= thresh){
                           volume.setProgressColor(Color.parseColor("#d3e397"));
                       }
                       if(Vol > thresh){
                           volume.setProgressColor(Color.parseColor("#eb6e44"));
                       }
                       //////////////////
                       /*
                       q_count=q_count+1;//yasir

                       if(q_count>100)
                       {
                           ques.setText(questions[q_nmbr]);
                           q_nmbr=q_nmbr+1;
                           if(q_nmbr==4)
                           {
                               q_nmbr=0;
                           }
                           q_count=0;

                       }//yasir
                        */
                       /////////////////

                       volume.setProgress((float) ((Vol -30.0)/40) * 100);
                       DecimalFormat df = new DecimalFormat("#.######");
                       Log.i(TAG,"updateVOLprogress " + Vol);
                       //Vol = Double.valueOf(df.format(Vol)); //error
                       //String message = Double.toString(Vol) + " " + Float.toString(pitchInHz) + " " + Float.toString(finalmfcc_val_float) ;
                       //mSocket.emit("new message", message);
                       //series_aud.appendData(new DataPoint(lastX++, pitchInHz), true, 20);
                       //res_mfcc.setText(Float.toString(finalmfcc_val_float)); //error

                       lowLevFeatures[count][0] = (float)Vol;
                       lowLevFeatures[count][1] = pitchInHz;

                       if(count < updateRate -1){
                           count++;
                       }else {

                           LayoutInflater inflater = getLayoutInflater();
                           float avgVOl = classificationData(lowLevFeatures)[0][0];
                           float avgPITCH = classificationData(lowLevFeatures)[0][1];

                           Log.i(TAG,Float.toString(avgVOl));
                           Log.i(TAG,Float.toString(avgPITCH));
                          /**
                           if (gender == true){ //female
                               if(avgVOl >= thresh-25 && avgVOl <= thresh-15){
                                   View view = inflater.inflate(R.layout.cust_toast_layout,//35 45
                                           (ViewGroup) findViewById(R.id.toastSilience));
                                   Toast toast = new Toast(getApplicationContext());
                                   toast.setView(view);
                                   toast.show();
                               }else
                               if(avgVOl > thresh-15 && avgVOl <= thresh){
                                   View view = inflater.inflate(R.layout.cust_toast_layout2,//45 60
                                           (ViewGroup) findViewById(R.id.toastSpeaking));
                                   Toast toast = new Toast(getApplicationContext());
                                   toast.setView(view);
                                   toast.show();
                               }else if(avgVOl >thresh){
                                   View view = inflater.inflate(R.layout.cust_toast_layout3,//60
                                           (ViewGroup) findViewById(R.id.toastScreaming));
                                   Toast toast = new Toast(getApplicationContext());
                                   toast.setView(view);
                                   toast.show();
                               }
                           }else { //male
                               if(avgVOl >= thresh-25 && avgVOl <= thresh-15){
                                   View view = inflater.inflate(R.layout.cust_toast_layoutguy,//35 45
                                           (ViewGroup) findViewById(R.id.toastSilienceguy));
                                   Toast toast = new Toast(getApplicationContext());
                                   toast.setView(view);
                                   toast.show();
                               }else
                               if(avgVOl > thresh-15 && avgVOl <= thresh){
                                   View view = inflater.inflate(R.layout.cust_toast_layout2guy,//45 60
                                           (ViewGroup) findViewById(R.id.toastSpeakingguy));
                                   Toast toast = new Toast(getApplicationContext());
                                   toast.setView(view);
                                   toast.show();
                               }else if(avgVOl > thresh){
                                   View view = inflater.inflate(R.layout.cust_toast_layout3guy,//60
                                           (ViewGroup) findViewById(R.id.toastScreamingguy));
                                   Toast toast = new Toast(getApplicationContext());
                                   toast.setView(view);
                                   toast.show();
                               }
                           }

                           if(avgPITCH >=85 && avgPITCH <= 165){
                               gender = false;
                               //Toast.makeText(getApplicationContext(), "Are you a gentleman?", Toast.LENGTH_SHORT).show();
                           }else if (avgPITCH >=165 && avgPITCH <= 255){
                               gender = true;
                               //Toast.makeText(getApplicationContext(), "Are you a lady?", Toast.LENGTH_SHORT).show();
                           }*/
                           count = 0;
                       }
                   }


               });
           }
       };
       //previous sampleRate:22050
       AudioProcessor p = new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN, 8000, 1024, pdh);
       dispatcher.addAudioProcessor(p);
       new Thread(dispatcher, "Audio Dispatcher").start();
   }

    private Double calculate(float[] floatBuffer) {

        double power = 0.0D;
        for (float element : floatBuffer) {
            power += element * element;
        }
        double value = Math.pow(power, 0.5)/ floatBuffer.length;;
        return 20.0 * Math.log10(value);
    }

    private float[][] classificationData (float[][] LLF){
        float avgVol = 0;
        float avgPitch = 0;

        int avgCount = 0;

        for (int i = 0; i < updateRate -1; i++){
            if (LLF[i][1] > 0 ){
                avgVol = avgVol + LLF[i][0];
                avgPitch = avgPitch + LLF[i][1];
                avgCount++;
            }
        }
        //only take avg of the vol/pitch with pitch > 0
        if (avgCount != 0 ){
            avgVol = (float) (avgVol/avgCount); //normalized average volume
            avgPitch = (float)(avgPitch/avgCount); //normalized average pitch
        }else {
            avgVol = 0;
            avgPitch = 0;
        }

        float[][] lowLevFeaturesResult = {{avgVol,avgPitch}};
        Log.i(TAG, "average volume: " +  String.valueOf(lowLevFeaturesResult[0][0]) + ", average pitch:" + String.valueOf(lowLevFeaturesResult[0][1]));

        return lowLevFeaturesResult;
    }
    /** //socio1
     public static void verifyStoragePermissions(Activity activity) {
     // Check if we have write permission
     int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
     //int permission2 = ActivityCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO);

     if (permission != PackageManager.PERMISSION_GRANTED) {
     // We don't have permission so prompt the user
     ActivityCompat.requestPermissions(
     activity,
     PERMISSIONS_STORAGE,
     REQUEST_EXTERNAL_STORAGE
     );
     }
     }*/
    @Override
    public void onDestroy() {
        Log.d(MAIN,"onDestory ");
        super.onDestroy();
        //mSocket.disconnect();
    }
}
