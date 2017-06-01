package com.example.gwy.gkc4v2;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;


import static android.content.ContentValues.TAG;
import static android.provider.ContactsContract.CommonDataKinds.Website.URL;

public class LoginActivity extends Activity {

    // 帐号和密码
    private EditText edname;
    private EditText edpassword;
    private String url ="http://45.63.50.188/login/login";
    private Button btregister;
    private Button btlogin;
    private TextView tv_result;
    private TextView shuru;
    // 创建SQLite数据库
    public static SQLiteDatabase db;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        edname = (EditText) findViewById(R.id.edname);
        edpassword = (EditText) findViewById(R.id.edpassword);
        btregister = (Button) findViewById(R.id.btregister);
        btlogin = (Button) findViewById(R.id.btlogin);
        tv_result =  (TextView) findViewById(R.id.tv_result);
        shuru =  (TextView) findViewById(R.id.shuru);
        db = SQLiteDatabase.openOrCreateDatabase(LoginActivity.this.getFilesDir().toString()
                + "/test.dbs", null);
        // 跳转到注册界面
        Toast.makeText(getApplicationContext(), "欢迎登录",
                Toast.LENGTH_SHORT).show();

        btregister.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent intent = new Intent();
                intent.setClass(LoginActivity.this, RegistersActivity.class);
                startActivity(intent);
            }
        });
        btlogin.setOnClickListener(new LoginListener());
    }


    class LoginListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            String name = edname.getText().toString();
            final String password = edpassword.getText().toString();
            if (name.equals("") || password.equals("")) {
                // 弹出消息框
                new AlertDialog.Builder(LoginActivity.this).setTitle("错误")
                        .setMessage("帐号或密码不能空").setPositiveButton("确定", null)
                        .show();
            }
            else {
                login(name, password); // 调用loginByPost方法
                //isUserinfo(name, password);
            }
        }


        // 判断输入的用户是否正确
        public Boolean isUserinfo(String name, String pwd) {

            try{
                String str="select * from tb_user where name=? and password=?";
                Cursor cursor = db.rawQuery(str, new String []{name,pwd});
                if(cursor.getCount()<=0){
                    new AlertDialog.Builder(LoginActivity.this).setTitle("错误")
                            .setMessage("帐号或密码错误！").setPositiveButton("确定", null)
                            .show();
                    return false;
                }else{
                    new AlertDialog.Builder(LoginActivity.this).setTitle("正确")
                            .setMessage("成功登录").setPositiveButton("确定", null)
                            .show();
                    Intent intent = new Intent();
                    intent.setClass(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    return true;
                }

            }catch(SQLiteException e){
                createDb();
            }
            return false;
        }

    }

    // 创建数据库和用户表
    public void createDb() {
        db.execSQL("create table tb_user( name varchar(30) primary key,password varchar(30))");
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    private void login(final String name, String pwd)  {
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
                                tv_result.setText(result);
                                shuru.setText("登录成功");
                                Intent intent = new Intent();
                                intent.setClass(LoginActivity.this, chat.class);
                                startActivity(intent);
                            }else{
                                shuru.setText("登录失败");
                                tv_result.setText(result);
                            }
                        }
                    });
                } catch (IOException e) {

                    tv_result.setText("网络连接失败");
                }
            }
        }.start();

    }
}