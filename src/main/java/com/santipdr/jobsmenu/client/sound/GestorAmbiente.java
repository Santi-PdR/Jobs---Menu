package com.santipdr.jobsmenu.client.sound;

import com.santipdr.jobsmenu.client.scene.Nivel;
import com.santipdr.jobsmenu.client.scene.Presencia;
import com.santipdr.jobsmenu.client.scene.RotacionNiveles;
import com.santipdr.jobsmenu.config.ConfigTurno;

import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * El que decide que se escucha en el pasillo y cuando.
 *
 * COMO SE CONSTRUYE UN AMBIENTE
 *
 * Un unico archivo en bucle nunca suena a lugar: suena a archivo en bucle. Lo
 * que hay aca son tres capas que corren a velocidades distintas y que nunca
 * vuelven a alinearse igual:
 *
 *   1. BASE. Un bucle largo por nivel (18 a 24 s, ninguno igual a otro), con
 *      su volumen respirando en ciclos de mas de un minuto: la nota del sitio.
 *   2. CARACTER. Una segunda cama, tambien continua y tambien siempre
 *      encendida, con lo que se mueve: el aire corriendo, el agua del vaso, la
 *      circulacion de las canerias. Dura distinto que la base a proposito
 *      (25 a 36 s), asi que las dos nunca se vuelven a alinear igual.
 *   3. ACTIVIDAD. Una tercera cama, la mas larga (45 a 59 s) y casi siempre en
 *      silencio: cada tanto ocurre algo LEJOS -la chapa del techo, un azulejo
 *      en el fondo del vaso, algo que cae dos plantas mas abajo-. Las camas
 *      continuas evitan el silencio pero se vuelven mobiliario; lo que sostiene
 *      un sitio durante diez minutos es que cada tanto pase algo. Va en bucle
 *      y no como evento para solaparse con lo demas en vez de esperar turno.
 *      Las tres las lleva CapaAmbiente, que cambia de comportamiento segun el
 *      papel: en el apagon la actividad se queda casi entera y las otras dos
 *      se caen, porque el edificio no deja de moverse por un corte de luz.
 *   4. EVENTOS. Tres o cuatro sonidos sueltos por nivel que se disparan con
 *      separacion aleatoria dentro de una ventana, cada uno con su peso, su
 *      volumen y su tono variables. Los lleva esta clase.
 *   5. TRANSICION Y FIGURA. Sonidos puntuales enganchados a lo que pasa en
 *      pantalla. Los llama la pantalla, no el reloj.
 *
 * Con la separacion aleatoria, el volumen aleatorio y el tono aleatorio, dos
 * sesiones distintas del mismo nivel no suenan igual aunque los archivos sean
 * los mismos.
 *
 * Todo el estado vive aca y no en la pantalla: la pantalla se recrea cada vez
 * que se cambia el tamano de la ventana, y con ella se perderian los relojes.
 */
public final class GestorAmbiente {

    private GestorAmbiente() {
    }

    /**
     * Un sonido ocasional del nivel.
     *
     * @param sonido  que se escucha
     * @param peso    cuanto mas probable es que salga este y no otro del nivel
     * @param volMin  volumen minimo
     * @param volMax  volumen maximo
     * @param tonoMin tono minimo
     * @param tonoMax tono maximo
     */
    private record Evento(RegistryObject<SoundEvent> sonido, float peso,
                          float volMin, float volMax, float tonoMin, float tonoMax) {
    }

    /**
     * El repertorio de cada nivel y cada cuanto pasa algo.
     *
     * Las ventanas son largas. Un evento cada quince segundos deja de ser un
     * evento y pasa a ser parte del ambiente; uno cada cuarenta y cinco todavia
     * sorprende. El deposito es el que menos habla porque el silencio ES el
     * deposito, y las piscinas el que mas porque un natatorio vacio nunca se
     * queda del todo callado.
     */
    private record Repertorio(long esperaMin, long esperaMax, Evento[] eventos) {
    }

    private static final Repertorio[] REPERTORIOS = new Repertorio[] {

            // Nivel 0 - Administracion. Instalacion electrica y edificio.
            new Repertorio(14_000L, 34_000L, new Evento[] {
                    new Evento(SonidosNivel.EV_N0_TUBO, 3.0F, 0.30F, 0.55F, 0.96F, 1.05F),
                    new Evento(SonidosNivel.EV_N0_PLACA, 2.0F, 0.22F, 0.42F, 0.92F, 1.10F),
                    new Evento(SonidosNivel.EV_N0_PUERTA, 1.0F, 0.18F, 0.34F, 0.90F, 1.04F),
            }),

            // Nivel 1 - Deposito. Habla poco y siempre desde lejos.
            new Repertorio(22_000L, 52_000L, new Evento[] {
                    new Evento(SonidosNivel.EV_N1_ESTRUCTURA, 3.0F, 0.24F, 0.44F, 0.94F, 1.03F),
                    new Evento(SonidosNivel.EV_N1_LEJANO, 2.5F, 0.18F, 0.36F, 0.90F, 1.06F),
                    new Evento(SonidosNivel.EV_N1_METAL, 1.0F, 0.20F, 0.40F, 0.92F, 1.08F),
            }),

            // Nivel 2 - Servicio. Es el nivel de las maquinas: pasa cosas seguido.
            new Repertorio(9_000L, 24_000L, new Evento[] {
                    new Evento(SonidosNivel.EV_N2_GOTEO, 3.0F, 0.26F, 0.48F, 0.92F, 1.12F),
                    new Evento(SonidosNivel.EV_N2_CANO, 2.0F, 0.28F, 0.52F, 0.88F, 1.10F),
                    new Evento(SonidosNivel.EV_N2_VALVULA, 1.5F, 0.22F, 0.42F, 0.94F, 1.06F),
            }),

            // Nivel 3 - Piscinas. El repertorio mas amplio y el mas repartido.
            new Repertorio(11_000L, 28_000L, new Evento[] {
                    new Evento(SonidosNivel.EV_N3_GOTA, 3.0F, 0.26F, 0.50F, 0.90F, 1.14F),
                    new Evento(SonidosNivel.EV_N3_ONDAS, 2.5F, 0.24F, 0.46F, 0.95F, 1.05F),
                    new Evento(SonidosNivel.EV_N3_VENTILACION, 1.5F, 0.20F, 0.38F, 0.96F, 1.04F),
                    new Evento(SonidosNivel.EV_N3_LEJANO, 1.0F, 0.18F, 0.34F, 0.92F, 1.08F),
            }),
    };

    private static final Random AZAR = new Random();

    /** Las camas que estan sonando. Dos por nivel, y hasta cuatro en el cambio. */
    private static final List<CapaAmbiente> CAPAS = new ArrayList<>();

    /** Cuando toca el proximo evento, en milisegundos del reloj del sistema. */
    private static long proximoEvento;

    /** El ultimo evento que salio, para no repetirlo dos veces seguidas. */
    private static RegistryObject<SoundEvent> ultimoEvento;

    /** El nivel que sonaba la ultima vez que se miro. */
    private static int nivelSonando = -1;

    /** Si ya se disparo el aviso previo de la transicion en curso. */
    private static boolean titileoSonado;

    /** Si ya se disparo el apagon de la transicion en curso. */
    private static boolean apagonSonado;

    /** Si ya se disparo el encendido del nivel nuevo. */
    private static boolean encendidoSonado;

    /** Si la figura de este ciclo ya se anuncio. */
    private static boolean presenciaSonada;

    /**
     * Arranca el ambiente al abrirse el menu.
     *
     * Es idempotente: si la pantalla se reconstruye porque cambio el tamano de
     * la ventana, no se apila una segunda copia del mismo bucle.
     */
    public static void abrir() {
        nivelSonando = -1;
        proximoEvento = System.currentTimeMillis() + 6_000L;
        titileoSonado = false;
        apagonSonado = false;
        encendidoSonado = false;
        presenciaSonada = false;
        atender();
    }

    /** Cierra todo lo que este sonando. Las capas se apagan solas, con caida. */
    public static void cerrar() {
        CAPAS.clear();
        nivelSonando = -1;
    }

    /**
     * Un paso del reloj. Se llama una vez por fotograma desde la pantalla.
     *
     * Se hace por fotograma y no por tick porque la pantalla de titulo no
     * garantiza ticks regulares, y todo lo de aca se mide contra el reloj del
     * sistema, asi que la frecuencia de llamada no cambia el resultado.
     */
    public static void atender() {
        Minecraft cliente = Minecraft.getInstance();
        if (!(cliente.screen instanceof com.santipdr.jobsmenu.client.screen.PantallaNivel)) {
            return;
        }

        CAPAS.removeIf(CapaAmbiente::agotada);

        if (!ConfigTurno.sonidoAmbiente()) {
            return;
        }

        int nivel = RotacionNiveles.indiceActual();
        asegurarCamas(nivel);
        atenderTransicion();
        atenderPresencia();
        atenderEventos(nivel);
    }

    /**
     * Se asegura de que las tres camas del nivel a la vista esten
     * sonando, y las levanta si falta alguna.
     *
     * Se comprueban por separado porque no nacen juntas necesariamente: si el
     * jugador desactiva y reactiva el sonido de ambiente en medio de un cambio
     * de nivel, puede quedar una viva y la otra apagada.
     */
    private static void asegurarCamas(int nivel) {
        boolean cambio = nivelSonando != nivel;

        asegurarCama(nivel, CapaAmbiente.Papel.BASE, baseDe(nivel));
        asegurarCama(nivel, CapaAmbiente.Papel.CARACTER, caracterDe(nivel));
        asegurarCama(nivel, CapaAmbiente.Papel.ACTIVIDAD, actividadDe(nivel));

        if (cambio) {
            nivelSonando = nivel;
            reprogramarEvento(nivel);
        }
    }

    private static void asegurarCama(int nivel, CapaAmbiente.Papel papel,
                                     RegistryObject<SoundEvent> sonido) {
        for (CapaAmbiente capa : CAPAS) {
            if (capa.nivel() == nivel && capa.papel() == papel) {
                return;
            }
        }

        CapaAmbiente capa = new CapaAmbiente(sonido.get(), nivel, papel);
        CAPAS.add(capa);
        Minecraft.getInstance().getSoundManager().play(capa);
    }

    private static RegistryObject<SoundEvent> baseDe(int nivel) {
        switch (nivel) {
            case 1:
                return SonidosNivel.AMBIENTE_NIVEL1;
            case 2:
                return SonidosNivel.AMBIENTE_NIVEL2;
            case 3:
                return SonidosNivel.AMBIENTE_NIVEL3;
            default:
                return SonidosNivel.AMBIENTE_NIVEL0;
        }
    }

    private static RegistryObject<SoundEvent> caracterDe(int nivel) {
        switch (nivel) {
            case 1:
                return SonidosNivel.CARACTER_NIVEL1;
            case 2:
                return SonidosNivel.CARACTER_NIVEL2;
            case 3:
                return SonidosNivel.CARACTER_NIVEL3;
            default:
                return SonidosNivel.CARACTER_NIVEL0;
        }
    }

    private static RegistryObject<SoundEvent> actividadDe(int nivel) {
        switch (nivel) {
            case 1:
                return SonidosNivel.ACTIVIDAD_NIVEL1;
            case 2:
                return SonidosNivel.ACTIVIDAD_NIVEL2;
            case 3:
                return SonidosNivel.ACTIVIDAD_NIVEL3;
            default:
                return SonidosNivel.ACTIVIDAD_NIVEL0;
        }
    }

    /**
     * Los tres golpes de la transicion, cada uno en su momento exacto.
     *
     * El orden importa y es el que se ve en pantalla: primero el tubo duda,
     * despues se corta, y al final el nivel nuevo prende. El titileo se dispara
     * antes de que la luz empiece a caer, porque un aviso que llega junto con
     * la cosa avisada no avisa nada.
     */
    private static void atenderTransicion() {
        if (!RotacionNiveles.enTransicion()) {
            apagonSonado = false;
            encendidoSonado = false;

            // El titileo va en el ultimo tramo de la estancia, antes de que la
            // luz empiece a caer: un aviso que llega junto con la cosa avisada
            // no avisa nada.
            if (RotacionNiveles.porTransicionar()) {
                if (!titileoSonado) {
                    titileoSonado = true;
                    MezclaAudio.ambiental(SonidosNivel.NIVEL_TITILEO,
                            MezclaAudio.TRANSICION * 0.45F * ConfigTurno.volumenAmbiente(), 1.0F);
                }
            } else {
                titileoSonado = false;
            }
            return;
        }
        titileoSonado = false;

        float avance = RotacionNiveles.avanceTransicion();

        if (!apagonSonado) {
            apagonSonado = true;
            MezclaAudio.ambiental(SonidosNivel.NIVEL_APAGON,
                    MezclaAudio.TRANSICION * ConfigTurno.volumenAmbiente(), 1.0F);
        }

        // El encendido entra apenas empieza a volver la luz.
        if (!encendidoSonado && avance >= RotacionNiveles.repartoApagado()) {
            encendidoSonado = true;
            MezclaAudio.ambiental(SonidosNivel.NIVEL_ENCENDIDO,
                    MezclaAudio.TRANSICION * 0.90F * ConfigTurno.volumenAmbiente(), 1.0F);
        }
    }

    /**
     * El sonido de la figura, una sola vez por manifestacion.
     *
     * El volumen y el tono salen del modo, no son fijos. Si suena siempre
     * igual, el oido aprende el sonido y deja de creerle a la imagen: lo que
     * se ve cambia y lo que se escucha no. Cada modo suena como se ve.
     */
    private static void atenderPresencia() {
        boolean hay = Presencia.presente();
        if (hay && !presenciaSonada) {
            presenciaSonada = true;
            int modo = Presencia.modo();

            // Corte: seco y un punto mas agudo, como el sonido se recorta con
            // la imagen. Sumergida: apagado y grave, porque viene del agua y
            // no de algo parado ahi. Doble: mas cuerpo, son dos.
            float volumen = MezclaAudio.FIGURA;
            float tono = 1.0F;
            if (modo == Presencia.MODO_CORTE) {
                volumen *= 0.82F;
                tono = 1.045F;
            } else if (modo == Presencia.MODO_SUMERGIDA) {
                volumen *= 0.66F;
                tono = 0.94F;
            } else if (modo == Presencia.MODO_DOBLE) {
                volumen *= 1.12F;
                tono = 0.978F;
            }

            MezclaAudio.ambiental(SonidosNivel.FIGURA_PRESENCIA,
                    volumen * ConfigTurno.volumenAmbiente(), tono);
        } else if (!hay && Presencia.avance() < 0.0F) {
            presenciaSonada = false;
        }
    }

    /** Si llego la hora, saca un evento del repertorio del nivel y lo suena. */
    private static void atenderEventos(int nivel) {
        long ahora = System.currentTimeMillis();
        if (ahora < proximoEvento) {
            return;
        }

        // Durante el apagon no suena nada del nivel: no hay nivel.
        if (RotacionNiveles.luzDisponible() < 0.25F) {
            proximoEvento = ahora + 2_000L;
            return;
        }

        Repertorio repertorio = REPERTORIOS[Math.floorMod(nivel, REPERTORIOS.length)];
        Evento elegido = sortear(repertorio.eventos());
        if (elegido != null) {
            float volumen = mezclar(elegido.volMin(), elegido.volMax())
                    * MezclaAudio.EVENTO * ConfigTurno.volumenAmbiente();

            // Con algo al fondo, hasta los eventos se retiran.
            volumen *= 1.0F - (1.0F - MezclaAudio.AGACHE_FIGURA) * Presencia.visibilidad();

            MezclaAudio.ambiental(elegido.sonido(), volumen,
                    mezclar(elegido.tonoMin(), elegido.tonoMax()));
            ultimoEvento = elegido.sonido();
        }
        reprogramarEvento(nivel);
    }

    /**
     * Sorteo por peso, descartando el ultimo que sono.
     *
     * Evitar la repeticion inmediata importa mas que la distribucion exacta:
     * dos gotas identicas seguidas es lo unico que hace que el jugador piense
     * "esto es un archivo de sonido".
     */
    private static Evento sortear(Evento[] eventos) {
        float total = 0.0F;
        for (Evento evento : eventos) {
            if (evento.sonido() != ultimoEvento || eventos.length == 1) {
                total += evento.peso();
            }
        }
        if (total <= 0.0F) {
            return eventos.length > 0 ? eventos[0] : null;
        }

        float tirada = AZAR.nextFloat() * total;
        for (Evento evento : eventos) {
            if (evento.sonido() == ultimoEvento && eventos.length > 1) {
                continue;
            }
            tirada -= evento.peso();
            if (tirada <= 0.0F) {
                return evento;
            }
        }
        return eventos[eventos.length - 1];
    }

    /**
     * Fija cuando toca el proximo evento.
     *
     * La espera no es uniforme sino sesgada hacia el final de la ventana: se
     * eleva la tirada al cuadrado. Asi las pausas largas son mas frecuentes que
     * las cortas, que es como se comporta un edificio de verdad, y ademas se
     * evita el goteo constante de eventos que arruina cualquier ambiente.
     */
    private static void reprogramarEvento(int nivel) {
        Repertorio repertorio = REPERTORIOS[Math.floorMod(nivel, REPERTORIOS.length)];
        float sesgo = AZAR.nextFloat();
        sesgo = sesgo * sesgo;
        long espera = repertorio.esperaMin()
                + (long) (sesgo * (repertorio.esperaMax() - repertorio.esperaMin()));

        // Uno de cada seis silencios es el doble de largo. Los huecos raros son
        // lo que mantiene despierto al oido.
        if (AZAR.nextInt(6) == 0) {
            espera *= 2;
        }
        proximoEvento = System.currentTimeMillis() + espera;
    }

    private static float mezclar(float minimo, float maximo) {
        return minimo + AZAR.nextFloat() * (maximo - minimo);
    }
}
