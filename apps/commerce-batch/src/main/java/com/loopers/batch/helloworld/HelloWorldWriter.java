package com.loopers.batch.helloworld;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * HelloWorld 배치 Writer
 * 처리된 아이템들을 콘솔에 출력
 */
@Slf4j
@Component
public class HelloWorldWriter implements ItemWriter<HelloWorldItem> {

    @Override
    public void write(Chunk<? extends HelloWorldItem> chunk) throws Exception {
        List<? extends HelloWorldItem> items = chunk.getItems();

        log.info("Writer processing chunk with {} items", items.size());

        for (HelloWorldItem item : items) {
            log.info("Writing item: {}", item);
            System.out.println("🎉 " + item.getMessage());
        }

        log.info("Writer completed processing chunk");
    }
}
