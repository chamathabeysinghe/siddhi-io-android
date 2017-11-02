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

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.daimajia.swipe.adapters.BaseSwipeAdapter;
import org.wso2.siddhi.android.sample.database.AppDbContract;
import org.wso2.siddhi.android.sample.database.SiddhiAppDBHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CustomAppActivity extends AppCompatActivity {
    private static final int READ_REQUEST_CODE = 42;
    private SiddhiAppDBHelper siddhiAppDBHelper = null;
    private View mainView;
    private List<String> appList;
    private ListView listView;
    private GridViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_app);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mainView = findViewById(R.id.main_view);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                performFileSearch();

            }
        });
        siddhiAppDBHelper = new SiddhiAppDBHelper(this);

        this.appList = readAppFromDatabase();

        this.listView = findViewById(R.id.listView);
        this.adapter = new GridViewAdapter(this, appList);
        this.listView.setAdapter(this.adapter);


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();
                String inputStr = readTextFromUri(uri);
                for (String app : inputStr.split("###")) {
                    writeAppToDatabase(app);
                }
                this.appList = readAppFromDatabase();
                this.adapter = new GridViewAdapter(this,appList);
                this.listView.setAdapter(this.adapter);
                Snackbar.make(mainView, "Apps were saved", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        }
    }

    private String readTextFromUri(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    inputStream));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            return stringBuilder.toString();
        } catch (IOException e) {
            Snackbar.make(null, "Could not read file", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
        return null;
    }

    private void writeAppToDatabase(String appDefiniton) {
        SQLiteDatabase db = siddhiAppDBHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(AppDbContract.SiddhiAppEntry.COLUMN_NAME_APP_DEFINITION, appDefiniton);
        db.insert(AppDbContract.SiddhiAppEntry.TABLE_NAME, null, contentValues);
    }

    private List<String> readAppFromDatabase() {
        SQLiteDatabase db = siddhiAppDBHelper.getReadableDatabase();
        String[] projection = {AppDbContract.SiddhiAppEntry.COLUMN_NAME_APP_DEFINITION};

        Cursor cursor = db.query(AppDbContract.SiddhiAppEntry.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                null);

        List<String> items = new ArrayList<>();
        while (cursor.moveToNext()) {
            String appDefinition = cursor.getString(cursor.getColumnIndex(
                    AppDbContract.SiddhiAppEntry.COLUMN_NAME_APP_DEFINITION));
            items.add(appDefinition);
        }
        return items;
    }

    /**
     * Fires an intent to spin up the "file chooser" UI and select an image.
     */
    public void performFileSearch() {

        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/*");
        startActivityForResult(intent, READ_REQUEST_CODE);
    }

    private class GridViewAdapter extends BaseSwipeAdapter {

        private Context mContext;
        private List<String> appList;
        private List<Boolean> isRunning;

        GridViewAdapter(Context mContext, List<String> appList) {
            this.mContext = mContext;
            this.appList = appList;
            this.isRunning = new ArrayList<>();
            for (int x = 0; x < appList.size(); x++) {
                this.isRunning.add(false);
            }
        }

        @Override
        public int getSwipeLayoutResourceId(int position) {
            return R.id.sample2;
        }

        @Override
        public View generateView(int position, ViewGroup parent) {
            return LayoutInflater.from(mContext).inflate(R.layout.list_view_item, null);
        }

        @Override
        public void fillValues(int position, View convertView) {
            LinearLayout layout = convertView.findViewById(R.id.bottom_wrapper);
            TextView appTextView = convertView.findViewById(R.id.textView);
            appTextView.setText(appList.get(position));
            TextView actionTextView = convertView.findViewById(R.id.textView2);
            layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isRunning.get(position)) {
                        if (stopApp(position)) {
                            actionTextView.setText("Start App");
                            layout.setBackgroundColor(CustomAppActivity.this.getResources().getColor(R.color.runSiddhiApp));
                            isRunning.set(position, false);
                            Toast.makeText(CustomAppActivity.this,"App Stopped",Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(CustomAppActivity.this, "Can't stop the app", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        if (startApp(position)) {
                            actionTextView.setText("Stop App");
                            layout.setBackgroundColor(CustomAppActivity.this.getResources().getColor(R.color.stopSiddhiApp));
                            isRunning.set(position, true);
                            Toast.makeText(CustomAppActivity.this,"App started",Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(CustomAppActivity.this, "Can't execute the app", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
        }

        private boolean startApp(int position) {

            try {
                String s = MainActivity.comman.startSiddhiApp(appList.get(position));
                return s != null;
            } catch (RemoteException e) {
                Toast.makeText(null, "Error in executing Siddhi app",
                        Toast.LENGTH_SHORT).show();
            }
            return false;

        }

        private boolean stopApp(int position) {

            try {
                String app = appList.get(position);
                String appName = extractAppName(app);
                MainActivity.comman.stopSiddhiApp(appName);
                return true;
            } catch (RemoteException e) {
                Toast.makeText(null, "Error in executing siddhi app",
                        Toast.LENGTH_SHORT).show();
            }
            return false;

        }

        private String extractAppName(String app){
            Pattern pattern = Pattern.compile("(?<=app:name\\(').*?(?='\\))");
            Matcher matcher = pattern.matcher(app);
            if(matcher.find()){
                return matcher.group(0);
            }
            return null;
        }

        @Override
        public int getCount() {
            return appList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }
    }
}

