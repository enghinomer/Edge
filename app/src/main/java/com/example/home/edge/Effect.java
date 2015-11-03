package com.example.home.edge;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.renderscript.ScriptIntrinsicConvolve3x3;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.util.Random;

/**
 * Created by home on 1/23/2015.
 */
public class Effect {

    private int scale = 1;
    private int delta = 0;
    private Bitmap bitmap;
    private byte[] edgeArray;
    private byte[] oldArray;
    private Bitmap.Config configuration;

    public Effect(String configuration){
        if(configuration.equals("Low") || configuration.equals("High")){
            //this.configuration = Bitmap.Config.RGB_565;
            this.configuration = Bitmap.Config.ARGB_8888;
            Log.d("123","config= rgb");
            }else if(configuration.equals("Medium") || configuration.equals("Very High")){
            this.configuration = Bitmap.Config.ARGB_8888;
            Log.d("123","config= Argb");
        }
    }


    public Bitmap oldEffect(Bitmap bmp){
        int width = bmp.getWidth();
        int height = bmp.getHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height, this.configuration);
        Log.d("123","bmp nytes= "+bmp.getByteCount());
        Log.d("123","bitmap nytes= "+bitmap.getByteCount());
        int pixColor = 0;
        int pixR = 0;
        int pixG = 0;
        int pixB = 0;
        int newR = 0;
        int newG = 0;
        int newB = 0;
        int[] pixels = new int[width * height];
        bmp.getPixels(pixels, 0, width, 0, 0, width, height);
        for (int i = 0; i < height; i++)
        {
            for (int k = 0; k < width; k++)
            {
                pixColor = pixels[width * i + k];
                pixR = Color.red(pixColor);
                pixG = Color.green(pixColor);
                pixB = Color.blue(pixColor);
                newR = (int) (0.393 * pixR + 0.769 * pixG + 0.189 * pixB);
                newG = (int) (0.349 * pixR + 0.686 * pixG + 0.168 * pixB);
                newB = (int) (0.272 * pixR + 0.534 * pixG + 0.131 * pixB);
                int newColor = Color.argb(255, newR > 255 ? 255 : newR, newG > 255 ? 255 : newG, newB > 255 ? 255 : newB);
                pixels[width * i + k] = newColor;
            }
        }

        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        //oldArray = bitmapToByte(bitmap);
        return bitmap;
    }

    public Bitmap getGreyScaleARGB(Bitmap bmp){
        int width = bmp.getWidth();
        int height = bmp.getHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        int pixColor = 0;
        int A;
        int pixR = 0;
        int pixG = 0;
        int pixB = 0;
        int newR = 0;
        int newG = 0;
        int newB = 0;
        int[] pixels = new int[width * height];
        bmp.getPixels(pixels, 0, width, 0, 0, width, height);
        for (int i = 0; i < height; i++)
        {
            for (int k = 0; k < width; k++)
            {
                pixColor = pixels[width * i + k];
                A = Color.alpha(pixColor);
                pixR = Color.red(pixColor);
                pixG = Color.green(pixColor);
                pixB = Color.blue(pixColor);
                newR = newG = newB = (int) (0.299 * pixR + 0.587 * pixG + 0.114 * pixB);
                int newColor = Color.argb(A, newR > 255 ? 255 : newR, newG > 255 ? 255 : newG, newB > 255 ? 255 : newB);
                pixels[width * i + k] = newColor;
            }
        }

        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        //oldArray = bitmapToByte(bitmap);
        return bitmap;
    }

    public Bitmap getGreyScale(Bitmap bmp){
        int width = bmp.getWidth();
        int height = bmp.getHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height, this.configuration);
        int pixColor = 0;
        int A;
        int pixR = 0;
        int pixG = 0;
        int pixB = 0;
        int newR = 0;
        int newG = 0;
        int newB = 0;
        int[] pixels = new int[width * height];
        bmp.getPixels(pixels, 0, width, 0, 0, width, height);
        for (int i = 0; i < height; i++)
        {
            for (int k = 0; k < width; k++)
            {
                pixColor = pixels[width * i + k];
                A = Color.alpha(pixColor);
                pixR = Color.red(pixColor);
                pixG = Color.green(pixColor);
                pixB = Color.blue(pixColor);
                newR = newG = newB = (int) (0.299 * pixR + 0.587 * pixG + 0.114 * pixB);
                int newColor = Color.argb(A, newR > 255 ? 255 : newR, newG > 255 ? 255 : newG, newB > 255 ? 255 : newB);
                pixels[width * i + k] = newColor;
            }
        }

        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        //oldArray = bitmapToByte(bitmap);
        return bitmap;
    }

    public Bitmap getHighlightImage(Bitmap src) {
        // create new bitmap, which will be painted and becomes result image
        Bitmap bmOut = Bitmap.createBitmap(src.getWidth() + 96, src.getHeight() + 96, Bitmap.Config.ARGB_8888);
        // setup canvas for painting
        Canvas canvas = new Canvas(bmOut);
        // setup default color
        canvas.drawColor(0, PorterDuff.Mode.CLEAR);

        // create a blur paint for capturing alpha
        Paint ptBlur = new Paint();
        ptBlur.setMaskFilter(new BlurMaskFilter(15, BlurMaskFilter.Blur.NORMAL));
        int[] offsetXY = new int[2];
        // capture alpha into a bitmap
        Bitmap bmAlpha = src.extractAlpha(ptBlur, offsetXY);
        // create a color paint
        Paint ptAlphaColor = new Paint();
        ptAlphaColor.setColor(0xFFFFFFFF);
        // paint color for captured alpha region (bitmap)
        canvas.drawBitmap(bmAlpha, offsetXY[0], offsetXY[1], ptAlphaColor);
        // free memory
        bmAlpha.recycle();

        // paint the image source
        canvas.drawBitmap(src, 0, 0, null);

        // return out final image
        return bmOut;
    }

    public Bitmap getSnowEffect(Bitmap source) {
        // get image size
        int width = source.getWidth();
        int height = source.getHeight();
        int[] pixels = new int[width * height];
        int COLOR_MAX = 0xff;
        // get pixel array from source
        source.getPixels(pixels, 0, width, 0, 0, width, height);
        // random object
        Random random = new Random();

        int R, G, B, index = 0, thresHold = 50;
        // iteration through pixels
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                // get current index in 2D-matrix
                index = y * width + x;
                // get color
                R = Color.red(pixels[index]);
                G = Color.green(pixels[index]);
                B = Color.blue(pixels[index]);
                // generate threshold
                thresHold = random.nextInt(COLOR_MAX);
                if (R > thresHold && G > thresHold && B > thresHold) {
                    pixels[index] = Color.rgb(COLOR_MAX, COLOR_MAX, COLOR_MAX);
                }
            }
        }
        // output bitmap
        Bitmap bmOut = Bitmap
                .createBitmap(width, height, this.configuration);
        bmOut.setPixels(pixels, 0, width, 0, 0, width, height);
        return bmOut;
    }

    public Bitmap getTintImage(Bitmap src, int degree) {

         final double PI = 3.14159d;
         final double FULL_CIRCLE_DEGREE = 360d;
         final double HALF_CIRCLE_DEGREE = 180d;
         final double RANGE = 256d;

        int width = src.getWidth();
        int height = src.getHeight();

        int[] pix = new int[width * height];
        src.getPixels(pix, 0, width, 0, 0, width, height);

        int RY, GY, BY, RYY, GYY, BYY, R, G, B, Y;
        double angle = (PI * (double) degree) / HALF_CIRCLE_DEGREE;

        int S = (int) (RANGE * Math.sin(angle));
        int C = (int) (RANGE * Math.cos(angle));

        for (int y = 0; y < height; y++)
            for (int x = 0; x < width; x++) {
                int index = y * width + x;
                int r = (pix[index] >> 16) & 0xff;
                int g = (pix[index] >> 8) & 0xff;
                int b = pix[index] & 0xff;
                RY = (70 * r - 59 * g - 11 * b) / 100;
                GY = (-30 * r + 41 * g - 11 * b) / 100;
                BY = (-30 * r - 59 * g + 89 * b) / 100;
                Y = (30 * r + 59 * g + 11 * b) / 100;
                RYY = (S * BY + C * RY) / 256;
                BYY = (C * BY - S * RY) / 256;
                GYY = (-51 * RYY - 19 * BYY) / 100;
                R = Y + RYY;
                R = (R < 0) ? 0 : ((R > 255) ? 255 : R);
                G = Y + GYY;
                G = (G < 0) ? 0 : ((G > 255) ? 255 : G);
                B = Y + BYY;
                B = (B < 0) ? 0 : ((B > 255) ? 255 : B);
                pix[index] = 0xff000000 | (R << 16) | (G << 8) | B;
            }

        Bitmap outBitmap = Bitmap.createBitmap(width, height, this.configuration);
        outBitmap.setPixels(pix, 0, width, 0, 0, width, height);

        pix = null;

        return outBitmap;
    }

    public Bitmap doInvertARGB(Bitmap bmp) {
        int width = bmp.getWidth();
        int height = bmp.getHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        int pixColor = 0;
        int A;
        int pixR = 0;
        int pixG = 0;
        int pixB = 0;
        int newR = 0;
        int newG = 0;
        int newB = 0;
        int[] pixels = new int[width * height];
        bmp.getPixels(pixels, 0, width, 0, 0, width, height);
        for (int i = 0; i < height; i++)
        {
            for (int k = 0; k < width; k++)
            {
                pixColor = pixels[width * i + k];
                A = Color.alpha(pixColor);
                pixR = Color.red(pixColor);
                pixG = Color.green(pixColor);
                pixB = Color.blue(pixColor);
                newR = 255 - pixR;
                newG = 255 - pixG;
                newB = 255 - pixB;
                int newColor = Color.argb(A, newR, newG , newB);
                pixels[width * i + k] = newColor;
            }
        }

        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        //oldArray = bitmapToByte(bitmap);
        return bitmap;
    }

    public Bitmap doInvert(Bitmap bmp) {
        int width = bmp.getWidth();
        int height = bmp.getHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height, this.configuration);
        int pixColor = 0;
        int A;
        int pixR = 0;
        int pixG = 0;
        int pixB = 0;
        int newR = 0;
        int newG = 0;
        int newB = 0;
        int[] pixels = new int[width * height];
        bmp.getPixels(pixels, 0, width, 0, 0, width, height);
        for (int i = 0; i < height; i++)
        {
            for (int k = 0; k < width; k++)
            {
                pixColor = pixels[width * i + k];
                A = Color.alpha(pixColor);
                pixR = Color.red(pixColor);
                pixG = Color.green(pixColor);
                pixB = Color.blue(pixColor);
                newR = 255 - pixR;
                newG = 255 - pixG;
                newB = 255 - pixB;
                int newColor = Color.argb(A, newR, newG , newB);
                pixels[width * i + k] = newColor;
            }
        }

        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        //oldArray = bitmapToByte(bitmap);
        return bitmap;
    }

    public Bitmap addEffect(Bitmap bmp, int depth, double red, double green, double blue){
        int width = bmp.getWidth();
        int height = bmp.getHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height,this.configuration);
        final double grayScale_Red = 0.3;
        final double grayScale_Green = 0.59;
        final double grayScale_Blue = 0.11;
        int pixColor = 0;
        int A;
        int pixR = 0;
        int pixG = 0;
        int pixB = 0;
        int newR = 0;
        int newG = 0;
        int newB = 0;
        int[] pixels = new int[width * height];
        bmp.getPixels(pixels, 0, width, 0, 0, width, height);
        for (int i = 0; i < height; i++)
        {
            for (int k = 0; k < width; k++)
            {
                pixColor = pixels[width * i + k];
                A = Color.alpha(pixColor);
                pixR = Color.red(pixColor);
                pixG = Color.green(pixColor);
                pixB = Color.blue(pixColor);
                newR = (int)(grayScale_Red * pixR + grayScale_Green * pixG + grayScale_Blue * pixB);
                newG = (int)(grayScale_Red * pixR + grayScale_Green * pixG + grayScale_Blue * pixB);
                newB = (int)(grayScale_Red * pixR + grayScale_Green * pixG + grayScale_Blue * pixB);

                newR += (depth * red);
                if(newR > 255)
                {
                    newR = 255;
                }
                newG += (depth * green);
                if(newG > 255)
                {
                    newG = 255;
                }
                newB += (depth * blue);
                if(newB > 255)
                {
                    newB = 255;
                }

                int newColor = Color.argb(A, newR, newG , newB);
                pixels[width * i + k] = newColor;
            }
        }

        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        //oldArray = bitmapToByte(bitmap);
        return bitmap;
    }

    public Bitmap decreaseColorDepth(Bitmap src, int bitOffset) {
        int width = src.getWidth();
        int height = src.getHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height,this.configuration);
        int pixColor = 0;
        int A;
        int pixR = 0;
        int pixG = 0;
        int pixB = 0;
        int newR = 0;
        int newG = 0;
        int newB = 0;
        int[] pixels = new int[width * height];
        src.getPixels(pixels, 0, width, 0, 0, width, height);
        for (int i = 0; i < height; i++)
        {
            for (int k = 0; k < width; k++)
            {
                pixColor = pixels[width * i + k];
                A = Color.alpha(pixColor);
                pixR = Color.red(pixColor);
                pixG = Color.green(pixColor);
                pixB = Color.blue(pixColor);
                newR = ((pixR + (bitOffset / 2)) - ((pixR + (bitOffset / 2)) % bitOffset) - 1);
                if(newR < 0) { newR = 0; }
                newG = ((pixG + (bitOffset / 2)) - ((pixG + (bitOffset / 2)) % bitOffset) - 1);
                if(newG < 0) { newG = 0; }
                newB = ((pixB + (bitOffset / 2)) - ((pixB + (bitOffset / 2)) % bitOffset) - 1);
                if(newB < 0) { newB = 0; }
                int newColor = Color.argb(A, newR, newG , newB);
                pixels[width * i + k] = newColor;
            }
        }

        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        //oldArray = bitmapToByte(bitmap);
        return bitmap;
    }

    public Bitmap applyGaussianBlur1(Bitmap src) {

        float[] blur =
                { 0.111f, 0.111f, 0.111f ,
                        0.111f, 0.111f, 0.111f ,
                        0.111f, 0.111f, 0.111f   };
        Bitmap bmp = this.convolve3x3ARGB(src,blur);

        return bmp;
    }

    public Bitmap yellowNoise(Bitmap src) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        options.inPreferredConfig = this.configuration;

        // get the image (png file) filter from resources using the options
        Bitmap filter = BitmapFactory.decodeResource(MainActivity.act.getResources(), R.drawable.yellow,options);

        // create a scaled copy of the filter
        filter = Bitmap.createScaledBitmap(filter, src.getWidth(),src.getHeight(), true);

        Paint paint = new Paint();
         paint.setAlpha(120);
        //paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY));

        //paint.setAlpha(230); - if a discrete value is set, then the image beneath
        Bitmap mutableBitmap = Bitmap.createBitmap(src);
        mutableBitmap = mutableBitmap.copy(this.configuration, true);
        mutableBitmap = this.getGreyScale(mutableBitmap);
        // create a canvas with the original image as the underlying image
        Canvas canvas = new Canvas(mutableBitmap);
        // now, draw the filter on top of the bitmap
        canvas.drawBitmap(filter, 0,0, paint);

        // recycle the used bitmap
        filter.recycle();

        return mutableBitmap;
    }

    public Bitmap yellowishNoise(Bitmap src) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        options.inPreferredConfig = this.configuration;

        // get the image (png file) filter from resources using the options
        Bitmap filter = BitmapFactory.decodeResource(MainActivity.act.getResources(), R.drawable.yellowish,options);

        // create a scaled copy of the filter
        filter = Bitmap.createScaledBitmap(filter, src.getWidth(),src.getHeight(), true);

        Paint paint = new Paint();
        // paint.setAlpha(120);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY));

        //paint.setAlpha(230); - if a discrete value is set, then the image beneath
        Bitmap mutableBitmap = Bitmap.createBitmap(src);
        mutableBitmap = mutableBitmap.copy(this.configuration, true);
        mutableBitmap = this.getGreyScale(mutableBitmap);
        // create a canvas with the original image as the underlying image
        Canvas canvas = new Canvas(mutableBitmap);
        // now, draw the filter on top of the bitmap
        canvas.drawBitmap(filter, 0,0, paint);

        // recycle the used bitmap
        filter.recycle();

        return mutableBitmap;
    }

    public Bitmap blur(Bitmap original, float radius) {
        Bitmap bitmap = Bitmap.createBitmap(
                original.getWidth(), original.getHeight(),
                Bitmap.Config.ARGB_8888);

        RenderScript rs = RenderScript.create(MainActivity.ctx);

        Allocation allocIn = Allocation.createFromBitmap(rs, original);
        Allocation allocOut = Allocation.createFromBitmap(rs, bitmap);

        ScriptIntrinsicBlur blur = ScriptIntrinsicBlur.create(
                rs, Element.U8_4(rs));
        blur.setInput(allocIn);
        blur.setRadius(radius);
        blur.forEach(allocOut);

        allocOut.copyTo(bitmap);
        rs.destroy();
        return bitmap;
    }

    public Bitmap createBitmap_convolve(Bitmap src) {

        float[] matrix_sharpen =
                { 0, 1, 0,
                  1, -4, 1,
                  0, 1, 0};
        Bitmap result = Bitmap.createBitmap(src.getWidth(),
                src.getHeight(), Bitmap.Config.ARGB_8888);

        RenderScript renderScript = RenderScript.create(MainActivity.ctx);

        Allocation input = Allocation.createFromBitmap(renderScript, src);
        Allocation output = Allocation.createFromBitmap(renderScript, result);

        ScriptIntrinsicConvolve3x3 convolution = ScriptIntrinsicConvolve3x3
                .create(renderScript, Element.U8_4(renderScript));
        convolution.setInput(input);
        convolution.setCoefficients(matrix_sharpen);
        convolution.forEach(output);

        output.copyTo(result);
        renderScript.destroy();

        Bitmap bitmap ;
        bitmap = result.copy(Bitmap.Config.RGB_565,true);
        bitmap = doInvert(bitmap);


        return bitmap;
    }

    public Bitmap sobelFilter(Bitmap src){
        float sobel_x[] = {-1,0,1,
                         -2,0,2,
                         -1,0,1};
        float sobel_y[] = {-1,-2,-1,
                         0,0,0,
                         1,2,1};
        int Ax,pixRx,pixGx,pixBx,Ay,pixRy,pixGy,pixBy,newR,newG,newB;

        Bitmap resultx = this.convolve3x3(src,sobel_x);
        Bitmap resulty = this.convolve3x3(src,sobel_y);
        int[] pixelsFinal = new int[resultx.getWidth() * resultx.getHeight()];

        int[] pixelsx = new int[resultx.getWidth() * resultx.getHeight()];
        resultx.getPixels(pixelsx, 0, resultx.getWidth(), 0, 0, resultx.getWidth(), resultx.getHeight());

        int[] pixelsy = new int[resulty.getWidth() * resulty.getHeight()];
        resulty.getPixels(pixelsy, 0, resulty.getWidth(), 0, 0, resulty.getWidth(), resulty.getHeight());

        for(int i=0; i<resultx.getWidth() * resultx.getHeight();i++){
            Ax = Color.alpha(pixelsx[i]);
            pixRx = Color.red(pixelsx[i]);
            pixGx = Color.green(pixelsx[i]);
            pixBx = Color.blue(pixelsx[i]);
            Ay = Color.alpha(pixelsy[i]);
            pixRy = Color.red(pixelsy[i]);
            pixGy = Color.green(pixelsy[i]);
            pixBy = Color.blue(pixelsy[i]);
            newR = (int)Math.sqrt((pixRx * pixRx) + (pixRy * pixRy));
            newG = (int)Math.sqrt((pixGx * pixGx) + (pixGy * pixGy));
            newB = (int)Math.sqrt((pixBx * pixBx) + (pixBy * pixBy));

            if(newR>255)
                newR=255;
            else if(newR<0)
                newR=0;
            if(newG>255)
                newG=255;
            else if(newG<0)
                newG=0;
            if(newB>255)
                newB=255;
            else if(newB<0)
                newB=0;
            int newColor = Color.argb(Ax, newR, newG , newB);
            pixelsFinal[i] = newColor;
        }

        Bitmap bitmap = Bitmap.createBitmap(resultx.getWidth(), resultx.getHeight(), Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixelsFinal, 0, resultx.getWidth(), 0, 0, resultx.getWidth(), resultx.getHeight());

        bitmap = this.doInvertARGB(bitmap);
        bitmap = this.getGreyScaleARGB(bitmap);
        bitmap = this.applyGaussianBlur1(bitmap);


        return bitmap;
    }

    public Bitmap convolve3x3(Bitmap src, float [] matrix){
        Bitmap result = Bitmap.createBitmap(src.getWidth(),
                src.getHeight(), Bitmap.Config.ARGB_8888);

        RenderScript renderScript = RenderScript.create(MainActivity.ctx);

        Allocation input = Allocation.createFromBitmap(renderScript, src);
        Allocation output = Allocation.createFromBitmap(renderScript, result);

        ScriptIntrinsicConvolve3x3 convolution = ScriptIntrinsicConvolve3x3
                .create(renderScript, Element.U8_4(renderScript));
        convolution.setInput(input);
        convolution.setCoefficients(matrix);
        convolution.forEach(output);

        output.copyTo(result);
        renderScript.destroy();

        Bitmap bitmap ;
        bitmap = result.copy(Bitmap.Config.RGB_565,true);

        return bitmap;
    }

    public Bitmap convolve3x3ARGB(Bitmap src, float [] matrix){
        Bitmap result = Bitmap.createBitmap(src.getWidth(),
                src.getHeight(), Bitmap.Config.ARGB_8888);

        RenderScript renderScript = RenderScript.create(MainActivity.ctx);

        Allocation input = Allocation.createFromBitmap(renderScript, src);
        Allocation output = Allocation.createFromBitmap(renderScript, result);

        ScriptIntrinsicConvolve3x3 convolution = ScriptIntrinsicConvolve3x3
                .create(renderScript, Element.U8_4(renderScript));
        convolution.setInput(input);
        convolution.setCoefficients(matrix);
        convolution.forEach(output);

        output.copyTo(result);
        renderScript.destroy();

        Bitmap bitmap ;
        bitmap = result.copy(Bitmap.Config.RGB_565,true);
        result.recycle();

        return bitmap;
    }

    public Bitmap addWhiteNoise(Bitmap src){
        Bitmap bmp = BitmapFactory.decodeResource(MainActivity.act.getResources(), R.drawable.white_noise);

        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(src.getWidth(), src.getHeight());

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(bmp, 0, 0, src.getWidth(),  src.getHeight(), matrix, false);

        int A;
        int pixR = 0;
        int pixG = 0;
        int pixB = 0;
        int newR = 0;
        int newG = 0;
        int newB = 0;

        int[] pixelsNoise = new int[bmp.getWidth() * bmp.getHeight()];
        bmp.getPixels(pixelsNoise, 0, bmp.getWidth(), 0, 0, bmp.getWidth(), bmp.getHeight());

        int width = src.getWidth();
        int height = src.getHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height,this.configuration);
        int[] pixels = new int[width * height];
        src.getPixels(pixels, 0, width, 0, 0, width, height);
        int min = pixelsNoise[0];
        int max = pixelsNoise[0];
        for (int i = 0; i < width*height; i++)
        {
            if(pixelsNoise[i]>max)
                max = pixelsNoise[i];
            if(pixelsNoise[i]<min)
                min = pixelsNoise[i];
        }
        Log.d("123","max= "+max);
        Log.d("123","min= "+min);

        for (int i = 0; i < width*height; i++)
        {
            if(pixelsNoise[i] == -1){
                pixels[i] = pixelsNoise[i];
            }else {
                pixR = Color.red(pixels[i]);
                pixG = Color.green(pixels[i]);
                pixB = Color.blue(pixels[i]);
                newR = (int) (0.393 * pixR + 0.769 * pixG + 0.189 * pixB);
                newG = (int) (0.349 * pixR + 0.686 * pixG + 0.168 * pixB);
                newB = (int) (0.272 * pixR + 0.534 * pixG + 0.131 * pixB);
                int newColor = Color.argb(255, newR > 255 ? 255 : newR, newG > 255 ? 255 : newG, newB > 255 ? 255 : newB);
                pixels[i] = newColor;
            }
        }

        bitmap.setPixels(pixelsNoise, 0, width, 0, 0, width, height);
        //oldArray = bitmapToByte(bitmap);
        return resizedBitmap;
    }



    public Bitmap addWhiteNoise1(Bitmap src){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        options.inPreferredConfig = this.configuration;

        // get the image (png file) filter from resources using the options
        Bitmap filter = BitmapFactory.decodeResource(MainActivity.act.getResources(), R.drawable.white_noise,options);

        // create a scaled copy of the filter
        // create a scaled copy of the filter
        filter = Bitmap.createScaledBitmap(filter, src.getWidth(),src.getHeight(), true);

        Paint paint = new Paint();
         paint.setAlpha(105);
        //paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY));

        //paint.setAlpha(230); - if a discrete value is set, then the image beneath
        Bitmap mutableBitmap = Bitmap.createBitmap(src);
        mutableBitmap = mutableBitmap.copy(this.configuration, true);
        mutableBitmap = this.getGreyScale(mutableBitmap);
        // create a canvas with the original image as the underlying image
        Canvas canvas = new Canvas(mutableBitmap);
        // now, draw the filter on top of the bitmap
        canvas.drawBitmap(filter, 0,0, paint);

        // recycle the used bitmap
        filter.recycle();

        return mutableBitmap;
    }

    public Bitmap addBorder(Bitmap src){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        options.inPreferredConfig = this.configuration;

        // get the image (png file) filter from resources using the options
        Bitmap filter = BitmapFactory.decodeResource(MainActivity.act.getResources(), R.drawable.border,options);

        // create a scaled copy of the filter
        filter = Bitmap.createScaledBitmap(filter, src.getWidth(),src.getHeight(), true);

        Paint paint = new Paint();
        paint.setAlpha(120);
       // paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY));

        //paint.setAlpha(230); - if a discrete value is set, then the image beneath
        Bitmap mutableBitmap = Bitmap.createBitmap(src);
        mutableBitmap = mutableBitmap.copy(this.configuration, true);
        mutableBitmap = this.getGreyScale(mutableBitmap);
        // create a canvas with the original image as the underlying image
        Canvas canvas = new Canvas(mutableBitmap);
        // now, draw the filter on top of the bitmap
        canvas.drawBitmap(filter, 0,0, paint);

        // recycle the used bitmap
        filter.recycle();

        return mutableBitmap;
    }

    public Bitmap addBorder2(Bitmap src){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        options.inPreferredConfig = this.configuration;

        // get the image (png file) filter from resources using the options
        Bitmap filter = BitmapFactory.decodeResource(MainActivity.act.getResources(), R.drawable.border2,options);

        // create a scaled copy of the filter
        filter = Bitmap.createScaledBitmap(filter, src.getWidth(),src.getHeight(), true);

        Paint paint = new Paint();
       // paint.setAlpha(120);
         paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY));

        //paint.setAlpha(230); - if a discrete value is set, then the image beneath
        Bitmap mutableBitmap = Bitmap.createBitmap(src);
        mutableBitmap = mutableBitmap.copy(this.configuration, true);
        mutableBitmap = this.getGreyScale(mutableBitmap);
        // create a canvas with the original image as the underlying image
        Canvas canvas = new Canvas(mutableBitmap);
        // now, draw the filter on top of the bitmap
        canvas.drawBitmap(filter, 0,0, paint);

        // recycle the used bitmap
        filter.recycle();

        return mutableBitmap;
    }

    public Bitmap addBlackNoise(Bitmap src){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        options.inPreferredConfig = this.configuration;

        // get the image (png file) filter from resources using the options
        Bitmap filter = BitmapFactory.decodeResource(MainActivity.act.getResources(), R.drawable.black_noise,options);

        // create a scaled copy of the filter
        filter = Bitmap.createScaledBitmap(filter, src.getWidth(),src.getHeight(), true);

        Paint paint = new Paint();
        // paint.setAlpha(120);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY));

        //paint.setAlpha(230); - if a discrete value is set, then the image beneath
        Bitmap mutableBitmap = Bitmap.createBitmap(src);
        mutableBitmap = mutableBitmap.copy(this.configuration, true);
        mutableBitmap = this.getGreyScale(mutableBitmap);
        // create a canvas with the original image as the underlying image
        Canvas canvas = new Canvas(mutableBitmap);
        // now, draw the filter on top of the bitmap
        canvas.drawBitmap(filter, 0,0, paint);

        // recycle the used bitmap
        filter.recycle();

        return mutableBitmap;
    }

    public Bitmap papirus(Bitmap src){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        options.inPreferredConfig = this.configuration;

        // get the image (png file) filter from resources using the options
        Bitmap filter = BitmapFactory.decodeResource(MainActivity.act.getResources(), R.drawable.papirus,options);

        // create a scaled copy of the filter
        filter = Bitmap.createScaledBitmap(filter, src.getWidth(),src.getHeight(), true);

        Paint paint = new Paint();
        // paint.setAlpha(120);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY));

        //paint.setAlpha(230); - if a discrete value is set, then the image beneath
        Bitmap mutableBitmap = Bitmap.createBitmap(src);
        mutableBitmap = mutableBitmap.copy(this.configuration, true);
        mutableBitmap = this.getGreyScale(mutableBitmap);
        // create a canvas with the original image as the underlying image
        Canvas canvas = new Canvas(mutableBitmap);
        // now, draw the filter on top of the bitmap
        canvas.drawBitmap(filter, 0,0, paint);

        // recycle the used bitmap
        filter.recycle();

        return mutableBitmap;
    }

    public Bitmap desert(Bitmap src){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        options.inPreferredConfig = this.configuration;

        // get the image (png file) filter from resources using the options
        Bitmap filter = BitmapFactory.decodeResource(MainActivity.act.getResources(), R.drawable.desert,options);

        // create a scaled copy of the filter
        filter = Bitmap.createScaledBitmap(filter, src.getWidth(),src.getHeight(), true);

        Paint paint = new Paint();
        // paint.setAlpha(120);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY));

        //paint.setAlpha(230); - if a discrete value is set, then the image beneath
        Bitmap mutableBitmap = Bitmap.createBitmap(src);
        mutableBitmap = mutableBitmap.copy(this.configuration, true);
        mutableBitmap = this.getGreyScale(mutableBitmap);
        // create a canvas with the original image as the underlying image
        Canvas canvas = new Canvas(mutableBitmap);
        // now, draw the filter on top of the bitmap
        canvas.drawBitmap(filter, 0,0, paint);

        // recycle the used bitmap
        filter.recycle();

        return mutableBitmap;
    }

    public Bitmap abstract1(Bitmap src){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        options.inPreferredConfig = this.configuration;

        // get the image (png file) filter from resources using the options
        Bitmap filter = BitmapFactory.decodeResource(MainActivity.act.getResources(), R.drawable.abstract1,options);

        // create a scaled copy of the filter
        filter = Bitmap.createScaledBitmap(filter, src.getWidth(),src.getHeight(), true);

        Paint paint = new Paint();
         paint.setAlpha(50);
        //paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY));

        //paint.setAlpha(230); - if a discrete value is set, then the image beneath
        Bitmap mutableBitmap = Bitmap.createBitmap(src);
        mutableBitmap = mutableBitmap.copy(this.configuration, true);
        mutableBitmap = this.getGreyScale(mutableBitmap);
        // create a canvas with the original image as the underlying image
        Canvas canvas = new Canvas(mutableBitmap);
        // now, draw the filter on top of the bitmap
        canvas.drawBitmap(filter, 0,0, paint);

        // recycle the used bitmap
        filter.recycle();

        return mutableBitmap;
    }

    public Bitmap abstract2(Bitmap src){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        options.inPreferredConfig = this.configuration;

        // get the image (png file) filter from resources using the options
        Bitmap filter = BitmapFactory.decodeResource(MainActivity.act.getResources(), R.drawable.abstract2,options);

        // create a scaled copy of the filter
        filter = Bitmap.createScaledBitmap(filter, src.getWidth(),src.getHeight(), true);

        Paint paint = new Paint();
        // paint.setAlpha(120);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY));

        //paint.setAlpha(230); - if a discrete value is set, then the image beneath
        Bitmap mutableBitmap = Bitmap.createBitmap(src);
        mutableBitmap = mutableBitmap.copy(this.configuration, true);
        mutableBitmap = this.getGreyScale(mutableBitmap);
        // create a canvas with the original image as the underlying image
        Canvas canvas = new Canvas(mutableBitmap);
        // now, draw the filter on top of the bitmap
        canvas.drawBitmap(filter, 0,0, paint);

        // recycle the used bitmap
        filter.recycle();

        return mutableBitmap;
    }

    public Bitmap abstract3(Bitmap src){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        options.inPreferredConfig = this.configuration;

        // get the image (png file) filter from resources using the options
        Bitmap filter = BitmapFactory.decodeResource(MainActivity.act.getResources(), R.drawable.abstract3,options);

        // create a scaled copy of the filter
        filter = Bitmap.createScaledBitmap(filter, src.getWidth(),src.getHeight(), true);

        Paint paint = new Paint();
        // paint.setAlpha(120);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY));

        //paint.setAlpha(230); - if a discrete value is set, then the image beneath
        Bitmap mutableBitmap = Bitmap.createBitmap(src);
        mutableBitmap = mutableBitmap.copy(this.configuration, true);
        mutableBitmap = this.getGreyScale(mutableBitmap);
        // create a canvas with the original image as the underlying image
        Canvas canvas = new Canvas(mutableBitmap);
        // now, draw the filter on top of the bitmap
        canvas.drawBitmap(filter, 0,0, paint);

        // recycle the used bitmap
        filter.recycle();

        return mutableBitmap;
    }

    public Bitmap sharpenFilter(Bitmap src){
        float[] matrix_sharpen =
                { -1, -1, -1 ,
                  -1, 9, -1 ,
                  -1, -1, -1 };
        float[] blur =
                { 0.111f, 0.111f, 0.111f ,
                  0.111f, 0.111f, 0.111f ,
                  0.111f, 0.111f, 0.111f   };
        Bitmap bmp = this.convolve3x3ARGB(src,matrix_sharpen);

        return bmp;
    }

    public Bitmap mean(Bitmap src){
        float[] matrix_sharpen =
                { 1/9, 1/9, 1/9 ,
                        1/9, 1/9, 1/9 ,
                        1/9, 1/9, 1/9  };
        Bitmap result = Bitmap.createBitmap(src.getWidth(),
                src.getHeight(), Bitmap.Config.ARGB_8888);

        RenderScript renderScript = RenderScript.create(MainActivity.ctx);

        Allocation input = Allocation.createFromBitmap(renderScript, src);
        Allocation output = Allocation.createFromBitmap(renderScript, result);

        ScriptIntrinsicConvolve3x3 convolution = ScriptIntrinsicConvolve3x3
                .create(renderScript, Element.U8_4(renderScript));
        convolution.setInput(input);
        convolution.setCoefficients(matrix_sharpen);
        convolution.forEach(output);

        output.copyTo(result);
        renderScript.destroy();

        Bitmap bitmap ;
        bitmap = result.copy(Bitmap.Config.RGB_565,true);


        return bitmap;
    }

    public Bitmap rescaleFilter(Bitmap bmp){
        int width = bmp.getWidth();
        int height = bmp.getHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height, this.configuration);
        int pixColor = 0;
        int A;
        int pixR = 0;
        int pixG = 0;
        int pixB = 0;
        int newR = 0;
        int newG = 0;
        int newB = 0;
        int[] pixels = new int[width * height];
        bmp.getPixels(pixels, 0, width, 0, 0, width, height);
        for (int i = 0; i < height; i++)
        {
            for (int k = 0; k < width; k++)
            {
                pixColor = pixels[width * i + k];
                A = Color.alpha(pixColor);
                pixR = Color.red(pixColor);
                pixG = Color.green(pixColor);
                pixB = Color.blue(pixColor);
                newR = newG = newB = (int) (1.25 * pixR + 1.25 * pixG + 1.25 * pixB);
                int newColor = Color.argb(A, newR > 255 ? 255 : newR, newG > 255 ? 255 : newG, newB > 255 ? 255 : newB);
                pixels[width * i + k] = newColor;
            }
        }

        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        //oldArray = bitmapToByte(bitmap);
        return bitmap;
    }

    /**
     *
     * @param bmp input bitmap
     * @param contrast 0..10 1 is default
     * @param brightness -255..255 0 is default
     * @return new bitmap
     */
    public Bitmap changeBitmapContrastBrightness(Bitmap bmp, float contrast, float brightness)
    {
        ColorMatrix cm = new ColorMatrix(new float[]
                {
                        contrast, 0, 0, 0, brightness,
                        0, contrast, 0, 0, brightness,
                        0, 0, contrast, 0, brightness,
                        0, 0, 0, 1, 0
                });

        Bitmap ret = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), bmp.getConfig());

        Canvas canvas = new Canvas(ret);

        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(cm));
        canvas.drawBitmap(bmp, 0, 0, paint);

        return ret;
    }

    public static byte[] bitmapToByte(Bitmap bitmap){
        byte[] data;
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        data = stream.toByteArray();
        return data;
    }
}
