package ek.osnb.demo.coffeeshop.ai.tools;

import ek.osnb.demo.coffeeshop.order.OrderApi;
import ek.osnb.demo.coffeeshop.order.OrderSummaryInfo;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderTools {
    private final OrderApi orders;

    public OrderTools(OrderApi orders) {
        this.orders = orders;
    }

    @Tool(description = "List the customer's own orders, newest first")
    List<OrderSummaryInfo> myOrders(ToolContext context){
        return orders.findOrders((Long) context.getContext().get("customerId"));
    }
}
