package fr.bmartel.bluetooth.hcidebugger.view;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

/**
 * Created by akinaru on 24/03/16.
 */
public class CustomRecyclerView extends RecyclerView {
    Context context;

    public CustomRecyclerView(Context context) {
        super(context);
        this.context = context;
    }

    public CustomRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean fling(int velocityX, int velocityY) {

        return super.fling(velocityX, velocityY);
    }
}
