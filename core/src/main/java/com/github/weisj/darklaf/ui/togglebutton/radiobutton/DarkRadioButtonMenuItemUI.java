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
package com.github.weisj.darklaf.ui.togglebutton.radiobutton;

import com.github.weisj.darklaf.decorators.MouseClickListener;
import com.github.weisj.darklaf.ui.menu.DarkMenuItemUIBase;
import com.github.weisj.darklaf.ui.togglebutton.StateIconUI;
import com.github.weisj.darklaf.ui.togglebutton.ToggleButtonMenuItemConstants;
import sun.swing.MenuItemLayoutHelper;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import java.awt.*;

/**
 * @author Jannis Weis
 */
public class DarkRadioButtonMenuItemUI extends DarkMenuItemUIBase implements StateIconUI,
                                                                             ToggleButtonMenuItemConstants {

    private final MouseClickListener clickListener = e -> SwingUtilities.invokeLater(() -> {
        if (menuItem != null) menuItem.setArmed(true);
    });

    protected int iconBaselineOffset;
    private Icon radioIcon;
    private Icon radioDisabledIcon;
    private Icon radioFocusedIcon;
    private Icon radioSelectedIcon;
    private Icon radioSelectedDisabledIcon;
    private Icon radioSelectedFocusedIcon;


    public static ComponentUI createUI(final JComponent c) {
        return new DarkRadioButtonMenuItemUI();
    }

    @Override
    protected String getPropertyPrefix() {
        return "RadioButtonMenuItem";
    }

    @Override
    public void installUI(final JComponent c) {
        super.installUI(c);
        acceleratorFont = UIManager.getFont("MenuItem.font");
        acceleratorForeground = UIManager.getColor("MenuItem.foreground");
        acceleratorSelectionForeground = UIManager.getColor("MenuItem.selectionForeground");
        c.putClientProperty(KEY_NO_NOT_CLOSE_ON_CLICK, UIManager.getBoolean(KEY_NO_NOT_CLOSE_ON_CLICK));
    }

    @Override
    public void installDefaults() {
        super.installDefaults();
        iconBaselineOffset = UIManager.getInt(getPropertyPrefix() + ".iconBaselineOffset");
        installIcons();
    }

    protected void installIcons() {
        radioIcon = UIManager.getIcon("RadioButton.unchecked.icon");
        radioDisabledIcon = UIManager.getIcon("RadioButton.uncheckedDisabled.icon");
        radioFocusedIcon = UIManager.getIcon("RadioButton.uncheckedFocused.icon");
        radioSelectedIcon = UIManager.getIcon("RadioButton.selected.icon");
        radioSelectedDisabledIcon = UIManager.getIcon("RadioButton.selectedDisabled.icon");
        radioSelectedFocusedIcon = UIManager.getIcon("RadioButton.selectedFocused.icon");
    }

    @Override
    protected void installListeners() {
        super.installListeners();
        menuItem.addMouseListener(clickListener);
    }

    @Override
    protected void uninstallListeners() {
        super.uninstallListeners();
        menuItem.removeMouseListener(clickListener);
    }

    @Override
    protected void paintCheckIcon(final Graphics g2, final MenuItemLayoutHelper lh,
                                  final MenuItemLayoutHelper.LayoutResult lr,
                                  final Color holdc, final Color foreground) {
        Rectangle rect = lr.getCheckRect();
        getStateIcon(lh.getMenuItem()).paintIcon(lh.getMenuItem(), g2, rect.x, rect.y + iconBaselineOffset);
    }

    protected Icon getStateIcon(final AbstractButton b) {
        return StateIconUI.getStateIcon(this, b);
    }

    public Icon getSelectedFocusedIcon() {
        return radioSelectedFocusedIcon;
    }

    public Icon getSelectedIcon() {
        return radioSelectedIcon;
    }

    public Icon getSelectedDisabledIcon() {
        return radioSelectedDisabledIcon;
    }

    public Icon getFocusedIcon() {
        return radioFocusedIcon;
    }

    public Icon getIcon() {
        return radioIcon;
    }

    public Icon getDisabledIcon() {
        return radioDisabledIcon;
    }
}