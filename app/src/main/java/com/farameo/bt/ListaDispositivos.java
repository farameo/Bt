package com.farameo.bt;

import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;

import static android.os.Build.VERSION_CODES.M;

/**
 * Created by goosfancito on 08/09/17.
 */

public class ListaDispositivos extends ListActivity {

    private BluetoothAdapter bluetoothAdapterLista;
    static String MAC = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        bluetoothAdapterLista = BluetoothAdapter.getDefaultAdapter();

        Set<BluetoothDevice> dispositivosPareados = bluetoothAdapterLista.getBondedDevices();
        if (dispositivosPareados.size() > 0) {
            for (BluetoothDevice dispositivo: dispositivosPareados) {
                String nombreBT = dispositivo.getName();
                String macBT = dispositivo.getAddress();
                arrayAdapter.add(nombreBT + "\n" + macBT);
            }
            setListAdapter(arrayAdapter);
        }

    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        String texto = ((TextView) v).getText().toString();

        //Toast.makeText(getApplicationContext(), texto, Toast.LENGTH_SHORT).show();

        String MACRetornado = texto.substring(texto.length()-17);
        Intent intent = new Intent();
        intent.putExtra(MAC, MACRetornado);

        setResult(RESULT_OK, intent);
        finish();
    }
}
