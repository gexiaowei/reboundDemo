package com.demo.vivian.test;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.facebook.rebound.BaseSpringSystem;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringListener;
import com.facebook.rebound.SpringSystem;
import com.facebook.rebound.SpringUtil;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by vivian on 15/1/3.
 */
public class Chart extends View implements SpringListener {
    //3种颜色值
    private final static int[] COLORS = new int[]{0xFFF34C43, 0xFFF39F56, 0xFFE6C460};
    private final static int TYPE_LENGTH = COLORS.length;

    //rebound的一些数据
    private final BaseSpringSystem mSpringSystem = SpringSystem.create();
    private Spring mScaleSpring;

    //需要绘制的数据
    private List<Float[]> datas;
    //所有数据的最大值
    private float maxValue = 0;
    //计算过程中临时产生的一些数据
    private List<CalculateData> calculateDatas = new ArrayList<CalculateData>();

    //画笔工具
    private Paint mPaint;
    //需要绘制的rect
    private List<RectF[]> rectFList = new ArrayList<RectF[]>();

    //预留的参数
    //TODO 留于计算左边字体宽度和底部字体高度用和其他地方用
    private float leftWidth = 0,
            rightWidth = 0,
            topHeight = 0,
            bottomHeight = 0;

    //测试数据
    private ChartHandler mChartHandler = new ChartHandler(this);
    private int testDataLength = 5;

    /**
     * 构造函数
     *
     * @param context
     */
    public Chart(Context context) {
        super(context);
        init();
    }

    /**
     * 构造函数
     *
     * @param context
     * @param attrs
     */
    public Chart(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * 构造函数
     *
     * @param context
     * @param attrs
     * @param defStyleAttr
     */
    public Chart(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * 初始化信息
     */
    private void init() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
    }

    /**
     * 获取测试数据
     *
     * @return 测试数据
     */
    private List<Float[]> getTestDatas(int maxLength) {
        List<Float[]> testdatas = new ArrayList<Float[]>();
        for (int m = 0; m < maxLength; m++) {
            Float[] tempData = new Float[TYPE_LENGTH];
            for (int n = 0; n < TYPE_LENGTH; n++) {
                tempData[n] = 100 * (float) Math.random();
            }
            testdatas.add(tempData);
        }
        return testdatas;
    }

    /**
     * 更新测试数据
     */
    private List<Float[]> updataTest() {
        testDataLength += 3;
        if (testDataLength > 15) {
            return null;
        }
        return getTestDatas(testDataLength);
    }


    /**
     * 测试数据用方法
     */
    public void setValue() {
        setDatas(getTestDatas(testDataLength));
    }

    /**
     * 设置数据
     *
     * @param datas 数据
     */
    public void setDatas(List<Float[]> datas) {
        this.datas = datas;
        calculate();
        mScaleSpring = mSpringSystem.createSpring();
        SpringConfig springConfig = new SpringConfig(100, 10);
        mScaleSpring.setSpringConfig(springConfig);
        mScaleSpring.addListener(this);
        mScaleSpring.setEndValue(1);
    }

    /**
     * 计算数据
     */
    private void calculate() {
        //清除临时数据
        calculateDatas.clear();
        //计算最大值
        //计算每个数据的百分比和each的百分比
        maxValue = 0;
        int m = 0;
        for (; m < datas.size(); m++) {
            CalculateData calculateData = new CalculateData();
            float tempValue = 0;
            Float[] tempData = datas.get(m);
            float[] eachPercents = new float[tempData.length];
            int n = 0;
            for (; n < tempData.length; n++) {
                tempValue += tempData[n];
            }
            for (n = 0; n < tempData.length; n++) {
                eachPercents[n] = tempData[n] / tempValue;
            }
            calculateData.eachPercents = eachPercents;
            calculateData.totalValue = tempValue;
            calculateDatas.add(calculateData);
            maxValue = Math.max(maxValue, tempValue);
        }

        for (m = 0; m < calculateDatas.size(); m++) {
            calculateDatas.get(m).percent = calculateDatas.get(m).totalValue / maxValue;
        }
    }

    /**
     * 计算需要绘制的Rect
     *
     * @param coefficient 当前变化系数
     */
    private void calculateRects(float coefficient) {
        //TODO 做进一步优化 可以复用RectF
        rectFList.clear();
        float totalWidth = getMeasuredWidth() - leftWidth - rightWidth;
        float totalHeight = getMeasuredHeight() - bottomHeight - topHeight;
        float perWidth = totalWidth / (calculateDatas.size() * 2 - 1);
        for (int m = 0; m < calculateDatas.size(); m++) {
            CalculateData tempData = calculateDatas.get(m);
            RectF[] rectFs = new RectF[tempData.eachPercents.length];
            float tempPercent = 1;
            for (int n = 0; n < tempData.eachPercents.length; n++) {
                //TODO 做进一步优化 可以复用RectF
                RectF rectF = new RectF();
                rectF.left = leftWidth + perWidth * 2 * m;
                rectF.right = rectF.left + perWidth;
                rectF.bottom = totalHeight + topHeight;
                rectF.top = rectF.bottom - totalHeight * tempData.percent * tempPercent * coefficient;
                rectFs[n] = rectF;
                tempPercent -= tempData.eachPercents[n];
            }
            rectFList.add(rectFs);
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        setBackgroundColor(0xFFDAE5E7);
        for (int m = 0; m < rectFList.size(); m++) {
            RectF[] tempRectFs = rectFList.get(m);
            for (int n = 0; n < tempRectFs.length; n++) {
                mPaint.setColor(COLORS[n]);
                canvas.drawRect(tempRectFs[n], mPaint);
            }
        }
        super.onDraw(canvas);
    }

    @Override
    public void onSpringUpdate(Spring spring) {
        Log.d("info", "onSpringUpdate");
        double value = SpringUtil.mapValueFromRangeToRange(spring.getCurrentValue(), 0.5, 1, 0, 1);
        calculateRects((float) value);
    }

    @Override
    public void onSpringAtRest(Spring spring) {
        Log.d("info", "onSpringAtRest");
        mChartHandler.sendEmptyMessageDelayed(1, 2000);
    }

    @Override
    public void onSpringActivate(Spring spring) {
        Log.d("info", "onSpringActivate");
    }

    @Override
    public void onSpringEndStateChange(Spring spring) {
        Log.d("info", "onSpringEndStateChange");
    }

    /**
     * 计算临时用类
     */
    static class CalculateData {
        float percent;
        float totalValue;
        float[] eachPercents;
    }

    static class ChartHandler extends Handler {

        private WeakReference<Chart> mChart;

        public ChartHandler(Chart chart) {
            mChart = new WeakReference<Chart>(chart);
        }

        @Override
        public void handleMessage(Message msg) {
            if (mChart.get() == null) {
                return;
            }
            List<Float[]> datas = mChart.get().updataTest();
            if (datas != null) {
                mChart.get().setDatas(datas);
            }

        }
    }
}
