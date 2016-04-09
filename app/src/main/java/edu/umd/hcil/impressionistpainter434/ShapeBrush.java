package edu.umd.hcil.impressionistpainter434;

import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;

/**
 * Created by Ryan Zimpo on 4/8/16.
 */
public class ShapeBrush {
    private float _touchX = 0;
    private float _touchY = 0;
    private final int _width;
    private Path _path = new Path();

    /* create a polygon-shaped brush depending on the enumerated brushtype */
    public ShapeBrush(BrushType type, float x, float y, int width) {
        _touchX = x;
        _touchY = y;
        _width = width;

        switch(type) {
            case Triangle:
                buildTriangle();
                break;
            case Hexagon:
                buildHexagon();
                break;
        }
    }

    /* builds a triangle path */
    private void buildTriangle() {
        // initialize all the triangle's points
        PaintPoint topPoint = new PaintPoint(_touchX, _touchY - _width);
        PaintPoint leftPoint = new PaintPoint(_touchX - _width, _touchY);
        PaintPoint rightPoint = new PaintPoint(_touchX + _width, _touchY);

        // start at top of triangle and connect to left and right
        _path.moveTo(topPoint.getX(), topPoint.getY());
        _path.lineTo(leftPoint.getX(), leftPoint.getY());
        _path.lineTo(rightPoint.getX(), rightPoint.getY());

        // connect left point to right point
        _path.moveTo(leftPoint.getX(), leftPoint.getY());
        _path.lineTo(rightPoint.getX(), rightPoint.getY());
    }

    /* builds a hexagonal path */
    private void buildHexagon() {
        // initialize hexagon's points
        PaintPoint topPoint = new PaintPoint(_touchX, _touchY - _width);
        PaintPoint topLeft = new PaintPoint(_touchX - _width, _touchY - _width/2);
        PaintPoint topRight = new PaintPoint(_touchX + _width, _touchY - _width/2);
        PaintPoint bottomPoint = new PaintPoint(_touchX, _touchY + _width);
        PaintPoint bottomLeft = new PaintPoint(_touchX - _width, _touchY + _width/2);
        PaintPoint bottomRight = new PaintPoint(_touchX + _width, _touchY + _width/2);

        // start at top of hexagon
        _path.moveTo(topPoint.getX(), topPoint.getY());
        _path.lineTo(topLeft.getX(), topLeft.getY());
        _path.lineTo(topRight.getX(), topRight.getY());

        // start at bottom of hexagon
        _path.moveTo(bottomPoint.getX(), bottomPoint.getY());
        _path.lineTo(bottomLeft.getX(), bottomLeft.getY());
        _path.lineTo(bottomRight.getX(), bottomRight.getY());

        // start at bottom left
        _path.moveTo(bottomLeft.getX(), bottomLeft.getY());
        _path.lineTo(topLeft.getX(), topLeft.getY());
        _path.lineTo(bottomRight.getX(), bottomRight.getY());

        //start at top right
        _path.moveTo(topRight.getX(), topRight.getY());
        _path.lineTo(bottomRight.getX(), bottomRight.getY());
        _path.lineTo(topLeft.getX(), topLeft.getY());
    }

    /* return the star-patterned path */
    public Path getPath() {
        return _path;
    }


}
