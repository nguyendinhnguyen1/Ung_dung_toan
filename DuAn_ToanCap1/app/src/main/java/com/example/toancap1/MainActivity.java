package com.example.toancap1;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private TextView txtSavedScore;
    private SoundPool soundPool;
    private int clickSoundId;
    private MediaPlayer backgroundMusic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageButton playButton = findViewById(R.id.play_button);
        txtSavedScore = findViewById(R.id.btn_save);

        // === Khởi tạo SoundPool để phát âm thanh click ===
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(1)
                .setAudioAttributes(audioAttributes)
                .build();

        clickSoundId = soundPool.load(this, R.raw.click, 1);  // File click.mp3 hoặc .wav trong res/raw

        // === Phát nhạc nền ===
        backgroundMusic = MediaPlayer.create(this, R.raw.atchinh); // File atchinh.mp3 trong res/raw
        backgroundMusic.setLooping(true);
        backgroundMusic.setVolume(0.5f, 0.5f);
        backgroundMusic.start();

        // === Tải hiệu ứng thu nhỏ khi click (res/anim/button_click.xml) ===
        Animation clickAnim = AnimationUtils.loadAnimation(this, R.anim.animation);

        // === Sự kiện khi nhấn nút "Chơi" ===
        playButton.setOnClickListener(v -> {
            playButton.startAnimation(clickAnim); // Bắt đầu hiệu ứng
            soundPool.play(clickSoundId, 1, 1, 0, 0, 1); // Phát âm thanh click
            Intent intent = new Intent(MainActivity.this, GameActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Khi trở về màn hình chính, tiếp tục nhạc nền nếu chưa phát
        if (backgroundMusic != null && !backgroundMusic.isPlaying()) {
            backgroundMusic.start();
        }

        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        int savedScore = prefs.getInt("saved_score", 0);
        txtSavedScore.setText("" + savedScore);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (backgroundMusic != null && backgroundMusic.isPlaying()) {
            backgroundMusic.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
        if (backgroundMusic != null) {
            backgroundMusic.release();
            backgroundMusic = null;
        }
    }
}
