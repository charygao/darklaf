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
package com.github.weisj.darklaf.platform.macos;

import com.github.weisj.darklaf.platform.NativeUtil;
import com.github.weisj.darklaf.util.SystemInfo;

import java.util.logging.Level;
import java.util.logging.Logger;

public class MacOSLibrary {

    private static final Logger LOGGER = Logger.getLogger(MacOSLibrary.class.getName());
    private static boolean loaded;
    private static boolean attemptedLoad;

    /**
     * Load the decorations-library if necessary.
     */
    public static void updateLibrary() {
        if (!loaded && !attemptedLoad) {
            loadLibrary();
        }
    }

    private static void loadLibrary() {
        attemptedLoad = true;
        if (!SystemInfo.isMac || loaded) {
            return;
        }
        try {
            if (SystemInfo.isX64) {
                NativeUtil.loadLibraryFromJar(
                    "/com/github/weisj/darklaf/platform/darklaf-macos/macos-x86-64/libdarklaf-macos.dylib");
                loaded = true;
                LOGGER.info("Loaded libdarklaf-macos.dylib. Native features are enabled.");
            } else {
                LOGGER.warning("JRE model '"
                               + SystemInfo.jreArchitecture
                               + "' not supported. Native features will be disabled");
            }
        } catch (Throwable e) {
            //Library not found, SecurityManager prevents library loading etc.
            LOGGER.log(Level.SEVERE, "Could not load decorations library libdarklaf-macos.dylib." +
                                     " Native features will be disabled", e);
        }
    }

    public static boolean isLoaded() {
        return loaded;
    }
}
