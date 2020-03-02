package com.mallotec.reb.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
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
 * 初始绘制思路：
 * 1.计算好画布大小
 * 2.计算好 BaseLine，若继承的是 TextView 而不是 View 则可直接通过 getBaseLine() 获得。详情参考 https://www.cnblogs.com/tianzhijiexian/p/4297664.html
 * 3.在 BaseLine 位置开始绘制文字
 *
 * 改进后思路：
 * 由于直接继承 TextView，因此我们可以直接使用 TextView 的 setText() 方法实现，不需要自己实现绘制文字，同时也随之支持了 Padding 等属性。
 *
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

    public PrinterTextView(Context context) {
        super(context);
        textBuffer = new StringBuffer();
        initPrintAnimation(duration);
        // 避免在 xml 设置了 android:text 属性导致刚开始的时候会显示出所有内容
        setText("");
    }

    public PrinterTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        textBuffer = new StringBuffer();
        initPrintAnimation(duration);
        // 避免在 xml 设置了 android:text 属性导致刚开始的时候会显示出所有内容
        setText("");
    }

    public PrinterTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        textBuffer = new StringBuffer();
        initPrintAnimation(duration);
        // 避免在 xml 设置了 android:text 属性导致刚开始的时候会显示出所有内容
        setText("");
    }

    public void setTextAnimationListener(TextAnimationListener textAnimationListener) {
        this.textAnimationListener = textAnimationListener;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    private void initPrintAnimation(int duration) {
        isStringInitialized = true;
        printerAnimator = ValueAnimator.ofInt();
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

                    setText(textBuffer.toString());

                    if (index == textCount - 1) {
                        if (textAnimationListener != null) {
                            textAnimationListener.finish();
                        }
                        // 动画结束后要回收和重置index
                        recycle();
                    }
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
            isStringInitialized = true;
            // 因为每次开始都有可能是不同的字串，所以有变动的初始化放在开始动画的地方而不是初始化动画的地方
            // 设置动画阈值
            printerAnimator.setIntValues(0, textCount - 1);
            // 设置动画总时间
            printerAnimator.setDuration(textCount * duration);
            printerAnimator.start();
        }
    }

    /**
     * 重置和回收
     */
    private void recycle() {
        isStringInitialized = false;
        textBuffer.delete(0, textBuffer.length());
        currentIndex = -1;
        // 不 cancel 动画会出错，详情自己注释后尝试同一个 View 实例先后播放两个不同的字串即可知晓
        printerAnimator.cancel();
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
        // 动画结束后要回收和重置index
        recycle();
        return true;
    }

    /**
     * 显示全文
     */
    public void showAllText() {
        if (stopPrintAnimation()) {
            setText(textArray);
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
