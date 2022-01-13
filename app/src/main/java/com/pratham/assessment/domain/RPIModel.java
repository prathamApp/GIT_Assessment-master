package com.pratham.assessment.domain;

import org.json.JSONArray;

public class RPIModel {
    public int count;
    public Object next;
    public Object previous;
    public JSONArray results;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public Object getNext() {
        return next;
    }

    public void setNext(Object next) {
        this.next = next;
    }

    public Object getPrevious() {
        return previous;
    }

    public void setPrevious(Object previous) {
        this.previous = previous;
    }

    public JSONArray getResults() {
        return results;
    }

    public void setResults(JSONArray results) {
        this.results = results;
    }
}
