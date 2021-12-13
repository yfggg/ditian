package com.test.ditian.prolificacy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.function.Function;

@Component
@Slf4j
public class CommonTool2<T> {

    public <U extends Function<? super U, ? extends U>> U aa(U object, U function) {
        return Optional.ofNullable(object)
                .map(function)
                .orElseGet(()->{
            System.out.println("222");
            return null;
        });
    }

}
