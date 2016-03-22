package com.bitalino.sensordroid.Handlers;

import android.os.RemoteException;

//import com.bitalino.comm.BITalinoFrame;
import com.bitalino.sensordroid.util.BitalinoTransfer;
import com.sensordroid.IMainServiceConnection;
import com.bitalino.sensordroid.util.BITalinoFrame;
import com.bitalino.sensordroid.util.JSONHelper;

public class FrameHandler implements Runnable {
    private final IMainServiceConnection binder;
    private final BITalinoFrame frame;
    private final int id;
    private final int[] typeList;
    private final int[] channelList;

    public FrameHandler(final IMainServiceConnection binder, final BITalinoFrame frame, 
                        final int id, final int[] typeList, final int[] channelList){
        this.id = id;
        this.binder = binder;
        this.frame = frame;
        this.typeList = typeList;
        this.channelList = channelList;
    }

    @Override
    public void run() {
        try {
            Double[] data = new Double[channelList.length];
            int index = 0;
            for (int type : typeList){
                if (index >= channelList.length)
                    break;

                // Two last channels got 6 bit resolution
                int res = 10;
                if(index >= 4){
                    res = 6;
                }

                switch (type) {
                    case BitalinoTransfer.TYPE_RAW:
                        data[index] = Double.valueOf(frame.getAnalog(channelList[index++]));
                        break;
                    case BitalinoTransfer.TYPE_LUX:
                        data[index] = BitalinoTransfer.scaleLUX(frame.getAnalog(channelList[index++]), res);
                        break;
                    case BitalinoTransfer.TYPE_ACC:
                        data[index] = BitalinoTransfer.scaleACC(frame.getAnalog(channelList[index++]));
                        break;
                    case BitalinoTransfer.TYPE_PZT:
                        data[index] = BitalinoTransfer.scalePZT(frame.getAnalog(channelList[index++]), res);
                        break;
                    case BitalinoTransfer.TYPE_ECG:
                        data[index] = BitalinoTransfer.scaleECG(frame.getAnalog(channelList[index++]), res);
                        break;
                    case BitalinoTransfer.TYPE_EEG:
                        data[index] = BitalinoTransfer.scaleEEG(frame.getAnalog(channelList[index++]), res);
                        break;
                    case BitalinoTransfer.TYPE_EDA:
                        data[index] = BitalinoTransfer.scaleEDA(frame.getAnalog(channelList[index++]), res);
                        break;
                    case BitalinoTransfer.TYPE_EMG:
                        data[index] = BitalinoTransfer.scaleEMG(frame.getAnalog(channelList[index++]), res);
                        break;
                    case BitalinoTransfer.TYPE_TMP:
                        data[index] = BitalinoTransfer.scaleTMP(frame.getAnalog(channelList[index++]), res, true);
                        break;
                }
            }

            // Construct and send JSON-string
            String sendString = JSONHelper.construct(id, channelList, data).toString();
            binder.putJson(sendString);
        } catch (RemoteException re) {
            re.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
