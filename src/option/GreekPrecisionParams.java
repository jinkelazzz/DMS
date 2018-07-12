package option;

import calculator.utility.ConstantNumber;

import java.io.Serializable;

/**
 * 设置Greeks计算精度
 * 默认值全部为万分之一
 *
 * @author liangcy
 */
public class GreekPrecisionParams implements Serializable {
    private double deltaPrecision = 1.0 / 10000;
    private double gammaPrecision = 1.0 / 10000;
    private double vegaPrecision = 1.0 / 10000;
    private double thetaPrecision = 1.0 / 10000;
    private double rhoPrecision = 1.0 / 10000;
    /**
     * 精度最小值
     */
    private double eps = ConstantNumber.EPS * 2;

    public double getDeltaPrecision() {
        return deltaPrecision;
    }

    public void setDeltaPrecision(double deltaPrecision) {
        if (deltaPrecision > eps) {
            this.deltaPrecision = deltaPrecision;
        }
    }

    public double getGammaPrecision() {
        return gammaPrecision;
    }

    public void setGammaPrecision(double gammaPrecision) {
        if (gammaPrecision > eps) {
            this.gammaPrecision = gammaPrecision;
        }
    }

    public double getVegaPrecision() {
        return vegaPrecision;
    }

    public void setVegaPrecision(double vegaPrecision) {
        if (vegaPrecision > eps) {
            this.vegaPrecision = vegaPrecision;
        }
    }

    public double getThetaPrecision() {
        return thetaPrecision;
    }

    public void setThetaPrecision(double thetaPrecision) {
        if (thetaPrecision > eps) {
            this.thetaPrecision = thetaPrecision;
        }
    }

    public double getRhoPrecision() {
        return rhoPrecision;
    }

    public void setRhoPrecision(double rhoPrecision) {
        if (rhoPrecision > eps) {
            this.rhoPrecision = rhoPrecision;
        }
    }
}
