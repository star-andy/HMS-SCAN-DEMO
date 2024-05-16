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

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.Nullable;

import com.huawei.hms.ml.scan.HmsScan;

import java.util.ArrayList;
import java.util.List;

public class ScanResultView3 extends View {

    private final Object lock = new Object();
    protected float widthScaleFactor = 1.0f;
    protected float heightScaleFactor = 1.0f;
    protected float previewWidth;
    protected float previewHeight;

    private final List<HmsScanGraphic> hmsScanGraphics = new ArrayList<>();

    public ScanResultView3(Context context) {
        super(context);
    }

    public ScanResultView3(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
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
        postInvalidate();
    }

    /**
     * Draw MultiCodes on screen.
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        Display defaultDisplay = windowManager.getDefaultDisplay();
        Point outPoint = new Point();
        defaultDisplay.getRealSize(outPoint);
        float sceenWidth = outPoint.x;
        float sceenHeight = outPoint.y;

        Log.d("hh-tag", "sw = " + sceenWidth + ", sh = " + sceenHeight);
        Log.d("hh-tag", "pw = " + previewWidth + ", ph = " + previewHeight);
        synchronized (lock) {
            if ((previewWidth != 0) && (previewHeight != 0)) {
//                widthScaleFactor = (float) canvas.getWidth() / (float) previewWidth;
                widthScaleFactor = (float) 1080f / (float) (1440f - 0);
                heightScaleFactor = (float) 1920f / (float) 2560f;
//                heightScaleFactor = 1792f / 2560f;
//                heightScaleFactor = 1.0f;

//                widthScaleFactor = (float) 1080 / (float) 1920;
//                heightScaleFactor = (float) 1572 / (float) 1920;
            }

            for (HmsScanGraphic graphic : hmsScanGraphics) {
                graphic.drawGraphic(canvas);
            }
        }
    }

    public static class HmsScanGraphic {

        private static final int TEXT_COLOR = Color.WHITE;
        private static final float TEXT_SIZE = 35.0f;
        private static final float STROKE_WIDTH = 4.0f;

        private final Paint rectPaint;
        private final Paint hmsScanResult;
        private final HmsScan hmsScan;
        private ScanResultView3 scanResultView;

        public HmsScanGraphic(ScanResultView3 scanResultView, HmsScan hmsScan) {
            this(scanResultView, hmsScan, Color.WHITE);
        }

        public HmsScanGraphic(ScanResultView3 scanResultView, HmsScan hmsScan, int color) {
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


        public void drawGraphic(Canvas canvas) {
            if (hmsScan == null) {
                return;
            }

//            scanResultView.widthScaleFactor = 1.0f;
//            scanResultView.heightScaleFactor = 1.0f;
            RectF rect = new RectF(hmsScan.getBorderRect());
            RectF other = new RectF();
            other.left = canvas.getWidth()-scaleX(rect.top);

//            1080

            other.top = scaleY(rect.left);
            other.right = canvas.getWidth()-scaleX(rect.bottom);
            other.bottom = scaleY(rect.right);

            canvas.drawRect(other, rectPaint);

//            RectF other = new RectF();
//            other.left = canvas.getWidth() - scaleX(rect.top);
//            other.top = scaleY(rect.left);
//            other.right = canvas.getWidth() - scaleX(rect.bottom);
//            other.bottom = scaleY(rect.right);
//
//            canvas.drawRect(other, rectPaint);

            Log.d("hh-tag", "rect = " + rect.toString());


//            RectF other = new RectF();
//            other.left = scaleX(rect.top);
//            other.top = scaleY(rect.left);
//            other.right = scaleX(rect.bottom);
//            other.bottom = scaleY(rect.right);
//            canvas.drawRect(other, rectPaint);

            Log.d("hh-tag", "width = " + canvas.getWidth() + ", height = " + canvas.getHeight());

            Log.d("hh-tag", "other = " + other.toString());
            Log.d("hh-tag", "h w = " + scanResultView.widthScaleFactor + ", h s = " + scanResultView.heightScaleFactor);
//            scanResultView.widthScaleFactor = 1.0f;
//            scanResultView.heightScaleFactor = 1.0f;



//            canvas.drawText(hmsScan.getOriginalValue(), other.right, other.bottom, hmsScanResult);
//            canvas.drawText(hmsScan.getOriginalValue(), scaleX(rect.bottom), scaleY(rect.right), hmsScanResult);
        }

        public float scaleX(float horizontal) {
            return horizontal * scanResultView.widthScaleFactor;
        }

        public float scaleY(float vertical) {
            return vertical * scanResultView.heightScaleFactor;
        }

    }
}
