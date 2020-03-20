package com.example.mobilev2yolov3;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private YOLOV3 yolov3 = new YOLOV3(); //java接口实例化　下面直接利用java函数调用NDK c++函数

    private Bitmap yourSelectedImage = null;


    private static final String TAG = MainActivity.class.getName();
    private ImageView show_image;
    private TextView result_text;
    private boolean load_result = false;
    private int[] ddims = {1, 3, 352, 352}; //这里的维度的值要和train model的input 一一对应
    private List<String> resultLabel = new ArrayList<>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        try
        {
            initMobileNetV2YOLOV3();//初始化模型
            Log.e("MainActivity", "initMobileNetSSD ok");
        } catch (IOException e) {
            Log.e("MainActivity", "initMobileNetSSD error");
        }


        readCacheLabelFromLocalFile();//初始化读取words.txt
        init_view();//检测+view画图



    }


    private void initMobileNetV2YOLOV3() throws IOException {
        byte[] param = null;
        byte[] bin = null;
        {
            //用io流读取二进制文件，最后存入到byte[]数组中
            InputStream assetsInputStream = getAssets().open("mobilenetv2_yolov3.param.bin");// param：  网络结构文件
            int available = assetsInputStream.available();
            param = new byte[available];
            int byteCode = assetsInputStream.read(param);
            assetsInputStream.close();
        }
        {
            //用io流读取二进制文件，最后存入到byte上，转换为int型
            InputStream assetsInputStream = getAssets().open("mobilenetv2_yolov3.bin");//bin：   model文件
            int available = assetsInputStream.available();
            bin = new byte[available];
            int byteCode = assetsInputStream.read(bin);
            assetsInputStream.close();
        }

        load_result = yolov3.Init(param, bin);// 再将文件传入java的NDK接口(c++ 代码中的init接口 )
        Log.d("load model", "MobileNetv2YOLOV3_load_model_result:" + load_result);
    }




    // initialize view
    private void init_view() {
        show_image = (ImageView) findViewById(R.id.show_image);
        result_text = (TextView) findViewById(R.id.result_text);
        result_text.setMovementMethod(ScrollingMovementMethod.getInstance());
        Button use_photo = (Button) findViewById(R.id.use_photo);
        use_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                yourSelectedImage = BitmapFactory.decodeResource(getResources(), R.drawable.dog);
                show_image.setImageBitmap(yourSelectedImage);
            }
        });



        Button detect_photo = (Button) findViewById(R.id.detect_photo);
        detect_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                if (yourSelectedImage == null)
                    return;

                predict_image(yourSelectedImage);
            }

        });
    }





    // load label's name
    private void readCacheLabelFromLocalFile() {
        try {
            AssetManager assetManager = getApplicationContext().getAssets();
            BufferedReader reader = new BufferedReader(new InputStreamReader(assetManager.open("words.txt")));//这里是label的文件
            String readLine = null;
            while ((readLine = reader.readLine()) != null) {
                resultLabel.add(readLine);

            }
            reader.close();
        } catch (Exception e) {
            Log.e("labelCache", "error " + e);
        }
    }



    //  predict image
    private void predict_image(Bitmap bmp) {
        // picture to float array
        Bitmap rgba = bmp.copy(Bitmap.Config.ARGB_8888, true);
        // resize
        Bitmap input_bmp = Bitmap.createScaledBitmap(rgba, ddims[2], ddims[3], false);


        try {
            // Data format conversion takes too long
            // Log.d("inputData", Arrays.toString(inputData));
            long start = System.currentTimeMillis();
            // get predict result
            float[] result = yolov3.Detect(input_bmp);
            // time end
            long end = System.currentTimeMillis();
            Log.d(TAG, "origin predict result:" + Arrays.toString(result));
            long time = end - start;
            Log.d("result length", "length of result: " + String.valueOf(result.length));
            // show predict result and time
            // float[] r = get_max_result(result);


            DecimalFormat decimalFormat = new DecimalFormat("0.00");
            String show_text="";
            int numm=0;
            while(numm<result.length){
                show_text +="name： ";
                show_text +=resultLabel.get((int) result[numm]);
                show_text +="  probability：";
                show_text +=result[numm+1]+"\n";
                show_text +="box：";
                show_text +=decimalFormat.format(result[numm+2])+"  "+decimalFormat.format(result[numm+3])+"  "+decimalFormat.format(result[numm+4])+"  "+decimalFormat.format(result[numm+5])+"\n";
                numm+=6;
            }
            show_text+="time：" + time + "ms";
            result_text.setText(show_text);

            // 画布配置
            Canvas canvas = new Canvas(rgba);
            //图像上画矩形
            Paint paint = new Paint();
            paint.setColor(Color.RED);
            paint.setStyle(Paint.Style.STROKE);//不填充
            paint.setStrokeWidth(5); //线的宽度


            float get_finalresult[][] = TwoArry(result);
            Log.d("zhuanhuan",get_finalresult+"");
            int object_num = 0;
            int num = result.length/6;// number of object
            //continue to draw rect
            for(object_num = 0; object_num < num; object_num++){
                Log.d(TAG, "result :" + Arrays.toString(get_finalresult));
                // 画框
                paint.setColor(Color.RED);
                paint.setStyle(Paint.Style.STROKE);//不填充
                paint.setStrokeWidth(10); //线的宽度
                canvas.drawRect(get_finalresult[object_num][2] * rgba.getWidth(), get_finalresult[object_num][3] * rgba.getHeight(),
                        get_finalresult[object_num][4] * rgba.getWidth(), get_finalresult[object_num][5] * rgba.getHeight(), paint);

                paint.setColor(Color.YELLOW);
                paint.setStyle(Paint.Style.FILL);//不填充
                paint.setStrokeWidth(1); //线的宽度
                paint.setTextSize(70);
                canvas.drawText(resultLabel.get((int) get_finalresult[object_num][0]) + "\n" + get_finalresult[object_num][1],
                        get_finalresult[object_num][2]*rgba.getWidth(),get_finalresult[object_num][3]*rgba.getHeight(),paint);
            }

            show_image.setImageBitmap(rgba);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //一维数组转化为二维数组
    public static float[][] TwoArry(float[] inputfloat){
        int n = inputfloat.length;
        int num = inputfloat.length/6;
        float[][] outputfloat = new float[num][6];
        int k = 0;
        for(int i = 0; i < num ; i++)
        {
            int j = 0;

            while(j<6)
            {
                outputfloat[i][j] =  inputfloat[k];
                k++;
                j++;
            }

        }

        return outputfloat;
    }






}
