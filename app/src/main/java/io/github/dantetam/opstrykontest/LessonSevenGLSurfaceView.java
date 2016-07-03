package io.github.dantetam.opstrykontest;

import android.app.Activity;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import io.github.dantetam.world.Tile;

public class LessonSevenGLSurfaceView extends GLSurfaceView
{
    private LessonSevenActivity mActivity;
	private LessonSevenRenderer mRenderer;
	
	// Offsets for touch events	 
    private float mPreviousX;
    private float mPreviousY;
    
    private float mDensity;

    private MousePicker mousePicker;

    //private Tile selectedTile = null;
        	
	public LessonSevenGLSurfaceView(Context context)
	{
		super(context);
	}
	
	public LessonSevenGLSurfaceView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

    public void init(LessonSevenActivity activity, MousePicker mousePicker) {
        mActivity = activity;
        this.mousePicker = mousePicker;
    }

	@Override
	public boolean onTouchEvent(MotionEvent event) 
	{
		if (event != null)
		{			
			float x = event.getX();
			float y = event.getY();
			
			if (event.getAction() == MotionEvent.ACTION_MOVE)
			{
				if (mRenderer != null)
				{
					float deltaX = (x - mPreviousX) / mDensity / 2f;
					float deltaY = (y - mPreviousY) / mDensity / 2f;
					
					//mRenderer.mDeltaX += deltaX;
					//mRenderer.mDeltaY += deltaY;
					mRenderer.camera.moveShift(-deltaX/10, 0, -deltaY/10);
					mRenderer.camera.pointShift(-deltaX/10, 0, -deltaY/10);

                    if (mousePicker != null) {
                        mousePicker.update(x, y);

                        if (mousePicker.selectedNeedsUpdating) {
                            //mousePicker.selectedNeedsUpdating = false;

                            boolean selectedTileExists = mousePicker.getSelectedTile() != null;
                            boolean selectedEntityExists = mousePicker.getSelectedEntity() != null;

                            mActivity.findViewById(R.id.build_menu).setVisibility(selectedTileExists ? View.VISIBLE : View.INVISIBLE);

                            Button unitMenu = (Button) mActivity.findViewById(R.id.unit_menu);
                            unitMenu.setVisibility(
                                    selectedTileExists || selectedEntityExists ? View.VISIBLE : View.INVISIBLE
                            );
                            if (selectedTileExists) {
                                unitMenu.setText("Units (" + mousePicker.getSelectedTile().occupants.size() + ")");
                            }
                            else if (selectedEntityExists) {
                                unitMenu.setText("Units (" + mousePicker.getSelectedEntity().location().occupants.size() + ")");
                            }

                        }
                        /*Vector3f v = mousePicker.rayCastHit;
                        mousePicker.getTileClickedOn();*/
                    }
				}
			}	
			
			mPreviousX = x;
			mPreviousY = y;
			
			return true;
		}
		else
		{
			return super.onTouchEvent(event);
		}		
	}

    // Hides superclass method.
	public void setRenderer(LessonSevenRenderer renderer, float density) 
	{
		mRenderer = renderer;
		mDensity = density;
		super.setRenderer(renderer);
	}
}
