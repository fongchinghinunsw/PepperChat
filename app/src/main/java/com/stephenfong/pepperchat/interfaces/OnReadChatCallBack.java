package com.stephenfong.pepperchat.interfaces;

import com.stephenfong.pepperchat.model.chat.Chat;

import java.util.List;

public interface OnReadChatCallBack {
    void onReadSuccess(List<Chat> list);
    void onReadFailed();
}
