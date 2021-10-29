package euphoria.psycho.bilibili;

import android.app.Notification.Builder;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Process;
import android.os.SystemClock;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.annotation.Nullable;

public class BilibiliService extends Service {

    public static final String BILIBILI_CHANNEL = "Bilibili";
    private NotificationManager mNotificationManager;
    private ExecutorService mExecutorService;
    private int mStartId;
    private Handler mHandler = new Handler();
    private BilibiliDatabase mBilibiliDatabase;

    private Builder getBuilder() {
        Builder builder;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            builder = new Builder(getApplicationContext(), BILIBILI_CHANNEL);
        } else {
            builder = new Builder(getApplicationContext());
        }
        return builder;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("B5aOx2", String.format("onCreate, %s", ""));
        mNotificationManager = getSystemService(NotificationManager.class);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    BILIBILI_CHANNEL,
                    "Bilibili", NotificationManager.IMPORTANCE_LOW
            );
            mNotificationManager.createNotificationChannel(channel);
        }
        mExecutorService = Executors.newSingleThreadExecutor();
        mStartId = hashCode();
        mBilibiliDatabase = new BilibiliDatabase(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        List<BilibiliTask> bilibiliTasks = mBilibiliDatabase.queryUnfinishedTasks();
        for (BilibiliTask bilibiliTask : bilibiliTasks) {
            mExecutorService.submit(new DownloadThread(bilibiliTask));
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private class DownloadThread implements Runnable {
        private final BilibiliTask mBilibiliTask;

        private DownloadThread(BilibiliTask bilibiliTask) {
            mBilibiliTask = bilibiliTask;
        }

        private void notify(String title, String content) {
            mHandler.post(() -> {
                Builder builder = getBuilder();
                builder.setSmallIcon(android.R.drawable.stat_sys_download)
                        .setContentTitle(title)
                        .setContentText(content)
                        .setWhen(System.currentTimeMillis())
                        .setShowWhen(true)
                        .setOngoing(true);
                mNotificationManager.notify(mStartId, builder.build());
            });
        }

        private void notifyFailed(String title, String content) {
            mHandler.post(() -> {
                Builder builder = getBuilder();
                builder.setSmallIcon(android.R.drawable.stat_sys_download)
                        .setContentTitle(title)
                        .setContentText(content)
                        .setWhen(System.currentTimeMillis())
                        .setShowWhen(true)
                ;
                mNotificationManager.notify(mStartId, builder.build());
            });
        }

        private void notifyProgress(String title, String content, int progress) {
            mHandler.post(() -> {
                Builder builder = getBuilder();
                builder.setSmallIcon(android.R.drawable.stat_sys_download)
                        .setContentTitle(title)
                        .setContentText(content)
                        .setWhen(System.currentTimeMillis())
                        .setShowWhen(true)
                        .setOngoing(true)
                        .setProgress(100, progress, false);
                mNotificationManager.notify(mStartId, builder.build());
            });
        }

        @Override
        public void run() {
            android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            notify("准备下载B站视频", mBilibiliTask.Url);
            URL url;
            try {
                url = new URL(mBilibiliTask.BilibiliThreads[0].Url);
                HttpURLConnection c = (HttpURLConnection) url.openConnection();
                c.setRequestProperty("Referer", "https://www.bilibili.com/");
                c.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/94.0.4606.71 Safari/537.36");
                File videoFile = new File(mBilibiliTask.BilibiliThreads[0].Filename);
                if (videoFile.exists()) {
                    c.setRequestProperty("Range", "bytes=" + videoFile.length() + "-");
                }
                int status = c.getResponseCode();
                if (status == 416) {
                    return;
                }
                if (status != 200 && status != 206) {
                    notifyFailed("下载失败", String.format("服务器返回代码%s", status));
                    mBilibiliTask.Status = BilibiliStatus.ERROR_STATUS_CODE;
                    mBilibiliDatabase.updateBilibiliTask(mBilibiliTask);
                    return;
                }
                notify("开始下载B站视频", mBilibiliTask.Url);
                RandomAccessFile out = new RandomAccessFile(videoFile, "rw");
                long currentBytes = 0;
                long totalBytes = c.getContentLengthLong();
                if (videoFile.exists()) {
                    currentBytes = (int) videoFile.length();
                    totalBytes += currentBytes;
                    out.seek(currentBytes);
                }
                InputStream in = null;
                try {
                    try {
                        in = c.getInputStream();
                    } catch (IOException e) {
                        Log.e("B5aOx2", String.format("run, %s", e.getMessage()));
                        notify("下载失败", mBilibiliTask.Url);
                        return;
                    }
                    final byte[] buffer = new byte[8192];
                    long speedSampleStart = 0;
                    long speedSampleBytes = 0;
                    long speed = 0;
                    while (true) {
//
//                        if (mShutdownRequested) {
//                            throw new StopRequestException(STATUS_HTTP_DATA_ERROR,
//                                    "Local halt requested; job probably timed out");
//                        }
                        int len = -1;
                        try {
                            len = in.read(buffer);
                        } catch (IOException e) {
                            notify("下载失败", mBilibiliTask.Url);
                            return;
                        }
                        if (len == -1) {
                            break;
                        }
                        try {
                            out.write(buffer, 0, len);
                            currentBytes += len;
                            final long now = SystemClock.elapsedRealtime();
                            final long sampleDelta = now - speedSampleStart;
                            if (sampleDelta > 500) {
                                final long sampleSpeed = ((currentBytes - speedSampleBytes) * 1000)
                                        / sampleDelta;
                                if (speed == 0) {
                                    speed = sampleSpeed;
                                } else {
                                    speed = ((speed * 3) + sampleSpeed) / 4;
                                }
                                if (speedSampleStart != 0) {
                                    final int percent = (int) ((currentBytes * 100) / totalBytes);
                                    final long remainingMillis = ((totalBytes - currentBytes) * 1000) / speed;
                                    notifyProgress("正在下载" + mBilibiliTask.Filename,
                                            "剩余时间" + BilibiliUtils.formatDuration(remainingMillis), percent);
                                }
                                speedSampleStart = now;
                                speedSampleBytes = currentBytes;
                            }
                        } catch (IOException e) {
                            notify("下载失败", mBilibiliTask.Url);
                        }
                    }

                } finally {
                    try {
                        out.close();
                    } catch (IOException ignored) {
                    }
                }
            } catch (Exception e) {
                Log.e("B5aOx2", String.format("run, %s", e.getMessage()));
                notify("下载失败", mBilibiliTask.Url);
            }
        }
    }
}
