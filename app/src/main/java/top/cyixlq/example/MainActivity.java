package top.cyixlq.example;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import top.cyixlq.annotation.BindView;
import top.cyixlq.annotation.OnClick;
import top.cyixlq.cbutterknife.CButterKnife;
import top.cyixlq.example.third.ThirdActivity;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.tvInfo)
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        CButterKnife.bind(this);
        textView.setText("lalala");
    }

    @OnClick({R.id.btnToast, R.id.tvInfo})
    public void toast(View textView) {
        Toast.makeText(this, "lalala", Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.btnGoTo)
    public void goToSecondActivity() {
        startActivity(new Intent(this, SecondActivity.class));
    }

    @OnClick(R.id.btnGoToThird)
    public void goToThirdActivity() {
        startActivity(new Intent(this, ThirdActivity.class));
    }
}
