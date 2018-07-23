// Extension Cord Control Application
// For the Royal Devon and Exeter NHS Foundation Trust
// Developed by Javier Geis and Artur Jürgenson
// 27 Jun 2018

package com.extcord.jg3215.ecc;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    // GUI Components
    // Bluetooth Status textbox at the top
    private TextView mBluetoothStatus;
    //The textbox that displays the Current in Amps
    private TextView CurrentTextView;
    //The textbox that displays warning messages
    private TextView warningmessage;

    private BluetoothAdapter mBTAdapter;
    private Set<BluetoothDevice> mPairedDevices;
    private ArrayAdapter<String> mBTArrayAdapter;
    private ListView mDevicesListView;
    private PopupWindow popupWindow;

    private final String TAG = MainActivity.class.getSimpleName();
    private BluetoothSocket mBTSocket = null; // bi-directional client-to-client data path

    public static String deviceAddress = null;

    // #defines for identifying shared types between calling functions
    private final static int REQUEST_ENABLE_BT = 1; // used to identify adding bluetooth names

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Foreground.init(getApplication());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CurrentTextView = findViewById(R.id.CurrentTextView);
        warningmessage = findViewById(R.id.warningmessage);
        mBluetoothStatus = findViewById(R.id.mBluetoothStatus);

        //Registers the broadcast receivers that will be listening to Blueooth events that happen in the service,
        LocalBroadcastManager.getInstance(this).registerReceiver(ConnectedMessageReceiver,
                new IntentFilter("ConnectedBluetooth"));
        LocalBroadcastManager.getInstance(this).registerReceiver(CouldntConnectMessageReceiver,
                new IntentFilter("CouldntConnect"));
        LocalBroadcastManager.getInstance(this).registerReceiver(CurrentReceivedMessageReceiver,
                new IntentFilter("CurrentReceived"));

        mBTArrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1);
        mBTAdapter = BluetoothAdapter.getDefaultAdapter(); // get a handle on the bluetooth radio

        if (!mBTAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        mPairedDevices = mBTAdapter.getBondedDevices();

        if(mBTAdapter.isEnabled()) {
            // put it's one to the adapter
            for (BluetoothDevice device : mPairedDevices)
                mBTArrayAdapter.add(device.getName() + "\n" + device.getAddress());
        }

        // Ask for location permission if not already allowed
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);

        if (mBTArrayAdapter == null) {
            // Device does not support Bluetooth
            Toast.makeText(getApplicationContext(),"Bluetooth device not found!",Toast.LENGTH_SHORT).show();
        }

        final Button btnOpenPopup = findViewById(R.id.connectbutton);
        btnOpenPopup.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View arg0) {
                LayoutInflater layoutInflater = (LayoutInflater)getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
                final View popupView = layoutInflater.inflate(R.layout.choosechord, null);
                        popupWindow = new PopupWindow(
                        popupView,
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);
                
                mDevicesListView = popupView.findViewById(R.id.devicesListView);
                mDevicesListView.setAdapter(mBTArrayAdapter); // assign model to view
                mDevicesListView.setOnItemClickListener(mDeviceClickListener);

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

    //When the Bluetooth connection is made, this broadcast receiver receives the message that the service
    //sends and sets the Bluetooth Status textview accordingly. it also closes the cord selection popupwindow
    private BroadcastReceiver ConnectedMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            TextView textView3 = findViewById(R.id.mBluetoothStatus);
            String instr = "Bluetooth Status: Connected";
            textView3.setText(instr);
            popupWindow.dismiss();
        }
    };

    //If the connection attempt fails, this receives the message from the service, says that the connection failed
    //stops the bluetooth service.
    private BroadcastReceiver CouldntConnectMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            stopService(new Intent(MainActivity.this, BluetoothService.class));
        }
    };

    //When a new current is received via bluetooth in the service, the service sends the new current and
    // the color so which range it is in in a message that this broadcast receiver listens to.
    //This code sets the Currenttextview to the new value and accoring to what range the message says it is in
    //it sets the color of the textview. THIS code should be edited to add what warning message should be displayed
    // depening on what the color is.
    private BroadcastReceiver CurrentReceivedMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String current = intent.getExtras().getString("Current");
            String color = intent.getExtras().getString("Color");
            String text = current + " Amps";
            CurrentTextView.setText(text);

            if(color.equals("green")){
                CurrentTextView.setTextColor(Color.GREEN);
            }
            else if(color.equals("orange")){
                CurrentTextView.setTextColor(Color.rgb(255,165,0));
            }
            else if(color.equals("red")){
                CurrentTextView.setTextColor(Color.RED);
            }
            else if(color.equals("black")){
                CurrentTextView.setTextColor(Color.BLACK);
            }
        }
    };

    //Sends message to bluetooth service to shut down the connection
    private void DisconnectMessage() {
        Intent intent = new Intent("DisconnectBluetooth");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    //Called when the disconnect button is pressed. Sends the message just previously described and stops
    //the bluetooth service afterwards.
    public void discbutton(View v){
        if (mBluetoothStatus.getText().equals("Bluetooth Status: Connected")){
            DisconnectMessage();
            String text = "Bluetooth Status: Not Connected";
            mBluetoothStatus.setText(text);
            stopService(new Intent(MainActivity.this, BluetoothService.class));
        }
    }

    //Opens Threshold activity when edit thresholds button is pressed
    public void editThresholds(View v){
        Intent intent = new Intent(getApplicationContext(), ThresholdActivity.class);
        startActivity(intent);
    }

    // Enter here after user selects "yes" or "no" to enabling radio
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent Data){
        // Check which request we're responding to
        if (requestCode == REQUEST_ENABLE_BT) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                // The user picked a contact.
                // The Intent's data Uri identifies which contact was selected.
                mPairedDevices = mBTAdapter.getBondedDevices();

                if(mBTAdapter.isEnabled()) {
                    // put it's one to the adapter
                    for (BluetoothDevice device : mPairedDevices)
                        mBTArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                }

                Toast.makeText(getApplicationContext(),"Bluetooth enabled",Toast.LENGTH_SHORT).show();
            }
            else
                Toast.makeText(getApplicationContext(),"Please enable bluetooth",Toast.LENGTH_SHORT).show();
        }
    }

    final BroadcastReceiver blReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // add the name to the list
                mBTArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                mBTArrayAdapter.notifyDataSetChanged();
            }
        }
    };

    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            if(!mBTAdapter.isEnabled()) {
                Toast.makeText(getBaseContext(), "Bluetooth not on", Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(getBaseContext(), "Connecting...", Toast.LENGTH_SHORT).show();
            // Get the device MAC address, which is the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            final String address = info.substring(info.length() - 17);
            deviceAddress = address;
            if(deviceAddress != null){
                Intent mIntent = new Intent(MainActivity.this, BluetoothService.class);
                startService(mIntent);
            }
           // final String name = info.substring(0,info.length() - 17);
        }
    };

    public void toastAnywhere(final String text) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            public void run() {
                Toast.makeText( getApplicationContext(), text,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
