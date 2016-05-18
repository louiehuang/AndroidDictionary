package com.writing.hlyin.dicttest.algorithm;

/**
 * Created by hlyin on 5/18/16.
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 保存运行时的相关数据
 *
 */
class RuntimeData {
    //筛选结果
    HashMap<String, Integer> selectedWord = new HashMap<String, Integer>();
    //默认线程数
    int defThreadCount = 5;
    //已经执行完成的线程数
    int finishThreadCount;

    /**
     * 根据数据长度获取线程数，线程数不会大于数组的长度。
     * @param selectedWord
     * @return
     */
    public int getThreadCount(ArrayList<String> selectedWord) {
        if (selectedWord.size() < defThreadCount) {
            return selectedWord.size();
        }
        return defThreadCount;
    }

    public void addResult(HashMap<String, Integer> resultMap){
        Iterator<Map.Entry<String, Integer>> entries = resultMap.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry<String, Integer> entry = entries.next();
            String word = entry.getKey();
            Integer similarity = entry.getValue();
            this.selectedWord.put(word, similarity);
        }
    }

}