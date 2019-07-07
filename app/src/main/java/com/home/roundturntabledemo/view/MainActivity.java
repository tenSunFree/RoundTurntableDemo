package com.home.roundturntabledemo.view;

import android.annotation.SuppressLint;
import android.app.Service;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import com.home.roundturntabledemo.R;
import com.home.roundturntabledemo.data.RoundTurntableData;
import com.home.roundturntabledemo.utils.NoDoubleClickUtils;
import com.home.roundturntabledemo.view.component.RoundTurntableView;
import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private String[] textArray = new String[]{
            "糖糖果凍",
            "青夢",
            "頑皮鬼",
            "石炭毛毛",
            "淘氣小惡魔",
            "比波西力士"
    };
    private int[] imageArray = new int[]{
            R.drawable.icon_activity_main_monster1,
            R.drawable.icon_activity_main_monster2,
            R.drawable.icon_activity_main_monster3,
            R.drawable.icon_activity_main_monster4,
            R.drawable.icon_activity_main_monster5,
            R.drawable.icon_activity_main_monster6,
    };
    private int resultPosition;
    private boolean isRotationEnd = true;
    private int totalNumber = textArray.length; // 轉盤總共幾個Item
    private long currentDegrees = 0;
    private List<RoundTurntableData> roundTurntableDataList = new ArrayList<>();
    private RoundTurntableView roundTurntableView;
    private TextView resultTextView;
    private Vibrator vibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Animation.AnimationListener animationListener = initializeAnimationListener();
        initializeView(animationListener);
    }

    private Animation.AnimationListener initializeAnimationListener() {
        vibrator = (Vibrator) getApplication().getSystemService(Service.VIBRATOR_SERVICE); // 取得震動服務
        resultTextView = (TextView) findViewById(R.id.resultTextView);
        return new Animation.AnimationListener() {
            @SuppressLint({"SetTextI18n", "CheckResult"})
            @Override
            public void onAnimationEnd(Animation animation) {
                roundTurntableView.setResult(resultPosition);
                resultTextView.setText("恭喜你獲得 " + textArray[resultPosition]);
                isRotationEnd = true;
            }

            @SuppressLint("CheckResult")
            @Override
            public void onAnimationStart(Animation animation) {
                int initialDelay = 0; // 幾豪秒後才開始
                int period = 70; // 每隔幾豪秒發射一個long值出去
                final int take = (int) (animation.getDuration() / period); // 發射次數
                final double endingRatio = 0.7; // 用來區分是否快要結束轉動
                final int repeat = -1;
                // 0豪秒後, 每間隔70豪秒發射一個Long值出去, 數值從0開始, 每次加1, 一共發射take次
                Observable.interval(initialDelay, period, TimeUnit.MILLISECONDS)
                        .take(take)
                        .subscribe(new Consumer<Long>() {
                            @Override
                            public void accept(Long aLong) {
                                if (aLong < take * endingRatio) {
                                    vibrator.vibrate(new long[]{30, 30}, repeat);
                                } else {
                                    vibrator.vibrate(new long[]{60, 10}, repeat);
                                }
                            }
                        });
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        };
    }

    private void initializeView(final Animation.AnimationListener animationListener) {
        final ImageView arrowImageView = (ImageView) findViewById(R.id.arrowImageView);
        getRoundTurntableDataList();
        roundTurntableView = (RoundTurntableView) findViewById(R.id.rouletteImage);
        roundTurntableView.setBitInfos(roundTurntableDataList);
        roundTurntableView.post(new Runnable() {
            @Override
            public void run() {
                FrameLayout.LayoutParams layoutParams;
                layoutParams = (FrameLayout.LayoutParams) arrowImageView.getLayoutParams();
                layoutParams.height = (int) (roundTurntableView.getBackgroundWidth() * 0.2);
                layoutParams.width = (int) (roundTurntableView.getBackgroundWidth() * 0.2);
                arrowImageView.setLayoutParams(layoutParams); // 設定ImageView的大小
            }
        });
        arrowImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!NoDoubleClickUtils.isDoubleClick()) {
                    startRotateAnimation(animationListener, arrowImageView);
                }
            }
        });
    }

    private void startRotateAnimation(Animation.AnimationListener animationListener, ImageView arrowImageView) {
        if (isRotationEnd) {
            isRotationEnd = false;
            resultTextView.setText("？？？");
            roundTurntableView.removeResult();
            int roundAngle = 360;
            int roundAngleMultiple = 10;
            int randomNumber = new Random().nextInt(roundAngle) + roundAngle * roundAngleMultiple; // 取得轉圈角度的亂數
            float fromDegrees = currentDegrees;
            float toDegrees;
            int min = 0;
            if (currentDegrees != min) {
                toDegrees = currentDegrees + (roundAngle - currentDegrees) + randomNumber;
            } else {
                toDegrees = currentDegrees + randomNumber;
            }
            int pivotXType = Animation.RELATIVE_TO_PARENT;
            float pivotXValue = 0.5f;
            int pivotYType = Animation.RELATIVE_TO_PARENT;
            float pivotYValue = 0.5f;
            RotateAnimation rotateAnimation = new RotateAnimation(
                    fromDegrees, // 開始角度
                    toDegrees, // 結束角度
                    pivotXType, // X軸的伸縮模式
                    pivotXValue, // X座標的伸縮值
                    pivotYType, // Y軸的伸縮模式
                    pivotYValue // Y座標的伸縮值
            );
            int baseRange = roundAngle / totalNumber;
            double baseRangeRatio = 0.3;
            boolean isErrorNumber = true;
            long newToDegrees = (long) toDegrees % roundAngle;
            for (int i = min; i < totalNumber; i++) {
                int number = i * baseRange;
                if (i == min) {
                    int minNumber = (int) (roundAngle - baseRange * baseRangeRatio);
                    int maxNumber = (int) (baseRange * baseRangeRatio);
                    if (newToDegrees <= maxNumber && newToDegrees >= number) {
                        isErrorNumber = false;
                        resultPosition = i;
                        break;
                    }
                    if (newToDegrees >= minNumber && newToDegrees <= roundAngle) {
                        isErrorNumber = false;
                        resultPosition = i;
                        break;
                    }
                } else {
                    int minNumber = (int) (number - baseRange * baseRangeRatio);
                    int maxNumber = (int) (number + baseRange * baseRangeRatio);
                    if (newToDegrees <= maxNumber && newToDegrees >= minNumber) {
                        isErrorNumber = false;
                        resultPosition = totalNumber - i;
                        break;
                    }
                }
            }
            if (!isErrorNumber) {
                rotateAnimation.setDuration((long) randomNumber); // 動畫開始到結束的執行時間
                rotateAnimation.setFillAfter(true); // 將圖片停在旋轉停止的角度
                rotateAnimation.setInterpolator(new DecelerateInterpolator()); // 減速度插值器; 減速度的變化速率, 值越大, 動畫初始移動速度越快, 然後以更慢的速度運動至結束
                rotateAnimation.setAnimationListener(animationListener);
                roundTurntableView.setAnimation(rotateAnimation);
                roundTurntableView.startAnimation(rotateAnimation);
                currentDegrees = newToDegrees;
            } else {
                isRotationEnd = true;
                startRotateAnimation(animationListener, arrowImageView);
            }
        }
    }

    public void getRoundTurntableDataList() {
        for (int i = 0; i < textArray.length; i++) {
            roundTurntableDataList.add(new RoundTurntableData(
                    textArray[i], BitmapFactory.decodeResource(getResources(), imageArray[i])));
        }
    }
}
