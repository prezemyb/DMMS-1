package com.libsensordroid;

import android.os.Binder;
import android.util.Log;

public interface IMainServiceConnection {
    public void putFrame(int id, int[] presentData, byte[] data)
}
