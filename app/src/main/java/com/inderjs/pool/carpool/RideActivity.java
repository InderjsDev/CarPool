package com.inderjs.pool.carpool;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class RideActivity extends AppCompatActivity {

    private Button mEndBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride);
        setTitle("Enjoy Your Ride");


        mEndBtn = (Button)findViewById(R.id.endBtn);

        mEndBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent bill = new Intent(RideActivity.this, ReceiptActivity.class);

                startActivity(bill);
            }
        });
    }
}
