package com.example.controlrobotstestapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.AsyncTask;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.os.Handler;

import android.util.Log;

import java.lang.String;

public class LedControl extends AppCompatActivity implements JoystickView.JoystickListener {
    public static final int UP = 1;
    public static final int UPRIGHT = 2;
    public static final int RIGHT = 3;
    public static final int DOWNRIGHT = 4;
    public static final int DOWN = 5;
    public static final int DOWNLEFT = 6;
    public static final int LEFT = 7;
    public static final int UPLEFT = 8;
    public static final int NONE = 9;

    public static final int POSITIVEDISTANCE = 6; // "мертвая зона" джойстика
    public static final int NEGATIVEDISTANCE = -6;

    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private boolean isBtConnected = false;
    private OutputStream outputStream;
    private ProgressDialog progress;
    private final int INPUT_ACTIVITY = 1;

    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;

    Button buttonDisconnect;
    LinearLayout joystickLayout;

    String address = null;

    String[] testData;

    JoystickView joystick;
    TextView xValueTextView, yValueTextView, degreeValueTextView, directionValueTextView;
    String xValue, yValue;


    public ConnectedThread mConnectedThread;
    private static final String TAG = "myLogs";
    Handler h;
    final int RECIEVE_MESSAGE = 1;
    private StringBuilder sb = new StringBuilder();
    final Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        joystick = new JoystickView(this);

        // receive the address of the bluetooth device
        Intent newint = getIntent();
        address = newint.getStringExtra(MainActivity.EXTRA_ADDRESS);

        // view of the ledControl layout
        setContentView(R.layout.activity_led_control);

        // call the widgtes
        buttonDisconnect = (Button) findViewById(R.id.buttonDisconnectBT);
        joystickLayout = (LinearLayout) findViewById(R.id.linearLayout);

        xValueTextView = (TextView) findViewById(R.id.xValue);
        yValueTextView = (TextView) findViewById(R.id.yValue);
        degreeValueTextView = (TextView) findViewById(R.id.degreeValue);
        directionValueTextView = (TextView) findViewById(R.id.directionValue);

        xValueTextView.setVisibility(View.GONE); // INVISIBLE, GONE
        yValueTextView.setVisibility(View.GONE);
        degreeValueTextView.setVisibility(View.GONE);
        directionValueTextView.setVisibility(View.GONE);

        //testData = new String[]{"0", "1", "1", "1", "9", "360", "360"}; // 11 байтов
        //testData = new String[]{"7", "99", "99"}; // 5 байтов
        //testData = new String[]{"7", "9"}; // 2 байтов
        testData = new String[]{"1", "1", "1", "9"}; // 4 байта

        new ConnectBT().execute(); // call the class to connect

        buttonDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Disconnect(); // close connection
            }
        });

        h = new Handler() {
            public void handleMessage(android.os.Message msg) {
                switch (msg.what) {
                    case RECIEVE_MESSAGE:
                        byte[] readBuf = (byte[]) msg.obj;
                        String strIncom = new String(readBuf, 0, msg.arg1);
                        sb.append(strIncom);
                        int endOfLineIndex = sb.indexOf("\r\n");
                        if (endOfLineIndex > 0) {
                            String sbprint = sb.substring(0, endOfLineIndex);
                            sb.delete(0, sb.length());
                        }
                        break;
                }
            }

            ;
        };
    }

    @Override
    public void onJoystickMoved(float xCenter, float yCenter,  float xPosition, float yPosition, float xPercent, float yPercent, int id) {
//        int xInt, yInt;
        double xDouble, yDouble, degree, xCenterJoystick, yCenterJoystick;
        int direction;

        xCenterJoystick = Math.round(xCenter) / 10;
        yCenterJoystick = Math.round(yCenter) / 10;

//        xInt = (Math.round(xPosition) / 10) - 51;
//        yInt = (Math.round(yPosition) / 10) - 51;

        xDouble = (Math.round(xPosition) / 10) - xCenterJoystick;
        yDouble = (Math.round(yPosition) / 10) - yCenterJoystick;

//        xValue = Float.toString(xPosition);
//        yValue = Float.toString(yPosition);
//        xValueTextView.setText("X position: " + xValue);
//        yValueTextView.setText("Y position: " + yValue);

        degree = calculatingDegrees(xDouble, yDouble);
        direction = getDirection(degree, xDouble, yDouble, POSITIVEDISTANCE, NEGATIVEDISTANCE);

        xValueTextView.setText("X position: " + xDouble);
        yValueTextView.setText("Y position: " + yDouble);
        degreeValueTextView.setText("Degree: " + degree);
        //directionValueTextView.setText("Direction: " + direction);

//        xValue = Double.toString(xInt);
//        yValue = Double.toString(yInt);

//        xValue = Integer.toString(Math.round((int)xDouble));
//        yValue = Integer.toString(Math.round((int)yDouble));

        // преобразование значений координат в тип string
        xValue = Double.toString(Math.round(xDouble));
        yValue = Double.toString(Math.round(yDouble));

//        testData[1] = xValue;
//        testData[2] = yValue;

        //testData[1] = Integer.toString(direction); // old 2 bytes
        testData[3] = Integer.toString(direction);

//        try {
//            btSocket.getOutputStream().write(testData[0].getBytes());
//            btSocket.getOutputStream().write(testData[1].getBytes());
//            //btSocket.getOutputStream().write(testData[2].getBytes());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mConnectedThread.write(testData[0] + testData[1] + testData[2] + testData[3] + ",");
            }
        }, 10);

    }

    private double calculatingDegrees(double x, double y) {

        double calcDegree = 0;
        double degreeValue = 0;

        if (x >= 0 && y < 0) // если точка в первой четверти
        {
            calcDegree = Math.toDegrees(Math.atan(x / y)); // вычисляем радианы, переводим в градусы и округляем
            degreeValue = calcDegree + 90; // прибавляем 90 к полученному значению
        } else if (x < 0 && y < 0) // если точка во второй четверти
        {
            calcDegree = Math.toDegrees(Math.atan(x / y));
            degreeValue = calcDegree + 90; // прибавляем 90 к полученному значению
        } else if (x < 0 && y >= 0) // если точка в третьей четверти
        {
            calcDegree = Math.toDegrees(Math.atan(x / y));
            degreeValue = calcDegree + 270; // прибавляем 270 к полученному значению
        } else if (x >= 0 && y >= 0) // если точка в четвертой четверти
        {
            calcDegree = Math.toDegrees(Math.atan(x / y));
            degreeValue = calcDegree + 270; // прибавляем 270 к полученному значению
        }
        return degreeValue;
    }

    public int getDirection(double degree, double x, double y, int positiveDistance, int negativeDistance) {
        int direction = 0;

        if ((x < positiveDistance && x > negativeDistance) && (y < positiveDistance && y > negativeDistance)) // "мертвая зона" джойстика
        {
            direction = NONE;
            directionValueTextView.setText("Direction: NONE");
        } else {
            if (degree >= 67.5 && degree <= 112.5) // UP
            {
                direction = UP;
                directionValueTextView.setText("Direction: UP");
            } else if (degree > 22.5 && degree < 67.5) // UPRIGHT
            {
                direction = UPRIGHT;
                directionValueTextView.setText("Direction: UPRIGHT");
            } else if (degree > 112.5 && degree < 157.5) // UPLEFT
            {
                direction = UPLEFT;
                directionValueTextView.setText("Direction: UPLEFT");
            } else if (degree >= 337.5 || degree <= 22.5) // RIGHT
            {
                direction = RIGHT;
                directionValueTextView.setText("Direction: RIGHT");
            } else if (degree >= 157.5 && degree <= 202.5) // LEFT
            {
                direction = LEFT;
                directionValueTextView.setText("Direction: LEFT");
            } else if (degree >= 247.5 && degree <= 292.5) // DOWN
            {
                direction = DOWN;
                directionValueTextView.setText("Direction: DOWN");
            } else if (degree > 292.5 && degree < 337.5) // DOWNRIGHT
            {
                direction = DOWNRIGHT;
                directionValueTextView.setText("Direction: DOWNRIGHT");
            } else if (degree > 202.5 && degree < 247.5) // DOWNLEFT
            {
                direction = DOWNLEFT;
                directionValueTextView.setText("Direction: DOWNLEFT");
            }
        }
        return direction;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.led_control, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.control_methods: // для пункта меню «Метод управления»
                //receiveControlMethod(item);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void receiveControlMethod(MenuItem item) {
        Intent newAct = new Intent(getApplicationContext(), ControlMethods.class);
        // startActivity(newAct); // для простого вызова активити
        startActivityForResult(newAct, INPUT_ACTIVITY); // для вызова активити с результатом
        // второй параметр нужен для отличия результатов при нескольких дополнительных результатов
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) { // оператор выбора для определения, от какого активити пришел результат
            case INPUT_ACTIVITY: // идентификатор нашего активити ввода нового языка
                // создаем строку newItemTwo, в которую записываем результаты из второго окна
                // далее переносим данные в массив testData
                String[] newItem = data.getStringArrayExtra("newItem");
                testData[0] = newItem[0];
                testData[1] = newItem[1];
                testData[2] = newItem[2];
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    private void messageShow(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
    }

    private void Disconnect() {
        String command;

        if (btSocket != null) // if the btSocket is busy
        {
            // отправляем символ d в последывательный порт (для стабилизации вывода след. массивов)
            command = "d";
            try {
                btSocket.getOutputStream().write(command.getBytes());
            } catch (IOException e1) {
                messageShow("Error");
            }

            try {
                btSocket.close(); // close connection
            } catch (IOException e) {
                messageShow("Error");
            }
        }
        finish(); // return to the first layout
    }

    private class ConnectBT extends AsyncTask<Void, Void, Void> // UI thread
    {
        private boolean ConnectSuccess = true; // if it's here, it's almost connected

        @Override
        protected void onPreExecute() {
            progress = ProgressDialog.show(LedControl.this, "Connecting...", "Please wait!!!"); // show a progress
            // dialog
        }

        @Override
        protected Void doInBackground(Void... devices) // while the progress dialog is shown, the connection is done in
        // background
        {
            try {
                if (btSocket == null || !isBtConnected) {
                    myBluetooth = BluetoothAdapter.getDefaultAdapter(); // get the mobile bluetooth device
                    BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(address); // connects to the device's
                    // address and checks if it's
                    // available
                    btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID); // create a RFCOMM (SPP)
                    // connection
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect(); // start connection
                }
            } catch (IOException e) {
                ConnectSuccess = false; // if the try failed, you can check the exception here
            }

            mConnectedThread = new ConnectedThread(btSocket);
            mConnectedThread.start();

            return null;
        }

        @Override
        protected void onPostExecute(Void result) // after the doInBackground, it checks if everything went fine
        {
            super.onPostExecute(result);

            if (!ConnectSuccess) {
                messageShow("Connection Failed. Is it a SPP Bluetooth? Try again.");
                finish();
            } else {
                messageShow("Connected.");
                isBtConnected = true;
            }
            progress.dismiss();
        }
    }

    private class ConnectedThread extends Thread {
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
            Log.d(TAG, "On constructor");
        }

        public void run() {
            byte[] buffer = new byte[256]; // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer); //read bytes from input buffer
                    // Send the obtained bytes to the UI Activity via handler
                    h.obtainMessage(RECIEVE_MESSAGE, bytes, -1, buffer).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(String message) {
            Log.d(TAG, "...Посылаем данные: " + message + "...");
            byte[] msgBuffer = message.getBytes(); // converts entered String into bytes
            try {
                mmOutStream.write(msgBuffer); // write bytes over BT connection via outstream
            } catch (IOException e) {
                Log.d(TAG, "...ошибка отправки данных: " + e.getMessage() + "...");
            }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            }
        }
    }


}
