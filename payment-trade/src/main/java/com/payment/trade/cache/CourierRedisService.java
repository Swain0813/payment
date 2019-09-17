package com.payment.trade.cache;
import com.payment.common.entity.Courier;
import com.payment.trade.dao.CourierMapper;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 *所有运输商的信息
 */
@Slf4j
@Service
public class CourierRedisService {
    @Autowired
    private CourierMapper courierMapper;

    /**
     * 运输商信息本地缓存30天
     */
    private Cache<String, List<Courier>> iCourierLists = CacheBuilder.newBuilder().expireAfterWrite(30, TimeUnit.DAYS).build();

    /**
     *运输商信息本地缓存化
     * @return
     */
    public List<Courier> getCourierLists(){
        try{
            String key = "tradeICourier";
            return iCourierLists.get(key, new Callable<List<Courier>>() {
                @Override
                public List<Courier> call() throws Exception {
                    List<Courier> courierLists = courierMapper.getCouriers();
                    return courierLists;
                }
            });
        }catch (Exception e){
            log.error("运输商信息本地缓存失败:"+e);
        }
        return  null;
    }
}
