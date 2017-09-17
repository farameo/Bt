package com.farameo.bt;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.ParcelUuid;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.util.UUID;

import static android.os.Build.VERSION_CODES.M;
import static com.farameo.bt.R.id.btnBuscar;

public class MainActivity extends AppCompatActivity {
    static final int REQUEST_ACTIVATION = 1;
    static final int REQUEST_CONECTION_BT = 2 ;
    private String MAC = null;
    private static final UUID UUID_BT = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    Button button;
    BluetoothAdapter bluetoothAdapter;
    BluetoothDevice bluetoothDevice = null;
    BluetoothSocket bluetoothSocket = null;

    Boolean bConexion = false;

    UUID u;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = (Button) findViewById(btnBuscar);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "Seu dispositivo nao possui bluetooth", Toast.LENGTH_SHORT).show();
        }else if (!bluetoothAdapter.isEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, REQUEST_ACTIVATION);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode) {
            case REQUEST_ACTIVATION:
                if (resultCode == RESULT_OK)
                    button.setEnabled(true);
                else {
                    button.setEnabled(false);
                    finish();
                }

                break;
            case REQUEST_CONECTION_BT:

                if (resultCode == Activity.RESULT_OK) {
                    MAC = data.getExtras().getString(ListaDispositivos.MAC);
                    bluetoothDevice = bluetoothAdapter.getRemoteDevice(MAC);
                    /*
                    ParcelUuid[] uuids = bluetoothDevice.getUuids();
                    Log.i("BT_BT", uuids[0].getUuid().toString());
                    */

                    try {
                        bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(UUID_BT);
                        bluetoothSocket.connect();
                        bConexion = true;
                        button.setText("Desconectar");
                        Toast.makeText(this, "conectado con " + UUID_BT, Toast.LENGTH_SHORT).show();
                    } catch (IOException error) {
                        bConexion = false;
                        button.setText("Conectar");
                        Toast.makeText(this, "No se pudo conectar con " +UUID_BT, Toast.LENGTH_SHORT).show();
                        Log.i("BT_BT", error.getMessage());
                        bConexion = true;
                    }
                } else {
                    Toast.makeText(this, "No se ha seleccionado", Toast.LENGTH_SHORT).show();
                }
                
        }

    }

    public void ocAcciones(View v) {
        switch (v.getId()) {
            case btnBuscar :
                if (bConexion) {
                    try {
                        bluetoothSocket.close();
                        bConexion = false;
                        button.setText("Conectar");
                    } catch (IOException error) {
                        Log.e("BT_BT", error.getMessage());
                    }
                } else {
                    Intent intent = new Intent(MainActivity.this, ListaDispositivos.class);
                    startActivityForResult(intent, REQUEST_CONECTION_BT);
                }


                break;
        }
    }

    public void buscar() {
        
    }
}