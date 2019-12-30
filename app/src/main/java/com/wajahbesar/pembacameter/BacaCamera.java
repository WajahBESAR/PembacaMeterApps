package com.wajahbesar.pembacameter;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.MeteringRectangle;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.location.Location;
import android.media.ImageReader;
import android.media.MediaActionSound;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Size;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.google.android.material.snackbar.Snackbar;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.wajahbesar.pembacameter.Database.DatabaseHandler;
import com.wajahbesar.pembacameter.Database.TablePetugas;
import com.wajahbesar.pembacameter.Database.TableSetting;
import com.wajahbesar.pembacameter.Utilities.Functions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class BacaCamera extends AppCompatActivity implements OnTouchListener {

    protected CameraDevice cameraDevice;
    protected CameraCaptureSession cameraCaptureSessions;
    protected CaptureRequest.Builder captureRequestBuilder;
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private Size imageDimension;
    private CropImageView cropImage;
    private int scrWidth, scrHeight;
    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;
    private ImageReader imageReader;
    private boolean torchOnOff;
    private CameraCharacteristics characteristics;
    private boolean mManualFocusEngaged = false;

    private TextView txtRead;
    private TextureView texture;
    private ImageView imageView;
    private ImageView imgCapture;
    private ImageView imgReset;
    private ImageView imgSave;

    private DatabaseHandler databaseHandler;
    private String extNopel;
    private String dbsInitial;
    private Double vLat, vLng;
    private String hari = "";

    private ProgressDialog progressDialog;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_baca_camera);

        // TRANSPARENT NOTIFBAR
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        // FULLSCREEN DISPLAY
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        progressDialog = new ProgressDialog(BacaCamera.this, R.style.Theme_MaterialComponents);
        progressDialog.setCancelable(false);
        progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        Objects.requireNonNull(progressDialog.getWindow()).setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        Objects.requireNonNull(progressDialog.getWindow()).setBackgroundDrawableResource(R.color.transparent_progress);
        progressDialog.setProgressStyle(android.R.style.Widget_ProgressBar);
        progressDialog.getWindow().setGravity(Gravity.BOTTOM);
        progressDialog.setMessage("Mohon bersabar, ini ujian...");
        progressDialog.setContentView(new ProgressBar(this));

        databaseHandler = new DatabaseHandler(this);

        // GET NOPEL
        extNopel = getIntent().getStringExtra("extNopel");

        // Cek, udah dibaca atau belum
        // ..

        texture = findViewById(R.id.texture);
        texture.setSurfaceTextureListener(textureListener);
        texture.setOnTouchListener(this);

        imageView = findViewById(R.id.imageView);
        txtRead = findViewById(R.id.txtRead);

        cropImage = findViewById(R.id.cropImage);
        cropImage.setGuidelines(CropImageView.Guidelines.OFF);
        cropImage.setAutoZoomEnabled(false);
        cropImage.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return false;
            }
        });
        cropImage.setOnCropImageCompleteListener(new CropImageView.OnCropImageCompleteListener() {
            @Override
            public void onCropImageComplete(CropImageView view, CropImageView.CropResult result) {
                imageView.setImageBitmap(cropImage.getCroppedImage());
                Bitmap bmp = cropImage.getCroppedImage();
                String readOCR = ReadImageOCR(bmp);
                txtRead.setVisibility(View.VISIBLE);
                txtRead.setText(readOCR);
            }
        });

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        scrWidth = size.x;
        scrHeight = size.y;

        // GET INITIAL
        final List<TablePetugas> tablePetugasList = databaseHandler.selectPetugas();
        for(TablePetugas tablePetugas: tablePetugasList) {
            dbsInitial = tablePetugas.getInisial();
        }

        // GET CURRENT LOCATION
        FusedLocationProviderClient mFusedLocation = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocation.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null){
//                    Log.d("My Current location", "Lat : " + location.getLatitude() + " Long : " + location.getLongitude());
                    vLat = location.getLatitude();
                    vLng = location.getLongitude();
                }
            }
        });
        // --------------------

        imgCapture = findViewById(R.id.imgCapture);
        imgCapture.setVisibility(View.VISIBLE);
        imgCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Functions(getApplicationContext()).Getar();

                // Cekrek
                MediaActionSound sound = new MediaActionSound();
                sound.play(MediaActionSound.SHUTTER_CLICK);

                progressDialog.show();

                // UPDATE TABEL PETUGAS
                String api_token = ((GlobalVars) getApplication()).getApiToken();
                String serverRequest = ambilAPISetting() + "/?action=datetime&token=" + api_token;
                RequestQueue queue = Volley.newRequestQueue(getApplicationContext());

                StringRequest stringRequest = new StringRequest(Request.Method.GET, serverRequest, new Response.Listener<String>() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onResponse(String response) {
                        String nama = "";
                        String ava = "";
                        String tanggalServer = "";
                        if(response.length() > 20) {
                            tanggalServer = response;
                        } else {
                            List<TablePetugas> tablePetugasList = databaseHandler.selectPetugas();
                            for (TablePetugas tablePetugas : tablePetugasList) {
                                nama = tablePetugas.getNama();
                                hari = tablePetugas.getHaribaca();
                                ava = tablePetugas.getAvatar();
                                tanggalServer = tablePetugas.getTanggal();
                            }
                        }

                        String tudei;
                        String tahun;
                        String bulan;
                        String tanggal;

                        if (tanggalServer.equals("")){
                            switch (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)){
                                case 1: tudei = "Minggu"; break;
                                case 2: tudei = "Senin"; break;
                                case 3: tudei = "Selasa"; break;
                                case 4: tudei = "Rabu"; break;
                                case 5: tudei = "Kamis"; break;
                                case 6: tudei = "Jumat"; break;
                                default: tudei = "Sabtu"; break;
                            }
                            switch (Calendar.getInstance().get(Calendar.MONTH)){
                                case Calendar.JANUARY: bulan = "01"; break;
                                case Calendar.FEBRUARY: bulan = "02"; break;
                                case Calendar.MARCH: bulan = "03"; break;
                                case Calendar.APRIL: bulan = "04"; break;
                                case Calendar.MAY: bulan = "05"; break;
                                case Calendar.JUNE: bulan = "06"; break;
                                case Calendar.JULY: bulan = "07"; break;
                                case Calendar.AUGUST: bulan = "08"; break;
                                case Calendar.SEPTEMBER: bulan = "09"; break;
                                case Calendar.OCTOBER: bulan = "10"; break;
                                case Calendar.NOVEMBER: bulan = "11"; break;
                                default: bulan = "12"; break;
                            }
                            if (Calendar.getInstance().get(Calendar.DATE) < 10 ) {
                                tanggal = "0" + Calendar.getInstance().get(Calendar.DATE);
                            } else {
                                tanggal = String.valueOf(Calendar.getInstance().get(Calendar.DATE));
                            }
                            tahun = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
                        } else {
                            tudei = tanggalServer.substring(20);
                            tahun = tanggalServer.substring(0, 4);
                            bulan = tanggalServer.substring(5, 7);
                            tanggal = tanggalServer.substring(8, 10);
                        }
                        String jam;
                        if (Calendar.getInstance().get(Calendar.HOUR) < 10 ) {
                            jam = "0" + Calendar.getInstance().get(Calendar.HOUR);
                        } else {
                            jam = String.valueOf(Calendar.getInstance().get(Calendar.HOUR));
                        }
                        String menit;
                        if (Calendar.getInstance().get(Calendar.MINUTE) < 10 ) {
                            menit = "0" + Calendar.getInstance().get(Calendar.MINUTE);
                        } else {
                            menit = String.valueOf(Calendar.getInstance().get(Calendar.MINUTE));
                        }
                        String detik;
                        if (Calendar.getInstance().get(Calendar.SECOND) < 10 ) {
                            detik = "0" + Calendar.getInstance().get(Calendar.SECOND);
                        } else {
                            detik = String.valueOf(Calendar.getInstance().get(Calendar.SECOND));
                        }
                        String tgl = tahun
                                + "-" + bulan
                                + "-" + tanggal
                                + " " + jam
                                + ":" + menit
                                + ":" + detik
                                + " " + tudei;
                        databaseHandler.updatePetugas(nama, hari, ava, dbsInitial, tgl);

                        progressDialog.dismiss();
                    }
                }, new Response.ErrorListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                    }
                });

                // Add the request to the RequestQueue.
                queue.add(stringRequest);
                // --------------

                imageView.setImageBitmap(texture.getBitmap());
                cropImage.setImageBitmap(texture.getBitmap());
                cropImage.setCropRect(new Rect(scrWidth / 3, scrHeight / 4, (scrWidth / 3) * 2, (scrHeight / 4) + ((scrHeight / 4) / 4)));
                Bitmap bmp = cropImage.getCroppedImage();
                String readOCR = ReadImageOCR(bmp);
                txtRead.setVisibility(View.VISIBLE);
                txtRead.setText(readOCR);

                // matikan flash
                captureRequestBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF);
                captureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_OFF);
                view.setAlpha((float) .3);
                torchOnOff = false;
                try {
                    cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), null, null);
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }

                // reset tampilan
                imgReset.setVisibility(View.VISIBLE);
                imgSave.setVisibility(View.VISIBLE);
                imgCapture.setVisibility(View.INVISIBLE);
            }
        });

        imgReset = findViewById(R.id.imgReset);
        imgReset.setVisibility(View.INVISIBLE);
        imgReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Functions(getApplicationContext()).Getar();

                finish();
                overridePendingTransition(0, 0);
                startActivity(getIntent());
                overridePendingTransition(0, 0);
            }
        });

        imgSave = findViewById(R.id.imgSave);
        imgSave.setVisibility(View.INVISIBLE);
        imgSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Functions(getApplicationContext()).Getar();

                Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();

                // Urusan nama file
                List<TablePetugas> tablePetugasList = databaseHandler.selectPetugas();
                String datetime_full = "";
                for(TablePetugas tablePetugas: tablePetugasList) {
                    datetime_full = tablePetugas.getTanggal(); // 2019-09-24 xxxxxx
                }
                String bln, thn;
                if (datetime_full.length() > 10) {
                    bln = datetime_full.substring(5, 7);
                    thn = datetime_full.substring(0, 4);
                } else {
                    bln = String.valueOf(Calendar.getInstance().get(Calendar.MONTH));
                    thn = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
                }

                // extNopel = 12341234
                String imageFileName = extNopel.substring(0, 4) + "-" + extNopel.substring(4, 8) + "_" + bln + "_" + thn + ".JPG"; // DEFAULT FILE NAME PHOTO METER
                File direktori = new File(Environment.getExternalStorageDirectory().getPath() + "/PembacaMeter/Photo/");

                FileOutputStream fileOutputStream = null;
                try {
                    // Sebelum di simpan, bikin watermark
                    Bitmap dest = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getWidth() + 200, Bitmap.Config.ARGB_8888);
                    Canvas cs = new Canvas(dest);

                    Paint bScaledPaint = new Paint();
                    bScaledPaint.setDither(true);
                    cs.drawBitmap(bitmap, 0f, 0f, bScaledPaint);

                    Paint tPaint = new Paint();
                    tPaint.setTextSize(46);
                    tPaint.setColor(Color.WHITE);
                    tPaint.setStyle(Paint.Style.FILL);
                    tPaint.setAntiAlias(true);
                    cs.drawText("PEMBACA METER - TIRTAPAKUAN", 270, 120, tPaint);
                    tPaint.setTextSize(36);
                    cs.drawText("ID: " + extNopel + "  [" + datetime_full + "]", 270, 160, tPaint);
                    cs.drawText(dbsInitial + " [" + vLat + "," + vLng + "]", 270, 200, tPaint);

                    Bitmap logopdam = BitmapFactory.decodeResource(getResources(), R.drawable.logopdam_white_kecil);
                    Paint iPaint = new Paint();
                    iPaint.setAntiAlias(true);
                    cs.drawBitmap(logopdam,50, 50, iPaint);

                    // Simpan tambah watermark, dikecilin
                    fileOutputStream = new FileOutputStream(new File(direktori, imageFileName));
                    dest.compress(Bitmap.CompressFormat.JPEG, 30, fileOutputStream);
                    dest.recycle();
                } catch (Exception e) {
                    e.printStackTrace();
//                    Log.i("GAGAL", Objects.requireNonNull(e.getMessage()));
                    new Functions(getApplicationContext()).showMessage(cropImage, "Gagal", e.getMessage(), Snackbar.LENGTH_SHORT);
                } finally {
                    try {
                        bitmap.recycle();
                        if (fileOutputStream != null) {
                            fileOutputStream.close();

                            // Kirim ke BacaStand
                            Bundle extras = new Bundle();
                            extras.putString("extNopel", extNopel);
                            extras.putString("extFilename", imageFileName);
                            extras.putString("extStand", txtRead.getText().toString());

                            Intent intent = new Intent(BacaCamera.this, BacaStand.class);
                            intent.putExtras(extras);

                            startActivity(intent);

                            // Keluar
                            finish();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
//                        Log.i("GAGAL", Objects.requireNonNull(e.getMessage()));
                        new Functions(getApplicationContext()).showMessage(cropImage, "Gagal", e.getMessage(), Snackbar.LENGTH_SHORT);
                    }
                }
            }
        });

        ImageView imgFlash = findViewById(R.id.imgFlash);
        imgFlash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Functions(getApplicationContext()).Getar();

                try {
                    if (torchOnOff) {
                        captureRequestBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF);
                        captureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_OFF);
                        view.setAlpha((float) .3);
                        torchOnOff = false;
                    } else {
                        captureRequestBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_TORCH);
                        captureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);
                        view.setAlpha(1);
                        torchOnOff = true;
                    }
                    cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), null, null);
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void openCamera() {
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
//        Log.e("CAMERA_OPEN", "is camera open");
        try {
            String cameraId;
            if (manager != null) {
                cameraId = manager.getCameraIdList()[0];
                characteristics = manager.getCameraCharacteristics(cameraId);
                StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                if (map != null) {
                    imageDimension = map.getOutputSizes(SurfaceTexture.class)[0];
                }
                // Add permission for camera and let user grant the permission
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(BacaCamera.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
                    return;
                }
                manager.openCamera(cameraId, stateCallback, null);

                cropImage.setImageBitmap(texture.getBitmap());
                cropImage.setCropRect(new Rect(scrWidth / 3, scrHeight / 4, (scrWidth / 3) * 2, (scrHeight / 4) + ((scrHeight / 4) / 4) ));
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
//        Log.e("OPEN_CAMERA_X", "openCamera X");
    }

    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            //This is called when the camera is open
//            Log.e("OPEN_CAMERA", "onOpened");
            cameraDevice = camera;
            createCameraPreview();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            cameraDevice.close();
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            cameraDevice.close();
            cameraDevice = null;
        }
    };

    protected void createCameraPreview() {
        try {
            SurfaceTexture surfaceTexture = texture.getSurfaceTexture();
            surfaceTexture.setDefaultBufferSize(imageDimension.getWidth(), imageDimension.getHeight());
            Surface surface = new Surface(surfaceTexture);
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);
            cameraDevice.createCaptureSession(Collections.singletonList(surface), new CameraCaptureSession.StateCallback(){
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    //The camera is already closed
                    if (null == cameraDevice) {
                        return;
                    }
                    // When the session is ready, we start displaying the preview.
                    cameraCaptureSessions = cameraCaptureSession;
                    updatePreview();
                }
                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Toast.makeText(BacaCamera.this, "Configuration change", Toast.LENGTH_SHORT).show();
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    protected void updatePreview() {
        captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_OFF); // .CONTROL_AF_MODE_AUTO);
        captureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);
        mManualFocusEngaged = true;

        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        try {
            cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), null, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {

//        Log.d("TOUCH", "DITOUCH");
        //Override in your touch-enabled view (this can be different than the view you use for displaying the cam preview)
        final int actionMasked = motionEvent.getActionMasked();
        if (actionMasked == MotionEvent.ACTION_UP) {
            return false;
        }
        if (mManualFocusEngaged) {
//            Log.d("MANUAL_FOCUS", "Manual focus already engaged");
            return true;
        }

        final Rect sensorArraySize = characteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);

        // xTODO: here I just flip x,y, but this needs to correspond with the sensor orientation (via SENSOR_ORIENTATION)
        final int y;
        final int x;
        assert sensorArraySize != null;
        y = (int) ((motionEvent.getX() / (float) view.getWidth()) * (float) sensorArraySize.height());
        x = (int) ((motionEvent.getY() / (float) view.getHeight()) * (float) sensorArraySize.width());
        final int halfTouchWidth = 50; //(int)motionEvent.getTouchMajor(); // xTODO: this doesn't represent actual touch size in pixel. Values range in [3, 10]...
        final int halfTouchHeight = 50; //(int)motionEvent.getTouchMinor();
        MeteringRectangle focusAreaTouch = new MeteringRectangle(Math.max(x - halfTouchWidth, 0),
                Math.max(y - halfTouchHeight, 0),
                halfTouchWidth * 2,
                halfTouchHeight * 2,
                MeteringRectangle.METERING_WEIGHT_MAX - 1);

        CameraCaptureSession.CaptureCallback captureCallbackHandler = new CameraCaptureSession.CaptureCallback() {
            @Override
            public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                super.onCaptureCompleted(session, request, result);
                mManualFocusEngaged = false;

                if (request.getTag() == "FOCUS_TAG") {
                    //the focus trigger is complete - resume repeating (preview surface will get frames), clear AF trigger
                    captureRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, null);
                    try {
                        cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), null, null);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCaptureFailed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureFailure failure) {
                super.onCaptureFailed(session, request, failure);
//                Log.e("MANUAL_AF", "Manual AF failure: " + failure);
                mManualFocusEngaged = false;
            }
        };

        //first stop the existing repeating request
        try {
            cameraCaptureSessions.stopRepeating();
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        //cancel any existing AF trigger (repeated touches, etc.)
        captureRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
        captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_OFF);
        captureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_OFF);
        try {
            cameraCaptureSessions.capture(captureRequestBuilder.build(), captureCallbackHandler, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        //Now add a new AF trigger with focus region
        if (isMeteringAreaAFSupported()) {
            captureRequestBuilder.set(CaptureRequest.CONTROL_AF_REGIONS, new MeteringRectangle[]{focusAreaTouch});
        }
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_AUTO);
        captureRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START);
        captureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);
        captureRequestBuilder.setTag("FOCUS_TAG"); //we'll capture this later for resuming the preview

        //then we ask for a single request (not repeating!)
        try {
            cameraCaptureSessions.capture(captureRequestBuilder.build(), captureCallbackHandler, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        mManualFocusEngaged = true;

        return true;
    }

    protected void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("Camera Background");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    protected void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private boolean isMeteringAreaAFSupported() {
        Integer value = characteristics.get(CameraCharacteristics.CONTROL_MAX_REGIONS_AF);
        if (value != null) {
            return value >= 1;
        } else {
            return false;
        }
    }

    private void closeCamera() {
        if (null != cameraDevice) {
            cameraDevice.close();
            cameraDevice = null;
        }
        if (null != imageReader) {
            imageReader.close();
            imageReader = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                // close the app
                Toast.makeText(BacaCamera.this, "Sorry!!!, you can't use this app without granting permission", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
//        Log.e("RESUME", "onResume");
        startBackgroundThread();
        if (texture.isAvailable()) {
            openCamera();
        } else {
            texture.setSurfaceTextureListener(textureListener);
        }
    }
    @Override

    protected void onPause() {
//        Log.e("PAUSE", "onPause");
        closeCamera();
        stopBackgroundThread();
        super.onPause();
    }

    private String ReadImageOCR (Bitmap bitmap) {
        TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();
        if (!textRecognizer.isOperational()) {
//            Log.w("READ_IMAGE_OCR", "Detector dependencies not loaded yet");
            new Functions(getApplicationContext()).showMessage(cropImage, "Gagal", "Detector dependencies not loaded yet", Snackbar.LENGTH_SHORT);
        } else {
            Frame imageFrame = new Frame.Builder().setBitmap(bitmap).build();

            String imageText = "";
            SparseArray<TextBlock> textBlocks = textRecognizer.detect(imageFrame);

            for (int i = 0; i < textBlocks.size(); i++) {
                TextBlock textBlock = textBlocks.get(textBlocks.keyAt(i));
                imageText = textBlock.getValue();
            }
            return imageText;
        }
        return "";
    }

    private TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            // buka kamera
            openCamera();
        }
        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            // Transform you image captured size according to the surface width and height
        }
        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }
        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        }
    };

    private String ambilAPISetting() {
        String tempApiSetting = "";
        List<TableSetting> tableSettingList = databaseHandler.bukaSetting();
        if(tableSettingList.size() > 0) {
            for (TableSetting tableSetting : tableSettingList) {
                tempApiSetting = tableSetting.getURLAPI();
            }
        }
        return tempApiSetting;
    }

}
