package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.order.query.OrderFlatDto;
import jpabook.jpashop.repository.order.query.OrderItemQueryDto;
import jpabook.jpashop.repository.order.query.OrderQueryDto;
import jpabook.jpashop.repository.order.query.OrderQueryRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

@RestController
@RequiredArgsConstructor
public class OrderApiController {
    private final OrderRepository orderRepository;
    private final OrderQueryRepository orderQueryRepository;

    // 1. 엔티티 사용
    @GetMapping("/api/v1/orders")
    public List<Order> ordersV1() {
        List<Order> all = orderRepository.findAllByString(new OrderSearch());

        // LAZY LOADING 초기화
        for (Order order : all) {
            order.getMember().getName(); // (참고) `spring.jpa.open-in-view: false` 설정 시 지연로딩 프록시 객체 초기화가 불가능
            order.getDelivery().getAddress();
            List<OrderItem> orderItems = order.getOrderItems();

//            for (OrderItem orderItem : orderItems) {
//                orderItem.getItem().getName();
//            }
            orderItems.stream().forEach(o -> o.getItem().getName());
        }

        return all;
    }

    // 2. 엔티티 => DTO로 변환
    @GetMapping("/api/v2/orders")
    public List<OrderDto> ordersV2() {
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());

        List<OrderDto> collect = orders.stream()
                .map(o -> new OrderDto(o))
                .collect(toList());

        return collect;
    }

    // 2. 엔티티 => DTO로 변환 => fetch join 적용
    @GetMapping("/api/v3/orders")
    public List<OrderDto> ordersV3() {
        List<Order> orders = orderRepository.findAllWithItem();

        for (Order order : orders) {
            System.out.println("order ref = " + order + " id = " + order.getId());
        }

        List<OrderDto> collect = orders.stream()
                .map(o -> new OrderDto(o))
                .collect(toList());

        return collect;
    }

    @GetMapping("/api/v3.1/orders")
    public List<OrderDto> ordersV3_page(@RequestParam(value="offset", defaultValue = "0") int offset,
                                        @RequestParam(value="limit", defaultValue = "100") int limit
                                        ) {
        List<Order> orders = orderRepository.findAllWithMemberDelivery_page(offset, limit);

        List<OrderDto> collect = orders.stream()
                .map(o -> new OrderDto(o))
                .collect(toList());

        return collect;
    }

    @GetMapping("/api/v4/orders")
    public List<OrderQueryDto> ordersV4() {
        return orderQueryRepository.findOrderQueryDtos();
    }

    @GetMapping("/api/v5/orders") // => 권장 방법
    public List<OrderQueryDto> ordersV5() {
        return orderQueryRepository.findAllByDto_optimization();
    }

    @GetMapping("/api/v6/orders")
    public List<OrderQueryDto> ordersV6() {
        List<OrderFlatDto> flats = orderQueryRepository.findAllByDto_flat();

        // OrderFlatDto => OrderQueryDto로 변환 시 stream을 사용하면 아래와 같이 복잡한 로직이 필요
        return flats.stream()
                .collect(groupingBy(o -> new OrderQueryDto(o.getOrderId(), o.getName(), o.getOrderDate(), o.getOrderStatus(), o.getAddress()),
                        mapping(o -> new OrderItemQueryDto(o.getOrderId(), o.getItemName(), o.getOrderPrice(), o.getCount()), toList())
                )).entrySet().stream()
                .map(e -> new OrderQueryDto(e.getKey().getOrderId(), e.getKey().getName(), e.getKey().getOrderDate(), e.getKey().getOrderStatus(), e.getKey().getAddress(), e.getValue())).collect(toList());

    }

    @Data
    static class OrderDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;
        //private List<OrderItem> orderItems;
        private List<OrderItemDto> orderItems;

        public OrderDto(Order o) {
            orderId = o.getId();
            name = o.getMember().getName();
            orderDate = o.getOrderDate();
            orderStatus = o.getStatus();
            address = o.getDelivery().getAddress();
//            orderItems = o.getOrderItems(); // (1) 이대로만 사용하면 OrderItems는 null 출력

//            o.getOrderItems().stream().forEach(s -> s.getItem().getName()); // (2) LAZY LOADING 으로 인한 프록시 초기화
//            orderItems = o.getOrderItems();

            orderItems = o.getOrderItems().stream().map(orderItem -> new OrderItemDto(orderItem)).collect(toList());
        }
    }

    @Data
    static class OrderItemDto {
        private String itemName; // 상품명
        private int orderPrice; // 주문 가격
        private int count; // 주문 수량

        public OrderItemDto(OrderItem orderItem) {
            itemName = orderItem.getItem().getName();
            orderPrice = orderItem.getOrderPrice();
            count = orderItem.getCount();
        }
    }
}
