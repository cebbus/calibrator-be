package com.cebbus.calibrator.filter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class SortWrapper {

    private SortWrapper() {
    }

    public static Sort valueOf(String orders) {
        if (orders == null || orders.equals("[]")) {
            return Sort.unsorted();
        }

        ObjectMapper mapper = new ObjectMapper();
        List<Sort.Order> orderList = new ArrayList<>();

        try {
            JsonNode jsonNode = mapper.readTree(orders);

            for (JsonNode node : jsonNode) {
                String property = node.get("property").asText();
                String direction = node.get("direction").asText();

                orderList.add(createOrder(property, direction));
            }

            return Sort.by(orderList);

        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }

        return Sort.unsorted();
    }

    private static Sort.Order createOrder(String property, String dir) {
        return new Sort.Order(Sort.Direction.fromString(dir), property);
    }
}
