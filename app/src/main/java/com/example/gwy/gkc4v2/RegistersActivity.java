package com.example.gwy.gkc4v2;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;

public class RegistersActivity extends Activity {

    private EditText edname1;
    private EditText edpassword1;
    private Button btregister1;
    private Button back1;
    SQLiteDatabase db;
    private String url ="http://45.63.50.188/register/register";
    private TextView res1;
    private TextView res2;

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        db.close();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registers);
        res1 =  (TextView) findViewById(R.id.t1);
        res2 =  (TextView) findViewById(R.id.t2);

        edname1 = (EditText) findViewById(R.id.edname1);
        edpassword1 = (EditText) findViewById(R.id.edpassword1);
        btregister1 = (Button) findViewById(R.id.btregister1);
        btregister1.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                String name = edname1.getText().toString();
                String password = edpassword1.getText().toString();
                if (!(name.equals("") && password.equals(""))) {
                    regist(name, password);

                   /* if (addUser(name, password)) {
                        DialogInterface.OnClickListener ss = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                // TODO Auto-generated method stub
                                // 跳转到登录界面
                                Intent in = new Intent();
                                in.setClass(RegistersActivity.this,
                                        LoginActivity.class);
                                startActivity(in);
                                // 销毁当前activity
                                RegistersActivity.this.onDestroy();
                            }
                        };
                        new AlertDialog.Builder(RegistersActivity.this)
                                .setTitle("注册成功").setMessage("注册成功")
                                .setPositiveButton("确定", ss).show();

                    } else {
                        new AlertDialog.Builder(RegistersActivity.this)
                                .setTitle("注册失败").setMessage("注册失败")
                                .setPositiveButton("确定", null);
                    }
                    */
                } else {
                    new AlertDialog.Builder(RegistersActivity.this)
                            .setTitle("帐号密码不能为空").setMessage("帐号密码不能为空")
                            .setPositiveButton("确定", null);
                }

            }
        });

        back1 = (Button) findViewById(R.id.back);
        back1.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(RegistersActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

    }

    private void regist(final String name, String pwd)  {
        final String username = name;
        final String password = pwd;
        final String user ="{\"username\":\""+ username +"\", \"password\":\""+ password +"\"}";
        new Thread(){
            @Override
            public void run() {
                HttpUtils httpUtils = new HttpUtils();
                //转换为JSON
                //final String user ="{\"username\":\"sdfsdf\", \"password\":\"bbb\"}";
                try {
                    final String result = httpUtils.login(url,user );
                    //Log.d(TAG, "结果:" + result);
                    //更新UI,在UI线程中
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(result.contains("1")){
                                res1.setText(result);
                                res2.setText("注册成功");
                                Intent intent = new Intent();
                                intent.setClass(RegistersActivity.this, LoginActivity.class);
                                startActivity(intent);
                            }else{
                                res1.setText("注册失败");
                                res2.setText(result);
                            }
                        }
                    });
                } catch (IOException e) {

                    res1.setText("网络连接失败");
                }
            }
        }.start();

    }

    // 添加用户
    public Boolean addUser(String name, String password) {
        String str = "insert into tb_user values(?,?) ";
        LoginActivity main = new LoginActivity();
        db = SQLiteDatabase.openOrCreateDatabase(this.getFilesDir().toString()
                + "/test.dbs", null);
        main.db = db;
        try {
            db.execSQL(str, new String[] { name, password });
            return true;
        } catch (Exception e) {
            main.createDb();
        }
        return false;
    }

}