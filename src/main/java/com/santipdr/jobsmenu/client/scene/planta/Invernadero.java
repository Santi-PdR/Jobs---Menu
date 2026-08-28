package com.santipdr.jobsmenu.client.scene.planta;

import com.santipdr.jobsmenu.client.scene.Marco;
import com.santipdr.jobsmenu.client.scene.Nivel;
import com.santipdr.jobsmenu.client.ui.Paleta;
import net.minecraft.client.gui.GuiGraphics;

/** Nivel 6 reconstruido: invernadero de hierro, vidrio y vegetacion invasiva. */
public final class Invernadero implements Planta {
    private static final int TRAMOS = 12;
    public int tramos(){ return TRAMOS; }

    @Override public void dibujar(GuiGraphics g, Marco m, Nivel n, float luz, float tiempo){
        NuevaEscena.base(g,m,n,luz,TRAMOS,0.46F);
        for(int j=3;j<=10;j+=2){
            float dx=Trazo.profundidad(j,10);
            if(dx>4.5F) continue;
            int x0=Math.round(m.enX(dx,-0.82F));
            int x1=Math.round(m.enX(dx,0.82F));
            int y=Math.round(m.techoEn(dx)+m.h()*dx*0.12F);
            g.fill(x0,y,x1,y+Math.max(1,(int)(m.h()*dx*0.018F)),Paleta.conAlfa(n.techoJunta,0.62F));
            NuevaEscena.columna(g,m,n,dx,-0.88F,0.025F,n.junta,luz);
            NuevaEscena.columna(g,m,n,dx,0.88F,0.025F,n.junta,luz);
        }
        int beamW=Math.max(16,m.ancho()/9);
        int cx=(int)(m.ancho()*0.52F);
        g.fillGradient(cx-beamW,0,cx+beamW,(int)(m.alto()*0.72F),
                Paleta.conAlfa(0xFFE8FFD8,0.16F*luz),Paleta.conAlfa(0xFFB9D49B,0.01F));
        for(int i=0;i<28;i++){
            int x=(int)(Trazo.pseudo(1300+i*3)*m.ancho());
            int y=(int)(m.alto()*(0.36F+Trazo.pseudo(1301+i*3)*0.58F));
            int h=4+(int)(Trazo.pseudo(1302+i*3)*18);
            int green=Paleta.mezclar(0xFF1C2A13,0xFF738B42,Trazo.pseudo(1400+i));
            g.fill(x,y,x+1,y+h,Paleta.conAlfa(green,0.55F*luz));
            if(i%3==0) g.fill(x-2,y+2,x+3,y+4,Paleta.conAlfa(green,0.42F*luz));
        }
        for(int i=0;i<7;i++){
            int x=(int)(m.ancho()*(0.08F+i*0.14F));
            int y=(int)(m.alto()*(0.20F+(i%3)*0.07F));
            g.fill(x,y,x+Math.max(8,m.ancho()/22),y+1,Paleta.conAlfa(n.luz,0.08F*luz));
        }
    }

    @Override public void primerPlano(GuiGraphics g, Marco m, Nivel n, float luz, float tiempo){
        int w=Math.max(10,m.ancho()/20);
        g.fill(0,0,w,m.alto(),Paleta.conAlfa(0xFF10160B,0.42F));
        g.fill(m.ancho()-w,0,m.ancho(),m.alto(),Paleta.conAlfa(0xFF10160B,0.42F));
    }
}
