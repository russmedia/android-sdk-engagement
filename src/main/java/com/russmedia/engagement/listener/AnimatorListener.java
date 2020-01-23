package com.russmedia.engagement.listener;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.view.View;
import android.widget.TextView;

import com.russmedia.engagement.helper.Utils;

public class AnimatorListener extends AnimatorListenerAdapter {

    TextView animatedTextView;
    TextView valueTextView;
    String text;

    public AnimatorListener(TextView animatedTextView, TextView valueTextView, String text) {
        this.animatedTextView = animatedTextView;
        this.valueTextView = valueTextView;
        this.text = text;
    }

    @Override
    public void onAnimationEnd(Animator animation) {
        super.onAnimationEnd(animation);
        animatedTextView.setVisibility(View.GONE);
        valueTextView.setText(Utils.setThousandsSeperator( Integer.parseInt(text),"." ));
        valueTextView.setVisibility(View.VISIBLE);
    }
}
