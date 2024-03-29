package video.pano.panocall.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.DrawableRes;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.content.ContextCompat;

import video.pano.panocall.R;

/** */
public class ClearableEditText extends AppCompatEditText implements TextWatcher {

    @DrawableRes
    private static final int DEFAULT_CLEAR_ICON_RES_ID = R.drawable.svg_icon_delete;
    @DrawableRes
    private static final int DEFAULT_CLEAR_BG_RES_ID = R.drawable.edit_input_selector_bg;

    private Drawable mClearIconDrawable;

    private boolean mIsClearIconShown = false;

    private boolean mClearIconDrawWhenFocused = true;

    public ClearableEditText(Context context) {
        this(context, null);
    }

    public ClearableEditText(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.editTextStyle);
    }

    public ClearableEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a =
                context.obtainStyledAttributes(attrs, R.styleable.ClearableEditText, defStyle, 0);

        if (a.hasValue(R.styleable.ClearableEditText_clearIconDrawable)) {
            mClearIconDrawable = a.getDrawable(R.styleable.ClearableEditText_clearIconDrawable);
        } else {
            mClearIconDrawable = ContextCompat.getDrawable(context, DEFAULT_CLEAR_ICON_RES_ID);
        }
        if (mClearIconDrawable != null) {
            mClearIconDrawable.setCallback(this);
        }
//        if (a.hasValue(R.styleable.ClearableEditText_clearBgDrawable)) {
//            Drawable clearBgDrawable = a.getDrawable(R.styleable.ClearableEditText_clearBgDrawable);
//            setBackground(clearBgDrawable);
//        } else {
//            Drawable clearBgDrawable = ContextCompat.getDrawable(context, DEFAULT_CLEAR_BG_RES_ID);
//            setBackground(clearBgDrawable);
//        }

        mClearIconDrawWhenFocused =
                a.getBoolean(R.styleable.ClearableEditText_clearIconDrawWhenFocused, true);

        a.recycle();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        // no operation
    }

    @Override
    public void afterTextChanged(Editable s) {}

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        return mIsClearIconShown ? new ClearIconSavedState(superState, true) : superState;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof ClearIconSavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }
        ClearIconSavedState savedState = (ClearIconSavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        mIsClearIconShown = savedState.isClearIconShown();
        showClearIcon(mIsClearIconShown);
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (!mClearIconDrawWhenFocused || hasFocus()) {
            showClearIcon(!TextUtils.isEmpty(s));
        }
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        showClearIcon((!mClearIconDrawWhenFocused || focused) && !TextUtils.isEmpty(getText()));
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isClearIconTouched(event)) {
            setText(null);
            event.setAction(MotionEvent.ACTION_CANCEL);
            showClearIcon(false);
            return false;
        }
        return super.onTouchEvent(event);
    }

    private boolean isClearIconTouched(MotionEvent event) {
        if (!mIsClearIconShown) {
            return false;
        }

        final int touchPointX = (int) event.getX();

        final int widthOfView = getWidth();
        final int compoundPadding =
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1
                        ? getCompoundPaddingEnd()
                        : getCompoundPaddingRight();

        return touchPointX >= widthOfView - compoundPadding;
    }

    private void showClearIcon(boolean show) {
        Drawable[] drawables = getCompoundDrawablesRelative();
        if (show) {
            // show icon on the right
            setCompoundDrawablesRelativeWithIntrinsicBounds(
                    drawables[0], drawables[1], mClearIconDrawable, drawables[3]);
        } else {
            // remove icon
            setCompoundDrawablesRelative(drawables[0], drawables[1], null, drawables[3]);
        }

        mIsClearIconShown = show;
    }

    protected static class ClearIconSavedState extends BaseSavedState {

        public static final Creator<ClearIconSavedState> CREATOR =
                new Creator<ClearIconSavedState>() {
                    @Override
                    public ClearIconSavedState createFromParcel(Parcel source) {
                        return new ClearIconSavedState(source);
                    }

                    @Override
                    public ClearIconSavedState[] newArray(int size) {
                        return new ClearIconSavedState[size];
                    }
                };
        private final boolean mIsClearIconShown;

        private ClearIconSavedState(Parcel source) {
            super(source);
            mIsClearIconShown = source.readByte() != 0;
        }

        ClearIconSavedState(Parcelable superState, boolean isClearIconShown) {
            super(superState);
            mIsClearIconShown = isClearIconShown;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeByte((byte) (mIsClearIconShown ? 1 : 0));
        }

        boolean isClearIconShown() {
            return mIsClearIconShown;
        }
    }
}
