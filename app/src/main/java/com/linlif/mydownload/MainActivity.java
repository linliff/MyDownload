package com.linlif.mydownload;

import android.support.v4.print.PrintHelper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.aigestudio.downloader.DownloadSelf;
import cn.aigestudio.downloader.bizs.DLHeader;
import cn.aigestudio.downloader.bizs.DLInfo;
import cn.aigestudio.downloader.bizs.DLManager;
import cn.aigestudio.downloader.interfaces.IDListener;
import cn.aigestudio.downloader.interfaces.SysListener;
import cn.aigestudio.downloader.sys.DownloadBySy;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{


    private String title = "";
    private String url = "";
    private String dir = "";
    private String filename = "";
    private List<DLHeader> dlHeaders;
    private boolean downloadBySys = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button start1 = (Button) findViewById(R.id.main_dl_start_btn1);
        Button stop1 = (Button) findViewById(R.id.main_dl_stop_btn1);

        start1.setOnClickListener(this);
        stop1.setOnClickListener(this);


        HashMap<String , String> headers = new HashMap<String, String>();
        List<DLHeader> dlHeaders = new ArrayList<>();
        for (Map.Entry<String, String> e : headers.entrySet()) {
            DLHeader dlHeader = new DLHeader(e.getKey(), e.getValue());
            dlHeaders.add(dlHeader);
        }


    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.self:
                downloadBySys = false;
                break;
            case R.id.sys:
                downloadBySys = true;
                break;
            case R.id.main_dl_start_btn1:
                if (downloadBySys){
                    downloadBySys();
                }else {
                    downloadBySelf();
                }
                break;
            case R.id.main_dl_stop_btn1:

                DownloadSelf.getInstance(this).stopDownload(url);
                break;


        }

    }

    private void downloadBySys() {

        DownloadBySy.getInstance(this).doStartDownload(url, title, true, dir, filename, dlHeaders, new SysListener() {
            @Override
            public void onStart(long id) {

            }
        });

    }

    private void downloadBySelf() {
        DownloadSelf.getInstance(this).doStartDownload(url, true, new IDListener() {
            @Override
            public void onPrepare(DLInfo info) {

            }

            @Override
            public void onStart(DLInfo info) {

            }

            @Override
            public void onProgress(DLInfo info) {

            }

            @Override
            public void onStop(DLInfo info) {

            }

            @Override
            public void onFinish(DLInfo info) {

            }

            @Override
            public void onError(int error, String msg, DLInfo info) {

            }

            @Override
            public void onCancel(DLInfo info) {

            }
        }, filename, dir, title, dlHeaders, null);
    }


}
