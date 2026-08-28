package com.santipdr.jobsmenu.client.scene.planta;

import com.santipdr.jobsmenu.client.scene.Marco;
import com.santipdr.jobsmenu.client.scene.Nivel;
import com.santipdr.jobsmenu.client.ui.Paleta;
import net.minecraft.client.gui.GuiGraphics;

/** Nivel 9 reconstruido: salon del trono vertical, ceremonial y arruinado. */
public final class Trono implements Planta {
    private static final int TRAMOS = 12;
    public int tramos(){ return TRAMOS; }

    @Override public void dibujar(GuiGraphics g, Marco m, Nivel n, float luz, float tiempo){
        NuevaEscena.base(g,m,n,luz,TRAMOS,0.66F);
        int cx=Math.round(m.fx());
        int base=Math.round(m.sueloEn(1.0F));
        int beam=Math.max(18,m.ancho()/12);
        g.fillGradient(cx-beam,0,cx+beam,base,
                Paleta.conAlfa(0xFFFFE2A4,0.13F*luz),Paleta.conAlfa(n.luz,0.01F));
        for(int j=3;j<=10;j+=2){
            float dx=Trazo.profundidad(j,11);
            if(dx>4.5F) continue;
            NuevaEscena.columna(g,m,n,dx,-0.74F,0.050F,n.paredBaja,luz);
            NuevaEscena.columna(g,m,n,dx,0.74F,0.050F,n.paredBaja,luz);
        }
        int tw=Math.max(12,(int)(m.anchoEn(1.0F)*0.12F));
        int th=Math.max(22,(int)(m.h()*0.95F));
        int ty=base-th;
        g.fillGradient(cx-tw,ty,cx+tw,base,
                Paleta.iluminar(Paleta.mezclar(n.paredAlta,0xFF5A4A24,0.36F),luz*0.80F),
                Paleta.iluminar(Paleta.mezclar(n.paredBaja,Paleta.VANO,0.32F),luz*0.58F));
        g.fill(cx-tw-5,base-6,cx+tw+5,base,Paleta.conAlfa(n.junta,0.82F));
        g.fill(cx-tw-9,base-3,cx+tw+9,base,Paleta.conAlfa(Paleta.VANO,0.64F));
        NuevaEscena.cadena(g,(int)(m.ancho()*0.24F),0,(int)(m.alto()*0.52F),Paleta.conAlfa(n.junta,0.92F),1);
        NuevaEscena.cadena(g,(int)(m.ancho()*0.76F),0,(int)(m.alto()*0.60F),Paleta.conAlfa(n.junta,0.92F),1);
        for(int i=0;i<4;i++){
            int x=(int)(m.ancho()*(0.20F+i*0.20F));
            int y=(int)(m.alto()*(0.53F+(i%2)*0.05F));
            NuevaEscena.glow(g,x,y,Math.max(8,m.ancho()/65),0xFFFFC46E,0.50F*luz);
        }
    }

    @Override public void primerPlano(GuiGraphics g, Marco m, Nivel n, float luz, float tiempo){
        int w=Math.max(14,m.ancho()/15);
        g.fill(0,0,w,m.alto(),Paleta.conAlfa(Paleta.VANO,0.58F));
        g.fill(m.ancho()-w,0,m.ancho(),m.alto(),Paleta.conAlfa(Paleta.VANO,0.58F));
    }
}
