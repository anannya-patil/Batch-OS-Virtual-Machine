import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;

public class OS_Phase1
{
    //Registers
    char R[] = new char[4]; //general register
    char IR[] = new char[4]; //instruction register to store instructions
    int IC = 0; //instruction counter: saves address of next instruction
    boolean C; //for result of comparision with R. Match = true. No match = false.
    
    //Memory
    char M[][] = new char[100][4];  //100 words. Each of 4 bytes.

    int SI;
    int address;

    String line;
    BufferedReader br;

    public void LOAD()
    {
        boolean continue_loading = true;
        int Mi=0, Mj=0;

        try
        {
            //read instruction from input.txt
            br = new BufferedReader(new FileReader("input.txt"));
    
            while((line = br.readLine()) != null)
            {
                if(line.charAt(0)=='$' && line.charAt(1)=='E' && line.charAt(2)=='N' && line.charAt(3)=='D')
                {
                    System.out.println("End of job.");
                    printMemory();
                    try
                    {
                        FileWriter f = new FileWriter("output.txt", true);
                        f.write("\n\n");
                        f.close();
                    }
                    catch(Exception e)
                    {
                        e.printStackTrace();
                    }
                    continue;
                }
                else if(line.charAt(0)=='$' && line.charAt(1)=='A' && line.charAt(2)=='M' && line.charAt(3)=='J')
                {
                    continue_loading = true;
                    //initalize
                    for(int i=0; i<4; i++)
                    {
                        R[i] = '\0';
                        IR[i] = '\0';
                    }
                    IC = 0;
                    C = false;
                    for(int j=0; j<100; j++)
                    {
                        for(int i=0; i<4; i++)
                        {
                            M[j][i] = '\0';
                        }
    
                    }
                    Mi = 0;
                }
                else if(line.charAt(0)=='$' && line.charAt(1)=='D' && line.charAt(2)=='T' && line.charAt(3)=='A')
                {
                    //execute user program
                    continue_loading = false;
                    execute();
                }
                else
                {
                    if(continue_loading==true)
                    {
                        if(line.charAt(0)=='H')
                        {
                            M[Mi][0] = 'H';
                            M[Mi][1] = ' ';
                            M[Mi][2] = ' ';
                            M[Mi][3] = ' ';
                        }
                        else
                        {
                            for(Mj=0;Mj<4;Mj++)
                            {
                                M[Mi][Mj] = line.charAt(Mj);
                            }
                        }
                        Mi++;
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
            for(int i=0; i<4; i++)
            {
                IR[i] = M[IC][i];
            }
            IC++;
    
            if(IR[0]=='G' && IR[1]=='D')
            {
                address = (IR[2] - '0') * 10 + (IR[3] - '0');
                SI = 1;
                MOS();
            }
            else if(IR[0]=='P' && IR[1]=='D')
            {
                address = (IR[2] - '0') * 10 + (IR[3] - '0');
                SI = 2;
                MOS();
            }
            else if(IR[0]=='H')
            {
                SI = 3;
                MOS();
                break;
            }
            else if(IR[0]=='L' && IR[1]=='R')
            {
                address = (IR[2] - '0') * 10 + (IR[3] - '0');
                //load content into R register
                for(int i=0; i<4; i++)
                    R[i] = M[address][i];
            }
            else if(IR[0]=='S' && IR[1]=='R')
            {
                address = (IR[2] - '0') * 10 + (IR[3] - '0');
                //store contents of R into memory
                for(int i=0; i<4; i++)
                    M[address][i] = R[i];
            }
            else if(IR[0]=='C' && IR[1]=='R')
            {
                address = (IR[2] - '0') * 10 + (IR[3] - '0');
                //compare R with memory
                C = true;
                for(int i=0; i<4; i++)
                {
                    if(M[address][i] != R[i])
                        C = false;
                }
            }
            else if(IR[0]=='B' && IR[1]=='T')
            {
                address = (IR[2] - '0') * 10 + (IR[3] - '0');
                //branch when true
                if(C==true)
                {
                    IC = address;
                }
            }
        }

    }

    public void MOS()
    {
        if(SI==1)
        {
            //GD
            //read card (input.txt) and store in address
            try
            {
                line = br.readLine();
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }

            int j = 0;
            for(int i = 0; i<line.length(); i++)
            {
                M[address][j] = line.charAt(i);
                j++;
                
                if(j==4)
                {
                    address++; 
                    j=0;
                }

            }
        }
        else if (SI==2)
        {
            try
            {
                FileWriter out = new FileWriter("output.txt", true);
                for(int i=address; i<address+10; i++)
                {
                    //append to output.txt
                    for(int j=0; j<4; j++)
                    {
                        out.write(M[i][j]);
                    }
                }
                out.write('\n');
                out.close();
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }

        }
        else if (SI==3)
        {
            try
            {
                //halt
                FileWriter f = new FileWriter("output.txt", true);
                f.write("\n\n");
                f.close();
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    public void printMemory()
    {
        try
        {
            FileWriter fw = new FileWriter("p1mem.txt", true);
            fw.write("\nMemory:\n");
            for(int i=0; i<100; i++)
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

    public static void main(String args[])
    {
        OS_Phase1 os = new OS_Phase1();
        os.LOAD();
    }
}