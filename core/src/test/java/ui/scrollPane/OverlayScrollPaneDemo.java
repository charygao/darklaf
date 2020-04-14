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
package ui.scrollPane;

import com.github.weisj.darklaf.components.OverlayScrollPane;
import com.github.weisj.darklaf.util.StringUtil;
import ui.ComponentDemo;
import ui.DemoPanel;
import ui.DemoResources;

import javax.swing.*;
import java.awt.*;

public class OverlayScrollPaneDemo implements ComponentDemo {

    public static void main(final String[] args) {
        ComponentDemo.showDemo(new OverlayScrollPaneDemo());
    }

    @Override
    public JComponent createComponent() {
        OverlayScrollPane scrollPane = new OverlayScrollPane(
            new JTextArea(StringUtil.repeat(DemoResources.LOREM_IPSUM, 5)));
        return new DemoPanel(scrollPane, new BorderLayout(), 0);
    }

    @Override
    public String getTitle() {
        return "OverlayScrollPane Demo";
    }
}
