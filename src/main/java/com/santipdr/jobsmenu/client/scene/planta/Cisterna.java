package com.santipdr.jobsmenu.client.scene.planta;

import com.santipdr.jobsmenu.client.scene.Marco;
import com.santipdr.jobsmenu.client.scene.Nivel;
import com.santipdr.jobsmenu.client.ui.Paleta;
import net.minecraft.client.gui.GuiGraphics;

/** Nivel 8 reconstruido: cisterna inmensa de agua negra y columnas. */
public final class Cisterna implements Planta {
    private static final int TRAMOS = 11;
    public int tramos(){ return TRAMOS; }

    @Override public void dibujar(GuiGraphics g, Marco m, Nivel n, float luz, float tiempo){
        NuevaEscena.base(g,m,n,luz,TRAMOS,0.62F);
        NuevaEscena.agua(g,m,n,luz,tiempo,0.49F,14);
        for(int j=3;j<=9;j+=2){
            float dx=Trazo.profundidad(j,10);
            if(dx>4.5F) continue;
            for(float frac:new float[]{-0.72F,-0.24F,0.24F,0.72F}){
                NuevaEscena.columna(g,m,n,dx,frac,0.035F,Paleta.mezclar(n.paredBaja,n.junta,0.35F),luz);
            }
        }
        for(int i=0;i<8;i++){
            int x=(int)(m.ancho()*(0.08F+i*0.12F));
            int y=(int)(m.alto()*(0.57F+(i%3)*0.055F));
            NuevaEscena.glow(g,x,y,Math.max(8,m.ancho()/60),0xFF58FF85,0.58F*luz);
        }
        int pasarelaY=(int)(m.alto()*0.66F);
        int ancho=(int)(m.ancho()*0.14F);
        int cx=(int)(m.ancho()*0.50F);
        g.fillGradient(cx-ancho,pasarelaY,cx+ancho,m.alto(),
                Paleta.conAlfa(Paleta.mezclar(n.junta,n.suelo,0.35F),0.74F),Paleta.conAlfa(Paleta.VANO,0.95F));
        for(int y=pasarelaY;y<m.alto();y+=Math.max(5,m.alto()/28)){
            g.fill(cx-ancho,y,cx+ancho,y+1,Paleta.conAlfa(n.techoJunta,0.28F));
        }
    }

    @Override public void primerPlano(GuiGraphics g, Marco m, Nivel n, float luz, float tiempo){
        int h=Math.max(8,m.alto()/22);
        g.fill(0,m.alto()-h,m.ancho(),m.alto(),Paleta.conAlfa(Paleta.VANO,0.80F));
    }

    @Override public float pisoPresencia(){ return 0.74F; }
}
