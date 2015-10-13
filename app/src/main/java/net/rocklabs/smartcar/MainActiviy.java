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
import android.bluetooth.BluetoothServerSocket;

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

    Button btnConnect;
    TextView txtNameBT;

    BluetoothAdapter btAdapter;
    BluetoothSocket btSocket;
    BluetoothDevice btDevice;
    Set<BluetoothDevice> pairedDevices;
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
                if(!btAdapter.isEnabled()) {
                    Intent intentBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(intentBluetooth, 0);
                }
                //if (btAdapter.getState()) {
                    btDevice = btAdapter.getRemoteDevice("00:13:03:26:13:56");
                    Toast.makeText(getApplication(), "Name: " + btDevice.getName(), Toast.LENGTH_LONG).show();
                //}
                /*pairedDevices = btAdapter.getBondedDevices();
                if(pairedDevices.size() > 0){
                    for (BluetoothDevice device : pairedDevices){
                        txtNameBT.setText(device.getName());

                    }
                }*/

                connAsynTask.execute(btDevice);

                OutputStream mOutStream = null;
                try{
                    if (btSocket.isConnected()){
                        mOutStream = btSocket.getOutputStream();
                        mOutStream.write("01".getBytes());
                    }
                } catch (IOException e){}
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
                String mUUID = "00001101-0000-1000-8000-00805F9B34FB";
                mSocket = mDevice.createInsecureRfcommSocketToServiceRecord(UUID.fromString(mUUID));
                mSocket.connect();
            } catch (Exception e){}
            return  mSocket;
        }
        @Override
        protected void onPostExecute(BluetoothSocket result){
            btSocket = result;

        }
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
    protected void onResume(){
        super.onResume();
        Log.e(TAG, "+ ON RESUME +");

    }

    @Override
    protected void onDestroy(){
        super.onDestroy();

        if (btAdapter != null)
            btAdapter.cancelDiscovery();
        //this.unregisterReceiver();
    }
}
