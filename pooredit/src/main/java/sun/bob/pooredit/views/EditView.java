package sun.bob.pooredit.views;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import sun.bob.pooredit.beans.ElementBean;
import sun.bob.pooredit.utils.Constants;

/**
 * Created by bob.sun on 15/11/26.
 */
public class EditView extends LinearLayout {

    public static BaseContainer editing = null;
    protected static EditView instance;
    public int currentIndex = 0;
    public EditView(Context context) {
        super(context);
        initUI();
    }

    public EditView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initUI();
    }

    private void initUI(){
        this.setOrientation(VERTICAL);
        instance = this;

        append(new Text(getContext()));

    }

    private Text addTextOn(int index){
        Text text = new Text(getContext());
        this.addView(text, index);
        return text;
    }

    private Image addImageOn(int index){
        Image image = new Image(getContext());
        this.addView(image, index);
        this.addView(new Text(getContext()));
        return image;
    }

    private Todo addTodoOn(int index){
        Todo todo = new Todo(getContext());
        this.addView(todo, index);
        this.addView(new Text(getContext()));
        return todo;
    }

    private sun.bob.pooredit.views.File addFileOn(int index){
        sun.bob.pooredit.views.File file = new sun.bob.pooredit.views.File(getContext());
        this.addView(file, index);
        this.addView(new Text(getContext()));
        return file;
    }
    protected void append(BaseContainer e){
        this.addView(e);
        currentIndex++;
    }

    public String exportJSON(String where){
        File file = new File(where);
        if (!file.exists()){
            file.mkdirs();
        }
        file = new File(where + "content.json");
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ArrayList<Object> content = new ArrayList<>();
        Gson gson = new Gson();
        BaseContainer e;
        int empty = 0;
        for (int i = 0; i < getChildCount(); i++){
            e = (BaseContainer) getChildAt(i);
            switch (e.getType()){
                case Constants.TYPE_TEXT:
                    if (!e.isEmpty()) {
                        content.add(((ElementBean) e.getJsonBean()).setIndex(i - empty));
                    } else {
                        empty ++;
                    }
                    break;
                case Constants.TYPE_IMAGE:
                    if (!e.isEmpty()){
                        content.add(((ElementBean)e.getJsonBean()).setIndex(i - empty));
                    } else {
                        empty++;
                    }
                    break;
                case Constants.TYPE_TODO:
                    if (!e.isEmpty()) {
                        content.add(((ElementBean) e.getJsonBean()).setIndex(i - empty));
                    } else {
                        empty ++;
                    }
                    break;
                case Constants.TYPE_ATT:
                    if (!e.isEmpty()) {
                        content.add(((ElementBean) e.getJsonBean()).setIndex(i - empty));
                    } else {
                        empty ++;
                    }
                    break;
                default:
                    break;
            }
        }
        FileWriter dos = null;
        try {
            dos = new FileWriter(file);
            dos.write(gson.toJson(content));
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        } finally {
            if (dos != null)
                try {
                    dos.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
        }

        return file.getName();
    }

    public void loadJson(String folderPath){
        String content = "";
        File file = new File(folderPath + "content.json");
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader(file));
            String read;
            while ((read = bufferedReader.readLine()) != null){
                content += read;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bufferedReader != null)
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        this.removeAllViews();
        ArrayList<LinkedTreeMap> beans = new Gson().fromJson(content, ArrayList.class);
        if (beans == null){
            return;
        }
        for (LinkedTreeMap<String, Object> bean : beans){
            //Fuck me.
            int type = (int) Math.round((Double) bean.get("type"));
            switch (type){
                case Constants.TYPE_TEXT:
                    Text text = addTextOn((int) Math.round((Double) bean.get("index")));
                    SpannableString spannableString = new SpannableString((String) bean.get("text"));
                    int length = spannableString.length();
                    ArrayList<LinkedTreeMap> spans = (ArrayList) bean.get("spans");
                    ArrayList styles = (ArrayList<Integer>) bean.get("styles");
                    int s, e;
                    for (int i = 0; i < styles.size(); i++){
                        int style = (int) Math.round((Double)styles.get(i));
                        s = (int) Math.round((Double) spans.get(i).get("start"));
                        e = (int) Math.round((Double) spans.get(i).get("end"));
                        if (s >= length || e >= length){
                            continue;
                        }
                        switch (style){
                            case ToolBar.StyleButton.BOLD:
                                spannableString.setSpan(new StyleSpan(Typeface.BOLD), s, e, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                break;
                            case ToolBar.StyleButton.ITALIC:
                                spannableString.setSpan(new StyleSpan(Typeface.ITALIC), s, e, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                break;
                            case ToolBar.StyleButton.BOLD + ToolBar.StyleButton.ITALIC:
                                spannableString.setSpan(new StyleSpan(Typeface.BOLD_ITALIC), s, e, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                break;
                            default:
                                break;
                        }
                    }
                    text.setText(spannableString);

                    break;
                case Constants.TYPE_IMAGE:
                    Image img = addImageOn((int) Math.round((Double) bean.get("index")));
                    img.setImage((String) bean.get("imgPath"),(int) Math.round((Double) bean.get("width")) );
                    break;
                case Constants.TYPE_TODO:
                    Todo todo = addTodoOn((int) Math.round((Double) bean.get("index")));
                    SpannableString ssTodo = new SpannableString((String) bean.get("text"));
                    int lenTodo = ssTodo.length();
                    ArrayList<LinkedTreeMap> spansTodo = (ArrayList) bean.get("spans");
                    ArrayList stylesTodo = (ArrayList<Integer>) bean.get("styles");
                    int st, et;
                    for (int i = 0; i < stylesTodo.size(); i++){
                        int style = (int) Math.round((Double)stylesTodo.get(i));
                        st = (int) Math.round((Double) spansTodo.get(i).get("start"));
                        et = (int) Math.round((Double) spansTodo.get(i).get("end"));
                        if (st >= lenTodo || et >= lenTodo){
                            continue;
                        }
                        switch (style){
                            case ToolBar.StyleButton.BOLD:
                                ssTodo.setSpan(new StyleSpan(Typeface.BOLD), st, et, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                break;
                            case ToolBar.StyleButton.ITALIC:
                                ssTodo.setSpan(new StyleSpan(Typeface.ITALIC), st, et, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                break;
                            case ToolBar.StyleButton.BOLD + ToolBar.StyleButton.ITALIC:
                                ssTodo.setSpan(new StyleSpan(Typeface.BOLD_ITALIC), st, et, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                break;
                            default:
                                break;
                        }
                    }
                    todo.setText(ssTodo);
                    todo.setChecked((Boolean) bean.get("checked"));
                    break;
                case Constants.TYPE_ATT:
                    sun.bob.pooredit.views.File fileView = addFileOn((int) Math.round((Double) bean.get("index")));
                    fileView.setFilePath((String) bean.get("filePath"));
                    break;
                default:
                    break;
            }
        }

    }
}