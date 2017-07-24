package com.example.admin.bubbleboard;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import static org.opencv.core.Core.bitwise_and;
import static org.opencv.core.Core.bitwise_not;
import static org.opencv.core.Core.countNonZero;
import static org.opencv.core.Core.sort;
import static org.opencv.core.Core.subtract;
import static org.opencv.core.CvType.CV_64FC1;
import static org.opencv.core.CvType.CV_8UC1;
import static org.opencv.imgproc.Imgproc.COLOR_RGB2GRAY;
import static org.opencv.imgproc.Imgproc.CV_DIST_HUBER;
import static org.opencv.imgproc.Imgproc.CV_HOUGH_STANDARD;
import static org.opencv.imgproc.Imgproc.boundingRect;
import static org.opencv.imgproc.Imgproc.contourArea;


public class ImageDisplayActivity extends AppCompatActivity {

    private static ImageView imageView;
    private String filename;
    private Bitmap image;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_display);

        filename = getIntent().getStringExtra("filename");
        //filename="/storage/sdcard/omr.jpg";
        //filename= Environment.getExternalStorageDirectory().getPath()+"download/BJq8M.jpg";
        imageView = (ImageView)findViewById(R.id.displayImage);
        image=BitmapFactory.decodeFile(filename);
        //detectEdges(image);
        showAllCircles(image);


    }


   public void showAllCircles(Bitmap paramView)
    {
        paramView = BitmapFactory.decodeFile(filename);
        Mat localMat1 = new Mat();
        Utils.bitmapToMat(paramView, localMat1);

        Mat localMat2 = new Mat();
        Imgproc.GaussianBlur(localMat1, localMat2, new Size(5.0D, 5.0D), 7.0D, 6.5D);
        Object localObject = new Mat();
        Imgproc.cvtColor(localMat2, (Mat)localObject, COLOR_RGB2GRAY);
        Mat cloneMat= ((Mat) localObject).clone();
        //Mat blackwhite= ((Mat) localObject).clone();
        localMat2 = localMat1.clone();
        bitwise_not(cloneMat,cloneMat);
        Imgproc.threshold(cloneMat,localMat2,127,255,Imgproc.THRESH_OTSU);
        Mat thresh=localMat2.clone();

        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        List<MatOfPoint> questions = new ArrayList<MatOfPoint>();
        List<MatOfPoint> sorted = new ArrayList<MatOfPoint>();

        Mat hierarchy = new Mat();
        Imgproc.findContours(localMat2, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        Rect rect,rect2;
        int groups[] = new int[30];
        int i=0,l=0;
        for (int contourIdx = 0; contourIdx < contours.size(); contourIdx++) {
             rect = Imgproc.boundingRect(contours.get(contourIdx));
            //float area=rect.width/(float)rect.height;


            if(rect.width>=29 && rect.height>=29){
                questions.add(contours.get(contourIdx));

                //rect3 = Imgproc.boundingRect(questions.get(i));
                //Log.i("Before------",rect3.tl()+" ");

               for(int ctr=0;ctr<questions.size()-1;ctr++){
                    MatOfPoint ctr1 = questions.get(i);
                    rect = Imgproc.boundingRect(questions.get(i));
                    MatOfPoint ctr2 = questions.get(ctr);
                    rect2 = Imgproc.boundingRect(questions.get(ctr));
                    if(rect.tl().x<rect2.tl().x){
                            questions.set(ctr,ctr1);
                        questions.set(i,ctr2);
                    }
                }
                //rect3 = Imgproc.boundingRect(questions.get(i));
                //Log.i("after",rect3.tl()+" ");
                i++;

                if(i%5==0){
                    groups[l]=questions.indexOf(contours.get(contourIdx));
                    l++;
                    //Log.i("groups---",""+groups[l-1]);

                }
            }
        }

        int j=0;i=0;
        while(j!=questions.size()){

            for(int ctr=0;ctr<questions.size()-1;ctr++){
                MatOfPoint ctr1 = questions.get(i);
                rect = Imgproc.boundingRect(questions.get(i));
                MatOfPoint ctr2 = questions.get(ctr);
                rect2 = Imgproc.boundingRect(questions.get(ctr));
                if(rect.tl().y<rect2.tl().y){
                    questions.set(ctr,ctr1);
                    questions.set(i,ctr2);
                }
            }
        i++;
        j++;
        }

        //Collections.sort(questions, Collections.reverseOrder());
        int bubble =0;
        for (int contourIdx = 0; contourIdx < questions.size(); contourIdx++) {

            Rect rectCrop = boundingRect(questions.get(contourIdx));
            Mat imageROI= thresh.submat(rectCrop);

            int total = countNonZero(imageROI);
            double pixel =total/contourArea(questions.get(contourIdx))*100;
            if(pixel>=80){
                Log.i("Answer:",bubble+" - "+contourIdx%5);
                //sorted.add(questions.get(contourIdx));
                Imgproc.drawContours(localMat1, questions, contourIdx, new Scalar(255.0D, 0.0D, 0.0D), 2);
                bubble++;

            }

        }

        //Random rnd = new Random();
        //i=0;
        //int r=0,g=0,b=0;

        /*for (int contourIdx = 0; contourIdx <sorted.size(); contourIdx++) {

               // r=rnd.nextInt(256);
                //g=rnd.nextInt(256);
                //b=rnd.nextInt(256);
            Imgproc.drawContours(localMat1, sorted, contourIdx, new Scalar(255.0D, 0.0D, 0.0D), 2);
            i=i+1;
            //Log.i("Local Objects","Local Object Point -
            // ------------------"+localMat2);
        }*/

        Utils.matToBitmap(localMat1, paramView);
        this.imageView.setImageBitmap(paramView);

    }

}
