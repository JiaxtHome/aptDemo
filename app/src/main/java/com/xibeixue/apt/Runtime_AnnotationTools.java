package com.xibeixue.apt;

import android.app.Activity;

import com.xibeixue.apt_annotation.BindViewTo;

import java.lang.reflect.Field;

public class Runtime_AnnotationTools {

    public static void getAllAnnotationView(Activity activity) {
        //获得成员变量
        Field[] fields = activity.getClass().getDeclaredFields();

        for (Field field : fields) {
            try {
                if (field.getAnnotations() != null) {
                    //判断BindViewTo注解是否存在
                    if (field.isAnnotationPresent(BindViewTo.class)) {
                        //获取访问权限
                        field.setAccessible(true);
                        BindViewTo getViewTo = field.getAnnotation(BindViewTo.class);
                        //获取View id
                        int id = getViewTo.value();
                        //通过id获取View，并赋值该成员变量
                        field.set(activity, activity.findViewById(id));
                    }
                }
            } catch (Exception e) {
            }
        }
    }
}
