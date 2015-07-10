package com.example.anton.speedometer;

import android.app.Activity;
import android.content.res.AssetManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;


public class SpeedometerAppActivity extends ActionBarActivity {

    public static final int updateTime = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speedometer_app);

        Button btnOk = (Button)findViewById(R.id.okButton);
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SpeedometerView speedometerView = (SpeedometerView)findViewById(R.id.speedometrView);
                EditText        textEditorView = (EditText)findViewById(R.id.velocityText);
                Integer         newValue = null;

                try {
                    newValue = Integer.valueOf(textEditorView.getText().toString());
                }
                catch (NumberFormatException e){
                    newValue = new Integer(0);
                }

                speedometerView.setVelocity(newValue.intValue());
            }
        });

        startAnimateThread();
    }

    private void startAnimateThread()
    {
        final TextView speedText = (TextView) findViewById(R.id.textView);
        final SpeedometerView spView = (SpeedometerView) findViewById(R.id.speedometrView);
        final Activity currActivity = this;

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while(true) {
                        currActivity.runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                spView.invalidate();
                                speedText.setText("Velocity: " + (int)spView.getVelocity());
                            }
                        });

                        Thread.sleep(updateTime);
                    }
                } catch (InterruptedException e) {
                }
            }
        }).start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_speedometer_app, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
