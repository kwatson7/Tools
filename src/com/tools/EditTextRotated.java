package com.tools;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.EditText;

public class EditTextRotated extends EditText {

	public EditTextRotated(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onDraw(Canvas canvas) {
	     canvas.save();
	     canvas.rotate(90);
	     super.onDraw(canvas);
	     canvas.restore();
	     

	}

}
