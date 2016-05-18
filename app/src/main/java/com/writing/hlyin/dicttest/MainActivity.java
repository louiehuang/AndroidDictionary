package com.writing.hlyin.dicttest;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.writing.hlyin.dicttest.adapter.MyAdapter;
import com.writing.hlyin.dicttest.adapter.SpinnerAdapter;
import com.writing.hlyin.dicttest.algorithm.matchAlgorithm;
import com.writing.hlyin.dicttest.model.Words;
import com.writing.hlyin.dicttest.util.HttpCallBackListener;
import com.writing.hlyin.dicttest.util.HttpUtil;
import com.writing.hlyin.dicttest.util.ParseJSON;
import com.writing.hlyin.dicttest.util.ParseXML;
import com.writing.hlyin.dicttest.util.WordsAction;
import com.writing.hlyin.dicttest.util.WordsHandler;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * Created by hlyin on 2016/5/15.
 * 查词界面
 */
public class MainActivity extends Activity {

    /**
     * 匹配模式，1~4分别表示正向匹配、逆向匹配、包含匹配、k近似匹配
     */
    private int matchMode;

    /**
     * 候选词ListView
     */
    private ListView candidate_listView;

    /**
     * 候选词ListView的adapter
     */
    private MyAdapter myAdapter;

    /**
     * 全部词库中词所组成的List
     */
    private static ArrayList<String> wordList;

    /**
     * 模式选择Spinner
     */
    private Spinner matchMode_spinner;

    /**
     * 单词搜索框
     */
    private SearchView searchView;

    /**
     * 每日一句的图片
     */
    private ImageView daily_ImageView;

    /**
     * 每日一句、翻译、阅读量、日期
     */
    private TextView publishTime_value_textView, viewCount_value_textView, dailySent_value_textView, dailyTrans_value_textView;

    /**
     * 查询词、英式音标、美式音标、释义、例句TextView
     */
    private TextView searchWords_key, searchWords_psE, searchWords_psA, searchWords_posAcceptation, searchWords_sent;

    /**
     * 英式和美式读音的图标按钮
     */
    private ImageButton searchWords_voiceE, searchWords_voiceA;

    /**
     * 英式音标、美式音标、搜索词显示、最外层布局
     */
    private LinearLayout searchWords_posA_layout, searchWords_posE_layout, searchWords_linerLayout, searchWords_fatherLayout;


    private ScrollView home_scrollView;

    /**
     * 单词行为类
     */
    private WordsAction wordsAction;

    private Words words = new Words(); //选中的单词

    /**
     * 网络查词完成后回调handleMessage方法，更新UI显示或提示查不到词
     */
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 111:
                    //判断网络查找不到该词的情况
                    if (words.getSent().length() > 0) { //有查询结果
                        upDateView(); //更新显示
                        Log.d("测试", "查询成功");
                    } else { //查不到
                        //隐藏查询结果布局（隐藏上一次查词结果）
                        searchWords_linerLayout.setVisibility(View.GONE);
                        Toast.makeText(MainActivity.this, "抱歉，找不到该词！", Toast.LENGTH_SHORT).show();
                        Log.d("测试", "查不到结果");
                    }
                    break;
                case 123:
                    Toast.makeText(MainActivity.this, "请检查联网情况", Toast.LENGTH_SHORT).show();

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*初始化每日一句布局*/
        publishTime_value_textView = (TextView) findViewById(R.id.publishTime_value_textView);
        viewCount_value_textView = (TextView) findViewById(R.id.viewCount_value_textView);
        dailySent_value_textView = (TextView) findViewById(R.id.dailySent_value_textView);
        dailyTrans_value_textView = (TextView) findViewById(R.id.dailyTrans_value_textView);
        /*加载每日一句信息*/
        upDateDailyView();


        wordsAction = WordsAction.getInstance(this);

        /*初始化基本布局*/
        daily_ImageView = (ImageView) findViewById(R.id.daily_ImageView);
        /*随机初始化daily_ImageView图片*/
        Random random = new Random();
        int result = random.nextInt(5) + 1; //1~5随机数
        int resId = R.drawable.img1;
        switch (result){
            case 1 : resId = R.drawable.img1; break;
            case 2 : resId = R.drawable.img2; break;
            case 3 : resId = R.drawable.img3; break;
            case 4 : resId = R.drawable.img4; break;
            default : resId = R.drawable.img5; break;
        }
        daily_ImageView.setImageResource(resId);


        home_scrollView = (ScrollView) findViewById(R.id.home_scrollView);
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

        /*初始化基本控件*/
        searchWords_key = (TextView) findViewById(R.id.searchWords_key);
        searchWords_psE = (TextView) findViewById(R.id.searchWords_psE);
        searchWords_psA = (TextView) findViewById(R.id.searchWords_psA);
        searchWords_posAcceptation = (TextView) findViewById(R.id.searchWords_posAcceptation);
        searchWords_sent = (TextView) findViewById(R.id.searchWords_sent);
        searchWords_voiceE = (ImageButton) findViewById(R.id.searchWords_voiceE);
        //（英式）点击读音按钮播放读音
        searchWords_voiceE.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wordsAction.playMP3(words.getKey(), "E", MainActivity.this);
            }
        });
        searchWords_voiceA = (ImageButton) findViewById(R.id.searchWords_voiceA);
        //（英式）点击读音按钮播放读音
        searchWords_voiceA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wordsAction.playMP3(words.getKey(), "A", MainActivity.this);
            }
        });

        /*Spinner控件，自定义布局*/
        matchMode_spinner = (Spinner) findViewById(R.id.matchMode_spinner); //匹配模式
        String [] mStringArray = getResources().getStringArray(R.array.spingarr);
        //使用自定义的ArrayAdapter
        SpinnerAdapter sAdapter = new SpinnerAdapter(MainActivity.this, mStringArray);
        //设置下拉列表风格
        //mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        matchMode_spinner.setAdapter(sAdapter);
        //监听Spinner
        matchMode_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                matchMode = position + 1; //更新选择的匹配模式
                Log.d("selected mode: ", "" + matchMode);
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // 没有选项时触发
            }
        });

        /*初始化显示候选词的ListView*/
        candidate_listView = (ListView) findViewById(R.id.candidate_listView);
        //初始时不可见，在SearchView输入后可见，SeachView为空时再次不可见
        candidate_listView.setVisibility(View.GONE);
        //监听点击子项事件，点击后进行查词并由点击项补全SearchView的Query显示
        candidate_listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String word = candidate_listView.getItemAtPosition(position).toString(); //apple
                Log.d("测试", "click: " + word + ", position: " + position);

                //点击子项时同时也触发了SearchView的onQueryTextSubmit？
                //执行了两次loadWords
                loadWords(word); //加载选择词（调API查询并更新UI）
                //补全searchView
                searchView.setQuery(word, true);
                //选择后显示单词释义，此时隐藏candidate_listView
                candidate_listView.setVisibility(View.GONE);
            }
        });

        /*将词库单词读入wordList存放*/
        InitWordList();


        /*搜索框searchView初始化及设置*/
        searchView = (SearchView) findViewById(R.id.searchWords_searchView);
        //更改字体颜色，通过修改系统的search_src_text
        int id = searchView.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);
        TextView textView = (TextView) searchView.findViewById(id);
        textView.setTextColor(Color.WHITE); //搜索框文字显示颜色
        textView.setHintTextColor(Color.GRAY); //搜索框提示字体颜色
        //设置
        searchView.setSubmitButtonEnabled(true);//设置显示搜索按钮
        searchView.setIconifiedByDefault(false);//设置不自动缩小为图标
        //监听SearchView变化
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            //提交搜索
            @Override
            public boolean onQueryTextSubmit(String query) {
                home_scrollView.setVisibility(View.GONE);
                candidate_listView.setVisibility(View.GONE); //隐藏候选词listView
                loadWords(query);
                return true;
            }

            //查询内容改变
            @Override
            public boolean onQueryTextChange(String newText) {
                home_scrollView.setVisibility(View.GONE);
                candidate_listView.setVisibility(View.VISIBLE); //显示候选词listView
                searchWords_linerLayout.setVisibility(View.GONE); //隐藏上一次的查询结果

                if (newText.length() != 0) {
                    setFilterText(newText); //查询
                } else {
                    clearTextFilter(); //清空
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
        //Alt + Enter 快速导入包; Alt + command + L 快速排版
        List<String> list = new ArrayList<String>(); //匹配结果List，用于adapter内容赋值
        HashMap<String, Integer> resultMap; //匹配结果哈希表，存放<单词，匹配度>，按匹配度降序排
        matchAlgorithm match = new matchAlgorithm(wordList); //matchAlgorithm类中提供4种匹配方法

        switch (matchMode) {
            //正向匹配
            case 1:
                resultMap = match.matchFromBeginning(filterText, 1);
                list = match.hashMapToArrayList(resultMap); //转ArrayList
                break;
            case 2:
                resultMap = match.matchFromEnd(filterText, 1);
                list = match.hashMapToArrayList(resultMap); //转ArrayList
                break;
            //包含匹配
            case 3:
                for (int i = 0; i < wordList.size(); i++) {
                    if (wordList.get(i).contains(filterText)) {
                        list.add(wordList.get(i));
                    }
                }
                break;
            //k近似匹配
            default: //k = 4
                /*** 模糊匹配，找相似率大于k(0.60)的集合 ***/
                resultMap = match.approximateMatch(filterText, 0.60);
                list = match.hashMapToArrayList(resultMap); //转ArrayList
                break;
        }

        //自定义Adapter
        myAdapter = new MyAdapter(this);
        myAdapter.setList(list);

        candidate_listView.setAdapter(myAdapter);
    }

    /**
     * 清空候选词listView的显示
     */
    public void clearTextFilter() {
        //这里可以隐藏上一次查询结果布局


        home_scrollView.setVisibility(View.VISIBLE);

        //清空候选词listView，给其赋1个空List
        List<String> list = new ArrayList<String>();
        myAdapter = new MyAdapter(this);
        myAdapter.setList(list);
        candidate_listView.setAdapter(myAdapter);
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
                    handler.sendEmptyMessage(123);
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
            //查询词是汉语，则释义显示翻译，且不显示音标
            searchWords_posAcceptation.setText(words.getFy());
            searchWords_posA_layout.setVisibility(View.GONE);
            searchWords_posE_layout.setVisibility(View.GONE);
        } else {
            //查询词是英语
            searchWords_posAcceptation.setText(words.getPosAcceptation()); //汉语释义
            if (words.getPsE() != "") { //有英式音标
                searchWords_psE.setText(String.format(getResources().getString(R.string.psE), words.getPsE()));
                searchWords_posE_layout.setVisibility(View.VISIBLE);
            } else {
                searchWords_posE_layout.setVisibility(View.GONE);
            }

            if (words.getPsA() != "") { //有美式音标
                searchWords_psA.setText(String.format(getResources().getString(R.string.psA), words.getPsA()));
                searchWords_posA_layout.setVisibility(View.VISIBLE);
            } else {
                searchWords_posA_layout.setVisibility(View.GONE);
            }
        }
        searchWords_key.setText(words.getKey()); //显示查询词
        searchWords_sent.setText(words.getSent()); //显示例句
        searchWords_linerLayout.setVisibility(View.VISIBLE); //设置整个查询结果可见
    }


    /**
     * 更新每日一句（打开程序时更新）
     */
    public void upDateDailyView() {
        String finalUrl = "http://open.iciba.com/dsapi/";
        ParseJSON obj = new ParseJSON(finalUrl);
        obj.fetchJSON();

        while(obj.parsingComplete);

        publishTime_value_textView.setText(obj.getPublishTime());
        viewCount_value_textView.setText(obj.getViewCount());
        dailySent_value_textView.setText(obj.getEnglish());
        dailyTrans_value_textView.setText(obj.getTranslation());
    }


    //加载actionbar的菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.actionbar_layout_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }


    /**
     * 从词库读取单词，初始化wordList
     */
    public void InitWordList() {
        wordList = new ArrayList<String>();
        try {
            //得到资源中的Raw数据流
            InputStream in = getResources().openRawResource(R.raw.words);
            InputStreamReader reader = new InputStreamReader(in);
            BufferedReader bufferedReader = new BufferedReader(reader);
            // 按行读文件
            String line = null; // 文件行
            while ((line = bufferedReader.readLine()) != null) {
                line = line.trim();
                wordList.add(line); // 1行1词，所以直接添加即可
            }
            bufferedReader.close();
            reader.close();
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



}
