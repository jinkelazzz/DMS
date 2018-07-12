package calculator.derivatives;

import calculator.utility.NewtonIterationParams;
import option.BaseSingleOption;

/**
 * @author liangcy
 */
public abstract class BaseSingleOptionCalculator extends BaseCalculator {

    BaseSingleOption option;

    private boolean useVolatilitySurface = false;

    /**
     * 牛顿迭代参数
     */
    NewtonIterationParams iterParams = new NewtonIterationParams();

    public BaseSingleOptionCalculator() {

    }

    public BaseSingleOptionCalculator(BaseSingleOption option) {
        setOption(option);
    }

    public NewtonIterationParams getIterParams() {
        return iterParams;
    }

    public void setIterParams(NewtonIterationParams iterParams) {
        this.iterParams = iterParams;
    }

    public BaseSingleOption getOption() {
        return option;
    }

    public void setOption(BaseSingleOption option) {
        this.option = option;
    }

    public void enableVolSurface() {
        this.useVolatilitySurface = true;
    }

    public void disableVolSurface() {
        this.useVolatilitySurface = false;
    }

    /**
     * 计算价格
     */
    @Override
    public abstract void calculatePrice();

    /**
     * 计算隐含波动率, 并把隐含波动率赋给volatility
     */
    public abstract void calculateImpliedVolatility();

    /**
     * 计算Delta;
     */
    public abstract void calculateDelta();

    /**
     * 计算1%的Vega
     */
    public abstract void calculateVega();

    /**
     * 计算1天的Theta(按365天计算), 而且是单边差分计算
     */
    public abstract void calculateTheta();

    public abstract void calculateGamma();

    /**
     * 计算1个BP的Rho
     */
    public abstract void calculateRho();


}
