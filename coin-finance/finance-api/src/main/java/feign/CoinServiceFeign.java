package feign;

import com.elevenchu.config.feign.OAuth2FeignConfig;
import dto.CoinDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "finance-service",contextId = "coinServiceFeign",configuration = OAuth2FeignConfig.class,path = "/coins")
public interface CoinServiceFeign {

    @GetMapping("/list")
    List<CoinDto> findCoins(@RequestParam(name = "coinIds")  List<Long> coinIds);

}
