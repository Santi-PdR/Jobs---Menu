package com.santipdr.jobsmenu.client.scene.planta;

import com.santipdr.jobsmenu.client.scene.Marco;
import com.santipdr.jobsmenu.client.scene.Nivel;

import net.minecraft.client.gui.GuiGraphics;

/**
 * Un tipo de recinto.
 *
 * Cada implementacion dibuja un recinto entero, del fondo hacia la camara,
 * partiendo del lienzo, el encuadre ({@link Marco}) y los colores del nivel.
 * No existe una geometria de pasillo compartida: cada clase es responsable de
 * su composicion, arquitectura, materiales y silueta.
 * Lo que hay antes -la luz, el reloj, la transicion- y lo que hay despues -la
 * presencia, el polvo, la vineta- no es asunto suyo: de eso se ocupa
 * {@code EscenaNivel}.
 */
public interface Planta {

    /**
     * Dibuja el recinto completo.
     *
     * @param marco  encuadre: fuga y tamano de la pared del fondo
     * @param nivel  colores y materiales
     * @param luz    luz disponible ya resuelta, de 0 a 1
     * @param tiempo segundos, para lo que respire o se mueva
     */
    void dibujar(GuiGraphics grafico, Marco marco, Nivel nivel, float luz, float tiempo);

    /**
     * Lo que esta mas cerca que la camara.
     *
     * Se dibuja despues del recinto y antes de la presencia: lo cercano tapa
     * lo lejano, y lo que se aparece vive dentro del recinto, no delante del
     * mostrador. Sin una silueta cercana, cualquier recinto se lee como una
     * maqueta sin escala.
     *
     * Una planta que no tenga primer plano puede no implementarlo, pero
     * deberia tenerlo: es lo que la distingue de un pasillo.
     */
    default void primerPlano(GuiGraphics grafico, Marco marco, Nivel nivel,
                             float luz, float tiempo) {
    }

    /**
     * A que altura del vano del fondo apoya lo que se aparece.
     *
     * En un recinto con el suelo seco la figura apoya casi en la linea del
     * suelo; en el natatorio apoya sobre el borde de la pileta, que esta mas
     * arriba. Devolver 1.0 es apoyar en el suelo del fondo.
     */
    default float pisoPresencia() {
        return 0.94F;
    }
}
