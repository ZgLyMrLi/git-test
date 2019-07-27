package Test;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring/applicationContext-redis.xml")
public class TestHash {

    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    public void setValue() {
        redisTemplate.boundHashOps("nameHash").put("a","唐僧");
        redisTemplate.boundHashOps("nameHash").put("b","孙悟空");
        redisTemplate.boundHashOps("nameHash").put("c","八戒");
        redisTemplate.boundHashOps("nameHash").put("d","沙僧");
    }

    @Test
    public void getValue1() {
        String a = (String) redisTemplate.boundHashOps("nameHash").get("a");
        System.out.println(a);
    }

    @Test
    public void getValue2() {
        Set nameHash = redisTemplate.boundHashOps("nameHash").keys();
        System.out.println(nameHash);
    }

    @Test
    public void getValue3() {
        List nameHash = redisTemplate.boundHashOps("nameHash").values();
        System.out.println(nameHash);
    }

    @Test
    public void getMap3() {
        Map nameHash = redisTemplate.boundHashOps("nameHash").entries();
        /*Iterator iterator = nameHash.entrySet().iterator();
        while (iterator.hasNext()){
            System.out.println(iterator.next());
        }*/
        /*Set set = nameHash.entrySet();
        for (Object o : set) {
            System.out.println(o);
        }*/
        Set set = nameHash.keySet();
        for (Object o : set) {
            System.out.println(o+"="+nameHash.get(o));
        }
    }

    @Test
    public void removek() {
        redisTemplate.boundHashOps("nameHash").delete("c");
    }

    @Test
    public void removeK() {
        redisTemplate.delete("nameHash");
    }
    
}
