package com.example.adminapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class SeatManagementFragment extends Fragment {

    private GridLayout gridLayout;
    private JSONArray seatsArray;
    private boolean isEditMode = false;
    private boolean isMultiSelectMode = false;
    private Button btnBatchOperation;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_seat_management, container, false);
        gridLayout = view.findViewById(R.id.grid_layout);
        Button btnEditMode = view.findViewById(R.id.btn_edit_mode);
        Button btnMultiSelect = view.findViewById(R.id.btn_multi_select);
        btnBatchOperation = view.findViewById(R.id.btn_batch_operation);

        btnEditMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isEditMode = !isEditMode;
                isMultiSelectMode = false;
                btnEditMode.setText(isEditMode ? "退出编辑" : "编辑模式");
                btnMultiSelect.setText("多选模式");
                btnBatchOperation.setVisibility(View.GONE);
                Toast.makeText(getContext(), isEditMode ? "进入编辑模式" : "退出编辑模式", Toast.LENGTH_SHORT).show();
            }
        });

        btnMultiSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isMultiSelectMode = !isMultiSelectMode;
                isEditMode = false;
                btnMultiSelect.setText(isMultiSelectMode ? "退出多选" : "多选模式");
                btnEditMode.setText("编辑模式");
                btnBatchOperation.setVisibility(isMultiSelectMode ? View.VISIBLE : View.GONE);
                Toast.makeText(getContext(), isMultiSelectMode ? "进入多选模式" : "退出多选模式", Toast.LENGTH_SHORT).show();
            }
        });

        btnBatchOperation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 批量操作逻辑
                showBatchOperationDialog();
            }
        });

        loadSeatsData();
        return view;
    }

    private void loadSeatsData() {
        try {
            // 优先读取我们写入的座位数据文件
            File seatsFile = new File(getContext().getFilesDir(), "library_seats.json");
            String jsonString;

            if (seatsFile.exists()) {
                BufferedReader reader = new BufferedReader(new FileReader(seatsFile));
                StringBuilder builder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
                reader.close();
                jsonString = builder.toString();
            } else {
                // 如果文件不存在，从raw资源中读取
                InputStream inputStream = getContext().getResources().openRawResource(R.raw.library_seats);
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder builder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
                reader.close();
                jsonString = builder.toString();
            }

            JSONObject jsonObject = new JSONObject(jsonString);
            seatsArray = jsonObject.getJSONArray("seats");

            // 清空之前的视图
            gridLayout.removeAllViews();

            // 找出最大的行和列数
            int maxRow = 0;
            int maxCol = 0;
            for (int i = 0; i < seatsArray.length(); i++) {
                JSONObject seatObject = seatsArray.getJSONObject(i);
                int x = seatObject.getInt("x");
                int y = seatObject.getInt("y");
                maxRow = Math.max(maxRow, x);
                maxCol = Math.max(maxCol, y);
            }

            // 设置网格布局的行列数
            gridLayout.setRowCount(maxRow);
            gridLayout.setColumnCount(maxCol);

            // 创建一个二维数组来存储座位
            ImageButton[][] seats = new ImageButton[maxRow][maxCol];

            // 初始化所有座位为空白
            for (int i = 0; i < maxRow; i++) {
                for (int j = 0; j < maxCol; j++) {
                    ImageButton seat = new ImageButton(getContext());
                    seat.setBackgroundResource(android.R.color.transparent);
                    seat.setEnabled(false);
                    seat.setScaleType(ImageView.ScaleType.CENTER);
                    seat.setMaxWidth(60);
                    seat.setMaxHeight(60);
                    GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                    params.width = GridLayout.LayoutParams.WRAP_CONTENT;
                    params.height = GridLayout.LayoutParams.WRAP_CONTENT;
                    params.rowSpec = GridLayout.spec(i);
                    params.columnSpec = GridLayout.spec(j);
                    params.setMargins(4, 4, 4, 4);
                    seat.setLayoutParams(params);
                    gridLayout.addView(seat);
                    seats[i][j] = seat;
                }
            }

            // 根据JSON数据设置座位
            for (int i = 0; i < seatsArray.length(); i++) {
                JSONObject seatObject = seatsArray.getJSONObject(i);
                int x = seatObject.getInt("x") - 1; // 转换为0-based索引
                int y = seatObject.getInt("y") - 1;
                int status = seatObject.getInt("status");

                if (status == 1) {
                    ImageButton seat = seats[x][y];
                    seat.setImageResource(android.R.drawable.ic_menu_search);
                    seat.setEnabled(true);
                    seat.setOnClickListener(new SeatClickListener(i, seatObject));
                } else {
                    ImageButton seat = seats[x][y];
                    seat.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
                    seat.setEnabled(true);
                    seat.setOnClickListener(new SeatClickListener(i, seatObject));
                }
            }

        } catch (IOException | JSONException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "加载座位数据失败", Toast.LENGTH_SHORT).show();
        }
    }

    private class SeatClickListener implements View.OnClickListener {
        private int position;
        private JSONObject seatObject;

        public SeatClickListener(int position, JSONObject seatObject) {
            this.position = position;
            this.seatObject = seatObject;
        }

        @Override
        public void onClick(View v) {
            if (isEditMode) {
                // 编辑模式下的点击逻辑
                showSeatEditDialog(position, seatObject);
            } else if (isMultiSelectMode) {
                // 多选模式下的点击逻辑
                boolean isSelected = !v.isSelected();
                v.setSelected(isSelected);
                // 添加选中状态的视觉反馈
                if (isSelected) {
                    v.setBackgroundColor(0x8000FF00); // 半透明绿色
                } else {
                    v.setBackgroundResource(android.R.color.transparent);
                }
            } else {
                // 普通模式下的点击逻辑
                showSeatInfoDialog(position, seatObject);
            }
        }
    }

    private void showSeatInfoDialog(int position, JSONObject seatObject) {
        // 显示座位信息对话框
        try {
            int x = seatObject.getInt("x");
            int y = seatObject.getInt("y");
            int status = seatObject.getInt("status");
            JSONArray reservationsArray = seatObject.getJSONArray("reservations");

            StringBuilder message = new StringBuilder();
            message.append("座位: " + x + "-" + y + "\n");
            message.append("状态: " + (status == 1 ? "可用" : "不可用") + "\n");
            message.append("预约信息:\n");

            if (reservationsArray.length() == 0) {
                message.append("无预约");
            } else {
                for (int i = 0; i < reservationsArray.length(); i++) {
                    JSONObject reservation = reservationsArray.getJSONObject(i);
                    String date = reservation.getString("date");
                    String startTime = reservation.getString("startTime");
                    String endTime = reservation.getString("endTime");
                    message.append(date + " " + startTime + "-" + endTime + "\n");
                }
            }

            new android.app.AlertDialog.Builder(getContext())
                    .setTitle("座位信息")
                    .setMessage(message.toString())
                    .setPositiveButton("确定", null)
                    .show();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void showSeatEditDialog(int position, JSONObject seatObject) {
        // 显示座位编辑对话框
        try {
            int x = seatObject.getInt("x");
            int y = seatObject.getInt("y");
            int status = seatObject.getInt("status");

            // 这里可以创建一个更复杂的编辑对话框，包含座位位置、方向、可预约时间段等设置
            new android.app.AlertDialog.Builder(getContext())
                    .setTitle("编辑座位")
                    .setMessage("座位: " + x + "-" + y + "\n当前状态: " + (status == 1 ? "可用" : "不可用"))
                    .setPositiveButton("修改状态", (dialog, which) -> {
                        try {
                            // 直接修改seatsArray中的原始对象
                            JSONObject originalSeatObject = seatsArray.getJSONObject(position);
                            originalSeatObject.put("status", status == 1 ? 0 : 1);
                            saveSeatsData();
                            loadSeatsData();
                            Toast.makeText(getContext(), "座位状态已更新", Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    })
                    .setNegativeButton("取消", null)
                    .show();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void saveSeatsData() {
        try {
            JSONObject seatsData = new JSONObject();
            seatsData.put("floor", 1);
            seatsData.put("seats", seatsArray);

            File seatsFile = new File(getContext().getFilesDir(), "library_seats.json");
            FileWriter writer = new FileWriter(seatsFile);
            writer.write(seatsData.toString());
            writer.close();

        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
    }

    private void showBatchOperationDialog() {
        // 显示批量操作对话框
        new android.app.AlertDialog.Builder(getContext())
                .setTitle("批量操作")
                .setItems(new String[]{"设为可用", "设为不可用", "取消"}, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            batchUpdateSeatStatus(1);
                            break;
                        case 1:
                            batchUpdateSeatStatus(0);
                            break;
                        case 2:
                            dialog.dismiss();
                            break;
                    }
                })
                .show();
    }

    private void batchUpdateSeatStatus(int status) {
        // 批量更新选中座位的状态
        int count = 0;
        for (int i = 0; i < gridLayout.getChildCount(); i++) {
            View child = gridLayout.getChildAt(i);
            if (child.isSelected()) {
                // 找到对应的座位对象并更新状态
                try {
                    // 这里需要根据座位的位置找到对应的seatsArray中的对象
                    // 由于座位是按顺序添加的，我们可以通过索引来找到对应的座位
                    if (i < seatsArray.length()) {
                        JSONObject seatObject = seatsArray.getJSONObject(i);
                        seatObject.put("status", status);
                        count++;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        if (count > 0) {
            saveSeatsData();
            loadSeatsData();
            Toast.makeText(getContext(), "已更新 " + count + " 个座位的状态", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "请先选择座位", Toast.LENGTH_SHORT).show();
        }
    }
}