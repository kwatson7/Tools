package com.tools;

import java.util.ArrayList;
import java.util.Collections;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.CommonDataKinds.Nickname;
import android.provider.ContactsContract.Data;

/**
 * A helper class with many different static methods for calling database methods to extract user info.
 * @author Kyle
 *
 */
//TODO: finish commenting this class
public class CustomCursors {

	// some variables
	/** account type for google accounts */
	public static final String GOOGLE_ACCOUNT_TYPE = "com.google";
	public static final String YOU_TUBE_DOMAIN = "@youtube.com";
	
	/**
	 * Get a cursor that has all group names and ids
	 * @param act The activity that this cursor call happens within
	 * @return cursor with these fields:
	 * ContactsContract.Groups.TITLE, 
				ContactsContract.Groups._ID,
				ContactsContract.Groups.SYSTEM_ID
	 */
	public static Cursor getGroupNamesAndIdsCursor(Activity act){

		//columns to return
		String[] projection = {ContactsContract.Groups.TITLE, 
				ContactsContract.Groups._ID,
				ContactsContract.Groups.SYSTEM_ID,  
				ContactsContract.Groups.ACCOUNT_NAME,
				ContactsContract.Groups.ACCOUNT_TYPE};

		// sql where clause - used to require to be visible, but this is not good and will block groups t
		// that shouldn't be blocked
		String selection = null;

		// sort order alphabetical
		String sortOrder = ContactsContract.Groups.TITLE + " COLLATE LOCALIZED ASC";

		// perform the cursor call
		Cursor cursor = act.managedQuery(ContactsContract.Groups.CONTENT_URI, 
				projection, selection, null, sortOrder);
		
		// manage the cursor
		act.startManagingCursor(cursor);
		return cursor;
	}
	
	public static Cursor getGroupNamesAndIdsCursor(ContentResolver cr){

		//columns to return
		String[] projection = {ContactsContract.Groups.TITLE, 
				ContactsContract.Groups._ID,
				ContactsContract.Groups.SYSTEM_ID,  
				ContactsContract.Groups.ACCOUNT_NAME,
				ContactsContract.Groups.ACCOUNT_TYPE};

		// sql where clause - used to require to be visible, but this is not good and will block groups t
		// that shouldn't be blocked
		String selection = null;

		// sort order alphabetical
		String sortOrder = ContactsContract.Groups.TITLE + " COLLATE LOCALIZED ASC";

		// perform the cursor call
		Cursor cursor = cr.query(ContactsContract.Groups.CONTENT_URI, 
				projection, selection, null, sortOrder);
		
		return cursor;
	}

	/**
	 * Grab all the account names on the phone and the account types.
	 * @param ctx A context used to implement AccountManager
	 * @return An arrayList of two Strings, with name, and type
	 */
	public static  ArrayList<TwoStrings> getAccountNamesAndType(Context ctx){
		// grab the accounts
		Account[] accounts = AccountManager.get(ctx).getAccounts(); 
		
		// initialize list
		ArrayList<TwoStrings> accountList = new ArrayList<TwoStrings>(accounts.length);
		
		// loop across accounts grouping them together
		 for (Account acc : accounts){
			 accountList.add(new TwoStrings(acc.name, acc.type, "Account names and types"));
		 }
		 
		 return accountList;
	}
	
	public static  ArrayList<String> getGoogleAccountNames(Context ctx){
		
		// grab the accounts
		Account[] accounts = AccountManager.get(ctx).getAccounts(); 
		
		// initialize list
		ArrayList<String> accountList = new ArrayList<String>();
		
		// loop across accounts finding groups that are google and NOT youtube
		 for (Account acc : accounts){
			 if (acc.type.equals(GOOGLE_ACCOUNT_TYPE) 
					 && !acc.name.contains(YOU_TUBE_DOMAIN))
				 accountList.add(acc.name);
		 }
		 
		 return accountList;
	}
	
	/** Get a twoobject element where each element is a listarray. 
	 * One list array is the titles of the groups, and the other is the IDs */
	public static TwoObjects<ArrayList<String>, ArrayList<String>> getGroupNamesAndIdsList0(Activity act){

		// grab cursor
		Cursor cursor = getGroupNamesAndIdsCursor(act);
		int N = cursor.getCount();

		// initialize outputs
		ArrayList<String> titles = new ArrayList<String>(N);
		ArrayList<String> ids = new ArrayList<String>(N);

		// columns to read
		int colTitle = cursor.getColumnIndex(ContactsContract.Groups.TITLE);
		int colId = cursor.getColumnIndex(ContactsContract.Groups._ID);
		int systemId = cursor.getColumnIndex(ContactsContract.Groups.SYSTEM_ID);

		// loop through cursor
		String tmpTitle;
		if (cursor != null && cursor.moveToFirst()){
			do{
				// see if we want systemid name or title
				tmpTitle = cursor.getString(systemId);
				if (tmpTitle == null)
					tmpTitle = cursor.getString(colTitle);

				// don't add duplicate names
				//	if (titles.contains(tmpTitle))
				//		continue;	

				// add title and id to array	
				titles.add(tmpTitle);
				ids.add(cursor.getString(colId));
			}while(cursor.moveToNext());
		}

		if (cursor != null)
			cursor.close();
		
		// sort by title		

		// put array list into output
		return new TwoObjects<ArrayList<String>, ArrayList<String>>(titles, ids);
	}

	/** Get a twoobject element where each element is a listarray. 
	 * One list array is the titles of the groups, and the other is the IDs */
	public static ArrayList<TwoStrings> getGroupNamesAndIdsList(Activity act){

		// grab cursor
		Cursor cursor = getGroupNamesAndIdsCursor(act);
		int N = cursor.getCount();

		// initialize outputs
		ArrayList<TwoStrings> titlesAndIds = new ArrayList<TwoStrings>(N);

		// columns to read
		int colTitle = cursor.getColumnIndex(ContactsContract.Groups.TITLE);
		int colId = cursor.getColumnIndex(ContactsContract.Groups._ID);
		int systemId = cursor.getColumnIndex(ContactsContract.Groups.SYSTEM_ID);

		// loop through cursor
		String tmpTitle;
		if (cursor != null && cursor.moveToFirst()){
			do{
				// object that holds id and title
				TwoStrings input = new TwoStrings("", "0");

				// see if we want systemid name or title
				tmpTitle = cursor.getString(systemId);
				if (tmpTitle == null)
					tmpTitle = cursor.getString(colTitle);

				// put title into input object
				input.mObject1 = tmpTitle;
				
				//if still null, then break out
				if (tmpTitle == null)
					continue;

				// don't add duplicate names
				//	if (titlesAndIds.contains(input))
				//		continue;	

				// put the id into input object
				input.mObject2 = cursor.getString(colId);

				// check if this group is empty, if so don't return it
				Cursor cursorEmpty = act.managedQuery(ContactsContract.Groups.CONTENT_SUMMARY_URI, 
						new String[]{ContactsContract.Groups.SUMMARY_COUNT}, 
						ContactsContract.Groups._ID + " = "+input.mObject2, 
						null, null);
				act.startManagingCursor(cursorEmpty);
				if (cursorEmpty == null || !cursorEmpty.moveToFirst()){
					if (cursorEmpty != null)
						cursorEmpty.close();
					continue;
				}
					
				if (cursorEmpty.getInt(cursorEmpty.getColumnIndex(ContactsContract.Groups.SUMMARY_COUNT)) < 1){
					cursorEmpty.close();
					continue;			
				}

				// add title and id to array	
				cursorEmpty.close();
				titlesAndIds.add(input);
			}while(cursor.moveToNext());
		}

		// sort by title	
		Collections.sort(titlesAndIds);
		
		if (cursor != null)
			cursor.close();

		// put array list into output
		return titlesAndIds;
	}
	
	public static ArrayList<TwoStrings> getGroupNamesAndIdsList(ContentResolver cr){

		// grab cursor
		Cursor cursor = getGroupNamesAndIdsCursor(cr);
		int N = cursor.getCount();

		// initialize outputs
		ArrayList<TwoStrings> titlesAndIds = new ArrayList<TwoStrings>(N);

		// columns to read
		int colTitle = cursor.getColumnIndex(ContactsContract.Groups.TITLE);
		int colId = cursor.getColumnIndex(ContactsContract.Groups._ID);
		int systemId = cursor.getColumnIndex(ContactsContract.Groups.SYSTEM_ID);

		// loop through cursor
		String tmpTitle;
		if (cursor != null && cursor.moveToFirst()){
			do{
				// object that holds id and title
				TwoStrings input = new TwoStrings("", "0");

				// see if we want systemid name or title
				tmpTitle = cursor.getString(systemId);
				if (tmpTitle == null)
					tmpTitle = cursor.getString(colTitle);

				// put title into input object
				input.mObject1 = tmpTitle;
				
				//if still null, then break out
				if (tmpTitle == null)
					continue;

				// don't add duplicate names
				//	if (titlesAndIds.contains(input))
				//		continue;	

				// put the id into input object
				input.mObject2 = cursor.getString(colId);

				// check if this group is empty, if so don't return it
				Cursor cursorEmpty = cr.query(ContactsContract.Groups.CONTENT_SUMMARY_URI, 
						new String[]{ContactsContract.Groups.SUMMARY_COUNT}, 
						ContactsContract.Groups._ID + " = "+input.mObject2, 
						null, null);

				if (cursorEmpty == null || !cursorEmpty.moveToFirst()){
					if (cursorEmpty != null)
						cursorEmpty.close();
					continue;
				}
					
				if (cursorEmpty.getInt(cursorEmpty.getColumnIndex(ContactsContract.Groups.SUMMARY_COUNT)) < 1){
					cursorEmpty.close();
					continue;			
				}

				// add title and id to array	
				titlesAndIds.add(input);
				cursorEmpty.close();
			}while(cursor.moveToNext());
		}

		// sort by title	
		Collections.sort(titlesAndIds);
		
		if (cursor != null)
			cursor.close();

		// put array list into output
		return titlesAndIds;
	}
	
	public static Cursor getGroupProjection(Activity act, String[] projection){

		// sql where clause
		String selection = ContactsContract.Groups.GROUP_VISIBLE + " = '1'";

		// sort order alphabetical
		String sortOrder = ContactsContract.Groups.TITLE + " COLLATE LOCALIZED ASC";

		Cursor cursor = act.managedQuery(ContactsContract.Groups.CONTENT_URI, 
				projection, selection, null, sortOrder);
		act.startManagingCursor(cursor);

		return cursor;
	}

	public static Cursor getPhoneProjection(Activity act, String[] projection, String srchName){

		// replace any "'" in srchName with "''" to search properly in sql
		srchName = srchName.replace("'", "''");
		
		// create string for searching
		String selection ="(UPPER(" + CommonDataKinds.Phone.DISPLAY_NAME + ") LIKE UPPER('" + srchName + "%') OR UPPER(" 
		+ CommonDataKinds.Phone.DISPLAY_NAME + ") LIKE UPPER('% " + srchName + "%'))";
		 //ContactsContract.Contacts.IN_VISIBLE_GROUP + " = '0' "+

		//selection arguments
		String[] selectionArgs =new String[] {	ContactsContract.Contacts.IN_VISIBLE_GROUP,
				CommonDataKinds.Phone.DISPLAY_NAME,
				CommonDataKinds.Phone.DISPLAY_NAME};
		selectionArgs = null;

		// grab URI	  
		Uri uri = CommonDataKinds.Phone.CONTENT_URI;

		// sort order alphabetical
		String sortOrder = CommonDataKinds.Phone.DISPLAY_NAME + " COLLATE LOCALIZED ASC";

		// grab cursor from search result
		Cursor cursor = act.managedQuery(uri, projection, selection, selectionArgs, sortOrder);
		act.startManagingCursor(cursor);
		return cursor;
	}
	
	public static Cursor getContactInfoInGroup(ContentResolver cr, String group, Boolean isIdFlag, String[] projection){

		// if isn't Id, then we must find the id and return null if not found
		if (!isIdFlag){

			// read the names and ids
			ArrayList<TwoStrings> namesAndIds = getGroupNamesAndIdsList(cr);

			// find the id that matches the name and return null if no match
			String tmpGroup = null;
			TwoStrings current;
			for (int i=0; i < namesAndIds.size(); i++){
				current = namesAndIds.get(i);
				if (current.mObject1.compareToIgnoreCase(group)==0){
					tmpGroup = current.mObject2;
					break;
				}
			}
			if (tmpGroup == null)
				return null;
			group = tmpGroup;
		}


		String selection = ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID+ 
		" = '"+String.valueOf(group)+"'";

		Cursor cursor = cr.query(Data.CONTENT_URI, projection,
				selection, null, null);

		return cursor;
	}
	
	public static Cursor getContactInfoInGroup(Activity act, String group, Boolean isIdFlag, String[] projection){

		// if isn't Id, then we must find the id and return null if not found
		if (!isIdFlag){

			// read the names and ids
			ArrayList<TwoStrings> namesAndIds = getGroupNamesAndIdsList(act);

			// find the id that matches the name and return null if no match
			String tmpGroup = null;
			TwoStrings current;
			for (int i=0; i < namesAndIds.size(); i++){
				current = namesAndIds.get(i);
				if (current.mObject1.compareToIgnoreCase(group)==0){
					tmpGroup = current.mObject2;
					break;
				}
			}
			if (tmpGroup == null)
				return null;
			group = tmpGroup;
		}


		String selection = ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID+ 
		" = '"+String.valueOf(group)+"'";

		Cursor cursor = act.managedQuery(Data.CONTENT_URI, projection,
				selection, null, null);

		act.startManagingCursor(cursor);
		return cursor;
	}

	public static Cursor getRowsFromContactId(Activity act, int contactId, String[] projection){
		String selection = ContactsContract.CommonDataKinds.Phone.CONTACT_ID+ 
		" = '"+String.valueOf(contactId)+"'";

		Cursor cursor = act.managedQuery(CommonDataKinds.Phone.CONTENT_URI, projection,
				selection, null, null);

		act.startManagingCursor(cursor);
		return cursor;
	}
	
	public static String getRawContactIdFromPhoneId(Activity act, long contactId){
		String selection = ContactsContract.CommonDataKinds.Phone._ID+
		" = '"+String.valueOf(contactId)+"'";
		String[] projection = {ContactsContract.CommonDataKinds.Phone.RAW_CONTACT_ID};
		String out;
		
		Cursor cursor = act.managedQuery(CommonDataKinds.Phone.CONTENT_URI, projection,
				selection, null, null);
		if (cursor.moveToFirst()){
			out = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.RAW_CONTACT_ID));
		}else
			out = "";
		
		cursor.close();

		return out;
	}

	public static Cursor getRowsFromContactId(Activity act, long contactId, String[] projection){
		String selection = ContactsContract.CommonDataKinds.Phone.CONTACT_ID+ 
		" = '"+String.valueOf(contactId)+"'";

		Cursor cursor = act.managedQuery(CommonDataKinds.Phone.CONTENT_URI, projection,
				selection, null, null);

		act.startManagingCursor(cursor);
		return cursor;
	}

	public static Cursor getRowsFromId(Activity act, long contactId, String[] projection){
		String selection = ContactsContract.CommonDataKinds.Phone._ID+ 
		" = '"+String.valueOf(contactId)+"'";

		Cursor cursor = act.managedQuery(CommonDataKinds.Phone.CONTENT_URI, projection,
				selection, null, null);

		act.startManagingCursor(cursor);
		return cursor;
	}
	
	public static Cursor getRowsFromId(ContentResolver cr, long contactId, String[] projection){
		String selection = ContactsContract.CommonDataKinds.Phone._ID+ 
		" = '"+String.valueOf(contactId)+"'";

		Cursor cursor = cr.query(CommonDataKinds.Phone.CONTENT_URI, projection,
				selection, null, null);

		return cursor;
	}

	public static String getFirstName(Cursor cursor, Activity act){
		String selectedName = null;
		String lastName = null;
		long contactId = cursor.getLong(cursor.getColumnIndex(CommonDataKinds.Phone.RAW_CONTACT_ID));
		Uri uri = ContactsContract.Data.CONTENT_URI;
		String selection = ContactsContract.Data.RAW_CONTACT_ID+ " = " + contactId+
			" AND "+ ContactsContract.Data.MIMETYPE+ " = '"+
			ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE+"'";

		// grab cursor from search result
		Cursor cursor2 = act.managedQuery(uri, null, selection, null, null);
		
		// grab first and last name
		if (cursor2 != null && cursor2.moveToFirst()){
			selectedName = cursor2.getString(cursor2.getColumnIndex
					(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME));
			lastName = cursor2.getString(cursor2.getColumnIndex
					(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME));
		}
		
		// if we have no last name and a space in the first name, then
		// it's very likely the last name is actually falsly in the first,
		// so throw out everything pass the first space
		if (selectedName != null && selectedName.length() != 0 && 
				(lastName == null || lastName.length() == 0)){
			int lastSpace = selectedName.lastIndexOf(" ");
			if (lastSpace != -1){
				selectedName = selectedName.substring(0, lastSpace).replaceAll("\\s+$", "");;
			}
		}
		
		if (cursor2 != null)
			cursor2.close();
		
		return selectedName;
	}

	public static String getNickname(Cursor cursor, Activity act){

		//grab the id we are currently at
		int id = cursor.getInt(cursor.getColumnIndex(CommonDataKinds.Phone.CONTACT_ID));

		// the string to search on, the id, nickname mimetype and custom type
		String selection = CommonDataKinds.Nickname.CONTACT_ID + " = " + id + " AND "+
			CommonDataKinds.Nickname.MIMETYPE + " = '" + CommonDataKinds.Nickname.CONTENT_ITEM_TYPE + "'";
			//	+ " AND (" + CommonDataKinds.Nickname.TYPE + " = " + CommonDataKinds.Nickname.TYPE_CUSTOM + ")";

		// the columns to grab back
		String[] projection = {CommonDataKinds.Nickname.TYPE, 
				CommonDataKinds.Nickname.NAME};

		// do the search
		Cursor cursorNickname = act.managedQuery(Data.CONTENT_URI, 
				projection,
				selection, null, null);

		// grab the columns of interest
		int typeCol = cursorNickname.getColumnIndex(CommonDataKinds.Nickname.TYPE);
		int nameCol = cursorNickname.getColumnIndex(CommonDataKinds.Nickname.NAME);
		
		// the string to output
		String nickname = null;
		
		// search across all matches
		if (cursorNickname != null && cursorNickname.moveToFirst()){
			do{
				//must be a custom type (which is the nickname i think)
				if (cursorNickname.getInt(typeCol) == CommonDataKinds.Nickname.TYPE_CUSTOM){
					
					//grab the nickname
					nickname = cursorNickname.getString(nameCol);
					
					// if we have a value, then stop searching
					if (nickname != null)
						break;
				}
			}while(cursorNickname.moveToNext());
		}
		
		if (cursorNickname != null)
			cursorNickname.close();
		
		// return the nickname
		return nickname;
	}
	
	public static Uri insertNewNickname(Cursor cursor, Activity act, String nickname){
		
		//grab the id we are currently at
		int rawId = cursor.getInt(cursor.getColumnIndex(CommonDataKinds.Phone.RAW_CONTACT_ID));

		// initialize values
		ContentValues values = new ContentValues();
		values.put(Nickname.RAW_CONTACT_ID, rawId);
		values.put(CommonDataKinds.Nickname.MIMETYPE, CommonDataKinds.Nickname.CONTENT_ITEM_TYPE);
		values.put(CommonDataKinds.Nickname.NAME, nickname);
		values.put(CommonDataKinds.Nickname.TYPE, CommonDataKinds.Nickname.TYPE_CUSTOM);

		// insert the value
		Uri url = act.getContentResolver().insert(Data.CONTENT_URI, values);
		
		return url;
	}
	
	public static int getNicknameID(Cursor cursor, Activity act){

		//grab the id we are currently at
		int id = cursor.getInt(cursor.getColumnIndex(CommonDataKinds.Phone.CONTACT_ID));

		// the string to search on, the id, nickname mimetype and custom type
		String selection = CommonDataKinds.Nickname.CONTACT_ID + " = " + id + " AND "+
		CommonDataKinds.Nickname.MIMETYPE + " = '" + CommonDataKinds.Nickname.CONTENT_ITEM_TYPE + "'";
		//	+ " AND (" + CommonDataKinds.Nickname.TYPE + " = " + CommonDataKinds.Nickname.TYPE_CUSTOM + ")";

		// the columns to grab back
		String[] projection = {CommonDataKinds.Nickname.TYPE, 
				CommonDataKinds.Nickname.NAME, 
				CommonDataKinds.Nickname._ID};

		// do the search
		Cursor cursorNickname = act.managedQuery(Data.CONTENT_URI, 
				projection,
				selection, null, null);

		// grab the columns of interest
		int typeCol = cursorNickname.getColumnIndex(CommonDataKinds.Nickname.TYPE);
		int nameCol = cursorNickname.getColumnIndex(CommonDataKinds.Nickname.NAME);
		int idCol = cursorNickname.getColumnIndex(CommonDataKinds.Nickname._ID);
		
		// the string to output
		String nickname = null;
		int id1 = -1;
		
		// search across all matches
		if (cursorNickname != null && cursorNickname.moveToFirst()){
			do{
				//must be a custom type (which is the nickname i think)
				if (cursorNickname.getInt(typeCol) == CommonDataKinds.Nickname.TYPE_CUSTOM){
					
					//grab the nickname
					nickname = cursorNickname.getString(nameCol);
					
					// if we have a value, then stop searching
					if (nickname != null){
						id1 = cursorNickname.getInt(idCol);
						break;
					}
				}
			}while(cursorNickname.moveToNext());
		}
		
		if (cursorNickname != null)
			cursorNickname.close();
		
		// return the nickname
		return id1;
	}
}
