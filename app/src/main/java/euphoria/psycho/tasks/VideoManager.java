package euphoria.psycho.tasks;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import euphoria.psycho.explorer.App;
import euphoria.psycho.share.Logger;

public class VideoManager implements VideoTaskListener {

    private static WeakReference<VideoManager> sVideoManager;
    private final VideoTaskDatabase mDatabase;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private RequestQueue mQueue;
    private final List<VideoTaskListener> mVideoTaskListeners = new ArrayList<>();

    public VideoManager(Context context) {
        mDatabase = VideoTaskDatabase.getInstance(context);
    }


    public void addVideoTaskListener(VideoTaskListener taskListener) {
        Logger.e(String.format("addVideoTaskListener, %s", taskListener.getClass().getSimpleName()));
        mVideoTaskListeners.add(taskListener);
    }

    public VideoTaskDatabase getDatabase() {
        return mDatabase;
    }

    public Handler getHandler() {
        return mHandler;
    }

    public static VideoManager getInstance() {
        if (sVideoManager.get() == null) {
            sVideoManager = new WeakReference<>(new VideoManager(App.getContext()));
        }
        return sVideoManager.get();
    }

    public RequestQueue getQueue() {
        if (mQueue == null) {
            mQueue = new RequestQueue();
            mQueue.start();
        }
        return mQueue;
    }


    public static VideoManager newInstance(Context context) {
        if (sVideoManager == null || sVideoManager.get() == null) {
            sVideoManager = new WeakReference<>(new VideoManager(context));
        }
        return sVideoManager.get();
    }


    public void removeVideoTaskListener(VideoTaskListener videoTaskListener) {
        mVideoTaskListeners.remove(videoTaskListener);
    }

    @Override
    public void synchronizeTask(VideoTask videoTask) {
        mDatabase.updateVideoTask(videoTask);
        mVideoTaskListeners.forEach(videoTaskListener -> videoTaskListener.synchronizeTask(videoTask));
        if (videoTask.Status < 0 || videoTask.Status == TaskStatus.MERGE_VIDEO_FINISHED) {
            videoTask.Request.finish();
        }
    }

    @Override
    public void taskProgress(VideoTask videoTask) {
        mVideoTaskListeners.forEach(videoTaskListener -> videoTaskListener.taskProgress(videoTask));
    }


    public static void post(Runnable runnable) {
        getInstance().getHandler().post(runnable);
    }


}
