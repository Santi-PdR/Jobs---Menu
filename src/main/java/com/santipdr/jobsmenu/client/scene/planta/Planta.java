package com.santipdr.jobsmenu.client.scene.planta;

import com.santipdr.jobsmenu.client.scene.Marco;
import com.santipdr.jobsmenu.client.scene.Nivel;

import net.minecraft.client.gui.GuiGraphics;

/**
 * Un tipo de recinto.
 *
 * Hasta la version anterior habia una sola geometria -un corredor- y los
 * cuatro niveles eran esa misma geometria repintada. Se notaba: cambiar de
 * nivel cambiaba el color y nada mas. Un nivel no es una paleta, es un lugar
 * distinto, y un deposito no se parece en nada a un natatorio aunque los dos
 * esten en el mismo edificio.
 *
 * Cada implementacion dibuja su recinto entero, del fondo hacia la camara,
 * partiendo del encuadre comun ({@link Marco}) y de los colores del nivel.
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
     * Cuantos tramos de profundidad tiene el recinto.
     *
     * Un pasillo de servicio se lee en tramos cortos y frecuentes; una nave,
     * en tramos largos y espaciados. Es la diferencia entre caminar por un
     * sitio y cruzarlo.
     */
    int tramos();

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
