

import java.net.*;
import java.io.*;

public class Slave {

    int elementos;
    Socket con = null;
    String ip_servidor;
    int porta;
    String compath; //diretorio compartilhado para comunicação
    String logpath; //diretorio em que deve ser salvo os tempos obtidos
    String name; //nome do escravo que será usado para identificar o log
    
	
    public Slave(int port, String compath, String logpath) throws IOException{
        this.porta = port;
        this.compath = compath;
        this.logpath = logpath;
        this.name = "S" + System.currentTimeMillis();
    }
	
    public void computa() throws ClassNotFoundException, IOException, InterruptedException{
        int i;
        int[] retorno;
        //Job job;
        
        ObjectInputStream oi;
        ObjectOutputStream oo;
        
        int cont = 0; //contador para saber qual a tarefa
        String tempos = "T1-AntesDeConectar;T2-AposConectar"; //string para guardar os tempos
            
        //vou ficar monitorando o diretorio compartilhado para verificar se há conexão disponivel
        File diretorio = new File(compath);
        File fList[];
        boolean bloqueado = true;
        int x;
        
        tempos = tempos + "\n" + System.currentTimeMillis(); //T1-AntesDeConectar
        
        while (bloqueado){
            Thread.sleep(2000);
            //System.out.println("Verificando permissão para conectar...");
            fList = diretorio.listFiles();
            x = 0;
            while((x < fList.length) && bloqueado){
                if (fList[x].getName().equals("serverok.txt")){
                    //se tem arquivo liberando conexão leio ele e libero
                    BufferedReader in = new BufferedReader(new FileReader(compath + "serverok.txt"));
                    this.ip_servidor = in.readLine();
                    in.close();                    
                    fList[x].delete();
                    bloqueado = false;
                }
                x++;
            }
        }
        //conexão liberada
       
        con = new Socket(ip_servidor, porta);
        
        //tempos = tempos + ";" + System.currentTimeMillis() +"\n\n" + "T3-AntesDeReceberTarefa;T4-AposReceberTarefa;T5-AntesCalcular;T6-AposCalcular-AntesDeEnviar;T7-AposEnviar"; //T2-AposConectar
        tempos = tempos + ";" + System.currentTimeMillis() +"\n\n" + "Contador;IniLoop"; //T2-AposConectar
        
        //System.out.println("Conexão realizada com sucesso");
        oi = new ObjectInputStream(con.getInputStream());
        oo = new ObjectOutputStream(con.getOutputStream());
        
        boolean ativo = true;
        while(ativo){
            tempos = tempos + "\n" + cont + ";" + System.currentTimeMillis(); //IniLoop
            Job job = (Job) oi.readObject();
           // tempos = tempos + ";" + System.currentTimeMillis(); //T4-AposReceberTarefa
            //System.out.println("Dados recebidos...");
            if (job.get_msg().equalsIgnoreCase("quit")){
                //System.out.println("Mensagem de quit recebida...");
                ativo = false;
            } else {
                //System.out.println("Calculando dados: " + job.get_part_qtde_slices());
                //tempos = tempos + ";" + System.currentTimeMillis(); //T5-AntesCalcular
                job.calcula();
                //tempos = tempos + ";" + System.currentTimeMillis(); //T6-AposCalcular-AntesDeEnviar
                //System.out.println("Enviando dados para o server...");    
                oo.writeObject(job);
                //tempos = tempos + ";" + System.currentTimeMillis(); //T7-AposEnviar
                //System.out.println("Dados enviados...");
            }
            cont++;
        }
        oo.close();
        oi.close();
        con.close();
        
        File arquivo = new File(logpath + name + ".csv");
        BufferedWriter escritor = new BufferedWriter(new FileWriter(arquivo));
        escritor.write(tempos);
        escritor.close();
    }
}
