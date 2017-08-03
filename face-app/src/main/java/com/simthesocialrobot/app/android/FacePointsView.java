package com.simthesocialrobot.app.android;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by jluetke on 7/31/17.
 */

public class FacePointsView extends View {

    public PointF[] points;
    public int pH = 1;
    public int pW = 1;

    public FacePointsView(Context context) {
        super(context);
    }

    public FacePointsView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void onDraw(Canvas canvas) {
        if (points == null) return;

        int scaleH = canvas.getHeight() / pH;
        int scaleW = canvas.getWidth() / pW;

        PointF[] drawPoints = points;
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);

        canvas.drawLine(0, 0, canvas.getWidth(), 0, paint); // ----
        canvas.drawLine(0, 0, 0, canvas.getHeight(), paint); // |
        canvas.drawLine(canvas.getWidth(), 0, canvas.getWidth() - 1, canvas.getHeight() - 1, paint);//     |
        canvas.drawLine(0, canvas.getHeight(), canvas.getWidth() - 1, canvas.getHeight() - 1, paint);

        for (PointF p : drawPoints) {
            canvas.drawCircle(p.x * scaleW, p.y * scaleH, 10, paint);
        }
    }
}
