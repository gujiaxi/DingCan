package com.dingcan.adapter;  

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;  

import com.dingcan.app.MainActivity;
import com.dingcan.app.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;  
import android.view.View.OnClickListener;
import android.view.ViewGroup;  
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class OrdAdapter extends BaseAdapter {

    private Context context;
    private LayoutInflater inflater;
    private int[] imagesID;
    public static  List<HashMap<String,Object>> list_list;
    public static boolean uriFlag = false;
    public OrdAdapter(Context context, int[] imagesID, List<HashMap<String,Object>> list_list){
        this.context = context;
        this.imagesID = imagesID;
        this.list_list = list_list;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return imagesID.length;
    }

    @Override
    public Object getItem(int i) {
        return imagesID[i];
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        final ListItemView listItemView;
        if(view == null){
            listItemView = new ListItemView();
            view = inflater.inflate(R.layout.dish_item, null);
            listItemView.img = (ImageView)view.findViewById(R.id.img);
            listItemView.title = (TextView)view.findViewById(R.id.title);
            listItemView.price = (TextView)view.findViewById(R.id.price);
            listItemView.vprice = (TextView)view.findViewById(R.id.vprice);
            listItemView.info = (TextView)view.findViewById(R.id.info);
            listItemView.btn_minus = (ImageButton)view.findViewById(R.id.imageButton1);
            listItemView.btn_plus = (ImageButton)view.findViewById(R.id.imageButton2);
            listItemView.quantity = (TextView)view.findViewById(R.id.textView1);
            listItemView.note = (EditText)view.findViewById(R.id.editText1);
            view.setTag(listItemView);
        }else{
            listItemView = (ListItemView)view.getTag();
        }
        try {
        	listItemView.title.setText(list_list.get(i).get("title").toString());
        	listItemView.price.setText("￥" + list_list.get(i).get("price").toString());
        	listItemView.vprice.setText("会员价: ￥" + list_list.get(i).get("vprice").toString());
        	listItemView.info.setText(list_list.get(i).get("info").toString());
        	listItemView.id = (String)list_list.get(i).get("id");
        	listItemView.file_bmp = (Bitmap) list_list.get(i).get("file_bmp");
        	listItemView.file_name = (String)list_list.get(i).get("file_name");
        	listItemView.item_id = i;
        } catch (NullPointerException e) {
        	return view;
        }
        if (!uriFlag) 
        	listItemView.img.setImageResource(imagesID[i]);
        else {
//        	listItemView.img.setImageResource(imagesID[i]);
    		BitmapFactory.Options options = new BitmapFactory.Options();
    		options.inJustDecodeBounds = false;
        	try {
				listItemView.img.setImageBitmap(BitmapFactory.decodeStream(new FileInputStream(new File(listItemView.file_name)), null,options));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        OnClickListener listener = new OnClickListener() {
        	int q = Integer.parseInt((String) listItemView.quantity.getText());
        	@Override
        	public void onClick(View v) {
        		if (v == listItemView.btn_minus) {
        			//TODO 减去一件
        			if (q !=0 ) {
        				q--;
        				listItemView.quantity.setText(String.valueOf(q));
        				if (!MainActivity.isLoggedin) MainActivity.total_price -= Double.parseDouble(list_list.get(i).get("price").toString());
        				else MainActivity.total_price -= Double.parseDouble(list_list.get(i).get("vprice").toString());
        			}
        		}
        		if (v == listItemView.btn_plus) {
        			//TODO 加上一件 
        			q++;
        			listItemView.quantity.setText(String.valueOf(q));
    				if (!MainActivity.isLoggedin) MainActivity.total_price += Double.parseDouble(list_list.get(i).get("price").toString());
    				else MainActivity.total_price += Double.parseDouble(list_list.get(i).get("vprice").toString());
        		}
                list_list.get(listItemView.item_id).put("quantity", Integer.parseInt((String)listItemView.quantity.getText()));
                list_list.get(listItemView.item_id).put("note", listItemView.note.getText().toString());
        	}
        };
        listItemView.btn_minus.setOnClickListener(listener);
        listItemView.btn_plus.setOnClickListener(listener);

        return view;
    }

    public class ListItemView{
    	public ImageView img;
        public TextView title;
        public TextView price;
        public TextView vprice;
        public TextView info;
        public ImageButton btn_minus;
        public TextView quantity;
        public ImageButton btn_plus;
        public EditText note;
        public String id;
        public int item_id;
        public String file_name;
        public Bitmap file_bmp;
    }
}