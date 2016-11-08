package com.doubleman.wifidemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by fmm on 2016/10/14.
 */

public class WifiSearch {
    private static final String Tag = WifiSearch.class.getSimpleName();
    private static final int WIFI_SEARCH_TIMEOUT = 20; //扫描WIFI的超时时间

    private Context mContext;
    private WifiManager mWifiManager;
    private WiFiScanReceiver mWifiReceiver;
    private Lock mLock;
    private Condition mCondition;
    private SearchWifiListener mSearchWifiListener;
    private boolean mIsWifiScanCompleted = false;
    private List<WifiConfiguration> wifiConfigList;

    public static enum ErrorType {
        SEARCH_WIFI_TIMEOUT, //扫描WIFI超时（一直搜不到结果）
        NO_WIFI_FOUND,       //扫描WIFI结束，没有找到任何WIFI信号
    }

    //扫描结果通过该接口返回给Caller
    public interface SearchWifiListener {
        public void onSearchWifiFailed(ErrorType errorType);

        public void onSearchWifiSuccess(List<ScanResult> scanResults);
    }


    public WifiSearch(Context context, SearchWifiListener listener) {

        mContext = context;
        mSearchWifiListener = listener;

        mLock = new ReentrantLock();
        mCondition = mLock.newCondition();
        mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);

        mWifiReceiver = new WiFiScanReceiver();
    }

    public void search() {

        new Thread(new Runnable() {

            @Override
            public void run() {
                //如果WIFI没有打开，则打开WIFI
                if (!mWifiManager.isWifiEnabled()) {
                    mWifiManager.setWifiEnabled(true);
                }

                //注册接收WIFI扫描结果的监听类对象
                mContext.registerReceiver(mWifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

                //开始扫描
                mWifiManager.startScan();

                mLock.lock();

                //阻塞等待扫描结果
                try {
                    mIsWifiScanCompleted = false;
                    mCondition.await(WIFI_SEARCH_TIMEOUT, TimeUnit.SECONDS);
                    if (!mIsWifiScanCompleted) {
                        mSearchWifiListener.onSearchWifiFailed(ErrorType.SEARCH_WIFI_TIMEOUT);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                mLock.unlock();

                //删除注册的监听类对象
                mContext.unregisterReceiver(mWifiReceiver);
            }
        }).start();
    }

    //系统WIFI扫描结果消息的接收者
    protected class WiFiScanReceiver extends BroadcastReceiver {

        public void onReceive(Context c, Intent intent) {
            //提取扫描结果
            List<ScanResult> scanResults = mWifiManager.getScanResults();
            //检测扫描结果
            if (scanResults != null && scanResults.size() > 0) {
                mSearchWifiListener.onSearchWifiSuccess(scanResults);
            } else {
                mSearchWifiListener.onSearchWifiFailed(ErrorType.NO_WIFI_FOUND);
            }

            mLock.lock();
            mIsWifiScanCompleted = true;
            mCondition.signalAll();
            mLock.unlock();
        }
    }
    //获取wifi已连接信息
    public WifiInfo getWifiInfo() {
        return this.mWifiManager.getConnectionInfo();
    }
    //根据ssid判断该wifi是否连接
    public boolean isCurrentConnect(String ssID){
        ssID ="\""+ssID+"\"";
        WifiInfo info  = getWifiInfo();
        //当前的ssid为空，未连接wifi
        if(TextUtils.isEmpty(info.getSSID())){
            return false;
        }else {
           String tempSSID =  info.getSSID();
            if(tempSSID.equals(ssID)){
                return true;
            }else {
                return false;
            }
        }
    }
    //得到Wifi配置好的信息
    public List<WifiConfiguration> getConfiguration() {
        wifiConfigList = mWifiManager.getConfiguredNetworks();//得到配置好的网络信息
        if (wifiConfigList == null) {
            wifiConfigList = new ArrayList<>();
        }
        for (int i = 0; i < wifiConfigList.size(); i++) {
            WifiConfiguration configuration =  wifiConfigList.get(i);
            int networkId  = configuration.networkId;
           String ssid = configuration.SSID;
            Log.d(Tag,"wifi WifiConfiguration---->"+ssid+"-networkId"+networkId);
        }
        return wifiConfigList;
    }

    //去除重复同名信号
    public List<ScanResult> rmWeakSameNameScanResult(List<ScanResult> list){
        for  ( int  i  =   0 ; i  <  list.size()  -   1 ; i ++ )   {
            for  ( int  j  =  list.size()  -   1 ; j  >  i; j -- )   {
                ScanResult scanResultTmpI = list.get(i);
                ScanResult scanResultTmpJ = list.get(j);
                if  (scanResultTmpJ.SSID.equals(scanResultTmpI.SSID))   {
                    list.remove(j);
                }
            }
        }
        return list;
    }
    //按照信号强弱排序
    public List<ScanResult> BubbleSort(List<ScanResult> list){
        if(list!=null&&list.size()>0){
            ScanResult scanResultTmpPre= null;
            ScanResult scanResultTmpNex= null;
            for (int i = 0; i < list.size()-1; i++) {
                for (int j = 0; j < list.size()-i-1; j++) {
                    scanResultTmpPre = list.get(j);
                    scanResultTmpNex = list.get(j+1);
                    //升序，信号是负值
                    if(Math.abs(scanResultTmpPre.level)> Math.abs(scanResultTmpNex.level)){
                        list.set(j,scanResultTmpNex);
                        list.set(j+1,scanResultTmpPre);
                        scanResultTmpPre=null;
                        scanResultTmpNex=null;
                    }
                }
            }
        }
        //讲当前已连接的wifi放在第一位
       List<ScanResult> scanResults =  new ArrayList<>();
        for(ScanResult scanResult :list){
            if(scanResult.SSID.equals(getWifiInfo().getSSID())){
                scanResults.add(scanResult);
            }
        }
        for(ScanResult scanResult :list){
            if(!scanResult.SSID.equals(getWifiInfo().getSSID())){
                scanResults.add(scanResult);
            }
        }

        return scanResults;
    }

    /**
     * 判断 ssid 连接过、
     * @param SSID
     * @return
     */
    public int IsConfiguration(String SSID){
        List<WifiConfiguration> wifiConfigList = getConfiguration();
        for(int i = 0; i < wifiConfigList.size(); i++){
            if(wifiConfigList.size()>0){
                if(SSID.equals(wifiConfigList.get(i).SSID)){//地址相同
                    return wifiConfigList.get(i).networkId;
                }
            }
        }
        return -1;
    }
}

