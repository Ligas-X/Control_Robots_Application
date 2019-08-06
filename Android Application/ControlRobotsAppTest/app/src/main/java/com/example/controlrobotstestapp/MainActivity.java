package com.example.controlrobotstestapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import java.util.Set;
import java.util.ArrayList;
import android.widget.Toast;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.TextView;
import android.content.Intent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

public class MainActivity extends AppCompatActivity {
    Button buttonPair;
    ListView deviceList;
    TextView inscription, logo;
    LinearLayout mainLayout, deviceListLayout, pairButtonLayout;

    private BluetoothAdapter myBluetooth = null;
    private Set<BluetoothDevice> pairedDevices;
    public static String EXTRA_ADDRESS = "device_address";

    Animation upToDown, downToUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        buttonPair = (Button) findViewById(R.id.buttonPairBT);
        deviceList = (ListView) findViewById(R.id.deviceList);
        inscription = (TextView) findViewById(R.id.inscription);
        logo = (TextView) findViewById(R.id.logoTextView);
        mainLayout = (LinearLayout) findViewById(R.id.mainLinearLayout);
        deviceListLayout = (LinearLayout) findViewById(R.id.deviceListLayout);
        pairButtonLayout = (LinearLayout) findViewById(R.id.pairButtonLayout);

        inscription.setText("Нажмите на кнопку для подключения!");

        myBluetooth = BluetoothAdapter.getDefaultAdapter();
        if (myBluetooth == null) {
            Toast.makeText(getApplicationContext(), "Bluetooth Device Not Available", Toast.LENGTH_LONG).show();
            finish();
        } else {
            if (myBluetooth.isEnabled()) {
            } else {
                Intent turnBTon = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(turnBTon, 1);
            }
        }

        upToDown = AnimationUtils.loadAnimation(MainActivity.this, R.anim.uptodown);
        downToUp = AnimationUtils.loadAnimation(MainActivity.this, R.anim.downtoup);

        logo.setAnimation(upToDown);
        deviceListLayout.setAnimation(upToDown);
        pairButtonLayout.setAnimation(downToUp);

        buttonPair.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //inscription.setVisibility(View.VISIBLE);
                inscription.setText("Список Bluetooth-устройств:");
                pairedDevicesList(); // method that will be called
            }
        });
    }

    private void pairedDevicesList() {
        pairedDevices = myBluetooth.getBondedDevices();
        ArrayList list = new ArrayList();

        if (pairedDevices.size() > 0) {
            for (BluetoothDevice bt : pairedDevices) {
                list.add(bt.getName() + "\n" + bt.getAddress()); // Get the device's name and the address
            }
        } else {
            Toast.makeText(getApplicationContext(), "No Paired Bluetooth Devices Found.", Toast.LENGTH_LONG).show();
        }

        final ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, list);
        deviceList.setAdapter(adapter);
        deviceList.setOnItemClickListener(myListClickListener); // Method called when the device from the list is clicked
    }

    private AdapterView.OnItemClickListener myListClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView av, View v, int arg2, long arg3) {
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);
            Intent i = new Intent(MainActivity.this, LedControl.class);
            i.putExtra(EXTRA_ADDRESS, address); // this will be received at ledControl (class) Activity
            startActivity(i);
        }
    };

}
