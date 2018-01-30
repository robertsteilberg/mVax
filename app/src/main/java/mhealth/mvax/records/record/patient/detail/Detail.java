/*
Copyright (C) 2018 Duke University

This file is part of mVax.

mVax is free software: you can redistribute it and/or
modify it under the terms of the GNU Affero General Public License
as published by the Free Software Foundation, either version 3,
or (at your option) any later version.

mVax is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU General Public
License along with mVax; see the file LICENSE. If not, see
<http://www.gnu.org/licenses/>.
*/
package mhealth.mvax.records.record.patient.detail;

import android.widget.EditText;

/**
 * @author Robert Steilberg
 *         <p>
 *         Abstract class for storing information about a generic Detail,
 *         which is used to popuate a ListView when creating, editing, or
 *         viewing Person data
 */

public abstract class Detail<T> {

    //================================================================================
    // Properties
    //================================================================================

    private T mValue; // raw value of the detail
    String mStringValue; // String representation of the value to be displayed
    private Runnable mSetter; // defines code that sets the value in the Person object

    private final int mLabelStringId; // label displayed next to value
    private final int mHintStringId; // hint displayed in the value field when there is no set value

    //================================================================================
    // Constructors
    //================================================================================

    Detail(T value, int labelStringId, int hintStringId) {
        this.mValue = value;
        this.mLabelStringId = labelStringId;
        this.mHintStringId = hintStringId;
        updateStringValue(value);
    }

    //================================================================================
    // Abstract methods
    //================================================================================

    /**
     * Perform setup operations on the EditText displaying the mValue
     *
     * @param valueView the EditText on which setup is performed
     */
    public abstract void configureValueView(EditText valueView);


    /**
     * Listener to attach to the EditText displaying the mValue
     *
     * @param valueView the EditText on which the listener is attached
     */
    public abstract void getValueViewListener(EditText valueView);

    /**
     * Performs operations to create the String representation of the Detail's
     * value and then sets it through setStringValue()
     *
     * @param value the raw value of the detail
     */
    public abstract void updateStringValue(T value);

    //================================================================================
    // Getter methods
    //================================================================================

    public T getValue() {
        return mValue;
    }

    public String getStringValue() {
        return this.mStringValue;
    }

    public int getLabelStringId() {
        return this.mLabelStringId;
    }

    public int getHintStringId() {
        return this.mHintStringId;
    }

    //================================================================================
    // Setter methods
    //================================================================================

    /**
     * Updates the mValue with a new mValue, updates the UI representation,
     * and runs the mSetter so that the Person object is updated with the
     * new mValue
     *
     * @param value the new mValue
     */
    protected void setValue(T value) {
        this.mValue = value;
        updateStringValue(value);
        mSetter.run();
    }

    /**
     * Defines the setter that is run when mValue updates;
     * should be set immediately after Detail initialization
     *
     * @param setter setter that updates mValue in the
     *               Person object
     */
    public void setSetter(Runnable setter) {
        this.mSetter = setter;
    }

}