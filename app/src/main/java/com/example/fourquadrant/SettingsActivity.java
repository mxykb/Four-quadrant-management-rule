package com.example.fourquadrant;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class SettingsActivity extends AppCompatActivity {
    
    private SeekBar maxScoreSeekBar;
    private TextView maxScoreValue;
    private SeekBar chartSizeSeekBar;
    private TextView chartSizeValue;
    private Button saveButton;
    private Button resetButton;
    
    private SharedPreferences preferences;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        
        preferences = getSharedPreferences("QuadrantSettings", MODE_PRIVATE);
        
        initViews();
        loadSettings();
        setupListeners();
    }
    
    private void initViews() {
        maxScoreSeekBar = findViewById(R.id.max_score_seekbar);
        maxScoreValue = findViewById(R.id.max_score_value);
        chartSizeSeekBar = findViewById(R.id.chart_size_seekbar);
        chartSizeValue = findViewById(R.id.chart_size_value);
        saveButton = findViewById(R.id.save_settings_button);
        resetButton = findViewById(R.id.reset_settings_button);
    }
    
    private void loadSettings() {
        int maxScore = preferences.getInt("max_score", 10);
        int chartSize = preferences.getInt("chart_size", 300);
        
        maxScoreSeekBar.setProgress(maxScore);
        maxScoreValue.setText(String.valueOf(maxScore));
        
        chartSizeSeekBar.setProgress((chartSize - 200) / 10); // 200-500范围
        chartSizeValue.setText(chartSize + "dp");
    }
    
    private void setupListeners() {
        maxScoreSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                maxScoreValue.setText(String.valueOf(progress));
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        
        chartSizeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int chartSize = 200 + progress * 10;
                chartSizeValue.setText(chartSize + "dp");
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSettings();
                Toast.makeText(SettingsActivity.this, "设置已保存", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
        
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetSettings();
                Toast.makeText(SettingsActivity.this, "设置已重置", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void saveSettings() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("max_score", maxScoreSeekBar.getProgress());
        editor.putInt("chart_size", 200 + chartSizeSeekBar.getProgress() * 10);
        editor.apply();
    }
    
    private void resetSettings() {
        maxScoreSeekBar.setProgress(10);
        maxScoreValue.setText("10");
        
        chartSizeSeekBar.setProgress(10); // 300dp
        chartSizeValue.setText("300dp");
    }
} 