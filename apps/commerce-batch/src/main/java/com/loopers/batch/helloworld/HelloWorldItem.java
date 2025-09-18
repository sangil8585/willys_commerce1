package com.loopers.batch.helloworld;

/**
 * HelloWorld 배치에서 사용할 데이터 클래스
 */
public class HelloWorldItem {

    private final Integer number;
    private String message;

    public HelloWorldItem(Integer number) {
        this.number = number;
    }

    public HelloWorldItem(Integer number, String message) {
        this.number = number;
        this.message = message;
    }

    public Integer getNumber() {
        return number;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "HelloWorldItem{" +
                "number=" + number +
                ", message='" + message + '\'' +
                '}';
    }
}
