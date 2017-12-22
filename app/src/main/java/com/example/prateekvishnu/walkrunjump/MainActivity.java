package com.example.prateekvishnu.walkrunjump;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    static Button btn_record;
    static Button btn_convert;
    static Button btn_train;
    static EditText toast_text;
    static String database_name = "Group_assignment17";
    static String table_name = "activity_data";
    String database_location = Environment.getExternalStorageDirectory() + File.separator + "Android/Data/CSE535_ASSIGNMENT3" + File.separator + database_name;
    static SQLiteDatabase db;
    SensorManager sensorManager;
    Sensor acclnSensor;
    int accln_count1 = 1;
    int accln_count2 = 1;
    int accln_count3 = 1;

    long update = 0;
    float[] accln_x = new float[50];
    float[] accln_y = new float[50];
    float[] accln_z = new float[50];

    boolean flag_2 = false;
    boolean flag = false;
    static int id;
    long row=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_record = (Button) findViewById(R.id.button1);
        btn_convert = (Button) findViewById(R.id.button2);
        btn_train = (Button) findViewById(R.id.button3);
        toast_text = (EditText) findViewById(R.id.editText1);
        sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        acclnSensor= sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener((SensorEventListener) this,acclnSensor,SensorManager.SENSOR_DELAY_NORMAL);

        btn_record.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                create_table();
                flag_2 = true;
            }
        });

        btn_convert.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    convert_data();
                    Toast.makeText(MainActivity.this,"Database Converted",Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        btn_train.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,TrainActivity.class);
                startActivity(intent);
            }
        });


    }

    public void create_table() {
        try {
            File folder = new File(Environment.getExternalStorageDirectory() + "/Android/Data/CSE535_ASSIGNMENT3");
            if (!folder.exists()) {
                folder.mkdir();
            }
            db = SQLiteDatabase.openOrCreateDatabase(database_location, null);
            db.beginTransaction();
            try {
                String sqlCreateTable = "create table " + table_name + "(id integer primary key autoincrement,X";
                for (int i = 1; i <= 50; i++) {
                    if (i == 50) {
                        sqlCreateTable += Integer.toString(i) + " float,Y";
                        sqlCreateTable += Integer.toString(i) + " float,Z";
                        sqlCreateTable += Integer.toString(i) + " float,label varchar(20));";
                    } else {
                        sqlCreateTable += Integer.toString(i) + " float,Y";
                        sqlCreateTable += Integer.toString(i) + " float,Z";
                        sqlCreateTable += Integer.toString(i) + " float,X";
                    }

            }
                db.execSQL(sqlCreateTable);
                db.setTransactionSuccessful();

            }
            catch (SQLiteException e) {
                e.printStackTrace();
            }
            finally {
                db.endTransaction();
            }
        } catch (SQLException e) {

            Toast.makeText(this, "Error Creating DB - Check Permissions", Toast.LENGTH_LONG).show();
        }
    }


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;
        long currtime = System.currentTimeMillis();
        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER && flag_2) {
            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];
            float z = sensorEvent.values[2];

            if ((currtime - update > 100) && accln_count3 <= 51) {
                update = currtime;
                accln_x[accln_count3 -1] = x;
                accln_y[accln_count3 -1] = y;
                accln_z[accln_count3 -1] = z;
                accln_count3++;

            }

            if (flag) {
                accln_count1 = 1;
                accln_count2 = 1;
                accln_count3 = 1;
                flag = false;
            }

            if (accln_count3 >= 51) {
                for (int i = 1; i <= 50; i++) {
                    set_table();
                    flag = true;
                }
                Toast.makeText(MainActivity.this, "Table Created", Toast.LENGTH_LONG).show();
                flag_2 = false;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, acclnSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }


    public void set_table() {
        String input = toast_text.getText().toString();
        db = SQLiteDatabase.openOrCreateDatabase(database_location, null);
        db.beginTransaction();

        try {
            if (accln_count2 == 1) {
                ContentValues values = new ContentValues();
                values.put("label",input);
                values.put("x1", accln_x[accln_count1 -1]);
                values.put("y1", accln_y[accln_count1 -1]);
                values.put("z1", accln_z[accln_count1 -1]);
                row = db.insert(table_name, null, values);
                accln_count2++;
            }
            else
            {
                String Update = "UPDATE " + table_name
                        + " SET "
                        + "x"+ accln_count1 + " = " + accln_x[accln_count1 -1] + ", "
                        + "y"+ accln_count1 + " = " + accln_y[accln_count1 -1] + ", "
                        + "z"+ accln_count1 + " = " + accln_z[accln_count1 -1]
                        + " WHERE ID = " + row;
                db.execSQL(Update);
            }
            accln_count1++;
        }
        finally {
            db.setTransactionSuccessful();
            db.endTransaction();
        }
    }


    public void convert_data() throws IOException {
        db=SQLiteDatabase.openOrCreateDatabase(database_location,null);
        File filewrite = new File(Environment.getExternalStorageDirectory() + "/Android/Data/CSE535_ASSIGNMENT3","database.txt");
        Cursor cursor = null;
        String sqlQuery = "select MAX(id) from "+ table_name;
        cursor = db.rawQuery(sqlQuery,null);
        cursor.moveToFirst();
        id = cursor.getInt(0);
        FileWriter writer=new FileWriter(filewrite);
        for(int i = 0; i < id; i++)
        {
            String output = "";
            String sqlquery = "Select * from "+table_name+" where id="+(i+1);
            cursor = db.rawQuery(sqlquery,null);
            cursor.moveToFirst();
            String labels = cursor.getString(151);

            if(labels.equalsIgnoreCase("walking"))
                output = "+1 ";
            else if(labels.equalsIgnoreCase("running"))
                output = "+2 ";
            else if(labels.equalsIgnoreCase("jumping"))
                output = "+3 ";



            for(int j = 1;j <= 150;j++)
                output += j+":"+cursor.getFloat(j)+" ";

            output.trim();
            output += "\n";

            writer.append(output);
            writer.flush();

        }
        writer.close();
    }


}