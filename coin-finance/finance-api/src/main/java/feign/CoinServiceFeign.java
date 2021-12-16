package feign;

import com.elevenchu.config.feign.OAuth2FeignConfig;
import dto.CoinDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "finance-service",configuration = OAuth2FeignConfig.class)
public interface CoinServiceFeign {

    @GetMapping("/list")
    public List<CoinDto> findCoins(@RequestParam  List<Long> coinIds);

}
