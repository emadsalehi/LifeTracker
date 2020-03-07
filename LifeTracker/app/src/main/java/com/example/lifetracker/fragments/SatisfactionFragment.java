package com.example.lifetracker.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.lifetracker.R;

import java.util.Objects;

public class SatisfactionFragment extends Fragment {

    private RatingBar ratingBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.add_satisfaction, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ratingBar = Objects.requireNonNull(getView()).findViewById(R.id.ratingBar);
        assert getArguments() != null;
        TextView timeSplitView = Objects.requireNonNull(getView()).findViewById(R.id.satisfaction_time_splits);
        String text = getArguments().getString("startSplit") + " to " + getArguments().getString("endSplit");
        timeSplitView.setText(text);
    }

    public RatingBar getRatingBar() {
        return ratingBar;
    }
}
