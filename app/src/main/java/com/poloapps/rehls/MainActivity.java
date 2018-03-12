package com.poloapps.rehls;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    final Handler handler = new Handler();
    Timer timer = new Timer();

    TimerTask task = new TimerTask() {
        @Override
        public void run() {
            handler.post(new Runnable() {
                public void run() {
                    try {
                        Toast.makeText(getBaseContext(), "task started",
                                Toast.LENGTH_SHORT).show();
                        setCurrentPosition();
                    } catch (Exception e) {
                        // error, do something
                    }
                }
            });
        }
    };

    ProgressDialog mDialog;
    VideoView      videoView;
    ImageButton    btnPlayPause;

    int    stopPosition = 0;
    int    currPosition = 0;
    int        duration = 0;
    String timeDuration = "0:00";
    String currDuration = "0:00";

    String SourceURL = "https://s3.amazonaws.com/interview-quiz-stuff/tos/master.m3u8";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        timer.schedule(task, 100 , 1000);  // interval of .5 sec
        final TextView durationTime = findViewById(R.id.duration_time);
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
                        task.run();
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
                        task.cancel();
                        setCurrentPosition();
                        Toast.makeText(getBaseContext(), "task canceled",
                                Toast.LENGTH_SHORT).show();
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
                        if(duration < 60){
                            timeDuration = "0:" + duration;
                        }else{
                            int mins = duration / 60 ;
                            int sec  = duration % 60 ;
                            timeDuration = mins + ":" + sec;
                        }
                        durationTime.setText(timeDuration);
                        videoView.start();
                        btnPlayPause.setImageResource(R.drawable.ic_pause);
                    }
                });
            }
        });


    }
    private void setCurrentPosition(){
        final TextView currentTime = findViewById(R.id.current_time);
        currPosition = videoView.getCurrentPosition() / 1000;
        if (currPosition < 60) {
            currDuration = "0:" + currPosition;
        } else {
            int mins = currPosition / 60;
            int sec = currPosition % 60;
            currDuration = mins + ":" + sec;
        }
        currentTime.setText(currDuration);
    }
    @Override
    public void onPause() {
        super.onPause();
        task.cancel();
    }

}
