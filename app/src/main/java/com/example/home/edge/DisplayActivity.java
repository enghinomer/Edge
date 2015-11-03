package com.example.home.edge;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.GradientDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.LruCache;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


public class DisplayActivity extends Activity {

    public Bitmap bitmap;
    public SharedPreferences preferences;
    private ImageView image;
    private int imageRotation;
    private int screenWidth;
    private int screenHight;
    private int imageWidth = 0;
    private int imageHeight = 0;
    private int uploadedImageWidh;
    private int uploadedImageHeight;
    private int uploadedImageRotation = -1;
    private float contrast = 1;
    private float brightness = 0;
    private String quality;
    private boolean isFullScreen = false;
    private View decorView;
    private Effect effects;
    private Bitmap originalBitmap;
    private Activity act;
    private ProgressBar mProgress;
    private Button lastButton = null;
    private int lastButtonImage = 0;
    private static int RESULT_LOAD_IMAGE = 1;
    final private String IMAGE_RESOURCE_ID = "11";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("123", "DisplayActivity");
        super.onCreate(savedInstanceState);
        act = this;
        Configuration config = getResources().getConfiguration();
        if (config.orientation == Configuration.ORIENTATION_LANDSCAPE){
            setContentView(R.layout.activity_display_landscape);
        }else if (config.orientation == Configuration.ORIENTATION_PORTRAIT){
            setContentView(R.layout.activity_display);
        }

        originalBitmap = getBitmapFromMemCache(IMAGE_RESOURCE_ID);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        quality = preferences.getString("Quality","Medium");
        Log.d("123","quality= "+quality);
        this.effects = new Effect(quality);

        /*
        bitmap1 = this.effects.oldEffect(originalBitmap);
        bitmap2 = this.effects.edgeEffect(originalBitmap);*/
        initializeButtons();

        decorView = getWindow().getDecorView();
        this.showSystemUI();

        getScreenSizes();

        Bundle extras = getIntent().getExtras();
        this.imageRotation = extras.getInt("rotation");

        mProgress = (ProgressBar) findViewById(R.id.progressBar);
        mProgress.setVisibility(View.INVISIBLE);

        image = (ImageView) findViewById(R.id.imageView1);
        getImageSizes();
        image.getLayoutParams().width = imageWidth;
        image.getLayoutParams().height = imageHeight;


        image.setImageBitmap(originalBitmap);
        //deleteTempFile();
        //saveFile();
    }

    @Override
    protected void onResume(){
        super.onResume();
        loadBitmap(11, image, 0);
    }

    @Override
    protected void onDestroy(){
        Log.d("123","detroy");
        super.onDestroy();
        //MainActivity.mMemoryCache.remove("11");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_display, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            Bitmap scaled = decodeSampledBitmapFromFile(picturePath,this.screenWidth,this.imageHeight);

            this.uploadedImageWidh = scaled.getWidth();
            this.uploadedImageHeight = scaled.getHeight();

            Log.d("123","width: "+scaled.getWidth()+"height: "+scaled.getHeight());
            originalBitmap = Bitmap.createScaledBitmap(scaled,(int)(this.screenHight*1), (int)(this.screenWidth*1), true);
            scaled.recycle();

            Matrix mtx = new Matrix();
            //mtx.setScale(-1, 1);
            uploadedImageRotation = getOrientationFromExif(picturePath);
            if(uploadedImageRotation >= 0){
                mtx.postRotate(uploadedImageRotation);
            }else{
                uploadedImageRotation = getOrientationFromMedia(selectedImage);
                mtx.postRotate(uploadedImageRotation);
            }

            getImageSizes(uploadedImageRotation,uploadedImageWidh,uploadedImageHeight);
            originalBitmap=Bitmap.createBitmap(originalBitmap, 0, 0, this.screenHight, this.screenWidth, mtx, true);

            image.setImageBitmap(originalBitmap);

        }


    }

    private void initializeButtons(){

        //back to camera
        final ImageButton back = (ImageButton) findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish() ;
            }
        });

        //save image button
        final ImageButton save = (ImageButton) findViewById(R.id.download);
        save.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(isExternalStorageWritable()) {
                    new SaveImageTask().execute();
                }else{
                    Toast.makeText(act, "SD Card not mounted", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //upload an image
        final ImageButton upload = (ImageButton) findViewById(R.id.upload);
        upload.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(
                        Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                startActivityForResult(i, RESULT_LOAD_IMAGE);
            }
        });
/*
        //add contrast button
        final ImageButton addContrast = (ImageButton) findViewById(R.id.addContrast);
        addContrast.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d("123","add= "+contrast);
                contrast+=0.2;
                bitmap = effects.changeBitmapContrastBrightness(originalBitmap,contrast,brightness);
                image.setImageBitmap(bitmap);
            }
        });

        //add contrast button
        final ImageButton decreaseContrast = (ImageButton) findViewById(R.id.decreaseContrast);
        decreaseContrast.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d("123","minus= "+contrast);
                contrast-=0.2;
                bitmap = effects.changeBitmapContrastBrightness(originalBitmap,contrast,brightness);
                image.setImageBitmap(bitmap);
            }
        });

        //add brightness button
        final ImageButton addBrightness = (ImageButton) findViewById(R.id.addBrightness);
        addBrightness.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                brightness+=10;
                bitmap = effects.changeBitmapContrastBrightness(originalBitmap,contrast,brightness);
                image.setImageBitmap(bitmap);
            }
        });

        //add contrast button
        final ImageButton decreaseBrightness = (ImageButton) findViewById(R.id.decreaseBrightness);
        decreaseBrightness.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                brightness-=10;
                bitmap = effects.changeBitmapContrastBrightness(originalBitmap,contrast,brightness);
                image.setImageBitmap(bitmap);
            }
        });
*/
        final Button button = (Button) findViewById(R.id.button1);
        button.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                loadBitmap(11, image, 1);
            }
        });

        final Button button2 = (Button) findViewById(R.id.button2);

        button2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(lastButton != null && lastButtonImage != 0){
                    lastButton.setBackgroundResource(lastButtonImage);
                }
                lastButton = button2;
                lastButtonImage = R.drawable.uppsala;
                button2.setBackgroundResource(R.drawable.back1);

                //loadBitmap(11, image, 2);
                new LoadImageTask().execute(2);
            }
        });

        final Button button3 = (Button) findViewById(R.id.button3);
        button3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(lastButton != null && lastButtonImage != 0){
                    lastButton.setBackgroundResource(lastButtonImage);
                }
                lastButton = button3;
                lastButtonImage = R.drawable.uppsala;
                button3.setBackgroundResource(R.drawable.back1);
                //loadBitmap(11, image, 3);
                new LoadImageTask().execute(3);
            }
        });

        final Button button4 = (Button) findViewById(R.id.button4);
        button4.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //loadBitmap(11, image, 4);
                new LoadImageTask().execute(4);
            }
        });

        final Button button5 = (Button) findViewById(R.id.button5);
        button5.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //loadBitmap(11, image, 5);
                new LoadImageTask().execute(5);
            }
        });

        final Button button6 = (Button) findViewById(R.id.button6);
        button6.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //loadBitmap(11, image, 6);
                new LoadImageTask().execute(6);
            }
        });

        final Button button7 = (Button) findViewById(R.id.button7);
        button7.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //loadBitmap(11, image, 7);
                new LoadImageTask().execute(7);
            }
        });

        final Button button8 = (Button) findViewById(R.id.button8);
        button8.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //loadBitmap(11, image, 8);
                new LoadImageTask().execute(8);
            }
        });

        final Button button9 = (Button) findViewById(R.id.button9);
        button9.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //loadBitmap(11, image, 9);
                new LoadImageTask().execute(9);
            }
        });

        final Button button10 = (Button) findViewById(R.id.button10);
        button10.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //loadBitmap(11, image, 10);
                new LoadImageTask().execute(10);
            }
        });

        final Button button11 = (Button) findViewById(R.id.button11);
        button11.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //loadBitmap(11, image, 11);
                new LoadImageTask().execute(11);
            }
        });

        final Button button12 = (Button) findViewById(R.id.button12);
        button12.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //loadBitmap(11, image, 12);
                new LoadImageTask().execute(12);
            }
        });

        final Button button13 = (Button) findViewById(R.id.button13);
        button13.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //loadBitmap(11, image, 13);
                new LoadImageTask().execute(13);
            }
        });

        final Button button14 = (Button) findViewById(R.id.button14);
        button14.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //loadBitmap(11, image, 14);
                new LoadImageTask().execute(14);
            }
        });

        final Button button15 = (Button) findViewById(R.id.button15);
        button15.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //loadBitmap(11, image, 15);
                new LoadImageTask().execute(15);
            }
        });

        final Button button16 = (Button) findViewById(R.id.button16);
        button16.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //loadBitmap(11, image, 16);
                new LoadImageTask().execute(16);
            }
        });

        final Button button17 = (Button) findViewById(R.id.button17);
        button17.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //loadBitmap(11, image, 17);
                new LoadImageTask().execute(17);
            }
        });

        final Button button18 = (Button) findViewById(R.id.button18);
        button18.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //loadBitmap(11, image, 18);
                new LoadImageTask().execute(18);
            }
        });

        final Button button19 = (Button) findViewById(R.id.button19);
        button19.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //loadBitmap(11, image, 19);
                new LoadImageTask().execute(19);
            }
        });

        final Button button20 = (Button) findViewById(R.id.button20);
        button20.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //loadBitmap(11, image, 20);
                new LoadImageTask().execute(20);
            }
        });

        final Button button21 = (Button) findViewById(R.id.button21);
        button21.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //loadBitmap(11, image, 21);
                new LoadImageTask().execute(21);
            }
        });

        final Button button22 = (Button) findViewById(R.id.button22);
        button22.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //loadBitmap(11, image, 22);
                new LoadImageTask().execute(22);
            }
        });

        final Button button23 = (Button) findViewById(R.id.button23);
        button23.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //loadBitmap(11, image, 23);
                new LoadImageTask().execute(23);
            }
        });

        final Button button24 = (Button) findViewById(R.id.button24);
        button24.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //loadBitmap(11, image, 24);
                new LoadImageTask().execute(24);
            }
        });

        final Button button25 = (Button) findViewById(R.id.button25);
        button25.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //loadBitmap(11, image, 25);
                new LoadImageTask().execute(25);
            }
        });

        final Button button26 = (Button) findViewById(R.id.button26);
        button26.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //loadBitmap(11, image, 26);
                new LoadImageTask().execute(26);
            }
        });

        final Button button27 = (Button) findViewById(R.id.button27);
        button27.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //loadBitmap(11, image, 27);
                new LoadImageTask().execute(27);
            }
        });

        final Button button28 = (Button) findViewById(R.id.button28);
        button28.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //loadBitmap(11, image, 28);
                new LoadImageTask().execute(28);
            }
        });

        final Button button29 = (Button) findViewById(R.id.button29);
        button29.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //loadBitmap(11, image, 29);
                new LoadImageTask().execute(29);
            }
        });
    }

    public void saveFile(){
        FileOutputStream outStream = null;
        try {
        //File sdCard = Environment.getExternalStorageDirectory();
        //Log.d("123",sdCard+" sd");
        //File dir = new File (sdCard.getAbsolutePath() + "/camtest");
        //dir.mkdirs();

        String fileName = String.format("%d.jpg", System.currentTimeMillis());
        File outFile = new File(getAlbumStorageDir("Edge"), fileName);

        //Log.d("123", sdCard.getAbsolutePath() + "/camtest");

            //create bitmap with the same size as the original one
            Bitmap finalBitmap = Bitmap.createScaledBitmap(bitmap, image.getWidth(),image.getHeight(), true);

            outStream = new FileOutputStream(outFile);
            outStream.write(Effect.bitmapToByte(finalBitmap));
            outStream.flush();
            outStream.close();
            refreshGallery(outFile);
            finalBitmap.recycle();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
         catch (IOException e) {
            e.printStackTrace();
        }
        act.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(act, "Picture Saved", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void refreshGallery(File file) {
        Intent mediaScanIntent = new Intent( Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(Uri.fromFile(file));
        sendBroadcast(mediaScanIntent);
    }

    public void loadBitmap(int resId, ImageView imageView, int effectId) {
        /*final String imageKey = String.valueOf(resId);

        Bitmap bitmap = getBitmapFromMemCache(imageKey);*/

        bitmap = null;

        if (originalBitmap != null) {
            switch (effectId){
                case 1:
                    bitmap = originalBitmap;
                    imageView.setImageBitmap(bitmap);
                    break;
                case 2:
                    bitmap = effects.oldEffect(originalBitmap);
                    //imageView.setImageBitmap(bitmap);
                    break;
                case 3:
                    bitmap = effects.getGreyScale(originalBitmap);
                    //imageView.setImageBitmap(bitmap);
                    break;
                case 4:
                    bitmap = effects.addBorder(originalBitmap);
                    //imageView.setImageBitmap(bitmap);
                    break;
                case 5:
                    bitmap = effects.getSnowEffect(originalBitmap);
                    //imageView.setImageBitmap(bitmap);
                    break;
                case 6:
                    bitmap = effects.getTintImage(originalBitmap, 90);
                    //imageView.setImageBitmap(bitmap);
                    break;
                case 7:
                    bitmap = effects.doInvert(originalBitmap);
                    //imageView.setImageBitmap(bitmap);
                    break;
                case 8:
                    bitmap = effects.addEffect(originalBitmap,5,5.0,6.0,0.0);
                    //imageView.setImageBitmap(bitmap);
                    break;
                case 9:
                    bitmap = effects.addEffect(originalBitmap,5,5.0,0.0,10.0);
                    //imageView.setImageBitmap(bitmap);
                    break;
                case 10:
                    bitmap = effects.addEffect(originalBitmap,5,0.0,10.0,0.0);
                    //imageView.setImageBitmap(bitmap);
                    break;
                case 11:
                    bitmap = effects.addEffect(originalBitmap,15,5.0,0.0,10.0);
                    //imageView.setImageBitmap(bitmap);
                    break;
                case 12:
                    bitmap = effects.addEffect(originalBitmap,5,10.0,0.0,0.0);
                    //imageView.setImageBitmap(bitmap);
                    break;
                case 13:
                    bitmap = effects.decreaseColorDepth(originalBitmap,32);
                    //imageView.setImageBitmap(bitmap);
                    break;
                case 14:
                    bitmap = effects.decreaseColorDepth(originalBitmap,64);
                    //imageView.setImageBitmap(bitmap);
                    break;
                case 15:
                    bitmap = effects.decreaseColorDepth(originalBitmap,128);
                    //imageView.setImageBitmap(bitmap);
                    break;
                case 16:
                    bitmap = effects.yellowNoise(originalBitmap);
                    //imageView.setImageBitmap(bitmap);
                    break;
                case 17:
                    bitmap = effects.createBitmap_convolve(originalBitmap);
                    //imageView.setImageBitmap(bitmap);
                    break;
                case 18:
                    bitmap = effects.sobelFilter(originalBitmap);
                    //imageView.setImageBitmap(bitmap);
                    break;
                case 19:
                    bitmap = effects.addWhiteNoise1(originalBitmap);
                    //imageView.setImageBitmap(bitmap);
                    break;
                case 20:
                    bitmap = effects.addBorder2(originalBitmap);
                    //imageView.setImageBitmap(bitmap);
                    break;
                case 21:
                    bitmap = effects.addBlackNoise(originalBitmap);
                    //imageView.setImageBitmap(bitmap);
                    break;
                case 22:
                    bitmap = effects.sharpenFilter(originalBitmap);
                    //imageView.setImageBitmap(bitmap);
                    break;
                case 23:
                    bitmap = effects.rescaleFilter(originalBitmap);
                    //imageView.setImageBitmap(bitmap);
                    break;
                case 24:
                    bitmap = effects.yellowishNoise(originalBitmap);
                    //imageView.setImageBitmap(bitmap);
                    break;
                case 25:
                    bitmap = effects.papirus(originalBitmap);
                    //imageView.setImageBitmap(bitmap);
                    break;
                case 26:
                    bitmap = effects.desert(originalBitmap);
                    //imageView.setImageBitmap(bitmap);
                    break;
                case 27:
                    bitmap = effects.abstract1(originalBitmap);
                    //imageView.setImageBitmap(bitmap);
                    break;
                case 28:
                    bitmap = effects.abstract2(originalBitmap);
                    //imageView.setImageBitmap(bitmap);
                    break;
                case 29:
                    bitmap = effects.abstract3(originalBitmap);
                    //imageView.setImageBitmap(bitmap);
                    break;
            }
            /*if(bitmap == null)
                imageView.setImageBitmap(originalBitmap);
                else
                imageView.setImageBitmap(bitmap);*/
        } else {
            Log.d("123", "display null");
        }
    }

    private void getScreenSizes(){
        final DisplayMetrics metrics = new DisplayMetrics();
        Display display = getWindowManager().getDefaultDisplay();
        display.getRealMetrics(metrics);

            this.screenWidth = metrics.widthPixels;
            this.screenHight = metrics.heightPixels;

        Log.d("123","this.screenWidths= "+this.screenWidth);
        Log.d("123","this.screenHight= "+this.screenHight);
    }

    private void getImageSizes(){

        int width = preferences.getInt("PictureWidth",0);
        int height = preferences.getInt("PictureHeight",0);
        double ratio = (double)width/(double)height;
        //ratio = 1.7777778;

        if(this.imageRotation == 0 || this.imageRotation == 2) {
            if ((this.screenWidth * ratio) > this.screenHight) {
                imageHeight = this.screenHight;
                imageWidth = (int) (this.screenHight / ratio);
            } else {
                imageWidth = this.screenWidth;
                imageHeight = (int) (this.screenWidth * ratio);
            }
        }else if(this.imageRotation == 1 || this.imageRotation == 3){
            if ((this.screenHight * ratio) > this.screenWidth){
                imageHeight = (int)(this.screenWidth/ratio);
                imageWidth = this.screenWidth;
            }else{
                imageHeight = this.screenHight;
                imageWidth = (int)(this.screenHight*ratio);
            }
        }

        Log.d("123","ratio= "+String.valueOf(ratio));
        Log.d("123","image h= "+imageHeight);
        Log.d("123","image w= "+imageWidth);
    }

    private void getImageSizes(int rotation, int width, int height){

        double ratio = (double)height/(double)width;
        Log.d("123","ratio= "+String.valueOf(ratio));

        if(rotation == 0 || rotation == 180) {
            if(rotation == 0){
                this.imageRotation = 0;
            }else if(rotation == 180){
                this.imageRotation = 2;
            }
            if ((this.screenWidth * ratio) > this.screenHight) {
                imageHeight = this.screenHight;
                imageWidth = (int) (this.screenHight / ratio);
            } else {
                imageWidth = this.screenWidth;
                imageHeight = (int) (this.screenWidth * ratio);
            }
        }else if(rotation == 90 || rotation == 270){
            if(rotation == 90){
                this.imageRotation = 1;
            }else if(rotation == 270){
                this.imageRotation = 3;
            }
            if ((this.screenHight * ratio) > this.screenWidth){
                imageHeight = (int)(this.screenWidth/ratio);
                imageWidth = this.screenWidth;
            }else{
                imageHeight = this.screenHight;
                imageWidth = (int)(this.screenHight*ratio);
            }
        }

        Log.d("123","imageWidth="+imageWidth);
        Log.d("123","imageHeight="+imageHeight);
        image.getLayoutParams().width = imageWidth;
        image.getLayoutParams().height = imageHeight;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d("123","on config changed");
        //getImageSizes();
        getImageSizes(this.uploadedImageRotation, this.uploadedImageWidh, this.uploadedImageHeight);

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setContentView(R.layout.activity_display_landscape);
            mProgress = (ProgressBar) findViewById(R.id.progressBar);
            mProgress.setVisibility(View.INVISIBLE);
            image = (ImageView) findViewById(R.id.imageView1);
            //getImageSizes();
            getImageSizes(this.uploadedImageRotation, this.uploadedImageWidh, this.uploadedImageHeight);
            image.getLayoutParams().width = imageWidth;
            image.getLayoutParams().height = imageHeight;

            initializeButtons();

            if(bitmap == null){
                image.setImageBitmap(originalBitmap);
            }else {
                image.setImageBitmap(bitmap);
            }
            this.showSystemUI();
            if(this.imageRotation == 0 || this.imageRotation == 2){
                image.getLayoutParams().width = (imageWidth * imageWidth)/imageHeight;
                image.getLayoutParams().height = imageWidth;
            }
            else{
                image.getLayoutParams().width = imageWidth;
                image.getLayoutParams().height = imageHeight;
            }

        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            setContentView(R.layout.activity_display);
            mProgress = (ProgressBar) findViewById(R.id.progressBar);
            mProgress.setVisibility(View.INVISIBLE);
            image = (ImageView) findViewById(R.id.imageView1);
            //getImageSizes();
            getImageSizes(this.uploadedImageRotation, this.uploadedImageWidh, this.uploadedImageHeight);
            image.getLayoutParams().width = imageWidth;
            image.getLayoutParams().height = imageHeight;

            initializeButtons();

            if(bitmap == null){
                image.setImageBitmap(originalBitmap);
            }else {
                image.setImageBitmap(bitmap);
            }
            this.showSystemUI();
            if(this.imageRotation == 1 || this.imageRotation == 3){
                image.getLayoutParams().width = imageHeight;
                image.getLayoutParams().height = (imageHeight *imageHeight)/imageWidth;
            }
            else {
                image.getLayoutParams().width = imageWidth;
                image.getLayoutParams().height = imageHeight;
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int eventAction = event.getAction();

        switch (eventAction) {

            case MotionEvent.ACTION_DOWN:
                if(this.isFullScreen == true){
                    this.showSystemUI();
                    this.isFullScreen = false;
                }else{
                    this.hideSystemUI();
                    this.isFullScreen = true;
                }
                break;

            case MotionEvent.ACTION_MOVE:
                break;

            case MotionEvent.ACTION_UP:
                break;
        }
        return true;
    }

    private void hideSystemUI() {
        // Set the IMMERSIVE flag.
        // Set the content to appear under the system bars so that the content
        // doesn't resize when the system bars hide and show.
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);

        View view = findViewById(R.id.scroll);
        view.setVisibility(View.GONE);
    }

    // This snippet shows the system bars. It does this by removing all the flags
// except for the ones that make the content appear under the system bars.
    private void showSystemUI() {
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        View view = findViewById(R.id.scroll);
        view.setVisibility(View.VISIBLE);
    }


    public  Bitmap getBitmapFromMemCache(String key) {
        return MainActivity.mMemoryCache.get(key);
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    public File getAlbumStorageDir(String albumName) {
        // Get the directory for the user's public pictures directory.
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), albumName);
        if (!file.mkdirs()) {
            Log.e("err", "Directory not created");
        }
        return file;
    }

    private int getOrientationFromExif(String imagePath) {
        int orientation = -1;
        try {
            ExifInterface exif = new ExifInterface(imagePath);
            int exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);

            switch (exifOrientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    orientation = 270;

                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    orientation = 180;

                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    orientation = 90;

                    break;

                case ExifInterface.ORIENTATION_NORMAL:
                    orientation = 0;

                    break;
                default:
                    break;
            }
        } catch (IOException e) {
            Log.e("err", "Unable to get image exif orientation", e);
        }

        return orientation;
    }

    private int getOrientationFromMedia(Uri imageUri){
        String[] orientationColumn = {MediaStore.Images.Media.ORIENTATION};
        Cursor cur = getContentResolver().query(imageUri, orientationColumn, null, null, null);
        int orientation = -1;
        if (cur != null && cur.moveToFirst()) {
            orientation = cur.getInt(cur.getColumnIndex(orientationColumn[0]));
        }
        return orientation;
    }

    private int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        Log.d("123", "original height= "+height);
        Log.d("123", "original width= "+width);
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

    private Bitmap decodeSampledBitmapFromFile(String filePath, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filePath, options);
    }

    private class SaveImageTask extends AsyncTask<byte[], Void, Void> {

        @Override
        protected Void doInBackground(byte[]... data) {
            FileOutputStream outStream = null;

            // Write to SD Card
            try {
                saveFile();

            } catch (Exception e) {
                e.printStackTrace();
            }finally {
            }
            return null;
        }

    }

    private class LoadImageTask extends AsyncTask<Integer, Void, Void> {

        @Override public void onPreExecute() {
            mProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Integer... data) {
            int effectNumber = data[0];
            Log.d("123","effect number= "+effectNumber);
            loadBitmap(11, image, effectNumber);
            return null;
        }

        @Override
        public void onPostExecute(Void v) {
            image.setImageBitmap(bitmap);
            mProgress.setVisibility(View.INVISIBLE);
            return;
        }
    }

}


