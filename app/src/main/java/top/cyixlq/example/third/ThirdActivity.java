package top.cyixlq.example.third;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import top.cyixlq.annotation.OnClick;
import top.cyixlq.cbutterknife.CButterKnife;
import top.cyixlq.example.R;

public class ThirdActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_third);
        CButterKnife.bind(this);
    }

    @OnClick(R.id.btnToast)
    public void toastMessage(Button button) {
        Toast.makeText(this, button.getText(), Toast.LENGTH_SHORT).show();
    }
}
