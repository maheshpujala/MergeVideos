package project.maheshpujala.com.mergevideos;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.TextView;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by maheshpujala on 29/12/16.
 */

public class DownloadVideo extends AsyncTask<String, Integer, String>{
    int TIMEOUT_CONNECTION = 20000,TIMEOUT_SOCKET=10000;
    private AsyncResponse delegate = null;
    Context context;
    TextView textPercentage;

    public void setProgressText(TextView text) {
        this.textPercentage = text;
    }

    interface AsyncResponse {
        void processFinish(String outputFile);
    }

    DownloadVideo(AsyncResponse delegate, Context context){
        this.delegate = delegate;
        this.context = context;
    }
    @Override
    protected void onPreExecute() {
        textPercentage.setText("Processing: 0%");
        textPercentage.setVisibility(View.VISIBLE);
    }

    @Override
    protected String doInBackground(String... urls) {
        String tempFilePath = null;
        for (String url : urls) {
            try {

                tempFilePath = saveVideoToTemp(url);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return tempFilePath;
    }
    @Override
    protected void onProgressUpdate(Integer... values) {
        if (this.textPercentage != null) {
            textPercentage.setText("Processing: " + values[0] + "%");
        }
    }

    @Override
    protected void onPostExecute(String result) {
        delegate.processFinish(result);
        textPercentage.setVisibility(View.GONE);
    }

    private String saveVideoToTemp(String video) throws IOException {
        URL url = new URL(video);
        int contentLength;
        double sumCount=0;
        String name = video.substring(video.lastIndexOf("/") + 1);
        File outputCacheDir = context.getCacheDir();
        String CacheDir = outputCacheDir+"/"+name;
        // Open a connection to that URL
        URLConnection ucon = url.openConnection();
        contentLength = ucon.getContentLength();
        // this timeout affects how long it takes for the app to realize there's a connection problem
        ucon.setReadTimeout(TIMEOUT_CONNECTION);
        ucon.setConnectTimeout(TIMEOUT_SOCKET);
        // Define InputStreams to read from the URLConnection.
        // uses 3KB download buffer
        InputStream is = ucon.getInputStream();
        BufferedInputStream inStream = new BufferedInputStream(is, 1024 * 5);
        FileOutputStream outStream = new FileOutputStream(CacheDir);
        byte[] buff = new byte[5 * 1024];
        int len;
        while ((len = inStream.read(buff)) != -1) {
            outStream.write(buff, 0, len);
            sumCount += len;
            if (contentLength > 0) {
                publishProgress((int)(sumCount / contentLength * 100));
            }
        }
        outStream.flush();
        outStream.close();
        inStream.close();
        return CacheDir;
    }
}