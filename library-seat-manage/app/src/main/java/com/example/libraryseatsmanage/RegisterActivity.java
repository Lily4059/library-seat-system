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
import com.example.libraryseatsmanage.model.RegisterRequest;
import com.google.android.material.textfield.TextInputLayout;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reigister);

        TextView backToLogin = findViewById(R.id.tv_back_to_login);
        Button registerBtn = findViewById(R.id.btn_register);
        TextInputLayout usernameLayout = findViewById(R.id.til_register_username);
        TextInputLayout emailLayout = findViewById(R.id.til_register_email);
        TextInputLayout passwordLayout = findViewById(R.id.til_register_password);
        TextInputLayout confirmPasswordLayout = findViewById(R.id.til_register_confirm_password);

        backToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = usernameLayout.getEditText().getText().toString().trim();
                String email = emailLayout.getEditText().getText().toString().trim();
                String password = passwordLayout.getEditText().getText().toString().trim();
                String confirmPassword = confirmPasswordLayout.getEditText().getText().toString().trim();

                if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "请填写所有信息", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!password.equals(confirmPassword)) {
                    Toast.makeText(RegisterActivity.this, "两次输入的密码不一致", Toast.LENGTH_SHORT).show();
                    return;
                }

                register(username, password, email);
            }
        });
    }

    private void register(String username, String password, String name) {
        RegisterRequest request = new RegisterRequest(username, password, name);

        RetrofitClient.getInstance().getApiService().register(request)
                .enqueue(new Callback<ApiResponse<Object>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse<Object> apiResponse = response.body();
                            if (apiResponse.isSuccess()) {
                                Toast.makeText(RegisterActivity.this, "注册成功", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(RegisterActivity.this, apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(RegisterActivity.this, "注册失败，请稍后重试", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                        t.printStackTrace();
                        Toast.makeText(RegisterActivity.this, "网络错误，请检查网络连接", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
