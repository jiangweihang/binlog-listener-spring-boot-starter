package org.binlog.listener.tactics.impl;

import com.github.shyiko.mysql.binlog.event.Event;
import com.github.shyiko.mysql.binlog.event.EventType;
import org.binlog.listener.tactics.BinLogListener;

import static com.github.shyiko.mysql.binlog.event.EventType.*;

/**
 * 这个监听器只做两件事，判断用的是ROW模式还是STATEMENT模式
 * 根据不同的模式，调用不同的监听器处理事件
 *
 * @Author: Emiya
 * @Date: 2026/4/3 21:38
 * @Description: MIXED模式监听器
 */
public class MixedTypeBinLogListener implements BinLogListener {

    private final RowTypeBinLogListener rowListener = new RowTypeBinLogListener();

    private final StatementTypeBinLogListener statementListener = new StatementTypeBinLogListener();

    @Override
    public void onEvent(Event event) {
        EventType eventType = event.getHeader().getEventType();
        if (eventType.equals(EventType.QUERY)) {
            statementListener.onEvent(event);
        } else if (eventType.equals(EventType.TABLE_MAP) || isWrite(eventType)
                || isUpdate(eventType) || isDelete(eventType)) {
            rowListener.onEvent(event);
        }
    }

}
