package com.brianchuquiruna.vc_android.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;

import com.brianchuquiruna.vc_android.R;
import com.brianchuquiruna.vc_android.helpers.ImageHelperActivity;

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
            module = Module.load(assetFilePath(this, "mobile_only_grape_SqueezeNet_model.ptl"));
        } catch (IOException e) {
            Log.i("PTRTDryRun", "Error reading assets", e);
            finish();
        }
    }

    public static String assetFilePath(Context context, String assetName) throws IOException {
        File file = new File(context.getFilesDir(), assetName);
        if (file.exists() && file.length() > 0) {
            return file.getAbsolutePath();
        }

        try (InputStream is = context.getAssets().open(assetName)) {
            try (OutputStream os = new FileOutputStream(file)) {
                byte[] buffer = new byte[4 * 1024];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                }
                os.flush();
            }
            return file.getAbsolutePath();
        }
    }

    public String[] getClassesNames() {
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
//        Bitmap argbBitmap;
//        //Si la imagen es del tamaño de entrada de la CNN no se hace nada
//        if (_argbBitmap.getHeight()==256 && _argbBitmap.getWidth()==256){
//            argbBitmap = _argbBitmap;
//        }else{//Pero en caso la imagen no sea del mismo ancho y alto de la capa de entrada de la CNN entrenada, entonces hacemos un resize
//            argbBitmap = Bitmap.createScaledBitmap(_argbBitmap,256,256,true);
//        }

        try {
            final Tensor inputTensor = TensorImageUtils.bitmapToFloat32Tensor(argbBitmap,
                    TensorImageUtils.TORCHVISION_NORM_MEAN_RGB, TensorImageUtils.TORCHVISION_NORM_STD_RGB);

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
            for (int i = 0; i < scores.length; i++) {
                if (scores[i] > maxScore) {
                    maxScore = scores[i];
                    maxScoreIdx = i;
                }
            }
            String[] classes = getClassesNames();
            String className = classes[maxScoreIdx];
            getOutputTextView().setText(className);

        } catch (Exception e) {
            Log.i("CAMARA_ERROR", e.toString());
        }
    }
}
