package com.example.coed_test;

import static android.speech.tts.TextToSpeech.ERROR;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class SubActivity extends AppCompatActivity {

    //Button speedup, speeddown, goback; server1, server2;
    ImageButton server1, server2, goback, help;
    Switch serverselect;
    SeekBar ttsbar;
    TextToSpeech tts;
    float Subttsspeed;
    //private RadioGroup rg;
    //private RadioButton rb1, rb2;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub);


        //speedup = findViewById(R.id.tts_speed_up);
        //speeddown = findViewById(R.id.tts_speed_down);
        goback = findViewById(R.id.goback);
        //rg =  findViewById(R.id.radioGroup);
        //rb1 = findViewById(R.id.radioButton);
        //rb2 = findViewById(R.id.radioButton2);
        //serverselect = findViewById(R.id.server12);
        server1 = findViewById(R.id.server1);
        server2 = findViewById(R.id.server2);
        ttsbar = findViewById(R.id.seekBar);
        help = findViewById(R.id.help);

        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != ERROR) {

                    Intent intent = getIntent();
                    Subttsspeed = intent.getFloatExtra("mainSpeed", 5.0f);
                    ttsbar.setProgress((int)Subttsspeed);

                    tts.setLanguage(Locale.KOREAN);
                    tts.setSpeechRate(Subttsspeed);
                }
            }
        });

        help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tts.speak("카메라와 인식 대상과의 거리를 10cm에서 30cm 정도로 유지해주세요. 인식 될 때까지 물건을 회전시켜주세요. 손이 바코드를 가리거나, 빛이 반사 될 경우 인식이 어렵습니다.", TextToSpeech.QUEUE_FLUSH, null);
            }
        });
        server1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SubActivity.this, MainActivity.class);
                intent.putExtra("Server", "barcode");
                tts.speak("바코드 정보가 적지만 속도가 빠릅니다.", TextToSpeech.QUEUE_FLUSH, null);
                finish();
                startActivity(intent);
            }
        });
        server2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SubActivity.this, MainActivity.class);
                intent.putExtra("Server", "barcode2");
                tts.speak("바코드 정보가 많지만 속도가 느립니다.", TextToSpeech.QUEUE_FLUSH, null);
                finish();
                startActivity(intent);
            }
        });
        /*serverselect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    Intent intent = new Intent(SubActivity.this, MainActivity.class);
                    intent.putExtra("Server", "barcode2");
                    finish();
                    startActivity(intent);
                }
                else{
                    Intent intent = new Intent(SubActivity.this, MainActivity.class);
                    intent.putExtra("Server", "barcode");
                    finish();
                    startActivity(intent);
                }
            }
        });
        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if(i == R.id.radioButton2){
                    Intent intent = new Intent(SubActivity.this, MainActivity.class);
                    intent.putExtra("Server", "barcode2");
                    finish();
                    startActivity(intent);
                }
                else{
                    Intent intent = new Intent(SubActivity.this, MainActivity.class);
                    intent.putExtra("Server", "barcode");
                    finish();
                    startActivity(intent);
                }

            }
        });*/

        goback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SubActivity.this, MainActivity.class);
                intent.putExtra("Speed", Subttsspeed);
                finish();
                startActivity(intent);
            }
        });

        /*speedup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Subttsspeed = Subttsspeed + 1f;
                tts.setSpeechRate(Subttsspeed);
                tts.speak("tts 속도가" + Subttsspeed.toString() + "으로 증가하였습니다.", TextToSpeech.QUEUE_FLUSH, null);

            }
        });

        speeddown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Subttsspeed = Subttsspeed - 1f;
                tts.setSpeechRate(Subttsspeed);
                tts.speak("tts 속도가" + Subttsspeed.toString() + "으로 감소하였습니다.", TextToSpeech.QUEUE_FLUSH, null);

            }
        });*/
        ttsbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                Subttsspeed = (float)i;

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if(Subttsspeed < 2.0f){
                    tts.speak("가장 느린 속도입니다.", TextToSpeech.QUEUE_FLUSH, null);
                    ttsbar.setProgress(2);
                    Subttsspeed = 2.0f;
                }
                else{
                    tts.speak("tts 속도가" + Subttsspeed + "으로 변경되었습니다.", TextToSpeech.QUEUE_FLUSH, null);
                    tts.setSpeechRate(Subttsspeed);
                }


            }
        });




    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        tts.speak("바코드 인식 화면으로 이동합니다.", TextToSpeech.QUEUE_FLUSH, null);
        Intent intent = new Intent(SubActivity.this, MainActivity.class);
        intent.putExtra("Speed", Subttsspeed);
        finish();
        startActivity(intent);

        return false;
    }

}