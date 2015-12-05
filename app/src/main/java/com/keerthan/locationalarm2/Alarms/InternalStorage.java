package com.keerthan.locationalarm2.Alarms;

import android.content.Context;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public final class InternalStorage{

    //Note, memory based methods are notoriously error prone, hence the error handling

    public static void writeObject(Context context, String FileKey, Object object) throws IOException {
        FileOutputStream fos = context.openFileOutput(FileKey, Context.MODE_PRIVATE);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(object);
        oos.close();
        fos.close();
    }

    public static Object readObject(Context context, String FileKey) throws IOException,
            ClassNotFoundException {
        FileInputStream fis = context.openFileInput(FileKey); //This FileKey is simply like a unique ID to specify which file is being opened
        ObjectInputStream ois = new ObjectInputStream(fis);
        Object object = ois.readObject();
        return object;
    }
}
