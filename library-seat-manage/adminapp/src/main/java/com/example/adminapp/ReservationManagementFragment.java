package com.example.adminapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class ReservationManagementFragment extends Fragment {

    private ListView reservationList;
    private TextView emptyText;
    private ArrayList<ReservationItem> reservationItems;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reservation_management, container, false);
        reservationList = view.findViewById(R.id.reservation_list);
        emptyText = view.findViewById(R.id.empty_text);
        loadReservationData();
        return view;
    }

    private void loadReservationData() {
        try {
            reservationItems = new ArrayList<>();

            // 读取用户数据文件
            File userFile = new File(getContext().getFilesDir(), "user_data.json");
            if (!userFile.exists()) {
                emptyText.setVisibility(View.VISIBLE);
                reservationList.setVisibility(View.GONE);
                return;
            }

            BufferedReader reader = new BufferedReader(new FileReader(userFile));
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            reader.close();

            JSONArray usersArray = new JSONArray(builder.toString());

            // 遍历所有用户的预约信息
            for (int i = 0; i < usersArray.length(); i++) {
                JSONObject userObj = usersArray.getJSONObject(i);
                String username = userObj.getString("username");
                JSONArray userReservationsArray = userObj.getJSONArray("reservations");

                for (int j = 0; j < userReservationsArray.length(); j++) {
                    JSONObject reservationObj = userReservationsArray.getJSONObject(j);
                    String seatId = reservationObj.getString("seatId");
                    String date = reservationObj.getString("date");
                    String startTime = reservationObj.getString("startTime");
                    String endTime = reservationObj.getString("endTime");
                    boolean isCheckedIn = reservationObj.optBoolean("checkedIn", false);
                    boolean isCheckedOut = reservationObj.optBoolean("checkedOut", false);

                    reservationItems.add(new ReservationItem(username, seatId, date, startTime, endTime, isCheckedIn, isCheckedOut));
                }

                // 读取历史预约
                if (userObj.has("historyReservations")) {
                    JSONArray historyArray = userObj.getJSONArray("historyReservations");
                    for (int j = 0; j < historyArray.length(); j++) {
                        JSONObject reservationObj = historyArray.getJSONObject(j);
                        String seatId = reservationObj.getString("seatId");
                        String date = reservationObj.getString("date");
                        String startTime = reservationObj.getString("startTime");
                        String endTime = reservationObj.getString("endTime");
                        boolean isCheckedIn = reservationObj.optBoolean("checkedIn", false);
                        boolean isCheckedOut = reservationObj.optBoolean("checkedOut", true);

                        reservationItems.add(new ReservationItem(username, seatId, date, startTime, endTime, isCheckedIn, isCheckedOut));
                    }
                }
            }

            if (reservationItems.size() == 0) {
                emptyText.setVisibility(View.VISIBLE);
                reservationList.setVisibility(View.GONE);
                return;
            }

            ReservationAdapter adapter = new ReservationAdapter();
            reservationList.setAdapter(adapter);
            emptyText.setVisibility(View.GONE);
            reservationList.setVisibility(View.VISIBLE);

        } catch (IOException | JSONException e) {
            e.printStackTrace();
            emptyText.setVisibility(View.VISIBLE);
            reservationList.setVisibility(View.GONE);
        }
    }

    private class ReservationItem {
        String username;
        String seatId;
        String date;
        String startTime;
        String endTime;
        boolean isCheckedIn;
        boolean isCheckedOut;

        public ReservationItem(String username, String seatId, String date, String startTime, String endTime, boolean isCheckedIn, boolean isCheckedOut) {
            this.username = username;
            this.seatId = seatId;
            this.date = date;
            this.startTime = startTime;
            this.endTime = endTime;
            this.isCheckedIn = isCheckedIn;
            this.isCheckedOut = isCheckedOut;
        }
    }

    private class ReservationAdapter extends android.widget.BaseAdapter {

        @Override
        public int getCount() {
            return reservationItems.size();
        }

        @Override
        public Object getItem(int position) {
            return reservationItems.get(position);
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
                holder.reservationInfo = view.findViewById(R.id.reservation_info);
                view.setTag(holder);
            } else {
                view = convertView;
                holder = (ViewHolder) view.getTag();
            }

            ReservationItem item = reservationItems.get(position);
            StringBuilder info = new StringBuilder();
            info.append("用户: " + item.username + "\n");
            info.append("座位: " + item.seatId + "\n");
            info.append("日期: " + item.date + "\n");
            info.append("时间: " + item.startTime + "-" + item.endTime + "\n");
            info.append("状态: ");

            if (item.isCheckedOut) {
                info.append("已完成");
            } else if (item.isCheckedIn) {
                info.append("已签到");
            } else {
                info.append("未签到");
            }

            holder.reservationInfo.setText(info.toString());
            return view;
        }

        private class ViewHolder {
            TextView reservationInfo;
        }
    }
}