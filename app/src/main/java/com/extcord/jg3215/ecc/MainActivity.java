// Extension Cord Control Application
// For the Royal Devon and Exeter NHS Foundation Trust
// Developed by Javier Geis and Artur Jürgenson
// 27 Jun 2018

package com.extcord.jg3215.ecc;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    // GUI Components
    private TextView mBluetoothStatus;
    private TextView mReadBuffer;
    private TextView CurrentTextView;
    private int GreenThreshold = 0;
    private int OrangeThreshold = 0;
    private int RedThreshold = 0;

    private BluetoothAdapter mBTAdapter;
    private Set<BluetoothDevice> mPairedDevices;
    private ArrayAdapter<String> mBTArrayAdapter;
    private ListView mDevicesListView;

    private final String TAG = MainActivity.class.getSimpleName();
    private ConnectedThread mConnectedThread; // bluetooth background worker thread to send and receive data
    private BluetoothSocket mBTSocket = null; // bi-directional client-to-client data path

    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // "random" unique identifier

    // #defines for identifying shared types between calling functions
    private final static int REQUEST_ENABLE_BT = 1; // used to identify adding bluetooth names

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CurrentTextView = (TextView) findViewById(R.id.CurrentTextView);
        File directory = getExternalFilesDir("/");
        File file = new File(directory,"Thresholds.txt");

        try {
            if(file.length()!=0) {
                FileReader fileReader = new FileReader(file);
                // Always wrap FileReader in BufferedReader.
                BufferedReader bufferedReader = new BufferedReader(fileReader);
                GreenThreshold = Integer.parseInt(bufferedReader.readLine());
                OrangeThreshold = Integer.parseInt(bufferedReader.readLine());
                RedThreshold = Integer.parseInt(bufferedReader.readLine());
                bufferedReader.close(); //Closes file.
                //Toast.makeText(getBaseContext(), Integer.toString(GreenThreshold) + "  "+ Integer.toString(OrangeThreshold) +"  " + Integer.toString(RedThreshold), Toast.LENGTH_SHORT).show();
            }else{
                GreenThreshold = 150;
                OrangeThreshold = 150;
                RedThreshold = 150;
               // Toast.makeText(getBaseContext(), Integer.toString(GreenThreshold) + "  "+ Integer.toString(OrangeThreshold) +"  " + Integer.toString(RedThreshold), Toast.LENGTH_SHORT).show();
            }
        }catch (IOException e) {
            Toast.makeText(getBaseContext(), "Failed to read Thresholds", Toast.LENGTH_SHORT).show();
        }

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

        mDevicesListView = findViewById(R.id.devicesListView);
        mDevicesListView.setAdapter(mBTArrayAdapter); // assign model to view
        mDevicesListView.setOnItemClickListener(mDeviceClickListener);

        // Ask for location permission if not already allowed
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);

        if (mBTArrayAdapter == null) {
            // Device does not support Bluetooth
            Toast.makeText(getApplicationContext(),"Bluetooth device not found!",Toast.LENGTH_SHORT).show();
        }
       // else {
        /*    mScanBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bluetoothOn(v);
                }
            }); */


         /*   mListPairedDevicesBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v){
                    listPairedDevices(v);
                }
            });

            mDiscoverBtn.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    discover(v);
                }
            }); */
       // }
    }

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

                mDevicesListView = findViewById(R.id.devicesListView);
                mDevicesListView.setAdapter(mBTArrayAdapter); // assign model to view
                mDevicesListView.setOnItemClickListener(mDeviceClickListener);
                Toast.makeText(getApplicationContext(),"Bluetooth enabled",Toast.LENGTH_SHORT).show();
            }
            else
                Toast.makeText(getApplicationContext(),"Please enable bluetooth",Toast.LENGTH_SHORT).show();
        }
    }

    private void discover(View view){
        // Check if the device is already discovering
        if(mBTAdapter.isDiscovering()){
            mBTAdapter.cancelDiscovery();
            Toast.makeText(getApplicationContext(),"Discovery stopped",Toast.LENGTH_SHORT).show();
        }
        else{
            if(mBTAdapter.isEnabled()) {
                mBTArrayAdapter.clear(); // clear items
                mBTAdapter.startDiscovery();
                Toast.makeText(getApplicationContext(), "Discovery started", Toast.LENGTH_SHORT).show();
                registerReceiver(blReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
            }
            else{
                Toast.makeText(getApplicationContext(), "Bluetooth not on", Toast.LENGTH_SHORT).show();
            }
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
            final String name = info.substring(0,info.length() - 17);

            // Spawn a new thread to avoid blocking the GUI one
            new Thread()
            {
                public void run() {
                    boolean fail = false;

                    BluetoothDevice device = mBTAdapter.getRemoteDevice(address);

                    try {
                        mBTSocket = createBluetoothSocket(device);
                    } catch (IOException e) {
                        fail = true;
                        Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_SHORT).show();
                    }
                    // Establish the Bluetooth socket connection.
                    try {
                        mBTSocket.connect();
                    } catch (IOException e) {
                        try {
                            fail = true;
                            mBTSocket.close();
                            Toast.makeText(getApplicationContext(),"Connection Failed",Toast.LENGTH_SHORT).show();
                        } catch (IOException e2) {
                            //insert code to deal with this
                            Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                    if(fail == false) {
                        mConnectedThread = new ConnectedThread(mBTSocket);
                        mConnectedThread.start();
                        Toast.makeText(getApplicationContext(),"Connected to device",Toast.LENGTH_SHORT).show();
                    }
                }
            }.start();
        }
    };

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        try {
            final Method m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", UUID.class);
            return (BluetoothSocket) m.invoke(device, BTMODULEUUID);
        } catch (Exception e) {
            Log.e(TAG, "Could not create Insecure RFComm Connection",e);
        }
        return  device.createRfcommSocketToServiceRecord(BTMODULEUUID);
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mMessageReceiver,
                    new IntentFilter("ThresholdChanged"));

            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                write("T");
                String newThreshold = intent.getExtras().getString("Threshold");
                write(newThreshold);
            }
        };

        public void run() {
            byte[] buffer = new byte[1];  // buffer store for the stream
            int bytes; // bytes returned from read()
            String Currentstring = null;
            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);
                    if(bytes != 0) {
                        final String strReceived = new String(buffer, 0, bytes);
                        // SystemClock.sleep(100); //pause and wait for rest of data. Adjust this depending on your sending speed.
                        if(!Objects.equals(strReceived,"#")){
                            if(!Objects.equals(strReceived,"S")){
                                Currentstring = Currentstring + strReceived;
                            }//else{
                                //Do someth
                           // }
                        }else {
                            String toprint = Currentstring + " mA";
                            CurrentTextView.setText(toprint);
                            int current = Integer.parseInt(Currentstring);
                            if (current < GreenThreshold){
                                CurrentTextView.setTextColor(Color.GREEN);
                            }
                            else if (current >= GreenThreshold && current < OrangeThreshold){
                                CurrentTextView.setTextColor(Color.rgb(255,165,0));
                            }
                            else if(current >= OrangeThreshold && current < RedThreshold){
                                CurrentTextView.setTextColor(Color.RED);
                            }
                            else if(current > RedThreshold){
                                CurrentTextView.setText("Power Shut DOWN !");
                                write("S");
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();

                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(String input) {
            byte[] bytes = input.getBytes();           //converts entered String into bytes
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) { }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }
}
