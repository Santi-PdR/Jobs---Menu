package com.santipdr.jobsmenu.client.scene;

import com.santipdr.jobsmenu.client.scene.planta.Planta;
import com.santipdr.jobsmenu.client.scene.planta.Trazo;
import com.santipdr.jobsmenu.client.ui.Paleta;
import com.santipdr.jobsmenu.client.ui.RelojAparicion;
import com.santipdr.jobsmenu.config.ConfigTurno;
import net.minecraft.client.gui.GuiGraphics;

/** Pipeline comun para los fondos reconstruidos desde cero. */
public final class EscenaNivel {
    private EscenaNivel() {}
    private static final int MOTAS = 44;

    public static void dibujar(GuiGraphics grafico, int ancho, int alto) {
        Nivel nivel = RotacionNiveles.actual();
        boolean viva = ConfigTurno.escenaViva();
        boolean destellos = viva && !ConfigTurno.destellosReducidos();
        boolean movimiento = viva && !ConfigTurno.movimientoReducido();
        float tiempo = viva ? (System.currentTimeMillis() % 600_000L) / 1000.0F : 3.0F;
        float penumbra = RelojAparicion.penumbra();
        float luz = brilloFluorescente(tiempo, destellos)
                * (1.0F - 0.55F * penumbra)
                * RotacionNiveles.luzDisponible();
        if (movimiento) luz *= Presencia.sombra();
        luz = Trazo.limitar(luz, 0.0F, 1.0F);

        float fx = ancho * nivel.fugaX;
        float fy = alto * nivel.fugaY;
        if (movimiento) {
            fx += (float)Math.sin(tiempo * 0.13F) * ancho * 0.0036F;
            fy += (float)Math.sin(tiempo * 0.087F + 1.3F) * alto * 0.0030F;
        }
        Marco marco = new Marco(ancho, alto, fx, fy,
                ancho * nivel.semiIzq, ancho * nivel.semiDer,
                ancho * nivel.semiAlto, ancho * nivel.semiBajo);

        Planta planta = nivel.planta;
        planta.dibujar(grafico, marco, nivel, luz, tiempo);
        TratamientoEscena.dibujar(grafico, ancho, alto, nivel, luz, tiempo, movimiento);
        planta.primerPlano(grafico, marco, nivel, luz, tiempo);

        if (movimiento) {
            EventosAmbientales.dibujar(grafico, ancho, alto, nivel, luz);
            Presencia.dibujar(grafico, nivel, marco, luz, planta.pisoPresencia());
            motas(grafico, ancho, alto, tiempo, luz, nivel);
        }
        vineta(grafico, ancho, alto, penumbra);
    }

    private static void motas(GuiGraphics g, int ancho, int alto, float tiempo, float luz, Nivel n) {
        for (int i=0;i<MOTAS;i++) {
            float x=(Trazo.pseudo(i*11)+(float)Math.sin(tiempo*0.17F+i)*0.008F+1.0F)%1.0F;
            float y=(Trazo.pseudo(i*11+1)+tiempo*(0.0018F+Trazo.pseudo(i*11+2)*0.0025F))%1.0F;
            int px=(int)(x*ancho), py=(int)(y*alto);
            float a=(0.05F+Trazo.pseudo(i*11+3)*0.12F)*luz;
            int c=(i%5==0)?n.luz:Paleta.FLUOR;
            g.fill(px,py,px+1,py+1,Paleta.conAlfa(c,a));
        }
    }

    private static void vineta(GuiGraphics g,int ancho,int alto,float penumbra){
        int franja=Math.max(8,ancho/7); float intensidad=0.34F+0.44F*penumbra;
        for(int x=0;x<franja;x+=3){ float t=1.0F-x/(float)franja; int c=Paleta.conAlfa(Paleta.VANO,intensidad*t*t); g.fill(x,0,Math.min(franja,x+3),alto,c); g.fill(Math.max(0,ancho-x-3),0,ancho-x,alto,c); }
        int fv=Math.max(6,alto/8);
        for(int y=0;y<fv;y+=3){ float t=1.0F-y/(float)fv; int c=Paleta.conAlfa(Paleta.VANO,intensidad*0.72F*t*t); g.fill(0,y,ancho,Math.min(fv,y+3),c); g.fill(0,Math.max(0,alto-y-3),ancho,alto-y,c); }
    }

    public static float brilloFluorescente(float tiempo, boolean destellos){
        if(!destellos) return 0.90F;
        float v=0.90F+0.026F*(float)Math.sin(tiempo*1.7F)+0.012F*(float)Math.sin(tiempo*5.9F+1.3F)+0.006F*(float)Math.sin(tiempo*12.7F+0.4F);
        if(Math.floorMod((long)(tiempo*3.0F),97L)==0L) v*=0.70F;
        return Trazo.limitar(v,0.50F,1.0F);
    }
}
