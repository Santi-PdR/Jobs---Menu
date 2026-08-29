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

    /** Evita reiniciar relojes y sorteos cuando init() corre por un resize. */
    private static boolean abierto;

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

            // Nivel 4 - La sala de piedra. Fuego y una construccion vieja: la
            // antorcha que prende es lo mas frecuente; la piedra que se asienta,
            // lo mas raro y lo que mas inquieta. El tono se mueve poco: son
            // objetos reconocibles (fuego, hierro, roca), no lechos de ruido.
            new Repertorio(12_000L, 30_000L, new Evento[] {
                    new Evento(SonidosNivel.EV_N4_ANTORCHA, 3.0F, 0.24F, 0.44F, 0.95F, 1.06F),
                    new Evento(SonidosNivel.EV_N4_CADENA, 2.0F, 0.20F, 0.38F, 0.96F, 1.05F),
                    new Evento(SonidosNivel.EV_N4_PIEDRA, 1.2F, 0.22F, 0.40F, 0.94F, 1.04F),
            }),

            // Nivel 5 - La biblioteca. El sitio mas callado: ventanas largas
            // entre sucesos, y los sucesos son sordos. El susurro es lo mas
            // raro y por eso pesa menos. Casi sin variacion de tono: son
            // objetos concretos (papel, madera, un reloj), no ruido.
            new Repertorio(16_000L, 38_000L, new Evento[] {
                    new Evento(SonidosNivel.EV_N5_LIBRO, 3.0F, 0.22F, 0.40F, 0.96F, 1.05F),
                    new Evento(SonidosNivel.EV_N5_RELOJ, 1.6F, 0.18F, 0.34F, 0.98F, 1.02F),
                    new Evento(SonidosNivel.EV_N5_SUSURRO, 1.0F, 0.16F, 0.30F, 0.97F, 1.03F),
            }),

            // Nivel 6 - El invernadero. Vidrio, agua y follaje. La gota es lo
            // mas frecuente; las hojas, lo mas raro. Tono estable: objetos
            // concretos (vidrio, agua, plantas).
            new Repertorio(13_000L, 32_000L, new Evento[] {
                    new Evento(SonidosNivel.EV_N6_GOTA, 3.0F, 0.22F, 0.42F, 0.94F, 1.10F),
                    new Evento(SonidosNivel.EV_N6_VIDRIO, 2.0F, 0.20F, 0.38F, 0.96F, 1.05F),
                    new Evento(SonidosNivel.EV_N6_HOJAS, 1.0F, 0.16F, 0.30F, 0.97F, 1.03F),
            }),

            // Nivel 7 - Las catacumbas. El sitio mas quieto y grave. La gota es
            // lo comun; el viento en el tunel, lo raro y lo que inquieta. Ventanas
            // largas: aca el silencio pesa.
            new Repertorio(15_000L, 40_000L, new Evento[] {
                    new Evento(SonidosNivel.EV_N7_GOTA, 3.0F, 0.22F, 0.42F, 0.94F, 1.10F),
                    new Evento(SonidosNivel.EV_N7_PIEDRA, 1.6F, 0.20F, 0.38F, 0.95F, 1.04F),
                    new Evento(SonidosNivel.EV_N7_VIENTO, 1.0F, 0.16F, 0.30F, 0.98F, 1.02F),
            }),

            // Nivel 8 - La cisterna. Enorme y quieta: gotas al agua con eco
            // larguisimo, y de fondo el chapoteo de algo que no se ve. Ventanas
            // largas; la cola de la sala hace casi todo el trabajo.
            new Repertorio(14_000L, 36_000L, new Evento[] {
                    new Evento(SonidosNivel.EV_N8_GOTA, 3.0F, 0.22F, 0.42F, 0.92F, 1.10F),
                    new Evento(SonidosNivel.EV_N8_COLUMNA, 1.5F, 0.20F, 0.38F, 0.95F, 1.03F),
                    new Evento(SonidosNivel.EV_N8_CHAPOTEO, 1.0F, 0.16F, 0.30F, 0.96F, 1.04F),
            }),

            // Nivel 9 - El salon del trono. Ruinas: el cascote que cae es lo
            // comun; la puerta lejana, lo raro y lo mas grande. Sala alta con
            // cola larga; ventanas medias.
            new Repertorio(13_000L, 34_000L, new Evento[] {
                    new Evento(SonidosNivel.EV_N9_CASCOTE, 3.0F, 0.22F, 0.42F, 0.94F, 1.08F),
                    new Evento(SonidosNivel.EV_N9_ESTANDARTE, 1.8F, 0.18F, 0.34F, 0.96F, 1.04F),
                    new Evento(SonidosNivel.EV_N9_PUERTA, 1.0F, 0.18F, 0.34F, 0.95F, 1.03F),
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

    /**
     * Cual fue el ultimo parpadeo que se sono, para no repetirlo.
     *
     * Guarda el indice y no un booleano porque los parpadeos son varios y hay
     * que poder distinguir uno del siguiente: con una bandera, el segundo
     * chispazo de la misma fase no llegaba a sonar nunca.
     */
    private static int ultimoChispazo = -1;

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
        if (abierto) {
            atender();
            return;
        }
        abierto = true;
        nivelSonando = -1;
        proximoEvento = 0L;
        ultimoChispazo = -1;
        apagonSonado = false;
        encendidoSonado = false;
        presenciaSonada = false;
        reprogramarEvento(RotacionNiveles.indiceActual());
        atender();
    }

    /** Cierra todo lo que este sonando y no deja instancias huerfanas. */
    public static void cerrar() {
        for (CapaAmbiente capa : CAPAS) {
            capa.detenerAhora();
        }
        CAPAS.clear();
        nivelSonando = -1;
        abierto = false;
    }

    /** El SoundEngine se reconstruyo: ninguna instancia anterior es valida. */
    public static void recursosRecargados() {
        cerrar();
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
            case 4:
                return SonidosNivel.AMBIENTE_NIVEL4;
            case 5:
                return SonidosNivel.AMBIENTE_NIVEL5;
            case 6:
                return SonidosNivel.AMBIENTE_NIVEL6;
            case 7:
                return SonidosNivel.AMBIENTE_NIVEL7;
            case 8:
                return SonidosNivel.AMBIENTE_NIVEL8;
            case 9:
                return SonidosNivel.AMBIENTE_NIVEL9;
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
            case 4:
                return SonidosNivel.CARACTER_NIVEL4;
            case 5:
                return SonidosNivel.CARACTER_NIVEL5;
            case 6:
                return SonidosNivel.CARACTER_NIVEL6;
            case 7:
                return SonidosNivel.CARACTER_NIVEL7;
            case 8:
                return SonidosNivel.CARACTER_NIVEL8;
            case 9:
                return SonidosNivel.CARACTER_NIVEL9;
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
            case 4:
                return SonidosNivel.ACTIVIDAD_NIVEL4;
            case 5:
                return SonidosNivel.ACTIVIDAD_NIVEL5;
            case 6:
                return SonidosNivel.ACTIVIDAD_NIVEL6;
            case 7:
                return SonidosNivel.ACTIVIDAD_NIVEL7;
            case 8:
                return SonidosNivel.ACTIVIDAD_NIVEL8;
            case 9:
                return SonidosNivel.ACTIVIDAD_NIVEL9;
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
        // Cada parpadeo que se VE se OYE, y en el mismo fotograma. La imagen y
        // el sonido ya no llevan cada uno su cuenta: los dos preguntan por el
        // mismo chispazo a RotacionNiveles, que es quien tiene la tabla.
        atenderChispazos();

        if (!RotacionNiveles.enTransicion()) {
            apagonSonado = false;
            encendidoSonado = false;
            return;
        }

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
     * Un chasquido por parpadeo, exactamente cuando se ve.
     *
     * Antes esto era un unico titileo disparado al entrar en la ventana de
     * aviso: se veian cuatro bajones de luz y se oia uno solo, y ni siquiera
     * en el momento de ninguno de ellos. El ojo y el oido se contradecian, y
     * una contradiccion asi se nota aunque no se sepa senalar.
     *
     * El volumen sale del peso del parpadeo -un bajon leve suena leve- y el
     * tono sube un poco en los chispazos del corte: ahi el tubo ya no esta
     * dudando, se esta yendo.
     */
    private static void atenderChispazos() {
        int chispazo = RotacionNiveles.chispazoActual();
        if (chispazo < 0) {
            ultimoChispazo = -1;
            return;
        }
        if (chispazo == ultimoChispazo) {
            return;
        }
        ultimoChispazo = chispazo;

        float peso = RotacionNiveles.pesoChispazo(chispazo);
        boolean enCorte = chispazo >= 10;
        float volumen = MezclaAudio.TRANSICION * (0.30F + 0.55F * peso)
                * ConfigTurno.volumenAmbiente();
        float tono = enCorte ? 1.06F : 0.98F;
        MezclaAudio.ambiental(SonidosNivel.NIVEL_TITILEO, volumen, tono);
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
     * EL SESGO ESTABA AL REVES
     *
     * Aca habia un error que llevaba varias versiones sin detectarse porque el
     * comentario decia lo correcto y el codigo hacia lo contrario. La linea era
     * "sesgo = sesgo * sesgo" sobre una tirada uniforme entre 0 y 1, y elevar
     * al cuadrado un numero menor que uno lo ACERCA A CERO: el 75 % de las
     * tiradas caia por debajo de 0.5. O sea que la espera tendia al minimo de
     * la ventana y los eventos salian mas seguido de lo previsto, justo lo
     * contrario de lo que el comentario prometia.
     *
     * Ese era, en buena medida, el motivo de que el ambiente sonara mas lleno
     * de lo que se habia disenado. La correccion es una raiz en vez de un
     * cuadrado.
     *
     * EL SILENCIO COMO DECISION
     *
     * Ademas de esperar mas, ahora el sitio se calla a proposito. Una de cada
     * cinco veces se abre un respiro de entre dos y cuatro ventanas enteras sin
     * un solo suceso. No es una pausa mas larga: es un hueco que se nota, y
     * hace falta que se note.
     *
     * El motivo es que un edificio que produce un ruido cada veinte segundos,
     * puntualmente, deja de ser un edificio y pasa a ser un metronomo. El oido
     * aprende el intervalo en tres o cuatro repeticiones y a partir de ahi ya
     * no escucha: espera. Un silencio largo rompe esa cuenta y devuelve al
     * siguiente suceso todo el peso que tenia el primero.
     */
    private static void reprogramarEvento(int nivel) {
        Repertorio repertorio = REPERTORIOS[Math.floorMod(nivel, REPERTORIOS.length)];

        // Raiz, no cuadrado: ahora si la tirada tiende al final de la ventana
        // y las pausas largas son mas frecuentes que las cortas.
        float sesgo = (float) Math.sqrt(AZAR.nextFloat());
        long ventana = repertorio.esperaMax() - repertorio.esperaMin();
        long espera = repertorio.esperaMin() + (long) (sesgo * ventana);

        if (AZAR.nextInt(5) == 0) {
            // El respiro. Entre dos y cuatro ventanas sin nada.
            espera += ventana * (2 + AZAR.nextInt(3));
        }
        proximoEvento = System.currentTimeMillis() + espera;
    }

    private static float mezclar(float minimo, float maximo) {
        return minimo + AZAR.nextFloat() * (maximo - minimo);
    }
}
