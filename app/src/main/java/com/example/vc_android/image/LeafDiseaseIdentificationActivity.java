package com.example.vc_android.image;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;

import com.example.vc_android.R;
import com.example.vc_android.helpers.ImageHelperActivity;

import org.pytorch.Device;
import org.pytorch.IValue;
import org.pytorch.Module;
import org.pytorch.Tensor;
import org.pytorch.torchvision.TensorImageUtils;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;

public class LeafDiseaseIdentificationActivity extends ImageHelperActivity {

    private Module module;
//    private static String[] classes = {"Apple___Apple_scab", "Apple___Black_rot", "Apple___Cedar_apple_rust", "Apple___healthy",
//            "Blueberry___healthy", "Cherry_(including_sour)___Powdery_mildew", "Cherry_(including_sour)___healthy",
//            "Corn_(maize)___Cercospora_leaf_spot Gray_leaf_spot", "Corn_(maize)___Common_rust_",
//            "Corn_(maize)___Northern_Leaf_Blight", "Corn_(maize)___healthy", "Grape___Black_rot", "Grape___Esca_(Black_Measles)",
//            "Grape___Leaf_blight_(Isariopsis_Leaf_Spot)", "Grape___healthy", "Orange___Haunglongbing_(Citrus_greening)",
//            "Peach___Bacterial_spot", "Peach___healthy", "Pepper,_bell___Bacterial_spot", "Pepper,_bell___healthy",
//            "Potato___Early_blight", "Potato___Late_blight", "Potato___healthy", "Raspberry___healthy", "Soybean___healthy",
//            "Squash___Powdery_mildew", "Strawberry___Leaf_scorch", "Strawberry___healthy", "Tomato___Bacterial_spot",
//            "Tomato___Early_blight", "Tomato___Late_blight", "Tomato___Leaf_Mold", "Tomato___Septoria_leaf_spot",
//            "Tomato___Spider_mites Two-spotted_spider_mite", "Tomato___Target_Spot", "Tomato___Tomato_Yellow_Leaf_Curl_Virus",
//            "Tomato___Tomato_mosaic_virus", "Tomato___healthy"};

    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {

            module = Module.load(assetFilePath(this,"model_know_normalize_from_pt_to_ptl.ptl"));

        }catch (IOException e){
            Log.e("PTRTDryRun","Error reading assets",e);
            finish();
        }
    }

    public static String assetFilePath(Context context, String assetName) throws IOException {
        File file = new File (context.getFilesDir(),assetName);
        if(file.exists() && file.length()>0){
            return file.getAbsolutePath();
        }

        try(InputStream is = context.getAssets().open(assetName)){
            try(OutputStream os = new FileOutputStream(file)){
                byte[] buffer = new byte[4*1024];
                int read;
                while ( (read=is.read(buffer)) !=-1 ){
                    os.write(buffer,0,read);
                }
                os.flush();
            }
            return file.getAbsolutePath();
        }
    }

    public String[] getClassesNames(){
        InputStream inputStream = getResources().openRawResource(R.raw.classes_names);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

        ArrayList<String> listaStrings = new ArrayList<>();
        String linea;

        try {
            while ((linea = bufferedReader.readLine()) != null) {
                listaStrings.add(linea);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        String[] arrayStrings = listaStrings.toArray(new String[0]);

        return arrayStrings;
    }

    protected void runClassification(final Bitmap bitmap) {
        Bitmap argbBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        final Tensor inputTensor = TensorImageUtils.bitmapToFloat32Tensor(argbBitmap,
                TensorImageUtils.TORCHVISION_NORM_MEAN_RGB,TensorImageUtils.TORCHVISION_NORM_STD_RGB);


        //running the model
        final Tensor outputTensor = this.module.forward(IValue.from(inputTensor)).toTensor();

        //getting tensor content as java arrary of floats
        final float[] scores = outputTensor.getDataAsFloatArray();


        for (int i = 0; i < scores.length; i++) {
            System.out.println(scores[i]);
        }
        //searching for the index with maximun score
        float maxScore = -Float.MAX_VALUE;
        int maxScoreIdx = -1;
        for(int i=0; i<scores.length;i++){
            if (scores[i]>maxScore){
                maxScore=scores[i];
                maxScoreIdx = i;
            }
        }
        String[] classes = getClassesNames();
        String className = classes[maxScoreIdx];
        getOutputTextView().setText(className);
    }
}
