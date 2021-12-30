package com.example.horizontalselectedviewlibrary;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.TintTypedArray;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;


import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by ruedy on 2017/5/5.
 */

public class HorizontalselectedView extends View {

    private Context context;
    private List<Pair<String, Integer>> strings = new ArrayList<Pair<String, Integer>>();//数据源字符串数组

    private int anInt;//改变选中的距离
    private TextPaint textPaint;
    private boolean firstVisible = true;
    private int width;//控件宽度
    private int height;//控件高度
    private Paint selectedPaint;//被选中文字的画笔
    private int n;
    private float downX;
    private float downY;
    private float anOffset;
    private float selectedTextSize;
    private int selectedColor;
    private float textSize;
    private int textColor;
    private Rect rect = new Rect();

    private int space;//图片间距

    private Paint selectedImgPaint;
    private Paint imgPaint;
    Bitmap seBitmapPaint;
    Bitmap bitmapPaint;
    int dp78;
    int dp48;
    int dp3;

    public HorizontalselectedView(Context context) {
        this(context, null);
    }

    public HorizontalselectedView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HorizontalselectedView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        dp78 = Util.dp2px(context, 78);
        dp48 = Util.dp2px(context, 48);
        dp3 = Util.dp2px(context, 3);

        space = Util.dp2px(context, 16);
        setWillNotDraw(false);
        setClickable(true);
        initAttrs(attrs);//初始化属性
        initPaint();//初始化画笔

    }

    /**
     * 初始化画笔
     */
    private void initPaint() {
        textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextSize(textSize);
        textPaint.setColor(textColor);
        textPaint.setTextAlign(Paint.Align.CENTER);
        selectedPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        selectedPaint.setColor(selectedColor);
        selectedPaint.setTextSize(selectedTextSize);

        selectedImgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        imgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        seBitmapPaint = resizeImage(BitmapFactory.decodeResource(context.getResources(), R.drawable.voice), dp78, dp78);
        bitmapPaint = resizeImage(BitmapFactory.decodeResource(context.getResources(), R.drawable.refresh), dp48, dp48);
    }


    /**
     * 初始化属性
     *
     * @param attrs
     */
    @SuppressLint("RestrictedApi")
    private void initAttrs(AttributeSet attrs) {
        TintTypedArray tta = TintTypedArray.obtainStyledAttributes(getContext(), attrs,
                R.styleable.HorizontalselectedView);
        //两种字体颜色和字体大小
        selectedTextSize = tta.getFloat(R.styleable.HorizontalselectedView_HorizontalselectedViewSelectedTextSize, 50);
        selectedColor = tta.getColor(R.styleable.HorizontalselectedView_HorizontalselectedViewSelectedTextColor, context.getResources().getColor(android.R.color.black));
        textSize = tta.getFloat(R.styleable.HorizontalselectedView_HorizontalselectedViewTextSize, 40);
        textColor = tta.getColor(R.styleable.HorizontalselectedView_HorizontalselectedViewTextColor, context.getResources().getColor(android.R.color.darker_gray));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.e("action", "onTouchEvent: " + event.getAction());
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = event.getX();//获得点下去的x坐标
                downY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE://复杂的是移动时的判断
                float scrollX = event.getX();

                if (n != 0 && n != strings.size() - 1)
                    anOffset = scrollX - downX;//滑动时的偏移量，用于计算每个是数据源文字的坐标值
                else {
                    anOffset = (float) ((scrollX - downX) / 1.5);//当滑到两端的时候添加一点阻力
                }

                if (scrollX > downX) {
                    //向右滑动，当滑动距离大于每个单元的长度时，则改变被选中的文字。
                    if (scrollX - downX >= anInt) {
                        if (n > 0) {
                            anOffset = 0;
                            n = n - 1;
                            downX = scrollX;
                        }
                    }
                } else {

                    //向左滑动，当滑动距离大于每个单元的长度时，则改变被选中的文字。
                    if (downX - scrollX >= anInt) {

                        if (n < strings.size() - 1) {
                            anOffset = 0;
                            n = n + 1;
                            downX = scrollX;
                        }
                    }
                }

                invalidate();
                break;

            case MotionEvent.ACTION_UP:
                //抬起手指时，偏移量归零，相当于回弹。
                anOffset = 0;
                invalidate();

                break;
            default:
                break;
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (firstVisible) {//第一次绘制的时候得到控件 宽高；
            width = getWidth();
            height = getHeight();
            anInt = dp78;
            firstVisible = false;
        }
        if (n >= 0 && n <= strings.size() - 1) {//加个保护；防止越界

            String s = strings.get(n).first;//得到被选中的文字
            /**
             * 得到被选中文字 绘制时所需要的宽高
             */
            selectedPaint.getTextBounds(s, 0, s.length(), rect);

            Bitmap mSeBitmap = selectedBitmap(strings.get(n).second);
            canvas.drawBitmap(mSeBitmap, getWidth() / 2 - mSeBitmap.getWidth() / 2 + anOffset, 0, selectedImgPaint);

            for (int i = 0; i < strings.size(); i++) {//遍历strings，把每个地方都绘制出来，

                if (i != n) {
                    // 绘制文字
                    int uNSelectedTextHeight = getHeight() / 2 + dp48 / 2 + dp3 + rect.height() / 2;

                    if (i - n < 0) {
                        canvas.drawText(strings.get(i).first, getWidth() / 2 - dp78 / 2 + (i - n) * (dp48 / 2 + space) + (i - n + 1) * dp48 / 2 + anOffset, uNSelectedTextHeight, textPaint);//画出每组文字
                    } else {
                        canvas.drawText(strings.get(i).first, getWidth() / 2 + dp78 / 2 + (i - n) * (dp48 / 2 + space) + (i - n - 1) * dp48 / 2 + anOffset, uNSelectedTextHeight, textPaint);//画出每组文字
                    }

                    // 绘制图片
                    Bitmap mUnSeBitmap = unSelectedBitmap(strings.get(i).second);
                    int uNSelectedImgHeight = getHeight() / 2 - rect.height() / 2 - mUnSeBitmap.getHeight() / 2;

                    if (i - n < 0) {
                        canvas.drawBitmap(mUnSeBitmap, getWidth() / 2 - dp78 / 2 + (i - n) * (space + dp48) + anOffset, uNSelectedImgHeight, imgPaint);
                    } else {
                        canvas.drawBitmap(mUnSeBitmap, getWidth() / 2 + dp78 / 2 + (i - n) * (space + dp48) - dp48 + anOffset, uNSelectedImgHeight, imgPaint);
                    }
                }
            }


        }

    }

    public Bitmap selectedBitmap(int drawable) {
        return resizeImage(BitmapFactory.decodeResource(context.getResources(), drawable), dp78, dp78);
    }

    public Bitmap unSelectedBitmap(int drawable) {
        return resizeImage(BitmapFactory.decodeResource(context.getResources(), drawable), dp48, dp48);
    }


    public Bitmap resizeImage(Bitmap bitmap, int width, int height) {
        int bmpWidth = bitmap.getWidth();
        int bmpHeight = bitmap.getHeight();

        float scaleWidth = ((float) width) / bmpWidth;
        float scaleHeight = ((float) height) / bmpHeight;

        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);

        return Bitmap.createBitmap(bitmap, 0, 0, bmpWidth, bmpHeight, matrix, true);
    }

    /**
     * 向左移动一个单元
     */
    public void setAnLeftOffset() {
        if (n < strings.size() - 1) {
            n = n + 1;
            invalidate();
        }
    }

    /**
     * 向右移动一个单元
     */
    public void setAnRightOffset() {
        if (n > 0) {
            n = n - 1;
            invalidate();
        }
    }

    /**
     * 设置个数据源
     *
     * @param strings 数据源String集合
     */

    public void setData(List<Pair<String, Integer>> strings) {
        this.strings = strings;
        n = strings.size() / 2;
        invalidate();
    }

    /**
     * 获得被选中的文本
     *
     * @return 被选中的文本
     */
    public String getSelectedString() {
        if (strings.size() != 0) {
            return strings.get(n).first;
        }
        return null;
    }

    public void getOnClickView(){

    }
}
