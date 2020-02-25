package com.example.mainactivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import android.widget.BaseAdapter;
import android.content.Context;

import com.polidea.rxandroidble2.RxBleClient;
import com.polidea.rxandroidble2.RxBleDevice;

import io.reactivex.disposables.Disposable;


public class MainActivity extends AppCompatActivity implements SensorEventListener, View.OnClickListener {

    private static final String TAG = "MyActivity";

    TextView viewX, viewY, viewZ,deviceText;
    Button startButton, stopButton,buttonBluetooth;

    //sensors from sensor manager
    private SensorManager sensorManager;
    private Sensor accelerometer;


    // dont know the mac of my device hostname is VR
    // private String deviceaddress = ("4A:21:25:DD:B3:2D");

    //  hostname is Mindo
  //  private String deviceaddress = ("DF:4A:07:7E:13:92");
  //  public UUID[] serviceUuidArray = {UUID.fromString("0000A012-0000-1000-8000-00805F9B34FB"),UUID.fromString("0000fff3-0000-1000-8000-00805F9B34FB")};




    //private String deviceaddress = ("FC:57:1A:B7:22:AD");
    //public final static String ACTION_DATA_AVAILABLE = "de.example.BluetoothLETest.ACTION_DATA_AVAILABLE";
/*
    public static final UUID Accel_SERVICE_UUID = UUID.fromString("A012");
    public static final UUID Accel_X_CHAR_UUID = UUID.fromString("A013");
    public static final UUID Accel_Y_CHAR_UUID = UUID.fromString("A014");
    public static final UUID Accel_Z_CHAR_UUID = UUID.fromString("A015");
    public static final UUID Joystick_SERVICE_UUID = UUID.fromString("fff3");
    public static final UUID Joystick_X_CHAR_UUID = UUID.fromString("0001");
    public static final UUID Joystick_Y_CHAR_UUID = UUID.fromString("0002");
    public static final UUID Joystick_Z_CHAR_UUID = UUID.fromString("0003");
    public String[] serviceUuidArray = {"A012","fff3"};
    BluetoothManager bluetoothManager;
    BluetoothAdapter bluetoothAdapter;
    String bluetoothDeviceAddress;
    BluetoothGatt bluetoothGatt;
    int connectionState;
    final  int STATE_DISCONNECTED = 0;
    final  int STATE_CONNECTING = 1;
    final  int STATE_CONNECTED = 2;
    final String ACTION_GATT_CONNECTED = "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    final String ACTION_GATT_DISCONNECTED = "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    final  String ACTION_GATT_SERVICES_DISCOVERED = "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    final  String ACTION_DATA_AVAILABLE = "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    final  String EXTRA_DATA = "com.example.bluetooth.le.EXTRA_DATA";
*/
    public static final UUID Accel_X_CHAR_UUID = UUID.fromString("0000A013-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startButton = findViewById(R.id.buttonStart);
        startButton.setOnClickListener(this);


        stopButton = findViewById(R.id.buttonStop);
        stopButton.setOnClickListener(this);

        buttonBluetooth = findViewById(R.id.buttonBluetooth);
        buttonBluetooth.setOnClickListener(this);

        viewX = findViewById(R.id.viewX);
        viewX.setText("0.0");
        viewY = findViewById(R.id.viewY);
        viewY.setText("0.0");
        viewZ = findViewById(R.id.viewZ);
        viewZ.setText("0.0");
        deviceText = findViewById(R.id.deviceText);
        deviceText.setText("0.0");



        //instantiate object of sensor manager and accelerometer to be used by the application
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        //get accelerometer sensor from sensor manager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);



    }

    public void setupBluetooth() {

        RxBleClient rxBleClient = RxBleClient.create(MainActivity.this);
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        int REQUEST_ENABLE_BT = 1;
        MainActivity.this.startActivityForResult(enableBtIntent,REQUEST_ENABLE_BT);
        Log.d("scan","request BT access");

        String macAddress = "DF:4A:07:7E:13:92";
        RxBleDevice device = rxBleClient.getBleDevice(macAddress);

        Disposable disposable = device.establishConnection(false) // <-- autoConnect flag
                .subscribe(
                        rxBleConnection -> {
                            // All GATT operations are done through the rxBleConnection.
                            Log.d("scan","request BT access"+ device.getConnectionState());
                            Log.d("scan","name "+ device.getName());
                            Log.d("scan","connected");
                            readChars(device);

                        },
                        throwable -> {
                            // Handle an error here.
                        }
                );

       //disposable.dispose();
    }
public void readChars(RxBleDevice device){

    device.establishConnection(false)
            .flatMapSingle(rxBleConnection -> rxBleConnection.readCharacteristic(Accel_X_CHAR_UUID))
            .subscribe(
                    characteristicValue -> {
                        // Read characteristic value.

                        Log.d("scan","accel X value " + characteristicValue.toString());
                    },
                    throwable -> {
                        // Handle an error here.
                    }
            );

}








    @Override
    public void onClick(View v){
        switch (v.getId()){
            case R.id.buttonStart: {
                //register the accelerometer with the sensor manager
                if (sensorManager != null) {
                    sensorManager.registerListener(this, accelerometer, sensorManager.SENSOR_DELAY_NORMAL);
                }

                break;
            }
            case R.id.buttonStop:
            {
                sensorManager.unregisterListener(this);
            }
            case R.id.buttonBluetooth:
            {
                setupBluetooth();
            }

        }
    }











    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            // get values for each axes X,Y,Z
            Log.d("NEIL","Collecting sensor values");
            float X = event.values[0];
            float Y = event.values[1];
            float Z = event.values[2];
            Log.d("NEIL","Logging sensor values" + "\n" + X + "\n" + Y + "\n" + Z);

            float alpha = (float) 0.8;

            float[] gravity = new float[3];
            gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
            gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
            gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

            float[] linear_acceleration = new float[3];
            linear_acceleration[0] = event.values[0] - gravity[0];
            linear_acceleration[1] = event.values[1] - gravity[1];
            linear_acceleration[2] = event.values[2] - gravity[2];

            // set value on the screen
            String textX = Float.toString(linear_acceleration[0]);
            String textY = Float.toString(linear_acceleration[1]);
            String textZ = Float.toString(linear_acceleration[2]);

            viewX.setText(textX);
            viewY.setText(textY);
            viewZ.setText(textZ);

            StringBuilder sb =  new StringBuilder();
            sb.append(textX+"/").append(textY+"/").append(textZ+"/");

            String convertedMessage = sb.toString();
            System.out.println("CONVERTED MESSAGE" + convertedMessage);
            Log.d("CONVERTED MESSAGE" ,convertedMessage);

            int port = 5555;

            try {
                DatagramSocket udpSocket = new DatagramSocket(port);
                SendMessage(convertedMessage,udpSocket);
            } catch (SocketException e) {
                e.printStackTrace();
            }

        }
    }
    public void SendMessage(String message, DatagramSocket udpSocket ) {

        int port = 5555;
        String ipAddr = "192.168.0.137";
        try {
            byte[] sendBuffer = message.getBytes();
            InetAddress serverAddr = InetAddress.getByName(ipAddr);
            DatagramPacket packet = new DatagramPacket( sendBuffer, sendBuffer.length,serverAddr,port);
            udpSocket.send(packet);
            Log.d("SENT" , "Sending Packet "+ packet.getData());
        }
        catch(SocketException socketEx){
            Log.e("UDP:" , "Socket Error", socketEx);
        }
        catch(IOException ioEx){
            Log.e("UDP Send:" , "Input Output Error", ioEx);
        }
    }

    @Override
    //this is an abstract method in the SensorEventListener , it must be implemented
    // but its not used in this application
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }


}