package com.poloapps.rehls;

import android.app.ProgressDialog;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import android.content.SharedPreferences;

//git check
public class VideoPlayActivity extends AppCompatActivity {
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
    Handler mHandler;
    ProgressDialog mDialog;
    VideoView videoView;
    ImageButton btnPlayPause;
    int stopPosition = 0;
    int currPosition = 0;
    int progPosition = 0;
    int duration = 1;

    String timeDuration = "0:00";
    String currDuration = "0:00";
    String selSource_0  = "https://s3.amazonaws.com/interview-quiz-stuff/tos-trailer/master.m3u8";
    String selSource_1  = "https://s3.amazonaws.com/interview-quiz-stuff/tos/master.m3u8";

    String SourceURL = selSource_0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences mSettings = this.getSharedPreferences("Settings", 0);
        setContentView(R.layout.activity_video_view);

       int source = getIntent().getIntExtra("src",0);
       if (source == 1) SourceURL = selSource_1;

       videoView = findViewById(R.id.videoView);
       btnPlayPause = findViewById(R.id.play_pause_btn);
       Boolean autoPlay = mSettings.getBoolean("AP", true);
       if (autoPlay) {
           setDiag();
           play_start();
       }
    }

    @Override
    public void onResume() {
        super.onResume();
        final SeekBar seekBar = findViewById(R.id.Seekbar);
        videoView = findViewById(R.id.videoView);
        btnPlayPause = findViewById(R.id.play_pause_btn);
        setDiag();
        btnPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                play_start();
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                seek(seekBar.getProgress());
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}
        });

    }

    private void seek(int pos){

        if(videoView.getDuration() == -1){
            videoURI();
            videoFocus();
            play_start();
        }

        double posStop = pos * .01;
        stopPosition = (int) (posStop * videoView.getDuration());

        try {
            if (videoView.isPlaying()) {
                mDialog.show();
                setCurrentPosition();
                videoURI();

            } else {

                videoView.pause();
                mHandler.removeCallbacks(mRunnable);
                mDialog.dismiss();
                btnPlayPause.setImageResource(R.drawable.ic_play);
            }

        } catch (Exception ex) {
            mDialog.dismiss();
            ex.printStackTrace();
            Toast.makeText(getBaseContext(), "seek error occurred",
                    Toast.LENGTH_SHORT).show();
        }
        videoFocus();
    }

    private void play_start() {
        try {
            if (!videoView.isPlaying()) {
                mDialog.show();
                setCurrentPosition();
                videoURI();

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
        videoFocus();
        mDialog.dismiss();
    }

    private void setDiag(){
        mDialog = new ProgressDialog(VideoPlayActivity.this);
        mDialog.setMessage("Please Wait ...");
        mDialog.setCanceledOnTouchOutside(false);
    }

    private void videoURI() {
        Uri uri = Uri.parse(SourceURL);
        videoView.setVideoURI(uri);
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                btnPlayPause.setImageResource(R.drawable.ic_play);
                stopPosition = 0;
            }
        });

    }

    private void videoFocus() {
        final TextView durationTime = findViewById(R.id.duration_time);
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


    private void setCurrentPosition(){

        final SeekBar seekBar = findViewById(R.id.Seekbar);
        final TextView currentTime = findViewById(R.id.current_time);
        currPosition    = videoView.getCurrentPosition() / 1000;
        progPosition    = currPosition * 100 / duration;
        seekBar.setProgress(progPosition);

        if      (currPosition < 10) currDuration = "0:0" + currPosition;
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
        SharedPreferences mSettings = this.getSharedPreferences("Settings", 0);
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putBoolean("AP",false);
        editor.apply();
    }

}
