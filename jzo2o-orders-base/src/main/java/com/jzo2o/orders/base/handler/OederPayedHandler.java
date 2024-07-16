package com.jzo2o.orders.base.handler;

import com.jzo2o.common.expcetions.CommonException;
import com.jzo2o.orders.base.enums.OrderPayStatusEnum;
import com.jzo2o.orders.base.enums.OrderStatusEnum;
import com.jzo2o.orders.base.model.dto.OrderSnapshotDTO;
import com.jzo2o.orders.base.model.dto.OrderUpdateStatusDTO;
import com.jzo2o.orders.base.service.IOrdersCommonService;
import com.jzo2o.statemachine.core.StatusChangeEvent;
import com.jzo2o.statemachine.core.StatusChangeHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

//import static java.awt.Container.log

//支付成功之后要执行的动作
@Component("order_payed")//bean的名称规则：状态机名称——事件名称
@Slf4j
public class OederPayedHandler implements StatusChangeHandler<OrderSnapshotDTO> {

    @Resource
    private IOrdersCommonService ordersCommonService;
    @Override
    public void handler(String bizId, StatusChangeEvent statusChangeEventEnum, OrderSnapshotDTO bizSnapshot) {
        log.info("支付成功事件发生，执行此方法");
        //统一对订单状态进行更新，将订单状态由待支付改为派单中
        OrderUpdateStatusDTO orderUpdateStatusDTO = new OrderUpdateStatusDTO();
        orderUpdateStatusDTO.setId(bizSnapshot.getId());
        orderUpdateStatusDTO.setOriginStatus(OrderStatusEnum.NO_PAY.getStatus());
        orderUpdateStatusDTO.setTargetStatus(OrderStatusEnum.DISPATCHING.getStatus());
        orderUpdateStatusDTO.setPayStatus(OrderPayStatusEnum.PAY_SUCCESS.getStatus());
        orderUpdateStatusDTO.setTradingOrderNo(bizSnapshot.getTradingOrderNo());
        orderUpdateStatusDTO.setTransactionId(bizSnapshot.getThirdOrderId());
        orderUpdateStatusDTO.setPayTime(bizSnapshot.getPayTime());
        orderUpdateStatusDTO.setTradingChannel(bizSnapshot.getTradingChannel());
        Integer integer = ordersCommonService.updateStatus(orderUpdateStatusDTO);
        if(integer < 1){
            throw  new CommonException("订单" +  bizSnapshot.getSnapshotId() +" 支付成功事件执行动作失败");
        }


    }
}
