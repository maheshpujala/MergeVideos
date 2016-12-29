package project.maheshpujala.com.mergevideos;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Environment;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;

import java.io.FileOutputStream;
import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements DownloadVideo.AsyncResponse,View.OnClickListener{
    int VIDEO_ONE = 1 , VIDEO_TWO = 2,CLOUD_VIDEO_ONE = 3,CLOUD_VIDEO_TWO = 4,videoOneWidth,videoOneHeight,videoTwoHeight,videoTwoWidth,firstCloudVideoHeight,firstCloudVideoWidth,secondCloudVideoHeight,secondCloudVideoWidth;

    private static final int REQUEST_WRITE_STORAGE = 112;
    String  videoOnePath,videoTwoPath,firstCloudVideoPath,secondCloudVideoPath,output,videoTwoType,videoOneType,firstCloudVideoType,secondCloudVideoType;
    int permissionCheck;
    Button appendButton,showVideoButton,addButton1,addButton2,appendCloudButton,addCloudButton,showCloudVideoButton;
    VideoView videoView,cloudVideoView;
     TextView progressText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressText= (TextView) findViewById(R.id.ProgressText);

        appendButton = (Button)findViewById(R.id.append );
        appendCloudButton = (Button)findViewById(R.id.appendVideoCloud );

        showVideoButton = (Button)findViewById(R.id.showMergedVideo);
        showCloudVideoButton = (Button)findViewById(R.id.showMergedVideoCloud);
        showVideoButton.setVisibility(View.GONE);
        showCloudVideoButton.setVisibility(View.GONE);

        videoView = (VideoView) findViewById(R.id.videoView);
        cloudVideoView = (VideoView) findViewById(R.id.videoViewCloud);
        videoView.setVisibility(View.GONE);
        cloudVideoView.setVisibility(View.GONE);

        addButton1 = (Button)findViewById(R.id.addVideo1);
        addButton2 = (Button)findViewById(R.id.addVideo2);
        addCloudButton = (Button)findViewById(R.id.addFirstVideoCloud);
        addCloudButton.setBackgroundColor(Color.GRAY);
        addButton1.setBackgroundColor(Color.GRAY);
        addButton2.setBackgroundColor(Color.GRAY);

        appendButton.setOnClickListener(this);
        addButton1.setOnClickListener(this);
        addButton2.setOnClickListener(this);
        showVideoButton.setOnClickListener(this);
        addCloudButton.setOnClickListener(this);
        appendCloudButton.setOnClickListener(this);
        showCloudVideoButton.setOnClickListener(this);
        setTabhost();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.addVideo1:
               addVideoMethod(VIDEO_ONE);
                break;

            case R.id.addVideo2:
                addVideoMethod(VIDEO_TWO);
                break;
            case R.id.append:
                if (videoOnePath == null || videoTwoPath == null) {
                    Toast.makeText(this, "Add Both Videos", Toast.LENGTH_LONG).show();
                } else {
                    if (videoOneHeight == videoTwoHeight) {
                        if (videoOneType.equals("video/mp4") && videoTwoType.equals("video/mp4")) {
                            String root = Environment.getExternalStorageDirectory().toString();
                            output = root + "/" + "Merged_Video.mp4";

                            addButton1.setText(getResources().getString(R.string.add_video_one));
                            addButton2.setText(getResources().getString(R.string.add_video_two));
                            addButton1.setBackgroundColor(Color.GRAY);
                            addButton2.setBackgroundColor(Color.GRAY);
                            if (mergeVideos(videoOnePath, videoTwoPath, output)) {
                                Toast.makeText(this, "Merged Video Output Path" + output, Toast.LENGTH_LONG).show();
                                showVideoButton.setVisibility(View.VISIBLE);
                            } else {
                                Toast.makeText(this, "Some Error Occurred.Try Again", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            addButton1.setText(getResources().getString(R.string.add_video_one));
                            addButton2.setText(getResources().getString(R.string.add_video_two));
                            addButton1.setBackgroundColor(Color.GRAY);
                            addButton2.setBackgroundColor(Color.GRAY);
                            Toast.makeText(this, "Make sure both videos are of file type mp4", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        addButton1.setText(getResources().getString(R.string.add_video_one));
                        addButton2.setText(getResources().getString(R.string.add_video_two));
                        addButton1.setBackgroundColor(Color.GRAY);
                        addButton2.setBackgroundColor(Color.GRAY);
                        Toast.makeText(this, "Make sure both videos are of same resolution", Toast.LENGTH_LONG).show();
                    }
                }
                break;
            case R.id.showMergedVideo:
                videoView.setVisibility(View.VISIBLE);
                videoView.setVideoPath(output);
                videoView.start();
                break;
            case R.id.addFirstVideoCloud:
               addVideoMethod(CLOUD_VIDEO_ONE);
                break;
            case R.id.appendVideoCloud:
                if (firstCloudVideoPath == null) {
                    Toast.makeText(this, "Add first video for merging", Toast.LENGTH_LONG).show();
                } else if (secondCloudVideoPath == null) {
                    downloadVideoFromCloud();
                }else{
                    if (firstCloudVideoHeight == secondCloudVideoHeight) {
                        if (firstCloudVideoType.equals("video/mp4") && secondCloudVideoType.equals("video/mp4")) {
                            String root = Environment.getExternalStorageDirectory().toString();
                            output = root + "/" + "Merged_Video.mp4";

                            addCloudButton.setText(getResources().getString(R.string.add_correct_resolution_video));
                            addCloudButton.setBackgroundColor(Color.GRAY);
                            if (mergeVideos(firstCloudVideoPath,secondCloudVideoPath , output)) {
                                Toast.makeText(this, "Merged Video Output Path" + output, Toast.LENGTH_LONG).show();
                                showCloudVideoButton.setVisibility(View.VISIBLE);
                            } else {
                                Toast.makeText(this, "Some Error Occurred.Try Again", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            addCloudButton.setText(getResources().getString(R.string.add_correct_resolution_video));
                            addCloudButton.setBackgroundColor(Color.GRAY);
                            Toast.makeText(this, "Make sure both videos are of file type mp4", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        addCloudButton.setText(getResources().getString(R.string.add_correct_resolution_video));
                        addCloudButton.setBackgroundColor(Color.GRAY);
                        Toast.makeText(this, "Make sure both videos are of same resolution", Toast.LENGTH_LONG).show();
                    }
                }
                break;
            case R.id.showMergedVideoCloud:
               cloudVideoView.setVisibility(View.VISIBLE);
                cloudVideoView.setVideoPath(output);
                cloudVideoView.start();
                break;
        }
    }
    @Override
    public void processFinish(String outputFile){
        //Here you will receive the result fired from Async class
        //of onPostExecute(result) method.
        secondCloudVideoPath = outputFile;
        getVideoResolution(outputFile,CLOUD_VIDEO_TWO);
        secondCloudVideoType = getMimeType(outputFile);
        appendCloudButton.performClick();
    }
    private void downloadVideoFromCloud() {
        String cloudVideoUrl = "https://s3.amazonaws.com/ogrimar/spree/videos/2.mp4";
        DownloadVideo downloadTask = new DownloadVideo(this,getApplicationContext());
        downloadTask.setProgressText(progressText);
        downloadTask.execute(cloudVideoUrl);
    }

    private void addVideoMethod(int VideoSerialNO) {
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            checkStoragePermission();
        }else{
            addVideo(VideoSerialNO);
            showVideoButton.setVisibility(View.GONE);
            videoView.setVisibility(View.GONE);
        }
    }

    private void checkStoragePermission( ) {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE},
                REQUEST_WRITE_STORAGE);
    }

    private void addVideo(int videoNO) {
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, videoNO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == VIDEO_ONE && resultCode == RESULT_OK && null != data) {
            Uri selectedVideo = data.getData();
            String[] filePathColumn = {MediaStore.Video.Media.DATA};
            Cursor cursor = getContentResolver().query(selectedVideo,
                    filePathColumn, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
            }

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            videoOnePath = cursor.getString(columnIndex);
            cursor.close();
            addButton1.setText(getResources().getString(R.string.added_video_one));
            addButton1.setBackgroundColor(Color.GREEN);
            getVideoResolution(videoOnePath,VIDEO_ONE);
            videoOneType = getMimeType(videoOnePath);
        }else if (requestCode == VIDEO_TWO && resultCode == RESULT_OK && null != data) {
            Uri selectedVideo = data.getData();
            String[] filePathColumn = {MediaStore.Video.Media.DATA};

            Cursor cursor = getContentResolver().query(selectedVideo,
                    filePathColumn, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
            }

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            videoTwoPath = cursor.getString(columnIndex);
            cursor.close();
            addButton2.setText(getResources().getString(R.string.added_video_two));
            addButton2.setBackgroundColor(Color.GREEN);
            getVideoResolution(videoTwoPath,VIDEO_TWO);
            videoTwoType = getMimeType(videoTwoPath);
        }else if(requestCode == CLOUD_VIDEO_ONE && resultCode == RESULT_OK && null != data) {
            Uri selectedVideo = data.getData();
            String[] filePathColumn = {MediaStore.Video.Media.DATA};

            Cursor cursor = getContentResolver().query(selectedVideo,
                    filePathColumn, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
            }
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            firstCloudVideoPath = cursor.getString(columnIndex);
            cursor.close();
            addCloudButton.setText(getResources().getString(R.string.added_video_one));
            addCloudButton.setBackgroundColor(Color.GREEN);
            getVideoResolution(firstCloudVideoPath,CLOUD_VIDEO_ONE);
           firstCloudVideoType = getMimeType(firstCloudVideoPath);
        }
    }

    private void getVideoResolution(String videoPath,int videoSNo) {
        MediaMetadataRetriever retriever = new  MediaMetadataRetriever();
        Bitmap bmp = null;
        retriever.setDataSource(videoPath);
        bmp = retriever.getFrameAtTime();
        if(videoSNo == VIDEO_ONE){
            videoOneHeight=bmp.getHeight();
            videoOneWidth=bmp.getWidth();
        }else if (videoSNo == VIDEO_TWO){
            videoTwoHeight=bmp.getHeight();
            videoTwoWidth=bmp.getWidth();
        }else if (videoSNo == CLOUD_VIDEO_ONE){
            firstCloudVideoHeight=bmp.getHeight();
            firstCloudVideoWidth=bmp.getWidth();
        }else if(videoSNo == CLOUD_VIDEO_TWO){
            secondCloudVideoHeight=bmp.getHeight();
            secondCloudVideoWidth=bmp.getWidth();
        }
    }
    public static String getMimeType(String path) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(path);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }

    public boolean mergeVideos(String videoFile, String videoFileTwo, String outputFile) {
        try {
            Movie[] inMovies = new Movie[]{
                    MovieCreator.build(videoFile),
                    MovieCreator.build(videoFileTwo)};
            List<Track> videoTracks = new LinkedList<Track>();
            List<Track> audioTracks = new LinkedList<Track>();
            for (Movie m : inMovies) {
                for (Track t : m.getTracks()) {
                    if (t.getHandler().equals("vide")) {
                        videoTracks.add(t);
                    }
                    if (t.getHandler().equals("soun")) {
                        audioTracks.add(t);
                    }
                }
            }
            Movie result = new Movie();
            if (videoTracks.size() > 0) {
                result.addTrack(new AppendTrack(videoTracks.toArray(new Track[videoTracks.size()])));
            }
            if (audioTracks.size() > 0) {
                result.addTrack(new AppendTrack(audioTracks.toArray(new Track[audioTracks.size()])));
            }
            Container out = new DefaultMp4Builder().build(result);
            FileOutputStream fos = new FileOutputStream(outputFile);
            out.writeContainer(fos.getChannel());
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
    @Override
    public void onResume(){
        permissionCheck = ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        super.onResume();
    }

    private void setTabhost() {
        TabHost host = (TabHost)findViewById(R.id.tabhost);
        host.setup();
        //Tab 1
        TabHost.TabSpec spec = host.newTabSpec("Tab One");
        spec.setContent(R.id.local);
        spec.setIndicator("Local Files");
        host.addTab(spec);
        //Tab 2
        spec = host.newTabSpec("Tab Two");
        spec.setContent(R.id.cloud);
        spec.setIndicator("Cloud Files");
        host.addTab(spec);
    }
}