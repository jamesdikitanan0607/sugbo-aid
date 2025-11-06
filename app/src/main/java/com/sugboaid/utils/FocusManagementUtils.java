package com.sugboaid.utils;

import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for managing focus and keyboard navigation
 * Ensures proper focus order and accessibility for keyboard users
 */
public class FocusManagementUtils {
    
    /**
     * Set up proper focus order for a container view
     */
    public static void setupFocusOrder(ViewGroup container) {
        List<View> focusableViews = getFocusableViews(container);
        
        for (int i = 0; i < focusableViews.size(); i++) {
            View currentView = focusableViews.get(i);
            
            // Set next focus down
            if (i < focusableViews.size() - 1) {
                currentView.setNextFocusDownId(focusableViews.get(i + 1).getId());
            }
            
            // Set next focus up
            if (i > 0) {
                currentView.setNextFocusUpId(focusableViews.get(i - 1).getId());
            }
            
            // Ensure view is focusable
            setupViewFocusability(currentView);
        }
    }
    
    /**
     * Get all focusable views in a container
     */
    private static List<View> getFocusableViews(ViewGroup container) {
        List<View> focusableViews = new ArrayList<>();
        addFocusableViews(container, focusableViews);
        return focusableViews;
    }
    
    /**
     * Recursively add focusable views to the list
     */
    private static void addFocusableViews(ViewGroup parent, List<View> focusableViews) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);
            
            if (child instanceof ViewGroup && !(child instanceof RecyclerView)) {
                // Recursively check child ViewGroups (except RecyclerView which handles its own focus)
                addFocusableViews((ViewGroup) child, focusableViews);
            } else if (isFocusableView(child)) {
                focusableViews.add(child);
            }
        }
    }
    
    /**
     * Check if a view should be focusable
     */
    private static boolean isFocusableView(View view) {
        return view.isClickable() || 
               view.isFocusable() || 
               view instanceof EditText ||
               view.hasOnClickListeners();
    }
    
    /**
     * Setup proper focusability for a view
     */
    private static void setupViewFocusability(View view) {
        if (view instanceof EditText) {
            // EditText should be focusable in touch mode
            view.setFocusable(true);
            view.setFocusableInTouchMode(true);
        } else if (view.isClickable() || view.hasOnClickListeners()) {
            // Clickable views should be focusable but not in touch mode
            view.setFocusable(true);
            view.setFocusableInTouchMode(false);
        }
        
        // Ensure proper accessibility
        ViewCompat.setImportantForAccessibility(view, ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_YES);
    }
    
    /**
     * Request focus on the first focusable view in a container
     */
    public static void requestInitialFocus(ViewGroup container) {
        List<View> focusableViews = getFocusableViews(container);
        if (!focusableViews.isEmpty()) {
            focusableViews.get(0).requestFocus();
        }
    }
    
    /**
     * Setup focus management for form inputs
     */
    public static void setupFormFocusOrder(List<EditText> inputs) {
        for (int i = 0; i < inputs.size(); i++) {
            EditText currentInput = inputs.get(i);
            
            // Set IME action for next/done
            if (i < inputs.size() - 1) {
                currentInput.setImeOptions(android.view.inputmethod.EditorInfo.IME_ACTION_NEXT);
                final EditText nextInput = inputs.get(i + 1);
                currentInput.setOnEditorActionListener((v, actionId, event) -> {
                    if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_NEXT) {
                        nextInput.requestFocus();
                        return true;
                    }
                    return false;
                });
            } else {
                currentInput.setImeOptions(android.view.inputmethod.EditorInfo.IME_ACTION_DONE);
            }
            
            // Setup accessibility
            setupViewFocusability(currentInput);
        }
    }
    
    /**
     * Setup focus management for RecyclerView items
     */
    public static void setupRecyclerViewFocus(RecyclerView recyclerView) {
        recyclerView.setFocusable(true);
        recyclerView.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
        
        // Handle focus changes within RecyclerView
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    // Restore focus to visible items if needed
                    View focusedChild = recyclerView.getFocusedChild();
                    if (focusedChild == null) {
                        // Try to focus the first visible item
                        View firstVisible = recyclerView.getLayoutManager().getChildAt(0);
                        if (firstVisible != null && firstVisible.isFocusable()) {
                            firstVisible.requestFocus();
                        }
                    }
                }
            }
        });
    }
    
    /**
     * Clear focus from all views in a container
     */
    public static void clearFocus(ViewGroup container) {
        container.clearFocus();
        
        // Also clear focus from all child views
        for (int i = 0; i < container.getChildCount(); i++) {
            View child = container.getChildAt(i);
            if (child instanceof ViewGroup) {
                clearFocus((ViewGroup) child);
            } else {
                child.clearFocus();
            }
        }
    }
    
    /**
     * Setup focus trap for modal dialogs
     */
    public static void setupFocusTrap(ViewGroup dialog) {
        List<View> focusableViews = getFocusableViews(dialog);
        
        if (focusableViews.size() >= 2) {
            View firstFocusable = focusableViews.get(0);
            View lastFocusable = focusableViews.get(focusableViews.size() - 1);
            
            // Trap focus within the dialog
            firstFocusable.setOnKeyListener((v, keyCode, event) -> {
                if (keyCode == android.view.KeyEvent.KEYCODE_TAB && 
                    event.getAction() == android.view.KeyEvent.ACTION_DOWN &&
                    event.isShiftPressed()) {
                    lastFocusable.requestFocus();
                    return true;
                }
                return false;
            });
            
            lastFocusable.setOnKeyListener((v, keyCode, event) -> {
                if (keyCode == android.view.KeyEvent.KEYCODE_TAB && 
                    event.getAction() == android.view.KeyEvent.ACTION_DOWN &&
                    !event.isShiftPressed()) {
                    firstFocusable.requestFocus();
                    return true;
                }
                return false;
            });
        }
    }
}