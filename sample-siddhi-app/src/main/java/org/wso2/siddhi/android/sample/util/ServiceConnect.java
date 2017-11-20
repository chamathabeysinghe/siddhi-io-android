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

package org.wso2.siddhi.android.sample.util;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import org.wso2.siddhi.android.platform.SiddhiAppController;
import org.wso2.siddhi.android.platform.SiddhiAppService;

/**
 * Create a singleton service connection to SiddhiAndroidService.
 */
public class ServiceConnect {

    private static SiddhiAppController controller;
    private static ServiceConnection serviceCon = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            controller = SiddhiAppController.Stub.asInterface(iBinder);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            controller = null;
        }
    };

    private ServiceConnect() {
    }

    public static SiddhiAppController getServiceConnection(Context context) {
        if (controller != null) {
            return controller;
        }
        Intent intent = new Intent(context, SiddhiAppService.class);
        context.startService(intent);
        context.bindService(intent, serviceCon, context.BIND_AUTO_CREATE);
        return controller;
    }
}
