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
package com.github.weisj.darklaf.ui.rootpane;

import com.github.weisj.darklaf.components.border.MutableLineBorder;
import com.github.weisj.darklaf.platform.DecorationsHandler;

import javax.swing.*;
import javax.swing.plaf.UIResource;

public class DarkRootPaneBorder extends MutableLineBorder implements UIResource {

    public DarkRootPaneBorder() {
        super(UIManager.getInsets("RootPane.borderInsets"), UIManager.getColor("RootPane.borderColor"));
    }

    @Override
    public int getTop() {
        return DecorationsHandler.getSharedInstance().isCustomDecorationSupported()
               ? super.getTop() : 0;
    }
}
