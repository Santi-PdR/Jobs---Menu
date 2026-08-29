package com.santipdr.jobsmenu.client.scene;

import com.santipdr.jobsmenu.client.scene.planta.Biblioteca;
import com.santipdr.jobsmenu.client.scene.planta.Catacumba;
import com.santipdr.jobsmenu.client.scene.planta.Cisterna;
import com.santipdr.jobsmenu.client.scene.planta.Cripta;
import com.santipdr.jobsmenu.client.scene.planta.Invernadero;
import com.santipdr.jobsmenu.client.scene.planta.Nave;
import com.santipdr.jobsmenu.client.scene.planta.Natatorio;
import com.santipdr.jobsmenu.client.scene.planta.Planta;
import com.santipdr.jobsmenu.client.scene.planta.Sala;
import com.santipdr.jobsmenu.client.scene.planta.Servicio;
import com.santipdr.jobsmenu.client.scene.planta.Trono;

/**
 * Un nivel del servidor, visto desde donde esta pegado el aviso.
 *
 * Un nivel une una {@link Planta}, una paleta material y una camara para la
 * presencia. La planta decide arquitectura, planos, foco y movimiento; la
 * paleta conserva parentesco entre interfaz, iluminacion y audio del nivel.
 * Las superficies son procedurales: juntas, desgaste, vetas, condensacion y
 * reflejos se pintan en runtime sin texturas externas ni shaders.
 */
public final class Nivel {

    /** Clave del nivel. Sirve de sufijo para las cadenas de idioma. */
    public final String clave;

    /** Que clase de recinto es. Decide toda la geometria. */
    public final Planta planta;

    /** Pared junto al cielorraso, donde pega la luz. */
    public final int paredAlta;

    /** Pared junto al zocalo, donde la luz ya no llega. */
    public final int paredBaja;

    /** Linea que separa un panel de pared del siguiente. */
    public final int junta;

    /** Suelo a los pies del que mira. */
    public final int suelo;

    /** Suelo contra la abertura del fondo. */
    public final int sueloLejos;

    /** Transversales del suelo. */
    public final int sueloJunta;

    /** Placa del cielorraso. */
    public final int techo;

    /** Perfileria que sostiene las placas. */
    public final int techoJunta;

    /** A donde se va todo con la distancia. */
    public final int niebla;

    /** Color de la luminaria de este nivel. */
    public final int luz;

    /** Lo que se ve por la abertura del fondo. Nunca se aclara. */
    public final int fondo;

    // ---- La camara del recinto -------------------------------------------
    // Cuatro semiejes independientes y fuga propia. Es lo que hace que un
    // nivel no sea otro repintado: ver Marco para el detalle de por que dos
    // semiejes no alcanzaban.

    /** Fuga horizontal, en fraccion del ancho de pantalla. */
    public final float fugaX;

    /** Fuga vertical, en fraccion del alto de pantalla. */
    public final float fugaY;

    /** Cuanto se abre el recinto hacia la izquierda, en fraccion del ancho. */
    public final float semiIzq;

    /** Cuanto se abre el recinto hacia la derecha, en fraccion del ancho. */
    public final float semiDer;

    /** Cuanto se abre el recinto hacia arriba, en fraccion del ancho. */
    public final float semiAlto;

    /** Cuanto se abre el recinto hacia abajo, en fraccion del ancho. */
    public final float semiBajo;

    /** Cuanta luz devuelve el suelo, de 0.0 a 1.0. */
    public final float reflejo;

    /** Cantidad de filtraciones en las paredes, de 0.0 a 1.0. */
    public final float humedad;

    private Nivel(String clave, Planta planta, int paredAlta, int paredBaja, int junta,
                  int suelo, int sueloLejos, int sueloJunta,
                  int techo, int techoJunta, int niebla, int luz, int fondo,
                  float fugaX, float fugaY, float semiIzq, float semiDer,
                  float semiAlto, float semiBajo, float reflejo, float humedad) {
        this.clave = clave;
        this.planta = planta;
        this.paredAlta = paredAlta;
        this.paredBaja = paredBaja;
        this.junta = junta;
        this.suelo = suelo;
        this.sueloLejos = sueloLejos;
        this.sueloJunta = sueloJunta;
        this.techo = techo;
        this.techoJunta = techoJunta;
        this.niebla = niebla;
        this.luz = luz;
        this.fondo = fondo;
        this.fugaX = fugaX;
        this.fugaY = fugaY;
        this.semiIzq = semiIzq;
        this.semiDer = semiDer;
        this.semiAlto = semiAlto;
        this.semiBajo = semiBajo;
        this.reflejo = reflejo;
        this.humedad = humedad;
    }

    /**
     * Los niveles por los que rota el fondo del menu, en orden de profundidad.
     * El Nivel 0 va primero porque es donde empieza todo el mundo.
     */
    public static final Nivel[] CATALOGO = new Nivel[] {

            // Nivel 0 - Vestibulo administrativo brutalista: mostrador de
            // atencion, mamparas, archivo y techo suspendido colapsado.
            new Nivel("nivel0", new Sala(),
                    0xFFE6D264, 0xFF9A8630, 0xFF5E5222,
                    0xFF8A7638, 0xFF6E5C2A, 0xFF4C401E,
                    0xFFD5CB9B, 0xFF8E8760,
                    0xFFC9B455, 0xFFFFF7D2, 0xFF0D0B07,
                    // Camara descentrada para la presencia junto a recepcion.
                    0.680F, 0.470F, 0.330F, 0.105F, 0.150F, 0.135F,
                    0.16F, 1.00F),

            // Nivel 1 - Terminal subterranea de carga: darsenas, canon de
            // contenedores, vias diagonales y puente grua.
            new Nivel("nivel1", new Nave(),
                    0xFFB6BAAE, 0xFF74786C, 0xFF4A4E43,
                    0xFF80847A, 0xFF5A5E54, 0xFF3C4036,
                    0xFF9EA298, 0xFF5C6055,
                    0xFF6E7268, 0xFFE8F0FF, 0xFF171B1D,
                    // Horizonte bajo para una presencia empequenecida por la nave.
                    0.505F, 0.720F, 0.235F, 0.255F, 0.300F, 0.098F,
                    0.30F, 0.35F),

            // Nivel 2 - Camara de calderas dominada por un recipiente circular,
            // colectores, manometros, valvula y pasarela de rejilla.
            new Nivel("nivel2", new Servicio(),
                    0xFF6E4A28, 0xFF3E2A17, 0xFF241609,
                    0xFF413025, 0xFF2A1F16, 0xFF1B120C,
                    0xFF4A3520, 0xFF2A1C0E,
                    0xFF54371C, 0xFFFFB65E, 0xFF0B0703,
                    // Camara compacta: la presencia queda entre colector y caldera.
                    0.395F, 0.505F, 0.062F, 0.078F, 0.108F, 0.098F,
                    0.22F, 0.75F),

            // Nivel 3 - Natatorio visto desde plataforma alta: vaso diagonal,
            // torre de salto, graderio, ventanales y reflejos.
            new Nivel("nivel3", new Natatorio(),
                    0xFFE4EFEC, 0xFFA9C6C2, 0xFF7EA5A2,
                    0xFF63B6B4, 0xFF2F7E82, 0xFF3E9A9A,
                    0xFFE8F2F0, 0xFFB2CCC9,
                    0xFFBEDCD9, 0xFFF4FFFD, 0xFF08171A,
                    // Encuadre ancho; la presencia apoya al otro lado del vaso.
                    0.455F, 0.330F, 0.300F, 0.270F, 0.080F, 0.124F,
                    0.62F, 0.30F),

            // Nivel 4 - Rotonda funeraria radial, con capillas perimetrales,
            // oculo, relicario vacio y pocas velas.
            new Nivel("nivel4", new Cripta(),
                    0xFF9A7444, 0xFF5E4227, 0xFF34220F,
                    0xFF6E5432, 0xFF463320, 0xFF2C1C0C,
                    0xFF836540, 0xFF4E3822,
                    0xFF4A3520, 0xFFFFC070, 0xFF0A0603,
                    // Camara casi frontal para una presencia tras el relicario.
                    0.505F, 0.500F, 0.150F, 0.150F, 0.185F, 0.150F,
                    0.20F, 0.55F),

            // Nivel 5 - Archivo circular de tres plantas alrededor de un pozo
            // de lectura y una escalera helicoidal.
            new Nivel("nivel5", new Biblioteca(),
                    0xFF7C6142, 0xFF4E3B26, 0xFF2C2013,
                    0xFF5A4A34, 0xFF3C3020, 0xFF241B10,
                    0xFF6E5C42, 0xFF3E3020,
                    0xFF433624, 0xFFE9D8A0, 0xFF120E08,
                    // Eje central para la aparicion dentro del pozo de lectura.
                    0.500F, 0.500F, 0.140F, 0.140F, 0.150F, 0.140F,
                    0.14F, 0.45F),

            // Nivel 6 - Conservatorio de cupula rota, atravesado por un arbol
            // maduro, raices, vegetacion y condensacion.
            new Nivel("nivel6", new Invernadero(),
                    0xFF8A9A6E, 0xFF566040, 0xFF3B3B22,
                    0xFF4C5436, 0xFF343A24, 0xFF20240E,
                    0xFFC8D4B0, 0xFF6E7A50,
                    0xFF7E8C64, 0xFFF2F6E0, 0xFF141810,
                    // Luz natural alta; la presencia queda detras del tronco.
                    0.500F, 0.500F, 0.165F, 0.165F, 0.175F, 0.130F,
                    0.18F, 0.60F),

            // Nivel 7 - Excavacion funeraria descendente: tres rellanos en
            // zigzag, nichos irregulares y una unica lampara.
            new Nivel("nivel7", new Catacumba(),
                    0xFF6A7078, 0xFF3C4248, 0xFF23282C,
                    0xFF43484C, 0xFF2A2E32, 0xFF181B1E,
                    0xFF565C62, 0xFF303539,
                    0xFF32383E, 0xFFFFDC96, 0xFF06080A,
                    // Camara desplazada al primer rellano del descenso.
                    0.470F, 0.470F, 0.070F, 0.082F, 0.130F, 0.112F,
                    0.24F, 0.85F),

            // Nivel 8 - Pozo hidraulico visto desde arriba: anillos de fabrica,
            // contrafuertes, escalera, bajante y agua negra muy abajo.
            new Nivel("nivel8", new Cisterna(),
                    0xFF4A5A6E, 0xFF2A3644, 0xFF17202A,
                    0xFF1E2A38, 0xFF121A24, 0xFF0A0F16,
                    0xFF3A4A5C, 0xFF22303E,
                    0xFF1E2A38, 0xFFFFC878, 0xFF05080C,
                    // Eje alto para que la presencia parezca hundida en el pozo.
                    0.500F, 0.500F, 0.190F, 0.190F, 0.092F, 0.118F,
                    0.80F, 0.55F),

            // Nivel 9 - Camara ceremonial fracturada: oculo inmenso, abismo,
            // puentes rotos y estrado suspendido.
            new Nivel("nivel9", new Trono(),
                    0xFF6C6A82, 0xFF3E3C50, 0xFF242234,
                    0xFF46445A, 0xFF2C2A3C, 0xFF181628,
                    0xFF56546A, 0xFF302E44,
                    0xFF34324A, 0xFFE8C878, 0xFF0A0812,
                    // Eje ceremonial reservado al estrado y a la presencia.
                    0.500F, 0.500F, 0.150F, 0.150F, 0.185F, 0.140F,
                    0.26F, 0.55F),
    };

    /** Cuantos niveles hay en la rotacion. */
    public static int cantidad() {
        return CATALOGO.length;
    }

    /** Devuelve un nivel del catalogo, dando la vuelta si el indice se pasa. */
    public static Nivel porIndice(int indice) {
        int n = CATALOGO.length;
        return CATALOGO[((indice % n) + n) % n];
    }

    /**
     * El numero del nivel tal como lo lee el ocupante: el que va en el nombre.
     *
     * Sale de la clave ("nivel0" -> 0, "nivel7" -> 7) y no del indice de la
     * rotacion, porque son cosas distintas: la rotacion podria reordenarse o
     * saltear alguno, pero "Nivel 7" siempre es el mismo sitio. La hoja del
     * aviso lo usa para decir en que nivel esta parado el ocupante y cuanto
     * cuesta el siguiente, en vez de mentir siempre "Nivel 0".
     */
    public int numero() {
        int n = 0;
        for (int i = 0; i < this.clave.length(); i++) {
            char c = this.clave.charAt(i);
            if (c >= '0' && c <= '9') {
                n = n * 10 + (c - '0');
            }
        }
        return n;
    }
}
