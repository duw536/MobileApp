package com.example.smoking;

/**
 * 금연 현황 계산 클래스 (기획서 3장 기준).
 *
 * 흡연량을 "하루 평균 개비 수"로 바꾼 뒤, 부분 일수까지 반영한 경과 일수를 곱한다.
 *   예) 하루 10개비, 1.5일 경과 → 15개비
 *
 * 화면(Activity)과 저장(SharedPreferences)에 의존하지 않으므로
 * 숫자만 넣어서 바로 테스트할 수 있다.
 */
public class QuitCalculator {

    public static final String UNIT_CIGARETTE = "개비";
    public static final String UNIT_PACK = "갑";

    private static final long MILLIS_PER_HOUR = 60L * 60 * 1000;
    private static final long MILLIS_PER_DAY = 24L * MILLIS_PER_HOUR;

    private final String smokingUnit;      // "개비" 또는 "갑"
    private final double smokingAmount;    // 기간 동안 피운 양
    private final int smokingPeriod;       // 흡연 기간(일). 하루 기준이면 1
    private final int cigarettePrice;      // 한 갑 가격(원)
    private final int cigarettesPerPack;   // 한 갑당 개비 수

    /**
     * 사용 예
     *   하루 10개비   → new QuitCalculator("개비", 10, 1, 4500, 20)
     *   4일에 2갑     → new QuitCalculator("갑", 2, 4, 4500, 20)
     */
    public QuitCalculator(String smokingUnit, double smokingAmount, int smokingPeriod,
                          int cigarettePrice, int cigarettesPerPack) {
        if (smokingAmount <= 0 || smokingPeriod <= 0
                || cigarettePrice <= 0 || cigarettesPerPack <= 0) {
            throw new IllegalArgumentException("입력값은 모두 0보다 커야 합니다.");
        }
        if (!UNIT_CIGARETTE.equals(smokingUnit) && !UNIT_PACK.equals(smokingUnit)) {
            throw new IllegalArgumentException("단위는 '개비' 또는 '갑'이어야 합니다.");
        }
        this.smokingUnit = smokingUnit;
        this.smokingAmount = smokingAmount;
        this.smokingPeriod = smokingPeriod;
        this.cigarettePrice = cigarettePrice;
        this.cigarettesPerPack = cigarettesPerPack;
    }

    /** 경과 시간(밀리초). 기기 시각이 시작 시각보다 이르면 0으로 처리한다. */
    public static long elapsedMillis(long quitStartTime, long now) {
        return Math.max(0L, now - quitStartTime);
    }

    /** 경과 일수. 부분 일수를 포함한다 (예: 36시간 → 1.5일). */
    public static double elapsedDays(long elapsedMillis) {
        return (double) elapsedMillis / MILLIS_PER_DAY;
    }

    /** 하루 평균 흡연 개비 수. 예) 4일에 2갑(20개비/갑) → 10.0 */
    public double cigarettesPerDay() {
        double totalCigarettes = UNIT_PACK.equals(smokingUnit)
                ? smokingAmount * cigarettesPerPack
                : smokingAmount;
        return totalCigarettes / smokingPeriod;
    }

    /**
     * 미흡연 개비 수 = 하루 평균 개비 수 × 경과 일수.
     * 화면에는 정수 개비만 보여 주므로 소수점 아래는 버린다.
     */
    public long skippedCigarettes(long elapsedMillis) {
        // 부동소수점 오차(예: 14.999999)로 1개비가 덜 세지는 것을 막기 위해 아주 작은 값을 더한다.
        return (long) Math.floor(cigarettesPerDay() * elapsedDays(elapsedMillis) + 1e-9);
    }

    /** 미흡연 갑 수. 예) 70개비 → 3.5갑 */
    public double skippedPacks(long elapsedMillis) {
        return (double) skippedCigarettes(elapsedMillis) / cigarettesPerPack;
    }

    /**
     * 절약 금액 = (미흡연 개비 수 ÷ 한 갑당 개비 수) × 한 갑 가격, 원 단위 반올림.
     * 화면에 보이는 개비 수와 금액이 어긋나지 않도록 정수 개비 수로 계산한다.
     */
    public long savedMoney(long elapsedMillis) {
        double packs = (double) skippedCigarettes(elapsedMillis) / cigarettesPerPack;
        return Math.round(packs * cigarettePrice);
    }

    /**
     * 미흡연 담배를 "N갑 M개비" 형식으로 만든다.
     *   70개비 → "3갑 10개비", 60개비 → "3갑", 5개비 → "5개비", 0개비 → "0개비"
     */
    public String formatSkipped(long elapsedMillis) {
        long cigarettes = skippedCigarettes(elapsedMillis);
        long packs = cigarettes / cigarettesPerPack;
        long rest = cigarettes % cigarettesPerPack;
        if (packs == 0) return rest + "개비";
        if (rest == 0) return packs + "갑";
        return packs + "갑 " + rest + "개비";
    }

    /** 경과 시간을 "N일 N시간" 형식으로 만든다. */
    public static String formatElapsed(long elapsedMillis) {
        long days = elapsedMillis / MILLIS_PER_DAY;
        long hours = (elapsedMillis % MILLIS_PER_DAY) / MILLIS_PER_HOUR;
        return days + "일 " + hours + "시간";
    }
}
