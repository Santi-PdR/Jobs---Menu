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
 * Un nivel son dos cosas: un TIPO DE RECINTO -que decide la geometria, que hay
 * construido y como se dibuja- y una piel de colores y proporciones.
 *
 * Hasta la version anterior era solo lo segundo: los cuatro niveles eran el
 * mismo corredor repintado, y se notaba. Ahora cada uno tiene su
 * {@link Planta}: una sala ancha y baja, una nave con pilares, un pasillo de
 * servicio que dobla y un natatorio con agua. Cambiar de nivel es cambiar de
 * lugar, no de paleta.
 *
 * No hay texturas: todo se dibuja con rectangulos.
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

            // Nivel 0 - El papel mural. La postal del servidor: amarillo que no
            // se apaga. Sala ancha y baja, de las que se cruzan sin pensarlo.
            new Nivel("nivel0", new Sala(),
                    0xFFE6D264, 0xFF9A8630, 0xFF5E5222,
                    0xFF8A7638, 0xFF6E5C2A, 0xFF4C401E,
                    0xFFD5CB9B, 0xFF8E8760,
                    0xFFC9B455, 0xFFFFF7D2, 0xFF0D0B07,
                    // Vista desde una esquina: la fuga esta corrida a la
                    // derecha, la pared izquierda domina el cuadro y la
                    // derecha se va rapido. Lo contrario de un pasillo.
                    0.680F, 0.470F, 0.330F, 0.105F, 0.150F, 0.135F,
                    0.16F, 1.00F),

            // Nivel 1 - El deposito. Hormigon, altura y demasiado espacio para
            // uno solo. Nave con pilares, cerchas y campanas a medio prender.
            new Nivel("nivel1", new Nave(),
                    0xFFB6BAAE, 0xFF74786C, 0xFF4A4E43,
                    0xFF80847A, 0xFF5A5E54, 0xFF3C4036,
                    0xFF9EA298, 0xFF5C6055,
                    0xFF6E7268, 0xFFE8F0FF, 0xFF171B1D,
                    // Mirada desde el suelo: horizonte muy bajo y techo
                    // lejisimos. Se abre casi igual a los dos lados porque es
                    // un volumen, no un corredor.
                    0.505F, 0.720F, 0.235F, 0.255F, 0.300F, 0.098F,
                    0.30F, 0.35F),

            // Nivel 2 - Servicio. Estrecho, caliente, con las canerias a la
            // vista. El unico que sigue siendo un pasillo, y dobla.
            new Nivel("nivel2", new Servicio(),
                    0xFF6E4A28, 0xFF3E2A17, 0xFF241609,
                    0xFF413025, 0xFF2A1F16, 0xFF1B120C,
                    0xFF4A3520, 0xFF2A1C0E,
                    0xFF54371C, 0xFFFFB65E, 0xFF0B0703,
                    // El unico que SI es un pasillo, y se permite serlo:
                    // estrecho, alto y con la fuga descentrada a la izquierda
                    // porque el haz de canerias dobla hacia alla.
                    0.395F, 0.505F, 0.062F, 0.078F, 0.108F, 0.098F,
                    0.22F, 0.75F),

            // Nivel 3 - Las piscinas. Azulejo, agua tibia y un eco que tarda de
            // mas. Aca la mitad de abajo de la escena no es suelo: es agua.
            new Nivel("nivel3", new Natatorio(),
                    0xFFE4EFEC, 0xFFA9C6C2, 0xFF7EA5A2,
                    0xFF63B6B4, 0xFF2F7E82, 0xFF3E9A9A,
                    0xFFE8F2F0, 0xFFB2CCC9,
                    0xFFBEDCD9, 0xFFF4FFFD, 0xFF08171A,
                    // Recinto ancho y de techo bajo, visto desde el borde del
                    // agua: se abre mucho a lo ancho y poco a lo alto, y el
                    // vaso ocupa casi todo el cuadro inferior.
                    0.455F, 0.330F, 0.300F, 0.270F, 0.080F, 0.124F,
                    0.62F, 0.30F),

            // Nivel 4 - La sala. El primer recinto que no es un backroom: piedra
            // calida iluminada por fuego, el guino al lobby del server. Ambar y
            // ocre, boveda de sillares, antorchas y un candil de rueda. El vano
            // del fondo es la boca de un tunel sin luz. 'luz' es el color de la
            // llama (ambar), no de un fluorescente.
            new Nivel("nivel4", new Cripta(),
                    0xFF9A7444, 0xFF5E4227, 0xFF34220F,
                    0xFF6E5432, 0xFF463320, 0xFF2C1C0C,
                    0xFF836540, 0xFF4E3822,
                    0xFF4A3520, 0xFFFFC070, 0xFF0A0603,
                    // Nave abovedada, honda y de buena altura, vista casi de
                    // frente y un punto desde abajo: el candil domina el techo.
                    0.505F, 0.500F, 0.150F, 0.150F, 0.185F, 0.150F,
                    0.20F, 0.55F),

            // Nivel 5 - La biblioteca. Estanterias de madera oscura hasta el
            // techo, lamparas de pantalla verde, polvo. Marron calido y verde
            // apagado; la sala mas quieta de todas. Fondo: un ventanal gris.
            new Nivel("nivel5", new Biblioteca(),
                    0xFF7C6142, 0xFF4E3B26, 0xFF2C2013,
                    0xFF5A4A34, 0xFF3C3020, 0xFF241B10,
                    0xFF6E5C42, 0xFF3E3020,
                    0xFF433624, 0xFFE9D8A0, 0xFF120E08,
                    // Sala honda de techo medio, vista de frente por el pasillo
                    // central entre las dos hileras de estanterias.
                    0.500F, 0.500F, 0.140F, 0.140F, 0.150F, 0.140F,
                    0.14F, 0.45F),

            // Nivel 6 - El invernadero. Vidrio y hierro tomados por las plantas.
            // La luz entra por el techo, blanca y difusa. Verdes humedos, hierro
            // oxidado; el unico recinto iluminado desde arriba y por luz natural.
            new Nivel("nivel6", new Invernadero(),
                    0xFF8A9A6E, 0xFF566040, 0xFF3B3B22,
                    0xFF4C5436, 0xFF343A24, 0xFF20240E,
                    0xFFC8D4B0, 0xFF6E7A50,
                    0xFF7E8C64, 0xFFF2F6E0, 0xFF141810,
                    // Nave ancha y alta de techo a dos aguas, vista de frente por
                    // el sendero central. Se abre a lo alto: el techo es el tema.
                    0.500F, 0.500F, 0.165F, 0.165F, 0.175F, 0.130F,
                    0.18F, 0.60F),

            // Nivel 7 - Las catacumbas. Tunel angosto y bajo de piedra fria, con
            // nichos en las paredes y un farol colgado. Azul-gris, humedo, el
            // pariente oscuro de la sala. Un pasillo, estrecho y arqueado.
            new Nivel("nivel7", new Catacumba(),
                    0xFF6A7078, 0xFF3C4248, 0xFF23282C,
                    0xFF43484C, 0xFF2A2E32, 0xFF181B1E,
                    0xFF565C62, 0xFF303539,
                    0xFF32383E, 0xFFFFDC96, 0xFF06080A,
                    // Pasillo estrecho y alto, fuga apenas descentrada; se baja
                    // un poco hacia el fondo (horizonte alto).
                    0.470F, 0.470F, 0.070F, 0.082F, 0.130F, 0.112F,
                    0.24F, 0.85F),

            // Nivel 8 - La cisterna. Aljibe enorme: columnas naciendo de un agua
            // negra que lo refleja todo, focos sumergidos que la tinen, goteo
            // con eco. Azul profundo y ambar de los focos. Reflejo altisimo.
            new Nivel("nivel8", new Cisterna(),
                    0xFF4A5A6E, 0xFF2A3644, 0xFF17202A,
                    0xFF1E2A38, 0xFF121A24, 0xFF0A0F16,
                    0xFF3A4A5C, 0xFF22303E,
                    0xFF1E2A38, 0xFFFFC878, 0xFF05080C,
                    // Nave ancha y de techo bajo, vista al ras del agua desde una
                    // pasarela; se abre a lo ancho, poco a lo alto.
                    0.500F, 0.500F, 0.190F, 0.190F, 0.092F, 0.118F,
                    0.80F, 0.55F),

            // Nivel 9 - El salon del trono. Sala de audiencias en ruinas: columnas
            // partidas, techo con boquetes, un trono vacio al fondo bajo un haz de
            // luz. Oro apagado y azul de piedra; nada de rojo (es de los Executores).
            new Nivel("nivel9", new Trono(),
                    0xFF6C6A82, 0xFF3E3C50, 0xFF242234,
                    0xFF46445A, 0xFF2C2A3C, 0xFF181628,
                    0xFF56546A, 0xFF302E44,
                    0xFF34324A, 0xFFE8C878, 0xFF0A0812,
                    // Nave alta y honda, vista de frente por la alfombra central;
                    // el trono cae justo en la fuga, al fondo.
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
}
