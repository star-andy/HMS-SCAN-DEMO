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
package com.example.scankitdemo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.util.SparseArray;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.scankitdemo.draw.ScanResultView;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hms.hmsscankit.ScanUtil;
import com.huawei.hms.ml.scan.HmsScan;
import com.huawei.hms.ml.scan.HmsScanAnalyzer;
import com.huawei.hms.ml.scan.HmsScanAnalyzerOptions;
import com.huawei.hms.mlsdk.common.MLFrame;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Objects;


public class CommonActivity extends Activity {

    public static final int REQUEST_CODE_PHOTO = 0X1113;
    private static final String TAG = "CommonActivity";
    private int defaultValue = -1;
    private SurfaceHolder surfaceHolder;
    private CameraOperation cameraOperation;
    private SurfaceCallBack surfaceCallBack;
    private CommonHandler handler;
    private boolean isShow;
    private int mode;
    private ImageView backBtn;
    private ImageView imgBtn;
    private ImageView mscanArs;
    private TextView mscanTips;
    public static final String SCAN_RESULT = "scanResult";

    /**
     * 隐藏状态栏
     */
    protected void hideBottomUIMenu() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            getWindow().setAttributes(lp);
        }
    }

    public ScanResultView scanResultView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        hideBottomUIMenu();

        setContentView(R.layout.activity_common);
        mode = getIntent().getIntExtra(Constant.DECODE_MODE, defaultValue);
        mscanArs = findViewById(R.id.scan_ars);
        mscanTips = findViewById(R.id.scan_tip);
        if (mode == Constant.MULTIPROCESSOR_ASYN_CODE || mode == Constant.MULTIPROCESSOR_SYN_CODE) {
            mscanArs.setVisibility(View.INVISIBLE);
            mscanTips.setText(R.string.scan_showresult);
            AlphaAnimation disappearAnimation = new AlphaAnimation(1, 0);
            disappearAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    if (mscanTips != null) {
                        mscanTips.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            disappearAnimation.setDuration(3000);
            mscanTips.startAnimation(disappearAnimation);
        }
        cameraOperation = new CameraOperation();
        surfaceCallBack = new SurfaceCallBack();
        SurfaceView cameraPreview = findViewById(R.id.surfaceView);
        adjustSurface(cameraPreview);
        surfaceHolder = cameraPreview.getHolder();
        isShow = false;
        setBackOperation();
        setPictureScanOperation();

        scanResultView = findViewById(R.id.scan_result_view);
        scanResultView.setOnCloseViewListener(this::finish);
    }

    public static float sw = 0;
    public static float sh = 0;
    public static float leftM = 0;
    public static float topM = 0;

    private void adjustSurface(SurfaceView cameraPreview) {
        FrameLayout.LayoutParams paramSurface = (FrameLayout.LayoutParams) cameraPreview.getLayoutParams();
        if (getSystemService(Context.WINDOW_SERVICE) != null) {
            WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
            Display defaultDisplay = windowManager.getDefaultDisplay();
            Point outPoint = new Point();
            defaultDisplay.getRealSize(outPoint);
            float sceenWidth = outPoint.x;
            float sceenHeight = outPoint.y;
            float rate;

//            CameraOperation.height = (int) sceenHeight;
//            CameraOperation.width = (int) sceenWidth;

            if (sceenWidth / (float) CameraOperation.height > sceenHeight / (float) CameraOperation.width) {
                rate = sceenWidth / (float) CameraOperation.height;
                int targetHeight = (int) (CameraOperation.width * rate);
                paramSurface.width = FrameLayout.LayoutParams.MATCH_PARENT;
                paramSurface.height = targetHeight;
                sw = sceenWidth;
                sh = targetHeight;
                int topMargin = (int) (-(targetHeight - sceenHeight) / 2);
                leftM = 0;
                topM = topMargin;
                Log.d("hh-tag", "sw = " + sw + ", sh = " + sh + ", topM = " + topMargin);
                if (topMargin < 0) {
                    paramSurface.topMargin = topMargin;
                }
            } else {
                rate = sceenHeight / (float) CameraOperation.width;
                int targetWidth = (int) (CameraOperation.height * rate);
                paramSurface.width = targetWidth;
                paramSurface.height = FrameLayout.LayoutParams.MATCH_PARENT;
                sw = targetWidth;
                sh = sceenHeight;
                int leftMargin = (int) (-(targetWidth - sceenWidth) / 2);
                leftM = leftMargin;
                topM = 0;
                Log.d("hh-tag", "sw = " + sw + ", sh = " + sh + ", leftM = " + leftMargin);
                if (leftMargin < 0) {
                    paramSurface.leftMargin = leftMargin;
                }
            }
        }
    }

    private void setBackOperation() {
        backBtn = findViewById(R.id.back_img);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mode == Constant.MULTIPROCESSOR_ASYN_CODE || mode == Constant.MULTIPROCESSOR_SYN_CODE) {
                    setResult(RESULT_CANCELED);
                }
                CommonActivity.this.finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (mode == Constant.MULTIPROCESSOR_ASYN_CODE || mode == Constant.MULTIPROCESSOR_SYN_CODE) {
            setResult(RESULT_CANCELED);
        }
        CommonActivity.this.finish();
    }

    private void setPictureScanOperation() {
        imgBtn = findViewById(R.id.img_btn);
        imgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent pickIntent = new Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                pickIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                CommonActivity.this.startActivityForResult(pickIntent, REQUEST_CODE_PHOTO);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isShow) {
            initCamera();
        } else {
            surfaceHolder.addCallback(surfaceCallBack);
        }
    }

    @Override
    protected void onPause() {
        if (handler != null) {
            handler.quit();
            handler = null;
        }
        cameraOperation.close();
        if (!isShow) {
            surfaceHolder.removeCallback(surfaceCallBack);
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void initCamera() {
        try {
            cameraOperation.open(surfaceHolder);
            if (handler == null) {
                handler = new CommonHandler(CommonActivity.this, cameraOperation, mode);
            }
        } catch (IOException e) {
            Log.w(TAG, e);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK || data == null || requestCode != REQUEST_CODE_PHOTO) {
            return;
        }
        try {
            // Image-based scanning mode
            if (mode == Constant.BITMAP_CODE) {
                decodeBitmap(MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getData()), HmsScan.ALL_SCAN_TYPE);
            } else if (mode == Constant.MULTIPROCESSOR_SYN_CODE) {
                decodeMultiSyn(MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getData()));
            } else if (mode == Constant.MULTIPROCESSOR_ASYN_CODE) {
                Bitmap bit = MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getData());
                if (bit != null) {
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    bit.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                    byte[] byteArray = byteArrayOutputStream.toByteArray();
                    String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);
                } else {
                }
                decodeMultiAsyn(bit);
            }
        } catch (Exception e) {
            Log.e(TAG, Objects.requireNonNull(e.getMessage()));
        }
    }

    private void decodeBitmap(Bitmap bitmap, int scanType) {
        HmsScan[] hmsScans = ScanUtil.decodeWithBitmap(CommonActivity.this, bitmap, new HmsScanAnalyzerOptions.Creator().setHmsScanTypes(scanType).setPhotoMode(true).create());
        if (hmsScans != null && hmsScans.length > 0 && hmsScans[0] != null && !TextUtils.isEmpty(hmsScans[0].getOriginalValue())) {
            Intent intent = new Intent();
            intent.putExtra(SCAN_RESULT, hmsScans);
            setResult(RESULT_OK, intent);
            CommonActivity.this.finish();
        }
    }

    private void decodeMultiAsyn(Bitmap bitmap) {
        MLFrame image = MLFrame.fromBitmap(bitmap);
        HmsScanAnalyzer analyzer = new HmsScanAnalyzer.Creator(this).setHmsScanTypes(HmsScan.ALL_SCAN_TYPE).create();
        analyzer.analyzInAsyn(image).addOnSuccessListener(new OnSuccessListener<List<HmsScan>>() {
            @Override
            public void onSuccess(List<HmsScan> hmsScans) {
                if (hmsScans != null && hmsScans.size() > 0 && hmsScans.get(0) != null && !TextUtils.isEmpty(hmsScans.get(0).getOriginalValue())) {
                    HmsScan[] infos = new HmsScan[hmsScans.size()];
                    Intent intent = new Intent();
                    intent.putExtra(SCAN_RESULT, hmsScans.toArray(infos));
                    setResult(RESULT_OK, intent);
                    CommonActivity.this.finish();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Log.w(TAG, e);
            }
        });
    }

    private void decodeMultiSyn(Bitmap bitmap) {
        MLFrame image = MLFrame.fromBitmap(bitmap);
        HmsScanAnalyzer analyzer = new HmsScanAnalyzer.Creator(this).setHmsScanTypes(HmsScan.ALL_SCAN_TYPE).create();
        SparseArray<HmsScan> result = analyzer.analyseFrame(image);
        if (result != null && result.size() > 0 && result.valueAt(0) != null && !TextUtils.isEmpty(result.valueAt(0).getOriginalValue())) {
            HmsScan[] info = new HmsScan[result.size()];
            for (int index = 0; index < result.size(); index++) {
                info[index] = result.valueAt(index);
            }
            Intent intent = new Intent();
            intent.putExtra(SCAN_RESULT, info);
            setResult(RESULT_OK, intent);
            CommonActivity.this.finish();
        }
    }

    class SurfaceCallBack implements SurfaceHolder.Callback {

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            if (!isShow) {
                isShow = true;
                initCamera();
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            isShow = false;
        }
    }
}
