package com.extcord.jg3215.ecc;

import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.IntentCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.Buffer;

public class ThresholdActivity extends AppCompatActivity {

    // DUE TO MISUNDERSTANDING - LOWER THRESHOLD IS CALLED GREEN, MIDDLE ONE IS CALLED ORANGE AND TOP ONE IS CALLED RED
    private int GreenProgress;
    private int OrangeProgress;
    private int RedProgress;
    private int GreenMin;
    private int GreenMax;
    private int OrangeMin;
    private int OrangeMax;
    private int RedMin;
    private int RedMax;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_threshold);

        SeekBar GreenSeekBar = findViewById(R.id.seekBarGreen);
        SeekBar OrangeSeekBar = findViewById(R.id.seekBarOrange);
        SeekBar RedSeekBar = findViewById(R.id.seekBarRed);
        TextView textViewGreen = findViewById(R.id.textViewgreen);
        TextView textViewOrange = findViewById(R.id.textVieworange);
        TextView textViewRed = findViewById(R.id.textViewred);

        File directory = getExternalFilesDir("/");
        File file = new File(directory,"Limits.txt");
        File file2 = new File(directory,"Thresholds.txt");

        try {
            if(file.length()!=0) {
                FileReader fileReader = new FileReader(file);
                // Always wrap FileReader in BufferedReader.
                BufferedReader bufferedReader = new BufferedReader(fileReader);
                GreenSeekBar.setMin(Integer.parseInt(bufferedReader.readLine()));
                GreenMin = GreenSeekBar.getMin();
                GreenSeekBar.setMax(Integer.parseInt(bufferedReader.readLine()));
                GreenMax = GreenSeekBar.getMax();
                OrangeSeekBar.setMin(Integer.parseInt(bufferedReader.readLine()));
                OrangeMin = OrangeSeekBar.getMin();
                OrangeSeekBar.setMax(Integer.parseInt(bufferedReader.readLine()));
                OrangeMax = OrangeSeekBar.getMax();
                RedSeekBar.setMin(Integer.parseInt(bufferedReader.readLine()));
                RedMin = RedSeekBar.getMin();
                RedSeekBar.setMax(Integer.parseInt(bufferedReader.readLine()));
                RedMax = RedSeekBar.getMax();
                bufferedReader.close(); //Closes file.
            }else{
                GreenMin = 0;
                GreenMax = 150;
                OrangeMin = 0;
                OrangeMax = 150;
                RedMin = 0;
                RedMax = 150;
                GreenSeekBar.setMax(150);
                OrangeSeekBar.setMax(150);
                RedSeekBar.setMax(150);
            }
        }catch (IOException e) {
            Toast.makeText(getBaseContext(), "Failed to read Limits", Toast.LENGTH_SHORT).show();
        }

        try{
            if(file2.length()!=0){
                FileReader fileReader2 = new FileReader(file2);
                BufferedReader bufferedReader2 = new BufferedReader(fileReader2);
                GreenSeekBar.setProgress(Integer.parseInt(bufferedReader2.readLine()));
                GreenProgress = GreenSeekBar.getProgress();
                OrangeSeekBar.setProgress(Integer.parseInt(bufferedReader2.readLine()));
                OrangeProgress = OrangeSeekBar.getProgress();
                RedSeekBar.setProgress(Integer.parseInt(bufferedReader2.readLine()));
                RedProgress = RedSeekBar.getProgress();
                bufferedReader2.close();
            }else{
                GreenProgress = 75;
                OrangeProgress = 75;
                RedProgress = 75;
                GreenSeekBar.setProgress(75);
                OrangeSeekBar.setProgress(75);
                RedSeekBar.setProgress(75);
            }
        }catch (IOException e) {
            Toast.makeText(getBaseContext(), "Failed to read Thresholds", Toast.LENGTH_SHORT).show();
        }

        textViewGreen.setText(Integer.toString(GreenSeekBar.getProgress())+ " mA");
        textViewOrange.setText(Integer.toString(OrangeSeekBar.getProgress())+ " mA");
        textViewRed.setText(Integer.toString(RedSeekBar.getProgress())+ " mA");

        GreenSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressChangedValue = 0;
            TextView textViewGreen = findViewById(R.id.textViewgreen);
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressChangedValue = progress;
                String temp = Integer.toString(progressChangedValue) + " mA";
                textViewGreen.setText(temp);
            }
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }
            public void onStopTrackingTouch(SeekBar seekBar) {
                String temp = Integer.toString(progressChangedValue) + " mA";
                textViewGreen.setText(temp);
                File directory = getExternalFilesDir("/");
                File file = new File(directory,"Thresholds.txt");
                try{
                    FileWriter outstream = new FileWriter(file);
                    GreenProgress = progressChangedValue;
                    outstream.write(Integer.toString(progressChangedValue)+"\n");
                    outstream.write(Integer.toString(OrangeProgress)+"\n");
                    outstream.write(Integer.toString(RedProgress));
                    outstream.close();
                    sendOrangeThreshold(Integer.toString(progressChangedValue));

                }catch (IOException e) {

                }
            }
        });

        OrangeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressChangedValue = 0;
            TextView textViewOrange = findViewById(R.id.textVieworange);
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressChangedValue = progress;
                String temp = Integer.toString(progressChangedValue) + " mA";
                textViewOrange.setText(temp);
            }
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }
            public void onStopTrackingTouch(SeekBar seekBar) {
                String temp = Integer.toString(progressChangedValue) + " mA";
                textViewOrange.setText(temp);
                File directory = getExternalFilesDir("/");
                File file = new File(directory,"Thresholds.txt");
                try{
                    FileWriter outstream = new FileWriter(file);
                    OrangeProgress = progressChangedValue;
                    outstream.write(Integer.toString(GreenProgress)+"\n");
                    outstream.write(Integer.toString(progressChangedValue)+"\n");
                    outstream.write(Integer.toString(RedProgress));
                    outstream.close();
                    sendRedThreshold(Integer.toString(progressChangedValue));

                }catch (IOException e) {

                }
            }
        });

        RedSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressChangedValue = 0;
            TextView textViewRed = findViewById(R.id.textViewred);
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressChangedValue = progress;
                String temp = Integer.toString(progressChangedValue) + " mA";
                textViewRed.setText(temp);
            }
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }
            public void onStopTrackingTouch(SeekBar seekBar) {
                String temp = Integer.toString(progressChangedValue) + " mA";
                textViewRed.setText(temp);
                File directory = getExternalFilesDir("/");
                File file = new File(directory,"Thresholds.txt");
                try{
                    FileWriter outstream = new FileWriter(file);
                    RedProgress = progressChangedValue;
                    outstream.write(Integer.toString(GreenProgress)+"\n");
                    outstream.write(Integer.toString(OrangeProgress)+"\n");
                    outstream.write(Integer.toString(progressChangedValue));
                    outstream.close();
                    sendPowerThreshold(Integer.toString(progressChangedValue));
                }catch (IOException e) {

                }
            }
        });

        final Button btnOpenPopup = (Button)findViewById(R.id.modifylimits);
        btnOpenPopup.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View arg0) {
                LayoutInflater layoutInflater = (LayoutInflater)getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
                final View popupView = layoutInflater.inflate(R.layout.modifypopupwindow, null);
                final PopupWindow popupWindow = new PopupWindow(
                        popupView,
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);

                Button btnOK = popupView.findViewById(R.id.ok);
                btnOK.setOnClickListener(new Button.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        EditText greenMin = popupView.findViewById(R.id.greenmin);
                        EditText greenMax = popupView.findViewById(R.id.greenmax);
                        EditText orangeMin = popupView.findViewById(R.id.orangemin);
                        EditText orangeMax = popupView.findViewById(R.id.orangemax);
                        EditText redMin = popupView.findViewById(R.id.redmin);
                        EditText redMax = popupView.findViewById(R.id.redmax);
                        SeekBar GreenSeekBar = findViewById(R.id.seekBarGreen);
                        SeekBar OrangeSeekBar = findViewById(R.id.seekBarOrange);
                        SeekBar RedSeekBar = findViewById(R.id.seekBarRed);

                        if(!greenMin.getText().toString().isEmpty()){
                            GreenMin = Integer.parseInt(greenMin.getText().toString());
                            GreenSeekBar.setMin(GreenMin);
                        }
                        if(!greenMax.getText().toString().isEmpty()){
                            GreenMax = Integer.parseInt(greenMax.getText().toString());
                            GreenSeekBar.setMax(GreenMax);
                        }
                        if(!orangeMin.getText().toString().isEmpty()){
                            OrangeMin = Integer.parseInt(orangeMin.getText().toString());
                            OrangeSeekBar.setMin(OrangeMin);
                        }
                        if(!orangeMax.getText().toString().isEmpty()){
                            OrangeMax = Integer.parseInt(orangeMax.getText().toString());
                            OrangeSeekBar.setMax(OrangeMax);
                        }
                        if(!redMin.getText().toString().isEmpty()){
                            RedMin = Integer.parseInt(redMin.getText().toString());
                            RedSeekBar.setMin(RedMin);
                        }
                        if(!redMax.getText().toString().isEmpty()){
                            RedMax = Integer.parseInt(redMax.getText().toString());
                            RedSeekBar.setMax(RedMax);
                        }

                        File directory = getExternalFilesDir("/");
                        File file = new File(directory,"Limits.txt");
                        try{
                            FileWriter outstream = new FileWriter(file);
                            outstream.write(Integer.toString(GreenMin)+"\n");
                            outstream.write(Integer.toString(GreenMax)+"\n");
                            outstream.write(Integer.toString(OrangeMin)+"\n");
                            outstream.write(Integer.toString(OrangeMax)+"\n");
                            outstream.write(Integer.toString(RedMin)+"\n");
                            outstream.write(Integer.toString(RedMax));
                            outstream.close();

                        }catch (IOException e) {

                        }
                        popupWindow.dismiss();
                    }});

                Button btnDismiss = popupView.findViewById(R.id.cancel);
                btnDismiss.setOnClickListener(new Button.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        popupWindow.dismiss();
                    }});

                popupWindow.setFocusable(true);
                popupWindow.update();
                popupWindow.showAtLocation(arg0, Gravity.CENTER, 0, 0);
            }
        });
    }

    private void sendOrangeThreshold(String threshold){
        Intent intent = new Intent("OrangeThresholdChanged");
        intent.putExtra("Threshold", threshold);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void sendRedThreshold(String threshold){
        Intent intent = new Intent("RedThresholdChanged");
        intent.putExtra("Threshold", threshold);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void sendPowerThreshold(String threshold){
        Intent intent = new Intent("PowerThresholdChanged");
        intent.putExtra("Threshold", threshold);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}
