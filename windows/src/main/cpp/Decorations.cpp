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
#include "Decorations.h"
#include "com_github_weisj_darklaf_platform_windows_JNIDecorationsWindows.h"
#include <dwmapi.h>
#include <map>
#include <iostream>
#include <winuser.h>

std::map<HWND, WindowWrapper *> wrapper_map = std::map<HWND, WindowWrapper *>();

LRESULT HitTestNCA(HWND hWnd, WPARAM wParam, LPARAM lParam, WindowWrapper *wrapper)
{
    if (wrapper->popup_menu) return HTCLIENT;
    // Get the point coordinates for the hit test.
    POINT ptMouse = {GET_X_LPARAM(lParam), GET_Y_LPARAM(lParam)};

    // Get the window rectangle.
    RECT rcWindow;
    GetWindowRect(hWnd, &rcWindow);

    // Determine if the hit test is for resizing. Default middle (1,1).
    USHORT uRow = 1;
    USHORT uCol = 1;

    // Determine if the point is at the top or bottom of the window.
    if (ptMouse.y >= rcWindow.top && ptMouse.y < rcWindow.top + 5)
    {
        uRow = 0;
    }
    else if (ptMouse.y < rcWindow.bottom && ptMouse.y >= rcWindow.bottom - 5)
    {
        uRow = 2;
    }

    // Determine if the point is at the left or right of the window.
    if (ptMouse.x >= rcWindow.left && ptMouse.x < rcWindow.left + 5)
    {
        uCol = 0; // left side
    }
    else if (ptMouse.x < rcWindow.right && ptMouse.x >= rcWindow.right - 5)
    {
        uCol = 2; // right side
    }

    // Hit test (HTTOPLEFT, ... HTBOTTOMRIGHT)
    LRESULT hitTests[3][3] = {
        {HTTOPLEFT, HTTOP, HTTOPRIGHT},
        {HTLEFT, HTNOWHERE, HTRIGHT},
        {HTBOTTOMLEFT, HTBOTTOM, HTBOTTOMRIGHT},
    };
    LRESULT hit = hitTests[uRow][uCol];
    if (hit == HTNOWHERE || !wrapper->resizable)
    {
        //Handle window drag.
        if (ptMouse.y < rcWindow.top + wrapper->height
            && ptMouse.x >= rcWindow.left + wrapper->left
            && ptMouse.x <= rcWindow.right - wrapper->right)
        {
            return HTCAPTION;
        }
        return HTCLIENT;
    }
    else
    {
        return hit;
    }
}

bool Maximized(HWND hwnd)
{
    WINDOWPLACEMENT placement;
    if (!GetWindowPlacement(hwnd, &placement)) return false;
    return placement.showCmd == SW_MAXIMIZE;
}

void AdjustMaximizedClientArea(HWND window, RECT& rect)
{
    if (!Maximized(window)) return;

    auto monitor = MonitorFromWindow(window, MONITOR_DEFAULTTONULL);
    if (!monitor) return;

    MONITORINFO monitor_info{};
    monitor_info.cbSize = sizeof(monitor_info);
    if (!GetMonitorInfoW(monitor, &monitor_info)) return;

    rect = monitor_info.rcWork;
}

void AdjustMinMaxInfo(HWND hwnd, LPARAM lParam)
{
    HMONITOR hPrimaryMonitor = MonitorFromWindow(nullptr, MONITOR_DEFAULTTOPRIMARY);
    HMONITOR hTargetMonitor = MonitorFromWindow(hwnd, MONITOR_DEFAULTTONEAREST);

    MONITORINFO primaryMonitorInfo{sizeof(MONITORINFO)};
    MONITORINFO targetMonitorInfo{sizeof(MONITORINFO)};

    GetMonitorInfo(hPrimaryMonitor, &primaryMonitorInfo);
    GetMonitorInfo(hTargetMonitor, &targetMonitorInfo);

    MINMAXINFO *min_max_info = reinterpret_cast<MINMAXINFO *>(lParam);
    RECT max_rect = primaryMonitorInfo.rcWork;
    RECT target_rect = targetMonitorInfo.rcWork;
    min_max_info->ptMaxSize.x = target_rect.right - target_rect.left;
    min_max_info->ptMaxSize.y = target_rect.bottom - target_rect.top;
    min_max_info->ptMaxPosition.x = max_rect.left;
    min_max_info->ptMaxPosition.y = max_rect.top;
}

void PaintBackground(HWND hwnd, WPARAM wParam, WindowWrapper *wrapper)
{
    HDC hdc = reinterpret_cast<HDC>(wParam);
    RECT clientRect;
    GetClientRect(hwnd, &clientRect);
    FillRect(hdc, &clientRect, wrapper->bgBrush);
}

LRESULT CALLBACK WindowWrapper::WindowProc(_In_ HWND hwnd, _In_ UINT uMsg, _In_ WPARAM wParam, _In_ LPARAM lParam)
{
    HWND handle = reinterpret_cast<HWND>(hwnd);
    auto wrapper = wrapper_map[handle];

    if (uMsg == WM_NCACTIVATE)
    {
        return TRUE;
    }
    else if (uMsg == WM_NCCALCSIZE)
    {
        if (wParam == TRUE) {
            NCCALCSIZE_PARAMS& params = *reinterpret_cast<NCCALCSIZE_PARAMS*>(lParam);
            AdjustMaximizedClientArea(handle, params.rgrc[0]);
            return TRUE;
        }
    }
    else if (uMsg == WM_GETMINMAXINFO)
    {
        AdjustMinMaxInfo(hwnd, lParam);
        return FALSE;
    }
    else if (uMsg == WM_NCHITTEST)
    {
        return HitTestNCA(hwnd, wParam, lParam, wrapper);
    }
    else if (uMsg == WM_MOVE)
    {
        wrapper->moving = wrapper->move_mode;
    }
    else if (uMsg == WM_ENTERSIZEMOVE)
    {
        wrapper->move_mode = TRUE;
    }
    else if (uMsg == WM_EXITSIZEMOVE)
    {
        wrapper->moving = FALSE;
        wrapper->move_mode = FALSE;
    }
    else if ((uMsg == WM_PAINT || uMsg == WM_ERASEBKGND) && wrapper->bgBrush)
    {
        if (!wrapper->moving) PaintBackground(hwnd, wParam, wrapper);
        if (uMsg == WM_ERASEBKGND) return TRUE;
    }

    return CallWindowProc(wrapper->prev_proc, hwnd, uMsg, wParam, lParam);
}

JNIEXPORT void JNICALL
Java_com_github_weisj_darklaf_platform_windows_JNIDecorationsWindows_setResizable(JNIEnv *env, jclass obj, jlong hwnd, jboolean res)
{
    HWND handle = reinterpret_cast<HWND>(hwnd);
    auto wrap = wrapper_map[handle];
    if (wrap)
    {
        wrap->resizable = res;
    }
}

JNIEXPORT void JNICALL
Java_com_github_weisj_darklaf_platform_windows_JNIDecorationsWindows_updateValues(JNIEnv *env, jclass obj, jlong hwnd,
                                                                                  jint l, jint r, jint h)
{
    HWND handle = reinterpret_cast<HWND>(hwnd);
    auto wrap = wrapper_map[handle];
    if (wrap)
    {
        wrap->left = l;
        wrap->right = r;
        wrap->height = h;
    }
}

JNIEXPORT void JNICALL
Java_com_github_weisj_darklaf_platform_windows_JNIDecorationsWindows_setBackground(JNIEnv *env, jclass obj, jlong hwnd, jint r, jint g, jint b)
{
    HWND handle = reinterpret_cast<HWND>(hwnd);
    auto wrap = wrapper_map[handle];
    if (wrap)
    {
        wrap->bgBrush = CreateSolidBrush(RGB(r, g, b));
    }
}

void ExtendClientFrame(HWND handle)
{
    MARGINS margins = {1, 1, 1, 1};
    DwmExtendFrameIntoClientArea(handle, &margins);
}

void SetupWindowStyle(HWND handle)
{
    auto style = GetWindowLongPtr(handle, GWL_STYLE);
    SetWindowLongPtr(handle, GWL_STYLE, (style | WS_THICKFRAME));
}

bool InstallDecorations(HWND handle, bool is_popup)
{
    //Prevent multiple installations overriding the real window procedure.
    auto it = wrapper_map.find(handle);
    if (it != wrapper_map.end()) return false;

    SetupWindowStyle(handle);
    ExtendClientFrame(handle);

    WNDPROC proc = reinterpret_cast<WNDPROC>(GetWindowLongPtr(handle, GWLP_WNDPROC));

    WindowWrapper *wrapper = new WindowWrapper();
    wrapper->prev_proc = proc;
    wrapper->popup_menu = is_popup;
    wrapper_map[handle] = wrapper;
    SetWindowLongPtr(handle, GWLP_WNDPROC, (LONG_PTR)WindowWrapper::WindowProc);
    UINT flags = SWP_NOZORDER | SWP_NOOWNERZORDER | SWP_NOMOVE | SWP_NOSIZE | SWP_FRAMECHANGED;
    SetWindowPos(handle, NULL, 0, 0, 0, 0, flags);
    return true;
}

JNIEXPORT jboolean JNICALL
Java_com_github_weisj_darklaf_platform_windows_JNIDecorationsWindows_installDecorations(JNIEnv *env, jclass obj, jlong hwnd)
{
    HWND handle = reinterpret_cast<HWND>(hwnd);
    return (jboolean) InstallDecorations(handle, false);
}

JNIEXPORT void JNICALL
Java_com_github_weisj_darklaf_platform_windows_JNIDecorationsWindows_uninstallDecorations(JNIEnv *env, jclass obj, jlong hwnd)
{
    HWND handle = reinterpret_cast<HWND>(hwnd);
    auto wrap = wrapper_map[handle];
    if (wrap)
    {
        SetWindowLongPtr(handle, GWLP_WNDPROC, reinterpret_cast<LONG_PTR>(wrap->prev_proc));
        wrapper_map.erase(handle);
        delete (wrap);
    }
}

JNIEXPORT jboolean JNICALL
Java_com_github_weisj_darklaf_platform_windows_JNIDecorationsWindows_installPopupMenuDecorations(JNIEnv *env, jclass obj, jlong hwnd)
{
    HWND handle = reinterpret_cast<HWND>(hwnd);
    return (jboolean) InstallDecorations(handle, true);
}

//Window functions.

JNIEXPORT void JNICALL
Java_com_github_weisj_darklaf_platform_windows_JNIDecorationsWindows_minimize(JNIEnv *env, jclass obj, jlong hwnd)
{
    HWND handle = reinterpret_cast<HWND>(hwnd);
    ShowWindow(handle, SW_MINIMIZE);
}

JNIEXPORT void JNICALL
Java_com_github_weisj_darklaf_platform_windows_JNIDecorationsWindows_maximize(JNIEnv *env, jclass obj, jlong hwnd)
{
    HWND handle = reinterpret_cast<HWND>(hwnd);
    ShowWindow(handle, SW_MAXIMIZE);
}

JNIEXPORT void JNICALL
Java_com_github_weisj_darklaf_platform_windows_JNIDecorationsWindows_restore(JNIEnv *env, jclass obj, jlong hwnd)
{
    HWND handle = reinterpret_cast<HWND>(hwnd);
    ShowWindow(handle, SW_RESTORE);
}
