package com.dingcan.background;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import android.os.Bundle;
import android.util.Log;

public class ClientCaseThread extends Thread {
	public static int MENU_VER_INT = 4;
	public static final String MOBILE_BIND = "mobile_bind";
	public static final String PCK_NO = "pck_no";
	public static final String DESK_NO = "desk_no";
	public static final String LOGIN_NAME = "login_name";
	public static final String LOGIN_PSW = "login_psw";
	public static final String MENU_VER = "menu_ver";
	public static final String MENU_DISH = "menu_dish";
	public static final String MENU_COUNT = "menu_count";
	public static final String MENU_NOTE = "menu_note";
	public static final String BILL_NO = "bill_no";
	public static final String VIP = "vip";
	private byte caseNo = 111;
	private Bundle paraBundle = null;
	private Socket client = null;
	public ClientCaseThread(byte c, Bundle b, Socket s) {
		caseNo = c;
		paraBundle = b;
		client = s;
	}
	@Override
	public void run() {
		super.run();
		if (caseNo == -1 || paraBundle == null || client == null) {
			Log.i("order client case", "invalide parameter for case thread");
			return;
		}
		try {
			DataOutputStream dos = new DataOutputStream(client.getOutputStream());
			switch (caseNo) {
			case 0:
				mobileBind(dos);
				break;
			case 1:
				setDestNo(dos);
				break;
			case 2:
				login(dos);
				break;
			case 3:
				order(dos);
				break;
			case 4:
				checkOut(dos);
				break;
			case 5:
				memberLogin(dos);
				break;
			case 7:
				menuVer(dos);
			default:
				break;
			}
		} catch (IOException e) {
			e.printStackTrace();
			Log.e("order client case err", "IO Exception");
		}
	}
	private void menuVer(DataOutputStream dos) throws IOException {
		int wrtLen = 0;
		int pckNo = paraBundle.getInt(PCK_NO);
		byte[] style = {caseNo};
		int pckLen = ClientHeartBeatThread.CHAR_LEN;
		byte[] tmpBuf = new byte[ClientHeartBeatThread.INT_LEN * 2  + pckLen];
		ClientHeartBeatThread.byteArrayCopy(ClientHeartBeatThread.intToByteArray(pckNo), 0, tmpBuf, 0, ClientHeartBeatThread.INT_LEN);
		wrtLen += ClientHeartBeatThread.INT_LEN;
		ClientHeartBeatThread.byteArrayCopy(ClientHeartBeatThread.intToByteArray(pckLen), 0, tmpBuf, wrtLen, ClientHeartBeatThread.INT_LEN);
		wrtLen += ClientHeartBeatThread.INT_LEN;
		ClientHeartBeatThread.byteArrayCopy(style, 0, tmpBuf, wrtLen, ClientHeartBeatThread.CHAR_LEN);
		wrtLen += ClientHeartBeatThread.CHAR_LEN;
		dos.write(tmpBuf, 0, wrtLen);
		dos.flush();			
	}
	private void memberLogin(DataOutputStream dos) throws IOException {
		int wrtLen = 0;
		int pckNo = paraBundle.getInt(PCK_NO);
		int deskNo = paraBundle.getInt(DESK_NO);
		int ver = paraBundle.getInt(MENU_VER);
		int vip = paraBundle.getInt(VIP);
		String imei = paraBundle.getString(MOBILE_BIND);
		byte[] imeiBytes = imei.getBytes(); 
		byte[] style = {caseNo};
		int pckLen = ClientHeartBeatThread.CHAR_LEN + ClientHeartBeatThread.INT_LEN * 3 + imeiBytes.length;
		byte[] tmpBuf = new byte[ClientHeartBeatThread.INT_LEN * 2  + pckLen];
		ClientHeartBeatThread.byteArrayCopy(ClientHeartBeatThread.intToByteArray(pckNo), 0, tmpBuf, 0, ClientHeartBeatThread.INT_LEN);
		wrtLen += ClientHeartBeatThread.INT_LEN;
		ClientHeartBeatThread.byteArrayCopy(ClientHeartBeatThread.intToByteArray(pckLen), 0, tmpBuf, wrtLen, ClientHeartBeatThread.INT_LEN);
		wrtLen += ClientHeartBeatThread.INT_LEN;
		ClientHeartBeatThread.byteArrayCopy(style, 0, tmpBuf, wrtLen, ClientHeartBeatThread.CHAR_LEN);
		wrtLen += ClientHeartBeatThread.CHAR_LEN;
		ClientHeartBeatThread.byteArrayCopy(ClientHeartBeatThread.intToByteArray(deskNo), 0, tmpBuf, wrtLen, ClientHeartBeatThread.INT_LEN);
		wrtLen += ClientHeartBeatThread.INT_LEN;
		ClientHeartBeatThread.byteArrayCopy(ClientHeartBeatThread.intToByteArray(ver), 0, tmpBuf, wrtLen, ClientHeartBeatThread.INT_LEN);
		wrtLen += ClientHeartBeatThread.INT_LEN;
		ClientHeartBeatThread.byteArrayCopy(ClientHeartBeatThread.intToByteArray(vip), 0, tmpBuf, wrtLen, ClientHeartBeatThread.INT_LEN);
		wrtLen += ClientHeartBeatThread.INT_LEN;
		ClientHeartBeatThread.byteArrayCopy(imeiBytes, 0, tmpBuf, wrtLen, imeiBytes.length);
		wrtLen += imeiBytes.length;
		dos.write(tmpBuf, 0, wrtLen);
		dos.flush();			
	}
	private void checkOut(DataOutputStream dos) throws IOException {
		int wrtLen = 0;
		int pckNo = paraBundle.getInt(PCK_NO);
		int billId = paraBundle.getInt(BILL_NO);
		byte[] style = {caseNo};
		int pckLen = ClientHeartBeatThread.INT_LEN + ClientHeartBeatThread.CHAR_LEN;
		byte[] tmpBuf = new byte[ClientHeartBeatThread.INT_LEN * 2  + pckLen];
		ClientHeartBeatThread.byteArrayCopy(ClientHeartBeatThread.intToByteArray(pckNo), 0, tmpBuf, 0, ClientHeartBeatThread.INT_LEN);
		wrtLen += ClientHeartBeatThread.INT_LEN;
		ClientHeartBeatThread.byteArrayCopy(ClientHeartBeatThread.intToByteArray(pckLen), 0, tmpBuf, wrtLen, ClientHeartBeatThread.INT_LEN);
		wrtLen += ClientHeartBeatThread.INT_LEN;
		ClientHeartBeatThread.byteArrayCopy(style, 0, tmpBuf, wrtLen, ClientHeartBeatThread.CHAR_LEN);
		wrtLen += ClientHeartBeatThread.CHAR_LEN;
		ClientHeartBeatThread.byteArrayCopy(ClientHeartBeatThread.intToByteArray(billId), 0, tmpBuf, wrtLen, ClientHeartBeatThread.INT_LEN);
		wrtLen += ClientHeartBeatThread.INT_LEN;
		dos.write(tmpBuf, 0, wrtLen);
		dos.flush();			
	}
	private void order(DataOutputStream dos) throws IOException {
		int wrtLen = 0;
		int pckNo = paraBundle.getInt(PCK_NO);
		int version = paraBundle.getInt(MENU_VER);
		int dish = paraBundle.getInt(MENU_DISH);
		int count = paraBundle.getInt(MENU_COUNT);
		String note = paraBundle.getString(MENU_NOTE);
		byte[] noteBytes = note.getBytes("gbk"); 
		byte[] style = {caseNo};
		int pckLen = ClientHeartBeatThread.CHAR_LEN + ClientHeartBeatThread.INT_LEN*3 + noteBytes.length;
		byte[] tmpBuf = new byte[ClientHeartBeatThread.INT_LEN * 2  + pckLen];
		ClientHeartBeatThread.byteArrayCopy(ClientHeartBeatThread.intToByteArray(pckNo), 0, tmpBuf, 0, ClientHeartBeatThread.INT_LEN);
		wrtLen += ClientHeartBeatThread.INT_LEN;
		ClientHeartBeatThread.byteArrayCopy(ClientHeartBeatThread.intToByteArray(pckLen), 0, tmpBuf, wrtLen, ClientHeartBeatThread.INT_LEN);
		wrtLen += ClientHeartBeatThread.INT_LEN;
		ClientHeartBeatThread.byteArrayCopy(style, 0, tmpBuf, wrtLen, ClientHeartBeatThread.CHAR_LEN);
		wrtLen += ClientHeartBeatThread.CHAR_LEN;
		ClientHeartBeatThread.byteArrayCopy(ClientHeartBeatThread.intToByteArray(version), 0, tmpBuf, wrtLen, ClientHeartBeatThread.INT_LEN);
		wrtLen += ClientHeartBeatThread.INT_LEN;
		ClientHeartBeatThread.byteArrayCopy(ClientHeartBeatThread.intToByteArray(dish), 0, tmpBuf, wrtLen, ClientHeartBeatThread.INT_LEN);
		wrtLen += ClientHeartBeatThread.INT_LEN;
		ClientHeartBeatThread.byteArrayCopy(ClientHeartBeatThread.intToByteArray(count), 0, tmpBuf, wrtLen, ClientHeartBeatThread.INT_LEN);
		wrtLen += ClientHeartBeatThread.INT_LEN;
		ClientHeartBeatThread.byteArrayCopy(noteBytes, 0, tmpBuf, wrtLen, noteBytes.length);
		wrtLen += noteBytes.length;
		dos.write(tmpBuf, 0, wrtLen);
		dos.flush();		
	}
	private void login(DataOutputStream dos) throws IOException {
		int wrtLen = 0;
		int pckNo = paraBundle.getInt(PCK_NO);
		String name = paraBundle.getString(LOGIN_NAME);
		byte[] nameBytes = name.getBytes();
		String psw = paraBundle.getString(LOGIN_PSW);
		byte[] pswBytes = psw.getBytes(); 
		byte[] style = {caseNo};
		int pckLen = ClientHeartBeatThread.CHAR_LEN + name.length() + psw.length();
		byte[] tmpBuf = new byte[ClientHeartBeatThread.INT_LEN * 2  + pckLen];
		ClientHeartBeatThread.byteArrayCopy(ClientHeartBeatThread.intToByteArray(pckNo), 0, tmpBuf, 0, ClientHeartBeatThread.INT_LEN);
		wrtLen += ClientHeartBeatThread.INT_LEN;
		ClientHeartBeatThread.byteArrayCopy(ClientHeartBeatThread.intToByteArray(pckLen), 0, tmpBuf, wrtLen, ClientHeartBeatThread.INT_LEN);
		wrtLen += ClientHeartBeatThread.INT_LEN;
		ClientHeartBeatThread.byteArrayCopy(style, 0, tmpBuf, wrtLen, ClientHeartBeatThread.CHAR_LEN);
		wrtLen += ClientHeartBeatThread.CHAR_LEN;
		ClientHeartBeatThread.byteArrayCopy(nameBytes, 0, tmpBuf, wrtLen, nameBytes.length);
		wrtLen += nameBytes.length;
		ClientHeartBeatThread.byteArrayCopy(pswBytes, 0, tmpBuf, wrtLen, pswBytes.length);
		wrtLen += pswBytes.length;
		dos.write(tmpBuf, 0, wrtLen);
		dos.flush();			
	}
	private void setDestNo(DataOutputStream dos) throws IOException {
		int wrtLen = 0;
		int pckNo = paraBundle.getInt(PCK_NO);
		int deskNo = paraBundle.getInt(DESK_NO);
		byte[] style = {caseNo};
		int pckLen = ClientHeartBeatThread.INT_LEN + ClientHeartBeatThread.CHAR_LEN;
		byte[] tmpBuf = new byte[ClientHeartBeatThread.INT_LEN * 2  + pckLen];
		ClientHeartBeatThread.byteArrayCopy(ClientHeartBeatThread.intToByteArray(pckNo), 0, tmpBuf, 0, ClientHeartBeatThread.INT_LEN);
		wrtLen += ClientHeartBeatThread.INT_LEN;
		ClientHeartBeatThread.byteArrayCopy(ClientHeartBeatThread.intToByteArray(pckLen), 0, tmpBuf, wrtLen, ClientHeartBeatThread.INT_LEN);
		wrtLen += ClientHeartBeatThread.INT_LEN;
		ClientHeartBeatThread.byteArrayCopy(style, 0, tmpBuf, wrtLen, ClientHeartBeatThread.CHAR_LEN);
		wrtLen += ClientHeartBeatThread.CHAR_LEN;
		ClientHeartBeatThread.byteArrayCopy(ClientHeartBeatThread.intToByteArray(deskNo), 0, tmpBuf, wrtLen, ClientHeartBeatThread.INT_LEN);
		wrtLen += ClientHeartBeatThread.INT_LEN;
		dos.write(tmpBuf, 0, wrtLen);
		dos.flush();	
	}
	private void mobileBind(DataOutputStream dos) throws IOException {
		int wrtLen = 0;
		int pckNo = paraBundle.getInt(PCK_NO);
		String bindStr = paraBundle.getString(MOBILE_BIND);
		byte[] bindStrBytes = bindStr.getBytes();
		byte[] style = {caseNo};
		int pckLen = bindStr.length() + ClientHeartBeatThread.CHAR_LEN;
		byte[] tmpBuf = new byte[ClientHeartBeatThread.INT_LEN * 2  + pckLen];
		ClientHeartBeatThread.byteArrayCopy(ClientHeartBeatThread.intToByteArray(pckNo), 0, tmpBuf, 0, ClientHeartBeatThread.INT_LEN);
		wrtLen += ClientHeartBeatThread.INT_LEN;
		ClientHeartBeatThread.byteArrayCopy(ClientHeartBeatThread.intToByteArray(pckLen), 0, tmpBuf, wrtLen, ClientHeartBeatThread.INT_LEN);
		wrtLen += ClientHeartBeatThread.INT_LEN;
		ClientHeartBeatThread.byteArrayCopy(style, 0, tmpBuf, wrtLen, ClientHeartBeatThread.CHAR_LEN);
		wrtLen += ClientHeartBeatThread.CHAR_LEN;
		ClientHeartBeatThread.byteArrayCopy(bindStrBytes, 0, tmpBuf, wrtLen, bindStrBytes.length);
		wrtLen += bindStrBytes.length;
		dos.write(tmpBuf, 0, wrtLen);
		dos.flush();		
	}

}
