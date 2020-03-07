package com.example.lifetracker.fragments;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.lifetracker.MainActivity;
import com.example.lifetracker.R;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class TimeCategoryFragment extends Fragment {

    private LinearLayout categoriesLinearLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.time_category_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        fillScrollView();
    }


    private void fillScrollView() {
        categoriesLinearLayout = Objects.requireNonNull(getView()).findViewById(R.id.category_layout);
        ((MainActivity) Objects.requireNonNull(getActivity())).readCsvFile();
        List<String> categories = ((MainActivity) Objects.requireNonNull(getActivity())).getCategories();
        categoriesLinearLayout.removeAllViews();
        for (final String category : categories) {
            addTextToLinearLayout(category);
        }
    }

    public void addCategory(String category) {
        List<String> categories = ((MainActivity) Objects.requireNonNull(getActivity())).getCategories();
        Map<String, List<String>> categoryTable = ((MainActivity) getActivity()).getCategoryTable();
        if (categories.contains(category)) {
            ((MainActivity) Objects.requireNonNull(getActivity())).onCategoryClicked(category);
        } else {
            addTextToLinearLayout(category);
            categories.add(category);
            for (Map.Entry mapElement : categoryTable.entrySet()) {
                String key = (String) mapElement.getKey();
                Objects.requireNonNull(categoryTable.get(key)).add("None|0");
            }
            ((MainActivity) getActivity()).updateCsvFile();
        }
    }

    private void addTextToLinearLayout(final String text) {
        TextView textView = new TextView(getContext());
        textView.setText(text);
        textView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT
                , ViewGroup.LayoutParams.WRAP_CONTENT));
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) Objects.requireNonNull(getActivity())).onCategoryClicked(text);
            }
        });
        textView.setTextSize(30);
        textView.setTextColor(ContextCompat.getColor(Objects.requireNonNull(getContext()), R.color.colorPrimaryDark));
        textView.setGravity(Gravity.CENTER);
        categoriesLinearLayout.addView(textView);
    }
}
