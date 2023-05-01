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
import androidx.room.Room;

import java.util.Locale;

public class SubActivity extends AppCompatActivity {

    private UserDao mUserDao;
    //Button speedup, speeddown, goback; server1, server2;
    ImageButton server1, server2, goback, help;
    Switch serverselect;
    SeekBar ttsbar;
    TextToSpeech tts;
    float Subttsspeed;
    String server;
    //private RadioGroup rg;
    //private RadioButton rb1, rb2;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub);

        UserDatabase database = Room.databaseBuilder(getApplicationContext(), UserDatabase.class, "db")
                .fallbackToDestructiveMigration()
                .allowMainThreadQueries()
                .build();

        mUserDao = database.userDao();

        // 서버 설정 값 가져오기
        server = mUserDao.getServer();
        Subttsspeed = mUserDao.getTtsSpeed();


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

                    Subttsspeed = mUserDao.getTtsSpeed();
                    // ttsbar 프로그레스 값 설정
                    ttsbar.setProgress((int) Subttsspeed);

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
                User user2 = new User();
                user2.setId(1);
                user2.setServer("barcode");
                user2.setTtsspeed(Subttsspeed);

                mUserDao.setUpdateUser(user2);

                tts.speak("바코드 정보가 적지만 속도가 빠릅니다.", TextToSpeech.QUEUE_FLUSH, null);
                finish();
                startActivity(intent);
            }
        });
        server2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SubActivity.this, MainActivity.class);
                User user3 = new User();
                user3.setId(1);
                user3.setServer("barcode2");
                user3.setTtsspeed(Subttsspeed);

                mUserDao.setUpdateUser(user3);

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

                User user4 = new User();
                user4.setId(1);
                user4.setServer(mUserDao.getServer());
                user4.setTtsspeed(mUserDao.getTtsSpeed());

                mUserDao.setUpdateUser(user4);

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

                    User user5 = new User();
                    user5.setId(1);
                    user5.setServer(mUserDao.getServer());
                    user5.setTtsspeed(1.0f);

                    mUserDao.setUpdateUser(user5);

                    tts.setSpeechRate(Subttsspeed);
                    tts.speak("가장 느린 속도입니다.", TextToSpeech.QUEUE_FLUSH, null);
                    ttsbar.setProgress(1);
                }
                else{
                    tts.setSpeechRate(Subttsspeed);
                    tts.speak("tts 속도가" + Subttsspeed + "으로 변경되었습니다.", TextToSpeech.QUEUE_FLUSH, null);

                    User user6 = new User();
                    user6.setId(1);
                    user6.setServer(mUserDao.getServer());
                    user6.setTtsspeed(Subttsspeed);

                    mUserDao.setUpdateUser(user6);
                }


            }
        });




    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        tts.speak("바코드 인식 화면으로 이동합니다.", TextToSpeech.QUEUE_FLUSH, null);
        Intent intent = new Intent(SubActivity.this, MainActivity.class);

        User user7 = new User();
        user7.setId(1);
        user7.setServer(mUserDao.getServer());
        user7.setTtsspeed(mUserDao.getTtsSpeed());

        mUserDao.setUpdateUser(user7);

        finish();
        startActivity(intent);

        return false;
    }

}