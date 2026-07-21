package com.useinsider.kotlindemo.model

/** UI status of a single App Frames placement, driven by the SDK view listener callbacks. */
public sealed interface FrameStatus {
    public object Idle : FrameStatus
    public object Loading : FrameStatus
    public object Loaded : FrameStatus
    public object HeightUpdated : FrameStatus
    public object ActionTriggered : FrameStatus
    public object Dismissed : FrameStatus
    public data class Failed(val message: String) : FrameStatus
}
