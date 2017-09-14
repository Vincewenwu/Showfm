package co.bankoo.zuweie.showfm;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class StartupActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startup);
        Button lsbtn = (Button) findViewById(R.id.ls);
        lsbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(StartupActivity.this, ListViewActivity.class);
                startActivity(it);
            }
        });
    }

    @Override
    public String getTag() {
        return "Startup Activity";
    }
}
