package com.doubleman.wifidemo;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fmm on 2016/10/14.
 */

public class WifiListActivity extends AppCompatActivity {
    private static final String Tag = WifiListActivity.class.getSimpleName();
    private Button btn_refresh;
    private ListView listview;
    private List<ScanResult> mScanResults =new ArrayList<>();
    private WifiListAdapter mListAdapter;
    private Context mContext;
    private WifiConnector mWifiConnector;
    private WifiSearch mWifiSearch;
    private ProgressDialog mWifiProgressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifilist);
        mContext = this;

        initView();
        initWifiListData();
        settingWifi();
    }
    private void initView() {
        btn_refresh = (Button) findViewById(R.id.btn_refresh);
        btn_refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initWifiListData();
            }
        });
        listview =(ListView)findViewById(R.id.listview);
        mListAdapter = new WifiListAdapter(this,mScanResults);
        listview.setAdapter(mListAdapter);
    }
    private void initWifiListData() {
        mWifiSearch = new WifiSearch(this, new WifiSearch.SearchWifiListener() {
            @Override
            public void onSearchWifiFailed(WifiSearch.ErrorType errorType) {
                dismissDialog();
            }
            @Override
            public void onSearchWifiSuccess(List<ScanResult> scanResults) {
                dismissDialog();
                scanResults = mWifiSearch.rmWeakSameNameScanResult(scanResults);
                scanResults = mWifiSearch.BubbleSort(scanResults);
                mListAdapter.addAll(scanResults);
                for (int i = 0; i < scanResults.size(); i++) {
                    String str = scanResults.get(i).SSID;
                    int level = scanResults.get(i).level;
                    Log.d(Tag,"wifi all ssid result ---->"+str+":level -->"+level);
                }
            }
        });
        mWifiSearch.search();
        showDialog();
    }
    private void settingWifi() {
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //1、点击是已经连接的提示 已连接
                //2、已保存的直接连接
                //3、未保存的 输入密码连接
                final ScanResult scanResult =  mListAdapter.getItem(position);
                int wifiItemId =-1;
                if(!TextUtils.isEmpty( scanResult.SSID)){
                    wifiItemId = mWifiSearch.IsConfiguration("\""+scanResult.SSID+"\"");
                }
                if(wifiItemId != -1){
                    if(mWifiSearch.isCurrentConnect(scanResult.SSID)){
                        Toast.makeText(mContext,"该wifi已连接！",Toast.LENGTH_SHORT).show();
                    }else{
                        //直接连接

                    }
                }else{
                   //密码连接
                    WifiPswDialog wifiPswDialog = new WifiPswDialog(mContext, new WifiPswDialog.OnCustomDialogListener() {
                        @Override
                        public void back(String str) {
                            showDialog();
                            connectWifi(scanResult,str);
                        }
                    },scanResult.SSID);
                    wifiPswDialog.show();
                }
            }
        });
    }

    private void connectWifi(final ScanResult scanResult,String passWord){
        mWifiConnector = new WifiConnector(mContext, new WifiConnector.WifiConnectListener() {
            @Override
            public void OnWifiConnectCompleted(boolean isConnected) {
                if(isConnected){
                    dismissDialog();
                    Log.d(Tag,"OnWifiConnectCompleted ---->"+isConnected);
                }else{
                    dismissDialog();
                    Log.d(Tag,"OnWifiConnectCompleted ---->"+isConnected);
                    Toast.makeText(mContext,"wifi连接失败请重试",Toast.LENGTH_SHORT).show();
                    //连接wif失败删除配置
                    mWifiConnector.removeWifiConfig(scanResult.SSID);
                }
                initWifiListData();
            }
        });
        mWifiConnector.connect(scanResult.SSID,passWord, WifiConnector.SecurityMode.WPA2);
    }

    public void showDialog(){
        if(mWifiProgressDialog==null){
            mWifiProgressDialog =new ProgressDialog(mContext);
            mWifiProgressDialog.setMessage("加载中,请稍候...");
        }
        mWifiProgressDialog.show();
    }
    public void dismissDialog(){
        if(mWifiProgressDialog!=null){
            mWifiProgressDialog.dismiss();
        }
    }
}
