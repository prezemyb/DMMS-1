// IMainServiceConnection.aidl
package com.libsensordroid;

interface IMainServiceConnection {
    void putFrame(in int id, in int[] presentData, in byte[] data);
    void putJson(in String json);
    void regMetadata(in int id, in int numData);
}
