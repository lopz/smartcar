package net.rocklabs.smartcar;

import java.io.IOException;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.io.OutputStream;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import android.widget.Button;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.Switch;


public class MainActiviy extends Activity {
    // Debugging
    private static final String TAG = "SmartCar";
    private static final String address = "00:13:03:26:13:56";
    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;

    // Unique UUID for this application
    private static final UUID MY_UUID_SECURE =
            UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
    private static final UUID MY_UUID_INSECURE =
            UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");

    private Button btnConnect;
    private Button btnEngine;
    private TextView txtNameBT;

    BluetoothAdapter btAdapter;
    BluetoothSocket btSocket;
    BluetoothDevice btDevice;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    ConnectAsyncTask connAsynTask = new ConnectAsyncTask();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_activiy);

        btnConnect = (Button)findViewById(R.id.btnConnet);
        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btAdapter = BluetoothAdapter.getDefaultAdapter();
                // If BT is not on, request that it be enabled.
                // setupChat() will then be called during onActivityResult
                if (!btAdapter.isEnabled()) {
                    Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
                    // Otherwise, setup the chat session
                } else {
                    connectDevice();
                }
            }
        });

        btnEngine = (Button)findViewById(R.id.btnEngine);
        btnEngine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*OutputStream mOutStream;
                try{
                    if (btSocket.isConnected()){
                        byte[] code = "01".getBytes();
                        mOutStream = btSocket.getOutputStream();
                        mOutStream.write(code);
                    }
                } catch (IOException e){}*/


            }
        });
    }


    private class ConnectAsyncTask extends AsyncTask<BluetoothDevice, Integer, BluetoothSocket>{
        private BluetoothDevice mDevice;
        private BluetoothSocket mSocket;

        @Override
        protected BluetoothSocket doInBackground(BluetoothDevice... device){
            mDevice = device[0];
            try {
                mSocket = mDevice.createInsecureRfcommSocketToServiceRecord(MY_UUID_INSECURE);
                mSocket.connect();
            } catch (Exception e){
                Log.e(TAG, "Socket create() failed", e);
            }
            return  mSocket;
        }
        @Override
        protected void onPostExecute(BluetoothSocket result){
            btSocket = result;

        }
    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;
            mmDevice = device;

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app's UUID string, also used by the server code
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID_SECURE);
            } catch (IOException e) { }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it will slow down the connection
            btAdapter.cancelDiscovery();

            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and get out
                try {
                    mmSocket.close();
                } catch (IOException closeException) { }
                return;
            }

            // Do work to manage the connection (in a separate thread)
            //ConnectedThread manageConnectedThread = new ConnectedThread();
            //manageConnectedSocket(mmSocket);
            mConnectedThread = new ConnectedThread(mmSocket);
            mConnectedThread.start();
        }

        /** Will cancel an in-progress connection, and close the socket */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(byte[] bytes) {
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

    private void connectDevice() {
        Log.d(TAG, "connectDevice ");
        // Get the device MAC address
        //String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
        BluetoothDevice btDevice = btAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        //mChatService.connect(device, secure);
        Toast.makeText(this, "Device: " + btDevice.getName(), Toast.LENGTH_LONG).show();
        //connAsynTask.execute(btDevice);
        mConnectThread = new ConnectThread(btDevice);
        mConnectThread.start();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult " + resultCode);
        connectDevice();
        /*switch (requestCode) {
            connectDevice();
            case REQUEST_CONNECT_DEVICE_SECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice();
                }
                break;
            case REQUEST_CONNECT_DEVICE_INSECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice();
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    //setupChat();
                } else {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(this, "BT not enabled", Toast.LENGTH_SHORT).show();
                    finish();
                }
        }*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_activiy, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.e(TAG, "++ ON START ++");

        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        /*if (!btAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        } else {
            //Connected
        }*/
    }

    @Override
    protected void onResume(){
        super.onResume();
        Log.e(TAG, "+ ON RESUME +");

    }

    @Override
    protected void onDestroy(){
        super.onDestroy();

        if (btAdapter != null)
            btAdapter.cancelDiscovery();
        if (btSocket != null){
            try {
                btSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }
}
