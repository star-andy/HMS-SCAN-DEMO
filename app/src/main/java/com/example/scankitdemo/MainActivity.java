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
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.Manifest;
import android.Manifest.permission;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import com.huawei.hms.hmsscankit.ScanUtil;
import com.huawei.hms.ml.scan.HmsScan;
import com.huawei.hms.ml.scan.HmsScanAnalyzerOptions;



public class MainActivity extends Activity implements ActivityCompat.OnRequestPermissionsResultCallback {

    /**
     * Define requestCode.
     */
    public static final int CAMERA_REQ_CODE = 111;
    public static final int DEFINED_CODE = 222;
    public static final int BITMAP_CODE = 333;
    public static final int MULTIPROCESSOR_SYN_CODE = 444;
    public static final int MULTIPROCESSOR_ASYN_CODE = 555;
    public static final int GENERATE_CODE = 666;
    public static final int DECODE = 1;
    public static final int GENERATE = 2;
    private static final int REQUEST_CODE_SCAN_ONE = 0X01;
    private static final int REQUEST_CODE_DEFINE = 0X0111;
    private static final int REQUEST_CODE_SCAN_MULTI = 0X011;
    public static final String DECODE_MODE = "decode_mode";
    public static final String RESULT = "SCAN_RESULT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_mwcmain);

        int orientation = Configuration.ORIENTATION_LANDSCAPE;

        Resources resources = this.getResources();
        if (resources != null) {
            orientation = resources.getConfiguration().orientation;
        }
        if (orientation == Configuration.ORIENTATION_LANDSCAPE){
            setContentView(R.layout.activity_mwcmain_landscape);
        }else{
            setContentView(R.layout.activity_mwcmain);
        }


        this.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        //Set noTitleBar.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window window = getWindow();
            this.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            if (window != null) {
                window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            }
        }
    }

    /**
     * Call the default view.
     */
    public void loadScanKitBtnClick(View view) {
        requestPermission(CAMERA_REQ_CODE, DECODE);
    }

    /**
     * Call the customized view.
     */
    public void newViewBtnClick(View view) {
        requestPermission(DEFINED_CODE, DECODE);
    }

    /**
     * Call the bitmap.
     */
    public void bitmapBtnClick(View view) {
        requestPermission(BITMAP_CODE, DECODE);
    }

    /**
     * Call the MultiProcessor API in synchronous mode.
     */
    public void multiProcessorSynBtnClick(View view) {
        requestPermission(MULTIPROCESSOR_SYN_CODE, DECODE);
    }

    /**
     * Call the MultiProcessor API in asynchronous mode.
     */
    public void multiProcessorAsynBtnClick(View view) {
        requestPermission(MULTIPROCESSOR_ASYN_CODE, DECODE);
    }

    /**
     * Start generating the barcode.
     */
    public void generateQRCodeBtnClick(View view) {
        requestPermission(GENERATE_CODE, GENERATE);
    }

    /**
     * Apply for permissions.
     */
    private void requestPermission(int requestCode, int mode) {
        if (mode == DECODE) {
            decodePermission(requestCode);
        } else if (mode == GENERATE) {
            generatePermission(requestCode);
        }
    }

    /**
     * Apply for permissions.
     */
    private void decodePermission(int requestCode) {
        if (VERSION.SDK_INT >= VERSION_CODES.M) {
            if (checkSelfPermission(READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Log.d("hh-tag", "has per");
            }
        }
        ActivityCompat.requestPermissions(
                this,
                new String[]{permission.CAMERA, READ_EXTERNAL_STORAGE},
                requestCode);
    }

    /**
     * Apply for permissions.
     */
    private void generatePermission(int requestCode) {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                requestCode);
    }

    /**
     * Call back the permission application result. If the permission application is successful, the barcode scanning view will be displayed.
     * @param requestCode Permission application code.
     * @param permissions Permission array.
     * @param grantResults: Permission application result array.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (permissions == null || grantResults == null) {
            return;
        }

//        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && requestCode == GENERATE_CODE) {
//            Intent intent = new Intent(this, GenerateCodeActivity.class);
//            this.startActivity(intent);
//        }
//
//        if (grantResults.length < 2 || grantResults[0] != PackageManager.PERMISSION_GRANTED || grantResults[1] != PackageManager.PERMISSION_GRANTED) {
//            return;
//        }
        //Default View Mode
        if (requestCode == CAMERA_REQ_CODE) {
            ScanUtil.startScan(this, REQUEST_CODE_SCAN_ONE, new HmsScanAnalyzerOptions.Creator().create());
        }
        //Customized View Mode
        if (requestCode == DEFINED_CODE) {
            Intent intent = new Intent(this, DefinedActivity.class);
            this.startActivityForResult(intent, REQUEST_CODE_DEFINE);
        }
        //Bitmap Mode
        if (requestCode == BITMAP_CODE) {
            Intent intent = new Intent(this, CommonActivity.class);
            intent.putExtra(DECODE_MODE, BITMAP_CODE);
            this.startActivityForResult(intent, REQUEST_CODE_SCAN_MULTI);
        }
        //Multiprocessor Synchronous Mode
        if (requestCode == MULTIPROCESSOR_SYN_CODE) {
            Intent intent = new Intent(this, CommonActivity.class);
            intent.putExtra(DECODE_MODE, MULTIPROCESSOR_SYN_CODE);
            this.startActivityForResult(intent, REQUEST_CODE_SCAN_MULTI);
        }
        //Multiprocessor Asynchronous Mode
        if (requestCode == MULTIPROCESSOR_ASYN_CODE) {
            Intent intent = new Intent(this, CommonActivity.class);
            intent.putExtra(DECODE_MODE, MULTIPROCESSOR_ASYN_CODE);
            this.startActivityForResult(intent, REQUEST_CODE_SCAN_MULTI);
        }
    }

    /**
     * Event for receiving the activity result.
     *
     * @param requestCode Request code.
     * @param resultCode Result code.
     * @param data        Result.
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK || data == null) {
            return;
        }
        //Default View
        if (requestCode == REQUEST_CODE_SCAN_ONE) {
            HmsScan obj = data.getParcelableExtra(ScanUtil.RESULT);
            if (obj != null) {
                Intent intent = new Intent(this, DisPlayActivity.class);
                intent.putExtra(RESULT, obj);
                startActivity(intent);
            }
            //MultiProcessor & Bitmap
        }  else if (requestCode == REQUEST_CODE_DEFINE | requestCode == REQUEST_CODE_SCAN_MULTI) {
            HmsScan obj = data.getParcelableExtra(Constant.SCAN_RESULT);
            if (obj != null) {
                Intent intent = new Intent(this, DisPlayActivity.class);
                intent.putExtra(RESULT, obj);
                startActivity(intent);
            }
        }
    }
}
