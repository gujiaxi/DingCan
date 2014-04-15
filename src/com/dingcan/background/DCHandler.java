package com.dingcan.background;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

import com.dingcan.adapter.OrdAdapter;
import com.dingcan.app.MainActivity;
import com.dingcan.frag.OrdFragment;
import com.dingcan.frag.ResFragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class DCHandler extends Handler {
	public static String dirPath = "/mnt/sdcard/dingcan/";
	public static String IP_FILE_PATH = dirPath+"ip.txt";
	public static final int SOCKET_MSG = 100;
	public static final int SET_IP = 103;
	static final int PCK_ERR = 101;
	static final int RECONN = 102;
	static final int MSG_16 =16;
	static final int MSG_9 = 9;
	static final String MSG_9_CNT = "msg_9_cnt";
	static final String MSG_9_ID = "msg_9_id";
	static final String MSG_9_DISH = "msg_9_dish";
	static final int MSG_22 = 22;
	static final String MSG_22_DISH_ID = "msg_22_dish_id";
	static final String MSG_22_DISH_NAME = "msg_22_dish_name";
	static final String MSG_22_DISH_MSG = "msg_22_dish_msg";
	static final String MSG_22_PRICE = "msg_22_price";
	static final String MSG_22_VIP_PRICE = "msg_22_vip_price";
	static final String MSG_22_FILELEN = "msg_22_filelen";
	static final String MSG_22_FILE_BYTES = "msg_22_file_bytes";
	static final String MSG_22_FILE_NAME = "msg_22_file_name";
	static final int MSG_17 = 17;
	static final int MSG_18 = 18;
	static final int MSG_19 = 19;
	static final int MSG_20 = 20;
	static final int MSG_21 = 21;
	
	Context context = null;
	
	int countTot = 0;
	int countCur = 0;
	int[] dishId = null;
	String[] dishName = null;
	String[] dishMsg = null;
	private float[] dishPrice;
	private float[] dishVipPrice;
	private String[] dishFileName;	
	private Bitmap[] dishBmp;
	public DCHandler(Context c) {
		context = c;
	}
	@Override
	public void handleMessage(Message msg) {
		super.handleMessage(msg);
		switch (msg.what) {
		case SET_IP:
			setIPFile((String)msg.obj);
			break;
		case RECONN:
			
			break;
		case PCK_ERR:
			Toast.makeText(context, (String)msg.obj, Toast.LENGTH_LONG).show();
			break;
		case SOCKET_MSG:
			Toast.makeText(context, (String)msg.obj, Toast.LENGTH_LONG).show();
			break;
		case MSG_9:
			//rcv new version of menu
			Bundle b = (Bundle)msg.obj;
			countTot = b.getInt(MSG_9_CNT);
			dishId = new int[countTot];
			dishName = new String[countTot];
			dishMsg = new String[countTot];
			dishPrice = new float[countTot];
			dishVipPrice = new float[countTot];
			dishFileName = new String[countTot];
			dishBmp = new Bitmap[countTot];
//			Toast.makeText(context, String.valueOf(MSG_9), Toast.LENGTH_LONG).show();
			break;
		case MSG_16:
//			Toast.makeText(context, (String)msg.obj, Toast.LENGTH_LONG).show();
			break;
		case MSG_22:
			//rcv new dish
			if (countCur < countTot){
				Bundle bdish = (Bundle)msg.obj;
				dishId[countCur] = bdish.getInt(DCHandler.MSG_22_DISH_ID);
				dishName[countCur] = bdish.getString(DCHandler.MSG_22_DISH_NAME);
				dishMsg[countCur] = bdish.getString(DCHandler.MSG_22_DISH_MSG);
				dishPrice[countCur] = bdish.getFloat(DCHandler.MSG_22_PRICE);
				dishVipPrice[countCur] = bdish.getFloat(DCHandler.MSG_22_VIP_PRICE);
				dishFileName[countCur] = dirPath+bdish.getString(DCHandler.MSG_22_FILE_NAME)+".jpg";
				countCur++;
			}
			if ( countTot != 0 && countCur == countTot) {
				OrdFragment.str0 = getStringArrFromIntArr(dishId);
				OrdFragment.str1 = dishName;
				OrdFragment.str2 = getStringArrFromFloatArr(dishPrice);
				OrdFragment.str3 = getStringArrFromFloatArr(dishVipPrice);
				OrdFragment.str4 = dishMsg;
				OrdAdapter.uriFlag = true;
				OrdFragment.files = dishFileName;
//				OrdFragment.bmps = dishBmp;
			}
//			Toast.makeText(context, String.valueOf(MSG_22), Toast.LENGTH_LONG).show();
			break;
		case MSG_17:
			Toast.makeText(context, (String)msg.obj, Toast.LENGTH_LONG).show();
			Message m2 = new Message();
			m2.arg1 = msg.arg1;
			ResFragment.handler.sendMessage(m2);
			break;
		case MSG_18:
			Toast.makeText(context, (String)msg.obj, Toast.LENGTH_LONG).show();
			break;
		case MSG_19:
			MainActivity.billId = msg.arg1;
			MainActivity.payFlag = true;
			Toast.makeText(context, (String)msg.obj + " 请再次点击按钮结账", Toast.LENGTH_LONG).show();
			break;
		case MSG_20:
			Toast.makeText(context, (String)msg.obj, Toast.LENGTH_LONG).show();
			break;
		case MSG_21:
			Toast.makeText(context, (String)msg.obj, Toast.LENGTH_LONG).show();
			break;
			default:
				Toast.makeText(context, "undefined msg", Toast.LENGTH_LONG).show();
		}
	}
	private void setIPFile(String obj) {
		File ipFile = new File(IP_FILE_PATH);
		FileWriter fw;
		try {
			fw = new FileWriter(ipFile, false);
			ClientHeartBeatThread.HOST = obj;
			fw.write(obj);
			fw.close();
		} catch (IOException e) {
			Log.e("set ip file err", "write file err");
			e.printStackTrace();
		}
	}
	private String[] getStringArrFromFloatArr(float[] dishPrice2) {
		String[] resStrings = new String [dishPrice2.length];
		for (int i = 0; i < dishPrice2.length; i++) {
			resStrings[i] = String.valueOf(dishPrice2[i]);
		}
		return resStrings;
	}
	private String[] getStringArrFromIntArr(int[] dishId2) {
		String[] resStrings = new String [dishId2.length];
		for (int i = 0; i < dishId2.length; i++) {
			resStrings[i] = String.valueOf(dishId2[i]);
		}
		return resStrings;
	}
}
