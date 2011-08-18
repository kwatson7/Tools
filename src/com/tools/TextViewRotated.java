package com.tools;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * A simple TextView that is rotated 90 degrees
 * @author Kyle
 *
 */
public class TextViewRotated extends TextView {

	public TextViewRotated(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}
	
	public TextViewRotated(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	public TextViewRotated(Context context, AttributeSet attrs) {
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
