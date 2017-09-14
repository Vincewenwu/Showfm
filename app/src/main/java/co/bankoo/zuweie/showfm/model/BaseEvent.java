package co.bankoo.zuweie.showfm.model;
public class BaseEvent {
    public BaseEvent (int what) {
        this.what = what;
    }

    public BaseEvent (int what, Object data) {
        this.what = what;
        this.data = data;
    }

    public int what;
    public Object data;
}