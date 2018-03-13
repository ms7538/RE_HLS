package com.poloapps.rehls;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;


//git check
public class MainActivity extends AppCompatActivity {
    Handler           mHandler;
    int               count = 0;
    ProgressDialog    mDialog;
    VideoView         videoView;
    ImageButton       btnPlayPause;

    int    stopPosition = 0;
    int    currPosition = 0;
    int    progPosition = 0;
    int        duration = 1;
    String timeDuration = "0:00";
    String currDuration = "0:00";

    String SourceURL = "https://s3.amazonaws.com/interview-quiz-stuff/tos/master.m3u8";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }
    @Override
    public void onResume() {
        super.onResume();

        final TextView durationTime = findViewById(R.id.duration_time);
        //final ProgressBar progressBar = findViewById(R.id.Progressbar);

        videoView    = findViewById(R.id.videoView);
        btnPlayPause = findViewById(R.id.play_pause_btn);
        mDialog      = new ProgressDialog(MainActivity.this );
        mDialog.setMessage("Please Wait ...");
        mDialog.setCanceledOnTouchOutside(false);
        btnPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (!videoView.isPlaying()) {
                        mDialog.show();
                        setCurrentPosition();
                        Uri uri = Uri.parse(SourceURL);
                        videoView.setVideoURI(uri);
                        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mp) {
                                btnPlayPause.setImageResource(R.drawable.ic_play);
                                stopPosition = 0;
                            }
                        });

                    } else {
                        videoView.pause();
                        mHandler.removeCallbacks(mRunnable);
                        stopPosition = videoView.getCurrentPosition();
                        mDialog.dismiss();
                        btnPlayPause.setImageResource(R.drawable.ic_play);
                    }

                } catch (Exception ex) {
                    mDialog.dismiss();
                    ex.printStackTrace();
                    Toast.makeText(getBaseContext(), "error occurred",
                            Toast.LENGTH_SHORT).show();
                }
                videoView.requestFocus();
                videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        mDialog.dismiss();
                        mp.setLooping(true);
                        if(stopPosition > 0) videoView.seekTo(stopPosition);
                        duration = videoView.getDuration() / 1000;
                        if(duration < 10) timeDuration = "0:0" + duration;
                        else if (duration < 60) timeDuration = "0:" + duration;
                        else{
                            int mins = duration / 60 ;
                            int sec  = duration % 60 ;
                            if (sec < 10) timeDuration = mins + ":0" + sec;
                            else timeDuration = mins + ":" + sec;
                        }
                        durationTime.setText(timeDuration);
                        useHandler();
                        videoView.start();
                        btnPlayPause.setImageResource(R.drawable.ic_pause);

                    }
                });
            }
        });
    }

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            setCurrentPosition();
            mHandler.postDelayed(mRunnable, 1000);
        }
    };
    public void useHandler() {
        mHandler = new Handler();
        mHandler.postDelayed(mRunnable, 1000);
    }
    private void setCurrentPosition(){
        count++;
        final ProgressBar progressBar = findViewById(R.id.Progressbar);
        progressBar.setBackgroundColor(Color.CYAN);
        final TextView currentTime = findViewById(R.id.current_time);

        currPosition    = videoView.getCurrentPosition() / 1000;
        progPosition    = currPosition * 100 / duration;
        progressBar.setProgress(progPosition);

        if(currPosition < 10) currDuration = "0:0" + currPosition;
        else if (currPosition < 60) currDuration = "0:" + currPosition;
        else {
            int mins = currPosition / 60;
            int sec = currPosition % 60;
            if (sec < 10) currDuration = mins + ":0" + sec;
            else currDuration = mins + ":" + sec;

        }
        currentTime.setText(currDuration);
    }
    @Override
    public void onPause() {
        super.onPause();
        try {
            mHandler.removeCallbacks(mRunnable);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
