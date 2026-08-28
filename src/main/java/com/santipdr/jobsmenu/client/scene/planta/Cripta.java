package com.santipdr.jobsmenu.client.scene.planta;

import com.santipdr.jobsmenu.client.scene.Marco;
import com.santipdr.jobsmenu.client.scene.Nivel;
import com.santipdr.jobsmenu.client.ui.Paleta;
import net.minecraft.client.gui.GuiGraphics;

/** Nivel 4 reconstruido: cripta ceremonial de piedra y fuego. */
public final class Cripta implements Planta {
    private static final int TRAMOS = 13;
    public int tramos() { return TRAMOS; }

    @Override public void dibujar(GuiGraphics g, Marco m, Nivel n, float luz, float tiempo) {
        NuevaEscena.base(g,m,n,luz,TRAMOS,0.64F);
        int cx=Math.round(m.fx());
        int base=Math.round(m.sueloEn(1.0F));
        int aw=Math.max(18,(int)(m.anchoEn(1.0F)*0.30F));
        int ah=Math.max(22,(int)(m.h()*1.35F));
        NuevaEscena.arco(g,cx,base,aw,ah,3,Paleta.iluminar(Paleta.mezclar(n.junta,n.paredAlta,0.18F),luz));
        for(int j=3;j<=10;j+=2){
            float dx=Trazo.profundidad(j,11);
            if(dx>4.5F) continue;
            NuevaEscena.columna(g,m,n,dx,-0.78F,0.045F,n.paredBaja,luz);
            NuevaEscena.columna(g,m,n,dx,0.78F,0.045F,n.paredBaja,luz);
        }
        int yCad=(int)(m.alto()*0.02F);
        NuevaEscena.cadena(g,(int)(m.ancho()*0.18F),yCad,(int)(m.alto()*0.44F),Paleta.conAlfa(n.junta,0.92F),1);
        NuevaEscena.cadena(g,(int)(m.ancho()*0.82F),yCad,(int)(m.alto()*0.39F),Paleta.conAlfa(n.junta,0.92F),1);
        for(int i=0;i<5;i++){
            int x=(int)(m.ancho()*(0.18F+i*0.16F));
            int y=(int)(m.alto()*(0.52F+(i%2)*0.04F));
            NuevaEscena.glow(g,x,y,Math.max(9,m.ancho()/55),0xFFFFB74D,0.68F*luz);
        }
    }

    @Override public void primerPlano(GuiGraphics g, Marco m, Nivel n, float luz, float tiempo){
        int w=Math.max(12,m.ancho()/17);
        g.fill(0,0,w,m.alto(),Paleta.conAlfa(Paleta.VANO,0.52F));
        g.fill(m.ancho()-w,0,m.ancho(),m.alto(),Paleta.conAlfa(Paleta.VANO,0.52F));
    }
}
