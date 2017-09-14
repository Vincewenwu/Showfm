package co.bankoo.zuweie.showfm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;

import co.bankoo.zuweie.showfm.ctrl.Tool;
import co.bankoo.zuweie.showfm.model.BaseEvent;
import co.bankoo.zuweie.showfm.model.Konst;
import co.bankoo.zuweie.showfm.model.Msg;
import co.bankoo.zuweie.showfm.model.ServiceEvent;
import de.hdodenhof.circleimageview.CircleImageView;

public class PlaybackActivity extends BaseActivity {

    @Override
    public String getTag() {
        return "Playback Activity";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playback);
        initUi();

        // TODO : play the mp3
        //String url = getIntent().getStringExtra("url");
        //EventBus.getDefault().post(new ServiceEvent(Msg.PLAY, url));
    }

    @Override
    protected void onResume () {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter(Konst.PACKBACKUPDATEACTION);
        registerReceiver(m_Receiver, intentFilter);
    }

    @Override
    protected void onPause () {
        super.onPause();
        unregisterReceiver(m_Receiver);
    }

    void initUi() {
        m_NovelCover = (CircleImageView) findViewById(R.id.poster);
        m_Start = (Button) findViewById(R.id.start);
        m_Start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //String url = getIntent().getStringExtra("url");
                EventBus.getDefault().post(new ServiceEvent(Msg.PLAY));
            }
        });

        m_PlaybackStatus = (TextView) findViewById(R.id.status_tx);

        m_Progress = (SeekBar) findViewById(R.id.seekbar);
        m_Progress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // 只有是自己拽的才发msg给service去seekto
                if (fromUser) {
                    EventBus.getDefault().post(new ServiceEvent(Msg.SEEKTO, progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        String poster = getIntent().getStringExtra("novel_poster");
        Tool.getImageCache(PlaybackActivity.this).get(poster, m_NovelCover);

        // 定义一个旋转的动效
        m_Animation = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        //m_Animation = new RotateAnimation(0, 360);
        m_Animation.setFillAfter(true);
        m_Animation.setDuration(1000*3);
        m_Animation.setRepeatCount(-1);
        m_Animation.setRepeatMode(Animation.RESTART); //Animation.REVERSE


    }

    public void updateUi(int status, int duration, int pos) {
        if (status == PlaybackService.STA_PREPARING) {
            m_Start.setText("加载中");
            if (m_hasStartedAnimation == true) {
                m_NovelCover.clearAnimation();
                m_hasStartedAnimation = false;
            }
        }else if (status == PlaybackService.STA_PAUSED) {
            m_Start.setText("开始");
            m_Progress.setMax(duration);
            m_Progress.setProgress(pos);
            if (m_hasStartedAnimation == true) {
                m_NovelCover.clearAnimation();
                m_hasStartedAnimation = false;
            }
        }else if (status == PlaybackService.STA_STARTED) {
            m_Start.setText("暂停");
            m_Progress.setMax(duration);
            m_Progress.setProgress(pos);
            if (m_hasStartedAnimation == false) {
                m_NovelCover.startAnimation(m_Animation);
                m_hasStartedAnimation = true;
            }
        }

        int pos_m = (pos / 1000) / 60;
        int pos_s = (pos / 1000) % 60;

        int duration_m = (duration / 1000) / 60;
        int duration_s = (duration / 1000) % 60;

        m_PlaybackStatus.setText(String.format("%02d:%02d / %02d:%02d", pos_m, pos_s, duration_m, duration_s));
    }

    BroadcastReceiver m_Receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int status = intent.getIntExtra("status", -1);
            updateUi(status, intent.getIntExtra("duration",0), intent.getIntExtra("current_position", 0));
        }
    };

    CircleImageView m_NovelCover;
    TextView m_PlaybackStatus;
    Button m_Start;
    SeekBar m_Progress;
    RotateAnimation m_Animation;
    boolean m_hasStartedAnimation = false;
}
