package co.bankoo.zuweie.showfm;

import android.content.Intent;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import co.bankoo.zuweie.showfm.ctrl.Tool;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import co.bankoo.zuweie.showfm.model.Konst;
import co.bankoo.zuweie.showfm.model.Novel;
import co.bankoo.zuweie.showfm.model.Record;
import cz.msebera.android.httpclient.Header;
import de.hdodenhof.circleimageview.CircleImageView;

public class ListViewActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_view);

        m_lsNovel = (ListView) findViewById(R.id.lsnovel);
        m_HttpClient = new AsyncHttpClient();
        m_MylsAdapter = new MyLsAdapter();
        m_lsNovel.setAdapter(m_MylsAdapter);
        m_lsNovel.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Novel novel = (Novel) parent.getAdapter().getItem(position);
                Intent it = new Intent(ListViewActivity.this, RecordlistActivity.class);
                it.putExtra("novel_id", novel.id);
                it.putExtra("novel_poster", novel.poster);
                it.putExtra("novel_url", novel.url);
                startActivity(it);
            }
        });

        // 实例化 Viewpager 的layout 文件
        //getLayoutInflater().inflate(R.layout.viewpager, null);
        View view = getLayoutInflater().inflate(R.layout.viewpager, m_lsNovel, false);

        // 找到 Viewpager 然后对它进行初始化
        //m_vpNovel = (ViewPager) view.findViewById(R.id.viewpager);
        m_vpNovel = (MyViewPager) view.findViewById(R.id.viewpager);
        m_Indicators = (LinearLayout) view.findViewById(R.id.indicator);
        m_MyVpAdapter = new MyVpAdapter();
        m_vpNovel.setAdapter(m_MyVpAdapter);
        m_vpNovel.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager.SCROLL_STATE_SETTLING) {
                    updateIndicator();
                }
            }
        });
        // 初始化万了将它塞入ListView 的头部 。

        m_lsNovel.addHeaderView(view);

        // 加载数据 。
        loadData();

        // 启动service
        Intent it = new Intent(ListViewActivity.this, PlaybackService.class);
        startService(it);
    }

    @Override
    protected void onDestroy () {
        super.onDestroy();
        // 停止service
        Intent it = new Intent (ListViewActivity.this, PlaybackService.class);
        stopService(it);

    }

    public void loadData() {
        // 使用AsyncClient http请求。
        m_HttpClient.get(Konst.NOVELAPI, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    JSONObject jsonObject = new JSONObject(new String(responseBody));
                    JSONArray jsonArray = jsonObject.getJSONArray("novels");
                    List<Novel> novels = new LinkedList<Novel>();
                    for (int i=0; i<jsonArray.length(); ++i) {
                        Novel novel = Novel.json2novel(jsonArray.getJSONObject(i));
                        //m_Novels.add(Novel.json2novel(jsonArray.getJSONObject(i)));
                        if (novel != null) {
                            novels.add(novel);
                        }
                    }

                    Novel.updateNovelsToDb(ListViewActivity.this, novels);
                } catch (JSONException e) {
                    Toast.makeText(ListViewActivity.this, "加载失败！", Toast.LENGTH_LONG).show();
                }

                m_Novels = Novel.loadNovelsFromDb(ListViewActivity.this);

                // 选取前5个novel作为 viewpager来显示
                initPages();
                //m_vpNovel.setCurrentItem(2500);
                // 数据加载 完毕， 通知列表去更新
                m_MylsAdapter.notifyDataSetChanged();
                m_MyVpAdapter.notifyDataSetChanged();
                //m_vpNovel.setCurrentItem(2500);

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(ListViewActivity.this, "加载失败！", Toast.LENGTH_LONG).show();
          //如果没有网络就加载本地的
                m_Novels = Novel.loadNovelsFromDb(ListViewActivity.this);

                // 选取前5个novel作为 viewpager来显示
                initPages();

                // 数据加载 完毕， 通知列表去更新
                m_MylsAdapter.notifyDataSetChanged();
                m_MyVpAdapter.notifyDataSetChanged();
            }
        });
    }


    public void initPages () {
        pages.clear();
        // 当novels 有数据的时候才 截取前五个来做 轮播。
        if (m_Novels != null && m_Novels.size() > 5) {
            for (int i = 0; i < 5; ++i) {
                Novel novel = m_Novels.get(i);
                ImageView cover = (ImageView) getLayoutInflater().inflate(R.layout.viewpager_item, null);
                Tool.getImageCache(this).get(novel.poster, cover);
                pages.add(cover);
            }
        }


        //m_vpNovel.setCurrentItem(500);

        // 更新小圆点
        m_Indicators.removeAllViews();
        for(int i=0; i<pages.size(); ++i)  {

            float width = Tool.dip2px(this, 8.0f);
            float height = Tool.dip2px(this, 8.0f);
            float margin_right = Tool.dip2px(this, 5.0f);

            View view = new View(this);
            if (i==0) {
                view.setBackgroundResource(R.mipmap.indicator_selected);
            }else {
                view.setBackgroundResource(R.mipmap.indicator_nor);
            }
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams((int)width, (int)height, 1.0f);
            params.rightMargin = (int) margin_right;
            view.setLayoutParams(params);
            m_Indicators.addView(view);
        }
    }

    public void updateIndicator () {
        int pos = m_vpNovel.getCurrentItem();
        for (int i=0; i<m_Indicators.getChildCount(); ++i) {
            View view = m_Indicators.getChildAt(i);
            view.setBackgroundResource(R.mipmap.indicator_nor);
        }
        m_Indicators.getChildAt(pos%5).setBackgroundResource(R.mipmap.indicator_selected);
    }

    @Override
    public String getTag() {
        return "ListViewActivity";
    }

    class MyVpAdapter extends PagerAdapter {

        @Override
        public int getCount() {
           return pages.size() * 1000;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem (ViewGroup container, int position) {
            //View view = pages.get(position % 5);
            container.addView(pages.get(position % 5));
            return pages.get(position % 5);
        }

        @Override
        public void destroyItem (ViewGroup container,int position, Object object) {
            View view = (View) object;
            container.removeView(view);
        }
    }

    class MyLsAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return m_Novels != null ? m_Novels.size() - 5 : 0;
        }

        @Override
        public Object getItem(int position) {
            return m_Novels.get(position+4);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Holder holder;
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.list_item, null);
                holder = new Holder();
                holder.avatar = (CircleImageView) convertView.findViewById(R.id.avatar);
                holder.novel_name = (TextView) convertView.findViewById(R.id.novel_title_tx);
                holder.nj_name = (TextView) convertView.findViewById(R.id.nj_name_tx);
                convertView.setTag(holder);
            }else {
                holder = (Holder) convertView.getTag();
            }

            // 以下对每个Item子控件后，根据不同NovelObject做itemUI的更新！

            Novel novel = (Novel) getItem(position);

            // 使用ImageCache 做异步图像加载.
            Tool.getImageCache(ListViewActivity.this).get(novel.nj_avatar, holder.avatar);

            holder.novel_name.setText(novel.novel_name);
            holder.nj_name.setText(novel.nj_name);
            return convertView;
        }

        class Holder {
            CircleImageView avatar;
            TextView novel_name;
            TextView nj_name;
        }
    }

    // 小数列表控件
    Handler m_handler = new Handler();
    ListView m_lsNovel;
    List<Novel> m_Novels;// = new LinkedList<>();
    AsyncHttpClient m_HttpClient;
    MyLsAdapter m_MylsAdapter;
    MyVpAdapter m_MyVpAdapter;
    //ViewPager m_vpNovel;
    MyViewPager m_vpNovel;
    LinearLayout m_Indicators;
    List<View> pages = new LinkedList<>();
}
