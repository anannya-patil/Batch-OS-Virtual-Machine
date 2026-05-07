import java.io.*;

public class OS_Phase2
{
    char M[][] = new char[300][4];
    char IR[] = new char[4];
    char R[] = new char[4];

    int IC, PTR, real_address, virtual_address;
    //PTR = page table register. stores starting address of page table.
    int jobId, TTL, TLL, TTC, LLC;
    int SI, PI, TI; //service interrupt, program interrupt, time interrupt
    boolean C;
    boolean is_frame_allocated[] = new boolean[30];
    boolean terminateFlag;

    BufferedReader br;
    BufferedWriter bw;

    public OS_Phase2()
    {
        try
        {
            br = new BufferedReader(new FileReader("input.txt"));
            bw = new BufferedWriter(new FileWriter("output.txt"));
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public void initialize()
    {
        for(int i=0; i<300; i++)
        {
            for(int j=0; j<4; j++)
                M[i][j] = ' ';
        }

        for(int i=0; i<4; i++)
        {
            IR[i] = ' ';
            R[i] = ' ';
        }

        IC=0; PTR=0; 
        real_address=0; virtual_address=0; 
        SI = 0; PI = 0; TI = 0; C = false; TTC = 0; LLC=0; terminateFlag = false;

        for(int i=0; i<30; i++)
            is_frame_allocated[i] = false;
    }

    public int allocatePF() //allocating page frame to a page.
    {
        int f;
        do
        {
            f = (int)(Math.random()*30);
        } while(is_frame_allocated[f]); //continue finding random page frames till an empty one is found

        is_frame_allocated[f] = true;

        return f;
    }

    public int addressMap(int virtual_address)
    {
        if(virtual_address < 0 || virtual_address >= 100) //one job only gets 100 virtual adrs
        {
            PI = 2; //operand error if invalid addr
            return -1;
        }

        int pte = PTR + virtual_address/10;    //page table entry

        if(M[pte][0] == '*')
        {
            PI = 3; //page fault
            return -1;
        }

        /*
        The page table, for each word: bit 2 and 4 give page frame no.
        so for two diff digits a and b,
        a*10+b gives values of the no. ab.
        */
        int frame = (M[pte][2] - '0')*10 + (M[pte][3]-'0');
        real_address = frame*10 + virtual_address%10;
        return real_address;
    }

    public void LOAD()
    {
        String line;
        int page = 0, offset = 0, f = 0, loc = 0;

        try
        {
            while((line=br.readLine())!=null)
            {
                if(line.charAt(0)=='$' && line.charAt(1)=='A' && line.charAt(2)=='M' && line.charAt(3)=='J')
                {
                    initialize();

                    jobId = Integer.parseInt(""+line.charAt(5)+line.charAt(6)+line.charAt(7)+line.charAt(8));
                    TTL = Integer.parseInt(""+line.charAt(10)+line.charAt(11)+line.charAt(12)+line.charAt(13));
                    TLL = Integer.parseInt(""+line.charAt(15)+line.charAt(16)+line.charAt(17)+line.charAt(18));

                    int pf = allocatePF();
                    PTR = pf*10;

                    for(int i=PTR; i<PTR+10; i++)
                    {
                        M[i][0]='*';
                        M[i][1]='*';
                        M[i][2]='*';
                        M[i][3]='*';
                    }

                    page = 0; offset=0;

                    f = allocatePF();
                    int pte = PTR + page;
                    M[pte][0]='0';
                    M[pte][1]='0';
                    M[pte][2]=(char)(f/10+'0');
                    M[pte][3]=(char)(f%10+'0');

                    loc = f*10;

                    for(int i=loc; i<loc+10; i++)
                        for(int j=0; j<4; j++)
                            M[i][j]=' ';
                }
                else if(line.charAt(0)=='$' && line.charAt(1)=='D' && line.charAt(2)=='T' && line.charAt(3)=='A')
                {
                    execute();
                }
                else if(line.charAt(0)=='$' && line.charAt(1)=='E' && line.charAt(2)=='N' && line.charAt(3)=='D')
                {
                    continue;
                }
                else
                {
                    if(line.charAt(0) == 'H')
                    {
                        M[loc+offset][0] = 'H';
                        M[loc+offset][1]=' ';
                        M[loc+offset][2] = ' ';
                        M[loc+offset][3]=' ';
                    }
                    else
                    {
                        for(int i=0; i<4; i++)
                        {
                            M[loc+offset][i] = line.charAt(i);
                        }
                    }
                    offset++;

                    if(offset == 10)
                    {
                        page++;
                        offset = 0;

                        f = allocatePF();

                        int pte = PTR + page;

                        M[pte][0]='0';
                        M[pte][1]='0';
                        M[pte][2] = (char)(f/10 + '0');
                        M[pte][3] = (char)(f%10 + '0');

                        loc = f*10;

                        for(int i=loc; i<loc+10; i++)
                            for(int j=0; j<4; j++)
                                M[i][j] = ' ';
                    }
                }
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public void execute()
    {
        IC = 0;

        while(true)
        {
            if(terminateFlag) 
                return;

            real_address = addressMap(IC);

            if(PI!=0)
            {
                MOS();
                return;
            }

            for(int i=0; i<4; i++)
                IR[i] = M[real_address][i];

            IC++;

            String op = "" + IR[0] + IR[1];

            if(!(op.equals("LR")||op.equals("SR")||op.equals("CR")|| op.equals("BT")||op.equals("GD")||op.equals("PD")||IR[0]=='H'))
            {
                PI=1;
                MOS();
                return;
            }

            if(IR[0]!='H')
            {
                if(!(Character.isDigit(IR[2]) && Character.isDigit(IR[3])))
                {
                    PI=2;
                    MOS();
                    return;
                }

                virtual_address=(IR[2]-'0')*10 + (IR[3]-'0');
                real_address = addressMap(virtual_address);

                if(PI==3)
                {
                    if(op.equals("GD") || op.equals("SR"))
                    {
                        int f = allocatePF();
                        int pte=PTR+virtual_address/10;

                        M[pte][0]='0';
                        M[pte][1]='0';
                        M[pte][2] = (char)(f/10+'0');
                        M[pte][3]=(char)(f%10+'0');

                        PI = 0;
                        real_address = addressMap(virtual_address);
                        continue;
                    }
                    else
                    {
                        MOS();
                        return;
                    }
                }
            }

            if(op.equals("LR"))
            {
                for(int i =0;i<4;i++)
                    R[i] = M[real_address][i];
                TTC++;
            }
            else if(op.equals("SR"))
            {
                for(int i=0;i<4;i++)
                    M[real_address][i]=R[i];
                TTC++;
            }
            else if(op.equals("CR"))
            {
                C=true;
                for(int i=0;i<4;i++)
                {
                    if(R[i]!=M[real_address][i]) 
                        C=false;
                }
                TTC++;
            }
            else if(op.equals("BT"))
            {
                if(C) 
                    IC=virtual_address;
                TTC++;
            }
            else if(op.equals("GD"))
            {
                SI=1;
                TTC+=2;

                if(TTC>TTL) 
                    TI=2;

                MOS();
                if(terminateFlag) 
                    return;
            }
            else if(op.equals("PD"))
            {
                SI=2;
                TTC++;

                if(TTC>TTL) 
                    TI=2;

                MOS();
                if(terminateFlag) 
                    return;
            }
            else if(IR[0]=='H')
            {
                SI=3;
                TTC++;

                if(TTC>TTL) 
                    TI=2;

                MOS();
                return;
            }

            if(TTC>TTL)
            {
                TI=2;
                MOS();
                return;
            }
        }
    }

    public void MOS()
    {
        try
        {
            if(TI==0 && SI==1)
                read();
            else if(TI==0 && SI==2)
                write();
            else if(TI==0 && SI==3)
                terminate(0);
            else if(TI==2 && SI==1)
                terminate(3);
            else if(TI==2 && SI==2)
            {
                LLC++;
                if(LLC > TLL)
                    terminate(2);
                else
                {
                    for(int i=real_address; i<real_address+10; i++)
                        for(int j=0;j<4;j++)
                            bw.write(M[i][j]);
                    bw.newLine();
                    terminate(3);
                }
            }
            else if(TI==2 && SI==3)
                terminate(3);
            else if(TI==0 && PI==1)
                terminate(4);
            else if(TI==0 && PI==2)
                terminate(5);
            else if(TI==0 && PI==3)
                terminate(6);
            else if(TI==2)
                terminate(3);

            SI=0; 
            PI=0; 
            TI=0;
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public void read()
    {
        try
        {
            String data=br.readLine();

            if(data.charAt(0)=='$' && data.charAt(1)=='E' && data.charAt(2)=='N' && data.charAt(3)=='D')
            {
                terminate(1);
                return;
            }

            int loc = real_address, k=0;

            for(int i=0;i<40;i++)
            {
                if(i<data.length())
                    M[loc][k] = data.charAt(i);
                else
                    M[loc][k] = ' ';

                k++;

                if(k==4)
                {
                    k=0;
                    loc++;
                }
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public void write()
    {
        try
        {
            LLC++;

            if(LLC>TLL)
            {
                terminate(2);
                return;
            }

            for(int i=real_address; i < real_address+10; i++)
                for(int j=0; j<4; j++)
                    bw.write(M[i][j]);

            bw.newLine();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public void printMemory()
    {
        try
        {
            FileWriter fw = new FileWriter("p2mem.txt", true);
            fw.write("\nMemory:\n");
            for(int i=0; i<300; i++)
            {
                fw.write(String.format(i + " : "));
                for(int j=0; j<4; j++)
                {
                    fw.write(M[i][j]);
                    fw.write(' ');
                }
                fw.write("\n");
            }
            fw.write("\n\n\n");
            fw.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public void terminate(int em)
    {
        try
        {
            bw.write("\n\n");

            if(em==0) 
                bw.write("Program Executed Successfully\n");
            else if(em==1) 
                bw.write("Out of Data\n");
            else if(em==2) 
                bw.write("Line Limit Exceeded\n");
            else if(em==3) 
                bw.write("Time Limit Exceeded\n");
            else if(em==4) 
                bw.write("Operation Code Error\n");
            else if(em==5) 
                bw.write("Operand Error\n");
            else if(em==6) 
                bw.write("Invalid Page Fault\n");

            bw.write("Job ID: "+jobId+" IC: "+IC+" TTC: "+TTC+" LLC: "+LLC+"\n");
            bw.flush();

            printMemory();

            terminateFlag=true;
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void main(String args[])
    {
        OS_Phase2 os = new OS_Phase2();
        os.LOAD();
    }
}