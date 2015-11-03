package com.example.home.edge;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

/**
 * Created by home on 3/15/2015.
 */
public class ConvolutionMatrix
{
    public static final int SIZE = 3;

    public  int[][] Matrix;
    public double Factor = 1;
    public double Offset = 0;

    public ConvolutionMatrix(int size) {
        Matrix = new int[size][size];
    }

    public void setAll(int value) {
        for (int x = 0; x < SIZE; ++x) {
            for (int y = 0; y < SIZE; ++y) {
                Matrix[x][y] = value;
            }
        }
    }

    public void applyConfig(int[][] config) {
        for(int x = 0; x < SIZE; ++x) {
            for(int y = 0; y < SIZE; ++y) {
                Matrix[x][y] = config[x][y];
            }
        }
    }

    public static Bitmap computeConvolution3x3(Bitmap src, ConvolutionMatrix matrix) {
        int width = src.getWidth();
        int height = src.getHeight();
        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

        int A, R, G, B;
        int count= 0;
        int sumR, sumG, sumB;
        int[][] pixels = new int[SIZE][SIZE];
        for(int y = 0; y < height - 2; ++y) {
            for(int x = 0; x < width - 2; ++x) {
                count += Color.red(src.getPixel(x , y ))+Color.blue(src.getPixel(x, y))+Color.green(src.getPixel(x, y));}}

        for(int y = 0; y < height - 2; ++y) {
            for(int x = 0; x < width - 2; ++x) {
                // get pixel matrix
                for(int i = 0; i < SIZE; ++i) {
                    for(int j = 0; j < SIZE; ++j) {
                        pixels[i][j] = src.getPixel(x + i, y + j);
                    }
                }

                // get alpha of center pixel
                A = Color.alpha(pixels[1][1]);


                // init color sum
                sumR = sumG = sumB = 0;

                // get sum of RGB on matrix
                for(int i = 0; i < SIZE; ++i) {
                    for(int j = 0; j < SIZE; ++j) {
                        sumR += (Color.red(pixels[i][j]) * matrix.Matrix[i][j]);
                        sumG += (Color.green(pixels[i][j]) * matrix.Matrix[i][j]);
                        sumB += (Color.blue(pixels[i][j]) * matrix.Matrix[i][j]);
                    }
                }

                // get final Red
                R = (int)(sumR / matrix.Factor + matrix.Offset);
                if(R < 0) { R = 0; }
                else if(R > 255) { R = 255; }

                // get final Green
                G = (int)(sumG / matrix.Factor + matrix.Offset);
                if(G < 0) { G = 0; }
                else if(G > 255) { G = 255; }

                // get final Blue
                B = (int)(sumB / matrix.Factor + matrix.Offset);
                if(B < 0) { B = 0; }
                else if(B > 255) { B = 255; }

                if(R==0 && G==0 && B==0)
                count ++;
                // apply new pixel
                result.setPixel(x + 1, y + 1, Color.argb(A, R, G, B));
            }
        }
        Log.d("123","count0= "+count);
        // final image
        return result;
    }

    public Bitmap convolve(Bitmap bmp, ConvolutionMatrix matrix) {
        int width = bmp.getWidth();
        int height = bmp.getHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        int pixColor;
        int A;
        int pixR;
        int pixG;
        int pixB;
        int newR;
        int newG;
        int newB;

        int[] pixels = new int[width * height];
        int[] finalPixels = new int[width * height];
        bmp.getPixels(pixels, 0, width, 0, 0, width, height);
        for (int i = 1; i < height-2; ++i)
        {
            for (int k = 1; k < width-2; ++k)
            {
                pixColor = pixels[width * i + k];
                A = Color.alpha(pixColor);
                pixR = Color.red(pixels[width * i + k])*matrix.Matrix[1][1]+Color.red(pixels[width * (i-1) + (k-1)])*matrix.Matrix[0][0]+Color.red(pixels[width * (i-1) + (k)])*matrix.Matrix[0][1]+
                       Color.red(pixels[width * (i-1) + k+1])*matrix.Matrix[0][2]+Color.red(pixels[width * (i) + (k-1)])*matrix.Matrix[1][0]+Color.red(pixels[width * (i) + (k+1)])*matrix.Matrix[1][2]+
                       Color.red(pixels[width * (i+1) + k-1])*matrix.Matrix[2][0]+Color.red(pixels[width * (i+1) + (k)])*matrix.Matrix[2][1]+Color.red(pixels[width * (i+1) + (k+1)])*matrix.Matrix[2][2];
                pixG = Color.green(pixels[width * i + k])*matrix.Matrix[1][1]+Color.green(pixels[width * (i - 1) + (k - 1)])*matrix.Matrix[0][0]+Color.green(pixels[width * (i - 1) + (k)])*matrix.Matrix[0][1]+
                       Color.green(pixels[width * (i - 1) + k + 1])*matrix.Matrix[0][2]+Color.green(pixels[width * (i) + (k - 1)])*matrix.Matrix[1][0]+Color.green(pixels[width * (i) + (k + 1)])*matrix.Matrix[1][2]+
                       Color.green(pixels[width * (i + 1) + k - 1])*matrix.Matrix[2][0]+Color.green(pixels[width * (i + 1) + (k)])*matrix.Matrix[2][1]+Color.green(pixels[width * (i + 1) + (k + 1)])*matrix.Matrix[2][2];
                pixB = Color.blue(pixels[width * i + k])*matrix.Matrix[1][1]+Color.blue(pixels[width * (i - 1) + (k - 1)])*matrix.Matrix[0][0]+Color.blue(pixels[width * (i - 1) + (k)])*matrix.Matrix[0][1]+
                       Color.blue(pixels[width * (i - 1) + k + 1])*matrix.Matrix[0][2]+Color.blue(pixels[width * (i) + (k - 1)])*matrix.Matrix[1][0]+Color.blue(pixels[width * (i) + (k + 1)])*matrix.Matrix[1][2]+
                       Color.blue(pixels[width * (i + 1) + k - 1])*matrix.Matrix[2][0]+Color.blue(pixels[width * (i + 1) + (k)])*matrix.Matrix[2][1]+Color.blue(pixels[width * (i + 1) + (k + 1)])*matrix.Matrix[2][2];

                newR = (int)(pixR / this.Factor + this.Offset);
                if(newR < 0) { newR = 0; }
                else if(newR > 255) { newR = 255; }

                // get final Green
                newG = (int)(pixG / this.Factor + this.Offset);
                if(newG < 0) { newG = 0; }
                else if(newG > 255) { newG = 255; }

                // get final Blue
                newB = (int)(pixB / this.Factor + this.Offset);
                if(newB < 0) { newB = 0; }
                else if(newB > 255) { newB = 255; }


                int newColor = Color.argb(A, newR, newG , newB);
                finalPixels[width * i + k] = newColor;

            }
        }

        bitmap.setPixels(finalPixels, 0, width, 0, 0, width, height);
        //oldArray = bitmapToByte(bitmap);
        return bitmap;
    }
}
