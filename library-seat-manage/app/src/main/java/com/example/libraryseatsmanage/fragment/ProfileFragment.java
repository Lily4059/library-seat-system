package com.example.libraryseatsmanage.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.libraryseatsmanage.MainActivity;
import com.example.libraryseatsmanage.R;
import com.example.libraryseatsmanage.api.RetrofitClient;
import com.example.libraryseatsmanage.model.ApiResponse;
import com.example.libraryseatsmanage.model.UserProfile;
import com.example.libraryseatsmanage.utils.UserManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {

    private TextView tvUsername;
    private TextView tvEmail;
    private TextView tvCredit;
    private TextView tvStudyTime;
    private Button btnLogout;
    private UserManager userManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        tvUsername = view.findViewById(R.id.tv_username);
        tvEmail = view.findViewById(R.id.tv_email);
        btnLogout = view.findViewById(R.id.btn_logout);
        userManager = UserManager.getInstance(getContext());

        loadUserData();

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logout();
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserData();
    }

    private void loadUserData() {
        int userId = userManager.getUserId();

        RetrofitClient.getInstance().getApiService().getUserProfile(userId)
                .enqueue(new Callback<ApiResponse<UserProfile>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<UserProfile>> call, Response<ApiResponse<UserProfile>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            UserProfile profile = response.body().getData();
                            displayUserData(profile);
                        } else {
                            displayLocalUserData();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<UserProfile>> call, Throwable t) {
                        t.printStackTrace();
                        displayLocalUserData();
                    }
                });
    }

    private void displayUserData(UserProfile profile) {
        if (profile != null) {
            tvUsername.setText(profile.getName());
            tvEmail.setText(profile.getStudentNo() != null ? profile.getStudentNo() : "未绑定学号");
        }
    }

    private void displayLocalUserData() {
        if (userManager.getCurrentUser() != null) {
            tvUsername.setText(userManager.getCurrentUser().getName());
            tvEmail.setText(userManager.getCurrentUser().getStudentNo() != null ?
                    userManager.getCurrentUser().getStudentNo() : "未绑定学号");
        } else {
            tvUsername.setText(R.string.not_logged_in);
            tvEmail.setText(R.string.not_logged_in);
        }
    }

    private void logout() {
        userManager.logout();
        Intent intent = new Intent(getActivity(), MainActivity.class);
        startActivity(intent);
        getActivity().finish();
    }
}
