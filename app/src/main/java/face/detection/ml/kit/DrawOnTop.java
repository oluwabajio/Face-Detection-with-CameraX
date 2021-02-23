package face.detection.ml.kit;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class DrawOnTop extends View {
    int screenCenterX = 0;
    int screenCenterY = 0;
    int radius = 50;

    Paint paint = new Paint();
    Paint paint2 = new Paint();
    RectF rectF = new RectF(0,0,0,0);

    public DrawOnTop(Context context) {
        super(context);
    }

    public DrawOnTop(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public DrawOnTop(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public DrawOnTop(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

    }


    public void DrawOnTopo(Context context, int screenCenterX, int screenCenterY, int radius) {
        this.screenCenterX = screenCenterX;
        this.screenCenterY = screenCenterY;
        this.radius = radius;


        paint.setStrokeWidth(15);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(getResources().getColor(R.color.black));

        paint2.setStyle(Paint.Style.FILL);
        paint2.setColor(getResources().getColor(R.color.white));


        postInvalidate();
    }

    public void drawRecto(RectF rectf) {


        paint.setStrokeWidth(15);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(getResources().getColor(R.color.black));

        paint2.setStyle(Paint.Style.FILL);
        paint2.setColor(getResources().getColor(R.color.white));
        this.rectF = rectf;

        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(getResources().getColor(R.color.white));
        paint.setAlpha(130);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawPaint(paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
//        canvas.drawCircle(screenCenterX, screenCenterY, radius, paint);
//        canvas.drawRect(0,0, getWidth(), getHeight(), paint);
        canvas.drawRect(rectF, paint);
        canvas.drawRect(rectF, paint2);
        super.onDraw(canvas);
    }
}
