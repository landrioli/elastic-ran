/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ElasticGrain;

import middlewares.OneManager;
import thresholds.Thresholds;
import java.lang.Math;


/**
 *
 * @author leandro.andrioli
 */
public class GrainEvaluator {
    private OneManager oneManager;
    private GrainFunctionEnum grainFunctionEnum;
    private boolean usarGraoElastico;
    private double percentualVariacaoGraoElasticoLinear;
    private float percentualVariacaoExponencial;
    private int quantidadeHostsCadastrados;
    private float newUsageAfterIncreases;
    
    
    private int iterationForExponentialFunction;
    
    public GrainEvaluator(OneManager om, GrainFunctionEnum gfe, boolean pusarGraoElastico, double ppercentualVariacaoGraoElasticoLinear, int pquantidadeHostsCadastrados){
        oneManager = om;
        grainFunctionEnum = gfe;
        usarGraoElastico = pusarGraoElastico;
        percentualVariacaoGraoElasticoLinear = ppercentualVariacaoGraoElasticoLinear;
        iterationForExponentialFunction = 1;
        quantidadeHostsCadastrados = pquantidadeHostsCadastrados;
        newUsageAfterIncreases = 0;
        percentualVariacaoExponencial = 15;
    } 
    
    public void computeElasticGrain(float lastDecisionCpuLoad, float lastDecisionMemLoad, float lastDecisionNetworkLoad,
            float currentDecisionCpuLoad, float currentDecisionMemLoad, float currentDecisionNetworkLoad)
    {
        if(usarGraoElastico && newUsageAfterIncreases > 0){
            System.out.println("Vai calcular o grão elastico: Atualmente esta em VM: " + oneManager.vms_per_operation + " e Host: " + oneManager.hosts_per_operation);
            System.out.println("Percentual de aumento ocorrido: " + String.valueOf(currentDecisionCpuLoad - newUsageAfterIncreases) + "Percentual definido SLA: " + percentualVariacaoGraoElasticoLinear);

            int grainSize = 0;
            float percentualVariacao = Math.abs(lastDecisionCpuLoad - currentDecisionCpuLoad);
            //If the current measurement is less than the last measurement it is linear INCREASE
            if(percentualVariacao > percentualVariacaoGraoElasticoLinear){
                grainSize = CalculateGrainSize(oneManager.vms_per_operation, true, percentualVariacao);
                if(((quantidadeHostsCadastrados * oneManager.quatidade_cores_host) - oneManager.getTotalActiveResources()) >= grainSize)                    
                    oneManager.vms_per_operation = grainSize;
                else{
                    oneManager.vms_per_operation = oneManager.getAvailableHosts() * oneManager.quatidade_cores_host; //maximo possivel dado que o grão é muito grande
                }
            }      

            //If the current measurement is less than the last measurement it is linear DECREASE
            if(percentualVariacao < percentualVariacaoGraoElasticoLinear){
                grainSize = CalculateGrainSize(oneManager.vms_per_operation, false, percentualVariacao);
                if (oneManager.getTotalActiveResources() >= grainSize)            
                    oneManager.vms_per_operation = grainSize;
                else{
                    oneManager.vms_per_operation = oneManager.getActiveHosts() * oneManager.quatidade_cores_host;
                }
            }
            System.out.println("Grão elastico após o calculo: " + grainSize + " | vmsOperation: " + oneManager.vms_per_operation + " | hostOperation: " + oneManager.hosts_per_operation);
        }
    }
    
    private int CalculateGrainSize(int grainSize, boolean ouveUmAumentoPercentualEmRelacaoAUltimaMEdicao, float percentualDeAumento){
        if(ouveUmAumentoPercentualEmRelacaoAUltimaMEdicao){
            if(percentualDeAumento > percentualVariacaoGraoElasticoLinear && percentualDeAumento < percentualVariacaoExponencial){
                grainSize = (int) grainSize + 1;
            }else if(percentualDeAumento >= percentualVariacaoExponencial){
                if(grainSize <= 1)
                    grainSize = grainSize + (int) Math.floor(Math.pow(2, 2));
                else
                    grainSize = grainSize + (int) Math.floor(Math.pow(grainSize, 2));
            }
        }
        else{
            if(percentualDeAumento > percentualVariacaoGraoElasticoLinear && percentualDeAumento < percentualVariacaoExponencial){
                grainSize = (int) grainSize + 1;
            }else if(percentualDeAumento >= percentualVariacaoExponencial){
                if(grainSize <= 1)
                    grainSize = grainSize - (int) Math.floor(Math.pow(2, 2));
                else
                    grainSize = grainSize - (int) Math.floor(Math.pow(grainSize, 2));
            }
            
            if(grainSize <= 0) grainSize = 1;
        }
        return grainSize;
    }
    
    
    public void UpdateNewUsageAfterIncreases(float cpuUsage){
        newUsageAfterIncreases = cpuUsage;
    }
    
   public void computeElasticGrainOLD(float lastDecisionCpuLoad, float lastDecisionMemLoad, float lastDecisionNetworkLoad,
            float currentDecisionCpuLoad, float currentDecisionMemLoad, float currentDecisionNetworkLoad)
    {
        if(usarGraoElastico){
            System.out.println("Vai calcular o grão elastico: Atualmente esta em VM: " + oneManager.vms_per_operation + " e Host: " + oneManager.hosts_per_operation);
            int grainSize = 0;
            //If the current measurement is less than the last measurement it is linear INCREASE
            if((currentDecisionCpuLoad < lastDecisionCpuLoad && (lastDecisionCpuLoad - currentDecisionCpuLoad) > percentualVariacaoGraoElasticoLinear) || 
               (currentDecisionCpuLoad > lastDecisionCpuLoad && (currentDecisionCpuLoad - lastDecisionCpuLoad) > percentualVariacaoGraoElasticoLinear)){
                grainSize = CalculateGrainSize(oneManager.vms_per_operation, true, 0);
                if(((quantidadeHostsCadastrados * oneManager.quatidade_cores_host) - oneManager.getTotalActiveResources()) >= grainSize)                    
                    oneManager.vms_per_operation = grainSize;
                else{
                    oneManager.vms_per_operation = oneManager.getAvailableHosts() * oneManager.quatidade_cores_host; //maximo possivel dado que o grão é muito grande
                }
            }
            else if((currentDecisionMemLoad < lastDecisionMemLoad && (lastDecisionMemLoad - currentDecisionMemLoad) > percentualVariacaoGraoElasticoLinear) ||
                    (currentDecisionMemLoad > lastDecisionMemLoad && (currentDecisionMemLoad - lastDecisionMemLoad) > percentualVariacaoGraoElasticoLinear)){
                grainSize = CalculateGrainSize(oneManager.vms_per_operation, true, 0);
                if(((quantidadeHostsCadastrados * oneManager.quatidade_cores_host) - oneManager.getTotalActiveResources()) >= grainSize)                    
                    oneManager.vms_per_operation = grainSize;
                else{
                    oneManager.vms_per_operation = oneManager.getAvailableHosts() * oneManager.quatidade_cores_host; //maximo possivel dado que o grão é muito grande
                }
            }
            /*if((currentDecisionNetworkLoad < lastDecisionNetworkLoad && (lastDecisionNetworkLoad - currentDecisionNetworkLoad) > percentualVariacaoGraoElastico) ||
               (currentDecisionNetworkLoad > lastDecisionNetworkLoad && (currentDecisionNetworkLoad - lastDecisionNetworkLoad) > percentualVariacaoGraoElastico)){
                grainSize = CalculateGrainSize(oneManager.hosts_per_operation, true);
                if(((quantidadeHostsCadastrados * oneManager.quatidade_cores_host) - oneManager.getTotalActiveResources()) >= 
                        (grainSize * oneManager.quatidade_cores_host)){
                    oneManager.hosts_per_operation = grainSize;
                }
                else{
                    oneManager.hosts_per_operation = oneManager.getAvailableHosts();
                }
            } */       

            //If the current measurement is less than the last measurement it is linear DECREASE
            if((currentDecisionCpuLoad < lastDecisionCpuLoad && (lastDecisionCpuLoad - currentDecisionCpuLoad) > percentualVariacaoGraoElasticoLinear) || 
               (currentDecisionCpuLoad > lastDecisionCpuLoad && (currentDecisionCpuLoad - lastDecisionCpuLoad) > percentualVariacaoGraoElasticoLinear)){
                grainSize = CalculateGrainSize(oneManager.vms_per_operation, false, 0);
                if(oneManager.getTotalActiveResources() >= grainSize && grainSize > 0)            
                    oneManager.vms_per_operation = grainSize;
                else{
                    oneManager.vms_per_operation = oneManager.getActiveHosts() * oneManager.quatidade_cores_host;
                }
            }
            else if((currentDecisionMemLoad < lastDecisionMemLoad && (lastDecisionMemLoad - currentDecisionMemLoad) > percentualVariacaoGraoElasticoLinear) ||
                    (currentDecisionMemLoad > lastDecisionMemLoad && (currentDecisionMemLoad - lastDecisionMemLoad) > percentualVariacaoGraoElasticoLinear)){
                grainSize = CalculateGrainSize(oneManager.vms_per_operation, false, 0);
                if(oneManager.getTotalActiveResources() >= grainSize && grainSize > 0)            
                    oneManager.vms_per_operation = grainSize;
                else{
                    oneManager.vms_per_operation = oneManager.getActiveHosts() * oneManager.quatidade_cores_host;
                }
            }
            /*((currentDecisionNetworkLoad < lastDecisionNetworkLoad && (lastDecisionNetworkLoad - currentDecisionNetworkLoad) > percentualVariacaoGraoElastico) ||
               (currentDecisionNetworkLoad > lastDecisionNetworkLoad && (currentDecisionNetworkLoad - lastDecisionNetworkLoad) > percentualVariacaoGraoElastico)){
                grainSize = CalculateGrainSize(oneManager.hosts_per_operation, false);
                if((oneManager.getTotalActiveResources()/2) >= grainSize && grainSize > 0){                        
                    oneManager.hosts_per_operation = grainSize;
                }
                else{
                    oneManager.hosts_per_operation = oneManager.getActiveHosts();
                }
            }*/
             System.out.println("Grão elastico após o calculo: " + grainSize);
        }
    }

    private int CalculateGrainSizeOLD(int grainSize, boolean ouveUmAumentoPercentualEmRelacaoAUltimaMEdicao, float percentualDeAumento){
        if(ouveUmAumentoPercentualEmRelacaoAUltimaMEdicao){
            switch (grainFunctionEnum){
                case Linear:
                    grainSize = (int) grainSize + 1;
                    break;
                case Quadratico:
                    grainSize =  grainSize + (int) Math.floor(Math.pow(grainSize, 2));
                    break;
                case Exponencial:
                    grainSize = grainSize + (int) Math.floor(Math.pow(oneManager.quatidade_cores_host, iterationForExponentialFunction));                
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
                    grainSize = grainSize - (int) Math.floor(Math.pow(oneManager.quatidade_cores_host, iterationForExponentialFunction));                
                    break;
            }
            if(grainSize < 0) grainSize = 1;
        }
        return grainSize;
    }
}
