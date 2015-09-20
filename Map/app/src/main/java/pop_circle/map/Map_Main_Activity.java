package pop_circle.map;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;

public class Map_Main_Activity extends Activity implements View.OnTouchListener {

    AutoCompleteTextView autoCompleteTextView;
    String[] Building_names;
    private static final String TAG = "Touch";
    @SuppressWarnings("unused")
    private static final float MIN_ZOOM = 1f,MAX_ZOOM = 1f;

    // These matrices will be used to scale points of the image
    Matrix matrix = new Matrix();
    Matrix savedMatrix = new Matrix();

    // The 3 states (events) which the user is trying to perform
    static final int NONE = 0;
    static final int DRAG = 1;
    static final int ZOOM = 2;
    int mode = NONE;

    // these PointF objects are used to record the point(s) the user is touching
    PointF start = new PointF();
    PointF mid = new PointF();
    ImageView view2;
    double oldDist = 1f;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map__main_);
        autoCompleteTextView = (AutoCompleteTextView)  findViewById(R.id.search);
        Building_names = getResources().getStringArray(R.array.building_names);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,Building_names);
        autoCompleteTextView.setAdapter(adapter);
        ImageView view = (ImageView) findViewById(R.id.imageViewMap);
        view2 = (ImageView) findViewById(R.id.imageViewColourMap);
        view.setOnTouchListener(this); // allows map to move around and zoom
    }

    public Map_Main_Activity() {
        super();
    }
///////////////////////////////////////////////////////////////////////////////Attempts to register map taps///////////////////////////////////////////////////////////////////
      /*  public class D3minmaxActivity extends Activity {
        @Override
        public boolean onTouchEvent(MotionEvent event) {
// We only care about the ACTION_UP event
            if (event.getAction() != MotionEvent.ACTION_UP) {
                return super.onTouchEvent(event);
            }
            //HashMap<Color, Integer> colorMap = new HashMap<Color, Integer>()
// Get the colour of the clicked coordinates
// And yes, I spell it coloUr.
            int x = (int) event.getX();
            int y = (int) event.getY();
            int touchColour = getHitboxColour(x, y);
            StringBuilder sb = new StringBuilder();
            sb.append("ARGB(");
            sb.append(Color.alpha(touchColour));
            sb.append(",");
            sb.append(Color.red(touchColour));
            sb.append(",");
            sb.append(Color.green(touchColour));
            sb.append(",");
            sb.append(Color.blue(touchColour));
            sb.append(")");
            Log.i("LOOK HERE: ","______________________HHHHHHEREREREREE IS THE COLOOUUURRRRRR________________________________________________");
            Log.e("Clicked", sb.toString());
// We do this because the pixel colour returned isn't an exact match due to scaling + cache quality
                *//*
                for (Integer col : colorMap.keySet()) {
                    if (closeMatch(col, touchColour)) {
                        Log.e("SuCESS!", colorMap.get(col).toString());
                        Intent data = new Intent(this, ModifiersActivity.class);
                        data.putExtra("slot", colorMap.get(col));
                        startActivity(data);
                        return true;
                    }
                }*//*


// No close matches found
            Log.e("clicked", "nothing");
            return false;
        }
        *//*
         45.
         * This is where the magic happens.
         46.
         * @return Color The colour of the clicked position.
        47.
        *//*

        public int getHitboxColour(int x, int y) {
            ImageView iv = (ImageView) findViewById(R.id.imageViewColourMap);
            Bitmap bmpHotspots;
            int pixel;
// Fix any offsets by the positioning of screen elements such as Activity titlebar.
// This part was causing me issues when I was testing out Bill Lahti's code.
            int[] location = new int[2];
            iv.getLocationOnScreen(location);
            x -= location[0];
            y -= location[1];
// Prevent crashes, return background noise
            if ((x < 0) || (y < 0)) {
                return Color.WHITE;
            }
// Draw the scaled bitmap into memory
            iv.setDrawingCacheEnabled(true);
            bmpHotspots = Bitmap.createBitmap(iv.getDrawingCache());
            iv.setDrawingCacheEnabled(false);
            pixel = bmpHotspots.getPixel(x, y);
            bmpHotspots.recycle();
            return pixel;
        }

        public boolean closeMatch(int color1, int color2) {
            int tolerance = 25;
            if ((int) Math.abs (Color.red (color1) - Color.red (color2)) > tolerance) {
                return false;
            }
            if ((int) Math.abs (Color.green (color1) - Color.green (color2)) > tolerance) {
                return false;
            }
            if ((int) Math.abs (Color.blue (color1) - Color.blue (color2)) > tolerance) {
                return false;
            }
            return true;
        }
    }*/
////////////////////////////////////////////////////////////////////////End of map tapping////////////////////////////////////////////////////////////////////////////////////
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_map__main_, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
//////////////////////////////////////////////////////////////// code to do zooming and moving of maps here:////////////////////////////////////////////////////////////////
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        ImageView view = (ImageView) v;

        view.setScaleType(ImageView.ScaleType.MATRIX);
        double scale;

        dumpEvent(event);

        switch (event.getAction() & MotionEvent.ACTION_MASK)
        {
            case MotionEvent.ACTION_DOWN:   // first finger down only
                savedMatrix.set(matrix);
                start.set(event.getX(), event.getY());
                Log.d(TAG, "mode=DRAG"); // write to LogCat
                mode = DRAG;
                break;

            case MotionEvent.ACTION_UP: // first finger lifted

            case MotionEvent.ACTION_POINTER_UP: // second finger lifted

                mode = NONE;
                Log.d(TAG, "mode=NONE");
                break;

            case MotionEvent.ACTION_POINTER_DOWN: // first and second finger down

                oldDist = spacing(event);
                Log.d(TAG, "oldDist=" + oldDist);
                if (oldDist > 5f) {
                    savedMatrix.set(matrix);
                    midPoint(mid, event);
                    mode = ZOOM;
                    Log.d(TAG, "mode=ZOOM");
                }
                break;

            case MotionEvent.ACTION_MOVE:

                if (mode == DRAG)
                {
                    matrix.set(savedMatrix);
                    matrix.postTranslate(event.getX() - start.x, event.getY() - start.y); // create the transformation in the matrix  of points
                }
                else if (mode == ZOOM)
                {
                    // pinch zooming
                    double newDist = spacing(event);
                    Log.d(TAG, "newDist=" + newDist);
                    if (newDist > 5f)
                    {
                        matrix.set(savedMatrix);
                        scale = newDist / oldDist; // setting the scaling of the
                        // matrix...if scale > 1 means
                        // zoom in...if scale < 1 means
                        // zoom out
                        matrix.postScale((float)scale,(float)scale, mid.x, mid.y);
                    }
                }
                break;
        }

        view.setImageMatrix(matrix); // display the transformation on screen
        view2.setImageMatrix(matrix);// Lets colour of map follow exact motions for tapping
        return true; // indicate event was handled
    }

    /*
     * --------------------------------------------------------------------------
     * Method: spacing Parameters: MotionEvent Returns: float Description:
     * checks the spacing between the two fingers on touch
     * ----------------------------------------------------
     */

    private double spacing(MotionEvent event)
    {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return Math.sqrt(x * x + y * y);//.sqrt(x * x + y * y);
    }

    /*
     * --------------------------------------------------------------------------
     * Method: midPoint Parameters: PointF object, MotionEvent Returns: void
     * Description: calculates the midpoint between the two fingers
     * ------------------------------------------------------------
     */

    private void midPoint(PointF point, MotionEvent event)
    {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

    /** Show an event in the LogCat view, for debugging */
    private void dumpEvent(MotionEvent event)
    {
        String names[] = { "DOWN", "UP", "MOVE", "CANCEL", "OUTSIDE","POINTER_DOWN", "POINTER_UP", "7?", "8?", "9?" };
        StringBuilder sb = new StringBuilder();
        int action = event.getAction();
        int actionCode = action & MotionEvent.ACTION_MASK;
        sb.append("event ACTION_").append(names[actionCode]);

        if (actionCode == MotionEvent.ACTION_POINTER_DOWN || actionCode == MotionEvent.ACTION_POINTER_UP)
        {
            sb.append("(pid ").append(action >> MotionEvent.ACTION_POINTER_ID_SHIFT);
            sb.append(")");
        }

        sb.append("[");
        for (int i = 0; i < event.getPointerCount(); i++)
        {
            sb.append("#").append(i);
            sb.append("(pid ").append(event.getPointerId(i));
            sb.append(")=").append((int) event.getX(i));
            sb.append(",").append((int) event.getY(i));
            if (i + 1 < event.getPointerCount())
                sb.append(";");
        }

        sb.append("]");
        Log.d("Touch Events ---------", sb.toString());
    }
    ///////////////////////////////////////////////////////////////////////////End of zooming etc//////////////////////////////////////////////////////////////////////////////
}
