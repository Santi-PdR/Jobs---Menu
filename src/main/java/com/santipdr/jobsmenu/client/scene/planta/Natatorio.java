package com.santipdr.jobsmenu.client.scene.planta;

import com.santipdr.jobsmenu.client.scene.Marco;
import com.santipdr.jobsmenu.client.scene.Nivel;
import com.santipdr.jobsmenu.client.ui.Paleta;
import net.minecraft.client.gui.GuiGraphics;

/** Nivel 3 reconstruido: natatorio oscuro y monumental. */
public final class Natatorio implements Planta {
    private static final int TRAMOS = 12;
    public int tramos() { return TRAMOS; }

    @Override public void dibujar(GuiGraphics g, Marco m, Nivel n, float luz, float tiempo) {
        NuevaEscena.base(g, m, n, luz, TRAMOS, 0.52F);
        int borde = (int)(m.alto()*0.54F);
        NuevaEscena.agua(g, m, n, luz, tiempo, 0.54F, 11);
        g.fill(0,borde-3,m.ancho(),borde,Paleta.conAlfa(n.techoJunta,0.72F));
        for(int i=0;i<6;i++){
            int x=(int)(m.ancho()*(0.10F+i*0.16F));
            int y=(int)(m.alto()*(0.35F+(i%2)*0.04F));
            NuevaEscena.glow(g,x,y,Math.max(8,m.ancho()/50),0xFF65FFB5,0.55F*luz);
        }
        int cx=Math.round(m.fx());
        int base=Math.round(m.sueloEn(1.0F));
        int aw=Math.max(20,(int)(m.anchoEn(1.0F)*0.34F));
        int ah=Math.max(18,(int)(m.h()*1.00F));
        NuevaEscena.arco(g,cx,base,aw,ah,2,Paleta.conAlfa(Paleta.iluminar(n.paredAlta,luz),0.72F));
        for(int s:new int[]{-1,1}){
            float frac=s*0.78F;
            NuevaEscena.columna(g,m,n,1.05F,frac,0.040F,Paleta.mezclar(n.paredBaja,n.junta,0.40F),luz);
        }
    }

    @Override public void primerPlano(GuiGraphics g, Marco m, Nivel n, float luz, float tiempo) {
        int h=Math.max(10,m.alto()/18);
        g.fillGradient(0,m.alto()-h,m.ancho(),m.alto(),Paleta.conAlfa(n.sueloJunta,0.70F),Paleta.conAlfa(Paleta.VANO,0.96F));
    }

    @Override public float pisoPresencia(){ return 0.72F; }
}
