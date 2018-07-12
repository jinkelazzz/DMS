package calculator.derivatives;

import calculator.utility.CalculateUtil;
import flanagan.roots.RealRoot;
import flanagan.roots.RealRootDerivFunction;
import option.BaseSingleOption;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static calculator.utility.CalculatorError.*;

/**
 * 构造计算隐含波动率的Newton方程。
 */
class ImpliedVolFunction implements RealRootDerivFunction {
    private BaseSingleOption option;

    public void setOption(BaseSingleOption option) {
        this.option = option;
    }

    @Override
    public double[] function(double estimateVol) {
        option.getVanillaOptionParams().setVolatility(estimateVol);
        SingleOptionAnalysisCalculator calculator = new SingleOptionAnalysisCalculator(option);
        calculator.calculatePrice();
        double[] y = new double[2];
        y[0] = calculator.getResult() - option.getVanillaOptionParams().getTargetPrice();
        calculator.calculateVega();
        // 计算的是1%的Vega, 计算斜率要乘100.
        y[1] = calculator.getResult() * 100;
        return y;
    }
}


/**
 * 计算解析解的价格, Greeks, implied volatility.
 *
 * @author liangcy
 */
public class SingleOptionAnalysisCalculator extends BaseSingleOptionCalculator {

    public SingleOptionAnalysisCalculator() {
        super();
    }

    public SingleOptionAnalysisCalculator(BaseSingleOption option) {
        super(option);
    }

    /**
     * @return 获取对象的方法，先检查自身的方法，再检查继承来的方法
     * @throws NoSuchMethodException
     */
    private Method getMethod() throws NoSuchMethodException {
        Class<?> c = option.getClass();
        String methodName = option.getVanillaOptionParams().getMethodName();
        try {
            return c.getDeclaredMethod(methodName);
        } catch (NoSuchMethodException e) {
            return c.getMethod(methodName);
        }
    }

    /**
     * 根据方法来获取结果。
     *
     * @param method 方法
     * @return 根据方法计算的价格。
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    private double getPrice(Method method) throws InvocationTargetException, IllegalAccessException {
        return (double) method.invoke(option);
    }

    private double getInitialVol() {
        double t = option.getVanillaOptionParams().getTimeRemaining();
        double k = option.getVanillaOptionParams().getStrikePrice();
        double futureValue = option.getUnderlying().getFutureValue(t);
        double logMoneyness = Math.log(futureValue / k);
        return Math.sqrt(Math.abs(logMoneyness) * 2 / t) + 0.1;
    }

    /**
     * 牛顿二分法计算隐含波动率, 下限0.005, 上限3.000
     * 这里会把隐含波动率赋值给volatility。
     */
    @Override
    public void calculateImpliedVolatility() {
        resetCalculator();
        ImpliedVolFunction function = new ImpliedVolFunction();
        function.setOption(option);
        RealRoot realRoot = new RealRoot();
        realRoot.setTolerance(iterParams.getTol());
        realRoot.setIterMax(iterParams.getIterations());
        double lowerLimit = 0.005;
        double upperLimit = 3.000;
        realRoot.setLowerBound(lowerLimit);
        realRoot.setUpperBound(upperLimit);
        //迭代初值
        double estimateVol = option.getVanillaOptionParams().getVolatility();
        if (estimateVol <= lowerLimit || estimateVol >= upperLimit) {
            estimateVol = getInitialVol();
        }
        realRoot.setEstimate(estimateVol);
        try {
            double root = realRoot.bisectNewtonRaphson(function);
            //root是NaN,计算失败
            if (Double.isNaN(root)) {
                setError(CALCULATE_NAN);
                return;
            }
            //达到迭代上限
            if (realRoot.getIterN() > realRoot.getIterMax()) {
                setResult(root);
                setError(REACH_MAX_ITERATION);
                return;
            }
            //计算成功
            {
                option.getVanillaOptionParams().setVolatility(root);
                setResult(root);
                setError(NORMAL);
            }
        } catch (Exception e) {
            setError(CALCULATE_FAILED);
        }
    }

    @Override
    public void calculatePrice() {
        resetCalculator();

        Method method;
        try {
            method = getMethod();
        } catch (NoSuchMethodException e) {
            setError(NOT_FOUND_METHOD);
            return;
        }

        try {
            double price = getPrice(method);
            if (Double.isNaN(price)) {
                setError(CALCULATE_NAN);
                return;
            }
            setResult(price);
            setError(NORMAL);
        } catch (IllegalAccessException | InvocationTargetException e) {
            setError(CALCULATE_FAILED);
        }
    }

    @Override
    public void calculateDelta() {
        resetCalculator();
        double deltaPrecision = option.getPrecision().getDeltaPrecision();
        double s = option.getUnderlying().getSpotPrice();
        double[] diffSpotPrice = CalculateUtil.midDiffValue(s, deltaPrecision);

        double upperSpotPrice = diffSpotPrice[1];
        option.getUnderlying().setSpotPrice(upperSpotPrice);
        calculatePrice();
        if (!isNormal()) {
            return;
        }
        double upperPrice = getResult();

        double lowerSpotPrice = diffSpotPrice[0];
        option.getUnderlying().setSpotPrice(lowerSpotPrice);
        calculatePrice();
        if (!isNormal()) {
            return;
        }
        double lowerPrice = getResult();

        //reset underlying price;
        option.getUnderlying().setSpotPrice(s);
        double delta = (upperPrice - lowerPrice) / (upperSpotPrice - lowerSpotPrice);
        if (Double.isNaN(delta)) {
            setError(CALCULATE_NAN);
            return;
        }
        setResult(delta);
        setError(NORMAL);
    }

    @Override
    public void calculateVega() {
        resetCalculator();
        double vol = option.getVanillaOptionParams().getVolatility();
        double vegaPrecision = option.getPrecision().getVegaPrecision();
        double[] diffVol = CalculateUtil.midDiffValue(vol, vegaPrecision);

        double upperVol = diffVol[1];
        option.getVanillaOptionParams().setVolatility(upperVol);
        calculatePrice();
        if (!isNormal()) {
            return;
        }
        double upperPrice = getResult();

        double lowerVol = diffVol[0];
        option.getVanillaOptionParams().setVolatility(lowerVol);
        calculatePrice();
        if (!isNormal()) {
            return;
        }
        double lowerPrice = getResult();

        option.getVanillaOptionParams().setVolatility(vol);
        double vega = (upperPrice - lowerPrice) / (upperVol - lowerVol) / 100;
        setResult(vega);
        setError(NORMAL);
    }

    @Override
    public void calculateTheta() {
        resetCalculator();
        double thetaPrecision = option.getPrecision().getThetaPrecision();
        double t = option.getVanillaOptionParams().getTimeRemaining();
        double[] diffTime = CalculateUtil.backwardDiffValue(t, thetaPrecision);

        option.getVanillaOptionParams().setTimeRemaining(diffTime[0]);
        calculatePrice();
        if (!isNormal()) {
            return;
        }
        double lowerPrice = getResult();

        option.getVanillaOptionParams().setTimeRemaining(t);
        calculatePrice();
        if (!isNormal()) {
            return;
        }
        double price = getResult();

        double theta = (lowerPrice - price) / (t * thetaPrecision) / 365;
        setResult(theta);
        setError(NORMAL);
    }

    @Override
    public void calculateGamma() {
        resetCalculator();
        double gammaPrecision = option.getPrecision().getGammaPrecision();
        double s = option.getUnderlying().getSpotPrice();
        double[] diffSpotPrice = CalculateUtil.midDiffValue(s, gammaPrecision);

        option.getUnderlying().setSpotPrice(diffSpotPrice[1]);
        calculateDelta();
        if (!isNormal()) {
            return;
        }
        double upperDelta = getResult();

        option.getUnderlying().setSpotPrice(diffSpotPrice[0]);
        calculateDelta();
        if (!isNormal()) {
            return;
        }
        double lowerDelta = getResult();

        option.getUnderlying().setSpotPrice(s);
        double gamma = (upperDelta - lowerDelta) / (s * gammaPrecision);
        setResult(gamma);
        setError(NORMAL);
    }

    @Override
    public void calculateRho() {
        resetCalculator();
        double r = option.getUnderlying().getRiskFreeRate();
        double rhoPrecision = option.getPrecision().getRhoPrecision();

        option.getUnderlying().setRiskFreeRate(r * (1 + rhoPrecision));
        calculatePrice();
        if (!isNormal()) {
            return;
        }
        double upperPrice = getResult();

        option.getUnderlying().setRiskFreeRate(r * (1 - rhoPrecision));
        calculatePrice();
        if (!isNormal()) {
            return;
        }
        double lowerPrice = getResult();

        option.getUnderlying().setRiskFreeRate(r);
        double rho = (upperPrice - lowerPrice) / (r * rhoPrecision) / 10000;
        setResult(rho);
        setError(NORMAL);
    }


}
