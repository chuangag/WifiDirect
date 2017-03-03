package com.example.d4561.wifidirect;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

/**
 * Created by d4561 on 2017/3/3.
 */

public class InfoAdapter extends ArrayAdapter<Info> {
    // 畫面資源編號
    private int resource;
    // 包裝的記事資料
    private List<Info> items;

    public InfoAdapter(Context context, int resource, List<Info> items) {
        super(context, resource, items);
        this.resource = resource;
        this.items = items;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LinearLayout itemView;
        // 讀取目前位置的記事物件
        final Info item = getItem(position);

        if (convertView == null) {
            // 建立項目畫面元件
            itemView = new LinearLayout(getContext());
            String inflater = Context.LAYOUT_INFLATER_SERVICE;
            LayoutInflater li = (LayoutInflater)
                    getContext().getSystemService(inflater);
            li.inflate(resource, itemView, true);
        }
        else {
            itemView = (LinearLayout) convertView;
        }
        TextView time=(TextView) itemView.findViewById(R.id.time_of_message);
        TextView sender=(TextView) itemView.findViewById(R.id.name_sender);
        TextView receiver=(TextView) itemView.findViewById(R.id.name_receiver);

        time.setText(item.getTimeOfMessage());
        sender.setText(item.getSender());
        receiver.setText(item.getReceiver());
       /* // 讀取記事顏色、已選擇、標題與日期時間元件
        RelativeLayout typeColor = (RelativeLayout) itemView.findViewById(R.id.type_color);
        ImageView selectedItem = (ImageView) itemView.findViewById(R.id.selected_item);
        TextView titleView = (TextView) itemView.findViewById(R.id.title_text);
        TextView dateView = (TextView) itemView.findViewById(R.id.date_text);

        // 設定記事顏色
        GradientDrawable background = (GradientDrawable)typeColor.getBackground();
        background.setColor(item.getColor().parseColor());

        // 設定標題與日期時間
        titleView.setText(item.getTitle());
        dateView.setText(item.getLocaleDatetime());

        // 設定是否已選擇
        selectedItem.setVisibility(item.isSelected() ? View.VISIBLE : View.INVISIBLE);*/

        return itemView;
    }

    // 設定指定編號的記事資料
    public void set(int index, Info item) {
        if (index >= 0 && index < items.size()) {
            items.set(index, item);
            notifyDataSetChanged();
        }
    }

    // 讀取指定編號的記事資料
    public Info get(int index) {
        return items.get(index);
    }
}
