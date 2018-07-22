package com.extcord.jg3215.ecc;

import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
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

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.Buffer;
import java.util.Objects;

public class ThresholdActivity extends AppCompatActivity {

    private int OrangeThreshold;
    private int RedThreshold;
    private int PowerThreshold;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_threshold);

        EditText editOrange = findViewById(R.id.editOrange);
        EditText editRed = findViewById(R.id.editRed);
        EditText editPower = findViewById(R.id.editPower);

        // Reads fr
        File directory = getExternalFilesDir("/");
        File file = new File(directory,"Thresholds.txt");

        try{
            if(file.length()!=0){
                FileReader fileReader = new FileReader(file);
                BufferedReader bufferedReader = new BufferedReader(fileReader);
                OrangeThreshold = Integer.parseInt(bufferedReader.readLine());
                RedThreshold = Integer.parseInt(bufferedReader.readLine());
                PowerThreshold = Integer.parseInt(bufferedReader.readLine());
                bufferedReader.close();
            }else{
                OrangeThreshold = 5;
                RedThreshold = 10;
                PowerThreshold = 15;
            }
        }catch (IOException e) {
            Toast.makeText(getBaseContext(), "Failed to read Thresholds", Toast.LENGTH_SHORT).show();
        }

        editOrange.setHint(Integer.toString(OrangeThreshold));
        editRed.setHint(Integer.toString(RedThreshold));
        editPower.setHint(Integer.toString(PowerThreshold));
    }

    public void orangeclick(View v)
    {
        EditText editOrange = findViewById(R.id.editOrange);
        String newthreshold = editOrange.getText().toString();
        if(Objects.equals(newthreshold, "")){
            Toast.makeText(getBaseContext(), "Please enter a number", Toast.LENGTH_SHORT).show();
        }
        else {
            OrangeThreshold = Integer.parseInt(newthreshold);
            File directory = getExternalFilesDir("/");
            File file = new File(directory,"Thresholds.txt");
            try{
                FileWriter outstream = new FileWriter(file);
                outstream.write(Integer.toString(OrangeThreshold)+"\n");
                outstream.write(Integer.toString(RedThreshold)+"\n");
                outstream.write(Integer.toString(PowerThreshold));
                outstream.close();
                sendOrangeThreshold(Integer.toString(OrangeThreshold));

            }catch (IOException e) {
                //Toast.makeText(getBaseContext(), "Fail", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void redclick(View v)
    {
        EditText editRed = findViewById(R.id.editRed);
        String newthreshold = editRed.getText().toString();
        if(Objects.equals(newthreshold, "")){
            Toast.makeText(getBaseContext(), "Please enter a number", Toast.LENGTH_SHORT).show();
        }
        else {
            RedThreshold = Integer.parseInt(newthreshold);
            File directory = getExternalFilesDir("/");
            File file = new File(directory,"Thresholds.txt");
            try{
                FileWriter outstream = new FileWriter(file);
                outstream.write(Integer.toString(OrangeThreshold)+"\n");
                outstream.write(Integer.toString(RedThreshold)+"\n");
                outstream.write(Integer.toString(PowerThreshold));
                outstream.close();
                sendRedThreshold(Integer.toString(RedThreshold));

            }catch (IOException e) {
                //Toast.makeText(getBaseContext(), "Fail", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void powerclick(View v)
    {
        EditText editPower = findViewById(R.id.editPower);
        String newthreshold = editPower.getText().toString();
        if(Objects.equals(newthreshold, "")){
            Toast.makeText(getBaseContext(), "Please enter a number", Toast.LENGTH_SHORT).show();
        }
        else {
            PowerThreshold = Integer.parseInt(newthreshold);
            File directory = getExternalFilesDir("/");
            File file = new File(directory,"Thresholds.txt");
            try{
                FileWriter outstream = new FileWriter(file);
                outstream.write(Integer.toString(OrangeThreshold)+"\n");
                outstream.write(Integer.toString(RedThreshold)+"\n");
                outstream.write(Integer.toString(PowerThreshold));
                outstream.close();
                sendPowerThreshold(Integer.toString(PowerThreshold));

            }catch (IOException e) {
                //Toast.makeText(getBaseContext(), "Fail", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void okclick(View v){
        ThresholdActivity.this.finish();
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
