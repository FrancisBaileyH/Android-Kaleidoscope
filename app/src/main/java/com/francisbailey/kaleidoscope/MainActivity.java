package com.francisbailey.kaleidoscope;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.SeekBar;


public class MainActivity extends AppCompatActivity {


    private ImageView kal;
    private SeekBar positionSeekBar;
    private SeekBar mirrorCountSeekBar;
    private SeekBar rotationSpeedSeekBar;

    private int segments;
    private float angle;
    private Bitmap source_image;
    private Bitmap imageview_bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.kal = (ImageView)findViewById(R.id.image_output);

        segments = 24;
        source_image = BitmapFactory.decodeResource(getResources(), R.drawable.cropped_landscape);
        imageview_bitmap = Bitmap.createBitmap(source_image.getWidth(), source_image.getHeight(), Bitmap.Config.ARGB_8888);

        drawKaleidoscope(310, 12);

        positionSeekBar = (SeekBar)findViewById(R.id.position);
        rotationSpeedSeekBar = (SeekBar)findViewById(R.id.rotationSpeed);
        mirrorCountSeekBar = (SeekBar)findViewById(R.id.mirrorCount);


        mirrorCountSeekBar.setProgress(12);
        positionSeekBar.setProgress(310);

        positionSeekBar.setOnSeekBarChangeListener(positionSeekBarListener);
        rotationSpeedSeekBar.setOnSeekBarChangeListener(rotationSeekBarListener);
        mirrorCountSeekBar.setOnSeekBarChangeListener(mirrorCountSeekBarListener);

    }



    /**
     * First we take a pie-sliced segment of our source image, then we
     * draw it once to the canvas. We rotate the canvas by the width of the
     * segment and draw again. We do this until half of the canvas has a segment drawn
     * to it with a gap in between each segment. Next, we mirror the canvas
     * and draw the same segment into the blank areas. This way we can easily
     * achieve a mirror image foreach segment.
     */
    private Bitmap generateKaleidoscopeBitmap(float start_angle) {

        Canvas canvas = new Canvas(imageview_bitmap);
        canvas.drawColor(Color.BLACK);
        BitmapShader fillShader;

        Path triangle_mask    = new Path();
        RectF r               = new RectF(0, 0, imageview_bitmap.getWidth(), imageview_bitmap.getHeight()); // create new rectangle to match the dimensions of our image

        int centerX = imageview_bitmap.getWidth() / 2;
        int centerY = imageview_bitmap.getHeight() / 2;

        // how much to rotate the canvas by after the image is flipped
        float offset = calculateCanvasSymmetryOffset(start_angle);


        // Create a pie-slice shaped clipping mask
        triangle_mask.moveTo(r.centerX(), r.centerY());
        triangle_mask.arcTo(r, start_angle, angle);
        triangle_mask.close();


        // Fill the triangle masked area with our shader now
        Paint fill = new Paint();
        fill.setColor(0xFFFFFFFF);
        fill.setStyle(Paint.Style.FILL);
        fillShader = new BitmapShader(source_image, Shader.TileMode.MIRROR, Shader.TileMode.MIRROR);
        fill.setShader(fillShader);


        // Rotate the canvas and draw the clipping mask to the canvas
        for (int i = 0; i < this.segments / 2; i++) {

            canvas.drawPath(triangle_mask, fill);
            canvas.rotate(angle * 2, centerX, centerY);
        }


        // mirror the canvas and rotate it once to counter the symmetrical offset
        canvas.scale(-1, 1, centerX, centerY);
        canvas.rotate(offset, centerX, centerY);


        // Rotate the now mirrored canvas and draw the clipping mask to it
        // This is a cheap and easy way of creating mirrored segments
        for (int i = 0; i < this.segments / 2; i++) {

            canvas.drawPath(triangle_mask, fill);
            canvas.rotate(angle * 2, centerX, centerY);
        }
        
        return imageview_bitmap;
    }


    /**
     * When we flip the canvas, the result may not be lined up correctly with
     * where we're drawing our image mask. To resolve this we calculate how much
     * we need to rotate/offset the canvas by. This offset rotation is based on
     * the start angle and the angle of each segment.
     *
     * @param start_angle
     * @return
     */
    private float calculateCanvasSymmetryOffset(float start_angle) {

        // for odd number of mirrors
        // if angle is e.g. 36 degrees, pattern for offset looks like so
        // Start Angle:      0  9 18 27 36
        // Required Offset: 36 18  0 18 36
        if ((this.segments / 2) % 2 == 1) {

            return angle - 2 * start_angle;
        }
        // for even number of mirrors
        else {

            // if angle is e.g. 30 degrees, pattern for offset looks like so:
            // Start Angle:     0 15 30 45 60
            // Required Offset: 0 30  0 30  0
            float percent_of_angle = start_angle / angle;
            float start_angle_converted = angle * (percent_of_angle - (int)percent_of_angle);

            if (start_angle_converted >= (angle / 2)) {
                return (angle * 2) - 2 * start_angle_converted;
            }
            else {
                return -(start_angle_converted * 2);
            }
        }
    }


    /**
     * Draw our generated bitmap to the ImageView and
     * generate new angle from segments and mirror count
     * @param start_angle
     * @param mirrorCount
     */
    public void drawKaleidoscope(float start_angle, int mirrorCount) {

        this.segments = mirrorCount * 2;
        this.angle = 360.0f  / segments;

        Bitmap image = this.generateKaleidoscopeBitmap(start_angle);
        kal.setImageBitmap(image);
    }


    private final SeekBar.OnSeekBarChangeListener positionSeekBarListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            int progress = mirrorCountSeekBar.getProgress();

            if (progress == 0) {
                kal.setImageResource(R.drawable.cropped_landscape);
            }
            else {
                drawKaleidoscope(i, mirrorCountSeekBar.getProgress());
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {     }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {      }
    };


    private final SeekBar.OnSeekBarChangeListener mirrorCountSeekBarListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            if (i < 1) {
                kal.setImageResource(R.drawable.cropped_landscape);
            }
            else {
                drawKaleidoscope(positionSeekBar.getProgress(), i);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {     }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {      }
    };


    private final SeekBar.OnSeekBarChangeListener rotationSeekBarListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {    }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {    }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

            Animation anim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_infinitely);
            int i = seekBar.getProgress();

            if (i == 0) {
                kal.clearAnimation();
            }
            else {
                anim.setDuration((seekBar.getMax() * 5000) / i);
                kal.startAnimation(anim);
            }
        }
    };


}


