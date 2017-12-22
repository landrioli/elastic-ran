/*
 * 06/07/2015
 * AgingFullEvaluator
 * - class to apply the exponential moving average in the system load of all observations
 */
package evaluators;

import static autoelastic.AutoElasticManager.gera_log;

/**
 * Reviews
 * @author viniciusfacco
 * 
 */
public class AgingFullEvaluator extends GenericEvaluator{
    
    /**
     *
     * @param viewsize - define the minimum of observations necessary
     */
    public AgingFullEvaluator(int viewsize) {
        super(viewsize);
        objname = "evaluators.AgingFullEvaluator";    //rewrite the name
    }
    
    /**
    * Return if the aging system identified if the factor is out of range between the thresholds.
    * @param upper_threshold - the current upper threshold.
    * @param lower_threshold - the current lower threshold.
    * @return 
    */
    @Override
    public boolean evaluate(float upper_threshold, float lower_threshold){        
        //gera_log(objname, "Main|AginFullEvaluator|evaluate: Aging = " + decision_cpu_load);
        if (counter >= VIEW_SIZE - 1){
            //test if the aging is out of the range between the thresholds
            if (decision_cpu_load > upper_threshold) { //test if we have a violation on the higher threshold after aply the aging
                high_alert = true; 
                low_alert = false; 
                return true;
            } else if (decision_cpu_load < lower_threshold){ //test if we have a violation on the lower threshold after aply the aging
                high_alert = false;
                low_alert = true;
                return true; 
            } else {
                high_alert = false;
                low_alert = false;
            }
        } else {
            counter++; //here, counter is used to define the observantions amount
        }
        return false;  
    }    
    
    @Override
    public float computeLoad(float cpuLoad, float memLoad, float networkLoad){
        decision_cpu_load = (float) (decision_cpu_load * 0.5 + cpuLoad * 0.5);
        decision_mem_load = (float) (decision_mem_load * 0.5 + memLoad * 0.5);
        decision_network_load = (float) (decision_network_load * 0.5 + networkLoad * 0.5);

        return 0;
    }
}
