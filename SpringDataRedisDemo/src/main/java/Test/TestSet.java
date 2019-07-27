package Test;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Set;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring/applicationContext-redis.xml")
public class TestSet {

    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    public void setValue() {
        redisTemplate.boundSetOps("nameSet").add("曹操");
        redisTemplate.boundSetOps("nameSet").add("刘备");
        redisTemplate.boundSetOps("nameSet").add("孙权");
    }

    @Test
    public void getValue(){
        Set nameSet = redisTemplate.boundSetOps("nameSet").members();
        System.out.println(nameSet);
    }

    @Test
    public void removeValue(){
        redisTemplate.boundSetOps("nameSet").remove("孙权");
    }

    @Test
    public void removeAllValue(){
        redisTemplate.delete("nameSet");
    }

}
