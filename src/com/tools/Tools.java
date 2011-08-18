/**
 * 
 */
package com.tools;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

/**
 * @author Kyle Watson
 *
 */
public class Tools {

	// static variables for use in methods
	/** SMS field to be inserted as a received message */
	public static final int MESSAGE_TYPE_INBOX = 1; 
	/** SMS field to be inserted as a sent message */
	public static final int MESSAGE_TYPE_SENT = 2; 
	
	
	/** Take input of original Size object that must fit within fitSize 
	 * and output new size that preserves aspect ratio with no cropping.*
	 * @param originalSize WidthHeight object of original size object
	 * @param fitSize WidthHeight object that the new object must fit into
	 * @return a WidthHeight object that is the new size
	 */
	public static WidthHeight fitNoCrop(WidthHeight originalSize, WidthHeight fitSize){

		WidthHeight result = null;
		// width hits edge first
		if (originalSize.getAspectRatio() >= fitSize.getAspectRatio()){

			result = new WidthHeight(fitSize.width, Math.round(fitSize.width/originalSize.getAspectRatio()));

			// height hits edge first	
		}else{
			result = new WidthHeight(Math.round(fitSize.height*originalSize.getAspectRatio()), fitSize.height);
		}

		return result;
	}

	/** Take input of original Size object that must fit within fitSize 
	 * and output new size that preserves aspect ratio butt can make 
	 * image larger that fitSize that then must be cropped.*
	 * @param originalSize WidthHeight object of original size object
	 * @param fitSize WidthHeight object that the new object must fit into
	 * @return a WidthHeight object that is the new size
	 */
	public static WidthHeight fitCrop(WidthHeight originalSize, WidthHeight fitSize){

		WidthHeight result = null;
		// height hits edge first
		if (originalSize.getAspectRatio() < fitSize.getAspectRatio()){

			result = new WidthHeight(fitSize.width, Math.round(fitSize.width/originalSize.getAspectRatio()));

			// height hits edge first	
		}else{
			result = new WidthHeight(Math.round(fitSize.height*originalSize.getAspectRatio()), fitSize.height);
		}

		return result;
	}

	/** attempts to save byte data from camera to the next default location on the SDcard. 
	 * Does not throw any exceptions, but returns success and any exceptions that were
	 * thrown as strings. Will return filename saved if successful.
	 * @author Kyle Watson
	 * @param ctx Context where various android data is pulled from, usually getApplicationContext
	 * @param data Byte array of data to be stored
	 * @param caption Caption to be stored for picture file
	 * @param displayToast Boolean to display toast message
	 * @param fileNameInput write data to this filename. If null, then writes to next available file on external storage*/
	public static SuccessReason saveByteDataToFile(Context ctx, 
			byte[] data, 
			String caption, 
			Boolean displayToast, 
			String fileNameInput){

		//TODO: save correct gps data

		// initialize result and fileName
		SuccessReason result = new SuccessReason(true);
		String fileName = "";

		// try catch wrapper over entire class
		try{
			// create values to store in file, just type and date
			ContentValues values = new android.content.ContentValues(2);
			values.put(MediaColumns.DATE_ADDED, System.currentTimeMillis()); 
			values.put(MediaColumns.MIME_TYPE, "image/jpeg");

			// store into database
			Uri uriTarget = null;
			if (fileNameInput == null){
				uriTarget = ctx.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

				// grab filename
				fileName = getFileNameUri(ctx, uriTarget);
			}else
				fileName = fileNameInput;

			// write file
			FileOutputStream imageFileOS = null;
			try {
				if (fileNameInput==null)
					imageFileOS = (FileOutputStream) ctx.getContentResolver().openOutputStream(uriTarget);
				else{
					try{
						imageFileOS = ctx.openFileOutput(fileNameInput, Context.MODE_WORLD_WRITEABLE);
					}catch (Exception e) {
						// likely had a file separator, so just write directly to that
						imageFileOS = new FileOutputStream(fileNameInput);
					}
				}
				imageFileOS.write(data);
				imageFileOS.flush();
				imageFileOS.close();

				// display toast
				if (displayToast)
					Toast.makeText(ctx,
							"Image saved: " + fileName,
							Toast.LENGTH_LONG).show();

				// error catch	
			} catch (FileNotFoundException e) {
				result = new SuccessReason(false, result.getReason()+" "+e.getLocalizedMessage());
			} catch (IOException e) {
				result = new SuccessReason(false, result.getReason()+" "+e.getLocalizedMessage());
			}

			// add gps data

			/*
			try{
				ExifInterface EI = new ExifInterface(fileName);
				EI.setAttribute(ExifInterface.TAG_ORIENTATION, Integer.toString(ExifInterface.ORIENTATION_ROTATE_90));
				//EI.setAttribute(ExifInterface.TAG_GPS_LATITUDE, Tools.convertAngletoString(15.42));
				//EI.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, "12/1,2/1,300/100");
				//EI.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, "N");
				//EI.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, "E");
				EI.saveAttributes();
			}catch(Exception e){
				result = new SuccessReason(false, result.getReason()+" "+e.toString());
			}
			*/

			MediaScannerConnection.scanFile(ctx, new String[] {fileName}, new String[] {"image/jpeg"}, null);
		}catch(Exception e){
			result = new SuccessReason(false, result.getReason()+" "+e.toString());
		}

		// no exceptions then success
		if (result.getReason().length()==0)
			result = new SuccessReason(true, fileName);

		/*
		try {
			ExifInterface exif = new ExifInterface(fileName);
			String a = exif.getAttribute(ExifInterface.TAG_DATETIME);
			int b = exif.getAttributeInt(ExifInterface.TAG_FLASH, -87);
			int kyle = 6;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 */


		return result;
	}

	public static String getFileNameUri(Context ctx, Uri uri){

		String[] projection = { MediaStore.Images.ImageColumns.DATA,
				MediaStore.Images.ImageColumns.DISPLAY_NAME}; 

		String result = null;
		Cursor cur = ctx.getContentResolver().query(uri, projection, null, null, null); 
		if (cur!=null && cur.moveToFirst()) { 
			result = cur.getString(0); 
		} 
		if (cur != null)
			cur.close();

		return result;
	} 

	public static String convertAngletoString(double angle){

		int deg;
		int min;
		int sec;

		deg = (int) angle;
		min = (int) ((angle - deg)*60);
		sec = (int) ((angle - (deg + min/60.0))*3600);
		return deg+"/1,"+min+"/1,"+sec*100+"/100";
	}

	/** Grab phones full number, see getMyStrippedPhoneNumber for removing +1*/
	public static String getMyPhoneNumber(Activity act){  
		TelephonyManager mTelephonyMgr;  
		mTelephonyMgr = (TelephonyManager)  
		act.getSystemService(Context.TELEPHONY_SERVICE);   
		return mTelephonyMgr.getLine1Number();  
	}  

	/** Grab phone number with leading +1 removed... if there */
	public static String getMyStrippedPhoneNumber(Activity act){  
		String s = getMyPhoneNumber(act); 

		if (s != null){
			// strip off 1 and +
			if (s.length()==0)
				return s;
			if (s.charAt(0) == '+')
				s = s.substring(1);
			if (s.length()==0)
				return s;
			if (s.charAt(0) == '1')
				s = s.substring(1);
		}

		return s; 
	}

	/** Eliminate all but numerics from a string*/
	public static String keepOnlyNumerics(String input){
		return input.replaceAll("[^0-9]", "");
	}

	/** Format phone number to 123-456-7890.
	 * If it cannot be formatted this way, then simply
	 * the incoming string is returned */
	public static String formatPhoneNumber(String input){

		String out = input;

		if (out.length() == 0)
			return out;
		// strip off 1 and +
		if (out.charAt(0) == '+')
			out = out.substring(1);
		if (out.length() == 0)
			return out;
		if (out.charAt(0) == '1')
			out = out.substring(1);

		out = keepOnlyNumerics(out);
		if (out.length() != 10)
			out = input;
		else
			out = out.substring(0, 3)+"-"+out.substring(3, 6)+"-"+out.substring(6);

		return out;
	}

	/** get the string indicated by the tag in the given xml string
	 * Return null if not found*/
	public static String getXmlValueAtTag(String xml, String tag){

		// null inputs
		if (xml==null || tag==null)
			return null;

		// look for tag
		int begOfFirstTag = xml.indexOf("<"+tag);
		if (begOfFirstTag==-1)
			return null;

		// find end of tag
		int endOfFirstTag = xml.indexOf(">", begOfFirstTag+tag.length()+1);
		if (endOfFirstTag==-1)
			return null;

		// look for beginning of end tag
		int begOfSecondTag = xml.indexOf("</"+tag, endOfFirstTag+1);
		if (begOfSecondTag==-1)
			return null;

		// find end of end tag
		int endOfSecondTag = xml.indexOf(">", begOfSecondTag+tag.length()+2);
		if (endOfSecondTag==-1)
			return null;

		// all tags were found, so simply look between tags
		return xml.substring(endOfFirstTag+1, begOfSecondTag);

	}

	/**
	 * Hide the keyboard.
	 * @param ctx The context to perform this action
	 * @param view Any view in the context that can request the window to be hidden
	 */
	public static void hideKeyboard(Context ctx, View view){
		InputMethodManager imm = (InputMethodManager)ctx.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
	}

	/**
	 * Show the keyboard.
	 * @param ctx The context to perform this action
	 * @param view Any view in the context that can request the window to be shown
	 */
	public static void showKeyboard(Context ctx, View view){
		InputMethodManager imm = (InputMethodManager)ctx.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.showSoftInput(view, 0);
	}

	/** Resize a byte array keeping aspect ratio. Will either
	 * crop the data, fill extra data with black bars, or resize the image 
	 * to as close to newWidthHeight, but not guaranteed.
	 * @author Kyle Watson
	 * @param input Byte array input data
	 * @param cropFlag "crop", "blackBars", "resizeLarge", "resizeSmall" options for what 
	 * to do with image that doesn't fit new size. 
	 * @param ctx Context context, usually getApplicationContext
	 * @param newWidthHeight New desired width and height
	 * @param orientationAngle Float for the orientation of the byte array. If the data
	 * is already stored in the byte array, then pass null and the value will be extracted.
	 * @throws IllegalArgumentException if cropFlag is not the right input type
	 */
	public static byte[] resizeByteArray(byte[] input, 
			WidthHeight newWidthHeight, 
			String cropFlag, 
			Context ctx, 
			Float orientationAngle) 
	throws IllegalArgumentException{

		// grab orientation angle from exif data
		if (orientationAngle == null)
			orientationAngle = getExifOrientationAngle(input, ctx);

		// create bitmap from data
		BitmapFactory.Options opt = new BitmapFactory.Options();
		opt.inDither = true;
		Bitmap bitmapOrg = BitmapFactory.decodeByteArray(input, 0, input.length, opt);

		// grab width and height from bitmap
		int width = bitmapOrg.getWidth();
		int height = bitmapOrg.getHeight();

		// check rotation to see if we need to switch width and height
		if ((int) Math.round(orientationAngle) % (int) 180 == 90){
			int tmp = width;
			width = height;
			height = tmp;
		}

		// check if no resizing required
		if (width == newWidthHeight.width && height == newWidthHeight.height && 
				orientationAngle == 0)
			return input;

		// find new width and height for temporary bitmap object
		WidthHeight tmpWidthHeight = null;
		if (cropFlag.compareToIgnoreCase("crop")==0 ||
				cropFlag.compareToIgnoreCase("resizeLarge")==0)
			tmpWidthHeight = com.tools.Tools.fitCrop
			(new WidthHeight(width, height), newWidthHeight);
		else if (cropFlag.compareToIgnoreCase("blackBars")==0 || 
				cropFlag.compareToIgnoreCase("resizeSmall")==0)
			tmpWidthHeight = com.tools.Tools.fitNoCrop
			(new WidthHeight(width, height), newWidthHeight);
		else
			throw new IllegalArgumentException
			("resizeByteArray cropFlag must have \"crop\", \"blackBars\", \"resizeLarge\", or \"resizeSmall\" as an input");

		// grab height and width
		int newWidth = tmpWidthHeight.width;
		int newHeight = tmpWidthHeight.height;

		// calculate the scale
		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;

		// create a matrix for the manipulation
		Matrix matrix = new Matrix();
		// resize the bit map
		matrix.postScale(scaleWidth, scaleHeight);
		// set rotation angle based on exif data
		if (orientationAngle != 0)
			matrix.postRotate(orientationAngle);

		// check rotation to see if we need to switch back to original width and height
		if ((int) Math.round(orientationAngle) % (int) 180 == 90){
			int tmp = width;
			width = height;
			height = tmp;
		}

		// recreate the new Bitmap
		Bitmap tmpResizedBitmap = Bitmap.createBitmap(bitmapOrg, 0, 0,
				width, height, matrix, true);

		Bitmap resizedBitmap = null;

		// if resizeSmall or resizeLarge, then tmpResizedBitmap is the same as final.
		// Also if the desired output is the same as the tmpSize then, also just copy over
		if (cropFlag.compareToIgnoreCase("resizeSmall")==0 ||
				cropFlag.compareToIgnoreCase("resizeLarge")==0 ||
				(tmpResizedBitmap.getWidth() == newWidthHeight.width && 
						tmpResizedBitmap.getHeight() == newWidthHeight.height))
			resizedBitmap = tmpResizedBitmap;

		// crop option, we will grab a subset of the tmpBitmap
		else if (cropFlag.compareToIgnoreCase("crop")==0){
			resizedBitmap = Bitmap.createBitmap
			(newWidthHeight.width, newWidthHeight.height, Bitmap.Config.RGB_565);
			int[] pixels = new int[resizedBitmap.getWidth()*resizedBitmap.getHeight()];
			int x = (int) Math.round((tmpResizedBitmap.getWidth() - resizedBitmap.getWidth())/2.0);
			int y = (int) Math.round((tmpResizedBitmap.getHeight() - resizedBitmap.getHeight())/2.0);
			tmpResizedBitmap.getPixels(pixels, 0, resizedBitmap.getWidth(), x, y, 
					resizedBitmap.getWidth(), resizedBitmap.getHeight());
			resizedBitmap.setPixels(pixels, 0, resizedBitmap.getWidth(), 0, 0, 
					resizedBitmap.getWidth(), resizedBitmap.getHeight());
		}

		// the blackBars option, we create a new bitmap that is larger and fill with tmpBitmap
		else {
			resizedBitmap = Bitmap.createBitmap
			(newWidthHeight.width, newWidthHeight.height, Bitmap.Config.RGB_565);
			int[] pixels = new int[resizedBitmap.getWidth()*resizedBitmap.getHeight()];
			int x = (int) -Math.round((tmpResizedBitmap.getWidth() - resizedBitmap.getWidth())/2.0);
			int y = (int) -Math.round((tmpResizedBitmap.getHeight() - resizedBitmap.getHeight())/2.0);
			tmpResizedBitmap.getPixels(pixels, 0, tmpResizedBitmap.getWidth(), 0, 0, 
					tmpResizedBitmap.getWidth(), tmpResizedBitmap.getHeight());
			resizedBitmap.setPixels(pixels, 0, tmpResizedBitmap.getWidth(), x, y, 
					tmpResizedBitmap.getWidth(), tmpResizedBitmap.getHeight());
		}

		// turn back into byte array
		ByteArrayOutputStream out = new ByteArrayOutputStream(resizedBitmap.getWidth()*resizedBitmap.getHeight());
		resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);   
		byte[] result = out.toByteArray();

		return result;		
	}


	public static ExifInterface readExifDataFromByteArray(byte[] data, Context ctx){

		// write data to file
		SuccessReason tmp = saveByteDataToFile(ctx, data, "", false, null);//, tmpFile);
		if (!tmp.getSuccess())
			return null;
		String tmpFile = tmp.getReason();

		// read exif data from file
		ExifInterface exif;
		try {
			exif = new ExifInterface(tmpFile);
		} catch (IOException e) {
			exif = null;
		}

		// delete file
		new File(tmpFile).delete();

		return exif;
	}

	public static float getExifOrientationAngle(ExifInterface exif){

		// null exif, return 0 angle
		if (exif==null)
			return 0;

		// read orienation
		int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 
				ExifInterface.ORIENTATION_NORMAL);

		// these are the angle corresponding to these orientations
		float angle = 0;
		if (orientation == ExifInterface.ORIENTATION_ROTATE_90)
			angle = 90;
		else if (orientation == ExifInterface.ORIENTATION_ROTATE_180)
			angle = 180;
		else if (orientation == ExifInterface.ORIENTATION_ROTATE_270)
			angle = 270;

		return angle;
	}

	public static float getExifOrientationAngle(byte[] data, Context ctx){

		// read exif from byte array and then read the angle data
		return getExifOrientationAngle(readExifDataFromByteArray(data, ctx));
	}

	public static float getExifOrientationAngle(String file){

		// read exif data
		ExifInterface exif = null;
		try {
			exif = new ExifInterface(file);
		} catch (IOException e) {
		}

		// grab the angle from exif data
		return getExifOrientationAngle(exif);
	}

	/**
	 * Send an sms message. If message is empty, then nothing happens and simply returns
	 * @param ctx The context to use
	 * @param phoneNumber The phone number to send it to
	 * @param message The message
	 */
	public static void sendSMS(Context ctx, String phoneNumber, String message)
	{        
		// break out if empty message
		if (message == null || message.length()==0)
			return;
		
		//TODO: check if sent successfully
		PendingIntent pi = PendingIntent.getActivity(ctx, 0,
				new Intent(ctx, SmsSentClass.class), 0);                
		SmsManager sms = SmsManager.getDefault();
		sms.sendTextMessage(phoneNumber, null, message, null, null);    			
		
		// save text into database
		insertSMSDatabse(ctx, phoneNumber, message, MESSAGE_TYPE_SENT);
	}
	
	/**
	 * Insert a message
	 * @param ctx The context to use to perform this action
	 * @param phoneNumber The phone number attached to the sms
	 * @param message The message of the sms
	 * @param messageType the message is either sent (com.tools.Toos.MESSAGE_TYPE_SENT) 
	 * or received (com.tools.Tools.MESSAGE_TYPE_INBOX)
	 * @return The url of the sms database in which the message was inserted
	 */
	public static Uri insertSMSDatabse(Context ctx,
			String phoneNumber, 
			String message, 
			int messageType){

		// the tags for various setting to go into the database
		//final String PERSON = "person"; 
		final String ADDRESS = "address"; 
		final String DATE = "date"; 
		final String READ = "read"; 
		final String STATUS = "status"; 
		final String TYPE = "type"; 
		final String BODY = "body";  
		
		// default values
		int status = -1;
		int read = 1;
		
		// grab current data
		Date date = new Date();

		ContentValues values = new ContentValues(); 
		values.put(ADDRESS, phoneNumber); 
		values.put(DATE, String.valueOf(date.getTime())); 
		values.put(READ, read); 
		values.put(STATUS, status); 
		values.put(TYPE, messageType); 
		values.put(BODY, message); 
		Uri inserted = null;
		try{
			inserted = ctx.getContentResolver().insert
				(Uri.parse("content://sms//sent"), values);
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return inserted;
	}

	private class SmsSentClass extends Activity {

		@Override
		protected void onCreate(Bundle savedInstanceState){

		}
	}
	
	/** Show a dialog that will only be shown once at startup. 
	 * This inputs in the calling ctx are required:
	 * SharedPreferences mPrefs;
	 * final String welcomeScreenShownPref = "welcomeScreenShown";
	 * 
	 * @param ctx The context where this dialog will be displayed
	 * @param prefs The preferences object that must present in the calling context/activity
	 * @param welcomeScreenShownPref The string where the preference is stored
	 * @param title The title of the box
	 * @param text The text in the body
	 * @param toShow Boolean whether to show or not. Input null to read from preferences
	 */
	public static void showOneTimeDialog(Context ctx,
			SharedPreferences prefs,
			String welcomeScreenShownPref,
			String title,
			String text, 
			Boolean toShow){
		prefs = PreferenceManager.getDefaultSharedPreferences(ctx);

		// second argument is the default to use if the preference can't be found
		if (toShow == null)
			toShow = !prefs.getBoolean(welcomeScreenShownPref, false);

		if (toShow) {
			// here you can launch another activity if you like
			// the code below will display a popup

			new AlertDialog.Builder(ctx).
				setIcon(android.R.drawable.ic_dialog_info).
				setTitle(title).setMessage(Html.fromHtml(text)).
				setPositiveButton("OK", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					}).show();
			if (prefs != null){
				SharedPreferences.Editor editor = prefs.edit();
				editor.putBoolean(welcomeScreenShownPref, true);
				editor.commit(); // Very important to save the preference
			}
		}
	}
	
	/**
	 * Create a new group in the android databse
	 * @param ctx The context to use
	 * @param accountType the account type, for example "com.google", null for phone
	 * @param accountName The account name, for example "user@gmail.com", null for phone
	 * @param groupName The group name, fore example "friends"
	 * @return The group ID is returned
	 */
	public static String makeNewGroup(Context ctx,
			String accountType,
			String accountName,
			String groupName){
		
		// grab the content resolver, put group name, accountType, and accountName into database and create
		ContentResolver cr = ctx.getContentResolver();
		ContentValues groupValues = new ContentValues();
		groupValues.put(ContactsContract.Groups.TITLE, groupName);
		groupValues.put(ContactsContract.Groups.ACCOUNT_NAME, accountName);
		groupValues.put(ContactsContract.Groups.ACCOUNT_TYPE, accountType);
		Uri uri = cr.insert(ContactsContract.Groups.CONTENT_URI, groupValues);
		
		// the group ID
		return uri.getLastPathSegment();
	}
	
	/**
	 * Take in an amount of minutes and convert it to a human readable output.
	 * For example, if minutes=384, then the output will be:
	 * "6 hours and 24 minutes".
	 * <p></p>
	 * The allowable outputs are years (365 days), weeks, days, hours, minutes, seconds
	 * <p></p>
	 * @param minutes
	 * @return
	 */
	public static String convertMinutesToFormattedString(double minutes){
		
		// the output string
		String output = "";
		
		// find how many years
		int years = (int) Math.floor(minutes/(365*60*24));
		if (years > 0){
			output = years + " years, ";
			minutes = minutes - (years*365*60*24);
		}
		
		// how many weeks
		int weeks = (int) Math.floor(minutes/(60*24*7));
		if (weeks > 0){
			output += weeks + " weeks, ";
			minutes = minutes - (weeks*60*24*7);
		}
		
		// how many days
		int days = (int) Math.floor(minutes/(60*24));
		if (days > 0){
			output += days + " days, ";
			minutes = minutes - (days*60*24);
		}
		
		// how many hours
		int hours = (int) Math.floor(minutes/(60));
		if (hours > 0){
			output += hours + " hours, ";
			minutes = minutes - (hours*60);
		}
		
		// how many minutes
		int minutesDisp = (int) Math.floor(minutes);
		if (minutesDisp > 0){
			output += minutesDisp + " minutes, ";
			minutes = minutes - (minutesDisp);
		}
		
		// how many seconds
		float seconds =  (float) (minutes*60.0);
		if (seconds > 0){
			output += seconds + " seconds, ";
		}
		
		// return the string minus the last comma and space
		return output.substring(0, output.length()-2);		
	}
	
	/**
	 * Request a sync for all your accounts. Make sure to include:
	 * <uses-permission android:name="android.permission.READ_SYNC_SETTINGS"/> 
	 * in your manifest
	 * @param ctx
	 */
	public static void requestSync(Context ctx)
	{
		try{
	    AccountManager am = AccountManager.get(ctx);
	    Account[] accounts = am.getAccounts();

	    for (Account account : accounts)
	    {
	        int isSyncable = ContentResolver.getIsSyncable(account, ContactsContract.AUTHORITY);

	        if (isSyncable > 0)
	        {
	            Bundle extras = new Bundle();
	            extras.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
	            ContentResolver.requestSync(account, ContactsContract.AUTHORITY, extras);
	        }
	    }
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}