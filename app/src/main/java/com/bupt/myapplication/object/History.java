package com.bupt.myapplication.object;

//从后端查到的历史提交记录类型，还没研究明白具体应该包含什么信息
public class History {

    private int id;
    private String time;
    private int score;
    private String answerSheet;

    public History() {
    }

    public History(int id, String time, int score, String answerSheet) {
        this.id = id;
        this.time = time;
        this.score = score;
        this.answerSheet = answerSheet;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getAnswerSheet() {
        return answerSheet;
    }

    public void setAnswerSheet(String answerSheet) {
        this.answerSheet = answerSheet;
    }
}