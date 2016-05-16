package com.writing.hlyin.dicttest;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.writing.hlyin.dicttest.model.Words;
import com.writing.hlyin.dicttest.util.HttpCallBackListener;
import com.writing.hlyin.dicttest.util.HttpUtil;
import com.writing.hlyin.dicttest.util.ParseXML;
import com.writing.hlyin.dicttest.util.WordsAction;
import com.writing.hlyin.dicttest.util.WordsHandler;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by hlyin on 2016/5/15.
 * 查词界面
 */
public class MainActivity extends Activity {

    private ListView lv1;
    private ArrayAdapter<String> aadapter;

    private MyAdapter myAdapter;

    private String[] testWords;
    private static List<String> wordlList;


    private SearchView searchView;
    private TextView searchWords_key, searchWords_psE, searchWords_psA, searchWords_posAcceptation, searchWords_sent;
    private ImageButton searchWords_voiceE, searchWords_voiceA;
    private LinearLayout searchWords_posA_layout, searchWords_posE_layout, searchWords_linerLayout, searchWords_fatherLayout;
    private WordsAction wordsAction;
    private Words words = new Words();

    /**
     * 网络查词完成后回调handleMessage方法
     */
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 111:
                    //判断网络查找不到该词的情况
                    if (words.getSent().length() > 0) {
                        upDateView();
                    } else {
                        searchWords_linerLayout.setVisibility(View.GONE);
                        Toast.makeText(MainActivity.this, "抱歉！找不到该词！", Toast.LENGTH_SHORT).show();
                    }
                    Log.d("测试", "tv保存2");
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        wordsAction = WordsAction.getInstance(this);
        //初始化控件
        searchWords_linerLayout = (LinearLayout) findViewById(R.id.searchWords_linerLayout);
        searchWords_linerLayout.setVisibility(View.GONE);
        searchWords_posA_layout = (LinearLayout) findViewById(R.id.searchWords_posA_layout);
        searchWords_posE_layout = (LinearLayout) findViewById(R.id.searchWords_posE_layout);
        searchWords_fatherLayout = (LinearLayout) findViewById(R.id.searchWords_fatherLayout);
        searchWords_fatherLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //点击输入框外实现软键盘隐藏
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
        });
        searchWords_key = (TextView) findViewById(R.id.searchWords_key);
        searchWords_psE = (TextView) findViewById(R.id.searchWords_psE);
        searchWords_psA = (TextView) findViewById(R.id.searchWords_psA);
        searchWords_posAcceptation = (TextView) findViewById(R.id.searchWords_posAcceptation);
        searchWords_sent = (TextView) findViewById(R.id.searchWords_sent);
        searchWords_voiceE = (ImageButton) findViewById(R.id.searchWords_voiceE);
        searchWords_voiceE.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wordsAction.playMP3(words.getKey(), "E", MainActivity.this);
            }
        });
        searchWords_voiceA = (ImageButton) findViewById(R.id.searchWords_voiceA);
        searchWords_voiceA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wordsAction.playMP3(words.getKey(), "A", MainActivity.this);
            }
        });


        testWords = new String[]{"apple", "appropriate", "abdicate",
                "antidate", "abrogate", "abomination", "aggregation",
                "candidate", "canada", "cardinal", "date",
                "documentation", "interesting", "initiate", "maximum",
                "new", "fate", "opinion"};

        lv1 = (ListView) findViewById(R.id.lv1);

        lv1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String word = lv1.getItemAtPosition(position).toString(); //apple
                Log.d("click: ", word);
                loadWords(word);
                lv1.setVisibility(View.GONE);
            }
        });

        wordlList = new ArrayList<>();
        //初始化wordlList（数据库调单词数据）
//        for (int i = 0; i < testWords.length; i++) {
//            wordlList.add(testWords[i]);
//        }

        String res = "";
        try {
            //得到资源中的Raw数据流
            InputStream in = getResources().openRawResource(R.raw.words);
            InputStreamReader reader = new InputStreamReader(in);
            BufferedReader bufferedReader = new BufferedReader(reader);
            // 按行读取文件
            String line = null; // 文件行
            while ((line = bufferedReader.readLine()) != null) {
                line = line.trim();
                wordlList.add(line); // 1行1词
            }
            bufferedReader.close();
            reader.close();
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }


        searchView = (SearchView) findViewById(R.id.searchWords_searchView);
        searchView.setSubmitButtonEnabled(true);//设置显示搜索按钮
        searchView.setIconifiedByDefault(false);//设置不自动缩小为图标
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                loadWords(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                lv1.setVisibility(View.VISIBLE);
                //searchWords_linerLayout.setVisibility(View.GONE);

                if (newText.length() != 0) {
                    setFilterText(newText);
                } else {
                    clearTextFilter();
                }
                return false;
            }
        });


    }


    /**
     * 根据用户的选择进行不同的匹配模式
     * @param filterText
     */
    public void setFilterText(String filterText) {
        //包含匹配
        //Alt + Enter 快速导入包; Alt + command + L 快速排版

        List<String> list = new ArrayList<String>();
        for (int i = 0; i < wordlList.size(); i++) {
            if (wordlList.get(i).contains(filterText)) {
                list.add(wordlList.get(i));
            }
        }

//        //k近似匹配
//        /*** 模糊匹配，找相似率大于k(0.60)的集合 ***/
//        HashMap<String, Integer> resultMap = approximateMatch(filterText, 0.60);
//        ArrayList<String> list = hashMapToArrayList(resultMap); //转ArrayList

        //自定义Adapter
        myAdapter = new MyAdapter(this);
        myAdapter.setList(list);

        lv1.setAdapter(myAdapter);
    }

    public void clearTextFilter() {
        List<String> list = new ArrayList<String>();
        myAdapter = new MyAdapter(this);
        myAdapter.setList(list);
        lv1.setAdapter(myAdapter);
    }

    /**
     * 读取words的方法，优先从数据库中搜索，没有在通过网络搜索
     */
    public void loadWords(String key) {
        words = wordsAction.getWordsFromSQLite(key);

        if ("" == words.getKey()) {
            String address = wordsAction.getAddressForWords(key);
            HttpUtil.sentHttpRequest(address, new HttpCallBackListener() {
                @Override
                public void onFinish(InputStream inputStream) {
                    WordsHandler wordsHandler = new WordsHandler();
                    ParseXML.parse(wordsHandler, inputStream);
                    words = wordsHandler.getWords();
                    wordsAction.saveWords(words);
                    wordsAction.saveWordsMP3(words);
                    handler.sendEmptyMessage(111);
                }

                @Override
                public void onError() {

                }
            });
        } else {
            upDateView();
        }
    }

    /**
     * 更新UI显示
     */
    public void upDateView() {
        if (words.getIsChinese()) {
            searchWords_posAcceptation.setText(words.getFy());
            searchWords_posA_layout.setVisibility(View.GONE);
            searchWords_posE_layout.setVisibility(View.GONE);
        } else {
            searchWords_posAcceptation.setText(words.getPosAcceptation());
            if (words.getPsE() != "") {
                searchWords_psE.setText(String.format(getResources().getString(R.string.psE), words.getPsE()));
                searchWords_posE_layout.setVisibility(View.VISIBLE);
            } else {
                searchWords_posE_layout.setVisibility(View.GONE);
            }
            if (words.getPsA() != "") {
                searchWords_psA.setText(String.format(getResources().getString(R.string.psA), words.getPsA()));
                searchWords_posA_layout.setVisibility(View.VISIBLE);
            } else {
                searchWords_posA_layout.setVisibility(View.GONE);
            }
        }
        searchWords_key.setText(words.getKey());
        searchWords_sent.setText(words.getSent());
        searchWords_linerLayout.setVisibility(View.VISIBLE);
    }

    //加载actionbar的菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.actionbar_layout_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }







    /**
     * 哈希转ArrayList
     *
     * @param hashMap
     */
    public static ArrayList<String> hashMapToArrayList(HashMap<String, Integer> hashMap) {
        ArrayList<String> result = new ArrayList<String>();
        // 遍历哈希表
        Iterator<Map.Entry<String, Integer>> entries = hashMap.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry<String, Integer> entry = entries.next();
            String word = entry.getKey();
            //Integer similarity = entry.getValue();
            result.add(word);
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

        for (int s = 0; s < wordlList.size(); s++) { //遍历字典
            String template = wordlList.get(s);
            Levenshtein lt = new Levenshtein();
            float similarity = lt.getSimilarityRatio(input, template);
            if (similarity >= d) {
                int value = (int) Math.floor(similarity * 100); //相似率转成整型, 向下取整, 如0.551转成55
                resultMap.put(template, value);
            }
        }
        // 排序哈希表
        HashMap<String, Integer> sortedMap = sortMap(resultMap);
        return sortedMap;
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
