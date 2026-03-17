package cn.bugstack.test;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @program: openai_code_review
 * @description:
 * @author: lcz
 * @create: 2026-03-17 11:13
 **/
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class ApplicationTest {

    @Test
    public void test(){
        System.out.println(Integer.parseInt("aaaa"));
    }
}
