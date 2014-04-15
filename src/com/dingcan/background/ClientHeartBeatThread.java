package com.dingcan.background;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;

import javax.crypto.spec.DESKeySpec;
import javax.security.auth.login.LoginException;

import android.app.Application;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.ViewDebug.FlagToString;
import android.widget.Toast;

public class ClientHeartBeatThread extends Thread {
	public static int pckNo = 1;
	public static final int LEN = 10;
	public static String HOST = "172.27.35.2";   
	public static final int PORT = 20000;
	public static final int INT_LEN = 4;
	public static final int FLOAT_LEN = 4;
	public static final int CHAR_LEN = 1;
	public static final int RD_HEAD_LEN = 9;
	public static final int MAX_FILE_LEN = 500000;
	public static final int MAX_PKG_LEN = 600000;
	
	public static int fileCnt = 1;
	private Socket client = null;
	private Handler handler = null;
	private int pckSNo = -99;
	private int pckLen = 0;
	private byte style = 0;
	int lossCnt = 0;
	public boolean runFlag = true;
	public ClientHeartBeatThread(Socket c, Handler h) {
		client = c;
		handler = h;
	}
	@Override
	public void run() {
		if (client == null)
			return;
		super.run();
		Message msg = new Message();
		byte[] buffer = new byte[LEN];
		byte[] rdHeadBuf = new byte[RD_HEAD_LEN];
		int wrtLen = 0, rdLen = 0;
		try {
			DataInputStream dis = new DataInputStream(client.getInputStream());
			DataOutputStream dos = new DataOutputStream(client.getOutputStream());
			while (runFlag) {
				wrtLen = 0;
				rdLen = 0;
				rdLen = dis.read(rdHeadBuf, 0, RD_HEAD_LEN);
				if (rdLen == -1) {
					// read the end of stream, continue or not????????
					if (lossCnt >= 3) {
						try {
							client.connect(new InetSocketAddress(HOST, PORT));
							lossCnt = 0;
						} catch (IOException e) {
							Log.e("reconn err", e.getMessage());
						}
					} else
						lossCnt ++;
					continue;
				}
				else {
					byte[] tmpByteArr = new byte[INT_LEN];
					byte[] tmpByteArr2 = new byte[INT_LEN];
					byteArrayCopy(rdHeadBuf, 0, tmpByteArr, 0, INT_LEN);
					pckSNo = byteArrayToInt(tmpByteArr, 0);
					Log.i("order client heart", "pckNo: " + pckSNo);
					byteArrayCopy(rdHeadBuf, INT_LEN, tmpByteArr2, 0, INT_LEN);
					pckLen = byteArrayToInt(tmpByteArr2, 0);
					Log.i("order client heart", "pckLen: " + pckLen);
					style = rdHeadBuf[2 * tmpByteArr.length];
					Log.i("order client heart", "style: " + style);
					if (pckSNo == -1) {
						Log.i("order client heart", "rcv heartbeat pkg");
						byteArrayCopy(intToByteArray(-1), 0, buffer, 0, INT_LEN);
						wrtLen += INT_LEN;
						byteArrayCopy(intToByteArray(0), 0, buffer, 4, INT_LEN);
						wrtLen += INT_LEN;
						dos.write(buffer, 0, wrtLen);
						dos.flush();
					} else {
						Log.i("client rcv msg", "msg len: " + pckLen);
						if (pckLen < 0 || pckLen > MAX_PKG_LEN) {
//							PkgException e = new PkgException();
//							throw e;
							continue;
						}
						byte[] rcvBuf = new byte[pckLen];
						int rcvLen = 0;
						while (rcvLen < pckLen-1)
							rcvLen += dis.read(rcvBuf, rcvLen, pckLen-1-rcvLen);
						Log.i("rcvBuf Len", "len: " + rcvLen);
						switch (style){ 
						case 9:
							//rcv menu version
							menuVer(rcvBuf);
							break;
						case 22:
							//rcv dishes msg 
							dishMsg(rcvBuf);
							break;
						case 16:
							//mobile bind
							mobileBind(rcvBuf);
							break;
						case 17:
							//desk bind
							deskBind(rcvBuf);
							break;
						case 18:
							//admin login
							memberLogin(rcvBuf);
							break;
						case 19:
							//order result
							order(rcvBuf);
							break;
						case 20:
							//pay
							pay(rcvBuf);
							break;
						case 21:
							//client login
							login(rcvBuf);
							break;
						default:
							Log.e("order client heart err", "undefined style: " + style);
							break;
						}
					}
				}
			}
			dis.close();
			dos.close();
			
		} catch (IOException e) {
			msg.what = DCHandler.SOCKET_MSG;
			msg.obj = "失去连接";
			Log.e("HeartBeatThread err", "IO exception pckSNo:" + pckSNo + " rdLen:" + rdLen);
		} 
//		catch (PkgException e){
//			msg.what = DCHandler.PCK_ERR;
//			msg.obj = "数据异常";
//			Log.e("HeartBeatThread err", "pkg exception pckSNo:" + pckSNo + " rdLen:" + rdLen + " pckLen:" + pckLen);
//		}
		finally {
			runFlag = false;
			handler.sendMessage(msg);
		}
	}
	private void login(byte[] rcvBuf) {
		Message msg = new Message();
		msg.what = DCHandler.MSG_21;
		int result = -99;
		byte[] tmpByteArr = new byte[INT_LEN];
		byteArrayCopy(rcvBuf, 0, tmpByteArr, 0, INT_LEN);
		result = byteArrayToInt(tmpByteArr, 0);
		if (result == 1)
			msg.obj = "用户登陆成功";
		else if (result == 0)
			msg.obj = "设备未绑定且桌号不正确";
		else if (result == -1) {
			Bundle b = new Bundle();
			b.putInt(ClientCaseThread.PCK_NO, ClientHeartBeatThread.pckNo);
			ClientHeartBeatThread.pckNo ++;
			new ClientCaseThread((byte)7, b, client).start();				
			msg.obj = "菜单版本不是最新的";
		}
		else if (result == -2)
			msg.obj = "设备未绑定";
		else if (result == -3)
			msg.obj = "没有该会员";
		else if (result == -4)
			msg.obj = "桌号不正确";
		handler.sendMessage(msg);
	}
	private void pay(byte[] rcvBuf) {
		Message msg = new Message();
		msg.what = DCHandler.MSG_20;
		float result = -1;
		byte[] tmpByteArr = new byte[FLOAT_LEN];
		byteArrayCopy(rcvBuf, 0, tmpByteArr, 0, FLOAT_LEN);
		result = Float.intBitsToFloat(byteArrayToInt(tmpByteArr, 0));   
		msg.obj = "总额："+result;
		handler.sendMessage(msg);
	}
	private void order(byte[] rcvBuf) {
		Message msg = new Message();
		msg.what = DCHandler.MSG_19;
		int result = -1;
		byte[] tmpByteArr = new byte[INT_LEN];
		byteArrayCopy(rcvBuf, 0, tmpByteArr, 0, INT_LEN);
		result = byteArrayToInt(tmpByteArr, 0);
		if (result == -1) {
			msg.obj = "菜单版本不是最新的";
			msg.arg1 = -1;
			Bundle b = new Bundle();
			b.putInt(ClientCaseThread.PCK_NO, ClientHeartBeatThread.pckNo);
			ClientHeartBeatThread.pckNo ++;
			new ClientCaseThread((byte)7, b, client).start();	
		}
		else {
			msg.obj = "下单成功，订单号：" + result;
			msg.arg1 = result;
		}
		handler.sendMessage(msg);
	}
	private void memberLogin(byte[] rcvBuf) {
		Message msg = new Message();
		msg.what = DCHandler.MSG_18;
		int result = 0;
		byte[] tmpByteArr = new byte[INT_LEN];
		byteArrayCopy(rcvBuf, 0, tmpByteArr, 0, INT_LEN);
		result = byteArrayToInt(tmpByteArr, 0);
		if (result == 1)
			msg.obj = "登陆成功";
		else if (result == -1)
			msg.obj = "密码错误或者用户不存在";
		handler.sendMessage(msg);
	}
	private void deskBind(byte[] rcvBuf) {
		Message msg = new Message();
		msg.what = DCHandler.MSG_17;
		int  result = 0;
		byte[] tmpByteArr = new byte[INT_LEN];
		byteArrayCopy(rcvBuf, 0, tmpByteArr, 0, INT_LEN);
		result = byteArrayToInt(tmpByteArr, 0);
		if (result == 1)
			msg.obj = "设置桌号成功";
		else if (result == -1)
			msg.obj = "该桌号已绑定";
		else if (result == -2)
			msg.obj = "该设备还没绑定";
		msg.arg1 = result;
		handler.sendMessage(msg);
	}
	private void dishMsg(byte[] rcvBuf) throws UnsupportedEncodingException {
		Message msg = new Message();
		msg.what = DCHandler.MSG_22;
		int dishId = 0;
		int rdLen = 0;
		byte[] tmpByteArr = new byte[INT_LEN]; 
		byteArrayCopy(rcvBuf, 0, tmpByteArr, 0, INT_LEN);
		dishId = byteArrayToInt(tmpByteArr, 0);
		rdLen += INT_LEN;
		int tmpLen = getSubArrayZeroEndLen(rcvBuf, rdLen);
		byte[] tmpDishNameArr = new byte[tmpLen];
		byteArrayCopy(rcvBuf, rdLen, tmpDishNameArr, 0, tmpLen); 
		String dishName = new String(tmpDishNameArr, "gbk");
		rdLen += tmpLen+1;		
		tmpLen = getSubArrayZeroEndLen(rcvBuf, rdLen);
		tmpDishNameArr = new byte[tmpLen];
		byteArrayCopy(rcvBuf, rdLen, tmpDishNameArr, 0, tmpLen); 
		String dishMsg = new String(tmpDishNameArr, "gbk");
		rdLen += tmpLen+1;
		float price = -1;
		byteArrayCopy(rcvBuf, rdLen, tmpByteArr, 0, FLOAT_LEN);
		rdLen += FLOAT_LEN;
		price = Float.intBitsToFloat(byteArrayToInt(tmpByteArr, 0));
		float vipPrice = -1;
		byteArrayCopy(rcvBuf, rdLen, tmpByteArr, 0, FLOAT_LEN);
		rdLen += FLOAT_LEN;
		vipPrice = Float.intBitsToFloat(byteArrayToInt(tmpByteArr, 0));
		int fileLen = 0;
		byteArrayCopy(rcvBuf, rdLen, tmpByteArr, 0, INT_LEN);
		fileLen = byteArrayToInt(tmpByteArr, 0);
		rdLen += INT_LEN;
		byte[] fileByteArr = null;
		if (fileLen > 0 && fileLen < MAX_FILE_LEN) {
			fileByteArr = new byte[fileLen];
			byteArrayCopy(rcvBuf , rdLen, fileByteArr, 0, fileLen);
		}
		String fileName = String.valueOf(fileCnt);
		fileCnt ++;
		File f = new File(DCHandler.dirPath+fileName+".jpg");
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(f);
			fos.write(fileByteArr);
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			Log.e("rcv file err", e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			Log.e("rcv file err", e.getMessage());
		}
		Log.i("dish ", dishId + dishName + dishMsg + price + vipPrice + fileLen + fileName);
		Bundle b = new Bundle();
		b.putInt(DCHandler.MSG_22_DISH_ID, dishId);
		b.putString(DCHandler.MSG_22_DISH_NAME, dishName);
		b.putString(DCHandler.MSG_22_DISH_MSG, dishMsg);
		b.putFloat(DCHandler.MSG_22_PRICE, price);
		b.putFloat(DCHandler.MSG_22_VIP_PRICE, vipPrice);
		b.putInt(DCHandler.MSG_22_FILELEN, fileLen);
		b.putString(DCHandler.MSG_22_FILE_NAME, fileName);
		b.putByteArray(DCHandler.MSG_22_FILE_BYTES, fileByteArr);
		msg.obj = b;
		handler.sendMessage(msg);
	}
	private void menuVer(byte[] rcvBuf) throws UnsupportedEncodingException {
		Message msg = new Message();
		msg.what = DCHandler.MSG_9;
		int dishCount = 0;
		int rdLen = 0;
		byte[] tmpByteArr = new byte[INT_LEN];
		byteArrayCopy(rcvBuf, rdLen, tmpByteArr, 0, INT_LEN);
		dishCount = byteArrayToInt(tmpByteArr, 0);
		rdLen += INT_LEN;
		int[] idArr = new int[dishCount];
		String[] dishArr = new String[dishCount];
		for (int i = 0; i < dishCount; i++) {
			byteArrayCopy(rcvBuf, rdLen, tmpByteArr, 0, INT_LEN);
			rdLen += INT_LEN;
			idArr[i] = byteArrayToInt(tmpByteArr, 0);
			int tmpLen = getSubArrayZeroEndLen(rcvBuf, rdLen);
			byte[] tmpDishNameArr = new byte[tmpLen];
			byteArrayCopy(rcvBuf, rdLen, tmpDishNameArr, 0, tmpLen); 
			dishArr[i] = new String(tmpDishNameArr, "gbk");
			rdLen += tmpLen+1;
			Log.i("order client heart","rcv dish -> " + idArr[i] + " " + dishArr[i]);
		}
		Bundle b = new Bundle();
		b.putInt(DCHandler.MSG_9_CNT, dishCount);
		b.putIntArray(DCHandler.MSG_9_ID, idArr);
		b.putStringArray(DCHandler.MSG_9_DISH, dishArr);
		msg.obj = b;
		handler.sendMessage(msg);
	}
	static int getSubArrayZeroEndLen(byte[] rcvBuf, int offset) {
		int i = offset;
		for (; i < rcvBuf.length; i++) {
			if (rcvBuf[i] != '\0') {
				continue;
			} else {
				return i - offset;
			}
		}
		return  i - offset - 1;
	}
	private void mobileBind(byte[] rcvBuf) {
		byte[] tmpArr = new byte[INT_LEN];
		byteArrayCopy(rcvBuf, 0, tmpArr, 0, INT_LEN);
		int result = byteArrayToInt(tmpArr, 0);
		Message msg = new Message();
		msg.what = DCHandler.MSG_16;
		if (result == 1) {
			msg.obj = "设备绑定成功";
			Log.d("order client heart", "device bind success");
		} else if (result == -1) {
			msg.obj = "设备不允许被绑定";
			Log.d("order client heart", "server denied bind");
		} else if (result == -2) {
			msg.obj = "设备已经被绑定";
			Log.d("order client heart", "device have been binded");
		}
		handler.sendMessage(msg);
	}
	public static byte[] intToByteArray(int i) {   
		  byte[] result = new byte[4];   
		  result[3] = (byte)((i >> 24) & 0xFF);
		  result[2] = (byte)((i >> 16) & 0xFF);
		  result[1] = (byte)((i >> 8) & 0xFF); 
		  result[0] = (byte)(i & 0xFF);
		  return result;
		}
	public static int byteArrayToInt(byte[] b, int offset) {
	       int value= 0;
	       for (int i = 0; i < 4; i++) {
	           int shift= i * 8;
	           value +=(b[i + offset] & 0x000000FF) << shift; 
	       }
	       return value;
	 }
	public static int byteArrayCopy(byte[] src, int srcOffset, byte[] dst, int dstOffset, int len){
		if (src.length < srcOffset + len)
			return -1;
		if (dst.length < dstOffset + len)
			return -2;
		for (int i = srcOffset, j = dstOffset; i < srcOffset + len; i++, j++) {
			dst[j] = src[i];
		}
		return 0;
		
	}
}
