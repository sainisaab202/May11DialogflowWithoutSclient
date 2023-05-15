package com.example.may11dialogflowwithoutsclient;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContextWrapper;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {


    final int MICROPHONE_PERMISSION_CODE = 200;
    public static Handler handler = new Handler();
    Handler handlerForUI = new Handler();


    static MediaRecorder mediaRecorder;
    static String recordingFilePath;
    MediaPlayer mediaPlayer;

    TextView tvCurrentAmplitude;
    Thread thread;

    StreamDetectIntent streamDetectIntent;

    int noAudioInput = 3; //use to count 3 times if voice input is not coming
    public static TextView tvResponse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //checking if mic is present and requesting for it's permission
        if (isMicrophonePresent()) {
            getMicrophonePermission();
        }

        tvCurrentAmplitude = findViewById(R.id.tvCurrentAmplitude);
        recordingFilePath = getRecordingFilePath();
        streamDetectIntent = new StreamDetectIntent();
    }

    public void btnRecordPressed(View view) {
        startRecording();
        Toast.makeText(this, "Recording is started", Toast.LENGTH_SHORT).show();
        thread = new Thread(runnable);
        thread.start();
    }

    public void btnStopPressed(View view) {
        thread.interrupt();
        stopRecording();
        Toast.makeText(this, "Recording is stopped", Toast.LENGTH_SHORT).show();
    }

    public static void startRecording() {
        try {
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_WB);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB);
            //this line is just for test
            mediaRecorder.setAudioSamplingRate(16000);
            mediaRecorder.setOutputFile(recordingFilePath);

            mediaRecorder.prepare();
            mediaRecorder.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopRecording() {
        try {
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void playRecording() {

        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(getRecordingFilePath());
            mediaPlayer.prepare();
            mediaPlayer.start();
            Log.e("PlayingRec", Thread.currentThread().getName() + " -for- " + String.valueOf(mediaPlayer.getDuration()));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            //this helping us to repeat this action
            handler.postDelayed(this, 250);
            if (mediaRecorder != null) {
                int maxAmplitude = mediaRecorder.getMaxAmplitude();
//                    Log.e("runnable", Thread.currentThread().getName()+" --with amp "+maxAmplitude);
                if (maxAmplitude > 0) {
                    //checking for any action
                    checkAmplitude(maxAmplitude);

                    handlerForUI.post(new Runnable() {
                        @Override
                        public void run() {
                            //this helping us to repeat this runnable
                            handler.postDelayed(this, 1000);
                            try {
                                if (mediaRecorder != null) {
                                    tvCurrentAmplitude.setText(String.valueOf(mediaRecorder.getMaxAmplitude()));
//                                        Log.e("handlerForUI", Thread.currentThread().getName()+" --with amp "+maxAmplitude);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }
        }
    };

    private void checkAmplitude(int maxAmplitude) {
        if (maxAmplitude < 150) {
            //count 3 times to trigger voiceRepeat
            if (noAudioInput > 0) {
                noAudioInput--;
            } else if (noAudioInput == 0) {
                Log.e("Sending audio", "triggering");
                noAudioInput--;

                //sending request to detectIntent
                sendAudioToDetectIntent();

            } else {
                noAudioInput--;
                //this is how long should we wait before resting current recording with no input voice
                if (noAudioInput < -6) {
                    //if no voice is coming we want to re-record from scratch
                    noAudioInput = -1;
                    stopRecording();
                    startRecording();
                }
            }
        } else {

            //resetting no input voice count
            noAudioInput = 3;
        }
    }

    private void sendAudioToDetectIntent() {
        stopRecording();

        try {
            Log.e("MainActivity", "MA in thread: "+Thread.currentThread().getName());
            streamDetectIntent.detectIntent(getRecordingFilePath(),this);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isMicrophonePresent() {
        if (this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_MICROPHONE)) {
            return true;
        } else {
            return false;
        }
    }

    private void getMicrophonePermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]
                    {Manifest.permission.RECORD_AUDIO}, MICROPHONE_PERMISSION_CODE);
        }
    }

    //get path for storing the file
    private String getRecordingFilePath() {
        ContextWrapper contextWrapper = new ContextWrapper(getApplicationContext());
        File musicDirectory = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_MUSIC);
        File file = new File(musicDirectory, "requestAudio" + ".aac");
        return file.getPath();
    }
}