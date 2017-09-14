package co.bankoo.zuweie.showfm.model;

import android.content.Context;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.greenrobot.greendao.annotation.Generated;

import co.bankoo.zuweie.showfm.ctrl.Tool;

@Entity
public class Record {

    public Record (Long id, String nj_id, String nj_name, String name, String url, String updated, int novel_id) {
        this.id = id;
        this.nj_id = nj_id;
        this.nj_name = nj_name;
        this.name = name;
        this.url = url;
        try {
            this.updated = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss").parse(updated);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        this.novel_id = novel_id;
    }


    @Generated(hash = 1486201659)
    public Record(Long id, String nj_id, String nj_name, String name, String url, Date updated, int novel_id) {
        this.id = id;
        this.nj_id = nj_id;
        this.nj_name = nj_name;
        this.name = name;
        this.url = url;
        this.updated = updated;
        this.novel_id = novel_id;
    }


    @Generated(hash = 477726293)
    public Record() {
    }


    public static Record json2record (JSONObject json) {

        try {
            return new Record(json.getLong("id"),
                    json.getString("nj_id"),
                    json.getString("nj_name"),
                    json.getString("name"),
                    json.getString("url"),
                    json.getString("updated"),
                    json.getInt("novel_id"));
        }catch (JSONException e) {
        }
        return null;
    }

    public String getDownloadKey () {
        return "dl_r_"+novel_id+"_"+id;
    }
    public String getDownloadfilename () {
        return "dl_r_"+novel_id+"_"+id+".mp3";
    }


    public Long getId() {
        return this.id;
    }


    public void setId(Long id) {
        this.id = id;
    }


    public String getNj_id() {
        return this.nj_id;
    }


    public void setNj_id(String nj_id) {
        this.nj_id = nj_id;
    }


    public String getNj_name() {
        return this.nj_name;
    }


    public void setNj_name(String nj_name) {
        this.nj_name = nj_name;
    }


    public String getName() {
        return this.name;
    }


    public void setName(String name) {
        this.name = name;
    }


    public String getUrl() {
        return this.url;
    }


    public void setUrl(String url) {
        this.url = url;
    }


    public Date getUpdated() {
        return this.updated;
    }


    public void setUpdated(Date updated) {
        this.updated = updated;
    }


    public int getNovel_id() {
        return this.novel_id;
    }


    public void setNovel_id(int novel_id) {
        this.novel_id = novel_id;
    }
    @Id
    public Long id;
    public String nj_id;
    public String nj_name;
    public String name;
    public String url;
    public Date updated;
    public int novel_id;


    public static void updateNovelsToDb (Context context, List<Record> novelstodb) {
        RecordDao novelDao = Tool.getDaoSession(context).getRecordDao();
        // 首先拿到已经存入DB的ID 最大 的 Novel的
        List<Record> novels = novelDao.queryBuilder().limit(1).orderDesc(RecordDao.Properties.Novel_id).list();
        // 如果未空，证明数据库里面还未有novel的存入。
        if (novels.isEmpty()) {
            for (Record novel : novelstodb) {
                novelDao.insert(novel);
            }
        }else {
            // 拿到最大的那个ID
            int biggestId = novels.get(0).novel_id;

            for (Record novel : novelstodb) {
                // 如果还有比这个大的ID，证明是新家伙
                if (biggestId < novel.novel_id) {
                    novelDao.insert(novel);
                }else {
                    novelDao.save(novel);
                }
            }
        }
    }
    public static List<Record> loadNovelsFromDb (Context context) {
        DaoSession daoSession = Tool.getDaoSession(context);
        RecordDao novelDao = daoSession.getRecordDao();
        List<Record> novels = novelDao.queryBuilder().orderDesc(RecordDao.Properties.Name).list();
        return novels;
    }
}