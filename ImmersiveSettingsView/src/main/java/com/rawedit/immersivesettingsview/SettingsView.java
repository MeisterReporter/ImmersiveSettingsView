package com.rawedit.immersivesettingsview;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.BlendMode;
import android.graphics.BlendModeColorFilter;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.transition.TransitionInflater;
import android.transition.TransitionManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.rawedit.immersivesettingsview.items.CheckBoxSettingsItem;
import com.rawedit.immersivesettingsview.items.CustomSettingsItem;
import com.rawedit.immersivesettingsview.items.EditTextSettingsItem;
import com.rawedit.immersivesettingsview.items.SettingsItem;
import com.rawedit.immersivesettingsview.items.SliderSettingsItem;
import com.rawedit.immersivesettingsview.items.SwitchSettingsItem;
import com.rawedit.immersivesettingsview.items.TextSettingsItem;
import com.rawedit.immersivesettingsview.pages.SettingsPage;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class SettingsView extends LinearLayout {

    private boolean initialized = false;
    private boolean animateLayoutChanges = true;
    private boolean showDividers = true;

    private int drawablePosition = TextSettingsItem.LEFT;

    @ColorInt
    private int rippleColor = 0;
    private long openPageDelay = 250;

    public static String settingsName = "ImmersiveSettingsView";

    public static final String MAIN_PAGE_NAME = "MAIN_PAGE";
    public static final String TAG = SettingsView.class.getSimpleName();

    private SettingsPage currentPage;
    private HashMap<String, Object> selectablePages = new HashMap<>();
    private ArrayList<String> lastPages = new ArrayList<>();

    private OnPageChanged onPageChangedListener;

    public interface OnPageChanged {
        /**
         * Notifies if the current page changed.
         * If you want to check if the current page is the Main Page
         * check if the title equals SettingsView.MAIN_PAGE_NAME.
         * @param title the Title of the current Page
         */
        void pageChanged(String title);
    }

    public SettingsView(@NonNull Context context) {
        this(context, null);
    }

    public SettingsView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SettingsView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public SettingsView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        TypedArray ta = context.getTheme().obtainStyledAttributes(attrs, R.styleable.SettingsView, defStyleAttr, defStyleRes);
        try {
            rippleColor = ta.getColor(R.styleable.SettingsView_rippleColor, context.getColor(R.color.defaultRipple));
            openPageDelay = ta.getInt(R.styleable.SettingsView_openPageDelay, 250);
            showDividers = ta.getBoolean(R.styleable.SettingsView_showItemDividers, true);
        }catch (Exception e) {
            e.printStackTrace();
            // Default Values if getting attributes failed
            rippleColor = context.getColor(R.color.defaultRipple);
            showDividers = true;
            openPageDelay = 250;
        }
        initMainPage();
        initialized = true;
        Log.d(TAG, "SettingsView initialized");
    }

    /**
     * Adds a new Page to the View which can be opened by clicking on the correct Item
     * @param page the Page to add
     */
    public void add(SettingsPage page) {
        selectablePages.put(page.getTitle(), page);
        animateLayoutChanges = false;
        if(initialized) initMainPage();
        animateLayoutChanges = true;
    }

    public void add(SettingsItem item) {
        if(item instanceof CustomSettingsItem) {
            CustomSettingsItem customSettingsItem = (CustomSettingsItem) item;
            customSettingsItem.getSetupViewsListener().setupViews(customSettingsItem.getView());
        }
        selectablePages.put(String.valueOf(selectablePages.size()), item);
        animateLayoutChanges = false;
        if(initialized) initMainPage();
        animateLayoutChanges = true;
    }

    /**
     * Initializes the Main Page which displays all descendant Pages or Items
     */
    public void initMainPage() {
        SettingsPage main = new SettingsPage(getContext());
        main.setRippleColor(rippleColor);
        main.setItemName(MAIN_PAGE_NAME);
        main.setTitle(MAIN_PAGE_NAME);
        Log.d(TAG, "Page Size " + selectablePages.size());
        for(Object value : selectablePages.values()) {
            if(value instanceof SettingsPage) {
                SettingsPage page = (SettingsPage) value;
                TextSettingsItem item = (TextSettingsItem) SettingsItem.createSettingsItem(SettingsItem.Type.TEXT, getContext());
                main.add(item, page.getTitle());
                if(item != null) {
                    if(page.getItemDrawable() != null) {
                        item.setDrawable(page.getItemDrawable(), drawablePosition);
                    }else if(page.getItemDrawableResource() > 0) {
                        item.setDrawable(page.getItemDrawableResource(), drawablePosition);
                    }
                    item.setText(page.getItemName());
                    item.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    Log.d(TAG, "Opening Page " + page.getTitle());
                                    setPage(page, true);
                                }
                            }, openPageDelay);
                        }
                    });
                    Log.d(TAG, "Inflated item " + page.getItemName());
                }else {
                    Log.d(TAG, "Item could not be inflated because its null: Unknown Item Type");
                }
            }else if(value instanceof SettingsItem) {
                SettingsItem item = (SettingsItem) value;
                if(item.getParent() != null) {
                    ViewGroup vg = (ViewGroup) item.getParent();
                    vg.removeView(item);
                }
                main.add(item);
            }
        }
        setPage(main, false);
        Log.d(TAG, "Main Page set");
    }

    /**
     * Removes the old page and adds the passed Page to the View
     * @param page the page to show
     * @param saveLastPage should the current page be saved, as a page to get back to later
     */
    public void setPage(SettingsPage page, boolean saveLastPage) {
        if(currentPage != null && saveLastPage) {
            Log.d(TAG, "Removing Page " + currentPage.getTitle());
            lastPages.add(currentPage.getTitle());
        }
        if(animateLayoutChanges) TransitionManager.beginDelayedTransition(this, TransitionInflater.from(getContext()).inflateTransition(R.transition.fade_transition));
        removeView(currentPage);
        currentPage = page;
        if(animateLayoutChanges) TransitionManager.beginDelayedTransition(this, TransitionInflater.from(getContext()).inflateTransition(R.transition.fade_transition));
        addView(page, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        if(onPageChangedListener != null) onPageChangedListener.pageChanged(page.getTitle());
        Log.d(TAG, "Set new Page " + currentPage.getTitle());
        page.updateLayout();
        page.resetStates();
        page.setRippleColor(rippleColor);
    }

    /**
     * Sets the Ripple Color for the Current Page and all its descendants
     * Any Incoming Pages or Items will automatically get the Ripple Color
     * @param rippleColor the Ripple Color as @ColorInt
     */
    public void setRippleColor(@ColorInt int rippleColor) {
        this.rippleColor = rippleColor;
        for(Object o : selectablePages.values()) {
            if(o instanceof SettingsPage) {
                SettingsPage p = (SettingsPage) o;
                p.setRippleColor(rippleColor);
            }else if(o instanceof SettingsItem) {
                SettingsItem i = (SettingsItem) o;
                i.setRippleColor(rippleColor);
            }
        }
    }

    public void setAlternativeColor(@ColorInt int alternativeColor) {
        for(Object o : selectablePages.values()) {
            if(o instanceof SettingsPage) {
                SettingsPage p = (SettingsPage) o;
                p.setAlternativeColor(alternativeColor);
            }else if(o instanceof SettingsItem) {
                if(o instanceof SwitchSettingsItem) {
                    SwitchSettingsItem i = (SwitchSettingsItem) o;
                    i.setSwitchColor(alternativeColor);
                }else if(o instanceof CheckBoxSettingsItem) {
                    CheckBoxSettingsItem i = (CheckBoxSettingsItem) o;
                    i.setCheckBoxColor(alternativeColor);
                }else if(o instanceof EditTextSettingsItem) {
                    EditTextSettingsItem i = (EditTextSettingsItem) o;
                    i.setStrokeColor(alternativeColor);
                }else if(o instanceof SliderSettingsItem) {
                    SliderSettingsItem i = (SliderSettingsItem) o;
                    i.setRippleColor(alternativeColor);
                }
            }
        }
    }

    /**
     * Move one Page Back til the Main Page
     * @return if there are any pages to get back to
     */
    public boolean back() {
        if(lastPages.size() > 0) {
            Object o = selectablePages.get(lastPages.get(lastPages.size() - 1));
            if(o instanceof SettingsPage) {
                SettingsPage lastPage = (SettingsPage) o;
                lastPages.remove(lastPages.size() - 1);
                setPage(lastPage, false);
                return true;
            }else {
                initMainPage();
                return true;
            }
        }else {
            return false;
        }
    }

    public boolean isShowDividers() {
        return showDividers;
    }

    /**
     * Sets if Dividers between Items should be shown
     * @param showDividers true if the dividers should be shown else false
     */
    public void setShowDividers(boolean showDividers) {
        this.showDividers = showDividers;
        if(currentPage != null) {
            currentPage.setShowDividers(showDividers);
        }
    }

    public OnPageChanged getOnPageChangedListener() {
        return onPageChangedListener;
    }

    /**
     * This sets the Listener which gets called every time the current Page changes
     * @param onPageChangedListener the new Listener
     */
    public void setOnPageChangedListener(OnPageChanged onPageChangedListener) {
        this.onPageChangedListener = onPageChangedListener;
    }

    /**
     * Use this Method to create a new SettingsPage instead of the Constructor
     * if you have no Theme Specific Context (do not use the Application Context)
     * @return a new Settings Page
     */
    public SettingsPage createSettingsPage() {
        return new SettingsPage(getContext());
    }

    public SettingsItem createSettingsItem(SettingsItem.Type type) {
        return SettingsItem.createSettingsItem(type, getContext());
    }

    public SettingsItem createSettingsItem(SettingsItem.Type type, int layout) {
        return SettingsItem.createSettingsItem(type, getContext(), layout);
    }

    public long getOpenPageDelay() {
        return openPageDelay;
    }

    public void setOpenPageDelay(long openPageDelay) {
        this.openPageDelay = openPageDelay;
    }

    public int getDrawablePosition() {
        return drawablePosition;
    }

    public void setDrawablePosition(int drawablePosition) {
        if(initialized) {
            this.drawablePosition = drawablePosition;
            initMainPage();
        }
    }

    // Static Helper Methods
    public static RippleDrawable getPressedColorRippleDrawable(int normalColor, int pressedColor) {
        return new RippleDrawable(getPressedColorSelector(normalColor, pressedColor), getColorDrawableFromColor(normalColor), null);
    }

    public static ColorStateList getPressedColorSelector(int normalColor, int pressedColor) {
        return new ColorStateList(
                new int[][]
                        {
                                new int[]{android.R.attr.state_pressed},
                                new int[]{android.R.attr.state_focused},
                                new int[]{android.R.attr.state_activated},
                                new int[]{}
                        },
                new int[]
                        {
                                pressedColor,
                                pressedColor,
                                pressedColor,
                                normalColor
                        }
        );
    }

    public static ColorDrawable getColorDrawableFromColor(int color) {
        return new ColorDrawable(color);
    }

    public static Drawable getAdaptiveRippleDrawable(int normalColor, int pressedColor, int cornerRadius) {
        return new RippleDrawable(ColorStateList.valueOf(pressedColor),
                null, getRippleMask(normalColor, cornerRadius));
    }

    public static Drawable getAdaptiveRippleDrawable(int pressedColor, View view) {

        return new RippleDrawable(ColorStateList.valueOf(pressedColor),
                view.getBackground(), view.getBackground());
    }

    private static Drawable getRippleMask(int color, int cornerRadius) {
        float[] outerRadii = new float[8];
        Arrays.fill(outerRadii, cornerRadius);

        RoundRectShape r = new RoundRectShape(outerRadii, null, null);
        ShapeDrawable shapeDrawable = new ShapeDrawable(r);
        shapeDrawable.getPaint().setColor(color);
        return shapeDrawable;
    }

    public static StateListDrawable getStateListDrawable(
            int normalColor, int pressedColor) {
        StateListDrawable states = new StateListDrawable();
        states.addState(new int[]{android.R.attr.state_pressed},
                new ColorDrawable(pressedColor));
        states.addState(new int[]{android.R.attr.state_focused},
                new ColorDrawable(pressedColor));
        states.addState(new int[]{android.R.attr.state_activated},
                new ColorDrawable(pressedColor));
        states.addState(new int[]{},
                new ColorDrawable(normalColor));
        return states;
    }

    @ColorInt
    public static int adjustAlpha(@ColorInt int color, float factor) {
        int alpha = Math.round(Color.alpha(color) * factor);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(alpha, red, green, blue);
    }

    public static void setCursorColor(EditText editText, @ColorInt int color) {
        try {
            editText.setHighlightColor(adjustAlpha(color, 0.5f));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                Drawable d = editText.getTextCursorDrawable();
                d.setTint(color);
                editText.setTextCursorDrawable(d);
                if (editText.getTextCursorDrawable() instanceof InsetDrawable) {
                    InsetDrawable insetDrawable = (InsetDrawable) editText.getTextCursorDrawable();
                    insetDrawable.setColorFilter(new BlendModeColorFilter(color, BlendMode.SRC_ATOP));

                    editText.setTextCursorDrawable(insetDrawable);
                }
                if (editText.getTextSelectHandle() instanceof BitmapDrawable) {
                    BitmapDrawable insetDrawable = (BitmapDrawable) editText.getTextSelectHandle();
                    insetDrawable.setColorFilter(new BlendModeColorFilter(color, BlendMode.SRC_ATOP));

                    editText.setTextSelectHandle(insetDrawable);
                }
                if (editText.getTextSelectHandleRight() instanceof BitmapDrawable) {
                    BitmapDrawable insetDrawable = (BitmapDrawable) editText.getTextSelectHandleRight();
                    insetDrawable.setColorFilter(new BlendModeColorFilter(color, BlendMode.SRC_ATOP));

                    editText.setTextSelectHandleRight(insetDrawable);
                }
                if (editText.getTextSelectHandleLeft() instanceof BitmapDrawable) {
                    BitmapDrawable insetDrawable = (BitmapDrawable) editText.getTextSelectHandleLeft();
                    insetDrawable.setColorFilter(new BlendModeColorFilter(color, BlendMode.SRC_ATOP));

                    editText.setTextSelectHandleLeft(insetDrawable);
                }
            } else {
                Field editorField = TextView.class.getDeclaredField("mEditor");
                if (!editorField.isAccessible()) {
                    editorField.setAccessible(true);
                }

                Object editor = editorField.get(editText);
                Class<?> editorClass = editor.getClass();

                String[] handleNames = {"mSelectHandleLeft", "mSelectHandleRight", "mSelectHandleCenter"};
                String[] resNames = {"mTextSelectHandleLeftRes", "mTextSelectHandleRightRes", "mTextSelectHandleRes"};

                for (int i = 0; i < handleNames.length; i++) {
                    Field handleField = editorClass.getDeclaredField(handleNames[i]);
                    if (!handleField.isAccessible()) {
                        handleField.setAccessible(true);
                    }

                    Drawable handleDrawable = (Drawable) handleField.get(editor);

                    if (handleDrawable == null) {
                        Field resField = TextView.class.getDeclaredField(resNames[i]);
                        if (!resField.isAccessible()) {
                            resField.setAccessible(true);
                        }
                        int resId = resField.getInt(editText);
                        handleDrawable = editText.getResources().getDrawable(resId);
                    }

                    if (handleDrawable != null) {
                        Drawable drawable = handleDrawable.mutate();
                        drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN);
                        handleField.set(editor, drawable);
                    }
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
