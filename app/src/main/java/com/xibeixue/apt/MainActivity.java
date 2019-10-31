package com.xibeixue.apt;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

import com.xibeixue.apt_annotation.BindViewTo;

public class MainActivity extends AppCompatActivity {

    @BindViewTo(R.id.text)
    public TextView mText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //调用注解绑定
        //Runtime_AnnotationTools.getAllAnnotationView(this);

        new MainActivity_ViewBinding().bindView(this);
        //测试绑定是否成功
        mText.setTextColor(Color.RED);
    }

}
