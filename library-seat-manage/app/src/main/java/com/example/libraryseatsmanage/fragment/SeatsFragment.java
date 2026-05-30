package com.example.libraryseatsmanage.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.view.MotionEvent;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ZoomControls;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.libraryseatsmanage.R;
import com.example.libraryseatsmanage.api.RetrofitClient;
import com.example.libraryseatsmanage.model.ApiResponse;
import com.example.libraryseatsmanage.model.Reservation;
import com.example.libraryseatsmanage.model.ReservationRequest;
import com.example.libraryseatsmanage.model.Seat;
import com.example.libraryseatsmanage.utils.UserManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SeatsFragment extends Fragment {

    private GridLayout gridLayout;
    private ZoomControls zoomControls;
    private Spinner floorSpinner;
    private String currentFloor = "1F";
    private float scale = 1.0f;
    private float lastX, lastY;
    private boolean isDragging = false;
    private UserManager userManager;
    private List<Seat> seatsList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_seats, container, false);
        gridLayout = view.findViewById(R.id.grid_layout);
        zoomControls = view.findViewById(R.id.zoom_controls);
        floorSpinner = view.findViewById(R.id.floor_spinner);
        userManager = UserManager.getInstance(getContext());
        setupFloorSpinner();
        setupZoomControls();
        setupTouchListener();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadSeatsData();
    }

    private void setupFloorSpinner() {
        String[] floors = {"1层", "2层"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, floors);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        floorSpinner.setAdapter(adapter);
        floorSpinner.setSelection(0);

        floorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentFloor = position == 0 ? "1F" : "2F";
                loadSeatsData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setupTouchListener() {
        gridLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        lastX = event.getRawX();
                        lastY = event.getRawY();
                        isDragging = true;
                        v.getParent().requestDisallowInterceptTouchEvent(true);
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        if (isDragging) {
                            float deltaX = event.getRawX() - lastX;
                            float deltaY = event.getRawY() - lastY;
                            float currentX = gridLayout.getX();
                            float currentY = gridLayout.getY();
                            gridLayout.setX(currentX + deltaX);
                            gridLayout.setY(currentY + deltaY);
                            lastX = event.getRawX();
                            lastY = event.getRawY();
                            return true;
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        isDragging = false;
                        v.getParent().requestDisallowInterceptTouchEvent(false);
                        break;
                }
                return false;
            }
        });
    }

    private void setupZoomControls() {
        zoomControls.setOnZoomInClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (scale < 2.0f) {
                    scale += 0.1f;
                    gridLayout.setScaleX(scale);
                    gridLayout.setScaleY(scale);
                }
            }
        });

        zoomControls.setOnZoomOutClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (scale > 0.5f) {
                    scale -= 0.1f;
                    gridLayout.setScaleX(scale);
                    gridLayout.setScaleY(scale);
                }
            }
        });
    }

    private void loadSeatsData() {
        RetrofitClient.getInstance().getApiService().getSeats(currentFloor)
                .enqueue(new Callback<ApiResponse<List<Seat>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<Seat>>> call, Response<ApiResponse<List<Seat>>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            seatsList = response.body().getData();
                            displaySeats();
                        } else {
                            Toast.makeText(getContext(), R.string.load_seats_failed, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<Seat>>> call, Throwable t) {
                        t.printStackTrace();
                        Toast.makeText(getContext(), "网络错误", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void displaySeats() {
        if (seatsList == null || seatsList.isEmpty()) {
            return;
        }

        gridLayout.removeAllViews();

        int maxRow = 0;
        int maxCol = 0;
        for (Seat seat : seatsList) {
            maxRow = Math.max(maxRow, seat.getX());
            maxCol = Math.max(maxCol, seat.getY());
        }

        gridLayout.setRowCount(maxRow + 1);
        gridLayout.setColumnCount(maxCol + 1);

        ImageButton[][] seats = new ImageButton[maxRow + 1][maxCol + 1];

        for (int i = 0; i <= maxRow; i++) {
            for (int j = 0; j <= maxCol; j++) {
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

        for (Seat seat : seatsList) {
            ImageButton seatBtn = seats[seat.getX()][seat.getY()];
            seatBtn.setImageResource(R.drawable.ic_seat);
            seatBtn.setEnabled(true);

            if ("occupied".equals(seat.getStatus())) {
                seatBtn.setColorFilter(0xFFFF0000);
            } else if ("temp_leave".equals(seat.getStatus())) {
                seatBtn.setColorFilter(0xFFFFFF00);
            } else {
                seatBtn.setColorFilter(null);
            }

            seatBtn.setOnClickListener(new SeatClickListener(seat));
        }
    }

    private class SeatClickListener implements View.OnClickListener {
        private Seat seat;

        public SeatClickListener(Seat seat) {
            this.seat = seat;
        }

        @Override
        public void onClick(View view) {
            if ("occupied".equals(seat.getStatus()) || "temp_leave".equals(seat.getStatus())) {
                Toast.makeText(getContext(), "该座位已被占用", Toast.LENGTH_SHORT).show();
                return;
            }

            showReservationDialog(seat);
        }

        private void showReservationDialog(Seat seat) {
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
            View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_calendar, null);
            builder.setView(dialogView);

            final EditText etYear = dialogView.findViewById(R.id.et_year);
            final EditText etMonth = dialogView.findViewById(R.id.et_month);
            final EditText etDay = dialogView.findViewById(R.id.et_day);
            final EditText etStartHour = dialogView.findViewById(R.id.et_start_hour);
            final EditText etStartMinute = dialogView.findViewById(R.id.et_start_minute);
            final EditText etEndHour = dialogView.findViewById(R.id.et_end_hour);
            final EditText etEndMinute = dialogView.findViewById(R.id.et_end_minute);
            Button btnCancel = dialogView.findViewById(R.id.btn_cancel);
            Button btnConfirm = dialogView.findViewById(R.id.btn_confirm);

            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH) + 1;
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            int roundedMinute = ((minute + 5) / 10) * 10;
            if (roundedMinute == 60) {
                roundedMinute = 0;
                hour += 1;
                if (hour == 24) {
                    hour = 0;
                    calendar.add(Calendar.DAY_OF_MONTH, 1);
                    year = calendar.get(Calendar.YEAR);
                    month = calendar.get(Calendar.MONTH) + 1;
                    day = calendar.get(Calendar.DAY_OF_MONTH);
                }
            }

            etYear.setText(String.valueOf(year));
            etMonth.setText(String.valueOf(month));
            etDay.setText(String.valueOf(day));
            etStartHour.setText(String.valueOf(hour));
            etStartMinute.setText(roundedMinute < 10 ? "0" + roundedMinute : String.valueOf(roundedMinute));

            int endHour = hour + 2;
            int endMinute = roundedMinute;
            if (endHour >= 24) {
                endHour -= 24;
            }
            etEndHour.setText(String.valueOf(endHour));
            etEndMinute.setText(endMinute < 10 ? "0" + endMinute : String.valueOf(endMinute));

            final android.app.AlertDialog dialog = builder.create();

            btnCancel.setOnClickListener(v -> dialog.dismiss());

            btnConfirm.setOnClickListener(v -> {
                String yearStr = etYear.getText().toString().trim();
                String monthStr = etMonth.getText().toString().trim();
                String dayStr = etDay.getText().toString().trim();
                String startHourStr = etStartHour.getText().toString().trim();
                String startMinuteStr = etStartMinute.getText().toString().trim();
                String endHourStr = etEndHour.getText().toString().trim();
                String endMinuteStr = etEndMinute.getText().toString().trim();

                if (yearStr.isEmpty() || monthStr.isEmpty() || dayStr.isEmpty() ||
                        startHourStr.isEmpty() || startMinuteStr.isEmpty() ||
                        endHourStr.isEmpty() || endMinuteStr.isEmpty()) {
                    Toast.makeText(getContext(), R.string.please_fill_all, Toast.LENGTH_SHORT).show();
                    return;
                }

                String startTime = String.format(Locale.getDefault(), "%s-%s-%sT%s:%s:00",
                        yearStr,
                        monthStr.length() == 1 ? "0" + monthStr : monthStr,
                        dayStr.length() == 1 ? "0" + dayStr : dayStr,
                        startHourStr.length() == 1 ? "0" + startHourStr : startHourStr,
                        startMinuteStr.length() == 1 ? "0" + startMinuteStr : startMinuteStr);

                String endTime = String.format(Locale.getDefault(), "%s-%s-%sT%s:%s:00",
                        yearStr,
                        monthStr.length() == 1 ? "0" + monthStr : monthStr,
                        dayStr.length() == 1 ? "0" + dayStr : dayStr,
                        endHourStr.length() == 1 ? "0" + endHourStr : endHourStr,
                        endMinuteStr.length() == 1 ? "0" + endMinuteStr : endMinuteStr);

                createReservation(seat, startTime, endTime, dialog);
            });

            dialog.show();
        }

        private void createReservation(Seat seat, String startTime, String endTime, android.app.AlertDialog dialog) {
            int userId = userManager.getUserId();
            ReservationRequest request = new ReservationRequest(
                    seat.getId(),
                    seat.getSeatNo(),
                    seat.getType(),
                    startTime,
                    endTime
            );

            RetrofitClient.getInstance().getApiService().createReservation(userId, request)
                    .enqueue(new Callback<ApiResponse<Reservation>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<Reservation>> call, Response<ApiResponse<Reservation>> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                ApiResponse<Reservation> apiResponse = response.body();
                                if (apiResponse.isSuccess()) {
                                    Toast.makeText(getContext(), R.string.reservation_success, Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                    loadSeatsData();
                                } else {
                                    Toast.makeText(getContext(), apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(getContext(), R.string.reservation_failed, Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponse<Reservation>> call, Throwable t) {
                            t.printStackTrace();
                            Toast.makeText(getContext(), "网络错误", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}
