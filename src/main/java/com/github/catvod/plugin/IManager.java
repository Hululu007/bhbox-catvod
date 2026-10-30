package com.github.catvod.plugin;

import com.github.catvod.plugin.bean.ApkParam;
import com.github.catvod.plugin.bean.PluginBean;

import java.util.List;

public interface IManager {

    PluginBean getPluginBeanById(String id);

    IRuntimePlugin getRuntimePluginById(String id);

    void savePluginParams(String id, List<ApkParam> params);

    List<ApkParam> getPluginParams(String id);

    /** 读取插件 APK 内 assets 文件（按插件 id 定位 APK；APK 不存在或读取失败返回 null） */
    byte[] readPluginAsset(String id, String path);

    /** readPluginAsset 的 UTF-8 字符串封装 */
    String readPluginAssetString(String id, String path);

}