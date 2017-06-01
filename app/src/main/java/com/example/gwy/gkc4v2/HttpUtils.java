package com.example.gwy.gkc4v2;

/**
 * Created by Gork on 2017/5/25.
 */
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Administrator on 2016-03-27.
 */
public class HttpUtils {
    public static String session;
    OkHttpClient client = new OkHttpClient();

    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    public String login(String url, String json) throws IOException {
        //把请求的内容字符串转换为json
        RequestBody body = RequestBody.create(JSON, json);
        //RequestBody formBody = new FormEncodingBuilder()

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        Response response = client.newCall(request).execute();

       String result = response.body().string();
        Headers header= response.headers();
        session = header.get("set-cookie");
        session = session.substring(16,48);

        return result;


    }

    public String bolwingJson(String username, String password) {
        return "{'username':" + username + ","+"'password':"+password+"}";

        //     "{'username':" + username + ","+"'password':"+password+"}";
    }


}