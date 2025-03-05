package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryDto;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * xToOne (ManyToOne, OneToOne)
 * Order
 * Order <-> Member
 * Order <-> Delivery
 */
@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {
    private final OrderRepository orderRepository;
    private final OrderSimpleQueryRepository orderSimpleQueryRepository;

    @GetMapping("/api/v1/simple-orders")
    public List<Order> ordersV1() {
        List<Order> all = orderRepository.findAllByString(new OrderSearch());

        // Order > Member 참조 / Member > Order 참조
        // 양방향 참조여서 무한루프 발생 => @JsonIgnore 사용 필요

//        for(Order order : all) {
//            order.getMember().getName(); // Lazy 강제 초기화 - Member는 프록시 객체이지만, getName()을 사용하면 DB에 있는 데이터를 끌고옴
//            order.getDelivery().getAddress(); // Lazy 강제 초기화 - Delivery는 프록시 객체이지만, getAddress()을 사용하면 DB에 있는 데이터를 끌고옴
//        }
        return all;
    }

    @GetMapping("/api/v2/simple-orders")
    public List<SimpleOrderDto> ordersV2() {
        // ORDER 2개
        // 1(ORDER) + N(MEMBER) + N(DELIVERY) => 지연로딩 조회 N번 실행됨
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());

        List<SimpleOrderDto> result = orders.stream()
                .map(SimpleOrderDto::new)
                .collect(Collectors.toList());
        return result;
    }

    @GetMapping("/api/v3/simple-orders")
    public List<SimpleOrderDto> ordersV3() {
        List<Order> orders = orderRepository.findAllWithMemberDelivery();

        List<SimpleOrderDto> result = orders.stream().map(o -> new SimpleOrderDto(o)).collect(Collectors.toList());

        return result;
    }

    @GetMapping("/api/v4/simple-orders") // Dto로 바로 조회하기 // 재사용성은 v3이 더 뛰어남 (DTO는 제한된 항목 조회)
    public List<OrderSimpleQueryDto> ordersV4() {
//        List<OrderSimpleQueryDto> result = orderRepository.findOrderDtos();
        
        // 성능 최적화 목적의 Repository 패키지 별도 생성해서 사용
        // => 화면 최적화 or API 스펙 최적화 등 목적성이 있는 쿼리는 별도 패키지에 관리하는 것이 유지보수 측면에서 좋음
        List<OrderSimpleQueryDto> result = orderSimpleQueryRepository.findOrderDtos();

        return result;
    }

    @Data
    static class SimpleOrderDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;

        public SimpleOrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName(); // LAZY 초기화
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress(); // LAZY 초기화
        }
    }
}
