package esprit.marketplace;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/mic3/marketplace")
public class MarketPlaceRestApi {

    @GetMapping("/hello")
    public String sayHello() {
        return "Hello from Agricultural Marketplace";
    }
}