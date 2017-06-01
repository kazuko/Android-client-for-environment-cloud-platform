package com.example.gwy.gkc4v2;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    private Button bt1;
    private Button bt2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bt1 = (Button) findViewById(R.id.button1);
        bt2 = (Button) findViewById(R.id.button2);
        bt1.setOnClickListener(new View.OnClickListener()
        {
                public void onClick(View v)
                {
                    Intent in = new Intent();
                    in.setClass(MainActivity.this,
                            chat.class);
                    startActivity(in);
                }
        });
        bt2.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                Intent in = new Intent();
                in.setClass(MainActivity.this,
                        LoginActivity.class);
                startActivity(in);
            }
        });

    }
}
