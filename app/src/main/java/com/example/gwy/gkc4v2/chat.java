package com.example.gwy.gkc4v2;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.example.gwy.gkc4v2.ChatMessage.Type;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class chat extends Activity
{
    private Socket mSocket;
    private String url = "http://45.63.50.188"+"?sessionID="+HttpUtils.session;
    private NotificationManager manager;
    NotificationCompat.Builder notifyBuilder;

    private Button vbt1;
    /**
     * 展示消息的listview
     */
    private ListView mChatView;
    /**
     * 文本域
     */
    private EditText mMsg;
    /**
     * 存储聊天消息
     */
    private List<ChatMessage> mDatas = new ArrayList<ChatMessage>();
    /**
     * 适配器
     */
    private ChatMessageAdapter mAdapter;

    private Handler mHandler = new Handler()
    {
        public void handleMessage(android.os.Message msg)
        {
            ChatMessage from = (ChatMessage) msg.obj;
            mDatas.add(from);
            mAdapter.notifyDataSetChanged();
            mChatView.setSelection(mDatas.size() - 1);
        };
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main_chatting);
        vbt1 = (Button) findViewById(R.id.vobtn);
        initView();
        mAdapter = new ChatMessageAdapter(this, mDatas);
        mChatView.setAdapter(mAdapter);
        SpeechUtility.createUtility(this, SpeechConstant.APPID + "=59266fe9");

        Toast.makeText(getApplicationContext(), url,
                Toast.LENGTH_SHORT).show();
        manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);


        //使用 onNewMessage 来监听服务器发来的 "new message" 事件
        try {
            //1.初始化socket.io，设置链接
            mSocket = IO.socket(url);
        } catch (URISyntaxException e) {}
        mSocket.on("alarm", onNewMessage);
        mSocket.connect();


        vbt1.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                btnVoice();
            }
        });
    }

    public void onDestroy() {
        super.onDestroy();

        mSocket.disconnect();
        mSocket.off("alarm", onNewMessage);
    }

    private Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override

        public void call(final Object... args) {
            //主线程调用
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    //res.setText(args[0].toString());
                    Toast.makeText(chat.this, url, Toast.LENGTH_SHORT).show();

                    JSONObject data = (JSONObject) args[0];
                    String test1 = "null";
                    ;
                    try {
                        test1 = data.getString("test");

                    } catch (JSONException e) {
                        return;
                    }
                    simNotification(test1);
                    Toast.makeText(chat.this, test1, Toast.LENGTH_SHORT).show();

                }
            });
        }
    };

    public void simNotification(String tex) {

        Toast.makeText(this, "hha", Toast.LENGTH_LONG).show();
        //设置对应IMAGEVIEW的ID的资源图片

        notifyBuilder = new NotificationCompat.Builder(this)
                .setWhen(System.currentTimeMillis())// 通知产生的时间，会在通知信息里显示
                .setTicker("New message")
                .setPriority(Notification.PRIORITY_DEFAULT)// 设置该通知优先级
                .setOngoing(false)//不是正在进行的   true为正在进行  效果和.flag一样
            /*设置small icon*/
                .setSmallIcon(R.drawable.sheep)
            /*设置title*/
                .setContentTitle(tex)
            /*设置详细文本*/
                .setContentText(tex);

        manager.notify(100, notifyBuilder.build());
    }

    private void btnVoice() {
        RecognizerDialog dialog = new RecognizerDialog(this,null);
        dialog.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
        dialog.setParameter(SpeechConstant.ACCENT, "mandarin");
        dialog.setListener(new RecognizerDialogListener() {
            @Override
            public void onResult(RecognizerResult recognizerResult, boolean b) {
                printResult(recognizerResult);
            }
            @Override
            public void onError(SpeechError speechError) {
            }
        });
        dialog.show();
        Toast.makeText(this, "请开始说话", Toast.LENGTH_SHORT).show();
    }
    //回调结果：
    private void printResult(RecognizerResult results) {
        final String msg = parseIatResult(results.getResultString());
        // 自动填写地址
        if (TextUtils.isEmpty(msg)) {
            Toast.makeText(getApplicationContext(), "请说出恰当的信息",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        if (msg.equals("。" ) ||msg.equals("？" ) ) {
            Toast.makeText(getApplicationContext(), "识别成功",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        ChatMessage to = new ChatMessage(Type.OUTPUT, msg);
        to.setDate(new Date());
        mDatas.add(to);

        mAdapter.notifyDataSetChanged();
        mChatView.setSelection(mDatas.size() - 1);

        mMsg.setText("");

        // 关闭软键盘
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        // 得到InputMethodManager的实例
        if (imm.isActive()) {
            // 如果开启
            imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT,
                    InputMethodManager.HIDE_NOT_ALWAYS);
            // 关闭软键盘，开启方法相同，这个方法是切换开启与关闭状态的
        }

        new Thread() {
            public void run() {
                ChatMessage from = null;
                if (msg.contains("好") || msg.contains("h")) {
                    from = new ChatMessage(Type.INPUT, "你好啊~~~");
                } else if (msg.contains("PM") || msg.contains("pm")) {
                    from = new ChatMessage(Type.INPUT, "目前PM2.5的值为  null");
                } else if (msg.contains("空气")) {
                    from = new ChatMessage(Type.INPUT, "目前空气质量为  null");
                } else if (msg.contains("地图")) {
                    from = new ChatMessage(Type.INPUT, "显示地图");
                    Intent intent = new Intent();
                    intent.setClass(chat.this, web.class);
                    startActivity(intent);

                } else if (msg.contains("车")) {
                    from = new ChatMessage(Type.INPUT, "小车已经就绪，请发布指令。");
                } else if (msg.contains("蓝牙")) {
                    from = new ChatMessage(Type.INPUT, "请连接蓝牙。");
                } else if (msg.contains("前")) {
                    from = new ChatMessage(Type.INPUT, "小车前进！！！");
                } else if (msg.contains("后")) {
                    from = new ChatMessage(Type.INPUT, "小车后退！！！");
                } else if (msg.contains("左")) {
                    from = new ChatMessage(Type.INPUT, "小车左转<<<");
                } else if (msg.contains("右")) {
                    from = new ChatMessage(Type.INPUT, "小车右转>>>");
                } else if (msg.contains("重力")) {
                    from = new ChatMessage(Type.INPUT, "重力感应模式。");
                } else if (msg.contains("键")) {
                    from = new ChatMessage(Type.INPUT, "方向键控制模式。");
                } else if (msg.contains("语音")) {
                    from = new ChatMessage(Type.INPUT, "语音控制摸模式。");
                } else if (msg.contains("e") || msg.contains("t") || msg.contains("a") || msg.contains("o") || msg.contains("n")) {
                    from = new ChatMessage(Type.INPUT, "很抱歉，我只听得懂中文:(");
                }

                else {
                    from = new ChatMessage(Type.INPUT, "我听不懂");
                }

                Message message = Message.obtain();
                message.obj = from;
                readAnswer(from.getMsg());
                mHandler.sendMessage(message);
            }

            ;
        }.start();



    }
    public static String parseIatResult(String json) {
        StringBuffer ret = new StringBuffer();
        try {
            JSONTokener tokener = new JSONTokener(json);
            JSONObject joResult = new JSONObject(tokener);
            JSONArray words = joResult.getJSONArray("ws");
            for (int i = 0; i < words.length(); i++) {
                // 转写结果词，默认使用第一个结果
                JSONArray items = words.getJSONObject(i).getJSONArray("cw");
                JSONObject obj = items.getJSONObject(0);
                ret.append(obj.getString("w"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret.toString();
    }

    private void initView()
    {
        mChatView = (ListView) findViewById(R.id.id_chat_listView);
        mMsg = (EditText) findViewById(R.id.id_chat_msg);
        mDatas.add(new ChatMessage(Type.INPUT, "您好，有什么需要帮助的吗？"));
    }

    public void sendMessage(View view)
    {
        final String msg = mMsg.getText().toString();
        if (TextUtils.isEmpty(msg)) {
            Toast.makeText(getApplicationContext(), "请输入恰当的信息",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        ChatMessage to = new ChatMessage(Type.OUTPUT, msg);
        to.setDate(new Date());
        mDatas.add(to);

        mAdapter.notifyDataSetChanged();
        mChatView.setSelection(mDatas.size() - 1);

        mMsg.setText("");

        // 关闭软键盘
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        // 得到InputMethodManager的实例
        if (imm.isActive()) {
            // 如果开启
            imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT,
                    InputMethodManager.HIDE_NOT_ALWAYS);
            // 关闭软键盘，开启方法相同，这个方法是切换开启与关闭状态的
        }

        new Thread() {
            public void run() {
                ChatMessage from = null;
                if (msg.contains("好") || msg.contains("h")) {
                    from = new ChatMessage(Type.INPUT, "你好啊~~~");
                } else if (msg.contains("PM") || msg.contains("pm")) {
                    from = new ChatMessage(Type.INPUT, "目前PM2.5的值为  null");
                } else if (msg.contains("空气")) {
                    from = new ChatMessage(Type.INPUT, "目前空气质量为  null");
                } else if (msg.contains("地图")) {

                    from = new ChatMessage(Type.INPUT, "显示地图");
                    Intent intent = new Intent();
                    intent.setClass(chat.this, web.class);
                    startActivity(intent);
                } else if (msg.contains("车")) {
                    from = new ChatMessage(Type.INPUT, "小车已经就绪，请发布指令。");
                } else if (msg.contains("蓝牙")) {
                    from = new ChatMessage(Type.INPUT, "请连接蓝牙。");
                } else if (msg.contains("前")) {
                    from = new ChatMessage(Type.INPUT, "小车前进！！！");
                } else if (msg.contains("后")) {
                    from = new ChatMessage(Type.INPUT, "小车后退！！！");
                } else if (msg.contains("左")) {
                    from = new ChatMessage(Type.INPUT, "小车左转<<<");
                } else if (msg.contains("右")) {
                    from = new ChatMessage(Type.INPUT, "小车右转>>>");
                } else if (msg.contains("重力")) {
                    from = new ChatMessage(Type.INPUT, "重力感应模式。");
                } else if (msg.contains("键")) {
                    from = new ChatMessage(Type.INPUT, "方向键控制模式。");
                } else if (msg.contains("语音")) {
                    from = new ChatMessage(Type.INPUT, "语音控制摸模式。");
                } else if (msg.contains("操") || msg.contains("滚") || msg.contains("逼") || msg.contains("傻")) {
                    from = new ChatMessage(Type.INPUT, "请不要说脏话。");
                } else if (msg.contains("e") || msg.contains("t") || msg.contains("a") || msg.contains("o") || msg.contains("n")) {
                    from = new ChatMessage(Type.INPUT, "很抱歉，我只听得懂中文:(");
                } else {
                    from = new ChatMessage(Type.INPUT, "我听不懂，换一个话题吧:(");
                }

                Message message = Message.obtain();
                message.obj = from;
                readAnswer(from.getMsg());
                mHandler.sendMessage(message);
            }

            ;
        }.start();
    }
    public void readAnswer(String text) {
        SpeechSynthesizer mTts= SpeechSynthesizer.createSynthesizer(chat.this, null);
        mTts.setParameter(SpeechConstant.VOICE_NAME, "xiaoyan");
        mTts.setParameter(SpeechConstant.SPEED, "50");
        mTts.setParameter(SpeechConstant.VOLUME, "80");
        mTts.setParameter(SpeechConstant.ENGINE_TYPE,
                    SpeechConstant.TYPE_CLOUD);
        mTts.startSpeaking(text, null);

    }
}