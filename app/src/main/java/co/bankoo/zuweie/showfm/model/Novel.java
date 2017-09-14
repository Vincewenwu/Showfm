package co.bankoo.zuweie.showfm.model;

import android.content.Context;
import android.text.format.DateFormat;

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

// 定义了小说的模型
@Entity
public class Novel {

     public static Novel json2novel (JSONObject json) {
         try {
             return new Novel(json.getLong("id"),
                     json.getString("nj_id"),
                     json.getString("nj_name"),
                     json.getString("nj_avatar"),
                     json.getString("novel_name"),
                     json.getString("url"),
                     json.getString("poster"),
                     json.getString("author"),
                     json.getString("body"),
                     json.getString("updated"),
                     json.getString("keyword"),
                     json.getString("category"));
         }catch (JSONException e) {
             e.printStackTrace();
         }
         return null;
     }

     public static void updateNovelsToDb (Context context, List<Novel> novelstodb) {
         NovelDao novelDao = Tool.getDaoSession(context).getNovelDao();
         // 首先拿到已经存入DB的ID 最大 的 Novel的
         List<Novel> novels = novelDao.queryBuilder().limit(1).orderDesc(NovelDao.Properties.Id).list();
         // 如果未空，证明数据库里面还未有novel的存入。
         if (novels.isEmpty()) {
             for (Novel novel : novelstodb) {
                 novelDao.insert(novel);
             }
         }else {
             // 拿到最大的那个ID
             Long biggestId = novels.get(0).id;

             for (Novel novel : novelstodb) {
                 // 如果还有比这个大的ID，证明是新家伙
                 if (biggestId < novel.id) {
                     novelDao.insert(novel);
                 }else {
                     novelDao.save(novel);
                 }
             }
         }
     }

     public static List<Novel> loadNovelsFromDb (Context context) {
         DaoSession daoSession = Tool.getDaoSession(context);
         NovelDao novelDao = daoSession.getNovelDao();
         List<Novel> novels = novelDao.queryBuilder().orderDesc(NovelDao.Properties.Updated,NovelDao.Properties.Url).list();
         return novels;
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

    public String getNj_avatar() {
        return this.nj_avatar;
    }

    public void setNj_avatar(String nj_avatar) {
        this.nj_avatar = nj_avatar;
    }

    public String getNovel_name() {
        return this.novel_name;
    }

    public void setNovel_name(String novel_name) {
        this.novel_name = novel_name;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPoster() {
        return this.poster;
    }

    public void setPoster(String poster) {
        this.poster = poster;
    }

    public String getAuthor() {
        return this.author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getBody() {
        return this.body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Date getUpdated() {
        return this.updated;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
    }

    public String getKeyword() {
        return this.keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getCategory() {
        return this.category;
    }

    public void setCategory(String category) {
        this.category = category;
    }


    public Novel(Long id, String nj_id, String nj_name, String nj_avatar, String novel_name, String url, String poster, String author, String body, String updated, String keyword, String category ) {
        this.id = id;
        this.nj_id = nj_id;
        this.nj_name = nj_name;
        this.nj_avatar = nj_avatar;
        this.novel_name = novel_name;
        this.url = url;
        this.poster = poster;
        this.author = author;
        this.body = body;
        try {
            this.updated = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss").parse(updated);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        this.keyword = keyword;
        this.category = category;

    }

    @Generated(hash = 123240777)
    public Novel(Long id, String nj_id, String nj_name, String nj_avatar, String novel_name, String url, String poster, String author, String body, Date updated, String keyword, String category) {
        this.id = id;
        this.nj_id = nj_id;
        this.nj_name = nj_name;
        this.nj_avatar = nj_avatar;
        this.novel_name = novel_name;
        this.url = url;
        this.poster = poster;
        this.author = author;
        this.body = body;
        this.updated = updated;
        this.keyword = keyword;
        this.category = category;
    }

    @Generated(hash = 1747797347)
    public Novel() {
    }

    @Id
    public Long id;
    public String nj_id;
    public String nj_name;
    public String nj_avatar;
    public String  novel_name;
    public String url;
    public String poster;
    public String author;
    public String body;
    public Date updated;
    public String keyword;
    public String category;
}