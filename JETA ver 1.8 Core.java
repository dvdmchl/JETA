
import java.io.*;
import java.util.*;

import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;

import javax.swing.*;
import javax.sound.midi.*;

class PlayerMid 
{
 private Sequencer skladba=null;  
 public final String FILE;
 
 public PlayerMid(Core program,String s)
 {
  FILE = s;
  
  try
  {   
   skladba = MidiSystem.getSequencer();
   skladba.open();            
   skladba.setSequence(new BufferedInputStream(new FileInputStream(new File(s))));
   
   skladba.addMetaEventListener(
   new MetaEventListener()
   {
    public void meta(MetaMessage e)
    {
     if(e.getType()==47)//vono to skoncilo prehravat
     {
      skladba.setMicrosecondPosition(0);
      start();
     }
    }
   }
  );
  
  }catch(Exception e)
  {
   program.napis("Nastala chyba pri nacitani skladby "+s+"\n"+e,'E');
  }
 }
 
 public void start()
 { 
  skladba.start();   
 }
 
 public void stop()
 {
  skladba.stop();   
 }
 
 public void close()
 {
  skladba.close();   
 }
}

class Consol extends JComponent
{
 public final int LOC_X=0;
 public final int LOC_Y=0;
 
 public final int SIZE_X=420;
 public final int SIZE_Y=360;
 
 private ArrayList text;
 
 public Consol()
 {
  text = new ArrayList();   
  setBounds(LOC_X,LOC_Y,SIZE_X,SIZE_Y);
 }
 
 public void cls()
 {
  text.clear();
  repaint();
 }
 
 public void add(String s)
 {
  text.add(s);
  if(text.size()>22)text.remove(0);
  repaint();
 }
 
 public void paintComponent(Graphics g)
 {
  g.setColor(Color.BLACK);
  g.fillRect(0,0,SIZE_X,SIZE_Y);
  
  g.setColor(Color.WHITE);
  for(int i=0;i<22;i++)
  {
   if(i==text.size())break;
   g.drawString((String)text.get(i),10,16+i*16);   
  }
 }
}

class Debugger extends JFrame implements ActionListener
{
 public final int LOC_X=10;
 public final int LOC_Y=10;

 public final int SIZE_X=420;
 public final int SIZE_Y=420;

 private final Core program;
 
 private final String TITLE = "Debugger - JETA";
 private final String FILE_DBG = ".DBG";
 private final String C_CLS = "CLS";
 private final String C_SAVE = "SAVE";
 private final String C_LOAD = "LOAD";
 
 private Consol out;
 private JTextField riadok;
 
 public Debugger(Core c)
 {
  program=c;
 }
 
 public void init()
 {
  setTitle(TITLE);
  setLocation(LOC_X,LOC_Y);
  setSize(SIZE_X,SIZE_Y);
  
  out = new Consol();
  
  riadok = new JTextField("");
  riadok.setBounds(0,360,420,32);
  riadok.addActionListener(this);
  
  getContentPane().setLayout(null);
  getContentPane().add(out);
  getContentPane().add(riadok);
  
  setResizable(false);
 }
 
 public void addOUT(String s)
 {
  out.add(s);   
 }
 
 public void clsOUT()
 {
  out.cls();   
 }
 
 public void actionPerformed(ActionEvent e)
 {
  String s=e.getActionCommand();
  
  if(s.trim().equals(""))return;
  
  out.add(">"+s);   
  riadok.setText("");
  
  if(s.equalsIgnoreCase(C_CLS))
  {
   out.cls();
   return;
  }

  if(s.equalsIgnoreCase(C_SAVE))
  {
   program.uloz(program.zdrojak.getNazovTextovky()+FILE_DBG);
   return;
  }

  if(s.equalsIgnoreCase(C_LOAD))
  {
   program.nacitaj(program.zdrojak.getNazovTextovky()+FILE_DBG);
   return;
  }
  
  if(s.startsWith(":") && s.indexOf(" ")>0)
  {
   program.zdrojak.zmen(s);
   program.zdrojak.chod();
   return;
  }
  
  if(s.startsWith(":") && s.indexOf(" ")==-1)
  {
   out.add("PREMENNA "+s+" = "+program.zdrojak.tabulka.get(s));   
   return;
  }
  
  program.zdrojak.chod(s);
 }
}

class FileJETA
{
 public static final String BIG_JETA = ".JETA";
 public static final String SMALL_JETA = ".jeta";
 
 private final Core program;
 
 public FileJETA(Core c)
 {
  program=c;    
 }
 
 public void zasifruj(String subor)
 {
  final String NAME = System.currentTimeMillis()+"SCRIPT.JETA";
  ArrayList text = new ArrayList();
  ArrayList sif = new ArrayList();
  
  try{         
   BufferedReader br = new BufferedReader(new FileReader(new File(subor)));
   String s=null;
   
   while( (s=br.readLine())!=null)
    text.add(s.trim());
   
   br.close(); 
   
  }catch(Exception e)
  {
   program.napis("Nastala chyba pri nacitani herneho scriptu\n"+e);   
  }
  
  byte[] key = new byte[256];
  for(int i=0;i<256;i++)
   key[i] = (byte)(Math.random()*64+1);   
 
  int pocet = text.size();
  int dlzka=0;
  int index=0;
  
  byte[] buffer = new byte[256];
  for(int i=0;i<pocet;i++)
  {
   buffer = ((String)text.get(i)).getBytes();
   dlzka = buffer.length;
 
   for(int j=0;j<dlzka;j++)
   {
    buffer[j]+=key[index++];
    if(index==256)index=0;
   }
   
   sif.add(buffer);
  }
  
  try{   
   ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(NAME)));
  
   oos.writeObject(key);
   oos.writeObject(sif);
   oos.close();

   program.napis("Script "+subor+" bol zasiforvany do suboru "+NAME,'I');  
  }catch(Exception e)
  {
   program.napis("Nastala chyba pri ulozeni zasifrovanych udajov do suboru "+NAME+"!\n"+e,'E');   
  }  
 }
 
 public ArrayList odsifruj(String subor)
 {
  byte[] key = new byte[256];
  ArrayList text = new ArrayList();
  ArrayList sif = new ArrayList();
  
  try{   
   ObjectInputStream oos = new ObjectInputStream(new FileInputStream(new File(subor)));
  
   key = (byte[])oos.readObject();
   sif = (ArrayList)oos.readObject();
   oos.close();

  }catch(Exception e)
  {
   program.napis("Nastala chyba pri nacitani zasifrovanych udajov zo suboru "+subor+"!\n"+e,'E');   
  }
  
  int pocet = sif.size();
  int dlzka=0;
  int index=0;
  
  byte[] buffer = new byte[256];
  for(int i=0;i<pocet;i++)
  {
   buffer = ((byte[])sif.get(i));
   dlzka = buffer.length;
   
   for(int j=0;j<dlzka;j++)
   {
    buffer[j]-=key[index++];
    if(index==256)index=0;
   }
   
   text.add(new String(buffer));
  }
  
  return (ArrayList)(text.clone());
 } 
}

class Tabulka implements Serializable
{
 private HashMap ident;
 
 public Tabulka()
 {
  ident = new HashMap();   
 }
  
 public void set(String s1,String s2)
 {
  if(s2.charAt(0)!=':')ident.put(s1,s2);
  else ident.put(s1,get(s2));
  
  //System.out.println(s1+" = "+get(s1));
 }
 
 public String get(String s)
 {
  Object obj = ident.get(s);
  
  if(obj==null)
  {
   set(s,"0");
   return get(s);
  }
  
  return (String)obj;  
 } 
 
 public int size()
 {
  return ident.size();   
 }
 
 public void reset()
 {
  ident.clear();   
 }
}

class Zdrojak
{ 
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
 public static final String I_SPAT = "<spä>";
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
     chyby.add("riadok : "+(i+1)+" - nesprávna definícia podmienky !"); 
     continue;       
    }
    
    if((s.length-2)%3!=0)
    {
     chyby.add("riadok : "+(i+1)+" \""+getRiadok(i)+"\" - podmienka ma nesprávny poèet parametrov !");   
    }
    else
    {
     for(int j=1;j<s.length-1;j+=3)
     {
      if(!isPremenna(s[j]) && !isCislo(s[j]))
       chyby.add("riadok : "+(i+1)+" parameter \""+s[j]+"\" nie je platnou hodnotou pri porovnávaní !");
      if(!isPremenna(s[j+2]) && !isCislo(s[j+2]))
       chyby.add("riadok : "+(i+1)+" parameter \""+s[j+2]+"\" nie je platnou hodnotou pri porovnávaní !");
      if(!s[j+1].equals(I_ROVNA_SA) && !s[j+1].equals(I_NEROVNA_SA))
       chyby.add("riadok : "+(i+1)+" operátor \""+s[j+1]+"\" - nie je platným parametom pri porovnávaní hodnôt !");   
     }
    }
   }
   
   s=getSlova(getPrikazBezPodmienky(getRiadok(i)));     
   if(s.length==0)continue;
   
   String nespravnyPocetParametrov = 
   "riadok : "+(i+1)+" \""+getRiadok(i)+"\" - nesprávny poèet parametrov !";
   
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
   if(priznak==0)chyby.add("riadok : "+(i+1)+" príkaz : \""+s[0]+"\" nie je platným príkazom interpretera "+program.TITLE+" !");
  }
  
  boolean jeTuPrikazName=false;
  for(int i=0;i<pocet;i++)
   if(getRiadok(i).startsWith(C_NAME))
   {
    nazovTextovky=getRiadok(i).replaceAll(C_NAME,"").trim().replaceAll(" ","_");
    jeTuPrikazName=true;
    break;
   }
  
  if(!jeTuPrikazName)chyby.add("príkaz \""+C_NAME+"\" nenájdený !");
  
  String nazovMiesta;
  for(int i=0;i<pocet;i++)
   if(getRiadok(i).startsWith(C_MIESTO))
   {
    nazovMiesta=getRiadok(i).replaceAll(C_MIESTO,"").trim();
    
    tento:for(int j=0;j<miesta.size();j++)
     if(((String)miesta.get(j)).equals(nazovMiesta))
     {
      chyby.add("riadok : "+(i+1)+" miesto : \""+nazovMiesta+"\" už existuje");
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
  
  if(!jeStart)chyby.add("miesto \""+I_START+"\" sa nepodarilo nájs !");

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
    if(!isCislo(s[1]))chyby.add("riadok : "+(i+1)+" parameter : \""+s[1]+"\" nie je platným èíslom !");
    if(!isCislo(s[2]))chyby.add("riadok : "+(i+1)+" parameter : \""+s[2]+"\" nie je platným èíslom !");
    if(!isCislo(s[3]))chyby.add("riadok : "+(i+1)+" parameter : \""+s[3]+"\" nie je platným èíslom !");
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

class Moznost extends JComponent implements MouseListener
{
 private Font FONT = new Font("SainSerif",Font.PLAIN,15);
 private Color COLOR = new Color(255,255,255);
 public final int SIZE_X=180;
 public final int SIZE_Y=20;
     
 private String text;
 private boolean tu;
 
 public Moznost(String s)
 {
  tu=false;
  addMouseListener(this);
  setSize(SIZE_X,SIZE_Y);
  setText(s);
 }
 
 public final void setText(String s)
 {
  text=s;
  repaint();
 }

 public final void setFont(Font f)
 {
  FONT = f;
  repaint();
 }
 
 public final void setButtonColor(Color c)
 {
  COLOR=c;
  repaint();
 }
 
 public final String getText()
 {
  return text.trim();   
 }
 
 public void mousePressed(MouseEvent e){}
 public void mouseReleased(MouseEvent e){}
 public void mouseEntered(MouseEvent e)
 {
  tu=true;   
  repaint();
 }
 
 public void mouseExited(MouseEvent e)
 {
  tu=false; 
  repaint();
 }
 
 public void mouseClicked(MouseEvent e)
 {
 }
 
 public void paintComponent(Graphics g)
 {
  //g.setColor(Color.BLACK);
  //g.fillRect(0,0,getWidth(),getHeight());
  
  if(text.equals(""))return;
  
  g.setColor(COLOR);
  g.setFont(FONT);
  if(tu)g.drawRect(0,0,SIZE_X-1,SIZE_Y-1);
  g.drawString(text.replaceAll("_"," "),(int)((SIZE_X-getFontMetrics(FONT).stringWidth(text))/2),FONT.getSize());
 }
}

class Menu extends JPanel implements MouseListener
{
 private final int LOC_X=10;
 private final int LOC_Y=410;

 public final int SIZE_X=620;
 public final int SIZE_Y=100;

 private Color color = new Color(255,255,255);
 
 private final Core program;
 private Moznost[] moznost;
 private String prikaz;
 
 private String menu[][];
 
 public Menu(Core c)
 {
  program=c; 
  menu = new String[10][9];
 }
 
 public void init()
 {
  setBounds(LOC_X,LOC_Y,SIZE_X,SIZE_Y);
  setBackground(Color.BLACK);
  setLayout(null);      
  
  moznost = new Moznost[9];
  
  for(int i=0;i<9;i++)
  {
   moznost[i] = new Moznost(""); 
   moznost[i].addMouseListener(this);
   
   switch(i)
   {
    case 0: case 1: case 2: moznost[i].setLocation(10+i*210,25+0); break;
    case 3: case 4: case 5: moznost[i].setLocation(10+(i-3)*210,25+25); break;
    case 6: case 7: case 8: moznost[i].setLocation(10+(i-6)*210,25+50); break;    
   }
   
   add(moznost[i]);
  } 
  
  cls();  
 }
 
 public void paint(Graphics g)
 {
  super.paint(g);
  if(moznost[0].getText().equals(""))return;
  
  g.setColor(color);
  if(!prikaz.equals(""))g.drawString(prikaz.replaceAll("_"," ")+" +",20,20);
  g.drawRect(0,0,getWidth()-1,getHeight()-1);
 }
 
 public void cls()
 {
  for(int i=0;i<10;i++)
   for(int j=0;j<9;j++)
    menu[i][j]="";   
     
  for(int i=0;i<9;i++)
   moznost[i].setText("");  
  
  prikaz="";
 }
 
 public void add(int n,String s)
 {
  for(int i=0;i<9;i++)
   if(menu[n][i].equals(""))
   {
    menu[n][i]=s;
    //System.out.println("menu["+n+"]["+i+"]="+s);
    break;
   }
   
  repaint(); 
 }

 public void add(String s1,String s2)
 {
  int n=0;
  
  for(int i=0;i<9;i++)
   if(menu[0][i].equals(s1))
   {
    n=i;
    break;
   }
  
  add(n+1,s2);
 }
 
 public void zobraz(int n)
 {
  for(int i=0;i<9;i++)
   moznost[i].setText(menu[n][i]);
  
  repaint();
 }

 public void zobraz(String s)
 {
  int n=0;
  
  for(int i=0;i<9;i++)
   if(menu[0][i].equals(s))
   {
    n=i;
    break;
   }
  
  zobraz(n+1);
 }
 
 public String getPrikaz()
 {
  return prikaz.trim();
 }
 
 public void setMenuColor(Color c)
 {
  color=c;
     
  for(int i=0;i<moznost.length;i++)
   moznost[i].setButtonColor(c);   
 }
 
 public void mouseClicked(MouseEvent e){}
 public void mouseEntered(MouseEvent e){}
 public void mouseExited(MouseEvent e){}
 public void mouseReleased(MouseEvent e){}
 public void mousePressed(MouseEvent e)
 {
  String s="";
  Object source=e.getSource();
  
  /*
   tato vetva je iba prebezpecnost
   keby sa nahodou zasekol program
  */
  if(prikaz.indexOf(" ")>0)
  {  
   program.zdrojak.chod();
   return;
  }
  
  for(int i=0;i<9;i++)
   if(source==moznost[i])
   {
    s=moznost[i].getText();
    break;
   }

  if(s.equals(""))return;
  
  if(s.equals(Zdrojak.I_OK))
  {
   program.texty.nacitajObrazok(null);
   program.zdrojak.pokracuj();
   return;
  }
  
  if(s.equals(Zdrojak.I_SPAT))
  {
   program.zdrojak.nastavMenu();
   return;
  }
  
  if(prikaz.equals(""))
  {
   prikaz=s;
   zobraz(s);
  }
  else 
  {
   prikaz+=" "+s;
   program.zdrojak.akcia(prikaz);
  }        
  
  //System.out.println("prikaz "+prikaz);
 }
}

class Texty extends JComponent
{
 private final Font FONT = new Font("SainSerif",Font.BOLD,16);
 private final String NAME_SHOW_IMAGE = "SHOW.PNG";
 private final Image image;
  
 private final int LOC_X=10;
 private final int LOC_Y=10;

 public final int SIZE_X=620;
 public final int SIZE_Y=400;

 private Core program;
 private ArrayList riadok;
 private ArrayList farba;
 private Image obrazok;
 
 public Texty(Core c)
 {
  program=c;
  image = program.getImage(NAME_SHOW_IMAGE);
  riadok = new ArrayList();
  farba = new ArrayList();
 }
 
 public void init()
 {
  setBounds(LOC_X,LOC_Y,SIZE_X,SIZE_Y);   
  //add("žiadna textovka nebola naèítaná..");
 }
  
 public void cls()
 {
  riadok.clear();   
  farba.clear();
  nacitajObrazok(null);
 }
 
 public void add(String s1)
 {
  add(s1,"255 255 255");   
 }
 
 public void add(String s1,String s2)
 {
  if(riadok.size()==20)riadok.remove(0);   
  if(farba.size()==20)farba.remove(0);   
  
  nacitajObrazok(null);
  riadok.add(s1);
  farba.add(s2);
  
  repaint();
 }
  
 public void nacitajObrazok(String s)
 {
  if(s==null)
  {
   obrazok=null;
   return;
  }
  
  try{  
   obrazok = new ImageIcon(program.zdrojak.getAdresarSuboru()+s).getImage();
  }catch(Exception e)
  {
   program.napis("Obrazok "+s+" sa nenasiel !",'E');   
  }
  
  repaint();
 }
 
 public void paintComponent(Graphics g)
 {  
  g.setColor(Color.BLACK);
  g.fillRect(0,0,SIZE_X,SIZE_Y);
   
  if(!program.zdrojak.isNacitanaTextovka())
  {
   g.drawImage(image,0,0,this);
   obrazok=null;
   return;
  }
    
  if(obrazok!=null)
  {
   g.drawImage(obrazok,(int)((getWidth()-obrazok.getWidth(this))/2),
   (int)((getHeight()-obrazok.getHeight(this))/2),this);
   return;
  }  
  
  g.setFont(FONT);
  
  for(int i=0;i<20;i++)
  {
   if(i<riadok.size())
   {
    g.setColor(program.zdrojak.getCreateColor((String)farba.get(i)));   
    g.drawString((String)riadok.get(i),10,16+i*20);       
   }
  }
 }
}

public class Core extends JFrame implements ActionListener,KeyListener
{
 public final String TITLE="JETA ver 1.8";

 public final String INFO=
 "Dušan Ïurech ( Oroborus )\n"+
 "http://oroborus.wz.cz\n"+
 "Dusan.D@Centrum.Sk";

 public static final String O_FILL="fill";
 public static final String O_PLATFORM="platform";
 public static final String O_NOSOUND="nosound";
 public static final String O_NOIMAGE="noimage";

 private final Image IKONA = getImage("IKONA.PNG");
 private final ImageIcon oroborusImage = getImageIcon("oroborus.jpg");
 
 private final boolean SCREEN;
 public final boolean NO_SOUND;
 public final boolean NO_IMAGE;
 
 private final int MAX_X=(int)Toolkit.getDefaultToolkit().getScreenSize().getWidth();
 private final int MAX_Y=(int)Toolkit.getDefaultToolkit().getScreenSize().getHeight();

 private final int SIZE_X=640;
 private final int SIZE_Y1=430;
 private final int SIZE_Y2=580;
 
 private JMenuBar lista;
 private JMenu menu1;
 private JMenuItem moznost1;
 private JMenuItem moznost2;
 private JMenuItem moznost3;
 private JMenuItem moznost4;
 private JMenuItem moznost5;
 private JMenuItem moznost6;
 private JMenuItem moznost7;
 private JMenuItem moznost8;
 private JMenuItem moznost9;
 
 private JFileChooser vyberSubor;
 private JPanel panel;
 public Texty texty;
 public Menu menu;
 
 public Zdrojak zdrojak;
 public FileJETA fileJETA;
 public Debugger debugger;
  
 public Core(boolean b1,boolean b2,boolean b3)
 {
  SCREEN=b1;   
  NO_SOUND=b2;
  NO_IMAGE=b3;  
 }
 
 public void init()
 { 
  vyberSubor = new JFileChooser();
  vyberSubor.setDialogTitle("Vyber súbor");
  vyberSubor.setCurrentDirectory(new File("."));
     
  texty = new Texty(this);   
  texty.init();
  
  menu = new Menu(this);
  menu.init();
  
  debugger = new Debugger(this);
  debugger.init();
  
  panel = new JPanel();
  panel.setBackground(Color.BLACK);
  panel.setLayout(null);
  panel.add(texty);
  panel.add(menu);

  zdrojak = new Zdrojak(this);
  fileJETA = new FileJETA(this);
    
  if(!SCREEN)panel.setBounds(0,0,SIZE_X,SIZE_Y2);   
  else panel.setBounds((MAX_X-SIZE_X)/2,(MAX_Y-SIZE_Y2)/2,SIZE_X,SIZE_Y2);
  
  moznost1 = new JMenuItem("Otvor..");
  moznost1.addActionListener(this);
  moznost1.setMnemonic('o');

  moznost2 = new JMenuItem("Reštartuj");
  moznost2.addActionListener(this);
  moznost2.setMnemonic('r');
  
  moznost3 = new JMenuItem("Ulož");
  moznost3.addActionListener(this);
  moznost3.setMnemonic('u');
  
  moznost4 = new JMenuItem("Naèítaj");
  moznost4.addActionListener(this);
  moznost4.setMnemonic('n');

  moznost5 = new JMenuItem("Debugger..");
  moznost5.addActionListener(this);
  moznost5.setMnemonic('b');

  moznost6 = new JMenuItem("Zašifruj subor..");
  moznost6.addActionListener(this);
  moznost6.setMnemonic('f');
  
  moznost7 = new JMenuItem("Manuál..");
  moznost7.addActionListener(this);
  moznost7.setMnemonic('m');
  
  moznost8 = new JMenuItem("Autor..");
  moznost8.addActionListener(this);
  moznost8.setMnemonic('o');
  
  moznost9 = new JMenuItem("Koniec");
  moznost9.addActionListener(this);
  moznost9.setMnemonic('k');
  
  menu1 = new JMenu("Program");
  menu1.setMnemonic('p');
  
  menu1.add(moznost1);
  menu1.addSeparator();
  menu1.add(moznost2);
  menu1.add(moznost3);
  menu1.add(moznost4);
  menu1.addSeparator();
  menu1.add(moznost5);
  menu1.add(moznost6);
  menu1.addSeparator();
  menu1.add(moznost7);
  menu1.add(moznost8);
  menu1.addSeparator();
  menu1.add(moznost9);
  
  lista = new JMenuBar();
  lista.add(menu1);
  
  setTitle(TITLE);
  setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
  setIconImage(IKONA);
  setJMenuBar(lista);
  
  getContentPane().setBackground(Color.BLACK);
  getContentPane().setLayout(null);
  getContentPane().add(panel);     
  
  setResizable(false);
  addKeyListener(this);
  if(SCREEN)setUndecorated(true);
  
  setUvod();  
 }
 
 public void setVelkost(int x,int y)
 {
  if(SCREEN)
  {
   setVisible(false);
   setLocation(0,0);     
   setSize(MAX_X,MAX_Y);     
   setVisible(true);
   return;
  }
  
  setVisible(false);
  setLocation((MAX_X-x)/2,(MAX_Y-y)/2);     
  setSize(x,y);
  setVisible(true);
 }
 
 public void setUvod()
 {
  texty.cls();
  menu.cls();
  moznost2.setEnabled(false);
  moznost3.setEnabled(false);
  moznost4.setEnabled(false);    
  moznost5.setEnabled(false);      
  setVelkost(SIZE_X,SIZE_Y1); 
 }
 
 public final ImageIcon getImageIcon(String s)
 {
  return new ImageIcon(getClass().getResource(s));   
 }

 public final Image getImage(String s)
 {
  return getImageIcon(s).getImage();   
 }
 
 public final void napis(String s)
 {
  napis(s,'P');   
 }
 
 public final void napis(String s,char mode)
 {
  final String FUCK_TITLE = "Fuck error - don't panic !";
  final int n;
  
  switch(mode)
  {
   case 'I' : n=JOptionPane.INFORMATION_MESSAGE; break;
   case 'W' : n=JOptionPane.WARNING_MESSAGE; break;
   case 'E' : n=JOptionPane.ERROR_MESSAGE; break;
   case 'P' : n=JOptionPane.PLAIN_MESSAGE; break;   
   default :
    napis("Nastala chyba pri zobrazovani okna !\n"+
    "Zle nastavenie parametra (mode="+mode+")",'E');
   return;
  }
  
  JOptionPane.showMessageDialog(this,s,(mode=='E'?FUCK_TITLE:TITLE),n);     
 }

 public final String otazka(String q,String[] f)
 {
  JOptionPane jop = new JOptionPane(q,JOptionPane.QUESTION_MESSAGE,0,null,f);
  JDialog jdl = jop.createDialog(this,TITLE);
  
  jdl.setModal(true);
  jdl.setVisible(true);
  
  return (String)jop.getValue();
 }
  
 private void otvor()
 {
  int hodnota = vyberSubor.showOpenDialog(this);  
  if(hodnota==JFileChooser.APPROVE_OPTION)otvor(vyberSubor.getSelectedFile().getPath());
 }
 
 public void otvor(String s)
 {
  if(!zdrojak.nacitaj(s))
  {
   setUvod();
   return;
  }
       
  moznost2.setEnabled(true);
  moznost3.setEnabled(true);
  moznost4.setEnabled(true);  
  moznost5.setEnabled(!zdrojak.isSifrovany());      
  
  setVelkost(SIZE_X,SIZE_Y2);
 }
 
 private void reset()
 {
  zdrojak.nacitaj();     
 }
 
 public void uloz()
 {
  uloz(zdrojak.getNazovTextovky()+".sav");   
 }
 
 public void uloz(String subor)
 {
  try{
   File file = new File(subor);
   FileOutputStream dos = new FileOutputStream(file);
   ObjectOutputStream oos = new ObjectOutputStream(dos);
  
   oos.writeObject(zdrojak.getMiesto());
   oos.writeObject(zdrojak.tabulka);
   oos.close();

   napis("Pozícia bola uložená do súboru : "+subor,'I');   

  }catch(Exception e)
  {      
   napis("Nastala chyba pri uložení pozície !\n"+e.toString(),'E');   
  }
 }
 
 public void nacitaj()
 {
  nacitaj(zdrojak.getNazovTextovky()+".sav");   
 }
 
 public void nacitaj(String subor)
 {  
  if(!(new File(subor)).exists())
  {
   napis("Subor "+subor+" sa nenasiel !",'W');
   return;
  }
  
  String s="";
  
  try{
   File file = new File(subor);
   FileInputStream dis = new FileInputStream(file);
   ObjectInputStream ois = new ObjectInputStream(dis);
  
   s = (String)ois.readObject();
   zdrojak.tabulka = (Tabulka)ois.readObject();
   ois.close();
   
   napis("Pozícia bola naèítaná zo súboru : "+subor,'I');      
  }catch(Exception e)
  {      
   napis("Nastala chyba pri naèítaní pozície !\n"+e.toString(),'E');   
  }    
  
  zdrojak.chod(s);
 }
 
 private void spustDebuger()
 {
  debugger.setVisible(!debugger.isVisible());   
  debugger.clsOUT();
 }
 
 private void zasifruj()
 {
  int hodnota = vyberSubor.showOpenDialog(this);  
  if(hodnota==JFileChooser.APPROVE_OPTION)fileJETA.zasifruj(vyberSubor.getSelectedFile().getPath());
 } 

 private void manual()
 {
  try{
   Runtime.getRuntime().exec("cmd.exe /c manual.TXT");
  }catch(Exception e)
  {
   napis("Nastala chyba pri zobrazení manuálu.\n"+e,'E');        
  }
 }

 private void oPrograme()
 {
  JOptionPane.showMessageDialog(this,INFO,TITLE,JOptionPane.PLAIN_MESSAGE,oroborusImage);      
 }
 
 public void exit()
 {
  dispose();
  System.exit(0);
 }
 
 public void actionPerformed(ActionEvent e)
 {
  Object source=e.getSource();   
  
  if(source==moznost1)otvor();
  if(source==moznost2)reset();
  if(source==moznost3)uloz();
  if(source==moznost4)nacitaj();
  if(source==moznost5)spustDebuger();
  if(source==moznost6)zasifruj();
  if(source==moznost7)manual();
  if(source==moznost8)oPrograme();
  if(source==moznost9)exit();
 }
 
 public String toString()
 {
  return "program=\""+TITLE+"\" cas=\""+System.currentTimeMillis()+
  "\" java version=\""+System.getProperty("java.version")+"\"";   
 }
  
 public void keyReleased(KeyEvent e){}
 public void keyTyped(KeyEvent e){}
 public void keyPressed(KeyEvent e)
 {
  if(e.getKeyCode()==32)lista.setVisible(!lista.isVisible());
 }
 
 public static void main(String[] args)
 {  
  boolean fill=false;
  boolean platform=false;
  boolean nosound=false;
  boolean noimage=false;
  String subor=null;
  
  for(int i=0;i<args.length;i++)
  {
   if(args[i].equals("-"+O_PLATFORM))platform=true;
   if(args[i].equals("-"+O_FILL))fill=true;
   if(args[i].equals("-"+O_NOSOUND))nosound=true;
   if(args[i].equals("-"+O_NOIMAGE))noimage=true;
   if(!args[i].startsWith("-"))subor=args[i].trim();
  }
  
  if(platform)
  {
   try{
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());   
   }catch(Exception e)
   {
    System.out.println("e="+e);   
   }
  }

  Core core = new Core(fill,nosound,noimage);
  
  try{
   core.init();
   if(subor!=null)core.otvor(subor);
  }catch(Exception e)
  {
   core.napis("Chyba typu :"+e,'E');   
  }
 }   
}