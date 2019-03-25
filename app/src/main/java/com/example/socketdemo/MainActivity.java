package com.example.socketdemo;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.os.StrictMode;

import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import static com.example.socketdemo.R.*;

public class MainActivity extends AppCompatActivity implements SocketThread.OnClientListener {
//登录所需

    private SessionManager session;

    //发消息所需
    private SocketThread socketThread;
    private StringBuilder stringBuilder = new StringBuilder();
    private TextView serviceTv;
    private EditText contentEt;
    private Button sendBtn;
    private Spinner spinner;
    public TextView text_interest;
    private ArrayAdapter<String> adapter;
    private String[] list_interest = { "food", "knowledage","entertainment", "sport" };
    private String tempintrest = "knowledage";
    private Bundle MsavedInstanceState;
    private boolean ifsame = true;
    String intrest;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        MsavedInstanceState = savedInstanceState;
        super.onCreate(savedInstanceState);

        session = new SessionManager(this);


        if(session.isantologin())      //选择了自动登录 直接登陆
        {}
        else {                         //没选则去登陆
            if (session.isLoggedIn())  //已通过登录过 并且选择自动登录 不用登陆
            {
            } else if (!session.isLoggedIn()) //没通过登录
            {
                // Redirect the user to the login activity
                System.out.println("进入到了登录界面");
                Intent intent = new Intent(getApplication(), Start.class);
                startActivity(intent);
                finish();                      //不起作用？
                //System.exit(0);         //太猛了 后面的可运行 但是输出没了
                android.os.Process.killProcess(android.os.Process.myPid());  //好用
                System.out.println("Main没有被结束");
            }
        }
        System.out.println("自动登录:"+session.isantologin()+"  isLogin:"+session.isLoggedIn()+"  登陆成功");
        session.setLogin(false);

        setContentView(layout.activity_main);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }



        text_interest = (TextView) findViewById(id.text_interest);
        spinner = (Spinner) findViewById(id.spi_interest);

        text_interest.setText("choose your interest");
        //新建arrayAdapter,android.R.layout.simple_spinner_item是调用android studio中默认的样式
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, list_interest);
        //adapter设置一个下拉列表
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //spinner加载适配器
        spinner.setAdapter(adapter);
        spinner.setSelection(1);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                intrest = list_interest[position];
                text_interest.setText(intrest);
                System.out.println("text_interest" +" "+ intrest);
                if(tempintrest.equals(intrest))
                {

                }
                else {
                    tempintrest = intrest;
                    reStartSocket();

                    LinearLayout llot = (LinearLayout) findViewById(R.id.layout_main);

                    switch (tempintrest)
                    {
                        case "food":{
                            llot.setBackgroundResource(mipmap.pic_food);
                             Toast.makeText(getApplicationContext(),
                                    "Welcome to food Chapter.", Toast.LENGTH_LONG)
                                    .show();
                            break;}
                        case "knowledage": {
                            llot.setBackgroundResource(mipmap.pic_knowledge);
                            Toast.makeText(getApplicationContext(),
                                    "Welcome to knowleadge Chapter.", Toast.LENGTH_LONG)
                                    .show();
                            break;}
                        case "entertainment":{
                            llot.setBackgroundResource(mipmap.pic_entertainment);
                            Toast.makeText(getApplicationContext(),
                                    "Welcome to entertainment Chapter.", Toast.LENGTH_LONG)
                                    .show();
                            break;}
                        case "sport":{
                            llot.setBackgroundResource(mipmap.pic_sport);
                            Toast.makeText(getApplicationContext(),
                                    "Welcome to sport Chapter.", Toast.LENGTH_LONG)
                                    .show();
                        break;}
                        default: { }
                    }
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        serviceTv = (TextView) findViewById(id.tv_service);
        contentEt = (EditText) findViewById(id.et_content);
        sendBtn = (Button) findViewById(id.btn_send);
        socketThread = new SocketThread(this,tempintrest,session.getLoinIP());
        socketThread.start();

        serviceTv.setMovementMethod(ScrollingMovementMethod.getInstance());  //设置滚轮效果
        serviceTv.setScrollbarFadingEnabled(false);
        System.out.println("in main intrest canshu is " + tempintrest);
        System.out.println("in main ip canshu is " + session.getLoinIP());
                    sendBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            stringBuilder.append("我:\n");
                            stringBuilder.append(contentEt.getText().toString());
                            stringBuilder.append("\n");
                            //serviceTv.setGravity(Gravity.RIGHT);
                            serviceTv.setText(stringBuilder.toString());
                            socketThread.sendMessage(contentEt.getText().toString());
                            contentEt.setText("");
                        }
                    });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        socketThread.disconnect();
    }

    protected void reStartSocket()
    {
        socketThread.disconnect();
        socketThread = new SocketThread(this,tempintrest,session.getLoinIP());
        socketThread.start();
    }

    @Override
    public void onNewMessage(String msg) {
        Log.i("收到的信息i", msg);
        String[] s = msg.split("#");
        stringBuilder.append(s[0]);
        stringBuilder.append("\n");
        stringBuilder.append(s[1]);
        stringBuilder.append("\n");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //serviceTv.setGravity(Gravity.LEFT);
                serviceTv.setText(stringBuilder.toString());
            }
        });
    }

    public void onNewStringList(String msg) {
        Log.i("收到的信息i", msg);
    }

    public void OnLogout(View view) {
        socketThread.disconnect();
        session.setLogin(false);
        session.setAutoLogin(false);

        Intent intent = new Intent(getApplication(), Start.class);
        startActivity(intent);
    }

    public String getinterest() {
        return text_interest.getText().toString();
    }
}
