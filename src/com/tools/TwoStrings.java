package com.tools;

/**
 * Stores two string together. Also these strings are comparable and can
 * thus be sorted. The sorting is performed on the first string, and then the second if the firsts are equal.
 * The first string is the main string and two objects are considered equal if the first strings match. The 
 * 2nd string is a support string.
 * @author Kyle
 *
 */
public class TwoStrings implements Comparable<TwoStrings>{

	public String mObject1;
	public String mObject2;
	public String mDescription;
	
	/**
	 * Constructor for simply storing two strings.
	 * @param obj1 First string
	 * @param obj2 Second string
	 */
	public TwoStrings(String obj1, String obj2) {
		mObject1 = obj1;
		mObject2 = obj2;
		mDescription = "";
	}
	
	/**
	 * Constructor for simply storing two strings and their description
	 * @param obj1 First string
	 * @param obj2 Second string
	 * @param description The description of the object in question
	 */
	public TwoStrings(String obj1, String obj2, String description){
		mObject1 = obj1;
		mObject2 = obj2;
		mDescription = description;
	}
	
	// the compare function. Sorts on the first string and then the 2nd if they are equal
//	@Override
	public int compareTo(TwoStrings input) {
		int output = this.mObject1.compareToIgnoreCase(input.mObject1);
		if (output == 0)
			output = this.mObject2.compareToIgnoreCase(input.mObject2);

		return output;
	}
	
	/**
	 * These two objects are considered equal if their first strings match
	 */
	public boolean equals(Object o){
		String e = this.mObject1;
		String input = ((TwoStrings) o).mObject1;
		return (input==null ? e==null : input.equals(e));
	}
	
	/**
	 * Return the first string as the string of the object
	 */
	@Override
	public String toString(){
		return mObject1;
	}
}
