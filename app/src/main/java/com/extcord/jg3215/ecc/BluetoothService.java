package com.extcord.jg3215.ecc;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.UUID;

import static android.content.ContentValues.TAG;

public class BluetoothService extends Service {

    private BluetoothAdapter mBTAdapter;
    private ConnectedThread mConnectedThread; // bluetooth background worker thread to send and receive data
    private BluetoothSocket mBTSocket = null; // bi-directional client-to-client data path
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // "random" unique identifier

    private int OrangeThreshold;
    private int RedThreshold;
    private int PowerThreshold;

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
        LocalBroadcastManager.getInstance(this).registerReceiver(DisconnectMessageReceiver,
                new IntentFilter("DisconnectBluetooth"));

        File directory = getExternalFilesDir("/");
        File file = new File(directory,"Thresholds.txt");

        try {
            if(file.length()!=0) {
                FileReader fileReader = new FileReader(file);
                // Always wrap FileReader in BufferedReader.
                BufferedReader bufferedReader = new BufferedReader(fileReader);
                OrangeThreshold = Integer.parseInt(bufferedReader.readLine());
                RedThreshold = Integer.parseInt(bufferedReader.readLine());
                PowerThreshold = Integer.parseInt(bufferedReader.readLine());
                bufferedReader.close(); //Closes file.
            }else{
                OrangeThreshold = 5;
                RedThreshold = 10;
                PowerThreshold = 15;
            }
        }catch (IOException e) {
            toastAnywhere("Failed to read Thresholds");}


        mBTAdapter = BluetoothAdapter.getDefaultAdapter(); // get a handle on the bluetooth radio
        new Thread()
        {
            public void run() {
                boolean fail = false;

                BluetoothDevice device = mBTAdapter.getRemoteDevice(MainActivity.deviceAddress);

                try {
                    mBTSocket = createBluetoothSocket(device);
                } catch (IOException e) {
                    fail = true;
                    // Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_SHORT).show();
                }
                // Establish the Bluetooth socket connection.
                try {
                    mBTSocket.connect();
                } catch (IOException e) {
                    try {
                        fail = true;
                        mBTSocket.close();
                        toastAnywhere("Connection Failed");
                        CouldntConnectMessage();
                    } catch (IOException e2) {
                        //insert code to deal with this
                        Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_SHORT).show();
                    }
                }
                if(!fail) {
                    mConnectedThread = new BluetoothService.ConnectedThread(mBTSocket);
                    mConnectedThread.start();
                    toastAnywhere("Connected to Bluetooth!");
                    ConnectedMessage();
                }
            }
        }.start();
    }

    private void ConnectedMessage() {
        Intent intent = new Intent("ConnectedBluetooth");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);


    }

    private void CouldntConnectMessage() {
        Intent intent = new Intent("CouldntConnect");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private BroadcastReceiver DisconnectMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mConnectedThread.cancel();
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
            LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mMessageReceiver1,
                    new IntentFilter("OrangeThresholdChanged"));
            LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mMessageReceiver2,
                    new IntentFilter("RedThresholdChanged"));
            LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mMessageReceiver3,
                    new IntentFilter("PowerThresholdChanged"));

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

        private void sendCurrent(String current, String color){
            Intent intent = new Intent("CurrentReceived");
            intent.putExtra("Current", current);
            intent.putExtra("Color", color);
            LocalBroadcastManager.getInstance(BluetoothService.this).sendBroadcast(intent);
        }

        private BroadcastReceiver mMessageReceiver1 = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String newThreshold = intent.getExtras().getString("Threshold");
                OrangeThreshold = Integer.parseInt(newThreshold);
                write(newThreshold);
                write("O");
            }
        };

        private BroadcastReceiver mMessageReceiver2 = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String newThreshold = intent.getExtras().getString("Threshold");
                RedThreshold = Integer.parseInt(newThreshold);
                write(newThreshold);
                write("R");
            }
        };

        private BroadcastReceiver mMessageReceiver3 = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String newThreshold = intent.getExtras().getString("Threshold");
                PowerThreshold = Integer.parseInt(newThreshold);
                write(newThreshold);
                write("P");
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
                            Currentstring = Currentstring + strReceived;
                        }else {
                            int current = Integer.parseInt(Currentstring);
                            if (current < OrangeThreshold) {
                                sendCurrent(Currentstring, "green");
                            }
                            else if (current >= OrangeThreshold && current < RedThreshold) {
                                //Checks whether the app is in the foreground. If it is, it sends a message to the mainactivity
                                //to display the current and its color. If it isnt, it shows a notification to alert the user
                                if(Foreground.get().isForeground()) {
                                    sendCurrent(Currentstring, "orange");
                                }else{
                                    //Code to send notification. Only the two lines with comments need to be changed
                                    NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext(), "notify_001");
                                    Intent ii = new Intent(getApplicationContext(), MainActivity.class);
                                    PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, ii, 0);
                                    mBuilder.setContentIntent(pendingIntent);
                                    mBuilder.setSmallIcon(R.drawable.notification_icon);
                                    //Title of notification
                                    mBuilder.setContentTitle("The Current Has Passed the Orange Threshold !");
                                    //Message of the notification, which is the value in Amps
                                    mBuilder.setContentText(Currentstring + " Amps");
                                    mBuilder.setPriority(Notification.PRIORITY_MAX);

                                    NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                        NotificationChannel channel = new NotificationChannel("notify_001",
                                                "Channel human readable title",
                                                NotificationManager.IMPORTANCE_DEFAULT);
                                        mNotificationManager.createNotificationChannel(channel);
                                    }

                                    mNotificationManager.notify(0, mBuilder.build());
                                }
                            } else if (current >= RedThreshold && current < PowerThreshold) {
                                if(Foreground.get().isForeground()) {
                                    sendCurrent(Currentstring, "red");
                                }else{
                                    NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext(), "notify_001");
                                    Intent ii = new Intent(getApplicationContext(), MainActivity.class);
                                    PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, ii, 0);
                                    mBuilder.setContentIntent(pendingIntent);
                                    mBuilder.setSmallIcon(R.drawable.notification_icon);
                                    mBuilder.setContentTitle("Current has passed the Red Threshold !");
                                    mBuilder.setContentText(Currentstring + " Amps");
                                    mBuilder.setPriority(Notification.PRIORITY_MAX);

                                    NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                        NotificationChannel channel = new NotificationChannel("notify_001",
                                                "Channel human readable title",
                                                NotificationManager.IMPORTANCE_DEFAULT);
                                        mNotificationManager.createNotificationChannel(channel);
                                    }

                                    mNotificationManager.notify(0, mBuilder.build());
                                }
                            } else if (current > PowerThreshold) {
                                if(Foreground.get().isForeground()) {
                                    sendCurrent(Currentstring, "black");
                                }else{
                                    NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext(), "notify_001");
                                    Intent ii = new Intent(getApplicationContext(), MainActivity.class);
                                    PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, ii, 0);
                                    mBuilder.setContentIntent(pendingIntent);
                                    mBuilder.setSmallIcon(R.drawable.notification_icon);
                                    mBuilder.setContentTitle("Device Shutdown !");
                                    mBuilder.setContentText(Currentstring + " Amps");
                                    mBuilder.setPriority(Notification.PRIORITY_MAX);

                                    NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                        NotificationChannel channel = new NotificationChannel("notify_001",
                                                "Channel human readable title",
                                                NotificationManager.IMPORTANCE_DEFAULT);
                                        mNotificationManager.createNotificationChannel(channel);
                                    }

                                    mNotificationManager.notify(0, mBuilder.build());
                                }
                            }
                            Currentstring = null;
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
