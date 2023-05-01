package com.example.coed_test;

import static android.speech.tts.TextToSpeech.ERROR;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.util.Size;
import android.view.KeyEvent;
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



public class MainActivity extends AppCompatActivity {

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

    String linkurl, parsing, price;
    String server = "barcode";
    TextToSpeech tts;
    float ttsspeed;

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

        //데이터 삽입
        // User 객체가 있는지 확인하고, 없으면 생성하여 데이터베이스에 삽입
        if (mUserDao.getUserAll().isEmpty()) {
            User user = new User();
            user.setServer("barcode");
            user.setTtsspeed(5.0f);
            mUserDao.setInsertUser(user);
        }

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

    private void requestCameraPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST);
            }
        }
    }

    /*@SuppressLint("MissingSuperCall")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == 101 && grantResults.length > 0){
            ProcessCameraProvider processCameraProvider = null;
            try {
                processCameraProvider = (ProcessCameraProvider) cameraProviderFuture.get();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            bindpreview(processCameraProvider);

        }
    }*/

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

    public class MyImageAnalyzer implements ImageAnalysis.Analyzer{
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
            for (Barcode barcode: barcodes) {
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
                        if (!bd.isAdded()){
                            bd.show(fragmentManager, "");
                        }
                        bd.fetchurl(barcode.getUrl().getUrl());


                        String title = barcode.getUrl().getUrl();
                        String url = barcode.getUrl().getUrl();
                        break;
                    case Barcode.TYPE_PRODUCT:
                        if(barcode.getRawValue().length() == 13){
                            list.add(barcode.getRawValue());
                            server = mUserDao.getServer();
                            if(list.size() >= 3){
                                if(list.get(0).equals(list.get(1)) && list.get(1).equals(list.get(2))){
                                    closeCamera();
                                    parsing = "";
                                    price = "";
                                    JsoupAsyncTask jsoupAsyncTask = new JsoupAsyncTask();
                                    linkurl = "http://sundaelove.iptime.org:8080/ShowYourThings/" + server + "/" + barcode.getRawValue();
                                    Toast.makeText(MainActivity.this, barcode.getRawValue().toString() + server, Toast.LENGTH_SHORT).show();
                                    jsoupAsyncTask.execute();

                                    list.clear();
                                }
                                else{
                                    tts.speak("초점이 맞지 않습니다.", TextToSpeech.QUEUE_FLUSH, null);
                                    list.clear();
                                }

                            }

                        }
                        break;
                    case Barcode.TYPE_ISBN:
                        if(barcode.getRawValue() != null){
                            closeCamera();
                            parsing = "";
                            price = "";
                            JsoupAsyncTask jsoupAsyncTask = new JsoupAsyncTask();
                            linkurl = "http://sundaelove.iptime.org:8080/ShowYourThings/barcode2/" + barcode.getRawValue().toString();
                            jsoupAsyncTask.execute();

                            list.clear();
                        }
                        else {}

                        break;

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

                    String priceUrl = "https://search.shopping.naver.com/search/all?query=" + parsing;

                    Document priceDoc = Jsoup.connect(priceUrl).get();
                    Elements priceLinks = priceDoc.select("div.basicList_price_area__K7DDT span.price_num__S2p_v");

                    for (Element link : priceLinks){
                        price = link.text();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }

                return null;

            }

            @Override
            protected void onPostExecute(Void unused) {
                if(parsing.equals("not found")){
                    tts.speak("데이터베이스에 없는 제품입니다.", TextToSpeech.QUEUE_FLUSH, null);
                    parsing = null;
                    price = null;
                    alertdg();
                    //init();

                }
                else if(parsing != null & !(parsing.equals("not found"))){
                    tts.speak(parsing + "네이버 가격" + price, TextToSpeech.QUEUE_FLUSH, null);
                    alertdg();
                }
                else {
                    tts.speak("에러 발생", TextToSpeech.QUEUE_FLUSH, null);
                    parsing = null;
                    price = null;
                    alertdg();
                }
            }
        }

    }
    private void closeCamera(){
        if (cameraProviderFuture != null && cameraExecutor != null){
            cameraProviderFuture.cancel(true);
            cameraProviderFuture = null;
            cameraExecutor.shutdown();
            cameraExecutor = null;
        }
    }
    private void init(){

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

        init();
    }

    private void alertdg(){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setCancelable(false);
        builder.setTitle("바코드 인식 결과");
        if(parsing != null){
            builder.setMessage(parsing + "\n네이버쇼핑 가격" + price);
        }
        else{builder.setMessage("데이터베이스에 없는 제품입니다.");}

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