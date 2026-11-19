package com.example.smoking;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class QuitCalculatorTest {

    private static final long HOUR = 60L * 60 * 1000;
    private static final long DAY = 24 * HOUR;

    // 기획서 3-4 계산 예시: 하루 10개비, 7일 → 70개비, 15,750원
    @Test
    public void 하루기준_기획서예시() {
        QuitCalculator calc = new QuitCalculator("개비", 10, 1, 4500, 20);
        assertEquals(70, calc.skippedCigarettes(7 * DAY));
        assertEquals(3.5, calc.skippedPacks(7 * DAY), 0.0001);
        assertEquals(15750, calc.savedMoney(7 * DAY));
    }

    // 4일에 2갑 = 하루 10개비이므로 같은 결과가 나와야 한다.
    @Test
    public void 기간기준_4일에2갑() {
        QuitCalculator calc = new QuitCalculator("갑", 2, 4, 4500, 20);
        assertEquals(10.0, calc.cigarettesPerDay(), 0.0001);
        assertEquals(70, calc.skippedCigarettes(7 * DAY));
        assertEquals(15750, calc.savedMoney(7 * DAY));
    }

    // 부분 일수 반영: 1.5일이면 15개비
    @Test
    public void 부분일수_반영() {
        QuitCalculator calc = new QuitCalculator("개비", 10, 1, 4500, 20);
        assertEquals(15, calc.skippedCigarettes(36 * HOUR));
        assertEquals(3375, calc.savedMoney(36 * HOUR)); // 15/20 × 4500
    }

    // 다 채우지 못한 개비는 버린다: 하루 10개비, 3시간 → 1.25개비 → 1개비
    @Test
    public void 개비_소수점_버림() {
        QuitCalculator calc = new QuitCalculator("개비", 10, 1, 4500, 20);
        assertEquals(1, calc.skippedCigarettes(3 * HOUR));
        assertEquals(225, calc.savedMoney(3 * HOUR));
    }

    // 금액 반올림: 1개비 × 4,510원 ÷ 20 = 225.5원 → 226원
    @Test
    public void 금액_반올림() {
        QuitCalculator calc = new QuitCalculator("개비", 24, 1, 4510, 20);
        assertEquals(1, calc.skippedCigarettes(1 * HOUR));
        assertEquals(226, calc.savedMoney(1 * HOUR));
    }

    // 기기 시각이 시작 시각보다 이르면 경과 시간 0
    @Test
    public void 음수_경과시간은_0() {
        long start = 1_000_000L;
        assertEquals(0, QuitCalculator.elapsedMillis(start, start - 5000));
    }

    @Test
    public void 경과시간_표시형식() {
        assertEquals("10일 5시간", QuitCalculator.formatElapsed(10 * DAY + 5 * HOUR + 30 * 60 * 1000));
    }

    // 갑 수 표시 형식: "N갑 M개비", 0인 부분은 생략
    @Test
    public void 갑수_표시형식() {
        QuitCalculator calc = new QuitCalculator("개비", 10, 1, 4500, 20);
        assertEquals("3갑 10개비", calc.formatSkipped(7 * DAY));
        assertEquals("3갑", calc.formatSkipped(6 * DAY));
        assertEquals("5개비", calc.formatSkipped(12 * HOUR));
        assertEquals("0개비", calc.formatSkipped(0));
    }

    // 1년이 지나도 int 범위를 넘지 않고 계산되는지 확인
    @Test
    public void 장기간_계산() {
        QuitCalculator calc = new QuitCalculator("개비", 20, 1, 4500, 20);
        assertEquals(7300, calc.skippedCigarettes(365 * DAY));
        assertEquals(1642500, calc.savedMoney(365 * DAY));
    }

    @Test(expected = IllegalArgumentException.class)
    public void 잘못된_입력은_예외() {
        new QuitCalculator("개비", 0, 1, 4500, 20);
    }
}
