package com.example.smoking;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

/**
 * 앱을 실행하면 가장 먼저 열리는 화면.
 *
 * 지금은 앱이 실행되는지 확인하기 위한 기본 화면이다.
 * 이후 기획서 4장 기준으로 다음 기능을 붙인다.
 *  - 저장된 정보가 없으면 최초 정보 입력 화면으로 이동
 *  - 저장된 정보가 있으면 금연 현황 화면 표시
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
