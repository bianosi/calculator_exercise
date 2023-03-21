package digital.metro.pricing.calculator;

import java.math.BigDecimal;

public class BasketEntry {

    private String articleId;
    private BigDecimal quantity;

    public BasketEntry(String articleId, BigDecimal quantity) {
        this.articleId = articleId;
        this.quantity = quantity;
    }

    public String getArticleId() {
        return articleId;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BasketEntry that = (BasketEntry) o;

        if (!getArticleId().equals(that.getArticleId())) return false;
        return getQuantity().equals(that.getQuantity());
    }

    @Override
    public int hashCode() {
        int result = getArticleId().hashCode();
        result = 31 * result + getQuantity().hashCode();
        return result;
    }
}
