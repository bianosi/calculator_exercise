package digital.metro.pricing.calculator;

import java.util.Set;

public class Basket {

    private String customerId;
    private Set<BasketEntry> entries;

    public Basket(String customerId, Set<BasketEntry> entries) {
        this.customerId = customerId;
        this.entries = entries;
    }

    public String getCustomerId() {
        return customerId;
    }

    public Set<BasketEntry> getEntries() {
        return entries;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Basket basket = (Basket) o;

        if (!getCustomerId().equals(basket.getCustomerId())) return false;
        return getEntries().equals(basket.getEntries());
    }

    @Override
    public int hashCode() {
        int result = getCustomerId().hashCode();
        result = 31 * result + getEntries().hashCode();
        return result;
    }
}
