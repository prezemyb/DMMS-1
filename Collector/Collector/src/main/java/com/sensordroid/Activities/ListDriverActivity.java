package com.sensordroid.Activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.sensordroid.Driver;
import com.sensordroid.R;
import com.sensordroid.RegisterReceiver;

import io.fabric.sdk.android.Fabric;
import java.util.ArrayList;

public class ListDriverActivity extends Activity {
    private int driverCount;
    MyCustomAdapter dataAdapter = null;
    ArrayList<Driver> drivers;

    BroadcastReceiver driverReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("driverReceiver", " - got intent");
            Driver driver = new Driver(intent.getStringExtra("ID"), driverCount++, false);
            displayView(driver);
            checkButtonClick();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_list);

        drivers = new ArrayList<>();
        driverCount = 0;

        Button networkButton = (Button)findViewById(R.id.buttonConfigure);
        networkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent configure = new Intent(ListDriverActivity.this, ConfigurationActivity.class);
                startActivity(configure);
            }
        });


        sendBroadcast(new Intent("com.sensordroid.HELLO"));
    }

    private void displayView(Driver driver) {
        // Arraylist of drivers
        drivers.add(driver);
        //create an ArrayAdaptar from the String Array
        dataAdapter = new MyCustomAdapter(this,
                R.layout.driver_info, drivers);
        ListView listView = (ListView) findViewById(R.id.listView1);
        // Assign adapter to ListView
        listView.setAdapter(dataAdapter);


    }
    public class MyCustomAdapter extends ArrayAdapter<Driver>{
        private ArrayList<Driver> driverList;

        public MyCustomAdapter(Context context, int textViewResourceId,
                               ArrayList<Driver> driverList) {
            super(context, textViewResourceId, driverList);
            this.driverList = new ArrayList<Driver>();
            this.driverList.addAll(driverList);
        }

        private class ViewHolder {
            TextView code;
            CheckBox name;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder holder = null;
            Log.v("ConvertView", String.valueOf(position));

            if (convertView == null) {
                LayoutInflater vi = (LayoutInflater)this.getContext().getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                convertView = vi.inflate(R.layout.driver_info, null);

                holder = new ViewHolder();
                holder.code = (TextView) convertView.findViewById(R.id.code);
                holder.name = (CheckBox) convertView.findViewById(R.id.checkBox1);
                convertView.setTag(holder);

                holder.name.setOnClickListener( new View.OnClickListener() {
                    public void onClick(View v) {
                        CheckBox cb = (CheckBox) v ;
                        Driver country = (Driver) cb.getTag();
                        country.setSelected(cb.isChecked());
                    }
                });
            }
            else {
                holder = (ViewHolder) convertView.getTag();
            }

            Driver driver = driverList.get(position);
            holder.code.setText(" ("+ driver.getId() + ") ");
            holder.name.setText(driver.getName());
            holder.name.setChecked(driver.isSelected());
            holder.name.setTag(driver);

            return convertView;
        }

    }

    private void checkButtonClick() {
        Button myButton = (Button) findViewById(R.id.findSelected);
        myButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                StringBuffer responseText = new StringBuffer();
                responseText.append("The following were selected...\n");
                ArrayList<String> driverArrayList = new ArrayList<>();


                ArrayList<Driver> driverList = dataAdapter.driverList;
                for(int i=0;i<driverList.size();i++){
                    Driver driver = driverList.get(i);
                    if(driver.isSelected()){
                        driverArrayList.add(driver.getName());
                        responseText.append("\n" + driver.getName());
                    }
                }

                Toast.makeText(getApplicationContext(),
                        responseText, Toast.LENGTH_LONG).show();
                Intent intent = new Intent(ListDriverActivity.this, MainActivity.class);
                intent.putStringArrayListExtra("DRIVERS", driverArrayList);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver((driverReceiver),
                new IntentFilter(RegisterReceiver.REGISTER_ACTION)
        );
    }

    @Override
    protected void onStop() {
        unregisterReceiver(driverReceiver);
        super.onStop();
    }
}
