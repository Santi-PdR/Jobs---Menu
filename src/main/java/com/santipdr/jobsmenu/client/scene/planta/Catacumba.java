package com.santipdr.jobsmenu.client.scene.planta;

import com.santipdr.jobsmenu.client.scene.Marco;
import com.santipdr.jobsmenu.client.scene.Nivel;
import com.santipdr.jobsmenu.client.ui.Paleta;
import net.minecraft.client.gui.GuiGraphics;

/** Nivel 7 reconstruido: catacumbas estrechas, nichos y cadenas. */
public final class Catacumba implements Planta {
    private static final int TRAMOS = 18;
    public int tramos(){ return TRAMOS; }

    @Override public void dibujar(GuiGraphics g, Marco m, Nivel n, float luz, float tiempo){
        NuevaEscena.base(g,m,n,luz,TRAMOS,0.70F);
        for(int j=3;j<=15;j+=3){
            float dx=Trazo.profundidad(j,TRAMOS);
            if(dx>5.5F) continue;
            for(int lado:new int[]{-1,1}){
                int cx=Math.round(m.enX(dx,lado*0.88F));
                int suelo=Math.round(m.sueloEn(dx));
                int alto=Math.max(8,(int)(m.h()*dx*0.52F));
                int ancho=Math.max(4,(int)(m.w()*dx*0.055F));
                g.fill(cx-ancho,suelo-alto,cx+ancho,suelo,
                        Paleta.conAlfa(Paleta.mezclar(Paleta.VANO,n.paredBaja,0.22F),0.92F));
                NuevaEscena.arco(g,cx,suelo,ancho,alto/2,1,Paleta.conAlfa(Paleta.iluminar(n.junta,luz),0.58F));
            }
        }
        NuevaEscena.cadena(g,(int)(m.ancho()*0.34F),0,(int)(m.alto()*0.58F),Paleta.conAlfa(n.junta,0.88F),1);
        NuevaEscena.cadena(g,(int)(m.ancho()*0.69F),0,(int)(m.alto()*0.46F),Paleta.conAlfa(n.junta,0.82F),1);
        for(int i=0;i<4;i++){
            int x=(int)(m.ancho()*(0.20F+i*0.20F));
            int y=(int)(m.alto()*(0.48F+(i%2)*0.05F));
            NuevaEscena.glow(g,x,y,Math.max(7,m.ancho()/70),0xFFFFD080,0.46F*luz);
        }
    }

    @Override public void primerPlano(GuiGraphics g, Marco m, Nivel n, float luz, float tiempo){
        int w=Math.max(16,m.ancho()/13);
        g.fill(0,0,w,m.alto(),Paleta.conAlfa(Paleta.VANO,0.70F));
        g.fill(m.ancho()-w,0,m.ancho(),m.alto(),Paleta.conAlfa(Paleta.VANO,0.70F));
    }
}
