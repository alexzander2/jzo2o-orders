package com.jzo2o.orders.manager.listener;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.jzo2o.api.trade.enums.TradingStateEnum;
import com.jzo2o.common.constants.MqConstants;
import com.jzo2o.common.model.msg.TradeStatusMsg;
import com.jzo2o.orders.manager.service.IOrdersCreateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

//监听MQ获取支付结果
@Component
@Slf4j
public class TradeStatusListener {

    @Resource
    private IOrdersCreateService ordersCreateService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = MqConstants.Queues.ORDERS_TRADE_UPDATE_STATUS),
            exchange = @Exchange(name = MqConstants.Exchanges.TRADE, type = ExchangeTypes.TOPIC),
            key = MqConstants.RoutingKeys.TRADE_UPDATE_STATUS
    ))
    public void listenTradeUpdatePayStatusMsg(String msg) {
        log.info("接收到支付结果状态的消息 ({})-> {}", MqConstants.Queues.ORDERS_TRADE_UPDATE_STATUS, msg);
        //将msg转为java对象
        List<TradeStatusMsg> tradeStatusMsgs = JSON.parseArray(msg, TradeStatusMsg.class);

        // 只处理家政服务的订单且是支付成功的
        List<TradeStatusMsg> msgList = tradeStatusMsgs.stream().filter(v -> v.getStatusCode().equals(TradingStateEnum.YJS.getCode()) && "jzo2o.orders".equals(v.getProductAppId())).collect(Collectors.toList());
        if (CollUtil.isEmpty(msgList)) {
            return;
        }

        //修改订单状态
        msgList.forEach(m -> ordersCreateService.paySuccess(m));
    }
}
