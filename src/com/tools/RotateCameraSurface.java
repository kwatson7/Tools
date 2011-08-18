package com.tools;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.hardware.SensorManager;
import android.view.OrientationEventListener;


public class RotateCameraSurface {

	// orientation variables
	private OrientationEventListener mOrientationEventListener;
	private int mOrientation =  -1;
	private static final int ORIENTATION_PORTRAIT_NORMAL =  1;
	private static final int ORIENTATION_PORTRAIT_INVERTED =  2;
	private static final int ORIENTATION_LANDSCAPE_NORMAL =  3;
	private static final int ORIENTATION_LANDSCAPE_INVERTED =  4;

	private Activity mAct;
	private Camera mCamera = null;
	private Boolean mChangeParameters;

	/** Class to keep camera surface from rotating and also to keep track of 
	 * orientation of camera so the picture is stored correctly.
	 * <p>
	 * You must call:<p>
	 *      (1)this class's onResume in the calling activity's onResume
	 *      (2)this class's onPause in the calling activity's onPause
	 *      (3)this class's onCreate in the calling activity's onCreate,
	 *      (4)this class's updateCam when the calling activity's camera is updated
	 * @param activity
	 * @param cam, pass null if not activated yet
	 * @param changeParameters, Boolean to change the parameters of the rotation in
	 * the camera settings. This should be true if you want the camera drivers to rotate the 
	 * image for you, or use getRotation() manually later to do it yourself
	 */
	public RotateCameraSurface(Activity activity, Camera cam, Boolean changeParameters){
		mAct = activity;
		mCamera = cam;
		this.mChangeParameters = changeParameters;

	}

	/** call this in the calling activity's onResume. MUST be done */
	public void onResume() {

		if (mOrientationEventListener == null) {            
			mOrientationEventListener = new OrientationEventListener(mAct, SensorManager.SENSOR_DELAY_NORMAL) {

				@Override
				public void onOrientationChanged(int orientation) {

					// determine our orientation based on sensor response
					int lastOrientation = mOrientation;

					if (orientation >= 315 || orientation < 45) {
						if (mOrientation != ORIENTATION_PORTRAIT_NORMAL) {                          
							mOrientation = ORIENTATION_PORTRAIT_NORMAL;
						}
					}
					else if (orientation < 315 && orientation >= 225) {
						if (mOrientation != ORIENTATION_LANDSCAPE_NORMAL) {
							mOrientation = ORIENTATION_LANDSCAPE_NORMAL;
						}                       
					}
					else if (orientation < 225 && orientation >= 135) {
						if (mOrientation != ORIENTATION_PORTRAIT_INVERTED) {
							mOrientation = ORIENTATION_PORTRAIT_INVERTED;
						}                       
					}
					else { // orientation <135 && orientation > 45
						if (mOrientation != ORIENTATION_LANDSCAPE_INVERTED) {
							mOrientation = ORIENTATION_LANDSCAPE_INVERTED;
						}                       
					}   

					if (lastOrientation != mOrientation) {
						changeRotation(mOrientation, lastOrientation);
					}
				}
			};
		}
		if (mOrientationEventListener.canDetectOrientation()) {
			mOrientationEventListener.enable();
		}
	}

	/** call this in the calling activity's onPause. MUST be done */
	public void onPause() {
		mOrientationEventListener.disable();
	}

	/**
	 * Performs required action to accommodate new orientation
	 * @param orientation
	 * @param lastOrientation
	 */
	private void changeRotation(int orientation, int lastOrientation) {
		if (mChangeParameters == null || !mChangeParameters)
			return;
		if (mCamera == null)
			return;
		Camera.Parameters parameters = mCamera.getParameters();
		switch (orientation) {
		case ORIENTATION_PORTRAIT_NORMAL:
			parameters.setRotation(90);
			break;
		case ORIENTATION_LANDSCAPE_NORMAL:
			parameters.setRotation(0);
			break;
		case ORIENTATION_PORTRAIT_INVERTED:
			parameters.setRotation(270);
			break;
		case ORIENTATION_LANDSCAPE_INVERTED:
			parameters.setRotation(180);
			break;
		}

		mCamera.setParameters(parameters);
	}

	/** call this in the calling activity's onCreate. MUST be done */
	public void onCreate(){

		// force portrait layout
		//mAct.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		mAct.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR | ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
	}

	/** call this function to get the camera rotation, usually at the time of taking the picture
	 * Return value will be 0, 90, 180, or 270 */
	public int getRotation(){
		int rotation = 0;
		switch (mOrientation) {
		case ORIENTATION_PORTRAIT_NORMAL:
			rotation = 90;
			break;
		case ORIENTATION_LANDSCAPE_NORMAL:
			rotation = 0;
			break;
		case ORIENTATION_PORTRAIT_INVERTED:
			rotation = 270;
			break;
		case ORIENTATION_LANDSCAPE_INVERTED:
			rotation = 180;
			break;
		}
		
		return rotation;
	}

	/** call this in the calling activity, when camera is updated. MUST be done */
	public void updateCam(Camera newCam){
		mCamera = newCam;
	}
}
