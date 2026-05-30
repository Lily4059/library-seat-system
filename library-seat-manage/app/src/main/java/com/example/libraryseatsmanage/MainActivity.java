package com.example.libraryseatsmanage;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.libraryseatsmanage.api.RetrofitClient;
import com.example.libraryseatsmanage.model.ApiResponse;
import com.example.libraryseatsmanage.model.LoginRequest;
import com.example.libraryseatsmanage.model.LoginResponse;
import com.example.libraryseatsmanage.utils.UserManager;
import com.google.android.material.textfield.TextInputLayout;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private UserManager userManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        userManager = UserManager.getInstance(this);

        if (userManager.isLoggedIn()) {
            startActivity(new Intent(this, Page1Activity.class));
            finish();
            return;
        }

        TextView register0;
        TextInputLayout zhanghao, mima;
        Button denglu;
        register0 = findViewById(R.id.tv_register);
        zhanghao = findViewById(R.id.til_username);
        mima = findViewById(R.id.til_password);
        denglu = findViewById(R.id.btn_login);

        denglu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = zhanghao.getEditText().getText().toString().trim();
                String password = mima.getEditText().getText().toString().trim();

                if (username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(MainActivity.this, "请输入用户名和密码", Toast.LENGTH_SHORT).show();
                    return;
                }

                login(username, password);
            }
        });

        register0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    private void login(String username, String password) {
        LoginRequest request = new LoginRequest(username, password);

        RetrofitClient.getInstance().getApiService().login(request)
                .enqueue(new Callback<ApiResponse<LoginResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<LoginResponse>> call, Response<ApiResponse<LoginResponse>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse<LoginResponse> apiResponse = response.body();
                            if (apiResponse.isSuccess()) {
                                LoginResponse loginResponse = apiResponse.getData();
                                userManager.saveUser(loginResponse.getToken(), loginResponse.getUserInfo());
                                Toast.makeText(MainActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(MainActivity.this, Page1Activity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(MainActivity.this, apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(MainActivity.this, "登录失败，请稍后重试", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<LoginResponse>> call, Throwable t) {
                        t.printStackTrace();
                        Toast.makeText(MainActivity.this, "网络错误，请检查网络连接", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
