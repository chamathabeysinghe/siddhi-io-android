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

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;
import org.wso2.siddhi.android.platform.SiddhiAppController;
import org.wso2.siddhi.android.platform.SiddhiAppService;

public class MainActivity extends AppCompatActivity {

    public static SiddhiAppController comman;
    private ServiceConnection serviceCon = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            comman = SiddhiAppController.Stub.asInterface(iBinder);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toast.makeText(this, "Binding to the service", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, SiddhiAppService.class);
        startService(intent);
        bindService(intent, serviceCon, BIND_AUTO_CREATE);
    }

    public void startSensorApp(View view){
        Intent intent= new Intent(this,SensorAppActivity.class);
        startActivity(intent);
    }

    public void startCustomApp(View view){
        Intent intent = new Intent(this,CustomAppActivity.class);
        startActivity(intent);
    }
}
