package com.mallotec.reb.learncustomview;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.mallotec.reb.widget.PrinterTextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final PrinterTextView printerTextView = findViewById(R.id.textView);
        printerTextView.setTextAnimationListener(new PrinterTextView.TextAnimationListener() {
            @Override
            public void finish() {
                Toast.makeText(MainActivity.this, "AnimationFinished", Toast.LENGTH_LONG).show();
            }
        });
        printerTextView.startPrintAnimation();

        Button btStop = findViewById(R.id.bt_stop);
        btStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                printerTextView.showAllText();
            }
        });
        Button btStart = findViewById(R.id.bt_start);
        btStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { printerTextView.startPrintAnimation();
            }
        });
    }
}
