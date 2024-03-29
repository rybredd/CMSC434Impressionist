package edu.umd.hcil.impressionistpainter434;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import java.io.File;
import java.text.MessageFormat;
import java.util.Random;

/**
 * Created by jon on 3/20/2016.
 */
public class ImpressionistView extends View {

    private ImageView _imageView;

    private Canvas _offScreenCanvas = null;
    private Bitmap _offScreenBitmap = null;
    private Paint _paint = new Paint();

    private int _alpha = 150;
    private int _defaultRadius = 25;
    private Point _lastPoint = null;
    private long _lastPointTime = -1;
    private boolean _useMotionSpeedForBrushStrokeSize = true;
    private Paint _paintBorder = new Paint();
    private BrushType _brushType = BrushType.Square;
    private float _minBrushRadius = 5;

    public ImpressionistView(Context context) {
        super(context);
        init(null, 0);
    }

    public ImpressionistView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public ImpressionistView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    /**
     * Because we have more than one constructor (i.e., overloaded constructors), we use
     * a separate initialization method
     * @param attrs
     * @param defStyle
     */
    private void init(AttributeSet attrs, int defStyle){

        // Set setDrawingCacheEnabled to true to support generating a bitmap copy of the view (for saving)
        // See: http://developer.android.com/reference/android/view/View.html#setDrawingCacheEnabled(boolean)
        //      http://developer.android.com/reference/android/view/View.html#getDrawingCache()
        this.setDrawingCacheEnabled(true);

        _paint.setColor(Color.RED);
        _paint.setAlpha(_alpha);
        _paint.setAntiAlias(true);
        _paint.setStyle(Paint.Style.FILL);
        _paint.setStrokeWidth(4);

        _paintBorder.setColor(Color.BLACK);
        _paintBorder.setStrokeWidth(3);
        _paintBorder.setStyle(Paint.Style.STROKE);
        _paintBorder.setAlpha(50);

        //_paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY));
    }

    @Override
    protected void onSizeChanged (int w, int h, int oldw, int oldh){

        Bitmap bitmap = getDrawingCache();
        Log.v("onSizeChanged", MessageFormat.format("bitmap={0}, w={1}, h={2}, oldw={3}, oldh={4}", bitmap, w, h, oldw, oldh));
        if(bitmap != null) {
            _offScreenBitmap = getDrawingCache().copy(Bitmap.Config.ARGB_8888, true);
            _offScreenCanvas = new Canvas(_offScreenBitmap);
        }
    }

    /**
     * Sets the ImageView, which hosts the image that we will paint in this view
     * @param imageView
     */
    public void setImageView(ImageView imageView){
        _imageView = imageView;
    }

    /**
     * Sets the brush type. Feel free to make your own and completely change my BrushType enum
     * @param brushType
     */
    public void setBrushType(BrushType brushType){
        _brushType = brushType;
    }

    /**
     * Clears the painting
     */
    public void clearPainting(){
        //TODO
        _offScreenBitmap = Bitmap.createBitmap(_imageView.getWidth(), _imageView.getHeight(), Bitmap.Config.ARGB_8888);
        _offScreenCanvas = new Canvas(_offScreenBitmap);
        invalidate();
    }

    /* returns a random 5 digit id for the randomly saved image file (to make it unique) */
    public String getImgId() {
        String alphaNumeric = "01234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String id = "";
        Random rand = new Random();

        for (int i = 0; i < 5; i++) {
            id += alphaNumeric.charAt(rand.nextInt(alphaNumeric.length() + 1));
        }

        return id;
    }

    /* saves the current painting to the media gallery, returns filename */
    public String savePainting(Context context) {
        try {
            String fileName = "img" + getImgId() + ".jpg";
            String url = MediaStore.Images.Media.insertImage(context.getContentResolver(), _offScreenBitmap, fileName, "");
            return url;
        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR";
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if(_offScreenBitmap != null) {
            canvas.drawBitmap(_offScreenBitmap, 0, 0, _paint);
        }

        // Draw the border. Helpful to see the size of the bitmap in the ImageView
        canvas.drawRect(getBitmapPositionInsideImageView(_imageView), _paintBorder);
    }

    public int calculateNewRadius(int newX, int newY) {
        double t1 = Math.pow(newX - _lastPoint.x, 2);
        double t2 = Math.pow(newY - _lastPoint.y, 2);
        double euclidean = Math.sqrt(t1 + t2);
        long elapsed = System.currentTimeMillis() - _lastPointTime;
        double v = euclidean/elapsed;

        // some random formula that has no bearing
        return ((int)(v * 15) < (int)_minBrushRadius ? (int)_minBrushRadius : (int)(v * 15));
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent){

        //TODO
        //Basically, the way this works is to liste for Touch Down and Touch Move events and determine where those
        //touch locations correspond to the bitmap in the ImageView. You can then grab info about the bitmap--like the pixel color--
        //at that location
        float touchX = motionEvent.getX();
        float touchY = motionEvent.getY();
        Bitmap imageBitmap = _imageView.getDrawingCache();
        int color = imageBitmap.getPixel((int) touchX, (int) touchY);
        int radius = 0;

        _paint.setColor(color);

        switch(motionEvent.getAction()) {
            case MotionEvent.ACTION_UP:
                _lastPoint = null;
                _lastPointTime = -1;
            case MotionEvent.ACTION_MOVE:
                if (_lastPoint != null) {
                    radius = calculateNewRadius((int)touchX, (int)touchY);
                    _lastPoint.set((int) touchX, (int) touchY);
                    _lastPointTime = System.currentTimeMillis();
                } else {
                    radius = _defaultRadius;
                    _lastPoint = new Point();
                    _lastPoint.set((int) touchX, (int) touchY);
                    _lastPointTime = System.currentTimeMillis();
                }

                if (inBounds((int)touchX, (int)touchY)) {
                    switch (_brushType) {
                        case Circle:
                            _offScreenCanvas.drawCircle(touchX, touchY, radius, _paint);
                            break;
                        case Square:
                            _offScreenCanvas.drawRect(touchX, touchY, touchX+radius, touchY-radius, _paint);
                            break;
                        case Triangle:
                            ShapeBrush triangleBrush = new ShapeBrush(_brushType, touchX, touchY, radius);
                            Path triangle = triangleBrush.getPath();
                            _offScreenCanvas.drawPath(triangle, _paint);
                            break;
                        case Hexagon:
                            ShapeBrush hexagonBrush = new ShapeBrush(_brushType, touchX, touchY, radius);
                            Path hexagon = hexagonBrush.getPath();
                            _offScreenCanvas.drawPath(hexagon, _paint);
                            break;
                    }
                }
                break;
        }

        invalidate();
        return true;
    }

    private boolean inBounds(int x, int y) {
        return (getBitmapPositionInsideImageView(_imageView).contains(x, y) &&
                x >= 0 && x < _offScreenBitmap.getScaledWidth(_offScreenCanvas) &&
                y < _offScreenBitmap.getScaledHeight(_offScreenCanvas) && y >= 0);
    }


    /**
     * This method is useful to determine the bitmap position within the Image View. It's not needed for anything else
     * Modified from:
     *  - http://stackoverflow.com/a/15538856
     *  - http://stackoverflow.com/a/26930938
     * @param imageView
     * @return
     */
    private static Rect getBitmapPositionInsideImageView(ImageView imageView){
        Rect rect = new Rect();

        if (imageView == null || imageView.getDrawable() == null) {
            return rect;
        }

        // Get image dimensions
        // Get image matrix values and place them in an array
        float[] f = new float[9];
        imageView.getImageMatrix().getValues(f);

        // Extract the scale values using the constants (if aspect ratio maintained, scaleX == scaleY)
        final float scaleX = f[Matrix.MSCALE_X];
        final float scaleY = f[Matrix.MSCALE_Y];

        // Get the drawable (could also get the bitmap behind the drawable and getWidth/getHeight)
        final Drawable d = imageView.getDrawable();
        final int origW = d.getIntrinsicWidth();
        final int origH = d.getIntrinsicHeight();

        // Calculate the actual dimensions
        final int widthActual = Math.round(origW * scaleX);
        final int heightActual = Math.round(origH * scaleY);

        // Get image position
        // We assume that the image is centered into ImageView
        int imgViewW = imageView.getWidth();
        int imgViewH = imageView.getHeight();

        int top = (int) (imgViewH - heightActual)/2;
        int left = (int) (imgViewW - widthActual)/2;

        rect.set(left, top, left + widthActual, top + heightActual);

        return rect;
    }
}

