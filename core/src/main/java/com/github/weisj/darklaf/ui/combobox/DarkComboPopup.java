/*
 * MIT License
 *
 * Copyright (c) 2020 Jannis Weis
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.github.weisj.darklaf.ui.combobox;

import com.github.weisj.darklaf.components.OverlayScrollPane;
import com.github.weisj.darklaf.ui.scrollpane.DarkScrollBarUI;

import javax.swing.*;
import javax.swing.plaf.basic.BasicComboPopup;
import java.awt.*;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

/**
 * @author Jannis Weis
 */
public class DarkComboPopup extends BasicComboPopup {

    private final AdjustmentListener adjustmentListener = e -> {
        Point p = MouseInfo.getPointerInfo().getLocation();
        SwingUtilities.convertPointFromScreen(p, list);
        MouseEvent ev = new MouseEvent(list,
                                       MouseEvent.MOUSE_MOVED, 0, 0, p.x, p.y, 1,
                                       false, 0);
        for (MouseMotionListener ml : list.getMouseMotionListeners()) {
            if (ml != null) {
                ml.mouseMoved(ev);
            }
        }
    };
    private double lastEvent;
    private boolean visible = false;
    private OverlayScrollPane overlayScrollPane;

    /**
     * Constructs a new instance of {@code BasicComboPopup}.
     *
     * @param combo an instance of {@code JComboBox}
     */
    public DarkComboPopup(final JComboBox<Object> combo) {
        super(combo);
    }


    @Override
    protected void firePopupMenuWillBecomeVisible() {
        if (list.getModel().getSize() != 0) {
            int height = list.getUI().getCellBounds(list, 0, 0).height;
            overlayScrollPane.getVerticalScrollBar().setUnitIncrement(height);
        }
        visible = true;
        overlayScrollPane.getVerticalScrollBar().addAdjustmentListener(adjustmentListener);
        super.firePopupMenuWillBecomeVisible();
    }

    @Override
    protected void firePopupMenuWillBecomeInvisible() {
        lastEvent = System.currentTimeMillis();
        visible = false;
        overlayScrollPane.getVerticalScrollBar().removeAdjustmentListener(adjustmentListener);
        super.firePopupMenuWillBecomeInvisible();
    }

    @Override
    protected void firePopupMenuCanceled() {
        visible = false;
        overlayScrollPane.getVerticalScrollBar().removeAdjustmentListener(adjustmentListener);
        super.firePopupMenuCanceled();
    }

    @Override
    protected JScrollPane createScroller() {
        overlayScrollPane = new OverlayScrollPane(list, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                                                  ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        overlayScrollPane.getVerticalScrollBar().putClientProperty(DarkScrollBarUI.KEY_SMALL, Boolean.TRUE);
        return overlayScrollPane.getScrollPane();
    }

    @Override
    protected void configureScroller() {
        scroller.setFocusable(false);
        scroller.getVerticalScrollBar().setFocusable(false);
        scroller.setBorder(null);
    }

    @Override
    protected void configurePopup() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorderPainted(true);
        setOpaque(false);
        add(overlayScrollPane);
        setFocusable(false);
    }

    @Override
    protected void togglePopup() {
        if (comboBox.getItemCount() == 0) return;
        if (visible) {
            visible = false;
            hide();
        } else if (lastEvent == 0 || (System.currentTimeMillis() - lastEvent) > 250) {
            visible = true;
            SwingUtilities.invokeLater(this::show);
        }
    }

    protected void reset() {
        lastEvent = 0;
        if (visible) {
            hide();
        }
        visible = false;
    }
}
