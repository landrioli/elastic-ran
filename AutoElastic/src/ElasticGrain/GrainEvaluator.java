/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ElasticGrain;

import middlewares.OneManager;
import thresholds.Thresholds;

/**
 *
 * @author leandro.andrioli
 */
public class GrainEvaluator {
    private OneManager oneManager;
    private GrainFunctionEnum grainFunctionEnum;
    private boolean usarGraoElastico;
    private double percentualVariacaoGraoElastico;
    
    private int iterationForExponentialFunction;
    
    public GrainEvaluator(OneManager om, GrainFunctionEnum gfe, boolean pusarGraoElastico, double ppercentualVariacaoGraoElastico){
        oneManager = om;
        grainFunctionEnum = gfe;
        usarGraoElastico = pusarGraoElastico;
        percentualVariacaoGraoElastico = ppercentualVariacaoGraoElastico;
        iterationForExponentialFunction = 1;
    } 
    
    public void computeElasticGrain(float lastDecisionCpuLoad, float lastDecisionMemLoad, float lastDecisionNetworkLoad,
            float currentDecisionCpuLoad, float currentDecisionMemLoad, float currentDecisionNetworkLoad)
    {
        if(usarGraoElastico){
            System.out.println("Vai calcular o grão elastico: Atualmente esta em VM: " + oneManager.vms_per_operation + " e Host: " + oneManager.hosts_per_operation);
            int grainSize = 0;
            //If the current measurement is less than the last measurement it is linear INCREASE
            if((currentDecisionCpuLoad < lastDecisionCpuLoad && (lastDecisionCpuLoad - currentDecisionCpuLoad) > percentualVariacaoGraoElastico) || 
               (currentDecisionCpuLoad > lastDecisionCpuLoad && (currentDecisionCpuLoad - lastDecisionCpuLoad) > percentualVariacaoGraoElastico)){
                grainSize = CalculateGrainSize(oneManager.vms_per_operation, true);
                if((oneManager.getAvailableHosts() * oneManager.quatidade_cores_host) >= grainSize)                    
                    oneManager.vms_per_operation = grainSize;
                else{
                    oneManager.vms_per_operation = oneManager.getAvailableHosts() * oneManager.quatidade_cores_host; //maximo possivel dado que o grão é muito grande
                }
            }
            else if((currentDecisionMemLoad < lastDecisionMemLoad && (lastDecisionMemLoad - currentDecisionMemLoad) > percentualVariacaoGraoElastico) ||
                    (currentDecisionMemLoad > lastDecisionMemLoad && (currentDecisionMemLoad - lastDecisionMemLoad) > percentualVariacaoGraoElastico)){
                grainSize = CalculateGrainSize(oneManager.vms_per_operation, true);
                if((oneManager.getAvailableHosts() * oneManager.quatidade_cores_host) >= grainSize)                    
                    oneManager.vms_per_operation = grainSize;
                else{
                    oneManager.vms_per_operation = oneManager.getAvailableHosts() * oneManager.quatidade_cores_host; //maximo possivel dado que o grão é muito grande
                }
            }
            if((currentDecisionNetworkLoad < lastDecisionNetworkLoad && (lastDecisionNetworkLoad - currentDecisionNetworkLoad) > percentualVariacaoGraoElastico) ||
               (currentDecisionNetworkLoad > lastDecisionNetworkLoad && (currentDecisionNetworkLoad - lastDecisionNetworkLoad) > percentualVariacaoGraoElastico)){
                grainSize = CalculateGrainSize(oneManager.hosts_per_operation, true);
                if(oneManager.getAvailableHosts() >= grainSize){
                    oneManager.hosts_per_operation = grainSize;
                }
                else{
                    oneManager.hosts_per_operation = oneManager.getAvailableHosts();
                }
            }        

            //If the current measurement is less than the last measurement it is linear DECREASE
            if((currentDecisionCpuLoad < lastDecisionCpuLoad && (lastDecisionCpuLoad - currentDecisionCpuLoad) > percentualVariacaoGraoElastico) || 
               (currentDecisionCpuLoad > lastDecisionCpuLoad && (currentDecisionCpuLoad - lastDecisionCpuLoad) > percentualVariacaoGraoElastico)){
                grainSize = CalculateGrainSize(oneManager.vms_per_operation, false);
                if((oneManager.getActiveHosts() * oneManager.quatidade_cores_host) >= grainSize && grainSize > 0)            
                    oneManager.vms_per_operation = grainSize;
                else{
                    oneManager.vms_per_operation = oneManager.getActiveHosts() * oneManager.quatidade_cores_host;
                }
            }
            else if((currentDecisionMemLoad < lastDecisionMemLoad && (lastDecisionMemLoad - currentDecisionMemLoad) > percentualVariacaoGraoElastico) ||
                    (currentDecisionMemLoad > lastDecisionMemLoad && (currentDecisionMemLoad - lastDecisionMemLoad) > percentualVariacaoGraoElastico)){
                grainSize = CalculateGrainSize(oneManager.vms_per_operation, false);
                if((oneManager.getActiveHosts() * oneManager.quatidade_cores_host) >= grainSize && grainSize > 0)            
                    oneManager.vms_per_operation = grainSize;
                else{
                    oneManager.vms_per_operation = oneManager.getActiveHosts() * oneManager.quatidade_cores_host;
                }
            }
            if((currentDecisionNetworkLoad < lastDecisionNetworkLoad && (lastDecisionNetworkLoad - currentDecisionNetworkLoad) > percentualVariacaoGraoElastico) ||
               (currentDecisionNetworkLoad > lastDecisionNetworkLoad && (currentDecisionNetworkLoad - lastDecisionNetworkLoad) > percentualVariacaoGraoElastico)){
                grainSize = CalculateGrainSize(oneManager.vms_per_operation, false);
                if(oneManager.getActiveHosts() >= grainSize && grainSize > 0){                        
                    oneManager.hosts_per_operation = grainSize;
                }
                else{
                    oneManager.hosts_per_operation = oneManager.getActiveHosts();
                }
            }
             System.out.println("Grão elastico após o calculo: " + grainSize);
        }
    }
    
    private int CalculateGrainSize(int grainSize, boolean ouveUmAumentoPercentualEmRelacaoAUltimaMEdicao){
        if(ouveUmAumentoPercentualEmRelacaoAUltimaMEdicao){
            switch (grainFunctionEnum){
                case Linear:
                    grainSize = (int) grainSize + 1;
                    break;
                case Quadratico:
                    grainSize =  grainSize + (int) Math.floor(Math.pow(grainSize, 2));
                    break;
                case Exponencial:
                    grainSize = grainSize + (int) Math.floor(Math.pow(2, iterationForExponentialFunction));                
                    break;
            }
        }
        else{
            switch (grainFunctionEnum){
                case Linear:
                    grainSize = (int) grainSize - 1;
                    break;
                case Quadratico:
                    grainSize =  grainSize - (int) Math.floor(Math.pow(grainSize, 2));
                    break;
                case Exponencial:
                    grainSize = grainSize - (int) Math.floor(Math.pow(2, iterationForExponentialFunction));                
                    break;
            }
            if(grainSize < 0) grainSize = 1;
        }
        return grainSize;
    }
}
