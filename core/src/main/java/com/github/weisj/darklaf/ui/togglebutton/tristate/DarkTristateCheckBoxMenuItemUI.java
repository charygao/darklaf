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
package com.github.weisj.darklaf.ui.togglebutton.tristate;

import com.github.weisj.darklaf.components.tristate.TristateButtonModel;
import com.github.weisj.darklaf.components.tristate.TristateCheckBoxMenuItem;
import com.github.weisj.darklaf.components.tristate.TristateState;
import com.github.weisj.darklaf.ui.togglebutton.checkbox.DarkCheckBoxMenuItemUI;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;

/**
 * @author Jannis Weis
 */
public class DarkTristateCheckBoxMenuItemUI extends DarkCheckBoxMenuItemUI {
    private Icon checkBoxIndeterminateIcon;
    private Icon checkBoxIndeterminateDisabledIcon;
    private Icon checkBoxIndeterminateFocusedIcon;

    public static ComponentUI createUI(final JComponent c) {
        return new DarkTristateCheckBoxMenuItemUI();
    }

    @Override
    public void installDefaults() {
        menuItem.setModel(new TristateButtonModel());
        super.installDefaults();
    }

    @Override
    protected void installIcons() {
        super.installIcons();
        checkBoxIndeterminateIcon = UIManager.getIcon("CheckBox.indeterminate.icon");
        checkBoxIndeterminateDisabledIcon = UIManager.getIcon("CheckBox.indeterminateDisabled.icon");
        checkBoxIndeterminateFocusedIcon = UIManager.getIcon("CheckBox.indeterminateFocused.icon");
    }

    @Override
    protected Icon getStateIcon(final AbstractButton b) {
        if (b instanceof TristateCheckBoxMenuItem) {
            TristateState state = ((TristateCheckBoxMenuItem) b).getTristateState();
            if (state == TristateState.INDETERMINATE) {
                if (b.isEnabled()) {
                    return b.hasFocus() ? checkBoxIndeterminateFocusedIcon
                                        : checkBoxIndeterminateIcon;
                } else {
                    return checkBoxIndeterminateDisabledIcon;
                }
            }
        }
        return super.getStateIcon(b);
    }
}
