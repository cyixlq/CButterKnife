package top.cyixlq.example;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import top.cyixlq.annotation.BindView;
import top.cyixlq.annotation.OnClick;
import top.cyixlq.cbutterknife.CButterKnife;

public class SecondFragment extends Fragment {

    @BindView(R.id.tvInfo)
    TextView textView;

    public SecondFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_second, container, false);
        CButterKnife.bind(this, view);
        return view;
    }

    @SuppressLint("SetTextI18n")
    @OnClick({R.id.btn1, R.id.btn2})
    public void btnClick(Button view) {
        textView.setText("info:" + view.getText());
    }
}
