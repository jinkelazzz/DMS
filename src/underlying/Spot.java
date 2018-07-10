package underlying;

import java.io.Serializable;

/**
 * @author liangcy
 */
public class Spot extends BaseUnderlying implements Serializable {
    public Spot() {

    }

    @Override
    public String toString() {
        return "Underlying type: spot" + sep +
                "spot price:" + getSpotPrice() + sep +
                "risk-free rate:" + getRiskFreeRate() + sep +
                "dividend rate:" + getDividendRate();
    }

    public Spot(double spotPrice, double riskFreeRate) {
        this.setSpotPrice(spotPrice);
        this.setRiskFreeRate(riskFreeRate);
    }

    public Spot(double spotPrice, double riskFreeRate, double dividendRate) {
        this.setSpotPrice(spotPrice);
        this.setRiskFreeRate(riskFreeRate);
        this.setDividendRate(dividendRate);
    }


}
