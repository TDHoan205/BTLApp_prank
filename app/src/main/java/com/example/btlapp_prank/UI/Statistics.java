    package com.example.btlapp_prank.UI;

    import android.app.DatePickerDialog;
    import android.content.Intent;
    import android.os.Bundle;
    import android.widget.*;
    import androidx.appcompat.app.AppCompatActivity;
    import androidx.core.content.ContextCompat;
    import androidx.drawerlayout.widget.DrawerLayout;
    import androidx.appcompat.widget.Toolbar;

    import com.example.btlapp_prank.R;
    import com.github.mikephil.charting.charts.*;
    import com.github.mikephil.charting.components.XAxis;
    import com.github.mikephil.charting.data.*;
    import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
    import com.google.android.material.navigation.NavigationView;

    import java.text.SimpleDateFormat;
    import java.util.*;

    public class Statistics extends AppCompatActivity {

        private Button btnSummaryUsers, btnSummarySounds;
        private Button btnFromDate, btnToDate, btnFilter;
        private Spinner spinnerChartType;
        private LineChart lineChart;
        private BarChart barChart;
        private PieChart pieChart;

        private UserDB userDB;
        private SoundDB soundDB;
        private PrefManager prefManager;

        private Date fromDate, toDate;
        private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        private final String[] chartTypes = {"Biểu đồ đường", "Biểu đồ cột", "Biểu đồ tròn"};

        private DrawerLayout drawerLayout;
        private NavigationView navigationView;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_statistics);

            prefManager = new PrefManager(this);
            userDB = new UserDB(this);
            soundDB = new SoundDB(this);

            initViews();
            setupSpinner();
            setupDatePickers();

            loadSummary();
            showUserStats();

            Toolbar toolbar = findViewById(R.id.toolbar);
            if (toolbar != null) {
                toolbar.setTitle("Trang thống kê");
                setSupportActionBar(toolbar);
                toolbar.setNavigationIcon(android.R.drawable.ic_menu_close_clear_cancel);
                toolbar.setNavigationOnClickListener(v -> finish());
            }

            drawerLayout = findViewById(R.id.drawer_layout);
            navigationView = findViewById(R.id.nav_view);

            if (toolbar != null) {
                toolbar.setNavigationOnClickListener(v -> {
                    String role = prefManager.getCurrentUserRole();
                    if (role != null && "admin".equalsIgnoreCase(role)) {
                        finish();
                    } else {
                        Toast.makeText(this, "Chỉ Admin mới được quay về Home!", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            if (navigationView != null) {
                navigationView.setNavigationItemSelectedListener(item -> {
                    int id = item.getItemId();
                    if (id == R.id.action_exit) {
                        logoutAndRedirect();
                    } else if (id == R.id.action_home) {
                        finish();
                    } else if (id == R.id.action_setting) {
                        startActivity(new Intent(this, Settings.class));
                    } else if (id == R.id.action_about) {
                        startActivity(new Intent(this, About.class));
                    } else if (id == R.id.action_premium) {
                        startActivity(new Intent(this, EnterCodeActivity.class));
                    }
                    drawerLayout.closeDrawers();
                    return true;
                });
            }
        }

        private void initViews() {
            btnSummaryUsers = findViewById(R.id.btnSummaryUsers);
            btnSummarySounds = findViewById(R.id.btnSummarySounds);
            btnFromDate = findViewById(R.id.btnFromDate);
            btnToDate = findViewById(R.id.btnToDate);
            btnFilter = findViewById(R.id.btnFilter);
            spinnerChartType = findViewById(R.id.spinnerChartType);

            lineChart = findViewById(R.id.lineChart);
            barChart = findViewById(R.id.barChart);
            pieChart = findViewById(R.id.pieChart);

            btnSummaryUsers.setOnClickListener(v -> showUserStats());
            btnSummarySounds.setOnClickListener(v -> showSoundStats());
            btnFilter.setOnClickListener(v -> filterData());
        }

        private void setupSpinner() {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, chartTypes);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerChartType.setAdapter(adapter);

            spinnerChartType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                    if (btnSummaryUsers.isSelected()) showUserStats();
                    else showSoundStats();
                }
                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });
        }

        private void setupDatePickers() {
            btnFromDate.setOnClickListener(v -> pickDate(true));
            btnToDate.setOnClickListener(v -> pickDate(false));
        }

        private void pickDate(boolean isFrom) {
            Calendar cal = Calendar.getInstance();
            DatePickerDialog dialog = new DatePickerDialog(this,
                    (view, year, month, day) -> {
                        cal.set(year, month, day);
                        if (isFrom) {
                            fromDate = cal.getTime();
                            btnFromDate.setText(sdf.format(fromDate));
                        } else {
                            toDate = cal.getTime();
                            btnToDate.setText(sdf.format(toDate));
                        }
                    },
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH));
            dialog.show();
        }

        private void loadSummary() {
            btnSummaryUsers.setText("Người dùng: " + getUserCount());
            btnSummarySounds.setText("Âm thanh: " + getSoundCount());
        }

        private void filterData() {
            loadSummary();
            if (btnSummaryUsers.isSelected()) showUserStats();
            else showSoundStats();
        }

        private int getUserCount() {
            return userDB.getAllUsersCount();
        }

        private int getSoundCount() {
            return soundDB.getAllSounds().size();
        }

        private Map<String, Integer> getUserLoginStatsFiltered() {
            String from = (fromDate != null) ? new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(fromDate) : null;
            String to = (toDate != null) ? new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(toDate) : null;
            return userDB.getUserLoginStats(from, to);
        }

        private void showUserStats() {
            btnSummaryUsers.setSelected(true);
            btnSummarySounds.setSelected(false);
            displayChart(getUserLoginStatsFiltered(), "Người dùng");
        }

        private Map<String, Integer> getSoundStats() {
            Map<String, Integer> stats = new LinkedHashMap<>();
            List<Sound> sounds = soundDB.getAllSounds();
            for (Sound s : sounds) {
                String key = s.getTitle() + " (" + (s.getAudioUri() != null ? s.getAudioUri().getLastPathSegment() : "") + ")";
                stats.put(key, s.getPlayCount());
            }
            return stats;
        }

        private void showSoundStats() {
            btnSummaryUsers.setSelected(false);
            btnSummarySounds.setSelected(true);
            displayChart(getSoundStats(), "Âm thanh");
        }

        private void displayChart(Map<String, Integer> stats, String label) {
            int pos = spinnerChartType.getSelectedItemPosition();
            lineChart.setVisibility(pos == 0 ? android.view.View.VISIBLE : android.view.View.GONE);
            barChart.setVisibility(pos == 1 ? android.view.View.VISIBLE : android.view.View.GONE);
            pieChart.setVisibility(pos == 2 ? android.view.View.VISIBLE : android.view.View.GONE);

            List<String> labels = new ArrayList<>(stats.keySet());

            switch (pos) {
                case 0: // Biểu đồ đường
                    List<Entry> lineEntries = new ArrayList<>();
                    for (int i = 0; i < labels.size(); i++) {
                        lineEntries.add(new Entry(i, stats.get(labels.get(i))));
                    }

                    LineDataSet lineSet = new LineDataSet(lineEntries, label);
                    lineSet.setValueTextSize(12f);
                    lineSet.setCircleRadius(6f);
                    lineSet.setCircleHoleRadius(3f);
                    lineSet.setLineWidth(2f);
                    lineSet.setColor(ContextCompat.getColor(this, R.color.teal_700));
                    lineSet.setCircleColor(ContextCompat.getColor(this, R.color.purple_700));

                    LineData lineData = new LineData(lineSet);
                    lineChart.setData(lineData);

                    XAxis xAxisLine = lineChart.getXAxis();
                    xAxisLine.setValueFormatter(new IndexAxisValueFormatter(labels));
                    xAxisLine.setPosition(XAxis.XAxisPosition.BOTTOM);
                    xAxisLine.setGranularity(1f);
                    xAxisLine.setLabelRotationAngle(45f);
                    xAxisLine.setTextSize(10f);

                    lineChart.getAxisLeft().setGranularity(1f);
                    lineChart.getAxisLeft().setAxisMinimum(0f);
                    lineChart.getAxisRight().setEnabled(false);

                    // Quan trọng: chỉ hiển thị tối đa 6 đối tượng, vuốt để xem thêm
                    lineChart.setVisibleXRangeMaximum(6);
                    lineChart.setDragEnabled(true);
                    lineChart.setScaleEnabled(true);

                    lineChart.getDescription().setEnabled(false);
                    lineChart.invalidate();
                    break;

                case 1: // Biểu đồ cột
                    List<BarEntry> barEntries = new ArrayList<>();
                    for (int i = 0; i < labels.size(); i++) {
                        barEntries.add(new BarEntry(i, stats.get(labels.get(i))));
                    }

                    BarDataSet barSet = new BarDataSet(barEntries, label);
                    barSet.setValueTextSize(12f);
                    barSet.setColors(new int[]{
                            ContextCompat.getColor(this, R.color.teal_700),
                            ContextCompat.getColor(this, R.color.purple_700),
                            ContextCompat.getColor(this, R.color.pink_700),
                            ContextCompat.getColor(this, R.color.orange_700),
                            ContextCompat.getColor(this, R.color.blue_700),
                            ContextCompat.getColor(this, R.color.green_700)
                    });

                    BarData barData = new BarData(barSet);
                    barData.setBarWidth(0.9f);
                    barChart.setData(barData);

                    XAxis xAxisBar = barChart.getXAxis();
                    xAxisBar.setValueFormatter(new IndexAxisValueFormatter(labels));
                    xAxisBar.setPosition(XAxis.XAxisPosition.BOTTOM);
                    xAxisBar.setGranularity(1f);
                    xAxisBar.setLabelRotationAngle(45f);
                    xAxisBar.setTextSize(10f);

                    barChart.getAxisLeft().setGranularity(1f);
                    barChart.getAxisLeft().setAxisMinimum(0f);
                    barChart.getAxisRight().setEnabled(false);

                    // Hiển thị tối đa 6 cột trên màn hình
                    barChart.setVisibleXRangeMaximum(6);
                    barChart.setDragEnabled(true);
                    barChart.setScaleEnabled(true);

                    barChart.getDescription().setEnabled(false);
                    barChart.setFitBars(true);
                    barChart.invalidate();
                    break;

                case 2: // Biểu đồ tròn
                    List<PieEntry> pieEntries = new ArrayList<>();
                    for (Map.Entry<String, Integer> e : stats.entrySet()) {
                        pieEntries.add(new PieEntry(e.getValue(), e.getKey()));
                    }

                    PieDataSet pieSet = new PieDataSet(pieEntries, label);
                    pieSet.setValueTextSize(12f);
                    pieSet.setSliceSpace(3f);
                    pieSet.setSelectionShift(5f);
                    pieSet.setColors(new int[]{
                            ContextCompat.getColor(this, R.color.teal_700),
                            ContextCompat.getColor(this, R.color.purple_700),
                            ContextCompat.getColor(this, R.color.pink_700),
                            ContextCompat.getColor(this, R.color.orange_700),
                            ContextCompat.getColor(this, R.color.blue_700),
                            ContextCompat.getColor(this, R.color.green_700)
                    });

                    PieData pieData = new PieData(pieSet);
                    pieChart.setData(pieData);
                    pieChart.getDescription().setEnabled(false);
                    pieChart.setUsePercentValues(true);
                    pieChart.invalidate();
                    break;
            }
        }


        private void logoutAndRedirect() {
            String email = prefManager.getCurrentUserEmail();
            if (email != null && !email.isEmpty()) {
                boolean logoutSuccess = userDB.updateLogoutTime(email);
                if (logoutSuccess) {
                    String lastLogout = userDB.getLastLogout(email);
                    Toast.makeText(this, "Đã đăng xuất lúc: " + lastLogout, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "Lỗi khi cập nhật thời gian đăng xuất", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
            }
            prefManager.clearUserSession();
            startActivity(new Intent(this, MainActivitylogin.class));
            finish();
        }
    }
