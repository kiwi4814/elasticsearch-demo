package com.example.elasticsearch.demo.util;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;

public class JSONUtil {
    public static void updateJsonByPath(String path, JSONObject jsonObject) {
        if (StringUtils.isNotBlank(path) && jsonObject != null) {
            String[] pathArray = StringUtils.split(path, ".");
            for (int i = 0; i < pathArray.length; i++) {
                JSONObject json = jsonObject.getJSONObject(pathArray[i]);
            }

        }

    }


}
