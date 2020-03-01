package com.mallotec.reb.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.text.Layout;
import android.text.StaticLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.animation.LinearInterpolator;

import androidx.appcompat.widget.AppCompatTextView;

/**
 * Created by reborn on 2020/2/29.
 * <p>
 * 打印机效果 TextView
 * <p>
 * 思路：
 * 从外部拿到字符串后，从0位置开始逐个拿出字符串的字符，用线程安全的StringBuffer存每一次拿出的字符，然后在动画更新的同时重绘TextView
 * <p>
 * 绘制思路：
 * 1.计算好画布大小
 * 2.计算好 BaseLine，若继承的是 TextView 而不是 View 则可直接通过 getBaseLine() 获得。详情参考 https://www.cnblogs.com/tianzhijiexian/p/4297664.html
 * 3.在 BaseLine 位置开始绘制文字
 *
 * TODO：支持padding等属性，目前建议与ScrollView搭配使用
 */
public class PrinterTextView extends AppCompatTextView {

    private final String TAG = "PrinterTextView";

    /**
     * 打印机动画回调
     */
    private TextAnimationListener textAnimationListener;
    /**
     * 每个字出现的间隔
     */
    private int duration = 100;
    /**
     * 针对属性的值进行动画 ，不会对UI造成改变
     */
    private ValueAnimator printerAnimator;

    /**
     * 用来保存字符串
     */
    private CharSequence textArray;
    /**
     * 字符串缓冲区
     */
    private StringBuffer textBuffer;
    /**
     * 字符总字数
     */
    private int textCount;
    /**
     * 当前字符串索引位置
     */
    private int currentIndex = -1;

    /**
     * 字符串是否初始化完毕
     */
    private boolean isStringInitialized = false;

    /**
     * 用于支持多行文本
     */
    private StaticLayout staticLayout;
    private StaticLayout allContentStaticLayout = null;

    public PrinterTextView(Context context) {
        super(context);
        textBuffer = new StringBuffer();
        initPrintAnimation(duration);
        setText("");
        // 清空 TextView 后必须重新设置预设宽度，否则不执行 onDraw()
        setWidth(Math.round(getPaint().measureText(textArray.toString())));
    }

    public PrinterTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        textBuffer = new StringBuffer();
        initPrintAnimation(duration);
        setText("");
        // 清空 TextView 后必须重新设置预设宽度，否则不执行 onDraw()
        setWidth(Math.round(getPaint().measureText(textArray.toString())));
    }

    public PrinterTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        textBuffer = new StringBuffer();
        initPrintAnimation(duration);
        setText("");
        // 清空 TextView 后必须重新设置预设宽度，否则不执行 onDraw()
        setWidth(Math.round(getPaint().measureText(textArray.toString())));
    }

    public void setTextAnimationListener(TextAnimationListener textAnimationListener) {
        this.textAnimationListener = textAnimationListener;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (textBuffer != null) {
            // 字符串缓冲区必须被初始化后才能进行绘制
            drawText(canvas, textBuffer.toString());
        }
    }

    /**
     * 绘制文字
     *
     * @param canvas 画布
     * @param text   要绘制的文字
     */
    private void drawText(Canvas canvas, String text) {
        // 绘制文字
//        canvas.drawText(text, getPaddingStart(), getBaseline(), getPaint());
        // 用于支持多行文本
        if (allContentStaticLayout != null) {
            staticLayout = allContentStaticLayout;
        } else {
            staticLayout = new StaticLayout(text, getPaint(), canvas.getWidth(), Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
        }
        staticLayout.draw(canvas);
        Log.i(TAG, "当前行：" + staticLayout.getLineCount());
        Log.i(TAG, "当前宽度：" + canvas.getWidth());
        Log.i(TAG, "当前高度：" + staticLayout.getHeight());
    }

    private void initPrintAnimation(int duration) {
        isStringInitialized = true;
        printerAnimator = ValueAnimator.ofInt(0, textCount - 1);
        // 设置动画总时间
        printerAnimator.setDuration(textCount * duration);
        // 设置以常量速率改变的插值器
        printerAnimator.setInterpolator(new LinearInterpolator());
        // 设置值更新监听器
        printerAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int index = (int) animation.getAnimatedValue();
                // 避免初始化时 index 为 -1 导致的数组越界问题
                if (currentIndex != index) {
                    textBuffer.append(textArray.charAt(index));
                    currentIndex = index;
                    if (index == textCount - 1) {
                        if (textAnimationListener != null) {
                            textAnimationListener.finish();
                        }
                    }
                    if (staticLayout != null) {
                        // 修复高度不变导致内容无法显示完全的问题
                        setHeight(staticLayout.getHeight());
                    }
                    invalidate();
                }
                Log.i(TAG, "当前index：" + index);
            }
        });
    }

    /**
     * 开始播放动画
     */
    public void startPrintAnimation() {
        if (printerAnimator != null) {
            printerAnimator.start();
        }
    }

    /**
     * 结束动画
     * @return 停止动画结果
     */
    public boolean stopPrintAnimation() {
        if (printerAnimator == null) {
            return false;
        }
        // 如果动画不在运行中说明已经结束了
        if (!printerAnimator.isRunning()) {
            return true;
        }
        printerAnimator.end();
        isStringInitialized = false;
        return true;
    }

    /**
     * 显示全文
     */
    public void showAllText() {
        if (stopPrintAnimation()) {
            textBuffer.delete(0, textBuffer.length());
            if (staticLayout != null) {
                // 修复高度不变导致内容无法显示完全的问题
                allContentStaticLayout = new StaticLayout(textArray, getPaint(), getWidth(), Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
                setHeight(allContentStaticLayout.getHeight());
            }
            invalidate();
        }
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
        // 对象还没创建时已经进来，可做相应初始化，一旦动画开始则即使TextView的text有变化也不可再缓存
        if (!isStringInitialized) {
            textCount = lengthAfter;
            textArray = text;
        }
    }

    /**
     * 打印机动画回调
     */
    public interface TextAnimationListener {
        void finish();
    }
}
