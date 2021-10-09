package com.example.detectmonitor;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class LogActivity extends AppCompatActivity {

    private SqliteHelper sqlite_helper;
    private SQLiteDatabase database;
    private String table_name = "Device_Record";

    private String[] DeviceName;
    private String[] DeviceTime;
    private ListView log_list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.log);

        sqlite_helper = new SqliteHelper(this, "Device_Log", null, 1, table_name);
        database = sqlite_helper.getWritableDatabase();
        get_list_data_and_show();

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @SuppressLint("ResourceType")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // R.menu.mymenu is a reference to an xml file named mymenu.xml which should be inside your res/menu directory.
        // If you don't have res/menu, just create a directory named "menu" inside res
        getMenuInflater().inflate(R.layout.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // action bar return button function
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:     // return button
                LogActivity.this.finish();
                return true;

            case R.id.trash:            // trash button
                AlertDialog.Builder builder = new AlertDialog.Builder(LogActivity.this);
                builder.setTitle("APP通知你");
                builder.setMessage("是否清除資料？");
                builder.setNegativeButton("是", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        database.execSQL("DELETE FROM " + table_name + " WHERE _id <> 0");
                        log_list.setAdapter(null);
                        dialogInterface.dismiss();
                    }
                });

                builder.setPositiveButton("否", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });

                builder.create().show();
                break;

            case R.id.refresh:          // refresh button
                get_list_data_and_show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        database.close();
    }

    public void get_list_data_and_show(){
        Cursor database_cursor = database.rawQuery("SELECT * FROM " + table_name,null);
        DeviceName = new String[database_cursor.getCount()];
        DeviceTime = new String[database_cursor.getCount()];
        for(int i=0;i<database_cursor.getCount();i++){
            database_cursor.moveToNext();
            DeviceName[i] = database_cursor.getString(1);
            DeviceTime[i] = database_cursor.getString(2);
            System.out.println(database_cursor.getString(1) + " + " + database_cursor.getString(2));
        }

        List_Layout_Adapter adapter = new List_Layout_Adapter(this);
        log_list = (ListView)findViewById(R.id.LogList);
        log_list.setAdapter(adapter);
    }

    public class List_Layout_Adapter extends BaseAdapter{

        private LayoutInflater List_Inflater;

        public List_Layout_Adapter(Context context){
            this.List_Inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return DeviceName.length;
        }

        @Override
        public Object getItem(int position) {
            return DeviceName[position] + "," + DeviceTime[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = List_Inflater.inflate(R.layout.log_item, null);
            TextView TV_DeviceName = (TextView) convertView.findViewById(R.id.DeviceName);
            TextView TV_DeviceTime = (TextView) convertView.findViewById(R.id.DeviceTime);

            TV_DeviceName.setText(DeviceName[position]);
            TV_DeviceTime.setText(DeviceTime[position]);
            return convertView;
        }
    }
}


