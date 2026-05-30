package com.example.adminapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.content.Intent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;

public class MainActivity extends AppCompatActivity {

    private TextInputLayout tilUsername;
    private TextInputLayout tilPassword;
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tilUsername = findViewById(R.id.til_username);
        tilPassword = findViewById(R.id.til_password);
        btnLogin = findViewById(R.id.btn_login);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = tilUsername.getEditText().getText().toString().trim();
                String password = tilPassword.getEditText().getText().toString().trim();

                if (username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(MainActivity.this, "请输入用户名和密码", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 简单的管理员验证
                if (username.equals("admin") && password.equals("admin123")) {
                    Toast.makeText(MainActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                    // 跳转到管理员主界面
                    Intent intent = new Intent(MainActivity.this, AdminMainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(MainActivity.this, "用户名或密码错误", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}