package com.nbow.advanceeditor.code;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.nbow.advanceeditor.R;

import java.util.ArrayList;
import java.util.List;

public class AutoCompleteKeywordAdapter extends ArrayAdapter<String> {
    private List<String> keywordListFull;

    public AutoCompleteKeywordAdapter(@NonNull Context context, @NonNull List<String> countryList) {
        super(context, 0, countryList);
        keywordListFull = new ArrayList<>(countryList);
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return keywordFilter;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(
                    R.layout.keyword_autocomplete_row, parent, false
            );
        }

        TextView textViewName = convertView.findViewById(R.id.tv_keyword);

        String keyword = getItem(position);

        if (keyword != null) {
            textViewName.setText(keyword);
        }

        return convertView;
    }


    private Filter keywordFilter = new Filter() {
        String prefix = new String("");
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            List<String> suggestions = new ArrayList<>();
//            Log.e("TAG", "performFiltering: constrain : "+constraint);
//            int gt = constraint.toString().lastIndexOf('>');
            int indexOfRightMostChar = constraint.toString().lastIndexOf('<');

            prefix = "";
            if (constraint == null || constraint.length() == 0) {
                suggestions.addAll(keywordListFull);
            } else {

                if(indexOfRightMostChar!=-1){
                    String filterPattern = constraint.toString().substring(indexOfRightMostChar).trim();
//                    Log.e("TAG", "performFiltering: filter pattern "+filterPattern);
                    prefix = constraint.toString().substring(0,indexOfRightMostChar);

                    if(filterPattern.length()>1)
                        for (String item : keywordListFull) {
                            if (item.toLowerCase().contains(filterPattern)) {
                                suggestions.add(item);
                            }
                        }
                }

            }

            results.values = suggestions;
            results.count = suggestions.size();

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            clear();
            if(results.values!=null)
                addAll((List) results.values);
            notifyDataSetChanged();
        }

        @Override
        public CharSequence convertResultToString(Object resultValue) {
            Log.e("TAG", "convertResultToString: "+prefix+(String) resultValue);
            String result = prefix+((String) resultValue);
            return  result;
        }
    };
}
