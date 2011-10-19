package perminute;

/**
 * @author Revence
 */

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;

class ResultPage extends Form implements CommandListener
{
    Displayable prev;
    MIDlet mama;
    StringItem disp;
    Command don;
    public ResultPage(String t, Displayable d, MIDlet m, int cmpt)
    {
        super(t);
        disp = new StringItem("", t + ": " + Integer.toString(cmpt) + " per minute.");
        mama = m;
        prev = d;
        don  = new Command("Done", Command.OK | Command.EXIT, 0);
        append(disp);
        setCommandListener(this);
        addCommand(don);
    }

    public void commandAction(Command c, Displayable d)
    {
        if(c == don && d == this)
        {
            Display.getDisplay(mama).setCurrent(prev);
        }
    }
}

class CounterPage extends Form implements CommandListener, Runnable
{
    Command inc, don, stt;
    StringItem lab;
    boolean air;
    Displayable prev;
    MIDlet mama;
    int cmpt, ctdn;
    ResultPage rslt;
    String ttl;
    Thread thd;
    public CounterPage(String t, Displayable p, MIDlet m)
    {
        super(t);
        ttl  = t;
        cmpt = 0;
        ctdn = 60;
        prev = p;
        air  = false;
        mama = m;
        lab  = new StringItem("", "Press \"Start\" to begin counting.");
        inc  = new Command("+1", Command.OK, 0);
        stt  = new Command("Start", Command.OK, 0);
        don  = new Command("Done", Command.EXIT, 0);
        append(lab);
        addCommand(stt);
        addCommand(don);
        setCommandListener(this);
    }

    public void commandAction(Command c, Displayable d)
    {

        if(! air)
        {
            if(c == don && d == this)
            {
                Display.getDisplay(mama).setCurrent(prev);
            }
            else if(c == stt && d == this)
            {
                air = true;
                removeCommand(stt);
                addCommand(inc);
                thd = new Thread(this);
                thd.start();
                this.increment();
            }
        }
        else
        {
            if(c == don && d == this)
            {
                //  thd.interrupt();    //  Not supported in low-end.
                ctdn = 0;
                this.nextScreen();
            }
        }
        
        if(c == inc && d == this && air)
        {
            this.increment();
        }
    }

    public void run()
    {
        try
        {
            for(; ctdn > 0; --ctdn)
            {
                repaint();
                Thread.sleep(1000);
                //  if thd.interrpt() no workee, simulate it.
                if(ctdn == 0) throw new InterruptedException();
            }
        }
        catch(InterruptedException e)
        {}
        this.nextScreen();
    }

    void nextScreen()
    {
        rslt = new ResultPage(ttl, prev, mama, cmpt);
        Display.getDisplay(mama).setCurrent(rslt);
    }

    void increment()
    {
        ++cmpt;
        /*Display d = Display.getDisplay(mama);
        d.vibrate(500);
        repaint();*/
        AlertType.CONFIRMATION.playSound(Display.getDisplay(mama));
    }

    void repaint()
    {
        lab.setText(Integer.toString(cmpt) + " count" + (cmpt == 1 ? "" : "s")
                + ".\n\n" +
                Integer.toString(ctdn) + " second" + (ctdn == 1 ? "" : "s") + " left.");
    }
}

class InitialPage extends List implements CommandListener
{
    Command quitter;
    MIDlet mama;
    CounterPage counter;
    public InitialPage(MIDlet m)
    {
        super("", Choice.IMPLICIT | Choice.EXCLUSIVE);
        mama = m;
        append("Breathing Rate", null);
        append("Heartbeat Rate", null);
        quitter = new Command("Exit", Command.EXIT, 0);
        addCommand(quitter);
        setCommandListener(this);
    }
    
    public void commandAction(Command c, Displayable d)
    {
        if(c == quitter && d == this)
        {
            mama.notifyDestroyed();
        }
        if(c != List.SELECT_COMMAND || d != this) return;
        String titre = "Heartbeat Rate";
        switch(getSelectedIndex())
        {
            case 0:
                titre = "Breathing Rate";
                break;
            default:
                //  Is the default.
        }
        counter = new CounterPage(titre, this, mama);
        Display.getDisplay(mama).setCurrent(counter);
    }
}

public class PerMinute extends MIDlet
{
    InitialPage ini;
    public PerMinute()
    {
        ini = new InitialPage(this);
    }

    public void startApp()
    {
        Display.getDisplay(this).setCurrent(ini);
    }

    public void pauseApp()
    {

    }

    public void destroyApp(boolean eh)
    {

    }
}