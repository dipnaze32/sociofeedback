package info.ntu.aiden;

    import java.io.BufferedOutputStream;
    import java.io.DataInputStream;
    import java.io.DataOutputStream;
    import java.io.File;
    import java.io.FileInputStream;
    import java.io.FileOutputStream;
    import java.io.IOException;
    import java.io.InputStream;
    import java.io.RandomAccessFile;
    import android.media.AudioFormat;
    import android.media.AudioRecord;
    import android.media.MediaRecorder;
    import java.nio.ByteBuffer;
    import java.nio.ByteOrder;
    import java.text.DecimalFormat;
    import java.util.Random;

    import android.Manifest;
    import android.app.Activity;
    import android.content.pm.PackageManager;
    import android.graphics.Color;
    import android.os.Bundle;
   import android.os.Environment;

    import android.os.Handler;
    import android.os.SystemClock;
    import android.os.Vibrator;
    //import android.support.annotation.RequiresPermission;
    import android.support.v4.app.ActivityCompat;
    import android.text.format.Time;
    import android.util.Log;
    import android.view.LayoutInflater;
    import android.view.View;
    import android.view.ViewGroup;
    import android.widget.ImageButton;
    import android.widget.ProgressBar;
    import android.widget.TextView;
    import android.widget.Toast;

    import com.akexorcist.roundcornerprogressbar.IconRoundCornerProgressBar;
    import com.github.nkzawa.socketio.client.Socket;
    import com.jjoe64.graphview.GraphView;
    import com.jjoe64.graphview.GridLabelRenderer;
    import com.jjoe64.graphview.Viewport;
    import com.jjoe64.graphview.series.DataPoint;
    import com.jjoe64.graphview.series.LineGraphSeries;

    import be.tarsos.dsp.AudioDispatcher;
    import be.tarsos.dsp.AudioEvent;
    import be.tarsos.dsp.AudioProcessor;
    import be.tarsos.dsp.io.TarsosDSPAudioFormat;
    import be.tarsos.dsp.io.android.AndroidAudioPlayer;
    import be.tarsos.dsp.io.android.AndroidAudioInputStream;
    import be.tarsos.dsp.io.android.AudioDispatcherFactory;
    import be.tarsos.dsp.mfcc.MFCC;
    import be.tarsos.dsp.pitch.PitchDetectionHandler;
    import be.tarsos.dsp.pitch.PitchDetectionResult;
    import be.tarsos.dsp.pitch.PitchProcessor;
    import be.tarsos.dsp.writer.WaveHeader;
    import be.tarsos.dsp.writer.WriterProcessor;

public class SingleActivity extends Activity {

    private AudioRecord mRecorder; //our recorder initialized first
    private File mRecording;
    private short[] mBuffer; //buffer where we will put captured samples
    private final String startRecordingLabel = "Start recording";
    private final String stopRecordingLabel = "Stop recording";
    private boolean mIsRecording = false; //fasle indicates if sound is currently being captured
    private ProgressBar mProgressBar; //progress bar recieved from layout
    private ImageButton button;

    //AudioRecorder
    private int audioSource = MediaRecorder.AudioSource.MIC;
    private static int sample_RATE = 48000;
    private static int channelConfig = AudioFormat.CHANNEL_IN_MONO;
    private static int aFormat = AudioFormat.ENCODING_PCM_16BIT;

    //TIMER
    private TextView timerValue;
    private long startTime = 0L;
    long timeInMilliseconds = 0L;
    long timeSwapBuff = 0L;
    long updatedTime = 0L;

    /*private ResideMenu resideMenu;
    private ResideMenuItem itemHome;
    private ResideMenuItem itemAboutUs;
    private ResideMenuItem itemSettings;*/

    //SYSTEM LOG
    private static final String TAG = "Speech Feedback";
    private static final String MAIN = "SYSTEM";
    static final String STATE_VOLUME = "volume selection";
    static final String STATE_PITCH = "pitch selection";
    //static final String STATE_SPEECHRATE = "speech rate selection";
    static final String STATE_MFCC = "mfcc selection";
    static final String STATE_IP = "ip address";
    static final String STATE_Thresh = "0";//yasir
    private static final String Preferences = "setting";

    private AudioDispatcher dispatcher;
    private AndroidAudioPlayer AP;
    private WaveHeader Header;
    private WriterProcessor Processor;
    private Handler customHandler = new Handler();
    private GraphView graph;

    private LineGraphSeries<DataPoint> userSeries1;
    //private LineGraphSeries<DataPoint> userSeries2;
    private static final Random RANDOM = new Random();
    private int lastX = 0;
    //private int lastY = 0;

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
    private int thresh = 0;//yasir
    private int q_count = 0;//yasir
    private int q_nmbr = 0;//yasir

    //MFCC centerfrequency Audio DispatcherFactory
    /*AudioDispatcher connected to the default microphone.
     * AudioSource.MIC | AudioFormat CHANNEL_IN_MONO | ENCODING_PCM_16BIT
     * @param sampleRate
     *            The requested sample rate.
     * @param audioBufferSize
     *            The size of the audio buffer (in samples).
     * @param bufferOverlap
     *            The size of the overlap (in samples).
     */
    private int audioBufferSize = 1024;
    private int sampleRate = 16000;
    private int bufferOverlap = 0;
    private MFCC mfcc = new MFCC(audioBufferSize, sampleRate);
    public static final int SAMPLE_RATE = 16000; //44100

    //Output WAVfile
   // private RandomAccessFile randomAccessFile;
    //TarsosDSPAudioFormat audioFormat;
    //RandomAccessFile output;
    private int audioLength = 0;
    private short Format;
    private short NumChannels;
    private int SampleRate;
    private short BitsPerSample;
    private int NumBytes;
    // Output file path
    private String          filePath = null;

    //for gender
    private boolean gender = true;  //female = true, male = false

    private Socket mSocket;
    {
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single);
        Log.d(MAIN, "System Initialized... ");
        verifyStoragePermissions(this); //new

        //Vol
        volumeLabel = (TextView) findViewById(R.id.volume);
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
        //ques = (TextView) findViewById(R.id.question); //yasir

        //Graph
        graph = (GraphView) findViewById(R.id.graph);
        graph.setTitleColor(Color.WHITE);

        //data to plot
        userSeries1 = new LineGraphSeries<DataPoint>();
        //userSeries2 = new LineGraphSeries<DataPoint>();
        graph.addSeries(userSeries1);
        //graph.addSeries(userSeries2);

        // customize - viewport
        Viewport viewport = graph.getViewport();
        viewport.setYAxisBoundsManual(true);
        viewport.setMinY(-1);
        viewport.setMaxY(1200);
        viewport.setScrollable(true);
        //viewport.setScalable(true);

        GridLabelRenderer labelGraph = graph.getGridLabelRenderer();
        labelGraph.setHorizontalAxisTitle("Time (s)");
        labelGraph.setVerticalAxisTitle("Pitch (Hz)");
        labelGraph.setTextSize(6);
        labelGraph.setVerticalAxisTitleColor(Color.parseColor("#C0C0C0"));
        labelGraph.setHorizontalAxisTitleColor(Color.parseColor("#C0C0C0"));
        labelGraph.setGridColor(Color.parseColor("#C0C0C0"));
        labelGraph.setGridStyle(GridLabelRenderer.GridStyle.BOTH);
        labelGraph.setHorizontalLabelsColor(Color.parseColor("#C0C0C0"));
        labelGraph.setVerticalLabelsColor(Color.parseColor("#C0C0C0"));
        labelGraph.setHorizontalAxisTitleTextSize(6);
        labelGraph.setVerticalAxisTitleTextSize(6);
    /*
        // attach to current activity;
        resideMenu = new ResideMenu(this);
        resideMenu.setUse3D(true);
        resideMenu.setBackground(R.drawable.background);
        resideMenu.attachToActivity(this);
        // create menu items;
        itemHome     = new ResideMenuItem(this, R.drawable.icon_home,     "Home");
        itemSettings = new ResideMenuItem(this, R.drawable.icon_settings, "Settings");
        itemAboutUs  = new ResideMenuItem(this, R.drawable.icon_profile,  "About us");

        itemHome.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent sociometrics = new Intent(SingleActivity.this, MainActivity.class);
                startActivity(sociometrics);
                finish();
            }
        });
        itemSettings.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent settings = new Intent(SociometricsActivity.this, SettingsActivity.class);
                startActivity(settings);
            }
        });
        itemAboutUs.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent aboutus = new Intent(SociometricsActivity.this, AboutActivity.class);
                startActivity(aboutus);
            }
        });

        resideMenu.addMenuItem(itemHome, ResideMenu.DIRECTION_LEFT);
        resideMenu.addMenuItem(itemSettings, ResideMenu.DIRECTION_LEFT);
        resideMenu.addMenuItem(itemAboutUs, ResideMenu.DIRECTION_LEFT);
        resideMenu.setSwipeDirectionDisable(ResideMenu.DIRECTION_RIGHT);

        findViewById(R.id.title_bar_left_menu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resideMenu.openMenu(ResideMenu.DIRECTION_LEFT);
            }
        }); */

        initRecorder(); //initialise audio record setting
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        button = (ImageButton) findViewById(R.id.button);
        timerValue = (TextView) findViewById(R.id.timerValue);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (!mIsRecording) {
                    //Timer initalised state
                    timeSwapBuff = 0L;
                    startTime = SystemClock.uptimeMillis();
                    customHandler.postDelayed(updateTimerThread, 0);
                    //Vibrate 250ms for start activity
                    Toast.makeText(SingleActivity.this, startRecordingLabel,
                            Toast.LENGTH_SHORT).show();
                    Vibrator vi = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                    vi.vibrate(250);

                    startFeedback();
                    //startWriter();

                    mIsRecording = true; //false initialise becomes true as click
                    //mRecorder.startRecording();
                    mRecording = getFile("raw");
                    startBufferedWrite(mRecording);
                } else {
                    //Time stop state and vibrate pulse
                    timeSwapBuff += timeInMilliseconds;
                    //Stop Timer
                    customHandler.removeCallbacks(updateTimerThread);
                    Toast.makeText(SingleActivity.this, stopRecordingLabel,
                            Toast.LENGTH_SHORT).show();
                    Vibrator vi = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                    long[] pattern = {0, 300, 100, 300};
                    vi.vibrate(pattern, -1); //-1 is important

                    stopFeedback();
                    //stopWriter();

                    mIsRecording = false; //true becomes false
                    //mRecorder.stop();
                    File waveFile = getFile("wav");
                    try {
                        rawToWave(mRecording, waveFile);
                    } catch (IOException e) {
                        Toast.makeText(SingleActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    Toast.makeText(SingleActivity.this, "File saved as " + waveFile.getName(),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //initialise update timer threading
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
/*
    private void startWriter() {
        Log.d(TAG, "Start writing audio");
        //Writes the ongoing sound to an output

        WriterProcessor wp = new WriterProcessor(audioFormat, output) {
            @Override
            public boolean process(AudioEvent audioEvent) {
                try {
                    audioLength+=audioEvent.getByteBuffer().length;
                    //write audio to the output
                    output.write(audioEvent.getByteBuffer());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return true;
            }
        };
    }

    private void stopWriter() {
        Log.d(TAG, "Stop writing audio");
        Processor.processingFinished();
    }*/


    private void stopFeedback() {
        Log.d(TAG, "stopFeedback ");
        dispatcher.stop();
        dispatcher = null;
        //mSocket.disconnect();
    }

    private void startFeedback() {
        //new method use the tarsos
        Log.d(TAG, "startFeedback ");

        dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(sampleRate, audioBufferSize, bufferOverlap);

        PitchDetectionHandler pdh = new PitchDetectionHandler() {
            @Override
            public void handlePitch(PitchDetectionResult result, AudioEvent audioEvent) {

                //Calculating MFCC
                float[] mfcc_buffer;
                float[] mfcc_val;
                float sum = 0;
                mfcc_buffer = audioEvent.getFloatBuffer();
                //time = audioEvent.getTimeStamp();
                //timeend = audioEvent.getEndTimeStamp();
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
                //final float finalmfcc_val_float = min_mfcc;

                //Calculating SPL value in dB
                final Double dbSPLValue = calculate(audioEvent.getFloatBuffer());
                Vol = dbSPLValue + 85.0;  //+70.0   //!
                //default silence threshold = -70db
                //Getting Pitch Frequency in Hz
                pitchInHz = result.getPitch();     //final float return pitch in Hz
                //probability = result.getProbability(); //setProbability(float)
                //isPitch = result.isPitched(); //setPitched(boolean pitch)


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

                        if (Vol >= 0 && Vol <= thresh - 25) {
                            volume.setProgressColor(Color.parseColor("#8dcdc1"));//35
                        } else if (Vol > thresh - 25 && Vol <= thresh - 15) {
                            volume.setProgressColor(Color.parseColor("#fff5c3"));
                        } else if (Vol > thresh - 15 && Vol <= thresh) {
                            volume.setProgressColor(Color.parseColor("#d3e397"));
                        }
                        if (Vol > thresh) {
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

                       }//yasir*/

                        volume.setProgress((float) ((Vol - 30.0) / 40) * 100);
                        DecimalFormat df = new DecimalFormat("#.######");
                        Log.i(TAG, "updateVOLprogress " + Vol);
                        /*error
                        //Vol = Double.valueOf(df.format(Vol));
                        //String message = Double.toString(Vol) + " " + Float.toString(pitchInHz) + " " + Float.toString(finalmfcc_val_float) ;
                        //mSocket.emit("new message", message);
                        res_mfcc.setText(Float.toString(finalmfcc_val_float)); */

                        userSeries1.appendData(new DataPoint(lastX++, pitchInHz), true, 20); //(x,y) plot graph series user 1

                        lowLevFeatures[count][0] = (float) Vol;
                        lowLevFeatures[count][1] = pitchInHz;

                        if (count < updateRate - 1) {
                            count++;
                        } else {
                            LayoutInflater inflater = getLayoutInflater();
                            float avgVOl = classificationData(lowLevFeatures)[0][0];
                            float avgPITCH = classificationData(lowLevFeatures)[0][1];

                            Log.i(TAG, Float.toString(avgVOl));
                            Log.i(TAG, Float.toString(avgPITCH));

                            if(avgPITCH >=85 && avgPITCH <= 165){
                                gender = false;
                                //Toast.makeText(getApplicationContext(), "Are you a gentleman?", Toast.LENGTH_SHORT).show();
                            }else if (avgPITCH >=165 && avgPITCH <= 255){
                                gender = true;
                                //Toast.makeText(getApplicationContext(), "Are you a lady?", Toast.LENGTH_SHORT).show();
                            }
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
                            count = 0;
                        }
                    }


                });
            }
        };
        //previous sampleRate:22050 current 8000
        AudioProcessor p = new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN, sampleRate, audioBufferSize, pdh);
        dispatcher.addAudioProcessor(p);
        new Thread(dispatcher, "Audio Dispatcher").start();
    }

    private Double calculate(float[] floatBuffer) {

        double power = 0.0D;
        for (float element : floatBuffer) {
            power += element * element;
        }
        double value = Math.pow(power, 0.5) / floatBuffer.length;
        ;
        return 20.0 * Math.log10(value);
    }

    private float[][] classificationData(float[][] LLF) {
        float avgVol = 0;
        float avgPitch = 0;

        int avgCount = 0;

        for (int i = 0; i < updateRate - 1; i++) {
            if (LLF[i][1] > 0) {
                avgVol = avgVol + LLF[i][0];
                avgPitch = avgPitch + LLF[i][1];
                avgCount++;
            }
        }
        //only take avg of the vol/pitch with pitch > 0
        if (avgCount != 0) {
            avgVol = (float) (avgVol / avgCount); //normalized average volume
            avgPitch = (float) (avgPitch / avgCount); //normalized average pitch
        } else {
            avgVol = 0;
            avgPitch = 0;
        }

        float[][] lowLevFeaturesResult = {{avgVol, avgPitch}};
        Log.i(TAG, "average volume: " + String.valueOf(lowLevFeaturesResult[0][0]) + ", average pitch:" + String.valueOf(lowLevFeaturesResult[0][1]));

        return lowLevFeaturesResult;
    }

//initialise recorder
    private void initRecorder() {
        int audioBufferSize = AudioRecord.getMinBufferSize(sample_RATE, channelConfig,
                AudioFormat.ENCODING_PCM_16BIT);
        mBuffer = new short[audioBufferSize];
        mRecorder = new AudioRecord(audioSource, sample_RATE, channelConfig,
                aFormat, audioBufferSize);
    }

    private void startBufferedWrite(final File file) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                DataOutputStream output = null;
                try {
                    output = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
                    while (mIsRecording) {
                        double sum = 0;
                        int readSize = mRecorder.read(mBuffer, 0, mBuffer.length);
                        for (int i = 0; i < readSize; i++) {
                            output.writeShort(mBuffer[i]);
                            sum += mBuffer[i] * mBuffer[i];
                        }
                        if (readSize > 0) {
                            final double amplitude = sum / readSize;
                            mProgressBar.setProgress((int) Math.sqrt(amplitude));
                            //max RMS 4000
                        }
                    }
                } catch (IOException e) {
                    Toast.makeText(SingleActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                } finally {
                    mProgressBar.setProgress(0);
                    if (output != null) {
                        try {
                            output.flush();
                        } catch (IOException e) {
                            Toast.makeText(SingleActivity.this, e.getMessage(), Toast.LENGTH_SHORT)
                                    .show();
                        } finally {
                            try {
                                output.close();
                            } catch (IOException e) {
                                Toast.makeText(SingleActivity.this, e.getMessage(), Toast.LENGTH_SHORT)
                                        .show();
                            }
                        }
                    }
                }
            }
        }).start();
    }

    private void rawToWave(final File rawFile, final File waveFile) throws IOException {

        byte[] rawData = new byte[(int) rawFile.length()];
        DataInputStream input = null;
        try {
            input = new DataInputStream(new FileInputStream(rawFile));
            input.read(rawData);
        } finally {
            if (input != null) {
                input.close();
            }
        }

        DataOutputStream output = null;
        try {
            output = new DataOutputStream(new FileOutputStream(waveFile));
            // WAVE header
            // see http://ccrma.stanford.edu/courses/422/projects/WaveFormat/
            writeString(output, "RIFF"); // chunk id
            writeInt(output, 36 + rawData.length); // chunk size
            writeString(output, "WAVE"); // format
            writeString(output, "fmt "); // subchunk 1 id
            writeInt(output, 16); // subchunk 1 size
            writeShort(output, (short) 1); // audio format (1 = PCM)
            writeShort(output, (short) 1); // number of channels
            writeInt(output, SAMPLE_RATE); // sample rate
            writeInt(output, SAMPLE_RATE * 2); // byte rate
            writeShort(output, (short) 2); // block align
            writeShort(output, (short) 16); // bits per sample
            writeString(output, "data"); // subchunk 2 id
            writeInt(output, rawData.length); // subchunk 2 size
            // Audio data (conversion big endian -> little endian)
            short[] shorts = new short[rawData.length / 2];
            ByteBuffer.wrap(rawData).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts);
            ByteBuffer bytes = ByteBuffer.allocate(shorts.length * 2);
            for (short s : shorts) {
                bytes.putShort(s);
            }
            output.write(bytes.array());
        } finally {
            if (output != null) {
                output.close();
            }
        }
    }

    private File getFile(final String suffix) {
        Time time = new Time();
        time.setToNow(); // identify the time
        //Initialise new folder directory
        String sep = File.separator; // Use this instead of hardcoding the "/"
        String newFolder = "recording";
        String extStorageDirectory = Environment.getExternalStorageDirectory().toString();
        File myNewFolder = new File(extStorageDirectory + sep + newFolder);
        myNewFolder.mkdir();

        return new File(Environment.getExternalStorageDirectory().toString()
                + sep + newFolder + sep + "recording " + time.format("%H%M%S") + "." + suffix); //saving filename to specific directory
    }

    private void writeInt(final DataOutputStream output, final int value) throws IOException {
        output.write(value >> 0);
        output.write(value >> 8);
        output.write(value >> 16);
        output.write(value >> 24);
    }

    private void writeShort(final DataOutputStream output, final short value) throws IOException {
        output.write(value >> 0);
        output.write(value >> 8);
    }

    private void writeString(final DataOutputStream output, final String value) throws IOException {
        for (int i = 0; i < value.length(); i++) {
            output.write(value.charAt(i));
        }
    }



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
    }

    //permission
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };


/*
    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences settings = this.getSharedPreferences(Preferences, Context.MODE_PRIVATE);
        Boolean restoredVol = settings.getBoolean(STATE_VOLUME, true);
        Boolean restoredPitch = settings.getBoolean(STATE_PITCH, true);
        //Boolean restoredSR = settings.getBoolean(STATE_SPEECHRATE, true);
        Boolean restoredMFCC = settings.getBoolean(STATE_MFCC, true);
        String restoredIP = settings.getString(STATE_IP, "IP");
        String restoredth=settings.getString(STATE_Thresh, "60");//yasir
        thresh=Integer.parseInt(restoredth);//yasir
        try{
            mSocket = IO.socket("http://"+restoredIP+":3000");//LWN: 172.22.185.215 Home:
        }catch(URISyntaxException e){
            throw new RuntimeException(e);
        }

        //respond according to settings
        if(restoredVol == true){
            volumeLabel.setVisibility(View.VISIBLE);
//            textViewV.setVisibility(View.VISIBLE);
            volume.setVisibility(View.VISIBLE);
            textVolume.setVisibility(View.VISIBLE);
            res_textVol.setVisibility(View.VISIBLE);
            lblvolume.setVisibility(View.VISIBLE);
            lblresvol.setVisibility(View.VISIBLE);
            //Log.i(TAG, "Show");
        } else {
            volumeLabel.setVisibility(View.INVISIBLE);
//            textViewV.setVisibility(View.INVISIBLE);
            volume.setVisibility(View.INVISIBLE);
            textVolume.setVisibility(View.INVISIBLE);
            res_textVol.setVisibility(View.INVISIBLE);
            lblvolume.setVisibility(View.INVISIBLE);
            lblresvol.setVisibility(View.INVISIBLE);
            //Log.i(TAG, "Not show");
        }

        if(restoredPitch == true){
            textPitch.setVisibility(View.VISIBLE);
            res_textPitch.setVisibility(View.VISIBLE);
            lblpitch.setVisibility(View.VISIBLE);
            lblrespitch.setVisibility(View.VISIBLE);
            graph.setVisibility(View.VISIBLE);
        }else{
            textPitch.setVisibility(View.INVISIBLE);
            res_textPitch.setVisibility(View.INVISIBLE);
            lblpitch.setVisibility(View.INVISIBLE);
            lblrespitch.setVisibility(View.INVISIBLE);
            graph.setVisibility(View.INVISIBLE);
        }

        if(restoredPitch == true){
            textPitch.setVisibility(View.VISIBLE);
            res_textPitch.setVisibility(View.VISIBLE);
            lblpitch.setVisibility(View.VISIBLE);
            lblrespitch.setVisibility(View.VISIBLE);
            graph.setVisibility(View.VISIBLE);
        }else{
            textPitch.setVisibility(View.INVISIBLE);
            res_textPitch.setVisibility(View.INVISIBLE);
            lblpitch.setVisibility(View.INVISIBLE);
            lblrespitch.setVisibility(View.INVISIBLE);
            graph.setVisibility(View.INVISIBLE);
        }

        if(restoredMFCC == true){
            lblmfcc.setVisibility(View.VISIBLE);
            res_mfcc.setVisibility(View.VISIBLE);
        }else{
            lblmfcc.setVisibility(View.INVISIBLE);
            res_mfcc.setVisibility(View.INVISIBLE);
        }
    }


    public void filePrepare(){
        //prepare and write file header
        BitsPerSample = Header.setBitsPerSample();
        Format = Header.setFormat();
        NumBytes = Header.setNumBytes();
        NumChannels = Header.setNumChannels();
        SampleRate = Header.setSampleRate();

        InputStream input = null;
        //Read and initialize a WaveHeader
        Header.read(InputStream input);

        OutputStream output = null
        //Write a WAV file header
        Header.write(OutputStream output);


        /*BitsPerSample = Header.getBitsPerSample();
        Format = Header.getFormat();
        NumBytes = Header.getNumBytes();
        NumChannels = Header.getNumChannels();
        SampleRate = Header.getSampleRate();*/
    //}
    @Override
    protected void onPause() {
        super.onPause();
        dispatcher.stop();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(MAIN, "onDestory ");
        //mRecorder.release();
        dispatcher.stop();
        dispatcher = null;

        //mSocket.disconnect();
    }
}