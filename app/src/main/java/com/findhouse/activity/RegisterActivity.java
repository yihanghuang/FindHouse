package com.findhouse.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.findhouse.data.JsonData;
import com.findhouse.data.User;
import com.findhouse.network.NetworkClient;
import com.findhouse.utils.Url;
import com.findhouse.utils.Uuid;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.Response;


public class RegisterActivity extends AppCompatActivity {
    EditText et_register_name;
    EditText et_register_password;
    EditText et_register_phone;
    Button btnRegister;

    private String type = "/user";
    private String route = "/regist";
    private List<User> user = new ArrayList<>();
    private boolean hasResult = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        et_register_name = (EditText) findViewById(R.id.editText1);
        et_register_password = (EditText) findViewById(R.id.editText2);
        et_register_phone = (EditText) findViewById(R.id.editText3);

        btnRegister = (Button) findViewById(R.id.buttonRegister);
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.buttonRegister:
                        String user_name = et_register_name.getText().toString().trim();
                        String user_password = et_register_password.getText().toString().trim();
                        String user_phone = et_register_phone.getText().toString().trim();
                        // 非空验证
                        if (user_name.isEmpty() ) {
                            Toast.makeText(RegisterActivity.this, "账号不能为空", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (user_password.isEmpty()) {
                            Toast.makeText(RegisterActivity.this, "密码不能为空", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (user_phone.isEmpty() ) {
                            Toast.makeText(RegisterActivity.this, "手机号不能为空", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // 注册
                        Uuid newUuid = new Uuid();
                        String uuid = newUuid.generateShortUuid();
                        Log.d("okhttp",uuid);
                        User userRegist = new User();
                        userRegist.setUid(uuid);
                        userRegist.setName(user_name);
                        userRegist.setPass(user_password);
                        userRegist.setTel(user_phone);

                        Url baseUrl = new Url();
                        baseUrl.setType(type);
                        baseUrl.setRoute(route);
                        String url = baseUrl.toString();

                        Gson gson = new Gson();
                        //使用Gson将对象转换为json字符串
                        String json = gson.toJson(userRegist);

                        //MediaType  设置Content-Type 标头中包含的媒体类型值
                        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8")
                                , json);

                        NetworkClient.postRequest(url, requestBody, new okhttp3.Callback() {

                            @Override
                            public void onFailure(Call call, IOException e) {
                                e.printStackTrace();

                                RegisterActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(RegisterActivity.this, "网络请求错误，请重试～", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                int code = response.code();
                                String responseJsonData = response.body().string();
                                // 解析json
                                hasResult = parseJSON(responseJsonData);
                                Log.d("okhttp", "code: " + code);
                                Log.d("okhttp", "body: " + responseJsonData);
                                RegisterActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if(hasResult){
                                            Toast.makeText(RegisterActivity.this, "注册成功", Toast.LENGTH_SHORT).show();
                                            Intent intent_main = new Intent(RegisterActivity.this, NewsActivity.class);
                                            startActivity(intent_main);
                                            RegisterActivity.this.finish();
                                        }
                                        //  失败
                                        else{
                                            Toast.makeText(RegisterActivity.this, "该用户已存在", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }

                        });


                }
            }
        });
    }

    private boolean parseJSON(String jsonData) {
        Gson gson = new Gson();
        JsonData<User> parseData = gson.fromJson(jsonData, new TypeToken<JsonData<User>>(){}.getType());
        if(null == parseData.getData()) {
            return false;
        }else{
            user = parseData.getData();
            return true;
        }
    }


}
