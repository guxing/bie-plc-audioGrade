package com.example.winbo.audiotest6.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.SPUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Luosiwei on 2017/9/5.
 */

public class MyAsynTask extends AsyncTask<Void, Integer, String> {
    private TextView content_tv;
    private Context mContext;
    private TextView tv_result;

    public MyAsynTask(TextView content_tv, Context context, TextView tv_result) {
        this.content_tv = content_tv;
        this.mContext = context;
        this.tv_result = tv_result;
    }

    @Override
    protected String doInBackground(Void... voids) {
        return ResetAPIUtils.getRecognizeResult();
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        try {
            JSONObject resultJsobject = new JSONObject(s);
            if (!resultJsobject.isNull("result")) {
                JSONArray recognizeJsonArray = resultJsobject.getJSONArray("result");
                String result = recognizeJsonArray.getString(0);
                if (!SPUtils.getInstance().getBoolean("istext")) {
                    // 识别唱名模式
                    StringBuilder sb = new StringBuilder();
                    for (int j = 0; j < result.length(); j++) {
                        char item = result.charAt(j);
                        String str = String.valueOf(item);
                        int indexDo = "得的到兜都斗抖陡蚪斗蝌豆读逗窦痘咄多哆夺度铎踱朵堕剁憜躲舵跺德道堵度读嘟".indexOf(str);
                        int indexRe = "a来y类瑞锐睿磊雷泪累蕾软".indexOf(str);
                        int indexMi = "咪眯瞇弥迷米芈弭你秘密".indexOf(str);
                        int indexFa = "发乏阀罚法笩筏砝".indexOf(str);
                        int indexSol = "叔骚收说朔铄硕欶搠碩色涩啬瑟塞嗖唆手搜索嗽叟搜嗖馊溲飕艘叟擞莎莏唆娑梭缩所唢索琐锁嗦苏俗玊夙诉速素宿谡粟诉塑溯缩".indexOf(str);
                        int indexLa = "拉垃啦喇辣了".indexOf(str);
                        int indexSi = "c夕兮吸西希析昔习席媳洗喜徙".indexOf(str);
                        if (indexDo != -1) {
                            sb.append("do ");
                        } else if (indexRe != -1) {
                            sb.append("re ");
                        } else if (indexMi != -1) {
                            sb.append("mi ");
                        } else if (indexFa != -1) {
                            sb.append("fa ");
                        } else if (indexSol != -1) {
                            sb.append("sol ");
                        } else if (indexLa != -1) {
                            sb.append("la ");
                        } else if (indexSi != -1) {
                            sb.append("si ");
                        } else {
                            if (str.equals("r")) {
                                sb.append("r");
                            } else if (str.equals("e")) {
                                sb.append("e");
                            } else if (str.equals(",")) {
                                sb.append("");
                            } else {
                                sb.append("x" + "");
                            }
                        }
                    }
                    content_tv.setText(sb.toString());
                } else {
                    // 识别歌词模式
                    content_tv.setText(result);
                }
                tv_result.setText(result);
                if (SPUtils.getInstance().getBoolean("displayresult")) {
                    tv_result.setVisibility(View.VISIBLE);
                } else {
                    tv_result.setVisibility(View.GONE);
                }
            } else {
                content_tv.setText("耳机录音效果最棒哦～");
                Toast.makeText(mContext, "唱名识别失败", Toast.LENGTH_SHORT).show();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
