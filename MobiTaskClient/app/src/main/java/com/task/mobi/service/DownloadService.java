package com.task.mobi.service;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;


/**
 * Created by neeraj on 17/5/18.
 */
public class DownloadService extends IntentService {
    public static final int UPDATE_PROGRESS = 8344;
    public static boolean isRunning = false;

    public DownloadService() {
        super("DownloadService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        isRunning = true;
        String urlToDownload = intent.getStringExtra("url");
        int id = Integer.parseInt(intent.getStringExtra("id"));
        String name = urlToDownload.substring(urlToDownload.lastIndexOf("/") + 1);
        File mediaFile;
        try {
            URL url = new URL(urlToDownload);
            URLConnection connection = url.openConnection();
            connection.connect();
            // this will be useful so that you can show a typical 0-100% progress bar
            int fileLength = connection.getContentLength();
            Log.e("Download Service", "Downloading file of length " + fileLength + " url " + urlToDownload);

            File mediaStorageDir = new File(this.getFilesDir() + File.separator + "audios");

            if (!mediaStorageDir.exists()) {
                mediaStorageDir.mkdirs();
            }
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + name);
            if (mediaFile.exists() && mediaFile.length() > 0) {
                if (mediaFile.length() == fileLength) {
                    Intent intent1 = new Intent("FileDownloaded");
                    intent1.putExtra("id", id);
                    intent1.putExtra("path", mediaFile.getPath());
                    LocalBroadcastManager.getInstance(this).sendBroadcast(intent1);
                    Log.e("Download Service", "File already exist");
                    return;
                } else {
                    Log.e("Download Service", "Deleted file " + mediaFile.length() + " < " + fileLength);
                    mediaFile.delete();
                    mediaFile = new File(mediaStorageDir.getPath() + File.separator + name);
                }
            }
            // download the file
            InputStream input = new BufferedInputStream(connection.getInputStream());
            OutputStream output = new FileOutputStream(mediaFile);

            byte data[] = new byte[1024];
            long total = 0;
            int count;
            while ((count = input.read(data)) != -1) {
                total += count;
                Intent intent1 = new Intent("FileDownloaded");
                intent1.putExtra("progress", (int) (total * 100 / fileLength));
                intent1.putExtra("id", id);
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent1);
                output.write(data, 0, count);
            }

            output.flush();
            output.close();
            input.close();
            Intent intent1 = new Intent("FileDownloaded");
            intent1.putExtra("id", id);
            intent1.putExtra("path", mediaFile.getPath());
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent1);
        } catch (FileNotFoundException e) {
            Intent intent1 = new Intent("FileDownloaded");
            intent1.putExtra("fail", true);
            intent1.putExtra("id", id);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent1);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            isRunning = false;
        }
    }
}