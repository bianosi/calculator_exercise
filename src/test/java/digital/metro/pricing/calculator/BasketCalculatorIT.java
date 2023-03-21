package digital.metro.pricing.calculator;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = CalculatorApplication.class)
@AutoConfigureMockMvc
public class BasketCalculatorIT {

    @Autowired
    private PriceRepository priceRepository;

    @Autowired
    private MockMvc restAvailabilityMockMvc;

    @Test
    public void testCalculateBasket() throws Exception {
        BigDecimal price = priceRepository.getPriceByArticleIdAndCustomerId("article1", "customer-1");
        BigDecimal rawPrice = priceRepository.getPriceByArticleId("article1");

        restAvailabilityMockMvc.perform(post("/calculator/calculate-basket")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"customerId\": \"customer-1\", \"entries\": [{\"articleId\": \"article1\", \"quantity\": 1}]}"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.customerId").value("customer-1"))
                .andExpect(jsonPath("$.pricedBasketEntries.article1").value(price))
                .andExpect(jsonPath("$.totalAmount").value(price.doubleValue()))
                .andExpect(jsonPath("$.totalAmount").value(rawPrice.multiply(new BigDecimal("0.9"))
                        .setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue()));
    }

    @Test
    public void testCalculateBasketRequiredBody() throws Exception {
        restAvailabilityMockMvc.perform(post("/calculator/calculate-basket")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testCalculateBasketWithMultipleItems() throws Exception {
        BigDecimal article1Price = priceRepository.getPriceByArticleIdAndCustomerId(
                "article1", "customer-1");
        BigDecimal article2Price = priceRepository.getPriceByArticleIdAndCustomerId(
                "article2", "customer-1");
        BigDecimal expectedTotal = article1Price.multiply(new BigDecimal(4))
                .add(article2Price.multiply(new BigDecimal(5)));

        restAvailabilityMockMvc.perform(post("/calculator/calculate-basket")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerId\": \"customer-1\", \"entries\": [{\"articleId\": \"article1\", " +
                                "\"quantity\": 4}, {\"articleId\": \"article2\", \"quantity\": 5}]}"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.customerId").value("customer-1"))
                .andExpect(jsonPath("$.pricedBasketEntries.article1")
                        .value(article1Price.multiply(new BigDecimal(4)).doubleValue()))
                .andExpect(jsonPath("$.pricedBasketEntries.article2")
                        .value(article2Price.multiply(new BigDecimal(5)).doubleValue()))
                .andExpect(jsonPath("$.totalAmount")
                        .value(expectedTotal.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue()));
    }

    @Test
    public void testCalculateBasketWithSameItemRepeated() throws Exception {
        BigDecimal article1Price = priceRepository.getPriceByArticleIdAndCustomerId(
                "article1", "customer-1");
        BigDecimal expectedTotal = article1Price.multiply(new BigDecimal(4))
                .add(article1Price.multiply(new BigDecimal(5)));

        restAvailabilityMockMvc.perform(post("/calculator/calculate-basket")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerId\": \"customer-1\", \"entries\": [{\"articleId\": \"article1\", " +
                                "\"quantity\": 4}, {\"articleId\": \"article1\", \"quantity\": 5}]}"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.customerId").value("customer-1"))
                .andExpect(jsonPath("$.pricedBasketEntries.article1")
                        .value(article1Price.multiply(new BigDecimal(9)).doubleValue()))
                .andExpect(jsonPath("$.totalAmount")
                        .value(expectedTotal.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue()));
    }

    @Test
    public void testCalculateBasketWithUnregisteredCustomer() throws Exception {
        BigDecimal price = priceRepository.getPriceByArticleId("article1");

        restAvailabilityMockMvc.perform(post("/calculator/calculate-basket")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerId\": \"customer-3\", \"entries\": [{\"articleId\": \"article1\", \"quantity\": 1}]}"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.customerId").value("customer-3"))
                .andExpect(jsonPath("$.pricedBasketEntries.article1").value(price.doubleValue()))
                .andExpect(jsonPath("$.totalAmount").value(price.doubleValue()));
    }

    @Test
    public void testCalculateArticle() throws Exception {
        BigDecimal price = priceRepository.getPriceByArticleId("article1");

        restAvailabilityMockMvc.perform(get("/calculator/article/article1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").value(price.doubleValue()));
    }

    @Test
    public void testCalculateArticleRequiredParameter() throws Exception {
        restAvailabilityMockMvc.perform(get("/calculator/article"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testCalculateArticleForCustomer() throws Exception {
        BigDecimal price = priceRepository.getPriceByArticleIdAndCustomerId("article1", "customer-1");

        restAvailabilityMockMvc.perform(get("/calculator/article/article1/customer/customer-1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").value(price.doubleValue()));
    }

    @Test
    public void testCalculateArticleForCustomerRequiredParameter() throws Exception {
        restAvailabilityMockMvc.perform(get("/calculator/article/article1/customer"))
                .andExpect(status().isNotFound());
    }
}
