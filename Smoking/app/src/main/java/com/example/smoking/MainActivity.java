package com.example.smoking;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    public static final String PREFS_NAME = "QuitSmokingPrefs";
    public static final String KEY_SMOKING_UNIT = "smokingUnit";
    public static final String KEY_SMOKING_AMOUNT = "smokingAmount";
    public static final String KEY_SMOKING_PERIOD = "smokingPeriod";
    public static final String KEY_CIGARETTE_PRICE = "cigarettePrice";
    public static final String KEY_CIGARETTES_PER_PACK = "cigarettesPerPack";
    public static final String KEY_QUIT_START_TIME = "quitStartTime";
    public static final String KEY_IS_STARTED = "isStarted";

    private static final String UNIT_DAILY = "개비";  // 하루 기준
    private static final String UNIT_PERIOD = "갑";   // 일정 기간 기준

    private static final int DEFAULT_CIGARETTES_PER_PACK = 20;

    private SharedPreferences prefs;

    private RadioGroup rgSmokingMode;
    private RadioButton rbDaily;
    private RadioButton rbPeriod;
    private LinearLayout layoutPeriod;
    private TextView tvAmountLabel;
    private EditText etPeriodDays;
    private EditText etSmokingAmount;
    private EditText etCigarettePrice;
    private EditText etCigarettesPerPack;
    private Button btnStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        if (prefs.getBoolean(KEY_IS_STARTED, false)) {
            goToNextScreen();
            return;
        }

        setContentView(R.layout.activity_main);

        initViews();

        etCigarettesPerPack.setText(String.valueOf(DEFAULT_CIGARETTES_PER_PACK));
        updateModeUi();

        rgSmokingMode.setOnCheckedChangeListener((group, checkedId) -> updateModeUi());
        btnStart.setOnClickListener(v -> onStartClicked());
    }

    private void initViews() {
        rgSmokingMode = findViewById(R.id.rgSmokingMode);
        rbDaily = findViewById(R.id.rbDaily);
        rbPeriod = findViewById(R.id.rbPeriod);
        layoutPeriod = findViewById(R.id.layoutPeriod);
        tvAmountLabel = findViewById(R.id.tvAmountLabel);
        etPeriodDays = findViewById(R.id.etPeriodDays);
        etSmokingAmount = findViewById(R.id.etSmokingAmount);
        etCigarettePrice = findViewById(R.id.etCigarettePrice);
        etCigarettesPerPack = findViewById(R.id.etCigarettesPerPack);
        btnStart = findViewById(R.id.btnStart);
    }

    // 입력 방식
    private void updateModeUi() {
        if (rbDaily.isChecked()) {
            layoutPeriod.setVisibility(LinearLayout.GONE);
            tvAmountLabel.setText("하루 평균 흡연량 (개비)");
            etSmokingAmount.setHint("예: 10");
        } else {
            layoutPeriod.setVisibility(LinearLayout.VISIBLE);
            tvAmountLabel.setText("해당 기간 동안 소비한 담배 (갑)");
            etSmokingAmount.setHint("예: 2");
        }
    }

    // 금연 시작 버튼
    private void onStartClicked() {
        boolean isDaily = rbDaily.isChecked();

        // 흡연량
        float amount = parseFloat(etSmokingAmount.getText().toString());
        if (amount <= 0f) {
            etSmokingAmount.setError("0보다 큰 값을 입력해 주세요");
            etSmokingAmount.requestFocus();
            return;
        }

        // 기간
        int period = 1;
        if (!isDaily) {
            period = parseInt(etPeriodDays.getText().toString());
            if (period <= 0) {
                etPeriodDays.setError("1일 이상 입력해 주세요");
                etPeriodDays.requestFocus();
                return;
            }
        }

        // 담배 가격
        int price = parseInt(etCigarettePrice.getText().toString());
        if (price <= 0) {
            etCigarettePrice.setError("담배 가격을 입력해 주세요");
            etCigarettePrice.requestFocus();
            return;
        }

        // 한 갑당 개비 수
        String perPackStr = etCigarettesPerPack.getText().toString().trim();
        int perPack = perPackStr.isEmpty() ? DEFAULT_CIGARETTES_PER_PACK : parseInt(perPackStr);
        if (perPack <= 0) {
            etCigarettesPerPack.setError("1개비 이상 입력해 주세요");
            etCigarettesPerPack.requestFocus();
            return;
        }

        // 저장
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_SMOKING_UNIT, isDaily ? UNIT_DAILY : UNIT_PERIOD);
        editor.putFloat(KEY_SMOKING_AMOUNT, amount);
        editor.putInt(KEY_SMOKING_PERIOD, period);
        editor.putInt(KEY_CIGARETTE_PRICE, price);
        editor.putInt(KEY_CIGARETTES_PER_PACK, perPack);
        editor.putLong(KEY_QUIT_START_TIME, System.currentTimeMillis());
        editor.putBoolean(KEY_IS_STARTED, true);
        editor.apply();

        goToNextScreen();
    }

    // 금연 현황 화면으로 이동 (뒤로 가기로 입력 화면에 돌아오지 않도록 현재 화면은 종료)
    private void goToNextScreen() {
        Intent intent = new Intent(this, StatusActivity.class);
        startActivity(intent);
        finish();
    }

    private float parseFloat(String s) {
        try {
            return Float.parseFloat(s.trim());
        } catch (NumberFormatException e) {
            return 0f;
        }
    }

    private int parseInt(String s) {
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}