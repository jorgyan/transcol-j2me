package com.lessandro.transcol;

import java.io.*;
import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import java.util.Vector;

/**
 * Transcol
 * @author lzm
 */
public class Transcol extends MIDlet implements CommandListener {

    Command browse;
    Command about;
    Command back;
    Command obs;
    Command ok;
    Form form;
    Vector v;
    List ls;
    TextField tf;

    int tela = 0;

    int tipo;
    int linha;
    int saida;
    int dias;

    int off_tipo;
    int size_tipo;
    int off_linha;
    int off_saida;
    Vector offs_saida;

    int num_linhas;
    int num_saidas;
    int num_dias;

    int table_len;
    int table_len2;
    int[] table_off;
    byte[] table_str;

    boolean show_obs = false;
    String str_obs;
    String str_hrs;
    StringItem hrs;

    int get_num(int line) {
        return Integer.parseInt(get_str(line));
    }

    String get_str(int line) {
        String tmp = "";
        int p = table_off[line];
        while (table_str[p] != '\n')
            tmp += (char)table_str[p++];

        return tmp;
    }

    public Transcol() {
        browse = new Command("Browse", Command.BACK, 1);
        about = new Command("About", Command.BACK, 1);
        back = new Command("Back", Command.BACK, 1);
        obs = new Command("Obs", Command.HELP, 1);
        ok = new Command("Ok", Command.OK, 1);
        v = new Vector();

        String nums[] = {"Seletivo", "500", "600", "700", "800", "900"};
        ls = new List("Transcol", List.IMPLICIT, nums, null);
        ls.setCommandListener(this);
        ls.addCommand(about);

        offs_saida = new Vector();

        table_len = 0;
        table_len2 = 0;
        
        try {
            InputStream is = getClass().getResourceAsStream("/transcol.txt");

            while (true) {
                int b = is.read();
                if (b == -1) {
                    break;
                }
                if (b == '\n') {
                    table_len++;
                }
                table_len2++;
            }
        } catch (IOException e) {
        }

        table_off = new int[table_len+1];
        table_str = new byte[table_len2];

        try {
            InputStream is = getClass().getResourceAsStream("/transcol.txt");
            int n = is.read(table_str);

            int p = 0;
            table_off[p++] = 0;

            for (int i=0; i<n; i++) {
                if (table_str[i] == '\n')
                    table_off[p++] = i+1;
            }
        } catch (IOException e) {
        }

        StringItem si1 = new StringItem(null, "Transcol App.\n");
        StringItem si2 = new StringItem(null, "por Lessandro Z. M.\n");
        StringItem si3 = new StringItem(null, "http://lessandro.com/transcol/\n");

        si1.setFont(Font.getFont(Font.FACE_PROPORTIONAL,
            Font.STYLE_BOLD, Font.SIZE_LARGE));
        si2.setFont(Font.getFont(Font.FACE_PROPORTIONAL,
            Font.STYLE_BOLD, Font.SIZE_MEDIUM));
        si3.setFont(Font.getFont(Font.FACE_PROPORTIONAL,
            Font.STYLE_UNDERLINED, Font.SIZE_SMALL));

        tf = new TextField("Linha:", "", 4, TextField.NUMERIC);

        form = new Form("About");
        form.append(si1);
        form.append(si2);
        form.append(si3);
        form.append(new Spacer(1, 10));
        form.append(tf);
        form.setCommandListener(this);
        form.addCommand(browse);
        form.addCommand(ok);
    }

    private void advance() {
        tela++;
        v.addElement(ls);

        switch (tela) {
        case 1:
            ls = new List("Linha", List.IMPLICIT);

            off_tipo = get_num(table_len-7+tipo);
            size_tipo = get_num(table_len-7+tipo+1) - off_tipo;
            num_linhas = get_num(off_tipo+size_tipo-1);

            for (int i=0; i<num_linhas; i++)
                ls.append(get_str(off_tipo+i), null);

            break;

        case 2:
            ls = new List("Saida", List.IMPLICIT);

            off_linha = get_num(off_tipo+size_tipo-num_linhas+linha-1);
            num_saidas = get_num(off_linha);

            for (int i=0; i<num_saidas; i++)
                ls.append(get_str(off_linha+i+1), null);

            int off = off_linha + num_saidas + 1;
            offs_saida.removeAllElements();
            
            for (int i=0; i<num_saidas; i++) {
                offs_saida.addElement(new Integer(off));
                off += 2*get_num(off)+1;
            }

            off = off_tipo+num_linhas;
            for (int i=0; i<linha; i++)
                off += get_num(off)+1;

            int num_obs = get_num(off);

            if (num_obs > 0) {
                str_obs = "";
                for (int i=0; i<num_obs; i++)
                    str_obs += get_str(off+i+1) + "\n";
            } else
                str_obs = null;

            break;

        case 3:
            ls = new List("Dias", List.IMPLICIT);

            off_saida = ((Integer)offs_saida.elementAt(saida)).intValue();
            num_saidas = get_num(off_saida);

            for (int i=0; i<num_saidas; i++)
                ls.append(get_str(off_saida+i+1), null);

            break;

        case 4:
            str_hrs = get_str(off_saida+num_saidas+dias+1);
            show_obs = false;

            hrs = new StringItem(null, str_hrs);
            hrs.setFont(Font.getFont(Font.FACE_MONOSPACE, Font.STYLE_PLAIN, Font.SIZE_MEDIUM));

            Form f = new Form("Horarios");
            f.append(hrs);
            f.addCommand(back);
            if (str_obs != null)
                f.addCommand(obs);
            f.setCommandListener(this);

            Display.getDisplay(this).setCurrent(f);
            
            return;
        }

        ls.setCommandListener(this);
        ls.addCommand(back);
        Display.getDisplay(this).setCurrent(ls);
    }

    public void commandAction(Command com, Displayable dis) {
        if (com == List.SELECT_COMMAND) {
            if (tela == 0) tipo = ls.getSelectedIndex();
            if (tela == 1) linha = ls.getSelectedIndex();
            if (tela == 2) saida = ls.getSelectedIndex();
            if (tela == 3) dias = ls.getSelectedIndex();
            if (tela == 4) return;

            advance();
        }

        if (com == about) {
            Display.getDisplay(this).setCurrent(form);
        }

        if (com == back) {
            ls = (List)v.lastElement();
            v.removeElementAt(v.size()-1);
            if (--tela == 0)
                Display.getDisplay(this).setCurrent(ls);
            Display.getDisplay(this).setCurrent(ls);
        }

        if (com == obs) {
            show_obs = !show_obs;

            hrs.setText(show_obs ? str_obs : str_hrs);
        }

        if (com == browse) {
            Display.getDisplay(this).setCurrent(ls);
        }

        if (com == ok) {
            String num = tf.getString();
            char t = num.charAt(0);

            tipo = -1;

            if (t == '1') tipo = 0;
            if (t == '5') tipo = 1;
            if (t == '6') tipo = 2;
            if (t == '7') tipo = 3;
            if (t == '8') tipo = 4;
            if (t == '9') tipo = 5;

            if (tipo == -1) return;
            advance();

            linha = -1;

            for (int i=0; i<num_linhas; i++)
                if (get_str(off_tipo+i).startsWith(num)) {
                    linha = i;
                    break;
                }

            if (linha == -1) return;
            advance();
        }
    }

    public void startApp() {
        Display.getDisplay(this).setCurrent(form);
    }

    public void pauseApp() {
    }

    public void destroyApp(boolean unconditional) {
    }
}
