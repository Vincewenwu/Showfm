package co.bankoo.zuweie.showfm.ctrl;

import android.util.SparseArray;

import co.bankoo.zuweie.showfm.model.BaseEvent;
import co.bankoo.zuweie.showfm.model.IHandler;

public class HandlerMapping {
    public HandlerMapping(SparseArray<IHandler> funcs)  {
        this.m_functions =funcs;
    }

    public void addFunc (int what, IHandler handler) {
        m_functions.append(what, handler);
    }

    public int exeFunc (BaseEvent event) {
        IHandler iHandler = m_functions.get(event.what);
        if (iHandler != null) {
            return iHandler.handleFunc(event);
        }else{
            return -1;
        }
    }

    SparseArray<IHandler> m_functions;
}