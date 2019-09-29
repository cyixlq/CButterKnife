package top.cyixlq.cbutterknife;

import android.app.Activity;
import android.view.View;

public class CButterKnife {

    private static final String VIEW_BINDING = "$ViewBinding";

    public static void bind(Activity activity) {
        bind(activity, activity.getWindow().getDecorView());
    }

    public static void bind(Object object, View view) {
        final Class<?> objectClass = object.getClass();
        final String bindClassName = objectClass.getCanonicalName() + VIEW_BINDING;
        try {
            Class<?> bindClass = Class.forName(bindClassName);
            bindClass.getConstructor(objectClass, View.class).newInstance(object, view);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
