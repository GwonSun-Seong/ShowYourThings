package com.ShowYourThings.coed_test;

import static android.speech.tts.TextToSpeech.ERROR;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Size;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.room.Room;

import com.chaquo.python.android.AndroidPlatform;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
//import org.w3c.dom.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.chaquo.python.Python;
import com.chaquo.python.PyObject;

import android.widget.ImageView;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private Python python;
    private UserDao mUserDao;
    private ListenableFuture cameraProviderFuture;
    private ExecutorService cameraExecutor;
    private PreviewView previewView;
    private MyImageAnalyzer analyzer;
    private final long finishtimeed = 2000;
    private long presstime = 0;
    private boolean isResumed = false;
    private static final int CAMERA_PERMISSION_REQUEST = 1000;
    ArrayList<String> list;
    long tempTimeB;

    String linkurl, parsing;
    String server = "barcode";
    String tempbarcode;
    String firstPrice, secondPrice;
    TextToSpeech tts;
    float ttsspeed;

    private ImageView imageView;
    private Animation fadeInAnimation;
    private Animation fadeOutAnimation;

    private SensorManager sensorManager;
    private long mShakeTime;
    private static final float SHAKE_THRESHOLD_GRAVITY = 6f;
    private static final int SHAKE_SKIP_TIME = 1000;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        UserDatabase database = Room.databaseBuilder(getApplicationContext(), UserDatabase.class, "db")
                .fallbackToDestructiveMigration()
                .allowMainThreadQueries()
                .build();

        mUserDao = database.userDao();

        if (!Python.isStarted()) {
            Python.start(new AndroidPlatform(this));
        }

        python = Python.getInstance();

        //데이터 삽입
        // User 객체가 있는지 확인하고, 없으면 생성하여 데이터베이스에 삽입
        if (mUserDao.getUserAll().isEmpty()) {
            User user = new User();
            user.setServer("api1");
            user.setTtsspeed(4.0f);
            mUserDao.setInsertUser(user);

            AlertDialog.Builder builder2 = new AlertDialog.Builder(MainActivity.this);
            builder2.setCancelable(false);
            builder2.setTitle("최초 도움말");
            builder2.setMessage("앱 실행 후 바코드를 촬영하여 바로 사용 가능합니다. \n\n손으로 가리지 않게끔 물체의 가장자리를 잡고 회전시키며 30cm 정도의 거리를 유지할 때 인식이 쉽습니다.");

            builder2.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();

                    AlertDialog.Builder builder3 = new AlertDialog.Builder(MainActivity.this);
                    builder3.setCancelable(false);
                    builder3.setTitle("최초 도움말");
                    builder3.setMessage("현재 서버컴퓨터가 개인컴퓨터이므로 코리안넷과 소비자24를 통한 조회는 상시 사용이 불가능합니다. 조회 불가 시 API를 이용한 인식을 권장합니다.\n\n" +
                            "조회 방법 선택, 기타 TTS 속도 설정 등은 설정화면에서 확인 가능하며 흔들거나, 바코드 인식 후 다이얼로그를 통해 설정화면으로 이동이 가능합니다.");

                    builder3.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            init();
                        }
                    }).show();

                }
            }).show();
        }

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);

        //데이터 조회
        List<User> userList = mUserDao.getUserAll();
//        for(int i=0; i<userList.size(); i++){
//            Log.d("Test", userList.get(i).getServer() + " " + userList.get(i).getTtsspeed());
//        }

       // 데이터 수정
//        User user2 = new User();
//        user2.setId(1);
//        user2.setServer("barcode");
//        user2.setTtsspeed(7.0f);
//
//        mUserDao.setUpdateUser(user2);

        // 데이터 삭제
//        User user3 = new User();
//        user3.setId(2);
//        mUserDao.setDeleteUser(user3);

        // 기존
        list = new ArrayList<String>();

        imageView = findViewById(R.id.image);
        fadeInAnimation = new AlphaAnimation(0.0f, 1.0f);
        fadeInAnimation.setDuration(1000);
        fadeOutAnimation = new AlphaAnimation(1.0f, 0.0f);
        fadeOutAnimation.setDuration(1000);
        fadeOutAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                // 애니메이션 시작 시 작업 수행
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // 애니메이션 종료 시 작업 수행
                imageView.setBackgroundResource(R.drawable.background_image2);
                imageView.startAnimation(fadeInAnimation);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // 애니메이션 반복 시 작업 수행
            }
        });

        init();

        ttsspeed = mUserDao.getTtsSpeed();

        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != ERROR) {
                    ttsspeed = mUserDao.getTtsSpeed();
                    tts.setLanguage(Locale.KOREAN);
                    tts.setSpeechRate(ttsspeed);
                }
            }
        });
    }
    private void startBarcodeRecognitionAnimation() {
        // 배경 이미지 변경
        imageView.setBackgroundResource(R.drawable.background_image2);
        imageView.startAnimation(fadeOutAnimation);
    }

    private void requestCameraPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST);
            }
        }
    }

    private void bindpreview(ProcessCameraProvider processCameraProvider){
        Preview preview = new Preview.Builder().build();
        CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());
        ImageCapture imageCapture = new ImageCapture.Builder().build();
        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setTargetResolution(new Size(1280, 720))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();
        imageAnalysis.setAnalyzer(cameraExecutor, analyzer);
        processCameraProvider.unbindAll();
        processCameraProvider.bindToLifecycle(this , cameraSelector, preview, imageCapture, imageAnalysis);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            float gx = x / SensorManager.GRAVITY_EARTH;
            float gy = y / SensorManager.GRAVITY_EARTH;
            float gz = z / SensorManager.GRAVITY_EARTH;

            Float f = gx * gx + gy * gy + gz * gz;
            double squaredD = Math.sqrt(f.doubleValue());
            float gForce = (float) squaredD;
            //float acceleration = (float) Math.sqrt(x * x + y * y + z * z);

            if (gForce > SHAKE_THRESHOLD_GRAVITY) {
                long currentTime = System.currentTimeMillis();
                if(mShakeTime + SHAKE_SKIP_TIME > currentTime){
                    return;
                }
                mShakeTime = currentTime;
                Toast.makeText(MainActivity.this, "설정 화면으로 이동합니다.", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, SubActivity.class);
                startActivity(intent);
            }
        }
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public class MyImageAnalyzer implements ImageAnalysis.Analyzer {
        private FragmentManager fragmentManager;
        private bottom_dialog bd;

        public MyImageAnalyzer(FragmentManager fragmentManager) {
            this.fragmentManager = fragmentManager;
            bd = new bottom_dialog();
        }

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public void analyze(@NonNull ImageProxy image) {
            scanbarcode(image);
        }


        private void scanbarcode(ImageProxy image) {
            @SuppressLint("UnsafeOptInUsageError") Image image1 = image.getImage();
            assert image1 != null;
            tempTimeB = System.currentTimeMillis();
            InputImage inputImage = InputImage.fromMediaImage(image1, image.getImageInfo().getRotationDegrees());
            BarcodeScannerOptions options =
                    new BarcodeScannerOptions.Builder()
                            .setBarcodeFormats(
                                    Barcode.FORMAT_EAN_13,
                                    Barcode.FORMAT_QR_CODE)
                            .build();
            BarcodeScanner scanner = BarcodeScanning.getClient(options);
            Task<List<Barcode>> result = scanner.process(inputImage)
                    .addOnSuccessListener(new OnSuccessListener<List<Barcode>>() {
                        @Override
                        public void onSuccess(List<Barcode> barcodes) {
                            readerBarcodeData(barcodes);
                            // Task completed successfully
                            // ...
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Task failed with an exception
                            // ...
                        }
                    }).addOnCompleteListener(new OnCompleteListener<List<Barcode>>() {
                        @Override
                        public void onComplete(@NonNull Task<List<Barcode>> task) {
                            image.close();
                        }
                    });

        }

        private void readerBarcodeData(List<Barcode> barcodes) {
            for (Barcode barcode : barcodes) {
                Rect bounds = barcode.getBoundingBox();
                Point[] corners = barcode.getCornerPoints();

                String rawValue = barcode.getRawValue();

                int valueType = barcode.getValueType();
                // See API reference for complete list of supported types
                switch (valueType) {
                    case Barcode.TYPE_WIFI:
                        String ssid = barcode.getWifi().getSsid();
                        String password = barcode.getWifi().getPassword();
                        int type = barcode.getWifi().getEncryptionType();
                        break;
                    case Barcode.TYPE_URL:
                        /*if (!bd.isAdded()) {
                            bd.show(fragmentManager, "");
                        }*/

                        if (barcodes.size() == 1 && !bd.isAdded()) {
                            bd.show(fragmentManager, "");
                        }
                        bd.fetchurl(barcode.getUrl().getUrl());

                        break;
                    case Barcode.TYPE_PRODUCT:
                        if (barcode.getRawValue().length() == 13) {
                            startBarcodeRecognitionAnimation();
                            list.add(barcode.getRawValue());
                            server = mUserDao.getServer();
                            tempbarcode = barcode.getRawValue();
                            if (list.size() >= 3) {
                                if (list.get(0).equals(list.get(1)) && list.get(1).equals(list.get(2))) {
                                    closeCamera();
                                    parsing = "";
                                    if (server.equals("barcode") || server.equals("barcode2")) {
                                        JsoupAsyncTask jsoupAsyncTask = new JsoupAsyncTask();
                                        linkurl = "http://sundaelove.iptime.org:8080/ShowYourThings/" + server + "/" + barcode.getRawValue();
                                        jsoupAsyncTask.execute();

                                        PriceAsyncTask priceAsyncTask = new PriceAsyncTask();
                                        priceAsyncTask.execute();


                                    } else if (server.equals("api1")) {
                                        PyObject pyObject = python.getModule("main");
                                        PyObject product_name = pyObject.callAttr("Useapi", barcode.getRawValue());
                                        parsing = product_name.toString();


                                        PriceAsyncTask priceAsyncTask = new PriceAsyncTask();
                                        priceAsyncTask.execute();

                                    }
                                    list.clear();
                                    if (mUserDao.getServer().equals("barcode")) {
                                        Toast.makeText(MainActivity.this, "코리안넷에 바코드번호" + barcode.getRawValue().toString() + "를 검색합니다.", Toast.LENGTH_SHORT).show();
                                    } else if (mUserDao.getServer().equals("barcode2")) {
                                        Toast.makeText(MainActivity.this, "소비자24에 바코드번호" + barcode.getRawValue().toString() + "를 검색합니다.", Toast.LENGTH_SHORT).show();
                                    } else if (mUserDao.getServer().equals("api1")) {
                                        Toast.makeText(MainActivity.this, "API를 이용해 바코드번호" + barcode.getRawValue().toString() + "를 검색합니다.", Toast.LENGTH_SHORT).show();
                                    }

                                } else {
                                    Toast.makeText(MainActivity.this, "초점이 맞지 않습니다.", Toast.LENGTH_SHORT).show();
                                    tts.speak("초점이 맞지 않습니다.", TextToSpeech.QUEUE_FLUSH, null);
                                    list.clear();
                                }

                            }

                        }
                        break;
                    case Barcode.TYPE_ISBN:
                        if (barcode.getRawValue() != null) {
                            startBarcodeRecognitionAnimation();
                            Toast.makeText(MainActivity.this, "도서 검색 중..", Toast.LENGTH_SHORT).show();
                            closeCamera();
                            list.clear();
                            parsing = "";
                            JsoupAsyncTask jsoupAsyncTask = new JsoupAsyncTask();
                            linkurl = "http://sundaelove.iptime.org:8080/ShowYourThings/barcode2/" + barcode.getRawValue().toString();
                            jsoupAsyncTask.execute();

                            PriceAsyncTask priceAsyncTask = new PriceAsyncTask();
                            priceAsyncTask.execute();

                        }

                        break;

                }
            }
        }
    }
        private class JsoupAsyncTask extends AsyncTask<String, Void, Void> {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected Void doInBackground(String... params) {
                try{
                    String callUrl = linkurl;
                    Document doc = Jsoup.connect(callUrl).get();
                    Elements links = doc.select("body");

                    for (Element link : links){
                        parsing += link.text();
                    }

                    /*String priceUrl = "https://search.shopping.naver.com/search/all?query=" + parsing;

                    Document priceDoc = Jsoup.connect(priceUrl).get();
                    Elements priceLinks = priceDoc.select("div.basicList_price_area__K7DDT span.price_num__S2p_v");

                    for (Element link : priceLinks){
                        price = link.text();
                    }*/

                } catch (IOException e) {
                    e.printStackTrace();
                }

                return null;

            }

            @Override
            protected void onPostExecute(Void unused) {
                /*if(parsing.equals("not found")){
                    //tts.speak("데이터베이스에 없는 제품입니다.", TextToSpeech.QUEUE_FLUSH, null);
                    parsing = null;
                    firstPrice = null;
                    secondPrice = null;
                    alertdg();
                    //init();

                }*/
            }
        }

    private class PriceAsyncTask extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(String... urls) {
            String url1 = "https://search.danawa.com/dsearch.php?k1=" + parsing;
            String url2 = "https://search.shopping.naver.com/search/all?query=" + parsing;
            try {
                Document doc1 = Jsoup.connect(url1).get();
                Element priceElement1 = doc1.select("p.price_sect strong").first();
                firstPrice = priceElement1 != null ? priceElement1.text() : "";

                Document doc2 = Jsoup.connect(url2).get();
                Element priceElement2 = doc2.select("span.price_num__S2p_v").first();
                secondPrice = priceElement2 != null ? priceElement2.text() : "";

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            if(parsing.equals("not found")){
                parsing = null;
                firstPrice = null;
                secondPrice = null;
            }

            else{
                if(firstPrice != null && secondPrice != null){
                    tts.speak(parsing + "다나와 가격" + firstPrice + "원 네이버 가격" + secondPrice, TextToSpeech.QUEUE_FLUSH, null);
                }
                else if (firstPrice == null && secondPrice != null){
                    tts.speak(parsing + "다나와 가격 조회 불가 네이버 가격" + secondPrice, TextToSpeech.QUEUE_FLUSH, null);
                }
                else if(firstPrice != null && secondPrice == null){
                    tts.speak(parsing + "다나와 가격" +  firstPrice + "원 네이버 가격 조회 불가", TextToSpeech.QUEUE_FLUSH, null);
                }
                else{
                    tts.speak(parsing + "가격 조회 불가", TextToSpeech.QUEUE_FLUSH, null);
                }
            }
            alertdg();
        }
    }

    public void closeCamera(){
        if (cameraProviderFuture != null && cameraExecutor != null){
            cameraProviderFuture.cancel(true);
            cameraProviderFuture = null;
            cameraExecutor.shutdown();
            cameraExecutor = null;
        }
    }
    public void init(){

        previewView = findViewById(R.id.previewview);
        this.getWindow().setFlags(1024,1024);

        cameraExecutor = Executors.newSingleThreadExecutor();
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        analyzer = new MyImageAnalyzer(getSupportFragmentManager());

        cameraProviderFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try{
                    if(ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != (PackageManager.PERMISSION_GRANTED)){
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, 101);
                    }else{
                        ProcessCameraProvider processCameraProvider = (ProcessCameraProvider) cameraProviderFuture.get();
                        bindpreview(processCameraProvider);
                    }
                }catch (ExecutionException e){
                    e.printStackTrace();
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
        }, ContextCompat.getMainExecutor(this));
    }

    @Override
    protected void onPause() {
        super.onPause();
        isResumed = false;
        sensorManager.unregisterListener(this);
        closeCamera();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeCamera();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!isResumed) {
            tts.speak("카메라 화면", TextToSpeech.QUEUE_FLUSH, null);
            isResumed = true;
        }
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        init();
    }

    private void alertdg(){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setCancelable(false);
        builder.setTitle("바코드 인식 결과");
        if (parsing != null && !parsing.isEmpty()){
            if (firstPrice != null && secondPrice != null){
                builder.setMessage(parsing + "\n다나와 가격" + firstPrice + "원\n네이버 가격" + secondPrice);
            }
            else {
                if (firstPrice == null && secondPrice != null){
                    builder.setMessage(parsing + "\n다나와 가격 조회 불가" + "\n네이버 가격" + secondPrice);
                }
                else if (firstPrice != null && secondPrice == null){
                    builder.setMessage(parsing + "\n다나와 가격" + firstPrice + "원\n네이버 가격 조회 불가");
                }
                else{builder.setMessage(parsing + "\n가격 조회 불가");}
            }
        }
        else if (parsing == null || parsing.equals("not found")){
            builder.setMessage("데이터베이스에 없는 제품입니다.");
            tts.speak("데이터베이스에 없는 제품입니다", TextToSpeech.QUEUE_FLUSH, null);
        }
        else {
            builder.setMessage("Error");
            tts.speak("에러 발생", TextToSpeech.QUEUE_FLUSH, null);
        }

        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                init();
            }
        });
        builder.setNegativeButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(MainActivity.this, SubActivity.class);
                startActivity(intent);
            }
        }).show();
    }

    @Override
    public void onBackPressed() {
        if (isTaskRoot()) {
            long tempTime = System.currentTimeMillis();
            long intervalTime = tempTime - presstime;
            if (0 <= intervalTime && finishtimeed >= intervalTime)
            {
                finish();
            }
            else
            {
                tts.speak("뒤로 가기 버튼을 한 번 더 누르면 앱이 종료됩니다.", TextToSpeech.QUEUE_FLUSH, null);
                presstime = tempTime;
                init();
            }
        } else {
            super.onBackPressed();
        }
    }

}