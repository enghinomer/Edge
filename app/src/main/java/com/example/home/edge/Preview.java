package com.example.home.edge;

/**
 * Created by home on 1/13/2015.
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import java.io.IOException;
import java.util.List;

class Preview extends ViewGroup implements SurfaceHolder.Callback {
    private final String TAG = "Preview";

    private SurfaceView mSurfaceView;
    public SurfaceHolder mHolder;
    public SharedPreferences preferences;
    private Size mPreviewSize;
    private static List sizes;
    private Camera.Size result;
    private List<Size> mSupportedPreviewSizes;
    private Camera mCamera;
    private OrientationEventListener orientationListener = null;
    private int pictureWidth;
    private int pictureHeight;
    private int screenWidth;
    private int screenHeight;
    private int cam;

    final private int REAR_CAMERA = 0;
    final private int FRONT_CAMERA = 1;

    public static int deg;

    Preview(Context context, SurfaceView sv) {
        super(context);

        preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.ctx);
        getScreenSize();
        mSurfaceView = sv;

        //mSurfaceView.getLayoutParams().width = 540;
        //mSurfaceView.getLayoutParams().height = 720;
        //and fix layout margin top
//        addView(mSurfaceView);

        mHolder = mSurfaceView.getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

    }

    private void getScreenSize(){
        final DisplayMetrics metrics = new DisplayMetrics();
        Display display = MainActivity.act.getWindowManager().getDefaultDisplay();
        display.getRealMetrics(metrics);
        this.screenWidth = metrics.widthPixels;
        this.screenHeight = metrics.heightPixels;
    }

    public void setCamera(Camera camera) {
        mCamera = camera;
        if (mCamera != null) {
            mSupportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();
            requestLayout();

            // get Camera parameters
            Camera.Parameters params = mCamera.getParameters();

            cam = preferences.getInt("camera", REAR_CAMERA);

            sizes = params.getSupportedPictureSizes();
            Camera.Size result = getPreviewSize(sizes);
            this.pictureHeight = result.height;
            Log.d("123","this.pictureHeigh= "+this.pictureHeight);
            this.pictureWidth = result.width;
            Log.d("123","this.pictureWidth= "+this.pictureWidth);
            setPreviewSize();

            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt("PictureWidth", result.width);
            editor.apply();
            editor.putInt("PictureHeight", result.height);
            editor.apply();
            List<String> focusModes = params.getSupportedFocusModes();
            if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                // set the focus mode
                params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            }
                //set rezolution


            if(cam == REAR_CAMERA)
                params.setPictureSize(result.width, result.height);
                //params.setPictureSize(3264, 1836);
            else if (cam ==FRONT_CAMERA)
                params.setPictureSize(result.width, result.height);
                // set Camera parameters
            mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, this.pictureWidth, this.pictureHeight);
                params.setPreviewSize(mPreviewSize.width, mPreviewSize.height);

            //params.setColorEffect(Camera.Parameters.SCENE_MODE_PORTRAIT);
                mCamera.setParameters(params);

        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // We purposely disregard child measurements because act as a
        // wrapper to a SurfaceView that centers the camera preview instead
        // of stretching it.

        final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        setMeasuredDimension(width, height);

        if (mSupportedPreviewSizes != null) {
            //send picture's width and height instead!!!!
            //mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, this.pictureWidth, this.pictureHeight);
            Log.d("123","optimal h= "+mPreviewSize.height);
            Log.d("123","optimal w= "+mPreviewSize.width);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        Log.d("123","onlayout");
        if (changed && getChildCount() > 0) {
            final View child = getChildAt(0);

            final int width = r - l;
            final int height = b - t;

            int previewWidth = width;
            int previewHeight = height;
            if (mPreviewSize != null) {
                previewWidth = mPreviewSize.width;
                previewHeight = mPreviewSize.height;
            }

            // Center the child SurfaceView within the parent.
            if (width * previewHeight > height * previewWidth) {
                final int scaledChildWidth = previewWidth * height / previewHeight;
                child.layout((width - scaledChildWidth) / 2, 0,
                        (width + scaledChildWidth) / 2, height);
            } else {
                final int scaledChildHeight = previewHeight * width / previewWidth;
                child.layout(0, (height - scaledChildHeight) / 2,
                        width, (height + scaledChildHeight) / 2);
            }
        }
    }

    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, acquire the camera and tell it where
        // to draw.
        //orientationListener.enable();
        try {
            if (mCamera != null) {
                mCamera.setPreviewDisplay(holder);
            }
        } catch (IOException exception) {
            Log.e(TAG, "IOException caused by setPreviewDisplay()", exception);
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // Surface will be destroyed when we return, so stop the preview.
        if (mCamera != null) {
            mCamera.stopPreview();
        }
       // orientationListener.disable();
    }




    private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) w / h;
        Log.d("123","ratio prev= "+targetRatio);
        if (sizes == null) return null;

        Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        // Try to find an size match aspect ratio and size
        for (Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    private Size getBestPictureSize(List sizes){
        int maxArea=0;
        Size result;
        Size bestSize = null;
        for (int i=0;i<sizes.size();i++){
            result = (Size) sizes.get(i);
            if(result.width * result.height > maxArea) {
                maxArea = result.width * result.height;
                bestSize = result;
            }
        }
        return bestSize;
    }

    private void setPreviewSize(){
        WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        int rotation = windowManager.getDefaultDisplay().getRotation();
        double ratio = (double)this.pictureWidth/(double)this.pictureHeight;

        if(rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180){
            int height = (int)(this.screenWidth * ratio);
            mSurfaceView.getLayoutParams().width = this.screenWidth;
            mSurfaceView.getLayoutParams().height = height;
        }else if(rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270){
            mSurfaceView.getLayoutParams().height = this.screenHeight;
            int width = (int)(this.screenHeight * ratio);
            mSurfaceView.getLayoutParams().width = width;
        }
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        Log.d("123","surfaceChanged");
        WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        int rotation = windowManager.getDefaultDisplay().getRotation();
        Log.d("123",rotation + "");
        if(mCamera != null) {
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
            requestLayout();

           // MainActivity.setCameraDisplayOrientation(MainActivity.act, 1, mCamera);
            switch (rotation) {
                case Surface.ROTATION_0:
                    mCamera.setDisplayOrientation(90);
                    Log.d("123","rot0");
                    deg = 90;
                    break;
                case Surface.ROTATION_90:
                    mCamera.setDisplayOrientation(0);
                    Log.d("123","rot90");
                    break;
                case Surface.ROTATION_180:
                    mCamera.setDisplayOrientation(270);
                    Log.d("123","rot180");
                    break;
                case Surface.ROTATION_270:
                    mCamera.setDisplayOrientation(180);
                    Log.d("123","rot270");
                    deg = 180;
                    break;
            }

            mCamera.setParameters(parameters);
            //mCamera.startPreview();
        }
    }

    public int getPictureWidth(){
        return this.pictureWidth;
    }

    public int getPictureHeight(){
        return this.pictureHeight;
    }

    public int getScreenWidth(){
        return this.screenWidth;
    }

    public int getScreenHeight(){
        return this.screenHeight;
    }

    public static List getSizes(){return sizes;}

    public static String getDefaultImageSize(){
        int median = sizes.size()/2;
        String defaultValue;
        Size result;
        result = (Size) sizes.get(median);
        defaultValue = result.width+"x"+result.height;
       return defaultValue;
    }

    public Size getPreviewSize(List supportedSizes){
        long area = 335600;
        long minArea = 999999;
        Size result;
        Size bestSize = null;
        for (int i=0;i<supportedSizes.size();i++){
            result = (Size) supportedSizes.get(i);
            if(Math.abs(result.width * result.height - area)<minArea) {
                bestSize = result;
                minArea = Math.abs(result.width * result.height - area);
            }
        }

        Log.d("123","size= "+bestSize.height+"x"+bestSize.width);
        return bestSize;
    }

    public Size getSelectedImageSize(List supportedSizes){
        String stringSize = preferences.getString("RearResolution", this.getDefaultImageSize());
        if(cam == REAR_CAMERA) {
             stringSize = preferences.getString("RearResolution", this.getDefaultImageSize());
        }else if(cam == FRONT_CAMERA){
             stringSize = preferences.getString("FrontResolution", this.getDefaultImageSize());
        }
        String [] sizes = stringSize.split("x");

        int selectedWidth = Integer.parseInt(sizes[0]);
        int selectedHeight = Integer.parseInt(sizes[1]);
        Size result;
        Size bestSize = null;
        for (int i=0;i<supportedSizes.size();i++){
            result = (Size) supportedSizes.get(i);
            if(result.width * result.height == selectedHeight*selectedWidth) {
                bestSize = result;
            }
        }

        return bestSize;
    }
}
