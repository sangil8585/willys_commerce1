package com.loopers.batch.helloworld;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;
import org.springframework.stereotype.Component;

/**
 * HelloWorld 배치 Reader
 * 1부터 10까지의 숫자를 생성하여 반환
 */
@Slf4j
@Component
public class HelloWorldReader implements ItemReader<HelloWorldItem> {

    private int count = 1;
    private static final int MAX_COUNT = 10;

    @Override
    public HelloWorldItem read() throws Exception {
        if (count > MAX_COUNT) {
            log.info("Reader finished: all items processed");
            return null; // null 반환 시 배치 종료
        }

        HelloWorldItem item = new HelloWorldItem(count);
        log.info("Reader created item: {}", item);
        count++;

        return item;
    }
}
