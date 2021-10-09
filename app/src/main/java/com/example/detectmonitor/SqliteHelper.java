package com.example.detectmonitor;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.Date;

public class SqliteHelper extends SQLiteOpenHelper {

    private String table_name = "";

    public SqliteHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version, String TableName) {
        super(context, name, null, version);
        this.table_name = TableName;
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL("CREATE TABLE IF NOT EXISTS " + this.table_name + " (" +
                         "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                         "Device text not null," +
                         "Time TEXT not null" +
                         ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        database.execSQL("DROP TABLE " + this.table_name);
    }

    public void insert_data(SQLiteDatabase database, String Device_name){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
        Date result_date = new Date(System.currentTimeMillis());

        ContentValues contentValues = new ContentValues();
        contentValues.put("Device", Device_name);
        contentValues.put("Time", sdf.format(result_date));
        System.out.println(Device_name + " / " + sdf.format(result_date));
        database.insert(table_name,null, contentValues);
    }
}
