package com.example.smoking;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 금연 현황 화면 (기획서 4-2).
 *
 * 저장된 흡연 정보와 현재 시각으로 금연 경과 시간, 피우지 않은 담배, 절약 금액을 계산해 보여 준다.
 * 계산 결과는 저장하지 않고, 화면을 열 때와 화면을 켜 둔 동안 1분마다 다시 계산한다.
 */
public class StatusActivity extends AppCompatActivity {

    private static final long REFRESH_INTERVAL_MILLIS = 60_000L; // 1분

    private SharedPreferences prefs;
    private QuitCalculator calculator;
    private long quitStartTime;

    private TextView tvStartDate;
    private TextView tvElapsed;
    private TextView tvSkipped;
    private TextView tvSavedMoney;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable refreshTask = new Runnable() {
        @Override
        public void run() {
            updateStatus();
            handler.postDelayed(this, REFRESH_INTERVAL_MILLIS);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        tvStartDate = findViewById(R.id.tvStartDate);
        tvElapsed = findViewById(R.id.tvElapsed);
        tvSkipped = findViewById(R.id.tvSkipped);
        tvSavedMoney = findViewById(R.id.tvSavedMoney);
        Button btnReset = findViewById(R.id.btnReset);

        prefs = getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE);

        // TODO(팀장): 초기화 선택 화면이 만들어지면 여기서 그 화면으로 이동하도록 연결
        btnReset.setOnClickListener(v ->
                Toast.makeText(this, "초기화 기능은 준비 중입니다.", Toast.LENGTH_SHORT).show());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 초기화 후 돌아오는 경우에도 최신 값으로 계산하도록 매번 다시 읽는다.
        if (!loadSavedInfo()) {
            return;
        }
        tvStartDate.setText(formatStartDate(quitStartTime));
        handler.post(refreshTask);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(refreshTask);
    }

    /** 저장된 흡연 정보를 읽어 계산기를 만든다. 값이 잘못됐으면 입력 화면으로 돌려보내고 false를 반환한다. */
    private boolean loadSavedInfo() {
        try {
            calculator = new QuitCalculator(
                    prefs.getString(MainActivity.KEY_SMOKING_UNIT, QuitCalculator.UNIT_CIGARETTE),
                    prefs.getFloat(MainActivity.KEY_SMOKING_AMOUNT, 0f),
                    prefs.getInt(MainActivity.KEY_SMOKING_PERIOD, 1),
                    prefs.getInt(MainActivity.KEY_CIGARETTE_PRICE, 0),
                    prefs.getInt(MainActivity.KEY_CIGARETTES_PER_PACK, 20));
            quitStartTime = prefs.getLong(MainActivity.KEY_QUIT_START_TIME, System.currentTimeMillis());
            return true;
        } catch (IllegalArgumentException e) {
            // 저장값이 비었거나 잘못된 경우: 시작 상태를 해제하고 입력 화면으로 이동
            prefs.edit().putBoolean(MainActivity.KEY_IS_STARTED, false).apply();
            Toast.makeText(this, "저장된 흡연 정보가 없어 다시 입력합니다.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return false;
        }
    }

    /** 현재 시각 기준으로 금연 현황을 다시 계산해 화면에 표시한다. */
    private void updateStatus() {
        long elapsed = QuitCalculator.elapsedMillis(quitStartTime, System.currentTimeMillis());

        tvElapsed.setText(QuitCalculator.formatElapsed(elapsed));
        tvSkipped.setText(calculator.formatSkipped(elapsed));
        tvSavedMoney.setText(String.format(Locale.KOREA, "%,d원", calculator.savedMoney(elapsed)));
    }

    private String formatStartDate(long millis) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.KOREA);
        return format.format(new Date(millis)) + " 금연 시작";
    }
}
