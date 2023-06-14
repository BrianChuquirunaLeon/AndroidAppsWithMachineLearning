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

    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {

            module = Module.load(assetFilePath(this,"mobile_only_grape_LeNet_model_12.ptl"));

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
