package com.example.toancap1;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class GameOverActivity extends AppCompatActivity {

    TextView txtDiem, txtCauHoi;
    Button btnLuuDiem;
    ImageButton btnQuayLai, btnHome;

    private SoundPool soundPool;
    private int clickSoundId;
    private MediaPlayer backgroundMusic;
    private Animation buttonClickAnim;

    int score;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_over);

        // Gán View từ layout
        txtDiem = findViewById(R.id.btn_diem);
        txtCauHoi = findViewById(R.id.btn_cauhoi);
        btnLuuDiem = findViewById(R.id.btn_luudiem);
        btnQuayLai = findViewById(R.id.btn_quaylai);
        btnHome = findViewById(R.id.btn_home);

        // Load animation
        buttonClickAnim = AnimationUtils.loadAnimation(this, R.anim.animation);

        // Khởi tạo SoundPool
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(1)
                .setAudioAttributes(audioAttributes)
                .build();

        clickSoundId = soundPool.load(this, R.raw.click, 1);

        // Phát nhạc nền (không lặp lại, phát chậm)
        backgroundMusic = MediaPlayer.create(this, R.raw.gameover);
        backgroundMusic.setLooping(false);
        backgroundMusic.setVolume(0.5f, 0.5f);

        // Giảm tốc độ nếu thiết bị hỗ trợ (API >= 23)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            PlaybackParams params = new PlaybackParams();
            params.setSpeed(0.9f); // 80% tốc độ gốc = chậm hơn
            backgroundMusic.setPlaybackParams(params);
        }

        backgroundMusic.start();

        // Nhận dữ liệu từ GameActivity
        Intent intent = getIntent();
        score = intent.getIntExtra("score", 0);
        String question = intent.getStringExtra("question");
        int correctAnswer = intent.getIntExtra("correctAnswer", 0);

        if (question != null && question.endsWith("?")) {
            question = question.substring(0, question.length() - 1);
        }

        String fullQuestion = question + correctAnswer;
        txtCauHoi.setText(fullQuestion);
        txtDiem.setText(String.valueOf(score));

        // Sự kiện nút "Quay lại" -> chơi lại
        btnQuayLai.setOnClickListener(v -> {
            v.startAnimation(buttonClickAnim);
            soundPool.play(clickSoundId, 1, 1, 0, 0, 1);
            stopMusic();
            startActivity(new Intent(GameOverActivity.this, GameActivity.class));
            finish();
        });

        // Sự kiện nút "Home" -> về màn chính
        btnHome.setOnClickListener(v -> {
            v.startAnimation(buttonClickAnim);
            soundPool.play(clickSoundId, 1, 1, 0, 0, 1);
            stopMusic();
            startActivity(new Intent(GameOverActivity.this, MainActivity.class));
            finish();
        });

        // Sự kiện nút "Lưu điểm"
        btnLuuDiem.setOnClickListener(v -> {
            v.startAnimation(buttonClickAnim);
            soundPool.play(clickSoundId, 1, 1, 0, 0, 1);
            SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
            int savedScore = prefs.getInt("saved_score", 0);
            if (score > savedScore) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt("saved_score", score);
                editor.apply();
            }
        });
    }

    private void stopMusic() {
        if (backgroundMusic != null) {
            if (backgroundMusic.isPlaying()) {
                backgroundMusic.stop();
            }
            backgroundMusic.release();
            backgroundMusic = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (backgroundMusic != null && backgroundMusic.isPlaying()) {
            backgroundMusic.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (backgroundMusic != null && !backgroundMusic.isPlaying()) {
            backgroundMusic.start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopMusic();
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
    }
}
