package com.farameo.bt;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelUuid;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import static android.os.Build.VERSION_CODES.M;
import static com.farameo.bt.R.id.btnConectarse;

public class MainActivity extends AppCompatActivity {

    static final int REQUEST_ACTIVATION = 1;
    static final int REQUEST_CONECTION_BT = 2 ;
    static final int MESSAGE_READ = 3;
    private String MAC = null;

    private static final UUID UUID_BT = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    StringBuilder stringBuilder = new StringBuilder();

    Button button;
    BluetoothAdapter bluetoothAdapter;
    BluetoothDevice bluetoothDevice = null;
    BluetoothSocket bluetoothSocket = null;

    Boolean bConexion = false;

    UUID u;

    int nIncrementoLed1 = 0;
    int nIncrementoLed2 = 0;

    CadenaPorSocket cadenaPorSocket;
    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = (Button) findViewById(btnConectarse);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "Seu dispositivo nao possui bluetooth", Toast.LENGTH_SHORT).show();
        }else if (!bluetoothAdapter.isEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, REQUEST_ACTIVATION);
        }

        handler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                if (msg.what == MESSAGE_READ) {
                    String string = (String) msg.obj;
                    stringBuilder.append(string);

                    int fInformacion = stringBuilder.indexOf("}");

                    if (fInformacion > 0) {
                        String sComando = stringBuilder.substring(1 , fInformacion -1);
                        Log.i("BT_BT", sComando);
                        stringBuilder.delete(0, fInformacion);

                    }

                }
            }

        };
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

                    try {
                        bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(UUID_BT);
                        bluetoothSocket.connect();
                        bConexion = true;

                        cadenaPorSocket = new CadenaPorSocket(bluetoothSocket);
                        cadenaPorSocket.start();

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
        /*
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
            
            case btnLed1:
                Toast.makeText(this, "LED 1", Toast.LENGTH_SHORT).show();

                if (bConexion) {
                    cadenaPorSocket.enviar("led1");
                    Toast.makeText(this, "envaido", Toast.LENGTH_SHORT).show();
                }

                break;

            case btnLed2:
                if (bConexion) {
                    cadenaPorSocket.enviar("led2");
                    Toast.makeText(this, "envaido", Toast.LENGTH_SHORT).show();
                }
                break;

        }
        */

    }


    private class CadenaPorSocket extends Thread {
        private final InputStream inputStream;
        private final OutputStream outputStream;

        public CadenaPorSocket(BluetoothSocket socket) {

            InputStream inputStreamTmp = null;
            OutputStream outputStreamTmp = null;

            try {
                inputStreamTmp = socket.getInputStream();
                outputStreamTmp = socket.getOutputStream();
            } catch (IOException e) {
                Log.e("BT_BT", e.getMessage());

            }

            inputStream = inputStreamTmp;
            outputStream = outputStreamTmp;

        }

        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            while (true){
                try {
                    bytes = inputStream.read(buffer);
                    String string = new String(buffer, 0, bytes);
                    handler.obtainMessage(MESSAGE_READ, bytes, -1, string).sendToTarget();

                } catch (IOException e) {
                    break;

                }
            }
        }

        public void enviar(String string) {
            byte[] cadena = string.getBytes();
            try {
                outputStream.write(cadena);
                Log.e("BT_OK", cadena + "\n");
            } catch (IOException e) {
                Log.e("BT_BT", e.getMessage());

            }
        }

    }

}
