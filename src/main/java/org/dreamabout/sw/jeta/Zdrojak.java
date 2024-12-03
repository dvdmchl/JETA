package org.dreamabout.sw.jeta;

import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class Zdrojak {
    public final String C_NAME = "NAME";
    public final String C_MIESTO = "MIESTO";
    public final String C_REM = "REM";
    public final String C_CLS = "CLS";
    public final String C_OUT = "OUT";
    public final String C_PRESSKEY = "PRESSKEY";
    public final String C_ZMEN = "ZMEN";
    public final String C_END = "END";
    public final String C_COMMAND = "COMMAND";
    public final String C_ENDCOMMAND = "ENDCOMMAND";
    public final String C_ADDMENU = "ADDMENU";
    public final String C_SETMAINMENU = "SETMAINMENU";
    public final String C_SETMIESTO = "SETMIESTO";
    public final String C_SETTEXTCOLOR = "SETTEXTCOLOR";
    public final String C_SETMENUCOLOR = "SETMENUCOLOR";
    public final String C_SHOWIMAGE = "SHOWIMAGE";
    public final String C_SOUND = "SOUND";
    public final String C_ALL = "ALL";
    public final String C_ENDALL = "ENDALL";

    public final String[] PRIKAZY = {
            C_NAME,C_MIESTO,C_REM,C_CLS,C_OUT,C_PRESSKEY,
            C_ZMEN,C_END,C_COMMAND,C_ENDCOMMAND,
            C_ADDMENU,C_SETMAINMENU,C_SETMIESTO,
            C_SETTEXTCOLOR,C_SETMENUCOLOR,C_SHOWIMAGE,
            C_ALL,C_ENDALL,C_SOUND};

    public final int POCET_PRIKAOV=PRIKAZY.length;

    public static final String I_START = "start";
    public static final String I_OK = "<OK>";
    public static final String I_SPAT = "<zpět>";
    public static final String I_ROVNA_SA = "==";
    public static final String I_NEROVNA_SA = "!=";

    private final Core program;
    private ArrayList text;
    public Tabulka tabulka;
    private PlayerMid playerMid;

    private String miesto;
    private int pauza;
    private String textColor;

    private String umiestenieSuboru;
    private String adresarSuboru;
    private String nazovTextovky;
    private boolean sifrovany;

    public Zdrojak(Core c)
    {
        program=c;
        text = new ArrayList();
        tabulka = new Tabulka();
    }

    private void resetData()
    {
        text.clear();
        tabulka.reset();
        program.menu.cls();
        program.debugger.clsOUT();
        textColor = "255 255 255";
        if(playerMid!=null)playerMid.stop();
        playerMid=null;
        pauza=-1;
    }

    public boolean nacitaj()
    {
        return nacitaj(umiestenieSuboru);
    }

    public boolean nacitaj(String subor)
    {
        umiestenieSuboru=subor.trim();
        adresarSuboru = getCestaKSuboru(umiestenieSuboru);

        resetData();

        if(subor.endsWith(FileJETA.BIG_JETA) ||
                subor.endsWith(FileJETA.SMALL_JETA))
        {
            text = program.fileJETA.odsifruj(subor);
            sifrovany=true;
        }
        else
        {
            text = nacitajTextZoSuboru(subor);
            sifrovany=false;
        }

        if(!syntax())
        {
            resetData();
            return false;
        }

        if(isSifrovany())program.debugger.setVisible(false);

        miesto=I_START;
        chod(miesto);
        return true;
    }

    private ArrayList nacitajTextZoSuboru(String subor)
    {
        ArrayList obsah  = new ArrayList();

        try{
            BufferedReader br = new BufferedReader(new FileReader(new File(subor)));
            String s=null;

            while( (s=br.readLine())!=null)
                obsah.add(s.trim());

            br.close();

        }catch(Exception e)
        {
            program.napis("Nastala chyba pri nacitani zdrojoveho kodu\n"+e,'E');
        }

        return obsah;
    }

    public String getUmiestenieSuboru()
    {
        return umiestenieSuboru.trim();
    }

    public String getAdresarSuboru()
    {
        return adresarSuboru.trim();
    }

    public String getNazovTextovky()
    {
        return nazovTextovky.trim();
    }

    private String getCestaKSuboru(String s)
    {
        StringBuffer sb = new StringBuffer(s);

        while(sb.length()!=0 && sb.charAt(sb.length()-1)!='\\')
            sb.deleteCharAt(sb.length()-1);

        return sb.toString();
    }

    public Color getCreateColor(String s)
    {
        String[] slova = getSlova(s);
        return getCreateColor(slova[0],slova[1],slova[2]);
    }

    private Color getCreateColor(String s1,String s2, String s3)
    {
        int a=Integer.parseInt(s1);
        int b=Integer.parseInt(s2);
        int c=Integer.parseInt(s3);

        if(a<0)a=0; if(a>255)a=255;
        if(b<0)b=0; if(b>255)b=255;
        if(c<0)c=0; if(c>255)c=255;

        return (new Color(a,b,c));
    }

    public String getMiesto()
    {
        return miesto.trim();
    }

    private String getRiadok(int n)
    {
        return ((String)text.get(n));
    }

    private int getAllIndex()
    {
        int i=0;

        for(;i<text.size();i++)
            if(getRiadok(i).startsWith(C_ALL))
            {
                return i+1;
            }

        return -1;
    }

    private String[] getSlova(String s)
    {
        ArrayList slova = new ArrayList();
        StringTokenizer toke = new StringTokenizer(s);
        String[] ret;

        while(toke.hasMoreTokens())slova.add(toke.nextToken());

        ret = new String[slova.size()];
        for(int i=0;i<ret.length;i++)ret[i]=(String)slova.get(i);

        return ret;
    }

    private String getPodmienka(String s)
    {
        if(s.length()==0)return "";
        if(s.charAt(0)!='(')return "";

        StringBuffer sb = new StringBuffer();
        String ret;
        int i=1;

        sb.append("( ");
        while(s.charAt(i)!=')')
            sb.append(s.charAt(i++));

        sb.append(" )");
        ret=sb.toString();

        ret=ret.replaceAll(I_ROVNA_SA," "+I_ROVNA_SA+" ");
        ret=ret.replaceAll(I_NEROVNA_SA," "+I_NEROVNA_SA+" ");

        //System.out.println("getPlatipodmienka("+s+")="+ret);
        return ret;
    }

    private boolean getPlatiPodmienka(String s)
    {
        if(s.equals(""))return true;

        String[] slova = getSlova(s);
        String s2=s.trim();
        boolean ret=true;

        for(int i=0;i<slova.length;i++)
            if(slova[i].startsWith(":"))s2=s2.replaceAll(slova[i],tabulka.get(slova[i]));

        slova = getSlova(s2);

        for(int i=1;i<slova.length-1;i+=3)
        {
            if(slova[i].equals(slova[i+2]) && slova[i+1].equals(I_NEROVNA_SA))ret=false;
            if(!slova[i].equals(slova[i+2]) && slova[i+1].equals(I_ROVNA_SA))ret=false;
        }

        //System.out.println("getPlatiPodmienka("+s+")="+ret);
        return ret;
    }

    private String getPrikazBezPodmienky(String s)
    {
        if(s.length()==0)return "";
        if(s.charAt(0)!='(')return s;

        StringBuffer sb  =new StringBuffer();
        String ret;
        int i=s.length()-1;

        while(s.charAt(i)!=')')
            sb.insert(0,s.charAt(i--));

        ret=sb.toString().trim();

        //System.out.println("ret="+ret);
        return ret;
    }

    private boolean isPremenna(String s)
    {
        return (s.charAt(0)==':'?true:false);
    }

    private boolean isCislo(String s)
    {
        boolean n=true;
        int p=s.length();

        for(int i=0;i<p;i++)
            if(!(s.charAt(i)>=48 && s.charAt(i)<=57))n=false;

        return n;
    }

    public boolean isPauza()
    {
        return (pauza==-1?false:true);
    }

    public boolean isNacitanaTextovka()
    {
        return (text.size()==0?false:true);
    }

    public boolean isSifrovany()
    {
        return sifrovany;
    }

    public boolean syntax()
    {
        ArrayList chyby = new ArrayList();
        ArrayList miesta = new ArrayList();

        String sprava;

        int priznak=0;
        int pocet = text.size();

        for(int i=0;i<pocet;i++)
        {
            String[] s=getSlova(getPodmienka(getRiadok(i)));

            if(s.length!=0)
            {
                if(!s[0].equals("(") || !s[s.length-1].equals(")"))
                {
                    chyby.add("riadok : "+(i+1)+" - nespr�vna defin�cia podmienky !");
                    continue;
                }

                if((s.length-2)%3!=0)
                {
                    chyby.add("riadok : "+(i+1)+" \""+getRiadok(i)+"\" - podmienka ma nespr�vny po�et parametrov !");
                }
                else
                {
                    for(int j=1;j<s.length-1;j+=3)
                    {
                        if(!isPremenna(s[j]) && !isCislo(s[j]))
                            chyby.add("riadok : "+(i+1)+" parameter \""+s[j]+"\" nie je platnou hodnotou pri porovn�van� !");
                        if(!isPremenna(s[j+2]) && !isCislo(s[j+2]))
                            chyby.add("riadok : "+(i+1)+" parameter \""+s[j+2]+"\" nie je platnou hodnotou pri porovn�van� !");
                        if(!s[j+1].equals(I_ROVNA_SA) && !s[j+1].equals(I_NEROVNA_SA))
                            chyby.add("riadok : "+(i+1)+" oper�tor \""+s[j+1]+"\" - nie je platn�m parametom pri porovn�van� hodn�t !");
                    }
                }
            }

            s=getSlova(getPrikazBezPodmienky(getRiadok(i)));
            if(s.length==0)continue;

            String nespravnyPocetParametrov =
                    "riadok : "+(i+1)+" \""+getRiadok(i)+"\" - nespr�vny po�et parametrov !";

            if(s[0].equals(C_MIESTO) && s.length!=2)chyby.add(nespravnyPocetParametrov);
            if(s[0].equals(C_CLS) && s.length!=1)chyby.add(nespravnyPocetParametrov);
            if(s[0].equals(C_PRESSKEY) && s.length!=1)chyby.add(nespravnyPocetParametrov);
            if(s[0].equals(C_ZMEN) && s.length!=3)chyby.add(nespravnyPocetParametrov);
            if(s[0].equals(C_COMMAND) && s.length!=3)chyby.add(nespravnyPocetParametrov);
            if(s[0].equals(C_ENDCOMMAND) && s.length!=1)chyby.add(nespravnyPocetParametrov);
            if(s[0].equals(C_ADDMENU) && s.length!=3)chyby.add(nespravnyPocetParametrov);
            if(s[0].equals(C_SETMAINMENU) && s.length>10)chyby.add(nespravnyPocetParametrov);
            if(s[0].equals(C_SETMIESTO) && s.length!=2)chyby.add(nespravnyPocetParametrov);
            if(s[0].equals(C_SETTEXTCOLOR) && s.length!=4)chyby.add(nespravnyPocetParametrov);
            if(s[0].equals(C_SETMENUCOLOR) && s.length!=4)chyby.add(nespravnyPocetParametrov);

            //ver 1.3
            if(s[0].equals(C_SHOWIMAGE) && s.length!=2)chyby.add(nespravnyPocetParametrov);
            if(s[0].equals(C_ALL) && s.length!=1)chyby.add(nespravnyPocetParametrov);
            if(s[0].equals(C_ENDALL) && s.length!=1)chyby.add(nespravnyPocetParametrov);

            // ver 1.8
            if(s[0].equals(C_SOUND) && s.length>2)chyby.add(nespravnyPocetParametrov);

            priznak=0;
            for(int j=0;j<POCET_PRIKAOV;j++)if(s[0].equals(PRIKAZY[j]))priznak++;
            if(priznak==0)chyby.add("riadok : "+(i+1)+" pr�kaz : \""+s[0]+"\" nie je platn�m pr�kazom interpretera "+program.TITLE+" !");
        }

        boolean jeTuPrikazName=false;
        for(int i=0;i<pocet;i++)
            if(getRiadok(i).startsWith(C_NAME))
            {
                nazovTextovky=getRiadok(i).replaceAll(C_NAME,"").trim().replaceAll(" ","_");
                jeTuPrikazName=true;
                break;
            }

        if(!jeTuPrikazName)chyby.add("pr�kaz \""+C_NAME+"\" nen�jden� !");

        String nazovMiesta;
        for(int i=0;i<pocet;i++)
            if(getRiadok(i).startsWith(C_MIESTO))
            {
                nazovMiesta=getRiadok(i).replaceAll(C_MIESTO,"").trim();

                tento:for(int j=0;j<miesta.size();j++)
                    if(((String)miesta.get(j)).equals(nazovMiesta))
                    {
                        chyby.add("riadok : "+(i+1)+" miesto : \""+nazovMiesta+"\" u� existuje");
                        break tento;
                    }

                miesta.add(nazovMiesta);
            }

        boolean jeMiesto=false;
        for(int i=0;i<pocet;i++)
            if(getRiadok(i).startsWith(C_SETMIESTO))
            {
                nazovMiesta=getRiadok(i).replaceAll(C_SETMIESTO,"").trim();
                jeMiesto=false;

                for(int j=0;j<miesta.size();j++)
                    if(((String)miesta.get(j)).equals(nazovMiesta))jeMiesto=true;

                if(!jeMiesto)chyby.add("riadok : "+(i+1)+" miesto : \""+nazovMiesta+"\" neexistuje !");
            }

        boolean jeStart=false;
        for(int i=0;i<miesta.size();i++)
            if(((String)miesta.get(i)).equals(I_START))jeStart=true;

        if(!jeStart)chyby.add("miesto \""+I_START+"\" sa nepodarilo n�js� !");

        for(int i=0;i<pocet;i++)
        {
            if(getRiadok(i).startsWith(C_ZMEN))
            {
                String[] s=getSlova(getRiadok(i));
                if(!isPremenna(s[1]))chyby.add("riadok : "+(i+1)+" parameter : \""+s[1]+"\" nie je platnou premennou !");
                if(!isPremenna(s[2]) && !isCislo(s[2]))chyby.add("riadok : "+(i+1)+" parameter : \""+s[2]+"\" nie je platnou hodnotou !");
            }

            if(getRiadok(i).startsWith(C_SETTEXTCOLOR) || getRiadok(i).startsWith(C_SETMENUCOLOR))
            {
                String[] s=getSlova(getRiadok(i));
                if(!isCislo(s[1]))chyby.add("riadok : "+(i+1)+" parameter : \""+s[1]+"\" nie je platn�m ��slom !");
                if(!isCislo(s[2]))chyby.add("riadok : "+(i+1)+" parameter : \""+s[2]+"\" nie je platn�m ��slom !");
                if(!isCislo(s[3]))chyby.add("riadok : "+(i+1)+" parameter : \""+s[3]+"\" nie je platn�m ��slom !");
            }
        }

        if(chyby.size()==0)return true;

        sprava="Chyby v hernom scipte :\n\n";
        for(int i=0;i<chyby.size();i++)
            sprava+=((String)chyby.get(i))+"\n";

        program.napis(sprava,'W');
        return false;
    }

    public boolean spracujRiadok(int i)
    {
        String riadok = getRiadok(i);

        if(!getPlatiPodmienka(getPodmienka(riadok)))
            return false;
        else
            riadok = getPrikazBezPodmienky(riadok);

        if(!riadok.startsWith(C_OUT) &&
                !riadok.startsWith(C_REM) &&
                !riadok.trim().equals(""))

            program.debugger.addOUT(i+" "+riadok);

        if(riadok.startsWith(C_CLS))cls(riadok);
        if(riadok.startsWith(C_OUT))out(riadok);
        if(riadok.startsWith(C_SETMAINMENU))setMainMenu(riadok);
        if(riadok.startsWith(C_ADDMENU))addMenu(riadok);
        if(riadok.startsWith(C_SETTEXTCOLOR))setTextColor(riadok);
        if(riadok.startsWith(C_SETMENUCOLOR))setMenuColor(riadok);
        if(riadok.startsWith(C_ZMEN))zmen(riadok);
        if(riadok.startsWith(C_SOUND))sound(riadok);
        if(riadok.equals(C_END))program.exit();

        if(riadok.startsWith(C_PRESSKEY))
        {
            pressKey(riadok,i);
            return true;
        }

        if(riadok.startsWith(C_SETMIESTO))
        {
            setMiesto(riadok);
            return true;
        }

        // 1.3
        if(!program.NO_IMAGE && riadok.startsWith(C_SHOWIMAGE))
        {
            showImage(riadok,i);
            return true;
        }

        return false;
    }

    public void chod()
    {
        chod(miesto);
    }

    public void chod(String s)
    {
        int n=0;
        while(!(getRiadok(n).startsWith(C_MIESTO) && getRiadok(n).endsWith(" "+s)))
        {
            if(++n==text.size())
            {
                program.napis("Miesto "+s+" sa nenaslo !",'W');
                return;
            }
        }

        miesto=s;

        for(int i=n;i<text.size();i++)
        {
            if(getRiadok(i).startsWith(C_COMMAND))break;
            if(spracujRiadok(i))return;
        }

        for(int i=getAllIndex();i<text.size();i++)
        {
            if(getAllIndex()==-1)break;
            if(getRiadok(i).startsWith(C_COMMAND))break;
            if(spracujRiadok(i))return;
        }

        for(int j=1;j<10;j++)
            program.menu.add(j,I_SPAT);
    }

    public void nastavMenu()
    {
        int n=0;
        while(!(getRiadok(n).startsWith(C_MIESTO) && getRiadok(n).endsWith(" "+miesto)))
        {
            if(++n==text.size())
            {
                program.napis("Miesto "+miesto+" sa nenaslo !",'W');
                return;
            }
        }

        for(int i=n;i<text.size();i++)
        {
            if(getRiadok(i).startsWith(C_COMMAND))break;

            if(getPrikazBezPodmienky(getRiadok(i)).startsWith(C_SETMAINMENU) ||
                    getPrikazBezPodmienky(getRiadok(i)).startsWith(C_ADDMENU))spracujRiadok(i);
        }

        for(int i=getAllIndex();i<text.size();i++)
        {
            if(getAllIndex()==-1)break;
            if(getRiadok(i).startsWith(C_COMMAND))break;
            if(spracujRiadok(i))return;
        }

        for(int j=1;j<10;j++)
            program.menu.add(j,I_SPAT);
    }

    public void akcia(String s)
    {
        program.debugger.addOUT("AKCIA "+s);

        String riadok=null;

        int n=0;
        while(!(getRiadok(n).startsWith(C_MIESTO) && getRiadok(n).endsWith(" "+miesto)))n++;

        for(int i=n+1;i<text.size() && !getRiadok(i).startsWith(C_MIESTO) && !getRiadok(i).startsWith(C_ALL);i++)
        {
            riadok = getRiadok(i);

            if(riadok.startsWith(C_COMMAND) && command(riadok,s))
            {
                i++;
                while(!getRiadok(i).startsWith(C_ENDCOMMAND))
                {
                    if(spracujRiadok(i))return;
                    i++;
                }

                nastavMenu();
            }
        }

        if(getAllIndex()==-1)return;

        for(int i=getAllIndex();i<text.size();i++)
        {
            riadok = getRiadok(i);

            if(riadok.startsWith(C_COMMAND) && command(riadok,s))
            {
                i++;
                while(!getRiadok(i).startsWith(C_ENDCOMMAND) &&
                        !getRiadok(i).startsWith(C_ENDALL))
                {
                    if(spracujRiadok(i))return;
                    i++;
                }

                nastavMenu();
            }
        }
    }

    public void pokracuj()
    {
        int i=pauza;
        pauza=-1;

        for(;i<text.size();i++)
        {
            if(getRiadok(i).startsWith(C_COMMAND))break;

            if(getRiadok(i).startsWith(C_ENDCOMMAND))
            {
                nastavMenu();
                return;
            }

            if(spracujRiadok(i))return;
        }

        for(i=getAllIndex();i<text.size();i++)
        {
            if(i==-1)break;

            if(getRiadok(i).startsWith(C_COMMAND))break;
            if(getRiadok(i).startsWith(C_ENDCOMMAND))
            {
                nastavMenu();
                return;
            }

            if(spracujRiadok(i))return;
        }

        for(int j=1;j<10;j++)
            program.menu.add(j,I_SPAT);
    }

    private void cls(String riadok)
    {
        program.texty.cls();
    }

    private void out(String riadok)
    {
        if(riadok.trim().equals(C_OUT))program.texty.add("");
        else program.texty.add(riadok.replaceAll(C_OUT+" ",""),textColor.trim());
    }

    private void pressKey(String riadok,int n)
    {
        program.menu.cls();
        program.menu.add(1,Zdrojak.I_OK);
        program.menu.zobraz(1);
        pauza=n+1;
    }

    private void addMenu(String riadok)
    {
        String[] s=getSlova(riadok);
        program.menu.add(s[1],s[2]);
    }

    private void setMainMenu(String riadok)
    {
        program.menu.cls();
        String[] s=getSlova(riadok);

        for(int j=1;j<s.length;j++)
            program.menu.add(0,s[j]);

        program.menu.zobraz(0);
    }

    private void setMiesto(String riadok)
    {
        String[] s=getSlova(riadok);
        chod(s[1]);
    }

    private void setTextColor(String riadok)
    {
        textColor = riadok.replaceAll(C_SETTEXTCOLOR,"");
    }

    private void setMenuColor(String riadok)
    {
        String[] s=getSlova(riadok);
        program.menu.setMenuColor(getCreateColor(s[1],s[2],s[3]));
    }

    public void zmen(String riadok)
    {
        String[] s=getSlova(riadok);

        if(!isPremenna(s[s.length-2]))
        {
            program.napis("Premenna :\""+s[s.length-2]+"\" nie je platnou premennou interpretera "+program.TITLE+" !",'W');
            return;
        }

        if(!isPremenna(s[s.length-1]) &&
                !isCislo(s[s.length-1]))
        {
            program.napis("Hodnota :\""+s[s.length-1]+"\" nie je platnou premennou, alebo hodnototu interpretera "+program.TITLE+" !",'W');
            return;
        }

        tabulka.set(s[s.length-2],s[s.length-1]);
    }

    private boolean command(String riadok,String prikaz)
    {
        String[] s1=getSlova(riadok);
        String[] s2=getSlova(prikaz);

        if(s1.length!=3 || s2.length!=2)return false;

        if(s1[1].equals(s2[0]) &&
                s1[2].equals(s2[1]))
            return true;
        else
            return false;
    }

    //ver 1.3
    private void showImage(String riadok,int n)
    {
        String[] s = getSlova(riadok);
        program.texty.nacitajObrazok(s[1]);

        program.menu.cls();
        program.menu.add(1,Zdrojak.I_OK);
        program.menu.zobraz(1);

        pauza=n+1;
    }

    //ver 1.8
    private void sound(String riadok)
    {
        if(program.NO_SOUND)return;

        String[] s = getSlova(riadok);

        if(s.length==1)
        {
            if(playerMid!=null)playerMid.stop();
            playerMid=null;
            return;
        }

        String subor = getAdresarSuboru()+s[1];

        if(playerMid!=null && subor.equals(playerMid.FILE))return;
        if(playerMid!=null)playerMid.stop();

        playerMid = new PlayerMid(program,subor);
        playerMid.start();
    }
}
