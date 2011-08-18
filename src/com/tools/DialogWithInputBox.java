package com.tools;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;

public class DialogWithInputBox extends Activity {
	
	public static final String HINT_BUNDLE = "hint";
	public static final String TITLE_BUNDLE = "title";
	public static final String DEFAULT_TEXT = "defaultText";
	public static final String RESULT = "result";
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// initialize variables
		String hint=null;
		String title=null;
		String defaultText=null;

		// grab extras
		Bundle extras = getIntent().getExtras();
		if (extras!=null){
			hint = extras.getString(HINT_BUNDLE);
			title = extras.getString(TITLE_BUNDLE);
			defaultText = extras.getString(DEFAULT_TEXT);	
		}
		
		// create dialog and edit text
		final AlertDialog.Builder alert = new AlertDialog.Builder(this);
		final EditText input = new EditText(this);
		
		// set title
		if (title != null){alert.setMessage(title);}
		
		// set hint
		if (hint !=null){input.setHint(hint);}
		
		// set default text
		if (defaultText !=null){input.setText(defaultText);}
		
		alert.setView(input);
		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				String value = input.getText().toString().trim();
				Bundle bundle = new Bundle();

                bundle.putString(RESULT, value);

                Intent mIntent = new Intent();
                mIntent.putExtras(bundle);
                setResult(RESULT_OK, mIntent);
                finish();
			}
		});

		alert.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						dialog.cancel();
					}
				});
		alert.show();

	}
}