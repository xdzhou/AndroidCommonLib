package com.loic.common.fragManage;

import java.util.List;

import com.loic.common.utils.R;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MenuDrawerAdapter extends ArrayAdapter<MenuElementItem>
{
	public MenuDrawerAdapter(Context context, List<MenuElementItem> objects) 
	{
		super(context, -1, objects);
	}

	@Override
	public int getItemViewType(int position) 
	{
		return getItem(position).isForSection ? 0 : 1;
	}

	@Override
	public int getViewTypeCount() 
	{
		return 2; //0:section 1:element
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) 
	{
		MenuElementItem item = getItem(position);
		if(convertView == null)
		{
			convertView = LayoutInflater.from(getContext()).inflate(R.layout.menu_item_view, parent, false);
			if(getItemViewType(position) == 0)
				convertView.setBackgroundColor(Color.GRAY);
			else
				convertView.setBackgroundColor(Color.LTGRAY);	
		}
		
		ImageView menuIcon = (ImageView) convertView.findViewById(R.id.menu_icon_image_view);
		TextView menuTitle = (TextView) convertView.findViewById(R.id.menu_title_text_view);
		
		if(item.iconResId == -1)
			menuIcon.setVisibility(View.GONE);
		else
			menuIcon.setImageResource(item.iconResId);
		
		menuTitle.setText(item.title);
		
		return convertView;
	}
}
