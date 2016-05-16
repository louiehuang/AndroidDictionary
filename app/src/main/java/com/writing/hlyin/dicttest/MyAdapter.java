package com.writing.hlyin.dicttest;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

/**
 * Created by hlyin on 5/16/16.
 */
//alt + enter 实现父类抽象方法
public class MyAdapter extends BaseAdapter {

    List<Map<String, Object>> list;
    LayoutInflater inflater; //反射器，在构造器中初始化

    //command + N 自动生成代码
    public MyAdapter(Context context) {
        this.inflater = LayoutInflater.from(context);
    }

    public void setList(List<Map<String, Object>> list) {
        this.list = list;
    }


    @Override
    public int getCount() {

        return list.size() > 30 ? 30 : list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item, null);
            viewHolder = new ViewHolder();

            //view才有findViewById, 而this(这里是Adapter)没有, (MainActivity有)
            viewHolder.word = (TextView) convertView.findViewById(R.id.item_tv_word);

            convertView.setTag(viewHolder); //
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Map map = list.get(position); //Map<String, Object>

        viewHolder.word.setText((String) map.get("word"));

        return convertView;
    }


    public class ViewHolder {
        TextView word;
    }
}
