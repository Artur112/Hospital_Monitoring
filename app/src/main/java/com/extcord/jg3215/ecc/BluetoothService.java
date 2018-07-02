package com.extcord.jg3215.ecc;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;
import java.util.UUID;


public class BluetoothService extends Service {

    public static boolean rawdatawritten = false;
    public ConnectedThread mConnectedThread; // bluetooth background worker thread to send and receive data
    public Handler mHandler; // Our main handler that will receive callback notifications
    private BluetoothSocket mBTSocket = null; // bi-directional client-to-client data path
    private BluetoothAdapter mBTAdapter;

    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // "random" unique identifier

    // #defines for identifying shared types between calling functions
    private final static int REQUEST_ENABLE_BT = 1; // used to identify adding bluetooth names
    private final static int MESSAGE_READ = 2; // used in bluetooth handler to identify message update
    private final static int CONNECTING_STATUS = 3; // used in bluetooth handler to identify message status

    public BluetoothService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Connect();
        return START_STICKY;// Keeps the service running
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        //bluetooth stuff
       /* if (!mBTAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } */

        //Toast.makeText(getBaseContext(), "BluetoothService created", Toast.LENGTH_SHORT).show();

        mBTAdapter = BluetoothAdapter.getDefaultAdapter();

        mHandler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == MESSAGE_READ) {
                    String readMessage = null;
                    try {
                        // readMessage = new String((byte[]) msg.obj, "UTF-8");
                        String messageread = (String) msg.obj;
                        // bluetoothstatus.setText("Received data");
                        writeToFile(messageread);
                       // Toast.makeText(getBaseContext(), "Data Written to device", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                if (msg.what == CONNECTING_STATUS) {
                    if (msg.arg1 == 1)
                        Toast.makeText(getBaseContext(), "Connected to Bluetooth", Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(getBaseContext(), "Connection failed", Toast.LENGTH_SHORT).show();
                }
            }
        };

        final BluetoothDevice device = mBTAdapter.getRemoteDevice("AB:9D:AC:56:34:02");
        new Thread() {
            public void run() {
                boolean fail = false;
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
                        mHandler.obtainMessage(CONNECTING_STATUS, -1, -1)
                                .sendToTarget();
                    } catch (IOException e2) {
                        //insert code to deal with this
                        Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_SHORT).show();
                    }
                }
                if (fail == false) {
                    mConnectedThread = new BluetoothService.ConnectedThread(mBTSocket);
                    mConnectedThread.start();

                    mHandler.obtainMessage(CONNECTING_STATUS, 1, -1)
                            .sendToTarget();
                }
            }
        }.start();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        // For time consuming an long tasks you can launch a new thread here...
        // Do your Bluetooth Work Here
       // Toast.makeText(this, "Bluetooth Service Started", Toast.LENGTH_LONG).show();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
     //   Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show();

    }

    /*
     * This is used to bind classes to the service so they can communicate
     */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    private static boolean isExternalStorageReadOnly() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(extStorageState)) {
            return true;
        }
        return false;
    }

    public void writeToFile(String data) {
        if(!Objects.equals(data,"#")) {
            if (isExternalStorageWritable() == true && isExternalStorageReadOnly() == false) {
                // Toast.makeText(getBaseContext(), "ExternalStorageAvailableandWritable", Toast.LENGTH_SHORT).show();
                File directory = getExternalFilesDir("/Data/");
                // Toast.makeText(getBaseContext(), directory.getAbsolutePath(), Toast.LENGTH_SHORT).show();
                File file = new File(directory, "rawdata.txt");

                if (directory.exists()) {
                    //  Toast.makeText(getBaseContext(), "Directory exists!", Toast.LENGTH_SHORT).show();
                    FileWriter outstream = null;
                    try {
                        outstream = new FileWriter(file, true);
                        outstream.write(data);
                        outstream.close();

                    } catch (IOException e) {
                        Toast.makeText(getBaseContext(), "File write failed", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getBaseContext(), "Directory doesn't exist", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getBaseContext(), "ExternalStorageNOTAvailable", Toast.LENGTH_SHORT).show();
            }
        }else{
            sendMessage();
           // onDestroy();
        }
    }

    private void sendMessage() {
        Intent intent = new Intent("raw data written to file");
        // You can also include some extra data.
        intent.putExtra("message", "Raw Data Written to File");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

 /*   @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent Data) {
        // Check which request we're responding to
        if (requestCode == REQUEST_ENABLE_BT) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                // The user picked a contact.
                // The Intent's data Uri identifies which contact was selected.
                Toast.makeText(getApplicationContext(), "Bluetooth turned on", Toast.LENGTH_SHORT).show();
            } else
                Toast.makeText(getBaseContext(), "Please Enable Bluetooth", Toast.LENGTH_SHORT).show();
        }
    } */

    public BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        return device.createRfcommSocketToServiceRecord(BTMODULEUUID);
        //creates secure outgoing connection with BT device using UUID
    }

    public class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1];  // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);
                    if (bytes != 0) {
                        final String strReceived = new String(buffer, 0, bytes);
                            Message msge = Message.obtain();
                            msge.obj = strReceived;
                            mHandler.obtainMessage(MESSAGE_READ, bytes, -1, msge.obj)
                                    .sendToTarget();
                    }
                } catch (IOException e) {
                    break;
                }
            }
        }

        // Call this from the main activity to send data to the remote device
        public void write(String input) {
            byte[] bytes = input.getBytes();           //converts entered String into bytes
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
            }
        }

        // Call this from the main activity to shutdown the connection
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            }
        }
    }

}
