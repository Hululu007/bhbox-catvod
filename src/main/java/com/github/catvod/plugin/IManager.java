package com.github.catvod.plugin;

import com.github.catvod.plugin.bean.ApkParam;
import com.github.catvod.plugin.bean.PluginBean;

import java.util.List;

public interface IManager {

    PluginBean getPluginBeanById(String id);

    IRuntimePlugin getRuntimePluginById(String id);

    void savePluginParams(String id, List<ApkParam> params);

    List<ApkParam> getPluginParams(String id);

}