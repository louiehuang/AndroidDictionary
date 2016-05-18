package com.writing.hlyin.dicttest.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.writing.hlyin.dicttest.R;

import java.util.List;

/**
 * Created by hlyin on 5/16/16.
 * 候选词ListView的adapter
 */
//alt + enter 实现父类抽象方法
public class MyAdapter extends BaseAdapter {

    List<String> list;
    LayoutInflater inflater; //反射器，在构造器中初始化

    //command + N 自动生成代码
    public MyAdapter(Context context) {
        this.inflater = LayoutInflater.from(context);
    }

    public void setList(List<String> list) {
        //this.list = list;
        /*只取30个元素*/
        int length = list.size() > 30 ? 30 : list.size();
        if(length <= 30){
            this.list = list;
        }
        else{
            this.list = list.subList(0, 30);
        }
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
        /*2级优化出现问题，listView下部分词点击后显示的释义不是所点的词*/
//        ViewHolder viewHolder = null;
//
//        if (convertView == null) {
//            convertView = inflater.inflate(R.layout.list_item, null);
//            viewHolder = new ViewHolder();
//
//            //view才有findViewById, 而this(这里是Adapter)没有, (MainActivity有)
//            viewHolder.word = (TextView) convertView.findViewById(R.id.item_tv_word);
//
//            convertView.setTag(viewHolder); //
//        } else {
//            viewHolder = (ViewHolder) convertView.getTag();
//        }
//
//
//        ViewHolder.word.setText(list.get(position));
//
//        return convertView;

        /*原始方法，效率低*/
        View view = inflater.inflate(R.layout.list_item, null);
        TextView word = (TextView) view.findViewById(R.id.item_tv_word);
        word.setText(list.get(position));
        return view;

    }


}


class ViewHolder {
    static TextView word;
}