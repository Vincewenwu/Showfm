package co.bankoo.zuweie.showfm.model;

public class ServiceEvent extends BaseEvent {

    public ServiceEvent(int what) {
        super(what);
    }

    public ServiceEvent(int what, Object data) {
        super(what, data);
    }
}