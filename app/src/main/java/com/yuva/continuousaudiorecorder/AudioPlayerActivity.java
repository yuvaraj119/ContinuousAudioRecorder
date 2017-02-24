package com.yuva.continuousaudiorecorder;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.continiousaudiorecorder.AudioPlayerView;

public class AudioPlayerActivity extends AppCompatActivity {

    AudioPlayerView audioPlayerView;
    Button btGetFilePath;
    TextView tvFilePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        audioPlayerView = (AudioPlayerView) findViewById(R.id.audio_player);
        btGetFilePath = (Button) findViewById(R.id.bt_getFilePath);
        tvFilePath = (TextView) findViewById(R.id.tv_filePath);
        btGetFilePath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (audioPlayerView.mAudioRecorder != null) {
                    tvFilePath.setText(audioPlayerView.getFilePath());
                }
            }
        });

    }
}
