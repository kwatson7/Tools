package com.tools;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * Make a listView with checkboxes, where multiple items can be checked. Simply set
 * OnClickListeners for the keys and use getChecked() to determine which items were selected.
 * You can pass an ArrayList of any object, and the toString() will be used to display the 
 * listView.
 * @author Kyle
 *
 * @param <objectType>
 */
public class MultipleCheckPopUp<objectType> extends AlertDialog{

	// class variables
	private ArrayList<objectType> mMainArrayList; 
	protected ListView listView;
	private Context mCtx;

	/**
	 * Constructor.
	 * @param ctx The context which calls this
	 * @param mainArrayList The arrayList of objects to show in listView
	 * @param title The title of the listView
	 * @param defaultClicked Whether items should be clicked or not
	 * @param overrideDefaultClickedIds an array of items that override the default behavior of clicked
	 */
	public MultipleCheckPopUp(Context ctx, 
			ArrayList < objectType> mainArrayList,
			String title,
			boolean defaultClicked,
			long [] overrideDefaultClickedIds){

		// construct it
		super(ctx);
		
		// assign inputs
		mMainArrayList = mainArrayList;
		mCtx = ctx;

		// create the dialog
		if(title != null){
			this.setTitle(title);
		}
		listView = new ListView(mCtx);
		listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

		//create and link adapters
		ArrayAdapter<objectType> adapter = new ArrayAdapter<objectType>(mCtx, 
				android.R.layout.simple_list_item_multiple_choice, mMainArrayList);
		listView.setAdapter(adapter);

		// add list view to dialog
		this.setView(listView);

		// make it cancelable
		this.setCancelable(true);
		
		// set them to checked or not checked
		for (int i = 0; i < listView.getCount(); i++){
			listView.setItemChecked(i, defaultClicked);
		}
		
		// override default
		if (overrideDefaultClickedIds != null)
			for (int i = 0; i<overrideDefaultClickedIds.length; i++)
				listView.setItemChecked((int) overrideDefaultClickedIds[i], !defaultClicked);
	}
	
	/**
	 * An array of the checked items.
	 * @return
	 */
	public long[] getChecked(){
		return listView.getCheckItemIds();
	}
}
