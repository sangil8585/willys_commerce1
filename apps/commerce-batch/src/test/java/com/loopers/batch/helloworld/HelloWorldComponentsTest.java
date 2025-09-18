package com.loopers.batch.helloworld;

import org.junit.jupiter.api.Test;
import org.springframework.batch.item.Chunk;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * HelloWorld 컴포넌트 단위 테스트
 * 데이터베이스 연결 없이 컴포넌트만 테스트
 */
class HelloWorldComponentsTest {

    @Test
    void testReader() throws Exception {
        // Given
        HelloWorldReader reader = new HelloWorldReader();

        // When & Then
        HelloWorldItem item1 = reader.read();
        assertThat(item1).isNotNull();
        assertThat(item1.getNumber()).isEqualTo(1);

        HelloWorldItem item2 = reader.read();
        assertThat(item2).isNotNull();
        assertThat(item2.getNumber()).isEqualTo(2);

        // 10개까지 읽기
        for (int i = 3; i <= 10; i++) {
            HelloWorldItem item = reader.read();
            assertThat(item).isNotNull();
            assertThat(item.getNumber()).isEqualTo(i);
        }

        // 11번째는 null
        HelloWorldItem item11 = reader.read();
        assertThat(item11).isNull();
    }

    @Test
    void testProcessor() throws Exception {
        // Given
        HelloWorldProcessor processor = new HelloWorldProcessor();
        HelloWorldItem input = new HelloWorldItem(5);

        // When
        HelloWorldItem output = processor.process(input);

        // Then
        assertThat(output).isNotNull();
        assertThat(output.getNumber()).isEqualTo(5);
        assertThat(output.getMessage()).isEqualTo("Hello World #5");
    }

    @Test
    void testWriter() throws Exception {
        // Given
        HelloWorldWriter writer = new HelloWorldWriter();
        Chunk<HelloWorldItem> chunk = new Chunk<>();

        HelloWorldItem item1 = new HelloWorldItem(1, "Hello World #1");
        HelloWorldItem item2 = new HelloWorldItem(2, "Hello World #2");
        chunk.add(item1);
        chunk.add(item2);

        // When (콘솔 출력 확인)
        writer.write(chunk);

        // Then - 별도 검증 없이 실행 완료 확인
        assertThat(chunk.getItems()).hasSize(2);
    }

    @Test
    void testItemClass() {
        // Given & When
        HelloWorldItem item = new HelloWorldItem(42, "Test Message");

        // Then
        assertThat(item.getNumber()).isEqualTo(42);
        assertThat(item.getMessage()).isEqualTo("Test Message");
    }
}
