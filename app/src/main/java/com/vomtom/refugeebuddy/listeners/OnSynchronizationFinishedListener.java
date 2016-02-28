package com.vomtom.refugeebuddy.listeners;

public interface OnSynchronizationFinishedListener {
    void onFinished(String id);
    void onError(String id);
    void onFinished();
}