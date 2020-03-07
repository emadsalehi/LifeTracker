package com.example.lifetracker.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.lifetracker.R;

import java.util.Objects;

public class AddTimeFragment extends Fragment {

    private EditText timeType, timeValue;
    private String category;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.add_time_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        assert getArguments() != null;
        TextView timeSplitView = Objects.requireNonNull(getView()).findViewById(R.id.time_splits);
        String text = getArguments().getString("startSplit") + " to " + getArguments().getString("endSplit");
        timeSplitView.setText(text);
        timeType = getView().findViewById(R.id.time_type);
        timeValue = getView().findViewById(R.id.value_number);
        category = getArguments().getString("category");
    }

    public EditText getTimeType() {
        return timeType;
    }

    public EditText getTimeValue() {
        return timeValue;
    }

    public String getCategory() {
        return category;
    }
}
