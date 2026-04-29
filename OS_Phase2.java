import java.io.*;

public class OS_Phase2
{
    char M[][] = new char[300][4];
    char IR[] = new char[4];
    char R[] = new char[4];

    int IC, PTR, RA, VA;
    int SI, PI, TI;
    boolean C;

    boolean used[] = new boolean[30];
    boolean terminateFlag;

    int jobId, TTL, TLL, TTC, LLC;

    BufferedReader br;
    BufferedWriter bw;

    public OS_Phase2()
    {
        try
        {
            br = new BufferedReader(new FileReader("input.txt"));
            bw = new BufferedWriter(new FileWriter("output.txt"));
        }
        catch(Exception e){e.printStackTrace();}
    }

    public void init()
    {
        for(int i=0; i<300; i++)
            for(int j=0; j<4; j++)
                M[i][j] = ' ';

        for(int i=0; i<4; i++)
        {
            IR[i] = ' ';
            R[i] = ' ';
        }

        IC=0; PTR=0; RA=0; VA=0;
        SI = 0; PI = 0; TI = 0;
        C = false;

        TTC = 0; LLC=0;
        terminateFlag = false;

        for(int i=0; i<30; i++)
            used[i] = false;
    }

    public int allocate()
    {
        int f;
        do
        {
            f = (int)(Math.random()*30);
        }while(used[f]);

        used[f] = true;
        return f;
    }

    public int addressMap(int VA)
    {
        if(VA < 0 || VA >= 100)
        {
            PI = 2;
            return -1;
        }

        int pte = PTR + VA/10;

        if(M[pte][0] == '*')
        {
            PI = 3;
            return -1;
        }

        int frame = (M[pte][2] - '0')*10 + (M[pte][3]-'0');
        return frame*10 + VA%10;
    }

    public void load()
    {
        String line;
        int page = 0;
        int wordOffset = 0;
        int f = 0;
        int loc = 0;

        try
        {
            while((line=br.readLine())!=null)
            {
                if(line.startsWith("$AMJ"))
                {
                    init();

                    String p[] = line.split(" +");
                    jobId = Integer.parseInt(p[1]);
                    TTL = Integer.parseInt(p[2]);
                    TLL = Integer.parseInt(p[3]);

                    int pf = allocate();
                    PTR = pf*10;

                    for(int i=PTR; i<PTR+10; i++)
                    {
                        M[i][0]='*';
                        M[i][1]='*';
                        M[i][2]='*';
                        M[i][3]='*';
                    }

                    page = 0;
                    wordOffset=0;

                    f = allocate();
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
                else if(line.startsWith("$DTA"))
                {
                    execute();
                }
                else if(line.startsWith("$END"))
                {
                    continue;
                }
                else
                {
                    for(int i=0; i<4; i++)
                    {
                        if(i < line.length())
                            M[loc + wordOffset][i] = line.charAt(i);
                        else
                            M[loc + wordOffset][i]=' ';
                    }

                    wordOffset++;

                    if(wordOffset==10)
                    {
                        page++;
                        wordOffset = 0;

                        f = allocate();

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
        catch(Exception e){e.printStackTrace();}
    }

    public void execute()
    {
        IC = 0;

        while(true)
        {
            if(terminateFlag) return;

            RA = addressMap(IC);

            if(PI!=0)
            {
                MOS();
                return;
            }

            for(int i=0; i<4; i++)
                IR[i] = M[RA][i];

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

                VA=(IR[2]-'0')*10 + (IR[3]-'0');
                RA=addressMap(VA);

                if(PI==3)
                {
                    if(op.equals("GD") || op.equals("SR"))
                    {
                        int f=allocate();
                        int pte=PTR+VA/10;

                        M[pte][0]='0';
                        M[pte][1]='0';
                        M[pte][2]=(char)(f/10+'0');
                        M[pte][3]=(char)(f%10+'0');

                        PI = 0;
                        RA = addressMap(VA);
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
                    R[i] =M [RA][i];
                TTC++;
            }
            else if(op.equals("SR"))
            {
                for(int i=0;i<4;i++)
                    M[RA][i]=R[i];
                TTC++;
            }
            else if(op.equals("CR"))
            {
                C=true;
                for(int i=0;i<4;i++)
                    if(R[i]!=M[RA][i]) C=false;
                TTC++;
            }
            else if(op.equals("BT"))
            {
                if(C) IC=VA;
                TTC++;
            }
            else if(op.equals("GD"))
            {
                SI=1;
                TTC+=2;

                if(TTC>TTL) TI=2;

                MOS();
                if(terminateFlag) return;
            }
            else if(op.equals("PD"))
            {
                SI=2;
                TTC++;

                if(TTC>TTL) TI=2;

                MOS();
                if(terminateFlag) return;
            }
            else if(IR[0]=='H')
            {
                SI=3;
                TTC++;

                if(TTC>TTL) TI=2;

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
                    for(int i=RA; i<RA+10; i++)
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
        catch(Exception e){e.printStackTrace();}
    }

    public void read()
    {
        try
        {
            String data=br.readLine();

            if(data.startsWith("$END"))
            {
                terminate(1);
                return;
            }

            int loc = RA, k=0;

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
        catch(Exception e){e.printStackTrace();}
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

            for(int i=RA; i < RA+10; i++)
                for(int j=0; j<4; j++)
                    bw.write(M[i][j]);

            bw.newLine();
        }
        catch(Exception e){e.printStackTrace();}
    }

    public void terminate(int em)
    {
        try
        {
            bw.write("\n\n");

            if(em==0) bw.write("Program Executed Successfully\n");
            else if(em==1) bw.write("Out of Data\n");
            else if(em==2) bw.write("Line Limit Exceeded\n");
            else if(em==3) bw.write("Time Limit Exceeded\n");
            else if(em==4) bw.write("Operation Code Error\n");
            else if(em==5) bw.write("Operand Error\n");
            else if(em==6) bw.write("Invalid Page Fault\n");

            bw.write("Job ID:"+jobId+" IC:"+IC+" TTC:"+TTC+" LLC:"+LLC+"\n");
            bw.flush();

            terminateFlag=true;
        }
        catch(Exception e){e.printStackTrace();}
    }

    public static void main(String args[])
    {
        OS_Phase2 os = new OS_Phase2();
        os.load();
    }
}