package co.bankoo.zuweie.showfm;

import android.app.DownloadManager;
import android.app.admin.DeviceAdminReceiver;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.LinkedList;
import java.util.List;

import co.bankoo.zuweie.showfm.ctrl.Tool;
import co.bankoo.zuweie.showfm.model.Konst;
import co.bankoo.zuweie.showfm.model.Msg;
import co.bankoo.zuweie.showfm.model.Novel;
import co.bankoo.zuweie.showfm.model.Record;
import co.bankoo.zuweie.showfm.model.ServiceEvent;
import cz.msebera.android.httpclient.Header;


public class RecordlistActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recordlist);
        m_lsRecord = (ListView) findViewById(R.id.listview);
        m_lsRecord.setAdapter(m_lsAdapter);
        m_lsRecord.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Record record = (Record) parent.getAdapter().getItem(position);
                boolean playlocal = false;
                long dlid = Tool.getdlId(RecordlistActivity.this, record.getDownloadKey());
                if (dlid > 0) {
                    Cursor cursor = Tool.queryDownload(RecordlistActivity.this, dlid);

                    int status = Tool.getStatus(cursor, false);

                    if (status == DownloadManager.STATUS_SUCCESSFUL) {
                        String file_uri = Tool.getDownloadFile(cursor, true);
                        EventBus.getDefault().post(new ServiceEvent(Msg.PLAY_LOCAL, file_uri));
                        playlocal = true;
                    }
                }

                if (playlocal == false) {
                    String novel_url = getIntent().getStringExtra("novel_url");
                    String url = Tool.getRecordUrl(novel_url, record.url, 1800);
                    EventBus.getDefault().post(new ServiceEvent(Msg.PLAY, url));
                }

                Intent it = new Intent(RecordlistActivity.this, PlaybackActivity.class);
                it.putExtra("novel_poster", getIntent().getStringExtra("novel_poster"));
                startActivity(it);

            }
        });
        m_poster = getIntent().getStringExtra("poster");
        m_url     = getIntent().getStringExtra("url");

        loadData();
    }

    public void loadData () {
        String api = Konst.RECORDAPI+"?novel_id="+getIntent().getLongExtra("novel_id", -1);
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(api, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    JSONObject jsonObject = new JSONObject(new String(responseBody));
                    JSONArray jsonArray = jsonObject.getJSONArray("records");
                    List<Record> records = new LinkedList<Record>();
                    for (int i=0; i<jsonArray.length(); ++i) {
                        JSONObject json = jsonArray.getJSONObject(i);
                        Record record = Record.json2record(json);
                        if (record != null) {
//                            m_Records.add(record);
                            records.add(record);
                        }
                    }
                    Record.updateNovelsToDb(RecordlistActivity.this, records);

                } catch (JSONException e) {
                    Toast.makeText(RecordlistActivity.this, "加载失败！", Toast.LENGTH_LONG).show();
                }
                m_Records=Record.loadNovelsFromDb(RecordlistActivity.this);
                // 数据加载 完毕， 通知列表去更新
                m_lsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(RecordlistActivity.this, "加载失败le！", Toast.LENGTH_LONG).show();
                //如果没有网络就加载本地的
                m_Records=Record.loadNovelsFromDb(RecordlistActivity.this);
                // 数据加载 完毕， 通知列表去更新
                m_lsAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    protected void onResume () {
//        m_Receiver这个是广播接收者
        super.onResume();
        IntentFilter intentFilter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        registerReceiver(m_Receiver, intentFilter);//这里相当于发出广播下面回去接收
    }

    @Override
    protected void onPause () {
        super.onPause();
        unregisterReceiver(m_Receiver);
    }

    BaseAdapter m_lsAdapter = new BaseAdapter() {
        @Override
        public int getCount() {
            return  m_Records.size();
        }

        @Override
        public Object getItem(int position) {
            return m_Records.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Holder holder;
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.rec_ls_item, parent, false);
                holder = new Holder();
                holder.mTx = (TextView) convertView.findViewById(R.id.name);
                holder.mBtn = (Button) convertView.findViewById(R.id.download_btn);
                holder.mBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Record record = (Record) v.getTag();
                        String novel_url = getIntent().getStringExtra("novel_url");
                        String url = Tool.getRecordUrl(novel_url, record.url, 1800);
//下载的时候存好对应的状态，遍历是否下载的状态下面要用到，是根据手机本地的下载为中心
                        Tool.postDownloadTask(RecordlistActivity.this, url, record.getDownloadfilename(), record.getDownloadKey());
                        ((TextView) v).setText("下载中");
                        v.setClickable(false);
                    }
                });
                convertView.setTag(holder);
            }else {
                holder = (Holder) convertView.getTag();
            }

            Record record = (Record) getItem(position);

            holder.mTx.setText(record.name);

            holder.mBtn.setTag(record);

            long downloadid = Tool.getdlId(RecordlistActivity.this, record.getDownloadKey());
            if (downloadid > 0) {
                int status = Tool.getStatus(Tool.queryDownload(RecordlistActivity.this, downloadid), true);
                if (status == DownloadManager.STATUS_SUCCESSFUL) {
                    holder.mBtn.setVisibility(View.GONE);
                    holder.mBtn.setClickable(true);
                }else if (status == DownloadManager.STATUS_FAILED){
                    holder.mBtn.setVisibility(View.VISIBLE);
                    holder.mBtn.setText("下载失败");
                    holder.mBtn.setClickable(true);
                }else {
                    holder.mBtn.setVisibility(View.VISIBLE);
                    holder.mBtn.setText("下载中1");
                    holder.mBtn.setClickable(false);
                }
            }else {
                holder.mBtn.setVisibility(View.VISIBLE);
                holder.mBtn.setText("下载");
                holder.mBtn.setClickable(true);
            }
            // 更新downloadbutton

            return convertView;
        }

        class Holder {
            TextView mTx;
            Button mBtn;
        }
    };
//    广播的接收者
    BroadcastReceiver m_Receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            m_lsAdapter.notifyDataSetChanged();//接收广播的动作就去更新m_lsAdapter
        }
    };

    @Override
    public String getTag() {
        return "Recordlist Actvity";
    }

    List<Record> m_Records = new LinkedList<>();
    ListView m_lsRecord;
    String m_poster;
    String m_url;
}
