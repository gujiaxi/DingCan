package com.dingcan.frag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.dingcan.adapter.OrdAdapter;
import com.dingcan.app.R;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

public class OrdFragment extends Fragment {
	
	public static float total_price;
	
	private SwipeRefreshLayout srl;
	public static int[] imagesID = {R.drawable.dish1, R.drawable.dish2};
	public static String[] str0 = {"1000", "2000", "3000"};
	public static String[] str1 = {"蒸羊羔", "烧花鸭", "筒子鸡"};
	public static String[] str2 = {"23.00", "35.00", "42.00"};
	public static String[] str3 = {"20.00", "30.00", "40.00"};
	public static String[] str4 = {"外酥里嫩，入口即化。", "肥而不腻，肉嫩汁多。", "肉质鲜美，回味无穷。"};
	public static String[] files = {"/mnt/sdcard/dingcan/1.jpg", "/mnt/sdcard/dingcan/1.jpg"};
	public static Bitmap[] bmps = null;
	private List<HashMap<String,Object>> list_list = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		srl = (SwipeRefreshLayout) inflater.inflate(R.layout.fragment_order, container, false);
		final ListView listView = (ListView) srl.findViewById(R.id.ListView1);
		list_list = getListItems();
		ListAdapter adapter = new OrdAdapter(getActivity().getApplicationContext(), imagesID, list_list);
		listView.setAdapter(adapter);
		srl.setColorScheme(android.R.color.holo_green_dark, android.R.color.holo_orange_dark, android.R.color.holo_blue_dark,
		     android.R.color.holo_red_dark);
		srl.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				new Handler().postDelayed(new Runnable() {
					public void run() {
						srl.setRefreshing(false);
						//TODO: 此处获取服务器端的最新菜单
					}
				}, 1000);
			}
		});
		return srl;
	}
	
	private List<HashMap<String,Object>> getListItems(){
        List<HashMap<String,Object>> list1 = new ArrayList<HashMap<String, Object>>();
        for(int i=0;i<imagesID.length;i++){
            HashMap<String,Object> map = new HashMap<String, Object>();
            map.put("id", str0[i]);
            map.put("title", str1[i]);
            map.put("price", str2[i]);
            map.put("vprice", str3[i]);
            map.put("info", str4[i]);
            map.put("quantity", 0);
            map.put("note", "");
            map.put("file_name", files[i]);
            map.put("file_bmp", null);
            list1.add(map);
        }
        return list1;
    }
}