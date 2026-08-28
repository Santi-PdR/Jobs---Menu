package com.santipdr.jobsmenu.client.scene.planta;

import com.santipdr.jobsmenu.client.scene.Marco;
import com.santipdr.jobsmenu.client.scene.Nivel;
import com.santipdr.jobsmenu.client.ui.Paleta;
import net.minecraft.client.gui.GuiGraphics;

/** Nivel 5 reconstruido: archivo-biblioteca profundo y silencioso. */
public final class Biblioteca implements Planta {
    private static final int TRAMOS = 15;
    public int tramos(){ return TRAMOS; }

    @Override public void dibujar(GuiGraphics g, Marco m, Nivel n, float luz, float tiempo){
        NuevaEscena.base(g,m,n,luz,TRAMOS,0.58F);
        for(int j=3;j<=12;j+=2){
            float dx=Trazo.profundidad(j,TRAMOS);
            if(dx>5.0F) continue;
            for(int lado:new int[]{-1,1}){
                float frac=lado*0.86F;
                int cx=Math.round(m.enX(dx,frac));
                int suelo=Math.round(m.sueloEn(dx));
                int alto=Math.max(10,(int)(m.h()*dx*0.72F));
                int ancho=Math.max(5,(int)(m.w()*dx*0.09F));
                NuevaEscena.panel(g,cx-ancho,suelo-alto,cx+ancho,suelo,
                        Paleta.iluminar(Paleta.mezclar(n.paredBaja,0xFF2B1B10,0.48F),luz),
                        Paleta.iluminar(n.junta,luz*0.65F),Paleta.iluminar(n.luz,luz),false);
                for(int s=1;s<5;s++){
                    int y=suelo-alto+s*alto/5;
                    g.fill(cx-ancho+1,y,cx+ancho-1,y+1,Paleta.conAlfa(Paleta.VANO,0.55F));
                }
            }
        }
        for(int i=0;i<4;i++){
            int x=(int)(m.ancho()*(0.28F+i*0.15F));
            int y=(int)(m.alto()*(0.42F+(i%2)*0.03F));
            NuevaEscena.glow(g,x,y,Math.max(7,m.ancho()/65),0xFF75C98A,0.42F*luz);
        }
        int cx=Math.round(m.fx());
        int base=Math.round(m.sueloEn(1.0F));
        g.fill(cx-2,(int)(m.techoEn(1.0F)),cx+2,base,Paleta.conAlfa(n.luz,0.13F*luz));
    }

    @Override public void primerPlano(GuiGraphics g, Marco m, Nivel n, float luz, float tiempo){
        int w=Math.max(15,m.ancho()/14);
        g.fill(0,0,w,m.alto(),Paleta.conAlfa(0xFF170E08,0.62F));
        g.fill(m.ancho()-w,0,m.ancho(),m.alto(),Paleta.conAlfa(0xFF170E08,0.62F));
    }
}
