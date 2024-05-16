/*
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.scankitdemo.draw;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import androidx.annotation.Nullable;

import com.example.scankitdemo.CommonActivity;
import com.example.scankitdemo.CommonHandler;
import com.example.scankitdemo.R;
import com.huawei.hms.ml.scan.HmsScan;

import java.util.ArrayList;
import java.util.List;


public class ScanResultView extends FrameLayout {

    private final Object lock = new Object();
    protected float widthScaleFactor = 1.0f;
    protected float heightScaleFactor = 1.0f;
    protected float previewWidth;
    protected float previewHeight;

    private OnCloseViewListener closeViewListener;

    public interface OnCloseViewListener {
        void onClose();
    }

    private final List<HmsScanGraphic> hmsScanGraphics = new ArrayList<>();

    public ScanResultView(Context context) {
        super(context);
    }

    public ScanResultView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public void setOnCloseViewListener(OnCloseViewListener closeViewListener) {
        this.closeViewListener = closeViewListener;
    }

    public void clear() {
        synchronized (lock) {
            hmsScanGraphics.clear();
        }
        postInvalidate();
    }

    public void add(HmsScanGraphic graphic) {
        synchronized (lock) {
            hmsScanGraphics.add(graphic);
        }
    }

    public void setCameraInfo(int previewWidth, int previewHeight) {
        synchronized (lock) {
            this.previewWidth = previewWidth;
            this.previewHeight = previewHeight;
        }
//        postInvalidate();
        addChildView();
    }

    private void addChildView() {
        synchronized (lock) {

            WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
            Display defaultDisplay = windowManager.getDefaultDisplay();
            Point outPoint = new Point();
            defaultDisplay.getRealSize(outPoint);

            if ((previewWidth != 0) && (previewHeight != 0)) {
                // 屏幕的宽高，和图片的宽高，计算出比例
                widthScaleFactor = CommonActivity.sw / CommonHandler.bitmapH;// bitmap2.width
                heightScaleFactor = CommonActivity.sh / CommonHandler.bitmapW; // bitmap2.height
            }

            removeAllViews();
            boolean b = false;
            for (HmsScanGraphic graphic : hmsScanGraphics) {
                graphic.addView(this, b);
                b = true;
            }
            ImageView imageView = new ImageView(getContext());
            LayoutParams params = new LayoutParams(80, 80);
//            canvas.drawText(hmsScan.getOriginalValue(), left, top, hmsScanResult);
            params.leftMargin = 40;
            params.topMargin = 40;
            imageView.setImageResource(R.drawable.back);
            imageView.setOnClickListener(view -> {
                if (closeViewListener != null) closeViewListener.onClose();
            });
            addView(imageView, params);
        }
    }

    /**
     * Draw MultiCodes on screen.
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        synchronized (lock) {
            if ((previewWidth != 0) && (previewHeight != 0)) {
                widthScaleFactor = (float) canvas.getWidth() / (float) previewWidth;
                heightScaleFactor = (float) canvas.getHeight() / (float) previewHeight;

//                widthScaleFactor = CommonActivity.sw / 1080f;
//                heightScaleFactor = CommonActivity.sh / 2340f;
//
//                widthScaleFactor = CommonActivity.sw / 1080f;
//                heightScaleFactor = CommonActivity.sh / 2340f;
            }

            for (HmsScanGraphic graphic : hmsScanGraphics) {
                graphic.drawGraphic(canvas);
            }
        }
    }

    public interface ClickResult {
        void onClick(HmsScan hmsScan);
    }

    public static class HmsScanGraphic {

        private static final int TEXT_COLOR = Color.WHITE;
        private static final float TEXT_SIZE = 35.0f;
        private static final float STROKE_WIDTH = 4.0f;

        private final Paint rectPaint;
        private final Paint hmsScanResult;
        private final HmsScan hmsScan;
        private ScanResultView scanResultView;
        private Bitmap sourceBitmap;

        private ClickResult clickResult;

        public HmsScanGraphic(ScanResultView scanResultView, HmsScan hmsScan) {
            this(scanResultView, hmsScan, Color.WHITE);
        }

        public HmsScanGraphic(ScanResultView scanResultView, HmsScan hmsScan, int color) {
            this.scanResultView = scanResultView;
            this.hmsScan = hmsScan;

            rectPaint = new Paint();
            rectPaint.setColor(color);
            rectPaint.setStyle(Paint.Style.STROKE);
            rectPaint.setStrokeWidth(STROKE_WIDTH);

            hmsScanResult = new Paint();
            hmsScanResult.setColor(TEXT_COLOR);
            hmsScanResult.setTextSize(TEXT_SIZE);
        }

        public HmsScanGraphic(ScanResultView scanResultView, HmsScan hmsScan, int color, Bitmap bitmap) {
            this.scanResultView = scanResultView;
            this.hmsScan = hmsScan;
            this.sourceBitmap = bitmap;

            rectPaint = new Paint();
            rectPaint.setColor(color);
            rectPaint.setStyle(Paint.Style.STROKE);
            rectPaint.setStrokeWidth(STROKE_WIDTH);

            hmsScanResult = new Paint();
            hmsScanResult.setColor(TEXT_COLOR);
            hmsScanResult.setTextSize(TEXT_SIZE);
        }

        public void setOnClickListener(ClickResult result) {
            this.clickResult = result;
        }

        public void drawGraphic(Canvas canvas) {
            if (hmsScan == null) {
                return;
            }

            RectF rect = new RectF(hmsScan.getBorderRect());
            RectF other = new RectF();
            other.left = canvas.getWidth() - scaleX(rect.top);
            other.top = scaleY(rect.left);
            other.right = canvas.getWidth() - scaleX(rect.bottom);
            other.bottom = scaleY(rect.right);
            canvas.drawRect(other, rectPaint);

            Log.d("im-tag", "rect 2 = " + rect.toString());
            Log.d("im-tag", "dest 2 = " + other.toString());

            float left = other.top;
            float top = other.left;
            Log.d("im-tag", "ori = " + hmsScan.getOriginalValue() + ", top = " + left + ", left = " + top);
            Log.d("im-tag", "canvas: width = " + canvas.getWidth() + ", height = " + canvas.getHeight());
//            canvas.getWidth()
            canvas.drawText(hmsScan.getOriginalValue(), left, top, hmsScanResult);
        }

        public void addView(FrameLayout layout, boolean b) {
            Bitmap bitmap = BitmapFactory.decodeResource(scanResultView.getResources(), R.drawable.test2);

            Rect rect = hmsScan.getBorderRect();

            int width = bitmap.getWidth();
            int height = bitmap.getHeight();

            Rect fixRect = new Rect();

            Log.d("hh-tag", "widthScaleFactor = " + scanResultView.widthScaleFactor + ", heightScaleFactor = " + scanResultView.heightScaleFactor);
            fixRect.left = (int) scaleX(rect.top);
            fixRect.right = (int) scaleX(rect.bottom);

            fixRect.top = (int) scaleY(rect.left);
            fixRect.bottom = (int) scaleY(rect.right);

            Log.d("hh-tag", "rect = " + rect.toString());

            RectF other = new RectF();
            float x = rect.top;
            float t = x / (CommonActivity.sw / 2f) * CommonActivity.leftM;
            // 扫码返回的是旋转了90度的，所有top 是left数据
            // 图片靠右，top数据越小，需要使用屏幕的宽度，减去缩放成屏幕的宽度数据，在添加最开始的布局距离
            other.left = CommonActivity.sw - scaleX(rect.top) + CommonActivity.leftM;
            Log.d("hh-tag", "x = " + x + ", t = " + t + " width = " + CommonActivity.sw + ", left = " + other.left);

            float tx = rect.left;
            // 将left值转换成y坐标，然后添加topM，计算出距离上面的距离
            other.top = scaleY(rect.left) + CommonActivity.topM;

            float x1 = rect.bottom;
            other.right = CommonActivity.sw - scaleX(rect.bottom) + CommonActivity.leftM;

            float tx2 = rect.right;
            other.bottom = scaleY(rect.right) + CommonActivity.topM;

            Log.d("hh-tag", "other = " + other.toString());

            int midX = (int) ((other.right - other.left) / 2 + other.left);
            int minY = (int) ((other.bottom - other.top) / 2 + other.top);
            int left = midX - width / 2;
            int top = minY - height / 2;

            Log.d("im-tag", "midX = " + midX + ", minY = " + minY);
            Log.d("im-tag", "left = " + left + ", top = " + top);

            if (sourceBitmap != null && !b) {
                Log.d("im-tag", "sourceBitmap: width = " + sourceBitmap.getWidth() + ", height = " + sourceBitmap.getHeight());
                ImageView bgView = new ImageView(layout.getContext());
                bgView.setImageBitmap(sourceBitmap);

                // 将图片的距离，设置成最开始的宽高，做覆盖使用
                LayoutParams params = new LayoutParams((int) CommonActivity.sw, (int) CommonActivity.sh);
                params.setMargins((int) CommonActivity.leftM, (int) CommonActivity.topM, 0, 0);
                layout.addView(bgView, params);
                bgView.setScaleType(ScaleType.FIT_XY);

                Log.d("im-tag", "sourceBitmap: width = " + sourceBitmap.getWidth() + ", height = " + sourceBitmap.getHeight());
                Log.d("im-tag", "layout: width = " + layout.getWidth() + ", height = " + layout.getHeight());
            }

            ImageView imageView = new ImageView(layout.getContext());
            LayoutParams params = new LayoutParams(width, height);
//            canvas.drawText(hmsScan.getOriginalValue(), left, top, hmsScanResult);
            params.leftMargin = left;
            params.topMargin = top;
            imageView.setImageResource(R.drawable.test2);
            imageView.setOnClickListener(view -> {
                if (clickResult != null) {
                    clickResult.onClick(hmsScan);
                }
            });
            layout.addView(imageView, params);
        }

        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        public static Bitmap toBitmap(Drawable drawable) {
            if (drawable instanceof BitmapDrawable) {
                return ((BitmapDrawable) drawable).getBitmap();
            } else if (drawable instanceof ColorDrawable) {
                //color
                Bitmap bitmap = Bitmap.createBitmap(32, 32, Bitmap.Config.ARGB_8888);
                if (Build.VERSION.SDK_INT >= 11) {
                    Canvas canvas = new Canvas(bitmap);
                    canvas.drawColor(((ColorDrawable) drawable).getColor());
                }
                return bitmap;
            } else if (drawable instanceof NinePatchDrawable) {
                //.9.png
                Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                        drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
                drawable.draw(canvas);
                return bitmap;
            }
            return null;
        }

//        public void addView() {
//            scanResultView.addV
//        }

        public float scaleX(float horizontal) {
            return horizontal * scanResultView.widthScaleFactor;
        }

        public float scaleY(float vertical) {
            return vertical * scanResultView.heightScaleFactor;
        }

    }
}
