package Test;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;
import java.util.Set;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring/applicationContext-redis.xml")
public class TestList {

    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    public void setValueRight() {
        redisTemplate.boundListOps("nameList").rightPush("刘备");
        redisTemplate.boundListOps("nameList").rightPush("关羽");
        redisTemplate.boundListOps("nameList").rightPush("张飞1");
    }

    @Test
    public void getValueRight(){
        List nameList = redisTemplate.boundListOps("nameList").range(0, -1);
        System.out.println(nameList);
    }

    @Test
    public void setValueLeft() {
        redisTemplate.boundListOps("nameList").leftPush("刘备");
        redisTemplate.boundListOps("nameList").leftPush("关羽");
        redisTemplate.boundListOps("nameList").leftPush("张飞2");
    }

    @Test
    public void getValueLeft(){
        List nameList = redisTemplate.boundListOps("nameList").range(0, -1);
        System.out.println(nameList);
    }

    @Test
    public void searchByIndex(){
        String nameList = (String) redisTemplate.boundListOps("nameList").index(1);
        System.out.println(nameList);
    }

    @Test
    public void RemoveValue(){
        redisTemplate.boundListOps("nameList").remove(1,"张飞");//移除的个数和值
//        redisTemplate.boundListOps("nameList").remove(2,"关羽");//移除的个数和值
//        redisTemplate.boundListOps("nameList").remove(2,"刘备");//移除的个数和值
    }

    @Test
    public void RemoveAllValue(){
        redisTemplate.delete("nameList");
    }

}
