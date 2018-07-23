package com.extcord.jg3215.ecc;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Objects;

public class ThresholdActivity extends AppCompatActivity {

    //The three Current Thresholds
    private int OrangeThreshold;
    private int RedThreshold;
    private int PowerThreshold;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_threshold);

        //The three EditText fields where the user will enter values
        EditText editOrange = findViewById(R.id.editOrange);
        EditText editRed = findViewById(R.id.editRed);
        EditText editPower = findViewById(R.id.editPower);

        // Reads from file to see if any thresholds were previously saved
        File directory = getExternalFilesDir("/");
        File file = new File(directory,"Thresholds.txt");

        try{
            if(file.length()!=0){
                FileReader fileReader = new FileReader(file);
                BufferedReader bufferedReader = new BufferedReader(fileReader);
                //The three thresholds are on consecutive lines in the text file
                OrangeThreshold = Integer.parseInt(bufferedReader.readLine());
                RedThreshold = Integer.parseInt(bufferedReader.readLine());
                PowerThreshold = Integer.parseInt(bufferedReader.readLine());
                bufferedReader.close();
            }else{
                //If no thresholds have been saved, ie never set, defaults to these options
                OrangeThreshold = 5;
                RedThreshold = 10;
                PowerThreshold = 15;
            }
        }catch (IOException e) {
            Toast.makeText(getBaseContext(), "Failed to read Thresholds", Toast.LENGTH_SHORT).show();
        }

        //Sets the current thresholds as hint texts for the edittext fields
        editOrange.setHint(Integer.toString(OrangeThreshold));
        editRed.setHint(Integer.toString(RedThreshold));
        editPower.setHint(Integer.toString(PowerThreshold));
    }

    //When the Orange Threshold edit text fiels is pressed, the new number is gotten and the new set of thresholds
    //is saved to file
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
                //Function that tells the bluetooth service that the orange threshold has changed, so that it can
                //send the new threshold to the device.
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

    //When user presses the OK button the activity is cloesed and Main activity is shown again
    public void okclick(View v){
        ThresholdActivity.this.finish();
    }

    //Functions that sends a broadcast to the broadcast receiver in the bluetooth service which listens to a
    //change in threshold. The new threshold is passed along with the message, so the bluetooth service can
    //send it to the device. Same functions for all the three threshold changes
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
