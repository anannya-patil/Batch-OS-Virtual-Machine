import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class OS
{
    //Registers
    char R[] = new char[4]; //general register
    char IR[] = new char[4]; //instruction register to store instructions
    int IC = 0; //instruction counter: saves address of next instruction
    boolean C; //for result of comparision with R. Match = true. No match = false.

    //Memory
    char M[][] = new char[100][4];  //100 words. Each of 4 bytes.

    public void LOAD()
    {
        boolean continue_loading = true;
        //read instruction from input.txt
        try (BufferedReader br = new BufferedReader(new FileReader("input.txt")))
        {
            String line;
            int Mi=0, Mj=0;

            while((line = br.readLine()) != null)
            {
                if(line.charAt(0)=='$' && line.charAt(1)=='E' && line.charAt(2)=='N' && line.charAt(3)=='D')
                {
                    System.out.println("End of job.");
                    break;
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
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    public void execute()
    {

    }

    public static void main(String args[])
    {
        System.out.println("Testing");
    }
}