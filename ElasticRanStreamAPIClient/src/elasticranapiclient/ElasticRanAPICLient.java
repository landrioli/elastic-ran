/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package elasticranapiclient;

import java.io.IOException;
import javax.xml.soap.*;

/**
 *
 * @author leand
 */
public class ElasticRanAPICLient {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws SOAPException, InterruptedException, IOException {
        SoapRequestStructure request = new SoapRequestStructure();
        
        Thread t = new Thread(new SoapClientInstance("10000", request));
        t.run();
        
        Thread t2 = new Thread(new SoapClientInstance("100000", request));
        t2.run();
        
        Thread t3 = new Thread(new SoapClientInstance("1000000", request));
        t3.run();
        
        Thread t4 = new Thread(new SoapClientInstance("10000000", request));
        t4.run();
        
        Thread t5 = new Thread(new SoapClientInstance("100000000", request));
        t5.run();
        
        Thread t6 = new Thread(new SoapClientInstance("300000000", request));
        t6.run();
        
        t.join();
        t2.join();
        t3.join();
        t4.join();
        t5.join();
        t6.join();
    }
    
}
