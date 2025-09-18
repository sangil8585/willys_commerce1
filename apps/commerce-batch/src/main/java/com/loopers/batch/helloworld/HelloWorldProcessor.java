package com.loopers.batch.helloworld;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

/**
 * HelloWorld 배치 Processor
 * 숫자에 "Hello World #" 접두사를 붙여서 변환
 */
@Slf4j
@Component
public class HelloWorldProcessor implements ItemProcessor<HelloWorldItem, HelloWorldItem> {

    @Override
    public HelloWorldItem process(HelloWorldItem item) throws Exception {
        String message = "Hello World #" + item.getNumber();
        HelloWorldItem processedItem = new HelloWorldItem(item.getNumber(), message);

        log.info("Processor transformed: {} -> {}", item, processedItem);
        return processedItem;
    }
}
