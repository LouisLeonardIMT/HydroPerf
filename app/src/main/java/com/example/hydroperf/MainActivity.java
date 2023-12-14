package com.example.hydroperf;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends Activity {

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_BLUETOOTH_PERMISSION = 2;

    private BluetoothAdapter bluetoothAdapter;
    private ArrayList<BluetoothDevice> devicesList = new ArrayList<>();
    private ArrayAdapter<String> listAdapter;
    private ConnectThread connectThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Demander la permission Bluetooth si elle n'est pas déjà accordée
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_BLUETOOTH_PERMISSION);
        } else {
            // La permission est déjà accordée, initialisez Bluetooth
            initializeBluetooth();
        }
    }

    @SuppressLint("MissingPermission")
    private void initializeBluetooth() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            // Le périphérique ne prend pas en charge Bluetooth
            return;
        }

        // Assurez-vous que le Bluetooth est activé
        if (!bluetoothAdapter.isEnabled()) {
            // Si le Bluetooth n'est pas activé, tentez de l'activer directement
            if (bluetoothAdapter.enable()) {
                // Le Bluetooth a été activé avec succès ou est en cours d'activation
                // Continuez avec l'initialisation de votre activité
                setupBluetoothUI();
            } else {
                // L'activation du Bluetooth a échoué
                // Vous pouvez gérer cela en conséquence
                Log.e("Bluetooth", "Échec de l'activation du Bluetooth");
            }
        } else {
            // Le Bluetooth est déjà activé, continuez avec l'initialisation de votre activité
            setupBluetoothUI();
        }
    }

    @SuppressLint("MissingPermission")
    private void setupBluetoothUI() {
        ListView listView = findViewById(R.id.listView);
        listAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        listView.setAdapter(listAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BluetoothDevice selectedDevice = devicesList.get(position);
                connectThread = new ConnectThread(selectedDevice);
                connectThread.start();
            }
        });

        Button searchButton = findViewById(R.id.searchButton);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchBluetoothDevices();
            }
        });

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    @SuppressLint("MissingPermission")
    private void searchBluetoothDevices() {
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        devicesList.clear();

        for (BluetoothDevice device : pairedDevices) {
            devicesList.add(device);
            listAdapter.add(device.getName() + "\n" + device.getAddress());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_BLUETOOTH_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission accordée, initialisez Bluetooth
                initializeBluetooth();
            } else {
                // L'utilisateur a refusé la permission, vous pouvez gérer cela en conséquence
                Log.e("Bluetooth", "Permission refusée");
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (connectThread != null) {
            connectThread.cancel();
        }
    }

    private class ConnectThread extends Thread {
        private BluetoothSocket socket;
        private final BluetoothDevice device;

        @SuppressLint("MissingPermission")
        public ConnectThread(BluetoothDevice device) {
            this.device = device;

            BluetoothSocket tmp = null;

            try {
                tmp = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
            } catch (IOException e) {
                Log.e("Bluetooth", "Erreur lors de la création de la socket", e);
            }
            socket = tmp;

            initiateBluetoothConnection();
        }

        @SuppressLint("MissingPermission")
        private void initiateBluetoothConnection() {
            try {
                socket.connect();
                // La connexion a réussi, faites ce que vous devez faire ici
            } catch (IOException connectException) {
                // Gestion des erreurs lors de la connexion
                try {
                    socket.close();
                } catch (IOException closeException) {
                    Log.e("Bluetooth", "Impossible de fermer la socket de connexion", closeException);
                }
            }
        }

        public void cancel() {
            try {
                socket.close();
            } catch (IOException e) {
                Log.e("Bluetooth", "Impossible de fermer la socket de connexion", e);
            }
        }
    }
}