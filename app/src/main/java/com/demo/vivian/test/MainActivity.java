package com.demo.vivian.test;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends ActionBarActivity implements View.OnClickListener {

    private Button btn_Zero, btn_one;
    private Chart mChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn_Zero = (Button) findViewById(R.id.val_zero);
        btn_one = (Button) findViewById(R.id.val_zero);
        mChart = (Chart) findViewById(R.id.view);
        btn_Zero.setOnClickListener(this);
        btn_one.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.val_zero:
                Log.i("info", "you click zero");
                mChart.setValue(1);
                break;
            case R.id.val_one:
                Log.i("info", "you click one");
                mChart.setValue(0);
                break;
            default:
                break;
        }
    }
}
