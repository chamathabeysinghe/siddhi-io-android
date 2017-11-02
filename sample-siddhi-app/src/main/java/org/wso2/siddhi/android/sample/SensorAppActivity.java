/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.siddhi.android.sample;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;
import org.wso2.siddhi.android.platform.util.SiddhiAndroidException;

import java.util.ArrayList;
import java.util.List;

public class SensorAppActivity extends AppCompatActivity {

    private DataUpdateReceiver dataUpdateReceiver;

    private String inSteams[] = {
            "@source(type='android-accelerometer', @map(type='keyvalue',fail.on.missing.attribute" +
                    "='false',@attributes(sensor='sensor',vector='accelerationX')))" +
                    "define stream sensorInStream ( sensor string, vector float);",
            "@source(type='android-game-rotation', @map(type='keyvalue',fail.on.missing.attribute" +
                    "='false',@attributes(sensor='sensor',vector='rotationX')))" +
                    "define stream sensorInStream ( sensor string, vector float);",
            "@source(type='android-gravity', @map(type='keyvalue',fail.on.missing.attribute" +
                    "='false',@attributes(sensor='sensor',vector='gravityX')))" +
                    "define stream sensorInStream ( sensor string, vector float);",

            "@source(type='android-gyroscope', @map(type='keyvalue',fail.on.missing.attribute" +
                    "='false',@attributes(sensor='sensor',vector='rotationX')))" +
                    "define stream sensorInStream ( sensor string, vector float);",
            "@source(type='android-humidity', @map(type='keyvalue',fail.on.missing.attribute" +
                    "='false',@attributes(sensor='sensor',vector='humidity')))" +
                    "define stream sensorInStream ( sensor string, vector float);",
            "@source(type='android-light', @map(type='keyvalue',fail.on.missing.attribute" +
                    "='false',@attributes(sensor='sensor',vector='light')))" +
                    "define stream sensorInStream ( sensor string, vector float);",

            "@source(type='android-linear-accelerometer', @map(type='keyvalue'," +
                    "fail.on.missing.attribute='false'," +
                    "@attributes(sensor='sensor',vector='accelerationX')))" +
                    "define stream sensorInStream ( sensor string, vector float);",
            "@source(type='android-magnetic', @map(type='keyvalue',fail.on.missing.attribute" +
                    "='false',@attributes(sensor='sensor',vector='magneticX')))" +
                    "define stream sensorInStream ( sensor string, vector float);",
            "@source(type='android-pressure', @map(type='keyvalue',fail.on.missing.attribute" +
                    "='false',@attributes(sensor='sensor',vector='pressure')))" +
                    "define stream sensorInStream ( sensor string, vector float);",

            "@source(type='android-proximity', @map(type='keyvalue',fail.on.missing.attribute=" +
                    "'false',@attributes(sensor='sensor',vector='proximity')))" +
                    "define stream sensorInStream ( sensor string, vector float);",
            "@source(type='android-rotation', @map(type='keyvalue',fail.on.missing.attribute=" +
                    "'false',@attributes(sensor='sensor',vector='rotationX')))" +
                    "define stream sensorInStream ( sensor string, vector float);",
            "@source(type='android-temperature', @map(type='keyvalue',fail.on.missing.attribute=" +
                    "'false',@attributes(sensor='sensor',vector='temperature')))" +
                    "define stream sensorInStream ( sensor string, vector float);",
            "@source(type='android-steps', @map(type='keyvalue',fail.on.missing.attribute=" +
                    "'false',@attributes(sensor='sensor',vector='steps')))" +
                    "define stream sensorInStream ( sensor string, vector float);",
            "@source(type='android-location', @map(type='keyvalue',fail.on.missing.attribute" +
                    "='false',@attributes(lo='longitude',la='latitude')))" +
                    "define stream sensorInStream ( lo double, la double);",

    };
    private String inNames[] = {"Accelerometer", "GameRotation", "Gravity",
            "gyroscope", "humidity", "light",
            "linear accelerometer", "magnetic", "pressure",
            "proximity",
            "rotation", "temperature", "steps", "location"};

    private String broadcastIdentifier = "sample.siddhi.app";
    private String outStream[] = {
            "@sink(type='android-broadcast' , identifier='" + broadcastIdentifier + "', " +
                    "@map(type='keyvalue'))" +
                    "define stream outputStream (sensor string, vector float); ",
            "@sink(type='android-notification' , title='Details'," +
                    "multiple.notifications = 'true', @map(type='keyvalue'))" +
                    "define stream outputStream (sensor string, vector float); ",
            "@sink(type='android-sound',time = '5' , @map(type='keyvalue'))" +
                    "define stream outputStream (sensor string, vector float); "
    };
    private String outputStreamNames[] = {
            "Broadcast", "Notification", "Ringing",
    };
    private String startLine = "@app:name('bar')";
    private String endLine = "from sensorInStream select * insert into outputStream";
    private final int MY_PERMISSION_ACCESS_LOCATION = 0;
    private String runningAppName = "";

    private ListView listView;
    private List<String> messageList = new ArrayList<>();
    private ArrayAdapter<String> listAdapter;
    private Spinner spinner1;
    private Spinner spinner2;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_app);
        listView = findViewById(R.id.messageList);
        spinner1 = findViewById(R.id.spinner);
        spinner2 = findViewById(R.id.spinner2);

        listAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,
                messageList);
        listView.setAdapter(listAdapter);

        spinner1.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, inNames));
        spinner2.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, outputStreamNames));
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                (this, android.Manifest.permission.ACCESS_COARSE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.ACCESS_COARSE_LOCATION)) {
                Toast.makeText(this,
                        "Location permission given", Toast.LENGTH_SHORT).show();

            } else {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSION_ACCESS_LOCATION);

                Toast.makeText(this,
                        "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_ACCESS_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Log.e("Location", "Permission given");

                } else {
                    Log.e("Location", "Permission not given");
                }
                return;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (dataUpdateReceiver == null) {
            dataUpdateReceiver = new DataUpdateReceiver();
        }
        IntentFilter intentFilter = new IntentFilter(broadcastIdentifier);
        registerReceiver(dataUpdateReceiver, intentFilter);
    }

    /**
     * Send the app stream to SiddhiService
     *
     * @param view
     */
    public void sendApp(View view) { //change name

        if (!runningAppName.equalsIgnoreCase("")) {
            stopApp(null);
        }
        try {
            int inIndex = spinner1.getSelectedItemPosition();
            int outIndex = spinner2.getSelectedItemPosition();

            String app = startLine + inSteams[inIndex] + outStream[outIndex] + endLine;
            Log.e("App",app);

//            This is for testing the WSO2Con app Suho asked for remove after testing
//
//            String testApp = "@app:name('foo')\n" +
//                    "@source(type='android-proximity',polling.interval='500', @map(type='keyvalue',fail.on.missing.attribute='false',@attributes(sensor='sensor',value='proximity')))\n" +
//                    "define stream sensorInStream (sensor string,value float);\n" +
//                    "@sink(type='android-sound' , play.time='5',@map(type='keyvalue'))\n" +
//                    "define stream outputStream (sensor string,value float);\n" +
//                    "from sensorInStream#window.timeBatch(20 sec)\n" +
//                    "select sensor, max(value) as value group by sensor having value < 5 insert into outputStream;";
//
//            app =testApp;

            String appName = MainActivity.comman.startSiddhiApp(app);
            this.runningAppName = appName;
            if (appName != null) {
                Toast.makeText(this, "Send the query : ", Toast.LENGTH_LONG).show();

            }
        } catch (RemoteException e) {
            Log.e("SampleApp", Log.getStackTraceString(e));
        } catch (SiddhiAndroidException e) {
            Toast.makeText(this, "Error in creating siddhi app",
                    Toast.LENGTH_SHORT).show();
            Log.e("Error", Log.getStackTraceString(e));
        }
    }

    /**
     * stop a running siddhi app
     *
     * @param view
     */
    public void stopApp(View view) {
        try {
            MainActivity.comman.stopSiddhiApp(runningAppName);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Broadcast receiver to get intents from the Siddhi Service
     * Has a hardcoded intent filter to match the query
     */
    private class DataUpdateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(broadcastIdentifier)) {
                messageList.add(intent.getStringExtra("sensor") + " : " +
                        String.valueOf(intent.getFloatExtra("vector", 0)));
//                messageList.add(intent.getDoubleExtra("lo",0)+" : "+String.valueOf(intent.getDoubleExtra("la",0)));
                listAdapter.notifyDataSetChanged();
            }
        }
    }
}
