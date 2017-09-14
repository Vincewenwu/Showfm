package co.bankoo.zuweie.showfm.ctrl;

import android.app.DownloadManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.TypedValue;

import org.greenrobot.greendao.database.Database;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import cn.trinea.android.common.service.impl.ImageCache;
import cn.trinea.android.common.util.DownloadManagerPro;
import cn.trinea.android.common.util.ImageCacheManager;
import cn.trinea.android.common.util.SystemUtils;
import co.bankoo.zuweie.showfm.model.DaoMaster;
import co.bankoo.zuweie.showfm.model.DaoSession;
import co.bankoo.zuweie.showfm.model.Konst;

public class Tool {
    // ImageCache 使用 单例 模式。只要一个对象够了。
    public static ImageCache getImageCache (Context context) {
        if (m_imageCache == null) {
            m_imageCache = ImageCacheManager.getImageCache();
            m_imageCache.setCacheFolder(context.getExternalCacheDir().getAbsolutePath());
        }
        return m_imageCache;
    }
    public static float dip2px(Context context, float dip) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, context.getResources().getDisplayMetrics());
    }

    public static String getRecordUrl(String novelUrl, String recordUrl, int expire){
        try {
            String down = "/downloads/" + novelUrl + "/" + recordUrl;
            long etime = System.currentTimeMillis() / 1000 + expire;
            String sign = md5(Konst.RESTOKEN + "&" + etime + "&" + down);
            sign = sign.substring(12, +20) + etime;
            return Konst.RESHOST + "/downloads/" + novelUrl + "/" + Uri.encode(recordUrl) + "?_upt=" + sign;
        }catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String md5(String text) throws NoSuchAlgorithmException {

        MessageDigest md5 = MessageDigest.getInstance("MD5");
        md5.update(text.getBytes());
        byte[] sign = md5.digest();
        int i;
        StringBuffer signbuffer = new StringBuffer("");
        for(int ii=0; ii<sign.length; ++ii){
            i = sign[ii];
            if (i < 0)
                i += 256;
            if (i < 16)
                signbuffer.append("0");
            signbuffer.append(Integer.toHexString(i));
        }
        return signbuffer.toString();
    }

    public static void setdlId (Context context, String key, long value) {
//        Download 音频文件
//        使用SharedPreferences 存储键值对
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(key, value);
        editor.commit();
    }

    public static long getdlId (Context context, String key) {
//      key: dl_r_53_2344
        return  PreferenceManager.getDefaultSharedPreferences(context).getLong(key, -1);
    }
//    使用query 来查询状态 返回cursor
    public static Cursor queryDownload (Context context, long downloadid) {

        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(downloadid);
        Cursor cursor = Tool.getDownloadManager(context).query(query);
        cursor.moveToFirst();
        return cursor;
    }

    public static int getStatus (Cursor cursor, boolean close) {
        int status = -1;
        if (cursor.getCount() != 0 && !cursor.isClosed()) {
            int state_index = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
            status = cursor.getInt(state_index);
        }

        if (close) {
            cursor.close();
        }
        return  status;
    }

    public static String getDownloadFile (Cursor cursor, boolean close) {
        String file = "";
        if (cursor.getCount() != 0 && !cursor.isClosed()) {
            int file_index = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI);
            file = cursor.getString(file_index);
        }
        if (close) {
            cursor.close();
        }
        return file;
    }

    public static DownloadManager getDownloadManager (Context context) {
        if (m_downloadManager == null) {
            m_downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        }
        return m_downloadManager;
    }

    public static long postDownloadTask (Context context, String url, String filename, String dlkey) {
//        Download 音频文件
//        使用DownloadManager来download
//        使用request提交下载请求
//        使用query 来查询状态 返回cursor
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setDestinationInExternalFilesDir(context, Environment.DIRECTORY_MUSIC, filename);
        //request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
        request.setMimeType("audio/mpeg");
        long dlid =  getDownloadManager(context).enqueue(request);
        setdlId(context, dlkey, dlid);
        return dlid;
    }

    public static DaoSession getDaoSession (Context context) {
        if (m_daoSession == null) {
            DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(context, "showfm-db");
            Database db = helper.getWritableDb();
            m_daoSession = new DaoMaster(db).newSession();
        }
        return m_daoSession;
    }
    private static DownloadManager m_downloadManager;
    private static ImageCache m_imageCache;
    private static DaoSession m_daoSession;
}