package com.business.businesstire.Cache;

import com.business.utils.models.IXMLBase;

import java.util.HashMap;

public class XmlCache {
    HashMap<String, IXMLBase> xmls = new HashMap<>();
    private static XmlCache mXmlCache;

    public static XmlCache getInstance() {
        if (mXmlCache == null)
            mXmlCache = new XmlCache();
        return mXmlCache;
    }

    public void Add(String Key, IXMLBase value) {
        xmls.put(Key, value);
    }

    public IXMLBase Get(String Key) {
        return xmls.get(Key);
    }


}
