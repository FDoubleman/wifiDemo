package com.doubleman.wifidemo;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


public class WifiPswDialog extends Dialog {
	private Button cancelButton;
	private Button okButton;
	private EditText pswEdit;
	private TextView ssidTextView;
	private OnCustomDialogListener customDialogListener;
	private String ssid;
	public WifiPswDialog(Context context, OnCustomDialogListener customListener, String ssid) {
		//OnCancelListener cancelListener) {
		super(context);
		// TODO Auto-generated constructor stub
		customDialogListener = customListener;
		this.ssid=ssid;

	}
	//定义dialog的回调事件
	public interface OnCustomDialogListener{
		void back(String str);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wifi_config_dialog);
		setTitle(ssid);
		/*ssidTextView = (TextView)findViewById(R.id.wifiDialogSsid);
		ssidTextView.setText(ssid);*/
		pswEdit = (EditText)findViewById(R.id.wifiDialogPsw);
		cancelButton = (Button)findViewById(R.id.wifiDialogCancel);
		okButton = (Button)findViewById(R.id.wifiDialogCertain);
		cancelButton.setOnClickListener(buttonDialogListener);
		okButton.setOnClickListener(buttonDialogListener);

	}

	private View.OnClickListener buttonDialogListener = new View.OnClickListener() {

		@Override
		public void onClick(View view) {
			// TODO Auto-generated method stub
			if(view.getId() == R.id.wifiDialogCancel){
				pswEdit = null;
				customDialogListener.back(null);
				cancel();//自动调用dismiss();
			}
			else{
				customDialogListener.back(pswEdit.getText().toString());
				dismiss();
			}
		}
	};

}
