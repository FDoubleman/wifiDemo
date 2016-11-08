package com.doubleman.wifidemo;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by fmm on 2016/10/14.
 */

public class WifiListAdapter extends BaseAdapter {
    private Context mContext;
    private List<ScanResult> mScanResults;
    private final WifiSearch mWifiSearch;

    public WifiListAdapter(Context context, List<ScanResult> scanResults){
        mContext =context;
        mScanResults =scanResults;
        mWifiSearch = new WifiSearch(context, new WifiSearch.SearchWifiListener() {
            @Override
            public void onSearchWifiFailed(WifiSearch.ErrorType errorType) {

            }

            @Override
            public void onSearchWifiSuccess(List<ScanResult> scanResults) {

            }
        });
    }
    @Override
    public int getCount() {
        return mScanResults.size();
    }

    @Override
    public ScanResult getItem(int position) {
        return mScanResults.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder =null;
        if(convertView==null){
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_wifilist_new,parent,false);
            holder =new ViewHolder();
            holder.wifiName = (TextView) convertView.findViewById(R.id.ssid_text);
            holder.wifiSingleStatus= (ImageView) convertView.findViewById(R.id.ssid_image);
            holder.wifiStatus =(TextView)convertView.findViewById(R.id.tv_wifistatus);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }
        ScanResult scanResult = mScanResults.get(position);

        int wifiItemId =-1;
        if(!TextUtils.isEmpty( scanResult.SSID)){
            wifiItemId = mWifiSearch.IsConfiguration("\""+scanResult.SSID+"\"");
        }
        if(wifiItemId != -1){
            if(mWifiSearch.isCurrentConnect(scanResult.SSID)){
                holder.wifiName.setTextColor(mContext.getResources().getColor(R.color.colorAccent));
                holder.wifiStatus.setText("(已连接)");
            }else{
                holder.wifiName.setTextColor(mContext.getResources().getColor(R.color.colorPrimaryDark));
                holder.wifiStatus.setText("(已保存)");
            }
        }else{
            holder.wifiName.setTextColor(mContext.getResources().getColor(R.color.colorPrimaryDark));
            holder.wifiStatus.setText("");
        }

        holder.wifiName.setText(scanResult.SSID);

        return convertView;
    }

    public void addAll(List<ScanResult> scanResults){
        mScanResults.clear();
        mScanResults.addAll(scanResults);
        notifyDataSetChanged();
    }


    class ViewHolder{
        TextView wifiName,wifiStatus;
        ImageView wifiSingleStatus;
    }
}
