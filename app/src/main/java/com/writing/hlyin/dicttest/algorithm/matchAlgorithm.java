package com.writing.hlyin.dicttest.algorithm;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by hlyin on 5/18/16.
 */
public class matchAlgorithm {

    private static ArrayList<String> wordList;

    public matchAlgorithm() {
    }

    public matchAlgorithm(ArrayList<String> wordList) {
        this.wordList = wordList;
    }


    /**
     * 正向匹配，返回匹配度大于等于k的单词哈希表
     *
     * @param input
     * @return 正向按匹配度从大到小排序后的匹配结果
     */
    public static HashMap<String, Integer> matchFromBeginning(String input, int k) {
        HashMap<String, Integer> resultMap = new HashMap<String, Integer>(); // <单词, 匹配度>
        int inputLength = input.length();

        for (int s = 0; s < wordList.size(); s++) { //遍历字典
            String template = wordList.get(s);
            int templateLength = template.length();
            int length = inputLength > templateLength ? inputLength : templateLength; //取较长的串长度
            int similarity = -1; //匹配相似度

            for (int i = 0, j = 0; i <= inputLength && j <= templateLength; i++, j++) {
                //i==inputLength或j==templateLength时下标越界，因此先判断这些情况
                if (i == inputLength || j == templateLength) {
                    // i=inputLength表示input是template的一个子集, 如car是cardinal子集；j=0反之
                    similarity = inputLength > templateLength ? templateLength : inputLength; //取小的
                } else if (i == inputLength && j == templateLength) { // input == templateLength
                    similarity = length; //取大的
                } else if (input.charAt(i) != template.charAt(j)) { // 从前往后依次比较，若不同，则此时可求相似度
                    // length为input和template中较长字符串的长度，所以取相似度时也要对应
                    if (inputLength > templateLength) { // 也即if(i > j)
                        similarity = i;  //下标即为此时的相似度
                    } else {
                        similarity = j;
                    }
                    break; // 不等时退出
                }
            }
            if (similarity >= k) { // 相似度大于一定程度
                resultMap.put(template, similarity);
            }
        }

        // 排序哈希表
        HashMap<String, Integer> sortedMap = sortMap(resultMap);
        return sortedMap;
    }


    /**
     * 反向匹配，返回匹配度大于等于k的单词哈希表
     *
     * @param input
     * @return 反向按匹配度从大到小排序后的匹配结果
     */
    public static HashMap<String, Integer> matchFromEnd(String input, int k) {
        HashMap<String, Integer> resultMap = new HashMap<String, Integer>(); // <单词, 匹配度>
        int inputLength = input.length();

        for (int s = 0; s < wordList.size(); s++) { //遍历字典
            String template = wordList.get(s);
            int templateLength = template.length();
            int length = inputLength > templateLength ? inputLength : templateLength; //取较长的串长度
            int similarity = -1; //匹配相似度

            for (int i = inputLength - 1, j = templateLength - 1; i >= 0 && j >= 0; i--, j--) {
                //注意if的顺序与正向不同
                if (input.charAt(i) != template.charAt(j)) { // 从后往前依次比较，若不同，则此时可求相似度
                    // length为input和template中较长那个字符串的长度，所以这里相减时也要按照对应的来减
                    if (inputLength > templateLength) { // 也即if(i > j)
                        similarity = (length - 1) - i; // 下标相减得到反向比较相同字符的个数
                    } else {
                        similarity = (length - 1) - j;
                    }
                    break; // 不等时退出
                } else if (i == 0 || j == 0) {
                    // i=0表示input是template的一个子集, 如date是antidate子集；j=0反之
                    similarity = inputLength > templateLength ? templateLength : inputLength; //取小的
                } else if (i == 0 && j == 0) { // input == templateLength
                    similarity = length; //取大的
                }
            }
            //System.out.println(template + ", " + similarity);
            if (similarity >= k) { // 相似度大于一定程度
                resultMap.put(template, similarity);
            }

        }
        // 排序哈希表
        HashMap<String, Integer> sortedMap = sortMap(resultMap);
        return sortedMap;
    }


    /**
     * 哈希转ArrayList
     *
     * @param hashMap
     */
    public static ArrayList<String> hashMapToArrayList(HashMap<String, Integer> hashMap) {
        ArrayList<String> result = new ArrayList<String>();
        // 遍历哈希表
        //int i = 1;

        Iterator<Map.Entry<String, Integer>> entries = hashMap.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry<String, Integer> entry = entries.next();
            String word = entry.getKey();
            result.add(word);
            //result.add(i + ". " + word);
            //i++;
        }
        return result;
    }


    /**
     * 模糊匹配，Levenshtein算法计算两字符串之间相似度，返回相似率大于d的降序排列结果
     *
     * @param input
     * @param d
     * @return
     */
    public static HashMap<String, Integer> approximateMatch(String input, double d) {
        HashMap<String, Integer> resultMap = new HashMap<String, Integer>(); // <单词, 匹配度>
        int inputLength = input.length();


        /*多线程计算，数组越界？？？*/
        //resultMap = selectWords(input);

        /*单线程计算*/
        Levenshtein lt = new Levenshtein();
        for (int s = 0; s < wordList.size(); s++) { //遍历字典
            String template = wordList.get(s);
            try {
                float similarity = lt.getSimilarityRatio(input, template);
                if (similarity >= d) {
                    int value = (int) Math.floor(similarity * 100); //相似率转成整型, 向下取整, 如0.551转成55
                    resultMap.put(template, value);
                }
            } catch (Exception e) {
                Log.v("", input + ", " + template);
                e.printStackTrace();
            }
        }

        // 排序哈希表
        HashMap<String, Integer> sortedMap = sortMap(resultMap);
        return sortedMap;
    }


    /* 多线程计算 */
    static public HashMap<String, Integer> selectWords(final String input) {
        final RuntimeData rd = new RuntimeData();
        int threadCount = rd.getThreadCount(wordList);
        // System.out.println("thread count:" + threadCount);

        // 每线程计算相似度大于k的单词
        final int lenPerThread = wordList.size() / threadCount;
        // System.out.println(lenPerThread);
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            new Thread() {
                @Override
                public void run() {
                    HashMap<String, Integer> resultMap = new HashMap<String, Integer>();
                    int start = index * lenPerThread;
                    int end = start + lenPerThread;
                    Levenshtein lt = new Levenshtein();

                    for (int s = start; s < end; s++) { // 遍历字典
                        String template = wordList.get(s);
                        try {
                            float similarity = lt.getSimilarityRatio(input, template);
                            if (similarity >= 0.6) {
                                int value = (int) Math.floor(similarity * 100);
                                resultMap.put(template, value);
                            }
                        } catch (Exception e) {
                            Log.v("", input + ", " + template);
                            e.printStackTrace();
                        }
                    }

                    synchronized (rd) {
                        Iterator<Map.Entry<String, Integer>> entries = resultMap.entrySet().iterator();
                        while (entries.hasNext()) {
                            Map.Entry<String, Integer> entry = entries.next();
                            String word = entry.getKey();
                            Integer similarity = entry.getValue();
                            rd.selectedWord.put(word, similarity);
                        }

                        rd.finishThreadCount++;
                    }
                }

                ;
            }.start();
        }

        while (rd.finishThreadCount != threadCount) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
        }
        return rd.selectedWord;
    }


    /**
     * HashMap排序
     *
     * @param originalMap
     * @return
     */
    public static HashMap<String, Integer> sortMap(Map<String, Integer> originalMap) {
        // 加入ArrayList
        ArrayList<Map.Entry<String, Integer>> list = new ArrayList<Map.Entry<String, Integer>>(originalMap.entrySet());

        // 对ArrayList进行排序
        Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
            // 从大到小排序
            @Override
            public int compare(Map.Entry<String, Integer> element1, Map.Entry<String, Integer> element2) {
                return element2.getValue() - element1.getValue();
            }
        });

        // 将排序结果加入新的Map
        HashMap<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();
        for (int i = 0; i < list.size(); i++) {
            sortedMap.put(list.get(i).getKey(), list.get(i).getValue());
        }
        return sortedMap;
    }

}
