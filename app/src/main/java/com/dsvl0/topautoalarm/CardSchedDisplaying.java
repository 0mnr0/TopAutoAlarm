package com.dsvl0.topautoalarm;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CardSchedDisplaying {
    private final LinearLayout container;
    private final LayoutInflater inflater;

    public CardSchedDisplaying(Context context, LinearLayout container) {
        this.container = container;
        this.inflater = LayoutInflater.from(context);
    }

    public void add(String time, String subjectName) {
        View cardView = inflater.inflate(R.layout.cardsched, container, false);
        TextView tvTime = cardView.findViewById(R.id.tvTime);
        TextView tvSubject = cardView.findViewById(R.id.tvSubject);
        tvTime.setText(time);
        if (time.isEmpty()) {tvTime.setVisibility(View.GONE);}
        tvSubject.setText(subjectName);
        container.addView(cardView);
    }

    public void clear() {
        container.removeAllViews();
    }
}
