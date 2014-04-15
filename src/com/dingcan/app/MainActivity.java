package com.dingcan.app;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;

import com.dingcan.frag.OrdFragment;
import com.dingcan.frag.ResFragment;
import com.dingcan.frag.BaseFragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.dingcan.adapter.OrdAdapter;
import com.dingcan.background.*;

public class MainActivity extends Activity {
	
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private String[] mDishTitles;
    
    public static Boolean isLoggedin = false;
    public static Double total_price = 0.00;
    
	ClientHeartBeatThread ct = null;
	Context context = null;
	public static  Socket client = null;
	public Handler handler = null;
	public static boolean payFlag = false;
	public static int billId = -1;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
		if (android.os.Build.VERSION.SDK_INT > 9) {
		    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		    StrictMode.setThreadPolicy(policy);
		}
		File dir = new File(DCHandler.dirPath);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		File ipFile = new File(DCHandler.IP_FILE_PATH);
		if (!ipFile.exists()) {
			try {
				if (ipFile.createNewFile()) {
					FileWriter fw = new FileWriter(ipFile);
					fw.write(ClientHeartBeatThread.HOST);
					fw.close();
				}
			} catch (IOException e) {
				Log.e("file err", e.getMessage());
				e.printStackTrace();
			}
		}
		context = getApplicationContext();
		handler = new DCHandler(context);
        setUpConnection();
        
        mTitle = mDrawerTitle = getTitle();
        mDishTitles = getResources().getStringArray(R.array.action_array);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listener
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, mDishTitles));
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        // enable ActionBar app icon to behave as action to toggle nav drawer
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
                ) {
            public void onDrawerClosed(View view) {
                getActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        if (savedInstanceState == null) {
            selectItem(0);
        }
        
        
    }

    public void setUpConnection() {
		Message msg = new Message();
		msg.what = DCHandler.SOCKET_MSG;
		try {
			if (client == null) {
				client = new Socket();
				client.connect(new InetSocketAddress(ClientHeartBeatThread.HOST, ClientHeartBeatThread.PORT), 5000);
			}
			if (ct == null || !ct.runFlag ) {
				ct = new ClientHeartBeatThread(client, handler);
				ct.start();
				msg.obj = "连接服务器成功";
			}
			Bundle b = new Bundle();
			String imei = ((TelephonyManager) getSystemService(TELEPHONY_SERVICE)).getDeviceId();
			b.putString(ClientCaseThread.MOBILE_BIND, imei);
			b.putInt(ClientCaseThread.PCK_NO,  ClientHeartBeatThread.pckNo);
			ClientHeartBeatThread.pckNo ++;
			new ClientCaseThread((byte)0, b, client).start();
		} catch (UnknownHostException e) {
			Log.e("order socket err", "Unknown host");
			msg.obj = "服务器无法访问";
			client = null;
		} catch (IOException e) {
			Log.e("order socket err", "Socket IO Exception :" + e.getMessage());
			msg.obj = "连接服务器失败";
			client = null;
		} finally {
			handler.sendMessage(msg);
		}		
	}

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        menu.findItem(R.id.checkout).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle action buttons
        switch(item.getItemId()) {
        case R.id.checkout:
	        if (OrdAdapter.list_list == null) 
	        	return super.onOptionsItemSelected(item);
	        if (payFlag) {
				Bundle b = new Bundle();
				b.putInt(ClientCaseThread.BILL_NO, billId);
				b.putInt(ClientCaseThread.PCK_NO, ClientHeartBeatThread.pckNo);
				ClientHeartBeatThread.pckNo ++;
				new ClientCaseThread((byte)4, b, client).start();	
	        	payFlag = false;
				finish();	        	
				startActivity(getIntent());
				setUpConnection();
	        } else {
	        	for (HashMap<String,Object> h : OrdAdapter.list_list) {
	        		int cnt = (Integer)h.get("quantity");
	        		if (cnt > 0) {
	        			int dish = Integer.parseInt((String) h.get("id"));     
	        			String note = (String)h.get("note");
	        			Bundle b = new Bundle();
	        			b.putInt(ClientCaseThread.PCK_NO, ClientHeartBeatThread.pckNo);
	        			b.putInt(ClientCaseThread.MENU_VER, ClientCaseThread.MENU_VER_INT);
	        			b.putInt(ClientCaseThread.MENU_DISH, dish);
	        			b.putInt(ClientCaseThread.MENU_COUNT, cnt);
	        			b.putString(ClientCaseThread.MENU_NOTE, note);
	        			ClientHeartBeatThread.pckNo++;
	        			new ClientCaseThread((byte)3, b, client).start();
	        		}
	        	}
	        }


			/*
        	new AlertDialog.Builder(this).setTitle("确认提交菜单吗？" + "总价:￥" + total_price)
	    		.setPositiveButton("确定", new DialogInterface.OnClickListener() { 
	    			@Override 
	    			public void onClick(DialogInterface dialog, int which) { 
	    				// 点击“确认”后的操作 

	    			} 
	    		}) 
	    		.setNegativeButton("取消", new DialogInterface.OnClickListener() {
	    			@Override 
	    			public void onClick(DialogInterface dialog, int which) { 
	    			// 点击“返回”后的操作,这里不设置没有任何操作 
	    			} 
	    		})
	    		.show();*/
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    /* The click listner for ListView in the navigation drawer */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    private void selectItem(int position) {
        // update the main content by replacing fragments
        Fragment Ord_fragment = new OrdFragment();
        Fragment Res_fragment = new ResFragment();
        Fragment Base_fragment = new BaseFragment();

        FragmentManager fragmentManager = getFragmentManager();

        // update selected item and title, then close the drawer
        mDrawerList.setItemChecked(position, true);
        setTitle(mDishTitles[position]);
        switch(position) {
        case 0: //登录部分
        	fragmentManager.beginTransaction().replace(R.id.content_frame, Base_fragment).commit();

        	LayoutInflater factory=LayoutInflater.from(MainActivity.this);
        	if (client == null) 
        		setUpConnection();
            //得到自定义对话框  
            final View DialogView=factory.inflate(R.layout.login_dialog, null);  
            //创建对话框 
            new AlertDialog.Builder(this)
            .setTitle("登录")
            .setView(DialogView)
            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                	isLoggedin = true;
                	EditText etU = (EditText) DialogView.findViewById(R.id.username);
                	EditText etP = (EditText) DialogView.findViewById(R.id.password);
                	String name = etU.getText().append('\0').toString();
                	String psw = etP.getText().append('\0').toString();
    				Bundle b = new Bundle();
    				b.putInt(ClientCaseThread.PCK_NO, ClientHeartBeatThread.pckNo);
    				b.putString(ClientCaseThread.LOGIN_NAME, name);
    				b.putString(ClientCaseThread.LOGIN_PSW, psw);
    				ClientHeartBeatThread.pckNo++;
    				new ClientCaseThread((byte)2, b, client).start();
                }
             })
            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) { 
                    // do nothing
                }
             })
             .show();
        	break;
        case 1: //选座部分
        	fragmentManager.beginTransaction().replace(R.id.content_frame, Res_fragment).commit();

//        	Toast toast1 = Toast.makeText(MainActivity.this, "选座部分", Toast.LENGTH_SHORT);
//        	toast1.show();
        	break;
        case 2: //点餐部分 
        	fragmentManager.beginTransaction().replace(R.id.content_frame, Ord_fragment).commit();

//        	Toast toast2 = Toast.makeText(MainActivity.this, "点餐部分!", Toast.LENGTH_SHORT);
//        	toast2.show();
        	break;
        case 3: //设置部分
        	fragmentManager.beginTransaction().replace(R.id.content_frame, Base_fragment).commit();
        	
        	final EditText etIP = new EditText(this);
        	new AlertDialog.Builder(this)
        	.setTitle("请输入服务器IP")
        	.setView(etIP)
        	.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) { 
                	String IPStr = etIP.getText().toString();
                	Message msg = new Message();
                	msg.what = DCHandler.SET_IP;
                	msg.obj = IPStr;
                	handler.sendMessage(msg);
                }
             })
        	.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) { 
                    // do nothing
                }
             })
        	.show();
        	break;
        case 4: //关于部分
        	fragmentManager.beginTransaction().replace(R.id.content_frame, Base_fragment).commit();
        	
        	new AlertDialog.Builder(this)
            .setTitle("关于")
            .setMessage("点餐系统")
            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // do nothing
                }
             })
             .show();
        	
        	break;
        default: ;
        }
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }
    
    @Override 
    public void onBackPressed() {
    	new AlertDialog.Builder(this).setTitle("确认退出吗？")
    		.setPositiveButton("确定", new DialogInterface.OnClickListener() { 
    			@Override 
    			public void onClick(DialogInterface dialog, int which) { 
    				// 点击“确认”后的操作 
    				MainActivity.this.finish(); 
    			} 
    		}) 
    		.setNegativeButton("取消", new DialogInterface.OnClickListener() {
    			@Override 
    			public void onClick(DialogInterface dialog, int which) { 
    			// 点击“返回”后的操作,这里不设置没有任何操作 
    			} 
    		})
    	.show();
    }
	@Override
	protected void onDestroy() {
		if (client != null) {
			try {
				client.close();
				if (ct != null)
					ct.runFlag = false;
			} catch (IOException e) {
				Log.e("order socket err", "close client socket error.");
			}
		}
		super.onDestroy();
	}    
}