package com.example.home.edge;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Debug;
import android.os.Environment;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.LruCache;
import android.view.Display;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;

public class MainActivity extends Activity {

    private static final String TAG = "CamTestActivity";
    private Preview preview;
    private Camera camera = null;
    private int imageRotation;
    private int screenWidth;
    private int screenHight;
    private String quality;
    private byte[] dataToSave;
    private static final int REQUEST_IMAGE_CAPTURE = 100;
    private OrientationEventListener orientationListener = null;
    private boolean isFrontCamera = true;
    private SharedPreferences preferences;
    public int rot;
    public static int degrees = 0;
    public static LruCache<String, Bitmap> mMemoryCache;
    public static Context ctx;
    public static Activity act;
    final private String IMAGE_RESOURCE_ID = "11";
    final private int REAR_CAMERA = 0;
    final private int FRONT_CAMERA = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d("123","on create");
        super.onCreate(savedInstanceState);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        ctx = this;
        act = this;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setRotationAnimation();
        setContentView(R.layout.activity_main);
        setLruCache();

        this.setActivityLayout();

        /*preview.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Log.d("123","before click");
                camera.takePicture(shutterCallback, rawCallback, jpegCallback);
                Log.d("123","on click");
            }
        });*/

        //Toast.makeText(ctx, getString(R.string.take_photo_help), Toast.LENGTH_LONG).show();

        //		buttonClick = (Button) findViewById(R.id.btnCapture);
        //
        //		buttonClick.setOnClickListener(new OnClickListener() {
        //			public void onClick(View v) {
        ////				preview.camera.takePicture(shutterCallback, rawCallback, jpegCallback);
        //				camera.takePicture(shutterCallback, rawCallback, jpegCallback);
        //			}
        //		});
        //
        //		buttonClick.setOnLongClickListener(new OnLongClickListener(){
        //			@Override
        //			public boolean onLongClick(View arg0) {
        //				camera.autoFocus(new AutoFocusCallback(){
        //					@Override
        //					public void onAutoFocus(boolean arg0, Camera arg1) {
        //						//camera.takePicture(shutterCallback, rawCallback, jpegCallback);
        //					}
        //				});
        //				return true;
        //			}
        //		});

    }
/*
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            if (status == LoaderCallbackInterface.SUCCESS ) {
                // now we can call opencv code !
                //openCamera(1);
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.ctx);
                int cam = preferences.getInt("camera",REAR_CAMERA);
                if(cam == REAR_CAMERA)
                    openRearCamera();
                else if(cam == FRONT_CAMERA)
                    openFrontCamera();
            } else {
                super.onManagerConnected(status);
            }
        }
    };*/

    @Override
    protected void onResume() {
        super.onResume();
       // OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_5,this, mLoaderCallback);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.ctx);
        int cam = preferences.getInt("camera",REAR_CAMERA);
        if(cam == REAR_CAMERA)
            openRearCamera();
        else if(cam == FRONT_CAMERA)
            openFrontCamera();
    }

    @Override
    protected void onPause() {
        if(camera != null) {
            camera.stopPreview();
            preview.setCamera(null);
            camera.release();
            camera = null;
        }
        super.onPause();
    }

    private void setLruCache(){
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 8;
        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return bitmap.getByteCount() / 1024;
            }
        };
    }

    private void initTakePhotoButton(){
        final ImageButton takePhoto = (ImageButton) findViewById(R.id.Take_Photo);
        takePhoto.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                camera.takePicture(shutterCallback, rawCallback, jpegCallback);
            }
        });
    }

    private void initSettingsButton(){
        final ImageButton settings = (ImageButton) findViewById(R.id.Settings);
        settings.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent myIntent = new Intent(MainActivity.this, SettingsActivity.class);
                MainActivity.this.startActivity(myIntent);
            }
        });
    }

    private void initSwitchCameraButton(){
        final ImageButton switchCamera = (ImageButton) findViewById(R.id.Rotate_Camera);
        switchCamera.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                camera.stopPreview();
                camera.release();
                camera = null;
                int cam = preferences.getInt("camera",REAR_CAMERA);
                Log.d("123","camera= "+cam);
                if(cam == FRONT_CAMERA)
                    openRearCamera();
                else if(cam ==REAR_CAMERA)
                    openFrontCamera();
            }
        });
    }

    private void openFrontCamera(){
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("camera", FRONT_CAMERA);
        editor.apply();
        if(camera == null)
            camera = Camera.open(FRONT_CAMERA);

        setCameraDisplayOrientation(this, FRONT_CAMERA, camera);
        try {
            camera.setPreviewDisplay(preview.mHolder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        camera.startPreview();
        preview.setCamera(camera);
    }

    private void openRearCamera(){
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("camera",REAR_CAMERA);
        editor.apply();
        int cam = preferences.getInt("camera",REAR_CAMERA);
        Log.d("123","camera= "+cam);

        if(camera == null)
            camera = Camera.open(REAR_CAMERA);

        setCameraDisplayOrientation(this, REAR_CAMERA, camera);
        try {
            camera.setPreviewDisplay(preview.mHolder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        camera.startPreview();
        preview.setCamera(camera);
    }

    private void refreshGallery(File file) {
        Intent mediaScanIntent = new Intent( Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(Uri.fromFile(file));
        sendBroadcast(mediaScanIntent);
    }

    Camera.ShutterCallback shutterCallback = new Camera.ShutterCallback() {
        public void onShutter() {
            		//	 Log.d(TAG, "onShutter'd");
        }
    };

    Camera.PictureCallback rawCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            //			 Log.d(TAG, "onPictureTaken - raw");
        }
    };

    Camera.PictureCallback jpegCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {


            Log.d("123", "onPictureTaken - jpeg");
            new SaveImageTask().execute(data);
            //resetCam();

        }
    };

    private class SaveImageTask extends AsyncTask<byte[], Void, Void> {

        @Override
        protected Void doInBackground(byte[]... data) {
            FileOutputStream outStream = null;

            // Write to SD Card
            try {
                Log.d("123","111");
                data[0] = fixRotation(data[0]);

                final BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;

                BitmapFactory.decodeByteArray(data[0] , 0, data[0].length);

                // Calculate inSampleSize
                options.inSampleSize = calculateInSampleSize(options, 126, 126);

                // Decode bitmap with inSampleSize set
                options.inJustDecodeBounds = false;
                Bitmap bitmap = BitmapFactory.decodeByteArray(data[0] , 0, data[0].length);


                if(bitmap == null)
                Log.d("123", "bit null");

                mMemoryCache.remove(IMAGE_RESOURCE_ID);
                addBitmapToMemoryCache(IMAGE_RESOURCE_ID, bitmap);

                Log.d("123","imageroation= "+ imageRotation);
                Intent intent = new Intent(MainActivity.this, DisplayActivity.class);
                intent.putExtra("rotation", imageRotation);
                MainActivity.this.startActivity(intent);

            } catch (Exception e) {
                e.printStackTrace();
            }finally {
            }
            return null;
        }

    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static void setCameraDisplayOrientation(Activity activity,
                                                   int cameraId, android.hardware.Camera camera) {
        Log.d("123","setCameraOrientation");
        android.hardware.Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
         degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }
        Log.d("123","degrees= "+degrees);

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
            Log.d("123","result= "+result);
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }

    public byte[] fixRotation(byte[] data){
        int rotation = this.getWindowManager().getDefaultDisplay().getRotation();
        this.imageRotation = rotation;
        Log.d("123", "rotation= "+rotation);
        byte[] byteArray = null;
        if (data != null) {
            int screenWidth = getResources().getDisplayMetrics().widthPixels;
            int screenHeight = getResources().getDisplayMetrics().heightPixels;
            Bitmap bm = BitmapFactory.decodeByteArray(data, 0, (data != null) ? data.length : 0);
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.ctx);
            int cam = preferences.getInt("camera",REAR_CAMERA);

            //here decide how big is going to be the bitmap
            this.getScreenSizes();
            double qualityRatio = 0.7;
            if(quality.equals("Low")){
                qualityRatio = 0.5;
            }else if(quality.equals("Medium")){
                qualityRatio = 0.65;
            }else if(quality.equals("High")){
                qualityRatio = 0.75;
            }else if(quality.equals("Very High")){
                qualityRatio=1;
            }
            Bitmap scaled = Bitmap.createScaledBitmap(bm,(int)(this.screenHight*qualityRatio), (int)(this.screenWidth*qualityRatio), true);

            if(cam == FRONT_CAMERA) {
                if(rotation == 0) {
                    int w = scaled.getWidth();
                    int h = scaled.getHeight();
                    // Setting post rotate to 90
                    Matrix mtx = new Matrix();
                    mtx.setScale(-1, 1);
                    mtx.postRotate(90);
                    // Rotating Bitmap
                    bm = Bitmap.createBitmap(scaled, 0, 0, w, h, mtx, true);
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bm.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    byteArray = stream.toByteArray();
                    scaled.recycle();
                    bm.recycle();
                    return byteArray;
                }else if (rotation == 3){// LANDSCAPE MODE
                    //No need to reverse width and height
                    int w = scaled.getWidth();
                    int h = scaled.getHeight();
                    Matrix mtx = new Matrix();
                    mtx.setScale(-1, 1);
                    mtx.postRotate(180);
                    bm=Bitmap.createBitmap(scaled, 0, 0, w, h, mtx, true);
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bm.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    byteArray = stream.toByteArray();
                    return byteArray;
                }else if(rotation == 2){
                    int w = scaled.getWidth();
                    int h = scaled.getHeight();
                    Matrix mtx = new Matrix();
                    mtx.setScale(-1, 1);
                    mtx.postRotate(270);
                    bm=Bitmap.createBitmap(scaled, 0, 0, w, h, mtx, true);
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bm.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    byteArray = stream.toByteArray();
                    return byteArray;
                } else if(rotation == 1){
                    int w = scaled.getWidth();
                    int h = scaled.getHeight();
                    Matrix mtx = new Matrix();
                    mtx.setScale(-1, 1);
                    // mtx.postRotate(0);
                    bm=Bitmap.createBitmap(scaled, 0, 0, w, h, mtx, true);
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bm.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    byteArray = stream.toByteArray();
                    return byteArray;
                }
            }else if(cam == REAR_CAMERA) {

                if (rotation == 0) {
                    int w = scaled.getWidth();
                    int h = scaled.getHeight();
                    // Setting post rotate to 90
                    Matrix mtx = new Matrix();
                    mtx.postRotate(90);
                    // Rotating Bitmap
                    bm = Bitmap.createBitmap(scaled, 0, 0, w, h, mtx, true);
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bm.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    byteArray = stream.toByteArray();
                    return byteArray;
                } else if (rotation == 3) {// LANDSCAPE MODE
                    //No need to reverse width and height
                    int w = scaled.getWidth();
                    int h = scaled.getHeight();
                    Matrix mtx = new Matrix();
                    mtx.postRotate(180);
                    bm = Bitmap.createBitmap(scaled, 0, 0, w, h, mtx, true);
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bm.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    byteArray = stream.toByteArray();
                    return byteArray;
                } else if (rotation == 2) {
                    int w = scaled.getWidth();
                    int h = scaled.getHeight();
                    Matrix mtx = new Matrix();
                    mtx.postRotate(270);
                    bm = Bitmap.createBitmap(scaled, 0, 0, w, h, mtx, true);
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bm.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    byteArray = stream.toByteArray();
                    return byteArray;
                } else if (rotation == 1) {
                    int w = scaled.getWidth();
                    int h = scaled.getHeight();
                    Matrix mtx = new Matrix();
                    bm = Bitmap.createBitmap(scaled, 0, 0, w, h, mtx, true);
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bm.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    byteArray = stream.toByteArray();
                    return byteArray;
                }
            }
        }
        return byteArray;
    }

    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    public  Bitmap getBitmapFromMemCache(String key) {
        return mMemoryCache.get(key);
    }



    private void setRotationAnimation() {
        WindowManager.LayoutParams layout = getWindow().getAttributes();
        layout.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL;
        layout.rotationAnimation = WindowManager.LayoutParams.ROTATION_ANIMATION_CROSSFADE;
        getWindow().setAttributes(layout);
    }

    private void getScreenSizes(){
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        quality = preferences.getString("Quality","Medium");
        Log.d("123","quality= "+quality);
        final DisplayMetrics metrics = new DisplayMetrics();
        Display display = getWindowManager().getDefaultDisplay();
        display.getRealMetrics(metrics);
       // if(quality.equals("Low") || quality.equals("High")) {
           // this.screenWidth = (int)(metrics.widthPixels*0.6);
          //  this.screenHight = (int)(metrics.heightPixels*0.6);
       // }else{
            this.screenWidth = metrics.widthPixels;
            this.screenHight = metrics.heightPixels;
       // }

        Log.d("123","this.screenWidths= "+this.screenWidth);
        Log.d("123","this.screenHight= "+this.screenHight);
    }

    private void setActivityLayout(){
        preview = new Preview(this, (SurfaceView)findViewById(R.id.surfaceView));
        preview.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        ((FrameLayout) findViewById(R.id.layout)).addView(preview);
        preview.setKeepScreenOn(true);
        this.onResume();
        initSwitchCameraButton();
        initTakePhotoButton();
        initSettingsButton();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setContentView(R.layout.activity_main_landscape);
            this.setActivityLayout();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            setContentView(R.layout.activity_main);
            this.setActivityLayout();
        }
    }
}