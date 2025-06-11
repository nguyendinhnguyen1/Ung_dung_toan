package com.example.toancap1;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

public class GameActivity extends AppCompatActivity {

    TextView txtScore, txtQuestion;
    Button btn1, btn2, btn3;
    int score = 0;
    int correctAnswer;
    Random random = new Random();

    private SoundPool soundPool;
    private int clickSoundId;
    private int correctSoundId;
    private int wrongSoundId;
    private MediaPlayer backgroundMusic;
    private Animation buttonClickAnim;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        txtScore = findViewById(R.id.btn_diem);
        txtQuestion = findViewById(R.id.btn_cauhoi);
        btn1 = findViewById(R.id.btn_dapan1);
        btn2 = findViewById(R.id.btn_dapan2);
        btn3 = findViewById(R.id.btn_dapan3);
        ImageButton btnHome = findViewById(R.id.btn_home);
        Button btnSaveScore = findViewById(R.id.btn_luudiem);

        // Load animation
        buttonClickAnim = AnimationUtils.loadAnimation(this, R.anim.animation);

        // Init SoundPool
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(3)
                .setAudioAttributes(audioAttributes)
                .build();

        clickSoundId = soundPool.load(this, R.raw.click, 1);
        correctSoundId = soundPool.load(this, R.raw.tich, 1);
        wrongSoundId = soundPool.load(this, R.raw.click, 1);

        // Background music
        backgroundMusic = MediaPlayer.create(this, R.raw.atchinh);
        backgroundMusic.setLooping(true);
        backgroundMusic.setVolume(0.5f, 0.5f);
        backgroundMusic.start();

        generateQuestion();

        btnHome.setOnClickListener(v -> {
            v.startAnimation(buttonClickAnim);
            soundPool.play(clickSoundId, 1, 1, 0, 0, 1);
            stopMusic();
            Intent intent = new Intent(GameActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        btnSaveScore.setOnClickListener(v -> {
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

        View.OnClickListener answerClickListener = view -> {
            view.startAnimation(buttonClickAnim);
            Button clicked = (Button) view;
            int selected = Integer.parseInt(clicked.getText().toString());

            if (selected == correctAnswer) {
                soundPool.play(correctSoundId, 1, 1, 0, 0, 1);
                score++;
                txtScore.setText(String.valueOf(score));
                generateQuestion();
            } else {
                soundPool.play(wrongSoundId, 1, 1, 0, 0, 1);
                stopMusic();
                Intent intent = new Intent(GameActivity.this, GameOverActivity.class);
                intent.putExtra("score", score);
                intent.putExtra("question", txtQuestion.getText().toString());
                intent.putExtra("correctAnswer", correctAnswer);
                startActivity(intent);
                finish(); // kết thúc luôn để tránh bị đè
            }
        };

        btn1.setOnClickListener(answerClickListener);
        btn2.setOnClickListener(answerClickListener);
        btn3.setOnClickListener(answerClickListener);
    }

    private void generateQuestion() {
        int a = random.nextInt(10) + 1;
        int b = random.nextInt(10) + 1;
        String[] ops = {"+", "-", "*", "/"};
        String op = ops[random.nextInt(ops.length)];

        switch (op) {
            case "+":
                correctAnswer = a + b;
                txtQuestion.setText(a + " + " + b + " = ?");
                break;
            case "-":
                correctAnswer = a - b;
                txtQuestion.setText(a + " - " + b + " = ?");
                break;
            case "*":
                correctAnswer = a * b;
                txtQuestion.setText(a + " × " + b + " = ?");
                break;
            case "/":
                b = random.nextInt(9) + 1;
                correctAnswer = a;
                int result = a * b;
                txtQuestion.setText(result + " / " + b + " = ?");
                break;
        }

        int correctPosition = random.nextInt(3);
        int wrong1 = correctAnswer + random.nextInt(4) + 1;
        int wrong2 = correctAnswer - (random.nextInt(4) + 1);

        if (wrong1 == correctAnswer) wrong1 += 2;
        if (wrong2 == correctAnswer) wrong2 -= 2;

        Button[] buttons = {btn1, btn2, btn3};
        buttons[correctPosition].setText(String.valueOf(correctAnswer));
        buttons[(correctPosition + 1) % 3].setText(String.valueOf(wrong1));
        buttons[(correctPosition + 2) % 3].setText(String.valueOf(wrong2));
    }

    private void stopMusic() {
        if (backgroundMusic != null && backgroundMusic.isPlaying()) {
            backgroundMusic.stop();
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
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
        stopMusic();
    }
}
