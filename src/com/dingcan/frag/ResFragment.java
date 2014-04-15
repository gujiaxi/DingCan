package com.dingcan.frag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ListAdapter;

import com.dingcan.adapter.ResAdapter;
import com.dingcan.app.MainActivity;
import com.dingcan.app.R;
import com.dingcan.background.ClientCaseThread;
import com.dingcan.background.ClientHeartBeatThread;

public class ResFragment extends Fragment {	
	static GridView gridview;
	static boolean flag = true, hadFlag = false;
	private static int[] imagesID = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
    private String[] seat = {"NO.1","NO.2","NO.3","NO.4", "NO.5", "NO.6", "NO.7", "NO.8", "NO.9", "NO.10"};
	protected static Context context;
    private static List<HashMap<String,Object>> grid_list = null;
    static int deskNo = 0;
    public static Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			if (msg.arg1 == -1 || msg.arg1 == 1) {
				if (msg.arg1 == 1)
					hadFlag = true;
				flag = false;
				if (deskNo >= 0 && deskNo < imagesID.length) {
					imagesID[deskNo] = R.drawable.seat_occupied;
					ListAdapter adapter = new ResAdapter(context, imagesID, grid_list);
					gridview.setAdapter(adapter);
				}
			}
			super.handleMessage(msg);
		}
    	
    };
	    
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_reservation, container,false);
		gridview = (GridView)view.findViewById(R.id.GridView1);
		grid_list = getListItems();
		if (flag){
			for (int i = 0; i < seat.length; i++) {
				imagesID[i] = R.drawable.seat_vacant;
			}
		}
		context = getActivity().getApplicationContext();
		ListAdapter adapter = new ResAdapter(context, imagesID, grid_list);
		gridview.setAdapter(adapter);
		gridview.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				if (hadFlag)
					return;
				Bundle b = new Bundle();
				deskNo = position;
				b.putInt(ClientCaseThread.DESK_NO, deskNo);
				b.putInt(ClientCaseThread.PCK_NO, ClientHeartBeatThread.pckNo);
				ClientHeartBeatThread.pckNo ++;
				new ClientCaseThread((byte)1, b, MainActivity.client).start();	
//				Toast.makeText(getActivity(),"此处加入桌位处理的代码 "+seat[position], Toast.LENGTH_SHORT).show();
			}
		});
		return view;
	}

	private List<HashMap<String,Object>> getListItems(){
        List<HashMap<String,Object>> list1 = new ArrayList<HashMap<String, Object>>();
        for(int i=0;i<seat.length;i++){
            HashMap<String,Object> map = new HashMap<String, Object>();
            map.put("text1", seat[i]);
            list1.add(map);
        }
        return list1;
    }
}
