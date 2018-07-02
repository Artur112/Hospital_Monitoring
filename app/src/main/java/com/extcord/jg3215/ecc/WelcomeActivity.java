// Extension Cord Control Application
// For the Royal Devon and Exeter NHS Foundation Trust
// Developed by Javier Geis and Artur Jürgenson
// 27 Jun 2018

package com.extcord.jg3215.ecc;

import android.support.v7.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;


public class WelcomeActivity extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    public void clickanywhere(View v)
    {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }

}
