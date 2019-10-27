package com.kapiteon;

import java.lang.reflect.Field;

public class Helper {
    public static Object getValueReflection(Object parent, Class fieldClass) throws IllegalArgumentException, IllegalAccessException {
        Field[] fields = parent.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.getType() == fieldClass) {
                field.setAccessible(true);
                return field.get(parent);
            }
        }
        return null;
    }
}
