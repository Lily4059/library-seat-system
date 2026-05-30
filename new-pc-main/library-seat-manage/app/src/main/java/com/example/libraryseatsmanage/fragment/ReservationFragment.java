package com.example.libraryseatsmanage.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.libraryseatsmanage.R;
import com.example.libraryseatsmanage.api.RetrofitClient;
import com.example.libraryseatsmanage.model.ApiResponse;
import com.example.libraryseatsmanage.model.CheckInRequest;
import com.example.libraryseatsmanage.model.Reservation;
import com.example.libraryseatsmanage.utils.UserManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReservationFragment extends Fragment {

    private ListView reservationList;
    private TextView emptyText;
    private List<Reservation> reservationsList;
    private UserManager userManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reservation, container, false);
        reservationList = view.findViewById(R.id.reservation_list);
        emptyText = view.findViewById(R.id.empty_text);
        userManager = UserManager.getInstance(getContext());
        loadReservationData();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadReservationData();
    }

    private void loadReservationData() {
        int userId = userManager.getUserId();

        RetrofitClient.getInstance().getApiService().getMyReservations(userId)
                .enqueue(new Callback<ApiResponse<List<Reservation>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<Reservation>>> call, Response<ApiResponse<List<Reservation>>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            reservationsList = response.body().getData();
                            if (reservationsList == null || reservationsList.isEmpty()) {
                                emptyText.setVisibility(View.VISIBLE);
                                reservationList.setVisibility(View.GONE);
                            } else {
                                displayReservations();
                            }
                        } else {
                            emptyText.setVisibility(View.VISIBLE);
                            reservationList.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<Reservation>>> call, Throwable t) {
                        t.printStackTrace();
                        Toast.makeText(getContext(), "网络错误", Toast.LENGTH_SHORT).show();
                        emptyText.setVisibility(View.VISIBLE);
                        reservationList.setVisibility(View.GONE);
                    }
                });
    }

    private void displayReservations() {
        sortReservations();
        ReservationAdapter adapter = new ReservationAdapter();
        reservationList.setAdapter(adapter);
        emptyText.setVisibility(View.GONE);
        reservationList.setVisibility(View.VISIBLE);
    }

    private void sortReservations() {
        if (reservationsList == null) return;

        java.util.Collections.sort(reservationsList, (o1, o2) -> {
            boolean o1CheckedIn = "checked_in".equals(o1.getStatus());
            boolean o1Completed = "completed".equals(o1.getStatus()) || "cancelled".equals(o1.getStatus());
            boolean o2CheckedIn = "checked_in".equals(o2.getStatus());
            boolean o2Completed = "completed".equals(o2.getStatus()) || "cancelled".equals(o2.getStatus());

            if (o1Completed && !o2Completed) return 1;
            if (!o1Completed && o2Completed) return -1;

            if (o1CheckedIn && !o2CheckedIn) return -1;
            if (!o1CheckedIn && o2CheckedIn) return 1;

            return 0;
        });
    }

    private class ReservationAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return reservationsList != null ? reservationsList.size() : 0;
        }

        @Override
        public Object getItem(int position) {
            return reservationsList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            ViewHolder holder;

            if (convertView == null) {
                view = LayoutInflater.from(getContext()).inflate(R.layout.reservation_item, parent, false);
                holder = new ViewHolder();
                holder.seatInfo = view.findViewById(R.id.seat_info);
                holder.checkInButton = view.findViewById(R.id.check_in_button);
                holder.cancelButton = view.findViewById(R.id.cancel_button);
                holder.deleteButton = view.findViewById(R.id.delete_button);
                view.setTag(holder);
            } else {
                view = convertView;
                holder = (ViewHolder) view.getTag();
            }

            Reservation reservation = reservationsList.get(position);

            String timeInfo = formatTime(reservation.getStartTime()) + " - " + formatTime(reservation.getEndTime());
            holder.seatInfo.setText(getString(R.string.seat) + "：" + reservation.getSeatNo() + "\n" +
                    getString(R.string.time) + timeInfo + "\n" +
                    "状态：" + getStatusText(reservation.getStatus()));

            boolean isPending = "pending".equals(reservation.getStatus());
            boolean isCheckedIn = "checked_in".equals(reservation.getStatus());
            boolean isCompleted = "completed".equals(reservation.getStatus()) || "cancelled".equals(reservation.getStatus());

            holder.deleteButton.setVisibility(isCompleted ? View.VISIBLE : View.GONE);
            holder.deleteButton.setOnClickListener(v -> {
                reservationsList.remove(position);
                notifyDataSetChanged();
                if (reservationsList.isEmpty()) {
                    emptyText.setVisibility(View.VISIBLE);
                    reservationList.setVisibility(View.GONE);
                }
            });

            if (isCompleted) {
                holder.checkInButton.setText("已完成");
                holder.checkInButton.setEnabled(false);
                holder.cancelButton.setVisibility(View.GONE);
            } else if (isCheckedIn) {
                holder.checkInButton.setText("已签到");
                holder.checkInButton.setEnabled(false);
                holder.cancelButton.setText("签退");
                holder.cancelButton.setEnabled(true);
                holder.cancelButton.setOnClickListener(v -> checkOutReservation(reservation));
            } else if (isPending) {
                holder.checkInButton.setEnabled(true);
                holder.checkInButton.setText("签到");
                holder.checkInButton.setOnClickListener(v -> checkInReservation(reservation));
                holder.cancelButton.setText("取消预约");
                holder.cancelButton.setEnabled(true);
                holder.cancelButton.setOnClickListener(v -> cancelReservation(reservation, position));
            }

            return view;
        }

        private class ViewHolder {
            TextView seatInfo;
            Button checkInButton;
            Button cancelButton;
            ImageView deleteButton;
        }
    }

    private String formatTime(String isoTime) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("MM-dd HH:mm", Locale.getDefault());
            Date date = inputFormat.parse(isoTime);
            return outputFormat.format(date);
        } catch (ParseException e) {
            return isoTime;
        }
    }

    private String getStatusText(String status) {
        switch (status) {
            case "pending":
                return "待签到";
            case "checked_in":
                return "已签到";
            case "completed":
                return "已完成";
            case "cancelled":
                return "已取消";
            case "violation":
                return "违约";
            default:
                return status;
        }
    }

    private void checkInReservation(Reservation reservation) {
        int userId = userManager.getUserId();
        CheckInRequest request = new CheckInRequest(reservation.getId());

        RetrofitClient.getInstance().getApiService().checkIn(userId, request)
                .enqueue(new Callback<ApiResponse<Object>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            Toast.makeText(getContext(), "签到成功", Toast.LENGTH_SHORT).show();
                            loadReservationData();
                        } else {
                            String message = response.body() != null ? response.body().getMessage() : "签到失败";
                            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                        t.printStackTrace();
                        Toast.makeText(getContext(), "网络错误", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkOutReservation(Reservation reservation) {
        int userId = userManager.getUserId();
        CheckInRequest request = new CheckInRequest(reservation.getId());

        RetrofitClient.getInstance().getApiService().checkOut(userId, request)
                .enqueue(new Callback<ApiResponse<Object>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            Toast.makeText(getContext(), "签退成功", Toast.LENGTH_SHORT).show();
                            loadReservationData();
                        } else {
                            String message = response.body() != null ? response.body().getMessage() : "签退失败";
                            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                        t.printStackTrace();
                        Toast.makeText(getContext(), "网络错误", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void cancelReservation(Reservation reservation, int position) {
        int userId = userManager.getUserId();

        RetrofitClient.getInstance().getApiService().cancelReservation(userId, reservation.getId())
                .enqueue(new Callback<ApiResponse<Object>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            Toast.makeText(getContext(), "取消成功", Toast.LENGTH_SHORT).show();
                            loadReservationData();
                        } else {
                            String message = response.body() != null ? response.body().getMessage() : "取消失败";
                            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                        t.printStackTrace();
                        Toast.makeText(getContext(), "网络错误", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
